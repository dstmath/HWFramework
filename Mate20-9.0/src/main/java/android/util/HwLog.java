package android.util;

public final class HwLog {
    public static final int HW_LOG_ID_DUBAI = 2;
    public static final int HW_LOG_ID_EXCEPTION = 0;
    public static final int HW_LOG_ID_JANK = 1;

    public static native int print_hwlog_native(int i, int i2, String str, String str2);

    private HwLog() {
    }

    public static int dubaiv(String tag, String msg) {
        return print_hwlog_native(2, 2, tag, msg);
    }

    public static int dubaid(String tag, String msg) {
        return print_hwlog_native(3, 2, tag, msg);
    }

    public static int dubaii(String tag, String msg) {
        return print_hwlog_native(4, 2, tag, msg);
    }

    public static int dubaiw(String tag, String msg) {
        return print_hwlog_native(5, 2, tag, msg);
    }

    public static int dubaie(String tag, String msg) {
        return print_hwlog_native(6, 2, tag, msg);
    }
}
