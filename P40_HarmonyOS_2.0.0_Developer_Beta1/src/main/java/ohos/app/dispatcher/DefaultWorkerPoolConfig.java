package ohos.app.dispatcher;

import ohos.app.dispatcher.threading.WorkerPoolConfig;

public class DefaultWorkerPoolConfig implements WorkerPoolConfig {
    private static final int DEFAULT_CORE_THREAD_COUNT = 16;
    private static final long DEFAULT_KEEP_ALIVE_TIME = 50;
    private static final int DEFAULT_MAX_THREAD_COUNT = 32;

    @Override // ohos.app.dispatcher.threading.WorkerPoolConfig
    public int getCoreThreadCount() {
        return 16;
    }

    @Override // ohos.app.dispatcher.threading.WorkerPoolConfig
    public long getKeepAliveTime() {
        return DEFAULT_KEEP_ALIVE_TIME;
    }

    @Override // ohos.app.dispatcher.threading.WorkerPoolConfig
    public int getMaxThreadCount() {
        return 32;
    }
}
