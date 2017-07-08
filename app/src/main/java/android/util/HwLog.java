package android.util;

public final class HwLog {
    public static final int HW_LOG_ID_BDAT = 2;
    public static final int HW_LOG_ID_EXCEPTION = 0;
    public static final int HW_LOG_ID_JANK = 1;

    public static native int print_hwlog_native(int i, int i2, String str, String str2);

    private HwLog() {
    }

    public static int bdatv(String tag, String msg) {
        return print_hwlog_native(HW_LOG_ID_BDAT, HW_LOG_ID_BDAT, tag, msg);
    }

    public static int bdatd(String tag, String msg) {
        return print_hwlog_native(3, HW_LOG_ID_BDAT, tag, msg);
    }

    public static int bdati(String tag, String msg) {
        return print_hwlog_native(4, HW_LOG_ID_BDAT, tag, msg);
    }

    public static int bdatw(String tag, String msg) {
        return print_hwlog_native(5, HW_LOG_ID_BDAT, tag, msg);
    }

    public static int bdate(String tag, String msg) {
        return print_hwlog_native(6, HW_LOG_ID_BDAT, tag, msg);
    }
}
