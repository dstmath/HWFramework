package com.android.server.pm;

import android.content.pm.IPackageManager;
import android.content.pm.PackageParser;
import android.content.pm.Signature;
import android.os.FileObserver;
import com.android.server.pm.PackageManagerService;
import java.util.HashMap;
import java.util.HashSet;

public abstract class AbsPackageManagerService extends IPackageManager.Stub {
    public static final String CUST_PRE_DEL_DIR = "/data/cust/";
    public static final String CUST_PRE_DEL_FILE = "delapp";
    public static final String DEL_APK_DIR = "/data/cust/xml/";
    public static final String DEL_APK_NAME = "unstall_apk.xml";
    public static final String FIRST_BOOT_TAG_DIR = "/data/data/";
    public static final String FIRST_BOOT_TAG_FILE = "firstbooted";
    public static final String SYSTEM_APP_DIR = "/system/app";
    public static final String SYSTEM_PRE_DEL_DIR = "/system/";
    public static final String SYSTEM_PRE_DEL_FILE = "delapp";
    public static final String SYSTEM_PRIV_APP_DIR = "/system/priv-app";
    public static final String UNINSTALLED_DELAPP_DIR = "/data/system/";
    public static final String UNINSTALLED_DELAPP_FILE = "uninstalled_delapp.xml";
    public static final String mAPKInstallList_DIR = "/data/cust/xml";
    public static final String mAPKInstallList_FILE = "APKInstallListEMUI5Release.txt";
    public static final String mDelAPKInstallList_FILE = "DelAPKInstallListEMUI5Release.txt";
    protected long mDexOptTotalTime = 0;
    public FileObserver[] mRemovableAppDirObserver = null;
    public boolean mUninstallUpdate = false;

    /* access modifiers changed from: protected */
    public void checkHwCertification(PackageParser.Package pkg, boolean isUpdate) {
    }

    /* access modifiers changed from: protected */
    public void hwCertCleanUp() {
    }

    /* access modifiers changed from: protected */
    public boolean getHwCertificationPermission(boolean allowed, PackageParser.Package pkg, String perm) {
        return allowed;
    }

    /* access modifiers changed from: protected */
    public void updatePackageBlackListInfo(String packageName) {
    }

    /* access modifiers changed from: protected */
    public void initHwCertificationManager() {
    }

    /* access modifiers changed from: protected */
    public int getHwCertificateType(PackageParser.Package pkg) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public boolean isContainHwCertification(PackageParser.Package pkg) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void replaceSignatureIfNeeded(PackageSetting ps, PackageParser.Package pkg, boolean isBootScan, boolean isUpdate) {
    }

    /* access modifiers changed from: protected */
    public void resetSharedUserSignaturesIfNeeded() {
    }

    /* access modifiers changed from: protected */
    public void initCertCompatSettings() {
    }

    /* access modifiers changed from: protected */
    public void writeCertCompatPackages(boolean update) {
    }

    /* access modifiers changed from: protected */
    public void updateCertCompatPackage(PackageParser.Package pkg, PackageSetting ps) {
    }

    /* access modifiers changed from: protected */
    public boolean isSystemSignatureUpdated(Signature[] previous, Signature[] current) {
        return false;
    }

    /* access modifiers changed from: protected */
    public Signature[] getRealSignature(PackageParser.Package pkg) {
        return new Signature[0];
    }

    /* access modifiers changed from: protected */
    public void setRealSignature(PackageParser.Package pkg, Signature[] sign) {
    }

    /* access modifiers changed from: protected */
    public void sendIncompatibleNotificationIfNeeded(String packageName) {
    }

    /* access modifiers changed from: protected */
    public void recordInstallAppInfo(String pkgName, long endTime, int installFlags) {
    }

    /* access modifiers changed from: protected */
    public boolean isMDMDisallowedInstallPackage(PackageParser.Package pkg, PackageManagerService.PackageInstalledInfo res) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isNotificationAddSplitButton(String packageName) {
        return false;
    }

    public HwCustPackageManagerService getCustPackageManagerService() {
        return null;
    }

    public void setCotaApksInstallStatus(int value) {
    }

    public HashMap<String, HashSet<String>> getCotaDelInstallMap() {
        return null;
    }

    public HashMap<String, HashSet<String>> getCotaInstallMap() {
        return null;
    }
}
