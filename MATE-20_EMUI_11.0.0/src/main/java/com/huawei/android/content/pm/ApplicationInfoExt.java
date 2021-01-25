package com.huawei.android.content.pm;

import android.content.pm.ApplicationInfo;

public class ApplicationInfoExt {
    public static final int PRIVATE_FLAG_PRIVILEGED = 8;

    public static int getPrivateFlags(ApplicationInfo info) {
        if (info == null) {
            return 0;
        }
        return info.privateFlags;
    }

    public static boolean isSystemApp(ApplicationInfo info) {
        if (info == null) {
            return false;
        }
        return info.isSystemApp();
    }

    public static boolean isPrivilegedApp(ApplicationInfo info) {
        if (info == null) {
            return false;
        }
        return info.isPrivilegedApp();
    }

    public static boolean isUpdatedSystemApp(ApplicationInfo info) {
        if (info == null) {
            return false;
        }
        return info.isUpdatedSystemApp();
    }

    public static int getHwFlags(ApplicationInfo info) {
        if (info == null) {
            return 0;
        }
        return info.hwFlags;
    }
}
