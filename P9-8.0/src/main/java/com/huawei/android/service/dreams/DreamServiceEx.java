package com.huawei.android.service.dreams;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.dreams.IDreamManager.Stub;

public class DreamServiceEx {
    public static final boolean isDreaming() throws RemoteException {
        return Stub.asInterface(ServiceManager.checkService("dreams")).isDreaming();
    }
}
