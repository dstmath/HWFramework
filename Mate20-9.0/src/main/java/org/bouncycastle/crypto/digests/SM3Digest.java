package org.bouncycastle.crypto.digests;

import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.Pack;

public class SM3Digest extends GeneralDigest {
    private static final int BLOCK_SIZE = 16;
    private static final int DIGEST_LENGTH = 32;
    private static final int[] T = new int[64];
    private int[] V = new int[8];
    private int[] W = new int[68];
    private int[] inwords = new int[16];
    private int xOff;

    static {
        int i;
        int i2 = 0;
        while (true) {
            if (i2 >= 16) {
                break;
            }
            T[i2] = (2043430169 >>> (32 - i2)) | (2043430169 << i2);
            i2++;
        }
        for (i = 16; i < 64; i++) {
            int i3 = i % 32;
            T[i] = (2055708042 >>> (32 - i3)) | (2055708042 << i3);
        }
    }

    public SM3Digest() {
        reset();
    }

    public SM3Digest(SM3Digest sM3Digest) {
        super((GeneralDigest) sM3Digest);
        copyIn(sM3Digest);
    }

    private int FF0(int i, int i2, int i3) {
        return (i ^ i2) ^ i3;
    }

    private int FF1(int i, int i2, int i3) {
        return (i & i3) | (i & i2) | (i2 & i3);
    }

    private int GG0(int i, int i2, int i3) {
        return (i ^ i2) ^ i3;
    }

    private int GG1(int i, int i2, int i3) {
        return ((~i) & i3) | (i2 & i);
    }

    private int P0(int i) {
        return (i ^ ((i << 9) | (i >>> 23))) ^ ((i << 17) | (i >>> 15));
    }

    private int P1(int i) {
        return (i ^ ((i << 15) | (i >>> 17))) ^ ((i << 23) | (i >>> 9));
    }

    private void copyIn(SM3Digest sM3Digest) {
        System.arraycopy(sM3Digest.V, 0, this.V, 0, this.V.length);
        System.arraycopy(sM3Digest.inwords, 0, this.inwords, 0, this.inwords.length);
        this.xOff = sM3Digest.xOff;
    }

    public Memoable copy() {
        return new SM3Digest(this);
    }

    public int doFinal(byte[] bArr, int i) {
        finish();
        Pack.intToBigEndian(this.V, bArr, i);
        reset();
        return 32;
    }

    public String getAlgorithmName() {
        return "SM3";
    }

    public int getDigestSize() {
        return 32;
    }

