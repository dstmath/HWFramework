package com.android.internal.util;

import com.android.internal.midi.MidiConstants;

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
        int data = (this.mBuf[index] & MidiConstants.STATUS_RESET) << 8;
        if (offset < 8) {
            data |= this.mBuf[index + 1] & MidiConstants.STATUS_RESET;
        }
        int data2 = (data >>> offset) & (-1 >>> (32 - bits));
        this.mPos += bits;
        return data2;
    }

    public byte[] readByteArray(int bits) throws AccessException {
        int bytes = (bits >>> 3) + ((bits & 7) > 0 ? 1 : 0);
        byte[] arr = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            int increment = Math.min(8, bits - (i << 3));
            arr[i] = (byte) (read(increment) << (8 - increment));
        }
        return arr;
    }

    public void skip(int bits) throws AccessException {
        if (this.mPos + bits <= this.mEnd) {
            this.mPos += bits;
            return;
        }
        throw new AccessException("illegal skip (pos " + this.mPos + ", end " + this.mEnd + ", bits " + bits + ")");
    }
}
