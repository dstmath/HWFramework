package com.android.server.gesture.anim;

import android.util.Log;
import com.android.server.gesture.GestureNavConst;

public class GLLogUtils {
    private static final String DEFAULT_CONNECTOR = "-";
    private static final String TAG = "GestureBackAnimation";

    private GLLogUtils() {
    }

    public static void logD(String tag, String content) {
        if (GestureNavConst.DEBUG) {
            Log.d(TAG, tag + DEFAULT_CONNECTOR + content);
        }
    }

    public static void logV(String tag, String content) {
        if (GestureNavConst.DEBUG) {
            Log.v(TAG, tag + DEFAULT_CONNECTOR + content);
        }
    }

    public static void logW(String tag, String content) {
        Log.w(TAG, tag + DEFAULT_CONNECTOR + content);
    }

    public static void logE(String tag, String content) {
        Log.e(TAG, tag + DEFAULT_CONNECTOR + content);
    }
}
