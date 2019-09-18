package android.util;

public final class HiLog {
    public static final int DEBUG = 3;
    public static final int DOMAIN_MAX = 999999999;
    public static final int DOMAIN_MIN = 0;
    public static final int ERROR = 6;
    public static final int HW_LOG_ID_CRASH = 4;
    public static final int HW_LOG_ID_EVENTS = 2;
    public static final int HW_LOG_ID_MAIN = 0;
    public static final int HW_LOG_ID_MAX = 4;
    public static final int HW_LOG_ID_RADIO = 1;
    public static final int HW_LOG_ID_SYSTEM = 3;
    public static final int INFO = 4;
    public static final int VERBOSE = 2;
    public static final int WARN = 5;

    public static native boolean isDebuggable();

    public static native boolean isLoggable(int i, String str, int i2);

    public static native int print_hilog_native(int i, int i2, String str, int i3, String str2);

    public static native int print_hwlogging_native(int i, int i2, String str, String str2);

    static {
        System.loadLibrary("hilog_jni");
    }

    private HiLog() {
    }

    public static int d(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        if (domain < 0 || domain > 999999999) {
            return -1;
        }
        return print_hilog_native(0, 3, tag, domain, HiLogString.format(isFmtStrPrivate, format, args));
    }

    public static int i(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        if (domain < 0 || domain > 999999999) {
            return -1;
        }
        return print_hilog_native(0, 4, tag, domain, HiLogString.format(isFmtStrPrivate, format, args));
    }

    public static int w(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        if (domain < 0 || domain > 999999999) {
            return -1;
        }
        return print_hilog_native(0, 5, tag, domain, HiLogString.format(isFmtStrPrivate, format, args));
    }

    public static int e(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        if (domain < 0 || domain > 999999999) {
            return -1;
        }
        return print_hilog_native(0, 6, tag, domain, HiLogString.format(isFmtStrPrivate, format, args));
    }
}
