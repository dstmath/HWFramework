package org.bouncycastle.crypto.digests;

import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.Pack;

public class MD5Digest extends GeneralDigest implements EncodableDigest {
    private static final int DIGEST_LENGTH = 16;
    private static final int S11 = 7;
    private static final int S12 = 12;
    private static final int S13 = 17;
    private static final int S14 = 22;
    private static final int S21 = 5;
    private static final int S22 = 9;
    private static final int S23 = 14;
    private static final int S24 = 20;
    private static final int S31 = 4;
    private static final int S32 = 11;
    private static final int S33 = 16;
    private static final int S34 = 23;
    private static final int S41 = 6;
    private static final int S42 = 10;
    private static final int S43 = 15;
    private static final int S44 = 21;
    private int H1;
    private int H2;
    private int H3;
    private int H4;
    private int[] X = new int[16];
    private int xOff;

    public MD5Digest() {
        reset();
    }

    public MD5Digest(MD5Digest mD5Digest) {
        super((GeneralDigest) mD5Digest);
        copyIn(mD5Digest);
    }

    public MD5Digest(byte[] bArr) {
        super(bArr);
        this.H1 = Pack.bigEndianToInt(bArr, 16);
        this.H2 = Pack.bigEndianToInt(bArr, 20);
        this.H3 = Pack.bigEndianToInt(bArr, 24);
        this.H4 = Pack.bigEndianToInt(bArr, 28);
        this.xOff = Pack.bigEndianToInt(bArr, 32);
        for (int i = 0; i != this.xOff; i++) {
            this.X[i] = Pack.bigEndianToInt(bArr, 36 + (i * 4));
        }
    }

    private int F(int i, int i2, int i3) {
        return ((~i) & i3) | (i2 & i);
    }

    private int G(int i, int i2, int i3) {
        return (i & i3) | (i2 & (~i3));
    }

    private int H(int i, int i2, int i3) {
        return (i ^ i2) ^ i3;
    }

    private int K(int i, int i2, int i3) {
        return (i | (~i3)) ^ i2;
    }

    private void copyIn(MD5Digest mD5Digest) {
        super.copyIn(mD5Digest);
        this.H1 = mD5Digest.H1;
        this.H2 = mD5Digest.H2;
        this.H3 = mD5Digest.H3;
        this.H4 = mD5Digest.H4;
        System.arraycopy(mD5Digest.X, 0, this.X, 0, mD5Digest.X.length);
        this.xOff = mD5Digest.xOff;
    }

    private int rotateLeft(int i, int i2) {
        return (i >>> (32 - i2)) | (i << i2);
    }

    private void unpackWord(int i, byte[] bArr, int i2) {
        bArr[i2] = (byte) i;
        bArr[i2 + 1] = (byte) (i >>> 8);
        bArr[i2 + 2] = (byte) (i >>> 16);
        bArr[i2 + 3] = (byte) (i >>> 24);
    }

    public Memoable copy() {
        return new MD5Digest(this);
    }

    public int doFinal(byte[] bArr, int i) {
        finish();
        unpackWord(this.H1, bArr, i);
        unpackWord(this.H2, bArr, i + 4);
        unpackWord(this.H3, bArr, i + 8);
        unpackWord(this.H4, bArr, i + 12);
        reset();
        return 16;
    }

    public String getAlgorithmName() {
        return "MD5";
    }

    public int getDigestSize() {
        return 16;
    }

    public byte[] getEncodedState() {
        byte[] bArr = new byte[((this.xOff * 4) + 36)];
        super.populateState(bArr);
        Pack.intToBigEndian(this.H1, bArr, 16);
        Pack.intToBigEndian(this.H2, bArr, 20);
        Pack.intToBigEndian(this.H3, bArr, 24);
        Pack.intToBigEndian(this.H4, bArr, 28);
        Pack.intToBigEndian(this.xOff, bArr, 32);
        for (int i = 0; i != this.xOff; i++) {
            Pack.intToBigEndian(this.X[i], bArr, (i * 4) + 36);
        }
        return bArr;
    }

