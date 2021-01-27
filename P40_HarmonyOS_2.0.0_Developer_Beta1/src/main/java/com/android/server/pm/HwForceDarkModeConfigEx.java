package com.android.server.pm;

public class HwForceDarkModeConfigEx {
    public static final int FORCE_DARK_IN_3RD_BLACK_LIST = 3;
    public static final String FORCE_DARK_PKGNAME_BLACK_LIST_SUFFIX = "@black_list";

    public static int getForceDarkModeFromAppTypeRecoManager(String pkgName, PackageSettingEx pkgSetting) {
        return HwForceDarkModeConfig.getInstance().getForceDarkModeFromAppTypeRecoManager(pkgName, pkgSetting.getPackageSetting());
    }

    public static boolean checkForceDark3rdBlackListFromAppTypeRecoManager(String pkgName) {
        return HwForceDarkModeConfig.getInstance().checkForceDark3rdBlackListFromAppTypeRecoManager(pkgName);
    }
}
