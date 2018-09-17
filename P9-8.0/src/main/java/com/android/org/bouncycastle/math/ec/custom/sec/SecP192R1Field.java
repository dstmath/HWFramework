package com.android.org.bouncycastle.math.ec.custom.sec;

import com.android.org.bouncycastle.math.raw.Nat;
import com.android.org.bouncycastle.math.raw.Nat192;
import java.math.BigInteger;

public class SecP192R1Field {
    private static final long M = 4294967295L;
    static final int[] P = new int[]{-1, -1, -2, -1, -1, -1};
    private static final int P5 = -1;
    static final int[] PExt = new int[]{1, 0, 2, 0, 1, 0, -2, -1, -3, -1, -1, -1};
    private static final int PExt11 = -1;
    private static final int[] PExtInv = new int[]{-1, -1, -3, -1, -2, -1, 1, 0, 2};

    public static void add(int[] x, int[] y, int[] z) {
        if (Nat192.add(x, y, z) != 0 || (z[5] == -1 && Nat192.gte(z, P))) {
            addPInvTo(z);
        }
    }

    public static void addExt(int[] xx, int[] yy, int[] zz) {
        if ((Nat.add(12, xx, yy, zz) != 0 || (zz[11] == -1 && Nat.gte(12, zz, PExt))) && Nat.addTo(PExtInv.length, PExtInv, zz) != 0) {
            Nat.incAt(12, zz, PExtInv.length);
        }
    }

    public static void addOne(int[] x, int[] z) {
        if (Nat.inc(6, x, z) != 0 || (z[5] == -1 && Nat192.gte(z, P))) {
            addPInvTo(z);
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
        long xx06 = ((long) xx[6]) & M;
        long xx07 = ((long) xx[7]) & M;
        long xx08 = ((long) xx[8]) & M;
        long xx09 = ((long) xx[9]) & M;
        long t0 = xx06 + (((long) xx[10]) & M);
        long t1 = xx07 + (((long) xx[11]) & M);
        long cc = 0 + ((((long) xx[0]) & M) + t0);
        int z0 = (int) cc;
        cc = (cc >> 32) + ((((long) xx[1]) & M) + t1);
        z[1] = (int) cc;
        t0 += xx08;
        t1 += xx09;
        cc = (cc >> 32) + ((((long) xx[2]) & M) + t0);
        long z2 = cc & M;
        cc = (cc >> 32) + ((((long) xx[3]) & M) + t1);
        z[3] = (int) cc;
        t1 -= xx07;
        cc = (cc >> 32) + ((((long) xx[4]) & M) + (t0 - xx06));
        z[4] = (int) cc;
        cc = (cc >> 32) + ((((long) xx[5]) & M) + t1);
        z[5] = (int) cc;
        cc >>= 32;
        z2 += cc;
        cc += ((long) z0) & M;
        z[0] = (int) cc;
        cc >>= 32;
        if (cc != 0) {
            cc += ((long) z[1]) & M;
            z[1] = (int) cc;
            z2 += cc >> 32;
        }
        z[2] = (int) z2;
        if (((z2 >> 32) != 0 && Nat.incAt(6, z, 3) != 0) || (z[5] == -1 && Nat192.gte(z, P))) {
            addPInvTo(z);
        }
    }

    public static void reduce32(int x, int[] z) {
        long cc = 0;
        if (x != 0) {
            long xx06 = ((long) x) & M;
            cc = 0 + ((((long) z[0]) & M) + xx06);
            z[0] = (int) cc;
            cc >>= 32;
            if (cc != 0) {
                cc += ((long) z[1]) & M;
                z[1] = (int) cc;
                cc >>= 32;
            }
            cc += (((long) z[2]) & M) + xx06;
            z[2] = (int) cc;
            cc >>= 32;
        }
        if ((cc != 0 && Nat.incAt(6, z, 3) != 0) || (z[5] == -1 && Nat192.gte(z, P))) {
            addPInvTo(z);
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
            subPInvFrom(z);
        }
    }

    public static void subtractExt(int[] xx, int[] yy, int[] zz) {
        if (Nat.sub(12, xx, yy, zz) != 0 && Nat.subFrom(PExtInv.length, PExtInv, zz) != 0) {
            Nat.decAt(12, zz, PExtInv.length);
        }
    }

    public static void twice(int[] x, int[] z) {
        if (Nat.shiftUpBit(6, x, 0, z) != 0 || (z[5] == -1 && Nat192.gte(z, P))) {
            addPInvTo(z);
        }
    }

    private static void addPInvTo(int[] z) {
        long c = (((long) z[0]) & M) + 1;
        z[0] = (int) c;
        c >>= 32;
        if (c != 0) {
            c += ((long) z[1]) & M;
            z[1] = (int) c;
            c >>= 32;
        }
        c += (((long) z[2]) & M) + 1;
        z[2] = (int) c;
        if ((c >> 32) != 0) {
            Nat.incAt(6, z, 3);
        }
    }

    private static void subPInvFrom(int[] z) {
        long c = (((long) z[0]) & M) - 1;
        z[0] = (int) c;
        c >>= 32;
        if (c != 0) {
            c += ((long) z[1]) & M;
            z[1] = (int) c;
            c >>= 32;
        }
        c += (((long) z[2]) & M) - 1;
        z[2] = (int) c;
        if ((c >> 32) != 0) {
            Nat.decAt(6, z, 3);
        }
    }
}
