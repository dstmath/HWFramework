package com.huawei.android.content.pm;

import android.content.pm.PackageManager;

public class PackageManagerExt {
    public static final int DELETE_ALL_USERS = 2;

    public static void deletePackage(PackageManager packageManager, String pkgName, int flags) {
        if (packageManager != null) {
            packageManager.deletePackage(pkgName, null, flags);
        }
    }
}
