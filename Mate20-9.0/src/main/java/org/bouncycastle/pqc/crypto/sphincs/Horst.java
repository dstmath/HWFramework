package org.bouncycastle.pqc.crypto.sphincs;

import org.bouncycastle.asn1.cmp.PKIFailureInfo;

class Horst {
    static final int HORST_K = 32;
    static final int HORST_LOGT = 16;
    static final int HORST_SIGBYTES = 13312;
    static final int HORST_SKBYTES = 32;
    static final int HORST_T = 65536;
    static final int N_MASKS = 32;

    Horst() {
    }

    static void expand_seed(byte[] bArr, byte[] bArr2) {
        Seed.prg(bArr, 0, 2097152, bArr2, 0);
    }

    static int horst_sign(HashFunctions hashFunctions, byte[] bArr, int i, byte[] bArr2, byte[] bArr3, byte[] bArr4, byte[] bArr5) {
        byte[] bArr6 = new byte[PKIFailureInfo.badSenderNonce];
        byte[] bArr7 = new byte[4194272];
        expand_seed(bArr6, bArr3);
        for (int i2 = 0; i2 < 65536; i2++) {
            hashFunctions.hash_n_n(bArr7, (65535 + i2) * 32, bArr6, i2 * 32);
        }
        HashFunctions hashFunctions2 = hashFunctions;
        int i3 = 0;
        while (i3 < 16) {
            int i4 = 16 - i3;
            long j = (long) ((1 << i4) - 1);
            int i5 = 1 << (i4 - 1);
            long j2 = (long) (i5 - 1);
            int i6 = 0;
            while (i6 < i5) {
                hashFunctions.hash_2n_n_mask(bArr7, (int) ((((long) i6) + j2) * 32), bArr7, (int) ((((long) (2 * i6)) + j) * 32), bArr4, 2 * i3 * 32);
                i6++;
                HashFunctions hashFunctions3 = hashFunctions;
                i5 = i5;
                j2 = j2;
                j = j;
            }
            i3++;
            HashFunctions hashFunctions4 = hashFunctions;
        }
        int i7 = 2016;
        int i8 = i;
        while (i7 < 4064) {
            bArr[i8] = bArr7[i7];
            i7++;
            i8++;
        }
        int i9 = 0;
        while (i9 < 32) {
            int i10 = 2 * i9;
            int i11 = (bArr5[i10] & 255) + ((bArr5[i10 + 1] & 255) << 8);
            int i12 = i8;
            int i13 = 0;
            while (i13 < 32) {
                bArr[i12] = bArr6[(i11 * 32) + i13];
                i13++;
                i12++;
            }
            int i14 = i11 + 65535;
            int i15 = i12;
            int i16 = 0;
            while (i16 < 10) {
                int i17 = (i14 & 1) != 0 ? i14 + 1 : i14 - 1;
                int i18 = i15;
                int i19 = 0;
                while (i19 < 32) {
                    bArr[i18] = bArr7[(i17 * 32) + i19];
                    i19++;
                    i18++;
                }
                i14 = (i17 - 1) / 2;
                i16++;
                i15 = i18;
            }
            i9++;
            i8 = i15;
        }
        for (int i20 = 0; i20 < 32; i20++) {
            bArr2[i20] = bArr7[i20];
        }
        return HORST_SIGBYTES;
    }

    static int horst_verify(HashFunctions hashFunctions, byte[] bArr, byte[] bArr2, int i, byte[] bArr3, byte[] bArr4) {
        int i2;
        HashFunctions hashFunctions2 = hashFunctions;
        byte[] bArr5 = bArr2;
        int i3 = i;
        byte[] bArr6 = new byte[1024];
        int i4 = i3 + 2048;
        int i5 = 0;
        int i6 = 0;
        while (true) {
            int i7 = 32;
            if (i6 < 32) {
                int i8 = 2 * i6;
                int i9 = (bArr4[i8] & 255) + ((bArr4[i8 + 1] & 255) << 8);
                if ((i9 & 1) == 0) {
                    hashFunctions2.hash_n_n(bArr6, i5, bArr5, i4);
                    for (int i10 = i5; i10 < 32; i10++) {
                        bArr6[32 + i10] = bArr5[i4 + 32 + i10];
                    }
                } else {
                    hashFunctions2.hash_n_n(bArr6, 32, bArr5, i4);
                    for (int i11 = i5; i11 < 32; i11++) {
                        bArr6[i11] = bArr5[i4 + 32 + i11];
                    }
                }
                int i12 = i4 + 64;
                int i13 = 1;
                while (i13 < 10) {
                    int i14 = i9 >>> 1;
                    if ((i14 & 1) == 0) {
                        i2 = i7;
                        hashFunctions2.hash_2n_n_mask(bArr6, 0, bArr6, 0, bArr3, (i13 - 1) * 2 * 32);
                        for (int i15 = 0; i15 < i2; i15++) {
                            bArr6[i2 + i15] = bArr5[i12 + i15];
                        }
                    } else {
                        i2 = i7;
                        hashFunctions2.hash_2n_n_mask(bArr6, 32, bArr6, 0, bArr3, (i13 - 1) * 2 * 32);
                        for (int i16 = 0; i16 < i2; i16++) {
                            bArr6[i16] = bArr5[i12 + i16];
                        }
                    }
                    i12 += 32;
                    i13++;
                    i7 = i2;
                    i9 = i14;
                }
                int i17 = i7;
                int i18 = i9 >>> 1;
                hashFunctions2.hash_2n_n_mask(bArr6, 0, bArr6, 0, bArr3, 576);
                for (int i19 = 0; i19 < i17; i19++) {
                    if (bArr5[i3 + (i18 * 32) + i19] != bArr6[i19]) {
                        for (int i20 = 0; i20 < i17; i20++) {
                            bArr[i20] = 0;
                        }
                        return -1;
                    }
                }
                i6++;
                i4 = i12;
                i5 = 0;
            } else {
                for (int i21 = 0; i21 < 32; i21++) {
                    hashFunctions2.hash_2n_n_mask(bArr6, i21 * 32, bArr5, i3 + (2 * i21 * 32), bArr3, 640);
                }
                for (int i22 = 0; i22 < 16; i22++) {
                    hashFunctions2.hash_2n_n_mask(bArr6, i22 * 32, bArr6, 2 * i22 * 32, bArr3, 704);
                }
                for (int i23 = 0; i23 < 8; i23++) {
                    hashFunctions2.hash_2n_n_mask(bArr6, i23 * 32, bArr6, 2 * i23 * 32, bArr3, 768);
                }
                for (int i24 = 0; i24 < 4; i24++) {
                    hashFunctions2.hash_2n_n_mask(bArr6, i24 * 32, bArr6, 2 * i24 * 32, bArr3, 832);
                }
                for (int i25 = 0; i25 < 2; i25++) {
                    hashFunctions2.hash_2n_n_mask(bArr6, i25 * 32, bArr6, 2 * i25 * 32, bArr3, 896);
                }
                hashFunctions2.hash_2n_n_mask(bArr, 0, bArr6, 0, bArr3, 960);
                return 0;
            }
        }
    }
}
