package com.android.server.location;

import android.content.Context;
import android.provider.Settings;
import com.android.server.appactcontrol.AppActConstant;

public class HwGpsLocationCustFeature implements IHwGpsLocationCustFeature {
    private static final String FORBIDDEN_MSA_SWTICH = "forbidden_msa_switch";
    private static final int GPS_POSITION_MODE_MS_ASSISTED = 2;
    private static final int GPS_POSITION_MODE_MS_BASED = 1;

    public int setPostionMode(Context context, int oldPositionMode, boolean isAgpsEnabled) {
        if (AppActConstant.VALUE_TRUE.equalsIgnoreCase(Settings.Global.getString(context.getContentResolver(), "hw_device_agps")) && !isAgpsEnabled) {
            return 2;
        }
        if (!AppActConstant.VALUE_TRUE.equals(Settings.Global.getString(context.getContentResolver(), FORBIDDEN_MSA_SWTICH)) || oldPositionMode != 2) {
            return oldPositionMode;
        }
        return 1;
    }
}