    /* access modifiers changed from: protected */
    public void processBlock() {
        int i = this.H1;
        int i2 = this.H2;
        int i3 = this.H3;
        int i4 = this.H4;
        int rotateLeft = rotateLeft(((i + F(i2, i3, i4)) + this.X[0]) - 680876936, 7) + i2;
        int rotateLeft2 = rotateLeft(((i4 + F(rotateLeft, i2, i3)) + this.X[1]) - 389564586, 12) + rotateLeft;
        int rotateLeft3 = rotateLeft(i3 + F(rotateLeft2, rotateLeft, i2) + this.X[2] + 606105819, 17) + rotateLeft2;
        int rotateLeft4 = rotateLeft(((i2 + F(rotateLeft3, rotateLeft2, rotateLeft)) + this.X[3]) - 1044525330, 22) + rotateLeft3;
        int rotateLeft5 = rotateLeft(((rotateLeft + F(rotateLeft4, rotateLeft3, rotateLeft2)) + this.X[4]) - 176418897, 7) + rotateLeft4;
        int rotateLeft6 = rotateLeft(rotateLeft2 + F(rotateLeft5, rotateLeft4, rotateLeft3) + this.X[5] + 1200080426, 12) + rotateLeft5;
        int rotateLeft7 = rotateLeft(((rotateLeft3 + F(rotateLeft6, rotateLeft5, rotateLeft4)) + this.X[6]) - 1473231341, 17) + rotateLeft6;
        int rotateLeft8 = rotateLeft(((rotateLeft4 + F(rotateLeft7, rotateLeft6, rotateLeft5)) + this.X[7]) - 45705983, 22) + rotateLeft7;
        int rotateLeft9 = rotateLeft(rotateLeft5 + F(rotateLeft8, rotateLeft7, rotateLeft6) + this.X[8] + 1770035416, 7) + rotateLeft8;
        int rotateLeft10 = rotateLeft(((rotateLeft6 + F(rotateLeft9, rotateLeft8, rotateLeft7)) + this.X[9]) - 1958414417, 12) + rotateLeft9;
        int rotateLeft11 = rotateLeft(((rotateLeft7 + F(rotateLeft10, rotateLeft9, rotateLeft8)) + this.X[10]) - 42063, 17) + rotateLeft10;
        int rotateLeft12 = rotateLeft(((rotateLeft8 + F(rotateLeft11, rotateLeft10, rotateLeft9)) + this.X[11]) - 1990404162, 22) + rotateLeft11;
        int rotateLeft13 = rotateLeft(rotateLeft9 + F(rotateLeft12, rotateLeft11, rotateLeft10) + this.X[12] + 1804603682, 7) + rotateLeft12;
        int rotateLeft14 = rotateLeft(((rotateLeft10 + F(rotateLeft13, rotateLeft12, rotateLeft11)) + this.X[13]) - 40341101, 12) + rotateLeft13;
        int rotateLeft15 = rotateLeft(((rotateLeft11 + F(rotateLeft14, rotateLeft13, rotateLeft12)) + this.X[14]) - 1502002290, 17) + rotateLeft14;
        int rotateLeft16 = rotateLeft(rotateLeft12 + F(rotateLeft15, rotateLeft14, rotateLeft13) + this.X[15] + 1236535329, 22) + rotateLeft15;
        int rotateLeft17 = rotateLeft(((rotateLeft13 + G(rotateLeft16, rotateLeft15, rotateLeft14)) + this.X[1]) - 165796510, 5) + rotateLeft16;
        int rotateLeft18 = rotateLeft(((rotateLeft14 + G(rotateLeft17, rotateLeft16, rotateLeft15)) + this.X[6]) - 1069501632, 9) + rotateLeft17;
        int rotateLeft19 = rotateLeft(rotateLeft15 + G(rotateLeft18, rotateLeft17, rotateLeft16) + this.X[11] + 643717713, 14) + rotateLeft18;
        int rotateLeft20 = rotateLeft(((rotateLeft16 + G(rotateLeft19, rotateLeft18, rotateLeft17)) + this.X[0]) - 373897302, 20) + rotateLeft19;
        int rotateLeft21 = rotateLeft(((rotateLeft17 + G(rotateLeft20, rotateLeft19, rotateLeft18)) + this.X[5]) - 701558691, 5) + rotateLeft20;
        int rotateLeft22 = rotateLeft(rotateLeft18 + G(rotateLeft21, rotateLeft20, rotateLeft19) + this.X[10] + 38016083, 9) + rotateLeft21;
        int rotateLeft23 = rotateLeft(((rotateLeft19 + G(rotateLeft22, rotateLeft21, rotateLeft20)) + this.X[15]) - 660478335, 14) + rotateLeft22;
        int rotateLeft24 = rotateLeft(((rotateLeft20 + G(rotateLeft23, rotateLeft22, rotateLeft21)) + this.X[4]) - 405537848, 20) + rotateLeft23;
        int rotateLeft25 = rotateLeft(rotateLeft21 + G(rotateLeft24, rotateLeft23, rotateLeft22) + this.X[9] + 568446438, 5) + rotateLeft24;
        int rotateLeft26 = rotateLeft(((rotateLeft22 + G(rotateLeft25, rotateLeft24, rotateLeft23)) + this.X[14]) - 1019803690, 9) + rotateLeft25;
        int rotateLeft27 = rotateLeft(((rotateLeft23 + G(rotateLeft26, rotateLeft25, rotateLeft24)) + this.X[3]) - 187363961, 14) + rotateLeft26;
        int rotateLeft28 = rotateLeft(rotateLeft24 + G(rotateLeft27, rotateLeft26, rotateLeft25) + this.X[8] + 1163531501, 20) + rotateLeft27;
        int rotateLeft29 = rotateLeft(((rotateLeft25 + G(rotateLeft28, rotateLeft27, rotateLeft26)) + this.X[13]) - 1444681467, 5) + rotateLeft28;
        int rotateLeft30 = rotateLeft(((rotateLeft26 + G(rotateLeft29, rotateLeft28, rotateLeft27)) + this.X[2]) - 51403784, 9) + rotateLeft29;
        int rotateLeft31 = rotateLeft(rotateLeft27 + G(rotateLeft30, rotateLeft29, rotateLeft28) + this.X[7] + 1735328473, 14) + rotateLeft30;
        int rotateLeft32 = rotateLeft(((rotateLeft28 + G(rotateLeft31, rotateLeft30, rotateLeft29)) + this.X[12]) - 1926607734, 20) + rotateLeft31;
        int rotateLeft33 = rotateLeft(((rotateLeft29 + H(rotateLeft32, rotateLeft31, rotateLeft30)) + this.X[5]) - 378558, 4) + rotateLeft32;
        int rotateLeft34 = rotateLeft(((rotateLeft30 + H(rotateLeft33, rotateLeft32, rotateLeft31)) + this.X[8]) - 2022574463, 11) + rotateLeft33;
        int rotateLeft35 = rotateLeft(rotateLeft31 + H(rotateLeft34, rotateLeft33, rotateLeft32) + this.X[11] + 1839030562, 16) + rotateLeft34;
        int rotateLeft36 = rotateLeft(((rotateLeft32 + H(rotateLeft35, rotateLeft34, rotateLeft33)) + this.X[14]) - 35309556, 23) + rotateLeft35;
        int rotateLeft37 = rotateLeft(((rotateLeft33 + H(rotateLeft36, rotateLeft35, rotateLeft34)) + this.X[1]) - 1530992060, 4) + rotateLeft36;
        int rotateLeft38 = rotateLeft(rotateLeft34 + H(rotateLeft37, rotateLeft36, rotateLeft35) + this.X[4] + 1272893353, 11) + rotateLeft37;
        int rotateLeft39 = rotateLeft(((rotateLeft35 + H(rotateLeft38, rotateLeft37, rotateLeft36)) + this.X[7]) - 155497632, 16) + rotateLeft38;
        int rotateLeft40 = rotateLeft(((rotateLeft36 + H(rotateLeft39, rotateLeft38, rotateLeft37)) + this.X[10]) - 1094730640, 23) + rotateLeft39;
        int rotateLeft41 = rotateLeft(rotateLeft37 + H(rotateLeft40, rotateLeft39, rotateLeft38) + this.X[13] + 681279174, 4) + rotateLeft40;
        int rotateLeft42 = rotateLeft(((rotateLeft38 + H(rotateLeft41, rotateLeft40, rotateLeft39)) + this.X[0]) - 358537222, 11) + rotateLeft41;
        int rotateLeft43 = rotateLeft(((rotateLeft39 + H(rotateLeft42, rotateLeft41, rotateLeft40)) + this.X[3]) - 722521979, 16) + rotateLeft42;
        int rotateLeft44 = rotateLeft(rotateLeft40 + H(rotateLeft43, rotateLeft42, rotateLeft41) + this.X[6] + 76029189, 23) + rotateLeft43;
        int rotateLeft45 = rotateLeft(((rotateLeft41 + H(rotateLeft44, rotateLeft43, rotateLeft42)) + this.X[9]) - 640364487, 4) + rotateLeft44;
        int rotateLeft46 = rotateLeft(((rotateLeft42 + H(rotateLeft45, rotateLeft44, rotateLeft43)) + this.X[12]) - 421815835, 11) + rotateLeft45;
        int rotateLeft47 = rotateLeft(rotateLeft43 + H(rotateLeft46, rotateLeft45, rotateLeft44) + this.X[15] + 530742520, 16) + rotateLeft46;
        int rotateLeft48 = rotateLeft(((rotateLeft44 + H(rotateLeft47, rotateLeft46, rotateLeft45)) + this.X[2]) - 995338651, 23) + rotateLeft47;
        int rotateLeft49 = rotateLeft(((rotateLeft45 + K(rotateLeft48, rotateLeft47, rotateLeft46)) + this.X[0]) - 198630844, 6) + rotateLeft48;
        int rotateLeft50 = rotateLeft(rotateLeft46 + K(rotateLeft49, rotateLeft48, rotateLeft47) + this.X[7] + 1126891415, 10) + rotateLeft49;
        int rotateLeft51 = rotateLeft(((rotateLeft47 + K(rotateLeft50, rotateLeft49, rotateLeft48)) + this.X[14]) - 1416354905, 15) + rotateLeft50;
        int rotateLeft52 = rotateLeft(((rotateLeft48 + K(rotateLeft51, rotateLeft50, rotateLeft49)) + this.X[5]) - 57434055, 21) + rotateLeft51;
        int rotateLeft53 = rotateLeft(rotateLeft49 + K(rotateLeft52, rotateLeft51, rotateLeft50) + this.X[12] + 1700485571, 6) + rotateLeft52;
        int rotateLeft54 = rotateLeft(((rotateLeft50 + K(rotateLeft53, rotateLeft52, rotateLeft51)) + this.X[3]) - 1894986606, 10) + rotateLeft53;
        int rotateLeft55 = rotateLeft(((rotateLeft51 + K(rotateLeft54, rotateLeft53, rotateLeft52)) + this.X[10]) - 1051523, 15) + rotateLeft54;
        int rotateLeft56 = rotateLeft(((rotateLeft52 + K(rotateLeft55, rotateLeft54, rotateLeft53)) + this.X[1]) - 2054922799, 21) + rotateLeft55;
        int rotateLeft57 = rotateLeft(rotateLeft53 + K(rotateLeft56, rotateLeft55, rotateLeft54) + this.X[8] + 1873313359, 6) + rotateLeft56;
        int rotateLeft58 = rotateLeft(((rotateLeft54 + K(rotateLeft57, rotateLeft56, rotateLeft55)) + this.X[15]) - 30611744, 10) + rotateLeft57;
        int rotateLeft59 = rotateLeft(((rotateLeft55 + K(rotateLeft58, rotateLeft57, rotateLeft56)) + this.X[6]) - 1560198380, 15) + rotateLeft58;
        int rotateLeft60 = rotateLeft(rotateLeft56 + K(rotateLeft59, rotateLeft58, rotateLeft57) + this.X[13] + 1309151649, 21) + rotateLeft59;
        int rotateLeft61 = rotateLeft(((rotateLeft57 + K(rotateLeft60, rotateLeft59, rotateLeft58)) + this.X[4]) - 145523070, 6) + rotateLeft60;
        int rotateLeft62 = rotateLeft(((rotateLeft58 + K(rotateLeft61, rotateLeft60, rotateLeft59)) + this.X[11]) - 1120210379, 10) + rotateLeft61;
        int rotateLeft63 = rotateLeft(rotateLeft59 + K(rotateLeft62, rotateLeft61, rotateLeft60) + this.X[2] + 718787259, 15) + rotateLeft62;
        this.H1 += rotateLeft61;
        this.H2 += rotateLeft(((rotateLeft60 + K(rotateLeft63, rotateLeft62, rotateLeft61)) + this.X[9]) - 343485551, 21) + rotateLeft63;
        this.H3 += rotateLeft63;
        this.H4 += rotateLeft62;
        this.xOff = 0;
        for (int i5 = 0; i5 != this.X.length; i5++) {
            this.X[i5] = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void processLength(long j) {
        if (this.xOff > 14) {
            processBlock();
        }
        this.X[14] = (int) (-1 & j);
        this.X[15] = (int) (j >>> 32);
    }

    /* access modifiers changed from: protected */
    public void processWord(byte[] bArr, int i) {
        int[] iArr = this.X;
        int i2 = this.xOff;
        this.xOff = i2 + 1;
        iArr[i2] = ((bArr[i + 3] & 255) << 24) | (bArr[i] & 255) | ((bArr[i + 1] & 255) << 8) | ((bArr[i + 2] & 255) << Tnaf.POW_2_WIDTH);
        if (this.xOff == 16) {
            processBlock();
        }
    }

    public void reset() {
        super.reset();
        this.H1 = 1732584193;
        this.H2 = -271733879;
        this.H3 = -1732584194;
        this.H4 = 271733878;
        this.xOff = 0;
        for (int i = 0; i != this.X.length; i++) {
            this.X[i] = 0;
        }
    }

    public void reset(Memoable memoable) {
        copyIn((MD5Digest) memoable);
    }
}
