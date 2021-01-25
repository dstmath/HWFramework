package org.bouncycastle.pqc.crypto.mceliece;

import java.math.BigInteger;
import org.bouncycastle.pqc.math.linearalgebra.BigIntUtils;
import org.bouncycastle.pqc.math.linearalgebra.GF2Vector;
import org.bouncycastle.pqc.math.linearalgebra.IntegerFunctions;

final class Conversions {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static final BigInteger ZERO = BigInteger.valueOf(0);

    private Conversions() {
    }

    public static byte[] decode(int i, int i2, GF2Vector gF2Vector) {
        if (gF2Vector.getLength() == i && gF2Vector.getHammingWeight() == i2) {
            int[] vecArray = gF2Vector.getVecArray();
            BigInteger binomial = IntegerFunctions.binomial(i, i2);
            BigInteger bigInteger = ZERO;
            int i3 = i2;
            int i4 = i;
            for (int i5 = 0; i5 < i; i5++) {
                binomial = binomial.multiply(BigInteger.valueOf((long) (i4 - i3))).divide(BigInteger.valueOf((long) i4));
                i4--;
                if ((vecArray[i5 >> 5] & (1 << (i5 & 31))) != 0) {
                    bigInteger = bigInteger.add(binomial);
                    i3--;
                    binomial = i4 == i3 ? ONE : binomial.multiply(BigInteger.valueOf((long) (i3 + 1))).divide(BigInteger.valueOf((long) (i4 - i3)));
                }
            }
            return BigIntUtils.toMinimalByteArray(bigInteger);
        }
        throw new IllegalArgumentException("vector has wrong length or hamming weight");
    }

    public static GF2Vector encode(int i, int i2, byte[] bArr) {
        if (i >= i2) {
            BigInteger binomial = IntegerFunctions.binomial(i, i2);
            BigInteger bigInteger = new BigInteger(1, bArr);
            if (bigInteger.compareTo(binomial) < 0) {
                GF2Vector gF2Vector = new GF2Vector(i);
                BigInteger bigInteger2 = bigInteger;
                int i3 = i2;
                int i4 = i;
                for (int i5 = 0; i5 < i; i5++) {
                    binomial = binomial.multiply(BigInteger.valueOf((long) (i4 - i3))).divide(BigInteger.valueOf((long) i4));
                    i4--;
                    if (binomial.compareTo(bigInteger2) <= 0) {
                        gF2Vector.setBit(i5);
                        bigInteger2 = bigInteger2.subtract(binomial);
                        i3--;
                        binomial = i4 == i3 ? ONE : binomial.multiply(BigInteger.valueOf((long) (i3 + 1))).divide(BigInteger.valueOf((long) (i4 - i3)));
                    }
                }
                return gF2Vector;
            }
            throw new IllegalArgumentException("Encoded number too large.");
        }
        throw new IllegalArgumentException("n < t");
    }

    public static byte[] signConversion(int i, int i2, byte[] bArr) {
        if (i >= i2) {
            BigInteger binomial = IntegerFunctions.binomial(i, i2);
            int bitLength = binomial.bitLength() - 1;
            int i3 = bitLength >> 3;
            int i4 = bitLength & 7;
            int i5 = 8;
            if (i4 == 0) {
                i3--;
                i4 = 8;
            }
            int i6 = i >> 3;
            int i7 = i & 7;
            if (i7 == 0) {
                i6--;
            } else {
                i5 = i7;
            }
            byte[] bArr2 = new byte[(i6 + 1)];
            if (bArr.length < bArr2.length) {
                System.arraycopy(bArr, 0, bArr2, 0, bArr.length);
                for (int length = bArr.length; length < bArr2.length; length++) {
                    bArr2[length] = 0;
                }
            } else {
                System.arraycopy(bArr, 0, bArr2, 0, i6);
                bArr2[i6] = (byte) (bArr[i6] & ((1 << i5) - 1));
            }
            BigInteger bigInteger = ZERO;
            int i8 = i;
            int i9 = i2;
            for (int i10 = 0; i10 < i; i10++) {
                binomial = binomial.multiply(new BigInteger(Integer.toString(i8 - i9))).divide(new BigInteger(Integer.toString(i8)));
                i8--;
                if (((byte) (bArr2[i10 >>> 3] & (1 << (i10 & 7)))) != 0) {
                    bigInteger = bigInteger.add(binomial);
                    i9--;
                    binomial = i8 == i9 ? ONE : binomial.multiply(new BigInteger(Integer.toString(i9 + 1))).divide(new BigInteger(Integer.toString(i8 - i9)));
                }
            }
            byte[] bArr3 = new byte[(i3 + 1)];
            byte[] byteArray = bigInteger.toByteArray();
            if (byteArray.length < bArr3.length) {
                System.arraycopy(byteArray, 0, bArr3, 0, byteArray.length);
                for (int length2 = byteArray.length; length2 < bArr3.length; length2++) {
                    bArr3[length2] = 0;
                }
            } else {
                System.arraycopy(byteArray, 0, bArr3, 0, i3);
                bArr3[i3] = (byte) (byteArray[i3] & ((1 << i4) - 1));
            }
            return bArr3;
        }
        throw new IllegalArgumentException("n < t");
    }
}
