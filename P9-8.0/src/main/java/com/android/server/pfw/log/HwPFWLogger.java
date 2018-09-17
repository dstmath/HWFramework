package com.android.server.pfw.log;

import android.util.Log;

public class HwPFWLogger {
    private static final boolean LOG_V = false;
    private static final String TAG = "HwPFWLogger";

    public static void v(String subtag, String msg) {
    }

    public static void d(String subtag, String msg) {
        Log.d(TAG, subtag + ":" + msg);
    }

    public static void i(String subtag, String msg) {
        Log.i(TAG, subtag + ":" + msg);
    }

    public static void w(String subtag, String msg) {
        Log.w(TAG, subtag + ":" + msg);
    }

    public static void e(String subtag, String msg) {
        Log.e(TAG, subtag + ":" + msg);
    }
}
