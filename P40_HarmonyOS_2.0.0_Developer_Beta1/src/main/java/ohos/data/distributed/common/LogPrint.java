package ohos.data.distributed.common;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogConstString;
import ohos.hiviewdfx.HiLogLabel;

public class LogPrint {
    private static final String FORMAT = "%{public}s";
    private static final HiLogLabel T_ZDDS = new HiLogLabel(3, 218109456, "ZDDSJ");

    public static final void debug(@HiLogConstString String str, @HiLogConstString String str2, Object... objArr) {
        HiLogLabel hiLogLabel = T_ZDDS;
        HiLog.debug(hiLogLabel, FORMAT, new Object[]{str + ": " + str2, objArr});
    }

    public static final void info(@HiLogConstString String str, @HiLogConstString String str2, Object... objArr) {
        HiLogLabel hiLogLabel = T_ZDDS;
        HiLog.info(hiLogLabel, FORMAT, new Object[]{str + ": " + str2, objArr});
    }

    public static final void warn(@HiLogConstString String str, @HiLogConstString String str2, Object... objArr) {
        HiLogLabel hiLogLabel = T_ZDDS;
        HiLog.warn(hiLogLabel, FORMAT, new Object[]{str + ": " + str2, objArr});
    }

    public static final void error(@HiLogConstString String str, @HiLogConstString String str2, Object... objArr) {
        HiLogLabel hiLogLabel = T_ZDDS;
        HiLog.error(hiLogLabel, FORMAT, new Object[]{str + ": " + str2, objArr});
    }
}
