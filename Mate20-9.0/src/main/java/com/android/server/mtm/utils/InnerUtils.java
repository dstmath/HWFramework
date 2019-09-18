package com.android.server.mtm.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ParceledListSlice;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InnerUtils {
    private static final boolean DEBUG = false;
    private static final String TAG = "InnerUtils";
    private static IPackageManager pm = null;

    public static String getPackageNameByUid(int uid) {
        String[] packageNameList;
        try {
            if (pm != null) {
                packageNameList = pm.getPackagesForUid(uid);
            } else {
                pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
                packageNameList = pm.getPackagesForUid(uid);
            }
            if (packageNameList == null || packageNameList.length <= 0) {
                return null;
            }
            return packageNameList[0];
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to packagemanagerservice");
            return null;
        }
    }

    public static ApplicationInfo getApplicationInfo(String pkg) {
        ApplicationInfo appInfo = null;
        if (pkg == null) {
            return null;
        }
        try {
            if (pm != null) {
                appInfo = pm.getApplicationInfo(pkg, 0, UserHandle.getCallingUserId());
            } else {
                pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
                appInfo = pm.getApplicationInfo(pkg, 0, UserHandle.getCallingUserId());
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to packagemanagerservice");
        }
        return appInfo;
    }

    public static String getAwarePkgName(int pid) {
        ProcessInfo procInfo = ProcessInfoCollector.getInstance().getProcessInfo(pid);
        if (procInfo == null || procInfo.mProcessName == null || procInfo.mPackageName == null || procInfo.mPackageName.size() == 0) {
            return null;
        }
        String processName = procInfo.mProcessName;
        ArrayList<String> packageNameList = procInfo.mPackageName;
        if (packageNameList.size() == 1) {
            return packageNameList.get(0);
        }
        for (int i = 0; i < packageNameList.size(); i++) {
            ApplicationInfo appinfo = getApplicationInfo(packageNameList.get(i));
            if (appinfo != null && processName.equals(appinfo.processName)) {
                return appinfo.packageName;
            }
        }
        return packageNameList.get(0);
    }

    public static boolean checkPackageNameByUid(int uid, String pkgname) {
        String[] packageNameList;
        try {
            if (pm != null) {
                packageNameList = pm.getPackagesForUid(uid);
            } else {
                pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
                packageNameList = pm.getPackagesForUid(uid);
            }
            if (packageNameList != null && packageNameList.length > 0) {
                for (String packageName : packageNameList) {
                    if (packageName != null && packageName.equals(pkgname)) {
                        return true;
                    }
                }
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to packagemanagerservice");
        }
        return false;
    }

    public static boolean checkPackageNameContainsByUid(int uid, String pkgname) {
        String[] packageNameList;
        try {
            if (pm != null) {
                packageNameList = pm.getPackagesForUid(uid);
            } else {
                pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
                packageNameList = pm.getPackagesForUid(uid);
            }
            if (packageNameList != null && packageNameList.length > 0) {
                for (String packageName : packageNameList) {
                    if (packageName != null && packageName.contains(pkgname)) {
                        return true;
                    }
                }
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to packagemanagerservice");
        }
        return false;
    }

    public static List<PackageInfo> getAllInstalledAppsInfo(int userId) {
        try {
            if (pm == null) {
                pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
            }
            ParceledListSlice<PackageInfo> parceledList = pm.getInstalledPackages(131072, userId);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList.getList();
        } catch (RemoteException e) {
            Slog.e(TAG, "can not connect to packagemanagerservice");
            return Collections.emptyList();
        }
    }
}
