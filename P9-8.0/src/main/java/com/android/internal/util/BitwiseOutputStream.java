package com.android.internal.util;

public class BitwiseOutputStream {
    private byte[] mBuf;
    private int mEnd;
    private int mPos = 0;

    public static class AccessException extends Exception {
        public AccessException(String s) {
            super("BitwiseOutputStream access failed: " + s);
        }
    }

    public BitwiseOutputStream(int startingLength) {
        this.mBuf = new byte[startingLength];
        this.mEnd = startingLength << 3;
    }

    public byte[] toByteArray() {
        int i;
        int i2 = this.mPos >>> 3;
        if ((this.mPos & 7) > 0) {
            i = 1;
        } else {
            i = 0;
        }
        int len = i2 + i;
        byte[] newBuf = new byte[len];
        System.arraycopy(this.mBuf, 0, newBuf, 0, len);
        return newBuf;
    }

    private void possExpand(int bits) {
        if (this.mPos + bits >= this.mEnd) {
            byte[] newBuf = new byte[((this.mPos + bits) >>> 2)];
            System.arraycopy(this.mBuf, 0, newBuf, 0, this.mEnd >>> 3);
            this.mBuf = newBuf;
            this.mEnd = newBuf.length << 3;
        }
    }

    public void write(int bits, int data) throws AccessException {
        if (bits < 0 || bits > 8) {
            throw new AccessException("illegal write (" + bits + " bits)");
        }
        possExpand(bits);
        int index = this.mPos >>> 3;
        int offset = (16 - (this.mPos & 7)) - bits;
        data = (data & (-1 >>> (32 - bits))) << offset;
        this.mPos += bits;
        byte[] bArr = this.mBuf;
        bArr[index] = (byte) (bArr[index] | (data >>> 8));
        if (offset < 8) {
            bArr = this.mBuf;
            int i = index + 1;
            bArr[i] = (byte) (bArr[i] | (data & 255));
        }
    }

    public void writeByteArray(int bits, byte[] arr) throws AccessException {
        for (int i = 0; i < arr.length; i++) {
            int increment = Math.min(8, bits - (i << 3));
            if (increment > 0) {
                write(increment, (byte) (arr[i] >>> (8 - increment)));
            }
        }
    }

    public void skip(int bits) {
        possExpand(bits);
        this.mPos += bits;
    }
}
