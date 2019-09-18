package org.bouncycastle.pqc.crypto.sphincs;

import org.bouncycastle.util.Pack;

class Permute {
    private static final int CHACHA_ROUNDS = 12;

    Permute() {
    }

    public static void permute(int i, int[] iArr) {
        int[] iArr2 = iArr;
        int i2 = 16;
        if (iArr2.length != 16) {
            throw new IllegalArgumentException();
        } else if (i % 2 == 0) {
            boolean z = false;
            int i3 = iArr2[0];
            int i4 = iArr2[1];
            int i5 = iArr2[2];
            int i6 = iArr2[3];
            int i7 = iArr2[4];
            int i8 = iArr2[5];
            int i9 = iArr2[6];
            int i10 = 7;
            int i11 = iArr2[7];
            int i12 = 8;
            int i13 = iArr2[8];
            int i14 = iArr2[9];
            int i15 = iArr2[10];
            int i16 = iArr2[11];
            int i17 = iArr2[12];
            int i18 = iArr2[13];
            int i19 = iArr2[14];
            int i20 = iArr2[15];
            int i21 = i;
            while (i21 > 0) {
                int i22 = i3 + i7;
                int rotl = rotl(i17 ^ i22, i2);
                int i23 = i13 + rotl;
                int rotl2 = rotl(i7 ^ i23, 12);
                int i24 = i22 + rotl2;
                int rotl3 = rotl(rotl ^ i24, i12);
                int i25 = i23 + rotl3;
                int rotl4 = rotl(rotl2 ^ i25, i10);
                int i26 = i4 + i8;
                int rotl5 = rotl(i18 ^ i26, i2);
                int i27 = i14 + rotl5;
                int rotl6 = rotl(i8 ^ i27, 12);
                int i28 = i26 + rotl6;
                int rotl7 = rotl(rotl5 ^ i28, i12);
                int i29 = i27 + rotl7;
                int rotl8 = rotl(rotl6 ^ i29, i10);
                int i30 = i5 + i9;
                int rotl9 = rotl(i19 ^ i30, i2);
                int i31 = i15 + rotl9;
                int rotl10 = rotl(i9 ^ i31, 12);
                int i32 = i30 + rotl10;
                int rotl11 = rotl(rotl9 ^ i32, i12);
                int i33 = i31 + rotl11;
                int rotl12 = rotl(rotl10 ^ i33, i10);
                int i34 = i6 + i11;
                int rotl13 = rotl(i20 ^ i34, i2);
                int i35 = i16 + rotl13;
                int rotl14 = rotl(i11 ^ i35, 12);
                int i36 = i34 + rotl14;
                int rotl15 = rotl(rotl13 ^ i36, i12);
                int i37 = i35 + rotl15;
                int rotl16 = rotl(rotl14 ^ i37, 7);
                int i38 = i24 + rotl8;
                int rotl17 = rotl(rotl15 ^ i38, 16);
                int i39 = i33 + rotl17;
                int rotl18 = rotl(rotl8 ^ i39, 12);
                i3 = i38 + rotl18;
                i20 = rotl(rotl17 ^ i3, 8);
                i15 = i39 + i20;
                int rotl19 = rotl(rotl18 ^ i15, 7);
                int i40 = i28 + rotl12;
                int rotl20 = rotl(rotl3 ^ i40, 16);
                int i41 = i37 + rotl20;
                int rotl21 = rotl(rotl12 ^ i41, 12);
                i4 = i40 + rotl21;
                i17 = rotl(rotl20 ^ i4, 8);
                i16 = i41 + i17;
                i9 = rotl(rotl21 ^ i16, 7);
                int i42 = i32 + rotl16;
                int rotl22 = rotl(rotl7 ^ i42, 16);
                int i43 = i25 + rotl22;
                int rotl23 = rotl(rotl16 ^ i43, 12);
                i5 = i42 + rotl23;
                i18 = rotl(rotl22 ^ i5, 8);
                i13 = i43 + i18;
                i11 = rotl(rotl23 ^ i13, 7);
                int i44 = i36 + rotl4;
                int rotl24 = rotl(rotl11 ^ i44, 16);
                int i45 = i29 + rotl24;
                int rotl25 = rotl(rotl4 ^ i45, 12);
                i6 = i44 + rotl25;
                i19 = rotl(rotl24 ^ i6, 8);
                i14 = i45 + i19;
                i7 = rotl(rotl25 ^ i14, 7);
                i21 -= 2;
                i2 = 16;
                i10 = 7;
                i8 = rotl19;
                z = false;
                i12 = 8;
            }
            iArr2[z] = i3;
            iArr2[1] = i4;
            iArr2[2] = i5;
            iArr2[3] = i6;
            iArr2[4] = i7;
            iArr2[5] = i8;
            iArr2[6] = i9;
            iArr2[i10] = i11;
            iArr2[8] = i13;
            iArr2[9] = i14;
            iArr2[10] = i15;
            iArr2[11] = i16;
            iArr2[12] = i17;
            iArr2[13] = i18;
            iArr2[14] = i19;
            iArr2[15] = i20;
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
            iArr[i] = Pack.littleEndianToInt(bArr2, 4 * i);
        }
        permute(12, iArr);
        for (int i2 = 0; i2 < 16; i2++) {
            Pack.intToLittleEndian(iArr[i2], bArr, 4 * i2);
        }
    }
}
