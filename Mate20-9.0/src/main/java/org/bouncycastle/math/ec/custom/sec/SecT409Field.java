package org.bouncycastle.math.ec.custom.sec;

import java.math.BigInteger;
import org.bouncycastle.asn1.cmc.BodyPartID;
import org.bouncycastle.math.raw.Interleave;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.math.raw.Nat448;

public class SecT409Field {
    private static final long M25 = 33554431;
    private static final long M59 = 576460752303423487L;

    public static void add(long[] jArr, long[] jArr2, long[] jArr3) {
        jArr3[0] = jArr[0] ^ jArr2[0];
        jArr3[1] = jArr[1] ^ jArr2[1];
        jArr3[2] = jArr[2] ^ jArr2[2];
        jArr3[3] = jArr[3] ^ jArr2[3];
        jArr3[4] = jArr[4] ^ jArr2[4];
        jArr3[5] = jArr[5] ^ jArr2[5];
        jArr3[6] = jArr2[6] ^ jArr[6];
    }

    public static void addExt(long[] jArr, long[] jArr2, long[] jArr3) {
        for (int i = 0; i < 13; i++) {
            jArr3[i] = jArr[i] ^ jArr2[i];
        }
    }

    public static void addOne(long[] jArr, long[] jArr2) {
        jArr2[0] = jArr[0] ^ 1;
        jArr2[1] = jArr[1];
        jArr2[2] = jArr[2];
        jArr2[3] = jArr[3];
        jArr2[4] = jArr[4];
        jArr2[5] = jArr[5];
        jArr2[6] = jArr[6];
    }

    public static long[] fromBigInteger(BigInteger bigInteger) {
        long[] fromBigInteger64 = Nat448.fromBigInteger64(bigInteger);
        reduce39(fromBigInteger64, 0);
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
        long j9 = jArr[8];
        long j10 = jArr[9];
        long j11 = jArr[10];
        long j12 = jArr[11];
        long j13 = jArr[12];
        long j14 = jArr[13];
        jArr[0] = j ^ (j2 << 59);
        jArr[1] = (j2 >>> 5) ^ (j3 << 54);
        jArr[2] = (j3 >>> 10) ^ (j4 << 49);
        jArr[3] = (j4 >>> 15) ^ (j5 << 44);
        jArr[4] = (j5 >>> 20) ^ (j6 << 39);
        jArr[5] = (j6 >>> 25) ^ (j7 << 34);
        jArr[6] = (j7 >>> 30) ^ (j8 << 29);
        jArr[7] = (j8 >>> 35) ^ (j9 << 24);
        jArr[8] = (j9 >>> 40) ^ (j10 << 19);
        jArr[9] = (j10 >>> 45) ^ (j11 << 14);
        jArr[10] = (j11 >>> 50) ^ (j12 << 9);
        jArr[11] = ((j12 >>> 55) ^ (j13 << 4)) ^ (j14 << 63);
        jArr[12] = (j13 >>> 60) ^ (j14 >>> 1);
        jArr[13] = 0;
    }

    protected static void implExpand(long[] jArr, long[] jArr2) {
        long j = jArr[0];
        long j2 = jArr[1];
        long j3 = jArr[2];
        long j4 = jArr[3];
        long j5 = jArr[4];
        long j6 = jArr[5];
        long j7 = jArr[6];
        jArr2[0] = j & M59;
        jArr2[1] = ((j >>> 59) ^ (j2 << 5)) & M59;
        jArr2[2] = ((j2 >>> 54) ^ (j3 << 10)) & M59;
        jArr2[3] = ((j3 >>> 49) ^ (j4 << 15)) & M59;
        jArr2[4] = ((j4 >>> 44) ^ (j5 << 20)) & M59;
        jArr2[5] = ((j5 >>> 39) ^ (j6 << 25)) & M59;
        jArr2[6] = (j6 >>> 34) ^ (j7 << 30);
    }

