package com.huawei.android.app;

public final class HiLog {
    public static final int LOG_APP = 0;

    private HiLog() {
    }

    public static int d(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        return android.util.HiLog.d(domain, tag, isFmtStrPrivate, format, args);
    }

    public static int debug(HiLogLabel label, @HiLogConstString String format, Object... args) {
        return android.util.HiLog.debug(label.label, format, args);
    }

    public static int i(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        return android.util.HiLog.i(domain, tag, isFmtStrPrivate, format, args);
    }

    public static int info(HiLogLabel label, @HiLogConstString String format, Object... args) {
        return android.util.HiLog.info(label.label, format, args);
    }

    public static int w(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        return android.util.HiLog.w(domain, tag, isFmtStrPrivate, format, args);
    }

    public static int warn(HiLogLabel label, @HiLogConstString String format, Object... args) {
        return android.util.HiLog.warn(label.label, format, args);
    }

    public static int e(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        return android.util.HiLog.e(domain, tag, isFmtStrPrivate, format, args);
    }

    public static int error(HiLogLabel label, @HiLogConstString String format, Object... args) {
        return android.util.HiLog.error(label.label, format, args);
    }

    public static int fatal(HiLogLabel label, @HiLogConstString String format, Object... args) {
        return android.util.HiLog.fatal(label.label, format, args);
    }

    public static boolean isLoggable(int domain, String tag, int level) {
        return android.util.HiLog.isLoggable(domain, tag, level);
    }
}
