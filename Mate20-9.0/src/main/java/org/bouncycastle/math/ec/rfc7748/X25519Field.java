package org.bouncycastle.math.ec.rfc7748;

public abstract class X25519Field {
    private static final int M24 = 16777215;
    private static final int M25 = 33554431;
    private static final int M26 = 67108863;
    private static final int[] ROOT_NEG_ONE = {34513072, 59165138, 4688974, 3500415, 6194736, 33281959, 54535759, 32551604, 163342, 5703241};
    public static final int SIZE = 10;

    private X25519Field() {
    }

    public static void add(int[] iArr, int[] iArr2, int[] iArr3) {
        for (int i = 0; i < 10; i++) {
            iArr3[i] = iArr[i] + iArr2[i];
        }
    }

    public static void addOne(int[] iArr) {
        iArr[0] = iArr[0] + 1;
    }

    public static void addOne(int[] iArr, int i) {
        iArr[i] = iArr[i] + 1;
    }

    public static void apm(int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4) {
        for (int i = 0; i < 10; i++) {
            int i2 = iArr[i];
            int i3 = iArr2[i];
            iArr3[i] = i2 + i3;
            iArr4[i] = i2 - i3;
        }
    }

    public static void carry(int[] iArr) {
        int i = iArr[0];
        int i2 = iArr[1];
        int i3 = iArr[2];
        int i4 = iArr[3];
        int i5 = iArr[4];
        int i6 = iArr[5];
        int i7 = iArr[6];
        int i8 = iArr[7];
        int i9 = iArr[8];
        int i10 = iArr[9];
        int i11 = i4 + (i3 >> 25);
        int i12 = i3 & M25;
        int i13 = i6 + (i5 >> 25);
        int i14 = i5 & M25;
        int i15 = i9 + (i8 >> 25);
        int i16 = i8 & M25;
        int i17 = i + ((i10 >> 25) * 38);
        int i18 = i10 & M25;
        int i19 = i2 + (i17 >> 26);
        int i20 = i17 & M26;
        int i21 = i7 + (i13 >> 26);
        int i22 = i13 & M26;
        int i23 = i12 + (i19 >> 26);
        int i24 = i19 & M26;
        int i25 = i14 + (i11 >> 26);
        int i26 = i11 & M26;
        int i27 = i16 + (i21 >> 26);
        int i28 = i21 & M26;
        int i29 = i15 & M26;
        iArr[0] = i20;
        iArr[1] = i24;
        iArr[2] = i23;
        iArr[3] = i26;
        iArr[4] = i25;
        iArr[5] = i22;
        iArr[6] = i28;
        iArr[7] = i27;
        iArr[8] = i29;
        iArr[9] = i18 + (i15 >> 26);
    }

    public static void cnegate(int i, int[] iArr) {
        int i2 = 0 - i;
        for (int i3 = 0; i3 < 10; i3++) {
            iArr[i3] = (iArr[i3] ^ i2) - i2;
        }
    }

    public static void copy(int[] iArr, int i, int[] iArr2, int i2) {
        for (int i3 = 0; i3 < 10; i3++) {
            iArr2[i2 + i3] = iArr[i + i3];
        }
    }

    public static int[] create() {
        return new int[10];
    }

    public static int[] createTable(int i) {
        return new int[(10 * i)];
    }

    public static void cswap(int i, int[] iArr, int[] iArr2) {
        int i2 = 0 - i;
        for (int i3 = 0; i3 < 10; i3++) {
            int i4 = iArr[i3];
            int i5 = iArr2[i3];
            int i6 = (i4 ^ i5) & i2;
            iArr[i3] = i4 ^ i6;
            iArr2[i3] = i5 ^ i6;
        }
    }

    public static void decode(byte[] bArr, int i, int[] iArr) {
        decode128(bArr, i, iArr, 0);
        decode128(bArr, i + 16, iArr, 5);
        iArr[9] = iArr[9] & M24;
    }

