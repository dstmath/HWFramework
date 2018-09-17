package java.util;

import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;

class ArrayPrefixHelpers {
    static final int CUMULATE = 1;
    static final int FINISHED = 4;
    static final int MIN_PARTITION = 16;
    static final int SUMMED = 2;

    static final class CumulateTask<T> extends CountedCompleter<Void> {
        private static final long serialVersionUID = 5293554502939613543L;
        final T[] array;
        final int fence;
        final BinaryOperator<T> function;
        final int hi;
        T in;
        CumulateTask<T> left;
        final int lo;
        final int origin;
        T out;
        CumulateTask<T> right;
        final int threshold;

        public CumulateTask(CumulateTask<T> parent, BinaryOperator<T> function, T[] array, int lo, int hi) {
            super(parent);
            this.function = function;
            this.array = array;
            this.origin = lo;
            this.lo = lo;
            this.fence = hi;
            this.hi = hi;
            int p = (hi - lo) / (ForkJoinPool.getCommonPoolParallelism() << 3);
            if (p <= 16) {
                p = 16;
            }
            this.threshold = p;
        }

        CumulateTask(CumulateTask<T> parent, BinaryOperator<T> function, T[] array, int origin, int fence, int threshold, int lo, int hi) {
            super(parent);
            this.function = function;
            this.array = array;
            this.origin = origin;
            this.fence = fence;
            this.threshold = threshold;
            this.lo = lo;
            this.hi = hi;
        }

        public final void compute() {
            BinaryOperator<T> fn = this.function;
            if (fn != null) {
                T[] a = this.array;
                if (a != null) {
                    int th = this.threshold;
                    int org = this.origin;
                    int fnc = this.fence;
                    CumulateTask t = this;
                    while (true) {
                        int l = t.lo;
                        if (l >= 0) {
                            int h = t.hi;
                            if (h > a.length) {
                                return;
                            }
                            CumulateTask<T> lt;
                            CumulateTask<T> rt;
                            T lout;
                            if (h - l > th) {
                                CumulateTask f;
                                lt = t.left;
                                rt = t.right;
                                if (lt == null) {
                                    int mid = (l + h) >>> 1;
                                    rt = new CumulateTask(t, fn, a, org, fnc, th, mid, h);
                                    t.right = rt;
                                    f = rt;
                                    lt = new CumulateTask(t, fn, a, org, fnc, th, l, mid);
                                    t.left = lt;
                                    t = lt;
                                } else {
                                    int c;
                                    CumulateTask<T> t2;
                                    T pin = t.in;
                                    lt.in = pin;
                                    t = null;
                                    f = null;
                                    if (rt != null) {
                                        lout = lt.out;
                                        if (l != org) {
                                            lout = fn.apply(pin, lout);
                                        }
                                        rt.in = lout;
                                        do {
                                            c = rt.getPendingCount();
                                            if ((c & 1) != 0) {
                                                break;
                                            }
                                        } while (!rt.compareAndSetPendingCount(c, c | 1));
                                        t2 = rt;
                                    }
                                    do {
                                        c = lt.getPendingCount();
                                        if ((c & 1) != 0) {
                                            break;
                                        }
                                    } while (!lt.compareAndSetPendingCount(c, c | 1));
                                    if (t != null) {
                                        f = t;
                                    }
                                    t2 = lt;
                                    if (t == null) {
                                        return;
                                    }
                                }
                                if (f != null) {
                                    f.fork();
                                }
                            } else {
                                int b;
                                int state;
                                T sum;
                                do {
                                    b = t.getPendingCount();
                                    if ((b & 4) == 0) {
                                        state = (b & 1) != 0 ? 4 : l > org ? 2 : 6;
                                    } else {
                                        return;
                                    }
                                } while (!t.compareAndSetPendingCount(b, b | state));
                                int i;
                                if (state != 2) {
                                    int first;
                                    if (l == org) {
                                        sum = a[org];
                                        first = org + 1;
                                    } else {
                                        sum = t.in;
                                        first = l;
                                    }
                                    for (i = first; i < h; i++) {
                                        sum = fn.apply(sum, a[i]);
                                        a[i] = sum;
                                    }
                                } else if (h < fnc) {
                                    sum = a[l];
                                    for (i = l + 1; i < h; i++) {
                                        sum = fn.apply(sum, a[i]);
                                    }
                                } else {
                                    sum = t.in;
                                }
                                t.out = sum;
                                while (true) {
                                    CumulateTask<T> partmp = (CumulateTask) t.getCompleter();
                                    CumulateTask<T> par = partmp;
                                    if (partmp != null) {
                                        b = partmp.getPendingCount();
                                        if (((b & state) & 4) != 0) {
                                            t = partmp;
                                        } else if (((b & state) & 2) != 0) {
                                            lt = partmp.left;
                                            if (lt != null) {
                                                rt = partmp.right;
                                                if (rt != null) {
                                                    lout = lt.out;
                                                    if (rt.hi != fnc) {
                                                        lout = fn.apply(lout, rt.out);
                                                    }
                                                    partmp.out = lout;
                                                }
                                            }
                                            int refork = ((b & 1) == 0 && partmp.lo == org) ? 1 : 0;
                                            int nextState = (b | state) | refork;
                                            if (nextState == b || partmp.compareAndSetPendingCount(b, nextState)) {
                                                state = 2;
                                                t = partmp;
                                                if (refork != 0) {
                                                    partmp.fork();
                                                }
                                            }
                                        } else {
                                            if (partmp.compareAndSetPendingCount(b, b | state)) {
                                                return;
                                            }
                                        }
                                    } else if ((state & 4) != 0) {
                                        t.quietlyComplete();
                                        return;
                                    } else {
                                        return;
                                    }
                                }
                            }
                        }
                        return;
                    }
                }
            }
            throw new NullPointerException();
        }
    }

