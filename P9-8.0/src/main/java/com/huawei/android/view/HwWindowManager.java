package com.huawei.android.view;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.AppAssociate;
import android.util.Log;
import android.util.Singleton;
import android.view.IWindowManager;
import com.huawei.android.view.IHwWindowManager.Stub;

public class HwWindowManager {
    private static final Singleton<IHwWindowManager> IWindowManagerSingleton = new Singleton<IHwWindowManager>() {
        protected IHwWindowManager create() {
            try {
                return Stub.asInterface(IWindowManager.Stub.asInterface(ServiceManager.getService(AppAssociate.ASSOC_WINDOW)).getHwInnerService());
            } catch (RemoteException e) {
                Log.e(HwWindowManager.TAG, "IHwWindowManager create() fail: " + e);
                return null;
            }
        }
    };
    private static final String TAG = "HwWindowManager";

    public static IHwWindowManager getService() {
        return (IHwWindowManager) IWindowManagerSingleton.get();
    }

    public static int releaseSnapshots(int memLevel) {
        if (getService() == null) {
            return 0;
        }
        try {
            return getService().releaseSnapshots(memLevel);
        } catch (RemoteException e) {
            Log.e(TAG, "releaseSnapshots catch RemoteException!");
            return 0;
        }
    }
}
