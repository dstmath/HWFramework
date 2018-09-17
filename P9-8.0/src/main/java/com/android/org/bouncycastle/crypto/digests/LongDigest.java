package com.android.org.bouncycastle.crypto.digests;

import com.android.org.bouncycastle.crypto.ExtendedDigest;
import com.android.org.bouncycastle.util.Memoable;
import com.android.org.bouncycastle.util.Pack;

public abstract class LongDigest implements ExtendedDigest, Memoable, EncodableDigest {
    private static final int BYTE_LENGTH = 128;
    static final long[] K = new long[]{4794697086780616226L, 8158064640168781261L, -5349999486874862801L, -1606136188198331460L, 4131703408338449720L, 6480981068601479193L, -7908458776815382629L, -6116909921290321640L, -2880145864133508542L, 1334009975649890238L, 2608012711638119052L, 6128411473006802146L, 8268148722764581231L, -9160688886553864527L, -7215885187991268811L, -4495734319001033068L, -1973867731355612462L, -1171420211273849373L, 1135362057144423861L, 2597628984639134821L, 3308224258029322869L, 5365058923640841347L, 6679025012923562964L, 8573033837759648693L, -7476448914759557205L, -6327057829258317296L, -5763719355590565569L, -4658551843659510044L, -4116276920077217854L, -3051310485924567259L, 489312712824947311L, 1452737877330783856L, 2861767655752347644L, 3322285676063803686L, 5560940570517711597L, 5996557281743188959L, 7280758554555802590L, 8532644243296465576L, -9096487096722542874L, -7894198246740708037L, -6719396339535248540L, -6333637450476146687L, -4446306890439682159L, -4076793802049405392L, -3345356375505022440L, -2983346525034927856L, -860691631967231958L, 1182934255886127544L, 1847814050463011016L, 2177327727835720531L, 2830643537854262169L, 3796741975233480872L, 4115178125766777443L, 5681478168544905931L, 6601373596472566643L, 7507060721942968483L, 8399075790359081724L, 8693463985226723168L, -8878714635349349518L, -8302665154208450068L, -8016688836872298968L, -6606660893046293015L, -4685533653050689259L, -4147400797238176981L, -3880063495543823972L, -3348786107499101689L, -1523767162380948706L, -757361751448694408L, 500013540394364858L, 748580250866718886L, 1242879168328830382L, 1977374033974150939L, 2944078676154940804L, 3659926193048069267L, 4368137639120453308L, 4836135668995329356L, 5532061633213252278L, 6448918945643986474L, 6902733635092675308L, 7801388544844847127L};
    protected long H1;
    protected long H2;
    protected long H3;
    protected long H4;
    protected long H5;
    protected long H6;
    protected long H7;
    protected long H8;
    private long[] W;
    private long byteCount1;
    private long byteCount2;
    private int wOff;
    private byte[] xBuf;
    private int xBufOff;

    protected LongDigest() {
        this.xBuf = new byte[8];
        this.W = new long[80];
        this.xBufOff = 0;
        reset();
    }

    protected LongDigest(LongDigest t) {
        this.xBuf = new byte[8];
        this.W = new long[80];
        copyIn(t);
    }

    protected void copyIn(LongDigest t) {
        System.arraycopy(t.xBuf, 0, this.xBuf, 0, t.xBuf.length);
        this.xBufOff = t.xBufOff;
        this.byteCount1 = t.byteCount1;
        this.byteCount2 = t.byteCount2;
        this.H1 = t.H1;
        this.H2 = t.H2;
        this.H3 = t.H3;
        this.H4 = t.H4;
        this.H5 = t.H5;
        this.H6 = t.H6;
        this.H7 = t.H7;
        this.H8 = t.H8;
        System.arraycopy(t.W, 0, this.W, 0, t.W.length);
        this.wOff = t.wOff;
    }

