package com.android.server.location;

import android.os.SystemProperties;

public class HwGpsLocationCustFeature implements IHwGpsLocationCustFeature {
    private static final int GPS_POSITION_MODE_MS_ASSISTED = 2;

    public int setPostionMode(int oldPositionMode, boolean agpsEnabled) {
        if (!SystemProperties.getBoolean("ro.config.hw_device_agps", false) || (agpsEnabled ^ 1) == 0) {
            return oldPositionMode;
        }
        return 2;
    }
}
