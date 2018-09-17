package com.huawei.android.content.pm;

import android.app.ActivityThread;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.os.UserHandle;

public class PackageManagerEx {
    public static final int INSTALL_REPLACE_EXISTING = 2;
    public static final int INSTALL_SUCCEEDED = 1;

    public static void deletePackage(PackageManager mPm, String packageName, IPackageDeleteObserverEx observer, int flags) {
        mPm.deletePackage(packageName, observer.getPackageDeleteObserver(), flags);
    }

    public static void grantRuntimePermission(PackageManager packageManager, String packageName, String permissionName, UserHandle user) {
        packageManager.grantRuntimePermission(packageName, permissionName, user);
    }

    public static void revokeRuntimePermission(PackageManager packageManager, String packageName, String permissionName, UserHandle user) {
        packageManager.revokeRuntimePermission(packageName, permissionName, user);
    }

    public static void installPackage(PackageManager mPm, Uri packageURI, IPackageInstallObserverEx observer, int flags, String installerPackageName) {
        mPm.installPackage(packageURI, observer.getPackageInstallObserver(), flags, installerPackageName);
    }

    public static boolean getBlockUninstallForUser(String packageName, int userId) throws RemoteException {
        return ActivityThread.getPackageManager().getBlockUninstallForUser(packageName, userId);
    }

    public static ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
        return ActivityThread.getPackageManager().resolveIntent(intent, resolvedType, flags, userId);
    }

    public static void deletePackageAsUser(PackageManager pm, String packageName, IPackageDeleteObserverEx observer, int flags, int userId) {
        pm.deletePackageAsUser(packageName, observer.getPackageDeleteObserver(), flags, userId);
    }

    public static ApplicationInfo getApplicationInfoAsUser(PackageManager pm, String packageName, int flags, int userId) throws NameNotFoundException {
        return pm.getApplicationInfoAsUser(packageName, flags, userId);
    }

    public static ResolveInfo resolveActivityAsUser(PackageManager pm, Intent intent, int flags, int userId) {
        return pm.resolveActivityAsUser(intent, flags, userId);
    }

    public static PackageInfo getPackageInfoAsUser(PackageManager pm, String packageName, int flags, int userId) throws NameNotFoundException {
        return pm.getPackageInfoAsUser(packageName, flags, userId);
    }

    public static void flushPackageRestrictionsAsUser(PackageManager pm, int userId) {
        if (pm != null) {
            pm.flushPackageRestrictionsAsUser(userId);
        }
    }

    public static boolean shouldShowRequestPermissionRationale(PackageManager pm, String permission) {
        return pm.shouldShowRequestPermissionRationale(permission);
    }
}
