package org.bouncycastle.math.ec.custom.sec;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.math.raw.Nat512;
import org.bouncycastle.util.Pack;

public class SecP521R1Field {
    static final int[] P = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, P16};
    private static final int P16 = 511;

    public static void add(int[] iArr, int[] iArr2, int[] iArr3) {
        int add = Nat.add(16, iArr, iArr2, iArr3) + iArr[16] + iArr2[16];
        if (add > P16 || (add == P16 && Nat.eq(16, iArr3, P))) {
            add = (add + Nat.inc(16, iArr3)) & P16;
        }
        iArr3[16] = add;
    }

    public static void addOne(int[] iArr, int[] iArr2) {
        int inc = Nat.inc(16, iArr, iArr2) + iArr[16];
        if (inc > P16 || (inc == P16 && Nat.eq(16, iArr2, P))) {
            inc = (inc + Nat.inc(16, iArr2)) & P16;
        }
        iArr2[16] = inc;
    }

    public static int[] fromBigInteger(BigInteger bigInteger) {
        int[] fromBigInteger = Nat.fromBigInteger(521, bigInteger);
        if (Nat.eq(17, fromBigInteger, P)) {
            Nat.zero(17, fromBigInteger);
        }
        return fromBigInteger;
    }

    public static void half(int[] iArr, int[] iArr2) {
        int i = iArr[16];
        iArr2[16] = (Nat.shiftDownBit(16, iArr, i, iArr2) >>> 23) | (i >>> 1);
    }

    protected static void implMultiply(int[] iArr, int[] iArr2, int[] iArr3) {
        Nat512.mul(iArr, iArr2, iArr3);
        int i = iArr[16];
        int i2 = iArr2[16];
        iArr3[32] = Nat.mul31BothAdd(16, i, iArr2, i2, iArr, iArr3, 16) + (i * i2);
    }

    protected static void implSquare(int[] iArr, int[] iArr2) {
        Nat512.square(iArr, iArr2);
        int i = iArr[16];
        iArr2[32] = Nat.mulWordAddTo(16, i << 1, iArr, 0, iArr2, 16) + (i * i);
    }

    public static void inv(int[] iArr, int[] iArr2) {
        if (isZero(iArr) == 0) {
            int[] create = Nat.create(17);
            square(iArr, create);
            multiply(create, iArr, create);
            int[] create2 = Nat.create(17);
            squareN(create, 2, create2);
            multiply(create2, create, create2);
            int[] create3 = Nat.create(17);
            squareN(create2, 4, create3);
            multiply(create3, create2, create3);
            int[] create4 = Nat.create(17);
            squareN(create3, 8, create4);
            multiply(create4, create3, create4);
            squareN(create4, 16, create3);
            multiply(create3, create4, create3);
            squareN(create3, 32, create4);
            multiply(create4, create3, create4);
            squareN(create4, 64, create3);
            multiply(create3, create4, create3);
            squareN(create3, 128, create4);
            multiply(create4, create3, create4);
            squareN(create4, 256, create3);
            multiply(create3, create4, create3);
            squareN(create3, 4, create4);
            multiply(create4, create2, create4);
            squareN(create4, 2, create2);
            multiply(create2, create, create2);
            square(create2, create);
            multiply(create, iArr, create);
            squareN(create, 2, create);
            multiply(iArr, create, iArr2);
            return;
        }
        throw new IllegalArgumentException("'x' cannot be 0");
    }

    public static int isZero(int[] iArr) {
        int i = 0;
        for (int i2 = 0; i2 < 17; i2++) {
            i |= iArr[i2];
        }
        return (((i >>> 1) | (i & 1)) - 1) >> 31;
    }

    public static void multiply(int[] iArr, int[] iArr2, int[] iArr3) {
        int[] create = Nat.create(33);
        implMultiply(iArr, iArr2, create);
        reduce(create, iArr3);
    }

    public static void negate(int[] iArr, int[] iArr2) {
        if (isZero(iArr) != 0) {
            int[] iArr3 = P;
            Nat.sub(17, iArr3, iArr3, iArr2);
            return;
        }
        Nat.sub(17, P, iArr, iArr2);
    }

    public static void random(SecureRandom secureRandom, int[] iArr) {
        byte[] bArr = new byte[68];
        do {
            secureRandom.nextBytes(bArr);
            Pack.littleEndianToInt(bArr, 0, iArr, 0, 17);
            iArr[16] = (int) (((long) iArr[16]) & 511);
        } while (Nat.lessThan(17, iArr, P) == 0);
    }

    public static void randomMult(SecureRandom secureRandom, int[] iArr) {
        do {
            random(secureRandom, iArr);
        } while (isZero(iArr) != 0);
    }

    public static void reduce(int[] iArr, int[] iArr2) {
        int i = iArr[32];
        int shiftDownBits = (Nat.shiftDownBits(16, iArr, 16, 9, i, iArr2, 0) >>> 23) + (i >>> 9) + Nat.addTo(16, iArr, iArr2);
        if (shiftDownBits > P16 || (shiftDownBits == P16 && Nat.eq(16, iArr2, P))) {
            shiftDownBits = (shiftDownBits + Nat.inc(16, iArr2)) & P16;
        }
        iArr2[16] = shiftDownBits;
    }

    public static void reduce23(int[] iArr) {
        int i = iArr[16];
        int addWordTo = Nat.addWordTo(16, i >>> 9, iArr) + (i & P16);
        if (addWordTo > P16 || (addWordTo == P16 && Nat.eq(16, iArr, P))) {
            addWordTo = (addWordTo + Nat.inc(16, iArr)) & P16;
        }
        iArr[16] = addWordTo;
    }

    public static void square(int[] iArr, int[] iArr2) {
        int[] create = Nat.create(33);
        implSquare(iArr, create);
        reduce(create, iArr2);
    }

    public static void squareN(int[] iArr, int i, int[] iArr2) {
        int[] create = Nat.create(33);
        implSquare(iArr, create);
        while (true) {
            reduce(create, iArr2);
            i--;
            if (i > 0) {
                implSquare(iArr2, create);
            } else {
                return;
            }
        }
    }

    public static void subtract(int[] iArr, int[] iArr2, int[] iArr3) {
        int sub = (Nat.sub(16, iArr, iArr2, iArr3) + iArr[16]) - iArr2[16];
        if (sub < 0) {
            sub = (sub + Nat.dec(16, iArr3)) & P16;
        }
        iArr3[16] = sub;
    }

    public static void twice(int[] iArr, int[] iArr2) {
        int i = iArr[16];
        iArr2[16] = (Nat.shiftUpBit(16, iArr, i << 23, iArr2) | (i << 1)) & P16;
    }
}
