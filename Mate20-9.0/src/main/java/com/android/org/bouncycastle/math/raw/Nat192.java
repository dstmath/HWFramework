package com.android.org.bouncycastle.math.raw;

import com.android.org.bouncycastle.util.Pack;
import java.math.BigInteger;

public abstract class Nat192 {
    private static final long M = 4294967295L;

    public static int add(int[] x, int[] y, int[] z) {
        long c = 0 + (((long) x[0]) & M) + (((long) y[0]) & M);
        z[0] = (int) c;
        long c2 = (c >>> 32) + (((long) x[1]) & M) + (((long) y[1]) & M);
        z[1] = (int) c2;
        long c3 = (c2 >>> 32) + (((long) x[2]) & M) + (((long) y[2]) & M);
        z[2] = (int) c3;
        long c4 = (c3 >>> 32) + (((long) x[3]) & M) + (((long) y[3]) & M);
        z[3] = (int) c4;
        long c5 = (c4 >>> 32) + (((long) x[4]) & M) + (((long) y[4]) & M);
        z[4] = (int) c5;
        long c6 = (c5 >>> 32) + (((long) x[5]) & M) + (((long) y[5]) & M);
        z[5] = (int) c6;
        return (int) (c6 >>> 32);
    }

    public static int addBothTo(int[] x, int[] y, int[] z) {
        long c = 0 + (((long) x[0]) & M) + (((long) y[0]) & M) + (((long) z[0]) & M);
        z[0] = (int) c;
        long c2 = (c >>> 32) + (((long) x[1]) & M) + (((long) y[1]) & M) + (((long) z[1]) & M);
        z[1] = (int) c2;
        long c3 = (c2 >>> 32) + (((long) x[2]) & M) + (((long) y[2]) & M) + (((long) z[2]) & M);
        z[2] = (int) c3;
        long c4 = (c3 >>> 32) + (((long) x[3]) & M) + (((long) y[3]) & M) + (((long) z[3]) & M);
        z[3] = (int) c4;
        long c5 = (c4 >>> 32) + (((long) x[4]) & M) + (((long) y[4]) & M) + (((long) z[4]) & M);
        z[4] = (int) c5;
        long c6 = (c5 >>> 32) + (((long) x[5]) & M) + (((long) y[5]) & M) + (((long) z[5]) & M);
        z[5] = (int) c6;
        return (int) (c6 >>> 32);
    }

    public static int addTo(int[] x, int[] z) {
        long c = 0 + (((long) x[0]) & M) + (((long) z[0]) & M);
        z[0] = (int) c;
        long c2 = (c >>> 32) + (((long) x[1]) & M) + (((long) z[1]) & M);
        z[1] = (int) c2;
        long c3 = (c2 >>> 32) + (((long) x[2]) & M) + (((long) z[2]) & M);
        z[2] = (int) c3;
        long c4 = (c3 >>> 32) + (((long) x[3]) & M) + (((long) z[3]) & M);
        z[3] = (int) c4;
        long c5 = (c4 >>> 32) + (((long) x[4]) & M) + (((long) z[4]) & M);
        z[4] = (int) c5;
        long c6 = (c5 >>> 32) + (((long) x[5]) & M) + (((long) z[5]) & M);
        z[5] = (int) c6;
        return (int) (c6 >>> 32);
    }

    public static int addTo(int[] x, int xOff, int[] z, int zOff, int cIn) {
        long c = (((long) cIn) & M) + (((long) x[xOff + 0]) & M) + (((long) z[zOff + 0]) & M);
        z[zOff + 0] = (int) c;
        long c2 = (c >>> 32) + (((long) x[xOff + 1]) & M) + (((long) z[zOff + 1]) & M);
        z[zOff + 1] = (int) c2;
        long c3 = (c2 >>> 32) + (((long) x[xOff + 2]) & M) + (((long) z[zOff + 2]) & M);
        z[zOff + 2] = (int) c3;
        long c4 = (c3 >>> 32) + (((long) x[xOff + 3]) & M) + (((long) z[zOff + 3]) & M);
        z[zOff + 3] = (int) c4;
        long c5 = (c4 >>> 32) + (((long) x[xOff + 4]) & M) + (((long) z[zOff + 4]) & M);
        z[zOff + 4] = (int) c5;
        long c6 = (c5 >>> 32) + (((long) x[xOff + 5]) & M) + (M & ((long) z[zOff + 5]));
        z[zOff + 5] = (int) c6;
        return (int) (c6 >>> 32);
    }

    public static int addToEachOther(int[] u, int uOff, int[] v, int vOff) {
        long c = 0 + (((long) u[uOff + 0]) & M) + (((long) v[vOff + 0]) & M);
        u[uOff + 0] = (int) c;
        v[vOff + 0] = (int) c;
        long c2 = (c >>> 32) + (((long) u[uOff + 1]) & M) + (((long) v[vOff + 1]) & M);
        u[uOff + 1] = (int) c2;
        v[vOff + 1] = (int) c2;
        long c3 = (c2 >>> 32) + (((long) u[uOff + 2]) & M) + (((long) v[vOff + 2]) & M);
        u[uOff + 2] = (int) c3;
        v[vOff + 2] = (int) c3;
        long c4 = (c3 >>> 32) + (((long) u[uOff + 3]) & M) + (((long) v[vOff + 3]) & M);
        u[uOff + 3] = (int) c4;
        v[vOff + 3] = (int) c4;
        long c5 = (c4 >>> 32) + (((long) u[uOff + 4]) & M) + (((long) v[vOff + 4]) & M);
        u[uOff + 4] = (int) c5;
        v[vOff + 4] = (int) c5;
        long c6 = (c5 >>> 32) + (((long) u[uOff + 5]) & M) + (((long) v[vOff + 5]) & M);
        u[uOff + 5] = (int) c6;
        v[vOff + 5] = (int) c6;
        return (int) (c6 >>> 32);
    }

