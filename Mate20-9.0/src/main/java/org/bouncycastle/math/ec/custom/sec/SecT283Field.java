package org.bouncycastle.math.ec.custom.sec;

import java.math.BigInteger;
import org.bouncycastle.asn1.cmc.BodyPartID;
import org.bouncycastle.crypto.tls.CipherSuite;
import org.bouncycastle.math.raw.Interleave;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.math.raw.Nat320;

public class SecT283Field {
    private static final long M27 = 134217727;
    private static final long M57 = 144115188075855871L;
    private static final long[] ROOT_Z = {878416384462358536L, 3513665537849438403L, -9076969306111048948L, 585610922974906400L, 34087042};

    public static void add(long[] jArr, long[] jArr2, long[] jArr3) {
        jArr3[0] = jArr[0] ^ jArr2[0];
        jArr3[1] = jArr[1] ^ jArr2[1];
        jArr3[2] = jArr[2] ^ jArr2[2];
        jArr3[3] = jArr[3] ^ jArr2[3];
        jArr3[4] = jArr2[4] ^ jArr[4];
    }

    public static void addExt(long[] jArr, long[] jArr2, long[] jArr3) {
        jArr3[0] = jArr[0] ^ jArr2[0];
        jArr3[1] = jArr[1] ^ jArr2[1];
        jArr3[2] = jArr[2] ^ jArr2[2];
        jArr3[3] = jArr[3] ^ jArr2[3];
        jArr3[4] = jArr[4] ^ jArr2[4];
        jArr3[5] = jArr[5] ^ jArr2[5];
        jArr3[6] = jArr[6] ^ jArr2[6];
        jArr3[7] = jArr[7] ^ jArr2[7];
        jArr3[8] = jArr2[8] ^ jArr[8];
    }

    public static void addOne(long[] jArr, long[] jArr2) {
        jArr2[0] = jArr[0] ^ 1;
        jArr2[1] = jArr[1];
        jArr2[2] = jArr[2];
        jArr2[3] = jArr[3];
        jArr2[4] = jArr[4];
    }

    public static long[] fromBigInteger(BigInteger bigInteger) {
        long[] fromBigInteger64 = Nat320.fromBigInteger64(bigInteger);
        reduce37(fromBigInteger64, 0);
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
        jArr[0] = j ^ (j2 << 57);
        jArr[1] = (j2 >>> 7) ^ (j3 << 50);
        jArr[2] = (j3 >>> 14) ^ (j4 << 43);
        jArr[3] = (j4 >>> 21) ^ (j5 << 36);
        jArr[4] = (j5 >>> 28) ^ (j6 << 29);
        jArr[5] = (j6 >>> 35) ^ (j7 << 22);
        jArr[6] = (j7 >>> 42) ^ (j8 << 15);
        jArr[7] = (j8 >>> 49) ^ (j9 << 8);
        jArr[8] = (j9 >>> 56) ^ (j10 << 1);
        jArr[9] = j10 >>> 63;
    }

    protected static void implExpand(long[] jArr, long[] jArr2) {
        long j = jArr[0];
        long j2 = jArr[1];
        long j3 = jArr[2];
        long j4 = jArr[3];
        long j5 = jArr[4];
        jArr2[0] = j & M57;
        jArr2[1] = ((j >>> 57) ^ (j2 << 7)) & M57;
        jArr2[2] = ((j2 >>> 50) ^ (j3 << 14)) & M57;
        jArr2[3] = ((j3 >>> 43) ^ (j4 << 21)) & M57;
        jArr2[4] = (j4 >>> 36) ^ (j5 << 28);
    }

