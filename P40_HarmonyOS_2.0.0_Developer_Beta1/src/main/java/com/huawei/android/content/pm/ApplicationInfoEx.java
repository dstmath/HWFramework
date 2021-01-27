package com.huawei.android.content.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.HwApplicationInfo;
import android.content.pm.PackageManager;
import android.util.SparseArray;
import com.huawei.annotation.HwSystemApi;

public class ApplicationInfoEx {
    public static final int BLACK_LIST_APK = 268435456;
    public static final int FLAG_HW_SPLIT_CONFIG = 536870912;
    public static final int FLAG_HW_SPLIT_FEATURE = 1073741824;
    public static final int FLAG_HW_SPLIT_PLUGIN = Integer.MIN_VALUE;
    public static final int FLAG_UPDATED_REMOVEABLE_APP = 67108864;
    @HwSystemApi
    public static final String INVALID_CREDENTIALPROTECTED_DATA_DIR = "";
    public static final int PARSE_IS_REMOVABLE_PREINSTALLED_APK = 33554432;
    private ApplicationInfo applicationInfo = new ApplicationInfo();

    public ApplicationInfoEx(ApplicationInfo applicationInfo2) {
        this.applicationInfo = applicationInfo2;
    }

    public int getHwFlags() {
        return this.applicationInfo.hwFlags;
    }

    public static boolean isDirectBootAware(ApplicationInfo applicationInfo2) {
        return applicationInfo2.isDirectBootAware();
    }

    public static int[] getHwSplitFlags(ApplicationInfo applicationInfo2) {
        if (applicationInfo2 == null) {
            return new int[0];
        }
        return applicationInfo2.hwSplitFlags;
    }

    public static int[] getSplitVersionCodes(ApplicationInfo applicationInfo2) {
        if (applicationInfo2 == null) {
            return new int[0];
        }
        return applicationInfo2.splitVersionCodes;
    }

    public static boolean hasPlugin(ApplicationInfo applicationInfo2) {
        if (applicationInfo2 == null) {
            return false;
        }
        return applicationInfo2.hasPlugin();
    }

    public static SparseArray<int[]> getSplitDependencies(ApplicationInfo applicationInfo2) {
        return HwApplicationInfo.getSplitDependencies(applicationInfo2);
    }

    public static long getLongVersionCode(ApplicationInfo applicationInfo2) {
        return applicationInfo2.longVersionCode;
    }

    @HwSystemApi
    public static int getVersionCode(ApplicationInfo applicationInfo2) {
        return applicationInfo2.versionCode;
    }

    @HwSystemApi
    public static int getGestureNavExtraFlags(ApplicationInfo applicationInfo2) {
        return applicationInfo2.gestnav_extra_flags;
    }

    @HwSystemApi
    public static String getCredentialProtectedDataDir(ApplicationInfo applicationInfo2) {
        if (applicationInfo2 == null) {
            return "";
        }
        return applicationInfo2.credentialProtectedDataDir;
    }

    @HwSystemApi
    public static int getHwFlags(ApplicationInfo applicationInfo2) {
        return applicationInfo2.hwFlags;
    }

    @HwSystemApi
    public static CharSequence loadUnsafeLabel(ApplicationInfo applicationInfo2, PackageManager pm) {
        return applicationInfo2.loadUnsafeLabel(pm);
    }

    @HwSystemApi
    public static String getSeInfo(ApplicationInfo applicationInfo2) {
        return applicationInfo2.seInfo;
    }

    public static boolean isPrivilegedApp(ApplicationInfo applicationInfo2) {
        return applicationInfo2.isPrivilegedApp();
    }
}
