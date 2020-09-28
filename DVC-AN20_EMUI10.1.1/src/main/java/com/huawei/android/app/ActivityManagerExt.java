package com.huawei.android.app;

import android.app.ActivityManager;
import android.os.RemoteException;

public class ActivityManagerExt {
    public static void registerUserSwitchObserver(UserSwitchObserverExt observer, String name) throws RemoteException {
        ActivityManager.getService().registerUserSwitchObserver(observer.getUserSwitchObserver(), name);
    }
}
