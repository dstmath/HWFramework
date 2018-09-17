package com.android.server.pm.auth.util;

import android.util.Log;
import android.util.Slog;

public class HwAuthLogger {
    private static final boolean DEBUG_LOGD = false;
    private static final boolean DEBUG_LOGE = false;
    private static final boolean DEBUG_LOGI;
    private static final boolean DEBUG_LOGW = true;

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable("HwAuthLogger", 4) : false : true;
        DEBUG_LOGI = isLoggable;
    }

    public static void v(String tag, String msg) {
    }

    public static void d(String tag, String msg) {
    }

    public static void i(String tag, String msg) {
        if (DEBUG_LOGI) {
            Slog.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        Slog.w(tag, msg);
    }

    public static void e(String tag, String msg) {
    }

    public static void v(String tag, String msg, Throwable tr) {
    }

    public static void d(String tag, String msg, Throwable tr) {
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (DEBUG_LOGI) {
            Slog.i(tag, msg, tr);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        Slog.w(tag, msg, tr);
    }

    public static void e(String tag, String msg, Throwable tr) {
    }

    public static boolean getHWDEBUG() {
        return false;
    }

    public static boolean getHWFLOW() {
        return DEBUG_LOGI;
    }
}
