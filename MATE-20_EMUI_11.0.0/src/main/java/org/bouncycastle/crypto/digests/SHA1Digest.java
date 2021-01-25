package org.bouncycastle.crypto.digests;

import org.bouncycastle.pqc.jcajce.spec.McElieceCCA2KeyGenParameterSpec;
import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.Pack;

public class SHA1Digest extends GeneralDigest implements EncodableDigest {
    private static final int DIGEST_LENGTH = 20;
    private static final int Y1 = 1518500249;
    private static final int Y2 = 1859775393;
    private static final int Y3 = -1894007588;
    private static final int Y4 = -899497514;
    private int H1;
    private int H2;
    private int H3;
    private int H4;
    private int H5;
    private int[] X;
    private int xOff;

    public SHA1Digest() {
        this.X = new int[80];
        reset();
    }

    public SHA1Digest(SHA1Digest sHA1Digest) {
        super(sHA1Digest);
        this.X = new int[80];
        copyIn(sHA1Digest);
    }

    public SHA1Digest(byte[] bArr) {
        super(bArr);
        this.X = new int[80];
        this.H1 = Pack.bigEndianToInt(bArr, 16);
        this.H2 = Pack.bigEndianToInt(bArr, 20);
        this.H3 = Pack.bigEndianToInt(bArr, 24);
        this.H4 = Pack.bigEndianToInt(bArr, 28);
        this.H5 = Pack.bigEndianToInt(bArr, 32);
        this.xOff = Pack.bigEndianToInt(bArr, 36);
        for (int i = 0; i != this.xOff; i++) {
            this.X[i] = Pack.bigEndianToInt(bArr, (i * 4) + 40);
        }
    }

    private void copyIn(SHA1Digest sHA1Digest) {
        this.H1 = sHA1Digest.H1;
        this.H2 = sHA1Digest.H2;
        this.H3 = sHA1Digest.H3;
        this.H4 = sHA1Digest.H4;
        this.H5 = sHA1Digest.H5;
        int[] iArr = sHA1Digest.X;
        System.arraycopy(iArr, 0, this.X, 0, iArr.length);
        this.xOff = sHA1Digest.xOff;
    }

    private int f(int i, int i2, int i3) {
        return ((~i) & i3) | (i2 & i);
    }

    private int g(int i, int i2, int i3) {
        return (i & i3) | (i & i2) | (i2 & i3);
    }

    private int h(int i, int i2, int i3) {
        return (i ^ i2) ^ i3;
    }

    @Override // org.bouncycastle.util.Memoable
    public Memoable copy() {
        return new SHA1Digest(this);
    }

    @Override // org.bouncycastle.crypto.Digest
    public int doFinal(byte[] bArr, int i) {
        finish();
        Pack.intToBigEndian(this.H1, bArr, i);
        Pack.intToBigEndian(this.H2, bArr, i + 4);
        Pack.intToBigEndian(this.H3, bArr, i + 8);
        Pack.intToBigEndian(this.H4, bArr, i + 12);
        Pack.intToBigEndian(this.H5, bArr, i + 16);
        reset();
        return 20;
    }

    @Override // org.bouncycastle.crypto.Digest
    public String getAlgorithmName() {
        return McElieceCCA2KeyGenParameterSpec.SHA1;
    }

    @Override // org.bouncycastle.crypto.Digest
    public int getDigestSize() {
        return 20;
    }

