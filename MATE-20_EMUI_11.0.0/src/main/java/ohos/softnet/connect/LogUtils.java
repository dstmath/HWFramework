package ohos.softnet.connect;

import java.util.Locale;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class LogUtils {
    private static final HiLogLabel LABEL = new HiLogLabel(3, LOG_ID_DISC, "softnet");
    private static final int LOG_ID_DISC = 218109232;

    public static void info(String str, String str2, String str3, Object... objArr) {
        HiLog.info(LABEL, "%{public}s: %{public}s: %{public}s", str, str2, String.format(Locale.ROOT, str3, objArr));
    }

    public static void debug(String str, String str2, String str3, Object... objArr) {
        HiLog.debug(LABEL, "%{public}s: %{public}s: %{public}s", str, str2, String.format(Locale.ROOT, str3, objArr));
    }

    public static void error(String str, String str2, String str3, Object... objArr) {
        HiLog.error(LABEL, "%{public}s: %{public}s: %{public}s", str, str2, String.format(Locale.ROOT, str3, objArr));
    }
}
