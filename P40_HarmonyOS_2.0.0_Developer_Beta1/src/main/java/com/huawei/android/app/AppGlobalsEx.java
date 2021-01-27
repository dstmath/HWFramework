package com.huawei.android.app;

import android.app.AppGlobals;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.os.RemoteException;
import com.huawei.android.content.pm.IPackageManagerEx;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class AppGlobalsEx {
    public static String getNameForUid(int uid) throws RemoteException {
        return AppGlobals.getPackageManager().getNameForUid(uid);
    }

    public static ApplicationInfo getApplicationInfo(String packageName, int flags, int userId) throws RemoteException {
        return AppGlobals.getPackageManager().getApplicationInfo(packageName, flags, userId);
    }

    public static IPackageManagerEx getPackageManager() {
        IPackageManager manager = AppGlobals.getPackageManager();
        IPackageManagerEx managerEx = new IPackageManagerEx();
        managerEx.setPackageManager(manager);
        return managerEx;
    }
}