    protected static void implMultiply(long[] jArr, long[] jArr2, long[] jArr3) {
        long[] jArr4 = new long[5];
        long[] jArr5 = new long[5];
        implExpand(jArr, jArr4);
        implExpand(jArr2, jArr5);
        long[] jArr6 = new long[26];
        long[] jArr7 = jArr6;
        implMulw(jArr4[0], jArr5[0], jArr7, 0);
        implMulw(jArr4[1], jArr5[1], jArr7, 2);
        implMulw(jArr4[2], jArr5[2], jArr7, 4);
        implMulw(jArr4[3], jArr5[3], jArr7, 6);
        implMulw(jArr4[4], jArr5[4], jArr7, 8);
        long j = jArr4[0] ^ jArr4[1];
        long j2 = jArr5[0] ^ jArr5[1];
        long j3 = jArr4[0] ^ jArr4[2];
        long j4 = jArr5[0] ^ jArr5[2];
        long j5 = jArr4[2] ^ jArr4[4];
        long j6 = jArr5[2] ^ jArr5[4];
        long j7 = jArr4[3] ^ jArr4[4];
        long j8 = jArr5[3] ^ jArr5[4];
        implMulw(j3 ^ jArr4[3], j4 ^ jArr5[3], jArr7, 18);
        implMulw(j5 ^ jArr4[1], j6 ^ jArr5[1], jArr7, 20);
        long j9 = j ^ j7;
        long j10 = j2 ^ j8;
        long j11 = j9 ^ jArr4[2];
        long[] jArr8 = jArr6;
        implMulw(j9, j10, jArr8, 22);
        implMulw(j11, jArr5[2] ^ j10, jArr8, 24);
        implMulw(j, j2, jArr8, 10);
        implMulw(j3, j4, jArr8, 12);
        implMulw(j5, j6, jArr8, 14);
        implMulw(j7, j8, jArr8, 16);
        jArr3[0] = jArr6[0];
        jArr3[9] = jArr6[9];
        long j12 = jArr6[0] ^ jArr6[1];
        long j13 = jArr6[2] ^ j12;
        long j14 = jArr6[10] ^ j13;
        jArr3[1] = j14;
        long j15 = jArr6[3] ^ jArr6[4];
        long j16 = j13 ^ (j15 ^ (jArr6[11] ^ jArr6[12]));
        jArr3[2] = j16;
        long j17 = jArr6[5] ^ jArr6[6];
        long j18 = ((j12 ^ j15) ^ j17) ^ jArr6[8];
        long j19 = jArr6[13] ^ jArr6[14];
        jArr3[3] = (j18 ^ j19) ^ ((jArr6[18] ^ jArr6[22]) ^ jArr6[24]);
        long j20 = (jArr6[7] ^ jArr6[8]) ^ jArr6[9];
        long j21 = j20 ^ jArr6[17];
        jArr3[8] = j21;
        long j22 = (j20 ^ j17) ^ (jArr6[15] ^ jArr6[16]);
        jArr3[7] = j22;
        long j23 = j14 ^ j22;
        long j24 = (jArr6[19] ^ jArr6[20]) ^ (jArr6[25] ^ jArr6[24]);
        jArr3[4] = (j24 ^ (jArr6[18] ^ jArr6[23])) ^ j23;
        jArr3[5] = ((j16 ^ j21) ^ j24) ^ (jArr6[21] ^ jArr6[22]);
        jArr3[6] = ((((jArr6[9] ^ (j18 ^ jArr6[0])) ^ j19) ^ jArr6[21]) ^ jArr6[23]) ^ jArr6[25];
        implCompactExt(jArr3);
    }

    protected static void implMulw(long j, long j2, long[] jArr, int i) {
        long j3 = j;
        long[] jArr2 = new long[8];
        jArr2[1] = j2;
        jArr2[2] = jArr2[1] << 1;
        jArr2[3] = jArr2[2] ^ j2;
        jArr2[4] = jArr2[2] << 1;
        jArr2[5] = jArr2[4] ^ j2;
        jArr2[6] = jArr2[3] << 1;
        jArr2[7] = jArr2[6] ^ j2;
        int i2 = 48;
        long j4 = 0;
        long j5 = jArr2[((int) j3) & 7];
        do {
            int i3 = (int) (j3 >>> i2);
            long j6 = (jArr2[i3 & 7] ^ (jArr2[(i3 >>> 3) & 7] << 3)) ^ (jArr2[(i3 >>> 6) & 7] << 6);
            j5 ^= j6 << i2;
            j4 ^= j6 >>> (-i2);
            i2 -= 9;
        } while (i2 > 0);
        jArr[i] = M57 & j5;
        jArr[i + 1] = (((((j3 & 72198606942111744L) & ((j2 << 7) >> 63)) >>> 8) ^ j4) << 7) ^ (j5 >>> 57);
    }

    protected static void implSquare(long[] jArr, long[] jArr2) {
        for (int i = 0; i < 4; i++) {
            Interleave.expand64To128(jArr[i], jArr2, i << 1);
        }
        jArr2[8] = Interleave.expand32to64((int) jArr[4]);
    }

