package com.android.org.bouncycastle.math.ec.custom.sec;

import com.android.org.bouncycastle.math.raw.Nat;
import com.android.org.bouncycastle.math.raw.Nat384;
import java.math.BigInteger;

public class SecP384R1Field {
    private static final long M = 4294967295L;
    static final int[] P = new int[]{-1, 0, 0, -1, -2, -1, -1, -1, -1, -1, -1, -1};
    private static final int P11 = -1;
    static final int[] PExt = new int[]{1, -2, 0, 2, 0, -2, 0, 2, 1, 0, 0, 0, -2, 1, 0, -2, -3, -1, -1, -1, -1, -1, -1, -1};
    private static final int PExt23 = -1;
    private static final int[] PExtInv = new int[]{-1, 1, -1, -3, -1, 1, -1, -3, -2, -1, -1, -1, 1, -2, -1, 1, 2};

    public static void add(int[] x, int[] y, int[] z) {
        if (Nat.add(12, x, y, z) != 0 || (z[11] == -1 && Nat.gte(12, z, P))) {
            addPInvTo(z);
        }
    }

    public static void addExt(int[] xx, int[] yy, int[] zz) {
        if ((Nat.add(24, xx, yy, zz) != 0 || (zz[23] == -1 && Nat.gte(24, zz, PExt))) && Nat.addTo(PExtInv.length, PExtInv, zz) != 0) {
            Nat.incAt(24, zz, PExtInv.length);
        }
    }

    public static void addOne(int[] x, int[] z) {
        if (Nat.inc(12, x, z) != 0 || (z[11] == -1 && Nat.gte(12, z, P))) {
            addPInvTo(z);
        }
    }

    public static int[] fromBigInteger(BigInteger x) {
        int[] z = Nat.fromBigInteger(384, x);
        if (z[11] == -1 && Nat.gte(12, z, P)) {
            Nat.subFrom(12, P, z);
        }
        return z;
    }

    public static void half(int[] x, int[] z) {
        if ((x[0] & 1) == 0) {
            Nat.shiftDownBit(12, x, 0, z);
        } else {
            Nat.shiftDownBit(12, z, Nat.add(12, x, P, z));
        }
    }

    public static void multiply(int[] x, int[] y, int[] z) {
        int[] tt = Nat.create(24);
        Nat384.mul(x, y, tt);
        reduce(tt, z);
    }

    public static void negate(int[] x, int[] z) {
        if (Nat.isZero(12, x)) {
            Nat.zero(12, z);
        } else {
            Nat.sub(12, P, x, z);
        }
    }

    public static void reduce(int[] xx, int[] z) {
        long xx16 = ((long) xx[16]) & M;
        long xx17 = ((long) xx[17]) & M;
        long xx18 = ((long) xx[18]) & M;
        long xx19 = ((long) xx[19]) & M;
        long xx20 = ((long) xx[20]) & M;
        long xx21 = ((long) xx[21]) & M;
        long xx22 = ((long) xx[22]) & M;
        long xx23 = ((long) xx[23]) & M;
        long t0 = ((((long) xx[12]) & M) + xx20) - 1;
        long t1 = (((long) xx[13]) & M) + xx22;
        long t2 = ((((long) xx[14]) & M) + xx22) + xx23;
        long t3 = (((long) xx[15]) & M) + xx23;
        long t4 = xx17 + xx21;
        long t5 = xx21 - xx23;
        long t6 = xx22 - xx23;
        long t7 = t0 + t5;
        long cc = 0 + ((((long) xx[0]) & M) + t7);
        z[0] = (int) cc;
        cc = (cc >> 32) + ((((((long) xx[1]) & M) + xx23) - t0) + t1);
        z[1] = (int) cc;
        cc = (cc >> 32) + ((((((long) xx[2]) & M) - xx21) - t1) + t2);
        z[2] = (int) cc;
        cc = (cc >> 32) + ((((((long) xx[3]) & M) - t2) + t3) + t7);
        z[3] = (int) cc;
        cc = (cc >> 32) + ((((((((long) xx[4]) & M) + xx16) + xx21) + t1) - t3) + t7);
        z[4] = (int) cc;
        cc = (cc >> 32) + (((((((long) xx[5]) & M) - xx16) + t1) + t2) + t4);
        z[5] = (int) cc;
        cc = (cc >> 32) + (((((((long) xx[6]) & M) + xx18) - xx17) + t2) + t3);
        z[6] = (int) cc;
        cc = (cc >> 32) + (((((((long) xx[7]) & M) + xx16) + xx19) - xx18) + t3);
        z[7] = (int) cc;
        cc = (cc >> 32) + (((((((long) xx[8]) & M) + xx16) + xx17) + xx20) - xx19);
        z[8] = (int) cc;
        cc = (cc >> 32) + ((((((long) xx[9]) & M) + xx18) - xx20) + t4);
        z[9] = (int) cc;
        cc = (cc >> 32) + (((((((long) xx[10]) & M) + xx18) + xx19) - t5) + t6);
        z[10] = (int) cc;
        cc = (cc >> 32) + ((((((long) xx[11]) & M) + xx19) + xx20) - t6);
        z[11] = (int) cc;
        reduce32((int) ((cc >> 32) + 1), z);
    }

