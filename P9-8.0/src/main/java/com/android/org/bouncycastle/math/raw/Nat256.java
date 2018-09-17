package com.android.org.bouncycastle.math.raw;

import com.android.org.bouncycastle.util.Pack;
import java.math.BigInteger;

public abstract class Nat256 {
    private static final long M = 4294967295L;

    public static int add(int[] x, int[] y, int[] z) {
        long c = 0 + ((((long) x[0]) & M) + (((long) y[0]) & M));
        z[0] = (int) c;
        c = (c >>> 32) + ((((long) x[1]) & M) + (((long) y[1]) & M));
        z[1] = (int) c;
        c = (c >>> 32) + ((((long) x[2]) & M) + (((long) y[2]) & M));
        z[2] = (int) c;
        c = (c >>> 32) + ((((long) x[3]) & M) + (((long) y[3]) & M));
        z[3] = (int) c;
        c = (c >>> 32) + ((((long) x[4]) & M) + (((long) y[4]) & M));
        z[4] = (int) c;
        c = (c >>> 32) + ((((long) x[5]) & M) + (((long) y[5]) & M));
        z[5] = (int) c;
        c = (c >>> 32) + ((((long) x[6]) & M) + (((long) y[6]) & M));
        z[6] = (int) c;
        c = (c >>> 32) + ((((long) x[7]) & M) + (((long) y[7]) & M));
        z[7] = (int) c;
        return (int) (c >>> 32);
    }

    public static int add(int[] x, int xOff, int[] y, int yOff, int[] z, int zOff) {
        long c = 0 + ((((long) x[xOff + 0]) & M) + (((long) y[yOff + 0]) & M));
        z[zOff + 0] = (int) c;
        c = (c >>> 32) + ((((long) x[xOff + 1]) & M) + (((long) y[yOff + 1]) & M));
        z[zOff + 1] = (int) c;
        c = (c >>> 32) + ((((long) x[xOff + 2]) & M) + (((long) y[yOff + 2]) & M));
        z[zOff + 2] = (int) c;
        c = (c >>> 32) + ((((long) x[xOff + 3]) & M) + (((long) y[yOff + 3]) & M));
        z[zOff + 3] = (int) c;
        c = (c >>> 32) + ((((long) x[xOff + 4]) & M) + (((long) y[yOff + 4]) & M));
        z[zOff + 4] = (int) c;
        c = (c >>> 32) + ((((long) x[xOff + 5]) & M) + (((long) y[yOff + 5]) & M));
        z[zOff + 5] = (int) c;
        c = (c >>> 32) + ((((long) x[xOff + 6]) & M) + (((long) y[yOff + 6]) & M));
        z[zOff + 6] = (int) c;
        c = (c >>> 32) + ((((long) x[xOff + 7]) & M) + (((long) y[yOff + 7]) & M));
        z[zOff + 7] = (int) c;
        return (int) (c >>> 32);
    }

    public static int addBothTo(int[] x, int[] y, int[] z) {
        long c = 0 + (((((long) x[0]) & M) + (((long) y[0]) & M)) + (((long) z[0]) & M));
        z[0] = (int) c;
        c = (c >>> 32) + (((((long) x[1]) & M) + (((long) y[1]) & M)) + (((long) z[1]) & M));
        z[1] = (int) c;
        c = (c >>> 32) + (((((long) x[2]) & M) + (((long) y[2]) & M)) + (((long) z[2]) & M));
        z[2] = (int) c;
        c = (c >>> 32) + (((((long) x[3]) & M) + (((long) y[3]) & M)) + (((long) z[3]) & M));
        z[3] = (int) c;
        c = (c >>> 32) + (((((long) x[4]) & M) + (((long) y[4]) & M)) + (((long) z[4]) & M));
        z[4] = (int) c;
        c = (c >>> 32) + (((((long) x[5]) & M) + (((long) y[5]) & M)) + (((long) z[5]) & M));
        z[5] = (int) c;
        c = (c >>> 32) + (((((long) x[6]) & M) + (((long) y[6]) & M)) + (((long) z[6]) & M));
        z[6] = (int) c;
        c = (c >>> 32) + (((((long) x[7]) & M) + (((long) y[7]) & M)) + (((long) z[7]) & M));
        z[7] = (int) c;
        return (int) (c >>> 32);
    }

    public static int addBothTo(int[] x, int xOff, int[] y, int yOff, int[] z, int zOff) {
        long c = 0 + (((((long) x[xOff + 0]) & M) + (((long) y[yOff + 0]) & M)) + (((long) z[zOff + 0]) & M));
        z[zOff + 0] = (int) c;
        c = (c >>> 32) + (((((long) x[xOff + 1]) & M) + (((long) y[yOff + 1]) & M)) + (((long) z[zOff + 1]) & M));
        z[zOff + 1] = (int) c;
        c = (c >>> 32) + (((((long) x[xOff + 2]) & M) + (((long) y[yOff + 2]) & M)) + (((long) z[zOff + 2]) & M));
        z[zOff + 2] = (int) c;
        c = (c >>> 32) + (((((long) x[xOff + 3]) & M) + (((long) y[yOff + 3]) & M)) + (((long) z[zOff + 3]) & M));
        z[zOff + 3] = (int) c;
        c = (c >>> 32) + (((((long) x[xOff + 4]) & M) + (((long) y[yOff + 4]) & M)) + (((long) z[zOff + 4]) & M));
        z[zOff + 4] = (int) c;
        c = (c >>> 32) + (((((long) x[xOff + 5]) & M) + (((long) y[yOff + 5]) & M)) + (((long) z[zOff + 5]) & M));
        z[zOff + 5] = (int) c;
        c = (c >>> 32) + (((((long) x[xOff + 6]) & M) + (((long) y[yOff + 6]) & M)) + (((long) z[zOff + 6]) & M));
        z[zOff + 6] = (int) c;
        c = (c >>> 32) + (((((long) x[xOff + 7]) & M) + (((long) y[yOff + 7]) & M)) + (((long) z[zOff + 7]) & M));
        z[zOff + 7] = (int) c;
        return (int) (c >>> 32);
    }

