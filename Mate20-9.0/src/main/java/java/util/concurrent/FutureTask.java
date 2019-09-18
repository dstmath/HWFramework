package java.util.concurrent;

import java.util.concurrent.locks.LockSupport;
import sun.misc.Unsafe;

public class FutureTask<V> implements RunnableFuture<V> {
    private static final int CANCELLED = 4;
    private static final int COMPLETING = 1;
    private static final int EXCEPTIONAL = 3;
    private static final int INTERRUPTED = 6;
    private static final int INTERRUPTING = 5;
    private static final int NEW = 0;
    private static final int NORMAL = 2;
    private static final long RUNNER;
    private static final long STATE;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long WAITERS;
    private Callable<V> callable;
    private Object outcome;
    private volatile Thread runner;
    private volatile int state;
    private volatile WaitNode waiters;

    static final class WaitNode {
        volatile WaitNode next;
        volatile Thread thread = Thread.currentThread();

        WaitNode() {
        }
    }

    private V report(int s) throws ExecutionException {
        V v = this.outcome;
        if (s == 2) {
            return v;
        }
        if (s >= 4) {
            throw new CancellationException();
        }
        throw new ExecutionException((Throwable) v);
    }

    public FutureTask(Callable<V> callable2) {
        if (callable2 != null) {
            this.callable = callable2;
            this.state = 0;
            return;
        }
        throw new NullPointerException();
    }

    public FutureTask(Runnable runnable, V result) {
        this.callable = Executors.callable(runnable, result);
        this.state = 0;
    }

    public boolean isCancelled() {
        return this.state >= 4;
    }

    public boolean isDone() {
        return this.state != 0;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        if (this.state == 0) {
            if (U.compareAndSwapInt(this, STATE, 0, mayInterruptIfRunning ? 5 : 4)) {
                if (mayInterruptIfRunning) {
                    try {
                        Thread t = this.runner;
                        if (t != null) {
                            t.interrupt();
                        }
                        U.putOrderedInt(this, STATE, 6);
                    } catch (Throwable th) {
                        finishCompletion();
                        throw th;
                    }
                }
                finishCompletion();
                return true;
            }
        }
        return false;
    }