    protected void populateState(byte[] state) {
        System.arraycopy(this.xBuf, 0, state, 0, this.xBufOff);
        Pack.intToBigEndian(this.xBufOff, state, 8);
        Pack.longToBigEndian(this.byteCount1, state, 12);
        Pack.longToBigEndian(this.byteCount2, state, 20);
        Pack.longToBigEndian(this.H1, state, 28);
        Pack.longToBigEndian(this.H2, state, 36);
        Pack.longToBigEndian(this.H3, state, 44);
        Pack.longToBigEndian(this.H4, state, 52);
        Pack.longToBigEndian(this.H5, state, 60);
        Pack.longToBigEndian(this.H6, state, 68);
        Pack.longToBigEndian(this.H7, state, 76);
        Pack.longToBigEndian(this.H8, state, 84);
        Pack.intToBigEndian(this.wOff, state, 92);
        for (int i = 0; i < this.wOff; i++) {
            Pack.longToBigEndian(this.W[i], state, (i * 8) + 96);
        }
    }

    protected void restoreState(byte[] encodedState) {
        this.xBufOff = Pack.bigEndianToInt(encodedState, 8);
        System.arraycopy(encodedState, 0, this.xBuf, 0, this.xBufOff);
        this.byteCount1 = Pack.bigEndianToLong(encodedState, 12);
        this.byteCount2 = Pack.bigEndianToLong(encodedState, 20);
        this.H1 = Pack.bigEndianToLong(encodedState, 28);
        this.H2 = Pack.bigEndianToLong(encodedState, 36);
        this.H3 = Pack.bigEndianToLong(encodedState, 44);
        this.H4 = Pack.bigEndianToLong(encodedState, 52);
        this.H5 = Pack.bigEndianToLong(encodedState, 60);
        this.H6 = Pack.bigEndianToLong(encodedState, 68);
        this.H7 = Pack.bigEndianToLong(encodedState, 76);
        this.H8 = Pack.bigEndianToLong(encodedState, 84);
        this.wOff = Pack.bigEndianToInt(encodedState, 92);
        for (int i = 0; i < this.wOff; i++) {
            this.W[i] = Pack.bigEndianToLong(encodedState, (i * 8) + 96);
        }
    }

    protected int getEncodedStateSize() {
        return (this.wOff * 8) + 96;
    }

    public void update(byte in) {
        byte[] bArr = this.xBuf;
        int i = this.xBufOff;
        this.xBufOff = i + 1;
        bArr[i] = in;
        if (this.xBufOff == this.xBuf.length) {
            processWord(this.xBuf, 0);
            this.xBufOff = 0;
        }
        this.byteCount1++;
    }

    public void update(byte[] in, int inOff, int len) {
        while (this.xBufOff != 0 && len > 0) {
            update(in[inOff]);
            inOff++;
            len--;
        }
        while (len > this.xBuf.length) {
            processWord(in, inOff);
            inOff += this.xBuf.length;
            len -= this.xBuf.length;
            this.byteCount1 += (long) this.xBuf.length;
        }
        while (len > 0) {
            update(in[inOff]);
            inOff++;
            len--;
        }
    }

    public void finish() {
        adjustByteCounts();
        long lowBitLength = this.byteCount1 << 3;
        long hiBitLength = this.byteCount2;
        update(Byte.MIN_VALUE);
        while (this.xBufOff != 0) {
            update((byte) 0);
        }
        processLength(lowBitLength, hiBitLength);
        processBlock();
    }

    public void reset() {
        int i;
        this.byteCount1 = 0;
        this.byteCount2 = 0;
        this.xBufOff = 0;
        for (i = 0; i < this.xBuf.length; i++) {
            this.xBuf[i] = (byte) 0;
        }
        this.wOff = 0;
        for (i = 0; i != this.W.length; i++) {
            this.W[i] = 0;
        }
    }

    public int getByteLength() {
        return 128;
    }

    protected void processWord(byte[] in, int inOff) {
        this.W[this.wOff] = Pack.bigEndianToLong(in, inOff);
        int i = this.wOff + 1;
        this.wOff = i;
        if (i == 16) {
            processBlock();
        }
    }

