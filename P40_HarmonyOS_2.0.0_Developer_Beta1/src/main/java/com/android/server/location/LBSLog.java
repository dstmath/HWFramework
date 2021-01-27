package com.android.server.location;

import android.util.HiSLog;

public class LBSLog {
    private static final boolean DBG = true;
    private static final int DOMAIN = 218104832;

    public static void d(String tag, boolean isFmtPrivate, String format, Object... args) {
        HiSLog.d((int) DOMAIN, tag, isFmtPrivate, format, args);
    }

    public static void i(String tag, boolean isFmtPrivate, String format, Object... args) {
        HiSLog.i((int) DOMAIN, tag, isFmtPrivate, format, args);
    }

    public static void w(String tag, boolean isFmtPrivate, String format, Object... args) {
        HiSLog.w((int) DOMAIN, tag, isFmtPrivate, format, args);
    }

    public static void e(String tag, boolean isFmtPrivate, String format, Object... args) {
        HiSLog.e((int) DOMAIN, tag, isFmtPrivate, format, args);
    }

    public static void d(String tag, String msg) {
        HiSLog.d((int) DOMAIN, tag, false, msg, new Object[0]);
    }

    public static void i(String tag, String msg) {
        HiSLog.i((int) DOMAIN, tag, false, msg, new Object[0]);
    }

    public static void w(String tag, String msg) {
        HiSLog.w((int) DOMAIN, tag, false, msg, new Object[0]);
    }

    public static void e(String tag, String msg) {
        HiSLog.e((int) DOMAIN, tag, false, msg, new Object[0]);
    }
}
