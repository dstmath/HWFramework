package ohos.ai.engine.utils;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class HiAILog {
    private static final HiLogLabel DEFAULT_LABEL = new HiLogLabel(3, (int) Constants.DOMAIN_ID, Constants.HIAI_TAG);
    private static final String LOG_FORMAT = "%s: %s";

    private HiAILog() {
    }

    public static void debug(String str, String str2) {
        HiLog.debug(DEFAULT_LABEL, LOG_FORMAT, new Object[]{str, str2});
    }

    public static void info(String str, String str2) {
        HiLog.info(DEFAULT_LABEL, LOG_FORMAT, new Object[]{str, str2});
    }

    public static void warn(String str, String str2) {
        HiLog.warn(DEFAULT_LABEL, LOG_FORMAT, new Object[]{str, str2});
    }

    public static void error(String str, String str2) {
        HiLog.error(DEFAULT_LABEL, LOG_FORMAT, new Object[]{str, str2});
    }
}
