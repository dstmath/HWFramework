package ohos.startup.utils;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public final class StartUpStringUtil {
    private StartUpStringUtil() {
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static void printException(HiLogLabel hiLogLabel, Throwable th) {
        printException(hiLogLabel, th, "Exception", true);
    }

    public static void printException(HiLogLabel hiLogLabel, Throwable th, String str) {
        printException(hiLogLabel, th, str, true);
    }

    public static void printException(HiLogLabel hiLogLabel, Throwable th, String str, boolean z) {
        if (isEmpty(str)) {
            str = "exception";
        }
        if (HiLog.isDebuggable()) {
            HiLog.error(hiLogLabel, "%{public}s : %{public}s", str, th);
            StackTraceElement[] stackTrace = th.getStackTrace();
            int length = stackTrace.length;
            for (int i = 0; i < length; i++) {
                HiLog.error(hiLogLabel, "%{public}s", stackTrace[i]);
            }
        } else if (z) {
            HiLog.error(hiLogLabel, "%{public}s : = %{public}s", str, th.getMessage());
        } else {
            HiLog.error(hiLogLabel, "%{public}s", str);
        }
    }

    public static void printDebug(HiLogLabel hiLogLabel, String str) {
        printDebug(hiLogLabel, str, null);
    }

    public static void printDebug(HiLogLabel hiLogLabel, String str, String str2) {
        if (!HiLog.isDebuggable()) {
            return;
        }
        if (isEmpty(str2)) {
            HiLog.debug(hiLogLabel, "%{public}s", str);
        } else {
            HiLog.debug(hiLogLabel, "%{public}s : = %{public}s", str, str2);
        }
    }
}
