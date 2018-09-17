package com.android.org.bouncycastle.math.raw;

import com.android.org.bouncycastle.util.Pack;
import java.math.BigInteger;

public abstract class Nat {
    private static final long M = 4294967295L;

    public static int add(int len, int[] x, int[] y, int[] z) {
        long c = 0;
        for (int i = 0; i < len; i++) {
            c += (((long) x[i]) & M) + (((long) y[i]) & M);
            z[i] = (int) c;
            c >>>= 32;
        }
        return (int) c;
    }

    public static int add33At(int len, int x, int[] z, int zPos) {
        long c = (((long) z[zPos + 0]) & M) + (((long) x) & M);
        z[zPos + 0] = (int) c;
        c = (c >>> 32) + ((((long) z[zPos + 1]) & M) + 1);
        z[zPos + 1] = (int) c;
        return (c >>> 32) == 0 ? 0 : incAt(len, z, zPos + 2);
    }

    public static int add33At(int len, int x, int[] z, int zOff, int zPos) {
        long c = (((long) z[zOff + zPos]) & M) + (((long) x) & M);
        z[zOff + zPos] = (int) c;
        c = (c >>> 32) + ((((long) z[(zOff + zPos) + 1]) & M) + 1);
        z[(zOff + zPos) + 1] = (int) c;
        return (c >>> 32) == 0 ? 0 : incAt(len, z, zOff, zPos + 2);
    }

    public static int add33To(int len, int x, int[] z) {
        long c = (((long) z[0]) & M) + (((long) x) & M);
        z[0] = (int) c;
        c = (c >>> 32) + ((((long) z[1]) & M) + 1);
        z[1] = (int) c;
        if ((c >>> 32) == 0) {
            return 0;
        }
        return incAt(len, z, 2);
    }

    public static int add33To(int len, int x, int[] z, int zOff) {
        long c = (((long) z[zOff + 0]) & M) + (((long) x) & M);
        z[zOff + 0] = (int) c;
        c = (c >>> 32) + ((((long) z[zOff + 1]) & M) + 1);
        z[zOff + 1] = (int) c;
        return (c >>> 32) == 0 ? 0 : incAt(len, z, zOff, 2);
    }

    public static int addBothTo(int len, int[] x, int[] y, int[] z) {
        long c = 0;
        for (int i = 0; i < len; i++) {
            c += ((((long) x[i]) & M) + (((long) y[i]) & M)) + (((long) z[i]) & M);
            z[i] = (int) c;
            c >>>= 32;
        }
        return (int) c;
    }

    public static int addBothTo(int len, int[] x, int xOff, int[] y, int yOff, int[] z, int zOff) {
        long c = 0;
        for (int i = 0; i < len; i++) {
            c += ((((long) x[xOff + i]) & M) + (((long) y[yOff + i]) & M)) + (((long) z[zOff + i]) & M);
            z[zOff + i] = (int) c;
            c >>>= 32;
        }
        return (int) c;
    }

    public static int addDWordAt(int len, long x, int[] z, int zPos) {
        long c = (((long) z[zPos + 0]) & M) + (x & M);
        z[zPos + 0] = (int) c;
        c = (c >>> 32) + ((((long) z[zPos + 1]) & M) + (x >>> 32));
        z[zPos + 1] = (int) c;
        return (c >>> 32) == 0 ? 0 : incAt(len, z, zPos + 2);
    }

    public static int addDWordAt(int len, long x, int[] z, int zOff, int zPos) {
        long c = (((long) z[zOff + zPos]) & M) + (x & M);
        z[zOff + zPos] = (int) c;
        c = (c >>> 32) + ((((long) z[(zOff + zPos) + 1]) & M) + (x >>> 32));
        z[(zOff + zPos) + 1] = (int) c;
        return (c >>> 32) == 0 ? 0 : incAt(len, z, zOff, zPos + 2);
    }

    public static int addDWordTo(int len, long x, int[] z) {
        long c = (((long) z[0]) & M) + (x & M);
        z[0] = (int) c;
        c = (c >>> 32) + ((((long) z[1]) & M) + (x >>> 32));
        z[1] = (int) c;
        if ((c >>> 32) == 0) {
            return 0;
        }
        return incAt(len, z, 2);
    }