    public static int addTo(int[] x, int[] z) {
        long c = 0 + ((((long) x[0]) & M) + (((long) z[0]) & M));
        z[0] = (int) c;
        c = (c >>> 32) + ((((long) x[1]) & M) + (((long) z[1]) & M));
        z[1] = (int) c;
        c = (c >>> 32) + ((((long) x[2]) & M) + (((long) z[2]) & M));
        z[2] = (int) c;
        c = (c >>> 32) + ((((long) x[3]) & M) + (((long) z[3]) & M));
        z[3] = (int) c;
        c = (c >>> 32) + ((((long) x[4]) & M) + (((long) z[4]) & M));
        z[4] = (int) c;
        c = (c >>> 32) + ((((long) x[5]) & M) + (((long) z[5]) & M));
        z[5] = (int) c;
        c = (c >>> 32) + ((((long) x[6]) & M) + (((long) z[6]) & M));
        z[6] = (int) c;
        c = (c >>> 32) + ((((long) x[7]) & M) + (((long) z[7]) & M));
        z[7] = (int) c;
        return (int) (c >>> 32);
    }

    public static int addTo(int[] x, int xOff, int[] z, int zOff, int cIn) {
        long c = (((long) cIn) & M) + ((((long) x[xOff + 0]) & M) + (((long) z[zOff + 0]) & M));
        z[zOff + 0] = (int) c;
        c = (c >>> 32) + ((((long) x[xOff + 1]) & M) + (((long) z[zOff + 1]) & M));
        z[zOff + 1] = (int) c;
        c = (c >>> 32) + ((((long) x[xOff + 2]) & M) + (((long) z[zOff + 2]) & M));
        z[zOff + 2] = (int) c;
        c = (c >>> 32) + ((((long) x[xOff + 3]) & M) + (((long) z[zOff + 3]) & M));
        z[zOff + 3] = (int) c;
        c = (c >>> 32) + ((((long) x[xOff + 4]) & M) + (((long) z[zOff + 4]) & M));
        z[zOff + 4] = (int) c;
        c = (c >>> 32) + ((((long) x[xOff + 5]) & M) + (((long) z[zOff + 5]) & M));
        z[zOff + 5] = (int) c;
        c = (c >>> 32) + ((((long) x[xOff + 6]) & M) + (((long) z[zOff + 6]) & M));
        z[zOff + 6] = (int) c;
        c = (c >>> 32) + ((((long) x[xOff + 7]) & M) + (((long) z[zOff + 7]) & M));
        z[zOff + 7] = (int) c;
        return (int) (c >>> 32);
    }

    public static int addToEachOther(int[] u, int uOff, int[] v, int vOff) {
        long c = 0 + ((((long) u[uOff + 0]) & M) + (((long) v[vOff + 0]) & M));
        u[uOff + 0] = (int) c;
        v[vOff + 0] = (int) c;
        c = (c >>> 32) + ((((long) u[uOff + 1]) & M) + (((long) v[vOff + 1]) & M));
        u[uOff + 1] = (int) c;
        v[vOff + 1] = (int) c;
        c = (c >>> 32) + ((((long) u[uOff + 2]) & M) + (((long) v[vOff + 2]) & M));
        u[uOff + 2] = (int) c;
        v[vOff + 2] = (int) c;
        c = (c >>> 32) + ((((long) u[uOff + 3]) & M) + (((long) v[vOff + 3]) & M));
        u[uOff + 3] = (int) c;
        v[vOff + 3] = (int) c;
        c = (c >>> 32) + ((((long) u[uOff + 4]) & M) + (((long) v[vOff + 4]) & M));
        u[uOff + 4] = (int) c;
        v[vOff + 4] = (int) c;
        c = (c >>> 32) + ((((long) u[uOff + 5]) & M) + (((long) v[vOff + 5]) & M));
        u[uOff + 5] = (int) c;
        v[vOff + 5] = (int) c;
        c = (c >>> 32) + ((((long) u[uOff + 6]) & M) + (((long) v[vOff + 6]) & M));
        u[uOff + 6] = (int) c;
        v[vOff + 6] = (int) c;
        c = (c >>> 32) + ((((long) u[uOff + 7]) & M) + (((long) v[vOff + 7]) & M));
        u[uOff + 7] = (int) c;
        v[vOff + 7] = (int) c;
        return (int) (c >>> 32);
    }

    public static void copy(int[] x, int[] z) {
        z[0] = x[0];
        z[1] = x[1];
        z[2] = x[2];
        z[3] = x[3];
        z[4] = x[4];
        z[5] = x[5];
        z[6] = x[6];
        z[7] = x[7];
    }

    public static void copy64(long[] x, long[] z) {
        z[0] = x[0];
        z[1] = x[1];
        z[2] = x[2];
        z[3] = x[3];
    }

    public static int[] create() {
        return new int[8];
    }

    public static long[] create64() {
        return new long[4];
    }

    public static int[] createExt() {
        return new int[16];
    }

