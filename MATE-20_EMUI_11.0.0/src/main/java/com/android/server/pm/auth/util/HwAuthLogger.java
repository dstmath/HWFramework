package com.android.server.pm.auth.util;

import android.util.Log;
import android.util.Slog;

public class HwAuthLogger {
    private static final boolean IS_HW_INFO = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable("HwAuthLogger", 4)));

    private HwAuthLogger() {
    }

    public static void info(String tag, String msg) {
        if (IS_HW_INFO) {
            Slog.i(tag, msg);
        }
    }

    public static void warn(String tag, String msg) {
        Slog.w(tag, msg);
    }

    public static void error(String tag, String msg) {
        Slog.e(tag, msg);
    }
}
