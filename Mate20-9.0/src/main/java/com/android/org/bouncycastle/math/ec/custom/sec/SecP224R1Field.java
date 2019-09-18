package com.android.org.bouncycastle.math.ec.custom.sec;

import com.android.org.bouncycastle.math.raw.Nat;
import com.android.org.bouncycastle.math.raw.Nat224;
import java.math.BigInteger;

public class SecP224R1Field {
    private static final long M = 4294967295L;
    static final int[] P = {1, 0, 0, -1, -1, -1, -1};
    private static final int P6 = -1;
    static final int[] PExt = {1, 0, 0, -2, -1, -1, 0, 2, 0, 0, -2, -1, -1, -1};
    private static final int PExt13 = -1;
    private static final int[] PExtInv = {-1, -1, -1, 1, 0, 0, -1, -3, -1, -1, 1};

    public static void add(int[] x, int[] y, int[] z) {
        if (Nat224.add(x, y, z) != 0 || (z[6] == -1 && Nat224.gte(z, P))) {
            addPInvTo(z);
        }
    }

    public static void addExt(int[] xx, int[] yy, int[] zz) {
        if ((Nat.add(14, xx, yy, zz) != 0 || (zz[13] == -1 && Nat.gte(14, zz, PExt))) && Nat.addTo(PExtInv.length, PExtInv, zz) != 0) {
            Nat.incAt(14, zz, PExtInv.length);
        }
    }

    public static void addOne(int[] x, int[] z) {
        if (Nat.inc(7, x, z) != 0 || (z[6] == -1 && Nat224.gte(z, P))) {
            addPInvTo(z);
        }
    }

    public static int[] fromBigInteger(BigInteger x) {
        int[] z = Nat224.fromBigInteger(x);
        if (z[6] == -1 && Nat224.gte(z, P)) {
            Nat224.subFrom(P, z);
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
        int[] iArr = z;
        long xx10 = ((long) xx[10]) & M;
        long xx11 = ((long) xx[11]) & M;
        long xx12 = ((long) xx[12]) & M;
        long xx13 = ((long) xx[13]) & M;
        long t0 = ((((long) xx[7]) & M) + xx11) - 1;
        long t1 = (((long) xx[8]) & M) + xx12;
        long xx122 = xx12;
        long t2 = (((long) xx[9]) & M) + xx13;
        long xx132 = xx13;
        long cc = 0 + ((((long) xx[0]) & M) - t0);
        long z0 = cc & M;
        long cc2 = (cc >> 32) + ((((long) xx[1]) & M) - t1);
        iArr[1] = (int) cc2;
        long cc3 = (cc2 >> 32) + ((((long) xx[2]) & M) - t2);
        iArr[2] = (int) cc3;
        long cc4 = (cc3 >> 32) + (((((long) xx[3]) & M) + t0) - xx10);
        long z3 = cc4 & M;
        long j = t0;
        long cc5 = (cc4 >> 32) + (((((long) xx[4]) & M) + t1) - xx11);
        iArr[4] = (int) cc5;
        long cc6 = (cc5 >> 32) + (((((long) xx[5]) & M) + t2) - xx122);
        iArr[5] = (int) cc6;
        long cc7 = (cc6 >> 32) + (((((long) xx[6]) & M) + xx10) - xx132);
        iArr[6] = (int) cc7;
        long cc8 = (cc7 >> 32) + 1;
        long z32 = z3 + cc8;
        long z02 = z0 - cc8;
        iArr[0] = (int) z02;
        long cc9 = z02 >> 32;
        if (cc9 != 0) {
            long j2 = xx10;
            long cc10 = cc9 + (((long) iArr[1]) & M);
            iArr[1] = (int) cc10;
            long cc11 = (((long) iArr[2]) & M) + (cc10 >> 32);
            iArr[2] = (int) cc11;
            z32 += cc11 >> 32;
        }
        iArr[3] = (int) z32;
        if (((z32 >> 32) != 0 && Nat.incAt(7, iArr, 4) != 0) || (iArr[6] == -1 && Nat224.gte(iArr, P))) {
            addPInvTo(z);
        }
    }

    public static void reduce32(int x, int[] z) {
        long cc = 0;
        if (x != 0) {
            long xx07 = ((long) x) & M;
            long cc2 = 0 + ((((long) z[0]) & M) - xx07);
            z[0] = (int) cc2;
            long cc3 = cc2 >> 32;
            if (cc3 != 0) {
                long cc4 = cc3 + (((long) z[1]) & M);
                z[1] = (int) cc4;
                long cc5 = (cc4 >> 32) + (((long) z[2]) & M);
                z[2] = (int) cc5;
                cc3 = cc5 >> 32;
            }
            long cc6 = cc3 + (M & ((long) z[3])) + xx07;
            z[3] = (int) cc6;
            cc = cc6 >> 32;
        }
        if ((cc != 0 && Nat.incAt(7, z, 4) != 0) || (z[6] == -1 && Nat224.gte(z, P))) {
            addPInvTo(z);
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
            subPInvFrom(z);
        }
    }

    public static void subtractExt(int[] xx, int[] yy, int[] zz) {
        if (Nat.sub(14, xx, yy, zz) != 0 && Nat.subFrom(PExtInv.length, PExtInv, zz) != 0) {
            Nat.decAt(14, zz, PExtInv.length);
        }
    }

    public static void twice(int[] x, int[] z) {
        if (Nat.shiftUpBit(7, x, 0, z) != 0 || (z[6] == -1 && Nat224.gte(z, P))) {
            addPInvTo(z);
        }
    }

    private static void addPInvTo(int[] z) {
        long c = (((long) z[0]) & M) - 1;
        z[0] = (int) c;
        long c2 = c >> 32;
        if (c2 != 0) {
            long c3 = c2 + (((long) z[1]) & M);
            z[1] = (int) c3;
            long c4 = (c3 >> 32) + (((long) z[2]) & M);
            z[2] = (int) c4;
            c2 = c4 >> 32;
        }
        long c5 = c2 + (M & ((long) z[3])) + 1;
        z[3] = (int) c5;
        if ((c5 >> 32) != 0) {
            Nat.incAt(7, z, 4);
        }
    }

    private static void subPInvFrom(int[] z) {
        long c = (((long) z[0]) & M) + 1;
        z[0] = (int) c;
        long c2 = c >> 32;
        if (c2 != 0) {
            long c3 = c2 + (((long) z[1]) & M);
            z[1] = (int) c3;
            long c4 = (c3 >> 32) + (((long) z[2]) & M);
            z[2] = (int) c4;
            c2 = c4 >> 32;
        }
        long c5 = c2 + ((M & ((long) z[3])) - 1);
        z[3] = (int) c5;
        if ((c5 >> 32) != 0) {
            Nat.decAt(7, z, 4);
        }
    }
}
