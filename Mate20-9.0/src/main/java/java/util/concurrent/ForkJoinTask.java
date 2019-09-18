package java.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.ReentrantLock;
import sun.misc.Unsafe;

public abstract class ForkJoinTask<V> implements Future<V>, Serializable {
    static final int CANCELLED = -1073741824;
    static final int DONE_MASK = -268435456;
    static final int EXCEPTIONAL = Integer.MIN_VALUE;
    private static final int EXCEPTION_MAP_CAPACITY = 32;
    static final int NORMAL = -268435456;
    static final int SIGNAL = 65536;
    static final int SMASK = 65535;
    private static final long STATUS;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final ExceptionNode[] exceptionTable = new ExceptionNode[32];
    private static final ReentrantLock exceptionTableLock = new ReentrantLock();
    private static final ReferenceQueue<Object> exceptionTableRefQueue = new ReferenceQueue<>();
    private static final long serialVersionUID = -7721805057305804111L;
    volatile int status;

    static final class AdaptedCallable<T> extends ForkJoinTask<T> implements RunnableFuture<T> {
        private static final long serialVersionUID = 2838392045355241008L;
        final Callable<? extends T> callable;
        T result;

        AdaptedCallable(Callable<? extends T> callable2) {
            if (callable2 != null) {
                this.callable = callable2;
                return;
            }
            throw new NullPointerException();
        }

        public final T getRawResult() {
            return this.result;
        }

        public final void setRawResult(T v) {
            this.result = v;
        }

        public final boolean exec() {
            try {
                this.result = this.callable.call();
                return true;
            } catch (RuntimeException rex) {
                throw rex;
            } catch (Exception ex) {
                throw new RuntimeException((Throwable) ex);
            }
        }

        public final void run() {
            invoke();
        }
    }

    static final class AdaptedRunnable<T> extends ForkJoinTask<T> implements RunnableFuture<T> {
        private static final long serialVersionUID = 5232453952276885070L;
        T result;
        final Runnable runnable;

        AdaptedRunnable(Runnable runnable2, T result2) {
            if (runnable2 != null) {
                this.runnable = runnable2;
                this.result = result2;
                return;
            }
            throw new NullPointerException();
        }

        public final T getRawResult() {
            return this.result;
        }

        public final void setRawResult(T v) {
            this.result = v;
        }

        public final boolean exec() {
            this.runnable.run();
            return true;
        }

        public final void run() {
            invoke();
        }
    }

    static final class AdaptedRunnableAction extends ForkJoinTask<Void> implements RunnableFuture<Void> {
        private static final long serialVersionUID = 5232453952276885070L;
        final Runnable runnable;

        AdaptedRunnableAction(Runnable runnable2) {
            if (runnable2 != null) {
                this.runnable = runnable2;
                return;
            }
            throw new NullPointerException();
        }

        public final Void getRawResult() {
            return null;
        }

        public final void setRawResult(Void v) {
        }

        public final boolean exec() {
            this.runnable.run();
            return true;
        }

        public final void run() {
            invoke();
        }
    }

    static final class ExceptionNode extends WeakReference<ForkJoinTask<?>> {
        final Throwable ex;
        final int hashCode;
        ExceptionNode next;
        final long thrower = Thread.currentThread().getId();

        ExceptionNode(ForkJoinTask<?> task, Throwable ex2, ExceptionNode next2, ReferenceQueue<Object> exceptionTableRefQueue) {
            super(task, exceptionTableRefQueue);
            this.ex = ex2;
            this.next = next2;
            this.hashCode = System.identityHashCode(task);
        }
    }

    static final class RunnableExecuteAction extends ForkJoinTask<Void> {
        private static final long serialVersionUID = 5232453952276885070L;
        final Runnable runnable;

        RunnableExecuteAction(Runnable runnable2) {
            if (runnable2 != null) {
                this.runnable = runnable2;
                return;
            }
            throw new NullPointerException();
        }

        public final Void getRawResult() {
            return null;
        }

        public final void setRawResult(Void v) {
        }

        public final boolean exec() {
            this.runnable.run();
            return true;
        }

        /* access modifiers changed from: package-private */
        public void internalPropagateException(Throwable ex) {
            rethrow(ex);
        }
    }

    /* access modifiers changed from: protected */
    public abstract boolean exec();

    public abstract V getRawResult();

    /* access modifiers changed from: protected */
    public abstract void setRawResult(V v);

