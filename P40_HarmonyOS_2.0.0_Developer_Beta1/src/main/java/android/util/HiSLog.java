package android.util;

public final class HiSLog {
    private HiSLog() {
    }

    public static int d(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        if (domain < 218103808 || domain > 234881024) {
            return -1;
        }
        return HiLog.print_hilog_native(3, 3, domain, tag, HiLogString.format(isFmtStrPrivate, format, args));
    }

    public static int i(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        if (domain < 218103808 || domain > 234881024) {
            return -1;
        }
        return HiLog.print_hilog_native(3, 4, domain, tag, HiLogString.format(isFmtStrPrivate, format, args));
    }

    public static int w(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        if (domain < 218103808 || domain > 234881024) {
            return -1;
        }
        return HiLog.print_hilog_native(3, 5, domain, tag, HiLogString.format(isFmtStrPrivate, format, args));
    }

    public static int e(int domain, String tag, boolean isFmtStrPrivate, String format, Object... args) {
        if (domain < 218103808 || domain > 234881024) {
            return -1;
        }
        return HiLog.print_hilog_native(3, 6, domain, tag, HiLogString.format(isFmtStrPrivate, format, args));
    }
}
