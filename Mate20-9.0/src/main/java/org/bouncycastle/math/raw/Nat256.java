package org.bouncycastle.math.raw;

import java.math.BigInteger;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.util.Pack;

public abstract class Nat256 {
    private static final long M = 4294967295L;

    public static int add(int[] iArr, int i, int[] iArr2, int i2, int[] iArr3, int i3) {
        long j = 0 + (((long) iArr[i + 0]) & 4294967295L) + (((long) iArr2[i2 + 0]) & 4294967295L);
        iArr3[i3 + 0] = (int) j;
        long j2 = (j >>> 32) + (((long) iArr[i + 1]) & 4294967295L) + (((long) iArr2[i2 + 1]) & 4294967295L);
        iArr3[i3 + 1] = (int) j2;
        long j3 = (j2 >>> 32) + (((long) iArr[i + 2]) & 4294967295L) + (((long) iArr2[i2 + 2]) & 4294967295L);
        iArr3[i3 + 2] = (int) j3;
        long j4 = (j3 >>> 32) + (((long) iArr[i + 3]) & 4294967295L) + (((long) iArr2[i2 + 3]) & 4294967295L);
        iArr3[i3 + 3] = (int) j4;
        long j5 = (j4 >>> 32) + (((long) iArr[i + 4]) & 4294967295L) + (((long) iArr2[i2 + 4]) & 4294967295L);
        iArr3[i3 + 4] = (int) j5;
        long j6 = (j5 >>> 32) + (((long) iArr[i + 5]) & 4294967295L) + (((long) iArr2[i2 + 5]) & 4294967295L);
        iArr3[i3 + 5] = (int) j6;
        long j7 = (j6 >>> 32) + (((long) iArr[i + 6]) & 4294967295L) + (((long) iArr2[i2 + 6]) & 4294967295L);
        iArr3[i3 + 6] = (int) j7;
        long j8 = (j7 >>> 32) + (((long) iArr[i + 7]) & 4294967295L) + (((long) iArr2[i2 + 7]) & 4294967295L);
        iArr3[i3 + 7] = (int) j8;
        return (int) (j8 >>> 32);
    }

    public static int add(int[] iArr, int[] iArr2, int[] iArr3) {
        long j = 0 + (((long) iArr[0]) & 4294967295L) + (((long) iArr2[0]) & 4294967295L);
        iArr3[0] = (int) j;
        long j2 = (j >>> 32) + (((long) iArr[1]) & 4294967295L) + (((long) iArr2[1]) & 4294967295L);
        iArr3[1] = (int) j2;
        long j3 = (j2 >>> 32) + (((long) iArr[2]) & 4294967295L) + (((long) iArr2[2]) & 4294967295L);
        iArr3[2] = (int) j3;
        long j4 = (j3 >>> 32) + (((long) iArr[3]) & 4294967295L) + (((long) iArr2[3]) & 4294967295L);
        iArr3[3] = (int) j4;
        long j5 = (j4 >>> 32) + (((long) iArr[4]) & 4294967295L) + (((long) iArr2[4]) & 4294967295L);
        iArr3[4] = (int) j5;
        long j6 = (j5 >>> 32) + (((long) iArr[5]) & 4294967295L) + (((long) iArr2[5]) & 4294967295L);
        iArr3[5] = (int) j6;
        long j7 = (j6 >>> 32) + (((long) iArr[6]) & 4294967295L) + (((long) iArr2[6]) & 4294967295L);
        iArr3[6] = (int) j7;
        long j8 = (j7 >>> 32) + (((long) iArr[7]) & 4294967295L) + (((long) iArr2[7]) & 4294967295L);
        iArr3[7] = (int) j8;
        return (int) (j8 >>> 32);
    }

    public static int addBothTo(int[] iArr, int i, int[] iArr2, int i2, int[] iArr3, int i3) {
        int i4 = i3 + 0;
        long j = 0 + (((long) iArr[i + 0]) & 4294967295L) + (((long) iArr2[i2 + 0]) & 4294967295L) + (((long) iArr3[i4]) & 4294967295L);
        iArr3[i4] = (int) j;
        int i5 = i3 + 1;
        long j2 = (j >>> 32) + (((long) iArr[i + 1]) & 4294967295L) + (((long) iArr2[i2 + 1]) & 4294967295L) + (((long) iArr3[i5]) & 4294967295L);
        iArr3[i5] = (int) j2;
        int i6 = i3 + 2;
        long j3 = (j2 >>> 32) + (((long) iArr[i + 2]) & 4294967295L) + (((long) iArr2[i2 + 2]) & 4294967295L) + (((long) iArr3[i6]) & 4294967295L);
        iArr3[i6] = (int) j3;
        int i7 = i3 + 3;
        long j4 = (j3 >>> 32) + (((long) iArr[i + 3]) & 4294967295L) + (((long) iArr2[i2 + 3]) & 4294967295L) + (((long) iArr3[i7]) & 4294967295L);
        iArr3[i7] = (int) j4;
        int i8 = i3 + 4;
        long j5 = (j4 >>> 32) + (((long) iArr[i + 4]) & 4294967295L) + (((long) iArr2[i2 + 4]) & 4294967295L) + (((long) iArr3[i8]) & 4294967295L);
        iArr3[i8] = (int) j5;
        int i9 = i3 + 5;
        long j6 = (j5 >>> 32) + (((long) iArr[i + 5]) & 4294967295L) + (((long) iArr2[i2 + 5]) & 4294967295L) + (((long) iArr3[i9]) & 4294967295L);
        iArr3[i9] = (int) j6;
        int i10 = i3 + 6;
        long j7 = (j6 >>> 32) + (((long) iArr[i + 6]) & 4294967295L) + (((long) iArr2[i2 + 6]) & 4294967295L) + (((long) iArr3[i10]) & 4294967295L);
        iArr3[i10] = (int) j7;
        int i11 = i3 + 7;
        long j8 = (j7 >>> 32) + (((long) iArr[i + 7]) & 4294967295L) + (((long) iArr2[i2 + 7]) & 4294967295L) + (((long) iArr3[i11]) & 4294967295L);
        iArr3[i11] = (int) j8;
        return (int) (j8 >>> 32);
    }

    public static int addBothTo(int[] iArr, int[] iArr2, int[] iArr3) {
        long j = 0 + (((long) iArr[0]) & 4294967295L) + (((long) iArr2[0]) & 4294967295L) + (((long) iArr3[0]) & 4294967295L);
        iArr3[0] = (int) j;
        long j2 = (j >>> 32) + (((long) iArr[1]) & 4294967295L) + (((long) iArr2[1]) & 4294967295L) + (((long) iArr3[1]) & 4294967295L);
        iArr3[1] = (int) j2;
        long j3 = (j2 >>> 32) + (((long) iArr[2]) & 4294967295L) + (((long) iArr2[2]) & 4294967295L) + (((long) iArr3[2]) & 4294967295L);
        iArr3[2] = (int) j3;
        long j4 = (j3 >>> 32) + (((long) iArr[3]) & 4294967295L) + (((long) iArr2[3]) & 4294967295L) + (((long) iArr3[3]) & 4294967295L);
        iArr3[3] = (int) j4;
        long j5 = (j4 >>> 32) + (((long) iArr[4]) & 4294967295L) + (((long) iArr2[4]) & 4294967295L) + (((long) iArr3[4]) & 4294967295L);
        iArr3[4] = (int) j5;
        long j6 = (j5 >>> 32) + (((long) iArr[5]) & 4294967295L) + (((long) iArr2[5]) & 4294967295L) + (((long) iArr3[5]) & 4294967295L);
        iArr3[5] = (int) j6;
        long j7 = (j6 >>> 32) + (((long) iArr[6]) & 4294967295L) + (((long) iArr2[6]) & 4294967295L) + (((long) iArr3[6]) & 4294967295L);
        iArr3[6] = (int) j7;
        long j8 = (j7 >>> 32) + (((long) iArr[7]) & 4294967295L) + (((long) iArr2[7]) & 4294967295L) + (((long) iArr3[7]) & 4294967295L);
        iArr3[7] = (int) j8;
        return (int) (j8 >>> 32);
    }

