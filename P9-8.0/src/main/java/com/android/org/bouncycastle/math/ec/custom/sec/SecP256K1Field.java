package com.android.org.bouncycastle.math.ec.custom.sec;

import com.android.org.bouncycastle.math.raw.Nat;
import com.android.org.bouncycastle.math.raw.Nat256;
import java.math.BigInteger;

public class SecP256K1Field {
    static final int[] P = new int[]{-977, -2, -1, -1, -1, -1, -1, -1};
    private static final int P7 = -1;
    static final int[] PExt = new int[]{954529, 1954, 1, 0, 0, 0, 0, 0, -1954, -3, -1, -1, -1, -1, -1, -1};
    private static final int PExt15 = -1;
    private static final int[] PExtInv = new int[]{-954529, -1955, -2, -1, -1, -1, -1, -1, 1953, 2};
    private static final int PInv33 = 977;

    public static void add(int[] x, int[] y, int[] z) {
        if (Nat256.add(x, y, z) != 0 || (z[7] == -1 && Nat256.gte(z, P))) {
            Nat.add33To(8, PInv33, z);
        }
    }

    public static void addExt(int[] xx, int[] yy, int[] zz) {
        if ((Nat.add(16, xx, yy, zz) != 0 || (zz[15] == -1 && Nat.gte(16, zz, PExt))) && Nat.addTo(PExtInv.length, PExtInv, zz) != 0) {
            Nat.incAt(16, zz, PExtInv.length);
        }
    }

    public static void addOne(int[] x, int[] z) {
        if (Nat.inc(8, x, z) != 0 || (z[7] == -1 && Nat256.gte(z, P))) {
            Nat.add33To(8, PInv33, z);
        }
    }

    public static int[] fromBigInteger(BigInteger x) {
        int[] z = Nat256.fromBigInteger(x);
        if (z[7] == -1 && Nat256.gte(z, P)) {
            Nat256.subFrom(P, z);
        }
        return z;
    }

    public static void half(int[] x, int[] z) {
        if ((x[0] & 1) == 0) {
            Nat.shiftDownBit(8, x, 0, z);
        } else {
            Nat.shiftDownBit(8, z, Nat256.add(x, P, z));
        }
    }

    public static void multiply(int[] x, int[] y, int[] z) {
        int[] tt = Nat256.createExt();
        Nat256.mul(x, y, tt);
        reduce(tt, z);
    }

    public static void multiplyAddToExt(int[] x, int[] y, int[] zz) {
        if ((Nat256.mulAddTo(x, y, zz) != 0 || (zz[15] == -1 && Nat.gte(16, zz, PExt))) && Nat.addTo(PExtInv.length, PExtInv, zz) != 0) {
            Nat.incAt(16, zz, PExtInv.length);
        }
    }

    public static void negate(int[] x, int[] z) {
        if (Nat256.isZero(x)) {
            Nat256.zero(z);
        } else {
            Nat256.sub(P, x, z);
        }
    }

    public static void reduce(int[] xx, int[] z) {
        if (Nat256.mul33DWordAdd(PInv33, Nat256.mul33Add(PInv33, xx, 8, xx, 0, z, 0), z, 0) != 0 || (z[7] == -1 && Nat256.gte(z, P))) {
            Nat.add33To(8, PInv33, z);
        }
    }

    public static void reduce32(int x, int[] z) {
        if ((x != 0 && Nat256.mul33WordAdd(PInv33, x, z, 0) != 0) || (z[7] == -1 && Nat256.gte(z, P))) {
            Nat.add33To(8, PInv33, z);
        }
    }

    public static void square(int[] x, int[] z) {
        int[] tt = Nat256.createExt();
        Nat256.square(x, tt);
        reduce(tt, z);
    }

    public static void squareN(int[] x, int n, int[] z) {
        int[] tt = Nat256.createExt();
        Nat256.square(x, tt);
        reduce(tt, z);
        while (true) {
            n--;
            if (n > 0) {
                Nat256.square(z, tt);
                reduce(tt, z);
            } else {
                return;
            }
        }
    }

    public static void subtract(int[] x, int[] y, int[] z) {
        if (Nat256.sub(x, y, z) != 0) {
            Nat.sub33From(8, PInv33, z);
        }
    }

    public static void subtractExt(int[] xx, int[] yy, int[] zz) {
        if (Nat.sub(16, xx, yy, zz) != 0 && Nat.subFrom(PExtInv.length, PExtInv, zz) != 0) {
            Nat.decAt(16, zz, PExtInv.length);
        }
    }

    public static void twice(int[] x, int[] z) {
        if (Nat.shiftUpBit(8, x, 0, z) != 0 || (z[7] == -1 && Nat256.gte(z, P))) {
            Nat.add33To(8, PInv33, z);
        }
    }
}
