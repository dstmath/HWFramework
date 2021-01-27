package ohos.sysappcomponents.contact;

import java.util.Locale;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class LogUtil {
    private static final String HI_LOG_FORMAT = "%{public}s";
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 218110213, TAG_LOG);
    private static final String LOG_FORMAT = "%s: %s";
    private static final String TAG_LOG = "contact";

    private LogUtil() {
    }

    public static void debug(String str, String str2) {
        HiLog.debug(LABEL_LOG, HI_LOG_FORMAT, String.format(Locale.ROOT, LOG_FORMAT, str, str2));
    }

    public static void info(String str, String str2) {
        HiLog.info(LABEL_LOG, HI_LOG_FORMAT, String.format(Locale.ROOT, LOG_FORMAT, str, str2));
    }

    public static void error(String str, String str2) {
        HiLog.error(LABEL_LOG, HI_LOG_FORMAT, String.format(Locale.ROOT, LOG_FORMAT, str, str2));
    }
}
