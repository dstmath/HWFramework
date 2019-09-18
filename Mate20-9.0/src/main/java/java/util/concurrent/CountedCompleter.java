package java.util.concurrent;

import sun.misc.Unsafe;

public abstract class CountedCompleter<T> extends ForkJoinTask<T> {
    private static final long PENDING;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long serialVersionUID = 5232453752276485070L;
    final CountedCompleter<?> completer;
    volatile int pending;

    public abstract void compute();

    protected CountedCompleter(CountedCompleter<?> completer2, int initialPendingCount) {
        this.completer = completer2;
        this.pending = initialPendingCount;
    }

    protected CountedCompleter(CountedCompleter<?> completer2) {
        this.completer = completer2;
    }

    protected CountedCompleter() {
        this.completer = null;
    }

    public void onCompletion(CountedCompleter<?> countedCompleter) {
    }

    public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> countedCompleter) {
        return true;
    }

    public final CountedCompleter<?> getCompleter() {
        return this.completer;
    }

    public final int getPendingCount() {
        return this.pending;
    }

    public final void setPendingCount(int count) {
        this.pending = count;
    }

    public final void addToPendingCount(int delta) {
        U.getAndAddInt(this, PENDING, delta);
    }

    public final boolean compareAndSetPendingCount(int expected, int count) {
        return U.compareAndSwapInt(this, PENDING, expected, count);
    }

    public final int decrementPendingCountUnlessZero() {
        int c;
        do {
            int i = this.pending;
            c = i;
            if (i == 0) {
                break;
            }
        } while (!U.compareAndSwapInt(this, PENDING, c, c - 1));
        return c;
    }

    public final CountedCompleter<?> getRoot() {
        CountedCompleter<?> a = this;
        while (true) {
            CountedCompleter<?> countedCompleter = a.completer;
            CountedCompleter<?> p = countedCompleter;
            if (countedCompleter == null) {
                return a;
            }
            a = p;
        }
    }

    public final void tryComplete() {
        CountedCompleter countedCompleter = this;
        CountedCompleter<?> a = countedCompleter;
        while (true) {
            int i = a.pending;
            int c = i;
            if (i == 0) {
                a.onCompletion(countedCompleter);
                countedCompleter = a;
                CountedCompleter<?> countedCompleter2 = a.completer;
                a = countedCompleter2;
                if (countedCompleter2 == null) {
                    countedCompleter.quietlyComplete();
                    return;
                }
            } else {
                if (U.compareAndSwapInt(a, PENDING, c, c - 1)) {
                    return;
                }
            }
        }
    }

    public final void propagateCompletion() {
        CountedCompleter<?> a = this;
        while (true) {
            int i = a.pending;
            int c = i;
            if (i == 0) {
                CountedCompleter countedCompleter = a;
                CountedCompleter<?> countedCompleter2 = a.completer;
                a = countedCompleter2;
                if (countedCompleter2 == null) {
                    countedCompleter.quietlyComplete();
                    return;
                }
            } else {
                if (U.compareAndSwapInt(a, PENDING, c, c - 1)) {
                    return;
                }
            }
        }
    }

    public void complete(T rawResult) {
        setRawResult(rawResult);
        onCompletion(this);
        quietlyComplete();
        CountedCompleter<?> countedCompleter = this.completer;
        CountedCompleter<?> p = countedCompleter;
        if (countedCompleter != null) {
            p.tryComplete();
        }
    }

    public final CountedCompleter<?> firstComplete() {
        int c;
        do {
            int i = this.pending;
            c = i;
            if (i == 0) {
                return this;
            }
        } while (!U.compareAndSwapInt(this, PENDING, c, c - 1));
        return null;
    }

    public final CountedCompleter<?> nextComplete() {
        CountedCompleter<?> countedCompleter = this.completer;
        CountedCompleter<?> p = countedCompleter;
        if (countedCompleter != null) {
            return p.firstComplete();
        }
        quietlyComplete();
        return null;
    }

    public final void quietlyCompleteRoot() {
        CountedCompleter<?> a = this;
        while (true) {
            CountedCompleter<?> countedCompleter = a.completer;
            CountedCompleter<?> p = countedCompleter;
            if (countedCompleter == null) {
                a.quietlyComplete();
                return;
            }
            a = p;
        }
    }

    public final void helpComplete(int maxTasks) {
        if (maxTasks > 0 && this.status >= 0) {
            Thread currentThread = Thread.currentThread();
            Thread t = currentThread;
            if (currentThread instanceof ForkJoinWorkerThread) {
                ForkJoinWorkerThread wt = (ForkJoinWorkerThread) t;
                wt.pool.helpComplete(wt.workQueue, this, maxTasks);
                return;
            }
            ForkJoinPool.common.externalHelpComplete(this, maxTasks);
        }
    }

    /* access modifiers changed from: package-private */
    public void internalPropagateException(Throwable ex) {
        CountedCompleter countedCompleter = this;
        CountedCompleter<?> a = countedCompleter;
        while (a.onExceptionalCompletion(ex, countedCompleter)) {
            countedCompleter = a;
            CountedCompleter<?> countedCompleter2 = a.completer;
            a = countedCompleter2;
            if (countedCompleter2 != null && a.status >= 0) {
                if (a.recordExceptionalCompletion(ex) != Integer.MIN_VALUE) {
                    return;
                }
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public final boolean exec() {
        compute();
        return false;
    }

    public T getRawResult() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void setRawResult(T t) {
    }

    static {
        try {
            PENDING = U.objectFieldOffset(CountedCompleter.class.getDeclaredField("pending"));
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }
}
