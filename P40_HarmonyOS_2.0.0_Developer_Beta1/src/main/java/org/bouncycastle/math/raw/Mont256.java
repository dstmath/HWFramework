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
        char c = 0;
        long j = ((long) iArr2[0]) & 4294967295L;
        int i2 = 0;
        int i3 = 0;
        while (i2 < 8) {
            long j2 = ((long) iArr[i2]) & 4294967295L;
            long j3 = j2 * j;
            long j4 = (j3 & 4294967295L) + (((long) iArr3[c]) & 4294967295L);
            long j5 = ((long) (((int) j4) * i)) & 4294967295L;
            long j6 = (((long) iArr4[c]) & 4294967295L) * j5;
            char c2 = ' ';
            long j7 = ((j4 + (j6 & 4294967295L)) >>> 32) + (j3 >>> 32) + (j6 >>> 32);
            int i4 = 1;
            while (i4 < 8) {
                long j8 = (((long) iArr2[i4]) & 4294967295L) * j2;
                long j9 = (((long) iArr4[i4]) & 4294967295L) * j5;
                long j10 = j7 + (j8 & 4294967295L) + (j9 & 4294967295L) + (((long) iArr3[i4]) & 4294967295L);
                iArr3[i4 - 1] = (int) j10;
                j7 = (j10 >>> 32) + (j8 >>> 32) + (j9 >>> 32);
                i4++;
                c2 = ' ';
                j5 = j5;
            }
            long j11 = j7 + (((long) i3) & 4294967295L);
            iArr3[7] = (int) j11;
            i3 = (int) (j11 >>> c2);
            i2++;
            j = j;
            c = 0;
        }
        if (i3 != 0 || Nat256.gte(iArr3, iArr4)) {
            Nat256.sub(iArr3, iArr4, iArr3);
        }
    }

    public static void multAddXF(int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4) {
        char c = 0;
        long j = ((long) iArr2[0]) & 4294967295L;
        int i = 0;
        int i2 = 0;
        while (true) {
            if (i >= 8) {
                break;
            }
            long j2 = ((long) iArr[i]) & 4294967295L;
            long j3 = (j2 * j) + (((long) iArr3[c]) & 4294967295L);
            long j4 = j3 & 4294967295L;
            long j5 = (j3 >>> 32) + j4;
            int i3 = 1;
            long j6 = j5;
            for (int i4 = 8; i3 < i4; i4 = 8) {
                long j7 = (((long) iArr2[i3]) & 4294967295L) * j2;
                long j8 = (((long) iArr4[i3]) & 4294967295L) * j4;
                long j9 = j6 + (j7 & 4294967295L) + (j8 & 4294967295L) + (((long) iArr3[i3]) & 4294967295L);
                iArr3[i3 - 1] = (int) j9;
                j6 = (j9 >>> 32) + (j7 >>> 32) + (j8 >>> 32);
                i3++;
                j = j;
                j2 = j2;
                j4 = j4;
            }
            long j10 = j6 + (((long) i2) & 4294967295L);
            iArr3[7] = (int) j10;
            i2 = (int) (j10 >>> 32);
            i++;
            j = j;
            c = 0;
        }
        if (i2 != 0 || Nat256.gte(iArr3, iArr4)) {
            Nat256.sub(iArr3, iArr4, iArr3);
        }
    }

    public static void reduce(int[] iArr, int[] iArr2, int i) {
        char c = 0;
        int i2 = 0;
        while (i2 < 8) {
            int i3 = iArr[c];
            long j = ((long) (i3 * i)) & 4294967295L;
            long j2 = (((((long) iArr2[c]) & 4294967295L) * j) + (((long) i3) & 4294967295L)) >>> 32;
            int i4 = 1;
            while (i4 < 8) {
                long j3 = j2 + ((((long) iArr2[i4]) & 4294967295L) * j) + (((long) iArr[i4]) & 4294967295L);
                iArr[i4 - 1] = (int) j3;
                j2 = j3 >>> 32;
                i4++;
                i2 = i2;
            }
            iArr[7] = (int) j2;
            i2++;
            c = 0;
        }
        if (Nat256.gte(iArr, iArr2)) {
            Nat256.sub(iArr, iArr2, iArr);
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
