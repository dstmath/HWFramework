package com.android.org.bouncycastle.math.ec.custom.sec;

import com.android.org.bouncycastle.math.raw.Nat;
import com.android.org.bouncycastle.math.raw.Nat192;
import java.math.BigInteger;

public class SecP192K1Field {
    static final int[] P = new int[]{-4553, -2, -1, -1, -1, -1};
    private static final int P5 = -1;
    static final int[] PExt = new int[]{20729809, 9106, 1, 0, 0, 0, -9106, -3, -1, -1, -1, -1};
    private static final int PExt11 = -1;
    private static final int[] PExtInv = new int[]{-20729809, -9107, -2, -1, -1, -1, 9105, 2};
    private static final int PInv33 = 4553;

    public static void add(int[] x, int[] y, int[] z) {
        if (Nat192.add(x, y, z) != 0 || (z[5] == -1 && Nat192.gte(z, P))) {
            Nat.add33To(6, PInv33, z);
        }
    }

    public static void addExt(int[] xx, int[] yy, int[] zz) {
        if ((Nat.add(12, xx, yy, zz) != 0 || (zz[11] == -1 && Nat.gte(12, zz, PExt))) && Nat.addTo(PExtInv.length, PExtInv, zz) != 0) {
            Nat.incAt(12, zz, PExtInv.length);
        }
    }

    public static void addOne(int[] x, int[] z) {
        if (Nat.inc(6, x, z) != 0 || (z[5] == -1 && Nat192.gte(z, P))) {
            Nat.add33To(6, PInv33, z);
        }
    }

    public static int[] fromBigInteger(BigInteger x) {
        int[] z = Nat192.fromBigInteger(x);
        if (z[5] == -1 && Nat192.gte(z, P)) {
            Nat192.subFrom(P, z);
        }
        return z;
    }

    public static void half(int[] x, int[] z) {
        if ((x[0] & 1) == 0) {
            Nat.shiftDownBit(6, x, 0, z);
        } else {
            Nat.shiftDownBit(6, z, Nat192.add(x, P, z));
        }
    }

    public static void multiply(int[] x, int[] y, int[] z) {
        int[] tt = Nat192.createExt();
        Nat192.mul(x, y, tt);
        reduce(tt, z);
    }

    public static void multiplyAddToExt(int[] x, int[] y, int[] zz) {
        if ((Nat192.mulAddTo(x, y, zz) != 0 || (zz[11] == -1 && Nat.gte(12, zz, PExt))) && Nat.addTo(PExtInv.length, PExtInv, zz) != 0) {
            Nat.incAt(12, zz, PExtInv.length);
        }
    }

    public static void negate(int[] x, int[] z) {
        if (Nat192.isZero(x)) {
            Nat192.zero(z);
        } else {
            Nat192.sub(P, x, z);
        }
    }

    public static void reduce(int[] xx, int[] z) {
        if (Nat192.mul33DWordAdd(PInv33, Nat192.mul33Add(PInv33, xx, 6, xx, 0, z, 0), z, 0) != 0 || (z[5] == -1 && Nat192.gte(z, P))) {
            Nat.add33To(6, PInv33, z);
        }
    }

    public static void reduce32(int x, int[] z) {
        if ((x != 0 && Nat192.mul33WordAdd(PInv33, x, z, 0) != 0) || (z[5] == -1 && Nat192.gte(z, P))) {
            Nat.add33To(6, PInv33, z);
        }
    }

    public static void square(int[] x, int[] z) {
        int[] tt = Nat192.createExt();
        Nat192.square(x, tt);
        reduce(tt, z);
    }

    public static void squareN(int[] x, int n, int[] z) {
        int[] tt = Nat192.createExt();
        Nat192.square(x, tt);
        reduce(tt, z);
        while (true) {
            n--;
            if (n > 0) {
                Nat192.square(z, tt);
                reduce(tt, z);
            } else {
                return;
            }
        }
    }

    public static void subtract(int[] x, int[] y, int[] z) {
        if (Nat192.sub(x, y, z) != 0) {
            Nat.sub33From(6, PInv33, z);
        }
    }

    public static void subtractExt(int[] xx, int[] yy, int[] zz) {
        if (Nat.sub(12, xx, yy, zz) != 0 && Nat.subFrom(PExtInv.length, PExtInv, zz) != 0) {
            Nat.decAt(12, zz, PExtInv.length);
        }
    }

    public static void twice(int[] x, int[] z) {
        if (Nat.shiftUpBit(6, x, 0, z) != 0 || (z[5] == -1 && Nat192.gte(z, P))) {
            Nat.add33To(6, PInv33, z);
        }
    }
}
