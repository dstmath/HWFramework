package com.android.server.power;

public class HwDisplayPowerController {
    private static final int MAX_RETRY_COUNT = 4;
    private static final int MIN_LUX_VALUE = 3;
    private static final String TAG = "HwDisplayPowerController";
    private static boolean mCoverClose = false;
    private static int mSensorCount = 4;

    public static void setIfCoverClosed(boolean isClosed) {
        mCoverClose = isClosed;
        mSensorCount = 0;
    }

    public static boolean isCoverClosed() {
        return mCoverClose;
    }

    public static boolean shouldFilteInvalidSensorVal(float lux) {
        if (mCoverClose) {
            return true;
        }
        if (4 <= mSensorCount || lux >= 3.0f) {
            mSensorCount = 4;
            return false;
        }
        mSensorCount++;
        return true;
    }
}
