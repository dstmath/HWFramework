package com.android.server.pm.auth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageParser.Package;
import android.os.Process;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import com.android.server.pm.auth.processor.HwCertificationProcessor;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import com.android.server.security.trustcircle.IOTController;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HwCertificationManager {
    public static final String TAG = "HwCertificationManager";
    private static Context mContext = null;
    public static final boolean mHasFeature = true;
    private static HwCertificationManager mInstance;
    private ConcurrentHashMap<String, HwCertification> mCertMap;
    private Object mLock;
    BroadcastReceiver mReceiver;
    private boolean mSystemReady;
    private final Runnable mWriteStateRunnable;
    private boolean mloaded;

    private HwCertificationManager() {
        this.mCertMap = new ConcurrentHashMap();
        this.mLock = new Object();
        this.mloaded = false;
        this.mSystemReady = false;
        this.mWriteStateRunnable = new Runnable() {
            public void run() {
                List<HwCertification> data = new ArrayList();
                data.addAll(HwCertificationManager.this.mCertMap.values());
                new HwCertXmlHandler().updateHwCert(data);
            }
        };
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    HwCertificationManager.this.handlePackagesChanged(intent);
                }
            }
        };
        readHwCertXml();
    }

    private synchronized void readHwCertXml() {
        if (!this.mloaded) {
            long start = System.currentTimeMillis();
            HwCertXmlHandler handler = new HwCertXmlHandler();
            this.mCertMap.clear();
            handler.readHwCertXml(this.mCertMap);
            this.mloaded = mHasFeature;
            long end = System.currentTimeMillis();
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i(TAG, "readHwCertXml  spend time:" + (end - start) + " ms");
            }
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i(TAG, "readHwCertXml  mCertMap size:" + this.mCertMap.size());
            }
        }
    }

    private void scheduleWriteStateLocked() {
        new Thread(this.mWriteStateRunnable).start();
    }

    private synchronized HwCertification parseAndVerify(Package pkg) {
        HwCertification cert = new HwCertification();
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i(TAG, "basecodePath:" + pkg.baseCodePath);
        }
        HwCertificationProcessor hwCertificationProcessor = new HwCertificationProcessor();
        try {
            hwCertificationProcessor.createZipFile(pkg.baseCodePath);
            if (!hwCertificationProcessor.readCert(pkg.baseCodePath, cert)) {
                HwAuthLogger.e(TAG, "read cert failed");
                cert.resetZipFile();
                hwCertificationProcessor.releaseZipFileResource();
                return null;
            } else if (!hwCertificationProcessor.parserCert(cert)) {
                HwAuthLogger.e(TAG, "parse cert failed");
                cert.resetZipFile();
                hwCertificationProcessor.releaseZipFileResource();
                return null;
            } else if (hwCertificationProcessor.verifyCert(pkg, cert)) {
                cert.resetZipFile();
                hwCertificationProcessor.releaseZipFileResource();
                return cert;
            } else {
                HwAuthLogger.e(TAG, "verify cert failed");
                cert.resetZipFile();
                hwCertificationProcessor.releaseZipFileResource();
                return null;
            }
        } catch (Throwable th) {
            cert.resetZipFile();
            hwCertificationProcessor.releaseZipFileResource();
        }
    }

    private void addHwPermission(Package pkg, HwCertification cert) {
        if (pkg.requestedPermissions != null) {
            for (String perm : cert.getPermissionList()) {
                if (!pkg.requestedPermissions.contains(perm)) {
                    pkg.requestedPermissions.add(perm);
                }
            }
        }
    }

    public static void initialize(Context ctx) {
        mContext = ctx;
    }

    public static boolean isInitialized() {
        return mContext != null ? mHasFeature : false;
    }

    public static synchronized HwCertificationManager getIntance() {
        synchronized (HwCertificationManager.class) {
            if (mContext == null) {
                throw new IllegalArgumentException("Impossible to get the instance. This class must be initialized before");
            }
            int uid = Process.myUid();
            if (uid == IOTController.TYPE_MASTER || uid == HwNetworkPropertyChecker.HW_DEFAULT_REEVALUATE_DELAY_MS || uid == 0) {
                if (mInstance == null) {
                    mInstance = new HwCertificationManager();
                }
                HwCertificationManager hwCertificationManager = mInstance;
                return hwCertificationManager;
            }
            HwAuthLogger.e(TAG, "getIntance from uid:" + uid + ",not system.return null");
            return null;
        }
    }

    public Context getContext() {
        return mContext;
    }

    public static boolean isSupportHwCertification(Package pkg) {
        if (pkg == null || pkg.requestedPermissions == null) {
            return false;
        }
        return !pkg.requestedPermissions.contains("com.huawei.permission.sec.MDM") ? pkg.requestedPermissions.contains("com.huawei.permission.sec.MDM.v2") : mHasFeature;
    }

    public synchronized boolean checkHwCertification(Package pkg) {
        synchronized (this.mLock) {
            long start = System.currentTimeMillis();
            HwCertification hwCertification = null;
            try {
                hwCertification = parseAndVerify(pkg);
                if (hwCertification != null) {
                    this.mCertMap.put(hwCertification.getPackageName().trim(), hwCertification);
                    addHwPermission(pkg, hwCertification);
                    scheduleWriteStateLocked();
                    end = System.currentTimeMillis();
                    if (HwAuthLogger.getHWFLOW()) {
                        HwAuthLogger.i(TAG, "check" + pkg.packageName + "HwCertification spend time:" + (end - start) + " ms");
                    }
                    return mHasFeature;
                }
                HwAuthLogger.e(TAG, "check HwCertification error, cert is null!");
                end = System.currentTimeMillis();
                if (HwAuthLogger.getHWFLOW()) {
                    HwAuthLogger.i(TAG, "check" + pkg.packageName + "HwCertification spend time:" + (end - start) + " ms");
                }
                return false;
            } catch (RuntimeException e) {
                HwAuthLogger.e(TAG, "check HwCertification error: RuntimeException!");
                end = System.currentTimeMillis();
                if (HwAuthLogger.getHWFLOW()) {
                    HwAuthLogger.i(TAG, "check" + pkg.packageName + "HwCertification spend time:" + (end - start) + " ms");
                }
                return false;
            } catch (Exception e2) {
                if (hwCertification != null) {
                    hwCertification.setPermissionList(new ArrayList());
                }
                HwAuthLogger.e(TAG, "check HwCertification error!");
                end = System.currentTimeMillis();
                if (HwAuthLogger.getHWFLOW()) {
                    HwAuthLogger.i(TAG, "check" + pkg.packageName + "HwCertification spend time:" + (end - start) + " ms");
                }
                return false;
            } catch (Throwable th) {
                end = System.currentTimeMillis();
                if (HwAuthLogger.getHWFLOW()) {
                    long end;
                    HwAuthLogger.i(TAG, "check" + pkg.packageName + "HwCertification spend time:" + (end - start) + " ms");
                }
            }
        }
    }

    public boolean isSystemReady() {
        return this.mSystemReady;
    }

    public static boolean hasFeature() {
        return mHasFeature;
    }

    public void systemReady() {
        this.mSystemReady = mHasFeature;
        try {
            removeNotExist();
        } catch (Exception e) {
        }
        resigterBroadcastReceiver();
    }

    private void removeNotExist() {
        List<String> pkgNameList = new ArrayList();
        for (String pkgName : this.mCertMap.keySet()) {
            if (!Utils.isPackageInstalled(pkgName, mContext)) {
                pkgNameList.add(pkgName);
            }
        }
        for (int i = 0; i < pkgNameList.size(); i++) {
            if (this.mCertMap.get(pkgNameList.get(i)) != null) {
                this.mCertMap.remove(pkgNameList.get(i));
                if (HwAuthLogger.getHWDEBUG()) {
                    HwAuthLogger.d(TAG, "package:" + ((String) pkgNameList.get(i)) + " not installed,removed from the cert list xml");
                }
            }
        }
        scheduleWriteStateLocked();
    }

    private void removeExistedCert(Package pkg) {
        if (HwAuthLogger.getHWDEBUG()) {
            HwAuthLogger.d(TAG, "removeExistedCert" + pkg.packageName);
        }
        if (this.mCertMap.get(pkg.packageName) != null) {
            this.mCertMap.remove(pkg.packageName);
            if (HwAuthLogger.getHWDEBUG()) {
                HwAuthLogger.d(TAG, "package:" + pkg.packageName + " installed,removed from the cert list xml");
            }
            scheduleWriteStateLocked();
        }
    }

    public void cleanUp(Package pkg) {
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i(TAG, "clean up the cert list xml");
        }
        removeExistedCert(pkg);
    }

    public void cleanUp() {
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i(TAG, "removeNotExist,clean up the cert list xml");
        }
        removeNotExist();
    }

    private void resigterBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme(ControlScope.PACKAGE_ELEMENT_KEY);
        mContext.registerReceiver(this.mReceiver, filter);
    }

    private void handlePackagesChanged(Intent intent) {
        if (intent.getData() != null && intent.getAction() != null) {
            String action = intent.getAction();
            String packageName = intent.getData().getSchemeSpecificPart();
            if ("android.intent.action.PACKAGE_REMOVED".equals(action) && !intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                onPackageRemoved(packageName);
            }
        }
    }

    private void onPackageRemoved(String packageName) {
        synchronized (this.mLock) {
            if (packageName != null) {
                if (this.mCertMap.containsKey(packageName)) {
                    try {
                        if (Utils.isPackageInstalled(packageName, mContext)) {
                            HwAuthLogger.w(TAG, "[package]:" + packageName + " is exist in the package");
                            return;
                        }
                        this.mCertMap.remove(packageName);
                        scheduleWriteStateLocked();
                        if (HwAuthLogger.getHWFLOW()) {
                            HwAuthLogger.i(TAG, "[package]:" + packageName + ",remove from the cert list xml");
                        }
                    } catch (Exception ex) {
                        HwAuthLogger.e(TAG, "onPackageRemoved error!", ex);
                    }
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean getHwCertificationPermission(boolean allowed, Package pkg, String perm) {
        if (allowed || pkg == null || !this.mCertMap.containsKey(pkg.packageName)) {
            return allowed;
        }
        List<String> permissions = ((HwCertification) this.mCertMap.get(pkg.packageName)).getPermissionList();
        if (permissions == null || !permissions.contains(perm) || pkg.requestedPermissions == null || !pkg.requestedPermissions.contains(perm)) {
            return allowed;
        }
        if (HwAuthLogger.getHWDEBUG()) {
            HwAuthLogger.i(TAG, "[package]:" + pkg.packageName + ",perm:" + perm);
        }
        return mHasFeature;
    }

    public int getHwCertificateType(String packageName) {
        HwCertification cert = (HwCertification) this.mCertMap.get(packageName);
        if (cert == null) {
            if (HwAuthLogger.getHWDEBUG()) {
                HwAuthLogger.i(TAG, "getHwCertificateType: cert is null, and pkg name is " + packageName);
            }
            return 5;
        }
        String certificate = cert.getCertificate();
        if (certificate == null) {
            return 6;
        }
        if (certificate.equals(HwCertification.SIGNATURE_PLATFORM)) {
            return 1;
        }
        if (certificate.equals(HwCertification.SIGNATURE_TESTKEY)) {
            return 2;
        }
        if (certificate.equals(HwCertification.SIGNATURE_SHARED)) {
            return 3;
        }
        if (certificate.equals(HwCertification.SIGNATURE_MEDIA)) {
            return 4;
        }
        if (certificate.equals(HwCertification.SIGNATURE_DEFAULT)) {
            return 0;
        }
        return -1;
    }

    public boolean isContainHwCertification(String packageName) {
        return this.mCertMap.get(packageName) == null ? false : mHasFeature;
    }

    public int getHwCertificateTypeNotMDM() {
        return 5;
    }
}