    static final class DoubleCumulateTask extends CountedCompleter<Void> {
        private static final long serialVersionUID = -586947823794232033L;
        final double[] array;
        final int fence;
        final DoubleBinaryOperator function;
        final int hi;
        double in;
        DoubleCumulateTask left;
        final int lo;
        final int origin;
        double out;
        DoubleCumulateTask right;
        final int threshold;

        public DoubleCumulateTask(DoubleCumulateTask parent, DoubleBinaryOperator function, double[] array, int lo, int hi) {
            super(parent);
            this.function = function;
            this.array = array;
            this.origin = lo;
            this.lo = lo;
            this.fence = hi;
            this.hi = hi;
            int p = (hi - lo) / (ForkJoinPool.getCommonPoolParallelism() << 3);
            if (p <= 16) {
                p = 16;
            }
            this.threshold = p;
        }

        DoubleCumulateTask(DoubleCumulateTask parent, DoubleBinaryOperator function, double[] array, int origin, int fence, int threshold, int lo, int hi) {
            super(parent);
            this.function = function;
            this.array = array;
            this.origin = origin;
            this.fence = fence;
            this.threshold = threshold;
            this.lo = lo;
            this.hi = hi;
        }

        public final void compute() {
            DoubleBinaryOperator fn = this.function;
            if (fn != null) {
                double[] a = this.array;
                if (a != null) {
                    int th = this.threshold;
                    int org = this.origin;
                    int fnc = this.fence;
                    DoubleCumulateTask t = this;
                    while (true) {
                        int l = t.lo;
                        if (l >= 0) {
                            int h = t.hi;
                            if (h > a.length) {
                                return;
                            }
                            DoubleCumulateTask lt;
                            DoubleCumulateTask rt;
                            double lout;
                            if (h - l > th) {
                                DoubleCumulateTask f;
                                lt = t.left;
                                rt = t.right;
                                if (lt == null) {
                                    int mid = (l + h) >>> 1;
                                    rt = new DoubleCumulateTask(t, fn, a, org, fnc, th, mid, h);
                                    t.right = rt;
                                    f = rt;
                                    lt = new DoubleCumulateTask(t, fn, a, org, fnc, th, l, mid);
                                    t.left = lt;
                                    t = lt;
                                } else {
                                    int c;
                                    double pin = t.in;
                                    lt.in = pin;
                                    t = null;
                                    f = null;
                                    if (rt != null) {
                                        lout = lt.out;
                                        if (l != org) {
                                            lout = fn.applyAsDouble(pin, lout);
                                        }
                                        rt.in = lout;
                                        do {
                                            c = rt.getPendingCount();
                                            if ((c & 1) != 0) {
                                                break;
                                            }
                                        } while (!rt.compareAndSetPendingCount(c, c | 1));
                                        t = rt;
                                    }
                                    do {
                                        c = lt.getPendingCount();
                                        if ((c & 1) != 0) {
                                            break;
                                        }
                                    } while (!lt.compareAndSetPendingCount(c, c | 1));
                                    if (t != null) {
                                        f = t;
                                    }
                                    t = lt;
                                    if (t == null) {
                                        return;
                                    }
                                }
                                if (f != null) {
                                    f.fork();
                                }
                            } else {
                                int b;
                                int state;
                                double sum;
                                do {
                                    b = t.getPendingCount();
                                    if ((b & 4) == 0) {
                                        state = (b & 1) != 0 ? 4 : l > org ? 2 : 6;
                                    } else {
                                        return;
                                    }
                                } while (!t.compareAndSetPendingCount(b, b | state));
                                int i;
                                if (state != 2) {
                                    int first;
                                    if (l == org) {
                                        sum = a[org];
                                        first = org + 1;
                                    } else {
                                        sum = t.in;
                                        first = l;
                                    }
                                    for (i = first; i < h; i++) {
                                        sum = fn.applyAsDouble(sum, a[i]);
                                        a[i] = sum;
                                    }
                                } else if (h < fnc) {
                                    sum = a[l];
                                    for (i = l + 1; i < h; i++) {
                                        sum = fn.applyAsDouble(sum, a[i]);
                                    }
                                } else {
                                    sum = t.in;
                                }
                                t.out = sum;
                                while (true) {
                                    DoubleCumulateTask par = (DoubleCumulateTask) t.getCompleter();
                                    if (par != null) {
                                        b = par.getPendingCount();
                                        if (((b & state) & 4) != 0) {
                                            t = par;
                                        } else if (((b & state) & 2) != 0) {
                                            lt = par.left;
                                            if (lt != null) {
                                                rt = par.right;
                                                if (rt != null) {
                                                    lout = lt.out;
                                                    if (rt.hi != fnc) {
                                                        lout = fn.applyAsDouble(lout, rt.out);
                                                    }
                                                    par.out = lout;
                                                }
                                            }
                                            int refork = ((b & 1) == 0 && par.lo == org) ? 1 : 0;
                                            int nextState = (b | state) | refork;
                                            if (nextState == b || par.compareAndSetPendingCount(b, nextState)) {
                                                state = 2;
                                                t = par;
                                                if (refork != 0) {
                                                    par.fork();
                                                }
                                            }
                                        } else {
                                            if (par.compareAndSetPendingCount(b, b | state)) {
                                                return;
                                            }
                                        }
                                    } else if ((state & 4) != 0) {
                                        t.quietlyComplete();
                                        return;
                                    } else {
                                        return;
                                    }
                                }
                            }
                        }
                        return;
                    }
                }
            }
            throw new NullPointerException();
        }
    }

