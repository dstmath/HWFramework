package com.huawei.android.hidl;

import android.hidl.manager.V1_0.IServiceManager;
import android.os.RemoteException;

public class IServiceManagerHidlAdapter {
    private static IServiceManager sServiceManager;

    private IServiceManagerHidlAdapter() {
    }

    public static IServiceManagerHidlAdapter getService() throws RemoteException {
        sServiceManager = IServiceManager.getService();
        if (sServiceManager != null) {
            return new IServiceManagerHidlAdapter();
        }
        return null;
    }

    public boolean registerForNotifications(String fqName, String name, IServiceNotificationHidlAdapter callback) throws RemoteException {
        IServiceManager iServiceManager = sServiceManager;
        if (iServiceManager != null) {
            return iServiceManager.registerForNotifications(fqName, name, callback.getIServiceNotification());
        }
        return false;
    }
}
