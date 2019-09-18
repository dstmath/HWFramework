package org.bouncycastle.math.ec.rfc7748;

import org.bouncycastle.math.raw.Nat;

public abstract class X448Field {
    private static final int M28 = 268435455;
    public static final int SIZE = 16;

    private X448Field() {
    }

    public static void add(int[] iArr, int[] iArr2, int[] iArr3) {
        for (int i = 0; i < 16; i++) {
            iArr3[i] = iArr[i] + iArr2[i];
        }
    }

    public static void addOne(int[] iArr) {
        iArr[0] = iArr[0] + 1;
    }

    public static void addOne(int[] iArr, int i) {
        iArr[i] = iArr[i] + 1;
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
        int i11 = iArr[10];
        int i12 = iArr[11];
        int i13 = iArr[12];
        int i14 = iArr[13];
        int i15 = iArr[14];
        int i16 = iArr[15];
        int i17 = i3 + (i2 >>> 28);
        int i18 = i2 & M28;
        int i19 = i7 + (i6 >>> 28);
        int i20 = i6 & M28;
        int i21 = i11 + (i10 >>> 28);
        int i22 = i10 & M28;
        int i23 = i15 + (i14 >>> 28);
        int i24 = i14 & M28;
        int i25 = i4 + (i17 >>> 28);
        int i26 = i17 & M28;
        int i27 = i8 + (i19 >>> 28);
        int i28 = i19 & M28;
        int i29 = i12 + (i21 >>> 28);
        int i30 = i21 & M28;
        int i31 = i16 + (i23 >>> 28);
        int i32 = i23 & M28;
        int i33 = i31 >>> 28;
        int i34 = i31 & M28;
        int i35 = i + i33;
        int i36 = i5 + (i25 >>> 28);
        int i37 = i25 & M28;
        int i38 = i9 + i33 + (i27 >>> 28);
        int i39 = i27 & M28;
        int i40 = i13 + (i29 >>> 28);
        int i41 = i29 & M28;
        int i42 = i18 + (i35 >>> 28);
        int i43 = i35 & M28;
        int i44 = i20 + (i36 >>> 28);
        int i45 = i36 & M28;
        int i46 = i22 + (i38 >>> 28);
        int i47 = i38 & M28;
        int i48 = i40 & M28;
        iArr[0] = i43;
        iArr[1] = i42;
        iArr[2] = i26;
        iArr[3] = i37;
        iArr[4] = i45;
        iArr[5] = i44;
        iArr[6] = i28;
        iArr[7] = i39;
        iArr[8] = i47;
        iArr[9] = i46;
        iArr[10] = i30;
        iArr[11] = i41;
        iArr[12] = i48;
        iArr[13] = i24 + (i40 >>> 28);
        iArr[14] = i32;
        iArr[15] = i34;
    }

    public static void cnegate(int i, int[] iArr) {
        int[] create = create();
        sub(create, iArr, create);
        Nat.cmov(16, i, create, 0, iArr, 0);
    }

    public static void copy(int[] iArr, int i, int[] iArr2, int i2) {
        for (int i3 = 0; i3 < 16; i3++) {
            iArr2[i2 + i3] = iArr[i + i3];
        }
    }

    public static int[] create() {
        return new int[16];
    }

    public static int[] createTable(int i) {
        return new int[(16 * i)];
    }

    public static void cswap(int i, int[] iArr, int[] iArr2) {
        int i2 = 0 - i;
        for (int i3 = 0; i3 < 16; i3++) {
            int i4 = iArr[i3];
            int i5 = iArr2[i3];
            int i6 = (i4 ^ i5) & i2;
            iArr[i3] = i4 ^ i6;
            iArr2[i3] = i5 ^ i6;
        }
    }

    public static void decode(byte[] bArr, int i, int[] iArr) {
        decode56(bArr, i, iArr, 0);
        decode56(bArr, i + 7, iArr, 2);
        decode56(bArr, i + 14, iArr, 4);
        decode56(bArr, i + 21, iArr, 6);
        decode56(bArr, i + 28, iArr, 8);
        decode56(bArr, i + 35, iArr, 10);
        decode56(bArr, i + 42, iArr, 12);
        decode56(bArr, i + 49, iArr, 14);
    }

    private static int decode24(byte[] bArr, int i) {
        int i2 = i + 1;
        return ((bArr[i2 + 1] & 255) << Tnaf.POW_2_WIDTH) | (bArr[i] & 255) | ((bArr[i2] & 255) << 8);
    }

