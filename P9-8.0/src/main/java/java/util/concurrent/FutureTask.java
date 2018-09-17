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
        Object x = this.outcome;
        if (s == 2) {
            return x;
        }
        if (s >= 4) {
            throw new CancellationException();
        }
        throw new ExecutionException((Throwable) x);
    }

    public FutureTask(Callable<V> callable) {
        if (callable == null) {
            throw new NullPointerException();
        }
        this.callable = callable;
        this.state = 0;
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
        boolean compareAndSwapInt;
        if (this.state == 0) {
            compareAndSwapInt = U.compareAndSwapInt(this, STATE, 0, mayInterruptIfRunning ? 5 : 4);
        } else {
            compareAndSwapInt = false;
        }
        if (!compareAndSwapInt) {
            return false;
        }
        if (mayInterruptIfRunning) {
            try {
                Thread t = this.runner;
                if (t != null) {
                    t.interrupt();
                }
                U.putOrderedInt(this, STATE, 6);
            } catch (Throwable th) {
                finishCompletion();
            }
        }
        finishCompletion();
        return true;
    }

    public V get() throws InterruptedException, ExecutionException {
        int s = this.state;
        if (s <= 1) {
            s = awaitDone(false, 0);
        }
        return report(s);
    }

    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (unit == null) {
            throw new NullPointerException();
        }
        int s = this.state;
        if (s <= 1) {
            s = awaitDone(true, unit.toNanos(timeout));
            if (s <= 1) {
                throw new TimeoutException();
            }
        }
        return report(s);
    }

    protected void done() {
    }

    protected void set(V v) {
        if (U.compareAndSwapInt(this, STATE, 0, 1)) {
            this.outcome = v;
            U.putOrderedInt(this, STATE, 2);
            finishCompletion();
        }
    }

    protected void setException(Throwable t) {
        if (U.compareAndSwapInt(this, STATE, 0, 1)) {
            this.outcome = t;
            U.putOrderedInt(this, STATE, 3);
            finishCompletion();
        }
    }

    public void run() {
        int s;
        if (this.state == 0) {
            if ((U.compareAndSwapObject(this, RUNNER, null, Thread.currentThread()) ^ 1) == 0) {
                Object result;
                boolean ran;
                try {
                    Callable<V> c = this.callable;
                    if (c != null && this.state == 0) {
                        result = c.call();
                        ran = true;
                        if (ran) {
                            set(result);
                        }
                    }
                } catch (Throwable th) {
                    this.runner = null;
                    s = this.state;
                    if (s >= 5) {
                        handlePossibleCancellationInterrupt(s);
                    }
                }
                this.runner = null;
                s = this.state;
                if (s >= 5) {
                    handlePossibleCancellationInterrupt(s);
                }
            }
        }
    }

    protected boolean runAndReset() {
        if (this.state == 0) {
            if ((U.compareAndSwapObject(this, RUNNER, null, Thread.currentThread()) ^ 1) == 0) {
                boolean z;
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
                    s = this.state;
                    if (s >= 5) {
                        handlePossibleCancellationInterrupt(s);
                    }
                }
                this.runner = null;
                s = this.state;
                if (s >= 5) {
                    handlePossibleCancellationInterrupt(s);
                }
                if (ran && s == 0) {
                    z = true;
                } else {
                    z = false;
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

    private void finishCompletion() {
        WaitNode q;
        do {
            q = this.waiters;
            if (q == null) {
                break;
            }
        } while (!U.compareAndSwapObject(this, WAITERS, q, null));
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
        done();
        this.callable = null;
    }

    private int awaitDone(boolean timed, long nanos) throws InterruptedException {
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
                long parkNanos;
                if (startTime == 0) {
                    startTime = System.nanoTime();
                    if (startTime == 0) {
                        startTime = 1;
                    }
                    parkNanos = nanos;
                } else {
                    long elapsed = System.nanoTime() - startTime;
                    if (elapsed >= nanos) {
                        removeWaiter(q);
                        return this.state;
                    }
                    parkNanos = nanos - elapsed;
                }
                if (this.state < 1) {
                    LockSupport.parkNanos(this, parkNanos);
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
                        if (U.compareAndSwapObject(this, WAITERS, q, s)) {
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
            Class<?> ensureLoaded = LockSupport.class;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
}
