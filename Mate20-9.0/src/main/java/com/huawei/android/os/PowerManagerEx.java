package com.huawei.android.os;

import android.os.IPowerManager;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
import java.util.List;

public class PowerManagerEx {
    private static final String TAG = "PowerManagerEx";

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

    public static void userActivity(PowerManager pm, long time, int event, int flags) {
        if (pm != null) {
            pm.userActivity(time, event, flags);
        }
    }

    public static int getUserActivityEventOther() {
        return 0;
    }

    public static List<String> getWakeLockPackageName() throws RemoteException {
        return IPowerManager.Stub.asInterface(ServiceManagerEx.getService("power")).getWakeLockPackageName();
    }

    public static void setTemporaryScreenAutoBrightnessSetting(int brightness) throws RemoteException {
        IPowerManager.Stub.asInterface(ServiceManagerEx.getService("power")).setTemporaryScreenAutoBrightnessSettingOverride(brightness);
    }

    public static void setTemporaryScreenAutoBrightnessAdjustmentSetting(float adj) throws RemoteException {
        IPowerManager.Stub.asInterface(ServiceManagerEx.getService("power")).setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(adj);
    }

    public static boolean isInteractive() throws RemoteException {
        return IPowerManager.Stub.asInterface(ServiceManagerEx.getService("power")).isInteractive();
    }

    public static void setTemporaryScreenBrightnessSetting(int brightness) throws RemoteException {
        IPowerManager.Stub.asInterface(ServiceManagerEx.getService("power")).setTemporaryScreenBrightnessSettingOverride(brightness);
    }

    public static int getCoverModeBrightnessFromLastScreenBrightness() throws RemoteException {
        return IPowerManager.Stub.asInterface(ServiceManagerEx.getService("power")).getCoverModeBrightnessFromLastScreenBrightness();
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
}
