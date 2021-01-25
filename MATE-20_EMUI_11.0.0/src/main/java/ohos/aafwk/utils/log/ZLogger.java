package ohos.aafwk.utils.log;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogConstString;

/* access modifiers changed from: package-private */
public enum ZLogger implements ILogger {
    INSTANCE;

    @Override // ohos.aafwk.utils.log.ILogger
    public int debug(LogLabel logLabel, @HiLogConstString String str, Object... objArr) {
        return HiLog.debug(logLabel.getLabel(), str, objArr);
    }

    @Override // ohos.aafwk.utils.log.ILogger
    public int info(LogLabel logLabel, @HiLogConstString String str, Object... objArr) {
        return HiLog.info(logLabel.getLabel(), str, objArr);
    }

    @Override // ohos.aafwk.utils.log.ILogger
    public int warn(LogLabel logLabel, @HiLogConstString String str, Object... objArr) {
        return HiLog.warn(logLabel.getLabel(), str, objArr);
    }

    @Override // ohos.aafwk.utils.log.ILogger
    public int error(LogLabel logLabel, @HiLogConstString String str, Object... objArr) {
        return HiLog.error(logLabel.getLabel(), str, objArr);
    }

    @Override // ohos.aafwk.utils.log.ILogger
    public int fatal(LogLabel logLabel, @HiLogConstString String str, Object... objArr) {
        return HiLog.fatal(logLabel.getLabel(), str, objArr);
    }

    @Override // ohos.aafwk.utils.log.ILogger
    public boolean isDebuggable() {
        return HiLog.isDebuggable();
    }

    @Override // ohos.aafwk.utils.log.ILogger
    public boolean isLoggable(int i, String str, int i2) {
        return HiLog.isLoggable(i, str, i2);
    }
}