    public static int addTo(int[] iArr, int i, int[] iArr2, int i2, int i3) {
        int i4 = i2 + 0;
        long j = (((long) i3) & 4294967295L) + (((long) iArr[i + 0]) & 4294967295L) + (((long) iArr2[i4]) & 4294967295L);
        iArr2[i4] = (int) j;
        int i5 = i2 + 1;
        long j2 = (j >>> 32) + (((long) iArr[i + 1]) & 4294967295L) + (((long) iArr2[i5]) & 4294967295L);
        iArr2[i5] = (int) j2;
        int i6 = i2 + 2;
        long j3 = (j2 >>> 32) + (((long) iArr[i + 2]) & 4294967295L) + (((long) iArr2[i6]) & 4294967295L);
        iArr2[i6] = (int) j3;
        int i7 = i2 + 3;
        long j4 = (j3 >>> 32) + (((long) iArr[i + 3]) & 4294967295L) + (((long) iArr2[i7]) & 4294967295L);
        iArr2[i7] = (int) j4;
        int i8 = i2 + 4;
        long j5 = (j4 >>> 32) + (((long) iArr[i + 4]) & 4294967295L) + (((long) iArr2[i8]) & 4294967295L);
        iArr2[i8] = (int) j5;
        int i9 = i2 + 5;
        long j6 = (j5 >>> 32) + (((long) iArr[i + 5]) & 4294967295L) + (((long) iArr2[i9]) & 4294967295L);
        iArr2[i9] = (int) j6;
        int i10 = i2 + 6;
        long j7 = (j6 >>> 32) + (((long) iArr[i + 6]) & 4294967295L) + (((long) iArr2[i10]) & 4294967295L);
        iArr2[i10] = (int) j7;
        int i11 = i2 + 7;
        long j8 = (j7 >>> 32) + (((long) iArr[i + 7]) & 4294967295L) + (4294967295L & ((long) iArr2[i11]));
        iArr2[i11] = (int) j8;
        return (int) (j8 >>> 32);
    }

    public static int addTo(int[] iArr, int[] iArr2) {
        long j = 0 + (((long) iArr[0]) & 4294967295L) + (((long) iArr2[0]) & 4294967295L);
        iArr2[0] = (int) j;
        long j2 = (j >>> 32) + (((long) iArr[1]) & 4294967295L) + (((long) iArr2[1]) & 4294967295L);
        iArr2[1] = (int) j2;
        long j3 = (j2 >>> 32) + (((long) iArr[2]) & 4294967295L) + (((long) iArr2[2]) & 4294967295L);
        iArr2[2] = (int) j3;
        long j4 = (j3 >>> 32) + (((long) iArr[3]) & 4294967295L) + (((long) iArr2[3]) & 4294967295L);
        iArr2[3] = (int) j4;
        long j5 = (j4 >>> 32) + (((long) iArr[4]) & 4294967295L) + (((long) iArr2[4]) & 4294967295L);
        iArr2[4] = (int) j5;
        long j6 = (j5 >>> 32) + (((long) iArr[5]) & 4294967295L) + (((long) iArr2[5]) & 4294967295L);
        iArr2[5] = (int) j6;
        long j7 = (j6 >>> 32) + (((long) iArr[6]) & 4294967295L) + (((long) iArr2[6]) & 4294967295L);
        iArr2[6] = (int) j7;
        long j8 = (j7 >>> 32) + (((long) iArr[7]) & 4294967295L) + (4294967295L & ((long) iArr2[7]));
        iArr2[7] = (int) j8;
        return (int) (j8 >>> 32);
    }

    public static int addToEachOther(int[] iArr, int i, int[] iArr2, int i2) {
        int i3 = i + 0;
        int i4 = i2 + 0;
        long j = 0 + (((long) iArr[i3]) & 4294967295L) + (((long) iArr2[i4]) & 4294967295L);
        int i5 = (int) j;
        iArr[i3] = i5;
        iArr2[i4] = i5;
        int i6 = i + 1;
        int i7 = i2 + 1;
        long j2 = (j >>> 32) + (((long) iArr[i6]) & 4294967295L) + (((long) iArr2[i7]) & 4294967295L);
        int i8 = (int) j2;
        iArr[i6] = i8;
        iArr2[i7] = i8;
        int i9 = i + 2;
        int i10 = i2 + 2;
        long j3 = (j2 >>> 32) + (((long) iArr[i9]) & 4294967295L) + (((long) iArr2[i10]) & 4294967295L);
        int i11 = (int) j3;
        iArr[i9] = i11;
        iArr2[i10] = i11;
        int i12 = i + 3;
        int i13 = i2 + 3;
        long j4 = (j3 >>> 32) + (((long) iArr[i12]) & 4294967295L) + (((long) iArr2[i13]) & 4294967295L);
        int i14 = (int) j4;
        iArr[i12] = i14;
        iArr2[i13] = i14;
        int i15 = i + 4;
        int i16 = i2 + 4;
        long j5 = (j4 >>> 32) + (((long) iArr[i15]) & 4294967295L) + (((long) iArr2[i16]) & 4294967295L);
        int i17 = (int) j5;
        iArr[i15] = i17;
        iArr2[i16] = i17;
        int i18 = i + 5;
        int i19 = i2 + 5;
        long j6 = (j5 >>> 32) + (((long) iArr[i18]) & 4294967295L) + (((long) iArr2[i19]) & 4294967295L);
        int i20 = (int) j6;
        iArr[i18] = i20;
        iArr2[i19] = i20;
        int i21 = i + 6;
        int i22 = i2 + 6;
        long j7 = (j6 >>> 32) + (((long) iArr[i21]) & 4294967295L) + (((long) iArr2[i22]) & 4294967295L);
        int i23 = (int) j7;
        iArr[i21] = i23;
        iArr2[i22] = i23;
        int i24 = i + 7;
        int i25 = i2 + 7;
        long j8 = (j7 >>> 32) + (((long) iArr[i24]) & 4294967295L) + (4294967295L & ((long) iArr2[i25]));
        int i26 = (int) j8;
        iArr[i24] = i26;
        iArr2[i25] = i26;
        return (int) (j8 >>> 32);
    }

    public static void copy(int[] iArr, int i, int[] iArr2, int i2) {
        iArr2[i2 + 0] = iArr[i + 0];
        iArr2[i2 + 1] = iArr[i + 1];
        iArr2[i2 + 2] = iArr[i + 2];
        iArr2[i2 + 3] = iArr[i + 3];
        iArr2[i2 + 4] = iArr[i + 4];
        iArr2[i2 + 5] = iArr[i + 5];
        iArr2[i2 + 6] = iArr[i + 6];
        iArr2[i2 + 7] = iArr[i + 7];
    }

    public static void copy(int[] iArr, int[] iArr2) {
        iArr2[0] = iArr[0];
        iArr2[1] = iArr[1];
        iArr2[2] = iArr[2];
        iArr2[3] = iArr[3];
        iArr2[4] = iArr[4];
        iArr2[5] = iArr[5];
        iArr2[6] = iArr[6];
        iArr2[7] = iArr[7];
    }

    public static void copy64(long[] jArr, int i, long[] jArr2, int i2) {
        jArr2[i2 + 0] = jArr[i + 0];
        jArr2[i2 + 1] = jArr[i + 1];
        jArr2[i2 + 2] = jArr[i + 2];
        jArr2[i2 + 3] = jArr[i + 3];
    }

    public static void copy64(long[] jArr, long[] jArr2) {
        jArr2[0] = jArr[0];
        jArr2[1] = jArr[1];
        jArr2[2] = jArr[2];
        jArr2[3] = jArr[3];
    }

    public static int[] create() {
        return new int[8];
    }

    public static long[] create64() {
        return new long[4];
    }

    public static int[] createExt() {
        return new int[16];
    }

    public static long[] createExt64() {
        return new long[8];
    }

    public static boolean diff(int[] iArr, int i, int[] iArr2, int i2, int[] iArr3, int i3) {
        boolean gte = gte(iArr, i, iArr2, i2);
        if (gte) {
            sub(iArr, i, iArr2, i2, iArr3, i3);
            return gte;
        }
        sub(iArr2, i2, iArr, i, iArr3, i3);
        return gte;
    }