    @Override // org.bouncycastle.crypto.digests.EncodableDigest
    public byte[] getEncodedState() {
        byte[] bArr = new byte[((this.xOff * 4) + 40)];
        super.populateState(bArr);
        Pack.intToBigEndian(this.H1, bArr, 16);
        Pack.intToBigEndian(this.H2, bArr, 20);
        Pack.intToBigEndian(this.H3, bArr, 24);
        Pack.intToBigEndian(this.H4, bArr, 28);
        Pack.intToBigEndian(this.H5, bArr, 32);
        Pack.intToBigEndian(this.xOff, bArr, 36);
        for (int i = 0; i != this.xOff; i++) {
            Pack.intToBigEndian(this.X[i], bArr, (i * 4) + 40);
        }
        return bArr;
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.crypto.digests.GeneralDigest
    public void processBlock() {
        for (int i = 16; i < 80; i++) {
            int[] iArr = this.X;
            int i2 = ((iArr[i - 3] ^ iArr[i - 8]) ^ iArr[i - 14]) ^ iArr[i - 16];
            iArr[i] = (i2 >>> 31) | (i2 << 1);
        }
        int i3 = this.H1;
        int i4 = this.H2;
        int i5 = this.H3;
        int i6 = this.H4;
        int i7 = this.H5;
        int i8 = i6;
        int i9 = 0;
        int i10 = i5;
        int i11 = i4;
        int i12 = i3;
        int i13 = 0;
        while (i13 < 4) {
            int i14 = i9 + 1;
            int f = i7 + ((i12 << 5) | (i12 >>> 27)) + f(i11, i10, i8) + this.X[i9] + Y1;
            int i15 = (i11 >>> 2) | (i11 << 30);
            int i16 = i14 + 1;
            int f2 = i8 + ((f << 5) | (f >>> 27)) + f(i12, i15, i10) + this.X[i14] + Y1;
            int i17 = (i12 >>> 2) | (i12 << 30);
            int i18 = i16 + 1;
            int f3 = i10 + ((f2 << 5) | (f2 >>> 27)) + f(f, i17, i15) + this.X[i16] + Y1;
            i7 = (f >>> 2) | (f << 30);
            int i19 = i18 + 1;
            i11 = i15 + ((f3 << 5) | (f3 >>> 27)) + f(f2, i7, i17) + this.X[i18] + Y1;
            i8 = (f2 >>> 2) | (f2 << 30);
            i12 = i17 + ((i11 << 5) | (i11 >>> 27)) + f(f3, i8, i7) + this.X[i19] + Y1;
            i10 = (f3 >>> 2) | (f3 << 30);
            i13++;
            i9 = i19 + 1;
        }
        int i20 = 0;
        while (i20 < 4) {
            int i21 = i9 + 1;
            int h = i7 + ((i12 << 5) | (i12 >>> 27)) + h(i11, i10, i8) + this.X[i9] + Y2;
            int i22 = (i11 >>> 2) | (i11 << 30);
            int i23 = i21 + 1;
            int h2 = i8 + ((h << 5) | (h >>> 27)) + h(i12, i22, i10) + this.X[i21] + Y2;
            int i24 = (i12 >>> 2) | (i12 << 30);
            int i25 = i23 + 1;
            int h3 = i10 + ((h2 << 5) | (h2 >>> 27)) + h(h, i24, i22) + this.X[i23] + Y2;
            i7 = (h >>> 2) | (h << 30);
            int i26 = i25 + 1;
            i11 = i22 + ((h3 << 5) | (h3 >>> 27)) + h(h2, i7, i24) + this.X[i25] + Y2;
            i8 = (h2 >>> 2) | (h2 << 30);
            i12 = i24 + ((i11 << 5) | (i11 >>> 27)) + h(h3, i8, i7) + this.X[i26] + Y2;
            i10 = (h3 >>> 2) | (h3 << 30);
            i20++;
            i9 = i26 + 1;
        }
        int i27 = 0;
        while (i27 < 4) {
            int i28 = i9 + 1;
            int g = i7 + ((i12 << 5) | (i12 >>> 27)) + g(i11, i10, i8) + this.X[i9] + Y3;
            int i29 = (i11 >>> 2) | (i11 << 30);
            int i30 = i28 + 1;
            int g2 = i8 + ((g << 5) | (g >>> 27)) + g(i12, i29, i10) + this.X[i28] + Y3;
            int i31 = (i12 >>> 2) | (i12 << 30);
            int i32 = i30 + 1;
            int g3 = i10 + ((g2 << 5) | (g2 >>> 27)) + g(g, i31, i29) + this.X[i30] + Y3;
            i7 = (g >>> 2) | (g << 30);
            int i33 = i32 + 1;
            i11 = i29 + ((g3 << 5) | (g3 >>> 27)) + g(g2, i7, i31) + this.X[i32] + Y3;
            i8 = (g2 >>> 2) | (g2 << 30);
            i12 = i31 + ((i11 << 5) | (i11 >>> 27)) + g(g3, i8, i7) + this.X[i33] + Y3;
            i10 = (g3 >>> 2) | (g3 << 30);
            i27++;
            i9 = i33 + 1;
        }
        int i34 = 0;
        while (i34 <= 3) {
            int i35 = i9 + 1;
            int h4 = i7 + ((i12 << 5) | (i12 >>> 27)) + h(i11, i10, i8) + this.X[i9] + Y4;
            int i36 = (i11 >>> 2) | (i11 << 30);
            int i37 = i35 + 1;
            int h5 = i8 + ((h4 << 5) | (h4 >>> 27)) + h(i12, i36, i10) + this.X[i35] + Y4;
            int i38 = (i12 >>> 2) | (i12 << 30);
            int i39 = i37 + 1;
            int h6 = i10 + ((h5 << 5) | (h5 >>> 27)) + h(h4, i38, i36) + this.X[i37] + Y4;
            i7 = (h4 >>> 2) | (h4 << 30);
            int i40 = i39 + 1;
            i11 = i36 + ((h6 << 5) | (h6 >>> 27)) + h(h5, i7, i38) + this.X[i39] + Y4;
            i8 = (h5 >>> 2) | (h5 << 30);
            i12 = i38 + ((i11 << 5) | (i11 >>> 27)) + h(h6, i8, i7) + this.X[i40] + Y4;
            i10 = (h6 >>> 2) | (h6 << 30);
            i34++;
            i9 = i40 + 1;
        }
        this.H1 += i12;
        this.H2 += i11;
        this.H3 += i10;
        this.H4 += i8;
        this.H5 += i7;
        this.xOff = 0;
        for (int i41 = 0; i41 < 16; i41++) {
            this.X[i41] = 0;
        }
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.crypto.digests.GeneralDigest
    public void processLength(long j) {
        if (this.xOff > 14) {
            processBlock();
        }
        int[] iArr = this.X;
        iArr[14] = (int) (j >>> 32);
        iArr[15] = (int) j;
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.crypto.digests.GeneralDigest
    public void processWord(byte[] bArr, int i) {
        int i2 = i + 1;
        int i3 = i2 + 1;
        int i4 = (bArr[i3 + 1] & 255) | (bArr[i] << 24) | ((bArr[i2] & 255) << 16) | ((bArr[i3] & 255) << 8);
        int[] iArr = this.X;
        int i5 = this.xOff;
        iArr[i5] = i4;
        int i6 = i5 + 1;
        this.xOff = i6;
        if (i6 == 16) {
            processBlock();
        }
    }

    @Override // org.bouncycastle.crypto.digests.GeneralDigest, org.bouncycastle.crypto.Digest
    public void reset() {
        super.reset();
        this.H1 = 1732584193;
        this.H2 = -271733879;
        this.H3 = -1732584194;
        this.H4 = 271733878;
        this.H5 = -1009589776;
        this.xOff = 0;
        int i = 0;
        while (true) {
            int[] iArr = this.X;
            if (i != iArr.length) {
                iArr[i] = 0;
                i++;
            } else {
                return;
            }
        }
    }

    @Override // org.bouncycastle.util.Memoable
    public void reset(Memoable memoable) {
        SHA1Digest sHA1Digest = (SHA1Digest) memoable;
        super.copyIn((GeneralDigest) sHA1Digest);
        copyIn(sHA1Digest);
    }
}
