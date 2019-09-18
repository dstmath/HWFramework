package com.huawei.android.location.activityrecognition;

import android.util.Log;

public class SDKLog {
    private static boolean mDebug = true;

    public static void d(String tag, String msg) {
        if (mDebug) {
            Log.d(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (mDebug) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }
}
