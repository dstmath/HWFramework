package com.huawei.android.os;

import android.os.IPowerManager;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.IScreenStateCallback;
import com.huawei.annotation.HwSystemApi;
import com.huawei.internal.telephony.ProxyControllerEx;
import java.util.List;

public class PowerManagerEx {
    @HwSystemApi
    public static final int GO_TO_SLEEP_REASON_HDMI = 5;
    @HwSystemApi
    public static final int GO_TO_SLEEP_REASON_MIN = 0;
    @HwSystemApi
    public static final int GO_TO_SLEEP_REASON_POWER_BUTTON = 4;
    private static final String TAG = "PowerManagerEx";
    @HwSystemApi
    public static final int WAKE_LOCK_LEVEL_MASK = 65535;
    @HwSystemApi
    public static final int WAKE_REASON_GESTURE = 4;
    @HwSystemApi
    public static final int WAKE_REASON_PLUGGED_IN = 3;
    @HwSystemApi
    public static final int WAKE_REASON_UNKNOWN = 0;
    private static volatile PowerManagerEx mSelf = null;
    private IScreenStateCallback mCallback;

    public static PowerManagerEx getDefault() {
        PowerManagerEx powerManagerEx;
        synchronized (PowerManagerEx.class) {
            if (mSelf == null) {
                mSelf = new PowerManagerEx();
            }
            powerManagerEx = mSelf;
        }
        return powerManagerEx;
    }

    public static int getDozeWakeLock() {
        return 64;
    }

    public static int getBrightnessOn() {
        return 255;
    }

    public static int getDefaultScreenBrightnessSetting(PowerManager powerManager) {
        return powerManager.getDefaultScreenBrightnessSetting();
    }

    public static int getMaximumScreenBrightnessSetting(PowerManager powerManager) {
        return powerManager.getMaximumScreenBrightnessSetting();
    }

    public static void goToSleep(PowerManager powerManager, long time, int reason, int flags) {
        powerManager.goToSleep(time, reason, flags);
    }

    public static void wakeUp(PowerManager powerManager, long time, String reason) {
        powerManager.wakeUp(time, reason);
    }

    @HwSystemApi
    public static void wakeUp(PowerManager powerManager, long time, int reason, String details) {
        if (powerManager != null) {
            powerManager.wakeUp(time, reason, details);
        }
    }

    public static void userActivity(PowerManager pm, long time, int event, int flags) {
        if (pm != null) {
            pm.userActivity(time, event, flags);
        }
    }

    @HwSystemApi
    public static void userActivity(PowerManager pm, long when, boolean isKeepLights) {
        if (pm != null) {
            pm.userActivity(when, isKeepLights);
        }
    }

    public static int getUserActivityEventOther() {
        return 0;
    }

    public static List<String> getWakeLockPackageName() throws RemoteException {
        return HwPowerManager.getWakeLockPackageName();
    }

    public static void setTemporaryScreenAutoBrightnessSetting(int brightness) throws RemoteException {
        IPowerManager.Stub.asInterface(ServiceManagerEx.getService("power")).setTemporaryScreenAutoBrightnessSettingOverride(brightness);
    }

    public static void setTemporaryScreenAutoBrightnessAdjustmentSetting(float adj) throws RemoteException {
    }

    public static boolean isInteractive() throws RemoteException {
        return IPowerManager.Stub.asInterface(ServiceManagerEx.getService("power")).isInteractive();
    }

    public static void setTemporaryScreenBrightnessSetting(int brightness) throws RemoteException {
        IPowerManager.Stub.asInterface(ServiceManagerEx.getService("power")).setTemporaryScreenBrightnessSettingOverride(brightness);
    }

    public static int getCoverModeBrightnessFromLastScreenBrightness() throws RemoteException {
        return 0;
    }

    public static void requestNoUserActivityNotification(int timeout) {
        IHwPowerManager pm = HwPowerManager.getService();
        if (pm != null) {
            try {
                pm.requestNoUserActivityNotification(timeout);
            } catch (RemoteException e) {
                Log.e(TAG, "requestNoUserActivityNotification catch RemoteException!");
            }
        }
    }

    public boolean registerScreenStateCallback(int remainTime, IScreenStateCallbackEx callback) {
        if (remainTime <= 0 || callback == null) {
            Log.e(TAG, "registerScreenStateCallback param error");
            return false;
        }
        this.mCallback = addCallback(callback);
        return HwPowerManager.registerScreenStateCallback(remainTime, this.mCallback);
    }

    public boolean unRegisterScreenStateCallback() {
        this.mCallback = null;
        return HwPowerManager.unRegisterScreenStateCallback();
    }

    private IScreenStateCallback addCallback(final IScreenStateCallbackEx callback) {
        return new IScreenStateCallback.Stub() {
            /* class com.huawei.android.os.PowerManagerEx.AnonymousClass1 */

            public void onStateChange(int screenState) {
                IScreenStateCallbackEx iScreenStateCallbackEx = callback;
                if (iScreenStateCallbackEx != null) {
                    try {
                        iScreenStateCallbackEx.onStateChange(screenState);
                    } catch (AbstractMethodError e) {
                        Log.i(PowerManagerEx.TAG, "callback error: " + callback);
                    }
                }
            }
        };
    }

    public static void setSmartChargeState(String scene, String value) {
        IHwPowerManager pm = HwPowerManager.getService();
        if (pm != null) {
            try {
                pm.setSmartChargeState(scene, value);
            } catch (RemoteException e) {
                Log.e(TAG, "setSmartChargeState catch RemoteException!");
            }
        }
    }

    public static String getSmartChargeState(String scene) {
        IHwPowerManager pm = HwPowerManager.getService();
        if (pm == null) {
            return ProxyControllerEx.MODEM_0;
        }
        try {
            return pm.getSmartChargeState(scene);
        } catch (RemoteException e) {
            Log.e(TAG, "getSmartChargeState catch RemoteException!");
            return ProxyControllerEx.MODEM_0;
        }
    }

    @HwSystemApi
    public static void setModeToAutoNoClearOffsetEnable(PowerManager powerManager, boolean isEnable) {
        if (powerManager == null) {
            Log.e(TAG, "setModeToAutoNoClearOffsetEnable: powerManager is null!");
        } else {
            powerManager.setModeToAutoNoClearOffsetEnable(isEnable);
        }
    }

    public void setBiometricDetectState(int state) {
        HwPowerManager.setBiometricDetectState(state);
    }
}
