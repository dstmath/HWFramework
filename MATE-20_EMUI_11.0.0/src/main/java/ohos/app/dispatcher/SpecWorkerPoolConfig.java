package ohos.app.dispatcher;

import ohos.app.dispatcher.threading.WorkerPoolConfig;

public class SpecWorkerPoolConfig implements WorkerPoolConfig {
    private static final int DEFAULT_CORE_THREAD_COUNT = 16;
    private static final long DEFAULT_KEEP_ALIVE_TIME = 50;
    private static final int DEFAULT_MAX_THREAD_COUNT = 32;
    private int coreThreadCount;
    private long keepAliveTime;
    private int maxThreadCount;

    public SpecWorkerPoolConfig() {
        this.maxThreadCount = 32;
        this.coreThreadCount = 16;
        this.keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
    }

    public SpecWorkerPoolConfig(int i, int i2) {
        this.maxThreadCount = 32;
        this.coreThreadCount = 16;
        this.keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
        this.maxThreadCount = i;
        this.coreThreadCount = i2;
    }

    public SpecWorkerPoolConfig(int i, int i2, long j) {
        this(i, i2);
        this.keepAliveTime = j;
    }

    public SpecWorkerPoolConfig(long j) {
        this.maxThreadCount = 32;
        this.coreThreadCount = 16;
        this.keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
        this.keepAliveTime = j;
    }

    @Override // ohos.app.dispatcher.threading.WorkerPoolConfig
    public int getMaxThreadCount() {
        return this.maxThreadCount;
    }

    @Override // ohos.app.dispatcher.threading.WorkerPoolConfig
    public int getCoreThreadCount() {
        return this.coreThreadCount;
    }

    @Override // ohos.app.dispatcher.threading.WorkerPoolConfig
    public long getKeepAliveTime() {
        return this.keepAliveTime;
    }
}
