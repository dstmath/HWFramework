package com.huawei.android.app;

import android.app.ActivityManagerNative;
import android.content.Intent;
import android.os.RemoteException;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ActivityManagerNativeEx {
    public static void broadcastStickyIntent(Intent intent, String permission, int userId) {
        ActivityManagerNative.broadcastStickyIntent(intent, permission, userId);
    }

    public static void registerProcessObserver(IProcessObserverEx observer) throws RemoteException {
        if (observer != null) {
            ActivityManagerNative.getDefault().registerProcessObserver(observer.getIProcessObserver());
        }
    }

    public static void unregisterProcessObserver(IProcessObserverEx observer) throws RemoteException {
        if (observer != null) {
            ActivityManagerNative.getDefault().unregisterProcessObserver(observer.getIProcessObserver());
        }
    }

    public static void registerUserSwitchObserver(IUserSwitchObserverEx observer, String name) throws RemoteException {
        if (observer != null) {
            ActivityManagerNative.getDefault().registerUserSwitchObserver(observer.getIUserSwitchObserver(), name);
        }
    }

    public static void unregisterUserSwitchObserver(IUserSwitchObserverEx observer) throws RemoteException {
        if (observer != null) {
            ActivityManagerNative.getDefault().unregisterUserSwitchObserver(observer.getIUserSwitchObserver());
        }
    }

    public static int[] getRunningUserIds() throws RemoteException {
        return ActivityManagerNative.getDefault().getRunningUserIds();
    }

    public static void forceStopPackage(String packageName, int userId) throws RemoteException {
        ActivityManagerNative.getDefault().forceStopPackage(packageName, userId);
    }
}
