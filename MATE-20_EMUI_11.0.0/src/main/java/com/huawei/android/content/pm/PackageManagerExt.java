package com.huawei.android.content.pm;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import com.huawei.annotation.HwSystemApi;
import java.util.ArrayList;
import java.util.List;

public class PackageManagerExt {
    public static final int DELETE_ALL_USERS = 2;
    @HwSystemApi
    public static final int FLAG_PERMISSION_APPLY_RESTRICTION = 16384;

    public static void deletePackage(PackageManager packageManager, String pkgName, int flags) {
        if (packageManager != null) {
            packageManager.deletePackage(pkgName, null, flags);
        }
    }

    public static List<ApplicationInfo> getInstalledApplicationsAsUser(PackageManager pm, int flags, int userId) {
        if (pm != null) {
            return pm.getInstalledApplicationsAsUser(flags, userId);
        }
        return new ArrayList();
    }

    public static ApplicationInfo getApplicationInfoAsUser(PackageManager pm, String packageName, int flags, int userId) throws PackageManager.NameNotFoundException {
        return pm.getApplicationInfoAsUser(packageName, flags, userId);
    }

    @HwSystemApi
    public static List<PackageInfo> getInstalledPackagesAsUser(PackageManager pm, int flags, int userId) {
        return pm.getInstalledPackagesAsUser(flags, userId);
    }

    @HwSystemApi
    public static int getPermissionFlags(PackageManager pm, String permissionName, String packageName, UserHandle user) {
        return pm.getPermissionFlags(permissionName, packageName, user);
    }

    @HwSystemApi
    public static PackageInfo getPackageInfoAsUser(PackageManager pm, String packageName, int flags, int userId) throws PackageManager.NameNotFoundException {
        return pm.getPackageInfoAsUser(packageName, flags, userId);
    }

    @HwSystemApi
    public static void addOnPermissionsChangeListener(PackageManager pm, OnPermissionsChangedListenerEx listenerEx) {
        pm.addOnPermissionsChangeListener(listenerEx.getListenerBridge());
    }

    @HwSystemApi
    public static void removeOnPermissionsChangeListener(PackageManager pm, OnPermissionsChangedListenerEx listenerEx) {
        pm.removeOnPermissionsChangeListener(listenerEx.getListenerBridge());
    }

    public static ResolveInfo resolveActivityAsUser(PackageManager pm, Intent intent, int flags, int userId) {
        if (pm != null) {
            return pm.resolveActivityAsUser(intent, flags, userId);
        }
        return null;
    }

    public static ResolveInfo resolveServiceAsUser(PackageManager pm, Intent intent, int flags, int userId) {
        if (pm != null) {
            return pm.resolveServiceAsUser(intent, flags, userId);
        }
        return null;
    }

    public static boolean isUpgrade(PackageManager pm) {
        if (pm == null) {
            return false;
        }
        return pm.isUpgrade();
    }

    public static List<ResolveInfo> queryIntentServicesAsUser(PackageManager packageManager, Intent intent, int flags, int userId) {
        if (packageManager == null) {
            return null;
        }
        return packageManager.queryIntentServicesAsUser(intent, flags, userId);
    }
}
