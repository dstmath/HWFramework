package ohos.aafwk.utils.log;

import ohos.hiviewdfx.HiLogConstString;

public interface ILogger {
    int debug(LogLabel logLabel, @HiLogConstString String str, Object... objArr);

    int error(LogLabel logLabel, @HiLogConstString String str, Object... objArr);

    int fatal(LogLabel logLabel, @HiLogConstString String str, Object... objArr);

    int info(LogLabel logLabel, @HiLogConstString String str, Object... objArr);

    boolean isDebuggable();

    boolean isLoggable(int i, String str, int i2);

    int warn(LogLabel logLabel, @HiLogConstString String str, Object... objArr);
}
