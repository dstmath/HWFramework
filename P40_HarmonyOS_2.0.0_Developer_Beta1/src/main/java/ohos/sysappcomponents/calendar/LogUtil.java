package ohos.sysappcomponents.calendar;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class LogUtil {
    public static final int DEBUG = 3;
    public static final int ERROR = 6;
    public static final int FATAL = 7;
    public static final int INFO = 4;
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 218110212, TAG_LOG);
    private static final String LOG_FORMAT = "%{public}s: %{public}s";
    private static final String TAG_LOG = "HwCalendar";
    public static final int WARN = 5;

    private LogUtil() {
    }

    public static void debug(Class<?> cls, String str) {
        HiLog.debug(LABEL_LOG, LOG_FORMAT, cls, str);
    }

    public static void debug(String str, String str2) {
        HiLog.debug(LABEL_LOG, LOG_FORMAT, str, str2);
    }

    public static void info(Class<?> cls, String str) {
        HiLog.info(LABEL_LOG, LOG_FORMAT, cls, str);
    }

    public static void info(String str, String str2) {
        HiLog.info(LABEL_LOG, LOG_FORMAT, str, str2);
    }

    public static void warn(String str, String str2) {
        HiLog.warn(LABEL_LOG, LOG_FORMAT, str, str2);
    }

    public static void error(Class<?> cls, String str) {
        HiLog.error(LABEL_LOG, LOG_FORMAT, cls, str);
    }

    public static void error(String str, String str2) {
        HiLog.error(LABEL_LOG, LOG_FORMAT, str, str2);
    }
}
