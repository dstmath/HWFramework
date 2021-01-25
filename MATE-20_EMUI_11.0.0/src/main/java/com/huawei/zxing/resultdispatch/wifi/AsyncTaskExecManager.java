package com.huawei.zxing.resultdispatch.wifi;

public final class AsyncTaskExecManager extends PlatformSupportManager<AsyncTaskExecInterface> {
    public AsyncTaskExecManager() {
        super(AsyncTaskExecInterface.class, new DefaultAsyncTaskExecInterface());
        addImplementationClass(11, "com.huawei.zxing.resultdispatch.wifi.HoneycombAsyncTaskExecInterface");
    }
}
