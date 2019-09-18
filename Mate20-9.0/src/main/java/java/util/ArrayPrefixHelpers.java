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

        public CumulateTask(CumulateTask<T> parent, BinaryOperator<T> function2, T[] array2, int lo2, int hi2) {
            super(parent);
            this.function = function2;
            this.array = array2;
            this.origin = lo2;
            this.lo = lo2;
            this.fence = hi2;
            this.hi = hi2;
            int p = (hi2 - lo2) / (ForkJoinPool.getCommonPoolParallelism() << 3);
            this.threshold = p > 16 ? p : 16;
        }

        CumulateTask(CumulateTask<T> parent, BinaryOperator<T> function2, T[] array2, int origin2, int fence2, int threshold2, int lo2, int hi2) {
            super(parent);
            this.function = function2;
            this.array = array2;
            this.origin = origin2;
            this.fence = fence2;
            this.threshold = threshold2;
            this.lo = lo2;
            this.hi = hi2;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:104:0x01a6, code lost:
            r24 = r1;
            r8 = r10;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:129:?, code lost:
            return;
         */
        /* JADX WARNING: Removed duplicated region for block: B:100:0x0190  */
        /* JADX WARNING: Removed duplicated region for block: B:128:0x019f A[SYNTHETIC] */
        public final void compute() {
            T sum;
            int th;
            int refork;
            int i;
            int first;
            T sum2;
            CumulateTask<T> t;
            CumulateTask<T> f;
            BinaryOperator<T> binaryOperator = this.function;
            BinaryOperator<T> fn = binaryOperator;
            if (binaryOperator != null) {
                T[] tArr = this.array;
                T[] a = tArr;
                if (tArr != null) {
                    int th2 = this.threshold;
                    int org = this.origin;
                    int fnc = this.fence;
                    CumulateTask<T> t2 = this;
                    while (true) {
                        CumulateTask<T> t3 = t2;
                        int i2 = t3.lo;
                        int l = i2;
                        if (i2 < 0) {
                            break;
                        }
                        int i3 = t3.hi;
                        int h = i3;
                        if (i3 <= a.length) {
                            if (h - l <= th2) {
                                int h2 = h;
                                int l2 = l;
                                while (true) {
                                    int pendingCount = t3.getPendingCount();
                                    int b = pendingCount;
                                    if ((pendingCount & 4) != 0) {
                                        break;
                                    }
                                    int state = (b & 1) != 0 ? 4 : l2 > org ? 2 : 6;
                                    if (t3.compareAndSetPendingCount(b, b | state)) {
                                        if (state != 2) {
                                            if (l2 == org) {
                                                sum2 = a[org];
                                                first = org + 1;
                                            } else {
                                                sum2 = t3.in;
                                                first = l2;
                                            }
                                            sum = sum2;
                                            int i4 = first;
                                            while (true) {
                                                int h3 = h2;
                                                if (i4 >= h3) {
                                                    break;
                                                }
                                                T apply = fn.apply(sum, a[i4]);
                                                sum = apply;
                                                a[i4] = apply;
                                                i4++;
                                                h2 = h3;
                                            }
                                        } else {
                                            int h4 = h2;
                                            if (h4 < fnc) {
                                                int i5 = l2 + 1;
                                                T sum3 = a[l2];
                                                while (true) {
                                                    int i6 = i5;
                                                    if (i6 >= h4) {
                                                        break;
                                                    }
                                                    sum3 = fn.apply(sum, a[i6]);
                                                    i5 = i6 + 1;
                                                }
                                            } else {
                                                sum = t3.in;
                                            }
                                        }
                                        t3.out = sum;
                                        while (true) {
                                            CumulateTask<T> partmp = (CumulateTask) t3.getCompleter();
                                            CumulateTask<T> par = partmp;
                                            if (partmp != null) {
                                                int b2 = par.getPendingCount();
                                                if ((b2 & state & 4) != 0) {
                                                    t3 = par;
                                                    th = th2;
                                                } else if ((b2 & state & 2) != 0) {
                                                    CumulateTask<T> cumulateTask = par.left;
                                                    CumulateTask<T> lt = cumulateTask;
                                                    if (cumulateTask != null) {
                                                        CumulateTask<T> cumulateTask2 = par.right;
                                                        CumulateTask<T> rt = cumulateTask2;
                                                        if (cumulateTask2 != null) {
                                                            T lout = lt.out;
                                                            th = th2;
                                                            CumulateTask<T> rt2 = rt;
                                                            par.out = rt2.hi == fnc ? lout : fn.apply(lout, rt2.out);
                                                            refork = ((b2 & 1) == 0 || par.lo != org) ? 0 : 1;
                                                            i = b2 | state | refork;
                                                            int nextState = i;
                                                            if (i != b2 || par.compareAndSetPendingCount(b2, nextState)) {
                                                                state = 2;
                                                                t3 = par;
                                                                if (refork == 0) {
                                                                    par.fork();
                                                                }
                                                            }
                                                        }
                                                    }
                                                    th = th2;
                                                    CumulateTask<T> cumulateTask3 = lt;
                                                    if ((b2 & 1) == 0) {
                                                    }
                                                    i = b2 | state | refork;
                                                    int nextState2 = i;
                                                    if (i != b2) {
                                                    }
                                                    state = 2;
                                                    t3 = par;
                                                    if (refork == 0) {
                                                    }
                                                } else {
                                                    th = th2;
                                                    if (par.compareAndSetPendingCount(b2, b2 | state)) {
                                                        return;
                                                    }
                                                }
                                                th2 = th;
                                            } else if ((state & 4) != 0) {
                                                t3.quietlyComplete();
                                            }
                                        }
                                    }
                                }
                            } else {
                                CumulateTask<T> lt2 = t3.left;
                                CumulateTask<T> rt3 = t3.right;
                                if (lt2 == null) {
                                    int mid = (l + h) >>> 1;
                                    CumulateTask cumulateTask4 = t3;
                                    BinaryOperator<T> binaryOperator2 = fn;
                                    T[] tArr2 = a;
                                    CumulateTask<T> f2 = r2;
                                    int i7 = org;
                                    CumulateTask<T> cumulateTask5 = rt3;
                                    int i8 = fnc;
                                    CumulateTask<T> cumulateTask6 = lt2;
                                    int i9 = th2;
                                    int l3 = l;
                                    CumulateTask<T> cumulateTask7 = new CumulateTask<>(cumulateTask4, binaryOperator2, tArr2, i7, i8, i9, mid, h);
                                    t3.right = f2;
                                    CumulateTask<T> rt4 = f2;
                                    CumulateTask<T> f3 = f2;
                                    CumulateTask<T> f4 = r2;
                                    CumulateTask<T> cumulateTask8 = new CumulateTask<>(cumulateTask4, binaryOperator2, tArr2, i7, i8, i9, l3, mid);
                                    t3.left = f4;
                                    t = f4;
                                    CumulateTask<T> cumulateTask9 = f4;
                                    int i10 = l3;
                                    f = f3;
                                } else {
                                    int i11 = h;
                                    int l4 = l;
                                    T pin = t3.in;
                                    CumulateTask<T> lt3 = lt2;
                                    lt3.in = pin;
                                    f = null;
                                    CumulateTask<T> t4 = null;
                                    CumulateTask<T> rt5 = rt3;
                                    if (rt5 != null) {
                                        T lout2 = lt3.out;
                                        rt5.in = l4 == org ? lout2 : fn.apply(pin, lout2);
                                        while (true) {
                                            int pendingCount2 = rt5.getPendingCount();
                                            int c = pendingCount2;
                                            if ((pendingCount2 & 1) == 0) {
                                                if (rt5.compareAndSetPendingCount(c, c | 1)) {
                                                    t4 = rt5;
                                                    break;
                                                }
                                            } else {
                                                break;
                                            }
                                        }
                                    }
                                    while (true) {
                                        int pendingCount3 = lt3.getPendingCount();
                                        int c2 = pendingCount3;
                                        if ((pendingCount3 & 1) != 0) {
                                            t = t4;
                                            break;
                                        } else if (lt3.compareAndSetPendingCount(c2, c2 | 1)) {
                                            if (t4 != null) {
                                                f = t4;
                                            }
                                            t = lt3;
                                        }
                                    }
                                    if (t == null) {
                                        break;
                                    }
                                    CumulateTask<T> cumulateTask10 = rt5;
                                }
                                if (f != null) {
                                    f.fork();
                                }
                                t2 = t;
                            }
                        } else {
                            break;
                        }
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

        public DoubleCumulateTask(DoubleCumulateTask parent, DoubleBinaryOperator function2, double[] array2, int lo2, int hi2) {
            super(parent);
            this.function = function2;
            this.array = array2;
            this.origin = lo2;
            this.lo = lo2;
            this.fence = hi2;
            this.hi = hi2;
            int p = (hi2 - lo2) / (ForkJoinPool.getCommonPoolParallelism() << 3);
            this.threshold = p > 16 ? p : 16;
        }

        DoubleCumulateTask(DoubleCumulateTask parent, DoubleBinaryOperator function2, double[] array2, int origin2, int fence2, int threshold2, int lo2, int hi2) {
            super(parent);
            this.function = function2;
            this.array = array2;
            this.origin = origin2;
            this.fence = fence2;
            this.threshold = threshold2;
            this.lo = lo2;
            this.hi = hi2;
        }

        /* JADX WARNING: Removed duplicated region for block: B:125:0x01a7 A[SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:98:0x0198  */
        public final void compute() {
            int b;
            int i;
            int state;
            double sum;
            double sum2;
            int refork;
            int i2;
            double d;
            double sum3;
            int first;
            DoubleCumulateTask t;
            DoubleCumulateTask f;
            double d2;
            DoubleBinaryOperator doubleBinaryOperator = this.function;
            DoubleBinaryOperator fn = doubleBinaryOperator;
            if (doubleBinaryOperator != null) {
                double[] dArr = this.array;
                double[] a = dArr;
                if (dArr != null) {
                    int th = this.threshold;
                    int org = this.origin;
                    int fnc = this.fence;
                    DoubleCumulateTask t2 = this;
                    while (true) {
                        DoubleCumulateTask t3 = t2;
                        int i3 = t3.lo;
                        int l = i3;
                        if (i3 >= 0) {
                            int i4 = t3.hi;
                            int h = i4;
                            if (i4 > a.length) {
                                return;
                            }
                            if (h - l > th) {
                                DoubleCumulateTask lt = t3.left;
                                DoubleCumulateTask rt = t3.right;
                                if (lt == null) {
                                    int mid = (l + h) >>> 1;
                                    DoubleCumulateTask doubleCumulateTask = t3;
                                    DoubleBinaryOperator doubleBinaryOperator2 = fn;
                                    double[] dArr2 = a;
                                    DoubleCumulateTask f2 = r2;
                                    int i5 = org;
                                    DoubleCumulateTask doubleCumulateTask2 = rt;
                                    int i6 = fnc;
                                    DoubleCumulateTask doubleCumulateTask3 = lt;
                                    int i7 = th;
                                    int l2 = l;
                                    DoubleCumulateTask doubleCumulateTask4 = new DoubleCumulateTask(doubleCumulateTask, doubleBinaryOperator2, dArr2, i5, i6, i7, mid, h);
                                    t3.right = f2;
                                    DoubleCumulateTask rt2 = f2;
                                    DoubleCumulateTask f3 = f2;
                                    DoubleCumulateTask f4 = r2;
                                    DoubleCumulateTask doubleCumulateTask5 = new DoubleCumulateTask(doubleCumulateTask, doubleBinaryOperator2, dArr2, i5, i6, i7, l2, mid);
                                    t3.left = f4;
                                    DoubleCumulateTask doubleCumulateTask6 = f4;
                                    t = f4;
                                    int i8 = l2;
                                    f = f3;
                                } else {
                                    int i9 = h;
                                    int l3 = l;
                                    double pin = t3.in;
                                    DoubleCumulateTask lt2 = lt;
                                    lt2.in = pin;
                                    f = null;
                                    DoubleCumulateTask t4 = null;
                                    if (rt != null) {
                                        double lout = lt2.out;
                                        if (l3 == org) {
                                            double d3 = pin;
                                            d2 = lout;
                                        } else {
                                            double d4 = pin;
                                            d2 = fn.applyAsDouble(pin, lout);
                                        }
                                        rt.in = d2;
                                        while (true) {
                                            int pendingCount = rt.getPendingCount();
                                            int c = pendingCount;
                                            if ((pendingCount & 1) == 0) {
                                                if (rt.compareAndSetPendingCount(c, c | 1)) {
                                                    t4 = rt;
                                                    break;
                                                }
                                            } else {
                                                break;
                                            }
                                        }
                                    } else {
                                        int i10 = l3;
                                    }
                                    while (true) {
                                        int pendingCount2 = lt2.getPendingCount();
                                        int c2 = pendingCount2;
                                        if ((pendingCount2 & 1) != 0) {
                                            t = t4;
                                            break;
                                        } else if (lt2.compareAndSetPendingCount(c2, c2 | 1)) {
                                            if (t4 != null) {
                                                f = t4;
                                            }
                                            t = lt2;
                                        }
                                    }
                                    if (t != null) {
                                        DoubleCumulateTask doubleCumulateTask7 = rt;
                                    } else {
                                        return;
                                    }
                                }
                                if (f != null) {
                                    f.fork();
                                }
                                t2 = t;
                            } else {
                                int h2 = h;
                                do {
                                    int pendingCount3 = t3.getPendingCount();
                                    b = pendingCount3;
                                    i = 4;
                                    if ((pendingCount3 & 4) == 0) {
                                        state = (b & 1) != 0 ? 4 : l > org ? 2 : 6;
                                    } else {
                                        return;
                                    }
                                } while (!t3.compareAndSetPendingCount(b, b | state));
                                if (state != 2) {
                                    if (l == org) {
                                        sum3 = a[org];
                                        first = org + 1;
                                    } else {
                                        sum3 = t3.in;
                                        first = l;
                                    }
                                    sum = sum3;
                                    int i11 = first;
                                    while (true) {
                                        int h3 = h2;
                                        if (i11 >= h3) {
                                            break;
                                        }
                                        double applyAsDouble = fn.applyAsDouble(sum, a[i11]);
                                        sum = applyAsDouble;
                                        a[i11] = applyAsDouble;
                                        i11++;
                                        h2 = h3;
                                    }
                                } else {
                                    int h4 = h2;
                                    if (h4 < fnc) {
                                        double sum4 = a[l];
                                        for (int i12 = l + 1; i12 < h4; i12++) {
                                            sum4 = fn.applyAsDouble(sum, a[i12]);
                                        }
                                    } else {
                                        sum = t3.in;
                                    }
                                }
                                double sum5 = sum;
                                t3.out = sum5;
                                while (true) {
                                    DoubleCumulateTask doubleCumulateTask8 = (DoubleCumulateTask) t3.getCompleter();
                                    DoubleCumulateTask par = doubleCumulateTask8;
                                    if (doubleCumulateTask8 != null) {
                                        int b2 = par.getPendingCount();
                                        if ((b2 & state & i) != 0) {
                                            sum2 = sum5;
                                            t3 = par;
                                        } else if ((b2 & state & 2) != 0) {
                                            DoubleCumulateTask doubleCumulateTask9 = par.left;
                                            DoubleCumulateTask lt3 = doubleCumulateTask9;
                                            if (doubleCumulateTask9 != null) {
                                                DoubleCumulateTask doubleCumulateTask10 = par.right;
                                                DoubleCumulateTask rt3 = doubleCumulateTask10;
                                                if (doubleCumulateTask10 != null) {
                                                    sum2 = sum5;
                                                    DoubleCumulateTask lt4 = lt3;
                                                    double lout2 = lt4.out;
                                                    DoubleCumulateTask rt4 = rt3;
                                                    if (rt4.hi == fnc) {
                                                        DoubleCumulateTask doubleCumulateTask11 = lt4;
                                                        d = lout2;
                                                    } else {
                                                        DoubleCumulateTask doubleCumulateTask12 = lt4;
                                                        d = fn.applyAsDouble(lout2, rt4.out);
                                                    }
                                                    par.out = d;
                                                    refork = ((b2 & 1) == 0 || par.lo != org) ? 0 : 1;
                                                    i2 = b2 | state | refork;
                                                    int nextState = i2;
                                                    if (i2 != b2 || par.compareAndSetPendingCount(b2, nextState)) {
                                                        state = 2;
                                                        t3 = par;
                                                        if (refork == 0) {
                                                            par.fork();
                                                        }
                                                    }
                                                }
                                            }
                                            sum2 = sum5;
                                            DoubleCumulateTask doubleCumulateTask13 = lt3;
                                            if ((b2 & 1) == 0) {
                                            }
                                            i2 = b2 | state | refork;
                                            int nextState2 = i2;
                                            if (i2 != b2) {
                                            }
                                            state = 2;
                                            t3 = par;
                                            if (refork == 0) {
                                            }
                                        } else {
                                            sum2 = sum5;
                                            if (par.compareAndSetPendingCount(b2, b2 | state)) {
                                                return;
                                            }
                                        }
                                        sum5 = sum2;
                                        i = 4;
                                    } else if ((state & 4) != 0) {
                                        t3.quietlyComplete();
                                        return;
                                    } else {
                                        return;
                                    }
                                }
                            }
                        } else {
                            return;
                        }
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

        public IntCumulateTask(IntCumulateTask parent, IntBinaryOperator function2, int[] array2, int lo2, int hi2) {
            super(parent);
            this.function = function2;
            this.array = array2;
            this.origin = lo2;
            this.lo = lo2;
            this.fence = hi2;
            this.hi = hi2;
            int p = (hi2 - lo2) / (ForkJoinPool.getCommonPoolParallelism() << 3);
            this.threshold = p > 16 ? p : 16;
        }

        IntCumulateTask(IntCumulateTask parent, IntBinaryOperator function2, int[] array2, int origin2, int fence2, int threshold2, int lo2, int hi2) {
            super(parent);
            this.function = function2;
            this.array = array2;
            this.origin = origin2;
            this.fence = fence2;
            this.threshold = threshold2;
            this.lo = lo2;
            this.hi = hi2;
        }

        /* JADX WARNING: Removed duplicated region for block: B:127:0x0193 A[SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:99:0x0186  */
        public final void compute() {
            int l;
            int b;
            int i;
            int i2;
            int state;
            int sum;
            int refork;
            int i3;
            int first;
            int sum2;
            IntCumulateTask t;
            IntCumulateTask f;
            IntBinaryOperator intBinaryOperator = this.function;
            IntBinaryOperator fn = intBinaryOperator;
            if (intBinaryOperator != null) {
                int[] iArr = this.array;
                int[] a = iArr;
                if (iArr != null) {
                    int th = this.threshold;
                    int org = this.origin;
                    int fnc = this.fence;
                    IntCumulateTask t2 = this;
                    while (true) {
                        IntCumulateTask t3 = t2;
                        int i4 = t3.lo;
                        l = i4;
                        if (i4 < 0) {
                            break;
                        }
                        int i5 = t3.hi;
                        int h = i5;
                        if (i5 > a.length) {
                            break;
                        } else if (h - l > th) {
                            IntCumulateTask lt = t3.left;
                            IntCumulateTask rt = t3.right;
                            if (lt == null) {
                                int mid = (l + h) >>> 1;
                                IntCumulateTask intCumulateTask = t3;
                                IntBinaryOperator intBinaryOperator2 = fn;
                                int[] iArr2 = a;
                                IntCumulateTask f2 = r2;
                                int i6 = org;
                                IntCumulateTask intCumulateTask2 = rt;
                                int i7 = fnc;
                                IntCumulateTask intCumulateTask3 = lt;
                                int i8 = th;
                                int l2 = l;
                                IntCumulateTask intCumulateTask4 = new IntCumulateTask(intCumulateTask, intBinaryOperator2, iArr2, i6, i7, i8, mid, h);
                                t3.right = f2;
                                IntCumulateTask rt2 = f2;
                                IntCumulateTask f3 = f2;
                                IntCumulateTask f4 = r2;
                                IntCumulateTask intCumulateTask5 = new IntCumulateTask(intCumulateTask, intBinaryOperator2, iArr2, i6, i7, i8, l2, mid);
                                t3.left = f4;
                                t = f4;
                                IntCumulateTask intCumulateTask6 = f4;
                                int i9 = l2;
                                f = f3;
                            } else {
                                int i10 = h;
                                int l3 = l;
                                int pin = t3.in;
                                IntCumulateTask lt2 = lt;
                                lt2.in = pin;
                                f = null;
                                IntCumulateTask t4 = null;
                                IntCumulateTask rt3 = rt;
                                if (rt3 != null) {
                                    int lout = lt2.out;
                                    rt3.in = l3 == org ? lout : fn.applyAsInt(pin, lout);
                                    while (true) {
                                        int pendingCount = rt3.getPendingCount();
                                        int c = pendingCount;
                                        if ((pendingCount & 1) == 0) {
                                            if (rt3.compareAndSetPendingCount(c, c | 1)) {
                                                t4 = rt3;
                                                break;
                                            }
                                        } else {
                                            break;
                                        }
                                    }
                                }
                                while (true) {
                                    int pendingCount2 = lt2.getPendingCount();
                                    int c2 = pendingCount2;
                                    if ((pendingCount2 & 1) != 0) {
                                        t = t4;
                                        break;
                                    } else if (lt2.compareAndSetPendingCount(c2, c2 | 1)) {
                                        if (t4 != null) {
                                            f = t4;
                                        }
                                        t = lt2;
                                    }
                                }
                                if (t != null) {
                                    IntCumulateTask intCumulateTask7 = rt3;
                                } else {
                                    return;
                                }
                            }
                            if (f != null) {
                                f.fork();
                            }
                            t2 = t;
                        } else {
                            int h2 = h;
                            int l4 = l;
                            do {
                                int pendingCount3 = t3.getPendingCount();
                                b = pendingCount3;
                                i = 4;
                                if ((pendingCount3 & 4) == 0) {
                                    i2 = 2;
                                    state = (b & 1) != 0 ? 4 : l4 > org ? 2 : 6;
                                } else {
                                    return;
                                }
                            } while (!t3.compareAndSetPendingCount(b, b | state));
                            if (state != 2) {
                                if (l4 == org) {
                                    sum2 = a[org];
                                    first = org + 1;
                                } else {
                                    sum2 = t3.in;
                                    first = l4;
                                }
                                sum = sum2;
                                int i11 = first;
                                while (true) {
                                    int h3 = h2;
                                    if (i11 >= h3) {
                                        break;
                                    }
                                    int applyAsInt = fn.applyAsInt(sum, a[i11]);
                                    sum = applyAsInt;
                                    a[i11] = applyAsInt;
                                    i11++;
                                    h2 = h3;
                                }
                            } else {
                                int h4 = h2;
                                if (h4 < fnc) {
                                    int i12 = l4 + 1;
                                    int sum3 = a[l4];
                                    while (true) {
                                        int i13 = i12;
                                        if (i13 >= h4) {
                                            break;
                                        }
                                        sum3 = fn.applyAsInt(sum, a[i13]);
                                        i12 = i13 + 1;
                                    }
                                } else {
                                    sum = t3.in;
                                }
                            }
                            t3.out = sum;
                            while (true) {
                                IntCumulateTask intCumulateTask8 = (IntCumulateTask) t3.getCompleter();
                                IntCumulateTask par = intCumulateTask8;
                                if (intCumulateTask8 != null) {
                                    int b2 = par.getPendingCount();
                                    if ((b2 & state & i) != 0) {
                                        t3 = par;
                                    } else if ((b2 & state & i2) != 0) {
                                        IntCumulateTask intCumulateTask9 = par.left;
                                        IntCumulateTask lt3 = intCumulateTask9;
                                        if (intCumulateTask9 != null) {
                                            IntCumulateTask intCumulateTask10 = par.right;
                                            IntCumulateTask rt4 = intCumulateTask10;
                                            if (intCumulateTask10 != null) {
                                                int lout2 = lt3.out;
                                                IntCumulateTask rt5 = rt4;
                                                par.out = rt5.hi == fnc ? lout2 : fn.applyAsInt(lout2, rt5.out);
                                                refork = ((b2 & 1) == 0 || par.lo != org) ? 0 : 1;
                                                i3 = b2 | state | refork;
                                                int nextState = i3;
                                                if (i3 != b2 || par.compareAndSetPendingCount(b2, nextState)) {
                                                    state = 2;
                                                    t3 = par;
                                                    if (refork == 0) {
                                                        par.fork();
                                                    }
                                                }
                                            }
                                        }
                                        if ((b2 & 1) == 0) {
                                        }
                                        i3 = b2 | state | refork;
                                        int nextState2 = i3;
                                        if (i3 != b2) {
                                        }
                                        state = 2;
                                        t3 = par;
                                        if (refork == 0) {
                                        }
                                    } else if (par.compareAndSetPendingCount(b2, b2 | state)) {
                                        return;
                                    }
                                    i = 4;
                                    i2 = 2;
                                } else if ((state & 4) != 0) {
                                    t3.quietlyComplete();
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

        public LongCumulateTask(LongCumulateTask parent, LongBinaryOperator function2, long[] array2, int lo2, int hi2) {
            super(parent);
            this.function = function2;
            this.array = array2;
            this.origin = lo2;
            this.lo = lo2;
            this.fence = hi2;
            this.hi = hi2;
            int p = (hi2 - lo2) / (ForkJoinPool.getCommonPoolParallelism() << 3);
            this.threshold = p > 16 ? p : 16;
        }

        LongCumulateTask(LongCumulateTask parent, LongBinaryOperator function2, long[] array2, int origin2, int fence2, int threshold2, int lo2, int hi2) {
            super(parent);
            this.function = function2;
            this.array = array2;
            this.origin = origin2;
            this.fence = fence2;
            this.threshold = threshold2;
            this.lo = lo2;
            this.hi = hi2;
        }

        /* JADX WARNING: Removed duplicated region for block: B:125:0x01a7 A[SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:98:0x0198  */
        public final void compute() {
            int b;
            int i;
            int state;
            long sum;
            long sum2;
            int refork;
            int i2;
            long j;
            long sum3;
            int first;
            LongCumulateTask t;
            LongCumulateTask f;
            long j2;
            LongBinaryOperator longBinaryOperator = this.function;
            LongBinaryOperator fn = longBinaryOperator;
            if (longBinaryOperator != null) {
                long[] jArr = this.array;
                long[] a = jArr;
                if (jArr != null) {
                    int th = this.threshold;
                    int org = this.origin;
                    int fnc = this.fence;
                    LongCumulateTask t2 = this;
                    while (true) {
                        LongCumulateTask t3 = t2;
                        int i3 = t3.lo;
                        int l = i3;
                        if (i3 >= 0) {
                            int i4 = t3.hi;
                            int h = i4;
                            if (i4 > a.length) {
                                return;
                            }
                            if (h - l > th) {
                                LongCumulateTask lt = t3.left;
                                LongCumulateTask rt = t3.right;
                                if (lt == null) {
                                    int mid = (l + h) >>> 1;
                                    LongCumulateTask longCumulateTask = t3;
                                    LongBinaryOperator longBinaryOperator2 = fn;
                                    long[] jArr2 = a;
                                    LongCumulateTask f2 = r2;
                                    int i5 = org;
                                    LongCumulateTask longCumulateTask2 = rt;
                                    int i6 = fnc;
                                    LongCumulateTask longCumulateTask3 = lt;
                                    int i7 = th;
                                    int l2 = l;
                                    LongCumulateTask longCumulateTask4 = new LongCumulateTask(longCumulateTask, longBinaryOperator2, jArr2, i5, i6, i7, mid, h);
                                    t3.right = f2;
                                    LongCumulateTask rt2 = f2;
                                    LongCumulateTask f3 = f2;
                                    LongCumulateTask f4 = r2;
                                    LongCumulateTask longCumulateTask5 = new LongCumulateTask(longCumulateTask, longBinaryOperator2, jArr2, i5, i6, i7, l2, mid);
                                    t3.left = f4;
                                    LongCumulateTask longCumulateTask6 = f4;
                                    t = f4;
                                    int i8 = l2;
                                    f = f3;
                                } else {
                                    int i9 = h;
                                    int l3 = l;
                                    long pin = t3.in;
                                    LongCumulateTask lt2 = lt;
                                    lt2.in = pin;
                                    f = null;
                                    LongCumulateTask t4 = null;
                                    if (rt != null) {
                                        long lout = lt2.out;
                                        if (l3 == org) {
                                            long j3 = pin;
                                            j2 = lout;
                                        } else {
                                            long j4 = pin;
                                            j2 = fn.applyAsLong(pin, lout);
                                        }
                                        rt.in = j2;
                                        while (true) {
                                            int pendingCount = rt.getPendingCount();
                                            int c = pendingCount;
                                            if ((pendingCount & 1) == 0) {
                                                if (rt.compareAndSetPendingCount(c, c | 1)) {
                                                    t4 = rt;
                                                    break;
                                                }
                                            } else {
                                                break;
                                            }
                                        }
                                    } else {
                                        int i10 = l3;
                                    }
                                    while (true) {
                                        int pendingCount2 = lt2.getPendingCount();
                                        int c2 = pendingCount2;
                                        if ((pendingCount2 & 1) != 0) {
                                            t = t4;
                                            break;
                                        } else if (lt2.compareAndSetPendingCount(c2, c2 | 1)) {
                                            if (t4 != null) {
                                                f = t4;
                                            }
                                            t = lt2;
                                        }
                                    }
                                    if (t != null) {
                                        LongCumulateTask longCumulateTask7 = rt;
                                    } else {
                                        return;
                                    }
                                }
                                if (f != null) {
                                    f.fork();
                                }
                                t2 = t;
                            } else {
                                int h2 = h;
                                do {
                                    int pendingCount3 = t3.getPendingCount();
                                    b = pendingCount3;
                                    i = 4;
                                    if ((pendingCount3 & 4) == 0) {
                                        state = (b & 1) != 0 ? 4 : l > org ? 2 : 6;
                                    } else {
                                        return;
                                    }
                                } while (!t3.compareAndSetPendingCount(b, b | state));
                                if (state != 2) {
                                    if (l == org) {
                                        sum3 = a[org];
                                        first = org + 1;
                                    } else {
                                        sum3 = t3.in;
                                        first = l;
                                    }
                                    sum = sum3;
                                    int i11 = first;
                                    while (true) {
                                        int h3 = h2;
                                        if (i11 >= h3) {
                                            break;
                                        }
                                        long applyAsLong = fn.applyAsLong(sum, a[i11]);
                                        sum = applyAsLong;
                                        a[i11] = applyAsLong;
                                        i11++;
                                        h2 = h3;
                                    }
                                } else {
                                    int h4 = h2;
                                    if (h4 < fnc) {
                                        long sum4 = a[l];
                                        for (int i12 = l + 1; i12 < h4; i12++) {
                                            sum4 = fn.applyAsLong(sum, a[i12]);
                                        }
                                    } else {
                                        sum = t3.in;
                                    }
                                }
                                long sum5 = sum;
                                t3.out = sum5;
                                while (true) {
                                    LongCumulateTask longCumulateTask8 = (LongCumulateTask) t3.getCompleter();
                                    LongCumulateTask par = longCumulateTask8;
                                    if (longCumulateTask8 != null) {
                                        int b2 = par.getPendingCount();
                                        if ((b2 & state & i) != 0) {
                                            sum2 = sum5;
                                            t3 = par;
                                        } else if ((b2 & state & 2) != 0) {
                                            LongCumulateTask longCumulateTask9 = par.left;
                                            LongCumulateTask lt3 = longCumulateTask9;
                                            if (longCumulateTask9 != null) {
                                                LongCumulateTask longCumulateTask10 = par.right;
                                                LongCumulateTask rt3 = longCumulateTask10;
                                                if (longCumulateTask10 != null) {
                                                    sum2 = sum5;
                                                    LongCumulateTask lt4 = lt3;
                                                    long lout2 = lt4.out;
                                                    LongCumulateTask rt4 = rt3;
                                                    if (rt4.hi == fnc) {
                                                        LongCumulateTask longCumulateTask11 = lt4;
                                                        j = lout2;
                                                    } else {
                                                        LongCumulateTask longCumulateTask12 = lt4;
                                                        j = fn.applyAsLong(lout2, rt4.out);
                                                    }
                                                    par.out = j;
                                                    refork = ((b2 & 1) == 0 || par.lo != org) ? 0 : 1;
                                                    i2 = b2 | state | refork;
                                                    int nextState = i2;
                                                    if (i2 != b2 || par.compareAndSetPendingCount(b2, nextState)) {
                                                        state = 2;
                                                        t3 = par;
                                                        if (refork == 0) {
                                                            par.fork();
                                                        }
                                                    }
                                                }
                                            }
                                            sum2 = sum5;
                                            LongCumulateTask longCumulateTask13 = lt3;
                                            if ((b2 & 1) == 0) {
                                            }
                                            i2 = b2 | state | refork;
                                            int nextState2 = i2;
                                            if (i2 != b2) {
                                            }
                                            state = 2;
                                            t3 = par;
                                            if (refork == 0) {
                                            }
                                        } else {
                                            sum2 = sum5;
                                            if (par.compareAndSetPendingCount(b2, b2 | state)) {
                                                return;
                                            }
                                        }
                                        sum5 = sum2;
                                        i = 4;
                                    } else if ((state & 4) != 0) {
                                        t3.quietlyComplete();
                                        return;
                                    } else {
                                        return;
                                    }
                                }
                            }
                        } else {
                            return;
                        }
                    }
                }
            }
            throw new NullPointerException();
        }
    }

    private ArrayPrefixHelpers() {
    }
}