    private static void decode128(byte[] bArr, int i, int[] iArr, int i2) {
        int decode32 = decode32(bArr, i + 0);
        int decode322 = decode32(bArr, i + 4);
        int decode323 = decode32(bArr, i + 8);
        int decode324 = decode32(bArr, i + 12);
        iArr[i2 + 0] = decode32 & M26;
        iArr[i2 + 1] = ((decode32 >>> 26) | (decode322 << 6)) & M26;
        iArr[i2 + 2] = ((decode323 << 12) | (decode322 >>> 20)) & M25;
        iArr[i2 + 3] = ((decode324 << 19) | (decode323 >>> 13)) & M26;
        iArr[i2 + 4] = decode324 >>> 7;
    }

    private static int decode32(byte[] bArr, int i) {
        int i2 = i + 1;
        int i3 = i2 + 1;
        return (bArr[i3 + 1] << 24) | (bArr[i] & 255) | ((bArr[i2] & 255) << 8) | ((bArr[i3] & 255) << Tnaf.POW_2_WIDTH);
    }

    public static void encode(int[] iArr, byte[] bArr, int i) {
        encode128(iArr, 0, bArr, i);
        encode128(iArr, 5, bArr, i + 16);
    }

    private static void encode128(int[] iArr, int i, byte[] bArr, int i2) {
        int i3 = iArr[i + 0];
        int i4 = iArr[i + 1];
        int i5 = iArr[i + 2];
        int i6 = iArr[i + 3];
        int i7 = iArr[i + 4];
        encode32((i4 << 26) | i3, bArr, i2 + 0);
        encode32((i4 >>> 6) | (i5 << 20), bArr, i2 + 4);
        encode32((i5 >>> 12) | (i6 << 13), bArr, i2 + 8);
        encode32((i7 << 7) | (i6 >>> 19), bArr, i2 + 12);
    }

    private static void encode32(int i, byte[] bArr, int i2) {
        bArr[i2] = (byte) i;
        int i3 = i2 + 1;
        bArr[i3] = (byte) (i >>> 8);
        int i4 = i3 + 1;
        bArr[i4] = (byte) (i >>> 16);
        bArr[i4 + 1] = (byte) (i >>> 24);
    }

    public static void inv(int[] iArr, int[] iArr2) {
        int[] create = create();
        int[] create2 = create();
        powPm5d8(iArr, create, create2);
        sqr(create2, 3, create2);
        mul(create2, create, iArr2);
    }

    public static boolean isZeroVar(int[] iArr) {
        int i = 0;
        for (int i2 = 0; i2 < 10; i2++) {
            i |= iArr[i2];
        }
        return i == 0;
    }

    public static void mul(int[] iArr, int i, int[] iArr2) {
        int i2 = iArr[0];
        int i3 = iArr[1];
        int i4 = iArr[2];
        int i5 = iArr[3];
        int i6 = iArr[4];
        int i7 = iArr[5];
        int i8 = iArr[6];
        int i9 = iArr[7];
        int i10 = iArr[8];
        int i11 = iArr[9];
        long j = (long) i;
        long j2 = ((long) i4) * j;
        int i12 = ((int) j2) & M25;
        long j3 = ((long) i6) * j;
        int i13 = ((int) j3) & M25;
        long j4 = ((long) i9) * j;
        int i14 = ((int) j4) & M25;
        long j5 = ((long) i11) * j;
        int i15 = ((int) j5) & M25;
        long j6 = ((j5 >> 25) * 38) + (((long) i2) * j);
        iArr2[0] = ((int) j6) & M26;
        long j7 = (j3 >> 25) + (((long) i7) * j);
        iArr2[5] = ((int) j7) & M26;
        long j8 = (j6 >> 26) + (((long) i3) * j);
        iArr2[1] = ((int) j8) & M26;
        long j9 = (j2 >> 25) + (((long) i5) * j);
        iArr2[3] = ((int) j9) & M26;
        long j10 = (j7 >> 26) + (((long) i8) * j);
        iArr2[6] = ((int) j10) & M26;
        long j11 = (j4 >> 25) + (((long) i10) * j);
        iArr2[8] = ((int) j11) & M26;
        iArr2[2] = i12 + ((int) (j8 >> 26));
        iArr2[4] = i13 + ((int) (j9 >> 26));
        iArr2[7] = i14 + ((int) (j10 >> 26));
        iArr2[9] = i15 + ((int) (j11 >> 26));
    }

