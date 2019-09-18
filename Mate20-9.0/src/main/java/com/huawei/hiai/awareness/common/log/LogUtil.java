package com.huawei.hiai.awareness.common.log;

import android.util.Log;

public class LogUtil {
    private static final boolean IS_HWLOG = isHWLog();
    private static final boolean IS_LOG = true;
    private static final boolean IS_LOG_D = true;
    private static final boolean IS_LOG_E = true;
    private static final boolean IS_LOG_I = true;
    private static final boolean IS_LOG_V = false;
    private static final boolean IS_LOG_W = true;
    public static final String LOG_TAG = " CAWARENESS_CLIENT: ";
    private static boolean debugLogEnable = true;
    private static boolean errorLogEnable;
    private static boolean infoLogEnable = (IS_HWLOG || Log.isLoggable(LOG_TAG, 4));
    private static boolean verboseLogEnable;
    private static boolean warnLogEnable;

    static {
        boolean z;
        boolean z2;
        boolean z3 = false;
        if (!IS_HWLOG) {
            if (Log.isLoggable(LOG_TAG, 2)) {
            }
            z = false;
        } else {
            z = true;
        }
        verboseLogEnable = z;
        if (IS_HWLOG || Log.isLoggable(LOG_TAG, 5)) {
            z2 = true;
        } else {
            z2 = false;
        }
        warnLogEnable = z2;
        if (IS_HWLOG || Log.isLoggable(LOG_TAG, 6)) {
            z3 = true;
        }
        errorLogEnable = z3;
    }

    private static boolean isHWLog() {
        try {
            return Log.class.getDeclaredField("HWLog").getBoolean(null);
        } catch (NoSuchFieldException e) {
            Log.e(LOG_TAG, "[getHWLog]: " + e.toString());
            return false;
        } catch (IllegalArgumentException e2) {
            Log.e(LOG_TAG, "[getHWLog]: " + e2.toString());
            return false;
        } catch (IllegalAccessException e3) {
            Log.e(LOG_TAG, "[getHWLog]: " + e3.toString());
            return false;
        }
    }

    public static boolean isInfoLogEnable() {
        return infoLogEnable;
    }

    public static boolean isDebugLogEnable() {
        return debugLogEnable;
    }

    public static boolean isVerboseLogEnable() {
        return verboseLogEnable;
    }

    public static boolean isWarnLogEnable() {
        return warnLogEnable;
    }

    public static boolean isErrorLogEnable() {
        return errorLogEnable;
    }

    public static void i(String tag, String msg) {
        if (tag != null && msg != null && infoLogEnable) {
            Log.i(tag, LOG_TAG + msg);
        }
    }

    public static void i(String tag, String msg, Throwable e) {
        if (tag != null && msg != null && infoLogEnable) {
            Log.i(tag, LOG_TAG + msg, e);
        }
    }

    public static void d(String tag, String msg) {
        if (tag != null && msg != null && debugLogEnable) {
            Log.d(tag, LOG_TAG + msg);
        }
    }

    public static void d(String tag, String msg, Throwable e) {
        if (tag != null && msg != null && debugLogEnable) {
            Log.d(tag, LOG_TAG + msg, e);
        }
    }

    public static void v(String tag, String msg) {
        if (tag != null && msg != null && verboseLogEnable) {
            Log.v(tag, LOG_TAG + msg);
        }
    }

    public static void v(String tag, String msg, Throwable e) {
        if (tag != null && msg != null && verboseLogEnable) {
            Log.v(tag, LOG_TAG + msg, e);
        }
    }

    public static void w(String tag, String msg) {
        if (tag != null && msg != null && warnLogEnable) {
            Log.w(tag, LOG_TAG + msg);
        }
    }

    public static void w(String tag, String msg, Throwable e) {
        if (tag != null && msg != null && warnLogEnable) {
            Log.w(tag, LOG_TAG + msg, e);
        }
    }

    public static void e(String tag, String msg) {
        if (tag != null && msg != null && errorLogEnable) {
            Log.e(tag, LOG_TAG + msg);
        }
    }

    public static void e(String tag, String msg, Throwable e) {
        if (tag != null && msg != null && errorLogEnable) {
            Log.e(tag, LOG_TAG + msg, e);
        }
    }
}
