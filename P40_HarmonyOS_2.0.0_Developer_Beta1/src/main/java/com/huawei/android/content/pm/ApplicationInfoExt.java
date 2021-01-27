package com.huawei.android.content.pm;

import android.content.pm.ApplicationInfo;

public class ApplicationInfoExt {
    public static final int FORCE_DARK_INVALID = 2;
    public static final int PRIVATE_FLAG_PRIVILEGED = 8;

    public static int getPrivateFlags(ApplicationInfo info) {
        if (info == null) {
            return 0;
        }
        return info.privateFlags;
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

    public static void andHwFlags(ApplicationInfo applicationInfo, int flag) {
        if (applicationInfo != null) {
            applicationInfo.hwFlags &= flag;
        }
    }

    public static void orHwFlags(ApplicationInfo applicationInfo, int flag) {
        if (applicationInfo != null) {
            applicationInfo.hwFlags |= flag;
        }
    }

    public static boolean isSystemApp(ApplicationInfo applicationInfo) {
        if (applicationInfo == null) {
            return false;
        }
        return applicationInfo.isSystemApp();
    }

    public static String getPackageName(ApplicationInfo applicationInfo) {
        return applicationInfo.packageName;
    }

    public static void setForceDarkMode(ApplicationInfo applicationInfo, int forceDarkMode) {
        applicationInfo.forceDarkMode = forceDarkMode;
    }

    public static boolean canChangeAspectRatio(ApplicationInfo applicationInfo, String aspectName) {
        if (applicationInfo == null) {
            return false;
        }
        return applicationInfo.canChangeAspectRatio(aspectName);
    }

    public static int getHwFlags(ApplicationInfo applicationInfo) {
        if (applicationInfo == null) {
            return 0;
        }
        return applicationInfo.hwFlags;
    }
}