    public static void mul(int[] iArr, int[] iArr2, int[] iArr3) {
        int i = iArr[0];
        int i2 = iArr2[0];
        int i3 = iArr[1];
        int i4 = iArr2[1];
        int i5 = iArr[2];
        int i6 = iArr2[2];
        int i7 = iArr[3];
        int i8 = iArr2[3];
        int i9 = iArr[4];
        int i10 = iArr2[4];
        int i11 = iArr[5];
        int i12 = iArr2[5];
        int i13 = iArr[6];
        int i14 = iArr2[6];
        int i15 = i13;
        int i16 = iArr[7];
        int i17 = iArr2[7];
        int i18 = iArr[8];
        int i19 = iArr2[8];
        int i20 = iArr[9];
        long j = (long) i;
        int i21 = i14;
        int i22 = i12;
        long j2 = (long) i2;
        long j3 = j * j2;
        int i23 = i;
        int i24 = i2;
        long j4 = (long) i4;
        int i25 = i4;
        long j5 = (long) i3;
        long j6 = (j * j4) + (j5 * j2);
        int i26 = i19;
        int i27 = i3;
        long j7 = (long) i6;
        int i28 = i6;
        long j8 = (long) i5;
        long j9 = (j * j7) + (j5 * j4) + (j8 * j2);
        long j10 = j4;
        long j11 = (long) i8;
        int i29 = i8;
        long j12 = (long) i7;
        long j13 = (((j5 * j7) + (j8 * j4)) << 1) + (j * j11) + (j12 * j2);
        long j14 = j7;
        int i30 = i7;
        int i31 = i10;
        long j15 = (long) i31;
        int i32 = i31;
        long j16 = j12;
        int i33 = i9;
        long j17 = (long) i33;
        long j18 = ((j8 * j7) << 1) + (j * j15) + (j5 * j11) + (j12 * j10) + (j2 * j17);
        long j19 = ((((j5 * j15) + (j8 * j11)) + (j16 * j14)) + (j17 * j10)) << 1;
        long j20 = (((j8 * j15) + (j17 * j14)) << 1) + (j16 * j11);
        long j21 = (j17 * j15) << 1;
        int i34 = i11;
        long j22 = (long) i34;
        int i35 = i22;
        long j23 = (long) i35;
        long j24 = j22 * j23;
        long j25 = j21;
        int i36 = i33;
        int i37 = i21;
        long j26 = (long) i37;
        int i38 = i5;
        int i39 = i15;
        long j27 = (long) i39;
        long j28 = (j22 * j26) + (j27 * j23);
        int i40 = i35;
        int i41 = i37;
        int i42 = i17;
        long j29 = (long) i42;
        int i43 = i34;
        int i44 = i16;
        long j30 = (long) i44;
        long j31 = (j22 * j29) + (j27 * j26) + (j30 * j23);
        int i45 = i44;
        int i46 = i26;
        long j32 = (long) i46;
        int i47 = i46;
        long j33 = j26;
        int i48 = i18;
        long j34 = (long) i48;
        long j35 = (((j27 * j29) + (j30 * j26)) << 1) + (j22 * j32) + (j34 * j23);
        int i49 = i48;
        long j36 = j29;
        int i50 = iArr2[9];
        long j37 = (long) i50;
        int i51 = i50;
        long j38 = j34;
        int i52 = i20;
        long j39 = (long) i52;
        long j40 = ((j30 * j29) << 1) + (j22 * j37) + (j27 * j32) + (j34 * j33) + (j23 * j39);
        long j41 = (j27 * j37) + (j30 * j32) + (j38 * j36) + (j39 * j33);
        long j42 = j3 - (j41 * 76);
        long j43 = j6 - (((((j30 * j37) + (j39 * j36)) << 1) + (j38 * j32)) * 38);
        long j44 = j9 - (((j38 * j37) + (j32 * j39)) * 38);
        long j45 = j13 - ((j39 * j37) * 76);
        long j46 = j25 - j35;
        long j47 = ((j16 * j15) + (j11 * j17)) - j31;
        long j48 = j20 - j28;
        long j49 = (long) (i23 + i43);
        long j50 = (long) (i24 + i40);
        long j51 = j19 - j24;
        long j52 = (long) (i25 + i41);
        long j53 = (long) (i27 + i39);
        long j54 = (j49 * j52) + (j53 * j50);
        long j55 = j46;
        long j56 = (long) (i28 + i42);
        long j57 = (long) (i38 + i45);
        long j58 = (j49 * j56) + (j53 * j52) + (j57 * j50);
        long j59 = (long) (i29 + i47);
        long j60 = j52;
        long j61 = (long) (i30 + i49);
        long j62 = (((j53 * j56) + (j57 * j52)) << 1) + (j49 * j59) + (j61 * j50);
        long j63 = j56;
        long j64 = (long) (i32 + i51);
        long j65 = j61;
        long j66 = (long) (i36 + i52);
        long j67 = ((j57 * j56) << 1) + (j49 * j64) + (j53 * j59) + (j61 * j60) + (j50 * j66);
        long j68 = (((j57 * j64) + (j66 * j63)) << 1) + (j65 * j59);
        long j69 = j55 + (j62 - j45);
        int i53 = ((int) j69) & M26;
        long j70 = (j69 >> 26) + ((j67 - j18) - j40);
        int i54 = ((int) j70) & M25;
        long j71 = j42 + ((((j70 >> 25) + (((((j53 * j64) + (j57 * j59)) + (j65 * j63)) + (j66 * j60)) << 1)) - j51) * 38);
        iArr3[0] = ((int) j71) & M26;
        long j72 = (j71 >> 26) + j43 + ((j68 - j48) * 38);
        iArr3[1] = ((int) j72) & M26;
        long j73 = (j72 >> 26) + j44 + ((((j65 * j64) + (j59 * j66)) - j47) * 38);
        iArr3[2] = ((int) j73) & M25;
        long j74 = (j73 >> 25) + j45 + ((((j66 * j64) << 1) - j55) * 38);
        iArr3[3] = ((int) j74) & M26;
        long j75 = (j74 >> 26) + j18 + (j40 * 38);
        iArr3[4] = ((int) j75) & M25;
        long j76 = (j75 >> 25) + j51 + ((j49 * j50) - j42);
        iArr3[5] = ((int) j76) & M26;
        long j77 = (j76 >> 26) + j48 + (j54 - j43);
        iArr3[6] = ((int) j77) & M26;
        long j78 = (j77 >> 26) + j47 + (j58 - j44);
        iArr3[7] = ((int) j78) & M25;
        long j79 = (j78 >> 25) + ((long) i53);
        iArr3[8] = ((int) j79) & M26;
        iArr3[9] = i54 + ((int) (j79 >> 26));
    }

