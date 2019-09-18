package org.bouncycastle.crypto.engines;

public final class CAST6Engine extends CAST5Engine {
    protected static final int BLOCK_SIZE = 16;
    protected static final int ROUNDS = 12;
    protected int[] _Km = new int[48];
    protected int[] _Kr = new int[48];
    protected int[] _Tm = new int[192];
    protected int[] _Tr = new int[192];
    private int[] _workingKey = new int[8];

    /* access modifiers changed from: protected */
    public final void CAST_Decipher(int i, int i2, int i3, int i4, int[] iArr) {
        int i5;
        int i6 = i;
        int i7 = 0;
        while (true) {
            if (i7 >= 6) {
                break;
            }
            int i8 = (11 - i7) * 4;
            i3 ^= F1(i4, this._Km[i8], this._Kr[i8]);
            int i9 = i8 + 1;
            i2 ^= F2(i3, this._Km[i9], this._Kr[i9]);
            int i10 = i8 + 2;
            i6 ^= F3(i2, this._Km[i10], this._Kr[i10]);
            int i11 = i8 + 3;
            i4 ^= F1(i6, this._Km[i11], this._Kr[i11]);
            i7++;
        }
        for (i5 = 6; i5 < 12; i5++) {
            int i12 = (11 - i5) * 4;
            int i13 = i12 + 3;
            i4 ^= F1(i6, this._Km[i13], this._Kr[i13]);
            int i14 = i12 + 2;
            i6 ^= F3(i2, this._Km[i14], this._Kr[i14]);
            int i15 = i12 + 1;
            i2 ^= F2(i3, this._Km[i15], this._Kr[i15]);
            i3 ^= F1(i4, this._Km[i12], this._Kr[i12]);
        }
        iArr[0] = i6;
        iArr[1] = i2;
        iArr[2] = i3;
        iArr[3] = i4;
    }

    /* access modifiers changed from: protected */
    public final void CAST_Encipher(int i, int i2, int i3, int i4, int[] iArr) {
        int i5;
        int i6 = i;
        int i7 = 0;
        while (true) {
            if (i7 >= 6) {
                break;
            }
            int i8 = i7 * 4;
            i3 ^= F1(i4, this._Km[i8], this._Kr[i8]);
            int i9 = i8 + 1;
            i2 ^= F2(i3, this._Km[i9], this._Kr[i9]);
            int i10 = i8 + 2;
            i6 ^= F3(i2, this._Km[i10], this._Kr[i10]);
            int i11 = i8 + 3;
            i4 ^= F1(i6, this._Km[i11], this._Kr[i11]);
            i7++;
        }
        for (i5 = 6; i5 < 12; i5++) {
            int i12 = i5 * 4;
            int i13 = i12 + 3;
            i4 ^= F1(i6, this._Km[i13], this._Kr[i13]);
            int i14 = i12 + 2;
            i6 ^= F3(i2, this._Km[i14], this._Kr[i14]);
            int i15 = i12 + 1;
            i2 ^= F2(i3, this._Km[i15], this._Kr[i15]);
            i3 ^= F1(i4, this._Km[i12], this._Kr[i12]);
        }
        iArr[0] = i6;
        iArr[1] = i2;
        iArr[2] = i3;
        iArr[3] = i4;
    }

    /* access modifiers changed from: protected */
    public int decryptBlock(byte[] bArr, int i, byte[] bArr2, int i2) {
        int[] iArr = new int[4];
        CAST_Decipher(BytesTo32bits(bArr, i), BytesTo32bits(bArr, i + 4), BytesTo32bits(bArr, i + 8), BytesTo32bits(bArr, i + 12), iArr);
        Bits32ToBytes(iArr[0], bArr2, i2);
        Bits32ToBytes(iArr[1], bArr2, i2 + 4);
        Bits32ToBytes(iArr[2], bArr2, i2 + 8);
        Bits32ToBytes(iArr[3], bArr2, i2 + 12);
        return 16;
    }

    /* access modifiers changed from: protected */
    public int encryptBlock(byte[] bArr, int i, byte[] bArr2, int i2) {
        int[] iArr = new int[4];
        CAST_Encipher(BytesTo32bits(bArr, i), BytesTo32bits(bArr, i + 4), BytesTo32bits(bArr, i + 8), BytesTo32bits(bArr, i + 12), iArr);
        Bits32ToBytes(iArr[0], bArr2, i2);
        Bits32ToBytes(iArr[1], bArr2, i2 + 4);
        Bits32ToBytes(iArr[2], bArr2, i2 + 8);
        Bits32ToBytes(iArr[3], bArr2, i2 + 12);
        return 16;
    }

    public String getAlgorithmName() {
        return "CAST6";
    }

    public int getBlockSize() {
        return 16;
    }

    public void reset() {
    }

