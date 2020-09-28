package com.huawei.iimagekit.blur.util;

import android.util.Log;

public class DebugUtil {
    private static final String TAG = "DEBUG";

    private DebugUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void log(String msg) {
        Log.i(TAG, msg);
    }

    public static void log(String format, Object... args) {
        Log.i(TAG, String.format(format, args));
    }
}