    public static long[] createExt64() {
        return new long[8];
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
        for (int i = 7; i >= 0; i--) {
            if (x[i] != y[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean eq64(long[] x, long[] y) {
        for (int i = 3; i >= 0; i--) {
            if (x[i] != y[i]) {
                return false;
            }
        }
        return true;
    }

    public static int[] fromBigInteger(BigInteger x) {
        if (x.signum() < 0 || x.bitLength() > 256) {
            throw new IllegalArgumentException();
        }
        int[] z = create();
        int i = 0;
        while (x.signum() != 0) {
            int i2 = i + 1;
            z[i] = x.intValue();
            x = x.shiftRight(32);
            i = i2;
        }
        return z;
    }

    public static long[] fromBigInteger64(BigInteger x) {
        if (x.signum() < 0 || x.bitLength() > 256) {
            throw new IllegalArgumentException();
        }
        long[] z = create64();
        int i = 0;
        while (x.signum() != 0) {
            int i2 = i + 1;
            z[i] = x.longValue();
            x = x.shiftRight(64);
            i = i2;
        }
        return z;
    }

    public static int getBit(int[] x, int bit) {
        if (bit == 0) {
            return x[0] & 1;
        }
        if ((bit & 255) != bit) {
            return 0;
        }
        return (x[bit >>> 5] >>> (bit & 31)) & 1;
    }

    public static boolean gte(int[] x, int[] y) {
        for (int i = 7; i >= 0; i--) {
            int x_i = x[i] ^ Integer.MIN_VALUE;
            int y_i = y[i] ^ Integer.MIN_VALUE;
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
        for (int i = 7; i >= 0; i--) {
            int x_i = x[xOff + i] ^ Integer.MIN_VALUE;
            int y_i = y[yOff + i] ^ Integer.MIN_VALUE;
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
        for (int i = 1; i < 8; i++) {
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
        for (int i = 1; i < 4; i++) {
            if (x[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean isZero(int[] x) {
        for (int i = 0; i < 8; i++) {
            if (x[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean isZero64(long[] x) {
        for (int i = 0; i < 4; i++) {
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
        long y_4 = ((long) y[4]) & M;
        long y_5 = ((long) y[5]) & M;
        long y_6 = ((long) y[6]) & M;
        long y_7 = ((long) y[7]) & M;
        long x_0 = ((long) x[0]) & M;
        long c = 0 + (x_0 * y_0);
        zz[0] = (int) c;
        c = (c >>> 32) + (x_0 * y_1);
        zz[1] = (int) c;
        c = (c >>> 32) + (x_0 * y_2);
        zz[2] = (int) c;
        c = (c >>> 32) + (x_0 * y_3);
        zz[3] = (int) c;
        c = (c >>> 32) + (x_0 * y_4);
        zz[4] = (int) c;
        c = (c >>> 32) + (x_0 * y_5);
        zz[5] = (int) c;
        c = (c >>> 32) + (x_0 * y_6);
        zz[6] = (int) c;
        c = (c >>> 32) + (x_0 * y_7);
        zz[7] = (int) c;
        zz[8] = (int) (c >>> 32);
        for (int i = 1; i < 8; i++) {
            long x_i = ((long) x[i]) & M;
            c = 0 + ((x_i * y_0) + (((long) zz[i + 0]) & M));
            zz[i + 0] = (int) c;
            c = (c >>> 32) + ((x_i * y_1) + (((long) zz[i + 1]) & M));
            zz[i + 1] = (int) c;
            c = (c >>> 32) + ((x_i * y_2) + (((long) zz[i + 2]) & M));
            zz[i + 2] = (int) c;
            c = (c >>> 32) + ((x_i * y_3) + (((long) zz[i + 3]) & M));
            zz[i + 3] = (int) c;
            c = (c >>> 32) + ((x_i * y_4) + (((long) zz[i + 4]) & M));
            zz[i + 4] = (int) c;
            c = (c >>> 32) + ((x_i * y_5) + (((long) zz[i + 5]) & M));
            zz[i + 5] = (int) c;
            c = (c >>> 32) + ((x_i * y_6) + (((long) zz[i + 6]) & M));
            zz[i + 6] = (int) c;
            c = (c >>> 32) + ((x_i * y_7) + (((long) zz[i + 7]) & M));
            zz[i + 7] = (int) c;
            zz[i + 8] = (int) (c >>> 32);
        }
    }

    public static void mul(int[] x, int xOff, int[] y, int yOff, int[] zz, int zzOff) {
        long y_0 = ((long) y[yOff + 0]) & M;
        long y_1 = ((long) y[yOff + 1]) & M;
        long y_2 = ((long) y[yOff + 2]) & M;
        long y_3 = ((long) y[yOff + 3]) & M;
        long y_4 = ((long) y[yOff + 4]) & M;
        long y_5 = ((long) y[yOff + 5]) & M;
        long y_6 = ((long) y[yOff + 6]) & M;
        long y_7 = ((long) y[yOff + 7]) & M;
        long x_0 = ((long) x[xOff + 0]) & M;
        long c = 0 + (x_0 * y_0);
        zz[zzOff + 0] = (int) c;
        c = (c >>> 32) + (x_0 * y_1);
        zz[zzOff + 1] = (int) c;
        c = (c >>> 32) + (x_0 * y_2);
        zz[zzOff + 2] = (int) c;
        c = (c >>> 32) + (x_0 * y_3);
        zz[zzOff + 3] = (int) c;
        c = (c >>> 32) + (x_0 * y_4);
        zz[zzOff + 4] = (int) c;
        c = (c >>> 32) + (x_0 * y_5);
        zz[zzOff + 5] = (int) c;
        c = (c >>> 32) + (x_0 * y_6);
        zz[zzOff + 6] = (int) c;
        c = (c >>> 32) + (x_0 * y_7);
        zz[zzOff + 7] = (int) c;
        zz[zzOff + 8] = (int) (c >>> 32);
        for (int i = 1; i < 8; i++) {
            zzOff++;
            long x_i = ((long) x[xOff + i]) & M;
            c = 0 + ((x_i * y_0) + (((long) zz[zzOff + 0]) & M));
            zz[zzOff + 0] = (int) c;
            c = (c >>> 32) + ((x_i * y_1) + (((long) zz[zzOff + 1]) & M));
            zz[zzOff + 1] = (int) c;
            c = (c >>> 32) + ((x_i * y_2) + (((long) zz[zzOff + 2]) & M));
            zz[zzOff + 2] = (int) c;
            c = (c >>> 32) + ((x_i * y_3) + (((long) zz[zzOff + 3]) & M));
            zz[zzOff + 3] = (int) c;
            c = (c >>> 32) + ((x_i * y_4) + (((long) zz[zzOff + 4]) & M));
            zz[zzOff + 4] = (int) c;
            c = (c >>> 32) + ((x_i * y_5) + (((long) zz[zzOff + 5]) & M));
            zz[zzOff + 5] = (int) c;
            c = (c >>> 32) + ((x_i * y_6) + (((long) zz[zzOff + 6]) & M));
            zz[zzOff + 6] = (int) c;
            c = (c >>> 32) + ((x_i * y_7) + (((long) zz[zzOff + 7]) & M));
            zz[zzOff + 7] = (int) c;
            zz[zzOff + 8] = (int) (c >>> 32);
        }
    }

    public static int mulAddTo(int[] x, int[] y, int[] zz) {
        long y_0 = ((long) y[0]) & M;
        long y_1 = ((long) y[1]) & M;
        long y_2 = ((long) y[2]) & M;
        long y_3 = ((long) y[3]) & M;
        long y_4 = ((long) y[4]) & M;
        long y_5 = ((long) y[5]) & M;
        long y_6 = ((long) y[6]) & M;
        long y_7 = ((long) y[7]) & M;
        long zc = 0;
        for (int i = 0; i < 8; i++) {
            long x_i = ((long) x[i]) & M;
            long c = 0 + ((x_i * y_0) + (((long) zz[i + 0]) & M));
            zz[i + 0] = (int) c;
            c = (c >>> 32) + ((x_i * y_1) + (((long) zz[i + 1]) & M));
            zz[i + 1] = (int) c;
            c = (c >>> 32) + ((x_i * y_2) + (((long) zz[i + 2]) & M));
            zz[i + 2] = (int) c;
            c = (c >>> 32) + ((x_i * y_3) + (((long) zz[i + 3]) & M));
            zz[i + 3] = (int) c;
            c = (c >>> 32) + ((x_i * y_4) + (((long) zz[i + 4]) & M));
            zz[i + 4] = (int) c;
            c = (c >>> 32) + ((x_i * y_5) + (((long) zz[i + 5]) & M));
            zz[i + 5] = (int) c;
            c = (c >>> 32) + ((x_i * y_6) + (((long) zz[i + 6]) & M));
            zz[i + 6] = (int) c;
            c = (c >>> 32) + ((x_i * y_7) + (((long) zz[i + 7]) & M));
            zz[i + 7] = (int) c;
            c = (c >>> 32) + ((((long) zz[i + 8]) & M) + zc);
            zz[i + 8] = (int) c;
            zc = c >>> 32;
        }
        return (int) zc;
    }

    public static int mulAddTo(int[] x, int xOff, int[] y, int yOff, int[] zz, int zzOff) {
        long y_0 = ((long) y[yOff + 0]) & M;
        long y_1 = ((long) y[yOff + 1]) & M;
        long y_2 = ((long) y[yOff + 2]) & M;
        long y_3 = ((long) y[yOff + 3]) & M;
        long y_4 = ((long) y[yOff + 4]) & M;
        long y_5 = ((long) y[yOff + 5]) & M;
        long y_6 = ((long) y[yOff + 6]) & M;
        long y_7 = ((long) y[yOff + 7]) & M;
        long zc = 0;
        for (int i = 0; i < 8; i++) {
            long x_i = ((long) x[xOff + i]) & M;
            long c = 0 + ((x_i * y_0) + (((long) zz[zzOff + 0]) & M));
            zz[zzOff + 0] = (int) c;
            c = (c >>> 32) + ((x_i * y_1) + (((long) zz[zzOff + 1]) & M));
            zz[zzOff + 1] = (int) c;
            c = (c >>> 32) + ((x_i * y_2) + (((long) zz[zzOff + 2]) & M));
            zz[zzOff + 2] = (int) c;
            c = (c >>> 32) + ((x_i * y_3) + (((long) zz[zzOff + 3]) & M));
            zz[zzOff + 3] = (int) c;
            c = (c >>> 32) + ((x_i * y_4) + (((long) zz[zzOff + 4]) & M));
            zz[zzOff + 4] = (int) c;
            c = (c >>> 32) + ((x_i * y_5) + (((long) zz[zzOff + 5]) & M));
            zz[zzOff + 5] = (int) c;
            c = (c >>> 32) + ((x_i * y_6) + (((long) zz[zzOff + 6]) & M));
            zz[zzOff + 6] = (int) c;
            c = (c >>> 32) + ((x_i * y_7) + (((long) zz[zzOff + 7]) & M));
            zz[zzOff + 7] = (int) c;
            c = (c >>> 32) + ((((long) zz[zzOff + 8]) & M) + zc);
            zz[zzOff + 8] = (int) c;
            zc = c >>> 32;
            zzOff++;
        }
        return (int) zc;
    }

    public static long mul33Add(int w, int[] x, int xOff, int[] y, int yOff, int[] z, int zOff) {
        long wVal = ((long) w) & M;
        long x0 = ((long) x[xOff + 0]) & M;
        long c = 0 + ((wVal * x0) + (((long) y[yOff + 0]) & M));
        z[zOff + 0] = (int) c;
        long x1 = ((long) x[xOff + 1]) & M;
        c = (c >>> 32) + (((wVal * x1) + x0) + (((long) y[yOff + 1]) & M));
        z[zOff + 1] = (int) c;
        long x2 = ((long) x[xOff + 2]) & M;
        c = (c >>> 32) + (((wVal * x2) + x1) + (((long) y[yOff + 2]) & M));
        z[zOff + 2] = (int) c;
        long x3 = ((long) x[xOff + 3]) & M;
        c = (c >>> 32) + (((wVal * x3) + x2) + (((long) y[yOff + 3]) & M));
        z[zOff + 3] = (int) c;
        long x4 = ((long) x[xOff + 4]) & M;
        c = (c >>> 32) + (((wVal * x4) + x3) + (((long) y[yOff + 4]) & M));
        z[zOff + 4] = (int) c;
        long x5 = ((long) x[xOff + 5]) & M;
        c = (c >>> 32) + (((wVal * x5) + x4) + (((long) y[yOff + 5]) & M));
        z[zOff + 5] = (int) c;
        long x6 = ((long) x[xOff + 6]) & M;
        c = (c >>> 32) + (((wVal * x6) + x5) + (((long) y[yOff + 6]) & M));
        z[zOff + 6] = (int) c;
        long x7 = ((long) x[xOff + 7]) & M;
        c = (c >>> 32) + (((wVal * x7) + x6) + (((long) y[yOff + 7]) & M));
        z[zOff + 7] = (int) c;
        return (c >>> 32) + x7;
    }

    public static int mulByWord(int x, int[] z) {
        long xVal = ((long) x) & M;
        long c = 0 + ((((long) z[0]) & M) * xVal);
        z[0] = (int) c;
        c = (c >>> 32) + ((((long) z[1]) & M) * xVal);
        z[1] = (int) c;
        c = (c >>> 32) + ((((long) z[2]) & M) * xVal);
        z[2] = (int) c;
        c = (c >>> 32) + ((((long) z[3]) & M) * xVal);
        z[3] = (int) c;
        c = (c >>> 32) + ((((long) z[4]) & M) * xVal);
        z[4] = (int) c;
        c = (c >>> 32) + ((((long) z[5]) & M) * xVal);
        z[5] = (int) c;
        c = (c >>> 32) + ((((long) z[6]) & M) * xVal);
        z[6] = (int) c;
        c = (c >>> 32) + ((((long) z[7]) & M) * xVal);
        z[7] = (int) c;
        return (int) (c >>> 32);
    }

    public static int mulByWordAddTo(int x, int[] y, int[] z) {
        long xVal = ((long) x) & M;
        long c = 0 + (((((long) z[0]) & M) * xVal) + (((long) y[0]) & M));
        z[0] = (int) c;
        c = (c >>> 32) + (((((long) z[1]) & M) * xVal) + (((long) y[1]) & M));
        z[1] = (int) c;
        c = (c >>> 32) + (((((long) z[2]) & M) * xVal) + (((long) y[2]) & M));
        z[2] = (int) c;
        c = (c >>> 32) + (((((long) z[3]) & M) * xVal) + (((long) y[3]) & M));
        z[3] = (int) c;
        c = (c >>> 32) + (((((long) z[4]) & M) * xVal) + (((long) y[4]) & M));
        z[4] = (int) c;
        c = (c >>> 32) + (((((long) z[5]) & M) * xVal) + (((long) y[5]) & M));
        z[5] = (int) c;
        c = (c >>> 32) + (((((long) z[6]) & M) * xVal) + (((long) y[6]) & M));
        z[6] = (int) c;
        c = (c >>> 32) + (((((long) z[7]) & M) * xVal) + (((long) y[7]) & M));
        z[7] = (int) c;
        return (int) (c >>> 32);
    }

    public static int mulWordAddTo(int x, int[] y, int yOff, int[] z, int zOff) {
        long xVal = ((long) x) & M;
        long c = 0 + (((((long) y[yOff + 0]) & M) * xVal) + (((long) z[zOff + 0]) & M));
        z[zOff + 0] = (int) c;
        c = (c >>> 32) + (((((long) y[yOff + 1]) & M) * xVal) + (((long) z[zOff + 1]) & M));
        z[zOff + 1] = (int) c;
        c = (c >>> 32) + (((((long) y[yOff + 2]) & M) * xVal) + (((long) z[zOff + 2]) & M));
        z[zOff + 2] = (int) c;
        c = (c >>> 32) + (((((long) y[yOff + 3]) & M) * xVal) + (((long) z[zOff + 3]) & M));
        z[zOff + 3] = (int) c;
        c = (c >>> 32) + (((((long) y[yOff + 4]) & M) * xVal) + (((long) z[zOff + 4]) & M));
        z[zOff + 4] = (int) c;
        c = (c >>> 32) + (((((long) y[yOff + 5]) & M) * xVal) + (((long) z[zOff + 5]) & M));
        z[zOff + 5] = (int) c;
        c = (c >>> 32) + (((((long) y[yOff + 6]) & M) * xVal) + (((long) z[zOff + 6]) & M));
        z[zOff + 6] = (int) c;
        c = (c >>> 32) + (((((long) y[yOff + 7]) & M) * xVal) + (((long) z[zOff + 7]) & M));
        z[zOff + 7] = (int) c;
        return (int) (c >>> 32);
    }

    public static int mul33DWordAdd(int x, long y, int[] z, int zOff) {
        long xVal = ((long) x) & M;
        long y00 = y & M;
        long c = 0 + ((xVal * y00) + (((long) z[zOff + 0]) & M));
        z[zOff + 0] = (int) c;
        long y01 = y >>> 32;
        c = (c >>> 32) + (((xVal * y01) + y00) + (((long) z[zOff + 1]) & M));
        z[zOff + 1] = (int) c;
        c = (c >>> 32) + ((((long) z[zOff + 2]) & M) + y01);
        z[zOff + 2] = (int) c;
        c = (c >>> 32) + (((long) z[zOff + 3]) & M);
        z[zOff + 3] = (int) c;
        return (c >>> 32) == 0 ? 0 : Nat.incAt(8, z, zOff, 4);
    }

    public static int mul33WordAdd(int x, int y, int[] z, int zOff) {
        long yVal = ((long) y) & M;
        long c = 0 + ((yVal * (((long) x) & M)) + (((long) z[zOff + 0]) & M));
        z[zOff + 0] = (int) c;
        c = (c >>> 32) + ((((long) z[zOff + 1]) & M) + yVal);
        z[zOff + 1] = (int) c;
        c = (c >>> 32) + (((long) z[zOff + 2]) & M);
        z[zOff + 2] = (int) c;
        return (c >>> 32) == 0 ? 0 : Nat.incAt(8, z, zOff, 3);
    }

    public static int mulWordDwordAdd(int x, long y, int[] z, int zOff) {
        long xVal = ((long) x) & M;
        long c = 0 + (((M & y) * xVal) + (((long) z[zOff + 0]) & M));
        z[zOff + 0] = (int) c;
        c = (c >>> 32) + (((y >>> 32) * xVal) + (((long) z[zOff + 1]) & M));
        z[zOff + 1] = (int) c;
        c = (c >>> 32) + (((long) z[zOff + 2]) & M);
        z[zOff + 2] = (int) c;
        return (c >>> 32) == 0 ? 0 : Nat.incAt(8, z, zOff, 3);
    }

    public static int mulWord(int x, int[] y, int[] z, int zOff) {
        long c = 0;
        long xVal = ((long) x) & M;
        int i = 0;
        do {
            c += (((long) y[i]) & M) * xVal;
            z[zOff + i] = (int) c;
            c >>>= 32;
            i++;
        } while (i < 8);
        return (int) c;
    }

    public static void square(int[] x, int[] zz) {
        long x_0 = ((long) x[0]) & M;
        int c = 0;
        int i = 7;
        int j = 16;
        while (true) {
            int i2 = i - 1;
            long xVal = ((long) x[i]) & M;
            long p = xVal * xVal;
            j--;
            zz[j] = (c << 31) | ((int) (p >>> 33));
            j--;
            zz[j] = (int) (p >>> 1);
            c = (int) p;
            if (i2 > 0) {
                i = i2;
            } else {
                p = x_0 * x_0;
                long zz_1 = (((long) (c << 31)) & M) | (p >>> 33);
                zz[0] = (int) p;
                long x_1 = ((long) x[1]) & M;
                long zz_2 = ((long) zz[2]) & M;
                zz_1 += x_1 * x_0;
                int w = (int) zz_1;
                zz[1] = (w << 1) | (((int) (p >>> 32)) & 1);
                c = w >>> 31;
                long x_2 = ((long) x[2]) & M;
                long zz_3 = ((long) zz[3]) & M;
                long zz_4 = ((long) zz[4]) & M;
                zz_2 = (zz_2 + (zz_1 >>> 32)) + (x_2 * x_0);
                w = (int) zz_2;
                zz[2] = (w << 1) | c;
                c = w >>> 31;
                zz_3 += (zz_2 >>> 32) + (x_2 * x_1);
                zz_4 += zz_3 >>> 32;
                long x_3 = ((long) x[3]) & M;
                long zz_5 = (((long) zz[5]) & M) + (zz_4 >>> 32);
                zz_4 &= M;
                long zz_6 = (((long) zz[6]) & M) + (zz_5 >>> 32);
                zz_5 &= M;
                zz_3 = (zz_3 & M) + (x_3 * x_0);
                w = (int) zz_3;
                zz[3] = (w << 1) | c;
                c = w >>> 31;
                zz_4 += (zz_3 >>> 32) + (x_3 * x_1);
                zz_5 += (zz_4 >>> 32) + (x_3 * x_2);
                zz_6 += zz_5 >>> 32;
                zz_5 &= M;
                long x_4 = ((long) x[4]) & M;
                long zz_7 = (((long) zz[7]) & M) + (zz_6 >>> 32);
                zz_6 &= M;
                long zz_8 = (((long) zz[8]) & M) + (zz_7 >>> 32);
                zz_7 &= M;
                zz_4 = (zz_4 & M) + (x_4 * x_0);
                w = (int) zz_4;
                zz[4] = (w << 1) | c;
                c = w >>> 31;
                zz_5 += (zz_4 >>> 32) + (x_4 * x_1);
                zz_6 += (zz_5 >>> 32) + (x_4 * x_2);
                zz_7 += (zz_6 >>> 32) + (x_4 * x_3);
                zz_6 &= M;
                zz_8 += zz_7 >>> 32;
                zz_7 &= M;
                long x_5 = ((long) x[5]) & M;
                long zz_9 = (((long) zz[9]) & M) + (zz_8 >>> 32);
                zz_8 &= M;
                long zz_10 = (((long) zz[10]) & M) + (zz_9 >>> 32);
                zz_9 &= M;
                zz_5 = (zz_5 & M) + (x_5 * x_0);
                w = (int) zz_5;
                zz[5] = (w << 1) | c;
                c = w >>> 31;
                zz_6 += (zz_5 >>> 32) + (x_5 * x_1);
                zz_7 += (zz_6 >>> 32) + (x_5 * x_2);
                zz_8 += (zz_7 >>> 32) + (x_5 * x_3);
                zz_7 &= M;
                zz_9 += (zz_8 >>> 32) + (x_5 * x_4);
                zz_8 &= M;
                zz_10 += zz_9 >>> 32;
                zz_9 &= M;
                long x_6 = ((long) x[6]) & M;
                long zz_11 = (((long) zz[11]) & M) + (zz_10 >>> 32);
                zz_10 &= M;
                long zz_12 = (((long) zz[12]) & M) + (zz_11 >>> 32);
                zz_11 &= M;
                zz_6 = (zz_6 & M) + (x_6 * x_0);
                w = (int) zz_6;
                zz[6] = (w << 1) | c;
                c = w >>> 31;
                zz_7 += (zz_6 >>> 32) + (x_6 * x_1);
                zz_8 += (zz_7 >>> 32) + (x_6 * x_2);
                zz_9 += (zz_8 >>> 32) + (x_6 * x_3);
                zz_8 &= M;
                zz_10 += (zz_9 >>> 32) + (x_6 * x_4);
                zz_9 &= M;
                zz_11 += (zz_10 >>> 32) + (x_6 * x_5);
                zz_10 &= M;
                zz_12 += zz_11 >>> 32;
                zz_11 &= M;
                long x_7 = ((long) x[7]) & M;
                long zz_13 = (((long) zz[13]) & M) + (zz_12 >>> 32);
                zz_12 &= M;
                long zz_14 = (((long) zz[14]) & M) + (zz_13 >>> 32);
                zz_13 &= M;
                zz_7 = (zz_7 & M) + (x_7 * x_0);
                w = (int) zz_7;
                zz[7] = (w << 1) | c;
                c = w >>> 31;
                zz_8 += (zz_7 >>> 32) + (x_7 * x_1);
                zz_9 += (zz_8 >>> 32) + (x_7 * x_2);
                zz_10 += (zz_9 >>> 32) + (x_7 * x_3);
                zz_11 += (zz_10 >>> 32) + (x_7 * x_4);
                zz_12 += (zz_11 >>> 32) + (x_7 * x_5);
                zz_13 += (zz_12 >>> 32) + (x_7 * x_6);
                zz_14 += zz_13 >>> 32;
                w = (int) zz_8;
                zz[8] = (w << 1) | c;
                c = w >>> 31;
                w = (int) zz_9;
                zz[9] = (w << 1) | c;
                c = w >>> 31;
                w = (int) zz_10;
                zz[10] = (w << 1) | c;
                c = w >>> 31;
                w = (int) zz_11;
                zz[11] = (w << 1) | c;
                c = w >>> 31;
                w = (int) zz_12;
                zz[12] = (w << 1) | c;
                c = w >>> 31;
                w = (int) zz_13;
                zz[13] = (w << 1) | c;
                c = w >>> 31;
                w = (int) zz_14;
                zz[14] = (w << 1) | c;
                zz[15] = ((zz[15] + ((int) (zz_14 >>> 32))) << 1) | (w >>> 31);
                return;
            }
        }
    }

    public static void square(int[] x, int xOff, int[] zz, int zzOff) {
        long x_0 = ((long) x[xOff + 0]) & M;
        int c = 0;
        int i = 7;
        int j = 16;
        while (true) {
            int i2 = i - 1;
            long xVal = ((long) x[xOff + i]) & M;
            long p = xVal * xVal;
            j--;
            zz[zzOff + j] = (c << 31) | ((int) (p >>> 33));
            j--;
            zz[zzOff + j] = (int) (p >>> 1);
            c = (int) p;
            if (i2 > 0) {
                i = i2;
            } else {
                p = x_0 * x_0;
                long zz_1 = (((long) (c << 31)) & M) | (p >>> 33);
                zz[zzOff + 0] = (int) p;
                long x_1 = ((long) x[xOff + 1]) & M;
                long zz_2 = ((long) zz[zzOff + 2]) & M;
                zz_1 += x_1 * x_0;
                int w = (int) zz_1;
                zz[zzOff + 1] = (w << 1) | (((int) (p >>> 32)) & 1);
                c = w >>> 31;
                long x_2 = ((long) x[xOff + 2]) & M;
                long zz_3 = ((long) zz[zzOff + 3]) & M;
                long zz_4 = ((long) zz[zzOff + 4]) & M;
                zz_2 = (zz_2 + (zz_1 >>> 32)) + (x_2 * x_0);
                w = (int) zz_2;
                zz[zzOff + 2] = (w << 1) | c;
                c = w >>> 31;
                zz_3 += (zz_2 >>> 32) + (x_2 * x_1);
                zz_4 += zz_3 >>> 32;
                long x_3 = ((long) x[xOff + 3]) & M;
                long zz_5 = (((long) zz[zzOff + 5]) & M) + (zz_4 >>> 32);
                zz_4 &= M;
                long zz_6 = (((long) zz[zzOff + 6]) & M) + (zz_5 >>> 32);
                zz_5 &= M;
                zz_3 = (zz_3 & M) + (x_3 * x_0);
                w = (int) zz_3;
                zz[zzOff + 3] = (w << 1) | c;
                c = w >>> 31;
                zz_4 += (zz_3 >>> 32) + (x_3 * x_1);
                zz_5 += (zz_4 >>> 32) + (x_3 * x_2);
                zz_6 += zz_5 >>> 32;
                zz_5 &= M;
                long x_4 = ((long) x[xOff + 4]) & M;
                long zz_7 = (((long) zz[zzOff + 7]) & M) + (zz_6 >>> 32);
                zz_6 &= M;
                long zz_8 = (((long) zz[zzOff + 8]) & M) + (zz_7 >>> 32);
                zz_7 &= M;
                zz_4 = (zz_4 & M) + (x_4 * x_0);
                w = (int) zz_4;
                zz[zzOff + 4] = (w << 1) | c;
                c = w >>> 31;
                zz_5 += (zz_4 >>> 32) + (x_4 * x_1);
                zz_6 += (zz_5 >>> 32) + (x_4 * x_2);
                zz_7 += (zz_6 >>> 32) + (x_4 * x_3);
                zz_6 &= M;
                zz_8 += zz_7 >>> 32;
                zz_7 &= M;
                long x_5 = ((long) x[xOff + 5]) & M;
                long zz_9 = (((long) zz[zzOff + 9]) & M) + (zz_8 >>> 32);
                zz_8 &= M;
                long zz_10 = (((long) zz[zzOff + 10]) & M) + (zz_9 >>> 32);
                zz_9 &= M;
                zz_5 = (zz_5 & M) + (x_5 * x_0);
                w = (int) zz_5;
                zz[zzOff + 5] = (w << 1) | c;
                c = w >>> 31;
                zz_6 += (zz_5 >>> 32) + (x_5 * x_1);
                zz_7 += (zz_6 >>> 32) + (x_5 * x_2);
                zz_8 += (zz_7 >>> 32) + (x_5 * x_3);
                zz_7 &= M;
                zz_9 += (zz_8 >>> 32) + (x_5 * x_4);
                zz_8 &= M;
                zz_10 += zz_9 >>> 32;
                zz_9 &= M;
                long x_6 = ((long) x[xOff + 6]) & M;
                long zz_11 = (((long) zz[zzOff + 11]) & M) + (zz_10 >>> 32);
                zz_10 &= M;
                long zz_12 = (((long) zz[zzOff + 12]) & M) + (zz_11 >>> 32);
                zz_11 &= M;
                zz_6 = (zz_6 & M) + (x_6 * x_0);
                w = (int) zz_6;
                zz[zzOff + 6] = (w << 1) | c;
                c = w >>> 31;
                zz_7 += (zz_6 >>> 32) + (x_6 * x_1);
                zz_8 += (zz_7 >>> 32) + (x_6 * x_2);
                zz_9 += (zz_8 >>> 32) + (x_6 * x_3);
                zz_8 &= M;
                zz_10 += (zz_9 >>> 32) + (x_6 * x_4);
                zz_9 &= M;
                zz_11 += (zz_10 >>> 32) + (x_6 * x_5);
                zz_10 &= M;
                zz_12 += zz_11 >>> 32;
                zz_11 &= M;
                long x_7 = ((long) x[xOff + 7]) & M;
                long zz_13 = (((long) zz[zzOff + 13]) & M) + (zz_12 >>> 32);
                zz_12 &= M;
                long zz_14 = (((long) zz[zzOff + 14]) & M) + (zz_13 >>> 32);
                zz_13 &= M;
                zz_7 = (zz_7 & M) + (x_7 * x_0);
                w = (int) zz_7;
                zz[zzOff + 7] = (w << 1) | c;
                c = w >>> 31;
                zz_8 += (zz_7 >>> 32) + (x_7 * x_1);
                zz_9 += (zz_8 >>> 32) + (x_7 * x_2);
                zz_10 += (zz_9 >>> 32) + (x_7 * x_3);
                zz_11 += (zz_10 >>> 32) + (x_7 * x_4);
                zz_12 += (zz_11 >>> 32) + (x_7 * x_5);
                zz_13 += (zz_12 >>> 32) + (x_7 * x_6);
                zz_14 += zz_13 >>> 32;
                w = (int) zz_8;
                zz[zzOff + 8] = (w << 1) | c;
                c = w >>> 31;
                w = (int) zz_9;
                zz[zzOff + 9] = (w << 1) | c;
                c = w >>> 31;
                w = (int) zz_10;
                zz[zzOff + 10] = (w << 1) | c;
                c = w >>> 31;
                w = (int) zz_11;
                zz[zzOff + 11] = (w << 1) | c;
                c = w >>> 31;
                w = (int) zz_12;
                zz[zzOff + 12] = (w << 1) | c;
                c = w >>> 31;
                w = (int) zz_13;
                zz[zzOff + 13] = (w << 1) | c;
                c = w >>> 31;
                w = (int) zz_14;
                zz[zzOff + 14] = (w << 1) | c;
                zz[zzOff + 15] = ((zz[zzOff + 15] + ((int) (zz_14 >>> 32))) << 1) | (w >>> 31);
                return;
            }
        }
    }

    public static int sub(int[] x, int[] y, int[] z) {
        long c = 0 + ((((long) x[0]) & M) - (((long) y[0]) & M));
        z[0] = (int) c;
        c = (c >> 32) + ((((long) x[1]) & M) - (((long) y[1]) & M));
        z[1] = (int) c;
        c = (c >> 32) + ((((long) x[2]) & M) - (((long) y[2]) & M));
        z[2] = (int) c;
        c = (c >> 32) + ((((long) x[3]) & M) - (((long) y[3]) & M));
        z[3] = (int) c;
        c = (c >> 32) + ((((long) x[4]) & M) - (((long) y[4]) & M));
        z[4] = (int) c;
        c = (c >> 32) + ((((long) x[5]) & M) - (((long) y[5]) & M));
        z[5] = (int) c;
        c = (c >> 32) + ((((long) x[6]) & M) - (((long) y[6]) & M));
        z[6] = (int) c;
        c = (c >> 32) + ((((long) x[7]) & M) - (((long) y[7]) & M));
        z[7] = (int) c;
        return (int) (c >> 32);
    }

    public static int sub(int[] x, int xOff, int[] y, int yOff, int[] z, int zOff) {
        long c = 0 + ((((long) x[xOff + 0]) & M) - (((long) y[yOff + 0]) & M));
        z[zOff + 0] = (int) c;
        c = (c >> 32) + ((((long) x[xOff + 1]) & M) - (((long) y[yOff + 1]) & M));
        z[zOff + 1] = (int) c;
        c = (c >> 32) + ((((long) x[xOff + 2]) & M) - (((long) y[yOff + 2]) & M));
        z[zOff + 2] = (int) c;
        c = (c >> 32) + ((((long) x[xOff + 3]) & M) - (((long) y[yOff + 3]) & M));
        z[zOff + 3] = (int) c;
        c = (c >> 32) + ((((long) x[xOff + 4]) & M) - (((long) y[yOff + 4]) & M));
        z[zOff + 4] = (int) c;
        c = (c >> 32) + ((((long) x[xOff + 5]) & M) - (((long) y[yOff + 5]) & M));
        z[zOff + 5] = (int) c;
        c = (c >> 32) + ((((long) x[xOff + 6]) & M) - (((long) y[yOff + 6]) & M));
        z[zOff + 6] = (int) c;
        c = (c >> 32) + ((((long) x[xOff + 7]) & M) - (((long) y[yOff + 7]) & M));
        z[zOff + 7] = (int) c;
        return (int) (c >> 32);
    }

    public static int subBothFrom(int[] x, int[] y, int[] z) {
        long c = 0 + (((((long) z[0]) & M) - (((long) x[0]) & M)) - (((long) y[0]) & M));
        z[0] = (int) c;
        c = (c >> 32) + (((((long) z[1]) & M) - (((long) x[1]) & M)) - (((long) y[1]) & M));
        z[1] = (int) c;
        c = (c >> 32) + (((((long) z[2]) & M) - (((long) x[2]) & M)) - (((long) y[2]) & M));
        z[2] = (int) c;
        c = (c >> 32) + (((((long) z[3]) & M) - (((long) x[3]) & M)) - (((long) y[3]) & M));
        z[3] = (int) c;
        c = (c >> 32) + (((((long) z[4]) & M) - (((long) x[4]) & M)) - (((long) y[4]) & M));
        z[4] = (int) c;
        c = (c >> 32) + (((((long) z[5]) & M) - (((long) x[5]) & M)) - (((long) y[5]) & M));
        z[5] = (int) c;
        c = (c >> 32) + (((((long) z[6]) & M) - (((long) x[6]) & M)) - (((long) y[6]) & M));
        z[6] = (int) c;
        c = (c >> 32) + (((((long) z[7]) & M) - (((long) x[7]) & M)) - (((long) y[7]) & M));
        z[7] = (int) c;
        return (int) (c >> 32);
    }

    public static int subFrom(int[] x, int[] z) {
        long c = 0 + ((((long) z[0]) & M) - (((long) x[0]) & M));
        z[0] = (int) c;
        c = (c >> 32) + ((((long) z[1]) & M) - (((long) x[1]) & M));
        z[1] = (int) c;
        c = (c >> 32) + ((((long) z[2]) & M) - (((long) x[2]) & M));
        z[2] = (int) c;
        c = (c >> 32) + ((((long) z[3]) & M) - (((long) x[3]) & M));
        z[3] = (int) c;
        c = (c >> 32) + ((((long) z[4]) & M) - (((long) x[4]) & M));
        z[4] = (int) c;
        c = (c >> 32) + ((((long) z[5]) & M) - (((long) x[5]) & M));
        z[5] = (int) c;
        c = (c >> 32) + ((((long) z[6]) & M) - (((long) x[6]) & M));
        z[6] = (int) c;
        c = (c >> 32) + ((((long) z[7]) & M) - (((long) x[7]) & M));
        z[7] = (int) c;
        return (int) (c >> 32);
    }

    public static int subFrom(int[] x, int xOff, int[] z, int zOff) {
        long c = 0 + ((((long) z[zOff + 0]) & M) - (((long) x[xOff + 0]) & M));
        z[zOff + 0] = (int) c;
        c = (c >> 32) + ((((long) z[zOff + 1]) & M) - (((long) x[xOff + 1]) & M));
        z[zOff + 1] = (int) c;
        c = (c >> 32) + ((((long) z[zOff + 2]) & M) - (((long) x[xOff + 2]) & M));
        z[zOff + 2] = (int) c;
        c = (c >> 32) + ((((long) z[zOff + 3]) & M) - (((long) x[xOff + 3]) & M));
        z[zOff + 3] = (int) c;
        c = (c >> 32) + ((((long) z[zOff + 4]) & M) - (((long) x[xOff + 4]) & M));
        z[zOff + 4] = (int) c;
        c = (c >> 32) + ((((long) z[zOff + 5]) & M) - (((long) x[xOff + 5]) & M));
        z[zOff + 5] = (int) c;
        c = (c >> 32) + ((((long) z[zOff + 6]) & M) - (((long) x[xOff + 6]) & M));
        z[zOff + 6] = (int) c;
        c = (c >> 32) + ((((long) z[zOff + 7]) & M) - (((long) x[xOff + 7]) & M));
        z[zOff + 7] = (int) c;
        return (int) (c >> 32);
    }

    public static BigInteger toBigInteger(int[] x) {
        byte[] bs = new byte[32];
        for (int i = 0; i < 8; i++) {
            int x_i = x[i];
            if (x_i != 0) {
                Pack.intToBigEndian(x_i, bs, (7 - i) << 2);
            }
        }
        return new BigInteger(1, bs);
    }

    public static BigInteger toBigInteger64(long[] x) {
        byte[] bs = new byte[32];
        for (int i = 0; i < 4; i++) {
            long x_i = x[i];
            if (x_i != 0) {
                Pack.longToBigEndian(x_i, bs, (3 - i) << 3);
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
        z[6] = 0;
        z[7] = 0;
    }
}
