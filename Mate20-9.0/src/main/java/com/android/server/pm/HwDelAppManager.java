package com.android.server.pm;

import java.io.File;

public class HwDelAppManager {
    public static final String CUST_PRE_DEL_DIR = "/data/cust/";
    public static final String CUST_PRE_DEL_FILE = "delapp";
    private static final int SCAN_AS_SYSTEM = 131072;
    public static final String SYSTEM_PRE_DEL_DIR = "/system/";
    public static final String SYSTEM_PRE_DEL_FILE = "delapp";
    private static final String TAG = "HwDelAppManager";
    private static volatile HwDelAppManager mInstance;
    private IHwPackageManagerServiceExInner mHwPmsExInner;

    private HwDelAppManager(IHwPackageManagerServiceExInner pmsEx) {
        this.mHwPmsExInner = pmsEx;
    }

    public static HwDelAppManager getInstance(IHwPackageManagerServiceExInner pmsEx) {
        if (mInstance == null) {
            synchronized (HwDelAppManager.class) {
                if (mInstance == null) {
                    mInstance = new HwDelAppManager(pmsEx);
                }
            }
        }
        return mInstance;
    }

    public boolean containDelPath(String sensePath) {
        return sensePath.startsWith("/data/cust/delapp") || sensePath.startsWith("/system/delapp");
    }

    public void scanRemovableAppDir(int scanMode) {
        File[] appDirs = getRemovableAppDirs();
        IHwPackageManagerInner pmsInner = this.mHwPmsExInner.getIPmsInner();
        for (File dir : appDirs) {
            if (dir != null && dir.exists()) {
                pmsInner.scanDirLIInner(dir, 16, scanMode | 131072, 0, 33554432);
            }
        }
    }

    private File[] getRemovableAppDirs() {
        return new File[]{new File(CUST_PRE_DEL_DIR, "delapp"), new File(SYSTEM_PRE_DEL_DIR, "delapp")};
    }

    public boolean isDelapp(PackageSetting ps) {
        if (ps.codePath == null) {
            return false;
        }
        return HwPackageManagerUtils.isHaveApkFile(getRemovableAppDirs(), ps.codePath.toString());
    }
}
