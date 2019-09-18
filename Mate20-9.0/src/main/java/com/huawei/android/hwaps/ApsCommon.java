package com.huawei.android.hwaps;

import android.os.SystemProperties;
import android.util.Log;

public class ApsCommon {
    public static final int INT_DEF = -1;
    private static final int LOG_DEBUG = 2;
    private static final int LOG_INFO = 1;
    private static int mLogLevel = -1;

    public static void logI(String tag, String msg) {
        if (isLogEnabled(1)) {
            Log.i(tag, "APS: " + msg);
        }
    }

    public static void logD(String tag, String msg) {
        if (isLogEnabled(2)) {
            Log.d(tag, "APS: " + msg);
        }
    }

    private static boolean isLogEnabled(int logLevel) {
        if (-1 == mLogLevel) {
            mLogLevel = SystemProperties.getInt("sys.aps.log", 0);
        }
        if (mLogLevel >= logLevel) {
            return true;
        }
        return false;
    }
}
