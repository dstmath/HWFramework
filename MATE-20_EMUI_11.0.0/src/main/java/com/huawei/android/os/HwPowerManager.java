package com.huawei.android.os;

import android.content.Context;
import android.os.Bundle;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import com.huawei.android.os.IHwPowerManager;
import java.util.List;

public class HwPowerManager {
    private static final int INVALID_VALUE = -1;
    private static final Singleton<IHwPowerManager> IPowerManagerSingleton = new Singleton<IHwPowerManager>() {
        /* class com.huawei.android.os.HwPowerManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwPowerManager create() {
            try {
                IPowerManager pms = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
                if (pms == null) {
                    return null;
                }
                return IHwPowerManager.Stub.asInterface(pms.getHwInnerService());
            } catch (RemoteException e) {
                Log.e(HwPowerManager.TAG, "IHwPowerManager create() fail");
                return null;
            }
        }
    };
    private static final String TAG = "HwPowerManager";

    public static IHwPowerManager getService() {
        return IPowerManagerSingleton.get();
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

    public static void setPowerState(boolean isEnable) {
        try {
            getService().setPowerState(isEnable);
        } catch (RemoteException e) {
            Log.e(TAG, "setPowerState catch RemoteException!");
        }
    }

    public static void setMirrorLinkPowerStatus(boolean isStatus) {
        try {
            getService().setMirrorLinkPowerStatus(isStatus);
        } catch (RemoteException e) {
            Log.e(TAG, "setMirrorLinkPowerStatus catch RemoteException!");
        }
    }

    public static void startWakeUpReady(Context context, long eventTime) {
        if (context == null) {
            Log.e(TAG, "startWakeUpReady fail");
            return;
        }
        try {
            getService().startWakeUpReady(eventTime, context.getOpPackageName());
        } catch (RemoteException e) {
            Log.e(TAG, "startWakeUpReady catch RemoteException!");
        }
    }

    public static void stopWakeUpReady(Context context, long eventTime, boolean isEnableBright) {
        if (context == null) {
            Log.e(TAG, "stopWakeUpReady fail");
            return;
        }
        try {
            getService().stopWakeUpReady(eventTime, isEnableBright, context.getOpPackageName());
        } catch (RemoteException e) {
            Log.e(TAG, "stopWakeUpReady catch RemoteException!");
        }
    }

    public static void setAuthSucceeded() {
        try {
            getService().setAuthSucceeded();
        } catch (RemoteException e) {
            Log.e(TAG, "setAuthSucceeded catch RemoteException!");
        }
    }

    public static int getDisplayPanelType() {
        try {
            return getService().getDisplayPanelType();
        } catch (RemoteException e) {
            Log.e(TAG, "getDisplayPanelType catch RemoteException!");
            return -1;
        }
    }

    public static void requestNoUserActivityNotification(int timeout) {
        try {
            getService().requestNoUserActivityNotification(timeout);
        } catch (RemoteException e) {
            Log.e(TAG, "requestUserInActivityNotification catch RemoteException!");
        }
    }

    public static int setColorTemperature(int colorTemper) {
        try {
            return getService().setColorTemperature(colorTemper);
        } catch (RemoteException e) {
            Log.e(TAG, "setColorTemperature catch RemoteException!");
            return -1;
        }
    }

    public static int updateRgbGamma(float red, float green, float blue) {
        try {
            return getService().updateRgbGamma(red, green, blue);
        } catch (RemoteException e) {
            Log.e(TAG, "updateRgbGamma catch RemoteException!");
            return -1;
        }
    }

    public static boolean isInteractive() {
        try {
            IPowerManager pm = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
            if (pm != null) {
                return pm.isInteractive();
            }
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static List<String> getWakeLockPackageName() {
        try {
            return getService().getWakeLockPackageName();
        } catch (RemoteException e) {
            Log.e(TAG, "getWakeLockPackageName catch RemoteException!");
            return null;
        }
    }

    public static boolean registerScreenStateCallback(int remainTime, IScreenStateCallback callback) {
        if (remainTime <= 0 || callback == null) {
            Log.e(TAG, "registerScreenStateCallback param error");
            return false;
        }
        try {
            return getService().registerScreenStateCallback(remainTime, callback);
        } catch (RemoteException e) {
            Log.e(TAG, "registerScreenStateCallback catch RemoteException!");
            return false;
        }
    }

    public static boolean unRegisterScreenStateCallback() {
        try {
            return getService().unRegisterScreenStateCallback();
        } catch (RemoteException e) {
            Log.e(TAG, "unRegisterScreenStateCallback catch RemoteException!");
            return false;
        }
    }

    public static int setHwBrightnessData(String name, Bundle data) {
        try {
            return getService().setHwBrightnessData(name, data);
        } catch (RemoteException e) {
            Log.e(TAG, "setHwBrightnessData catch RemoteException!");
            return -1;
        }
    }

    public static int getHwBrightnessData(String name, Bundle data) {
        try {
            return getService().getHwBrightnessData(name, data);
        } catch (RemoteException e) {
            Log.e(TAG, "getHwBrightnessData catch RemoteException!");
            return -1;
        }
    }

    public static void setBiometricDetectState(int state) {
        try {
            getService().setBiometricDetectState(state);
        } catch (RemoteException ex) {
            Log.e(TAG, "setBiometricDetectState RemoteException" + ex.getMessage());
        }
    }
}
