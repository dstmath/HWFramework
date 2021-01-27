package com.android.server.pm.auth;

import android.content.Context;
import android.content.pm.PackageParser;
import android.content.pm.PackageParserEx;

public class HwCertificationManagerEx {
    private static HwCertificationManagerEx sInstance;
    private HwCertificationManager mHwCertificationManager = HwCertificationManager.getInstance();

    public HwCertificationManager getHwCertificationManager() {
        return this.mHwCertificationManager;
    }

    public void setHwCertificationManager(HwCertificationManager hwCertificationManager) {
        this.mHwCertificationManager = hwCertificationManager;
    }

    public static synchronized HwCertificationManagerEx getInstance() {
        HwCertificationManagerEx hwCertificationManagerEx;
        synchronized (HwCertificationManagerEx.class) {
            if (sInstance == null) {
                sInstance = new HwCertificationManagerEx();
            }
            hwCertificationManagerEx = sInstance;
        }
        return hwCertificationManagerEx;
    }

    public static boolean hasFeature() {
        return HwCertificationManager.hasFeature();
    }

    public static boolean isSupportHwCertification(PackageParserEx.PackageEx pkg) {
        if (pkg == null || pkg.getPackageName() == null) {
            return false;
        }
        return HwCertificationManager.isSupportHwCertification((PackageParser.Package) pkg.getPackage());
    }

    public static boolean isInitialized() {
        return HwCertificationManager.isInitialized();
    }

    public static void initialize(Context context) {
        HwCertificationManager.initialize(context);
    }

    public boolean checkHwCertification(PackageParserEx.PackageEx pkg) {
        HwCertificationManager hwCertificationManager = this.mHwCertificationManager;
        if (hwCertificationManager != null) {
            return hwCertificationManager.checkHwCertification((PackageParser.Package) pkg.getPackage());
        }
        return false;
    }

    public boolean getHwCertificationPermission(boolean isAllowed, PackageParserEx.PackageEx pkg, String permission) {
        HwCertificationManager hwCertificationManager = this.mHwCertificationManager;
        if (hwCertificationManager == null || pkg == null) {
            return false;
        }
        return hwCertificationManager.getHwCertificationPermission(isAllowed, (PackageParser.Package) pkg.getPackage(), permission);
    }

    public int getHwCertSignatureVersion(String packageName) {
        HwCertificationManager hwCertificationManager = this.mHwCertificationManager;
        if (hwCertificationManager != null) {
            return hwCertificationManager.getHwCertSignatureVersion(packageName);
        }
        return -1;
    }

    public void cleanUp(PackageParserEx.PackageEx pkg) {
        if (pkg != null) {
            this.mHwCertificationManager.cleanUp((PackageParser.Package) pkg.getPackage());
        }
    }

    public void cleanUp() {
        this.mHwCertificationManager.cleanUp();
    }

    public void systemReady() {
        this.mHwCertificationManager.systemReady();
    }

    public int getHwCertificateTypeNotMdm() {
        return this.mHwCertificationManager.getHwCertificateTypeNotMdm();
    }

    public int getHwCertificateType(String packageName) {
        return this.mHwCertificationManager.getHwCertificateType(packageName);
    }

    public boolean isContainHwCertification(String packageName) {
        return this.mHwCertificationManager.isContainHwCertification(packageName);
    }

    public int verifyHwAuthCertification(String packagePath) {
        return this.mHwCertificationManager.verifyHwAuthCertification(packagePath);
    }
}