    public static void negate(int[] iArr, int[] iArr2) {
        for (int i = 0; i < 10; i++) {
            iArr2[i] = -iArr[i];
        }
    }

    public static void normalize(int[] iArr) {
        int i = (iArr[9] >>> 23) & 1;
        reduce(iArr, i);
        reduce(iArr, -i);
    }

    public static void one(int[] iArr) {
        iArr[0] = 1;
        for (int i = 1; i < 10; i++) {
            iArr[i] = 0;
        }
    }

    private static void powPm5d8(int[] iArr, int[] iArr2, int[] iArr3) {
        sqr(iArr, iArr2);
        mul(iArr, iArr2, iArr2);
        int[] create = create();
        sqr(iArr2, create);
        mul(iArr, create, create);
        sqr(create, 2, create);
        mul(iArr2, create, create);
        int[] create2 = create();
        sqr(create, 5, create2);
        mul(create, create2, create2);
        int[] create3 = create();
        sqr(create2, 5, create3);
        mul(create, create3, create3);
        sqr(create3, 10, create);
        mul(create2, create, create);
        sqr(create, 25, create2);
        mul(create, create2, create2);
        sqr(create2, 25, create3);
        mul(create, create3, create3);
        sqr(create3, 50, create);
        mul(create2, create, create);
        sqr(create, 125, create2);
        mul(create, create2, create2);
        sqr(create2, 2, create);
        mul(create, iArr, iArr3);
    }

