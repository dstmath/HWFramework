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
            if (p <= ArrayPrefixHelpers.MIN_PARTITION) {
                p = ArrayPrefixHelpers.MIN_PARTITION;
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
                    int l;
                    int h;
                    CumulateTask<T> lt;
                    CumulateTask<T> rt;
                    T lout;
                    int b;
                    int state;
                    T sum;
                    int th = this.threshold;
                    int org = this.origin;
                    int fnc = this.fence;
                    CumulateTask cumulateTask = this;
                    while (true) {
                        l = cumulateTask.lo;
                        if (l >= 0) {
                            h = cumulateTask.hi;
                            if (h <= a.length) {
                                if (h - l <= th) {
                                    break;
                                }
                                CumulateTask f;
                                lt = cumulateTask.left;
                                rt = cumulateTask.right;
                                if (lt == null) {
                                    int mid = (l + h) >>> ArrayPrefixHelpers.CUMULATE;
                                    rt = new CumulateTask(cumulateTask, fn, a, org, fnc, th, mid, h);
                                    cumulateTask.right = rt;
                                    f = rt;
                                    lt = new CumulateTask(cumulateTask, fn, a, org, fnc, th, l, mid);
                                    cumulateTask.left = lt;
                                    cumulateTask = lt;
                                } else {
                                    int c;
                                    CumulateTask<T> t;
                                    T pin = cumulateTask.in;
                                    lt.in = pin;
                                    cumulateTask = null;
                                    f = null;
                                    if (rt != null) {
                                        lout = lt.out;
                                        if (l != org) {
                                            lout = fn.apply(pin, lout);
                                        }
                                        rt.in = lout;
                                        do {
                                            c = rt.getPendingCount();
                                            if ((c & ArrayPrefixHelpers.CUMULATE) != 0) {
                                                break;
                                            }
                                        } while (!rt.compareAndSetPendingCount(c, c | ArrayPrefixHelpers.CUMULATE));
                                        t = rt;
                                    }
                                    do {
                                        c = lt.getPendingCount();
                                        if ((c & ArrayPrefixHelpers.CUMULATE) != 0) {
                                            break;
                                        }
                                    } while (!lt.compareAndSetPendingCount(c, c | ArrayPrefixHelpers.CUMULATE));
                                    if (cumulateTask != null) {
                                        f = cumulateTask;
                                    }
                                    t = lt;
                                    if (cumulateTask == null) {
                                        return;
                                    }
                                }
                                if (f != null) {
                                    f.fork();
                                }
                            } else {
                                return;
                            }
                        }
                        return;
                    }
                    do {
                        b = cumulateTask.getPendingCount();
                        if ((b & ArrayPrefixHelpers.FINISHED) == 0) {
                            state = (b & ArrayPrefixHelpers.CUMULATE) != 0 ? ArrayPrefixHelpers.FINISHED : l > org ? ArrayPrefixHelpers.SUMMED : 6;
                        } else {
                            return;
                        }
                    } while (!cumulateTask.compareAndSetPendingCount(b, b | state));
                    int i;
                    if (state != ArrayPrefixHelpers.SUMMED) {
                        int first;
                        if (l == org) {
                            sum = a[org];
                            first = org + ArrayPrefixHelpers.CUMULATE;
                        } else {
                            sum = cumulateTask.in;
                            first = l;
                        }
                        for (i = first; i < h; i += ArrayPrefixHelpers.CUMULATE) {
                            sum = fn.apply(sum, a[i]);
                            a[i] = sum;
                        }
                    } else if (h < fnc) {
                        sum = a[l];
                        for (i = l + ArrayPrefixHelpers.CUMULATE; i < h; i += ArrayPrefixHelpers.CUMULATE) {
                            sum = fn.apply(sum, a[i]);
                        }
                    } else {
                        sum = cumulateTask.in;
                    }
                    cumulateTask.out = sum;
                    while (true) {
                        CumulateTask<T> partmp = (CumulateTask) cumulateTask.getCompleter();
                        CumulateTask<T> par = partmp;
                        if (partmp == null) {
                            break;
                        }
                        b = partmp.getPendingCount();
                        if (((b & state) & ArrayPrefixHelpers.FINISHED) != 0) {
                            cumulateTask = partmp;
                        } else if (((b & state) & ArrayPrefixHelpers.SUMMED) != 0) {
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
                            int refork = ((b & ArrayPrefixHelpers.CUMULATE) == 0 && partmp.lo == org) ? ArrayPrefixHelpers.CUMULATE : 0;
                            int nextState = (b | state) | refork;
                            if (nextState == b || partmp.compareAndSetPendingCount(b, nextState)) {
                                state = ArrayPrefixHelpers.SUMMED;
                                cumulateTask = partmp;
                                if (refork != 0) {
                                    partmp.fork();
                                }
                            }
                        } else {
                            if (partmp.compareAndSetPendingCount(b, b | state)) {
                                return;
                            }
                        }
                    }
                    if ((state & ArrayPrefixHelpers.FINISHED) != 0) {
                        cumulateTask.quietlyComplete();
                        return;
                    }
                    return;
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
            if (p <= ArrayPrefixHelpers.MIN_PARTITION) {
                p = ArrayPrefixHelpers.MIN_PARTITION;
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
                    int l;
                    int h;
                    DoubleCumulateTask lt;
                    DoubleCumulateTask rt;
                    double lout;
                    int b;
                    int state;
                    double sum;
                    int th = this.threshold;
                    int org = this.origin;
                    int fnc = this.fence;
                    DoubleCumulateTask doubleCumulateTask = this;
                    while (true) {
                        l = doubleCumulateTask.lo;
                        if (l >= 0) {
                            h = doubleCumulateTask.hi;
                            if (h <= a.length) {
                                if (h - l <= th) {
                                    break;
                                }
                                DoubleCumulateTask f;
                                lt = doubleCumulateTask.left;
                                rt = doubleCumulateTask.right;
                                if (lt == null) {
                                    int mid = (l + h) >>> ArrayPrefixHelpers.CUMULATE;
                                    rt = new DoubleCumulateTask(doubleCumulateTask, fn, a, org, fnc, th, mid, h);
                                    doubleCumulateTask.right = rt;
                                    f = rt;
                                    lt = new DoubleCumulateTask(doubleCumulateTask, fn, a, org, fnc, th, l, mid);
                                    doubleCumulateTask.left = lt;
                                    doubleCumulateTask = lt;
                                } else {
                                    int c;
                                    double pin = doubleCumulateTask.in;
                                    lt.in = pin;
                                    doubleCumulateTask = null;
                                    f = null;
                                    if (rt != null) {
                                        lout = lt.out;
                                        if (l != org) {
                                            lout = fn.applyAsDouble(pin, lout);
                                        }
                                        rt.in = lout;
                                        do {
                                            c = rt.getPendingCount();
                                            if ((c & ArrayPrefixHelpers.CUMULATE) != 0) {
                                                break;
                                            }
                                        } while (!rt.compareAndSetPendingCount(c, c | ArrayPrefixHelpers.CUMULATE));
                                        doubleCumulateTask = rt;
                                    }
                                    do {
                                        c = lt.getPendingCount();
                                        if ((c & ArrayPrefixHelpers.CUMULATE) != 0) {
                                            break;
                                        }
                                    } while (!lt.compareAndSetPendingCount(c, c | ArrayPrefixHelpers.CUMULATE));
                                    if (doubleCumulateTask != null) {
                                        f = doubleCumulateTask;
                                    }
                                    doubleCumulateTask = lt;
                                    if (doubleCumulateTask == null) {
                                        return;
                                    }
                                }
                                if (f != null) {
                                    f.fork();
                                }
                            } else {
                                return;
                            }
                        }
                        return;
                    }
                    do {
                        b = doubleCumulateTask.getPendingCount();
                        if ((b & ArrayPrefixHelpers.FINISHED) == 0) {
                            state = (b & ArrayPrefixHelpers.CUMULATE) != 0 ? ArrayPrefixHelpers.FINISHED : l > org ? ArrayPrefixHelpers.SUMMED : 6;
                        } else {
                            return;
                        }
                    } while (!doubleCumulateTask.compareAndSetPendingCount(b, b | state));
                    int i;
                    if (state != ArrayPrefixHelpers.SUMMED) {
                        int first;
                        if (l == org) {
                            sum = a[org];
                            first = org + ArrayPrefixHelpers.CUMULATE;
                        } else {
                            sum = doubleCumulateTask.in;
                            first = l;
                        }
                        for (i = first; i < h; i += ArrayPrefixHelpers.CUMULATE) {
                            sum = fn.applyAsDouble(sum, a[i]);
                            a[i] = sum;
                        }
                    } else if (h < fnc) {
                        sum = a[l];
                        for (i = l + ArrayPrefixHelpers.CUMULATE; i < h; i += ArrayPrefixHelpers.CUMULATE) {
                            sum = fn.applyAsDouble(sum, a[i]);
                        }
                    } else {
                        sum = doubleCumulateTask.in;
                    }
                    doubleCumulateTask.out = sum;
                    while (true) {
                        DoubleCumulateTask par = (DoubleCumulateTask) doubleCumulateTask.getCompleter();
                        if (par == null) {
                            break;
                        }
                        b = par.getPendingCount();
                        if (((b & state) & ArrayPrefixHelpers.FINISHED) != 0) {
                            doubleCumulateTask = par;
                        } else if (((b & state) & ArrayPrefixHelpers.SUMMED) != 0) {
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
                            int refork = ((b & ArrayPrefixHelpers.CUMULATE) == 0 && par.lo == org) ? ArrayPrefixHelpers.CUMULATE : 0;
                            int nextState = (b | state) | refork;
                            if (nextState == b || par.compareAndSetPendingCount(b, nextState)) {
                                state = ArrayPrefixHelpers.SUMMED;
                                doubleCumulateTask = par;
                                if (refork != 0) {
                                    par.fork();
                                }
                            }
                        } else {
                            if (par.compareAndSetPendingCount(b, b | state)) {
                                return;
                            }
                        }
                    }
                    if ((state & ArrayPrefixHelpers.FINISHED) != 0) {
                        doubleCumulateTask.quietlyComplete();
                        return;
                    }
                    return;
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
            if (p <= ArrayPrefixHelpers.MIN_PARTITION) {
                p = ArrayPrefixHelpers.MIN_PARTITION;
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
                    int l;
                    int h;
                    IntCumulateTask lt;
                    IntCumulateTask rt;
                    int lout;
                    int b;
                    int state;
                    int sum;
                    int th = this.threshold;
                    int org = this.origin;
                    int fnc = this.fence;
                    IntCumulateTask intCumulateTask = this;
                    while (true) {
                        l = intCumulateTask.lo;
                        if (l >= 0) {
                            h = intCumulateTask.hi;
                            if (h <= a.length) {
                                if (h - l <= th) {
                                    break;
                                }
                                IntCumulateTask f;
                                lt = intCumulateTask.left;
                                rt = intCumulateTask.right;
                                if (lt == null) {
                                    int mid = (l + h) >>> ArrayPrefixHelpers.CUMULATE;
                                    rt = new IntCumulateTask(intCumulateTask, fn, a, org, fnc, th, mid, h);
                                    intCumulateTask.right = rt;
                                    f = rt;
                                    lt = new IntCumulateTask(intCumulateTask, fn, a, org, fnc, th, l, mid);
                                    intCumulateTask.left = lt;
                                    intCumulateTask = lt;
                                } else {
                                    int c;
                                    int pin = intCumulateTask.in;
                                    lt.in = pin;
                                    intCumulateTask = null;
                                    f = null;
                                    if (rt != null) {
                                        lout = lt.out;
                                        if (l != org) {
                                            lout = fn.applyAsInt(pin, lout);
                                        }
                                        rt.in = lout;
                                        do {
                                            c = rt.getPendingCount();
                                            if ((c & ArrayPrefixHelpers.CUMULATE) != 0) {
                                                break;
                                            }
                                        } while (!rt.compareAndSetPendingCount(c, c | ArrayPrefixHelpers.CUMULATE));
                                        intCumulateTask = rt;
                                    }
                                    do {
                                        c = lt.getPendingCount();
                                        if ((c & ArrayPrefixHelpers.CUMULATE) != 0) {
                                            break;
                                        }
                                    } while (!lt.compareAndSetPendingCount(c, c | ArrayPrefixHelpers.CUMULATE));
                                    if (intCumulateTask != null) {
                                        f = intCumulateTask;
                                    }
                                    intCumulateTask = lt;
                                    if (intCumulateTask == null) {
                                        return;
                                    }
                                }
                                if (f != null) {
                                    f.fork();
                                }
                            } else {
                                return;
                            }
                        }
                        return;
                    }
                    do {
                        b = intCumulateTask.getPendingCount();
                        if ((b & ArrayPrefixHelpers.FINISHED) == 0) {
                            state = (b & ArrayPrefixHelpers.CUMULATE) != 0 ? ArrayPrefixHelpers.FINISHED : l > org ? ArrayPrefixHelpers.SUMMED : 6;
                        } else {
                            return;
                        }
                    } while (!intCumulateTask.compareAndSetPendingCount(b, b | state));
                    int i;
                    if (state != ArrayPrefixHelpers.SUMMED) {
                        int first;
                        if (l == org) {
                            sum = a[org];
                            first = org + ArrayPrefixHelpers.CUMULATE;
                        } else {
                            sum = intCumulateTask.in;
                            first = l;
                        }
                        for (i = first; i < h; i += ArrayPrefixHelpers.CUMULATE) {
                            sum = fn.applyAsInt(sum, a[i]);
                            a[i] = sum;
                        }
                    } else if (h < fnc) {
                        sum = a[l];
                        for (i = l + ArrayPrefixHelpers.CUMULATE; i < h; i += ArrayPrefixHelpers.CUMULATE) {
                            sum = fn.applyAsInt(sum, a[i]);
                        }
                    } else {
                        sum = intCumulateTask.in;
                    }
                    intCumulateTask.out = sum;
                    while (true) {
                        IntCumulateTask par = (IntCumulateTask) intCumulateTask.getCompleter();
                        if (par == null) {
                            break;
                        }
                        b = par.getPendingCount();
                        if (((b & state) & ArrayPrefixHelpers.FINISHED) != 0) {
                            intCumulateTask = par;
                        } else if (((b & state) & ArrayPrefixHelpers.SUMMED) != 0) {
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
                            int refork = ((b & ArrayPrefixHelpers.CUMULATE) == 0 && par.lo == org) ? ArrayPrefixHelpers.CUMULATE : 0;
                            int nextState = (b | state) | refork;
                            if (nextState == b || par.compareAndSetPendingCount(b, nextState)) {
                                state = ArrayPrefixHelpers.SUMMED;
                                intCumulateTask = par;
                                if (refork != 0) {
                                    par.fork();
                                }
                            }
                        } else {
                            if (par.compareAndSetPendingCount(b, b | state)) {
                                return;
                            }
                        }
                    }
                    if ((state & ArrayPrefixHelpers.FINISHED) != 0) {
                        intCumulateTask.quietlyComplete();
                        return;
                    }
                    return;
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
            if (p <= ArrayPrefixHelpers.MIN_PARTITION) {
                p = ArrayPrefixHelpers.MIN_PARTITION;
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
                    int l;
                    int h;
                    LongCumulateTask lt;
                    LongCumulateTask rt;
                    long lout;
                    int b;
                    int state;
                    long sum;
                    int th = this.threshold;
                    int org = this.origin;
                    int fnc = this.fence;
                    LongCumulateTask longCumulateTask = this;
                    while (true) {
                        l = longCumulateTask.lo;
                        if (l >= 0) {
                            h = longCumulateTask.hi;
                            if (h <= a.length) {
                                if (h - l <= th) {
                                    break;
                                }
                                LongCumulateTask f;
                                lt = longCumulateTask.left;
                                rt = longCumulateTask.right;
                                if (lt == null) {
                                    int mid = (l + h) >>> ArrayPrefixHelpers.CUMULATE;
                                    rt = new LongCumulateTask(longCumulateTask, fn, a, org, fnc, th, mid, h);
                                    longCumulateTask.right = rt;
                                    f = rt;
                                    lt = new LongCumulateTask(longCumulateTask, fn, a, org, fnc, th, l, mid);
                                    longCumulateTask.left = lt;
                                    longCumulateTask = lt;
                                } else {
                                    int c;
                                    long pin = longCumulateTask.in;
                                    lt.in = pin;
                                    longCumulateTask = null;
                                    f = null;
                                    if (rt != null) {
                                        lout = lt.out;
                                        if (l != org) {
                                            lout = fn.applyAsLong(pin, lout);
                                        }
                                        rt.in = lout;
                                        do {
                                            c = rt.getPendingCount();
                                            if ((c & ArrayPrefixHelpers.CUMULATE) != 0) {
                                                break;
                                            }
                                        } while (!rt.compareAndSetPendingCount(c, c | ArrayPrefixHelpers.CUMULATE));
                                        longCumulateTask = rt;
                                    }
                                    do {
                                        c = lt.getPendingCount();
                                        if ((c & ArrayPrefixHelpers.CUMULATE) != 0) {
                                            break;
                                        }
                                    } while (!lt.compareAndSetPendingCount(c, c | ArrayPrefixHelpers.CUMULATE));
                                    if (longCumulateTask != null) {
                                        f = longCumulateTask;
                                    }
                                    longCumulateTask = lt;
                                    if (longCumulateTask == null) {
                                        return;
                                    }
                                }
                                if (f != null) {
                                    f.fork();
                                }
                            } else {
                                return;
                            }
                        }
                        return;
                    }
                    do {
                        b = longCumulateTask.getPendingCount();
                        if ((b & ArrayPrefixHelpers.FINISHED) == 0) {
                            state = (b & ArrayPrefixHelpers.CUMULATE) != 0 ? ArrayPrefixHelpers.FINISHED : l > org ? ArrayPrefixHelpers.SUMMED : 6;
                        } else {
                            return;
                        }
                    } while (!longCumulateTask.compareAndSetPendingCount(b, b | state));
                    int i;
                    if (state != ArrayPrefixHelpers.SUMMED) {
                        int first;
                        if (l == org) {
                            sum = a[org];
                            first = org + ArrayPrefixHelpers.CUMULATE;
                        } else {
                            sum = longCumulateTask.in;
                            first = l;
                        }
                        for (i = first; i < h; i += ArrayPrefixHelpers.CUMULATE) {
                            sum = fn.applyAsLong(sum, a[i]);
                            a[i] = sum;
                        }
                    } else if (h < fnc) {
                        sum = a[l];
                        for (i = l + ArrayPrefixHelpers.CUMULATE; i < h; i += ArrayPrefixHelpers.CUMULATE) {
                            sum = fn.applyAsLong(sum, a[i]);
                        }
                    } else {
                        sum = longCumulateTask.in;
                    }
                    longCumulateTask.out = sum;
                    while (true) {
                        LongCumulateTask par = (LongCumulateTask) longCumulateTask.getCompleter();
                        if (par == null) {
                            break;
                        }
                        b = par.getPendingCount();
                        if (((b & state) & ArrayPrefixHelpers.FINISHED) != 0) {
                            longCumulateTask = par;
                        } else if (((b & state) & ArrayPrefixHelpers.SUMMED) != 0) {
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
                            int refork = ((b & ArrayPrefixHelpers.CUMULATE) == 0 && par.lo == org) ? ArrayPrefixHelpers.CUMULATE : 0;
                            int nextState = (b | state) | refork;
                            if (nextState == b || par.compareAndSetPendingCount(b, nextState)) {
                                state = ArrayPrefixHelpers.SUMMED;
                                longCumulateTask = par;
                                if (refork != 0) {
                                    par.fork();
                                }
                            }
                        } else {
                            if (par.compareAndSetPendingCount(b, b | state)) {
                                return;
                            }
                        }
                    }
                    if ((state & ArrayPrefixHelpers.FINISHED) != 0) {
                        longCumulateTask.quietlyComplete();
                        return;
                    }
                    return;
                }
            }
            throw new NullPointerException();
        }
    }

    private ArrayPrefixHelpers() {
    }
}