    public static void copy(int[] x, int[] z) {
        z[0] = x[0];
        z[1] = x[1];
        z[2] = x[2];
        z[3] = x[3];
        z[4] = x[4];
        z[5] = x[5];
    }

    public static void copy64(long[] x, long[] z) {
        z[0] = x[0];
        z[1] = x[1];
        z[2] = x[2];
    }

    public static int[] create() {
        return new int[6];
    }

    public static long[] create64() {
        return new long[3];
    }

    public static int[] createExt() {
        return new int[12];
    }

    public static long[] createExt64() {
        return new long[6];
    }

    public static boolean diff(int[] x, int xOff, int[] y, int yOff, int[] z, int zOff) {
        boolean pos = gte(x, xOff, y, yOff);
        if (pos) {
            sub(x, xOff, y, yOff, z, zOff);
        } else {
            sub(y, yOff, x, xOff, z, zOff);
        }
        return pos;
    }

    public static boolean eq(int[] x, int[] y) {
        for (int i = 5; i >= 0; i--) {
            if (x[i] != y[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean eq64(long[] x, long[] y) {
        for (int i = 2; i >= 0; i--) {
            if (x[i] != y[i]) {
                return false;
            }
        }
        return true;
    }

    public static int[] fromBigInteger(BigInteger x) {
        if (x.signum() < 0 || x.bitLength() > 192) {
            throw new IllegalArgumentException();
        }
        int[] z = create();
        int i = 0;
        while (x.signum() != 0) {
            z[i] = x.intValue();
            x = x.shiftRight(32);
            i++;
        }
        return z;
    }

    public static long[] fromBigInteger64(BigInteger x) {
        if (x.signum() < 0 || x.bitLength() > 192) {
            throw new IllegalArgumentException();
        }
        long[] z = create64();
        int i = 0;
        while (x.signum() != 0) {
            z[i] = x.longValue();
            x = x.shiftRight(64);
            i++;
        }
        return z;
    }

    public static int getBit(int[] x, int bit) {
        if (bit == 0) {
            return x[0] & 1;
        }
        int w = bit >> 5;
        if (w < 0 || w >= 6) {
            return 0;
        }
        return (x[w] >>> (bit & 31)) & 1;
    }

    public static boolean gte(int[] x, int[] y) {
        for (int i = 5; i >= 0; i--) {
            int x_i = x[i] ^ Integer.MIN_VALUE;
            int y_i = Integer.MIN_VALUE ^ y[i];
            if (x_i < y_i) {
                return false;
            }
            if (x_i > y_i) {
                return true;
            }
        }
        return true;
    }

    public static boolean gte(int[] x, int xOff, int[] y, int yOff) {
        for (int i = 5; i >= 0; i--) {
            int x_i = x[xOff + i] ^ Integer.MIN_VALUE;
            int y_i = Integer.MIN_VALUE ^ y[yOff + i];
            if (x_i < y_i) {
                return false;
            }
            if (x_i > y_i) {
                return true;
            }
        }
        return true;
    }

    public static boolean isOne(int[] x) {
        if (x[0] != 1) {
            return false;
        }
        for (int i = 1; i < 6; i++) {
            if (x[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean isOne64(long[] x) {
        if (x[0] != 1) {
            return false;
        }
        for (int i = 1; i < 3; i++) {
            if (x[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean isZero(int[] x) {
        for (int i = 0; i < 6; i++) {
            if (x[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean isZero64(long[] x) {
        for (int i = 0; i < 3; i++) {
            if (x[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public static void mul(int[] x, int[] y, int[] zz) {
        long y_0 = ((long) y[0]) & M;
        long y_1 = ((long) y[1]) & M;
        long y_2 = ((long) y[2]) & M;
        long y_3 = ((long) y[3]) & M;
        long y_12 = y_1;
        long y_4 = ((long) y[4]) & M;
        long y_5 = ((long) y[5]) & M;
        long y_02 = y_0;
        long x_0 = ((long) x[0]) & M;
        long c = 0 + (x_0 * y_02);
        zz[0] = (int) c;
        long c2 = (c >>> 32) + (x_0 * y_12);
        int i = 1;
        zz[1] = (int) c2;
        long c3 = (c2 >>> 32) + (x_0 * y_2);
        zz[2] = (int) c3;
        long c4 = (c3 >>> 32) + (x_0 * y_3);
        zz[3] = (int) c4;
        long c5 = (c4 >>> 32) + (x_0 * y_4);
        zz[4] = (int) c5;
        long c6 = (c5 >>> 32) + (x_0 * y_5);
        zz[5] = (int) c6;
        int i2 = 6;
        zz[6] = (int) (c6 >>> 32);
        while (true) {
            int i3 = i;
            if (i3 < i2) {
                long x_i = ((long) x[i3]) & M;
                long c7 = 0 + (x_i * y_02) + (((long) zz[i3 + 0]) & M);
                zz[i3 + 0] = (int) c7;
                long y_52 = y_5;
                long c8 = (c7 >>> 32) + (x_i * y_12) + (((long) zz[i3 + 1]) & M);
                zz[i3 + 1] = (int) c8;
                long c9 = (c8 >>> 32) + (x_i * y_2) + (((long) zz[i3 + 2]) & M);
                zz[i3 + 2] = (int) c9;
                long c10 = (c9 >>> 32) + (x_i * y_3) + (((long) zz[i3 + 3]) & M);
                zz[i3 + 3] = (int) c10;
                long c11 = (c10 >>> 32) + (x_i * y_4) + (((long) zz[i3 + 4]) & M);
                zz[i3 + 4] = (int) c11;
                long c12 = (c11 >>> 32) + (x_i * y_52) + (((long) zz[i3 + 5]) & M);
                zz[i3 + 5] = (int) c12;
                zz[i3 + 6] = (int) (c12 >>> 32);
                i = i3 + 1;
                y_5 = y_52;
                i2 = 6;
            } else {
                return;
            }
        }
    }

    public static void mul(int[] x, int xOff, int[] y, int yOff, int[] zz, int zzOff) {
        long y_0 = ((long) y[yOff + 0]) & M;
        long y_1 = ((long) y[yOff + 1]) & M;
        long y_2 = ((long) y[yOff + 2]) & M;
        long y_3 = ((long) y[yOff + 3]) & M;
        long y_4 = ((long) y[yOff + 4]) & M;
        long y_5 = ((long) y[yOff + 5]) & M;
        long x_0 = ((long) x[xOff + 0]) & M;
        long c = 0 + (x_0 * y_0);
        long y_02 = y_0;
        zz[zzOff + 0] = (int) c;
        long c2 = (c >>> 32) + (x_0 * y_1);
        zz[zzOff + 1] = (int) c2;
        long c3 = (c2 >>> 32) + (x_0 * y_2);
        zz[zzOff + 2] = (int) c3;
        long c4 = (c3 >>> 32) + (x_0 * y_3);
        zz[zzOff + 3] = (int) c4;
        long c5 = (c4 >>> 32) + (x_0 * y_4);
        zz[zzOff + 4] = (int) c5;
        long c6 = (c5 >>> 32) + (x_0 * y_5);
        zz[zzOff + 5] = (int) c6;
        zz[zzOff + 6] = (int) (c6 >>> 32);
        int i = 1;
        int zzOff2 = zzOff;
        while (i < 6) {
            zzOff2++;
            long x_i = ((long) x[xOff + i]) & M;
            long c7 = 0 + (x_i * y_02) + (((long) zz[zzOff2 + 0]) & M);
            zz[zzOff2 + 0] = (int) c7;
            long c8 = (c7 >>> 32) + (x_i * y_1) + (((long) zz[zzOff2 + 1]) & M);
            zz[zzOff2 + 1] = (int) c8;
            long c9 = (c8 >>> 32) + (x_i * y_2) + (((long) zz[zzOff2 + 2]) & M);
            zz[zzOff2 + 2] = (int) c9;
            long c10 = (c9 >>> 32) + (x_i * y_3) + (((long) zz[zzOff2 + 3]) & M);
            zz[zzOff2 + 3] = (int) c10;
            long c11 = (c10 >>> 32) + (x_i * y_4) + (((long) zz[zzOff2 + 4]) & M);
            zz[zzOff2 + 4] = (int) c11;
            long c12 = (c11 >>> 32) + (x_i * y_5) + (((long) zz[zzOff2 + 5]) & M);
            zz[zzOff2 + 5] = (int) c12;
            zz[zzOff2 + 6] = (int) (c12 >>> 32);
            i++;
            y_1 = y_1;
        }
    }

    public static int mulAddTo(int[] x, int[] y, int[] zz) {
        long y_0 = ((long) y[0]) & M;
        long y_1 = ((long) y[1]) & M;
        long y_2 = ((long) y[2]) & M;
        long y_3 = ((long) y[3]) & M;
        long y_4 = ((long) y[4]) & M;
        long y_02 = y_0;
        long y_5 = ((long) y[5]) & M;
        long zc = 0;
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < 6) {
                long y_52 = y_5;
                long y_53 = ((long) x[i2]) & M;
                long y_42 = y_4;
                long c = 0 + (y_53 * y_02) + (((long) zz[i2 + 0]) & M);
                zz[i2 + 0] = (int) c;
                long y_12 = y_1;
                long c2 = (c >>> 32) + (y_53 * y_1) + (((long) zz[i2 + 1]) & M);
                zz[i2 + 1] = (int) c2;
                long c3 = (c2 >>> 32) + (y_53 * y_2) + (((long) zz[i2 + 2]) & M);
                zz[i2 + 2] = (int) c3;
                long c4 = (c3 >>> 32) + (y_53 * y_3) + (((long) zz[i2 + 3]) & M);
                zz[i2 + 3] = (int) c4;
                long c5 = (c4 >>> 32) + (y_53 * y_42) + (((long) zz[i2 + 4]) & M);
                zz[i2 + 4] = (int) c5;
                long c6 = (c5 >>> 32) + (y_53 * y_52) + (((long) zz[i2 + 5]) & M);
                zz[i2 + 5] = (int) c6;
                long c7 = (c6 >>> 32) + zc + (((long) zz[i2 + 6]) & M);
                zz[i2 + 6] = (int) c7;
                zc = c7 >>> 32;
                i = i2 + 1;
                y_5 = y_52;
                y_4 = y_42;
                y_1 = y_12;
                y_2 = y_2;
            } else {
                long j = y_1;
                long j2 = y_2;
                long j3 = y_4;
                return (int) zc;
            }
        }
    }

    public static int mulAddTo(int[] x, int xOff, int[] y, int yOff, int[] zz, int zzOff) {
        long y_0 = ((long) y[yOff + 0]) & M;
        long y_1 = ((long) y[yOff + 1]) & M;
        long y_2 = ((long) y[yOff + 2]) & M;
        long y_3 = ((long) y[yOff + 3]) & M;
        long y_4 = ((long) y[yOff + 4]) & M;
        long y_5 = ((long) y[yOff + 5]) & M;
        int i = 0;
        long zc = 0;
        int zzOff2 = zzOff;
        while (i < 6) {
            long y_52 = y_5;
            long x_i = ((long) x[xOff + i]) & M;
            long c = 0 + (x_i * y_0) + (((long) zz[zzOff2 + 0]) & M);
            zz[zzOff2 + 0] = (int) c;
            long c2 = (c >>> 32) + (x_i * y_1) + (((long) zz[zzOff2 + 1]) & M);
            zz[zzOff2 + 1] = (int) c2;
            long c3 = (c2 >>> 32) + (x_i * y_2) + (((long) zz[zzOff2 + 2]) & M);
            zz[zzOff2 + 2] = (int) c3;
            long c4 = (c3 >>> 32) + (x_i * y_3) + (((long) zz[zzOff2 + 3]) & M);
            zz[zzOff2 + 3] = (int) c4;
            long c5 = (c4 >>> 32) + (x_i * y_4) + (((long) zz[zzOff2 + 4]) & M);
            zz[zzOff2 + 4] = (int) c5;
            long c6 = (c5 >>> 32) + (x_i * y_52) + (((long) zz[zzOff2 + 5]) & M);
            zz[zzOff2 + 5] = (int) c6;
            long c7 = (c6 >>> 32) + zc + (((long) zz[zzOff2 + 6]) & M);
            zz[zzOff2 + 6] = (int) c7;
            zc = c7 >>> 32;
            zzOff2++;
            i++;
            y_5 = y_52;
            y_0 = y_0;
            y_1 = y_1;
            y_2 = y_2;
        }
        long j = y_0;
        long j2 = y_1;
        long j3 = y_2;
        return (int) zc;
    }

    public static long mul33Add(int w, int[] x, int xOff, int[] y, int yOff, int[] z, int zOff) {
        long wVal = ((long) w) & M;
        long x0 = ((long) x[xOff + 0]) & M;
        long c = 0 + (wVal * x0) + (((long) y[yOff + 0]) & M);
        z[zOff + 0] = (int) c;
        long x1 = ((long) x[xOff + 1]) & M;
        long j = x0;
        long c2 = (c >>> 32) + (wVal * x1) + x0 + (((long) y[yOff + 1]) & M);
        z[zOff + 1] = (int) c2;
        long x2 = ((long) x[xOff + 2]) & M;
        long j2 = x1;
        long c3 = (c2 >>> 32) + (wVal * x2) + x1 + (((long) y[yOff + 2]) & M);
        z[zOff + 2] = (int) c3;
        long x3 = ((long) x[xOff + 3]) & M;
        long j3 = x2;
        long c4 = (c3 >>> 32) + (wVal * x3) + x2 + (((long) y[yOff + 3]) & M);
        z[zOff + 3] = (int) c4;
        long x4 = ((long) x[xOff + 4]) & M;
        long j4 = x3;
        long c5 = (c4 >>> 32) + (wVal * x4) + x3 + (((long) y[yOff + 4]) & M);
        z[zOff + 4] = (int) c5;
        long x5 = ((long) x[xOff + 5]) & M;
        long c6 = (c5 >>> 32) + (wVal * x5) + x4 + (((long) y[yOff + 5]) & M);
        z[zOff + 5] = (int) c6;
        return (c6 >>> 32) + x5;
    }

    public static int mulWordAddExt(int x, int[] yy, int yyOff, int[] zz, int zzOff) {
        long xVal = ((long) x) & M;
        long c = 0 + ((((long) yy[yyOff + 0]) & M) * xVal) + (((long) zz[zzOff + 0]) & M);
        zz[zzOff + 0] = (int) c;
        long c2 = (c >>> 32) + ((((long) yy[yyOff + 1]) & M) * xVal) + (((long) zz[zzOff + 1]) & M);
        zz[zzOff + 1] = (int) c2;
        long c3 = (c2 >>> 32) + ((((long) yy[yyOff + 2]) & M) * xVal) + (((long) zz[zzOff + 2]) & M);
        zz[zzOff + 2] = (int) c3;
        long c4 = (c3 >>> 32) + ((((long) yy[yyOff + 3]) & M) * xVal) + (((long) zz[zzOff + 3]) & M);
        zz[zzOff + 3] = (int) c4;
        long c5 = (c4 >>> 32) + ((((long) yy[yyOff + 4]) & M) * xVal) + (((long) zz[zzOff + 4]) & M);
        zz[zzOff + 4] = (int) c5;
        long c6 = (c5 >>> 32) + ((((long) yy[yyOff + 5]) & M) * xVal) + (M & ((long) zz[zzOff + 5]));
        zz[zzOff + 5] = (int) c6;
        return (int) (c6 >>> 32);
    }

    public static int mul33DWordAdd(int x, long y, int[] z, int zOff) {
        int[] iArr = z;
        int i = zOff;
        long xVal = ((long) x) & M;
        long y00 = y & M;
        long c = 0 + (xVal * y00) + (((long) iArr[i + 0]) & M);
        iArr[i + 0] = (int) c;
        long y01 = y >>> 32;
        long c2 = (c >>> 32) + (xVal * y01) + y00 + (((long) iArr[i + 1]) & M);
        iArr[i + 1] = (int) c2;
        long c3 = (c2 >>> 32) + (((long) iArr[i + 2]) & M) + y01;
        iArr[i + 2] = (int) c3;
        long c4 = (c3 >>> 32) + (((long) iArr[i + 3]) & M);
        iArr[i + 3] = (int) c4;
        if ((c4 >>> 32) == 0) {
            return 0;
        }
        return Nat.incAt(6, iArr, i, 4);
    }

    public static int mul33WordAdd(int x, int y, int[] z, int zOff) {
        long xVal = ((long) x) & M;
        long yVal = ((long) y) & M;
        long c = 0 + (yVal * xVal) + (((long) z[zOff + 0]) & M);
        z[zOff + 0] = (int) c;
        long c2 = (c >>> 32) + (((long) z[zOff + 1]) & M) + yVal;
        z[zOff + 1] = (int) c2;
        long c3 = (c2 >>> 32) + (M & ((long) z[zOff + 2]));
        z[zOff + 2] = (int) c3;
        if ((c3 >>> 32) == 0) {
            return 0;
        }
        return Nat.incAt(6, z, zOff, 3);
    }

    public static int mulWordDwordAdd(int x, long y, int[] z, int zOff) {
        long xVal = ((long) x) & M;
        long c = 0 + ((y & M) * xVal) + (((long) z[zOff + 0]) & M);
        z[zOff + 0] = (int) c;
        long c2 = (c >>> 32) + ((y >>> 32) * xVal) + (((long) z[zOff + 1]) & M);
        z[zOff + 1] = (int) c2;
        long c3 = (c2 >>> 32) + (M & ((long) z[zOff + 2]));
        z[zOff + 2] = (int) c3;
        if ((c3 >>> 32) == 0) {
            return 0;
        }
        return Nat.incAt(6, z, zOff, 3);
    }

    public static int mulWord(int x, int[] y, int[] z, int zOff) {
        long c = 0;
        long xVal = ((long) x) & M;
        int i = 0;
        do {
            long c2 = c + ((((long) y[i]) & M) * xVal);
            z[zOff + i] = (int) c2;
            c = c2 >>> 32;
            i++;
        } while (i < 6);
        return (int) c;
    }

    public static void square(int[] x, int[] zz) {
        long j = M;
        long x_0 = ((long) x[0]) & M;
        int c = 0;
        int i = 5;
        int j2 = 12;
        while (true) {
            int i2 = i - 1;
            long xVal = ((long) x[i]) & j;
            long p = xVal * xVal;
            int j3 = j2 - 1;
            zz[j3] = ((int) (p >>> 33)) | (c << 31);
            j2 = j3 - 1;
            long x_02 = x_0;
            zz[j2] = (int) (p >>> 1);
            c = (int) p;
            if (i2 <= 0) {
                long p2 = x_02 * x_02;
                long zz_1 = (((long) (c << 31)) & M) | (p2 >>> 33);
                zz[0] = (int) p2;
                long x_1 = ((long) x[1]) & M;
                long zz_2 = ((long) zz[2]) & M;
                long zz_12 = zz_1 + (x_1 * x_02);
                int w = (int) zz_12;
                zz[1] = (w << 1) | (((int) (p2 >>> 32)) & 1);
                long x_2 = ((long) x[2]) & M;
                long x_12 = x_1;
                long zz_3 = ((long) zz[3]) & M;
                long zz_4 = ((long) zz[4]) & M;
                long zz_22 = zz_2 + (zz_12 >>> 32) + (x_2 * x_02);
                int w2 = (int) zz_22;
                zz[2] = (w2 << 1) | (w >>> 31);
                long zz_32 = zz_3 + (zz_22 >>> 32) + (x_2 * x_12);
                long zz_42 = zz_4 + (zz_32 >>> 32);
                long zz_33 = zz_32 & M;
                long j4 = zz_12;
                long x_3 = ((long) x[3]) & M;
                long zz_5 = (((long) zz[5]) & M) + (zz_42 >>> 32);
                long zz_43 = zz_42 & M;
                long j5 = zz_22;
                long zz_52 = zz_5 & M;
                long zz_34 = zz_33 + (x_3 * x_02);
                int w3 = (int) zz_34;
                zz[3] = (w3 << 1) | (w2 >>> 31);
                int c2 = w3 >>> 31;
                long zz_44 = zz_43 + (zz_34 >>> 32) + (x_3 * x_12);
                long zz_53 = zz_52 + (zz_44 >>> 32) + (x_3 * x_2);
                long zz_45 = zz_44 & M;
                long zz_6 = (((long) zz[6]) & M) + (zz_5 >>> 32) + (zz_53 >>> 32);
                long zz_54 = zz_53 & M;
                int i3 = w3;
                long j6 = zz_34;
                long x_4 = ((long) x[4]) & M;
                long x_22 = x_2;
                long zz_7 = (((long) zz[7]) & M) + (zz_6 >>> 32);
                long zz_62 = zz_6 & M;
                long x_32 = x_3;
                long zz_8 = (((long) zz[8]) & M) + (zz_7 >>> 32);
                long zz_72 = zz_7 & M;
                long zz_46 = zz_45 + (x_4 * x_02);
                int w4 = (int) zz_46;
                zz[4] = (w4 << 1) | c2;
                int c3 = w4 >>> 31;
                long zz_55 = zz_54 + (zz_46 >>> 32) + (x_4 * x_12);
                long zz_63 = zz_62 + (zz_55 >>> 32) + (x_4 * x_22);
                long zz_56 = zz_55 & M;
                long zz_73 = zz_72 + (zz_63 >>> 32) + (x_4 * x_32);
                long zz_64 = zz_63 & M;
                long zz_82 = zz_8 + (zz_73 >>> 32);
                long zz_74 = zz_73 & M;
                int i4 = w4;
                long j7 = zz_46;
                long x_5 = ((long) x[5]) & M;
                long zz_9 = (((long) zz[9]) & M) + (zz_82 >>> 32);
                long zz_83 = zz_82 & M;
                long x_42 = x_4;
                long zz_10 = (((long) zz[10]) & M) + (zz_9 >>> 32);
                long zz_92 = zz_9 & M;
                long zz_57 = zz_56 + (x_5 * x_02);
                int w5 = (int) zz_57;
                zz[5] = (w5 << 1) | c3;
                int c4 = w5 >>> 31;
                long zz_65 = zz_64 + (zz_57 >>> 32) + (x_5 * x_12);
                long zz_75 = zz_74 + (zz_65 >>> 32) + (x_5 * x_22);
                long zz_84 = zz_83 + (zz_75 >>> 32) + (x_5 * x_32);
                long zz_93 = zz_92 + (zz_84 >>> 32) + (x_5 * x_42);
                long zz_102 = zz_10 + (zz_93 >>> 32);
                int w6 = (int) zz_65;
                zz[6] = (w6 << 1) | c4;
                int c5 = w6 >>> 31;
                int w7 = (int) zz_75;
                zz[7] = (w7 << 1) | c5;
                int c6 = w7 >>> 31;
                int w8 = (int) zz_84;
                zz[8] = (w8 << 1) | c6;
                int c7 = w8 >>> 31;
                int w9 = (int) zz_93;
                zz[9] = (w9 << 1) | c7;
                int c8 = w9 >>> 31;
                int w10 = (int) zz_102;
                zz[10] = (w10 << 1) | c8;
                long j8 = zz_93;
                zz[11] = ((zz[11] + ((int) (zz_102 >>> 32))) << 1) | (w10 >>> 31);
                return;
            }
            i = i2;
            j = 4294967295L;
            x_0 = x_02;
        }
    }

    public static void square(int[] x, int xOff, int[] zz, int zzOff) {
        long j = M;
        long x_0 = ((long) x[xOff + 0]) & M;
        int c = 0;
        int i = 5;
        int j2 = 12;
        while (true) {
            int i2 = i - 1;
            long xVal = ((long) x[xOff + i]) & j;
            long p = xVal * xVal;
            int j3 = j2 - 1;
            zz[zzOff + j3] = ((int) (p >>> 33)) | (c << 31);
            j2 = j3 - 1;
            int i3 = c;
            zz[zzOff + j2] = (int) (p >>> 1);
            c = (int) p;
            if (i2 <= 0) {
                long p2 = x_0 * x_0;
                zz[zzOff + 0] = (int) p2;
                int i4 = c;
                int c2 = 1 & ((int) (p2 >>> 32));
                long x_1 = ((long) x[xOff + 1]) & M;
                long zz_2 = ((long) zz[zzOff + 2]) & M;
                long zz_1 = ((((long) (c << 31)) & M) | (p2 >>> 33)) + (x_1 * x_0);
                int w = (int) zz_1;
                zz[zzOff + 1] = (w << 1) | c2;
                long x_2 = ((long) x[xOff + 2]) & M;
                int i5 = w;
                long zz_3 = ((long) zz[zzOff + 3]) & M;
                long j4 = zz_1;
                long zz_4 = ((long) zz[zzOff + 4]) & M;
                long zz_22 = zz_2 + (zz_1 >>> 32) + (x_2 * x_0);
                long x_02 = x_0;
                int w2 = (int) zz_22;
                zz[zzOff + 2] = (w2 << 1) | (w >>> 31);
                int c3 = w2 >>> 31;
                long zz_32 = zz_3 + (zz_22 >>> 32) + (x_2 * x_1);
                long zz_42 = zz_4 + (zz_32 >>> 32);
                long zz_33 = zz_32 & M;
                int i6 = w2;
                long j5 = zz_22;
                long x_3 = ((long) x[xOff + 3]) & M;
                long zz_5 = (((long) zz[zzOff + 5]) & M) + (zz_42 >>> 32);
                long zz_43 = zz_42 & M;
                long x_22 = x_2;
                long zz_52 = zz_5 & M;
                long zz_34 = zz_33 + (x_3 * x_02);
                int w3 = (int) zz_34;
                zz[zzOff + 3] = (w3 << 1) | c3;
                int c4 = w3 >>> 31;
                long zz_44 = zz_43 + (zz_34 >>> 32) + (x_3 * x_1);
                long zz_53 = zz_52 + (zz_44 >>> 32) + (x_3 * x_22);
                long zz_45 = zz_44 & M;
                long zz_6 = (((long) zz[zzOff + 6]) & M) + (zz_5 >>> 32) + (zz_53 >>> 32);
                long zz_54 = zz_53 & M;
                int i7 = w3;
                long j6 = zz_34;
                long zz_35 = ((long) x[xOff + 4]) & M;
                long x_32 = x_3;
                long zz_7 = (((long) zz[zzOff + 7]) & M) + (zz_6 >>> 32);
                long zz_62 = zz_6 & M;
                long zz_8 = (((long) zz[zzOff + 8]) & M) + (zz_7 >>> 32);
                long zz_72 = zz_7 & M;
                long zz_46 = zz_45 + (zz_35 * x_02);
                int w4 = (int) zz_46;
                zz[zzOff + 4] = (w4 << 1) | c4;
                int c5 = w4 >>> 31;
                long zz_55 = zz_54 + (zz_46 >>> 32) + (zz_35 * x_1);
                long zz_63 = zz_62 + (zz_55 >>> 32) + (zz_35 * x_22);
                long zz_56 = zz_55 & M;
                long zz_73 = zz_72 + (zz_63 >>> 32) + (zz_35 * x_32);
                long zz_64 = zz_63 & M;
                long zz_82 = zz_8 + (zz_73 >>> 32);
                long zz_74 = zz_73 & M;
                int i8 = w4;
                long j7 = zz_46;
                long x_5 = ((long) x[xOff + 5]) & M;
                long x_4 = zz_35;
                long zz_9 = (((long) zz[zzOff + 9]) & M) + (zz_82 >>> 32);
                long zz_92 = zz_9 & M;
                long zz_57 = zz_56 + (x_5 * x_02);
                int w5 = (int) zz_57;
                zz[zzOff + 5] = (w5 << 1) | c5;
                int c6 = w5 >>> 31;
                long j8 = zz_57;
                long zz_65 = zz_64 + (zz_57 >>> 32) + (x_5 * x_1);
                long zz_75 = zz_74 + (zz_65 >>> 32) + (x_5 * x_22);
                long j9 = x_1;
                long x_12 = (zz_82 & M) + (zz_75 >>> 32) + (x_5 * x_32);
                long zz_93 = zz_92 + (x_12 >>> 32) + (x_5 * x_4);
                long zz_10 = (((long) zz[zzOff + 10]) & M) + (zz_9 >>> 32) + (zz_93 >>> 32);
                int w6 = (int) zz_65;
                zz[zzOff + 6] = (w6 << 1) | c6;
                int c7 = w6 >>> 31;
                int w7 = (int) zz_75;
                zz[zzOff + 7] = (w7 << 1) | c7;
                int c8 = w7 >>> 31;
                int w8 = (int) x_12;
                zz[zzOff + 8] = (w8 << 1) | c8;
                int c9 = w8 >>> 31;
                int w9 = (int) zz_93;
                zz[zzOff + 9] = (w9 << 1) | c9;
                int c10 = w9 >>> 31;
                int w10 = (int) zz_10;
                zz[zzOff + 10] = (w10 << 1) | c10;
                long j10 = zz_65;
                zz[zzOff + 11] = ((zz[zzOff + 11] + ((int) (zz_10 >>> 32))) << 1) | (w10 >>> 31);
                return;
            }
            int i9 = c;
            i = i2;
            j = 4294967295L;
        }
    }

    public static int sub(int[] x, int[] y, int[] z) {
        long c = 0 + ((((long) x[0]) & M) - (((long) y[0]) & M));
        z[0] = (int) c;
        long c2 = (c >> 32) + ((((long) x[1]) & M) - (((long) y[1]) & M));
        z[1] = (int) c2;
        long c3 = (c2 >> 32) + ((((long) x[2]) & M) - (((long) y[2]) & M));
        z[2] = (int) c3;
        long c4 = (c3 >> 32) + ((((long) x[3]) & M) - (((long) y[3]) & M));
        z[3] = (int) c4;
        long c5 = (c4 >> 32) + ((((long) x[4]) & M) - (((long) y[4]) & M));
        z[4] = (int) c5;
        long c6 = (c5 >> 32) + ((((long) x[5]) & M) - (((long) y[5]) & M));
        z[5] = (int) c6;
        return (int) (c6 >> 32);
    }

    public static int sub(int[] x, int xOff, int[] y, int yOff, int[] z, int zOff) {
        long c = 0 + ((((long) x[xOff + 0]) & M) - (((long) y[yOff + 0]) & M));
        z[zOff + 0] = (int) c;
        long c2 = (c >> 32) + ((((long) x[xOff + 1]) & M) - (((long) y[yOff + 1]) & M));
        z[zOff + 1] = (int) c2;
        long c3 = (c2 >> 32) + ((((long) x[xOff + 2]) & M) - (((long) y[yOff + 2]) & M));
        z[zOff + 2] = (int) c3;
        long c4 = (c3 >> 32) + ((((long) x[xOff + 3]) & M) - (((long) y[yOff + 3]) & M));
        z[zOff + 3] = (int) c4;
        long c5 = (c4 >> 32) + ((((long) x[xOff + 4]) & M) - (((long) y[yOff + 4]) & M));
        z[zOff + 4] = (int) c5;
        long c6 = (c5 >> 32) + ((((long) x[xOff + 5]) & M) - (((long) y[yOff + 5]) & M));
        z[zOff + 5] = (int) c6;
        return (int) (c6 >> 32);
    }

    public static int subBothFrom(int[] x, int[] y, int[] z) {
        long c = 0 + (((((long) z[0]) & M) - (((long) x[0]) & M)) - (((long) y[0]) & M));
        z[0] = (int) c;
        long c2 = (c >> 32) + (((((long) z[1]) & M) - (((long) x[1]) & M)) - (((long) y[1]) & M));
        z[1] = (int) c2;
        long c3 = (c2 >> 32) + (((((long) z[2]) & M) - (((long) x[2]) & M)) - (((long) y[2]) & M));
        z[2] = (int) c3;
        long c4 = (c3 >> 32) + (((((long) z[3]) & M) - (((long) x[3]) & M)) - (((long) y[3]) & M));
        z[3] = (int) c4;
        long c5 = (c4 >> 32) + (((((long) z[4]) & M) - (((long) x[4]) & M)) - (((long) y[4]) & M));
        z[4] = (int) c5;
        long c6 = (c5 >> 32) + (((((long) z[5]) & M) - (((long) x[5]) & M)) - (((long) y[5]) & M));
        z[5] = (int) c6;
        return (int) (c6 >> 32);
    }

    public static int subFrom(int[] x, int[] z) {
        long c = 0 + ((((long) z[0]) & M) - (((long) x[0]) & M));
        z[0] = (int) c;
        long c2 = (c >> 32) + ((((long) z[1]) & M) - (((long) x[1]) & M));
        z[1] = (int) c2;
        long c3 = (c2 >> 32) + ((((long) z[2]) & M) - (((long) x[2]) & M));
        z[2] = (int) c3;
        long c4 = (c3 >> 32) + ((((long) z[3]) & M) - (((long) x[3]) & M));
        z[3] = (int) c4;
        long c5 = (c4 >> 32) + ((((long) z[4]) & M) - (((long) x[4]) & M));
        z[4] = (int) c5;
        long c6 = (c5 >> 32) + ((((long) z[5]) & M) - (((long) x[5]) & M));
        z[5] = (int) c6;
        return (int) (c6 >> 32);
    }

    public static int subFrom(int[] x, int xOff, int[] z, int zOff) {
        long c = 0 + ((((long) z[zOff + 0]) & M) - (((long) x[xOff + 0]) & M));
        z[zOff + 0] = (int) c;
        long c2 = (c >> 32) + ((((long) z[zOff + 1]) & M) - (((long) x[xOff + 1]) & M));
        z[zOff + 1] = (int) c2;
        long c3 = (c2 >> 32) + ((((long) z[zOff + 2]) & M) - (((long) x[xOff + 2]) & M));
        z[zOff + 2] = (int) c3;
        long c4 = (c3 >> 32) + ((((long) z[zOff + 3]) & M) - (((long) x[xOff + 3]) & M));
        z[zOff + 3] = (int) c4;
        long c5 = (c4 >> 32) + ((((long) z[zOff + 4]) & M) - (((long) x[xOff + 4]) & M));
        z[zOff + 4] = (int) c5;
        long c6 = (c5 >> 32) + ((((long) z[zOff + 5]) & M) - (((long) x[xOff + 5]) & M));
        z[zOff + 5] = (int) c6;
        return (int) (c6 >> 32);
    }

    public static BigInteger toBigInteger(int[] x) {
        byte[] bs = new byte[24];
        for (int i = 0; i < 6; i++) {
            int x_i = x[i];
            if (x_i != 0) {
                Pack.intToBigEndian(x_i, bs, (5 - i) << 2);
            }
        }
        return new BigInteger(1, bs);
    }

    public static BigInteger toBigInteger64(long[] x) {
        byte[] bs = new byte[24];
        for (int i = 0; i < 3; i++) {
            long x_i = x[i];
            if (x_i != 0) {
                Pack.longToBigEndian(x_i, bs, (2 - i) << 3);
            }
        }
        return new BigInteger(1, bs);
    }

    public static void zero(int[] z) {
        z[0] = 0;
        z[1] = 0;
        z[2] = 0;
        z[3] = 0;
        z[4] = 0;
        z[5] = 0;
    }
}