    /* access modifiers changed from: protected */
    public void setKey(byte[] bArr) {
        byte[] bArr2 = bArr;
        int i = 19;
        int i2 = 1518500249;
        int i3 = 0;
        while (i3 < 24) {
            int i4 = i;
            int i5 = i2;
            for (int i6 = 0; i6 < 8; i6++) {
                int i7 = (i3 * 8) + i6;
                this._Tm[i7] = i5;
                i5 += 1859775393;
                this._Tr[i7] = i4;
                i4 = (i4 + 17) & 31;
            }
            i3++;
            i2 = i5;
            i = i4;
        }
        byte[] bArr3 = new byte[64];
        System.arraycopy(bArr2, 0, bArr3, 0, bArr2.length);
        for (int i8 = 0; i8 < 8; i8++) {
            this._workingKey[i8] = BytesTo32bits(bArr3, i8 * 4);
        }
        for (int i9 = 0; i9 < 12; i9++) {
            int i10 = i9 * 2;
            int i11 = i10 * 8;
            int[] iArr = this._workingKey;
            iArr[6] = iArr[6] ^ F1(this._workingKey[7], this._Tm[i11], this._Tr[i11]);
            int[] iArr2 = this._workingKey;
            int i12 = i11 + 1;
            iArr2[5] = iArr2[5] ^ F2(this._workingKey[6], this._Tm[i12], this._Tr[i12]);
            int[] iArr3 = this._workingKey;
            int i13 = i11 + 2;
            iArr3[4] = iArr3[4] ^ F3(this._workingKey[5], this._Tm[i13], this._Tr[i13]);
            int[] iArr4 = this._workingKey;
            int i14 = i11 + 3;
            iArr4[3] = F1(this._workingKey[4], this._Tm[i14], this._Tr[i14]) ^ iArr4[3];
            int[] iArr5 = this._workingKey;
            int i15 = i11 + 4;
            iArr5[2] = F2(this._workingKey[3], this._Tm[i15], this._Tr[i15]) ^ iArr5[2];
            int[] iArr6 = this._workingKey;
            int i16 = i11 + 5;
            iArr6[1] = F3(this._workingKey[2], this._Tm[i16], this._Tr[i16]) ^ iArr6[1];
            int[] iArr7 = this._workingKey;
            int i17 = i11 + 6;
            iArr7[0] = iArr7[0] ^ F1(this._workingKey[1], this._Tm[i17], this._Tr[i17]);
            int[] iArr8 = this._workingKey;
            int i18 = i11 + 7;
            iArr8[7] = F2(this._workingKey[0], this._Tm[i18], this._Tr[i18]) ^ iArr8[7];
            int i19 = (i10 + 1) * 8;
            int[] iArr9 = this._workingKey;
            iArr9[6] = iArr9[6] ^ F1(this._workingKey[7], this._Tm[i19], this._Tr[i19]);
            int[] iArr10 = this._workingKey;
            int i20 = i19 + 1;
            iArr10[5] = iArr10[5] ^ F2(this._workingKey[6], this._Tm[i20], this._Tr[i20]);
            int[] iArr11 = this._workingKey;
            int i21 = i19 + 2;
            iArr11[4] = iArr11[4] ^ F3(this._workingKey[5], this._Tm[i21], this._Tr[i21]);
            int[] iArr12 = this._workingKey;
            int i22 = i19 + 3;
            iArr12[3] = F1(this._workingKey[4], this._Tm[i22], this._Tr[i22]) ^ iArr12[3];
            int[] iArr13 = this._workingKey;
            int i23 = i19 + 4;
            iArr13[2] = F2(this._workingKey[3], this._Tm[i23], this._Tr[i23]) ^ iArr13[2];
            int[] iArr14 = this._workingKey;
            int i24 = i19 + 5;
            iArr14[1] = F3(this._workingKey[2], this._Tm[i24], this._Tr[i24]) ^ iArr14[1];
            int[] iArr15 = this._workingKey;
            int i25 = i19 + 6;
            iArr15[0] = iArr15[0] ^ F1(this._workingKey[1], this._Tm[i25], this._Tr[i25]);
            int[] iArr16 = this._workingKey;
            int i26 = i19 + 7;
            iArr16[7] = F2(this._workingKey[0], this._Tm[i26], this._Tr[i26]) ^ iArr16[7];
            int i27 = i9 * 4;
            this._Kr[i27] = this._workingKey[0] & 31;
            int i28 = i27 + 1;
            this._Kr[i28] = this._workingKey[2] & 31;
            int i29 = i27 + 2;
            this._Kr[i29] = this._workingKey[4] & 31;
            int i30 = i27 + 3;
            this._Kr[i30] = this._workingKey[6] & 31;
            this._Km[i27] = this._workingKey[7];
            this._Km[i28] = this._workingKey[5];
            this._Km[i29] = this._workingKey[3];
            this._Km[i30] = this._workingKey[1];
        }
    }
}
