package com.msic.qarth;

import android.util.Log;

public class QarthLog {
    private static String TAG = "QarthLog";

    public static void d(String tag, String msg) {
        String str = TAG;
        Log.d(str, "[" + tag + "] " + msg);
    }

    public static void w(String tag, String msg) {
        String str = TAG;
        Log.w(str, "[" + tag + "] " + msg);
    }

    public static void e(String tag, String msg) {
        String str = TAG;
        Log.e(str, "[" + tag + "] " + msg);
    }

    public static void i(String tag, String msg) {
        String str = TAG;
        Log.i(str, "[" + tag + "] " + msg);
    }
}
