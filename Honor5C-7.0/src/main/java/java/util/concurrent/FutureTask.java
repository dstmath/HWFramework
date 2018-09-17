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
    private static final long RUNNER = 0;
    private static final long STATE = 0;
    private static final Unsafe U = null;
    private static final long WAITERS = 0;
    private Callable<V> callable;
    private Object outcome;
    private volatile Thread runner;
    private volatile int state;
    private volatile WaitNode waiters;

    static final class WaitNode {
        volatile WaitNode next;
        volatile Thread thread;

        WaitNode() {
            this.thread = Thread.currentThread();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.FutureTask.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.FutureTask.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.FutureTask.<clinit>():void");
    }

    private V report(int s) throws ExecutionException {
        Object x = this.outcome;
        if (s == NORMAL) {
            return x;
        }
        if (s >= CANCELLED) {
            throw new CancellationException();
        }
        throw new ExecutionException((Throwable) x);
    }

    public FutureTask(Callable<V> callable) {
        if (callable == null) {
            throw new NullPointerException();
        }
        this.callable = callable;
        this.state = NEW;
    }

    public FutureTask(Runnable runnable, V result) {
        this.callable = Executors.callable(runnable, result);
        this.state = NEW;
    }

    public boolean isCancelled() {
        return this.state >= CANCELLED;
    }

    public boolean isDone() {
        return this.state != 0;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean compareAndSwapInt;
        if (this.state == 0) {
            compareAndSwapInt = U.compareAndSwapInt(this, STATE, NEW, mayInterruptIfRunning ? INTERRUPTING : CANCELLED);
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
                U.putOrderedInt(this, STATE, INTERRUPTED);
            } catch (Throwable th) {
                finishCompletion();
            }
        }
        finishCompletion();
        return true;
    }

    public V get() throws InterruptedException, ExecutionException {
        int s = this.state;
        if (s <= COMPLETING) {
            s = awaitDone(false, 0);
        }
        return report(s);
    }

    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (unit == null) {
            throw new NullPointerException();
        }
        int s = this.state;
        if (s <= COMPLETING) {
            s = awaitDone(true, unit.toNanos(timeout));
            if (s <= COMPLETING) {
                throw new TimeoutException();
            }
        }
        return report(s);
    }

    protected void done() {
    }

    protected void set(V v) {
        if (U.compareAndSwapInt(this, STATE, NEW, COMPLETING)) {
            this.outcome = v;
            U.putOrderedInt(this, STATE, NORMAL);
            finishCompletion();
        }
    }

    protected void setException(Throwable t) {
        if (U.compareAndSwapInt(this, STATE, NEW, COMPLETING)) {
            this.outcome = t;
            U.putOrderedInt(this, STATE, EXCEPTIONAL);
            finishCompletion();
        }
    }

    public void run() {
        if (this.state == 0 && U.compareAndSwapObject(this, RUNNER, null, Thread.currentThread())) {
            int s;
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
                if (s >= INTERRUPTING) {
                    handlePossibleCancellationInterrupt(s);
                }
            }
            this.runner = null;
            s = this.state;
            if (s >= INTERRUPTING) {
                handlePossibleCancellationInterrupt(s);
            }
        }
    }

    protected boolean runAndReset() {
        if (this.state != 0 || !U.compareAndSwapObject(this, RUNNER, null, Thread.currentThread())) {
            return false;
        }
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
            if (s >= INTERRUPTING) {
                handlePossibleCancellationInterrupt(s);
            }
        }
        this.runner = null;
        s = this.state;
        if (s >= INTERRUPTING) {
            handlePossibleCancellationInterrupt(s);
        }
        if (ran && s == 0) {
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    private void handlePossibleCancellationInterrupt(int s) {
        if (s == INTERRUPTING) {
            while (this.state == INTERRUPTING) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int awaitDone(boolean timed, long nanos) throws InterruptedException {
        long startTime = 0;
        WaitNode q = null;
        boolean z = false;
        while (true) {
            int s = this.state;
            if (s > COMPLETING) {
                break;
            } else if (s == COMPLETING) {
                Thread.yield();
            } else if (Thread.interrupted()) {
                break;
            } else if (q == null) {
                if (timed && nanos <= 0) {
                    return s;
                }
                q = new WaitNode();
            } else if (!z) {
                Unsafe unsafe = U;
                long j = WAITERS;
                WaitNode waitNode = this.waiters;
                q.next = waitNode;
                z = unsafe.compareAndSwapObject(this, j, waitNode, q);
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
                if (this.state < COMPLETING) {
                    LockSupport.parkNanos(this, parkNanos);
                }
            } else {
                LockSupport.park(this);
            }
        }
        removeWaiter(q);
        throw new InterruptedException();
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
                    } else if (U.compareAndSwapObject(this, WAITERS, q, s)) {
                    }
                    q = s;
                }
                return;
            }
        }
    }
}
