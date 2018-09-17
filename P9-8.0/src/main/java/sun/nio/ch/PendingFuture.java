package sun.nio.ch;

import java.io.IOException;
import java.nio.channels.AsynchronousChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class PendingFuture<V, A> implements Future<V> {
    private static final CancellationException CANCELLED = new CancellationException();
    private final A attachment;
    private final AsynchronousChannel channel;
    private volatile Object context;
    private volatile Throwable exc;
    private final CompletionHandler<V, ? super A> handler;
    private volatile boolean haveResult;
    private CountDownLatch latch;
    private volatile V result;
    private Future<?> timeoutTask;

    PendingFuture(AsynchronousChannel channel, CompletionHandler<V, ? super A> handler, A attachment, Object context) {
        this.channel = channel;
        this.handler = handler;
        this.attachment = attachment;
        this.context = context;
    }

    PendingFuture(AsynchronousChannel channel, CompletionHandler<V, ? super A> handler, A attachment) {
        this.channel = channel;
        this.handler = handler;
        this.attachment = attachment;
    }

    PendingFuture(AsynchronousChannel channel) {
        this(channel, null, null);
    }

    PendingFuture(AsynchronousChannel channel, Object context) {
        this(channel, null, null, context);
    }

    AsynchronousChannel channel() {
        return this.channel;
    }

    CompletionHandler<V, ? super A> handler() {
        return this.handler;
    }

    A attachment() {
        return this.attachment;
    }

    void setContext(Object context) {
        this.context = context;
    }

    Object getContext() {
        return this.context;
    }

    void setTimeoutTask(Future<?> task) {
        synchronized (this) {
            if (this.haveResult) {
                task.cancel(false);
            } else {
                this.timeoutTask = task;
            }
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0016, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean prepareForWait() {
        synchronized (this) {
            if (this.haveResult) {
                return false;
            } else if (this.latch == null) {
                this.latch = new CountDownLatch(1);
            }
        }
    }

    /* JADX WARNING: Missing block: B:14:0x0020, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void setResult(V res) {
        synchronized (this) {
            if (this.haveResult) {
                return;
            }
            this.result = res;
            this.haveResult = true;
            if (this.timeoutTask != null) {
                this.timeoutTask.cancel(false);
            }
            if (this.latch != null) {
                this.latch.countDown();
            }
        }
    }

    /* JADX WARNING: Missing block: B:19:0x0030, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void setFailure(Throwable x) {
        if (!((x instanceof IOException) || ((x instanceof SecurityException) ^ 1) == 0)) {
            x = new IOException(x);
        }
        synchronized (this) {
            if (this.haveResult) {
                return;
            }
            this.exc = x;
            this.haveResult = true;
            if (this.timeoutTask != null) {
                this.timeoutTask.cancel(false);
            }
            if (this.latch != null) {
                this.latch.countDown();
            }
        }
    }

    void setResult(V res, Throwable x) {
        if (x == null) {
            setResult(res);
        } else {
            setFailure(x);
        }
    }

    public V get() throws ExecutionException, InterruptedException {
        if (!this.haveResult && prepareForWait()) {
            this.latch.await();
        }
        if (this.exc == null) {
            return this.result;
        }
        if (this.exc == CANCELLED) {
            throw new CancellationException();
        }
        throw new ExecutionException(this.exc);
    }

    public V get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        if (!this.haveResult && prepareForWait() && !this.latch.await(timeout, unit)) {
            throw new TimeoutException();
        } else if (this.exc == null) {
            return this.result;
        } else {
            if (this.exc == CANCELLED) {
                throw new CancellationException();
            }
            throw new ExecutionException(this.exc);
        }
    }

    Throwable exception() {
        return this.exc != CANCELLED ? this.exc : null;
    }

    V value() {
        return this.result;
    }

    public boolean isCancelled() {
        return this.exc == CANCELLED;
    }

    public boolean isDone() {
        return this.haveResult;
    }

    /* JADX WARNING: Missing block: B:15:0x002c, code:
            if (r5 == false) goto L_0x0035;
     */
    /* JADX WARNING: Missing block: B:17:?, code:
            channel().close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean cancel(boolean mayInterruptIfRunning) {
        synchronized (this) {
            if (this.haveResult) {
                return false;
            }
            if (channel() instanceof Cancellable) {
                ((Cancellable) channel()).onCancel(this);
            }
            this.exc = CANCELLED;
            this.haveResult = true;
            if (this.timeoutTask != null) {
                this.timeoutTask.cancel(false);
            }
        }
        if (this.latch != null) {
            this.latch.countDown();
        }
        return true;
    }
}
