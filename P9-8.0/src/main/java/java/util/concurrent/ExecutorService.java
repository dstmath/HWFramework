package java.util.concurrent;

import java.util.Collection;
import java.util.List;

public interface ExecutorService extends Executor {
    boolean awaitTermination(long j, TimeUnit timeUnit) throws InterruptedException;

    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection) throws InterruptedException;

    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection, long j, TimeUnit timeUnit) throws InterruptedException;

    <T> T invokeAny(Collection<? extends Callable<T>> collection) throws InterruptedException, ExecutionException;

    <T> T invokeAny(Collection<? extends Callable<T>> collection, long j, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException;

    boolean isShutdown();

    boolean isTerminated();

    void shutdown();

    List<Runnable> shutdownNow();

    Future<?> submit(Runnable runnable);

    <T> Future<T> submit(Runnable runnable, T t);

    <T> Future<T> submit(Callable<T> callable);
}
