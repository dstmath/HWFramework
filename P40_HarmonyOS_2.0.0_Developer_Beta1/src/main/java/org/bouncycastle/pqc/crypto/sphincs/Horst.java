package org.bouncycastle.pqc.crypto.sphincs;

import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.crypto.digests.Blake2xsDigest;

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
            hashFunctions.hash_n_n(bArr7, (Blake2xsDigest.UNKNOWN_DIGEST_LENGTH + i2) * 32, bArr6, i2 * 32);
        }
        for (int i3 = 0; i3 < 16; i3++) {
            int i4 = 16 - i3;
            long j = (long) ((1 << i4) - 1);
            int i5 = 1 << (i4 - 1);
            long j2 = (long) (i5 - 1);
            int i6 = 0;
            while (i6 < i5) {
                hashFunctions.hash_2n_n_mask(bArr7, (int) ((((long) i6) + j2) * 32), bArr7, (int) ((((long) (i6 * 2)) + j) * 32), bArr4, i3 * 2 * 32);
                i6++;
                i5 = i5;
                j2 = j2;
            }
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
            int i10 = i9 * 2;
            int i11 = (bArr5[i10] & 255) + ((bArr5[i10 + 1] & 255) << 8);
            int i12 = i8;
            int i13 = 0;
            while (i13 < 32) {
                bArr[i12] = bArr6[(i11 * 32) + i13];
                i13++;
                i12++;
            }
            int i14 = i11 + Blake2xsDigest.UNKNOWN_DIGEST_LENGTH;
            int i15 = 0;
            while (i15 < 10) {
                int i16 = (i14 & 1) != 0 ? i14 + 1 : i14 - 1;
                int i17 = i12;
                int i18 = 0;
                while (i18 < 32) {
                    bArr[i17] = bArr7[(i16 * 32) + i18];
                    i18++;
                    i17++;
                }
                i14 = (i16 - 1) / 2;
                i15++;
                i12 = i17;
            }
            i9++;
            i8 = i12;
        }
        for (int i19 = 0; i19 < 32; i19++) {
            bArr2[i19] = bArr7[i19];
        }
        return HORST_SIGBYTES;
    }

    static int horst_verify(HashFunctions hashFunctions, byte[] bArr, byte[] bArr2, int i, byte[] bArr3, byte[] bArr4) {
        byte[] bArr5 = new byte[1024];
        int i2 = i + 2048;
        int i3 = 0;
        while (i3 < 32) {
            int i4 = i3 * 2;
            int i5 = (bArr4[i4] & 255) + ((bArr4[i4 + 1] & 255) << 8);
            if ((i5 & 1) == 0) {
                hashFunctions.hash_n_n(bArr5, 0, bArr2, i2);
                for (int i6 = 0; i6 < 32; i6++) {
                    bArr5[i6 + 32] = bArr2[i2 + 32 + i6];
                }
            } else {
                hashFunctions.hash_n_n(bArr5, 32, bArr2, i2);
                for (int i7 = 0; i7 < 32; i7++) {
                    bArr5[i7] = bArr2[i2 + 32 + i7];
                }
            }
            int i8 = i2 + 64;
            int i9 = 1;
            while (i9 < 10) {
                int i10 = i5 >>> 1;
                if ((i10 & 1) == 0) {
                    hashFunctions.hash_2n_n_mask(bArr5, 0, bArr5, 0, bArr3, (i9 - 1) * 2 * 32);
                    for (int i11 = 0; i11 < 32; i11++) {
                        bArr5[i11 + 32] = bArr2[i8 + i11];
                    }
                } else {
                    hashFunctions.hash_2n_n_mask(bArr5, 32, bArr5, 0, bArr3, (i9 - 1) * 2 * 32);
                    for (int i12 = 0; i12 < 32; i12++) {
                        bArr5[i12] = bArr2[i8 + i12];
                    }
                }
                i8 += 32;
                i9++;
                i5 = i10;
            }
            int i13 = i5 >>> 1;
            hashFunctions.hash_2n_n_mask(bArr5, 0, bArr5, 0, bArr3, 576);
            for (int i14 = 0; i14 < 32; i14++) {
                if (bArr2[(i13 * 32) + i + i14] != bArr5[i14]) {
                    for (int i15 = 0; i15 < 32; i15++) {
                        bArr[i15] = 0;
                    }
                    return -1;
                }
            }
            i3++;
            i2 = i8;
        }
        for (int i16 = 0; i16 < 32; i16++) {
            hashFunctions.hash_2n_n_mask(bArr5, i16 * 32, bArr2, i + (i16 * 2 * 32), bArr3, 640);
        }
        for (int i17 = 0; i17 < 16; i17++) {
            hashFunctions.hash_2n_n_mask(bArr5, i17 * 32, bArr5, i17 * 2 * 32, bArr3, 704);
        }
        for (int i18 = 0; i18 < 8; i18++) {
            hashFunctions.hash_2n_n_mask(bArr5, i18 * 32, bArr5, i18 * 2 * 32, bArr3, 768);
        }
        for (int i19 = 0; i19 < 4; i19++) {
            hashFunctions.hash_2n_n_mask(bArr5, i19 * 32, bArr5, i19 * 2 * 32, bArr3, 832);
        }
        for (int i20 = 0; i20 < 2; i20++) {
            hashFunctions.hash_2n_n_mask(bArr5, i20 * 32, bArr5, i20 * 2 * 32, bArr3, 896);
        }
        hashFunctions.hash_2n_n_mask(bArr, 0, bArr5, 0, bArr3, 960);
        return 0;
    }
}