    private static void reduce(int[] iArr, int i) {
        int i2 = iArr[9];
        int i3 = M24 & i2;
        int i4 = (((i2 >> 24) + i) * 19) + iArr[0];
        iArr[0] = i4 & M26;
        int i5 = (i4 >> 26) + iArr[1];
        iArr[1] = i5 & M26;
        int i6 = (i5 >> 26) + iArr[2];
        iArr[2] = i6 & M25;
        int i7 = (i6 >> 25) + iArr[3];
        iArr[3] = i7 & M26;
        int i8 = (i7 >> 26) + iArr[4];
        iArr[4] = i8 & M25;
        int i9 = (i8 >> 25) + iArr[5];
        iArr[5] = i9 & M26;
        int i10 = (i9 >> 26) + iArr[6];
        iArr[6] = i10 & M26;
        int i11 = (i10 >> 26) + iArr[7];
        iArr[7] = M25 & i11;
        int i12 = (i11 >> 25) + iArr[8];
        iArr[8] = M26 & i12;
        iArr[9] = (i12 >> 26) + i3;
    }

    public static void sqr(int[] iArr, int i, int[] iArr2) {
        sqr(iArr, iArr2);
        while (true) {
            i--;
            if (i > 0) {
                sqr(iArr2, iArr2);
            } else {
                return;
            }
        }
    }