    public static void reduce32(int x, int[] z) {
        long cc = 0;
        if (x != 0) {
            long xx12 = ((long) x) & M;
            cc = 0 + ((((long) z[0]) & M) + xx12);
            z[0] = (int) cc;
            cc = (cc >> 32) + ((((long) z[1]) & M) - xx12);
            z[1] = (int) cc;
            cc >>= 32;
            if (cc != 0) {
                cc += ((long) z[2]) & M;
                z[2] = (int) cc;
                cc >>= 32;
            }
            cc += (((long) z[3]) & M) + xx12;
            z[3] = (int) cc;
            cc = (cc >> 32) + ((((long) z[4]) & M) + xx12);
            z[4] = (int) cc;
            cc >>= 32;
        }
        if ((cc != 0 && Nat.incAt(12, z, 5) != 0) || (z[11] == -1 && Nat.gte(12, z, P))) {
            addPInvTo(z);
        }
    }

    public static void square(int[] x, int[] z) {
        int[] tt = Nat.create(24);
        Nat384.square(x, tt);
        reduce(tt, z);
    }

    public static void squareN(int[] x, int n, int[] z) {
        int[] tt = Nat.create(24);
        Nat384.square(x, tt);
        reduce(tt, z);
        while (true) {
            n--;
            if (n > 0) {
                Nat384.square(z, tt);
                reduce(tt, z);
            } else {
                return;
            }
        }
    }

    public static void subtract(int[] x, int[] y, int[] z) {
        if (Nat.sub(12, x, y, z) != 0) {
            subPInvFrom(z);
        }
    }

    public static void subtractExt(int[] xx, int[] yy, int[] zz) {
        if (Nat.sub(24, xx, yy, zz) != 0 && Nat.subFrom(PExtInv.length, PExtInv, zz) != 0) {
            Nat.decAt(24, zz, PExtInv.length);
        }
    }

    public static void twice(int[] x, int[] z) {
        if (Nat.shiftUpBit(12, x, 0, z) != 0 || (z[11] == -1 && Nat.gte(12, z, P))) {
            addPInvTo(z);
        }
    }

    private static void addPInvTo(int[] z) {
        long c = (((long) z[0]) & M) + 1;
        z[0] = (int) c;
        c = (c >> 32) + ((((long) z[1]) & M) - 1);
        z[1] = (int) c;
        c >>= 32;
        if (c != 0) {
            c += ((long) z[2]) & M;
            z[2] = (int) c;
            c >>= 32;
        }
        c += (((long) z[3]) & M) + 1;
        z[3] = (int) c;
        c = (c >> 32) + ((((long) z[4]) & M) + 1);
        z[4] = (int) c;
        if ((c >> 32) != 0) {
            Nat.incAt(12, z, 5);
        }
    }

    private static void subPInvFrom(int[] z) {
        long c = (((long) z[0]) & M) - 1;
        z[0] = (int) c;
        c = (c >> 32) + ((((long) z[1]) & M) + 1);
        z[1] = (int) c;
        c >>= 32;
        if (c != 0) {
            c += ((long) z[2]) & M;
            z[2] = (int) c;
            c >>= 32;
        }
        c += (((long) z[3]) & M) - 1;
        z[3] = (int) c;
        c = (c >> 32) + ((((long) z[4]) & M) - 1);
        z[4] = (int) c;
        if ((c >> 32) != 0) {
            Nat.decAt(12, z, 5);
        }
    }
}
