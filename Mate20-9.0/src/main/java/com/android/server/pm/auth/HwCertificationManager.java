package com.android.server.pm.auth;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageParser;
import android.os.Process;
import android.util.Flog;
import com.android.server.pm.HwMdmDFTUtilImpl;
import com.android.server.pm.auth.processor.HwCertificationProcessor;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class HwCertificationManager {
    public static final boolean HAS_FEATURE = true;
    public static final String TAG = "HwCertificationManager";
    private static Context mContext;
    private static HwCertificationManager mInstance;
    /* access modifiers changed from: private */
    public ConcurrentHashMap<String, HwCertification> mCertMap = new ConcurrentHashMap<>();
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
            List<HwCertification> data = new ArrayList<>();
            data.addAll(HwCertificationManager.this.mCertMap.values());
            new HwCertXmlHandler().updateHwCert(data);
        }
    };
    private boolean mloaded = false;

    private HwCertificationManager() {
        readHwCertXml();
    }

    public static void initialize(Context ctx) {
        mContext = ctx;
    }

    public static boolean isInitialized() {
        return mContext != null;
    }

    public static synchronized HwCertificationManager getIntance() {
        synchronized (HwCertificationManager.class) {
            if (mContext != null) {
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
            throw new IllegalArgumentException("Impossible to get the instance. This class must be initialized before");
        }
    }

    public static boolean isSupportHwCertification(PackageParser.Package pkg) {
        boolean z = false;
        if (pkg == null || pkg.requestedPermissions == null) {
            return false;
        }
        if (pkg.requestedPermissions.contains("com.huawei.permission.sec.MDM") || pkg.requestedPermissions.contains("com.huawei.permission.sec.MDM.v2")) {
            z = true;
        }
        return z;
    }

    public static boolean hasFeature() {
        return true;
    }

    private synchronized void readHwCertXml() {
        if (!this.mloaded) {
            long start = System.currentTimeMillis();
            HwCertXmlHandler handler = new HwCertXmlHandler();
            this.mCertMap.clear();
            handler.readHwCertXml(this.mCertMap);
            this.mloaded = true;
            long end = System.currentTimeMillis();
            if (HwAuthLogger.getHwFlow()) {
                HwAuthLogger.i("HwCertificationManager", "readHwCertXml  spend time:" + (end - start) + " ms");
            }
            if (HwAuthLogger.getHwFlow()) {
                HwAuthLogger.i("HwCertificationManager", "readHwCertXml  mCertMap size:" + this.mCertMap.size());
            }
        }
    }

    private void scheduleWriteStateLocked() {
        new Thread(this.mWriteStateRunnable).start();
    }

    private synchronized HwCertification parseAndVerify(PackageParser.Package pkg) {
        HwCertification cert = new HwCertification();
        HwCertificationProcessor hwCertificationProcessor = new HwCertificationProcessor();
        try {
            hwCertificationProcessor.createZipFile(pkg.baseCodePath);
            if (!hwCertificationProcessor.readCert(pkg.baseCodePath, cert)) {
                HwAuthLogger.e("HwCertificationManager", "read cert failed");
                return null;
            } else if (!hwCertificationProcessor.parserCert(cert)) {
                HwAuthLogger.e("HwCertificationManager", "parse cert failed");
                cert.resetZipFile();
                hwCertificationProcessor.releaseZipFileResource();
                return null;
            } else if (!hwCertificationProcessor.verifyCert(pkg, cert)) {
                HwAuthLogger.e("HwCertificationManager", "verify cert failed");
                cert.resetZipFile();
                hwCertificationProcessor.releaseZipFileResource();
                return null;
            } else {
                cert.resetZipFile();
                hwCertificationProcessor.releaseZipFileResource();
                return cert;
            }
        } finally {
            cert.resetZipFile();
            hwCertificationProcessor.releaseZipFileResource();
        }
    }

    private void addHwPermission(PackageParser.Package pkg, HwCertification cert) {
        if (pkg.requestedPermissions != null) {
            for (String perm : cert.getPermissionList()) {
                if (!pkg.requestedPermissions.contains(perm)) {
                    pkg.requestedPermissions.add(perm);
                }
            }
        }
    }

    public Context getContext() {
        return mContext;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0077, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00bc, code lost:
        return false;
     */
    public synchronized boolean checkHwCertification(PackageParser.Package pkg) {
        String str;
        String str2;
        String str3;
        String str4;
        synchronized (this.mLock) {
            long start = System.currentTimeMillis();
            HwCertification cert = null;
            try {
                cert = parseAndVerify(pkg);
                if (cert != null) {
                    this.mCertMap.put(cert.getPackageName().trim(), cert);
                    addHwPermission(pkg, cert);
                    scheduleWriteStateLocked();
                    if (getHwCertificateType(pkg.packageName) == 6) {
                        Flog.bdReport(mContext, 127, pkg.packageName);
                    }
                    if (isContainHwCertification(pkg.packageName)) {
                        HwMdmDFTUtilImpl.getMdmInstallInfoDft(mContext, pkg);
                    }
                    long end = System.currentTimeMillis();
                    if (HwAuthLogger.getHwFlow()) {
                        HwAuthLogger.i("HwCertificationManager", "check" + pkg.packageName + "HwCertification spend time:" + (end - start) + " ms");
                    }
                } else {
                    if (HwAuthLogger.getHwFlow()) {
                        HwAuthLogger.e("HwCertificationManager", "check HwCertification error, cert is null!");
                    }
                    removeExistedCert(pkg);
                    long end2 = System.currentTimeMillis();
                    if (HwAuthLogger.getHwFlow()) {
                        HwAuthLogger.i("HwCertificationManager", "check" + pkg.packageName + "HwCertification spend time:" + (end2 - start) + " ms");
                    }
                }
            } catch (RuntimeException e) {
                HwAuthLogger.e("HwCertificationManager", "check HwCertification error: RuntimeException!");
                long end3 = System.currentTimeMillis();
                if (HwAuthLogger.getHwFlow()) {
                    HwAuthLogger.i("HwCertificationManager", "check" + pkg.packageName + "HwCertification spend time:" + (end3 - start) + " ms");
                }
                return false;
            } catch (Exception e2) {
                try {
                    cert.setPermissionList(new ArrayList());
                    HwAuthLogger.e("HwCertificationManager", "check HwCertification error!");
                    return false;
                } finally {
                    long end4 = System.currentTimeMillis();
                    if (HwAuthLogger.getHwFlow()) {
                        str = "HwCertificationManager";
                        StringBuilder sb = new StringBuilder();
                        str2 = "check";
                        sb.append(str2);
                        sb.append(pkg.packageName);
                        str3 = "HwCertification spend time:";
                        sb.append(str3);
                        sb.append(end4 - start);
                        str4 = " ms";
                        sb.append(str4);
                        HwAuthLogger.i(str, sb.toString());
                    }
                }
            }
        }
    }

    public boolean isSystemReady() {
        return this.mSystemReady;
    }

    public void systemReady() {
        this.mSystemReady = true;
        try {
            removeNotExist();
        } catch (Exception e) {
            HwAuthLogger.e("HwCertificationManager", "remove invalid package list error!");
        }
        resigterBroadcastReceiver();
    }

    @SuppressLint({"AvoidMethodInForLoop"})
    private void removeNotExist() {
        List<String> pkgNameList = new ArrayList<>();
        for (String pkgName : this.mCertMap.keySet()) {
            if (!Utils.isPackageInstalled(pkgName, mContext)) {
                pkgNameList.add(pkgName);
            }
        }
        for (String pkgName2 : pkgNameList) {
            if (this.mCertMap.get(pkgName2) != null) {
                this.mCertMap.remove(pkgName2);
                if (HwAuthLogger.getHwDebug()) {
                    HwAuthLogger.d("HwCertificationManager", "package:" + pkgName2 + " not installed,removed from the cert list xml");
                }
            }
        }
        scheduleWriteStateLocked();
    }

    private void removeExistedCert(PackageParser.Package pkg) {
        if (HwAuthLogger.getHwDebug()) {
            HwAuthLogger.d("HwCertificationManager", "removeExistedCert" + pkg.packageName);
        }
        if (this.mCertMap.get(pkg.packageName) != null) {
            this.mCertMap.remove(pkg.packageName);
            if (HwAuthLogger.getHwDebug()) {
                HwAuthLogger.d("HwCertificationManager", "package:" + pkg.packageName + " installed,removed from the cert list xml");
            }
            scheduleWriteStateLocked();
        }
    }

    public void cleanUp(PackageParser.Package pkg) {
        if (HwAuthLogger.getHwFlow()) {
            HwAuthLogger.i("HwCertificationManager", "clean up the cert list xml");
        }
        removeExistedCert(pkg);
    }

    public void cleanUp() {
        if (HwAuthLogger.getHwFlow()) {
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

    /* access modifiers changed from: private */
    public void handlePackagesChanged(Intent intent) {
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
                try {
                    if (this.mCertMap.containsKey(packageName)) {
                        if (Utils.isPackageInstalled(packageName, mContext)) {
                            HwAuthLogger.w("HwCertificationManager", "[package]:" + packageName + " is exist in the package");
                            return;
                        }
                        this.mCertMap.remove(packageName);
                        scheduleWriteStateLocked();
                        if (HwAuthLogger.getHwFlow()) {
                            HwAuthLogger.i("HwCertificationManager", "[package]:" + packageName + ",remove from the cert list xml");
                        }
                    }
                } catch (Exception ex) {
                    HwAuthLogger.e("HwCertificationManager", "onPackageRemoved error!", ex);
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    public boolean getHwCertificationPermission(boolean allowed, PackageParser.Package pkg, String perm) {
        if (allowed || pkg == null || !this.mCertMap.containsKey(pkg.packageName)) {
            return allowed;
        }
        List<String> permissions = this.mCertMap.get(pkg.packageName).getPermissionList();
        if (permissions == null || !permissions.contains(perm) || pkg.requestedPermissions == null || !pkg.requestedPermissions.contains(perm)) {
            return allowed;
        }
        if (HwAuthLogger.getHwDebug()) {
            HwAuthLogger.i("HwCertificationManager", "[package]:" + pkg.packageName + ",perm:" + perm);
        }
        return true;
    }

    public int getHwCertificateType(String packageName) {
        HwCertification cert = this.mCertMap.get(packageName);
        if (cert == null) {
            if (HwAuthLogger.getHwDebug()) {
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
