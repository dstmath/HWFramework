package com.huawei.android.os;

import android.os.IDeviceIdleController;
import android.os.RemoteException;
import android.os.ServiceManager;

public class IDeviceIdleControllerEx {
    private IDeviceIdleController mDic = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));

    public int forceIdle() throws RemoteException {
        return this.mDic.forceIdle();
    }
}
