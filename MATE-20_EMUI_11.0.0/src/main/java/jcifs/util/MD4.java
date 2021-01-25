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

    @Override // java.security.MessageDigest, java.security.MessageDigestSpi, java.lang.Object
    public Object clone() {
        return new MD4(this);
    }

    @Override // java.security.MessageDigestSpi
    public void engineReset() {
        this.context[0] = 1732584193;
        this.context[1] = -271733879;
        this.context[2] = -1732584194;
        this.context[3] = 271733878;
        this.count = 0;
        for (int i = 0; i < 64; i++) {
            this.buffer[i] = 0;
        }
    }

    @Override // java.security.MessageDigestSpi
    public void engineUpdate(byte b) {
        int i = (int) (this.count % 64);
        this.count++;
        this.buffer[i] = b;
        if (i == 63) {
            transform(this.buffer, 0);
        }
    }

    @Override // java.security.MessageDigestSpi
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

    @Override // java.security.MessageDigestSpi
    public byte[] engineDigest() {
        int bufferNdx = (int) (this.count % 64);
        int padLen = bufferNdx < 56 ? 56 - bufferNdx : 120 - bufferNdx;
        byte[] tail = new byte[(padLen + 8)];
        tail[0] = Byte.MIN_VALUE;
        for (int i = 0; i < 8; i++) {
            tail[padLen + i] = (byte) ((int) ((this.count * 8) >>> (i * 8)));
        }
        engineUpdate(tail, 0, tail.length);
        byte[] result = new byte[16];
        for (int i2 = 0; i2 < 4; i2++) {
            for (int j = 0; j < 4; j++) {
                result[(i2 * 4) + j] = (byte) (this.context[i2] >>> (j * 8));
            }
        }
        engineReset();
        return result;
    }

    private void transform(byte[] block, int offset) {
        int offset2 = offset;
        for (int i = 0; i < 16; i++) {
            int offset3 = offset2 + 1;
            int offset4 = offset3 + 1;
            int offset5 = offset4 + 1;
            offset2 = offset5 + 1;
            this.X[i] = (block[offset2] & 255) | ((block[offset3] & 255) << 8) | ((block[offset4] & 255) << 16) | ((block[offset5] & 255) << 24);
        }
        int A = this.context[0];
        int B = this.context[1];
        int C = this.context[2];
        int D = this.context[3];
        int A2 = FF(A, B, C, D, this.X[0], 3);
        int D2 = FF(D, A2, B, C, this.X[1], 7);
        int C2 = FF(C, D2, A2, B, this.X[2], 11);
        int B2 = FF(B, C2, D2, A2, this.X[3], 19);
        int A3 = FF(A2, B2, C2, D2, this.X[4], 3);
        int D3 = FF(D2, A3, B2, C2, this.X[5], 7);
        int C3 = FF(C2, D3, A3, B2, this.X[6], 11);
        int B3 = FF(B2, C3, D3, A3, this.X[7], 19);
        int A4 = FF(A3, B3, C3, D3, this.X[8], 3);
        int D4 = FF(D3, A4, B3, C3, this.X[9], 7);
        int C4 = FF(C3, D4, A4, B3, this.X[10], 11);
        int B4 = FF(B3, C4, D4, A4, this.X[11], 19);
        int A5 = FF(A4, B4, C4, D4, this.X[12], 3);
        int D5 = FF(D4, A5, B4, C4, this.X[13], 7);
        int C5 = FF(C4, D5, A5, B4, this.X[14], 11);
        int B5 = FF(B4, C5, D5, A5, this.X[15], 19);
        int A6 = GG(A5, B5, C5, D5, this.X[0], 3);
        int D6 = GG(D5, A6, B5, C5, this.X[4], 5);
        int C6 = GG(C5, D6, A6, B5, this.X[8], 9);
        int B6 = GG(B5, C6, D6, A6, this.X[12], 13);
        int A7 = GG(A6, B6, C6, D6, this.X[1], 3);
        int D7 = GG(D6, A7, B6, C6, this.X[5], 5);
        int C7 = GG(C6, D7, A7, B6, this.X[9], 9);
        int B7 = GG(B6, C7, D7, A7, this.X[13], 13);
        int A8 = GG(A7, B7, C7, D7, this.X[2], 3);
        int D8 = GG(D7, A8, B7, C7, this.X[6], 5);
        int C8 = GG(C7, D8, A8, B7, this.X[10], 9);
        int B8 = GG(B7, C8, D8, A8, this.X[14], 13);
        int A9 = GG(A8, B8, C8, D8, this.X[3], 3);
        int D9 = GG(D8, A9, B8, C8, this.X[7], 5);
        int C9 = GG(C8, D9, A9, B8, this.X[11], 9);
        int B9 = GG(B8, C9, D9, A9, this.X[15], 13);
        int A10 = HH(A9, B9, C9, D9, this.X[0], 3);
        int D10 = HH(D9, A10, B9, C9, this.X[8], 9);
        int C10 = HH(C9, D10, A10, B9, this.X[4], 11);
        int B10 = HH(B9, C10, D10, A10, this.X[12], 15);
        int A11 = HH(A10, B10, C10, D10, this.X[2], 3);
        int D11 = HH(D10, A11, B10, C10, this.X[10], 9);
        int C11 = HH(C10, D11, A11, B10, this.X[6], 11);
        int B11 = HH(B10, C11, D11, A11, this.X[14], 15);
        int A12 = HH(A11, B11, C11, D11, this.X[1], 3);
        int D12 = HH(D11, A12, B11, C11, this.X[9], 9);
        int C12 = HH(C11, D12, A12, B11, this.X[5], 11);
        int B12 = HH(B11, C12, D12, A12, this.X[13], 15);
        int A13 = HH(A12, B12, C12, D12, this.X[3], 3);
        int D13 = HH(D12, A13, B12, C12, this.X[11], 9);
        int C13 = HH(C12, D13, A13, B12, this.X[7], 11);
        int B13 = HH(B12, C13, D13, A13, this.X[15], 15);
        int[] iArr = this.context;
        iArr[0] = iArr[0] + A13;
        int[] iArr2 = this.context;
        iArr2[1] = iArr2[1] + B13;
        int[] iArr3 = this.context;
        iArr3[2] = iArr3[2] + C13;
        int[] iArr4 = this.context;
        iArr4[3] = iArr4[3] + D13;
    }

    private int FF(int a, int b, int c, int d, int x, int s) {
        int t = ((b & c) | ((b ^ -1) & d)) + a + x;
        return (t << s) | (t >>> (32 - s));
    }

    private int GG(int a, int b, int c, int d, int x, int s) {
        int t = (((c | d) & b) | (c & d)) + a + x + 1518500249;
        return (t << s) | (t >>> (32 - s));
    }

    private int HH(int a, int b, int c, int d, int x, int s) {
        int t = ((b ^ c) ^ d) + a + x + 1859775393;
        return (t << s) | (t >>> (32 - s));
    }
}
