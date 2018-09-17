package java.util.concurrent;

public interface RunnableScheduledFuture<V> extends RunnableFuture<V>, ScheduledFuture<V> {
    boolean isPeriodic();
}
