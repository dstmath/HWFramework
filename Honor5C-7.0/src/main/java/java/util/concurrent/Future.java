package java.util.concurrent;

public interface Future<V> {
    boolean cancel(boolean z);

    V get() throws InterruptedException, ExecutionException;

    V get(long j, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException;

    boolean isCancelled();

    boolean isDone();
}
