package ohos.app.dispatcher.threading;

public interface WorkerPoolConfig {
    int getCoreThreadCount();

    long getKeepAliveTime();

    int getMaxThreadCount();
}