    static final class IntCumulateTask extends CountedCompleter<Void> {
        private static final long serialVersionUID = 3731755594596840961L;
        final int[] array;
        final int fence;
        final IntBinaryOperator function;
        final int hi;
        int in;
        IntCumulateTask left;
        final int lo;
        final int origin;
        int out;
        IntCumulateTask right;
        final int threshold;

        public IntCumulateTask(IntCumulateTask parent, IntBinaryOperator function, int[] array, int lo, int hi) {
            super(parent);
            this.function = function;
            this.array = array;
            this.origin = lo;
            this.lo = lo;
            this.fence = hi;
            this.hi = hi;
            int p = (hi - lo) / (ForkJoinPool.getCommonPoolParallelism() << 3);
            if (p <= 16) {
                p = 16;
            }
            this.threshold = p;
        }

        IntCumulateTask(IntCumulateTask parent, IntBinaryOperator function, int[] array, int origin, int fence, int threshold, int lo, int hi) {
            super(parent);
            this.function = function;
            this.array = array;
            this.origin = origin;
            this.fence = fence;
            this.threshold = threshold;
            this.lo = lo;
            this.hi = hi;
        }

        public final void compute() {
            IntBinaryOperator fn = this.function;
            if (fn != null) {
                int[] a = this.array;
                if (a != null) {
                    int th = this.threshold;
                    int org = this.origin;
                    int fnc = this.fence;
                    IntCumulateTask t = this;
                    while (true) {
                        int l = t.lo;
                        if (l >= 0) {
                            int h = t.hi;
                            if (h > a.length) {
                                return;
                            }
                            IntCumulateTask lt;
                            IntCumulateTask rt;
                            int lout;
                            if (h - l > th) {
                                IntCumulateTask f;
                                lt = t.left;
                                rt = t.right;
                                if (lt == null) {
                                    int mid = (l + h) >>> 1;
                                    rt = new IntCumulateTask(t, fn, a, org, fnc, th, mid, h);
                                    t.right = rt;
                                    f = rt;
                                    lt = new IntCumulateTask(t, fn, a, org, fnc, th, l, mid);
                                    t.left = lt;
                                    t = lt;
                                } else {
                                    int c;
                                    int pin = t.in;
                                    lt.in = pin;
                                    t = null;
                                    f = null;
                                    if (rt != null) {
                                        lout = lt.out;
                                        if (l != org) {
                                            lout = fn.applyAsInt(pin, lout);
                                        }
                                        rt.in = lout;
                                        do {
                                            c = rt.getPendingCount();
                                            if ((c & 1) != 0) {
                                                break;
                                            }
                                        } while (!rt.compareAndSetPendingCount(c, c | 1));
                                        t = rt;
                                    }
                                    do {
                                        c = lt.getPendingCount();
                                        if ((c & 1) != 0) {
                                            break;
                                        }
                                    } while (!lt.compareAndSetPendingCount(c, c | 1));
                                    if (t != null) {
                                        f = t;
                                    }
                                    t = lt;
                                    if (t == null) {
                                        return;
                                    }
                                }
                                if (f != null) {
                                    f.fork();
                                }
                            } else {
                                int b;
                                int state;
                                int sum;
                                do {
                                    b = t.getPendingCount();
                                    if ((b & 4) == 0) {
                                        state = (b & 1) != 0 ? 4 : l > org ? 2 : 6;
                                    } else {
                                        return;
                                    }
                                } while (!t.compareAndSetPendingCount(b, b | state));
                                int i;
                                if (state != 2) {
                                    int first;
                                    if (l == org) {
                                        sum = a[org];
                                        first = org + 1;
                                    } else {
                                        sum = t.in;
                                        first = l;
                                    }
                                    for (i = first; i < h; i++) {
                                        sum = fn.applyAsInt(sum, a[i]);
                                        a[i] = sum;
                                    }
                                } else if (h < fnc) {
                                    sum = a[l];
                                    for (i = l + 1; i < h; i++) {
                                        sum = fn.applyAsInt(sum, a[i]);
                                    }
                                } else {
                                    sum = t.in;
                                }
                                t.out = sum;
                                while (true) {
                                    IntCumulateTask par = (IntCumulateTask) t.getCompleter();
                                    if (par != null) {
                                        b = par.getPendingCount();
                                        if (((b & state) & 4) != 0) {
                                            t = par;
                                        } else if (((b & state) & 2) != 0) {
                                            lt = par.left;
                                            if (lt != null) {
                                                rt = par.right;
                                                if (rt != null) {
                                                    lout = lt.out;
                                                    if (rt.hi != fnc) {
                                                        lout = fn.applyAsInt(lout, rt.out);
                                                    }
                                                    par.out = lout;
                                                }
                                            }
                                            int refork = ((b & 1) == 0 && par.lo == org) ? 1 : 0;
                                            int nextState = (b | state) | refork;
                                            if (nextState == b || par.compareAndSetPendingCount(b, nextState)) {
                                                state = 2;
                                                t = par;
                                                if (refork != 0) {
                                                    par.fork();
                                                }
                                            }
                                        } else {
                                            if (par.compareAndSetPendingCount(b, b | state)) {
                                                return;
                                            }
                                        }
                                    } else if ((state & 4) != 0) {
                                        t.quietlyComplete();
                                        return;
                                    } else {
                                        return;
                                    }
                                }
                            }
                        }
                        return;
                    }
                }
            }
            throw new NullPointerException();
        }
    }

