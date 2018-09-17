package java.util.concurrent;

import javax.xml.datatype.DatatypeConstants;
import sun.misc.Unsafe;

public abstract class CountedCompleter<T> extends ForkJoinTask<T> {
    private static final long PENDING = 0;
    private static final Unsafe U = null;
    private static final long serialVersionUID = 5232453752276485070L;
    final CountedCompleter<?> completer;
    volatile int pending;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.CountedCompleter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.CountedCompleter.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CountedCompleter.<clinit>():void");
    }

    public abstract void compute();

    protected CountedCompleter(CountedCompleter<?> completer, int initialPendingCount) {
        this.completer = completer;
        this.pending = initialPendingCount;
    }

    protected CountedCompleter(CountedCompleter<?> completer) {
        this.completer = completer;
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
            c = this.pending;
            if (c == 0) {
                break;
            }
        } while (!U.compareAndSwapInt(this, PENDING, c, c - 1));
        return c;
    }

    public final CountedCompleter<?> getRoot() {
        CountedCompleter<?> a = this;
        while (true) {
            CountedCompleter<?> p = a.completer;
            if (p == null) {
                return a;
            }
            a = p;
        }
    }

    public final void tryComplete() {
        CountedCompleter<?> a = this;
        CountedCompleter<?> s = this;
        while (true) {
            int c = a.pending;
            if (c == 0) {
                a.onCompletion(s);
                s = a;
                a = a.completer;
                if (a == null) {
                    s.quietlyComplete();
                    return;
                }
            } else if (U.compareAndSwapInt(a, PENDING, c, c - 1)) {
                return;
            }
        }
    }

    public final void propagateCompletion() {
        CountedCompleter<?> a = this;
        CountedCompleter<?> s = this;
        while (true) {
            int c = a.pending;
            if (c == 0) {
                s = a;
                a = a.completer;
                if (a == null) {
                    s.quietlyComplete();
                    return;
                }
            } else if (U.compareAndSwapInt(a, PENDING, c, c - 1)) {
                return;
            }
        }
    }

    public void complete(T rawResult) {
        setRawResult(rawResult);
        onCompletion(this);
        quietlyComplete();
        CountedCompleter<?> p = this.completer;
        if (p != null) {
            p.tryComplete();
        }
    }

    public final CountedCompleter<?> firstComplete() {
        int c;
        do {
            c = this.pending;
            if (c == 0) {
                return this;
            }
        } while (!U.compareAndSwapInt(this, PENDING, c, c - 1));
        return null;
    }

    public final CountedCompleter<?> nextComplete() {
        CountedCompleter<?> p = this.completer;
        if (p != null) {
            return p.firstComplete();
        }
        quietlyComplete();
        return null;
    }

    public final void quietlyCompleteRoot() {
        CountedCompleter<?> a = this;
        while (true) {
            CountedCompleter<?> p = a.completer;
            if (p == null) {
                a.quietlyComplete();
                return;
            }
            a = p;
        }
    }

    public final void helpComplete(int maxTasks) {
        if (maxTasks > 0 && this.status >= 0) {
            Thread t = Thread.currentThread();
            if (t instanceof ForkJoinWorkerThread) {
                ForkJoinWorkerThread wt = (ForkJoinWorkerThread) t;
                wt.pool.helpComplete(wt.workQueue, this, maxTasks);
                return;
            }
            ForkJoinPool.common.externalHelpComplete(this, maxTasks);
        }
    }

    void internalPropagateException(Throwable ex) {
        CountedCompleter<?> a = this;
        CountedCompleter<?> s = this;
        while (a.onExceptionalCompletion(ex, s)) {
            s = a;
            a = a.completer;
            if (a != null && a.status >= 0) {
                if (a.recordExceptionalCompletion(ex) != DatatypeConstants.FIELD_UNDEFINED) {
                    return;
                }
            }
            return;
        }
    }

    protected final boolean exec() {
        compute();
        return false;
    }

    public T getRawResult() {
        return null;
    }

    protected void setRawResult(T t) {
    }
}
