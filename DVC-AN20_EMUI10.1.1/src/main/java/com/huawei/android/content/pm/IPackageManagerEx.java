package com.huawei.android.content.pm;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.RemoteException;
import com.huawei.annotation.HwSystemApi;

public class IPackageManagerEx {
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
}
