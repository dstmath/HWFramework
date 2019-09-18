package com.android.server;

import android.os.SystemProperties;

public class HwCustBatteryServiceImpl extends HwCustBatteryService {
    public boolean mutePowerConnectedTone() {
        return SystemProperties.getBoolean("ro.config.mute_usb_sound", false);
    }
}
