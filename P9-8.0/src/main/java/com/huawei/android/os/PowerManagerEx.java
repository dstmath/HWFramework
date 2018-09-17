package com.huawei.android.os;

import android.os.PowerManager;

public class PowerManagerEx {
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
}
