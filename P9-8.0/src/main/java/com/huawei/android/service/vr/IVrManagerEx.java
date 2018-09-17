package com.huawei.android.service.vr;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.vr.IVrManager;
import android.service.vr.IVrManager.Stub;

public class IVrManagerEx {
    private IVrManager mIVrManager;

    public static IVrManagerEx create() {
        IVrManager iVrManager = Stub.asInterface(ServiceManager.getService("vrmanager"));
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
