package ohos.hiviewdfx;

import android.util.Log;

public final class HiLog {
    public static final int DEBUG = 3;
    public static final int ERROR = 6;
    public static final int FATAL = 7;
    public static final int INFO = 4;
    public static final int LOG_CORE = 3;
    public static final int LOG_INIT = 1;
    public static final int WARN = 5;

    private HiLog() {
    }

    public static int debug(HiLogLabel hiLogLabel, @HiLogConstString String str, Object... objArr) {
        return com.huawei.android.app.HiLog.debug(hiLogLabel.label, str, objArr);
    }

    public static int info(HiLogLabel hiLogLabel, @HiLogConstString String str, Object... objArr) {
        return com.huawei.android.app.HiLog.info(hiLogLabel.label, str, objArr);
    }

    public static int warn(HiLogLabel hiLogLabel, @HiLogConstString String str, Object... objArr) {
        return com.huawei.android.app.HiLog.warn(hiLogLabel.label, str, objArr);
    }

    public static int error(HiLogLabel hiLogLabel, @HiLogConstString String str, Object... objArr) {
        return com.huawei.android.app.HiLog.error(hiLogLabel.label, str, objArr);
    }

    public static int fatal(HiLogLabel hiLogLabel, @HiLogConstString String str, Object... objArr) {
        return com.huawei.android.app.HiLog.fatal(hiLogLabel.label, str, objArr);
    }

    public static boolean isDebuggable() {
        return android.util.HiLog.isDebuggable();
    }

    public static boolean isLoggable(int i, String str, int i2) {
        return com.huawei.android.app.HiLog.isLoggable(i, str, i2);
    }

    public static String getStackTrace(Throwable th) {
        return Log.getStackTraceString(th);
    }
}
