package com.huawei.android.content.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ParceledListSlice;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.util.List;

public class IPackageManagerExt {
    public static ApplicationInfo getApplicationInfo(String pkgName, int flags, int userId) throws RemoteException {
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (packageManager != null) {
            return packageManager.getApplicationInfo(pkgName, flags, userId);
        }
        return null;
    }

    public static int getPackageUid(String packageName, int flags, int userId) throws RemoteException {
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (packageManager != null) {
            return packageManager.getPackageUid(packageName, flags, userId);
        }
        return -1;
    }

    public static int checkUidPermission(String permName, int uid) throws RemoteException {
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (packageManager != null) {
            return packageManager.checkUidPermission(permName, uid);
        }
        return -1;
    }

    public static String[] getPackagesForUid(int uid) throws RemoteException {
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (packageManager != null) {
            return packageManager.getPackagesForUid(uid);
        }
        return null;
    }

    public static List<PackageInfo> getInstalledPackages(int flags, int userId) throws RemoteException {
        ParceledListSlice<PackageInfo> parceledList;
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (packageManager == null || (parceledList = packageManager.getInstalledPackages(flags, userId)) == null) {
            return null;
        }
        return parceledList.getList();
    }

    public static String getNameForUid(int uid) throws RemoteException {
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (packageManager != null) {
            return packageManager.getNameForUid(uid);
        }
        return null;
    }

    public static boolean isFirstBoot() throws RemoteException {
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (packageManager != null) {
            return packageManager.isFirstBoot();
        }
        return false;
    }

    public static boolean isDeviceUpgrading() throws RemoteException {
        IPackageManager packageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (packageManager != null) {
            return packageManager.isDeviceUpgrading();
        }
        return false;
    }
}
