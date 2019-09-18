package com.android.server.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public final class HwPackageManagerServiceUtils {
    private static final boolean ANTIMAL_PROTECTION = "true".equalsIgnoreCase(SystemProperties.get("ro.product.antimal_protection", "true"));
    public static final int EVENT_APK_LOST_EXCEPTION = 907400026;
    public static final int EVENT_SETTINGS_EXCEPTION = 907400024;
    public static final int EVENT_UNINSTALLED_APPLICATION = 907400027;
    public static final int EVENT_UNINSTALLED_DELAPP_EXCEPTION = 907400025;
    private static final String FLAG_APK_NOSYS = "nosys";
    private static final String[] NOT_ALLOWED_UNINSTALL_WHITELIST = {"com.huawei.android.launcher", "com.huawei.systemmanager"};
    private static final String TAG = "HwPackageManagerServiceUtils";
    private static IHwPackageManagerServiceEx mHwPMSEx;
    private static HashMap<String, HashSet<String>> sCotaDelInstallMap = null;
    private static HashMap<String, HashSet<String>> sDelMultiInstallMap = null;

    public static boolean isDisableStatus(int state) {
        switch (state) {
            case 2:
            case 3:
            case 4:
                return true;
            default:
                return false;
        }
    }

    public static boolean isInAntiFillingWhiteList(String pkg, boolean isSupportHomeScreen) {
        if (!ANTIMAL_PROTECTION) {
            Slog.d(TAG, "AntimalProtection control is close!");
            return false;
        } else if (!isSupportHomeScreen) {
            return Arrays.asList(NOT_ALLOWED_UNINSTALL_WHITELIST).contains(pkg);
        } else {
            Slog.d(TAG, "Support home screen.");
            return false;
        }
    }

    public static void initHwPMSEx(IHwPackageManagerServiceEx hwPMSEx) {
        mHwPMSEx = hwPMSEx;
    }

    public static void reportException(int eventId, String message) {
        if (mHwPMSEx != null) {
            mHwPMSEx.reportEventStream(eventId, message);
        }
    }

    public static void updateFlagsForMarketSystemApp(PackageParser.Package pkg) {
        if (pkg != null && pkg.isUpdatedSystemApp() && pkg.mAppMetaData != null && pkg.mAppMetaData.getBoolean("android.huawei.MARKETED_SYSTEM_APP", false)) {
            Slog.i(TAG, "updateFlagsForMarketSystemApp" + pkg.packageName + " has MetaData HUAWEI_MARKETED_SYSTEM_APP, add FLAG_MARKETED_SYSTEM_APP");
            ApplicationInfo applicationInfo = pkg.applicationInfo;
            applicationInfo.hwFlags = applicationInfo.hwFlags | 536870912;
            if (pkg.mPersistentApp) {
                Slog.i(TAG, "updateFlagsForMarketSystemApp " + pkg.packageName + " is a persistent updated system app!");
                ApplicationInfo applicationInfo2 = pkg.applicationInfo;
                applicationInfo2.flags = applicationInfo2.flags | 8;
            }
        }
    }

    public static void setDelMultiInstallMap(HashMap<String, HashSet<String>> delMultiInstallMap) {
        if (delMultiInstallMap == null) {
            Slog.w(TAG, "DelMultiInstallMap is null!");
        }
        sDelMultiInstallMap = delMultiInstallMap;
    }

    public static void setCotaDelInstallMap(HashMap<String, HashSet<String>> cotaDelInstallMap) {
        if (cotaDelInstallMap == null) {
            Slog.w(TAG, "CotaDelInstallMap is null!");
        }
        sCotaDelInstallMap = cotaDelInstallMap;
    }

    public static boolean isNoSystemPreApp(String codePath) {
        String path;
        boolean z = false;
        if (TextUtils.isEmpty(codePath)) {
            Slog.w(TAG, "CodePath is null when check isNoSystemPreApp!");
            return false;
        }
        if (codePath.endsWith(".apk")) {
            path = getCustPackagePath(codePath);
        } else {
            path = codePath;
        }
        if (path == null) {
            return false;
        }
        boolean normalDelNoSysApp = sDelMultiInstallMap != null && sDelMultiInstallMap.get(FLAG_APK_NOSYS).contains(path);
        boolean cotaNoBootDelNoSysApp = sCotaDelInstallMap != null && sCotaDelInstallMap.get(FLAG_APK_NOSYS).contains(path);
        if (normalDelNoSysApp || cotaNoBootDelNoSysApp) {
            z = true;
        }
        return z;
    }

    public static String getCustPackagePath(String codePath) {
        String packagePath;
        if (TextUtils.isEmpty(codePath)) {
            Slog.w(TAG, "CodePath is null when getCustPackagePath!");
            return null;
        }
        int lastIndex = codePath.lastIndexOf(47);
        if (lastIndex > 0) {
            packagePath = codePath.substring(0, lastIndex);
        } else {
            packagePath = null;
            Log.e(TAG, "getCustPackagePath ERROR:  " + codePath);
        }
        return packagePath;
    }

    public static void addFlagsForRemovablePreApk(PackageParser.Package pkg, int hwFlags) {
        if ((hwFlags & DumpState.DUMP_HANDLE) != 0) {
            ApplicationInfo applicationInfo = pkg.applicationInfo;
            applicationInfo.hwFlags = 33554432 | applicationInfo.hwFlags;
        }
    }

    public static void addFlagsForUpdatedRemovablePreApk(PackageParser.Package pkg, int hwFlags) {
        if ((hwFlags & 67108864) != 0) {
            ApplicationInfo applicationInfo = pkg.applicationInfo;
            applicationInfo.hwFlags = 67108864 | applicationInfo.hwFlags;
        }
    }

    public static boolean hwlocationIsVendor(String codePath) {
        if (!TextUtils.isEmpty(codePath)) {
            return codePath.startsWith("/data/hw_init/vendor/");
        }
        Slog.w(TAG, "CodePath is null when check is vendor!");
        return false;
    }

    public static boolean hwlocationIsProduct(String codePath) {
        if (!TextUtils.isEmpty(codePath)) {
            return codePath.startsWith("/data/hw_init/product/");
        }
        Slog.w(TAG, "CodePath is null when check is product!");
        return false;
    }
}
