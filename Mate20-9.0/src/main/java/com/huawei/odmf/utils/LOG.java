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
    private static long sLogCount = 0;

    public static void logI(String msg) {
        if (FLOW_LOG) {
            logInternal(0, msg);
        }
    }

    public static void logD(String msg) {
        if (DBG_LOG) {
            logInternal(1, msg);
        }
    }

    public static void logE(String msg) {
        logInternal(3, msg);
    }

    public static void logW(String msg) {
        logInternal(2, msg);
    }

    private static void logInternal(int logLevel, String msg) {
        StackTraceElement[] elements = new Throwable().getStackTrace();
        String callerClassName = elements.length > 2 ? elements[2].getClassName() : "N/A";
        String callerMethodName = elements.length > 2 ? elements[2].getMethodName() : "N/A";
        int pos = callerClassName.lastIndexOf(46);
        if (pos >= 0) {
            callerClassName = callerClassName.substring(pos + 1);
        }
        String tag = callerClassName + "[ODMF]";
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.setLength(0);
        logBuilder.append("[").append(sLogCount).append("][").append(callerMethodName).append("] ").append(msg);
        String message = logBuilder.toString();
        sLogCount++;
        if (sLogCount >= Long.MAX_VALUE) {
            sLogCount = 1;
        }
        switch (logLevel) {
            case 0:
                Log.i(tag, message);
                return;
            case 1:
                Log.d(tag, message);
                return;
            case 2:
                Log.w(tag, message);
                return;
            case 3:
                Log.e(tag, message);
                return;
            default:
                return;
        }
    }
}
