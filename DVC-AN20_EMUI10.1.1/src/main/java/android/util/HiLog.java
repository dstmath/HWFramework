package android.util;

public final class HiLog {
    public static final int DEBUG = 3;
    public static final int DOMAIN_MAX = 234881024;
    public static final int DOMAIN_MIN = 218103808;
    public static final int ERROR = 6;
    public static final int FATAL = 7;
    public static final int HW_LOG_ID_CRASH = 4;
    public static final int HW_LOG_ID_EVENTS = 2;
    public static final int HW_LOG_ID_MAIN = 0;
    public static final int HW_LOG_ID_MAX = 4;
    public static final int HW_LOG_ID_RADIO = 1;
    public static final int HW_LOG_ID_SYSTEM = 3;
    public static final int INFO = 4;
    public static final int LOG_CORE = 3;
    public static final int LOG_INIT = 1;
    public static final int VERBOSE = 2;
    public static final int WARN = 5;

    public static native boolean isDebuggable();

    public static native boolean isLoggable(int i, String str, int i2);

    public static native int print_hilog_native(int i, int i2, int i3, String str, String str2);

    public static native int print_hwlogging_native(int i, int i2, String str, String str2);

    static {
        System.loadLibrary("hilog_jni");
    }

    private HiLog() {
    }

    public static int d(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        if ((domain < 218103808 || domain >= 234881024) && domain != 0) {
            return -1;
        }
        return print_hilog_native(0, 3, domain, tag, HiLogString.format(isFmtStrPrivate, format, args));
    }

    public static int debug(HiLogLabel label, @HiLogConstString String format, Object... args) {
        return print_hilog_native(label.type, 3, label.domain, label.tag, HiLogString.format(false, format, args));
    }

    public static int i(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        if ((domain < 218103808 || domain >= 234881024) && domain != 0) {
            return -1;
        }
        return print_hilog_native(0, 4, domain, tag, HiLogString.format(isFmtStrPrivate, format, args));
    }

    public static int info(HiLogLabel label, @HiLogConstString String format, Object... args) {
        return print_hilog_native(label.type, 4, label.domain, label.tag, HiLogString.format(false, format, args));
    }

    public static int w(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        if ((domain < 218103808 || domain >= 234881024) && domain != 0) {
            return -1;
        }
        return print_hilog_native(0, 5, domain, tag, HiLogString.format(isFmtStrPrivate, format, args));
    }

    public static int warn(HiLogLabel label, @HiLogConstString String format, Object... args) {
        return print_hilog_native(label.type, 5, label.domain, label.tag, HiLogString.format(false, format, args));
    }

    public static int e(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        if ((domain < 218103808 || domain >= 234881024) && domain != 0) {
            return -1;
        }
        return print_hilog_native(0, 6, domain, tag, HiLogString.format(isFmtStrPrivate, format, args));
    }

    public static int error(HiLogLabel label, @HiLogConstString String format, Object... args) {
        return print_hilog_native(label.type, 6, label.domain, label.tag, HiLogString.format(false, format, args));
    }

    public static int fatal(HiLogLabel label, @HiLogConstString String format, Object... args) {
        return print_hilog_native(label.type, 7, label.domain, label.tag, HiLogString.format(false, format, args));
    }
}