    private void adjustByteCounts() {
        if (this.byteCount1 > 2305843009213693951L) {
            this.byteCount2 += this.byteCount1 >>> 61;
            this.byteCount1 &= 2305843009213693951L;
        }
    }

    protected void processLength(long lowW, long hiW) {
        if (this.wOff > 14) {
            processBlock();
        }
        this.W[14] = hiW;
        this.W[15] = lowW;
    }

    protected void processBlock() {
        int t;
        int i;
        adjustByteCounts();
        for (t = 16; t <= 79; t++) {
            this.W[t] = ((Sigma1(this.W[t - 2]) + this.W[t - 7]) + Sigma0(this.W[t - 15])) + this.W[t - 16];
        }
        long a = this.H1;
        long b = this.H2;
        long c = this.H3;
        long d = this.H4;
        long e = this.H5;
        long f = this.H6;
        long g = this.H7;
        long h = this.H8;
        int t2 = 0;
        for (i = 0; i < 10; i++) {
            t = t2 + 1;
            h += ((Sum1(e) + Ch(e, f, g)) + K[t2]) + this.W[t2];
            d += h;
            long h2 = h + (Sum0(a) + Maj(a, b, c));
            t2 = t + 1;
            g += ((Ch(d, e, f) + Sum1(d)) + K[t]) + this.W[t];
            c += g;
            g += Maj(h2, a, b) + Sum0(h2);
            t = t2 + 1;
            f += ((Ch(c, d, e) + Sum1(c)) + K[t2]) + this.W[t2];
            b += f;
            f += Maj(g, h2, a) + Sum0(g);
            t2 = t + 1;
            e += ((Ch(b, c, d) + Sum1(b)) + K[t]) + this.W[t];
            a += e;
            e += Maj(f, g, h2) + Sum0(f);
            t = t2 + 1;
            d += ((Sum1(a) + Ch(a, b, c)) + K[t2]) + this.W[t2];
            h = h2 + d;
            d += Sum0(e) + Maj(e, f, g);
            t2 = t + 1;
            c += ((Ch(h, a, b) + Sum1(h)) + K[t]) + this.W[t];
            g += c;
            c += Sum0(d) + Maj(d, e, f);
            t = t2 + 1;
            b += ((Sum1(g) + Ch(g, h, a)) + K[t2]) + this.W[t2];
            f += b;
            b += Sum0(c) + Maj(c, d, e);
            t2 = t + 1;
            a += ((Sum1(f) + Ch(f, g, h)) + K[t]) + this.W[t];
            e += a;
            a += Sum0(b) + Maj(b, c, d);
        }
        this.H1 += a;
        this.H2 += b;
        this.H3 += c;
        this.H4 += d;
        this.H5 += e;
        this.H6 += f;
        this.H7 += g;
        this.H8 += h;
        this.wOff = 0;
        for (i = 0; i < 16; i++) {
            this.W[i] = 0;
        }
    }

    private long Ch(long x, long y, long z) {
        return (x & y) ^ ((~x) & z);
    }

    private long Maj(long x, long y, long z) {
        return ((x & y) ^ (x & z)) ^ (y & z);
    }

    private long Sum0(long x) {
        return (((x << 36) | (x >>> 28)) ^ ((x << 30) | (x >>> 34))) ^ ((x << 25) | (x >>> 39));
    }

    private long Sum1(long x) {
        return (((x << 50) | (x >>> 14)) ^ ((x << 46) | (x >>> 18))) ^ ((x << 23) | (x >>> 41));
    }

    private long Sigma0(long x) {
        return (((x << 63) | (x >>> 1)) ^ ((x << 56) | (x >>> 8))) ^ (x >>> 7);
    }

    private long Sigma1(long x) {
        return (((x << 45) | (x >>> 19)) ^ ((x << 3) | (x >>> 61))) ^ (x >>> 6);
    }
}
