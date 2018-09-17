package java.util.concurrent;

public interface RejectedExecutionHandler {
    void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor);
}
