package org.bouncycastle.pqc.crypto.sphincs;

class Wots {
    static final int WOTS_L = 67;
    static final int WOTS_L1 = 64;
    static final int WOTS_LOGW = 4;
    static final int WOTS_LOG_L = 7;
    static final int WOTS_SIGBYTES = 2144;
    static final int WOTS_W = 16;

    Wots() {
    }

    private static void clear(byte[] bArr, int i, int i2) {
        for (int i3 = 0; i3 != i2; i3++) {
            bArr[i3 + i] = 0;
        }
    }

    static void expand_seed(byte[] bArr, int i, byte[] bArr2, int i2) {
        clear(bArr, i, WOTS_SIGBYTES);
        Seed.prg(bArr, i, 2144, bArr2, i2);
    }

    static void gen_chain(HashFunctions hashFunctions, byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, int i3, int i4) {
        int i5 = 0;
        for (int i6 = 0; i6 < 32; i6++) {
            bArr[i6 + i] = bArr2[i6 + i2];
        }
        while (i5 < i4 && i5 < 16) {
            hashFunctions.hash_n_n_mask(bArr, i, bArr, i, bArr3, i3 + (i5 * 32));
            i5++;
        }
    }

    /* access modifiers changed from: package-private */
    public void wots_pkgen(HashFunctions hashFunctions, byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, int i3) {
        expand_seed(bArr, i, bArr2, i2);
        for (int i4 = 0; i4 < 67; i4++) {
            int i5 = i + (i4 * 32);
            gen_chain(hashFunctions, bArr, i5, bArr, i5, bArr3, i3, 15);
        }
    }

    /* access modifiers changed from: package-private */
    public void wots_sign(HashFunctions hashFunctions, byte[] bArr, int i, byte[] bArr2, byte[] bArr3, byte[] bArr4) {
        int i2 = i;
        int[] iArr = new int[67];
        int i3 = 0;
        int i4 = 0;
        while (i3 < 64) {
            int i5 = i3 / 2;
            iArr[i3] = bArr2[i5] & 15;
            int i6 = i3 + 1;
            iArr[i6] = (bArr2[i5] & 255) >>> 4;
            i4 = i4 + (15 - iArr[i3]) + (15 - iArr[i6]);
            i3 += 2;
        }
        while (i3 < 67) {
            iArr[i3] = i4 & 15;
            i4 >>>= 4;
            i3++;
        }
        byte[] bArr5 = bArr;
        expand_seed(bArr5, i2, bArr3, 0);
        for (int i7 = 0; i7 < 67; i7++) {
            int i8 = i2 + (i7 * 32);
            gen_chain(hashFunctions, bArr5, i8, bArr5, i8, bArr4, 0, iArr[i7]);
        }
    }

    /* access modifiers changed from: package-private */
    public void wots_verify(HashFunctions hashFunctions, byte[] bArr, byte[] bArr2, int i, byte[] bArr3, byte[] bArr4) {
        int[] iArr = new int[67];
        int i2 = 0;
        int i3 = 0;
        while (i2 < 64) {
            int i4 = i2 / 2;
            iArr[i2] = bArr3[i4] & 15;
            int i5 = i2 + 1;
            iArr[i5] = (bArr3[i4] & 255) >>> 4;
            i3 = i3 + (15 - iArr[i2]) + (15 - iArr[i5]);
            i2 += 2;
        }
        while (i2 < 67) {
            iArr[i2] = i3 & 15;
            i3 >>>= 4;
            i2++;
        }
        for (int i6 = 0; i6 < 67; i6++) {
            int i7 = i6 * 32;
            gen_chain(hashFunctions, bArr, i7, bArr2, i + i7, bArr4, iArr[i6] * 32, 15 - iArr[i6]);
        }
    }
}
