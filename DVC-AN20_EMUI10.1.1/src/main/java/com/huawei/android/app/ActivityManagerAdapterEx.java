package com.huawei.android.app;

import android.app.ActivityManager;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ActivityManagerAdapterEx {
    @HwSystemApi
    public static void moveActivityTaskToBack(IBinder token, boolean nonRoot) throws RemoteException {
        ActivityManager.getService().moveActivityTaskToBack(token, nonRoot);
    }

    @HwSystemApi
    public static int getTaskForActivity(IBinder token, boolean nonRoot) throws RemoteException {
        return ActivityManager.getService().getTaskForActivity(token, nonRoot);
    }
}
