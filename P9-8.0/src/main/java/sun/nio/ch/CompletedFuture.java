package sun.nio.ch;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

final class CompletedFuture<V> implements Future<V> {
    private final Throwable exc;
    private final V result;

    private CompletedFuture(V result, Throwable exc) {
        this.result = result;
        this.exc = exc;
    }

    static <V> CompletedFuture<V> withResult(V result) {
        return new CompletedFuture(result, null);
    }

    static <V> CompletedFuture<V> withFailure(Throwable exc) {
        if (!((exc instanceof IOException) || ((exc instanceof SecurityException) ^ 1) == 0)) {
            exc = new IOException(exc);
        }
        return new CompletedFuture(null, exc);
    }

    static <V> CompletedFuture<V> withResult(V result, Throwable exc) {
        if (exc == null) {
            return withResult(result);
        }
        return withFailure(exc);
    }

    public V get() throws ExecutionException {
        if (this.exc == null) {
            return this.result;
        }
        throw new ExecutionException(this.exc);
    }

    public V get(long timeout, TimeUnit unit) throws ExecutionException {
        if (unit == null) {
            throw new NullPointerException();
        } else if (this.exc == null) {
            return this.result;
        } else {
            throw new ExecutionException(this.exc);
        }
    }

    public boolean isCancelled() {
        return false;
    }

    public boolean isDone() {
        return true;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }
}
