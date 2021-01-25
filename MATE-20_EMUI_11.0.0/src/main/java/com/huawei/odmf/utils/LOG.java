package com.huawei.odmf.utils;

import android.util.Log;

public class LOG {
    private static boolean DBG_LOG = Log.isLoggable(TAG, 3);
    private static final boolean DEBUG_ON = false;
    private static boolean FLOW_LOG = Log.isLoggable(TAG, 4);
    private static final int LOG_DEBUG = 1;
    private static final int LOG_ERROR = 3;
    private static final int LOG_INFO = 0;
    private static final int LOG_WARNING = 2;
    private static final String TAG = "ODMF";
    private static long logCount;

    private LOG() {
    }

    public static void logI(String str) {
        if (FLOW_LOG) {
            logInternal(0, str);
        }
    }

    public static void logD(String str) {
        if (DBG_LOG) {
            logInternal(1, str);
        }
    }

    public static void logE(String str) {
        logInternal(3, str);
    }

    public static void logW(String str) {
        logInternal(2, str);
    }

    private static void logInternal(int i, String str) {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        String str2 = "N/A";
        String className = stackTrace.length > 2 ? stackTrace[2].getClassName() : str2;
        if (stackTrace.length > 2) {
            str2 = stackTrace[2].getMethodName();
        }
        int lastIndexOf = className.lastIndexOf(46);
        if (lastIndexOf >= 0) {
            className = className.substring(lastIndexOf + 1);
        }
        String str3 = className + "[ODMF]";
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        sb.append("[");
        sb.append(logCount);
        sb.append("][");
        sb.append(str2);
        sb.append("] ");
        sb.append(str);
        String sb2 = sb.toString();
        logCount++;
        if (logCount >= Long.MAX_VALUE) {
            logCount = 1;
        }
        if (i == 0) {
            Log.i(str3, sb2);
        } else if (i == 1) {
            Log.d(str3, sb2);
        } else if (i == 2) {
            Log.w(str3, sb2);
        } else if (i == 3) {
            Log.e(str3, sb2);
        }
    }
}