    protected static void implMultiply(long[] jArr, long[] jArr2, long[] jArr3) {
        long[] jArr4 = new long[7];
        long[] jArr5 = new long[7];
        implExpand(jArr, jArr4);
        implExpand(jArr2, jArr5);
        for (int i = 0; i < 7; i++) {
            implMulwAcc(jArr4, jArr5[i], jArr3, i);
        }
        implCompactExt(jArr3);
    }

    protected static void implMulwAcc(long[] jArr, long j, long[] jArr2, int i) {
        long[] jArr3 = new long[8];
        jArr3[1] = j;
        jArr3[2] = jArr3[1] << 1;
        jArr3[3] = jArr3[2] ^ j;
        jArr3[4] = jArr3[2] << 1;
        jArr3[5] = jArr3[4] ^ j;
        jArr3[6] = jArr3[3] << 1;
        int i2 = 7;
        jArr3[7] = jArr3[6] ^ j;
        for (int i3 = 0; i3 < i2; i3++) {
            long j2 = jArr[i3];
            int i4 = (int) j2;
            long j3 = 0;
            long j4 = jArr3[i4 & 7] ^ (jArr3[(i4 >>> 3) & i2] << 3);
            int i5 = 54;
            do {
                int i6 = (int) (j2 >>> i5);
                i2 = 7;
                long j5 = jArr3[i6 & 7] ^ (jArr3[(i6 >>> 3) & 7] << 3);
                j4 ^= j5 << i5;
                j3 ^= j5 >>> (-i5);
                i5 -= 6;
            } while (i5 > 0);
            int i7 = i + i3;
            jArr2[i7] = jArr2[i7] ^ (M59 & j4);
            int i8 = i7 + 1;
            jArr2[i8] = jArr2[i8] ^ ((j3 << 5) ^ (j4 >>> 59));
        }
    }

    protected static void implSquare(long[] jArr, long[] jArr2) {
        for (int i = 0; i < 6; i++) {
            Interleave.expand64To128(jArr[i], jArr2, i << 1);
        }
        jArr2[12] = Interleave.expand32to64((int) jArr[6]);
    }

    public static void invert(long[] jArr, long[] jArr2) {
        if (!Nat448.isZero64(jArr)) {
            long[] create64 = Nat448.create64();
            long[] create642 = Nat448.create64();
            long[] create643 = Nat448.create64();
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
            multiply(create64, create642, create643);
            squareN(create643, 24, create64);
            squareN(create64, 24, create642);
            multiply(create64, create642, create64);
            squareN(create64, 48, create642);
            multiply(create64, create642, create64);
            squareN(create64, 96, create642);
            multiply(create64, create642, create64);
            squareN(create64, 192, create642);
            multiply(create64, create642, create64);
            multiply(create64, create643, jArr2);
            return;
        }
        throw new IllegalStateException();
    }

    public static void multiply(long[] jArr, long[] jArr2, long[] jArr3) {
        long[] createExt64 = Nat448.createExt64();
        implMultiply(jArr, jArr2, createExt64);
        reduce(createExt64, jArr3);
    }

    public static void multiplyAddToExt(long[] jArr, long[] jArr2, long[] jArr3) {
        long[] createExt64 = Nat448.createExt64();
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
        long j8 = jArr[7];
        long j9 = jArr[12];
        long j10 = j7 ^ ((j9 >>> 25) ^ (j9 << 62));
        long j11 = j8 ^ (j9 >>> 2);
        long j12 = jArr[11];
        long j13 = j5 ^ (j12 << 39);
        long j14 = (j6 ^ (j9 << 39)) ^ ((j12 >>> 25) ^ (j12 << 62));
        long j15 = j10 ^ (j12 >>> 2);
        long j16 = jArr[10];
        long j17 = j4 ^ (j16 << 39);
        long j18 = j13 ^ ((j16 >>> 25) ^ (j16 << 62));
        long j19 = j14 ^ (j16 >>> 2);
        long j20 = jArr[9];
        long j21 = j3 ^ (j20 << 39);
        long j22 = j17 ^ ((j20 >>> 25) ^ (j20 << 62));
        long j23 = j18 ^ (j20 >>> 2);
        long j24 = jArr[8];
        long j25 = j ^ (j11 << 39);
        long j26 = (j21 ^ ((j24 >>> 25) ^ (j24 << 62))) ^ (j11 >>> 2);
        long j27 = j15 >>> 25;
        jArr2[0] = j25 ^ j27;
        long j28 = j27 << 23;
        jArr2[1] = j28 ^ ((j2 ^ (j24 << 39)) ^ ((j11 >>> 25) ^ (j11 << 62)));
        jArr2[2] = j26;
        jArr2[3] = j22 ^ (j24 >>> 2);
        jArr2[4] = j23;
        jArr2[5] = j19;
        jArr2[6] = j15 & M25;
    }

