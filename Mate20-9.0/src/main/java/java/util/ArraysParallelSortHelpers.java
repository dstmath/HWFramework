package java.util;

import java.util.concurrent.CountedCompleter;

class ArraysParallelSortHelpers {

    static final class EmptyCompleter extends CountedCompleter<Void> {
        static final long serialVersionUID = 2446542900576103244L;

        EmptyCompleter(CountedCompleter<?> p) {
            super(p);
        }

        public final void compute() {
        }
    }

    static final class FJByte {

        static final class Merger extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final byte[] a;
            final int gran;
            final int lbase;
            final int lsize;
            final int rbase;
            final int rsize;
            final byte[] w;
            final int wbase;

            Merger(CountedCompleter<?> par, byte[] a2, byte[] w2, int lbase2, int lsize2, int rbase2, int rsize2, int wbase2, int gran2) {
                super(par);
                this.a = a2;
                this.w = w2;
                this.lbase = lbase2;
                this.lsize = lsize2;
                this.rbase = rbase2;
                this.rsize = rsize2;
                this.wbase = wbase2;
                this.gran = gran2;
            }

            public final void compute() {
                int rh;
                int lh;
                byte t;
                byte[] a2 = this.a;
                byte[] w2 = this.w;
                int lb = this.lbase;
                int ln = this.lsize;
                int rb = this.rbase;
                int rn = this.rsize;
                int k = this.wbase;
                int g = this.gran;
                if (a2 == null || w2 == null || lb < 0 || rb < 0 || k < 0) {
                    byte[] bArr = a2;
                    throw new IllegalStateException();
                }
                int ln2 = ln;
                int rn2 = rn;
                while (true) {
                    int lo = 0;
                    if (ln2 >= rn2) {
                        if (ln2 <= g) {
                            break;
                        }
                        int rh2 = rn2;
                        int i = ln2 >>> 1;
                        int lh2 = i;
                        byte split = a2[i + lb];
                        while (lo < rh2) {
                            int rm = (lo + rh2) >>> 1;
                            if (split <= a2[rm + rb]) {
                                rh2 = rm;
                            } else {
                                lo = rm + 1;
                            }
                        }
                        rh = rh2;
                        lh = lh2;
                        byte[] bArr2 = a2;
                        byte[] bArr3 = w2;
                        int i2 = rn2;
                        int i3 = ln2;
                        int i4 = g;
                        Merger m = new Merger(this, bArr2, bArr3, lb + lh, ln2 - lh, rb + rh, rn2 - rh, k + lh + rh, g);
                        rn2 = rh;
                        ln2 = lh;
                        addToPendingCount(1);
                        m.fork();
                        a2 = a2;
                    } else if (rn2 <= g) {
                        break;
                    } else {
                        int lh3 = ln2;
                        int i5 = rn2 >>> 1;
                        int rh3 = i5;
                        byte split2 = a2[i5 + rb];
                        while (lo < lh3) {
                            int lm = (lo + lh3) >>> 1;
                            if (split2 <= a2[lm + lb]) {
                                lh3 = lm;
                            } else {
                                lo = lm + 1;
                            }
                        }
                        lh = lh3;
                        rh = rh3;
                        byte[] bArr22 = a2;
                        byte[] bArr32 = w2;
                        int i22 = rn2;
                        int i32 = ln2;
                        int i42 = g;
                        Merger m2 = new Merger(this, bArr22, bArr32, lb + lh, ln2 - lh, rb + rh, rn2 - rh, k + lh + rh, g);
                        rn2 = rh;
                        ln2 = lh;
                        addToPendingCount(1);
                        m2.fork();
                        a2 = a2;
                    }
                }
                int lf = lb + ln2;
                int rf = rb + rn2;
                while (lb < lf && rb < rf) {
                    byte b = a2[lb];
                    byte al = b;
                    byte b2 = a2[rb];
                    byte ar = b2;
                    if (b <= b2) {
                        lb++;
                        t = al;
                    } else {
                        rb++;
                        t = ar;
                    }
                    w2[k] = t;
                    k++;
                }
                if (rb < rf) {
                    System.arraycopy(a2, rb, w2, k, rf - rb);
                } else if (lb < lf) {
                    System.arraycopy(a2, lb, w2, k, lf - lb);
                }
                tryComplete();
            }
        }

        static final class Sorter extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final byte[] a;
            final int base;
            final int gran;
            final int size;
            final byte[] w;
            final int wbase;

            Sorter(CountedCompleter<?> par, byte[] a2, byte[] w2, int base2, int size2, int wbase2, int gran2) {
                super(par);
                this.a = a2;
                this.w = w2;
                this.base = base2;
                this.size = size2;
                this.wbase = wbase2;
                this.gran = gran2;
            }

