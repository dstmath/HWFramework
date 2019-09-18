package com.huawei.android.os;

import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import com.huawei.android.os.IHwPowerManager;

public class HwPowerManager {
    private static final Singleton<IHwPowerManager> IPowerManagerSingleton = new Singleton<IHwPowerManager>() {
        /* access modifiers changed from: protected */
        public IHwPowerManager create() {
            try {
                IPowerManager pms = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
                if (pms == null) {
                    return null;
                }
                return IHwPowerManager.Stub.asInterface(pms.getHwInnerService());
            } catch (RemoteException e) {
                Log.e(HwPowerManager.TAG, "IHwPowerManager create() fail: " + e);
                return null;
            }
        }
    };
    private static final String TAG = "HwPowerManager";

    public static IHwPowerManager getService() {
        return (IHwPowerManager) IPowerManagerSingleton.get();
    }

    public static boolean registerPowerMonitorCallback(IHwPowerDAMonitorCallback callback) {
        if (callback == null || getService() == null) {
            return false;
        }
        try {
            getService().registerPowerMonitorCallback(callback);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "registerPowerMonitorCallback catch RemoteException!");
            return false;
        }
    }

    public static void setChargeLimit(String limitValue) {
        try {
            getService().setChargeLimit(limitValue);
        } catch (RemoteException e) {
            Log.e(TAG, "setPowerLimit catch RemoteException!");
        }
    }

    public static void setPowerState(boolean state) {
        try {
            getService().setPowerState(state);
        } catch (RemoteException e) {
            Log.e(TAG, "setPowerState catch RemoteException!");
        }
    }

    public static void requestNoUserActivityNotification(int timeout) {
        try {
            getService().requestNoUserActivityNotification(timeout);
        } catch (RemoteException e) {
            Log.e(TAG, "requestUserInActivityNotification catch RemoteException!");
        }
    }

    public static boolean isInteractive() {
        try {
            IPowerManager pm = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
            if (pm != null) {
                return pm.isInteractive();
            }
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static void suspendSystem(boolean suspend, boolean forceUpdate) {
        try {
            getService().suspendSystem(suspend, forceUpdate);
        } catch (RemoteException e) {
            Log.e(TAG, "suspendSystem catch RemoteException!");
        }
    }

    public static boolean isSystemSuspending() {
        try {
            return getService().isSystemSuspending();
        } catch (RemoteException e) {
            Log.e(TAG, "isSystemSuspending catch RemoteException!");
            return false;
        }
    }
}