    public static void reduce39(long[] jArr, int i) {
        int i2 = i + 6;
        long j = jArr[i2];
        long j2 = j >>> 25;
        jArr[i] = jArr[i] ^ j2;
        int i3 = i + 1;
        jArr[i3] = (j2 << 23) ^ jArr[i3];
        jArr[i2] = j & M25;
    }

    public static void sqrt(long[] jArr, long[] jArr2) {
        long unshuffle = Interleave.unshuffle(jArr[0]);
        long unshuffle2 = Interleave.unshuffle(jArr[1]);
        long j = (unshuffle & BodyPartID.bodyIdMax) | (unshuffle2 << 32);
        long j2 = (unshuffle >>> 32) | (unshuffle2 & -4294967296L);
        long unshuffle3 = Interleave.unshuffle(jArr[2]);
        long unshuffle4 = Interleave.unshuffle(jArr[3]);
        long j3 = (unshuffle3 & BodyPartID.bodyIdMax) | (unshuffle4 << 32);
        long j4 = (unshuffle3 >>> 32) | (unshuffle4 & -4294967296L);
        long unshuffle5 = Interleave.unshuffle(jArr[4]);
        long unshuffle6 = Interleave.unshuffle(jArr[5]);
        long j5 = (unshuffle5 >>> 32) | (unshuffle6 & -4294967296L);
        long unshuffle7 = Interleave.unshuffle(jArr[6]);
        long j6 = BodyPartID.bodyIdMax & unshuffle7;
        long j7 = unshuffle7 >>> 32;
        jArr2[0] = j ^ (j2 << 44);
        jArr2[1] = (j3 ^ (j4 << 44)) ^ (j2 >>> 20);
        jArr2[2] = (((unshuffle5 & BodyPartID.bodyIdMax) | (unshuffle6 << 32)) ^ (j5 << 44)) ^ (j4 >>> 20);
        jArr2[3] = ((j6 ^ (j7 << 44)) ^ (j5 >>> 20)) ^ (j2 << 13);
        jArr2[4] = ((j7 >>> 20) ^ (j4 << 13)) ^ (j2 >>> 51);
        jArr2[5] = (j4 >>> 51) ^ (j5 << 13);
        jArr2[6] = (j7 << 13) ^ (j5 >>> 51);
    }

    public static void square(long[] jArr, long[] jArr2) {
        long[] create64 = Nat.create64(13);
        implSquare(jArr, create64);
        reduce(create64, jArr2);
    }

    public static void squareAddToExt(long[] jArr, long[] jArr2) {
        long[] create64 = Nat.create64(13);
        implSquare(jArr, create64);
        addExt(jArr2, create64, jArr2);
    }

    public static void squareN(long[] jArr, int i, long[] jArr2) {
        long[] create64 = Nat.create64(13);
        implSquare(jArr, create64);
        while (true) {
            reduce(create64, jArr2);
            i--;
            if (i > 0) {
                implSquare(jArr2, create64);
            } else {
                return;
            }
        }
    }

    public static int trace(long[] jArr) {
        return ((int) jArr[0]) & 1;
    }
}
