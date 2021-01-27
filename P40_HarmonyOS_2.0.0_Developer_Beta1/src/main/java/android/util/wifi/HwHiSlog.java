package android.util.wifi;

import android.util.HiSLog;

public final class HwHiSlog {
    public static final int DOMAIN = 218104322;
    public static final boolean HILOG_FALSE = false;

    private HwHiSlog() {
    }

    public static void d(String tag, boolean isFmtStrPrivate, String format, Object... args) {
        HiSLog.w(218104322, tag, isFmtStrPrivate, format, args);
    }

    public static void i(String tag, boolean isFmtStrPrivate, String format, Object... args) {
        HiSLog.i(218104322, tag, isFmtStrPrivate, format, args);
    }

    public static void w(String tag, boolean isFmtStrPrivate, String format, Object... args) {
        HiSLog.w(218104322, tag, isFmtStrPrivate, format, args);
    }

    public static void e(String tag, boolean isFmtStrPrivate, String format, Object... args) {
        HiSLog.e(218104322, tag, isFmtStrPrivate, format, args);
    }

    public static void v(String tag, boolean isFmtStrPrivate, String format, Object... args) {
        HiSLog.w(218104322, tag, isFmtStrPrivate, format, args);
    }
}