    public static void sqr(int[] iArr, int[] iArr2) {
        int i = iArr[0];
        int i2 = iArr[1];
        int i3 = iArr[2];
        int i4 = iArr[3];
        int i5 = iArr[4];
        int i6 = iArr[5];
        int i7 = iArr[6];
        int i8 = iArr[7];
        int i9 = iArr[8];
        int i10 = iArr[9];
        long j = (long) i;
        long j2 = j * j;
        int i11 = i9;
        long j3 = (long) (i2 * 2);
        long j4 = j * j3;
        int i12 = i8;
        int i13 = i7;
        long j5 = (long) (i3 * 2);
        long j6 = (long) i2;
        long j7 = (j * j5) + (j6 * j6);
        int i14 = i2;
        long j8 = (long) (i4 * 2);
        long j9 = (j3 * j5) + (j * j8);
        int i15 = i;
        long j10 = (long) (i5 * 2);
        long j11 = (((long) i3) * j5) + (j * j10) + (j6 * j8);
        long j12 = (j3 * j10) + (j8 * j5);
        int i16 = i4;
        long j13 = (long) i16;
        long j14 = (j5 * j10) + (j13 * j13);
        long j15 = j13 * j10;
        int i17 = i5;
        long j16 = j11;
        int i18 = i17;
        long j17 = ((long) i17) * j10;
        int i19 = i6;
        long j18 = (long) i19;
        long j19 = j18 * j18;
        int i20 = i16;
        long j20 = (long) (i13 * 2);
        long j21 = j18 * j20;
        long j22 = (long) (i12 * 2);
        long j23 = j15;
        int i21 = i3;
        int i22 = i13;
        long j24 = (long) i22;
        long j25 = (j18 * j22) + (j24 * j24);
        int i23 = i22;
        long j26 = (long) (i11 * 2);
        long j27 = j12;
        long j28 = j14;
        int i24 = i12;
        int i25 = i24;
        long j29 = (long) (i10 * 2);
        long j30 = (((long) i24) * j22) + (j18 * j29) + (j24 * j26);
        int i26 = i11;
        long j31 = (long) i26;
        long j32 = j2 - (((j20 * j29) + (j26 * j22)) * 38);
        long j33 = j4 - (((j22 * j29) + (j31 * j31)) * 38);
        long j34 = j7 - ((j31 * j29) * 38);
        long j35 = j9 - ((((long) i10) * j29) * 38);
        int i27 = i14 + i23;
        int i28 = i21 + i25;
        int i29 = i20 + i26;
        int i30 = i18 + i10;
        long j36 = j23 - j25;
        long j37 = j28 - j21;
        long j38 = j27 - j19;
        long j39 = (long) (i15 + i19);
        long j40 = j30;
        long j41 = (long) (i27 * 2);
        long j42 = j17 - ((j20 * j22) + (j18 * j26));
        long j43 = (long) (i28 * 2);
        long j44 = (long) i27;
        long j45 = (j39 * j43) + (j44 * j44);
        long j46 = (long) (i29 * 2);
        long j47 = (long) (i30 * 2);
        long j48 = (((long) i28) * j43) + (j39 * j47) + (j44 * j46);
        long j49 = (long) i29;
        long j50 = j42 + (((j41 * j43) + (j39 * j46)) - j35);
        int i31 = ((int) j50) & M26;
        long j51 = (j50 >> 26) + ((j48 - j16) - j40);
        int i32 = ((int) j51) & M25;
        long j52 = j32 + ((((j51 >> 25) + ((j41 * j47) + (j46 * j43))) - j38) * 38);
        iArr2[0] = ((int) j52) & M26;
        long j53 = (j52 >> 26) + j33 + ((((j43 * j47) + (j49 * j49)) - j37) * 38);
        iArr2[1] = ((int) j53) & M26;
        long j54 = (j53 >> 26) + j34 + (((j49 * j47) - j36) * 38);
        iArr2[2] = ((int) j54) & M25;
        long j55 = (j54 >> 25) + j35 + (((((long) i30) * j47) - j42) * 38);
        iArr2[3] = ((int) j55) & M26;
        long j56 = (j55 >> 26) + j16 + (38 * j40);
        iArr2[4] = ((int) j56) & M25;
        long j57 = (j56 >> 25) + j38 + ((j39 * j39) - j32);
        iArr2[5] = ((int) j57) & M26;
        long j58 = (j57 >> 26) + j37 + ((j39 * j41) - j33);
        iArr2[6] = ((int) j58) & M26;
        long j59 = (j58 >> 26) + j36 + (j45 - j34);
        iArr2[7] = ((int) j59) & M25;
        long j60 = (j59 >> 25) + ((long) i31);
        iArr2[8] = ((int) j60) & M26;
        iArr2[9] = i32 + ((int) (j60 >> 26));
    }

    public static boolean sqrtRatioVar(int[] iArr, int[] iArr2, int[] iArr3) {
        int[] create = create();
        int[] create2 = create();
        mul(iArr, iArr2, create);
        sqr(iArr2, create2);
        mul(create, create2, create);
        sqr(create2, create2);
        mul(create2, create, create2);
        int[] create3 = create();
        int[] create4 = create();
        powPm5d8(create2, create3, create4);
        mul(create4, create, create4);
        int[] create5 = create();
        sqr(create4, create5);
        mul(create5, iArr2, create5);
        sub(create5, iArr, create3);
        normalize(create3);
        if (isZeroVar(create3)) {
            copy(create4, 0, iArr3, 0);
            return true;
        }
        add(create5, iArr, create3);
        normalize(create3);
        if (!isZeroVar(create3)) {
            return false;
        }
        mul(create4, ROOT_NEG_ONE, iArr3);
        return true;
    }

    public static void sub(int[] iArr, int[] iArr2, int[] iArr3) {
        for (int i = 0; i < 10; i++) {
            iArr3[i] = iArr[i] - iArr2[i];
        }
    }

    public static void subOne(int[] iArr) {
        iArr[0] = iArr[0] - 1;
    }

    public static void zero(int[] iArr) {
        for (int i = 0; i < 10; i++) {
            iArr[i] = 0;
        }
    }
}
