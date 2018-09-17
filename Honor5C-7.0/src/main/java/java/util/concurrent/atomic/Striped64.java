package java.util.concurrent.atomic;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleBinaryOperator;
import java.util.function.LongBinaryOperator;
import sun.misc.Unsafe;

abstract class Striped64 extends Number {
    private static final long BASE = 0;
    private static final long CELLSBUSY = 0;
    static final int NCPU = 0;
    private static final long PROBE = 0;
    private static final Unsafe U = null;
    volatile transient long base;
    volatile transient Cell[] cells;
    volatile transient int cellsBusy;

    static final class Cell {
        private static final Unsafe U = null;
        private static final long VALUE = 0;
        volatile long value;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.atomic.Striped64.Cell.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.atomic.Striped64.Cell.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.atomic.Striped64.Cell.<clinit>():void");
        }

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
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.atomic.Striped64.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.atomic.Striped64.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.atomic.Striped64.<clinit>():void");
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
                                        this.cells = (Cell[]) Arrays.copyOf(as, n << 1);
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
                                        this.cells = (Cell[]) Arrays.copyOf(as, n << 1);
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