    public V get() throws InterruptedException, ExecutionException {
        int s = this.state;
        if (s <= 1) {
            s = awaitDone(false, 0);
        }
        return report(s);
    }

    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (unit != null) {
            int s = this.state;
            if (s <= 1) {
                int awaitDone = awaitDone(true, unit.toNanos(timeout));
                s = awaitDone;
                if (awaitDone <= 1) {
                    throw new TimeoutException();
                }
            }
            return report(s);
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: protected */
    public void done() {
    }

    /* access modifiers changed from: protected */
    public void set(V v) {
        if (U.compareAndSwapInt(this, STATE, 0, 1)) {
            this.outcome = v;
            U.putOrderedInt(this, STATE, 2);
            finishCompletion();
        }
    }

    /* access modifiers changed from: protected */
    public void setException(Throwable t) {
        if (U.compareAndSwapInt(this, STATE, 0, 1)) {
            this.outcome = t;
            U.putOrderedInt(this, STATE, 3);
            finishCompletion();
        }
    }

    public void run() {
        boolean ran;
        Throwable ex;
        if (this.state == 0) {
            if (U.compareAndSwapObject(this, RUNNER, null, Thread.currentThread())) {
                try {
                    Callable<V> c = this.callable;
                    if (c != null && this.state == 0) {
                        ex = c.call();
                        ran = true;
                        if (ran) {
                            set(ex);
                        }
                    }
                } catch (Throwable th) {
                    this.runner = null;
                    int s = this.state;
                    if (s >= 5) {
                        handlePossibleCancellationInterrupt(s);
                    }
                    throw th;
                }
                this.runner = null;
                int s2 = this.state;
                if (s2 >= 5) {
                    handlePossibleCancellationInterrupt(s2);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean runAndReset() {
        boolean z = false;
        if (this.state == 0) {
            if (U.compareAndSwapObject(this, RUNNER, null, Thread.currentThread())) {
                boolean ran = false;
                int s = this.state;
                try {
                    Callable<V> c = this.callable;
                    if (c != null && s == 0) {
                        c.call();
                        ran = true;
                    }
                } catch (Throwable th) {
                    this.runner = null;
                    int s2 = this.state;
                    if (s2 >= 5) {
                        handlePossibleCancellationInterrupt(s2);
                    }
                    throw th;
                }
                this.runner = null;
                int s3 = this.state;
                if (s3 >= 5) {
                    handlePossibleCancellationInterrupt(s3);
                }
                if (ran && s3 == 0) {
                    z = true;
                }
                return z;
            }
        }
        return false;
    }

    private void handlePossibleCancellationInterrupt(int s) {
        if (s == 5) {
            while (this.state == 5) {
                Thread.yield();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:0:0x0000 A[LOOP_START, MTH_ENTER_BLOCK] */
    private void finishCompletion() {
        while (true) {
            WaitNode waitNode = this.waiters;
            WaitNode q = waitNode;
            if (waitNode == null) {
                break;
            }
            if (U.compareAndSwapObject(this, WAITERS, q, null)) {
                while (true) {
                    Thread t = q.thread;
                    if (t != null) {
                        q.thread = null;
                        LockSupport.unpark(t);
                    }
                    WaitNode next = q.next;
                    if (next == null) {
                        break;
                    }
                    q.next = null;
                    q = next;
                }
            }
        }
        done();
        this.callable = null;
    }

    private int awaitDone(boolean timed, long nanos) throws InterruptedException {
        long elapsed;
        long startTime = 0;
        WaitNode q = null;
        boolean queued = false;
        while (true) {
            int s = this.state;
            if (s > 1) {
                if (q != null) {
                    q.thread = null;
                }
                return s;
            } else if (s == 1) {
                Thread.yield();
            } else if (Thread.interrupted()) {
                removeWaiter(q);
                throw new InterruptedException();
            } else if (q == null) {
                if (timed && nanos <= 0) {
                    return s;
                }
                q = new WaitNode();
            } else if (!queued) {
                Unsafe unsafe = U;
                long j = WAITERS;
                WaitNode waitNode = this.waiters;
                q.next = waitNode;
                queued = unsafe.compareAndSwapObject(this, j, waitNode, q);
            } else if (timed) {
                if (startTime == 0) {
                    startTime = System.nanoTime();
                    if (startTime == 0) {
                        startTime = 1;
                    }
                    elapsed = nanos;
                } else {
                    long elapsed2 = System.nanoTime() - startTime;
                    if (elapsed2 >= nanos) {
                        removeWaiter(q);
                        return this.state;
                    }
                    elapsed = nanos - elapsed2;
                }
                if (this.state < 1) {
                    LockSupport.parkNanos(this, elapsed);
                }
            } else {
                LockSupport.park(this);
            }
        }
    }

    private void removeWaiter(WaitNode node) {
        if (node != null) {
            node.thread = null;
            while (true) {
                WaitNode pred = null;
                WaitNode q = this.waiters;
                while (q != null) {
                    WaitNode s = q.next;
                    if (q.thread != null) {
                        pred = q;
                    } else if (pred != null) {
                        pred.next = s;
                        if (pred.thread == null) {
                        }
                    } else {
                        if (!U.compareAndSwapObject(this, WAITERS, q, s)) {
                        }
                    }
                    q = s;
                }
                return;
            }
        }
    }

    static {
        try {
            STATE = U.objectFieldOffset(FutureTask.class.getDeclaredField("state"));
            RUNNER = U.objectFieldOffset(FutureTask.class.getDeclaredField("runner"));
            WAITERS = U.objectFieldOffset(FutureTask.class.getDeclaredField("waiters"));
            Class<LockSupport> cls = LockSupport.class;
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }
}
