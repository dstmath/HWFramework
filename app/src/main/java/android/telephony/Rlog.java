package android.telephony;

import android.util.Log;

public final class Rlog {
    private Rlog() {
    }

    public static int v(String tag, String msg) {
        return Log.println_native(1, 2, tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return Log.println_native(1, 2, tag, msg + '\n' + Log.getStackTraceString(tr));
    }

    public static int d(String tag, String msg) {
        return Log.println_native(1, 3, tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return Log.println_native(1, 3, tag, msg + '\n' + Log.getStackTraceString(tr));
    }

    public static int i(String tag, String msg) {
        return Log.println_native(1, 4, tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return Log.println_native(1, 4, tag, msg + '\n' + Log.getStackTraceString(tr));
    }

    public static int w(String tag, String msg) {
        return Log.println_native(1, 5, tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return Log.println_native(1, 5, tag, msg + '\n' + Log.getStackTraceString(tr));
    }

    public static int w(String tag, Throwable tr) {
        return Log.println_native(1, 5, tag, Log.getStackTraceString(tr));
    }

    public static int e(String tag, String msg) {
        return Log.println_native(1, 6, tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return Log.println_native(1, 6, tag, msg + '\n' + Log.getStackTraceString(tr));
    }

    public static int println(int priority, String tag, String msg) {
        return Log.println_native(1, priority, tag, msg);
    }

    public static boolean isLoggable(String tag, int level) {
        return Log.isLoggable(tag, level);
    }
}
