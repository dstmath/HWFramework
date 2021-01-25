package ohos.vibrator.plugin;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class LogUtil {
    private static final int DOMAIN_ID = 218113828;
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, DOMAIN_ID, "VibratorLevel");
    private static final String LOG_FORMAT = "%{public}s: %{public}s";

    private LogUtil() {
    }

    public static void debug(String str, String str2) {
        HiLog.debug(LABEL_LOG, LOG_FORMAT, str, str2);
    }

    public static void info(String str, String str2) {
        HiLog.info(LABEL_LOG, LOG_FORMAT, str, str2);
    }

    public static void warn(String str, String str2) {
        HiLog.warn(LABEL_LOG, LOG_FORMAT, str, str2);
    }

    public static void error(String str, String str2) {
        HiLog.error(LABEL_LOG, LOG_FORMAT, str, str2);
    }
}
