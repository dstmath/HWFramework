package ohos.accessibility.utils;

import java.util.Locale;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class LogUtil {
    private static final int DOMAIN_ID = 218111232;
    private static final String FORMAT = "%{public}s";
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, (int) DOMAIN_ID, TAG_LOG);
    private static final String LOG_FORMAT = "%s: %s";
    private static final String TAG_LOG = "BarrierFree";

    private LogUtil() {
    }

    public static void debug(Class cls, String str) {
        HiLog.debug(LABEL_LOG, FORMAT, new Object[]{String.format(Locale.ENGLISH, LOG_FORMAT, cls.getSimpleName(), str)});
    }

    public static void debug(String str, String str2) {
        HiLog.debug(LABEL_LOG, FORMAT, new Object[]{String.format(Locale.ENGLISH, LOG_FORMAT, str, str2)});
    }

    public static void info(Class cls, String str) {
        HiLog.info(LABEL_LOG, FORMAT, new Object[]{String.format(Locale.ENGLISH, LOG_FORMAT, cls.getSimpleName(), str)});
    }

    public static void info(String str, String str2) {
        HiLog.info(LABEL_LOG, FORMAT, new Object[]{String.format(Locale.ENGLISH, LOG_FORMAT, str, str2)});
    }

    public static void error(Class cls, String str) {
        HiLog.error(LABEL_LOG, FORMAT, new Object[]{String.format(Locale.ENGLISH, LOG_FORMAT, cls.getSimpleName(), str)});
    }

    public static void error(String str, String str2) {
        HiLog.error(LABEL_LOG, FORMAT, new Object[]{String.format(Locale.ENGLISH, LOG_FORMAT, str, str2)});
    }
}
