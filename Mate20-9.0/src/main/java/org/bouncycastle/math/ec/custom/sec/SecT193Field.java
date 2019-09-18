package org.bouncycastle.math.ec.custom.sec;

import java.math.BigInteger;
import org.bouncycastle.asn1.cmc.BodyPartID;
import org.bouncycastle.math.raw.Interleave;
import org.bouncycastle.math.raw.Nat256;

public class SecT193Field {
    private static final long M01 = 1;
    private static final long M49 = 562949953421311L;

    public static void add(long[] jArr, long[] jArr2, long[] jArr3) {
        jArr3[0] = jArr[0] ^ jArr2[0];
        jArr3[1] = jArr[1] ^ jArr2[1];
        jArr3[2] = jArr[2] ^ jArr2[2];
        jArr3[3] = jArr2[3] ^ jArr[3];
    }

    public static void addExt(long[] jArr, long[] jArr2, long[] jArr3) {
        jArr3[0] = jArr[0] ^ jArr2[0];
        jArr3[1] = jArr[1] ^ jArr2[1];
        jArr3[2] = jArr[2] ^ jArr2[2];
        jArr3[3] = jArr[3] ^ jArr2[3];
        jArr3[4] = jArr[4] ^ jArr2[4];
        jArr3[5] = jArr[5] ^ jArr2[5];
        jArr3[6] = jArr2[6] ^ jArr[6];
    }

    public static void addOne(long[] jArr, long[] jArr2) {
        jArr2[0] = jArr[0] ^ M01;
        jArr2[1] = jArr[1];
        jArr2[2] = jArr[2];
        jArr2[3] = jArr[3];
    }

    public static long[] fromBigInteger(BigInteger bigInteger) {
        long[] fromBigInteger64 = Nat256.fromBigInteger64(bigInteger);
        reduce63(fromBigInteger64, 0);
        return fromBigInteger64;
    }

    protected static void implCompactExt(long[] jArr) {
        long j = jArr[0];
        long j2 = jArr[1];
        long j3 = jArr[2];
        long j4 = jArr[3];
        long j5 = jArr[4];
        long j6 = jArr[5];
        long j7 = jArr[6];
        long j8 = jArr[7];
        jArr[0] = j ^ (j2 << 49);
        jArr[1] = (j2 >>> 15) ^ (j3 << 34);
        jArr[2] = (j3 >>> 30) ^ (j4 << 19);
        jArr[3] = ((j4 >>> 45) ^ (j5 << 4)) ^ (j6 << 53);
        jArr[4] = ((j5 >>> 60) ^ (j7 << 38)) ^ (j6 >>> 11);
        jArr[5] = (j7 >>> 26) ^ (j8 << 23);
        jArr[6] = j8 >>> 41;
        jArr[7] = 0;
    }

    protected static void implExpand(long[] jArr, long[] jArr2) {
        long j = jArr[0];
        long j2 = jArr[1];
        long j3 = jArr[2];
        long j4 = jArr[3];
        jArr2[0] = j & M49;
        jArr2[1] = ((j >>> 49) ^ (j2 << 15)) & M49;
        jArr2[2] = ((j2 >>> 34) ^ (j3 << 30)) & M49;
        jArr2[3] = (j3 >>> 19) ^ (j4 << 45);
    }

