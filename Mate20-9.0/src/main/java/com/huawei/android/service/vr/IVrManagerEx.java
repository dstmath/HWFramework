package com.huawei.android.service.vr;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.vr.IVrManager;

public class IVrManagerEx {
    private IVrManager mIVrManager;

    public static IVrManagerEx create() {
        IVrManager iVrManager = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
        if (iVrManager == null) {
            return null;
        }
        return new IVrManagerEx(iVrManager);
    }

    private IVrManagerEx(IVrManager vr) {
        this.mIVrManager = vr;
    }

    public boolean getVrModeState() throws RemoteException {
        return this.mIVrManager.getVrModeState();
    }
}
