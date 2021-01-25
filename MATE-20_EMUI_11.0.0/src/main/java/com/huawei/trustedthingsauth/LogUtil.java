package com.huawei.trustedthingsauth;

import android.util.Log;

public class LogUtil {
    public static void info(String tag, String msg) {
        Log.i(tag, msg);
    }

    public static void error(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void debug(String tag, String msg) {
        Log.d(tag, msg);
    }
}
