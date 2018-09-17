package com.android.org.bouncycastle.crypto.digests;

import com.android.org.bouncycastle.crypto.ExtendedDigest;
import com.android.org.bouncycastle.util.Memoable;
import com.android.org.bouncycastle.util.Pack;

public abstract class GeneralDigest implements ExtendedDigest, Memoable {
    private static final int BYTE_LENGTH = 64;
    private long byteCount;
    private final byte[] xBuf;
    private int xBufOff;

    protected abstract void processBlock();

    protected abstract void processLength(long j);

    protected abstract void processWord(byte[] bArr, int i);

    protected GeneralDigest() {
        this.xBuf = new byte[4];
        this.xBufOff = 0;
    }

    protected GeneralDigest(GeneralDigest t) {
        this.xBuf = new byte[4];
        copyIn(t);
    }

    protected GeneralDigest(byte[] encodedState) {
        this.xBuf = new byte[4];
        System.arraycopy(encodedState, 0, this.xBuf, 0, this.xBuf.length);
        this.xBufOff = Pack.bigEndianToInt(encodedState, 4);
        this.byteCount = Pack.bigEndianToLong(encodedState, 8);
    }

    protected void copyIn(GeneralDigest t) {
        System.arraycopy(t.xBuf, 0, this.xBuf, 0, t.xBuf.length);
        this.xBufOff = t.xBufOff;
        this.byteCount = t.byteCount;
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
        this.byteCount++;
    }

    public void update(byte[] in, int inOff, int len) {
        int i;
        byte[] bArr;
        int i2;
        len = Math.max(0, len);
        int i3 = 0;
        if (this.xBufOff != 0) {
            do {
                i = i3;
                if (i >= len) {
                    i3 = i;
                    break;
                }
                bArr = this.xBuf;
                i2 = this.xBufOff;
                this.xBufOff = i2 + 1;
                i3 = i + 1;
                bArr[i2] = in[inOff + i];
            } while (this.xBufOff != 4);
            processWord(this.xBuf, 0);
            this.xBufOff = 0;
        }
        int limit = ((len - i3) & -4) + i3;
        while (i3 < limit) {
            processWord(in, inOff + i3);
            i3 += 4;
        }
        while (true) {
            i = i3;
            if (i < len) {
                bArr = this.xBuf;
                i2 = this.xBufOff;
                this.xBufOff = i2 + 1;
                i3 = i + 1;
                bArr[i2] = in[inOff + i];
            } else {
                this.byteCount += (long) len;
                return;
            }
        }
    }

    public void finish() {
        long bitLength = this.byteCount << 3;
        update(Byte.MIN_VALUE);
        while (this.xBufOff != 0) {
            update((byte) 0);
        }
        processLength(bitLength);
        processBlock();
    }

    public void reset() {
        this.byteCount = 0;
        this.xBufOff = 0;
        for (int i = 0; i < this.xBuf.length; i++) {
            this.xBuf[i] = (byte) 0;
        }
    }

    protected void populateState(byte[] state) {
        System.arraycopy(this.xBuf, 0, state, 0, this.xBufOff);
        Pack.intToBigEndian(this.xBufOff, state, 4);
        Pack.longToBigEndian(this.byteCount, state, 8);
    }

    public int getByteLength() {
        return 64;
    }
}
