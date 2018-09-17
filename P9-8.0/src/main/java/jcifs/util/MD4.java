package jcifs.util;

import java.security.MessageDigest;

public class MD4 extends MessageDigest implements Cloneable {
    private static final int BLOCK_LENGTH = 64;
    private int[] X;
    private byte[] buffer;
    private int[] context;
    private long count;

    public MD4() {
        super("MD4");
        this.context = new int[4];
        this.buffer = new byte[64];
        this.X = new int[16];
        engineReset();
    }

    private MD4(MD4 md) {
        this();
        this.context = (int[]) md.context.clone();
        this.buffer = (byte[]) md.buffer.clone();
        this.count = md.count;
    }

    public Object clone() {
        return new MD4(this);
    }

    public void engineReset() {
        this.context[0] = 1732584193;
        this.context[1] = -271733879;
        this.context[2] = -1732584194;
        this.context[3] = 271733878;
        this.count = 0;
        for (int i = 0; i < 64; i++) {
            this.buffer[i] = (byte) 0;
        }
    }

    public void engineUpdate(byte b) {
        int i = (int) (this.count % 64);
        this.count++;
        this.buffer[i] = b;
        if (i == 63) {
            transform(this.buffer, 0);
        }
    }

    public void engineUpdate(byte[] input, int offset, int len) {
        if (offset < 0 || len < 0 || ((long) offset) + ((long) len) > ((long) input.length)) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int bufferNdx = (int) (this.count % 64);
        this.count += (long) len;
        int partLen = 64 - bufferNdx;
        int i = 0;
        if (len >= partLen) {
            System.arraycopy(input, offset, this.buffer, bufferNdx, partLen);
            transform(this.buffer, 0);
            i = partLen;
            while ((i + 64) - 1 < len) {
                transform(input, offset + i);
                i += 64;
            }
            bufferNdx = 0;
        }
        if (i < len) {
            System.arraycopy(input, offset + i, this.buffer, bufferNdx, len - i);
        }
    }

    public byte[] engineDigest() {
        int i;
        int bufferNdx = (int) (this.count % 64);
        int padLen = bufferNdx < 56 ? 56 - bufferNdx : 120 - bufferNdx;
        byte[] tail = new byte[(padLen + 8)];
        tail[0] = Byte.MIN_VALUE;
        for (i = 0; i < 8; i++) {
            tail[padLen + i] = (byte) ((int) ((this.count * 8) >>> (i * 8)));
        }
        engineUpdate(tail, 0, tail.length);
        byte[] result = new byte[16];
        for (i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[(i * 4) + j] = (byte) (this.context[i] >>> (j * 8));
            }
        }
        engineReset();
        return result;
    }

    private void transform(byte[] block, int offset) {
        int offset2 = offset;
        for (int i = 0; i < 16; i++) {
            offset = offset2 + 1;
            offset2 = offset + 1;
            offset = offset2 + 1;
            offset2 = offset + 1;
            this.X[i] = (((block[offset2] & 255) | ((block[offset] & 255) << 8)) | ((block[offset2] & 255) << 16)) | ((block[offset] & 255) << 24);
        }
        int A = this.context[0];
        int B = this.context[1];
        int C = this.context[2];
        int D = this.context[3];
        A = FF(A, B, C, D, this.X[0], 3);
        D = FF(D, A, B, C, this.X[1], 7);
        C = FF(C, D, A, B, this.X[2], 11);
        B = FF(B, C, D, A, this.X[3], 19);
        A = FF(A, B, C, D, this.X[4], 3);
        D = FF(D, A, B, C, this.X[5], 7);
        C = FF(C, D, A, B, this.X[6], 11);
        B = FF(B, C, D, A, this.X[7], 19);
        A = FF(A, B, C, D, this.X[8], 3);
        D = FF(D, A, B, C, this.X[9], 7);
        C = FF(C, D, A, B, this.X[10], 11);
        B = FF(B, C, D, A, this.X[11], 19);
        A = FF(A, B, C, D, this.X[12], 3);
        D = FF(D, A, B, C, this.X[13], 7);
        C = FF(C, D, A, B, this.X[14], 11);
        B = FF(B, C, D, A, this.X[15], 19);
        A = GG(A, B, C, D, this.X[0], 3);
        D = GG(D, A, B, C, this.X[4], 5);
        C = GG(C, D, A, B, this.X[8], 9);
        B = GG(B, C, D, A, this.X[12], 13);
        A = GG(A, B, C, D, this.X[1], 3);
        D = GG(D, A, B, C, this.X[5], 5);
        C = GG(C, D, A, B, this.X[9], 9);
        B = GG(B, C, D, A, this.X[13], 13);
        A = GG(A, B, C, D, this.X[2], 3);
        D = GG(D, A, B, C, this.X[6], 5);
        C = GG(C, D, A, B, this.X[10], 9);
        B = GG(B, C, D, A, this.X[14], 13);
        A = GG(A, B, C, D, this.X[3], 3);
        D = GG(D, A, B, C, this.X[7], 5);
        C = GG(C, D, A, B, this.X[11], 9);
        B = GG(B, C, D, A, this.X[15], 13);
        A = HH(A, B, C, D, this.X[0], 3);
        D = HH(D, A, B, C, this.X[8], 9);
        C = HH(C, D, A, B, this.X[4], 11);
        B = HH(B, C, D, A, this.X[12], 15);
        A = HH(A, B, C, D, this.X[2], 3);
        D = HH(D, A, B, C, this.X[10], 9);
        C = HH(C, D, A, B, this.X[6], 11);
        B = HH(B, C, D, A, this.X[14], 15);
        A = HH(A, B, C, D, this.X[1], 3);
        D = HH(D, A, B, C, this.X[9], 9);
        C = HH(C, D, A, B, this.X[5], 11);
        B = HH(B, C, D, A, this.X[13], 15);
        A = HH(A, B, C, D, this.X[3], 3);
        D = HH(D, A, B, C, this.X[11], 9);
        C = HH(C, D, A, B, this.X[7], 11);
        B = HH(B, C, D, A, this.X[15], 15);
        int[] iArr = this.context;
        iArr[0] = iArr[0] + A;
        iArr = this.context;
        iArr[1] = iArr[1] + B;
        iArr = this.context;
        iArr[2] = iArr[2] + C;
        iArr = this.context;
        iArr[3] = iArr[3] + D;
    }

    private int FF(int a, int b, int c, int d, int x, int s) {
        int t = (((b & c) | ((b ^ -1) & d)) + a) + x;
        return (t << s) | (t >>> (32 - s));
    }

    private int GG(int a, int b, int c, int d, int x, int s) {
        int t = (((((c | d) & b) | (c & d)) + a) + x) + 1518500249;
        return (t << s) | (t >>> (32 - s));
    }

    private int HH(int a, int b, int c, int d, int x, int s) {
        int t = ((((b ^ c) ^ d) + a) + x) + 1859775393;
        return (t << s) | (t >>> (32 - s));
    }
}
