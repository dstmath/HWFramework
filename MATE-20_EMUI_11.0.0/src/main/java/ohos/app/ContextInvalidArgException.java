package ohos.app;

import ohos.appexecfwk.utils.AppLog;

public class ContextInvalidArgException extends RuntimeException {
    public ContextInvalidArgException(String str) {
        super(str);
        AppLog.w("ContextInvalidArgException %{public}s", str);
    }
}
