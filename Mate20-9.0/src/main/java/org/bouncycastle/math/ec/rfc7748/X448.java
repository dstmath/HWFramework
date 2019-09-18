package org.bouncycastle.math.ec.rfc7748;

import org.bouncycastle.asn1.cmp.PKIFailureInfo;

public abstract class X448 {
    private static final int C_A = 156326;
    private static final int C_A24 = 39082;
    private static final int[] PsubS_x = {161294112, 185702364, 163248300, 54522310, 189866924, 105098465, 66174309, 139206530, 156517789, 136025714, 231801628, 246922668, 59251455, 69446896, 83964484, 252685170};
    private static final int[] S_x = {268435454, 268435455, 268435455, 268435455, 268435455, 268435455, 268435455, 268435455, 268435454, 268435455, 268435455, 268435455, 268435455, 268435455, 268435455, 268435455};
    private static int[] precompBase = null;

    private static int decode32(byte[] bArr, int i) {
        int i2 = i + 1;
        int i3 = i2 + 1;
        return (bArr[i3 + 1] << 24) | (bArr[i] & 255) | ((bArr[i2] & 255) << 8) | ((bArr[i3] & 255) << Tnaf.POW_2_WIDTH);
    }

    private static void decodeScalar(byte[] bArr, int i, int[] iArr) {
        for (int i2 = 0; i2 < 14; i2++) {
            iArr[i2] = decode32(bArr, (i2 * 4) + i);
        }
        iArr[0] = iArr[0] & -4;
        iArr[13] = iArr[13] | PKIFailureInfo.systemUnavail;
    }

    private static void pointDouble(int[] iArr, int[] iArr2) {
        int[] create = X448Field.create();
        int[] create2 = X448Field.create();
        X448Field.add(iArr, iArr2, create);
        X448Field.sub(iArr, iArr2, create2);
        X448Field.sqr(create, create);
        X448Field.sqr(create2, create2);
        X448Field.mul(create, create2, iArr);
        X448Field.sub(create, create2, create);
        X448Field.mul(create, (int) C_A24, iArr2);
        X448Field.add(iArr2, create2, iArr2);
        X448Field.mul(iArr2, create, iArr2);
    }

    public static synchronized void precompute() {
        synchronized (X448.class) {
            if (precompBase == null) {
                precompBase = new int[7136];
                int[] iArr = precompBase;
                int[] iArr2 = new int[7120];
                int[] create = X448Field.create();
                create[0] = 5;
                int[] create2 = X448Field.create();
                create2[0] = 1;
                int[] create3 = X448Field.create();
                int[] create4 = X448Field.create();
                X448Field.add(create, create2, create3);
                X448Field.sub(create, create2, create4);
                int[] create5 = X448Field.create();
                X448Field.copy(create4, 0, create5, 0);
                int i = 0;
                while (true) {
                    X448Field.copy(create3, 0, iArr, i);
                    if (i == 7120) {
                        break;
                    }
                    pointDouble(create, create2);
                    X448Field.add(create, create2, create3);
                    X448Field.sub(create, create2, create4);
                    X448Field.mul(create3, create5, create3);
                    X448Field.mul(create5, create4, create5);
                    X448Field.copy(create4, 0, iArr2, i);
                    i += 16;
                }
                int[] create6 = X448Field.create();
                X448Field.inv(create5, create6);
                while (true) {
                    X448Field.copy(iArr, i, create, 0);
                    X448Field.mul(create, create6, create);
                    X448Field.copy(create, 0, precompBase, i);
                    if (i != 0) {
                        i -= 16;
                        X448Field.copy(iArr2, i, create2, 0);
                        X448Field.mul(create6, create2, create6);
                    } else {
                        return;
                    }
                }
            }
        }
    }

