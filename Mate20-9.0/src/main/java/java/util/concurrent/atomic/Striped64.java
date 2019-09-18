package java.util.concurrent.atomic;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleBinaryOperator;
import java.util.function.LongBinaryOperator;
import sun.misc.Unsafe;

abstract class Striped64 extends Number {
    private static final long BASE;
    private static final long CELLSBUSY;
    static final int NCPU = Runtime.getRuntime().availableProcessors();
    private static final long PROBE;
    private static final Unsafe U = Unsafe.getUnsafe();
    volatile transient long base;
    volatile transient Cell[] cells;
    volatile transient int cellsBusy;

    static final class Cell {
        private static final Unsafe U = Unsafe.getUnsafe();
        private static final long VALUE;
        volatile long value;

        Cell(long x) {
            this.value = x;
        }

        /* access modifiers changed from: package-private */
        public final boolean cas(long cmp, long val) {
            return U.compareAndSwapLong(this, VALUE, cmp, val);
        }

        /* access modifiers changed from: package-private */
        public final void reset() {
            U.putLongVolatile(this, VALUE, 0);
        }

        /* access modifiers changed from: package-private */
        public final void reset(long identity) {
            U.putLongVolatile(this, VALUE, identity);
        }

        static {
            try {
                VALUE = U.objectFieldOffset(Cell.class.getDeclaredField("value"));
            } catch (ReflectiveOperationException e) {
                throw new Error((Throwable) e);
            }
        }
    }

