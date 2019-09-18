package com.android.server.gesture.anim;

import android.util.Log;

public class GLLogUtils {
    private static final String TAG = "GestureBackAnimation";

    private GLLogUtils() {
    }

    public static void logD(String tag, String content) {
        Log.d(TAG, tag + "-" + content);
    }

    public static void logV(String tag, String content) {
        Log.v(TAG, tag + "-" + content);
    }

    public static void logW(String tag, String content) {
        Log.w(TAG, tag + "-" + content);
    }

    public static void logE(String tag, String content) {
        Log.e(TAG, tag + "-" + content);
    }
}