    static final class LongCumulateTask extends CountedCompleter<Void> {
        private static final long serialVersionUID = -5074099945909284273L;
        final long[] array;
        final int fence;
        final LongBinaryOperator function;
        final int hi;
        long in;
        LongCumulateTask left;
        final int lo;
        final int origin;
        long out;
        LongCumulateTask right;
        final int threshold;

        public LongCumulateTask(LongCumulateTask parent, LongBinaryOperator function, long[] array, int lo, int hi) {
            super(parent);
            this.function = function;
            this.array = array;
            this.origin = lo;
            this.lo = lo;
            this.fence = hi;
            this.hi = hi;
            int p = (hi - lo) / (ForkJoinPool.getCommonPoolParallelism() << 3);
            if (p <= 16) {
                p = 16;
            }
            this.threshold = p;
        }

        LongCumulateTask(LongCumulateTask parent, LongBinaryOperator function, long[] array, int origin, int fence, int threshold, int lo, int hi) {
            super(parent);
            this.function = function;
            this.array = array;
            this.origin = origin;
            this.fence = fence;
            this.threshold = threshold;
            this.lo = lo;
            this.hi = hi;
        }

        public final void compute() {
            LongBinaryOperator fn = this.function;
            if (fn != null) {
                long[] a = this.array;
                if (a != null) {
                    int th = this.threshold;
                    int org = this.origin;
                    int fnc = this.fence;
                    LongCumulateTask t = this;
                    while (true) {
                        int l = t.lo;
                        if (l >= 0) {
                            int h = t.hi;
                            if (h > a.length) {
                                return;
                            }
                            LongCumulateTask lt;
                            LongCumulateTask rt;
                            long lout;
                            if (h - l > th) {
                                LongCumulateTask f;
                                lt = t.left;
                                rt = t.right;
                                if (lt == null) {
                                    int mid = (l + h) >>> 1;
                                    rt = new LongCumulateTask(t, fn, a, org, fnc, th, mid, h);
                                    t.right = rt;
                                    f = rt;
                                    lt = new LongCumulateTask(t, fn, a, org, fnc, th, l, mid);
                                    t.left = lt;
                                    t = lt;
                                } else {
                                    int c;
                                    long pin = t.in;
                                    lt.in = pin;
                                    t = null;
                                    f = null;
                                    if (rt != null) {
                                        lout = lt.out;
                                        if (l != org) {
                                            lout = fn.applyAsLong(pin, lout);
                                        }
                                        rt.in = lout;
                                        do {
                                            c = rt.getPendingCount();
                                            if ((c & 1) != 0) {
                                                break;
                                            }
                                        } while (!rt.compareAndSetPendingCount(c, c | 1));
                                        t = rt;
                                    }
                                    do {
                                        c = lt.getPendingCount();
                                        if ((c & 1) != 0) {
                                            break;
                                        }
                                    } while (!lt.compareAndSetPendingCount(c, c | 1));
                                    if (t != null) {
                                        f = t;
                                    }
                                    t = lt;
                                    if (t == null) {
                                        return;
                                    }
                                }
                                if (f != null) {
                                    f.fork();
                                }
                            } else {
                                int b;
                                int state;
                                long sum;
                                do {
                                    b = t.getPendingCount();
                                    if ((b & 4) == 0) {
                                        state = (b & 1) != 0 ? 4 : l > org ? 2 : 6;
                                    } else {
                                        return;
                                    }
                                } while (!t.compareAndSetPendingCount(b, b | state));
                                int i;
                                if (state != 2) {
                                    int first;
                                    if (l == org) {
                                        sum = a[org];
                                        first = org + 1;
                                    } else {
                                        sum = t.in;
                                        first = l;
                                    }
                                    for (i = first; i < h; i++) {
                                        sum = fn.applyAsLong(sum, a[i]);
                                        a[i] = sum;
                                    }
                                } else if (h < fnc) {
                                    sum = a[l];
                                    for (i = l + 1; i < h; i++) {
                                        sum = fn.applyAsLong(sum, a[i]);
                                    }
                                } else {
                                    sum = t.in;
                                }
                                t.out = sum;
                                while (true) {
                                    LongCumulateTask par = (LongCumulateTask) t.getCompleter();
                                    if (par != null) {
                                        b = par.getPendingCount();
                                        if (((b & state) & 4) != 0) {
                                            t = par;
                                        } else if (((b & state) & 2) != 0) {
                                            lt = par.left;
                                            if (lt != null) {
                                                rt = par.right;
                                                if (rt != null) {
                                                    lout = lt.out;
                                                    if (rt.hi != fnc) {
                                                        lout = fn.applyAsLong(lout, rt.out);
                                                    }
                                                    par.out = lout;
                                                }
                                            }
                                            int refork = ((b & 1) == 0 && par.lo == org) ? 1 : 0;
                                            int nextState = (b | state) | refork;
                                            if (nextState == b || par.compareAndSetPendingCount(b, nextState)) {
                                                state = 2;
                                                t = par;
                                                if (refork != 0) {
                                                    par.fork();
                                                }
                                            }
                                        } else {
                                            if (par.compareAndSetPendingCount(b, b | state)) {
                                                return;
                                            }
                                        }
                                    } else if ((state & 4) != 0) {
                                        t.quietlyComplete();
                                        return;
                                    } else {
                                        return;
                                    }
                                }
                            }
                        }
                        return;
                    }
                }
            }
            throw new NullPointerException();
        }
    }

    private ArrayPrefixHelpers() {
    }
}
