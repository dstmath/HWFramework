package android.util.wifi;

import android.util.HiLog;

public final class HwHiLog {
    public static final int DOMAIN = 218104322;
    public static final boolean HILOG_FALSE = false;

    private HwHiLog() {
    }

    public static void d(String tag, boolean isFmtStrPrivate, String format, Object... args) {
        HiLog.w(218104322, tag, isFmtStrPrivate, format, args);
    }

    public static void i(String tag, boolean isFmtStrPrivate, String format, Object... args) {
        HiLog.i(218104322, tag, isFmtStrPrivate, format, args);
    }

    public static void w(String tag, boolean isFmtStrPrivate, String format, Object... args) {
        HiLog.w(218104322, tag, isFmtStrPrivate, format, args);
    }

    public static void e(String tag, boolean isFmtStrPrivate, String format, Object... args) {
        HiLog.e(218104322, tag, isFmtStrPrivate, format, args);
    }

    public static void v(String tag, boolean isFmtStrPrivate, String format, Object... args) {
        HiLog.w(218104322, tag, isFmtStrPrivate, format, args);
    }
}
