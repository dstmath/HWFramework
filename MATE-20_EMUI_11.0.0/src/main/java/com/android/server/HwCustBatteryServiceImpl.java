package com.android.server;

import android.os.SystemProperties;

public class HwCustBatteryServiceImpl extends HwCustBatteryService {
    public static final boolean IS_AUTO_POWER_OFF_ON = SystemProperties.getBoolean("ro.config.auto_power_off", false);

    public boolean mutePowerConnectedTone() {
        return SystemProperties.getBoolean("ro.config.mute_usb_sound", false);
    }

    public boolean isAutoPowerOffOn() {
        return IS_AUTO_POWER_OFF_ON;
    }
}