    private static int decode32(byte[] bArr, int i) {
        int i2 = i + 1;
        int i3 = i2 + 1;
        return (bArr[i3 + 1] << 24) | (bArr[i] & 255) | ((bArr[i2] & 255) << 8) | ((bArr[i3] & 255) << Tnaf.POW_2_WIDTH);
    }

    private static void decode56(byte[] bArr, int i, int[] iArr, int i2) {
        int decode32 = decode32(bArr, i);
        int decode24 = decode24(bArr, i + 4);
        iArr[i2] = M28 & decode32;
        iArr[i2 + 1] = (decode24 << 4) | (decode32 >>> 28);
    }

    public static void encode(int[] iArr, byte[] bArr, int i) {
        encode56(iArr, 0, bArr, i);
        encode56(iArr, 2, bArr, i + 7);
        encode56(iArr, 4, bArr, i + 14);
        encode56(iArr, 6, bArr, i + 21);
        encode56(iArr, 8, bArr, i + 28);
        encode56(iArr, 10, bArr, i + 35);
        encode56(iArr, 12, bArr, i + 42);
        encode56(iArr, 14, bArr, i + 49);
    }

    private static void encode24(int i, byte[] bArr, int i2) {
        bArr[i2] = (byte) i;
        int i3 = i2 + 1;
        bArr[i3] = (byte) (i >>> 8);
        bArr[i3 + 1] = (byte) (i >>> 16);
    }

    private static void encode32(int i, byte[] bArr, int i2) {
        bArr[i2] = (byte) i;
        int i3 = i2 + 1;
        bArr[i3] = (byte) (i >>> 8);
        int i4 = i3 + 1;
        bArr[i4] = (byte) (i >>> 16);
        bArr[i4 + 1] = (byte) (i >>> 24);
    }

    private static void encode56(int[] iArr, int i, byte[] bArr, int i2) {
        int i3 = iArr[i];
        int i4 = iArr[i + 1];
        encode32((i4 << 28) | i3, bArr, i2);
        encode24(i4 >>> 4, bArr, i2 + 4);
    }

    public static void inv(int[] iArr, int[] iArr2) {
        int[] create = create();
        powPm3d4(iArr, create);
        sqr(create, 2, create);
        mul(create, iArr, iArr2);
    }

