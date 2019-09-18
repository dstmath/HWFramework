package com.huawei.android.app;

public final class HiLog {
    private HiLog() {
    }

    public static int d(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        return android.util.HiLog.d(domain, tag, isFmtStrPrivate, format, args);
    }

    public static int i(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        return android.util.HiLog.i(domain, tag, isFmtStrPrivate, format, args);
    }

    public static int w(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        return android.util.HiLog.w(domain, tag, isFmtStrPrivate, format, args);
    }

    public static int e(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        return android.util.HiLog.e(domain, tag, isFmtStrPrivate, format, args);
    }

    public static boolean isLoggable(int domain, String tag, int level) {
        return android.util.HiLog.isLoggable(domain, tag, level);
    }
}
