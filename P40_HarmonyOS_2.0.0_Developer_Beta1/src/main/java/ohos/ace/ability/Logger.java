package ohos.ace.ability;

import com.huawei.ace.runtime.ILogger;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class Logger implements ILogger {
    private static final boolean IS_DEBUGGABLE = HiLog.isDebuggable();
    private static final int LOG_DOMAIN = 218118416;
    private static final String LOG_FORMAT = "%{public}s: %{private}s";
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, (int) LOG_DOMAIN, "Ace");

    @Override // com.huawei.ace.runtime.ILogger
    public boolean isDebuggable() {
        return IS_DEBUGGABLE;
    }

    @Override // com.huawei.ace.runtime.ILogger
    public void d(String str, String str2) {
        HiLog.debug(LOG_LABEL, LOG_FORMAT, new Object[]{str, str2});
    }

    @Override // com.huawei.ace.runtime.ILogger
    public void i(String str, String str2) {
        HiLog.info(LOG_LABEL, LOG_FORMAT, new Object[]{str, str2});
    }

    @Override // com.huawei.ace.runtime.ILogger
    public void w(String str, String str2) {
        HiLog.warn(LOG_LABEL, LOG_FORMAT, new Object[]{str, str2});
    }

    @Override // com.huawei.ace.runtime.ILogger
    public void e(String str, String str2) {
        HiLog.error(LOG_LABEL, LOG_FORMAT, new Object[]{str, str2});
    }
}
