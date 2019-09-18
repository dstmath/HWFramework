package com.android.server.location;

import android.content.Context;
import android.provider.Settings;

public class HwGpsLocationCustFeature implements IHwGpsLocationCustFeature {
    private static final int GPS_POSITION_MODE_MS_ASSISTED = 2;

    public int setPostionMode(Context context, int oldPositionMode, boolean agpsEnabled) {
        if (!"true".equalsIgnoreCase(Settings.Global.getString(context.getContentResolver(), "hw_device_agps")) || agpsEnabled) {
            return oldPositionMode;
        }
        return 2;
    }
}
