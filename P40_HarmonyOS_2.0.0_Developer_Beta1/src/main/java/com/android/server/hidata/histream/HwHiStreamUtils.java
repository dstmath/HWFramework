package com.android.server.hidata.histream;

import android.util.wifi.HwHiLog;

public class HwHiStreamUtils {
    public static final int INVALID_VALUE = -1;
    public static final int SCENES_UNKNOWN = -1;
    private static final String TAG = "HiData_HiStream";

    public static void logD(boolean isFmtStrPrivate, String info, Object... args) {
        HwHiLog.d(TAG, isFmtStrPrivate, info, args);
    }

    public static void logW(boolean isFmtStrPrivate, String info, Object... args) {
        HwHiLog.w(TAG, isFmtStrPrivate, info, args);
    }

    public static void logE(boolean isFmtStrPrivate, String info, Object... args) {
        HwHiLog.e(TAG, isFmtStrPrivate, info, args);
    }
}
