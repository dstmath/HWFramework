package com.android.server.security.tsmagent.utils;

import android.util.Log;
import android.util.Slog;

public class HwLog {
    private static final int CALL_LOG_LEVEL = 3;
    private static final boolean HWDBG;
    private static final boolean HWINFO;
    public static final String TAG = "HwTSM";
    private static String lastPkgName = "";

    static {
        boolean z = true;
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false : true;
        HWDBG = isLoggable;
        if (!Log.HWINFO) {
            z = Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false;
        }
        HWINFO = z;
    }

    public static void v(String msg) {
        if (HWDBG) {
            writeLog(2, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (HWDBG) {
            writeLog(2, tag, msg);
        }
    }

    public static void d(String msg) {
        if (HWDBG) {
            writeLog(3, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (HWDBG) {
            writeLog(3, tag, msg);
        }
    }

    public static void i(String msg) {
        if (HWINFO) {
            writeLog(4, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (HWINFO) {
            writeLog(4, tag, msg);
        }
    }

    public static void w(String msg) {
        Slog.w(TAG, msg);
    }

    public static void w(String tag, String msg) {
        Slog.w(joinTag(TAG, tag), msg);
    }

    public static void e(String msg) {
        Slog.e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        Slog.e(joinTag(TAG, tag), msg);
    }

    private static synchronized void writeLog(int priority, String msg) {
        synchronized (HwLog.class) {
            writeLog(priority, null, msg);
        }
    }

    private static synchronized void writeLog(int priority, String tag, String msg) {
        synchronized (HwLog.class) {
            StringBuilder msgSb = new StringBuilder();
            msgSb.append(msg);
            msgSb.append("[").append(Thread.currentThread().getName()).append("-").append(Thread.currentThread().getId()).append("]");
            StackTraceElement[] st = new Throwable().getStackTrace();
            if (st.length > 3) {
                msgSb.append("(").append(lastPkgName).append("/").append(st[3].getFileName()).append(":").append(st[3].getLineNumber()).append(")");
            } else {
                msgSb.append("(").append(lastPkgName).append("/unknown source)");
            }
            Slog.println(priority, tag == null ? TAG : joinTag(TAG, tag), msgSb.toString());
        }
    }

    private static String joinTag(String TAG1, String TAG2) {
        return TAG1 + "_" + TAG2;
    }
}
