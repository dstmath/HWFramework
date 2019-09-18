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

    PendingFuture(AsynchronousChannel channel2, CompletionHandler<V, ? super A> handler2, A attachment2, Object context2) {
        this.channel = channel2;
        this.handler = handler2;
        this.attachment = attachment2;
        this.context = context2;
    }

    PendingFuture(AsynchronousChannel channel2, CompletionHandler<V, ? super A> handler2, A attachment2) {
        this.channel = channel2;
        this.handler = handler2;
        this.attachment = attachment2;
    }

    PendingFuture(AsynchronousChannel channel2) {
        this(channel2, null, null);
    }

    PendingFuture(AsynchronousChannel channel2, Object context2) {
        this(channel2, null, null, context2);
    }

    /* access modifiers changed from: package-private */
    public AsynchronousChannel channel() {
        return this.channel;
    }

    /* access modifiers changed from: package-private */
    public CompletionHandler<V, ? super A> handler() {
        return this.handler;
    }

    /* access modifiers changed from: package-private */
    public A attachment() {
        return this.attachment;
    }

    /* access modifiers changed from: package-private */
    public void setContext(Object context2) {
        this.context = context2;
    }

    /* access modifiers changed from: package-private */
    public Object getContext() {
        return this.context;
    }

    /* access modifiers changed from: package-private */
    public void setTimeoutTask(Future<?> task) {
        synchronized (this) {
            if (this.haveResult) {
                task.cancel(false);
            } else {
                this.timeoutTask = task;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0015, code lost:
        return true;
     */
    private boolean prepareForWait() {
        synchronized (this) {
            if (this.haveResult) {
                return false;
            }
            if (this.latch == null) {
                this.latch = new CountDownLatch(1);
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0020, code lost:
        return;
     */
    public void setResult(V res) {
        synchronized (this) {
            if (!this.haveResult) {
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
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x002e, code lost:
        return;
     */
    public void setFailure(Throwable x) {
        if (!(x instanceof IOException) && !(x instanceof SecurityException)) {
            x = new IOException(x);
        }
        synchronized (this) {
            if (!this.haveResult) {
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
    }

    /* access modifiers changed from: package-private */
    public void setResult(V res, Throwable x) {
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

    /* access modifiers changed from: package-private */
    public Throwable exception() {
        if (this.exc != CANCELLED) {
            return this.exc;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public V value() {
        return this.result;
    }

    public boolean isCancelled() {
        return this.exc == CANCELLED;
    }

    public boolean isDone() {
        return this.haveResult;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002a, code lost:
        if (r4 == false) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        channel().close();
     */
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
