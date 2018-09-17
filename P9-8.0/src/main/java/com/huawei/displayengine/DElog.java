package com.huawei.displayengine;

import android.util.Log;
import android.util.Slog;

public final class DElog {
    private static final boolean ASSERT = true;
    private static final boolean DEBUG;
    private static final boolean ERROR = true;
    private static final boolean INFO;
    private static final String TAG = "DE J DElog";
    private static final boolean VERBOSE = true;
    private static final boolean WARN = true;

    static {
        boolean z = true;
        boolean isLoggable = !Log.HWLog ? Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false : true;
        DEBUG = isLoggable;
        if (!Log.HWINFO) {
            z = Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false;
        }
        INFO = z;
    }

    private DElog() {
    }

    public static int v(String tag, String msg) {
        return Slog.v(tag, "[effect] " + msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return Slog.v(tag, "[effect] " + msg, tr);
    }

    public static int d(String tag, String msg) {
        if (DEBUG) {
            return Slog.d(tag, "[effect] " + msg);
        }
        return 0;
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            return Slog.d(tag, "[effect] " + msg, tr);
        }
        return 0;
    }

    public static int i(String tag, String msg) {
        if (INFO) {
            return Slog.i(tag, "[effect] " + msg);
        }
        return 0;
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (INFO) {
            return Slog.i(tag, "[effect] " + msg, tr);
        }
        return 0;
    }

    public static int w(String tag, String msg) {
        return Slog.w(tag, "[effect] " + msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return Slog.w(tag, "[effect] " + msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        return Slog.w(tag, tr);
    }

    public static int e(String tag, String msg) {
        return Slog.e(tag, "[effect] " + msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return Slog.e(tag, "[effect] " + msg, tr);
    }

    public static int wtf(String tag, String msg) {
        return Slog.wtf(tag, "[effect] " + msg);
    }

    public static void wtfQuiet(String tag, String msg) {
        Slog.wtfQuiet(tag, "[effect] " + msg);
    }

    public static int wtfStack(String tag, String msg) {
        return Slog.wtfStack(tag, "[effect] " + msg);
    }

    public static int wtf(String tag, Throwable tr) {
        return Slog.wtf(tag, tr);
    }

    public static int wtf(String tag, String msg, Throwable tr) {
        return Slog.wtf(tag, "[effect] " + msg, tr);
    }

    public static int println(int priority, String tag, String msg) {
        return Slog.println(priority, tag, "[effect] " + msg);
    }
}
