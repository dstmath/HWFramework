package com.android.server.power;

import android.util.Log;

public class HwDisplayPowerController {
    private static final boolean IS_DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int MAX_RETRY_COUNT = 4;
    private static final int MIN_LUX_VALUE = 3;
    private static final String TAG = "HwDisplayPowerController";
    private static boolean sIsCoverClose = false;
    private static int sSensorCount = 4;

    public static void setIfCoverClosed(boolean isClosed) {
        sIsCoverClose = isClosed;
        sSensorCount = 0;
    }

    public static boolean isCoverClosed() {
        return sIsCoverClose;
    }

    public static boolean shouldFilteInvalidSensorVal(float lux) {
        if (sIsCoverClose) {
            return true;
        }
        int i = sSensorCount;
        if (i >= 4 || lux >= 3.0f) {
            sSensorCount = 4;
            return false;
        }
        sSensorCount = i + 1;
        return true;
    }
}
