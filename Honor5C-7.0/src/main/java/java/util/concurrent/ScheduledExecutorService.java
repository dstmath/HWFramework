package java.util.concurrent;

public interface ScheduledExecutorService extends ExecutorService {
    ScheduledFuture<?> schedule(Runnable runnable, long j, TimeUnit timeUnit);

    <V> ScheduledFuture<V> schedule(Callable<V> callable, long j, TimeUnit timeUnit);

    ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long j, long j2, TimeUnit timeUnit);

    ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long j, long j2, TimeUnit timeUnit);
}
