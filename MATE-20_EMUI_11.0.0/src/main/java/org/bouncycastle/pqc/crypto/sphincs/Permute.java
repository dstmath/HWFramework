package org.bouncycastle.pqc.crypto.sphincs;

import org.bouncycastle.util.Pack;

class Permute {
    private static final int CHACHA_ROUNDS = 12;

    Permute() {
    }

    public static void permute(int i, int[] iArr) {
        int i2 = 16;
        if (iArr.length != 16) {
            throw new IllegalArgumentException();
        } else if (i % 2 == 0) {
            char c = 0;
            int i3 = iArr[0];
            int i4 = iArr[1];
            int i5 = iArr[2];
            int i6 = iArr[3];
            int i7 = iArr[4];
            int i8 = iArr[5];
            int i9 = iArr[6];
            int i10 = 7;
            int i11 = iArr[7];
            int i12 = 8;
            int i13 = iArr[8];
            int i14 = iArr[9];
            int i15 = iArr[10];
            int i16 = iArr[11];
            int i17 = iArr[12];
            int i18 = iArr[13];
            int i19 = iArr[14];
            int i20 = iArr[15];
            int i21 = i19;
            int i22 = i18;
            int i23 = i17;
            int i24 = i16;
            int i25 = i15;
            int i26 = i14;
            int i27 = i13;
            int i28 = i11;
            int i29 = i9;
            int i30 = i8;
            int i31 = i7;
            int i32 = i6;
            int i33 = i5;
            int i34 = i4;
            int i35 = i3;
            int i36 = i;
            while (i36 > 0) {
                int i37 = i35 + i31;
                int rotl = rotl(i23 ^ i37, i2);
                int i38 = i27 + rotl;
                int rotl2 = rotl(i31 ^ i38, 12);
                int i39 = i37 + rotl2;
                int rotl3 = rotl(rotl ^ i39, i12);
                int i40 = i38 + rotl3;
                int rotl4 = rotl(rotl2 ^ i40, i10);
                int i41 = i34 + i30;
                int rotl5 = rotl(i22 ^ i41, i2);
                int i42 = i26 + rotl5;
                int rotl6 = rotl(i30 ^ i42, 12);
                int i43 = i41 + rotl6;
                int rotl7 = rotl(rotl5 ^ i43, i12);
                int i44 = i42 + rotl7;
                int rotl8 = rotl(rotl6 ^ i44, i10);
                int i45 = i33 + i29;
                int rotl9 = rotl(i21 ^ i45, i2);
                int i46 = i25 + rotl9;
                int rotl10 = rotl(i29 ^ i46, 12);
                int i47 = i45 + rotl10;
                int rotl11 = rotl(rotl9 ^ i47, i12);
                int i48 = i46 + rotl11;
                int rotl12 = rotl(rotl10 ^ i48, i10);
                int i49 = i32 + i28;
                int rotl13 = rotl(i20 ^ i49, i2);
                int i50 = i24 + rotl13;
                int rotl14 = rotl(i28 ^ i50, 12);
                int i51 = i49 + rotl14;
                int rotl15 = rotl(rotl13 ^ i51, i12);
                int i52 = i50 + rotl15;
                int rotl16 = rotl(rotl14 ^ i52, 7);
                int i53 = i39 + rotl8;
                int rotl17 = rotl(rotl15 ^ i53, 16);
                int i54 = i48 + rotl17;
                int rotl18 = rotl(rotl8 ^ i54, 12);
                i35 = i53 + rotl18;
                i20 = rotl(rotl17 ^ i35, 8);
                i25 = i54 + i20;
                i30 = rotl(rotl18 ^ i25, 7);
                int i55 = i43 + rotl12;
                int rotl19 = rotl(rotl3 ^ i55, 16);
                int i56 = i52 + rotl19;
                int rotl20 = rotl(rotl12 ^ i56, 12);
                i34 = i55 + rotl20;
                i23 = rotl(rotl19 ^ i34, 8);
                i24 = i56 + i23;
                i29 = rotl(rotl20 ^ i24, 7);
                int i57 = i47 + rotl16;
                int rotl21 = rotl(rotl7 ^ i57, 16);
                int i58 = i40 + rotl21;
                int rotl22 = rotl(rotl16 ^ i58, 12);
                i33 = i57 + rotl22;
                i22 = rotl(rotl21 ^ i33, 8);
                i27 = i58 + i22;
                i28 = rotl(rotl22 ^ i27, 7);
                int i59 = i51 + rotl4;
                i2 = 16;
                int rotl23 = rotl(rotl11 ^ i59, 16);
                int i60 = i44 + rotl23;
                int rotl24 = rotl(rotl4 ^ i60, 12);
                i32 = i59 + rotl24;
                i21 = rotl(rotl23 ^ i32, 8);
                i26 = i60 + i21;
                i31 = rotl(rotl24 ^ i26, 7);
                i36 -= 2;
                i10 = 7;
                c = 0;
                i12 = 8;
            }
            iArr[c] = i35;
            iArr[1] = i34;
            iArr[2] = i33;
            iArr[3] = i32;
            iArr[4] = i31;
            iArr[5] = i30;
            iArr[6] = i29;
            iArr[i10] = i28;
            iArr[8] = i27;
            iArr[9] = i26;
            iArr[10] = i25;
            iArr[11] = i24;
            iArr[12] = i23;
            iArr[13] = i22;
            iArr[14] = i21;
            iArr[15] = i20;
        } else {
            throw new IllegalArgumentException("Number of rounds must be even");
        }
    }

    protected static int rotl(int i, int i2) {
        return (i >>> (-i2)) | (i << i2);
    }

    /* access modifiers changed from: package-private */
    public void chacha_permute(byte[] bArr, byte[] bArr2) {
        int[] iArr = new int[16];
        for (int i = 0; i < 16; i++) {
            iArr[i] = Pack.littleEndianToInt(bArr2, i * 4);
        }
        permute(12, iArr);
        for (int i2 = 0; i2 < 16; i2++) {
            Pack.intToLittleEndian(iArr[i2], bArr, i2 * 4);
        }
    }
}
