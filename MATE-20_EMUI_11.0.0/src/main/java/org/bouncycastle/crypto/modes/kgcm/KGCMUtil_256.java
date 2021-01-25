package org.bouncycastle.crypto.modes.kgcm;

import org.bouncycastle.math.raw.Interleave;

public class KGCMUtil_256 {
    public static final int SIZE = 4;

    public static void add(long[] jArr, long[] jArr2, long[] jArr3) {
        jArr3[0] = jArr[0] ^ jArr2[0];
        jArr3[1] = jArr[1] ^ jArr2[1];
        jArr3[2] = jArr[2] ^ jArr2[2];
        jArr3[3] = jArr2[3] ^ jArr[3];
    }

    public static void copy(long[] jArr, long[] jArr2) {
        jArr2[0] = jArr[0];
        jArr2[1] = jArr[1];
        jArr2[2] = jArr[2];
        jArr2[3] = jArr[3];
    }

    public static boolean equal(long[] jArr, long[] jArr2) {
        return ((jArr2[3] ^ jArr[3]) | ((((jArr[0] ^ jArr2[0]) | 0) | (jArr[1] ^ jArr2[1])) | (jArr[2] ^ jArr2[2]))) == 0;
    }

    public static void multiply(long[] jArr, long[] jArr2, long[] jArr3) {
        long j = jArr[0];
        long j2 = jArr[1];
        long j3 = jArr[2];
        long j4 = jArr[3];
        long j5 = jArr2[0];
        long j6 = jArr2[1];
        long j7 = jArr2[2];
        long j8 = jArr2[3];
        long j9 = 0;
        long j10 = j2;
        long j11 = 0;
        long j12 = 0;
        long j13 = 0;
        long j14 = 0;
        long j15 = j;
        int i = 0;
        while (i < 64) {
            long j16 = -(j15 & 1);
            j15 >>>= 1;
            j9 ^= j5 & j16;
            long j17 = -(j10 & 1);
            j10 >>>= 1;
            j11 = (j11 ^ (j6 & j16)) ^ (j5 & j17);
            j12 = (j12 ^ (j7 & j16)) ^ (j6 & j17);
            j13 = (j13 ^ (j8 & j16)) ^ (j7 & j17);
            j14 ^= j8 & j17;
            long j18 = j8 >> 63;
            j8 = (j8 << 1) | (j7 >>> 63);
            j7 = (j7 << 1) | (j6 >>> 63);
            j6 = (j5 >>> 63) | (j6 << 1);
            j5 = (j5 << 1) ^ (j18 & 1061);
            i++;
            j4 = j4;
            j3 = j3;
        }
        long j19 = j3;
        long j20 = j4;
        long j21 = ((j8 ^ (j8 << 2)) ^ (j8 << 5)) ^ (j8 << 10);
        long j22 = j7;
        long j23 = j6;
        long j24 = (((j8 >>> 62) ^ j5) ^ (j8 >>> 59)) ^ (j8 >>> 54);
        for (int i2 = 0; i2 < 64; i2++) {
            long j25 = -(j19 & 1);
            j19 >>>= 1;
            j9 ^= j21 & j25;
            long j26 = -(j20 & 1);
            j20 >>>= 1;
            j11 = (j11 ^ (j24 & j25)) ^ (j21 & j26);
            j12 = (j12 ^ (j23 & j25)) ^ (j24 & j26);
            j13 = (j13 ^ (j22 & j25)) ^ (j23 & j26);
            j14 ^= j22 & j26;
            long j27 = j22 >> 63;
            j22 = (j22 << 1) | (j23 >>> 63);
            j23 = (j24 >>> 63) | (j23 << 1);
            j24 = (j21 >>> 63) | (j24 << 1);
            j21 = (j21 << 1) ^ (j27 & 1061);
        }
        jArr3[0] = j9 ^ (((j14 ^ (j14 << 2)) ^ (j14 << 5)) ^ (j14 << 10));
        jArr3[1] = j11 ^ (((j14 >>> 62) ^ (j14 >>> 59)) ^ (j14 >>> 54));
        jArr3[2] = j12;
        jArr3[3] = j13;
    }

    public static void multiplyX(long[] jArr, long[] jArr2) {
        long j = jArr[0];
        long j2 = jArr[1];
        long j3 = jArr[2];
        long j4 = jArr[3];
        jArr2[0] = ((j4 >> 63) & 1061) ^ (j << 1);
        jArr2[1] = (j >>> 63) | (j2 << 1);
        jArr2[2] = (j3 << 1) | (j2 >>> 63);
        jArr2[3] = (j4 << 1) | (j3 >>> 63);
    }

    public static void multiplyX8(long[] jArr, long[] jArr2) {
        long j = jArr[0];
        long j2 = jArr[1];
        long j3 = jArr[2];
        long j4 = jArr[3];
        long j5 = j4 >>> 56;
        jArr2[0] = ((((j << 8) ^ j5) ^ (j5 << 2)) ^ (j5 << 5)) ^ (j5 << 10);
        jArr2[1] = (j >>> 56) | (j2 << 8);
        jArr2[2] = (j3 << 8) | (j2 >>> 56);
        jArr2[3] = (j4 << 8) | (j3 >>> 56);
    }

    public static void one(long[] jArr) {
        jArr[0] = 1;
        jArr[1] = 0;
        jArr[2] = 0;
        jArr[3] = 0;
    }

    public static void square(long[] jArr, long[] jArr2) {
        int i = 8;
        long[] jArr3 = new long[8];
        for (int i2 = 0; i2 < 4; i2++) {
            Interleave.expand64To128(jArr[i2], jArr3, i2 << 1);
        }
        while (true) {
            i--;
            if (i >= 4) {
                long j = jArr3[i];
                int i3 = i - 4;
                jArr3[i3] = jArr3[i3] ^ ((((j << 2) ^ j) ^ (j << 5)) ^ (j << 10));
                int i4 = i3 + 1;
                jArr3[i4] = ((j >>> 54) ^ ((j >>> 62) ^ (j >>> 59))) ^ jArr3[i4];
            } else {
                copy(jArr3, jArr2);
                return;
            }
        }
    }

    public static void x(long[] jArr) {
        jArr[0] = 2;
        jArr[1] = 0;
        jArr[2] = 0;
        jArr[3] = 0;
    }

    public static void zero(long[] jArr) {
        jArr[0] = 0;
        jArr[1] = 0;
        jArr[2] = 0;
        jArr[3] = 0;
    }
}