    static {
        try {
            BASE = U.objectFieldOffset(Striped64.class.getDeclaredField("base"));
            CELLSBUSY = U.objectFieldOffset(Striped64.class.getDeclaredField("cellsBusy"));
            PROBE = U.objectFieldOffset(Thread.class.getDeclaredField("threadLocalRandomProbe"));
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }

    Striped64() {
    }

    /* access modifiers changed from: package-private */
    public final boolean casBase(long cmp, long val) {
        return U.compareAndSwapLong(this, BASE, cmp, val);
    }

    /* access modifiers changed from: package-private */
    public final boolean casCellsBusy() {
        return U.compareAndSwapInt(this, CELLSBUSY, 0, 1);
    }

    static final int getProbe() {
        return U.getInt(Thread.currentThread(), PROBE);
    }

    static final int advanceProbe(int probe) {
        int probe2 = probe ^ (probe << 13);
        int probe3 = probe2 ^ (probe2 >>> 17);
        int probe4 = probe3 ^ (probe3 << 5);
        U.putInt(Thread.currentThread(), PROBE, probe4);
        return probe4;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public final void longAccumulate(long x, LongBinaryOperator fn, boolean wasUncontended) {
        boolean wasUncontended2;
        boolean wasUncontended3;
        long j = x;
        LongBinaryOperator longBinaryOperator = fn;
        int probe = getProbe();
        int h = probe;
        if (probe == 0) {
            ThreadLocalRandom.current();
            h = getProbe();
            wasUncontended2 = true;
        } else {
            wasUncontended2 = wasUncontended;
        }
        int i = 0;
        boolean wasUncontended4 = wasUncontended2;
        boolean wasUncontended5 = false;
        while (true) {
            boolean collide = wasUncontended5;
            Cell[] cellArr = this.cells;
            Cell[] as = cellArr;
            if (cellArr != null) {
                int length = as.length;
                int n = length;
                if (length > 0) {
                    Cell cell = as[(n - 1) & h];
                    Cell a = cell;
                    if (cell == null) {
                        if (this.cellsBusy == 0) {
                            Cell r = new Cell(j);
                            if (this.cellsBusy == 0 && casCellsBusy()) {
                                try {
                                    Cell[] cellArr2 = this.cells;
                                    Cell[] rs = cellArr2;
                                    if (cellArr2 != null) {
                                        int length2 = rs.length;
                                        int m = length2;
                                        if (length2 > 0) {
                                            int i2 = (m - 1) & h;
                                            int j2 = i2;
                                            if (rs[i2] == null) {
                                                rs[j2] = r;
                                                this.cellsBusy = i;
                                                boolean z = wasUncontended4;
                                                return;
                                            }
                                        }
                                    }
                                    this.cellsBusy = i;
                                    wasUncontended5 = collide;
                                } catch (Throwable th) {
                                    this.cellsBusy = i;
                                    throw th;
                                }
                            }
                        }
                        collide = false;
                    } else if (!wasUncontended4) {
                        wasUncontended4 = true;
                    } else {
                        long j3 = a.value;
                        long v = j3;
                        wasUncontended3 = wasUncontended4;
                        if (!a.cas(j3, longBinaryOperator == null ? v + j : longBinaryOperator.applyAsLong(v, j))) {
                            if (n >= NCPU || this.cells != as) {
                                collide = false;
                                h = advanceProbe(h);
                                wasUncontended5 = collide;
                                wasUncontended4 = wasUncontended3;
                                i = 0;
                            } else {
                                if (!collide) {
                                    collide = true;
                                } else if (this.cellsBusy == 0 && casCellsBusy()) {
                                    try {
                                        if (this.cells == as) {
                                            this.cells = (Cell[]) Arrays.copyOf((T[]) as, n << 1);
                                        }
                                        i = 0;
                                        this.cellsBusy = 0;
                                        wasUncontended5 = false;
                                        wasUncontended4 = wasUncontended3;
                                    } catch (Throwable th2) {
                                        this.cellsBusy = 0;
                                        throw th2;
                                    }
                                }
                                h = advanceProbe(h);
                                wasUncontended5 = collide;
                                wasUncontended4 = wasUncontended3;
                                i = 0;
                            }
                        } else {
                            return;
                        }
                    }
                    wasUncontended3 = wasUncontended4;
                    h = advanceProbe(h);
                    wasUncontended5 = collide;
                    wasUncontended4 = wasUncontended3;
                    i = 0;
                }
            }
            boolean wasUncontended6 = wasUncontended4;
            if (this.cellsBusy == 0 && this.cells == as && casCellsBusy()) {
                try {
                    if (this.cells == as) {
                        Cell[] rs2 = new Cell[2];
                        rs2[h & 1] = new Cell(j);
                        this.cells = rs2;
                        this.cellsBusy = 0;
                        return;
                    }
                } finally {
                    this.cellsBusy = 0;
                }
            } else {
                i = 0;
                long j4 = this.base;
                long v2 = j4;
                if (casBase(j4, longBinaryOperator == null ? v2 + j : longBinaryOperator.applyAsLong(v2, j))) {
                    return;
                }
            }
            wasUncontended5 = collide;
            wasUncontended4 = wasUncontended6;
        }
    }

    private static long apply(DoubleBinaryOperator fn, long v, double x) {
        double d = Double.longBitsToDouble(v);
        return Double.doubleToRawLongBits(fn == null ? d + x : fn.applyAsDouble(d, x));
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public final void doubleAccumulate(double x, DoubleBinaryOperator fn, boolean wasUncontended) {
        boolean wasUncontended2;
        boolean wasUncontended3;
        double d = x;
        DoubleBinaryOperator doubleBinaryOperator = fn;
        int probe = getProbe();
        int h = probe;
        if (probe == 0) {
            ThreadLocalRandom.current();
            h = getProbe();
            wasUncontended2 = true;
        } else {
            wasUncontended2 = wasUncontended;
        }
        int i = 0;
        boolean wasUncontended4 = wasUncontended2;
        boolean wasUncontended5 = false;
        while (true) {
            boolean collide = wasUncontended5;
            Cell[] cellArr = this.cells;
            Cell[] as = cellArr;
            if (cellArr != null) {
                int length = as.length;
                int n = length;
                if (length > 0) {
                    Cell cell = as[(n - 1) & h];
                    Cell a = cell;
                    if (cell == null) {
                        if (this.cellsBusy == 0) {
                            Cell r = new Cell(Double.doubleToRawLongBits(x));
                            if (this.cellsBusy == 0 && casCellsBusy()) {
                                try {
                                    Cell[] cellArr2 = this.cells;
                                    Cell[] rs = cellArr2;
                                    if (cellArr2 != null) {
                                        int length2 = rs.length;
                                        int m = length2;
                                        if (length2 > 0) {
                                            int i2 = (m - 1) & h;
                                            int j = i2;
                                            if (rs[i2] == null) {
                                                rs[j] = r;
                                                this.cellsBusy = i;
                                                boolean z = wasUncontended4;
                                                return;
                                            }
                                        }
                                    }
                                    this.cellsBusy = i;
                                    wasUncontended5 = collide;
                                } catch (Throwable th) {
                                    this.cellsBusy = i;
                                    throw th;
                                }
                            }
                        }
                        collide = false;
                    } else if (!wasUncontended4) {
                        wasUncontended4 = true;
                    } else {
                        long v = a.value;
                        wasUncontended3 = wasUncontended4;
                        if (!a.cas(v, apply(doubleBinaryOperator, v, d))) {
                            if (n >= NCPU || this.cells != as) {
                                collide = false;
                                h = advanceProbe(h);
                                wasUncontended5 = collide;
                                wasUncontended4 = wasUncontended3;
                                i = 0;
                            } else {
                                if (!collide) {
                                    collide = true;
                                } else if (this.cellsBusy == 0 && casCellsBusy()) {
                                    try {
                                        if (this.cells == as) {
                                            this.cells = (Cell[]) Arrays.copyOf((T[]) as, n << 1);
                                        }
                                        i = 0;
                                        this.cellsBusy = 0;
                                        wasUncontended5 = false;
                                        wasUncontended4 = wasUncontended3;
                                    } catch (Throwable th2) {
                                        this.cellsBusy = 0;
                                        throw th2;
                                    }
                                }
                                h = advanceProbe(h);
                                wasUncontended5 = collide;
                                wasUncontended4 = wasUncontended3;
                                i = 0;
                            }
                        } else {
                            return;
                        }
                    }
                    wasUncontended3 = wasUncontended4;
                    h = advanceProbe(h);
                    wasUncontended5 = collide;
                    wasUncontended4 = wasUncontended3;
                    i = 0;
                }
            }
            boolean wasUncontended6 = wasUncontended4;
            if (this.cellsBusy == 0 && this.cells == as && casCellsBusy()) {
                try {
                    if (this.cells == as) {
                        Cell[] rs2 = new Cell[2];
                        rs2[h & 1] = new Cell(Double.doubleToRawLongBits(x));
                        this.cells = rs2;
                        this.cellsBusy = 0;
                        return;
                    }
                } finally {
                    this.cellsBusy = 0;
                }
            } else {
                i = 0;
                long v2 = this.base;
                if (casBase(v2, apply(doubleBinaryOperator, v2, d))) {
                    return;
                }
            }
            wasUncontended5 = collide;
            wasUncontended4 = wasUncontended6;
        }
    }
}
