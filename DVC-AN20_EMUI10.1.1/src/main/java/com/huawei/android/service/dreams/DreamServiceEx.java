package com.huawei.android.service.dreams;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.dreams.IDreamManager;

public class DreamServiceEx {
    public static final boolean isDreaming() throws RemoteException {
        return IDreamManager.Stub.asInterface(ServiceManager.checkService("dreams")).isDreaming();
    }

    public static final void awaken() throws RemoteException {
        IDreamManager.Stub.asInterface(ServiceManager.checkService("dreams")).awaken();
    }
}