    public static boolean isZeroVar(int[] iArr) {
        int i = 0;
        for (int i2 = 0; i2 < 16; i2++) {
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
        int i12 = iArr[10];
        int i13 = iArr[11];
        int i14 = iArr[12];
        int i15 = iArr[13];
        int i16 = i2;
        int i17 = iArr[14];
        int i18 = i10;
        int i19 = i6;
        long j = (long) i;
        long j2 = ((long) i3) * j;
        long j3 = ((long) i7) * j;
        int i20 = iArr[15];
        int i21 = ((int) j3) & M28;
        int i22 = i13;
        long j4 = ((long) i11) * j;
        int i23 = ((int) j4) & M28;
        long j5 = ((long) i15) * j;
        int i24 = ((int) j5) & M28;
        long j6 = (j2 >>> 28) + (((long) i4) * j);
        iArr2[2] = ((int) j6) & M28;
        long j7 = (j3 >>> 28) + (((long) i8) * j);
        iArr2[6] = ((int) j7) & M28;
        long j8 = (j4 >>> 28) + (((long) i12) * j);
        iArr2[10] = ((int) j8) & M28;
        long j9 = (j5 >>> 28) + (((long) i17) * j);
        iArr2[14] = ((int) j9) & M28;
        long j10 = (j6 >>> 28) + (((long) i5) * j);
        iArr2[3] = ((int) j10) & M28;
        long j11 = (j7 >>> 28) + (((long) i9) * j);
        iArr2[7] = ((int) j11) & M28;
        long j12 = (j8 >>> 28) + (((long) i22) * j);
        iArr2[11] = ((int) j12) & M28;
        long j13 = (j9 >>> 28) + (((long) i20) * j);
        iArr2[15] = ((int) j13) & M28;
        long j14 = j13 >>> 28;
        long j15 = (j10 >>> 28) + (((long) i19) * j);
        iArr2[4] = ((int) j15) & M28;
        long j16 = (j11 >>> 28) + j14 + (((long) i18) * j);
        iArr2[8] = ((int) j16) & M28;
        long j17 = (j12 >>> 28) + (((long) i14) * j);
        iArr2[12] = ((int) j17) & M28;
        long j18 = j14 + (((long) i16) * j);
        iArr2[0] = ((int) j18) & M28;
        iArr2[1] = (((int) j2) & M28) + ((int) (j18 >>> 28));
        iArr2[5] = i21 + ((int) (j15 >>> 28));
        iArr2[9] = i23 + ((int) (j16 >>> 28));
        iArr2[13] = i24 + ((int) (j17 >>> 28));
    }

    public static void mul(int[] iArr, int[] iArr2, int[] iArr3) {
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
        int i11 = iArr[10];
        int i12 = iArr[11];
        int i13 = iArr[12];
        int i14 = iArr[13];
        int i15 = i8;
        int i16 = iArr[14];
        int i17 = iArr[15];
        int i18 = iArr2[0];
        int i19 = iArr2[1];
        int i20 = iArr2[2];
        int i21 = iArr2[3];
        int i22 = iArr2[4];
        int i23 = iArr2[5];
        int i24 = iArr2[6];
        int i25 = iArr2[7];
        int i26 = iArr2[8];
        int i27 = iArr2[9];
        int i28 = iArr2[10];
        int i29 = iArr2[11];
        int i30 = iArr2[12];
        int i31 = iArr2[13];
        int i32 = iArr2[14];
        int i33 = iArr2[15];
        int i34 = i + i9;
        int i35 = i2 + i10;
        int i36 = i3 + i11;
        int i37 = i4 + i12;
        int i38 = i5 + i13;
        int i39 = i6 + i14;
        int i40 = i7 + i16;
        int i41 = i15 + i17;
        int i42 = i18 + i26;
        int i43 = i20 + i28;
        int i44 = i22 + i30;
        int i45 = i24 + i32;
        int i46 = i32;
        long j = (long) i;
        long j2 = (long) i18;
        long j3 = j * j2;
        long j4 = j;
        long j5 = (long) i15;
        long j6 = j2;
        long j7 = (long) i19;
        long j8 = j5 * j7;
        long j9 = j5;
        long j10 = (long) i7;
        long j11 = j7;
        long j12 = (long) i20;
        long j13 = (long) i6;
        long j14 = j10;
        long j15 = (long) i21;
        long j16 = j13;
        long j17 = (long) i5;
        long j18 = j15;
        long j19 = (long) i22;
        long j20 = j17;
        long j21 = (long) i4;
        long j22 = j19;
        long j23 = (long) i23;
        long j24 = j21;
        long j25 = (long) i3;
        long j26 = j23;
        long j27 = (long) i24;
        long j28 = j25;
        long j29 = (long) i2;
        long j30 = j27;
        long j31 = (long) i25;
        long j32 = j8 + (j10 * j12) + (j13 * j15) + (j17 * j19) + (j21 * j23) + (j25 * j27) + (j29 * j31);
        long j33 = (long) i9;
        long j34 = j31;
        long j35 = (long) i26;
        long j36 = j33 * j35;
        long j37 = j33;
        long j38 = (long) i17;
        long j39 = j35;
        long j40 = (long) i27;
        long j41 = j38 * j40;
        long j42 = (long) i16;
        long j43 = j38;
        long j44 = (long) i28;
        long j45 = j42;
        long j46 = (long) i14;
        long j47 = j44;
        long j48 = (long) i29;
        long j49 = (long) i13;
        long j50 = j46;
        long j51 = (long) i30;
        long j52 = j49;
        long j53 = (long) i12;
        long j54 = j51;
        long j55 = (long) i31;
        long j56 = (long) i11;
        long j57 = j53;
        long j58 = (long) i46;
        long j59 = (long) i10;
        long j60 = j56;
        long j61 = (long) i33;
        long j62 = j41 + (j42 * j44) + (j46 * j48) + (j49 * j51) + (j53 * j55) + (j56 * j58) + (j59 * j61);
        long j63 = j61;
        long j64 = (long) i34;
        long j65 = j58;
        long j66 = (long) i42;
        long j67 = j64 * j66;
        long j68 = j64;
        long j69 = (long) i41;
        long j70 = j66;
        long j71 = (long) (i19 + i27);
        long j72 = j69 * j71;
        long j73 = j69;
        long j74 = (long) i40;
        long j75 = j71;
        long j76 = (long) i43;
        long j77 = j74;
        long j78 = (long) i39;
        long j79 = j76;
        long j80 = (long) (i21 + i29);
        long j81 = j78;
        long j82 = (long) i38;
        long j83 = j80;
        long j84 = (long) i44;
        long j85 = j82;
        long j86 = (long) i37;
        long j87 = j84;
        long j88 = (long) (i23 + i31);
        long j89 = j86;
        long j90 = (long) i36;
        long j91 = j88;
        long j92 = (long) i45;
        long j93 = j90;
        long j94 = (long) i35;
        long j95 = j92;
        long j96 = (long) (i25 + i33);
        long j97 = j72 + (j74 * j76) + (j78 * j80) + (j82 * j84) + (j86 * j88) + (j90 * j92) + (j94 * j96);
        long j98 = j96;
        long j99 = ((j3 + j36) + j97) - j32;
        long j100 = j94;
        long j101 = j99 >>> 28;
        long j102 = ((j62 + j67) - j3) + j97;
        int i47 = ((int) j99) & M28;
        long j103 = (j29 * j6) + (j4 * j11);
        long j104 = (j73 * j79) + (j77 * j83) + (j81 * j87) + (j85 * j91) + (j89 * j95) + (j93 * j98);
        long j105 = j55;
        long j106 = j101 + (((j103 + ((j59 * j39) + (j37 * j40))) + j104) - ((((((j9 * j12) + (j14 * j18)) + (j16 * j22)) + (j20 * j26)) + (j24 * j30)) + (j28 * j34)));
        int i48 = ((int) j102) & M28;
        long j107 = (j102 >>> 28) + ((((((((j43 * j47) + (j45 * j48)) + (j50 * j54)) + (j52 * j55)) + (j57 * j65)) + (j60 * j63)) + ((j100 * j70) + (j68 * j75))) - j103) + j104;
        int i49 = ((int) j106) & M28;
        long j108 = (j28 * j6) + (j29 * j11) + (j4 * j12);
        long j109 = (j73 * j83) + (j77 * j87) + (j81 * j91) + (j85 * j95) + (j89 * j98);
        long j110 = (j106 >>> 28) + (((j108 + (((j60 * j39) + (j59 * j40)) + (j37 * j47))) + j109) - (((((j9 * j18) + (j14 * j22)) + (j16 * j26)) + (j20 * j30)) + (j24 * j34)));
        int i50 = ((int) j107) & M28;
        long j111 = (j107 >>> 28) + (((((((j43 * j48) + (j45 * j54)) + (j50 * j105)) + (j52 * j65)) + (j57 * j63)) + (((j93 * j70) + (j100 * j75)) + (j68 * j79))) - j108) + j109;
        int i51 = ((int) j110) & M28;
        long j112 = (j24 * j6) + (j28 * j11) + (j29 * j12) + (j4 * j18);
        long j113 = (j73 * j87) + (j77 * j91) + (j81 * j95) + (j85 * j98);
        long j114 = (j110 >>> 28) + (((j112 + ((((j57 * j39) + (j60 * j40)) + (j59 * j47)) + (j37 * j48))) + j113) - ((((j9 * j22) + (j14 * j26)) + (j16 * j30)) + (j20 * j34)));
        int i52 = ((int) j111) & M28;
        long j115 = (j111 >>> 28) + ((((((j43 * j54) + (j45 * j105)) + (j50 * j65)) + (j52 * j63)) + ((((j89 * j70) + (j93 * j75)) + (j100 * j79)) + (j68 * j83))) - j112) + j113;
        int i53 = ((int) j114) & M28;
        long j116 = (j20 * j6) + (j24 * j11) + (j28 * j12) + (j29 * j18) + (j4 * j22);
        long j117 = (j73 * j91) + (j77 * j95) + (j81 * j98);
        long j118 = (j114 >>> 28) + (((j116 + (((((j52 * j39) + (j57 * j40)) + (j60 * j47)) + (j59 * j48)) + (j37 * j54))) + j117) - (((j9 * j26) + (j14 * j30)) + (j16 * j34)));
        int i54 = ((int) j115) & M28;
        long j119 = (j115 >>> 28) + (((((j43 * j105) + (j45 * j65)) + (j50 * j63)) + (((((j85 * j70) + (j89 * j75)) + (j93 * j79)) + (j100 * j83)) + (j68 * j87))) - j116) + j117;
        int i55 = ((int) j118) & M28;
        long j120 = (j16 * j6) + (j20 * j11) + (j24 * j12) + (j28 * j18) + (j29 * j22) + (j4 * j26);
        long j121 = (j73 * j95) + (j77 * j98);
        long j122 = (j118 >>> 28) + (((j120 + ((((((j50 * j39) + (j52 * j40)) + (j57 * j47)) + (j60 * j48)) + (j59 * j54)) + (j37 * j105))) + j121) - ((j9 * j30) + (j14 * j34)));
        int i56 = ((int) j119) & M28;
        long j123 = (j119 >>> 28) + ((((j43 * j65) + (j45 * j63)) + ((((((j81 * j70) + (j85 * j75)) + (j89 * j79)) + (j93 * j83)) + (j100 * j87)) + (j68 * j91))) - j120) + j121;
        int i57 = ((int) j122) & M28;
        long j124 = (j14 * j6) + (j16 * j11) + (j20 * j12) + (j24 * j18) + (j28 * j22) + (j29 * j26) + (j4 * j30);
        long j125 = j73 * j98;
        long j126 = (j122 >>> 28) + (((j124 + (((((((j45 * j39) + (j50 * j40)) + (j52 * j47)) + (j57 * j48)) + (j60 * j54)) + (j59 * j105)) + (j37 * j65))) + j125) - (j9 * j34));
        int i58 = ((int) j123) & M28;
        long j127 = (j123 >>> 28) + (((j43 * j63) + (((((((j77 * j70) + (j81 * j75)) + (j85 * j79)) + (j89 * j83)) + (j93 * j87)) + (j100 * j91)) + (j68 * j95))) - j124) + j125;
        int i59 = ((int) j126) & M28;
        int i60 = ((int) j127) & M28;
        long j128 = (j9 * j6) + (j14 * j11) + (j12 * j16) + (j20 * j18) + (j24 * j22) + (j28 * j26) + (j29 * j30) + (j4 * j34);
        long j129 = (j126 >>> 28) + j128 + (j43 * j39) + (j40 * j45) + (j50 * j47) + (j52 * j48) + (j57 * j54) + (j60 * j105) + (j59 * j65) + (j37 * j63);
        int i61 = ((int) j129) & M28;
        long j130 = (j127 >>> 28) + (((((((((j73 * j70) + (j77 * j75)) + (j81 * j79)) + (j85 * j83)) + (j89 * j87)) + (j93 * j91)) + (j100 * j95)) + (j68 * j98)) - j128);
        int i62 = ((int) j130) & M28;
        long j131 = j130 >>> 28;
        long j132 = (j129 >>> 28) + j131 + ((long) i48);
        int i63 = ((int) j132) & M28;
        long j133 = j131 + ((long) i47);
        iArr3[0] = ((int) j133) & M28;
        iArr3[1] = i49 + ((int) (j133 >>> 28));
        iArr3[2] = i51;
        iArr3[3] = i53;
        iArr3[4] = i55;
        iArr3[5] = i57;
        iArr3[6] = i59;
        iArr3[7] = i61;
        iArr3[8] = i63;
        iArr3[9] = i50 + ((int) (j132 >>> 28));
        iArr3[10] = i52;
        iArr3[11] = i54;
        iArr3[12] = i56;
        iArr3[13] = i58;
        iArr3[14] = i60;
        iArr3[15] = i62;
    }

    public static void negate(int[] iArr, int[] iArr2) {
        sub(create(), iArr, iArr2);
    }

    public static void normalize(int[] iArr) {
        reduce(iArr, 1);
        reduce(iArr, -1);
    }

    public static void one(int[] iArr) {
        iArr[0] = 1;
        for (int i = 1; i < 16; i++) {
            iArr[i] = 0;
        }
    }

    private static void powPm3d4(int[] iArr, int[] iArr2) {
        int[] create = create();
        sqr(iArr, create);
        mul(iArr, create, create);
        int[] create2 = create();
        sqr(create, create2);
        mul(iArr, create2, create2);
        int[] create3 = create();
        sqr(create2, 3, create3);
        mul(create2, create3, create3);
        int[] create4 = create();
        sqr(create3, 3, create4);
        mul(create2, create4, create4);
        int[] create5 = create();
        sqr(create4, 9, create5);
        mul(create4, create5, create5);
        int[] create6 = create();
        sqr(create5, create6);
        mul(iArr, create6, create6);
        int[] create7 = create();
        sqr(create6, 18, create7);
        mul(create5, create7, create7);
        int[] create8 = create();
        sqr(create7, 37, create8);
        mul(create7, create8, create8);
        int[] create9 = create();
        sqr(create8, 37, create9);
        mul(create7, create9, create9);
        int[] create10 = create();
        sqr(create9, 111, create10);
        mul(create9, create10, create10);
        int[] create11 = create();
        sqr(create10, create11);
        mul(iArr, create11, create11);
        int[] create12 = create();
        sqr(create11, 223, create12);
        mul(create12, create10, iArr2);
    }

    private static void reduce(int[] iArr, int i) {
        int i2 = iArr[15];
        int i3 = i2 & M28;
        int i4 = (i2 >> 28) + i;
        iArr[8] = iArr[8] + i4;
        for (int i5 = 0; i5 < 15; i5++) {
            int i6 = i4 + iArr[i5];
            iArr[i5] = i6 & M28;
            i4 = i6 >> 28;
        }
        iArr[15] = i3 + i4;
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
        int i11 = iArr[10];
        int i12 = iArr[11];
        int i13 = iArr[12];
        int i14 = iArr[13];
        int i15 = iArr[14];
        int i16 = iArr[15];
        int i17 = i * 2;
        int i18 = i2 * 2;
        int i19 = i3 * 2;
        int i20 = i4 * 2;
        int i21 = i5 * 2;
        int i22 = i6 * 2;
        int i23 = i7 * 2;
        int i24 = i9 * 2;
        int i25 = i10 * 2;
        int i26 = i11 * 2;
        int i27 = i12 * 2;
        int i28 = i14 * 2;
        int i29 = i15 * 2;
        int i30 = i + i9;
        int i31 = i9;
        int i32 = i2 + i10;
        int i33 = i10;
        int i34 = i3 + i11;
        int i35 = i11;
        int i36 = i4 + i12;
        int i37 = i12;
        int i38 = i5 + i13;
        int i39 = i4;
        int i40 = i6 + i14;
        int i41 = i3;
        int i42 = i7 + i15;
        int i43 = i2;
        int i44 = i8 + i16;
        int i45 = i30 * 2;
        int i46 = i32 * 2;
        int i47 = i32;
        int i48 = i34 * 2;
        int i49 = i34;
        int i50 = i36 * 2;
        int i51 = i36;
        int i52 = i38 * 2;
        int i53 = i40 * 2;
        int i54 = i42 * 2;
        long j = (long) i;
        long j2 = j * j;
        long j3 = (long) i8;
        int i55 = i38;
        long j4 = (long) i18;
        long j5 = j3 * j4;
        long j6 = (long) i7;
        long j7 = j4;
        long j8 = (long) i19;
        long j9 = j6;
        long j10 = (long) i6;
        long j11 = j8;
        long j12 = (long) i20;
        long j13 = j10;
        long j14 = (long) i5;
        long j15 = j5 + (j6 * j8) + (j10 * j12) + (j14 * j14);
        long j16 = j14;
        long j17 = (long) i31;
        long j18 = j12;
        long j19 = (long) i16;
        long j20 = j3;
        long j21 = (long) i25;
        long j22 = j19 * j21;
        long j23 = j21;
        long j24 = (long) i15;
        long j25 = j19;
        long j26 = (long) i26;
        long j27 = (long) i14;
        long j28 = j24;
        long j29 = (long) i27;
        long j30 = (long) i13;
        long j31 = j30;
        long j32 = (long) i30;
        long j33 = j27;
        long j34 = (long) i44;
        long j35 = j29;
        long j36 = (long) i46;
        long j37 = j34 * j36;
        long j38 = j36;
        long j39 = (long) i42;
        long j40 = (long) i48;
        long j41 = j39;
        long j42 = (long) i40;
        long j43 = j34;
        long j44 = (long) i50;
        long j45 = j42;
        long j46 = (long) i55;
        long j47 = j37 + (j39 * j40) + (j42 * j44) + (j46 * j46);
        long j48 = ((j2 + (j17 * j17)) + j47) - j15;
        int i56 = ((int) j48) & M28;
        long j49 = (((((j22 + (j24 * j26)) + (j27 * j29)) + (j30 * j30)) + (j32 * j32)) - j2) + j47;
        int i57 = ((int) j49) & M28;
        long j50 = j26;
        long j51 = (long) i43;
        long j52 = j46;
        long j53 = (long) i17;
        long j54 = j51 * j53;
        int i58 = i56;
        long j55 = j51;
        long j56 = (long) i21;
        long j57 = (j20 * j11) + (j9 * j18) + (j13 * j56);
        long j58 = j56;
        long j59 = (long) i33;
        long j60 = j53;
        long j61 = (long) i24;
        long j62 = j59 * j61;
        long j63 = j59;
        long j64 = (long) (i13 * 2);
        long j65 = j64;
        long j66 = (long) i47;
        long j67 = j61;
        long j68 = (long) i45;
        long j69 = j40;
        long j70 = (long) i52;
        long j71 = (j43 * j40) + (j41 * j44) + (j45 * j70);
        long j72 = (j48 >>> 28) + (((j54 + j62) + j71) - j57);
        int i59 = ((int) j72) & M28;
        long j73 = (j49 >>> 28) + (((((j25 * j50) + (j28 * j35)) + (j33 * j64)) + (j66 * j68)) - j54) + j71;
        int i60 = ((int) j73) & M28;
        long j74 = (long) i41;
        long j75 = (j74 * j60) + (j55 * j55);
        int i61 = i59;
        long j76 = j74;
        long j77 = (long) i35;
        long j78 = (j77 * j67) + (j63 * j63);
        long j79 = j77;
        long j80 = (long) i49;
        long j81 = (j80 * j68) + (j66 * j66);
        long j82 = (j43 * j44) + (j41 * j70) + (j45 * j45);
        long j83 = (j72 >>> 28) + (((j75 + j78) + j82) - (((j20 * j18) + (j9 * j58)) + (j13 * j13)));
        int i62 = ((int) j83) & M28;
        long j84 = (j73 >>> 28) + (((((j25 * j35) + (j28 * j65)) + (j33 * j33)) + j81) - j75) + j82;
        int i63 = ((int) j84) & M28;
        long j85 = (long) i39;
        long j86 = (j85 * j60) + (j76 * j7);
        long j87 = j44;
        int i64 = i62;
        long j88 = (long) i22;
        long j89 = (j20 * j58) + (j9 * j88);
        long j90 = j88;
        long j91 = (long) i37;
        long j92 = (j91 * j67) + (j79 * j23);
        long j93 = j91;
        long j94 = (long) i28;
        long j95 = (j25 * j65) + (j28 * j94);
        long j96 = j94;
        long j97 = (long) i51;
        long j98 = (j97 * j68) + (j80 * j38);
        long j99 = j80;
        long j100 = (long) i53;
        long j101 = (j70 * j43) + (j41 * j100);
        long j102 = (j83 >>> 28) + (((j86 + j92) + j101) - j89);
        int i65 = ((int) j102) & M28;
        long j103 = (j84 >>> 28) + ((j95 + j98) - j86) + j101;
        int i66 = ((int) j103) & M28;
        long j104 = (j16 * j60) + (j85 * j7) + (j76 * j76);
        long j105 = (j100 * j43) + (j41 * j41);
        long j106 = (j102 >>> 28) + (((j104 + (((j31 * j67) + (j93 * j23)) + (j79 * j79))) + j105) - ((j20 * j90) + (j9 * j9)));
        int i67 = ((int) j106) & M28;
        long j107 = (j103 >>> 28) + ((((j25 * j96) + (j28 * j28)) + (((j52 * j68) + (j97 * j38)) + (j99 * j99))) - j104) + j105;
        long j108 = (j13 * j60) + (j16 * j7) + (j85 * j11);
        int i68 = ((int) j107) & M28;
        int i69 = i66;
        long j109 = j97;
        int i70 = i65;
        long j110 = ((long) i54) * j43;
        long j111 = (j106 >>> 28) + (((j108 + (((j33 * j67) + (j31 * j23)) + (j93 * j50))) + j110) - (((long) i23) * j20));
        int i71 = ((int) j111) & M28;
        long j112 = (j107 >>> 28) + (((((long) i29) * j25) + (((j45 * j68) + (j52 * j38)) + (j97 * j69))) - j108) + j110;
        int i72 = ((int) j112) & M28;
        long j113 = (j9 * j60) + (j13 * j7) + (j16 * j11) + (j85 * j85);
        long j114 = j43 * j43;
        long j115 = (j111 >>> 28) + (((((((j28 * j67) + (j33 * j23)) + (j31 * j50)) + (j93 * j93)) + j113) + j114) - (j20 * j20));
        int i73 = ((int) j115) & M28;
        long j116 = (j112 >>> 28) + (((j25 * j25) + ((((j41 * j68) + (j45 * j38)) + (j52 * j69)) + (j109 * j109))) - j113) + j114;
        int i74 = ((int) j116) & M28;
        long j117 = (j20 * j60) + (j9 * j7) + (j13 * j11) + (j16 * j18);
        long j118 = (j115 >>> 28) + (j25 * j67) + (j28 * j23) + (j33 * j50) + (j31 * j35) + j117;
        int i75 = ((int) j118) & M28;
        long j119 = (j116 >>> 28) + (((((j68 * j43) + (j41 * j38)) + (j45 * j69)) + (j52 * j87)) - j117);
        int i76 = ((int) j119) & M28;
        long j120 = j119 >>> 28;
        long j121 = (j118 >>> 28) + j120 + ((long) i57);
        int i77 = ((int) j121) & M28;
        long j122 = j120 + ((long) i58);
        iArr2[0] = ((int) j122) & M28;
        iArr2[1] = i61 + ((int) (j122 >>> 28));
        iArr2[2] = i64;
        iArr2[3] = i70;
        iArr2[4] = i67;
        iArr2[5] = i71;
        iArr2[6] = i73;
        iArr2[7] = i75;
        iArr2[8] = i77;
        iArr2[9] = i60 + ((int) (j121 >>> 28));
        iArr2[10] = i63;
        iArr2[11] = i69;
        iArr2[12] = i68;
        iArr2[13] = i72;
        iArr2[14] = i74;
        iArr2[15] = i76;
    }

    public static boolean sqrtRatioVar(int[] iArr, int[] iArr2, int[] iArr3) {
        int[] create = create();
        int[] create2 = create();
        sqr(iArr, create);
        mul(create, iArr2, create);
        sqr(create, create2);
        mul(create, iArr, create);
        mul(create2, iArr, create2);
        mul(create2, iArr2, create2);
        int[] create3 = create();
        powPm3d4(create2, create3);
        mul(create3, create, create3);
        int[] create4 = create();
        sqr(create3, create4);
        mul(create4, iArr2, create4);
        sub(iArr, create4, create4);
        normalize(create4);
        if (!isZeroVar(create4)) {
            return false;
        }
        copy(create3, 0, iArr3, 0);
        return true;
    }

    public static void sub(int[] iArr, int[] iArr2, int[] iArr3) {
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
        int i11 = iArr[10];
        int i12 = iArr[11];
        int i13 = iArr[12];
        int i14 = iArr[13];
        int i15 = iArr[14];
        int i16 = iArr[15];
        int i17 = iArr2[0];
        int i18 = iArr2[1];
        int i19 = iArr2[2];
        int i20 = iArr2[3];
        int i21 = iArr2[4];
        int i22 = iArr2[5];
        int i23 = iArr2[6];
        int i24 = iArr2[7];
        int i25 = iArr2[8];
        int i26 = iArr2[9];
        int i27 = iArr2[10];
        int i28 = iArr2[11];
        int i29 = iArr2[12];
        int i30 = iArr2[13];
        int i31 = iArr2[14];
        int i32 = (i + 536870910) - i17;
        int i33 = (i2 + 536870910) - i18;
        int i34 = (i6 + 536870910) - i22;
        int i35 = (i10 + 536870910) - i26;
        int i36 = (i14 + 536870910) - i30;
        int i37 = (i16 + 536870910) - iArr2[15];
        int i38 = ((i3 + 536870910) - i19) + (i33 >>> 28);
        int i39 = i33 & M28;
        int i40 = ((i7 + 536870910) - i23) + (i34 >>> 28);
        int i41 = i34 & M28;
        int i42 = ((i11 + 536870910) - i27) + (i35 >>> 28);
        int i43 = i35 & M28;
        int i44 = ((i15 + 536870910) - i31) + (i36 >>> 28);
        int i45 = i36 & M28;
        int i46 = ((i4 + 536870910) - i20) + (i38 >>> 28);
        int i47 = i38 & M28;
        int i48 = ((i8 + 536870910) - i24) + (i40 >>> 28);
        int i49 = i40 & M28;
        int i50 = ((i12 + 536870910) - i28) + (i42 >>> 28);
        int i51 = i42 & M28;
        int i52 = i37 + (i44 >>> 28);
        int i53 = i44 & M28;
        int i54 = i52 >>> 28;
        int i55 = i52 & M28;
        int i56 = i32 + i54;
        int i57 = ((i5 + 536870910) - i21) + (i46 >>> 28);
        int i58 = i46 & M28;
        int i59 = ((i9 + 536870908) - i25) + i54 + (i48 >>> 28);
        int i60 = i48 & M28;
        int i61 = ((i13 + 536870910) - i29) + (i50 >>> 28);
        int i62 = i50 & M28;
        int i63 = i39 + (i56 >>> 28);
        int i64 = i56 & M28;
        int i65 = i41 + (i57 >>> 28);
        int i66 = i57 & M28;
        int i67 = i43 + (i59 >>> 28);
        int i68 = i59 & M28;
        int i69 = i61 & M28;
        iArr3[0] = i64;
        iArr3[1] = i63;
        iArr3[2] = i47;
        iArr3[3] = i58;
        iArr3[4] = i66;
        iArr3[5] = i65;
        iArr3[6] = i49;
        iArr3[7] = i60;
        iArr3[8] = i68;
        iArr3[9] = i67;
        iArr3[10] = i51;
        iArr3[11] = i62;
        iArr3[12] = i69;
        iArr3[13] = i45 + (i61 >>> 28);
        iArr3[14] = i53;
        iArr3[15] = i55;
    }

    public static void subOne(int[] iArr) {
        int[] create = create();
        create[0] = 1;
        sub(iArr, create, iArr);
    }

    public static void zero(int[] iArr) {
        for (int i = 0; i < 16; i++) {
            iArr[i] = 0;
        }
    }
}