    public static void invert(long[] jArr, long[] jArr2) {
        if (!Nat320.isZero64(jArr)) {
            long[] create64 = Nat320.create64();
            long[] create642 = Nat320.create64();
            square(jArr, create64);
            multiply(create64, jArr, create64);
            squareN(create64, 2, create642);
            multiply(create642, create64, create642);
            squareN(create642, 4, create64);
            multiply(create64, create642, create64);
            squareN(create64, 8, create642);
            multiply(create642, create64, create642);
            square(create642, create642);
            multiply(create642, jArr, create642);
            squareN(create642, 17, create64);
            multiply(create64, create642, create64);
            square(create64, create64);
            multiply(create64, jArr, create64);
            squareN(create64, 35, create642);
            multiply(create642, create64, create642);
            squareN(create642, 70, create64);
            multiply(create64, create642, create64);
            square(create64, create64);
            multiply(create64, jArr, create64);
            squareN(create64, CipherSuite.TLS_PSK_WITH_AES_256_CBC_SHA, create642);
            multiply(create642, create64, create642);
            square(create642, jArr2);
            return;
        }
        throw new IllegalStateException();
    }

    public static void multiply(long[] jArr, long[] jArr2, long[] jArr3) {
        long[] createExt64 = Nat320.createExt64();
        implMultiply(jArr, jArr2, createExt64);
        reduce(createExt64, jArr3);
    }

    public static void multiplyAddToExt(long[] jArr, long[] jArr2, long[] jArr3) {
        long[] createExt64 = Nat320.createExt64();
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
        long j9 = jArr[8];
        long j10 = j5 ^ ((((j9 >>> 27) ^ (j9 >>> 22)) ^ (j9 >>> 20)) ^ (j9 >>> 15));
        long j11 = j ^ ((((j6 << 37) ^ (j6 << 42)) ^ (j6 << 44)) ^ (j6 << 49));
        long j12 = (j2 ^ ((((j7 << 37) ^ (j7 << 42)) ^ (j7 << 44)) ^ (j7 << 49))) ^ ((((j6 >>> 27) ^ (j6 >>> 22)) ^ (j6 >>> 20)) ^ (j6 >>> 15));
        long j13 = j10 >>> 27;
        jArr2[0] = (((j11 ^ j13) ^ (j13 << 5)) ^ (j13 << 7)) ^ (j13 << 12);
        jArr2[1] = j12;
        jArr2[2] = (j3 ^ ((((j8 << 37) ^ (j8 << 42)) ^ (j8 << 44)) ^ (j8 << 49))) ^ ((((j7 >>> 27) ^ (j7 >>> 22)) ^ (j7 >>> 20)) ^ (j7 >>> 15));
        jArr2[3] = (j4 ^ ((((j9 << 37) ^ (j9 << 42)) ^ (j9 << 44)) ^ (j9 << 49))) ^ ((((j8 >>> 27) ^ (j8 >>> 22)) ^ (j8 >>> 20)) ^ (j8 >>> 15));
        jArr2[4] = M27 & j10;
    }

    public static void reduce37(long[] jArr, int i) {
        int i2 = i + 4;
        long j = jArr[i2];
        long j2 = j >>> 27;
        jArr[i] = ((j2 << 12) ^ (((j2 << 5) ^ j2) ^ (j2 << 7))) ^ jArr[i];
        jArr[i2] = j & M27;
    }

    public static void sqrt(long[] jArr, long[] jArr2) {
        long[] jArr3 = jArr2;
        long[] create64 = Nat320.create64();
        long unshuffle = Interleave.unshuffle(jArr[0]);
        long unshuffle2 = Interleave.unshuffle(jArr[1]);
        create64[0] = (unshuffle >>> 32) | (unshuffle2 & -4294967296L);
        long unshuffle3 = Interleave.unshuffle(jArr[2]);
        long unshuffle4 = Interleave.unshuffle(jArr[3]);
        create64[1] = (unshuffle4 & -4294967296L) | (unshuffle3 >>> 32);
        long unshuffle5 = Interleave.unshuffle(jArr[4]);
        long j = unshuffle5 & BodyPartID.bodyIdMax;
        create64[2] = unshuffle5 >>> 32;
        multiply(create64, ROOT_Z, jArr3);
        jArr3[0] = jArr3[0] ^ ((unshuffle & BodyPartID.bodyIdMax) | (unshuffle2 << 32));
        jArr3[1] = jArr3[1] ^ ((unshuffle3 & BodyPartID.bodyIdMax) | (unshuffle4 << 32));
        jArr3[2] = jArr3[2] ^ j;
    }

    public static void square(long[] jArr, long[] jArr2) {
        long[] create64 = Nat.create64(9);
        implSquare(jArr, create64);
        reduce(create64, jArr2);
    }

    public static void squareAddToExt(long[] jArr, long[] jArr2) {
        long[] create64 = Nat.create64(9);
        implSquare(jArr, create64);
        addExt(jArr2, create64, jArr2);
    }

    public static void squareN(long[] jArr, int i, long[] jArr2) {
        long[] create64 = Nat.create64(9);
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
        return ((int) (jArr[0] ^ (jArr[4] >>> 15))) & 1;
    }
}
