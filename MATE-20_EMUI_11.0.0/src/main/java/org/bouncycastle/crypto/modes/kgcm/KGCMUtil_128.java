package org.bouncycastle.crypto.modes.kgcm;

import org.bouncycastle.math.raw.Interleave;

public class KGCMUtil_128 {
    public static final int SIZE = 2;

    public static void add(long[] jArr, long[] jArr2, long[] jArr3) {
        jArr3[0] = jArr[0] ^ jArr2[0];
        jArr3[1] = jArr2[1] ^ jArr[1];
    }

    public static void copy(long[] jArr, long[] jArr2) {
        jArr2[0] = jArr[0];
        jArr2[1] = jArr[1];
    }

    public static boolean equal(long[] jArr, long[] jArr2) {
        return ((jArr2[1] ^ jArr[1]) | ((jArr[0] ^ jArr2[0]) | 0)) == 0;
    }

    public static void multiply(long[] jArr, long[] jArr2, long[] jArr3) {
        long j = jArr[0];
        long j2 = 0;
        long j3 = jArr[1];
        long j4 = jArr2[0];
        long j5 = jArr2[1];
        long j6 = 0;
        long j7 = 0;
        long j8 = j;
        int i = 0;
        while (i < 64) {
            long j9 = -(j8 & 1);
            j8 >>>= 1;
            j2 ^= j4 & j9;
            long j10 = (j9 & j5) ^ j7;
            long j11 = -(j3 & 1);
            j3 >>>= 1;
            j6 ^= j11 & j5;
            long j12 = j5 >> 63;
            j5 = (j5 << 1) | (j4 >>> 63);
            j4 = (j4 << 1) ^ (j12 & 135);
            i++;
            j7 = j10 ^ (j4 & j11);
        }
        jArr3[0] = ((((j6 << 1) ^ j6) ^ (j6 << 2)) ^ (j6 << 7)) ^ j2;
        jArr3[1] = ((j6 >>> 57) ^ ((j6 >>> 63) ^ (j6 >>> 62))) ^ j7;
    }

    public static void multiplyX(long[] jArr, long[] jArr2) {
        long j = jArr[0];
        long j2 = jArr[1];
        jArr2[0] = ((j2 >> 63) & 135) ^ (j << 1);
        jArr2[1] = (j >>> 63) | (j2 << 1);
    }

    public static void multiplyX8(long[] jArr, long[] jArr2) {
        long j = jArr[0];
        long j2 = jArr[1];
        long j3 = j2 >>> 56;
        jArr2[0] = (j3 << 7) ^ ((((j << 8) ^ j3) ^ (j3 << 1)) ^ (j3 << 2));
        jArr2[1] = (j >>> 56) | (j2 << 8);
    }

    public static void one(long[] jArr) {
        jArr[0] = 1;
        jArr[1] = 0;
    }

    public static void square(long[] jArr, long[] jArr2) {
        long[] jArr3 = new long[4];
        Interleave.expand64To128(jArr[0], jArr3, 0);
        Interleave.expand64To128(jArr[1], jArr3, 2);
        long j = jArr3[0];
        long j2 = jArr3[1];
        long j3 = jArr3[2];
        long j4 = jArr3[3];
        long j5 = j3 ^ ((j4 >>> 57) ^ ((j4 >>> 63) ^ (j4 >>> 62)));
        long j6 = j ^ ((((j5 << 1) ^ j5) ^ (j5 << 2)) ^ (j5 << 7));
        jArr2[0] = j6;
        jArr2[1] = (j2 ^ ((((j4 << 1) ^ j4) ^ (j4 << 2)) ^ (j4 << 7))) ^ ((j5 >>> 57) ^ ((j5 >>> 63) ^ (j5 >>> 62)));
    }

    public static void x(long[] jArr) {
        jArr[0] = 2;
        jArr[1] = 0;
    }

    public static void zero(long[] jArr) {
        jArr[0] = 0;
        jArr[1] = 0;
    }
}
