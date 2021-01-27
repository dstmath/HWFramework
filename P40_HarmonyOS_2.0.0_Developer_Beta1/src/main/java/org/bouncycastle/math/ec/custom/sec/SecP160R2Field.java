package org.bouncycastle.math.ec.custom.sec;

import java.math.BigInteger;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.math.raw.Nat160;

public class SecP160R2Field {
    static final int[] P = {-21389, -2, -1, -1, -1};
    private static final int P4 = -1;
    static final int[] PExt = {457489321, 42778, 1, 0, 0, -42778, -3, -1, -1, -1};
    private static final int PExt9 = -1;
    private static final int[] PExtInv = {-457489321, -42779, -2, -1, -1, 42777, 2};
    private static final int PInv33 = 21389;

    public static void add(int[] iArr, int[] iArr2, int[] iArr3) {
        if (Nat160.add(iArr, iArr2, iArr3) != 0 || (iArr3[4] == -1 && Nat160.gte(iArr3, P))) {
            Nat.add33To(5, PInv33, iArr3);
        }
    }

    public static void addExt(int[] iArr, int[] iArr2, int[] iArr3) {
        if (Nat.add(10, iArr, iArr2, iArr3) != 0 || (iArr3[9] == -1 && Nat.gte(10, iArr3, PExt))) {
            int[] iArr4 = PExtInv;
            if (Nat.addTo(iArr4.length, iArr4, iArr3) != 0) {
                Nat.incAt(10, iArr3, PExtInv.length);
            }
        }
    }

    public static void addOne(int[] iArr, int[] iArr2) {
        if (Nat.inc(5, iArr, iArr2) != 0 || (iArr2[4] == -1 && Nat160.gte(iArr2, P))) {
            Nat.add33To(5, PInv33, iArr2);
        }
    }

    public static int[] fromBigInteger(BigInteger bigInteger) {
        int[] fromBigInteger = Nat160.fromBigInteger(bigInteger);
        if (fromBigInteger[4] == -1 && Nat160.gte(fromBigInteger, P)) {
            Nat160.subFrom(P, fromBigInteger);
        }
        return fromBigInteger;
    }

    public static void half(int[] iArr, int[] iArr2) {
        if ((iArr[0] & 1) == 0) {
            Nat.shiftDownBit(5, iArr, 0, iArr2);
        } else {
            Nat.shiftDownBit(5, iArr2, Nat160.add(iArr, P, iArr2));
        }
    }

    public static void multiply(int[] iArr, int[] iArr2, int[] iArr3) {
        int[] createExt = Nat160.createExt();
        Nat160.mul(iArr, iArr2, createExt);
        reduce(createExt, iArr3);
    }

    public static void multiplyAddToExt(int[] iArr, int[] iArr2, int[] iArr3) {
        if (Nat160.mulAddTo(iArr, iArr2, iArr3) != 0 || (iArr3[9] == -1 && Nat.gte(10, iArr3, PExt))) {
            int[] iArr4 = PExtInv;
            if (Nat.addTo(iArr4.length, iArr4, iArr3) != 0) {
                Nat.incAt(10, iArr3, PExtInv.length);
            }
        }
    }

    public static void negate(int[] iArr, int[] iArr2) {
        if (Nat160.isZero(iArr)) {
            Nat160.zero(iArr2);
        } else {
            Nat160.sub(P, iArr, iArr2);
        }
    }

    public static void reduce(int[] iArr, int[] iArr2) {
        if (Nat160.mul33DWordAdd(PInv33, Nat160.mul33Add(PInv33, iArr, 5, iArr, 0, iArr2, 0), iArr2, 0) != 0 || (iArr2[4] == -1 && Nat160.gte(iArr2, P))) {
            Nat.add33To(5, PInv33, iArr2);
        }
    }

    public static void reduce32(int i, int[] iArr) {
        if ((i != 0 && Nat160.mul33WordAdd(PInv33, i, iArr, 0) != 0) || (iArr[4] == -1 && Nat160.gte(iArr, P))) {
            Nat.add33To(5, PInv33, iArr);
        }
    }

    public static void square(int[] iArr, int[] iArr2) {
        int[] createExt = Nat160.createExt();
        Nat160.square(iArr, createExt);
        reduce(createExt, iArr2);
    }

    public static void squareN(int[] iArr, int i, int[] iArr2) {
        int[] createExt = Nat160.createExt();
        Nat160.square(iArr, createExt);
        while (true) {
            reduce(createExt, iArr2);
            i--;
            if (i > 0) {
                Nat160.square(iArr2, createExt);
            } else {
                return;
            }
        }
    }

    public static void subtract(int[] iArr, int[] iArr2, int[] iArr3) {
        if (Nat160.sub(iArr, iArr2, iArr3) != 0) {
            Nat.sub33From(5, PInv33, iArr3);
        }
    }

    public static void subtractExt(int[] iArr, int[] iArr2, int[] iArr3) {
        if (Nat.sub(10, iArr, iArr2, iArr3) != 0) {
            int[] iArr4 = PExtInv;
            if (Nat.subFrom(iArr4.length, iArr4, iArr3) != 0) {
                Nat.decAt(10, iArr3, PExtInv.length);
            }
        }
    }

    public static void twice(int[] iArr, int[] iArr2) {
        if (Nat.shiftUpBit(5, iArr, 0, iArr2) != 0 || (iArr2[4] == -1 && Nat160.gte(iArr2, P))) {
            Nat.add33To(5, PInv33, iArr2);
        }
    }
}
