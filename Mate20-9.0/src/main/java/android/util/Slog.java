package android.util;

public final class Slog {
    private Slog() {
    }

    public static int v(String tag, String msg) {
        return Log.println_native(3, 2, tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return Log.println_native(3, 2, tag, msg + 10 + Log.getStackTraceString(tr));
    }

    public static int d(String tag, String msg) {
        return Log.println_native(3, 3, tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return Log.println_native(3, 3, tag, msg + 10 + Log.getStackTraceString(tr));
    }

    public static int i(String tag, String msg) {
        return Log.println_native(3, 4, tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return Log.println_native(3, 4, tag, msg + 10 + Log.getStackTraceString(tr));
    }

    public static int w(String tag, String msg) {
        return Log.println_native(3, 5, tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return Log.println_native(3, 5, tag, msg + 10 + Log.getStackTraceString(tr));
    }

    public static int w(String tag, Throwable tr) {
        return Log.println_native(3, 5, tag, Log.getStackTraceString(tr));
    }

    public static int e(String tag, String msg) {
        return Log.println_native(3, 6, tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return Log.println_native(3, 6, tag, msg + 10 + Log.getStackTraceString(tr));
    }

    public static int wtf(String tag, String msg) {
        return Log.wtf(3, tag, msg, null, false, true);
    }

    public static void wtfQuiet(String tag, String msg) {
        Log.wtfQuiet(3, tag, msg, true);
    }

    public static int wtfStack(String tag, String msg) {
        return Log.wtf(3, tag, msg, null, true, true);
    }

    public static int wtf(String tag, Throwable tr) {
        return Log.wtf(3, tag, tr.getMessage(), tr, false, true);
    }

    public static int wtf(String tag, String msg, Throwable tr) {
        return Log.wtf(3, tag, msg, tr, false, true);
    }

    public static int println(int priority, String tag, String msg) {
        return Log.println_native(3, priority, tag, msg);
    }
}
