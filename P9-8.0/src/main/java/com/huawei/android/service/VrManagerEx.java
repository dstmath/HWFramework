package com.huawei.android.service;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.vr.IVrManager;
import android.service.vr.IVrManager.Stub;
import android.util.Log;
import android.util.Singleton;

public class VrManagerEx {
    private static final String TAG = "PackageManagerEx";
    private static final Singleton<IVrManager> gDefault = new Singleton<IVrManager>() {
        protected IVrManager create() {
            return Stub.asInterface(ServiceManager.getService("vrmanager"));
        }
    };

    private static IVrManager getDefault() {
        return (IVrManager) gDefault.get();
    }

    public static boolean getVrModeState() {
        try {
            return getDefault().getVrModeState();
        } catch (RemoteException e) {
            Log.e(TAG, "failed to getVrModeState");
            return false;
        }
    }
}
