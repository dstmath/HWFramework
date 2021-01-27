package com.android.server;

import android.content.Context;

public class HwCustCbsUtils {
    Context mContext;

    public HwCustCbsUtils(Context context) {
        this.mContext = context;
    }

    public boolean isNotAllowPkg(String currentVibrationPkg) {
        return false;
    }

    public boolean isAllowLowPowerPkg(String currentVibrationPkg) {
        return false;
    }

    public boolean allowVibrateWhenSlient(Context context, String currentVibrationPkg) {
        return true;
    }
}
