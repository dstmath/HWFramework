package com.huawei.displayengine;

import android.util.Log;
import android.util.Slog;

public final class DeLog {
    private static final boolean ASSERT = true;
    private static final boolean DEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean ERROR = true;
    private static final boolean INFO;
    private static final String LOG_HEAD = "[effect] ";
    private static final String TAG = "DE J DeLog";
    private static final boolean VERBOSE = true;
    private static final boolean WARN = true;

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) {
            z = true;
        }
        INFO = z;
    }

    private DeLog() {
    }

    public static void v(String tag, String msg) {
        if (DEBUG) {
            Slog.v(tag, LOG_HEAD + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (INFO) {
            Slog.i(tag, LOG_HEAD + msg);
        }
    }

    public static void i(String tag, String msg) {
        Slog.i(tag, LOG_HEAD + msg);
    }

    public static void w(String tag, String msg) {
        Slog.w(tag, LOG_HEAD + msg);
    }

    public static void e(String tag, String msg) {
        Slog.e(tag, LOG_HEAD + msg);
    }
}