    public static int addDWordTo(int len, long x, int[] z, int zOff) {
        long c = (((long) z[zOff + 0]) & M) + (x & M);
        z[zOff + 0] = (int) c;
        c = (c >>> 32) + ((((long) z[zOff + 1]) & M) + (x >>> 32));
        z[zOff + 1] = (int) c;
        return (c >>> 32) == 0 ? 0 : incAt(len, z, zOff, 2);
    }

    public static int addTo(int len, int[] x, int[] z) {
        long c = 0;
        for (int i = 0; i < len; i++) {
            c += (((long) x[i]) & M) + (((long) z[i]) & M);
            z[i] = (int) c;
            c >>>= 32;
        }
        return (int) c;
    }

    public static int addTo(int len, int[] x, int xOff, int[] z, int zOff) {
        long c = 0;
        for (int i = 0; i < len; i++) {
            c += (((long) x[xOff + i]) & M) + (((long) z[zOff + i]) & M);
            z[zOff + i] = (int) c;
            c >>>= 32;
        }
        return (int) c;
    }

    public static int addWordAt(int len, int x, int[] z, int zPos) {
        long c = (((long) x) & M) + (((long) z[zPos]) & M);
        z[zPos] = (int) c;
        return (c >>> 32) == 0 ? 0 : incAt(len, z, zPos + 1);
    }

    public static int addWordAt(int len, int x, int[] z, int zOff, int zPos) {
        long c = (((long) x) & M) + (((long) z[zOff + zPos]) & M);
        z[zOff + zPos] = (int) c;
        return (c >>> 32) == 0 ? 0 : incAt(len, z, zOff, zPos + 1);
    }

    public static int addWordTo(int len, int x, int[] z) {
        long c = (((long) x) & M) + (((long) z[0]) & M);
        z[0] = (int) c;
        if ((c >>> 32) == 0) {
            return 0;
        }
        return incAt(len, z, 1);
    }

    public static int addWordTo(int len, int x, int[] z, int zOff) {
        long c = (((long) x) & M) + (((long) z[zOff]) & M);
        z[zOff] = (int) c;
        return (c >>> 32) == 0 ? 0 : incAt(len, z, zOff, 1);
    }

    public static int[] copy(int len, int[] x) {
        int[] z = new int[len];
        System.arraycopy(x, 0, z, 0, len);
        return z;
    }

    public static void copy(int len, int[] x, int[] z) {
        System.arraycopy(x, 0, z, 0, len);
    }

    public static int[] create(int len) {
        return new int[len];
    }

    public static long[] create64(int len) {
        return new long[len];
    }

    public static int dec(int len, int[] z) {
        for (int i = 0; i < len; i++) {
            int i2 = z[i] - 1;
            z[i] = i2;
            if (i2 != -1) {
                return 0;
            }
        }
        return -1;
    }

    public static int dec(int len, int[] x, int[] z) {
        int i = 0;
        while (i < len) {
            int c = x[i] - 1;
            z[i] = c;
            i++;
            if (c != -1) {
                while (i < len) {
                    z[i] = x[i];
                    i++;
                }
                return 0;
            }
        }
        return -1;
    }

    public static int decAt(int len, int[] z, int zPos) {
        for (int i = zPos; i < len; i++) {
            int i2 = z[i] - 1;
            z[i] = i2;
            if (i2 != -1) {
                return 0;
            }
        }
        return -1;
    }

    public static int decAt(int len, int[] z, int zOff, int zPos) {
        for (int i = zPos; i < len; i++) {
            int i2 = zOff + i;
            int i3 = z[i2] - 1;
            z[i2] = i3;
            if (i3 != -1) {
                return 0;
            }
        }
        return -1;
    }

    public static boolean eq(int len, int[] x, int[] y) {
        for (int i = len - 1; i >= 0; i--) {
            if (x[i] != y[i]) {
                return false;
            }
        }
        return true;
    }

    public static int[] fromBigInteger(int bits, BigInteger x) {
        if (x.signum() < 0 || x.bitLength() > bits) {
            throw new IllegalArgumentException();
        }
        int[] z = create((bits + 31) >> 5);
        int i = 0;
        while (x.signum() != 0) {
            int i2 = i + 1;
            z[i] = x.intValue();
            x = x.shiftRight(32);
            i = i2;
        }
        return z;
    }

