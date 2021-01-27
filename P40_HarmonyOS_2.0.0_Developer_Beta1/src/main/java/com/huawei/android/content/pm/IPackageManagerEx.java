package com.huawei.android.content.pm;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import com.huawei.android.app.PackageManagerEx;
import com.huawei.annotation.HwSystemApi;

public class IPackageManagerEx {
    private IPackageManager packageManager;

    public IPackageManager getPackageManager() {
        return this.packageManager;
    }

    public void setPackageManager(IPackageManager packageManager2) {
        this.packageManager = packageManager2;
    }

    public static ResolveInfo getLastChosenActivity(Intent intent, String resolvedType, int flags) throws Exception {
        return AppGlobals.getPackageManager().getLastChosenActivity(intent, resolvedType, flags);
    }

    @HwSystemApi
    public static void setComponentEnabledSetting(ComponentName componentName, int newState, int flags, int userId) throws RemoteException {
        AppGlobals.getPackageManager().setComponentEnabledSetting(componentName, newState, flags, userId);
    }

    @HwSystemApi
    public static ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) throws RemoteException {
        return AppGlobals.getPackageManager().getApplicationInfo(packageName, flags, userId);
    }

    @HwSystemApi
    public static void setApplicationEnabledSetting(String packageName, int newState, int flags, int userId, String callingPackage) throws RemoteException {
        AppGlobals.getPackageManager().setApplicationEnabledSetting(packageName, newState, flags, userId, callingPackage);
    }

    public static boolean isPackageSuspendedForUser(Context context, String packageName, int userId) throws RemoteException {
        if (!PackageManagerEx.hasSystemSignaturePermission(context)) {
            return false;
        }
        return AppGlobals.getPackageManager().isPackageSuspendedForUser(packageName, userId);
    }

    @HwSystemApi
    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
        return this.packageManager.resolveIntent(intent, resolvedType, flags, userId);
    }

    @HwSystemApi
    public static PackageInfo getPackageInfo(String packageName, int flags, int userId) throws RemoteException {
        return AppGlobals.getPackageManager().getPackageInfo(packageName, flags, userId);
    }

    @HwSystemApi
    public static boolean isPackageAvailable(String packageName, int userId) throws RemoteException {
        return AppGlobals.getPackageManager().isPackageAvailable(packageName, userId);
    }
}
