package ohos.media.utils.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogConstString;
import ohos.hiviewdfx.HiLogLabel;

public final class Logger {
    public static final int LEVEL_DEBUG = 3;
    public static final int LEVEL_ERROR = 6;
    public static final int LEVEL_FATAL = 7;
    public static final int LEVEL_INFO = 4;
    public static final int LEVEL_OFF = Integer.MAX_VALUE;
    public static final int LEVEL_WARN = 5;
    public static final int LOGGER_OFF = -1;
    private final HiLogLabel label;
    private final int level;

    Logger(HiLogLabel hiLogLabel, int i) {
        this.label = hiLogLabel;
        this.level = i;
    }

    public int debug(@HiLogConstString String str, Object... objArr) {
        if (this.level <= 3) {
            return HiLog.debug(this.label, str, objArr);
        }
        return -1;
    }

    public int info(@HiLogConstString String str, Object... objArr) {
        if (this.level <= 4) {
            return HiLog.info(this.label, str, objArr);
        }
        return -1;
    }

    public int warn(@HiLogConstString String str, Object... objArr) {
        if (this.level <= 5) {
            return HiLog.warn(this.label, str, objArr);
        }
        return -1;
    }

    public int error(@HiLogConstString String str, Object... objArr) {
        if (this.level <= 6) {
            return HiLog.error(this.label, str, objArr);
        }
        return -1;
    }

    public int error(@HiLogConstString String str, Throwable th, Object... objArr) {
        if (this.level > 6) {
            return -1;
        }
        HiLog.error(this.label, str, objArr);
        HiLog.error(this.label, "%{public}s", parseStackTrace(th));
        return -1;
    }

    private String parseStackTrace(Throwable th) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        th.printStackTrace(printWriter);
        printWriter.close();
        return stringWriter.toString();
    }

    public int fatal(@HiLogConstString String str, Object... objArr) {
        if (this.level <= 7) {
            return HiLog.fatal(this.label, str, objArr);
        }
        return -1;
    }

    public int fatal(@HiLogConstString String str, Throwable th, Object... objArr) {
        if (this.level > 7) {
            return -1;
        }
        HiLog.fatal(this.label, str, objArr);
        HiLog.fatal(this.label, "%{public}s", parseStackTrace(th));
        return -1;
    }

    public void begin(String str) {
        if (this.level <= 3) {
            debug("%{public}s begin", str);
        }
    }

    public void end(String str) {
        if (this.level <= 3) {
            debug("%{public}s end", str);
        }
    }

    public HiLogLabel getLabel() {
        return this.label;
    }
}
