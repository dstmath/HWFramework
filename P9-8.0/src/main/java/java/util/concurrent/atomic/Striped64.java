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

        final boolean cas(long cmp, long val) {
            return U.compareAndSwapLong(this, VALUE, cmp, val);
        }

        final void reset() {
            U.putLongVolatile(this, VALUE, 0);
        }

        final void reset(long identity) {
            U.putLongVolatile(this, VALUE, identity);
        }

        static {
            try {
                VALUE = U.objectFieldOffset(Cell.class.getDeclaredField("value"));
            } catch (Throwable e) {
                throw new Error(e);
            }
        }
    }

    static {
        try {
            BASE = U.objectFieldOffset(Striped64.class.getDeclaredField("base"));
            CELLSBUSY = U.objectFieldOffset(Striped64.class.getDeclaredField("cellsBusy"));
            PROBE = U.objectFieldOffset(Thread.class.getDeclaredField("threadLocalRandomProbe"));
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    Striped64() {
    }

    final boolean casBase(long cmp, long val) {
        return U.compareAndSwapLong(this, BASE, cmp, val);
    }

    final boolean casCellsBusy() {
        return U.compareAndSwapInt(this, CELLSBUSY, 0, 1);
    }

    static final int getProbe() {
        return U.getInt(Thread.currentThread(), PROBE);
    }

    static final int advanceProbe(int probe) {
        probe ^= probe << 13;
        probe ^= probe >>> 17;
        probe ^= probe << 5;
        U.putInt(Thread.currentThread(), PROBE, probe);
        return probe;
    }

    final void longAccumulate(long x, LongBinaryOperator fn, boolean wasUncontended) {
        int h = getProbe();
        if (h == 0) {
            ThreadLocalRandom.current();
            h = getProbe();
            wasUncontended = true;
        }
        boolean collide = false;
        while (true) {
            Cell[] rs;
            long v;
            Cell[] as = this.cells;
            if (as != null) {
                int n = as.length;
                if (n > 0) {
                    Cell a = as[(n - 1) & h];
                    if (a == null) {
                        if (this.cellsBusy == 0) {
                            Cell r = new Cell(x);
                            if (this.cellsBusy == 0 && casCellsBusy()) {
                                try {
                                    rs = this.cells;
                                    if (rs != null) {
                                        int m = rs.length;
                                        if (m > 0) {
                                            int j = (m - 1) & h;
                                            if (rs[j] == null) {
                                                rs[j] = r;
                                                this.cellsBusy = 0;
                                                return;
                                            }
                                        }
                                    }
                                    this.cellsBusy = 0;
                                } catch (Throwable th) {
                                    this.cellsBusy = 0;
                                }
                            }
                        }
                        collide = false;
                    } else if (wasUncontended) {
                        v = a.value;
                        if (!a.cas(v, fn == null ? v + x : fn.applyAsLong(v, x))) {
                            if (n >= NCPU || this.cells != as) {
                                collide = false;
                            } else if (!collide) {
                                collide = true;
                            } else if (this.cellsBusy == 0 && casCellsBusy()) {
                                try {
                                    if (this.cells == as) {
                                        this.cells = (Cell[]) Arrays.copyOf((Object[]) as, n << 1);
                                    }
                                    this.cellsBusy = 0;
                                    collide = false;
                                } catch (Throwable th2) {
                                    this.cellsBusy = 0;
                                }
                            }
                        } else {
                            return;
                        }
                    } else {
                        wasUncontended = true;
                    }
                    h = advanceProbe(h);
                }
            }
            if (this.cellsBusy == 0 && this.cells == as && casCellsBusy()) {
                try {
                    if (this.cells == as) {
                        rs = new Cell[2];
                        rs[h & 1] = new Cell(x);
                        this.cells = rs;
                        this.cellsBusy = 0;
                        return;
                    }
                    this.cellsBusy = 0;
                } catch (Throwable th3) {
                    this.cellsBusy = 0;
                }
            } else {
                v = this.base;
                if (casBase(v, fn == null ? v + x : fn.applyAsLong(v, x))) {
                    return;
                }
            }
        }
    }

    private static long apply(DoubleBinaryOperator fn, long v, double x) {
        double d = Double.longBitsToDouble(v);
        return Double.doubleToRawLongBits(fn == null ? d + x : fn.applyAsDouble(d, x));
    }

    final void doubleAccumulate(double x, DoubleBinaryOperator fn, boolean wasUncontended) {
        int h = getProbe();
        if (h == 0) {
            ThreadLocalRandom.current();
            h = getProbe();
            wasUncontended = true;
        }
        boolean collide = false;
        while (true) {
            Cell[] rs;
            long v;
            Cell[] as = this.cells;
            if (as != null) {
                int n = as.length;
                if (n > 0) {
                    Cell a = as[(n - 1) & h];
                    if (a == null) {
                        if (this.cellsBusy == 0) {
                            Cell r = new Cell(Double.doubleToRawLongBits(x));
                            if (this.cellsBusy == 0 && casCellsBusy()) {
                                try {
                                    rs = this.cells;
                                    if (rs != null) {
                                        int m = rs.length;
                                        if (m > 0) {
                                            int j = (m - 1) & h;
                                            if (rs[j] == null) {
                                                rs[j] = r;
                                                this.cellsBusy = 0;
                                                return;
                                            }
                                        }
                                    }
                                    this.cellsBusy = 0;
                                } catch (Throwable th) {
                                    this.cellsBusy = 0;
                                }
                            }
                        }
                        collide = false;
                    } else if (wasUncontended) {
                        v = a.value;
                        if (!a.cas(v, apply(fn, v, x))) {
                            if (n >= NCPU || this.cells != as) {
                                collide = false;
                            } else if (!collide) {
                                collide = true;
                            } else if (this.cellsBusy == 0 && casCellsBusy()) {
                                try {
                                    if (this.cells == as) {
                                        this.cells = (Cell[]) Arrays.copyOf((Object[]) as, n << 1);
                                    }
                                    this.cellsBusy = 0;
                                    collide = false;
                                } catch (Throwable th2) {
                                    this.cellsBusy = 0;
                                }
                            }
                        } else {
                            return;
                        }
                    } else {
                        wasUncontended = true;
                    }
                    h = advanceProbe(h);
                }
            }
            if (this.cellsBusy == 0 && this.cells == as && casCellsBusy()) {
                try {
                    if (this.cells == as) {
                        rs = new Cell[2];
                        rs[h & 1] = new Cell(Double.doubleToRawLongBits(x));
                        this.cells = rs;
                        this.cellsBusy = 0;
                        return;
                    }
                    this.cellsBusy = 0;
                } catch (Throwable th3) {
                    this.cellsBusy = 0;
                }
            } else {
                v = this.base;
                if (casBase(v, apply(fn, v, x))) {
                    return;
                }
            }
        }
    }
}
