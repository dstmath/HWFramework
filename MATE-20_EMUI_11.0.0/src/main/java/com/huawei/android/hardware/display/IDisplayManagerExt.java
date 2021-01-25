package com.huawei.android.hardware.display;

import android.hardware.display.IDisplayManager;
import android.os.IBinder;
import android.os.RemoteException;

public class IDisplayManagerExt {
    public static IBinder getHwInnerService(IBinder binder) throws RemoteException {
        return IDisplayManager.Stub.asInterface(binder).getHwInnerService();
    }
}
