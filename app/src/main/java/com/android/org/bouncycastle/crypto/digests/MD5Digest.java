package com.android.org.bouncycastle.crypto.digests;

import com.android.org.bouncycastle.util.Memoable;

public class MD5Digest extends GeneralDigest {
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

    private int F(int r1, int r2, int r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.crypto.digests.MD5Digest.F(int, int, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.digests.MD5Digest.F(int, int, int):int");
    }

    private int G(int r1, int r2, int r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.crypto.digests.MD5Digest.G(int, int, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.digests.MD5Digest.G(int, int, int):int");
    }

    private int K(int r1, int r2, int r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.crypto.digests.MD5Digest.K(int, int, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.crypto.digests.MD5Digest.K(int, int, int):int");
    }

    public MD5Digest() {
        this.X = new int[S33];
        reset();
    }

    public MD5Digest(MD5Digest t) {
        super((GeneralDigest) t);
        this.X = new int[S33];
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
        return S33;
    }

    protected void processWord(byte[] in, int inOff) {
        int[] iArr = this.X;
        int i = this.xOff;
        this.xOff = i + 1;
        iArr[i] = (((in[inOff] & 255) | ((in[inOff + 1] & 255) << 8)) | ((in[inOff + 2] & 255) << S33)) | ((in[inOff + 3] & 255) << 24);
        if (this.xOff == S33) {
            processBlock();
        }
    }

    protected void processLength(long bitLength) {
        if (this.xOff > S23) {
            processBlock();
        }
        this.X[S23] = (int) (-1 & bitLength);
        this.X[S43] = (int) (bitLength >>> 32);
    }

    private void unpackWord(int word, byte[] out, int outOff) {
        out[outOff] = (byte) word;
        out[outOff + 1] = (byte) (word >>> 8);
        out[outOff + 2] = (byte) (word >>> S33);
        out[outOff + 3] = (byte) (word >>> 24);
    }

    public int doFinal(byte[] out, int outOff) {
        finish();
        unpackWord(this.H1, out, outOff);
        unpackWord(this.H2, out, outOff + S31);
        unpackWord(this.H3, out, outOff + 8);
        unpackWord(this.H4, out, outOff + S12);
        reset();
        return S33;
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

    private int H(int u, int v, int w) {
        return (u ^ v) ^ w;
    }

    protected void processBlock() {
        int a = this.H1;
        int b = this.H2;
        int c = this.H3;
        int d = this.H4;
        a = rotateLeft(((F(b, c, d) + a) + this.X[0]) - 680876936, S11) + b;
        d = rotateLeft(((F(a, b, c) + d) + this.X[1]) - 389564586, S12) + a;
        c = rotateLeft(((F(d, a, b) + c) + this.X[2]) + 606105819, S13) + d;
        b = rotateLeft(((F(c, d, a) + b) + this.X[3]) - 1044525330, S14) + c;
        a = rotateLeft(((F(b, c, d) + a) + this.X[S31]) - 176418897, S11) + b;
        d = rotateLeft(((F(a, b, c) + d) + this.X[S21]) + 1200080426, S12) + a;
        c = rotateLeft(((F(d, a, b) + c) + this.X[S41]) - 1473231341, S13) + d;
        b = rotateLeft(((F(c, d, a) + b) + this.X[S11]) - 45705983, S14) + c;
        a = rotateLeft(((F(b, c, d) + a) + this.X[8]) + 1770035416, S11) + b;
        d = rotateLeft(((F(a, b, c) + d) + this.X[S22]) - 1958414417, S12) + a;
        c = rotateLeft(((F(d, a, b) + c) + this.X[S42]) - 42063, S13) + d;
        b = rotateLeft(((F(c, d, a) + b) + this.X[S32]) - 1990404162, S14) + c;
        a = rotateLeft(((F(b, c, d) + a) + this.X[S12]) + 1804603682, S11) + b;
        d = rotateLeft(((F(a, b, c) + d) + this.X[13]) - 40341101, S12) + a;
        c = rotateLeft(((F(d, a, b) + c) + this.X[S23]) - 1502002290, S13) + d;
        b = rotateLeft(((F(c, d, a) + b) + this.X[S43]) + 1236535329, S14) + c;
        a = rotateLeft(((G(b, c, d) + a) + this.X[1]) - 165796510, S21) + b;
        d = rotateLeft(((G(a, b, c) + d) + this.X[S41]) - 1069501632, S22) + a;
        c = rotateLeft(((G(d, a, b) + c) + this.X[S32]) + 643717713, S23) + d;
        b = rotateLeft(((G(c, d, a) + b) + this.X[0]) - 373897302, S24) + c;
        a = rotateLeft(((G(b, c, d) + a) + this.X[S21]) - 701558691, S21) + b;
        d = rotateLeft(((G(a, b, c) + d) + this.X[S42]) + 38016083, S22) + a;
        c = rotateLeft(((G(d, a, b) + c) + this.X[S43]) - 660478335, S23) + d;
        b = rotateLeft(((G(c, d, a) + b) + this.X[S31]) - 405537848, S24) + c;
        a = rotateLeft(((G(b, c, d) + a) + this.X[S22]) + 568446438, S21) + b;
        d = rotateLeft(((G(a, b, c) + d) + this.X[S23]) - 1019803690, S22) + a;
        c = rotateLeft(((G(d, a, b) + c) + this.X[3]) - 187363961, S23) + d;
        b = rotateLeft(((G(c, d, a) + b) + this.X[8]) + 1163531501, S24) + c;
        a = rotateLeft(((G(b, c, d) + a) + this.X[13]) - 1444681467, S21) + b;
        d = rotateLeft(((G(a, b, c) + d) + this.X[2]) - 51403784, S22) + a;
        c = rotateLeft(((G(d, a, b) + c) + this.X[S11]) + 1735328473, S23) + d;
        b = rotateLeft(((G(c, d, a) + b) + this.X[S12]) - 1926607734, S24) + c;
        a = rotateLeft(((H(b, c, d) + a) + this.X[S21]) - 378558, S31) + b;
        d = rotateLeft(((H(a, b, c) + d) + this.X[8]) - 2022574463, S32) + a;
        c = rotateLeft(((H(d, a, b) + c) + this.X[S32]) + 1839030562, S33) + d;
        b = rotateLeft(((H(c, d, a) + b) + this.X[S23]) - 35309556, S34) + c;
        a = rotateLeft(((H(b, c, d) + a) + this.X[1]) - 1530992060, S31) + b;
        d = rotateLeft(((H(a, b, c) + d) + this.X[S31]) + 1272893353, S32) + a;
        c = rotateLeft(((H(d, a, b) + c) + this.X[S11]) - 155497632, S33) + d;
        b = rotateLeft(((H(c, d, a) + b) + this.X[S42]) - 1094730640, S34) + c;
        a = rotateLeft(((H(b, c, d) + a) + this.X[13]) + 681279174, S31) + b;
        d = rotateLeft(((H(a, b, c) + d) + this.X[0]) - 358537222, S32) + a;
        c = rotateLeft(((H(d, a, b) + c) + this.X[3]) - 722521979, S33) + d;
        b = rotateLeft(((H(c, d, a) + b) + this.X[S41]) + 76029189, S34) + c;
        a = rotateLeft(((H(b, c, d) + a) + this.X[S22]) - 640364487, S31) + b;
        d = rotateLeft(((H(a, b, c) + d) + this.X[S12]) - 421815835, S32) + a;
        c = rotateLeft(((H(d, a, b) + c) + this.X[S43]) + 530742520, S33) + d;
        b = rotateLeft(((H(c, d, a) + b) + this.X[2]) - 995338651, S34) + c;
        a = rotateLeft(((K(b, c, d) + a) + this.X[0]) - 198630844, S41) + b;
        d = rotateLeft(((K(a, b, c) + d) + this.X[S11]) + 1126891415, S42) + a;
        c = rotateLeft(((K(d, a, b) + c) + this.X[S23]) - 1416354905, S43) + d;
        b = rotateLeft(((K(c, d, a) + b) + this.X[S21]) - 57434055, S44) + c;
        a = rotateLeft(((K(b, c, d) + a) + this.X[S12]) + 1700485571, S41) + b;
        d = rotateLeft(((K(a, b, c) + d) + this.X[3]) - 1894986606, S42) + a;
        c = rotateLeft(((K(d, a, b) + c) + this.X[S42]) - 1051523, S43) + d;
        b = rotateLeft(((K(c, d, a) + b) + this.X[1]) - 2054922799, S44) + c;
        a = rotateLeft(((K(b, c, d) + a) + this.X[8]) + 1873313359, S41) + b;
        d = rotateLeft(((K(a, b, c) + d) + this.X[S43]) - 30611744, S42) + a;
        c = rotateLeft(((K(d, a, b) + c) + this.X[S41]) - 1560198380, S43) + d;
        b = rotateLeft(((K(c, d, a) + b) + this.X[13]) + 1309151649, S44) + c;
        a = rotateLeft(((K(b, c, d) + a) + this.X[S31]) - 145523070, S41) + b;
        d = rotateLeft(((K(a, b, c) + d) + this.X[S32]) - 1120210379, S42) + a;
        c = rotateLeft(((K(d, a, b) + c) + this.X[2]) + 718787259, S43) + d;
        b = rotateLeft(((K(c, d, a) + b) + this.X[S22]) - 343485551, S44) + c;
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
}
