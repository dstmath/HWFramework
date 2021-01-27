package com.android.server.appactcontrol;

import com.android.server.pm.PackageManagerServiceEx;

public class AppActUtilsEx {
    public static void setPms(PackageManagerServiceEx pmsEx) {
        AppActUtils.setPms(pmsEx.getPackageManagerSerivce());
    }
}
