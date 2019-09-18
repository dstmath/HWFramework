package java.util.concurrent;

public interface CompletionService<V> {
    Future<V> poll();

    Future<V> poll(long j, TimeUnit timeUnit) throws InterruptedException;

    Future<V> submit(Runnable runnable, V v);

    Future<V> submit(Callable<V> callable);

    Future<V> take() throws InterruptedException;
}