    /* access modifiers changed from: protected */
    public void processBlock() {
        int i;
        int i2 = 0;
        while (true) {
            if (i2 >= 16) {
                break;
            }
            this.W[i2] = this.inwords[i2];
            i2++;
        }
        for (int i3 = 16; i3 < 68; i3++) {
            int i4 = this.W[i3 - 3];
            int i5 = this.W[i3 - 13];
            this.W[i3] = (P1(((i4 >>> 17) | (i4 << 15)) ^ (this.W[i3 - 16] ^ this.W[i3 - 9])) ^ ((i5 >>> 25) | (i5 << 7))) ^ this.W[i3 - 6];
        }
        int i6 = this.V[0];
        int i7 = this.V[1];
        int i8 = this.V[2];
        int i9 = this.V[3];
        int i10 = this.V[4];
        int i11 = this.V[5];
        int i12 = this.V[6];
        int i13 = this.V[7];
        int i14 = i12;
        int i15 = i11;
        int i16 = i10;
        int i17 = i9;
        int i18 = i8;
        int i19 = i7;
        int i20 = i6;
        int i21 = 0;
        for (i = 16; i21 < i; i = 16) {
            int i22 = (i20 << 12) | (i20 >>> 20);
            int i23 = i22 + i16 + T[i21];
            int i24 = (i23 << 7) | (i23 >>> 25);
            int i25 = this.W[i21];
            int FF0 = FF0(i20, i19, i18) + i17 + (i24 ^ i22) + (this.W[i21 + 4] ^ i25);
            int GG0 = GG0(i16, i15, i14) + i13 + i24 + i25;
            int i26 = (i19 << 9) | (i19 >>> 23);
            int i27 = (i15 << 19) | (i15 >>> 13);
            i21++;
            i15 = i16;
            i16 = P0(GG0);
            i13 = i14;
            i14 = i27;
            i19 = i20;
            i20 = FF0;
            i17 = i18;
            i18 = i26;
        }
        int i28 = i19;
        int i29 = 16;
        int i30 = i20;
        int i31 = i17;
        int i32 = i18;
        int i33 = i15;
        int i34 = i16;
        while (i29 < 64) {
            int i35 = (i30 << 12) | (i30 >>> 20);
            int i36 = i35 + i34 + T[i29];
            int i37 = (i36 << 7) | (i36 >>> 25);
            int i38 = this.W[i29];
            int FF1 = FF1(i30, i28, i32) + i31 + (i35 ^ i37) + (this.W[i29 + 4] ^ i38);
            int GG1 = GG1(i34, i33, i14) + i13 + i37 + i38;
            i29++;
            i13 = i14;
            i14 = (i33 >>> 13) | (i33 << 19);
            i33 = i34;
            i34 = P0(GG1);
            int i39 = i32;
            i32 = (i28 >>> 23) | (i28 << 9);
            i28 = i30;
            i30 = FF1;
            i31 = i39;
        }
        int[] iArr = this.V;
        iArr[0] = i30 ^ iArr[0];
        int[] iArr2 = this.V;
        iArr2[1] = i28 ^ iArr2[1];
        int[] iArr3 = this.V;
        iArr3[2] = iArr3[2] ^ i32;
        int[] iArr4 = this.V;
        iArr4[3] = i31 ^ iArr4[3];
        int[] iArr5 = this.V;
        iArr5[4] = iArr5[4] ^ i34;
        int[] iArr6 = this.V;
        iArr6[5] = iArr6[5] ^ i33;
        int[] iArr7 = this.V;
        iArr7[6] = iArr7[6] ^ i14;
        int[] iArr8 = this.V;
        iArr8[7] = iArr8[7] ^ i13;
        this.xOff = 0;
    }

    /* access modifiers changed from: protected */
    public void processLength(long j) {
        if (this.xOff > 14) {
            this.inwords[this.xOff] = 0;
            this.xOff++;
            processBlock();
        }
        while (this.xOff < 14) {
            this.inwords[this.xOff] = 0;
            this.xOff++;
        }
        int[] iArr = this.inwords;
        int i = this.xOff;
        this.xOff = i + 1;
        iArr[i] = (int) (j >>> 32);
        int[] iArr2 = this.inwords;
        int i2 = this.xOff;
        this.xOff = i2 + 1;
        iArr2[i2] = (int) j;
    }

    /* access modifiers changed from: protected */
    public void processWord(byte[] bArr, int i) {
        int i2 = i + 1;
        int i3 = i2 + 1;
        this.inwords[this.xOff] = (bArr[i3 + 1] & 255) | ((bArr[i] & 255) << 24) | ((bArr[i2] & 255) << Tnaf.POW_2_WIDTH) | ((bArr[i3] & 255) << 8);
        this.xOff++;
        if (this.xOff >= 16) {
            processBlock();
        }
    }

    public void reset() {
        super.reset();
        this.V[0] = 1937774191;
        this.V[1] = 1226093241;
        this.V[2] = 388252375;
        this.V[3] = -628488704;
        this.V[4] = -1452330820;
        this.V[5] = 372324522;
        this.V[6] = -477237683;
        this.V[7] = -1325724082;
        this.xOff = 0;
    }

    public void reset(Memoable memoable) {
        SM3Digest sM3Digest = (SM3Digest) memoable;
        super.copyIn(sM3Digest);
        copyIn(sM3Digest);
    }
}