    public static int getBit(int[] x, int bit) {
        if (bit == 0) {
            return x[0] & 1;
        }
        int w = bit >> 5;
        if (w < 0 || w >= x.length) {
            return 0;
        }
        return (x[w] >>> (bit & 31)) & 1;
    }

    public static boolean gte(int len, int[] x, int[] y) {
        for (int i = len - 1; i >= 0; i--) {
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

    public static int inc(int len, int[] z) {
        for (int i = 0; i < len; i++) {
            int i2 = z[i] + 1;
            z[i] = i2;
            if (i2 != 0) {
                return 0;
            }
        }
        return 1;
    }

    public static int inc(int len, int[] x, int[] z) {
        int i = 0;
        while (i < len) {
            int c = x[i] + 1;
            z[i] = c;
            i++;
            if (c != 0) {
                while (i < len) {
                    z[i] = x[i];
                    i++;
                }
                return 0;
            }
        }
        return 1;
    }

    public static int incAt(int len, int[] z, int zPos) {
        for (int i = zPos; i < len; i++) {
            int i2 = z[i] + 1;
            z[i] = i2;
            if (i2 != 0) {
                return 0;
            }
        }
        return 1;
    }

    public static int incAt(int len, int[] z, int zOff, int zPos) {
        for (int i = zPos; i < len; i++) {
            int i2 = zOff + i;
            int i3 = z[i2] + 1;
            z[i2] = i3;
            if (i3 != 0) {
                return 0;
            }
        }
        return 1;
    }

    public static boolean isOne(int len, int[] x) {
        if (x[0] != 1) {
            return false;
        }
        for (int i = 1; i < len; i++) {
            if (x[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean isZero(int len, int[] x) {
        for (int i = 0; i < len; i++) {
            if (x[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public static void mul(int len, int[] x, int[] y, int[] zz) {
        zz[len] = mulWord(len, x[0], y, zz);
        for (int i = 1; i < len; i++) {
            zz[i + len] = mulWordAddTo(len, x[i], y, 0, zz, i);
        }
    }

    public static void mul(int len, int[] x, int xOff, int[] y, int yOff, int[] zz, int zzOff) {
        zz[zzOff + len] = mulWord(len, x[xOff], y, yOff, zz, zzOff);
        for (int i = 1; i < len; i++) {
            zz[(zzOff + i) + len] = mulWordAddTo(len, x[xOff + i], y, yOff, zz, zzOff + i);
        }
    }

    public static int mulAddTo(int len, int[] x, int[] y, int[] zz) {
        long zc = 0;
        for (int i = 0; i < len; i++) {
            long c = (((long) mulWordAddTo(len, x[i], y, 0, zz, i)) & M) + ((((long) zz[i + len]) & M) + zc);
            zz[i + len] = (int) c;
            zc = c >>> 32;
        }
        return (int) zc;
    }

    public static int mulAddTo(int len, int[] x, int xOff, int[] y, int yOff, int[] zz, int zzOff) {
        long zc = 0;
        for (int i = 0; i < len; i++) {
            long c = (((long) mulWordAddTo(len, x[xOff + i], y, yOff, zz, zzOff)) & M) + ((((long) zz[zzOff + len]) & M) + zc);
            zz[zzOff + len] = (int) c;
            zc = c >>> 32;
            zzOff++;
        }
        return (int) zc;
    }

    public static int mul31BothAdd(int len, int a, int[] x, int b, int[] y, int[] z, int zOff) {
        long c = 0;
        long aVal = ((long) a) & M;
        long bVal = ((long) b) & M;
        int i = 0;
        do {
            c += (((((long) x[i]) & M) * aVal) + ((((long) y[i]) & M) * bVal)) + (((long) z[zOff + i]) & M);
            z[zOff + i] = (int) c;
            c >>>= 32;
            i++;
        } while (i < len);
        return (int) c;
    }

    public static int mulWord(int len, int x, int[] y, int[] z) {
        long c = 0;
        long xVal = ((long) x) & M;
        int i = 0;
        do {
            c += (((long) y[i]) & M) * xVal;
            z[i] = (int) c;
            c >>>= 32;
            i++;
        } while (i < len);
        return (int) c;
    }

    public static int mulWord(int len, int x, int[] y, int yOff, int[] z, int zOff) {
        long c = 0;
        long xVal = ((long) x) & M;
        int i = 0;
        do {
            c += (((long) y[yOff + i]) & M) * xVal;
            z[zOff + i] = (int) c;
            c >>>= 32;
            i++;
        } while (i < len);
        return (int) c;
    }

    public static int mulWordAddTo(int len, int x, int[] y, int yOff, int[] z, int zOff) {
        long c = 0;
        long xVal = ((long) x) & M;
        int i = 0;
        do {
            c += ((((long) y[yOff + i]) & M) * xVal) + (((long) z[zOff + i]) & M);
            z[zOff + i] = (int) c;
            c >>>= 32;
            i++;
        } while (i < len);
        return (int) c;
    }

    public static int mulWordDwordAddAt(int len, int x, long y, int[] z, int zPos) {
        long xVal = ((long) x) & M;
        long c = 0 + (((M & y) * xVal) + (((long) z[zPos + 0]) & M));
        z[zPos + 0] = (int) c;
        c = (c >>> 32) + (((y >>> 32) * xVal) + (((long) z[zPos + 1]) & M));
        z[zPos + 1] = (int) c;
        c = (c >>> 32) + (((long) z[zPos + 2]) & M);
        z[zPos + 2] = (int) c;
        return (c >>> 32) == 0 ? 0 : incAt(len, z, zPos + 3);
    }

    public static int shiftDownBit(int len, int[] z, int c) {
        int i = len;
        while (true) {
            i--;
            if (i < 0) {
                return c << 31;
            }
            int next = z[i];
            z[i] = (next >>> 1) | (c << 31);
            c = next;
        }
    }

    public static int shiftDownBit(int len, int[] z, int zOff, int c) {
        int i = len;
        while (true) {
            i--;
            if (i < 0) {
                return c << 31;
            }
            int next = z[zOff + i];
            z[zOff + i] = (next >>> 1) | (c << 31);
            c = next;
        }
    }

    public static int shiftDownBit(int len, int[] x, int c, int[] z) {
        int i = len;
        while (true) {
            i--;
            if (i < 0) {
                return c << 31;
            }
            int next = x[i];
            z[i] = (next >>> 1) | (c << 31);
            c = next;
        }
    }

    public static int shiftDownBit(int len, int[] x, int xOff, int c, int[] z, int zOff) {
        int i = len;
        while (true) {
            i--;
            if (i < 0) {
                return c << 31;
            }
            int next = x[xOff + i];
            z[zOff + i] = (next >>> 1) | (c << 31);
            c = next;
        }
    }

    public static int shiftDownBits(int len, int[] z, int bits, int c) {
        int i = len;
        while (true) {
            i--;
            if (i < 0) {
                return c << (-bits);
            }
            int next = z[i];
            z[i] = (next >>> bits) | (c << (-bits));
            c = next;
        }
    }

    public static int shiftDownBits(int len, int[] z, int zOff, int bits, int c) {
        int i = len;
        while (true) {
            i--;
            if (i < 0) {
                return c << (-bits);
            }
            int next = z[zOff + i];
            z[zOff + i] = (next >>> bits) | (c << (-bits));
            c = next;
        }
    }

    public static int shiftDownBits(int len, int[] x, int bits, int c, int[] z) {
        int i = len;
        while (true) {
            i--;
            if (i < 0) {
                return c << (-bits);
            }
            int next = x[i];
            z[i] = (next >>> bits) | (c << (-bits));
            c = next;
        }
    }

    public static int shiftDownBits(int len, int[] x, int xOff, int bits, int c, int[] z, int zOff) {
        int i = len;
        while (true) {
            i--;
            if (i < 0) {
                return c << (-bits);
            }
            int next = x[xOff + i];
            z[zOff + i] = (next >>> bits) | (c << (-bits));
            c = next;
        }
    }

    public static int shiftDownWord(int len, int[] z, int c) {
        int i = len;
        while (true) {
            i--;
            if (i < 0) {
                return c;
            }
            int next = z[i];
            z[i] = c;
            c = next;
        }
    }

    public static int shiftUpBit(int len, int[] z, int c) {
        for (int i = 0; i < len; i++) {
            int next = z[i];
            z[i] = (next << 1) | (c >>> 31);
            c = next;
        }
        return c >>> 31;
    }

    public static int shiftUpBit(int len, int[] z, int zOff, int c) {
        for (int i = 0; i < len; i++) {
            int next = z[zOff + i];
            z[zOff + i] = (next << 1) | (c >>> 31);
            c = next;
        }
        return c >>> 31;
    }

    public static int shiftUpBit(int len, int[] x, int c, int[] z) {
        for (int i = 0; i < len; i++) {
            int next = x[i];
            z[i] = (next << 1) | (c >>> 31);
            c = next;
        }
        return c >>> 31;
    }

    public static int shiftUpBit(int len, int[] x, int xOff, int c, int[] z, int zOff) {
        for (int i = 0; i < len; i++) {
            int next = x[xOff + i];
            z[zOff + i] = (next << 1) | (c >>> 31);
            c = next;
        }
        return c >>> 31;
    }

    public static long shiftUpBit64(int len, long[] x, int xOff, long c, long[] z, int zOff) {
        for (int i = 0; i < len; i++) {
            long next = x[xOff + i];
            z[zOff + i] = (next << 1) | (c >>> 63);
            c = next;
        }
        return c >>> 63;
    }

    public static int shiftUpBits(int len, int[] z, int bits, int c) {
        for (int i = 0; i < len; i++) {
            int next = z[i];
            z[i] = (next << bits) | (c >>> (-bits));
            c = next;
        }
        return c >>> (-bits);
    }

    public static int shiftUpBits(int len, int[] z, int zOff, int bits, int c) {
        for (int i = 0; i < len; i++) {
            int next = z[zOff + i];
            z[zOff + i] = (next << bits) | (c >>> (-bits));
            c = next;
        }
        return c >>> (-bits);
    }

    public static long shiftUpBits64(int len, long[] z, int zOff, int bits, long c) {
        for (int i = 0; i < len; i++) {
            long next = z[zOff + i];
            z[zOff + i] = (next << bits) | (c >>> (-bits));
            c = next;
        }
        return c >>> (-bits);
    }

    public static int shiftUpBits(int len, int[] x, int bits, int c, int[] z) {
        for (int i = 0; i < len; i++) {
            int next = x[i];
            z[i] = (next << bits) | (c >>> (-bits));
            c = next;
        }
        return c >>> (-bits);
    }

    public static int shiftUpBits(int len, int[] x, int xOff, int bits, int c, int[] z, int zOff) {
        for (int i = 0; i < len; i++) {
            int next = x[xOff + i];
            z[zOff + i] = (next << bits) | (c >>> (-bits));
            c = next;
        }
        return c >>> (-bits);
    }

    public static long shiftUpBits64(int len, long[] x, int xOff, int bits, long c, long[] z, int zOff) {
        for (int i = 0; i < len; i++) {
            long next = x[xOff + i];
            z[zOff + i] = (next << bits) | (c >>> (-bits));
            c = next;
        }
        return c >>> (-bits);
    }

    public static void square(int len, int[] x, int[] zz) {
        int extLen = len << 1;
        int c = 0;
        int j = len;
        int k = extLen;
        do {
            j--;
            long xVal = ((long) x[j]) & M;
            long p = xVal * xVal;
            k--;
            zz[k] = (c << 31) | ((int) (p >>> 33));
            k--;
            zz[k] = (int) (p >>> 1);
            c = (int) p;
        } while (j > 0);
        for (int i = 1; i < len; i++) {
            addWordAt(extLen, squareWordAdd(x, i, zz), zz, i << 1);
        }
        shiftUpBit(extLen, zz, x[0] << 31);
    }

    public static void square(int len, int[] x, int xOff, int[] zz, int zzOff) {
        int extLen = len << 1;
        int c = 0;
        int j = len;
        int k = extLen;
        do {
            j--;
            long xVal = ((long) x[xOff + j]) & M;
            long p = xVal * xVal;
            k--;
            zz[zzOff + k] = (c << 31) | ((int) (p >>> 33));
            k--;
            zz[zzOff + k] = (int) (p >>> 1);
            c = (int) p;
        } while (j > 0);
        for (int i = 1; i < len; i++) {
            addWordAt(extLen, squareWordAdd(x, xOff, i, zz, zzOff), zz, zzOff, i << 1);
        }
        shiftUpBit(extLen, zz, zzOff, x[xOff] << 31);
    }

    public static int squareWordAdd(int[] x, int xPos, int[] z) {
        long c = 0;
        long xVal = ((long) x[xPos]) & M;
        int i = 0;
        do {
            c += ((((long) x[i]) & M) * xVal) + (((long) z[xPos + i]) & M);
            z[xPos + i] = (int) c;
            c >>>= 32;
            i++;
        } while (i < xPos);
        return (int) c;
    }

    public static int squareWordAdd(int[] x, int xOff, int xPos, int[] z, int zOff) {
        long c = 0;
        long xVal = ((long) x[xOff + xPos]) & M;
        int i = 0;
        do {
            c += ((((long) x[xOff + i]) & M) * xVal) + (((long) z[xPos + zOff]) & M);
            z[xPos + zOff] = (int) c;
            c >>>= 32;
            zOff++;
            i++;
        } while (i < xPos);
        return (int) c;
    }

    public static int sub(int len, int[] x, int[] y, int[] z) {
        long c = 0;
        for (int i = 0; i < len; i++) {
            c += (((long) x[i]) & M) - (((long) y[i]) & M);
            z[i] = (int) c;
            c >>= 32;
        }
        return (int) c;
    }

    public static int sub(int len, int[] x, int xOff, int[] y, int yOff, int[] z, int zOff) {
        long c = 0;
        for (int i = 0; i < len; i++) {
            c += (((long) x[xOff + i]) & M) - (((long) y[yOff + i]) & M);
            z[zOff + i] = (int) c;
            c >>= 32;
        }
        return (int) c;
    }

    public static int sub33At(int len, int x, int[] z, int zPos) {
        long c = (((long) z[zPos + 0]) & M) - (((long) x) & M);
        z[zPos + 0] = (int) c;
        c = (c >> 32) + ((((long) z[zPos + 1]) & M) - 1);
        z[zPos + 1] = (int) c;
        return (c >> 32) == 0 ? 0 : decAt(len, z, zPos + 2);
    }

    public static int sub33At(int len, int x, int[] z, int zOff, int zPos) {
        long c = (((long) z[zOff + zPos]) & M) - (((long) x) & M);
        z[zOff + zPos] = (int) c;
        c = (c >> 32) + ((((long) z[(zOff + zPos) + 1]) & M) - 1);
        z[(zOff + zPos) + 1] = (int) c;
        return (c >> 32) == 0 ? 0 : decAt(len, z, zOff, zPos + 2);
    }

    public static int sub33From(int len, int x, int[] z) {
        long c = (((long) z[0]) & M) - (((long) x) & M);
        z[0] = (int) c;
        c = (c >> 32) + ((((long) z[1]) & M) - 1);
        z[1] = (int) c;
        if ((c >> 32) == 0) {
            return 0;
        }
        return decAt(len, z, 2);
    }

    public static int sub33From(int len, int x, int[] z, int zOff) {
        long c = (((long) z[zOff + 0]) & M) - (((long) x) & M);
        z[zOff + 0] = (int) c;
        c = (c >> 32) + ((((long) z[zOff + 1]) & M) - 1);
        z[zOff + 1] = (int) c;
        return (c >> 32) == 0 ? 0 : decAt(len, z, zOff, 2);
    }

    public static int subBothFrom(int len, int[] x, int[] y, int[] z) {
        long c = 0;
        for (int i = 0; i < len; i++) {
            c += ((((long) z[i]) & M) - (((long) x[i]) & M)) - (((long) y[i]) & M);
            z[i] = (int) c;
            c >>= 32;
        }
        return (int) c;
    }

    public static int subBothFrom(int len, int[] x, int xOff, int[] y, int yOff, int[] z, int zOff) {
        long c = 0;
        for (int i = 0; i < len; i++) {
            c += ((((long) z[zOff + i]) & M) - (((long) x[xOff + i]) & M)) - (((long) y[yOff + i]) & M);
            z[zOff + i] = (int) c;
            c >>= 32;
        }
        return (int) c;
    }

    public static int subDWordAt(int len, long x, int[] z, int zPos) {
        long c = (((long) z[zPos + 0]) & M) - (x & M);
        z[zPos + 0] = (int) c;
        c = (c >> 32) + ((((long) z[zPos + 1]) & M) - (x >>> 32));
        z[zPos + 1] = (int) c;
        return (c >> 32) == 0 ? 0 : decAt(len, z, zPos + 2);
    }

    public static int subDWordAt(int len, long x, int[] z, int zOff, int zPos) {
        long c = (((long) z[zOff + zPos]) & M) - (x & M);
        z[zOff + zPos] = (int) c;
        c = (c >> 32) + ((((long) z[(zOff + zPos) + 1]) & M) - (x >>> 32));
        z[(zOff + zPos) + 1] = (int) c;
        return (c >> 32) == 0 ? 0 : decAt(len, z, zOff, zPos + 2);
    }

    public static int subDWordFrom(int len, long x, int[] z) {
        long c = (((long) z[0]) & M) - (x & M);
        z[0] = (int) c;
        c = (c >> 32) + ((((long) z[1]) & M) - (x >>> 32));
        z[1] = (int) c;
        if ((c >> 32) == 0) {
            return 0;
        }
        return decAt(len, z, 2);
    }

    public static int subDWordFrom(int len, long x, int[] z, int zOff) {
        long c = (((long) z[zOff + 0]) & M) - (x & M);
        z[zOff + 0] = (int) c;
        c = (c >> 32) + ((((long) z[zOff + 1]) & M) - (x >>> 32));
        z[zOff + 1] = (int) c;
        return (c >> 32) == 0 ? 0 : decAt(len, z, zOff, 2);
    }

    public static int subFrom(int len, int[] x, int[] z) {
        long c = 0;
        for (int i = 0; i < len; i++) {
            c += (((long) z[i]) & M) - (((long) x[i]) & M);
            z[i] = (int) c;
            c >>= 32;
        }
        return (int) c;
    }

    public static int subFrom(int len, int[] x, int xOff, int[] z, int zOff) {
        long c = 0;
        for (int i = 0; i < len; i++) {
            c += (((long) z[zOff + i]) & M) - (((long) x[xOff + i]) & M);
            z[zOff + i] = (int) c;
            c >>= 32;
        }
        return (int) c;
    }

    public static int subWordAt(int len, int x, int[] z, int zPos) {
        long c = (((long) z[zPos]) & M) - (((long) x) & M);
        z[zPos] = (int) c;
        return (c >> 32) == 0 ? 0 : decAt(len, z, zPos + 1);
    }

    public static int subWordAt(int len, int x, int[] z, int zOff, int zPos) {
        long c = (((long) z[zOff + zPos]) & M) - (((long) x) & M);
        z[zOff + zPos] = (int) c;
        return (c >> 32) == 0 ? 0 : decAt(len, z, zOff, zPos + 1);
    }

    public static int subWordFrom(int len, int x, int[] z) {
        long c = (((long) z[0]) & M) - (((long) x) & M);
        z[0] = (int) c;
        if ((c >> 32) == 0) {
            return 0;
        }
        return decAt(len, z, 1);
    }

    public static int subWordFrom(int len, int x, int[] z, int zOff) {
        long c = (((long) z[zOff + 0]) & M) - (((long) x) & M);
        z[zOff + 0] = (int) c;
        return (c >> 32) == 0 ? 0 : decAt(len, z, zOff, 1);
    }

    public static BigInteger toBigInteger(int len, int[] x) {
        byte[] bs = new byte[(len << 2)];
        for (int i = 0; i < len; i++) {
            int x_i = x[i];
            if (x_i != 0) {
                Pack.intToBigEndian(x_i, bs, ((len - 1) - i) << 2);
            }
        }
        return new BigInteger(1, bs);
    }

    public static void zero(int len, int[] z) {
        for (int i = 0; i < len; i++) {
            z[i] = 0;
        }
    }
}
