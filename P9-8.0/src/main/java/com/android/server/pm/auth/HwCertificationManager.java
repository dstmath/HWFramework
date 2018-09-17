package com.android.server.pm.auth;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageParser.Package;
import android.os.Process;
import android.util.Flog;
import com.android.server.pm.auth.processor.HwCertificationProcessor;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HwCertificationManager {
    public static final String TAG = "HwCertificationManager";
    private static Context mContext = null;
    public static final boolean mHasFeature = true;
    private static HwCertificationManager mInstance;
    private ConcurrentHashMap<String, HwCertification> mCertMap = new ConcurrentHashMap();
    private Object mLock = new Object();
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                HwCertificationManager.this.handlePackagesChanged(intent);
            }
        }
    };
    private boolean mSystemReady = false;
    private final Runnable mWriteStateRunnable = new Runnable() {
        public void run() {
            List<HwCertification> data = new ArrayList();
            data.addAll(HwCertificationManager.this.mCertMap.values());
            new HwCertXmlHandler().updateHwCert(data);
        }
    };
    private boolean mloaded = false;

    private HwCertificationManager() {
        readHwCertXml();
    }

    private synchronized void readHwCertXml() {
        if (!this.mloaded) {
            long start = System.currentTimeMillis();
            HwCertXmlHandler handler = new HwCertXmlHandler();
            this.mCertMap.clear();
            handler.readHwCertXml(this.mCertMap);
            this.mloaded = true;
            long end = System.currentTimeMillis();
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i("HwCertificationManager", "readHwCertXml  spend time:" + (end - start) + " ms");
            }
            if (HwAuthLogger.getHWFLOW()) {
                HwAuthLogger.i("HwCertificationManager", "readHwCertXml  mCertMap size:" + this.mCertMap.size());
            }
        }
    }

    private void scheduleWriteStateLocked() {
        new Thread(this.mWriteStateRunnable).start();
    }

    private synchronized HwCertification parseAndVerify(Package pkg) {
        HwCertification cert = new HwCertification();
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i("HwCertificationManager", "basecodePath:" + pkg.baseCodePath);
        }
        HwCertificationProcessor hwCertificationProcessor = new HwCertificationProcessor();
        try {
            hwCertificationProcessor.createZipFile(pkg.baseCodePath);
            if (!hwCertificationProcessor.readCert(pkg.baseCodePath, cert)) {
                HwAuthLogger.e("HwCertificationManager", "read cert failed");
                cert.resetZipFile();
                hwCertificationProcessor.releaseZipFileResource();
                return null;
            } else if (!hwCertificationProcessor.parserCert(cert)) {
                HwAuthLogger.e("HwCertificationManager", "parse cert failed");
                cert.resetZipFile();
                hwCertificationProcessor.releaseZipFileResource();
                return null;
            } else if (hwCertificationProcessor.verifyCert(pkg, cert)) {
                cert.resetZipFile();
                hwCertificationProcessor.releaseZipFileResource();
                return cert;
            } else {
                HwAuthLogger.e("HwCertificationManager", "verify cert failed");
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
        return mContext != null;
    }

    public static synchronized HwCertificationManager getIntance() {
        synchronized (HwCertificationManager.class) {
            if (mContext == null) {
                throw new IllegalArgumentException("Impossible to get the instance. This class must be initialized before");
            }
            int uid = Process.myUid();
            if (uid == 1000 || uid == 2000 || uid == 0) {
                if (mInstance == null) {
                    mInstance = new HwCertificationManager();
                }
                HwCertificationManager hwCertificationManager = mInstance;
                return hwCertificationManager;
            }
            HwAuthLogger.e("HwCertificationManager", "getIntance from uid:" + uid + ",not system.return null");
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
        return !pkg.requestedPermissions.contains("com.huawei.permission.sec.MDM") ? pkg.requestedPermissions.contains("com.huawei.permission.sec.MDM.v2") : true;
    }

    public synchronized boolean checkHwCertification(Package pkg) {
        synchronized (this.mLock) {
            long start = System.currentTimeMillis();
            HwCertification cert = null;
            long end;
            try {
                cert = parseAndVerify(pkg);
                if (cert != null) {
                    this.mCertMap.put(cert.getPackageName().trim(), cert);
                    addHwPermission(pkg, cert);
                    scheduleWriteStateLocked();
                    if (getHwCertificateType(pkg.packageName) == 6) {
                        Flog.bdReport(mContext, 127, pkg.packageName);
                    }
                    end = System.currentTimeMillis();
                    if (HwAuthLogger.getHWFLOW()) {
                        HwAuthLogger.i("HwCertificationManager", "check" + pkg.packageName + "HwCertification spend time:" + (end - start) + " ms");
                    }
                    return true;
                }
                HwAuthLogger.e("HwCertificationManager", "check HwCertification error, cert is null!");
                end = System.currentTimeMillis();
                if (HwAuthLogger.getHWFLOW()) {
                    HwAuthLogger.i("HwCertificationManager", "check" + pkg.packageName + "HwCertification spend time:" + (end - start) + " ms");
                }
                return false;
            } catch (RuntimeException e) {
                HwAuthLogger.e("HwCertificationManager", "check HwCertification error: RuntimeException!");
                end = System.currentTimeMillis();
                if (HwAuthLogger.getHWFLOW()) {
                    HwAuthLogger.i("HwCertificationManager", "check" + pkg.packageName + "HwCertification spend time:" + (end - start) + " ms");
                }
                return false;
            } catch (Exception e2) {
                cert.setPermissionList(new ArrayList());
                HwAuthLogger.e("HwCertificationManager", "check HwCertification error!");
                end = System.currentTimeMillis();
                if (HwAuthLogger.getHWFLOW()) {
                    HwAuthLogger.i("HwCertificationManager", "check" + pkg.packageName + "HwCertification spend time:" + (end - start) + " ms");
                }
                return false;
            } catch (Throwable th) {
                end = System.currentTimeMillis();
                if (HwAuthLogger.getHWFLOW()) {
                    HwAuthLogger.i("HwCertificationManager", "check" + pkg.packageName + "HwCertification spend time:" + (end - start) + " ms");
                }
            }
        }
    }

    public boolean isSystemReady() {
        return this.mSystemReady;
    }

    public static boolean hasFeature() {
        return true;
    }

    public void systemReady() {
        this.mSystemReady = true;
        try {
            removeNotExist();
        } catch (Exception e) {
        }
        resigterBroadcastReceiver();
    }

    @SuppressLint({"AvoidMethodInForLoop"})
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
                    HwAuthLogger.d("HwCertificationManager", "package:" + ((String) pkgNameList.get(i)) + " not installed,removed from the cert list xml");
                }
            }
        }
        scheduleWriteStateLocked();
    }

    private void removeExistedCert(Package pkg) {
        if (HwAuthLogger.getHWDEBUG()) {
            HwAuthLogger.d("HwCertificationManager", "removeExistedCert" + pkg.packageName);
        }
        if (this.mCertMap.get(pkg.packageName) != null) {
            this.mCertMap.remove(pkg.packageName);
            if (HwAuthLogger.getHWDEBUG()) {
                HwAuthLogger.d("HwCertificationManager", "package:" + pkg.packageName + " installed,removed from the cert list xml");
            }
            scheduleWriteStateLocked();
        }
    }

    public void cleanUp(Package pkg) {
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i("HwCertificationManager", "clean up the cert list xml");
        }
        removeExistedCert(pkg);
    }

    public void cleanUp() {
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i("HwCertificationManager", "removeNotExist,clean up the cert list xml");
        }
        removeNotExist();
    }

    private void resigterBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");
        mContext.registerReceiver(this.mReceiver, filter);
    }

    private void handlePackagesChanged(Intent intent) {
        if (intent.getData() != null && intent.getAction() != null) {
            String action = intent.getAction();
            String packageName = intent.getData().getSchemeSpecificPart();
            if ("android.intent.action.PACKAGE_REMOVED".equals(action) && (intent.getBooleanExtra("android.intent.extra.REPLACING", false) ^ 1) != 0) {
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
                            HwAuthLogger.w("HwCertificationManager", "[package]:" + packageName + " is exist in the package");
                            return;
                        }
                        this.mCertMap.remove(packageName);
                        scheduleWriteStateLocked();
                        if (HwAuthLogger.getHWFLOW()) {
                            HwAuthLogger.i("HwCertificationManager", "[package]:" + packageName + ",remove from the cert list xml");
                        }
                    } catch (Exception ex) {
                        HwAuthLogger.e("HwCertificationManager", "onPackageRemoved error!", ex);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:2:0x0004, code:
            return r5;
     */
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
            HwAuthLogger.i("HwCertificationManager", "[package]:" + pkg.packageName + ",perm:" + perm);
        }
        return true;
    }

    public int getHwCertificateType(String packageName) {
        HwCertification cert = (HwCertification) this.mCertMap.get(packageName);
        if (cert == null) {
            if (HwAuthLogger.getHWDEBUG()) {
                HwAuthLogger.i("HwCertificationManager", "getHwCertificateType: cert is null, and pkg name is " + packageName);
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
        if (certificate.equals("null")) {
            return 0;
        }
        return -1;
    }

    public boolean isContainHwCertification(String packageName) {
        return this.mCertMap.get(packageName) != null;
    }

    public int getHwCertificateTypeNotMDM() {
        return 5;
    }
}
