package com.android.org.bouncycastle.math.ec.custom.sec;

import com.android.org.bouncycastle.math.raw.Nat;
import com.android.org.bouncycastle.math.raw.Nat224;
import java.math.BigInteger;

public class SecP224K1Field {
    static final int[] P = new int[]{-6803, -2, -1, -1, -1, -1, -1};
    private static final int P6 = -1;
    static final int[] PExt = new int[]{46280809, 13606, 1, 0, 0, 0, 0, -13606, -3, -1, -1, -1, -1, -1};
    private static final int PExt13 = -1;
    private static final int[] PExtInv = new int[]{-46280809, -13607, -2, -1, -1, -1, -1, 13605, 2};
    private static final int PInv33 = 6803;

    public static void add(int[] x, int[] y, int[] z) {
        if (Nat224.add(x, y, z) != 0 || (z[6] == -1 && Nat224.gte(z, P))) {
            Nat.add33To(7, PInv33, z);
        }
    }

    public static void addExt(int[] xx, int[] yy, int[] zz) {
        if ((Nat.add(14, xx, yy, zz) != 0 || (zz[13] == -1 && Nat.gte(14, zz, PExt))) && Nat.addTo(PExtInv.length, PExtInv, zz) != 0) {
            Nat.incAt(14, zz, PExtInv.length);
        }
    }

    public static void addOne(int[] x, int[] z) {
        if (Nat.inc(7, x, z) != 0 || (z[6] == -1 && Nat224.gte(z, P))) {
            Nat.add33To(7, PInv33, z);
        }
    }

    public static int[] fromBigInteger(BigInteger x) {
        int[] z = Nat224.fromBigInteger(x);
        if (z[6] == -1 && Nat224.gte(z, P)) {
            Nat.add33To(7, PInv33, z);
        }
        return z;
    }

    public static void half(int[] x, int[] z) {
        if ((x[0] & 1) == 0) {
            Nat.shiftDownBit(7, x, 0, z);
        } else {
            Nat.shiftDownBit(7, z, Nat224.add(x, P, z));
        }
    }

    public static void multiply(int[] x, int[] y, int[] z) {
        int[] tt = Nat224.createExt();
        Nat224.mul(x, y, tt);
        reduce(tt, z);
    }

    public static void multiplyAddToExt(int[] x, int[] y, int[] zz) {
        if ((Nat224.mulAddTo(x, y, zz) != 0 || (zz[13] == -1 && Nat.gte(14, zz, PExt))) && Nat.addTo(PExtInv.length, PExtInv, zz) != 0) {
            Nat.incAt(14, zz, PExtInv.length);
        }
    }

    public static void negate(int[] x, int[] z) {
        if (Nat224.isZero(x)) {
            Nat224.zero(z);
        } else {
            Nat224.sub(P, x, z);
        }
    }

    public static void reduce(int[] xx, int[] z) {
        if (Nat224.mul33DWordAdd(PInv33, Nat224.mul33Add(PInv33, xx, 7, xx, 0, z, 0), z, 0) != 0 || (z[6] == -1 && Nat224.gte(z, P))) {
            Nat.add33To(7, PInv33, z);
        }
    }

    public static void reduce32(int x, int[] z) {
        if ((x != 0 && Nat224.mul33WordAdd(PInv33, x, z, 0) != 0) || (z[6] == -1 && Nat224.gte(z, P))) {
            Nat.add33To(7, PInv33, z);
        }
    }

    public static void square(int[] x, int[] z) {
        int[] tt = Nat224.createExt();
        Nat224.square(x, tt);
        reduce(tt, z);
    }

    public static void squareN(int[] x, int n, int[] z) {
        int[] tt = Nat224.createExt();
        Nat224.square(x, tt);
        reduce(tt, z);
        while (true) {
            n--;
            if (n > 0) {
                Nat224.square(z, tt);
                reduce(tt, z);
            } else {
                return;
            }
        }
    }

    public static void subtract(int[] x, int[] y, int[] z) {
        if (Nat224.sub(x, y, z) != 0) {
            Nat.sub33From(7, PInv33, z);
        }
    }

    public static void subtractExt(int[] xx, int[] yy, int[] zz) {
        if (Nat.sub(14, xx, yy, zz) != 0 && Nat.subFrom(PExtInv.length, PExtInv, zz) != 0) {
            Nat.decAt(14, zz, PExtInv.length);
        }
    }

    public static void twice(int[] x, int[] z) {
        if (Nat.shiftUpBit(7, x, 0, z) != 0 || (z[6] == -1 && Nat224.gte(z, P))) {
            Nat.add33To(7, PInv33, z);
        }
    }
}
