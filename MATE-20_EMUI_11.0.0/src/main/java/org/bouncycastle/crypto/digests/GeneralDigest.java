package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.util.Memoable;
import org.bouncycastle.util.Pack;

public abstract class GeneralDigest implements ExtendedDigest, Memoable {
    private static final int BYTE_LENGTH = 64;
    private long byteCount;
    private final byte[] xBuf;
    private int xBufOff;

    protected GeneralDigest() {
        this.xBuf = new byte[4];
        this.xBufOff = 0;
    }

    protected GeneralDigest(GeneralDigest generalDigest) {
        this.xBuf = new byte[4];
        copyIn(generalDigest);
    }

    protected GeneralDigest(byte[] bArr) {
        this.xBuf = new byte[4];
        byte[] bArr2 = this.xBuf;
        System.arraycopy(bArr, 0, bArr2, 0, bArr2.length);
        this.xBufOff = Pack.bigEndianToInt(bArr, 4);
        this.byteCount = Pack.bigEndianToLong(bArr, 8);
    }

    /* access modifiers changed from: protected */
    public void copyIn(GeneralDigest generalDigest) {
        byte[] bArr = generalDigest.xBuf;
        System.arraycopy(bArr, 0, this.xBuf, 0, bArr.length);
        this.xBufOff = generalDigest.xBufOff;
        this.byteCount = generalDigest.byteCount;
    }

    public void finish() {
        long j = this.byteCount << 3;
        byte b = Byte.MIN_VALUE;
        while (true) {
            update(b);
            if (this.xBufOff != 0) {
                b = 0;
            } else {
                processLength(j);
                processBlock();
                return;
            }
        }
    }

    @Override // org.bouncycastle.crypto.ExtendedDigest
    public int getByteLength() {
        return 64;
    }

    /* access modifiers changed from: protected */
    public void populateState(byte[] bArr) {
        System.arraycopy(this.xBuf, 0, bArr, 0, this.xBufOff);
        Pack.intToBigEndian(this.xBufOff, bArr, 4);
        Pack.longToBigEndian(this.byteCount, bArr, 8);
    }

    /* access modifiers changed from: protected */
    public abstract void processBlock();

    /* access modifiers changed from: protected */
    public abstract void processLength(long j);

    /* access modifiers changed from: protected */
    public abstract void processWord(byte[] bArr, int i);

    @Override // org.bouncycastle.crypto.Digest
    public void reset() {
        this.byteCount = 0;
        this.xBufOff = 0;
        int i = 0;
        while (true) {
            byte[] bArr = this.xBuf;
            if (i < bArr.length) {
                bArr[i] = 0;
                i++;
            } else {
                return;
            }
        }
    }

    @Override // org.bouncycastle.crypto.Digest
    public void update(byte b) {
        byte[] bArr = this.xBuf;
        int i = this.xBufOff;
        this.xBufOff = i + 1;
        bArr[i] = b;
        if (this.xBufOff == bArr.length) {
            processWord(bArr, 0);
            this.xBufOff = 0;
        }
        this.byteCount++;
    }

    @Override // org.bouncycastle.crypto.Digest
    public void update(byte[] bArr, int i, int i2) {
        int i3 = 0;
        int max = Math.max(0, i2);
        if (this.xBufOff != 0) {
            int i4 = 0;
            while (true) {
                if (i4 >= max) {
                    i3 = i4;
                    break;
                }
                byte[] bArr2 = this.xBuf;
                int i5 = this.xBufOff;
                this.xBufOff = i5 + 1;
                int i6 = i4 + 1;
                bArr2[i5] = bArr[i4 + i];
                if (this.xBufOff == 4) {
                    processWord(bArr2, 0);
                    this.xBufOff = 0;
                    i3 = i6;
                    break;
                }
                i4 = i6;
            }
        }
        int i7 = ((max - i3) & -4) + i3;
        while (i3 < i7) {
            processWord(bArr, i + i3);
            i3 += 4;
        }
        while (i3 < max) {
            byte[] bArr3 = this.xBuf;
            int i8 = this.xBufOff;
            this.xBufOff = i8 + 1;
            bArr3[i8] = bArr[i3 + i];
            i3++;
        }
        this.byteCount += (long) max;
    }
}
