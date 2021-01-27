package com.huawei.hiai.awareness.log;

import android.util.Log;

public class Logger {
    public static final String LOG_TAG = "CAWARENESS_CLIENT: ";
    private static boolean debugLogPrintEnable;

    static {
        debugLogPrintEnable = false;
        debugLogPrintEnable = SystemPropertiesUtil.isDomesticBeta();
        Log.i(LOG_TAG, "debugLogPrintEnable=" + debugLogPrintEnable);
    }

    private Logger() {
    }

    public static void d(String tag, Object... messages) {
        if (tag != null && messages != null && debugLogPrintEnable) {
            Log.d(LOG_TAG + tag, concat(messages));
        }
    }

    public static void i(String tag, Object... messages) {
        if (tag != null && messages != null) {
            Log.i(LOG_TAG + tag, concat(messages));
        }
    }

    public static void w(String tag, Object... messages) {
        if (tag != null && messages != null) {
            Log.w(LOG_TAG + tag, concat(messages));
        }
    }

    public static void e(String tag, Object... messages) {
        if (tag != null && messages != null) {
            Log.e(LOG_TAG + tag, concat(messages));
        }
    }

    private static String concat(Object... objects) {
        StringBuilder builder = new StringBuilder();
        for (Object object : objects) {
            builder.append(object);
        }
        return builder.toString();
    }
}
