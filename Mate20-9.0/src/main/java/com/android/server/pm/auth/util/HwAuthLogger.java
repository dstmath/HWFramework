package com.android.server.pm.auth.util;

import android.util.Log;
import android.util.Slog;

public class HwAuthLogger {
    private static final boolean DEBUG_LOGD = false;
    private static final boolean DEBUG_LOGE = true;
    private static final boolean DEBUG_LOGI = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable("HwAuthLogger", 4)));
    private static final boolean DEBUG_LOGW = false;

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
    }

    public static void e(String tag, String msg) {
        Slog.e(tag, msg);
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
    }

    public static void e(String tag, String msg, Throwable tr) {
        Slog.e(tag, msg, tr);
    }

    public static boolean getHwDebug() {
        return false;
    }

    public static boolean getHwFlow() {
        return false;
    }
}
