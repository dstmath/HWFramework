package com.android.org.bouncycastle.crypto.digests;

import com.android.org.bouncycastle.util.Memoable;
import com.android.org.bouncycastle.util.Pack;

public class SHA256Digest extends GeneralDigest implements EncodableDigest {
    private static final int DIGEST_LENGTH = 32;
    static final int[] K = new int[]{1116352408, 1899447441, -1245643825, -373957723, 961987163, 1508970993, -1841331548, -1424204075, -670586216, 310598401, 607225278, 1426881987, 1925078388, -2132889090, -1680079193, -1046744716, -459576895, -272742522, 264347078, 604807628, 770255983, 1249150122, 1555081692, 1996064986, -1740746414, -1473132947, -1341970488, -1084653625, -958395405, -710438585, 113926993, 338241895, 666307205, 773529912, 1294757372, 1396182291, 1695183700, 1986661051, -2117940946, -1838011259, -1564481375, -1474664885, -1035236496, -949202525, -778901479, -694614492, -200395387, 275423344, 430227734, 506948616, 659060556, 883997877, 958139571, 1322822218, 1537002063, 1747873779, 1955562222, 2024104815, -2067236844, -1933114872, -1866530822, -1538233109, -1090935817, -965641998};
    private int H1;
    private int H2;
    private int H3;
    private int H4;
    private int H5;
    private int H6;
    private int H7;
    private int H8;
    private int[] X;
    private int xOff;

    public SHA256Digest() {
        this.X = new int[64];
        reset();
    }

    public SHA256Digest(SHA256Digest t) {
        super((GeneralDigest) t);
        this.X = new int[64];
        copyIn(t);
    }

    private void copyIn(SHA256Digest t) {
        super.copyIn(t);
        this.H1 = t.H1;
        this.H2 = t.H2;
        this.H3 = t.H3;
        this.H4 = t.H4;
        this.H5 = t.H5;
        this.H6 = t.H6;
        this.H7 = t.H7;
        this.H8 = t.H8;
        System.arraycopy(t.X, 0, this.X, 0, t.X.length);
        this.xOff = t.xOff;
    }

    public SHA256Digest(byte[] encodedState) {
        super(encodedState);
        this.X = new int[64];
        this.H1 = Pack.bigEndianToInt(encodedState, 16);
        this.H2 = Pack.bigEndianToInt(encodedState, 20);
        this.H3 = Pack.bigEndianToInt(encodedState, 24);
        this.H4 = Pack.bigEndianToInt(encodedState, 28);
        this.H5 = Pack.bigEndianToInt(encodedState, 32);
        this.H6 = Pack.bigEndianToInt(encodedState, 36);
        this.H7 = Pack.bigEndianToInt(encodedState, 40);
        this.H8 = Pack.bigEndianToInt(encodedState, 44);
        this.xOff = Pack.bigEndianToInt(encodedState, 48);
        for (int i = 0; i != this.xOff; i++) {
            this.X[i] = Pack.bigEndianToInt(encodedState, (i * 4) + 52);
        }
    }

    public String getAlgorithmName() {
        return "SHA-256";
    }

    public int getDigestSize() {
        return 32;
    }

    protected void processWord(byte[] in, int inOff) {
        inOff++;
        inOff++;
        this.X[this.xOff] = (((in[inOff] << 24) | ((in[inOff] & 255) << 16)) | ((in[inOff] & 255) << 8)) | (in[inOff + 1] & 255);
        int i = this.xOff + 1;
        this.xOff = i;
        if (i == 16) {
            processBlock();
        }
    }

    protected void processLength(long bitLength) {
        if (this.xOff > 14) {
            processBlock();
        }
        this.X[14] = (int) (bitLength >>> 32);
        this.X[15] = (int) (-1 & bitLength);
    }

    public int doFinal(byte[] out, int outOff) {
        finish();
        Pack.intToBigEndian(this.H1, out, outOff);
        Pack.intToBigEndian(this.H2, out, outOff + 4);
        Pack.intToBigEndian(this.H3, out, outOff + 8);
        Pack.intToBigEndian(this.H4, out, outOff + 12);
        Pack.intToBigEndian(this.H5, out, outOff + 16);
        Pack.intToBigEndian(this.H6, out, outOff + 20);
        Pack.intToBigEndian(this.H7, out, outOff + 24);
        Pack.intToBigEndian(this.H8, out, outOff + 28);
        reset();
        return 32;
    }