            /* JADX WARNING: type inference failed for: r3v6, types: [java.util.ArraysParallelSortHelpers$EmptyCompleter] */
            /* JADX WARNING: Multi-variable type inference failed */
            public final void compute() {
                byte[] a2 = this.a;
                byte[] w2 = this.w;
                int b = this.base;
                int n = this.size;
                int wb = this.wbase;
                int g = this.gran;
                Sorter sorter = this;
                int n2 = n;
                while (true) {
                    int g2 = g;
                    if (n2 > g2) {
                        int h = n2 >>> 1;
                        int q = h >>> 1;
                        int u = h + q;
                        Merger merger = r2;
                        byte[] w3 = w2;
                        int g3 = g2;
                        Sorter sorter2 = sorter;
                        int i = g3;
                        Merger merger2 = new Merger(sorter, w2, a2, wb, h, wb + h, n2 - h, b, i);
                        Relay relay = new Relay(merger);
                        Relay fc = relay;
                        Merger merger3 = new Merger(fc, a2, w3, b + h, q, b + u, n2 - u, wb + h, i);
                        Relay rc = new Relay(merger3);
                        Sorter sorter3 = new Sorter(rc, a2, w3, b + u, n2 - u, wb + u, i);
                        sorter3.fork();
                        byte[] bArr = a2;
                        byte[] bArr2 = w3;
                        int i2 = q;
                        int wb2 = wb;
                        int b2 = b;
                        Sorter sorter4 = new Sorter(rc, bArr, bArr2, b + h, i2, wb + h, g3);
                        sorter4.fork();
                        Relay relay2 = fc;
                        Merger merger4 = r6;
                        byte[] w4 = w3;
                        byte[] a3 = a2;
                        Merger merger5 = new Merger(fc, bArr, bArr2, b2, i2, b2 + q, h - q, wb2, g3);
                        Relay bc = new Relay(merger4);
                        Sorter sorter5 = new Sorter(bc, a3, w4, b2 + q, h - q, wb2 + q, g3);
                        sorter5.fork();
                        sorter = new EmptyCompleter(bc);
                        n2 = q;
                        b = b2;
                        wb = wb2;
                        g = g3;
                        w2 = w4;
                        a2 = a3;
                    } else {
                        Sorter sorter6 = sorter;
                        int i3 = wb;
                        int b3 = b;
                        byte[] bArr3 = w2;
                        DualPivotQuicksort.sort(a2, b3, (b3 + n2) - 1);
                        sorter.tryComplete();
                        return;
                    }
                }
            }
        }

        FJByte() {
        }
    }

    static final class FJChar {

        static final class Merger extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final char[] a;
            final int gran;
            final int lbase;
            final int lsize;
            final int rbase;
            final int rsize;
            final char[] w;
            final int wbase;

            Merger(CountedCompleter<?> par, char[] a2, char[] w2, int lbase2, int lsize2, int rbase2, int rsize2, int wbase2, int gran2) {
                super(par);
                this.a = a2;
                this.w = w2;
                this.lbase = lbase2;
                this.lsize = lsize2;
                this.rbase = rbase2;
                this.rsize = rsize2;
                this.wbase = wbase2;
                this.gran = gran2;
            }

            public final void compute() {
                int rh;
                int lh;
                char t;
                char[] a2 = this.a;
                char[] w2 = this.w;
                int lb = this.lbase;
                int ln = this.lsize;
                int rb = this.rbase;
                int rn = this.rsize;
                int k = this.wbase;
                int g = this.gran;
                if (a2 == null || w2 == null || lb < 0 || rb < 0 || k < 0) {
                    char[] cArr = a2;
                    throw new IllegalStateException();
                }
                int ln2 = ln;
                int rn2 = rn;
                while (true) {
                    int lo = 0;
                    if (ln2 >= rn2) {
                        if (ln2 <= g) {
                            break;
                        }
                        int rh2 = rn2;
                        int i = ln2 >>> 1;
                        int lh2 = i;
                        char split = a2[i + lb];
                        while (lo < rh2) {
                            int rm = (lo + rh2) >>> 1;
                            if (split <= a2[rm + rb]) {
                                rh2 = rm;
                            } else {
                                lo = rm + 1;
                            }
                        }
                        rh = rh2;
                        lh = lh2;
                        char[] cArr2 = a2;
                        char[] cArr3 = w2;
                        int i2 = rn2;
                        int i3 = ln2;
                        int i4 = g;
                        Merger m = new Merger(this, cArr2, cArr3, lb + lh, ln2 - lh, rb + rh, rn2 - rh, k + lh + rh, g);
                        rn2 = rh;
                        ln2 = lh;
                        addToPendingCount(1);
                        m.fork();
                        a2 = a2;
                    } else if (rn2 <= g) {
                        break;
                    } else {
                        int lh3 = ln2;
                        int i5 = rn2 >>> 1;
                        int rh3 = i5;
                        char split2 = a2[i5 + rb];
                        while (lo < lh3) {
                            int lm = (lo + lh3) >>> 1;
                            if (split2 <= a2[lm + lb]) {
                                lh3 = lm;
                            } else {
                                lo = lm + 1;
                            }
                        }
                        lh = lh3;
                        rh = rh3;
                        char[] cArr22 = a2;
                        char[] cArr32 = w2;
                        int i22 = rn2;
                        int i32 = ln2;
                        int i42 = g;
                        Merger m2 = new Merger(this, cArr22, cArr32, lb + lh, ln2 - lh, rb + rh, rn2 - rh, k + lh + rh, g);
                        rn2 = rh;
                        ln2 = lh;
                        addToPendingCount(1);
                        m2.fork();
                        a2 = a2;
                    }
                }
                int lf = lb + ln2;
                int rf = rb + rn2;
                while (lb < lf && rb < rf) {
                    char c = a2[lb];
                    char al = c;
                    char c2 = a2[rb];
                    char ar = c2;
                    if (c <= c2) {
                        lb++;
                        t = al;
                    } else {
                        rb++;
                        t = ar;
                    }
                    w2[k] = t;
                    k++;
                }
                if (rb < rf) {
                    System.arraycopy((Object) a2, rb, (Object) w2, k, rf - rb);
                } else if (lb < lf) {
                    System.arraycopy((Object) a2, lb, (Object) w2, k, lf - lb);
                }
                tryComplete();
            }
        }

        static final class Sorter extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final char[] a;
            final int base;
            final int gran;
            final int size;
            final char[] w;
            final int wbase;

            Sorter(CountedCompleter<?> par, char[] a2, char[] w2, int base2, int size2, int wbase2, int gran2) {
                super(par);
                this.a = a2;
                this.w = w2;
                this.base = base2;
                this.size = size2;
                this.wbase = wbase2;
                this.gran = gran2;
            }

            /* JADX WARNING: type inference failed for: r2v7, types: [java.util.ArraysParallelSortHelpers$EmptyCompleter] */
            /* JADX WARNING: Multi-variable type inference failed */
            public final void compute() {
                char[] a2 = this.a;
                char[] w2 = this.w;
                int b = this.base;
                int n = this.size;
                int wb = this.wbase;
                int g = this.gran;
                Sorter sorter = this;
                int n2 = n;
                while (true) {
                    int g2 = g;
                    if (n2 > g2) {
                        int h = n2 >>> 1;
                        int q = h >>> 1;
                        int u = h + q;
                        Merger merger = r2;
                        char[] w3 = w2;
                        int g3 = g2;
                        Sorter sorter2 = sorter;
                        int i = g3;
                        Merger merger2 = new Merger(sorter, w2, a2, wb, h, wb + h, n2 - h, b, i);
                        Relay relay = new Relay(merger);
                        Relay fc = relay;
                        Merger merger3 = new Merger(fc, a2, w3, b + h, q, b + u, n2 - u, wb + h, i);
                        Relay rc = new Relay(merger3);
                        Sorter sorter3 = new Sorter(rc, a2, w3, b + u, n2 - u, wb + u, i);
                        sorter3.fork();
                        char[] cArr = a2;
                        char[] cArr2 = w3;
                        int i2 = q;
                        int wb2 = wb;
                        int b2 = b;
                        Sorter sorter4 = new Sorter(rc, cArr, cArr2, b + h, i2, wb + h, g3);
                        sorter4.fork();
                        char[] a3 = a2;
                        Merger merger4 = new Merger(fc, cArr, cArr2, b2, i2, b2 + q, h - q, wb2, g3);
                        Relay bc = new Relay(merger4);
                        Sorter sorter5 = new Sorter(bc, a3, cArr2, b2 + q, h - q, wb2 + q, g3);
                        sorter5.fork();
                        sorter = new EmptyCompleter(bc);
                        n2 = q;
                        g = g3;
                        wb = wb2;
                        w2 = w3;
                        b = b2;
                        a2 = a3;
                    } else {
                        Sorter sorter6 = sorter;
                        int b3 = b;
                        DualPivotQuicksort.sort(a2, b3, (b3 + n2) - 1, w2, wb, n2);
                        sorter.tryComplete();
                        return;
                    }
                }
            }
        }

        FJChar() {
        }
    }

    static final class FJDouble {

        static final class Merger extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final double[] a;
            final int gran;
            final int lbase;
            final int lsize;
            final int rbase;
            final int rsize;
            final double[] w;
            final int wbase;

            Merger(CountedCompleter<?> par, double[] a2, double[] w2, int lbase2, int lsize2, int rbase2, int rsize2, int wbase2, int gran2) {
                super(par);
                this.a = a2;
                this.w = w2;
                this.lbase = lbase2;
                this.lsize = lsize2;
                this.rbase = rbase2;
                this.rsize = rsize2;
                this.wbase = wbase2;
                this.gran = gran2;
            }

            public final void compute() {
                int rh;
                int lh;
                double t;
                double[] a2 = this.a;
                double[] w2 = this.w;
                int lb = this.lbase;
                int ln = this.lsize;
                int rb = this.rbase;
                int rn = this.rsize;
                int k = this.wbase;
                int g = this.gran;
                if (a2 == null || w2 == null || lb < 0 || rb < 0 || k < 0) {
                    double[] dArr = a2;
                    throw new IllegalStateException();
                }
                int ln2 = ln;
                int rn2 = rn;
                while (true) {
                    int lo = 0;
                    if (ln2 >= rn2) {
                        if (ln2 <= g) {
                            break;
                        }
                        int rh2 = rn2;
                        int i = ln2 >>> 1;
                        int lh2 = i;
                        double split = a2[i + lb];
                        while (lo < rh2) {
                            int rm = (lo + rh2) >>> 1;
                            if (split <= a2[rm + rb]) {
                                rh2 = rm;
                            } else {
                                lo = rm + 1;
                            }
                        }
                        rh = rh2;
                        lh = lh2;
                        double[] dArr2 = a2;
                        double[] dArr3 = w2;
                        int i2 = rn2;
                        int i3 = ln2;
                        int i4 = g;
                        Merger m = new Merger(this, dArr2, dArr3, lb + lh, ln2 - lh, rb + rh, rn2 - rh, k + lh + rh, g);
                        rn2 = rh;
                        ln2 = lh;
                        addToPendingCount(1);
                        m.fork();
                        a2 = a2;
                    } else if (rn2 <= g) {
                        break;
                    } else {
                        int lh3 = ln2;
                        int i5 = rn2 >>> 1;
                        int rh3 = i5;
                        double split2 = a2[i5 + rb];
                        while (lo < lh3) {
                            int lm = (lo + lh3) >>> 1;
                            if (split2 <= a2[lm + lb]) {
                                lh3 = lm;
                            } else {
                                lo = lm + 1;
                            }
                        }
                        lh = lh3;
                        rh = rh3;
                        double[] dArr22 = a2;
                        double[] dArr32 = w2;
                        int i22 = rn2;
                        int i32 = ln2;
                        int i42 = g;
                        Merger m2 = new Merger(this, dArr22, dArr32, lb + lh, ln2 - lh, rb + rh, rn2 - rh, k + lh + rh, g);
                        rn2 = rh;
                        ln2 = lh;
                        addToPendingCount(1);
                        m2.fork();
                        a2 = a2;
                    }
                }
                int lf = lb + ln2;
                int rf = rb + rn2;
                while (lb < lf && rb < rf) {
                    double d = a2[lb];
                    double al = d;
                    double d2 = a2[rb];
                    double ar = d2;
                    if (d <= d2) {
                        lb++;
                        t = al;
                    } else {
                        rb++;
                        t = ar;
                    }
                    w2[k] = t;
                    k++;
                }
                if (rb < rf) {
                    System.arraycopy((Object) a2, rb, (Object) w2, k, rf - rb);
                } else if (lb < lf) {
                    System.arraycopy((Object) a2, lb, (Object) w2, k, lf - lb);
                }
                tryComplete();
            }
        }

        static final class Sorter extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final double[] a;
            final int base;
            final int gran;
            final int size;
            final double[] w;
            final int wbase;

            Sorter(CountedCompleter<?> par, double[] a2, double[] w2, int base2, int size2, int wbase2, int gran2) {
                super(par);
                this.a = a2;
                this.w = w2;
                this.base = base2;
                this.size = size2;
                this.wbase = wbase2;
                this.gran = gran2;
            }

            /* JADX WARNING: type inference failed for: r2v7, types: [java.util.ArraysParallelSortHelpers$EmptyCompleter] */
            /* JADX WARNING: Multi-variable type inference failed */
            public final void compute() {
                double[] a2 = this.a;
                double[] w2 = this.w;
                int b = this.base;
                int n = this.size;
                int wb = this.wbase;
                int g = this.gran;
                Sorter sorter = this;
                int n2 = n;
                while (true) {
                    int g2 = g;
                    if (n2 > g2) {
                        int h = n2 >>> 1;
                        int q = h >>> 1;
                        int u = h + q;
                        Merger merger = r2;
                        double[] w3 = w2;
                        int g3 = g2;
                        Sorter sorter2 = sorter;
                        int i = g3;
                        Merger merger2 = new Merger(sorter, w2, a2, wb, h, wb + h, n2 - h, b, i);
                        Relay relay = new Relay(merger);
                        Relay fc = relay;
                        Merger merger3 = new Merger(fc, a2, w3, b + h, q, b + u, n2 - u, wb + h, i);
                        Relay rc = new Relay(merger3);
                        Sorter sorter3 = new Sorter(rc, a2, w3, b + u, n2 - u, wb + u, i);
                        sorter3.fork();
                        double[] dArr = a2;
                        double[] dArr2 = w3;
                        int i2 = q;
                        int wb2 = wb;
                        int b2 = b;
                        Sorter sorter4 = new Sorter(rc, dArr, dArr2, b + h, i2, wb + h, g3);
                        sorter4.fork();
                        double[] a3 = a2;
                        Merger merger4 = new Merger(fc, dArr, dArr2, b2, i2, b2 + q, h - q, wb2, g3);
                        Relay bc = new Relay(merger4);
                        Sorter sorter5 = new Sorter(bc, a3, dArr2, b2 + q, h - q, wb2 + q, g3);
                        sorter5.fork();
                        sorter = new EmptyCompleter(bc);
                        n2 = q;
                        g = g3;
                        wb = wb2;
                        w2 = w3;
                        b = b2;
                        a2 = a3;
                    } else {
                        Sorter sorter6 = sorter;
                        int b3 = b;
                        DualPivotQuicksort.sort(a2, b3, (b3 + n2) - 1, w2, wb, n2);
                        sorter.tryComplete();
                        return;
                    }
                }
            }
        }

        FJDouble() {
        }
    }

    static final class FJFloat {

        static final class Merger extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final float[] a;
            final int gran;
            final int lbase;
            final int lsize;
            final int rbase;
            final int rsize;
            final float[] w;
            final int wbase;

            Merger(CountedCompleter<?> par, float[] a2, float[] w2, int lbase2, int lsize2, int rbase2, int rsize2, int wbase2, int gran2) {
                super(par);
                this.a = a2;
                this.w = w2;
                this.lbase = lbase2;
                this.lsize = lsize2;
                this.rbase = rbase2;
                this.rsize = rsize2;
                this.wbase = wbase2;
                this.gran = gran2;
            }

            public final void compute() {
                int rh;
                int lh;
                float t;
                float[] a2 = this.a;
                float[] w2 = this.w;
                int lb = this.lbase;
                int ln = this.lsize;
                int rb = this.rbase;
                int rn = this.rsize;
                int k = this.wbase;
                int g = this.gran;
                if (a2 == null || w2 == null || lb < 0 || rb < 0 || k < 0) {
                    float[] fArr = a2;
                    throw new IllegalStateException();
                }
                int ln2 = ln;
                int rn2 = rn;
                while (true) {
                    int lo = 0;
                    if (ln2 >= rn2) {
                        if (ln2 <= g) {
                            break;
                        }
                        int rh2 = rn2;
                        int i = ln2 >>> 1;
                        int lh2 = i;
                        float split = a2[i + lb];
                        while (lo < rh2) {
                            int rm = (lo + rh2) >>> 1;
                            if (split <= a2[rm + rb]) {
                                rh2 = rm;
                            } else {
                                lo = rm + 1;
                            }
                        }
                        rh = rh2;
                        lh = lh2;
                        float[] fArr2 = a2;
                        float[] fArr3 = w2;
                        int i2 = rn2;
                        int i3 = ln2;
                        int i4 = g;
                        Merger m = new Merger(this, fArr2, fArr3, lb + lh, ln2 - lh, rb + rh, rn2 - rh, k + lh + rh, g);
                        rn2 = rh;
                        ln2 = lh;
                        addToPendingCount(1);
                        m.fork();
                        a2 = a2;
                    } else if (rn2 <= g) {
                        break;
                    } else {
                        int lh3 = ln2;
                        int i5 = rn2 >>> 1;
                        int rh3 = i5;
                        float split2 = a2[i5 + rb];
                        while (lo < lh3) {
                            int lm = (lo + lh3) >>> 1;
                            if (split2 <= a2[lm + lb]) {
                                lh3 = lm;
                            } else {
                                lo = lm + 1;
                            }
                        }
                        lh = lh3;
                        rh = rh3;
                        float[] fArr22 = a2;
                        float[] fArr32 = w2;
                        int i22 = rn2;
                        int i32 = ln2;
                        int i42 = g;
                        Merger m2 = new Merger(this, fArr22, fArr32, lb + lh, ln2 - lh, rb + rh, rn2 - rh, k + lh + rh, g);
                        rn2 = rh;
                        ln2 = lh;
                        addToPendingCount(1);
                        m2.fork();
                        a2 = a2;
                    }
                }
                int lf = lb + ln2;
                int rf = rb + rn2;
                while (lb < lf && rb < rf) {
                    float f = a2[lb];
                    float al = f;
                    float f2 = a2[rb];
                    float ar = f2;
                    if (f <= f2) {
                        lb++;
                        t = al;
                    } else {
                        rb++;
                        t = ar;
                    }
                    w2[k] = t;
                    k++;
                }
                if (rb < rf) {
                    System.arraycopy((Object) a2, rb, (Object) w2, k, rf - rb);
                } else if (lb < lf) {
                    System.arraycopy((Object) a2, lb, (Object) w2, k, lf - lb);
                }
                tryComplete();
            }
        }

        static final class Sorter extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final float[] a;
            final int base;
            final int gran;
            final int size;
            final float[] w;
            final int wbase;

            Sorter(CountedCompleter<?> par, float[] a2, float[] w2, int base2, int size2, int wbase2, int gran2) {
                super(par);
                this.a = a2;
                this.w = w2;
                this.base = base2;
                this.size = size2;
                this.wbase = wbase2;
                this.gran = gran2;
            }

            /* JADX WARNING: type inference failed for: r2v7, types: [java.util.ArraysParallelSortHelpers$EmptyCompleter] */
            /* JADX WARNING: Multi-variable type inference failed */
            public final void compute() {
                float[] a2 = this.a;
                float[] w2 = this.w;
                int b = this.base;
                int n = this.size;
                int wb = this.wbase;
                int g = this.gran;
                Sorter sorter = this;
                int n2 = n;
                while (true) {
                    int g2 = g;
                    if (n2 > g2) {
                        int h = n2 >>> 1;
                        int q = h >>> 1;
                        int u = h + q;
                        Merger merger = r2;
                        float[] w3 = w2;
                        int g3 = g2;
                        Sorter sorter2 = sorter;
                        int i = g3;
                        Merger merger2 = new Merger(sorter, w2, a2, wb, h, wb + h, n2 - h, b, i);
                        Relay relay = new Relay(merger);
                        Relay fc = relay;
                        Merger merger3 = new Merger(fc, a2, w3, b + h, q, b + u, n2 - u, wb + h, i);
                        Relay rc = new Relay(merger3);
                        Sorter sorter3 = new Sorter(rc, a2, w3, b + u, n2 - u, wb + u, i);
                        sorter3.fork();
                        float[] fArr = a2;
                        float[] fArr2 = w3;
                        int i2 = q;
                        int wb2 = wb;
                        int b2 = b;
                        Sorter sorter4 = new Sorter(rc, fArr, fArr2, b + h, i2, wb + h, g3);
                        sorter4.fork();
                        float[] a3 = a2;
                        Merger merger4 = new Merger(fc, fArr, fArr2, b2, i2, b2 + q, h - q, wb2, g3);
                        Relay bc = new Relay(merger4);
                        Sorter sorter5 = new Sorter(bc, a3, fArr2, b2 + q, h - q, wb2 + q, g3);
                        sorter5.fork();
                        sorter = new EmptyCompleter(bc);
                        n2 = q;
                        g = g3;
                        wb = wb2;
                        w2 = w3;
                        b = b2;
                        a2 = a3;
                    } else {
                        Sorter sorter6 = sorter;
                        int b3 = b;
                        DualPivotQuicksort.sort(a2, b3, (b3 + n2) - 1, w2, wb, n2);
                        sorter.tryComplete();
                        return;
                    }
                }
            }
        }

        FJFloat() {
        }
    }

    static final class FJInt {

        static final class Merger extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final int[] a;
            final int gran;
            final int lbase;
            final int lsize;
            final int rbase;
            final int rsize;
            final int[] w;
            final int wbase;

            Merger(CountedCompleter<?> par, int[] a2, int[] w2, int lbase2, int lsize2, int rbase2, int rsize2, int wbase2, int gran2) {
                super(par);
                this.a = a2;
                this.w = w2;
                this.lbase = lbase2;
                this.lsize = lsize2;
                this.rbase = rbase2;
                this.rsize = rsize2;
                this.wbase = wbase2;
                this.gran = gran2;
            }

            public final void compute() {
                int rh;
                int lh;
                int t;
                int[] a2 = this.a;
                int[] w2 = this.w;
                int lb = this.lbase;
                int ln = this.lsize;
                int rb = this.rbase;
                int rn = this.rsize;
                int k = this.wbase;
                int g = this.gran;
                if (a2 == null || w2 == null || lb < 0 || rb < 0 || k < 0) {
                    int[] iArr = a2;
                    throw new IllegalStateException();
                }
                int ln2 = ln;
                int rn2 = rn;
                while (true) {
                    int lo = 0;
                    if (ln2 >= rn2) {
                        if (ln2 <= g) {
                            break;
                        }
                        int rh2 = rn2;
                        int i = ln2 >>> 1;
                        int lh2 = i;
                        int split = a2[i + lb];
                        while (lo < rh2) {
                            int rm = (lo + rh2) >>> 1;
                            if (split <= a2[rm + rb]) {
                                rh2 = rm;
                            } else {
                                lo = rm + 1;
                            }
                        }
                        rh = rh2;
                        lh = lh2;
                        int[] iArr2 = a2;
                        int[] iArr3 = w2;
                        int i2 = rn2;
                        int i3 = ln2;
                        int i4 = g;
                        Merger m = new Merger(this, iArr2, iArr3, lb + lh, ln2 - lh, rb + rh, rn2 - rh, k + lh + rh, g);
                        rn2 = rh;
                        ln2 = lh;
                        addToPendingCount(1);
                        m.fork();
                        a2 = a2;
                    } else if (rn2 <= g) {
                        break;
                    } else {
                        int lh3 = ln2;
                        int i5 = rn2 >>> 1;
                        int rh3 = i5;
                        int split2 = a2[i5 + rb];
                        while (lo < lh3) {
                            int lm = (lo + lh3) >>> 1;
                            if (split2 <= a2[lm + lb]) {
                                lh3 = lm;
                            } else {
                                lo = lm + 1;
                            }
                        }
                        lh = lh3;
                        rh = rh3;
                        int[] iArr22 = a2;
                        int[] iArr32 = w2;
                        int i22 = rn2;
                        int i32 = ln2;
                        int i42 = g;
                        Merger m2 = new Merger(this, iArr22, iArr32, lb + lh, ln2 - lh, rb + rh, rn2 - rh, k + lh + rh, g);
                        rn2 = rh;
                        ln2 = lh;
                        addToPendingCount(1);
                        m2.fork();
                        a2 = a2;
                    }
                }
                int lf = lb + ln2;
                int rf = rb + rn2;
                while (lb < lf && rb < rf) {
                    int i6 = a2[lb];
                    int al = i6;
                    int i7 = a2[rb];
                    int ar = i7;
                    if (i6 <= i7) {
                        lb++;
                        t = al;
                    } else {
                        rb++;
                        t = ar;
                    }
                    w2[k] = t;
                    k++;
                }
                if (rb < rf) {
                    System.arraycopy((Object) a2, rb, (Object) w2, k, rf - rb);
                } else if (lb < lf) {
                    System.arraycopy((Object) a2, lb, (Object) w2, k, lf - lb);
                }
                tryComplete();
            }
        }

        static final class Sorter extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final int[] a;
            final int base;
            final int gran;
            final int size;
            final int[] w;
            final int wbase;

            Sorter(CountedCompleter<?> par, int[] a2, int[] w2, int base2, int size2, int wbase2, int gran2) {
                super(par);
                this.a = a2;
                this.w = w2;
                this.base = base2;
                this.size = size2;
                this.wbase = wbase2;
                this.gran = gran2;
            }

            /* JADX WARNING: type inference failed for: r2v7, types: [java.util.ArraysParallelSortHelpers$EmptyCompleter] */
            /* JADX WARNING: Multi-variable type inference failed */
            public final void compute() {
                int[] a2 = this.a;
                int[] w2 = this.w;
                int b = this.base;
                int n = this.size;
                int wb = this.wbase;
                int g = this.gran;
                Sorter sorter = this;
                int n2 = n;
                while (true) {
                    int g2 = g;
                    if (n2 > g2) {
                        int h = n2 >>> 1;
                        int q = h >>> 1;
                        int u = h + q;
                        Merger merger = r2;
                        int[] w3 = w2;
                        int g3 = g2;
                        Sorter sorter2 = sorter;
                        int i = g3;
                        Merger merger2 = new Merger(sorter, w2, a2, wb, h, wb + h, n2 - h, b, i);
                        Relay relay = new Relay(merger);
                        Relay fc = relay;
                        Merger merger3 = new Merger(fc, a2, w3, b + h, q, b + u, n2 - u, wb + h, i);
                        Relay rc = new Relay(merger3);
                        Sorter sorter3 = new Sorter(rc, a2, w3, b + u, n2 - u, wb + u, i);
                        sorter3.fork();
                        int[] iArr = a2;
                        int[] iArr2 = w3;
                        int i2 = q;
                        int wb2 = wb;
                        int b2 = b;
                        Sorter sorter4 = new Sorter(rc, iArr, iArr2, b + h, i2, wb + h, g3);
                        sorter4.fork();
                        int[] a3 = a2;
                        Merger merger4 = new Merger(fc, iArr, iArr2, b2, i2, b2 + q, h - q, wb2, g3);
                        Relay bc = new Relay(merger4);
                        Sorter sorter5 = new Sorter(bc, a3, iArr2, b2 + q, h - q, wb2 + q, g3);
                        sorter5.fork();
                        sorter = new EmptyCompleter(bc);
                        n2 = q;
                        g = g3;
                        wb = wb2;
                        w2 = w3;
                        b = b2;
                        a2 = a3;
                    } else {
                        Sorter sorter6 = sorter;
                        int b3 = b;
                        DualPivotQuicksort.sort(a2, b3, (b3 + n2) - 1, w2, wb, n2);
                        sorter.tryComplete();
                        return;
                    }
                }
            }
        }

        FJInt() {
        }
    }

    static final class FJLong {

        static final class Merger extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final long[] a;
            final int gran;
            final int lbase;
            final int lsize;
            final int rbase;
            final int rsize;
            final long[] w;
            final int wbase;

            Merger(CountedCompleter<?> par, long[] a2, long[] w2, int lbase2, int lsize2, int rbase2, int rsize2, int wbase2, int gran2) {
                super(par);
                this.a = a2;
                this.w = w2;
                this.lbase = lbase2;
                this.lsize = lsize2;
                this.rbase = rbase2;
                this.rsize = rsize2;
                this.wbase = wbase2;
                this.gran = gran2;
            }

            public final void compute() {
                int rh;
                int lh;
                long t;
                long[] a2 = this.a;
                long[] w2 = this.w;
                int lb = this.lbase;
                int ln = this.lsize;
                int rb = this.rbase;
                int rn = this.rsize;
                int k = this.wbase;
                int g = this.gran;
                if (a2 == null || w2 == null || lb < 0 || rb < 0 || k < 0) {
                    long[] jArr = a2;
                    throw new IllegalStateException();
                }
                int ln2 = ln;
                int rn2 = rn;
                while (true) {
                    int lo = 0;
                    if (ln2 >= rn2) {
                        if (ln2 <= g) {
                            break;
                        }
                        int rh2 = rn2;
                        int i = ln2 >>> 1;
                        int lh2 = i;
                        long split = a2[i + lb];
                        while (lo < rh2) {
                            int rm = (lo + rh2) >>> 1;
                            if (split <= a2[rm + rb]) {
                                rh2 = rm;
                            } else {
                                lo = rm + 1;
                            }
                        }
                        rh = rh2;
                        lh = lh2;
                        long[] jArr2 = a2;
                        long[] jArr3 = w2;
                        int i2 = rn2;
                        int i3 = ln2;
                        int i4 = g;
                        Merger m = new Merger(this, jArr2, jArr3, lb + lh, ln2 - lh, rb + rh, rn2 - rh, k + lh + rh, g);
                        rn2 = rh;
                        ln2 = lh;
                        addToPendingCount(1);
                        m.fork();
                        a2 = a2;
                    } else if (rn2 <= g) {
                        break;
                    } else {
                        int lh3 = ln2;
                        int i5 = rn2 >>> 1;
                        int rh3 = i5;
                        long split2 = a2[i5 + rb];
                        while (lo < lh3) {
                            int lm = (lo + lh3) >>> 1;
                            if (split2 <= a2[lm + lb]) {
                                lh3 = lm;
                            } else {
                                lo = lm + 1;
                            }
                        }
                        lh = lh3;
                        rh = rh3;
                        long[] jArr22 = a2;
                        long[] jArr32 = w2;
                        int i22 = rn2;
                        int i32 = ln2;
                        int i42 = g;
                        Merger m2 = new Merger(this, jArr22, jArr32, lb + lh, ln2 - lh, rb + rh, rn2 - rh, k + lh + rh, g);
                        rn2 = rh;
                        ln2 = lh;
                        addToPendingCount(1);
                        m2.fork();
                        a2 = a2;
                    }
                }
                int lf = lb + ln2;
                int rf = rb + rn2;
                while (lb < lf && rb < rf) {
                    long j = a2[lb];
                    long al = j;
                    long j2 = a2[rb];
                    long ar = j2;
                    if (j <= j2) {
                        lb++;
                        t = al;
                    } else {
                        rb++;
                        t = ar;
                    }
                    w2[k] = t;
                    k++;
                }
                if (rb < rf) {
                    System.arraycopy((Object) a2, rb, (Object) w2, k, rf - rb);
                } else if (lb < lf) {
                    System.arraycopy((Object) a2, lb, (Object) w2, k, lf - lb);
                }
                tryComplete();
            }
        }

        static final class Sorter extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final long[] a;
            final int base;
            final int gran;
            final int size;
            final long[] w;
            final int wbase;

            Sorter(CountedCompleter<?> par, long[] a2, long[] w2, int base2, int size2, int wbase2, int gran2) {
                super(par);
                this.a = a2;
                this.w = w2;
                this.base = base2;
                this.size = size2;
                this.wbase = wbase2;
                this.gran = gran2;
            }

            /* JADX WARNING: type inference failed for: r2v7, types: [java.util.ArraysParallelSortHelpers$EmptyCompleter] */
            /* JADX WARNING: Multi-variable type inference failed */
            public final void compute() {
                long[] a2 = this.a;
                long[] w2 = this.w;
                int b = this.base;
                int n = this.size;
                int wb = this.wbase;
                int g = this.gran;
                Sorter sorter = this;
                int n2 = n;
                while (true) {
                    int g2 = g;
                    if (n2 > g2) {
                        int h = n2 >>> 1;
                        int q = h >>> 1;
                        int u = h + q;
                        Merger merger = r2;
                        long[] w3 = w2;
                        int g3 = g2;
                        Sorter sorter2 = sorter;
                        int i = g3;
                        Merger merger2 = new Merger(sorter, w2, a2, wb, h, wb + h, n2 - h, b, i);
                        Relay relay = new Relay(merger);
                        Relay fc = relay;
                        Merger merger3 = new Merger(fc, a2, w3, b + h, q, b + u, n2 - u, wb + h, i);
                        Relay rc = new Relay(merger3);
                        Sorter sorter3 = new Sorter(rc, a2, w3, b + u, n2 - u, wb + u, i);
                        sorter3.fork();
                        long[] jArr = a2;
                        long[] jArr2 = w3;
                        int i2 = q;
                        int wb2 = wb;
                        int b2 = b;
                        Sorter sorter4 = new Sorter(rc, jArr, jArr2, b + h, i2, wb + h, g3);
                        sorter4.fork();
                        long[] a3 = a2;
                        Merger merger4 = new Merger(fc, jArr, jArr2, b2, i2, b2 + q, h - q, wb2, g3);
                        Relay bc = new Relay(merger4);
                        Sorter sorter5 = new Sorter(bc, a3, jArr2, b2 + q, h - q, wb2 + q, g3);
                        sorter5.fork();
                        sorter = new EmptyCompleter(bc);
                        n2 = q;
                        g = g3;
                        wb = wb2;
                        w2 = w3;
                        b = b2;
                        a2 = a3;
                    } else {
                        Sorter sorter6 = sorter;
                        int b3 = b;
                        DualPivotQuicksort.sort(a2, b3, (b3 + n2) - 1, w2, wb, n2);
                        sorter.tryComplete();
                        return;
                    }
                }
            }
        }

        FJLong() {
        }
    }

    static final class FJObject {

        static final class Merger<T> extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final T[] a;
            Comparator<? super T> comparator;
            final int gran;
            final int lbase;
            final int lsize;
            final int rbase;
            final int rsize;
            final T[] w;
            final int wbase;

            Merger(CountedCompleter<?> par, T[] a2, T[] w2, int lbase2, int lsize2, int rbase2, int rsize2, int wbase2, int gran2, Comparator<? super T> comparator2) {
                super(par);
                this.a = a2;
                this.w = w2;
                this.lbase = lbase2;
                this.lsize = lsize2;
                this.rbase = rbase2;
                this.rsize = rsize2;
                this.wbase = wbase2;
                this.gran = gran2;
                this.comparator = comparator2;
            }

            public final void compute() {
                int rh;
                int lh;
                T t;
                Comparator<? super T> c = this.comparator;
                T[] a2 = this.a;
                T[] w2 = this.w;
                int lb = this.lbase;
                int ln = this.lsize;
                int rb = this.rbase;
                int rn = this.rsize;
                int k = this.wbase;
                int g = this.gran;
                if (a2 == null || w2 == null || lb < 0 || rb < 0 || k < 0 || c == null) {
                    int i = k;
                    int i2 = rb;
                    T[] tArr = a2;
                    throw new IllegalStateException();
                }
                int ln2 = ln;
                int rn2 = rn;
                while (true) {
                    int lo = 0;
                    int i3 = 1;
                    if (ln2 >= rn2) {
                        if (ln2 <= g) {
                            break;
                        }
                        int rh2 = rn2;
                        int i4 = ln2 >>> 1;
                        int lh2 = i4;
                        T split = a2[i4 + lb];
                        while (lo < rh2) {
                            int rm = (lo + rh2) >>> i3;
                            if (c.compare(split, a2[rm + rb]) <= 0) {
                                rh2 = rm;
                            } else {
                                lo = rm + 1;
                            }
                            i3 = 1;
                        }
                        rh = rh2;
                        lh = lh2;
                        int i5 = rn2 - rh;
                        T[] tArr2 = a2;
                        T[] tArr3 = w2;
                        int i6 = rn2;
                        int i7 = ln2;
                        int ln3 = i5;
                        int g2 = g;
                        Merger<T> m = new Merger<>(this, tArr2, tArr3, lb + lh, ln2 - lh, rb + rh, ln3, k + lh + rh, g2, c);
                        rn2 = rh;
                        ln2 = lh;
                        addToPendingCount(1);
                        m.fork();
                        g = g2;
                        k = k;
                        a2 = a2;
                        rb = rb;
                    } else if (rn2 <= g) {
                        break;
                    } else {
                        int lh3 = ln2;
                        int i8 = rn2 >>> 1;
                        int rh3 = i8;
                        T split2 = a2[i8 + rb];
                        while (lo < lh3) {
                            int lm = (lo + lh3) >>> 1;
                            if (c.compare(split2, a2[lm + lb]) <= 0) {
                                lh3 = lm;
                            } else {
                                lo = lm + 1;
                            }
                        }
                        lh = lh3;
                        rh = rh3;
                        int i52 = rn2 - rh;
                        T[] tArr22 = a2;
                        T[] tArr32 = w2;
                        int i62 = rn2;
                        int i72 = ln2;
                        int ln32 = i52;
                        int g22 = g;
                        Merger<T> m2 = new Merger<>(this, tArr22, tArr32, lb + lh, ln2 - lh, rb + rh, ln32, k + lh + rh, g22, c);
                        rn2 = rh;
                        ln2 = lh;
                        addToPendingCount(1);
                        m2.fork();
                        g = g22;
                        k = k;
                        a2 = a2;
                        rb = rb;
                    }
                }
                int lf = lb + ln2;
                int rf = rb + rn2;
                while (lb < lf && rb < rf) {
                    T t2 = a2[lb];
                    T al = t2;
                    T t3 = a2[rb];
                    T ar = t3;
                    if (c.compare(t2, t3) <= 0) {
                        lb++;
                        t = al;
                    } else {
                        rb++;
                        t = ar;
                    }
                    w2[k] = t;
                    k++;
                }
                if (rb < rf) {
                    System.arraycopy((Object) a2, rb, (Object) w2, k, rf - rb);
                } else if (lb < lf) {
                    System.arraycopy((Object) a2, lb, (Object) w2, k, lf - lb);
                }
                tryComplete();
            }
        }

        static final class Sorter<T> extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final T[] a;
            final int base;
            Comparator<? super T> comparator;
            final int gran;
            final int size;
            final T[] w;
            final int wbase;

            Sorter(CountedCompleter<?> par, T[] a2, T[] w2, int base2, int size2, int wbase2, int gran2, Comparator<? super T> comparator2) {
                super(par);
                this.a = a2;
                this.w = w2;
                this.base = base2;
                this.size = size2;
                this.wbase = wbase2;
                this.gran = gran2;
                this.comparator = comparator2;
            }

            /* JADX WARNING: type inference failed for: r2v6, types: [java.util.ArraysParallelSortHelpers$EmptyCompleter] */
            /* JADX WARNING: Multi-variable type inference failed */
            public final void compute() {
                Comparator<? super T> c = this.comparator;
                T[] a2 = this.a;
                T[] w2 = this.w;
                int b = this.base;
                int n = this.size;
                int wb = this.wbase;
                int g = this.gran;
                Sorter sorter = this;
                int n2 = n;
                while (true) {
                    int g2 = g;
                    if (n2 > g2) {
                        int h = n2 >>> 1;
                        int q = h >>> 1;
                        int u = h + q;
                        Merger merger = r2;
                        T[] w3 = w2;
                        int g3 = g2;
                        Sorter sorter2 = sorter;
                        int i = b;
                        int wb2 = wb;
                        int wb3 = g3;
                        int b2 = b;
                        Comparator<? super T> comparator2 = c;
                        Merger merger2 = new Merger(sorter, w2, a2, wb, h, wb + h, n2 - h, i, wb3, comparator2);
                        Relay relay = new Relay(merger);
                        Relay fc = relay;
                        Merger merger3 = new Merger(fc, a2, w3, b2 + h, q, b2 + u, n2 - u, wb2 + h, wb3, comparator2);
                        Relay rc = new Relay(merger3);
                        Sorter sorter3 = new Sorter(rc, a2, w3, b2 + u, n2 - u, wb2 + u, wb3, comparator2);
                        sorter3.fork();
                        T[] tArr = w3;
                        int i2 = q;
                        T[] a3 = a2;
                        Sorter sorter4 = new Sorter(rc, a2, tArr, b2 + h, i2, wb2 + h, g3, c);
                        sorter4.fork();
                        T[] tArr2 = a3;
                        Comparator<? super T> c2 = c;
                        Merger merger4 = new Merger(fc, tArr2, tArr, b2, i2, b2 + q, h - q, wb2, g3, c2);
                        Relay bc = new Relay(merger4);
                        Sorter sorter5 = new Sorter(bc, tArr2, tArr, b2 + q, h - q, wb2 + q, g3, c2);
                        sorter5.fork();
                        sorter = new EmptyCompleter(bc);
                        n2 = q;
                        wb = wb2;
                        w2 = w3;
                        g = g3;
                        b = b2;
                        a2 = a3;
                        c = c2;
                    } else {
                        Sorter sorter6 = sorter;
                        int b3 = b;
                        TimSort.sort(a2, b3, b3 + n2, c, w2, wb, n2);
                        sorter.tryComplete();
                        return;
                    }
                }
            }
        }

        FJObject() {
        }
    }

    static final class FJShort {

        static final class Merger extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final short[] a;
            final int gran;
            final int lbase;
            final int lsize;
            final int rbase;
            final int rsize;
            final short[] w;
            final int wbase;

            Merger(CountedCompleter<?> par, short[] a2, short[] w2, int lbase2, int lsize2, int rbase2, int rsize2, int wbase2, int gran2) {
                super(par);
                this.a = a2;
                this.w = w2;
                this.lbase = lbase2;
                this.lsize = lsize2;
                this.rbase = rbase2;
                this.rsize = rsize2;
                this.wbase = wbase2;
                this.gran = gran2;
            }

            public final void compute() {
                int rh;
                int lh;
                short t;
                short[] a2 = this.a;
                short[] w2 = this.w;
                int lb = this.lbase;
                int ln = this.lsize;
                int rb = this.rbase;
                int rn = this.rsize;
                int k = this.wbase;
                int g = this.gran;
                if (a2 == null || w2 == null || lb < 0 || rb < 0 || k < 0) {
                    short[] sArr = a2;
                    throw new IllegalStateException();
                }
                int ln2 = ln;
                int rn2 = rn;
                while (true) {
                    int lo = 0;
                    if (ln2 >= rn2) {
                        if (ln2 <= g) {
                            break;
                        }
                        int rh2 = rn2;
                        int i = ln2 >>> 1;
                        int lh2 = i;
                        short split = a2[i + lb];
                        while (lo < rh2) {
                            int rm = (lo + rh2) >>> 1;
                            if (split <= a2[rm + rb]) {
                                rh2 = rm;
                            } else {
                                lo = rm + 1;
                            }
                        }
                        rh = rh2;
                        lh = lh2;
                        short[] sArr2 = a2;
                        short[] sArr3 = w2;
                        int i2 = rn2;
                        int i3 = ln2;
                        int i4 = g;
                        Merger m = new Merger(this, sArr2, sArr3, lb + lh, ln2 - lh, rb + rh, rn2 - rh, k + lh + rh, g);
                        rn2 = rh;
                        ln2 = lh;
                        addToPendingCount(1);
                        m.fork();
                        a2 = a2;
                    } else if (rn2 <= g) {
                        break;
                    } else {
                        int lh3 = ln2;
                        int i5 = rn2 >>> 1;
                        int rh3 = i5;
                        short split2 = a2[i5 + rb];
                        while (lo < lh3) {
                            int lm = (lo + lh3) >>> 1;
                            if (split2 <= a2[lm + lb]) {
                                lh3 = lm;
                            } else {
                                lo = lm + 1;
                            }
                        }
                        lh = lh3;
                        rh = rh3;
                        short[] sArr22 = a2;
                        short[] sArr32 = w2;
                        int i22 = rn2;
                        int i32 = ln2;
                        int i42 = g;
                        Merger m2 = new Merger(this, sArr22, sArr32, lb + lh, ln2 - lh, rb + rh, rn2 - rh, k + lh + rh, g);
                        rn2 = rh;
                        ln2 = lh;
                        addToPendingCount(1);
                        m2.fork();
                        a2 = a2;
                    }
                }
                int lf = lb + ln2;
                int rf = rb + rn2;
                while (lb < lf && rb < rf) {
                    short s = a2[lb];
                    short al = s;
                    short s2 = a2[rb];
                    short ar = s2;
                    if (s <= s2) {
                        lb++;
                        t = al;
                    } else {
                        rb++;
                        t = ar;
                    }
                    w2[k] = t;
                    k++;
                }
                if (rb < rf) {
                    System.arraycopy((Object) a2, rb, (Object) w2, k, rf - rb);
                } else if (lb < lf) {
                    System.arraycopy((Object) a2, lb, (Object) w2, k, lf - lb);
                }
                tryComplete();
            }
        }

        static final class Sorter extends CountedCompleter<Void> {
            static final long serialVersionUID = 2446542900576103244L;
            final short[] a;
            final int base;
            final int gran;
            final int size;
            final short[] w;
            final int wbase;

            Sorter(CountedCompleter<?> par, short[] a2, short[] w2, int base2, int size2, int wbase2, int gran2) {
                super(par);
                this.a = a2;
                this.w = w2;
                this.base = base2;
                this.size = size2;
                this.wbase = wbase2;
                this.gran = gran2;
            }

            /* JADX WARNING: type inference failed for: r2v7, types: [java.util.ArraysParallelSortHelpers$EmptyCompleter] */
            /* JADX WARNING: Multi-variable type inference failed */
            public final void compute() {
                short[] a2 = this.a;
                short[] w2 = this.w;
                int b = this.base;
                int n = this.size;
                int wb = this.wbase;
                int g = this.gran;
                Sorter sorter = this;
                int n2 = n;
                while (true) {
                    int g2 = g;
                    if (n2 > g2) {
                        int h = n2 >>> 1;
                        int q = h >>> 1;
                        int u = h + q;
                        Merger merger = r2;
                        short[] w3 = w2;
                        int g3 = g2;
                        Sorter sorter2 = sorter;
                        int i = g3;
                        Merger merger2 = new Merger(sorter, w2, a2, wb, h, wb + h, n2 - h, b, i);
                        Relay relay = new Relay(merger);
                        Relay fc = relay;
                        Merger merger3 = new Merger(fc, a2, w3, b + h, q, b + u, n2 - u, wb + h, i);
                        Relay rc = new Relay(merger3);
                        Sorter sorter3 = new Sorter(rc, a2, w3, b + u, n2 - u, wb + u, i);
                        sorter3.fork();
                        short[] sArr = a2;
                        short[] sArr2 = w3;
                        int i2 = q;
                        int wb2 = wb;
                        int b2 = b;
                        Sorter sorter4 = new Sorter(rc, sArr, sArr2, b + h, i2, wb + h, g3);
                        sorter4.fork();
                        short[] a3 = a2;
                        Merger merger4 = new Merger(fc, sArr, sArr2, b2, i2, b2 + q, h - q, wb2, g3);
                        Relay bc = new Relay(merger4);
                        Sorter sorter5 = new Sorter(bc, a3, sArr2, b2 + q, h - q, wb2 + q, g3);
                        sorter5.fork();
                        sorter = new EmptyCompleter(bc);
                        n2 = q;
                        g = g3;
                        wb = wb2;
                        w2 = w3;
                        b = b2;
                        a2 = a3;
                    } else {
                        Sorter sorter6 = sorter;
                        int b3 = b;
                        DualPivotQuicksort.sort(a2, b3, (b3 + n2) - 1, w2, wb, n2);
                        sorter.tryComplete();
                        return;
                    }
                }
            }
        }

        FJShort() {
        }
    }

    static final class Relay extends CountedCompleter<Void> {
        static final long serialVersionUID = 2446542900576103244L;
        final CountedCompleter<?> task;

        Relay(CountedCompleter<?> task2) {
            super(null, 1);
            this.task = task2;
        }

        public final void compute() {
        }

        public final void onCompletion(CountedCompleter<?> countedCompleter) {
            this.task.compute();
        }
    }

    ArraysParallelSortHelpers() {
    }
}
