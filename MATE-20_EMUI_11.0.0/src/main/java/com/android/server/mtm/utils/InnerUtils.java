package com.android.server.mtm.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.RemoteException;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.huawei.android.content.pm.IPackageManagerExt;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.SlogEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InnerUtils {
    private static final boolean DEBUG = false;
    private static final String TAG = "InnerUtils";

    public static String getPackageNameByUid(int uid) {
        try {
            String[] packageNameList = IPackageManagerExt.getPackagesForUid(uid);
            if (packageNameList == null || packageNameList.length <= 0) {
                return null;
            }
            return packageNameList[0];
        } catch (RemoteException e) {
            SlogEx.e(TAG, "can not connect to packagemanagerservice.");
            return null;
        }
    }

    public static ApplicationInfo getApplicationInfo(String pkg) {
        if (pkg == null) {
            return null;
        }
        try {
            return IPackageManagerExt.getApplicationInfo(pkg, 0, UserHandleEx.getCallingUserId());
        } catch (RemoteException e) {
            SlogEx.e(TAG, "can not connect to packagemanagerservice");
            return null;
        }
    }

    public static String getAwarePkgName(int pid) {
        ProcessInfo procInfo = ProcessInfoCollector.getInstance().getProcessInfo(pid);
        if (procInfo == null || procInfo.mProcessName == null || procInfo.mPackageName == null || procInfo.mPackageName.isEmpty()) {
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

    public static List<PackageInfo> getAllInstalledAppsInfo(int userId) {
        try {
            List<PackageInfo> parceledList = IPackageManagerExt.getInstalledPackages(131072, userId);
            if (parceledList == null) {
                return Collections.emptyList();
            }
            return parceledList;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "can not connect to packagemanagerservice");
            return Collections.emptyList();
        }
    }
}
