package com.android.org.bouncycastle.crypto.digests;

import com.android.org.bouncycastle.util.Memoable;
import com.android.org.bouncycastle.util.Pack;

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
    private int[] X;
    private int xOff;

    public MD5Digest() {
        this.X = new int[16];
        reset();
    }

    public MD5Digest(byte[] encodedState) {
        super(encodedState);
        this.X = new int[16];
        this.H1 = Pack.bigEndianToInt(encodedState, 16);
        this.H2 = Pack.bigEndianToInt(encodedState, 20);
        this.H3 = Pack.bigEndianToInt(encodedState, 24);
        this.H4 = Pack.bigEndianToInt(encodedState, 28);
        this.xOff = Pack.bigEndianToInt(encodedState, 32);
        for (int i = 0; i != this.xOff; i++) {
            this.X[i] = Pack.bigEndianToInt(encodedState, (i * 4) + 36);
        }
    }

    public MD5Digest(MD5Digest t) {
        super((GeneralDigest) t);
        this.X = new int[16];
        copyIn(t);
    }

    private void copyIn(MD5Digest t) {
        super.copyIn(t);
        this.H1 = t.H1;
        this.H2 = t.H2;
        this.H3 = t.H3;
        this.H4 = t.H4;
        System.arraycopy(t.X, 0, this.X, 0, t.X.length);
        this.xOff = t.xOff;
    }

    public String getAlgorithmName() {
        return "MD5";
    }

    public int getDigestSize() {
        return 16;
    }

    protected void processWord(byte[] in, int inOff) {
        int[] iArr = this.X;
        int i = this.xOff;
        this.xOff = i + 1;
        iArr[i] = (((in[inOff] & 255) | ((in[inOff + 1] & 255) << 8)) | ((in[inOff + 2] & 255) << 16)) | ((in[inOff + 3] & 255) << 24);
        if (this.xOff == 16) {
            processBlock();
        }
    }

    protected void processLength(long bitLength) {
        if (this.xOff > 14) {
            processBlock();
        }
        this.X[14] = (int) (-1 & bitLength);
        this.X[15] = (int) (bitLength >>> 32);
    }

    private void unpackWord(int word, byte[] out, int outOff) {
        out[outOff] = (byte) word;
        out[outOff + 1] = (byte) (word >>> 8);
        out[outOff + 2] = (byte) (word >>> 16);
        out[outOff + 3] = (byte) (word >>> 24);
    }

    public int doFinal(byte[] out, int outOff) {
        finish();
        unpackWord(this.H1, out, outOff);
        unpackWord(this.H2, out, outOff + 4);
        unpackWord(this.H3, out, outOff + 8);
        unpackWord(this.H4, out, outOff + 12);
        reset();
        return 16;
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

    private int rotateLeft(int x, int n) {
        return (x << n) | (x >>> (32 - n));
    }

    private int F(int u, int v, int w) {
        return (u & v) | ((~u) & w);
    }

    private int G(int u, int v, int w) {
        return (u & w) | ((~w) & v);
    }

    private int H(int u, int v, int w) {
        return (u ^ v) ^ w;
    }

    private int K(int u, int v, int w) {
        return ((~w) | u) ^ v;
    }

    protected void processBlock() {
        int a = this.H1;
        int b = this.H2;
        int c = this.H3;
        int d = this.H4;
        a = rotateLeft(((F(b, c, d) + a) + this.X[0]) - 680876936, 7) + b;
        d = rotateLeft(((F(a, b, c) + d) + this.X[1]) - 389564586, 12) + a;
        c = rotateLeft(((F(d, a, b) + c) + this.X[2]) + 606105819, 17) + d;
        b = rotateLeft(((F(c, d, a) + b) + this.X[3]) - 1044525330, 22) + c;
        a = rotateLeft(((F(b, c, d) + a) + this.X[4]) - 176418897, 7) + b;
        d = rotateLeft(((F(a, b, c) + d) + this.X[5]) + 1200080426, 12) + a;
        c = rotateLeft(((F(d, a, b) + c) + this.X[6]) - 1473231341, 17) + d;
        b = rotateLeft(((F(c, d, a) + b) + this.X[7]) - 45705983, 22) + c;
        a = rotateLeft(((F(b, c, d) + a) + this.X[8]) + 1770035416, 7) + b;
        d = rotateLeft(((F(a, b, c) + d) + this.X[9]) - 1958414417, 12) + a;
        c = rotateLeft(((F(d, a, b) + c) + this.X[10]) - 42063, 17) + d;
        b = rotateLeft(((F(c, d, a) + b) + this.X[11]) - 1990404162, 22) + c;
        a = rotateLeft(((F(b, c, d) + a) + this.X[12]) + 1804603682, 7) + b;
        d = rotateLeft(((F(a, b, c) + d) + this.X[13]) - 40341101, 12) + a;
        c = rotateLeft(((F(d, a, b) + c) + this.X[14]) - 1502002290, 17) + d;
        b = rotateLeft(((F(c, d, a) + b) + this.X[15]) + 1236535329, 22) + c;
        a = rotateLeft(((G(b, c, d) + a) + this.X[1]) - 165796510, 5) + b;
        d = rotateLeft(((G(a, b, c) + d) + this.X[6]) - 1069501632, 9) + a;
        c = rotateLeft(((G(d, a, b) + c) + this.X[11]) + 643717713, 14) + d;
        b = rotateLeft(((G(c, d, a) + b) + this.X[0]) - 373897302, 20) + c;
        a = rotateLeft(((G(b, c, d) + a) + this.X[5]) - 701558691, 5) + b;
        d = rotateLeft(((G(a, b, c) + d) + this.X[10]) + 38016083, 9) + a;
        c = rotateLeft(((G(d, a, b) + c) + this.X[15]) - 660478335, 14) + d;
        b = rotateLeft(((G(c, d, a) + b) + this.X[4]) - 405537848, 20) + c;
        a = rotateLeft(((G(b, c, d) + a) + this.X[9]) + 568446438, 5) + b;
        d = rotateLeft(((G(a, b, c) + d) + this.X[14]) - 1019803690, 9) + a;
        c = rotateLeft(((G(d, a, b) + c) + this.X[3]) - 187363961, 14) + d;
        b = rotateLeft(((G(c, d, a) + b) + this.X[8]) + 1163531501, 20) + c;
        a = rotateLeft(((G(b, c, d) + a) + this.X[13]) - 1444681467, 5) + b;
        d = rotateLeft(((G(a, b, c) + d) + this.X[2]) - 51403784, 9) + a;
        c = rotateLeft(((G(d, a, b) + c) + this.X[7]) + 1735328473, 14) + d;
        b = rotateLeft(((G(c, d, a) + b) + this.X[12]) - 1926607734, 20) + c;
        a = rotateLeft(((H(b, c, d) + a) + this.X[5]) - 378558, 4) + b;
        d = rotateLeft(((H(a, b, c) + d) + this.X[8]) - 2022574463, 11) + a;
        c = rotateLeft(((H(d, a, b) + c) + this.X[11]) + 1839030562, 16) + d;
        b = rotateLeft(((H(c, d, a) + b) + this.X[14]) - 35309556, 23) + c;
        a = rotateLeft(((H(b, c, d) + a) + this.X[1]) - 1530992060, 4) + b;
        d = rotateLeft(((H(a, b, c) + d) + this.X[4]) + 1272893353, 11) + a;
        c = rotateLeft(((H(d, a, b) + c) + this.X[7]) - 155497632, 16) + d;
        b = rotateLeft(((H(c, d, a) + b) + this.X[10]) - 1094730640, 23) + c;
        a = rotateLeft(((H(b, c, d) + a) + this.X[13]) + 681279174, 4) + b;
        d = rotateLeft(((H(a, b, c) + d) + this.X[0]) - 358537222, 11) + a;
        c = rotateLeft(((H(d, a, b) + c) + this.X[3]) - 722521979, 16) + d;
        b = rotateLeft(((H(c, d, a) + b) + this.X[6]) + 76029189, 23) + c;
        a = rotateLeft(((H(b, c, d) + a) + this.X[9]) - 640364487, 4) + b;
        d = rotateLeft(((H(a, b, c) + d) + this.X[12]) - 421815835, 11) + a;
        c = rotateLeft(((H(d, a, b) + c) + this.X[15]) + 530742520, 16) + d;
        b = rotateLeft(((H(c, d, a) + b) + this.X[2]) - 995338651, 23) + c;
        a = rotateLeft(((K(b, c, d) + a) + this.X[0]) - 198630844, 6) + b;
        d = rotateLeft(((K(a, b, c) + d) + this.X[7]) + 1126891415, 10) + a;
        c = rotateLeft(((K(d, a, b) + c) + this.X[14]) - 1416354905, 15) + d;
        b = rotateLeft(((K(c, d, a) + b) + this.X[5]) - 57434055, 21) + c;
        a = rotateLeft(((K(b, c, d) + a) + this.X[12]) + 1700485571, 6) + b;
        d = rotateLeft(((K(a, b, c) + d) + this.X[3]) - 1894986606, 10) + a;
        c = rotateLeft(((K(d, a, b) + c) + this.X[10]) - 1051523, 15) + d;
        b = rotateLeft(((K(c, d, a) + b) + this.X[1]) - 2054922799, 21) + c;
        a = rotateLeft(((K(b, c, d) + a) + this.X[8]) + 1873313359, 6) + b;
        d = rotateLeft(((K(a, b, c) + d) + this.X[15]) - 30611744, 10) + a;
        c = rotateLeft(((K(d, a, b) + c) + this.X[6]) - 1560198380, 15) + d;
        b = rotateLeft(((K(c, d, a) + b) + this.X[13]) + 1309151649, 21) + c;
        a = rotateLeft(((K(b, c, d) + a) + this.X[4]) - 145523070, 6) + b;
        d = rotateLeft(((K(a, b, c) + d) + this.X[11]) - 1120210379, 10) + a;
        c = rotateLeft(((K(d, a, b) + c) + this.X[2]) + 718787259, 15) + d;
        b = rotateLeft(((K(c, d, a) + b) + this.X[9]) - 343485551, 21) + c;
        this.H1 += a;
        this.H2 += b;
        this.H3 += c;
        this.H4 += d;
        this.xOff = 0;
        for (int i = 0; i != this.X.length; i++) {
            this.X[i] = 0;
        }
    }

    public Memoable copy() {
        return new MD5Digest(this);
    }

    public void reset(Memoable other) {
        copyIn((MD5Digest) other);
    }

    public byte[] getEncodedState() {
        byte[] state = new byte[((this.xOff * 4) + 36)];
        super.populateState(state);
        Pack.intToBigEndian(this.H1, state, 16);
        Pack.intToBigEndian(this.H2, state, 20);
        Pack.intToBigEndian(this.H3, state, 24);
        Pack.intToBigEndian(this.H4, state, 28);
        Pack.intToBigEndian(this.xOff, state, 32);
        for (int i = 0; i != this.xOff; i++) {
            Pack.intToBigEndian(this.X[i], state, (i * 4) + 36);
        }
        return state;
    }
}