    protected static void implMultiply(long[] jArr, long[] jArr2, long[] jArr3) {
        long[] jArr4 = new long[4];
        long[] jArr5 = new long[4];
        implExpand(jArr, jArr4);
        implExpand(jArr2, jArr5);
        long[] jArr6 = jArr3;
        implMulwAcc(jArr4[0], jArr5[0], jArr6, 0);
        implMulwAcc(jArr4[1], jArr5[1], jArr6, 1);
        implMulwAcc(jArr4[2], jArr5[2], jArr6, 2);
        implMulwAcc(jArr4[3], jArr5[3], jArr6, 3);
        for (int i = 5; i > 0; i--) {
            jArr3[i] = jArr3[i] ^ jArr3[i - 1];
        }
        implMulwAcc(jArr4[0] ^ jArr4[1], jArr5[0] ^ jArr5[1], jArr3, 1);
        implMulwAcc(jArr4[2] ^ jArr4[3], jArr5[2] ^ jArr5[3], jArr3, 3);
        for (int i2 = 7; i2 > 1; i2--) {
            jArr3[i2] = jArr3[i2] ^ jArr3[i2 - 2];
        }
        long j = jArr4[0] ^ jArr4[2];
        long j2 = jArr4[1] ^ jArr4[3];
        long j3 = jArr5[0] ^ jArr5[2];
        long j4 = jArr5[1] ^ jArr5[3];
        implMulwAcc(j ^ j2, j3 ^ j4, jArr3, 3);
        long[] jArr7 = new long[3];
        implMulwAcc(j, j3, jArr7, 0);
        implMulwAcc(j2, j4, jArr7, 1);
        long j5 = jArr7[0];
        long j6 = jArr7[1];
        long j7 = jArr7[2];
        jArr3[2] = jArr3[2] ^ j5;
        jArr3[3] = (j5 ^ j6) ^ jArr3[3];
        jArr3[4] = jArr3[4] ^ (j7 ^ j6);
        jArr3[5] = jArr3[5] ^ j7;
        implCompactExt(jArr3);
    }

    protected static void implMulwAcc(long j, long j2, long[] jArr, int i) {
        long j3 = j;
        long[] jArr2 = new long[8];
        jArr2[1] = j2;
        jArr2[2] = jArr2[1] << M01;
        jArr2[3] = jArr2[2] ^ j2;
        jArr2[4] = jArr2[2] << M01;
        jArr2[5] = jArr2[4] ^ j2;
        jArr2[6] = jArr2[3] << M01;
        int i2 = 7;
        jArr2[7] = jArr2[6] ^ j2;
        int i3 = (int) j3;
        long j4 = (jArr2[(i3 >>> 3) & 7] << 3) ^ jArr2[i3 & 7];
        long j5 = 0;
        int i4 = 36;
        while (true) {
            int i5 = (int) (j3 >>> i4);
            long j6 = (((jArr2[i5 & 7] ^ (jArr2[(i5 >>> 3) & 7] << 3)) ^ (jArr2[(i5 >>> 6) & 7] << 6)) ^ (jArr2[(i5 >>> 9) & 7] << 9)) ^ (jArr2[(i5 >>> 12) & i2] << 12);
            j4 ^= j6 << i4;
            j5 ^= j6 >>> (-i4);
            i4 -= 15;
            if (i4 <= 0) {
                jArr[i] = jArr[i] ^ (M49 & j4);
                int i6 = i + 1;
                jArr[i6] = ((j4 >>> 49) ^ (j5 << 15)) ^ jArr[i6];
                return;
            }
            i2 = 7;
        }
    }

    protected static void implSquare(long[] jArr, long[] jArr2) {
        Interleave.expand64To128(jArr[0], jArr2, 0);
        Interleave.expand64To128(jArr[1], jArr2, 2);
        Interleave.expand64To128(jArr[2], jArr2, 4);
        jArr2[6] = jArr[3] & M01;
    }

    public static void invert(long[] jArr, long[] jArr2) {
        if (!Nat256.isZero64(jArr)) {
            long[] create64 = Nat256.create64();
            long[] create642 = Nat256.create64();
            square(jArr, create64);
            squareN(create64, 1, create642);
            multiply(create64, create642, create64);
            squareN(create642, 1, create642);
            multiply(create64, create642, create64);
            squareN(create64, 3, create642);
            multiply(create64, create642, create64);
            squareN(create64, 6, create642);
            multiply(create64, create642, create64);
            squareN(create64, 12, create642);
            multiply(create64, create642, create64);
            squareN(create64, 24, create642);
            multiply(create64, create642, create64);
            squareN(create64, 48, create642);
            multiply(create64, create642, create64);
            squareN(create64, 96, create642);
            multiply(create64, create642, jArr2);
            return;
        }
        throw new IllegalStateException();
    }

