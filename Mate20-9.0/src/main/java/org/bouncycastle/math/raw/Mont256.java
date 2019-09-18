package org.bouncycastle.math.raw;

public abstract class Mont256 {
    private static final long M = 4294967295L;

    public static int inverse32(int i) {
        int i2 = (2 - (i * i)) * i;
        int i3 = i2 * (2 - (i * i2));
        int i4 = i3 * (2 - (i * i3));
        return i4 * (2 - (i * i4));
    }

    public static void multAdd(int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4, int i) {
        int[] iArr5 = iArr3;
        int[] iArr6 = iArr4;
        char c = 0;
        long j = ((long) iArr2[0]) & 4294967295L;
        int i2 = 0;
        int i3 = 0;
        while (i2 < 8) {
            long j2 = ((long) iArr[i2]) & 4294967295L;
            long j3 = j2 * j;
            long j4 = (j3 & 4294967295L) + (((long) iArr5[c]) & 4294967295L);
            long j5 = j;
            long j6 = ((long) (((int) j4) * i)) & 4294967295L;
            int i4 = i2;
            int i5 = i3;
            long j7 = (((long) iArr6[c]) & 4294967295L) * j6;
            char c2 = ' ';
            long j8 = ((j4 + (j7 & 4294967295L)) >>> 32) + (j3 >>> 32) + (j7 >>> 32);
            int i6 = 1;
            while (i6 < 8) {
                long j9 = (((long) iArr2[i6]) & 4294967295L) * j2;
                long j10 = (((long) iArr6[i6]) & 4294967295L) * j6;
                long j11 = j8 + (j9 & 4294967295L) + (j10 & 4294967295L) + (((long) iArr5[i6]) & 4294967295L);
                iArr5[i6 - 1] = (int) j11;
                j8 = (j11 >>> 32) + (j9 >>> 32) + (j10 >>> 32);
                i6++;
                c2 = ' ';
                j2 = j2;
                j6 = j6;
            }
            long j12 = j8 + (((long) i5) & 4294967295L);
            iArr5[7] = (int) j12;
            i3 = (int) (j12 >>> c2);
            i2 = i4 + 1;
            j = j5;
            c = 0;
        }
        if (i3 != 0 || Nat256.gte(iArr3, iArr4)) {
            Nat256.sub(iArr5, iArr6, iArr5);
        }
    }

    public static void multAddXF(int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4) {
        int[] iArr5 = iArr3;
        int[] iArr6 = iArr4;
        char c = 0;
        long j = ((long) iArr2[0]) & 4294967295L;
        int i = 0;
        int i2 = 0;
        while (i < 8) {
            long j2 = ((long) iArr[i]) & 4294967295L;
            long j3 = j;
            long j4 = (j2 * j) + (((long) iArr5[c]) & 4294967295L);
            long j5 = j4 & 4294967295L;
            long j6 = (j4 >>> 32) + j5;
            int i3 = 1;
            while (i3 < 8) {
                int i4 = i;
                long j7 = (((long) iArr2[i3]) & 4294967295L) * j2;
                long j8 = (((long) iArr6[i3]) & 4294967295L) * j5;
                long j9 = j6 + (j7 & 4294967295L) + (j8 & 4294967295L) + (((long) iArr5[i3]) & 4294967295L);
                iArr5[i3 - 1] = (int) j9;
                j6 = (j9 >>> 32) + (j7 >>> 32) + (j8 >>> 32);
                i3++;
                i = i4;
                i2 = i2;
                j2 = j2;
                j5 = j5;
            }
            long j10 = j6 + (((long) i2) & 4294967295L);
            iArr5[7] = (int) j10;
            i2 = (int) (j10 >>> 32);
            i++;
            j = j3;
            c = 0;
        }
        if (i2 != 0 || Nat256.gte(iArr3, iArr4)) {
            Nat256.sub(iArr5, iArr6, iArr5);
        }
    }

    public static void reduce(int[] iArr, int[] iArr2, int i) {
        int[] iArr3 = iArr;
        int[] iArr4 = iArr2;
        char c = 0;
        int i2 = 0;
        while (true) {
            if (i2 >= 8) {
                break;
            }
            int i3 = iArr3[c];
            long j = ((long) (i3 * i)) & 4294967295L;
            long j2 = (((((long) iArr4[c]) & 4294967295L) * j) + (((long) i3) & 4294967295L)) >>> 32;
            int i4 = 1;
            for (int i5 = 8; i4 < i5; i5 = 8) {
                long j3 = j2 + ((((long) iArr4[i4]) & 4294967295L) * j) + (((long) iArr3[i4]) & 4294967295L);
                iArr3[i4 - 1] = (int) j3;
                j2 = j3 >>> 32;
                i4++;
            }
            iArr3[7] = (int) j2;
            i2++;
            c = 0;
        }
        if (Nat256.gte(iArr, iArr2)) {
            Nat256.sub(iArr3, iArr4, iArr3);
        }
    }

    public static void reduceXF(int[] iArr, int[] iArr2) {
        for (int i = 0; i < 8; i++) {
            long j = ((long) iArr[0]) & 4294967295L;
            long j2 = j;
            for (int i2 = 1; i2 < 8; i2++) {
                long j3 = j2 + ((((long) iArr2[i2]) & 4294967295L) * j) + (((long) iArr[i2]) & 4294967295L);
                iArr[i2 - 1] = (int) j3;
                j2 = j3 >>> 32;
            }
            iArr[7] = (int) j2;
        }
        if (Nat256.gte(iArr, iArr2)) {
            Nat256.sub(iArr, iArr2, iArr);
        }
    }
}
