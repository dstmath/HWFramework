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

            Merger(CountedCompleter<?> par, byte[] a, byte[] w, int lbase, int lsize, int rbase, int rsize, int wbase, int gran) {
                super(par);
                this.a = a;
                this.w = w;
                this.lbase = lbase;
                this.lsize = lsize;
                this.rbase = rbase;
                this.rsize = rsize;
                this.wbase = wbase;
                this.gran = gran;
            }

            public final void compute() {
                byte[] a = this.a;
                byte[] w = this.w;
                int lb = this.lbase;
                int ln = this.lsize;
                int rb = this.rbase;
                int rn = this.rsize;
                int k = this.wbase;
                int g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0) {
                    throw new IllegalStateException();
                }
                int k2;
                while (true) {
                    int lh;
                    int rh;
                    byte split;
                    int lo;
                    Merger m;
                    if (ln < rn) {
                        if (rn <= g) {
                            break;
                        }
                        lh = ln;
                        rh = rn >>> 1;
                        split = a[rh + rb];
                        lo = 0;
                        while (lo < lh) {
                            int lm = (lo + lh) >>> 1;
                            if (split <= a[lm + lb]) {
                                lh = lm;
                            } else {
                                lo = lm + 1;
                            }
                        }
                        m = new Merger(this, a, w, lb + lh, ln - lh, rb + rh, rn - rh, (k + lh) + rh, g);
                        rn = rh;
                        ln = lh;
                        addToPendingCount(1);
                        m.fork();
                    } else if (ln <= g) {
                        break;
                    } else {
                        rh = rn;
                        lh = ln >>> 1;
                        split = a[lh + lb];
                        lo = 0;
                        while (lo < rh) {
                            int rm = (lo + rh) >>> 1;
                            if (split <= a[rm + rb]) {
                                rh = rm;
                            } else {
                                lo = rm + 1;
                            }
                        }
                        m = new Merger(this, a, w, lb + lh, ln - lh, rb + rh, rn - rh, (k + lh) + rh, g);
                        rn = rh;
                        ln = lh;
                        addToPendingCount(1);
                        m.fork();
                    }
                }
                int lf = lb + ln;
                int rf = rb + rn;
                while (true) {
                    k2 = k;
                    if (lb < lf && rb < rf) {
                        byte t;
                        byte al = a[lb];
                        byte ar = a[rb];
                        if (al <= ar) {
                            lb++;
                            t = al;
                        } else {
                            rb++;
                            t = ar;
                        }
                        k = k2 + 1;
                        w[k2] = t;
                    }
                }
                if (rb < rf) {
                    System.arraycopy(a, rb, w, k2, rf - rb);
                } else if (lb < lf) {
                    System.arraycopy(a, lb, w, k2, lf - lb);
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

            Sorter(CountedCompleter<?> par, byte[] a, byte[] w, int base, int size, int wbase, int gran) {
                super(par);
                this.a = a;
                this.w = w;
                this.base = base;
                this.size = size;
                this.wbase = wbase;
                this.gran = gran;
            }

            public final void compute() {
                CountedCompleter<?> s = this;
                byte[] a = this.a;
                byte[] w = this.w;
                int b = this.base;
                int n = this.size;
                int wb = this.wbase;
                int g = this.gran;
                while (n > g) {
                    int h = n >>> 1;
                    int q = h >>> 1;
                    int u = h + q;
                    Relay fc = new Relay(new Merger(s, w, a, wb, h, wb + h, n - h, b, g));
                    CountedCompleter relay = new Relay(new Merger(fc, a, w, b + h, q, b + u, n - u, wb + h, g));
                    new Sorter(relay, a, w, b + u, n - u, wb + u, g).fork();
                    new Sorter(relay, a, w, b + h, q, wb + h, g).fork();
                    relay = new Relay(new Merger(fc, a, w, b, q, b + q, h - q, wb, g));
                    new Sorter(relay, a, w, b + q, h - q, wb + q, g).fork();
                    s = new EmptyCompleter(relay);
                    n = q;
                }
                DualPivotQuicksort.sort(a, b, (b + n) - 1);
                s.tryComplete();
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

            Merger(CountedCompleter<?> par, char[] a, char[] w, int lbase, int lsize, int rbase, int rsize, int wbase, int gran) {
                super(par);
                this.a = a;
                this.w = w;
                this.lbase = lbase;
                this.lsize = lsize;
                this.rbase = rbase;
                this.rsize = rsize;
                this.wbase = wbase;
                this.gran = gran;
            }

            public final void compute() {
                char[] a = this.a;
                char[] w = this.w;
                int lb = this.lbase;
                int ln = this.lsize;
                int rb = this.rbase;
                int rn = this.rsize;
                int k = this.wbase;
                int g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0) {
                    throw new IllegalStateException();
                }
                int k2;
                while (true) {
                    int lh;
                    int rh;
                    char split;
                    int lo;
                    Merger m;
                    if (ln < rn) {
                        if (rn <= g) {
                            break;
                        }
                        lh = ln;
                        rh = rn >>> 1;
                        split = a[rh + rb];
                        lo = 0;
                        while (lo < lh) {
                            int lm = (lo + lh) >>> 1;
                            if (split <= a[lm + lb]) {
                                lh = lm;
                            } else {
                                lo = lm + 1;
                            }
                        }
                        m = new Merger(this, a, w, lb + lh, ln - lh, rb + rh, rn - rh, (k + lh) + rh, g);
                        rn = rh;
                        ln = lh;
                        addToPendingCount(1);
                        m.fork();
                    } else if (ln <= g) {
                        break;
                    } else {
                        rh = rn;
                        lh = ln >>> 1;
                        split = a[lh + lb];
                        lo = 0;
                        while (lo < rh) {
                            int rm = (lo + rh) >>> 1;
                            if (split <= a[rm + rb]) {
                                rh = rm;
                            } else {
                                lo = rm + 1;
                            }
                        }
                        m = new Merger(this, a, w, lb + lh, ln - lh, rb + rh, rn - rh, (k + lh) + rh, g);
                        rn = rh;
                        ln = lh;
                        addToPendingCount(1);
                        m.fork();
                    }
                }
                int lf = lb + ln;
                int rf = rb + rn;
                while (true) {
                    k2 = k;
                    if (lb < lf && rb < rf) {
                        char t;
                        char al = a[lb];
                        char ar = a[rb];
                        if (al <= ar) {
                            lb++;
                            t = al;
                        } else {
                            rb++;
                            t = ar;
                        }
                        k = k2 + 1;
                        w[k2] = t;
                    }
                }
                if (rb < rf) {
                    System.arraycopy(a, rb, w, k2, rf - rb);
                } else if (lb < lf) {
                    System.arraycopy(a, lb, w, k2, lf - lb);
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

            Sorter(CountedCompleter<?> par, char[] a, char[] w, int base, int size, int wbase, int gran) {
                super(par);
                this.a = a;
                this.w = w;
                this.base = base;
                this.size = size;
                this.wbase = wbase;
                this.gran = gran;
            }

            public final void compute() {
                CountedCompleter<?> s = this;
                char[] a = this.a;
                char[] w = this.w;
                int b = this.base;
                int n = this.size;
                int wb = this.wbase;
                int g = this.gran;
                while (n > g) {
                    int h = n >>> 1;
                    int q = h >>> 1;
                    int u = h + q;
                    Relay fc = new Relay(new Merger(s, w, a, wb, h, wb + h, n - h, b, g));
                    CountedCompleter relay = new Relay(new Merger(fc, a, w, b + h, q, b + u, n - u, wb + h, g));
                    new Sorter(relay, a, w, b + u, n - u, wb + u, g).fork();
                    new Sorter(relay, a, w, b + h, q, wb + h, g).fork();
                    relay = new Relay(new Merger(fc, a, w, b, q, b + q, h - q, wb, g));
                    new Sorter(relay, a, w, b + q, h - q, wb + q, g).fork();
                    s = new EmptyCompleter(relay);
                    n = q;
                }
                DualPivotQuicksort.sort(a, b, (b + n) - 1, w, wb, n);
                s.tryComplete();
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

            Merger(CountedCompleter<?> par, double[] a, double[] w, int lbase, int lsize, int rbase, int rsize, int wbase, int gran) {
                super(par);
                this.a = a;
                this.w = w;
                this.lbase = lbase;
                this.lsize = lsize;
                this.rbase = rbase;
                this.rsize = rsize;
                this.wbase = wbase;
                this.gran = gran;
            }

            public final void compute() {
                double[] a = this.a;
                double[] w = this.w;
                int lb = this.lbase;
                int ln = this.lsize;
                int rb = this.rbase;
                int rn = this.rsize;
                int k = this.wbase;
                int g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0) {
                    throw new IllegalStateException();
                }
                int k2;
                while (true) {
                    int lh;
                    int rh;
                    double split;
                    int lo;
                    Merger m;
                    if (ln < rn) {
                        if (rn <= g) {
                            break;
                        }
                        lh = ln;
                        rh = rn >>> 1;
                        split = a[rh + rb];
                        lo = 0;
                        while (lo < lh) {
                            int lm = (lo + lh) >>> 1;
                            if (split <= a[lm + lb]) {
                                lh = lm;
                            } else {
                                lo = lm + 1;
                            }
                        }
                        m = new Merger(this, a, w, lb + lh, ln - lh, rb + rh, rn - rh, (k + lh) + rh, g);
                        rn = rh;
                        ln = lh;
                        addToPendingCount(1);
                        m.fork();
                    } else if (ln <= g) {
                        break;
                    } else {
                        rh = rn;
                        lh = ln >>> 1;
                        split = a[lh + lb];
                        lo = 0;
                        while (lo < rh) {
                            int rm = (lo + rh) >>> 1;
                            if (split <= a[rm + rb]) {
                                rh = rm;
                            } else {
                                lo = rm + 1;
                            }
                        }
                        m = new Merger(this, a, w, lb + lh, ln - lh, rb + rh, rn - rh, (k + lh) + rh, g);
                        rn = rh;
                        ln = lh;
                        addToPendingCount(1);
                        m.fork();
                    }
                }
                int lf = lb + ln;
                int rf = rb + rn;
                while (true) {
                    k2 = k;
                    if (lb < lf && rb < rf) {
                        double t;
                        double al = a[lb];
                        double ar = a[rb];
                        if (al <= ar) {
                            lb++;
                            t = al;
                        } else {
                            rb++;
                            t = ar;
                        }
                        k = k2 + 1;
                        w[k2] = t;
                    }
                }
                if (rb < rf) {
                    System.arraycopy(a, rb, w, k2, rf - rb);
                } else if (lb < lf) {
                    System.arraycopy(a, lb, w, k2, lf - lb);
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

            Sorter(CountedCompleter<?> par, double[] a, double[] w, int base, int size, int wbase, int gran) {
                super(par);
                this.a = a;
                this.w = w;
                this.base = base;
                this.size = size;
                this.wbase = wbase;
                this.gran = gran;
            }

            public final void compute() {
                CountedCompleter<?> s = this;
                double[] a = this.a;
                double[] w = this.w;
                int b = this.base;
                int n = this.size;
                int wb = this.wbase;
                int g = this.gran;
                while (n > g) {
                    int h = n >>> 1;
                    int q = h >>> 1;
                    int u = h + q;
                    Relay fc = new Relay(new Merger(s, w, a, wb, h, wb + h, n - h, b, g));
                    CountedCompleter relay = new Relay(new Merger(fc, a, w, b + h, q, b + u, n - u, wb + h, g));
                    new Sorter(relay, a, w, b + u, n - u, wb + u, g).fork();
                    new Sorter(relay, a, w, b + h, q, wb + h, g).fork();
                    relay = new Relay(new Merger(fc, a, w, b, q, b + q, h - q, wb, g));
                    new Sorter(relay, a, w, b + q, h - q, wb + q, g).fork();
                    s = new EmptyCompleter(relay);
                    n = q;
                }
                DualPivotQuicksort.sort(a, b, (b + n) - 1, w, wb, n);
                s.tryComplete();
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

            Merger(CountedCompleter<?> par, float[] a, float[] w, int lbase, int lsize, int rbase, int rsize, int wbase, int gran) {
                super(par);
                this.a = a;
                this.w = w;
                this.lbase = lbase;
                this.lsize = lsize;
                this.rbase = rbase;
                this.rsize = rsize;
                this.wbase = wbase;
                this.gran = gran;
            }

            public final void compute() {
                float[] a = this.a;
                float[] w = this.w;
                int lb = this.lbase;
                int ln = this.lsize;
                int rb = this.rbase;
                int rn = this.rsize;
                int k = this.wbase;
                int g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0) {
                    throw new IllegalStateException();
                }
                int k2;
                while (true) {
                    int lh;
                    int rh;
                    float split;
                    int lo;
                    Merger m;
                    if (ln < rn) {
                        if (rn <= g) {
                            break;
                        }
                        lh = ln;
                        rh = rn >>> 1;
                        split = a[rh + rb];
                        lo = 0;
                        while (lo < lh) {
                            int lm = (lo + lh) >>> 1;
                            if (split <= a[lm + lb]) {
                                lh = lm;
                            } else {
                                lo = lm + 1;
                            }
                        }
                        m = new Merger(this, a, w, lb + lh, ln - lh, rb + rh, rn - rh, (k + lh) + rh, g);
                        rn = rh;
                        ln = lh;
                        addToPendingCount(1);
                        m.fork();
                    } else if (ln <= g) {
                        break;
                    } else {
                        rh = rn;
                        lh = ln >>> 1;
                        split = a[lh + lb];
                        lo = 0;
                        while (lo < rh) {
                            int rm = (lo + rh) >>> 1;
                            if (split <= a[rm + rb]) {
                                rh = rm;
                            } else {
                                lo = rm + 1;
                            }
                        }
                        m = new Merger(this, a, w, lb + lh, ln - lh, rb + rh, rn - rh, (k + lh) + rh, g);
                        rn = rh;
                        ln = lh;
                        addToPendingCount(1);
                        m.fork();
                    }
                }
                int lf = lb + ln;
                int rf = rb + rn;
                while (true) {
                    k2 = k;
                    if (lb < lf && rb < rf) {
                        float t;
                        float al = a[lb];
                        float ar = a[rb];
                        if (al <= ar) {
                            lb++;
                            t = al;
                        } else {
                            rb++;
                            t = ar;
                        }
                        k = k2 + 1;
                        w[k2] = t;
                    }
                }
                if (rb < rf) {
                    System.arraycopy(a, rb, w, k2, rf - rb);
                } else if (lb < lf) {
                    System.arraycopy(a, lb, w, k2, lf - lb);
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

            Sorter(CountedCompleter<?> par, float[] a, float[] w, int base, int size, int wbase, int gran) {
                super(par);
                this.a = a;
                this.w = w;
                this.base = base;
                this.size = size;
                this.wbase = wbase;
                this.gran = gran;
            }

            public final void compute() {
                CountedCompleter<?> s = this;
                float[] a = this.a;
                float[] w = this.w;
                int b = this.base;
                int n = this.size;
                int wb = this.wbase;
                int g = this.gran;
                while (n > g) {
                    int h = n >>> 1;
                    int q = h >>> 1;
                    int u = h + q;
                    Relay fc = new Relay(new Merger(s, w, a, wb, h, wb + h, n - h, b, g));
                    CountedCompleter relay = new Relay(new Merger(fc, a, w, b + h, q, b + u, n - u, wb + h, g));
                    new Sorter(relay, a, w, b + u, n - u, wb + u, g).fork();
                    new Sorter(relay, a, w, b + h, q, wb + h, g).fork();
                    relay = new Relay(new Merger(fc, a, w, b, q, b + q, h - q, wb, g));
                    new Sorter(relay, a, w, b + q, h - q, wb + q, g).fork();
                    s = new EmptyCompleter(relay);
                    n = q;
                }
                DualPivotQuicksort.sort(a, b, (b + n) - 1, w, wb, n);
                s.tryComplete();
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

            Merger(CountedCompleter<?> par, int[] a, int[] w, int lbase, int lsize, int rbase, int rsize, int wbase, int gran) {
                super(par);
                this.a = a;
                this.w = w;
                this.lbase = lbase;
                this.lsize = lsize;
                this.rbase = rbase;
                this.rsize = rsize;
                this.wbase = wbase;
                this.gran = gran;
            }

            public final void compute() {
                int[] a = this.a;
                int[] w = this.w;
                int lb = this.lbase;
                int ln = this.lsize;
                int rb = this.rbase;
                int rn = this.rsize;
                int k = this.wbase;
                int g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0) {
                    throw new IllegalStateException();
                }
                int k2;
                while (true) {
                    int lh;
                    int rh;
                    int split;
                    int lo;
                    Merger m;
                    if (ln < rn) {
                        if (rn <= g) {
                            break;
                        }
                        lh = ln;
                        rh = rn >>> 1;
                        split = a[rh + rb];
                        lo = 0;
                        while (lo < lh) {
                            int lm = (lo + lh) >>> 1;
                            if (split <= a[lm + lb]) {
                                lh = lm;
                            } else {
                                lo = lm + 1;
                            }
                        }
                        m = new Merger(this, a, w, lb + lh, ln - lh, rb + rh, rn - rh, (k + lh) + rh, g);
                        rn = rh;
                        ln = lh;
                        addToPendingCount(1);
                        m.fork();
                    } else if (ln <= g) {
                        break;
                    } else {
                        rh = rn;
                        lh = ln >>> 1;
                        split = a[lh + lb];
                        lo = 0;
                        while (lo < rh) {
                            int rm = (lo + rh) >>> 1;
                            if (split <= a[rm + rb]) {
                                rh = rm;
                            } else {
                                lo = rm + 1;
                            }
                        }
                        m = new Merger(this, a, w, lb + lh, ln - lh, rb + rh, rn - rh, (k + lh) + rh, g);
                        rn = rh;
                        ln = lh;
                        addToPendingCount(1);
                        m.fork();
                    }
                }
                int lf = lb + ln;
                int rf = rb + rn;
                while (true) {
                    k2 = k;
                    if (lb < lf && rb < rf) {
                        int t;
                        int al = a[lb];
                        int ar = a[rb];
                        if (al <= ar) {
                            lb++;
                            t = al;
                        } else {
                            rb++;
                            t = ar;
                        }
                        k = k2 + 1;
                        w[k2] = t;
                    }
                }
                if (rb < rf) {
                    System.arraycopy(a, rb, w, k2, rf - rb);
                } else if (lb < lf) {
                    System.arraycopy(a, lb, w, k2, lf - lb);
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

            Sorter(CountedCompleter<?> par, int[] a, int[] w, int base, int size, int wbase, int gran) {
                super(par);
                this.a = a;
                this.w = w;
                this.base = base;
                this.size = size;
                this.wbase = wbase;
                this.gran = gran;
            }

            public final void compute() {
                CountedCompleter<?> s = this;
                int[] a = this.a;
                int[] w = this.w;
                int b = this.base;
                int n = this.size;
                int wb = this.wbase;
                int g = this.gran;
                while (n > g) {
                    int h = n >>> 1;
                    int q = h >>> 1;
                    int u = h + q;
                    Relay fc = new Relay(new Merger(s, w, a, wb, h, wb + h, n - h, b, g));
                    CountedCompleter relay = new Relay(new Merger(fc, a, w, b + h, q, b + u, n - u, wb + h, g));
                    new Sorter(relay, a, w, b + u, n - u, wb + u, g).fork();
                    new Sorter(relay, a, w, b + h, q, wb + h, g).fork();
                    relay = new Relay(new Merger(fc, a, w, b, q, b + q, h - q, wb, g));
                    new Sorter(relay, a, w, b + q, h - q, wb + q, g).fork();
                    s = new EmptyCompleter(relay);
                    n = q;
                }
                DualPivotQuicksort.sort(a, b, (b + n) - 1, w, wb, n);
                s.tryComplete();
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

            Merger(CountedCompleter<?> par, long[] a, long[] w, int lbase, int lsize, int rbase, int rsize, int wbase, int gran) {
                super(par);
                this.a = a;
                this.w = w;
                this.lbase = lbase;
                this.lsize = lsize;
                this.rbase = rbase;
                this.rsize = rsize;
                this.wbase = wbase;
                this.gran = gran;
            }

            public final void compute() {
                long[] a = this.a;
                long[] w = this.w;
                int lb = this.lbase;
                int ln = this.lsize;
                int rb = this.rbase;
                int rn = this.rsize;
                int k = this.wbase;
                int g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0) {
                    throw new IllegalStateException();
                }
                int k2;
                while (true) {
                    int lh;
                    int rh;
                    long split;
                    int lo;
                    Merger m;
                    if (ln < rn) {
                        if (rn <= g) {
                            break;
                        }
                        lh = ln;
                        rh = rn >>> 1;
                        split = a[rh + rb];
                        lo = 0;
                        while (lo < lh) {
                            int lm = (lo + lh) >>> 1;
                            if (split <= a[lm + lb]) {
                                lh = lm;
                            } else {
                                lo = lm + 1;
                            }
                        }
                        m = new Merger(this, a, w, lb + lh, ln - lh, rb + rh, rn - rh, (k + lh) + rh, g);
                        rn = rh;
                        ln = lh;
                        addToPendingCount(1);
                        m.fork();
                    } else if (ln <= g) {
                        break;
                    } else {
                        rh = rn;
                        lh = ln >>> 1;
                        split = a[lh + lb];
                        lo = 0;
                        while (lo < rh) {
                            int rm = (lo + rh) >>> 1;
                            if (split <= a[rm + rb]) {
                                rh = rm;
                            } else {
                                lo = rm + 1;
                            }
                        }
                        m = new Merger(this, a, w, lb + lh, ln - lh, rb + rh, rn - rh, (k + lh) + rh, g);
                        rn = rh;
                        ln = lh;
                        addToPendingCount(1);
                        m.fork();
                    }
                }
                int lf = lb + ln;
                int rf = rb + rn;
                while (true) {
                    k2 = k;
                    if (lb < lf && rb < rf) {
                        long t;
                        long al = a[lb];
                        long ar = a[rb];
                        if (al <= ar) {
                            lb++;
                            t = al;
                        } else {
                            rb++;
                            t = ar;
                        }
                        k = k2 + 1;
                        w[k2] = t;
                    }
                }
                if (rb < rf) {
                    System.arraycopy(a, rb, w, k2, rf - rb);
                } else if (lb < lf) {
                    System.arraycopy(a, lb, w, k2, lf - lb);
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

            Sorter(CountedCompleter<?> par, long[] a, long[] w, int base, int size, int wbase, int gran) {
                super(par);
                this.a = a;
                this.w = w;
                this.base = base;
                this.size = size;
                this.wbase = wbase;
                this.gran = gran;
            }

            public final void compute() {
                CountedCompleter<?> s = this;
                long[] a = this.a;
                long[] w = this.w;
                int b = this.base;
                int n = this.size;
                int wb = this.wbase;
                int g = this.gran;
                while (n > g) {
                    int h = n >>> 1;
                    int q = h >>> 1;
                    int u = h + q;
                    Relay fc = new Relay(new Merger(s, w, a, wb, h, wb + h, n - h, b, g));
                    CountedCompleter relay = new Relay(new Merger(fc, a, w, b + h, q, b + u, n - u, wb + h, g));
                    new Sorter(relay, a, w, b + u, n - u, wb + u, g).fork();
                    new Sorter(relay, a, w, b + h, q, wb + h, g).fork();
                    relay = new Relay(new Merger(fc, a, w, b, q, b + q, h - q, wb, g));
                    new Sorter(relay, a, w, b + q, h - q, wb + q, g).fork();
                    s = new EmptyCompleter(relay);
                    n = q;
                }
                DualPivotQuicksort.sort(a, b, (b + n) - 1, w, wb, n);
                s.tryComplete();
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

            Merger(CountedCompleter<?> par, T[] a, T[] w, int lbase, int lsize, int rbase, int rsize, int wbase, int gran, Comparator<? super T> comparator) {
                super(par);
                this.a = a;
                this.w = w;
                this.lbase = lbase;
                this.lsize = lsize;
                this.rbase = rbase;
                this.rsize = rsize;
                this.wbase = wbase;
                this.gran = gran;
                this.comparator = comparator;
            }

            public final void compute() {
                Comparator<? super T> c = this.comparator;
                Object a = this.a;
                Object w = this.w;
                int lb = this.lbase;
                int ln = this.lsize;
                int rb = this.rbase;
                int rn = this.rsize;
                int k = this.wbase;
                int g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0 || c == null) {
                    throw new IllegalStateException();
                }
                int k2;
                while (true) {
                    int lh;
                    int rh;
                    T split;
                    int lo;
                    Merger<T> m;
                    if (ln < rn) {
                        if (rn <= g) {
                            break;
                        }
                        lh = ln;
                        rh = rn >>> 1;
                        split = a[rh + rb];
                        lo = 0;
                        while (lo < lh) {
                            int lm = (lo + lh) >>> 1;
                            if (c.compare(split, a[lm + lb]) <= 0) {
                                lh = lm;
                            } else {
                                lo = lm + 1;
                            }
                        }
                        m = new Merger(this, a, w, lb + lh, ln - lh, rb + rh, rn - rh, (k + lh) + rh, g, c);
                        rn = rh;
                        ln = lh;
                        addToPendingCount(1);
                        m.fork();
                    } else if (ln <= g) {
                        break;
                    } else {
                        rh = rn;
                        lh = ln >>> 1;
                        split = a[lh + lb];
                        lo = 0;
                        while (lo < rh) {
                            int rm = (lo + rh) >>> 1;
                            if (c.compare(split, a[rm + rb]) <= 0) {
                                rh = rm;
                            } else {
                                lo = rm + 1;
                            }
                        }
                        m = new Merger(this, a, w, lb + lh, ln - lh, rb + rh, rn - rh, (k + lh) + rh, g, c);
                        rn = rh;
                        ln = lh;
                        addToPendingCount(1);
                        m.fork();
                    }
                }
                int lf = lb + ln;
                int rf = rb + rn;
                while (true) {
                    k2 = k;
                    if (lb < lf && rb < rf) {
                        T t;
                        T al = a[lb];
                        T ar = a[rb];
                        if (c.compare(al, ar) <= 0) {
                            lb++;
                            t = al;
                        } else {
                            rb++;
                            t = ar;
                        }
                        k = k2 + 1;
                        w[k2] = t;
                    }
                }
                if (rb < rf) {
                    System.arraycopy(a, rb, w, k2, rf - rb);
                } else if (lb < lf) {
                    System.arraycopy(a, lb, w, k2, lf - lb);
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

            Sorter(CountedCompleter<?> par, T[] a, T[] w, int base, int size, int wbase, int gran, Comparator<? super T> comparator) {
                super(par);
                this.a = a;
                this.w = w;
                this.base = base;
                this.size = size;
                this.wbase = wbase;
                this.gran = gran;
                this.comparator = comparator;
            }

            public final void compute() {
                CountedCompleter<?> s = this;
                Comparator<? super T> c = this.comparator;
                T[] a = this.a;
                T[] w = this.w;
                int b = this.base;
                int n = this.size;
                int wb = this.wbase;
                int g = this.gran;
                while (n > g) {
                    int h = n >>> 1;
                    int q = h >>> 1;
                    int u = h + q;
                    Relay fc = new Relay(new Merger(s, w, a, wb, h, wb + h, n - h, b, g, c));
                    CountedCompleter relay = new Relay(new Merger(fc, a, w, b + h, q, b + u, n - u, wb + h, g, c));
                    new Sorter(relay, a, w, b + u, n - u, wb + u, g, c).fork();
                    new Sorter(relay, a, w, b + h, q, wb + h, g, c).fork();
                    relay = new Relay(new Merger(fc, a, w, b, q, b + q, h - q, wb, g, c));
                    new Sorter(relay, a, w, b + q, h - q, wb + q, g, c).fork();
                    s = new EmptyCompleter(relay);
                    n = q;
                }
                TimSort.sort(a, b, b + n, c, w, wb, n);
                s.tryComplete();
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

            Merger(CountedCompleter<?> par, short[] a, short[] w, int lbase, int lsize, int rbase, int rsize, int wbase, int gran) {
                super(par);
                this.a = a;
                this.w = w;
                this.lbase = lbase;
                this.lsize = lsize;
                this.rbase = rbase;
                this.rsize = rsize;
                this.wbase = wbase;
                this.gran = gran;
            }

            public final void compute() {
                short[] a = this.a;
                short[] w = this.w;
                int lb = this.lbase;
                int ln = this.lsize;
                int rb = this.rbase;
                int rn = this.rsize;
                int k = this.wbase;
                int g = this.gran;
                if (a == null || w == null || lb < 0 || rb < 0 || k < 0) {
                    throw new IllegalStateException();
                }
                int k2;
                while (true) {
                    int lh;
                    int rh;
                    short split;
                    int lo;
                    Merger m;
                    if (ln < rn) {
                        if (rn <= g) {
                            break;
                        }
                        lh = ln;
                        rh = rn >>> 1;
                        split = a[rh + rb];
                        lo = 0;
                        while (lo < lh) {
                            int lm = (lo + lh) >>> 1;
                            if (split <= a[lm + lb]) {
                                lh = lm;
                            } else {
                                lo = lm + 1;
                            }
                        }
                        m = new Merger(this, a, w, lb + lh, ln - lh, rb + rh, rn - rh, (k + lh) + rh, g);
                        rn = rh;
                        ln = lh;
                        addToPendingCount(1);
                        m.fork();
                    } else if (ln <= g) {
                        break;
                    } else {
                        rh = rn;
                        lh = ln >>> 1;
                        split = a[lh + lb];
                        lo = 0;
                        while (lo < rh) {
                            int rm = (lo + rh) >>> 1;
                            if (split <= a[rm + rb]) {
                                rh = rm;
                            } else {
                                lo = rm + 1;
                            }
                        }
                        m = new Merger(this, a, w, lb + lh, ln - lh, rb + rh, rn - rh, (k + lh) + rh, g);
                        rn = rh;
                        ln = lh;
                        addToPendingCount(1);
                        m.fork();
                    }
                }
                int lf = lb + ln;
                int rf = rb + rn;
                while (true) {
                    k2 = k;
                    if (lb < lf && rb < rf) {
                        short t;
                        short al = a[lb];
                        short ar = a[rb];
                        if (al <= ar) {
                            lb++;
                            t = al;
                        } else {
                            rb++;
                            t = ar;
                        }
                        k = k2 + 1;
                        w[k2] = t;
                    }
                }
                if (rb < rf) {
                    System.arraycopy(a, rb, w, k2, rf - rb);
                } else if (lb < lf) {
                    System.arraycopy(a, lb, w, k2, lf - lb);
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

            Sorter(CountedCompleter<?> par, short[] a, short[] w, int base, int size, int wbase, int gran) {
                super(par);
                this.a = a;
                this.w = w;
                this.base = base;
                this.size = size;
                this.wbase = wbase;
                this.gran = gran;
            }

            public final void compute() {
                CountedCompleter<?> s = this;
                short[] a = this.a;
                short[] w = this.w;
                int b = this.base;
                int n = this.size;
                int wb = this.wbase;
                int g = this.gran;
                while (n > g) {
                    int h = n >>> 1;
                    int q = h >>> 1;
                    int u = h + q;
                    Relay fc = new Relay(new Merger(s, w, a, wb, h, wb + h, n - h, b, g));
                    CountedCompleter relay = new Relay(new Merger(fc, a, w, b + h, q, b + u, n - u, wb + h, g));
                    new Sorter(relay, a, w, b + u, n - u, wb + u, g).fork();
                    new Sorter(relay, a, w, b + h, q, wb + h, g).fork();
                    relay = new Relay(new Merger(fc, a, w, b, q, b + q, h - q, wb, g));
                    new Sorter(relay, a, w, b + q, h - q, wb + q, g).fork();
                    s = new EmptyCompleter(relay);
                    n = q;
                }
                DualPivotQuicksort.sort(a, b, (b + n) - 1, w, wb, n);
                s.tryComplete();
            }
        }

        FJShort() {
        }
    }

    static final class Relay extends CountedCompleter<Void> {
        static final long serialVersionUID = 2446542900576103244L;
        final CountedCompleter<?> task;

        Relay(CountedCompleter<?> task) {
            super(null, 1);
            this.task = task;
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