    public static void multiply(long[] jArr, long[] jArr2, long[] jArr3) {
        long[] createExt64 = Nat256.createExt64();
        implMultiply(jArr, jArr2, createExt64);
        reduce(createExt64, jArr3);
    }

    public static void multiplyAddToExt(long[] jArr, long[] jArr2, long[] jArr3) {
        long[] createExt64 = Nat256.createExt64();
        implMultiply(jArr, jArr2, createExt64);
        addExt(jArr3, createExt64, jArr3);
    }

    public static void reduce(long[] jArr, long[] jArr2) {
        long j = jArr[0];
        long j2 = jArr[1];
        long j3 = jArr[2];
        long j4 = jArr[3];
        long j5 = jArr[4];
        long j6 = jArr[5];
        long j7 = jArr[6];
        long j8 = j5 ^ (j7 >>> 50);
        long j9 = (j3 ^ (j7 << 63)) ^ ((j6 >>> M01) ^ (j6 << 14));
        long j10 = (j4 ^ ((j7 >>> M01) ^ (j7 << 14))) ^ (j6 >>> 50);
        long j11 = j ^ (j8 << 63);
        long j12 = (j2 ^ (j6 << 63)) ^ ((j8 >>> M01) ^ (j8 << 14));
        long j13 = j9 ^ (j8 >>> 50);
        long j14 = j10 >>> M01;
        jArr2[0] = (j11 ^ j14) ^ (j14 << 15);
        jArr2[1] = (j14 >>> 49) ^ j12;
        jArr2[2] = j13;
        jArr2[3] = M01 & j10;
    }

    public static void reduce63(long[] jArr, int i) {
        int i2 = i + 3;
        long j = jArr[i2];
        long j2 = j >>> M01;
        jArr[i] = jArr[i] ^ ((j2 << 15) ^ j2);
        int i3 = i + 1;
        jArr[i3] = (j2 >>> 49) ^ jArr[i3];
        jArr[i2] = j & M01;
    }

    public static void sqrt(long[] jArr, long[] jArr2) {
        long unshuffle = Interleave.unshuffle(jArr[0]);
        long unshuffle2 = Interleave.unshuffle(jArr[1]);
        long j = (unshuffle & BodyPartID.bodyIdMax) | (unshuffle2 << 32);
        long j2 = (unshuffle >>> 32) | (unshuffle2 & -4294967296L);
        long unshuffle3 = Interleave.unshuffle(jArr[2]);
        long j3 = unshuffle3 >>> 32;
        jArr2[0] = j ^ (j2 << 8);
        jArr2[1] = ((((unshuffle3 & BodyPartID.bodyIdMax) ^ (jArr[3] << 32)) ^ (j3 << 8)) ^ (j2 >>> 56)) ^ (j2 << 33);
        jArr2[2] = (j2 >>> 31) ^ ((j3 >>> 56) ^ (j3 << 33));
        jArr2[3] = j3 >>> 31;
    }

    public static void square(long[] jArr, long[] jArr2) {
        long[] createExt64 = Nat256.createExt64();
        implSquare(jArr, createExt64);
        reduce(createExt64, jArr2);
    }

    public static void squareAddToExt(long[] jArr, long[] jArr2) {
        long[] createExt64 = Nat256.createExt64();
        implSquare(jArr, createExt64);
        addExt(jArr2, createExt64, jArr2);
    }

    public static void squareN(long[] jArr, int i, long[] jArr2) {
        long[] createExt64 = Nat256.createExt64();
        implSquare(jArr, createExt64);
        while (true) {
            reduce(createExt64, jArr2);
            i--;
            if (i > 0) {
                implSquare(jArr2, createExt64);
            } else {
                return;
            }
        }
    }

    public static int trace(long[] jArr) {
        return ((int) jArr[0]) & 1;
    }
}