    public void reset() {
        super.reset();
        this.H1 = 1779033703;
        this.H2 = -1150833019;
        this.H3 = 1013904242;
        this.H4 = -1521486534;
        this.H5 = 1359893119;
        this.H6 = -1694144372;
        this.H7 = 528734635;
        this.H8 = 1541459225;
        this.xOff = 0;
        for (int i = 0; i != this.X.length; i++) {
            this.X[i] = 0;
        }
    }

    protected void processBlock() {
        int t;
        int i;
        for (t = 16; t <= 63; t++) {
            this.X[t] = ((Theta1(this.X[t - 2]) + this.X[t - 7]) + Theta0(this.X[t - 15])) + this.X[t - 16];
        }
        int a = this.H1;
        int b = this.H2;
        int c = this.H3;
        int d = this.H4;
        int e = this.H5;
        int f = this.H6;
        int g = this.H7;
        int h = this.H8;
        t = 0;
        for (i = 0; i < 8; i++) {
            h += ((Sum1(e) + Ch(e, f, g)) + K[t]) + this.X[t];
            d += h;
            h += Sum0(a) + Maj(a, b, c);
            t++;
            g += ((Sum1(d) + Ch(d, e, f)) + K[t]) + this.X[t];
            c += g;
            g += Sum0(h) + Maj(h, a, b);
            t++;
            f += ((Sum1(c) + Ch(c, d, e)) + K[t]) + this.X[t];
            b += f;
            f += Sum0(g) + Maj(g, h, a);
            t++;
            e += ((Sum1(b) + Ch(b, c, d)) + K[t]) + this.X[t];
            a += e;
            e += Sum0(f) + Maj(f, g, h);
            t++;
            d += ((Sum1(a) + Ch(a, b, c)) + K[t]) + this.X[t];
            h += d;
            d += Sum0(e) + Maj(e, f, g);
            t++;
            c += ((Sum1(h) + Ch(h, a, b)) + K[t]) + this.X[t];
            g += c;
            c += Sum0(d) + Maj(d, e, f);
            t++;
            b += ((Sum1(g) + Ch(g, h, a)) + K[t]) + this.X[t];
            f += b;
            b += Sum0(c) + Maj(c, d, e);
            t++;
            a += ((Sum1(f) + Ch(f, g, h)) + K[t]) + this.X[t];
            e += a;
            a += Sum0(b) + Maj(b, c, d);
            t++;
        }
        this.H1 += a;
        this.H2 += b;
        this.H3 += c;
        this.H4 += d;
        this.H5 += e;
        this.H6 += f;
        this.H7 += g;
        this.H8 += h;
        this.xOff = 0;
        for (i = 0; i < 16; i++) {
            this.X[i] = 0;
        }
    }

    private int Ch(int x, int y, int z) {
        return (x & y) ^ ((~x) & z);
    }

    private int Maj(int x, int y, int z) {
        return ((x & y) ^ (x & z)) ^ (y & z);
    }

    private int Sum0(int x) {
        return (((x >>> 2) | (x << 30)) ^ ((x >>> 13) | (x << 19))) ^ ((x >>> 22) | (x << 10));
    }

    private int Sum1(int x) {
        return (((x >>> 6) | (x << 26)) ^ ((x >>> 11) | (x << 21))) ^ ((x >>> 25) | (x << 7));
    }

    private int Theta0(int x) {
        return (((x >>> 7) | (x << 25)) ^ ((x >>> 18) | (x << 14))) ^ (x >>> 3);
    }

    private int Theta1(int x) {
        return (((x >>> 17) | (x << 15)) ^ ((x >>> 19) | (x << 13))) ^ (x >>> 10);
    }

    public Memoable copy() {
        return new SHA256Digest(this);
    }

    public void reset(Memoable other) {
        copyIn((SHA256Digest) other);
    }

    public byte[] getEncodedState() {
        byte[] state = new byte[((this.xOff * 4) + 52)];
        super.populateState(state);
        Pack.intToBigEndian(this.H1, state, 16);
        Pack.intToBigEndian(this.H2, state, 20);
        Pack.intToBigEndian(this.H3, state, 24);
        Pack.intToBigEndian(this.H4, state, 28);
        Pack.intToBigEndian(this.H5, state, 32);
        Pack.intToBigEndian(this.H6, state, 36);
        Pack.intToBigEndian(this.H7, state, 40);
        Pack.intToBigEndian(this.H8, state, 44);
        Pack.intToBigEndian(this.xOff, state, 48);
        for (int i = 0; i != this.xOff; i++) {
            Pack.intToBigEndian(this.X[i], state, (i * 4) + 52);
        }
        return state;
    }
}