    public static boolean eq(int[] iArr, int[] iArr2) {
        for (int i = 7; i >= 0; i--) {
            if (iArr[i] != iArr2[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean eq64(long[] jArr, long[] jArr2) {
        for (int i = 3; i >= 0; i--) {
            if (jArr[i] != jArr2[i]) {
                return false;
            }
        }
        return true;
    }

    public static int[] fromBigInteger(BigInteger bigInteger) {
        if (bigInteger.signum() < 0 || bigInteger.bitLength() > 256) {
            throw new IllegalArgumentException();
        }
        int[] create = create();
        int i = 0;
        while (bigInteger.signum() != 0) {
            create[i] = bigInteger.intValue();
            bigInteger = bigInteger.shiftRight(32);
            i++;
        }
        return create;
    }

    public static long[] fromBigInteger64(BigInteger bigInteger) {
        if (bigInteger.signum() < 0 || bigInteger.bitLength() > 256) {
            throw new IllegalArgumentException();
        }
        long[] create64 = create64();
        int i = 0;
        while (bigInteger.signum() != 0) {
            create64[i] = bigInteger.longValue();
            bigInteger = bigInteger.shiftRight(64);
            i++;
        }
        return create64;
    }

    public static int getBit(int[] iArr, int i) {
        int i2;
        if (i == 0) {
            i2 = iArr[0];
        } else if ((i & 255) != i) {
            return 0;
        } else {
            i2 = iArr[i >>> 5] >>> (i & 31);
        }
        return i2 & 1;
    }

    public static boolean gte(int[] iArr, int i, int[] iArr2, int i2) {
        for (int i3 = 7; i3 >= 0; i3--) {
            int i4 = iArr[i + i3] ^ PKIFailureInfo.systemUnavail;
            int i5 = Integer.MIN_VALUE ^ iArr2[i2 + i3];
            if (i4 < i5) {
                return false;
            }
            if (i4 > i5) {
                return true;
            }
        }
        return true;
    }

    public static boolean gte(int[] iArr, int[] iArr2) {
        for (int i = 7; i >= 0; i--) {
            int i2 = iArr[i] ^ PKIFailureInfo.systemUnavail;
            int i3 = Integer.MIN_VALUE ^ iArr2[i];
            if (i2 < i3) {
                return false;
            }
            if (i2 > i3) {
                return true;
            }
        }
        return true;
    }

    public static boolean isOne(int[] iArr) {
        if (iArr[0] != 1) {
            return false;
        }
        for (int i = 1; i < 8; i++) {
            if (iArr[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean isOne64(long[] jArr) {
        if (jArr[0] != 1) {
            return false;
        }
        for (int i = 1; i < 4; i++) {
            if (jArr[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean isZero(int[] iArr) {
        for (int i = 0; i < 8; i++) {
            if (iArr[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public static boolean isZero64(long[] jArr) {
        for (int i = 0; i < 4; i++) {
            if (jArr[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public static void mul(int[] iArr, int i, int[] iArr2, int i2, int[] iArr3, int i3) {
        long j = ((long) iArr2[i2 + 0]) & 4294967295L;
        long j2 = ((long) iArr2[i2 + 1]) & 4294967295L;
        long j3 = ((long) iArr2[i2 + 2]) & 4294967295L;
        long j4 = ((long) iArr2[i2 + 3]) & 4294967295L;
        long j5 = ((long) iArr2[i2 + 4]) & 4294967295L;
        long j6 = ((long) iArr2[i2 + 5]) & 4294967295L;
        long j7 = ((long) iArr2[i2 + 6]) & 4294967295L;
        long j8 = ((long) iArr2[i2 + 7]) & 4294967295L;
        long j9 = ((long) iArr[i + 0]) & 4294967295L;
        long j10 = 0 + (j9 * j);
        long j11 = j;
        iArr3[i3 + 0] = (int) j10;
        long j12 = (j10 >>> 32) + (j9 * j2);
        long j13 = j2;
        iArr3[i3 + 1] = (int) j12;
        long j14 = (j12 >>> 32) + (j9 * j3);
        iArr3[i3 + 2] = (int) j14;
        long j15 = (j14 >>> 32) + (j9 * j4);
        iArr3[i3 + 3] = (int) j15;
        long j16 = (j15 >>> 32) + (j9 * j5);
        iArr3[i3 + 4] = (int) j16;
        long j17 = (j16 >>> 32) + (j9 * j6);
        iArr3[i3 + 5] = (int) j17;
        long j18 = (j17 >>> 32) + (j9 * j7);
        iArr3[i3 + 6] = (int) j18;
        long j19 = (j18 >>> 32) + (j9 * j8);
        iArr3[i3 + 7] = (int) j19;
        iArr3[i3 + 8] = (int) (j19 >>> 32);
        int i4 = 1;
        int i5 = i3;
        int i6 = 1;
        while (i6 < 8) {
            i5 += i4;
            long j20 = ((long) iArr[i + i6]) & 4294967295L;
            int i7 = i5 + 0;
            long j21 = 0 + (j20 * j11) + (((long) iArr3[i7]) & 4294967295L);
            iArr3[i7] = (int) j21;
            int i8 = i5 + 1;
            int i9 = i6;
            long j22 = (j21 >>> 32) + (j20 * j13) + (((long) iArr3[i8]) & 4294967295L);
            iArr3[i8] = (int) j22;
            int i10 = i5 + 2;
            long j23 = (j22 >>> 32) + (j20 * j3) + (((long) iArr3[i10]) & 4294967295L);
            iArr3[i10] = (int) j23;
            int i11 = i5 + 3;
            long j24 = (j23 >>> 32) + (j20 * j4) + (((long) iArr3[i11]) & 4294967295L);
            iArr3[i11] = (int) j24;
            int i12 = i5 + 4;
            long j25 = (j24 >>> 32) + (j20 * j5) + (((long) iArr3[i12]) & 4294967295L);
            iArr3[i12] = (int) j25;
            int i13 = i5 + 5;
            long j26 = (j25 >>> 32) + (j20 * j6) + (((long) iArr3[i13]) & 4294967295L);
            iArr3[i13] = (int) j26;
            int i14 = i5 + 6;
            long j27 = (j26 >>> 32) + (j20 * j7) + (((long) iArr3[i14]) & 4294967295L);
            iArr3[i14] = (int) j27;
            int i15 = i5 + 7;
            long j28 = (j27 >>> 32) + (j20 * j8) + (((long) iArr3[i15]) & 4294967295L);
            iArr3[i15] = (int) j28;
            iArr3[i5 + 8] = (int) (j28 >>> 32);
            i6 = i9 + 1;
            i4 = 1;
        }
    }

    public static void mul(int[] iArr, int[] iArr2, int[] iArr3) {
        long j = ((long) iArr2[2]) & 4294967295L;
        long j2 = ((long) iArr2[3]) & 4294967295L;
        long j3 = ((long) iArr2[1]) & 4294967295L;
        long j4 = ((long) iArr2[4]) & 4294967295L;
        long j5 = ((long) iArr2[5]) & 4294967295L;
        long j6 = ((long) iArr2[6]) & 4294967295L;
        long j7 = ((long) iArr2[0]) & 4294967295L;
        long j8 = ((long) iArr2[7]) & 4294967295L;
        long j9 = ((long) iArr[0]) & 4294967295L;
        long j10 = 0 + (j9 * j7);
        iArr3[0] = (int) j10;
        long j11 = (j10 >>> 32) + (j9 * j3);
        iArr3[1] = (int) j11;
        long j12 = (j11 >>> 32) + (j9 * j);
        iArr3[2] = (int) j12;
        long j13 = (j12 >>> 32) + (j9 * j2);
        iArr3[3] = (int) j13;
        long j14 = (j13 >>> 32) + (j9 * j4);
        iArr3[4] = (int) j14;
        long j15 = (j14 >>> 32) + (j9 * j5);
        iArr3[5] = (int) j15;
        long j16 = (j15 >>> 32) + (j9 * j6);
        iArr3[6] = (int) j16;
        long j17 = (j16 >>> 32) + (j9 * j8);
        iArr3[7] = (int) j17;
        iArr3[8] = (int) (j17 >>> 32);
        int i = 1;
        for (int i2 = 8; i < i2; i2 = 8) {
            long j18 = ((long) iArr[i]) & 4294967295L;
            int i3 = i + 0;
            long j19 = j18;
            long j20 = 0 + (j18 * j7) + (((long) iArr3[i3]) & 4294967295L);
            iArr3[i3] = (int) j20;
            int i4 = i + 1;
            long j21 = (j20 >>> 32) + (j19 * j3) + (((long) iArr3[i4]) & 4294967295L);
            iArr3[i4] = (int) j21;
            int i5 = i + 2;
            long j22 = (j21 >>> 32) + (j19 * j) + (((long) iArr3[i5]) & 4294967295L);
            iArr3[i5] = (int) j22;
            int i6 = i + 3;
            long j23 = (j22 >>> 32) + (j19 * j2) + (((long) iArr3[i6]) & 4294967295L);
            iArr3[i6] = (int) j23;
            int i7 = i + 4;
            long j24 = (j23 >>> 32) + (j19 * j4) + (((long) iArr3[i7]) & 4294967295L);
            iArr3[i7] = (int) j24;
            int i8 = i + 5;
            long j25 = (j24 >>> 32) + (j19 * j5) + (((long) iArr3[i8]) & 4294967295L);
            iArr3[i8] = (int) j25;
            int i9 = i + 6;
            long j26 = (j25 >>> 32) + (j19 * j6) + (((long) iArr3[i9]) & 4294967295L);
            iArr3[i9] = (int) j26;
            int i10 = i + 7;
            long j27 = (j26 >>> 32) + (j19 * j8) + (((long) iArr3[i10]) & 4294967295L);
            iArr3[i10] = (int) j27;
            iArr3[i + 8] = (int) (j27 >>> 32);
            i = i4;
            j = j;
            j2 = j2;
        }
    }

    public static long mul33Add(int i, int[] iArr, int i2, int[] iArr2, int i3, int[] iArr3, int i4) {
        long j = ((long) i) & 4294967295L;
        long j2 = ((long) iArr[i2 + 0]) & 4294967295L;
        long j3 = 0 + (j * j2) + (((long) iArr2[i3 + 0]) & 4294967295L);
        iArr3[i4 + 0] = (int) j3;
        long j4 = ((long) iArr[i2 + 1]) & 4294967295L;
        long j5 = (j3 >>> 32) + (j * j4) + j2 + (((long) iArr2[i3 + 1]) & 4294967295L);
        iArr3[i4 + 1] = (int) j5;
        long j6 = ((long) iArr[i2 + 2]) & 4294967295L;
        long j7 = (j5 >>> 32) + (j * j6) + j4 + (((long) iArr2[i3 + 2]) & 4294967295L);
        iArr3[i4 + 2] = (int) j7;
        long j8 = ((long) iArr[i2 + 3]) & 4294967295L;
        long j9 = (j7 >>> 32) + (j * j8) + j6 + (((long) iArr2[i3 + 3]) & 4294967295L);
        iArr3[i4 + 3] = (int) j9;
        long j10 = ((long) iArr[i2 + 4]) & 4294967295L;
        long j11 = (j9 >>> 32) + (j * j10) + j8 + (((long) iArr2[i3 + 4]) & 4294967295L);
        iArr3[i4 + 4] = (int) j11;
        long j12 = ((long) iArr[i2 + 5]) & 4294967295L;
        long j13 = (j11 >>> 32) + (j * j12) + j10 + (((long) iArr2[i3 + 5]) & 4294967295L);
        iArr3[i4 + 5] = (int) j13;
        long j14 = ((long) iArr[i2 + 6]) & 4294967295L;
        long j15 = (j13 >>> 32) + (j * j14) + j12 + (((long) iArr2[i3 + 6]) & 4294967295L);
        iArr3[i4 + 6] = (int) j15;
        long j16 = ((long) iArr[i2 + 7]) & 4294967295L;
        long j17 = (j15 >>> 32) + (j * j16) + j14 + (((long) iArr2[i3 + 7]) & 4294967295L);
        iArr3[i4 + 7] = (int) j17;
        return (j17 >>> 32) + j16;
    }

    public static int mul33DWordAdd(int i, long j, int[] iArr, int i2) {
        int[] iArr2 = iArr;
        int i3 = i2;
        long j2 = ((long) i) & 4294967295L;
        long j3 = j & 4294967295L;
        int i4 = i3 + 0;
        long j4 = (j2 * j3) + (((long) iArr2[i4]) & 4294967295L) + 0;
        iArr2[i4] = (int) j4;
        long j5 = j >>> 32;
        long j6 = (j2 * j5) + j3;
        int i5 = i3 + 1;
        long j7 = (j4 >>> 32) + j6 + (((long) iArr2[i5]) & 4294967295L);
        iArr2[i5] = (int) j7;
        int i6 = i3 + 2;
        long j8 = (j7 >>> 32) + j5 + (((long) iArr2[i6]) & 4294967295L);
        iArr2[i6] = (int) j8;
        long j9 = j8 >>> 32;
        int i7 = i3 + 3;
        long j10 = j9 + (((long) iArr2[i7]) & 4294967295L);
        iArr2[i7] = (int) j10;
        if ((j10 >>> 32) == 0) {
            return 0;
        }
        return Nat.incAt(8, iArr2, i3, 4);
    }

    public static int mul33WordAdd(int i, int i2, int[] iArr, int i3) {
        long j = ((long) i2) & 4294967295L;
        int i4 = i3 + 0;
        long j2 = ((((long) i) & 4294967295L) * j) + (((long) iArr[i4]) & 4294967295L) + 0;
        iArr[i4] = (int) j2;
        int i5 = i3 + 1;
        long j3 = (j2 >>> 32) + j + (((long) iArr[i5]) & 4294967295L);
        iArr[i5] = (int) j3;
        long j4 = j3 >>> 32;
        int i6 = i3 + 2;
        long j5 = j4 + (((long) iArr[i6]) & 4294967295L);
        iArr[i6] = (int) j5;
        if ((j5 >>> 32) == 0) {
            return 0;
        }
        return Nat.incAt(8, iArr, i3, 3);
    }

    public static int mulAddTo(int[] iArr, int i, int[] iArr2, int i2, int[] iArr3, int i3) {
        long j = ((long) iArr2[i2 + 0]) & 4294967295L;
        long j2 = ((long) iArr2[i2 + 1]) & 4294967295L;
        long j3 = ((long) iArr2[i2 + 2]) & 4294967295L;
        long j4 = ((long) iArr2[i2 + 3]) & 4294967295L;
        long j5 = ((long) iArr2[i2 + 4]) & 4294967295L;
        long j6 = ((long) iArr2[i2 + 5]) & 4294967295L;
        long j7 = ((long) iArr2[i2 + 6]) & 4294967295L;
        long j8 = ((long) iArr2[i2 + 7]) & 4294967295L;
        int i4 = 0;
        int i5 = i3;
        long j9 = 0;
        while (i4 < 8) {
            long j10 = ((long) iArr[i + i4]) & 4294967295L;
            int i6 = i5 + 0;
            long j11 = j;
            long j12 = 0 + (j10 * j) + (((long) iArr3[i6]) & 4294967295L);
            iArr3[i6] = (int) j12;
            int i7 = i5 + 1;
            long j13 = (j12 >>> 32) + (j10 * j2) + (((long) iArr3[i7]) & 4294967295L);
            iArr3[i7] = (int) j13;
            int i8 = i5 + 2;
            long j14 = (j13 >>> 32) + (j10 * j3) + (((long) iArr3[i8]) & 4294967295L);
            iArr3[i8] = (int) j14;
            int i9 = i5 + 3;
            long j15 = (j14 >>> 32) + (j10 * j4) + (((long) iArr3[i9]) & 4294967295L);
            iArr3[i9] = (int) j15;
            int i10 = i5 + 4;
            long j16 = (j15 >>> 32) + (j10 * j5) + (((long) iArr3[i10]) & 4294967295L);
            iArr3[i10] = (int) j16;
            int i11 = i5 + 5;
            long j17 = (j16 >>> 32) + (j10 * j6) + (((long) iArr3[i11]) & 4294967295L);
            iArr3[i11] = (int) j17;
            int i12 = i5 + 6;
            long j18 = (j17 >>> 32) + (j10 * j7) + (((long) iArr3[i12]) & 4294967295L);
            iArr3[i12] = (int) j18;
            int i13 = i5 + 7;
            long j19 = (j18 >>> 32) + (j10 * j8) + (((long) iArr3[i13]) & 4294967295L);
            iArr3[i13] = (int) j19;
            int i14 = i5 + 8;
            long j20 = (j19 >>> 32) + j9 + (((long) iArr3[i14]) & 4294967295L);
            iArr3[i14] = (int) j20;
            j9 = j20 >>> 32;
            i4++;
            i5 = i7;
            j = j11;
            j2 = j2;
            j8 = j8;
        }
        return (int) j9;
    }

    public static int mulAddTo(int[] iArr, int[] iArr2, int[] iArr3) {
        long j = ((long) iArr2[1]) & 4294967295L;
        long j2 = ((long) iArr2[2]) & 4294967295L;
        long j3 = ((long) iArr2[3]) & 4294967295L;
        long j4 = ((long) iArr2[4]) & 4294967295L;
        long j5 = ((long) iArr2[0]) & 4294967295L;
        long j6 = ((long) iArr2[5]) & 4294967295L;
        long j7 = ((long) iArr2[6]) & 4294967295L;
        long j8 = ((long) iArr2[7]) & 4294967295L;
        long j9 = 0;
        int i = 0;
        while (i < 8) {
            long j10 = j8;
            long j11 = ((long) iArr[i]) & 4294967295L;
            int i2 = i + 0;
            long j12 = j4;
            long j13 = 0 + (j11 * j5) + (((long) iArr3[i2]) & 4294967295L);
            iArr3[i2] = (int) j13;
            int i3 = i + 1;
            long j14 = j;
            long j15 = (j13 >>> 32) + (j11 * j) + (((long) iArr3[i3]) & 4294967295L);
            iArr3[i3] = (int) j15;
            int i4 = i + 2;
            long j16 = j2;
            long j17 = (j15 >>> 32) + (j11 * j2) + (((long) iArr3[i4]) & 4294967295L);
            iArr3[i4] = (int) j17;
            int i5 = i + 3;
            long j18 = (j17 >>> 32) + (j11 * j3) + (((long) iArr3[i5]) & 4294967295L);
            iArr3[i5] = (int) j18;
            int i6 = i + 4;
            long j19 = j3;
            long j20 = (j18 >>> 32) + (j11 * j12) + (((long) iArr3[i6]) & 4294967295L);
            iArr3[i6] = (int) j20;
            int i7 = i + 5;
            long j21 = (j20 >>> 32) + (j11 * j6) + (((long) iArr3[i7]) & 4294967295L);
            iArr3[i7] = (int) j21;
            int i8 = i + 6;
            long j22 = (j21 >>> 32) + (j11 * j7) + (((long) iArr3[i8]) & 4294967295L);
            iArr3[i8] = (int) j22;
            int i9 = i + 7;
            long j23 = (j22 >>> 32) + (j11 * j10) + (((long) iArr3[i9]) & 4294967295L);
            iArr3[i9] = (int) j23;
            int i10 = i + 8;
            long j24 = (j23 >>> 32) + j9 + (((long) iArr3[i10]) & 4294967295L);
            iArr3[i10] = (int) j24;
            j9 = j24 >>> 32;
            j8 = j10;
            i = i3;
            j4 = j12;
            j = j14;
            j2 = j16;
            j3 = j19;
        }
        return (int) j9;
    }

    public static int mulByWord(int i, int[] iArr) {
        long j = ((long) i) & 4294967295L;
        long j2 = 0 + ((((long) iArr[0]) & 4294967295L) * j);
        iArr[0] = (int) j2;
        long j3 = (j2 >>> 32) + ((((long) iArr[1]) & 4294967295L) * j);
        iArr[1] = (int) j3;
        long j4 = (j3 >>> 32) + ((((long) iArr[2]) & 4294967295L) * j);
        iArr[2] = (int) j4;
        long j5 = (j4 >>> 32) + ((((long) iArr[3]) & 4294967295L) * j);
        iArr[3] = (int) j5;
        long j6 = (j5 >>> 32) + ((((long) iArr[4]) & 4294967295L) * j);
        iArr[4] = (int) j6;
        long j7 = (j6 >>> 32) + ((((long) iArr[5]) & 4294967295L) * j);
        iArr[5] = (int) j7;
        long j8 = (j7 >>> 32) + ((((long) iArr[6]) & 4294967295L) * j);
        iArr[6] = (int) j8;
        long j9 = (j8 >>> 32) + (j * (4294967295L & ((long) iArr[7])));
        iArr[7] = (int) j9;
        return (int) (j9 >>> 32);
    }

    public static int mulByWordAddTo(int i, int[] iArr, int[] iArr2) {
        long j = ((long) i) & 4294967295L;
        long j2 = 0 + ((((long) iArr2[0]) & 4294967295L) * j) + (((long) iArr[0]) & 4294967295L);
        iArr2[0] = (int) j2;
        long j3 = (j2 >>> 32) + ((((long) iArr2[1]) & 4294967295L) * j) + (((long) iArr[1]) & 4294967295L);
        iArr2[1] = (int) j3;
        long j4 = (j3 >>> 32) + ((((long) iArr2[2]) & 4294967295L) * j) + (((long) iArr[2]) & 4294967295L);
        iArr2[2] = (int) j4;
        long j5 = (j4 >>> 32) + ((((long) iArr2[3]) & 4294967295L) * j) + (((long) iArr[3]) & 4294967295L);
        iArr2[3] = (int) j5;
        long j6 = (j5 >>> 32) + ((((long) iArr2[4]) & 4294967295L) * j) + (((long) iArr[4]) & 4294967295L);
        iArr2[4] = (int) j6;
        long j7 = (j6 >>> 32) + ((((long) iArr2[5]) & 4294967295L) * j) + (((long) iArr[5]) & 4294967295L);
        iArr2[5] = (int) j7;
        long j8 = (j7 >>> 32) + ((((long) iArr2[6]) & 4294967295L) * j) + (((long) iArr[6]) & 4294967295L);
        iArr2[6] = (int) j8;
        long j9 = (j8 >>> 32) + (j * (((long) iArr2[7]) & 4294967295L)) + (4294967295L & ((long) iArr[7]));
        iArr2[7] = (int) j9;
        return (int) (j9 >>> 32);
    }

    public static int mulWord(int i, int[] iArr, int[] iArr2, int i2) {
        long j = ((long) i) & 4294967295L;
        long j2 = 0;
        int i3 = 0;
        do {
            long j3 = j2 + ((((long) iArr[i3]) & 4294967295L) * j);
            iArr2[i2 + i3] = (int) j3;
            j2 = j3 >>> 32;
            i3++;
        } while (i3 < 8);
        return (int) j2;
    }

    public static int mulWordAddTo(int i, int[] iArr, int i2, int[] iArr2, int i3) {
        long j = ((long) i) & 4294967295L;
        int i4 = i3 + 0;
        long j2 = 0 + ((((long) iArr[i2 + 0]) & 4294967295L) * j) + (((long) iArr2[i4]) & 4294967295L);
        iArr2[i4] = (int) j2;
        int i5 = i3 + 1;
        long j3 = (j2 >>> 32) + ((((long) iArr[i2 + 1]) & 4294967295L) * j) + (((long) iArr2[i5]) & 4294967295L);
        iArr2[i5] = (int) j3;
        int i6 = i3 + 2;
        long j4 = (j3 >>> 32) + ((((long) iArr[i2 + 2]) & 4294967295L) * j) + (((long) iArr2[i6]) & 4294967295L);
        iArr2[i6] = (int) j4;
        int i7 = i3 + 3;
        long j5 = (j4 >>> 32) + ((((long) iArr[i2 + 3]) & 4294967295L) * j) + (((long) iArr2[i7]) & 4294967295L);
        iArr2[i7] = (int) j5;
        int i8 = i3 + 4;
        long j6 = (j5 >>> 32) + ((((long) iArr[i2 + 4]) & 4294967295L) * j) + (((long) iArr2[i8]) & 4294967295L);
        iArr2[i8] = (int) j6;
        int i9 = i3 + 5;
        long j7 = (j6 >>> 32) + ((((long) iArr[i2 + 5]) & 4294967295L) * j) + (((long) iArr2[i9]) & 4294967295L);
        iArr2[i9] = (int) j7;
        int i10 = i3 + 6;
        long j8 = (j7 >>> 32) + ((((long) iArr[i2 + 6]) & 4294967295L) * j) + (((long) iArr2[i10]) & 4294967295L);
        iArr2[i10] = (int) j8;
        int i11 = i3 + 7;
        long j9 = (j8 >>> 32) + (j * (((long) iArr[i2 + 7]) & 4294967295L)) + (((long) iArr2[i11]) & 4294967295L);
        iArr2[i11] = (int) j9;
        return (int) (j9 >>> 32);
    }

    public static int mulWordDwordAdd(int i, long j, int[] iArr, int i2) {
        long j2 = ((long) i) & 4294967295L;
        int i3 = i2 + 0;
        long j3 = ((j & 4294967295L) * j2) + (((long) iArr[i3]) & 4294967295L) + 0;
        iArr[i3] = (int) j3;
        long j4 = j2 * (j >>> 32);
        int i4 = i2 + 1;
        long j5 = (j3 >>> 32) + j4 + (((long) iArr[i4]) & 4294967295L);
        iArr[i4] = (int) j5;
        int i5 = i2 + 2;
        long j6 = (j5 >>> 32) + (((long) iArr[i5]) & 4294967295L);
        iArr[i5] = (int) j6;
        if ((j6 >>> 32) == 0) {
            return 0;
        }
        return Nat.incAt(8, iArr, i2, 3);
    }

    public static void square(int[] iArr, int i, int[] iArr2, int i2) {
        long j = 4294967295L;
        long j2 = ((long) iArr[i + 0]) & 4294967295L;
        int i3 = 16;
        int i4 = 0;
        int i5 = 7;
        while (true) {
            int i6 = i5 - 1;
            long j3 = ((long) iArr[i + i5]) & j;
            long j4 = j3 * j3;
            int i7 = i3 - 1;
            iArr2[i2 + i7] = ((int) (j4 >>> 33)) | (i4 << 31);
            i3 = i7 - 1;
            iArr2[i2 + i3] = (int) (j4 >>> 1);
            i4 = (int) j4;
            if (i6 <= 0) {
                long j5 = j2 * j2;
                iArr2[i2 + 0] = (int) j5;
                long j6 = ((long) iArr[i + 1]) & 4294967295L;
                int i8 = i2 + 2;
                long j7 = ((((long) (i4 << 31)) & 4294967295L) | (j5 >>> 33)) + (j6 * j2);
                int i9 = (int) j7;
                iArr2[i2 + 1] = (i9 << 1) | (((int) (j5 >>> 32)) & 1);
                long j8 = (((long) iArr2[i8]) & 4294967295L) + (j7 >>> 32);
                long j9 = ((long) iArr[i + 2]) & 4294967295L;
                int i10 = i2 + 3;
                int i11 = i2 + 4;
                long j10 = ((long) iArr2[i10]) & 4294967295L;
                long j11 = j8 + (j9 * j2);
                int i12 = (int) j11;
                iArr2[i8] = (i9 >>> 31) | (i12 << 1);
                long j12 = j10 + (j11 >>> 32) + (j9 * j6);
                long j13 = (((long) iArr2[i11]) & 4294967295L) + (j12 >>> 32);
                long j14 = ((long) iArr[i + 3]) & 4294967295L;
                int i13 = i2 + 5;
                long j15 = j9;
                long j16 = (((long) iArr2[i13]) & 4294967295L) + (j13 >>> 32);
                int i14 = i2 + 6;
                long j17 = (j12 & 4294967295L) + (j14 * j2);
                int i15 = (int) j17;
                iArr2[i10] = (i12 >>> 31) | (i15 << 1);
                int i16 = i15 >>> 31;
                long j18 = (j13 & 4294967295L) + (j17 >>> 32) + (j14 * j6);
                long j19 = (j16 & 4294967295L) + (j18 >>> 32) + (j14 * j15);
                long j20 = (((long) iArr2[i14]) & 4294967295L) + (j16 >>> 32) + (j19 >>> 32);
                long j21 = j14;
                long j22 = ((long) iArr[i + 4]) & 4294967295L;
                int i17 = i2 + 7;
                long j23 = j19 & 4294967295L;
                long j24 = (((long) iArr2[i17]) & 4294967295L) + (j20 >>> 32);
                int i18 = i2 + 8;
                long j25 = (j18 & 4294967295L) + (j22 * j2);
                int i19 = (int) j25;
                iArr2[i11] = i16 | (i19 << 1);
                int i20 = i19 >>> 31;
                long j26 = j23 + (j25 >>> 32) + (j22 * j6);
                long j27 = (j20 & 4294967295L) + (j26 >>> 32) + (j22 * j15);
                long j28 = (j24 & 4294967295L) + (j27 >>> 32) + (j22 * j21);
                long j29 = (((long) iArr2[i18]) & 4294967295L) + (j24 >>> 32) + (j28 >>> 32);
                long j30 = j22;
                long j31 = ((long) iArr[i + 5]) & 4294967295L;
                int i21 = i2 + 9;
                long j32 = j28 & 4294967295L;
                long j33 = (((long) iArr2[i21]) & 4294967295L) + (j29 >>> 32);
                int i22 = i2 + 10;
                long j34 = j29 & 4294967295L;
                long j35 = (((long) iArr2[i22]) & 4294967295L) + (j33 >>> 32);
                long j36 = (j26 & 4294967295L) + (j31 * j2);
                long j37 = j2;
                int i23 = (int) j36;
                iArr2[i13] = i20 | (i23 << 1);
                int i24 = i23 >>> 31;
                long j38 = (j27 & 4294967295L) + (j36 >>> 32) + (j31 * j6);
                long j39 = j32 + (j38 >>> 32) + (j31 * j15);
                long j40 = j34 + (j39 >>> 32) + (j31 * j21);
                long j41 = (j33 & 4294967295L) + (j40 >>> 32) + (j31 * j30);
                long j42 = j35 + (j41 >>> 32);
                long j43 = j31;
                long j44 = ((long) iArr[i + 6]) & 4294967295L;
                int i25 = i2 + 11;
                long j45 = j41 & 4294967295L;
                long j46 = (((long) iArr2[i25]) & 4294967295L) + (j42 >>> 32);
                int i26 = i2 + 12;
                long j47 = j42 & 4294967295L;
                long j48 = (((long) iArr2[i26]) & 4294967295L) + (j46 >>> 32);
                long j49 = (j38 & 4294967295L) + (j44 * j37);
                int i27 = (int) j49;
                iArr2[i14] = i24 | (i27 << 1);
                int i28 = i27 >>> 31;
                long j50 = (j39 & 4294967295L) + (j49 >>> 32) + (j44 * j6);
                long j51 = (j40 & 4294967295L) + (j50 >>> 32) + (j44 * j15);
                long j52 = j45 + (j51 >>> 32) + (j44 * j21);
                long j53 = j51 & 4294967295L;
                long j54 = j47 + (j52 >>> 32) + (j44 * j30);
                long j55 = (j46 & 4294967295L) + (j54 >>> 32) + (j44 * j43);
                long j56 = j48 + (j55 >>> 32);
                long j57 = j44;
                long j58 = ((long) iArr[i + 7]) & 4294967295L;
                int i29 = i2 + 13;
                long j59 = j55 & 4294967295L;
                long j60 = (((long) iArr2[i29]) & 4294967295L) + (j56 >>> 32);
                int i30 = i2 + 14;
                long j61 = 4294967295L & j60;
                long j62 = (j50 & 4294967295L) + (j58 * j37);
                int i31 = (int) j62;
                iArr2[i17] = i28 | (i31 << 1);
                int i32 = i31 >>> 31;
                long j63 = (j62 >>> 32) + (j6 * j58) + j53;
                long j64 = (j52 & 4294967295L) + (j63 >>> 32) + (j58 * j15);
                long j65 = (j54 & 4294967295L) + (j64 >>> 32) + (j58 * j21);
                long j66 = j59 + (j65 >>> 32) + (j58 * j30);
                long j67 = j66;
                long j68 = (j56 & 4294967295L) + (j66 >>> 32) + (j58 * j43);
                long j69 = j61 + (j68 >>> 32) + (j58 * j57);
                long j70 = (((long) iArr2[i30]) & 4294967295L) + (j60 >>> 32) + (j69 >>> 32);
                int i33 = (int) j63;
                iArr2[i18] = i32 | (i33 << 1);
                int i34 = i33 >>> 31;
                int i35 = (int) j64;
                iArr2[i21] = i34 | (i35 << 1);
                int i36 = i35 >>> 31;
                int i37 = (int) j65;
                iArr2[i22] = i36 | (i37 << 1);
                int i38 = i37 >>> 31;
                int i39 = (int) j67;
                iArr2[i25] = i38 | (i39 << 1);
                int i40 = i39 >>> 31;
                int i41 = (int) j68;
                iArr2[i26] = i40 | (i41 << 1);
                int i42 = i41 >>> 31;
                int i43 = (int) j69;
                iArr2[i29] = i42 | (i43 << 1);
                int i44 = i43 >>> 31;
                int i45 = (int) j70;
                iArr2[i30] = i44 | (i45 << 1);
                int i46 = i45 >>> 31;
                int i47 = i2 + 15;
                iArr2[i47] = ((iArr2[i47] + ((int) (j70 >>> 32))) << 1) | i46;
                return;
            }
            i5 = i6;
            j = 4294967295L;
        }
    }

    public static void square(int[] iArr, int[] iArr2) {
        long j = ((long) iArr[0]) & 4294967295L;
        int i = 0;
        int i2 = 16;
        int i3 = 7;
        while (true) {
            int i4 = i3 - 1;
            long j2 = ((long) iArr[i3]) & 4294967295L;
            long j3 = j2 * j2;
            int i5 = i2 - 1;
            iArr2[i5] = (i << 31) | ((int) (j3 >>> 33));
            i2 = i5 - 1;
            iArr2[i2] = (int) (j3 >>> 1);
            int i6 = (int) j3;
            if (i4 <= 0) {
                long j4 = j * j;
                long j5 = (j4 >>> 33) | (((long) (i6 << 31)) & 4294967295L);
                iArr2[0] = (int) j4;
                long j6 = ((long) iArr[1]) & 4294967295L;
                long j7 = j5 + (j6 * j);
                int i7 = (int) j7;
                iArr2[1] = (i7 << 1) | (((int) (j4 >>> 32)) & 1);
                long j8 = (((long) iArr2[2]) & 4294967295L) + (j7 >>> 32);
                long j9 = ((long) iArr[2]) & 4294967295L;
                long j10 = j6;
                long j11 = ((long) iArr2[3]) & 4294967295L;
                long j12 = j8 + (j9 * j);
                int i8 = (int) j12;
                iArr2[2] = (i8 << 1) | (i7 >>> 31);
                int i9 = i8 >>> 31;
                long j13 = j11 + (j12 >>> 32) + (j9 * j10);
                long j14 = (((long) iArr2[4]) & 4294967295L) + (j13 >>> 32);
                long j15 = j;
                long j16 = ((long) iArr[3]) & 4294967295L;
                long j17 = j9;
                long j18 = (((long) iArr2[5]) & 4294967295L) + (j14 >>> 32);
                long j19 = j14 & 4294967295L;
                long j20 = (((long) iArr2[6]) & 4294967295L) + (j18 >>> 32);
                long j21 = (j13 & 4294967295L) + (j16 * j15);
                int i10 = (int) j21;
                iArr2[3] = i9 | (i10 << 1);
                long j22 = j19 + (j21 >>> 32) + (j16 * j10);
                long j23 = (j18 & 4294967295L) + (j22 >>> 32) + (j16 * j17);
                long j24 = j20 + (j23 >>> 32);
                long j25 = ((long) iArr[4]) & 4294967295L;
                long j26 = (((long) iArr2[7]) & 4294967295L) + (j24 >>> 32);
                long j27 = j16;
                long j28 = (((long) iArr2[8]) & 4294967295L) + (j26 >>> 32);
                long j29 = (j22 & 4294967295L) + (j25 * j15);
                int i11 = (int) j29;
                iArr2[4] = (i10 >>> 31) | (i11 << 1);
                int i12 = i11 >>> 31;
                long j30 = (j23 & 4294967295L) + (j29 >>> 32) + (j25 * j10);
                long j31 = (j24 & 4294967295L) + (j30 >>> 32) + (j25 * j17);
                long j32 = (j26 & 4294967295L) + (j31 >>> 32) + (j25 * j27);
                long j33 = j28 + (j32 >>> 32);
                long j34 = j25;
                long j35 = ((long) iArr[5]) & 4294967295L;
                long j36 = j32 & 4294967295L;
                long j37 = (((long) iArr2[9]) & 4294967295L) + (j33 >>> 32);
                long j38 = j33 & 4294967295L;
                long j39 = (((long) iArr2[10]) & 4294967295L) + (j37 >>> 32);
                long j40 = (j30 & 4294967295L) + (j35 * j15);
                int i13 = (int) j40;
                iArr2[5] = i12 | (i13 << 1);
                int i14 = i13 >>> 31;
                long j41 = (j31 & 4294967295L) + (j40 >>> 32) + (j35 * j10);
                long j42 = j36 + (j41 >>> 32) + (j35 * j17);
                long j43 = j38 + (j42 >>> 32) + (j35 * j27);
                long j44 = (j37 & 4294967295L) + (j43 >>> 32) + (j35 * j34);
                long j45 = j39 + (j44 >>> 32);
                long j46 = j35;
                long j47 = ((long) iArr[6]) & 4294967295L;
                long j48 = j44 & 4294967295L;
                long j49 = (((long) iArr2[11]) & 4294967295L) + (j45 >>> 32);
                long j50 = j45 & 4294967295L;
                long j51 = (((long) iArr2[12]) & 4294967295L) + (j49 >>> 32);
                long j52 = (j41 & 4294967295L) + (j47 * j15);
                int i15 = (int) j52;
                iArr2[6] = i14 | (i15 << 1);
                int i16 = i15 >>> 31;
                long j53 = (j42 & 4294967295L) + (j52 >>> 32) + (j47 * j10);
                long j54 = (j43 & 4294967295L) + (j53 >>> 32) + (j47 * j17);
                long j55 = j48 + (j54 >>> 32) + (j47 * j27);
                long j56 = j50 + (j55 >>> 32) + (j47 * j34);
                long j57 = (j49 & 4294967295L) + (j56 >>> 32) + (j47 * j46);
                long j58 = j51 + (j57 >>> 32);
                long j59 = j47;
                long j60 = ((long) iArr[7]) & 4294967295L;
                long j61 = j57 & 4294967295L;
                long j62 = (((long) iArr2[13]) & 4294967295L) + (j58 >>> 32);
                long j63 = 4294967295L & j62;
                long j64 = (j53 & 4294967295L) + (j60 * j15);
                int i17 = (int) j64;
                iArr2[7] = i16 | (i17 << 1);
                int i18 = i17 >>> 31;
                long j65 = (j64 >>> 32) + (j60 * j10) + (j54 & 4294967295L);
                long j66 = (j55 & 4294967295L) + (j65 >>> 32) + (j60 * j17);
                long j67 = (j56 & 4294967295L) + (j66 >>> 32) + (j60 * j27);
                long j68 = j67;
                long j69 = j61 + (j67 >>> 32) + (j60 * j34);
                long j70 = j69;
                long j71 = (j58 & 4294967295L) + (j69 >>> 32) + (j60 * j46);
                long j72 = j63 + (j71 >>> 32) + (j60 * j59);
                long j73 = (((long) iArr2[14]) & 4294967295L) + (j62 >>> 32) + (j72 >>> 32);
                int i19 = (int) j65;
                iArr2[8] = i18 | (i19 << 1);
                int i20 = (int) j66;
                iArr2[9] = (i19 >>> 31) | (i20 << 1);
                int i21 = i20 >>> 31;
                int i22 = (int) j68;
                iArr2[10] = i21 | (i22 << 1);
                int i23 = i22 >>> 31;
                int i24 = (int) j70;
                iArr2[11] = i23 | (i24 << 1);
                int i25 = i24 >>> 31;
                int i26 = (int) j71;
                iArr2[12] = i25 | (i26 << 1);
                int i27 = i26 >>> 31;
                int i28 = (int) j72;
                iArr2[13] = i27 | (i28 << 1);
                int i29 = i28 >>> 31;
                int i30 = (int) j73;
                iArr2[14] = i29 | (i30 << 1);
                iArr2[15] = (i30 >>> 31) | ((iArr2[15] + ((int) (j73 >>> 32))) << 1);
                return;
            }
            i3 = i4;
            i = i6;
        }
    }

    public static int sub(int[] iArr, int i, int[] iArr2, int i2, int[] iArr3, int i3) {
        long j = 0 + ((((long) iArr[i + 0]) & 4294967295L) - (((long) iArr2[i2 + 0]) & 4294967295L));
        iArr3[i3 + 0] = (int) j;
        long j2 = (j >> 32) + ((((long) iArr[i + 1]) & 4294967295L) - (((long) iArr2[i2 + 1]) & 4294967295L));
        iArr3[i3 + 1] = (int) j2;
        long j3 = (j2 >> 32) + ((((long) iArr[i + 2]) & 4294967295L) - (((long) iArr2[i2 + 2]) & 4294967295L));
        iArr3[i3 + 2] = (int) j3;
        long j4 = (j3 >> 32) + ((((long) iArr[i + 3]) & 4294967295L) - (((long) iArr2[i2 + 3]) & 4294967295L));
        iArr3[i3 + 3] = (int) j4;
        long j5 = (j4 >> 32) + ((((long) iArr[i + 4]) & 4294967295L) - (((long) iArr2[i2 + 4]) & 4294967295L));
        iArr3[i3 + 4] = (int) j5;
        long j6 = (j5 >> 32) + ((((long) iArr[i + 5]) & 4294967295L) - (((long) iArr2[i2 + 5]) & 4294967295L));
        iArr3[i3 + 5] = (int) j6;
        long j7 = (j6 >> 32) + ((((long) iArr[i + 6]) & 4294967295L) - (((long) iArr2[i2 + 6]) & 4294967295L));
        iArr3[i3 + 6] = (int) j7;
        long j8 = (j7 >> 32) + ((((long) iArr[i + 7]) & 4294967295L) - (((long) iArr2[i2 + 7]) & 4294967295L));
        iArr3[i3 + 7] = (int) j8;
        return (int) (j8 >> 32);
    }

    public static int sub(int[] iArr, int[] iArr2, int[] iArr3) {
        long j = 0 + ((((long) iArr[0]) & 4294967295L) - (((long) iArr2[0]) & 4294967295L));
        iArr3[0] = (int) j;
        long j2 = (j >> 32) + ((((long) iArr[1]) & 4294967295L) - (((long) iArr2[1]) & 4294967295L));
        iArr3[1] = (int) j2;
        long j3 = (j2 >> 32) + ((((long) iArr[2]) & 4294967295L) - (((long) iArr2[2]) & 4294967295L));
        iArr3[2] = (int) j3;
        long j4 = (j3 >> 32) + ((((long) iArr[3]) & 4294967295L) - (((long) iArr2[3]) & 4294967295L));
        iArr3[3] = (int) j4;
        long j5 = (j4 >> 32) + ((((long) iArr[4]) & 4294967295L) - (((long) iArr2[4]) & 4294967295L));
        iArr3[4] = (int) j5;
        long j6 = (j5 >> 32) + ((((long) iArr[5]) & 4294967295L) - (((long) iArr2[5]) & 4294967295L));
        iArr3[5] = (int) j6;
        long j7 = (j6 >> 32) + ((((long) iArr[6]) & 4294967295L) - (((long) iArr2[6]) & 4294967295L));
        iArr3[6] = (int) j7;
        long j8 = (j7 >> 32) + ((((long) iArr[7]) & 4294967295L) - (((long) iArr2[7]) & 4294967295L));
        iArr3[7] = (int) j8;
        return (int) (j8 >> 32);
    }

    public static int subBothFrom(int[] iArr, int[] iArr2, int[] iArr3) {
        long j = 0 + (((((long) iArr3[0]) & 4294967295L) - (((long) iArr[0]) & 4294967295L)) - (((long) iArr2[0]) & 4294967295L));
        iArr3[0] = (int) j;
        long j2 = (j >> 32) + (((((long) iArr3[1]) & 4294967295L) - (((long) iArr[1]) & 4294967295L)) - (((long) iArr2[1]) & 4294967295L));
        iArr3[1] = (int) j2;
        long j3 = (j2 >> 32) + (((((long) iArr3[2]) & 4294967295L) - (((long) iArr[2]) & 4294967295L)) - (((long) iArr2[2]) & 4294967295L));
        iArr3[2] = (int) j3;
        long j4 = (j3 >> 32) + (((((long) iArr3[3]) & 4294967295L) - (((long) iArr[3]) & 4294967295L)) - (((long) iArr2[3]) & 4294967295L));
        iArr3[3] = (int) j4;
        long j5 = (j4 >> 32) + (((((long) iArr3[4]) & 4294967295L) - (((long) iArr[4]) & 4294967295L)) - (((long) iArr2[4]) & 4294967295L));
        iArr3[4] = (int) j5;
        long j6 = (j5 >> 32) + (((((long) iArr3[5]) & 4294967295L) - (((long) iArr[5]) & 4294967295L)) - (((long) iArr2[5]) & 4294967295L));
        iArr3[5] = (int) j6;
        long j7 = (j6 >> 32) + (((((long) iArr3[6]) & 4294967295L) - (((long) iArr[6]) & 4294967295L)) - (((long) iArr2[6]) & 4294967295L));
        iArr3[6] = (int) j7;
        long j8 = (j7 >> 32) + (((((long) iArr3[7]) & 4294967295L) - (((long) iArr[7]) & 4294967295L)) - (((long) iArr2[7]) & 4294967295L));
        iArr3[7] = (int) j8;
        return (int) (j8 >> 32);
    }

    public static int subFrom(int[] iArr, int i, int[] iArr2, int i2) {
        int i3 = i2 + 0;
        long j = 0 + ((((long) iArr2[i3]) & 4294967295L) - (((long) iArr[i + 0]) & 4294967295L));
        iArr2[i3] = (int) j;
        long j2 = j >> 32;
        int i4 = i2 + 1;
        long j3 = j2 + ((((long) iArr2[i4]) & 4294967295L) - (((long) iArr[i + 1]) & 4294967295L));
        iArr2[i4] = (int) j3;
        int i5 = i2 + 2;
        long j4 = (j3 >> 32) + ((((long) iArr2[i5]) & 4294967295L) - (((long) iArr[i + 2]) & 4294967295L));
        iArr2[i5] = (int) j4;
        int i6 = i2 + 3;
        long j5 = (j4 >> 32) + ((((long) iArr2[i6]) & 4294967295L) - (((long) iArr[i + 3]) & 4294967295L));
        iArr2[i6] = (int) j5;
        int i7 = i2 + 4;
        long j6 = (j5 >> 32) + ((((long) iArr2[i7]) & 4294967295L) - (((long) iArr[i + 4]) & 4294967295L));
        iArr2[i7] = (int) j6;
        int i8 = i2 + 5;
        long j7 = (j6 >> 32) + ((((long) iArr2[i8]) & 4294967295L) - (((long) iArr[i + 5]) & 4294967295L));
        iArr2[i8] = (int) j7;
        int i9 = i2 + 6;
        long j8 = (j7 >> 32) + ((((long) iArr2[i9]) & 4294967295L) - (((long) iArr[i + 6]) & 4294967295L));
        iArr2[i9] = (int) j8;
        int i10 = i2 + 7;
        long j9 = (j8 >> 32) + ((((long) iArr2[i10]) & 4294967295L) - (((long) iArr[i + 7]) & 4294967295L));
        iArr2[i10] = (int) j9;
        return (int) (j9 >> 32);
    }

    public static int subFrom(int[] iArr, int[] iArr2) {
        long j = 0 + ((((long) iArr2[0]) & 4294967295L) - (((long) iArr[0]) & 4294967295L));
        iArr2[0] = (int) j;
        long j2 = (j >> 32) + ((((long) iArr2[1]) & 4294967295L) - (((long) iArr[1]) & 4294967295L));
        iArr2[1] = (int) j2;
        long j3 = (j2 >> 32) + ((((long) iArr2[2]) & 4294967295L) - (((long) iArr[2]) & 4294967295L));
        iArr2[2] = (int) j3;
        long j4 = (j3 >> 32) + ((((long) iArr2[3]) & 4294967295L) - (((long) iArr[3]) & 4294967295L));
        iArr2[3] = (int) j4;
        long j5 = (j4 >> 32) + ((((long) iArr2[4]) & 4294967295L) - (((long) iArr[4]) & 4294967295L));
        iArr2[4] = (int) j5;
        long j6 = (j5 >> 32) + ((((long) iArr2[5]) & 4294967295L) - (((long) iArr[5]) & 4294967295L));
        iArr2[5] = (int) j6;
        long j7 = (j6 >> 32) + ((((long) iArr2[6]) & 4294967295L) - (((long) iArr[6]) & 4294967295L));
        iArr2[6] = (int) j7;
        long j8 = (j7 >> 32) + ((((long) iArr2[7]) & 4294967295L) - (4294967295L & ((long) iArr[7])));
        iArr2[7] = (int) j8;
        return (int) (j8 >> 32);
    }

    public static BigInteger toBigInteger(int[] iArr) {
        byte[] bArr = new byte[32];
        for (int i = 0; i < 8; i++) {
            int i2 = iArr[i];
            if (i2 != 0) {
                Pack.intToBigEndian(i2, bArr, (7 - i) << 2);
            }
        }
        return new BigInteger(1, bArr);
    }

    public static BigInteger toBigInteger64(long[] jArr) {
        byte[] bArr = new byte[32];
        for (int i = 0; i < 4; i++) {
            long j = jArr[i];
            if (j != 0) {
                Pack.longToBigEndian(j, bArr, (3 - i) << 3);
            }
        }
        return new BigInteger(1, bArr);
    }

    public static void zero(int[] iArr) {
        iArr[0] = 0;
        iArr[1] = 0;
        iArr[2] = 0;
        iArr[3] = 0;
        iArr[4] = 0;
        iArr[5] = 0;
        iArr[6] = 0;
        iArr[7] = 0;
    }
}