    private int setCompletion(int completion) {
        int s;
        do {
            int i = this.status;
            s = i;
            if (i < 0) {
                return s;
            }
        } while (!U.compareAndSwapInt(this, STATUS, s, s | completion));
        if ((s >>> 16) != 0) {
            synchronized (this) {
                notifyAll();
            }
        }
        return completion;
    }

    /* access modifiers changed from: package-private */
    public final int doExec() {
        int i = this.status;
        int s = i;
        if (i >= 0) {
            try {
                if (exec()) {
                    s = setCompletion(-268435456);
                }
            } catch (Throwable rex) {
                return setExceptionalCompletion(rex);
            }
        }
        return s;
    }

    /* access modifiers changed from: package-private */
    public final void internalWait(long timeout) {
        int i = this.status;
        int s = i;
        if (i >= 0) {
            if (U.compareAndSwapInt(this, STATUS, s, s | 65536)) {
                synchronized (this) {
                    if (this.status >= 0) {
                        try {
                            wait(timeout);
                        } catch (InterruptedException e) {
                        }
                    } else {
                        notifyAll();
                    }
                }
            }
        }
    }

    private int externalAwaitDone() {
        int s;
        int i;
        boolean interrupted = false;
        if (this instanceof CountedCompleter) {
            s = ForkJoinPool.common.externalHelpComplete((CountedCompleter) this, 0);
        } else {
            s = ForkJoinPool.common.tryExternalUnpush(this) ? doExec() : 0;
        }
        if (s >= 0) {
            int i2 = this.status;
            s = i2;
            if (i2 >= 0) {
                do {
                    if (U.compareAndSwapInt(this, STATUS, s, s | 65536)) {
                        synchronized (this) {
                            if (this.status >= 0) {
                                try {
                                    wait(STATUS);
                                } catch (InterruptedException e) {
                                    interrupted = true;
                                }
                            } else {
                                notifyAll();
                            }
                        }
                    }
                    i = this.status;
                    s = i;
                } while (i >= 0);
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return s;
    }

    private int externalInterruptibleAwaitDone() throws InterruptedException {
        if (!Thread.interrupted()) {
            int i = this.status;
            int s = i;
            if (i >= 0) {
                int i2 = 0;
                if (this instanceof CountedCompleter) {
                    i2 = ForkJoinPool.common.externalHelpComplete((CountedCompleter) this, 0);
                } else if (ForkJoinPool.common.tryExternalUnpush(this)) {
                    i2 = doExec();
                }
                s = i2;
                if (i2 >= 0) {
                    while (true) {
                        int i3 = this.status;
                        s = i3;
                        if (i3 < 0) {
                            break;
                        }
                        if (U.compareAndSwapInt(this, STATUS, s, s | 65536)) {
                            synchronized (this) {
                                if (this.status >= 0) {
                                    wait(STATUS);
                                } else {
                                    notifyAll();
                                }
                            }
                        }
                    }
                }
            }
            return s;
        }
        throw new InterruptedException();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0022, code lost:
        if (r0 < 0) goto L_0x0005;
     */
    private int doJoin() {
        int i = this.status;
        int s = i;
        if (i >= 0) {
            Thread currentThread = Thread.currentThread();
            Thread t = currentThread;
            if (!(currentThread instanceof ForkJoinWorkerThread)) {
                return externalAwaitDone();
            }
            ForkJoinWorkerThread forkJoinWorkerThread = (ForkJoinWorkerThread) t;
            ForkJoinWorkerThread wt = forkJoinWorkerThread;
            ForkJoinPool.WorkQueue workQueue = forkJoinWorkerThread.workQueue;
            ForkJoinPool.WorkQueue w = workQueue;
            if (workQueue.tryUnpush(this)) {
                int doExec = doExec();
                s = doExec;
            }
            return wt.pool.awaitJoin(w, this, STATUS);
        }
        return s;
    }

    private int doInvoke() {
        int doExec = doExec();
        int s = doExec;
        if (doExec < 0) {
            return s;
        }
        Thread currentThread = Thread.currentThread();
        Thread t = currentThread;
        if (!(currentThread instanceof ForkJoinWorkerThread)) {
            return externalAwaitDone();
        }
        ForkJoinWorkerThread wt = (ForkJoinWorkerThread) t;
        return wt.pool.awaitJoin(wt.workQueue, this, STATUS);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public final int recordExceptionalCompletion(Throwable ex) {
        int i = this.status;
        int s = i;
        if (i < 0) {
            return s;
        }
        int h = System.identityHashCode(this);
        ReentrantLock lock = exceptionTableLock;
        lock.lock();
        try {
            expungeStaleExceptions();
            ExceptionNode[] t = exceptionTable;
            int i2 = (t.length - 1) & h;
            ExceptionNode e = t[i2];
            while (true) {
                if (e == null) {
                    t[i2] = new ExceptionNode(this, ex, t[i2], exceptionTableRefQueue);
                    break;
                } else if (e.get() == this) {
                    break;
                } else {
                    e = e.next;
                }
            }
            lock.unlock();
            return setCompletion(Integer.MIN_VALUE);
        } catch (Throwable th) {
            lock.unlock();
            throw th;
        }
    }

    private int setExceptionalCompletion(Throwable ex) {
        int s = recordExceptionalCompletion(ex);
        if ((-268435456 & s) == Integer.MIN_VALUE) {
            internalPropagateException(ex);
        }
        return s;
    }

    /* access modifiers changed from: package-private */
    public void internalPropagateException(Throwable ex) {
    }

    static final void cancelIgnoringExceptions(ForkJoinTask<?> t) {
        if (t != null && t.status >= 0) {
            try {
                t.cancel(false);
            } catch (Throwable th) {
            }
        }
    }

    private void clearExceptionalCompletion() {
        int h = System.identityHashCode(this);
        ReentrantLock lock = exceptionTableLock;
        lock.lock();
        try {
            ExceptionNode[] t = exceptionTable;
            int i = (t.length - 1) & h;
            ExceptionNode e = t[i];
            ExceptionNode pred = null;
            while (true) {
                if (e == null) {
                    break;
                }
                ExceptionNode next = e.next;
                if (e.get() != this) {
                    pred = e;
                    e = next;
                } else if (pred == null) {
                    t[i] = next;
                } else {
                    pred.next = next;
                }
            }
            expungeStaleExceptions();
            this.status = 0;
        } finally {
            lock.unlock();
        }
    }

    private Throwable getThrowableException() {
        int h = System.identityHashCode(this);
        ReentrantLock lock = exceptionTableLock;
        lock.lock();
        try {
            expungeStaleExceptions();
            ExceptionNode[] t = exceptionTable;
            ExceptionNode e = t[(t.length - 1) & h];
            while (e != null && e.get() != this) {
                e = e.next;
            }
            if (e != null) {
                Throwable th = e.ex;
                Throwable ex = th;
                if (th != null) {
                    if (e.thrower != Thread.currentThread().getId()) {
                        try {
                            Constructor<?> noArgCtor = null;
                            for (Constructor<?> c : ex.getClass().getConstructors()) {
                                Class<?>[] ps = c.getParameterTypes();
                                if (ps.length == 0) {
                                    noArgCtor = c;
                                } else if (ps.length == 1 && ps[0] == Throwable.class) {
                                    return (Throwable) c.newInstance(ex);
                                }
                            }
                            if (noArgCtor != null) {
                                Throwable wx = (Throwable) noArgCtor.newInstance(new Object[0]);
                                wx.initCause(ex);
                                return wx;
                            }
                        } catch (Exception e2) {
                        }
                    }
                    return ex;
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    private static void expungeStaleExceptions() {
        while (true) {
            Object poll = exceptionTableRefQueue.poll();
            Object x = poll;
            if (poll == null) {
                return;
            }
            if (x instanceof ExceptionNode) {
                int hashCode = ((ExceptionNode) x).hashCode;
                ExceptionNode[] t = exceptionTable;
                int i = (t.length - 1) & hashCode;
                ExceptionNode e = t[i];
                ExceptionNode pred = null;
                while (true) {
                    if (e == null) {
                        break;
                    }
                    ExceptionNode next = e.next;
                    if (e != x) {
                        pred = e;
                        e = next;
                    } else if (pred == null) {
                        t[i] = next;
                    } else {
                        pred.next = next;
                    }
                }
            }
        }
    }

    static final void helpExpungeStaleExceptions() {
        ReentrantLock lock = exceptionTableLock;
        if (lock.tryLock()) {
            try {
                expungeStaleExceptions();
            } finally {
                lock.unlock();
            }
        }
    }

    static void rethrow(Throwable ex) {
        uncheckedThrow(ex);
    }

    static <T extends Throwable> void uncheckedThrow(Throwable t) throws Throwable {
        if (t != null) {
            throw t;
        }
        throw new Error("Unknown Exception");
    }

    private void reportException(int s) {
        if (s == CANCELLED) {
            throw new CancellationException();
        } else if (s == Integer.MIN_VALUE) {
            rethrow(getThrowableException());
        }
    }

    public final ForkJoinTask<V> fork() {
        Thread currentThread = Thread.currentThread();
        Thread t = currentThread;
        if (currentThread instanceof ForkJoinWorkerThread) {
            ((ForkJoinWorkerThread) t).workQueue.push(this);
        } else {
            ForkJoinPool.common.externalPush(this);
        }
        return this;
    }

    public final V join() {
        int doJoin = doJoin() & -268435456;
        int s = doJoin;
        if (doJoin != -268435456) {
            reportException(s);
        }
        return getRawResult();
    }

    public final V invoke() {
        int doInvoke = doInvoke() & -268435456;
        int s = doInvoke;
        if (doInvoke != -268435456) {
            reportException(s);
        }
        return getRawResult();
    }

    public static void invokeAll(ForkJoinTask<?> t1, ForkJoinTask<?> t2) {
        t2.fork();
        int doInvoke = t1.doInvoke() & -268435456;
        int s1 = doInvoke;
        if (doInvoke != -268435456) {
            t1.reportException(s1);
        }
        int doJoin = t2.doJoin() & -268435456;
        int s2 = doJoin;
        if (doJoin != -268435456) {
            t2.reportException(s2);
        }
    }

    public static void invokeAll(ForkJoinTask<?>... tasks) {
        int i = 1;
        int last = tasks.length - 1;
        Throwable ex = null;
        for (int i2 = last; i2 >= 0; i2--) {
            ForkJoinTask<?> t = tasks[i2];
            if (t == null) {
                if (ex == null) {
                    ex = new NullPointerException();
                }
            } else if (i2 != 0) {
                t.fork();
            } else if (t.doInvoke() < -268435456 && ex == null) {
                ex = t.getException();
            }
        }
        while (true) {
            int i3 = i;
            if (i3 > last) {
                break;
            }
            ForkJoinTask<?> t2 = tasks[i3];
            if (t2 != null) {
                if (ex != null) {
                    t2.cancel(false);
                } else if (t2.doJoin() < -268435456) {
                    ex = t2.getException();
                }
            }
            i = i3 + 1;
        }
        if (ex != null) {
            rethrow(ex);
        }
    }

    public static <T extends ForkJoinTask<?>> Collection<T> invokeAll(Collection<T> tasks) {
        if (!(tasks instanceof RandomAccess) || !(tasks instanceof List)) {
            invokeAll((ForkJoinTask<?>[]) (ForkJoinTask[]) tasks.toArray(new ForkJoinTask[tasks.size()]));
            return tasks;
        }
        List<? extends ForkJoinTask<?>> ts = (List) tasks;
        int i = 1;
        int last = ts.size() - 1;
        Throwable ex = null;
        for (int i2 = last; i2 >= 0; i2--) {
            ForkJoinTask<?> t = (ForkJoinTask) ts.get(i2);
            if (t == null) {
                if (ex == null) {
                    ex = new NullPointerException();
                }
            } else if (i2 != 0) {
                t.fork();
            } else if (t.doInvoke() < -268435456 && ex == null) {
                ex = t.getException();
            }
        }
        while (true) {
            int i3 = i;
            if (i3 > last) {
                break;
            }
            ForkJoinTask<?> t2 = (ForkJoinTask) ts.get(i3);
            if (t2 != null) {
                if (ex != null) {
                    t2.cancel(false);
                } else if (t2.doJoin() < -268435456) {
                    ex = t2.getException();
                }
            }
            i = i3 + 1;
        }
        if (ex != null) {
            rethrow(ex);
        }
        return tasks;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return (setCompletion(CANCELLED) & -268435456) == CANCELLED;
    }

    public final boolean isDone() {
        return this.status < 0;
    }

    public final boolean isCancelled() {
        return (this.status & -268435456) == CANCELLED;
    }

    public final boolean isCompletedAbnormally() {
        return this.status < -268435456;
    }

    public final boolean isCompletedNormally() {
        return (this.status & -268435456) == -268435456;
    }

    public final Throwable getException() {
        int s = this.status & -268435456;
        if (s >= -268435456) {
            return null;
        }
        if (s == CANCELLED) {
            return new CancellationException();
        }
        return getThrowableException();
    }

    public void completeExceptionally(Throwable ex) {
        Throwable th;
        if ((ex instanceof RuntimeException) || (ex instanceof Error)) {
            th = ex;
        } else {
            th = new RuntimeException(ex);
        }
        setExceptionalCompletion(th);
    }

    public void complete(V value) {
        try {
            setRawResult(value);
            setCompletion(-268435456);
        } catch (Throwable rex) {
            setExceptionalCompletion(rex);
        }
    }

    public final void quietlyComplete() {
        setCompletion(-268435456);
    }

    public final V get() throws InterruptedException, ExecutionException {
        int doJoin = -268435456 & (Thread.currentThread() instanceof ForkJoinWorkerThread ? doJoin() : externalInterruptibleAwaitDone());
        int s = doJoin;
        if (doJoin == CANCELLED) {
            throw new CancellationException();
        } else if (s != Integer.MIN_VALUE) {
            return getRawResult();
        } else {
            throw new ExecutionException(getThrowableException());
        }
    }

    public final V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        int s;
        Thread t;
        long deadline;
        long nanos = unit.toNanos(timeout);
        if (!Thread.interrupted()) {
            int i = this.status;
            int s2 = i;
            if (i >= 0 && nanos > STATUS) {
                long d = System.nanoTime() + nanos;
                long deadline2 = d == STATUS ? 1 : d;
                Thread t2 = Thread.currentThread();
                if (t2 instanceof ForkJoinWorkerThread) {
                    ForkJoinWorkerThread wt = (ForkJoinWorkerThread) t2;
                    s2 = wt.pool.awaitJoin(wt.workQueue, this, deadline2);
                } else {
                    int i2 = 0;
                    if (this instanceof CountedCompleter) {
                        i2 = ForkJoinPool.common.externalHelpComplete((CountedCompleter) this, 0);
                    } else if (ForkJoinPool.common.tryExternalUnpush(this)) {
                        i2 = doExec();
                    }
                    s2 = i2;
                    if (i2 >= 0) {
                        while (true) {
                            int i3 = this.status;
                            s = i3;
                            if (i3 < 0) {
                                break;
                            }
                            long nanoTime = deadline2 - System.nanoTime();
                            long ns = nanoTime;
                            if (nanoTime <= STATUS) {
                                break;
                            }
                            long ns2 = ns;
                            long ns3 = TimeUnit.NANOSECONDS.toMillis(ns2);
                            long ms = ns3;
                            if (ns3 > STATUS) {
                                long j = ns2;
                                t = t2;
                                deadline = deadline2;
                                if (U.compareAndSwapInt(this, STATUS, s, s | 65536)) {
                                    synchronized (this) {
                                        try {
                                            if (this.status >= 0) {
                                                wait(ms);
                                            } else {
                                                notifyAll();
                                            }
                                        } catch (Throwable th) {
                                            th = th;
                                            throw th;
                                        }
                                    }
                                }
                            } else {
                                t = t2;
                                deadline = deadline2;
                            }
                            int i4 = s;
                            deadline2 = deadline;
                            t2 = t;
                        }
                        s2 = s;
                    }
                }
            }
            if (s2 >= 0) {
                s2 = this.status;
            }
            int i5 = s2 & -268435456;
            int s3 = i5;
            if (i5 == -268435456) {
                return getRawResult();
            }
            if (s3 == CANCELLED) {
                throw new CancellationException();
            } else if (s3 != Integer.MIN_VALUE) {
                throw new TimeoutException();
            } else {
                throw new ExecutionException(getThrowableException());
            }
        } else {
            throw new InterruptedException();
        }
    }

    public final void quietlyJoin() {
        doJoin();
    }

    public final void quietlyInvoke() {
        doInvoke();
    }

    public static void helpQuiesce() {
        Thread currentThread = Thread.currentThread();
        Thread t = currentThread;
        if (currentThread instanceof ForkJoinWorkerThread) {
            ForkJoinWorkerThread wt = (ForkJoinWorkerThread) t;
            wt.pool.helpQuiescePool(wt.workQueue);
            return;
        }
        ForkJoinPool.quiesceCommonPool();
    }

    public void reinitialize() {
        if ((this.status & -268435456) == Integer.MIN_VALUE) {
            clearExceptionalCompletion();
        } else {
            this.status = 0;
        }
    }

    public static ForkJoinPool getPool() {
        Thread t = Thread.currentThread();
        if (t instanceof ForkJoinWorkerThread) {
            return ((ForkJoinWorkerThread) t).pool;
        }
        return null;
    }

    public static boolean inForkJoinPool() {
        return Thread.currentThread() instanceof ForkJoinWorkerThread;
    }

    public boolean tryUnfork() {
        Thread currentThread = Thread.currentThread();
        Thread t = currentThread;
        if (currentThread instanceof ForkJoinWorkerThread) {
            return ((ForkJoinWorkerThread) t).workQueue.tryUnpush(this);
        }
        return ForkJoinPool.common.tryExternalUnpush(this);
    }

    public static int getQueuedTaskCount() {
        ForkJoinPool.WorkQueue q;
        Thread currentThread = Thread.currentThread();
        Thread t = currentThread;
        if (currentThread instanceof ForkJoinWorkerThread) {
            q = ((ForkJoinWorkerThread) t).workQueue;
        } else {
            q = ForkJoinPool.commonSubmitterQueue();
        }
        if (q == null) {
            return 0;
        }
        return q.queueSize();
    }

    public static int getSurplusQueuedTaskCount() {
        return ForkJoinPool.getSurplusQueuedTaskCount();
    }

    protected static ForkJoinTask<?> peekNextLocalTask() {
        ForkJoinPool.WorkQueue q;
        Thread currentThread = Thread.currentThread();
        Thread t = currentThread;
        if (currentThread instanceof ForkJoinWorkerThread) {
            q = ((ForkJoinWorkerThread) t).workQueue;
        } else {
            q = ForkJoinPool.commonSubmitterQueue();
        }
        if (q == null) {
            return null;
        }
        return q.peek();
    }

    protected static ForkJoinTask<?> pollNextLocalTask() {
        Thread currentThread = Thread.currentThread();
        Thread t = currentThread;
        if (currentThread instanceof ForkJoinWorkerThread) {
            return ((ForkJoinWorkerThread) t).workQueue.nextLocalTask();
        }
        return null;
    }

    protected static ForkJoinTask<?> pollTask() {
        Thread currentThread = Thread.currentThread();
        Thread t = currentThread;
        if (!(currentThread instanceof ForkJoinWorkerThread)) {
            return null;
        }
        ForkJoinWorkerThread wt = (ForkJoinWorkerThread) t;
        return wt.pool.nextTaskFor(wt.workQueue);
    }

    protected static ForkJoinTask<?> pollSubmission() {
        Thread currentThread = Thread.currentThread();
        Thread t = currentThread;
        if (currentThread instanceof ForkJoinWorkerThread) {
            return ((ForkJoinWorkerThread) t).pool.pollSubmission();
        }
        return null;
    }

    public final short getForkJoinTaskTag() {
        return (short) this.status;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v0, resolved type: short} */
    /* JADX WARNING: Multi-variable type inference failed */
    public final short setForkJoinTaskTag(short newValue) {
        Unsafe unsafe;
        long j;
        int i;
        int s;
        do {
            unsafe = U;
            j = STATUS;
            i = this.status;
            s = i;
        } while (!unsafe.compareAndSwapInt(this, j, i, (SMASK & newValue) | (-65536 & s)));
        return (short) s;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v0, resolved type: short} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v0, resolved type: short} */
    /* JADX WARNING: Multi-variable type inference failed */
    public final boolean compareAndSetForkJoinTaskTag(short expect, short update) {
        int s;
        do {
            int i = this.status;
            s = i;
            if (((short) i) != expect) {
                return false;
            }
        } while (!U.compareAndSwapInt(this, STATUS, s, (-65536 & s) | (SMASK & update)));
        return true;
    }

    public static ForkJoinTask<?> adapt(Runnable runnable) {
        return new AdaptedRunnableAction(runnable);
    }

    public static <T> ForkJoinTask<T> adapt(Runnable runnable, T result) {
        return new AdaptedRunnable(runnable, result);
    }

    public static <T> ForkJoinTask<T> adapt(Callable<? extends T> callable) {
        return new AdaptedCallable(callable);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeObject(getException());
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        Object ex = s.readObject();
        if (ex != null) {
            setExceptionalCompletion((Throwable) ex);
        }
    }

    static {
        try {
            STATUS = U.objectFieldOffset(ForkJoinTask.class.getDeclaredField("status"));
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }
}
