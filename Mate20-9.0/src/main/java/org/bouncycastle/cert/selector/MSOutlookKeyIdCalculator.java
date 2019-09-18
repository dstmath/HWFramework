package org.bouncycastle.cert.selector;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.pqc.jcajce.spec.McElieceCCA2KeyGenParameterSpec;
import org.bouncycastle.util.Pack;

class MSOutlookKeyIdCalculator {

    private static abstract class GeneralDigest {
        private static final int BYTE_LENGTH = 64;
        private long byteCount;
        private byte[] xBuf;
        private int xBufOff;

        protected GeneralDigest() {
            this.xBuf = new byte[4];
            this.xBufOff = 0;
        }

        protected GeneralDigest(GeneralDigest generalDigest) {
            this.xBuf = new byte[generalDigest.xBuf.length];
            copyIn(generalDigest);
        }

        /* access modifiers changed from: protected */
        public void copyIn(GeneralDigest generalDigest) {
            System.arraycopy(generalDigest.xBuf, 0, this.xBuf, 0, generalDigest.xBuf.length);
            this.xBufOff = generalDigest.xBufOff;
            this.byteCount = generalDigest.byteCount;
        }

        public void finish() {
            long j = this.byteCount << 3;
            byte b = Byte.MIN_VALUE;
            while (true) {
                update(b);
                if (this.xBufOff != 0) {
                    b = 0;
                } else {
                    processLength(j);
                    processBlock();
                    return;
                }
            }
        }

        /* access modifiers changed from: protected */
        public abstract void processBlock();

        /* access modifiers changed from: protected */
        public abstract void processLength(long j);

        /* access modifiers changed from: protected */
        public abstract void processWord(byte[] bArr, int i);

        public void reset() {
            this.byteCount = 0;
            this.xBufOff = 0;
            for (int i = 0; i < this.xBuf.length; i++) {
                this.xBuf[i] = 0;
            }
        }

        public void update(byte b) {
            byte[] bArr = this.xBuf;
            int i = this.xBufOff;
            this.xBufOff = i + 1;
            bArr[i] = b;
            if (this.xBufOff == this.xBuf.length) {
                processWord(this.xBuf, 0);
                this.xBufOff = 0;
            }
            this.byteCount++;
        }

        public void update(byte[] bArr, int i, int i2) {
            while (this.xBufOff != 0 && i2 > 0) {
                update(bArr[i]);
                i++;
                i2--;
            }
            while (i2 > this.xBuf.length) {
                processWord(bArr, i);
                i += this.xBuf.length;
                i2 -= this.xBuf.length;
                this.byteCount += (long) this.xBuf.length;
            }
            while (i2 > 0) {
                update(bArr[i]);
                i++;
                i2--;
            }
        }
    }

    private static class SHA1Digest extends GeneralDigest {
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
        private int[] X = new int[80];
        private int xOff;

        public SHA1Digest() {
            reset();
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

        public String getAlgorithmName() {
            return McElieceCCA2KeyGenParameterSpec.SHA1;
        }

        public int getDigestSize() {
            return 20;
        }

        /* access modifiers changed from: protected */
        public void processBlock() {
            for (int i = 16; i < 80; i++) {
                int i2 = ((this.X[i - 3] ^ this.X[i - 8]) ^ this.X[i - 14]) ^ this.X[i - 16];
                this.X[i] = (i2 >>> 31) | (i2 << 1);
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
        public void processLength(long j) {
            if (this.xOff > 14) {
                processBlock();
            }
            this.X[14] = (int) (j >>> 32);
            this.X[15] = (int) (j & -1);
        }

        /* access modifiers changed from: protected */
        public void processWord(byte[] bArr, int i) {
            int i2 = i + 1;
            int i3 = i2 + 1;
            this.X[this.xOff] = (bArr[i3 + 1] & 255) | (bArr[i] << 24) | ((bArr[i2] & 255) << Tnaf.POW_2_WIDTH) | ((bArr[i3] & 255) << 8);
            int i4 = this.xOff + 1;
            this.xOff = i4;
            if (i4 == 16) {
                processBlock();
            }
        }

        public void reset() {
            super.reset();
            this.H1 = 1732584193;
            this.H2 = -271733879;
            this.H3 = -1732584194;
            this.H4 = 271733878;
            this.H5 = -1009589776;
            this.xOff = 0;
            for (int i = 0; i != this.X.length; i++) {
                this.X[i] = 0;
            }
        }
    }

    MSOutlookKeyIdCalculator() {
    }

    static byte[] calculateKeyId(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        SHA1Digest sHA1Digest = new SHA1Digest();
        byte[] bArr = new byte[sHA1Digest.getDigestSize()];
        byte[] bArr2 = new byte[0];
        try {
            byte[] encoded = subjectPublicKeyInfo.getEncoded(ASN1Encoding.DER);
            sHA1Digest.update(encoded, 0, encoded.length);
            sHA1Digest.doFinal(bArr, 0);
            return bArr;
        } catch (IOException e) {
            return new byte[0];
        }
    }
}
