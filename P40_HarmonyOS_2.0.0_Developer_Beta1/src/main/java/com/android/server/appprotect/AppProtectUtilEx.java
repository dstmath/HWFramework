package com.android.server.appprotect;

import com.android.server.pm.PackageManagerServiceEx;

public class AppProtectUtilEx {
    public static void setPms(PackageManagerServiceEx pmsEx) {
        if (pmsEx != null) {
            AppProtectUtil.setPms(pmsEx.getPackageManagerSerivce());
        }
    }
}