    public static void scalarMult(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, int i3) {
        int[] iArr = new int[14];
        decodeScalar(bArr, i, iArr);
        int[] create = X448Field.create();
        X448Field.decode(bArr2, i2, create);
        int[] create2 = X448Field.create();
        X448Field.copy(create, 0, create2, 0);
        int[] create3 = X448Field.create();
        create3[0] = 1;
        int[] create4 = X448Field.create();
        create4[0] = 1;
        int[] create5 = X448Field.create();
        int[] create6 = X448Field.create();
        int[] create7 = X448Field.create();
        int i4 = 447;
        int i5 = 1;
        while (true) {
            X448Field.add(create4, create5, create6);
            X448Field.sub(create4, create5, create4);
            X448Field.add(create2, create3, create5);
            X448Field.sub(create2, create3, create2);
            X448Field.mul(create6, create2, create6);
            X448Field.mul(create4, create5, create4);
            X448Field.sqr(create5, create5);
            X448Field.sqr(create2, create2);
            X448Field.sub(create5, create2, create7);
            X448Field.mul(create7, (int) C_A24, create3);
            X448Field.add(create3, create2, create3);
            X448Field.mul(create3, create7, create3);
            X448Field.mul(create2, create5, create2);
            X448Field.sub(create6, create4, create5);
            X448Field.add(create6, create4, create4);
            X448Field.sqr(create4, create4);
            X448Field.sqr(create5, create5);
            X448Field.mul(create5, create, create5);
            i4--;
            int i6 = (iArr[i4 >>> 5] >>> (i4 & 31)) & 1;
            int i7 = i5 ^ i6;
            X448Field.cswap(i7, create2, create4);
            X448Field.cswap(i7, create3, create5);
            if (i4 < 2) {
                break;
            }
            i5 = i6;
        }
        for (int i8 = 0; i8 < 2; i8++) {
            pointDouble(create2, create3);
        }
        X448Field.inv(create3, create3);
        X448Field.mul(create2, create3, create2);
        X448Field.normalize(create2);
        X448Field.encode(create2, bArr3, i3);
    }

    public static void scalarMultBase(byte[] bArr, int i, byte[] bArr2, int i2) {
        precompute();
        int[] iArr = new int[14];
        decodeScalar(bArr, i, iArr);
        int[] create = X448Field.create();
        int[] create2 = X448Field.create();
        X448Field.copy(S_x, 0, create2, 0);
        int[] create3 = X448Field.create();
        create3[0] = 1;
        int[] create4 = X448Field.create();
        X448Field.copy(PsubS_x, 0, create4, 0);
        int[] create5 = X448Field.create();
        create5[0] = 1;
        int[] create6 = X448Field.create();
        int i3 = 0;
        int i4 = 1;
        int i5 = 2;
        while (true) {
            X448Field.copy(precompBase, i3, create, 0);
            i3 += 16;
            int i6 = (iArr[i5 >>> 5] >>> (i5 & 31)) & 1;
            int i7 = i4 ^ i6;
            X448Field.cswap(i7, create2, create4);
            X448Field.cswap(i7, create3, create5);
            X448Field.add(create2, create3, create6);
            X448Field.sub(create2, create3, create3);
            X448Field.mul(create, create3, create);
            X448Field.carry(create6);
            X448Field.add(create6, create, create2);
            X448Field.sub(create6, create, create3);
            X448Field.sqr(create2, create2);
            X448Field.sqr(create3, create3);
            X448Field.mul(create5, create2, create2);
            X448Field.mul(create4, create3, create3);
            i5++;
            if (i5 >= 448) {
                break;
            }
            byte[] bArr3 = bArr2;
            int i8 = i2;
            i4 = i6;
        }
        for (int i9 = 0; i9 < 2; i9++) {
            pointDouble(create2, create3);
        }
        X448Field.inv(create3, create3);
        X448Field.mul(create2, create3, create2);
        X448Field.normalize(create2);
        X448Field.encode(create2, bArr2, i2);
    }
}
