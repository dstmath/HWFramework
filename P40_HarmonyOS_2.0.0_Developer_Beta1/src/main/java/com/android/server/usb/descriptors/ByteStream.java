package com.android.server.usb.descriptors;

public final class ByteStream {
    private static final String TAG = "ByteStream";
    private final byte[] mBytes;
    private int mIndex;
    private int mReadCount;

    public ByteStream(byte[] bytes) {
        if (bytes != null) {
            this.mBytes = bytes;
            return;
        }
        throw new IllegalArgumentException();
    }

    public void resetReadCount() {
        this.mReadCount = 0;
    }

    public int getReadCount() {
        return this.mReadCount;
    }

    public byte peekByte() {
        if (available() > 0) {
            return this.mBytes[this.mIndex + 1];
        }
        throw new IndexOutOfBoundsException();
    }

    public byte getByte() {
        if (available() > 0) {
            this.mReadCount++;
            byte[] bArr = this.mBytes;
            int i = this.mIndex;
            this.mIndex = i + 1;
            return bArr[i];
        }
        throw new IndexOutOfBoundsException();
    }

    public int getUnsignedByte() {
        if (available() > 0) {
            this.mReadCount++;
            byte[] bArr = this.mBytes;
            int i = this.mIndex;
            this.mIndex = i + 1;
            return bArr[i] & 255;
        }
        throw new IndexOutOfBoundsException();
    }

    public int unpackUsbShort() {
        if (available() >= 2) {
            return (getUnsignedByte() << 8) | getUnsignedByte();
        }
        throw new IndexOutOfBoundsException();
    }

    public int unpackUsbTriple() {
        if (available() >= 3) {
            return (getUnsignedByte() << 16) | (getUnsignedByte() << 8) | getUnsignedByte();
        }
        throw new IndexOutOfBoundsException();
    }

    public int unpackUsbInt() {
        if (available() >= 4) {
            int b0 = getUnsignedByte();
            int b1 = getUnsignedByte();
            return (getUnsignedByte() << 24) | (getUnsignedByte() << 16) | (b1 << 8) | b0;
        }
        throw new IndexOutOfBoundsException();
    }

    public void advance(int numBytes) {
        if (numBytes >= 0) {
            int i = this.mIndex;
            if (((long) i) + ((long) numBytes) < ((long) this.mBytes.length)) {
                this.mReadCount += numBytes;
                this.mIndex = i + numBytes;
                return;
            }
            throw new IndexOutOfBoundsException();
        }
        throw new IllegalArgumentException();
    }

    public void reverse(int numBytes) {
        if (numBytes >= 0) {
            int i = this.mIndex;
            if (i >= numBytes) {
                this.mReadCount -= numBytes;
                this.mIndex = i - numBytes;
                return;
            }
            throw new IndexOutOfBoundsException();
        }
        throw new IllegalArgumentException();
    }

    public int available() {
        return this.mBytes.length - this.mIndex;
    }
}
