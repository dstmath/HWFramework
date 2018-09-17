package com.android.internal.util;

public class BitwiseInputStream {
    private byte[] mBuf;
    private int mEnd;
    private int mPos = 0;

    public static class AccessException extends Exception {
        public AccessException(String s) {
            super("BitwiseInputStream access failed: " + s);
        }
    }

    public BitwiseInputStream(byte[] buf) {
        this.mBuf = buf;
        this.mEnd = buf.length << 3;
    }

    public int available() {
        return this.mEnd - this.mPos;
    }

    public int read(int bits) throws AccessException {
        int index = this.mPos >>> 3;
        int offset = (16 - (this.mPos & 7)) - bits;
        if (bits < 0 || bits > 8 || this.mPos + bits > this.mEnd) {
            throw new AccessException("illegal read (pos " + this.mPos + ", end " + this.mEnd + ", bits " + bits + ")");
        }
        int data = (this.mBuf[index] & 255) << 8;
        if (offset < 8) {
            data |= this.mBuf[index + 1] & 255;
        }
        data = (data >>> offset) & (-1 >>> (32 - bits));
        this.mPos += bits;
        return data;
    }

    public byte[] readByteArray(int bits) throws AccessException {
        int i = 0;
        int i2 = bits >>> 3;
        if ((bits & 7) > 0) {
            i = 1;
        }
        int bytes = i2 + i;
        byte[] arr = new byte[bytes];
        for (int i3 = 0; i3 < bytes; i3++) {
            int increment = Math.min(8, bits - (i3 << 3));
            arr[i3] = (byte) (read(increment) << (8 - increment));
        }
        return arr;
    }

    public void skip(int bits) throws AccessException {
        if (this.mPos + bits > this.mEnd) {
            throw new AccessException("illegal skip (pos " + this.mPos + ", end " + this.mEnd + ", bits " + bits + ")");
        }
        this.mPos += bits;
    }
}
