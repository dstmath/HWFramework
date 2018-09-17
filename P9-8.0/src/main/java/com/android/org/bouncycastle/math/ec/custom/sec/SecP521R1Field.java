package com.android.org.bouncycastle.math.ec.custom.sec;

import com.android.org.bouncycastle.math.raw.Nat;
import com.android.org.bouncycastle.math.raw.Nat512;
import java.math.BigInteger;

public class SecP521R1Field {
    static final int[] P = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, P16};
    private static final int P16 = 511;

    public static void add(int[] x, int[] y, int[] z) {
        int c = (Nat.add(16, x, y, z) + x[16]) + y[16];
        if (c > P16 || (c == P16 && Nat.eq(16, z, P))) {
            c = (c + Nat.inc(16, z)) & P16;
        }
        z[16] = c;
    }

    public static void addOne(int[] x, int[] z) {
        int c = Nat.inc(16, x, z) + x[16];
        if (c > P16 || (c == P16 && Nat.eq(16, z, P))) {
            c = (c + Nat.inc(16, z)) & P16;
        }
        z[16] = c;
    }

    public static int[] fromBigInteger(BigInteger x) {
        int[] z = Nat.fromBigInteger(521, x);
        if (Nat.eq(17, z, P)) {
            Nat.zero(17, z);
        }
        return z;
    }

    public static void half(int[] x, int[] z) {
        int x16 = x[16];
        z[16] = (x16 >>> 1) | (Nat.shiftDownBit(16, x, x16, z) >>> 23);
    }

    public static void multiply(int[] x, int[] y, int[] z) {
        int[] tt = Nat.create(33);
        implMultiply(x, y, tt);
        reduce(tt, z);
    }

    public static void negate(int[] x, int[] z) {
        if (Nat.isZero(17, x)) {
            Nat.zero(17, z);
        } else {
            Nat.sub(17, P, x, z);
        }
    }

    public static void reduce(int[] xx, int[] z) {
        int xx32 = xx[32];
        int c = ((Nat.shiftDownBits(16, xx, 16, 9, xx32, z, 0) >>> 23) + (xx32 >>> 9)) + Nat.addTo(16, xx, z);
        if (c > P16 || (c == P16 && Nat.eq(16, z, P))) {
            c = (c + Nat.inc(16, z)) & P16;
        }
        z[16] = c;
    }

    public static void reduce23(int[] z) {
        int z16 = z[16];
        int c = Nat.addWordTo(16, z16 >>> 9, z) + (z16 & P16);
        if (c > P16 || (c == P16 && Nat.eq(16, z, P))) {
            c = (c + Nat.inc(16, z)) & P16;
        }
        z[16] = c;
    }

    public static void square(int[] x, int[] z) {
        int[] tt = Nat.create(33);
        implSquare(x, tt);
        reduce(tt, z);
    }

    public static void squareN(int[] x, int n, int[] z) {
        int[] tt = Nat.create(33);
        implSquare(x, tt);
        reduce(tt, z);
        while (true) {
            n--;
            if (n > 0) {
                implSquare(z, tt);
                reduce(tt, z);
            } else {
                return;
            }
        }
    }

    public static void subtract(int[] x, int[] y, int[] z) {
        int c = (Nat.sub(16, x, y, z) + x[16]) - y[16];
        if (c < 0) {
            c = (c + Nat.dec(16, z)) & P16;
        }
        z[16] = c;
    }

    public static void twice(int[] x, int[] z) {
        int x16 = x[16];
        z[16] = (Nat.shiftUpBit(16, x, x16 << 23, z) | (x16 << 1)) & P16;
    }

    protected static void implMultiply(int[] x, int[] y, int[] zz) {
        Nat512.mul(x, y, zz);
        int x16 = x[16];
        int y16 = y[16];
        zz[32] = Nat.mul31BothAdd(16, x16, y, y16, x, zz, 16) + (x16 * y16);
    }

    protected static void implSquare(int[] x, int[] zz) {
        Nat512.square(x, zz);
        int x16 = x[16];
        zz[32] = Nat.mulWordAddTo(16, x16 << 1, x, 0, zz, 16) + (x16 * x16);
    }
}
