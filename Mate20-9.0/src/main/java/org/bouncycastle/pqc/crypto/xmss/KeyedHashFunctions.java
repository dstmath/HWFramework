package org.bouncycastle.pqc.crypto.xmss;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Xof;

final class KeyedHashFunctions {
    private final Digest digest;
    private final int digestSize;

    protected KeyedHashFunctions(Digest digest2, int i) {
        if (digest2 != null) {
            this.digest = digest2;
            this.digestSize = i;
            return;
        }
        throw new NullPointerException("digest == null");
    }

    private byte[] coreDigest(int i, byte[] bArr, byte[] bArr2) {
        byte[] bytesBigEndian = XMSSUtil.toBytesBigEndian((long) i, this.digestSize);
        this.digest.update(bytesBigEndian, 0, bytesBigEndian.length);
        this.digest.update(bArr, 0, bArr.length);
        this.digest.update(bArr2, 0, bArr2.length);
        byte[] bArr3 = new byte[this.digestSize];
        if (this.digest instanceof Xof) {
            ((Xof) this.digest).doFinal(bArr3, 0, this.digestSize);
            return bArr3;
        }
        this.digest.doFinal(bArr3, 0);
        return bArr3;
    }

    /* access modifiers changed from: protected */
    public byte[] F(byte[] bArr, byte[] bArr2) {
        if (bArr.length != this.digestSize) {
            throw new IllegalArgumentException("wrong key length");
        } else if (bArr2.length == this.digestSize) {
            return coreDigest(0, bArr, bArr2);
        } else {
            throw new IllegalArgumentException("wrong in length");
        }
    }

    /* access modifiers changed from: protected */
    public byte[] H(byte[] bArr, byte[] bArr2) {
        if (bArr.length != this.digestSize) {
            throw new IllegalArgumentException("wrong key length");
        } else if (bArr2.length == 2 * this.digestSize) {
            return coreDigest(1, bArr, bArr2);
        } else {
            throw new IllegalArgumentException("wrong in length");
        }
    }

    /* access modifiers changed from: protected */
    public byte[] HMsg(byte[] bArr, byte[] bArr2) {
        if (bArr.length == 3 * this.digestSize) {
            return coreDigest(2, bArr, bArr2);
        }
        throw new IllegalArgumentException("wrong key length");
    }

    /* access modifiers changed from: protected */
    public byte[] PRF(byte[] bArr, byte[] bArr2) {
        if (bArr.length != this.digestSize) {
            throw new IllegalArgumentException("wrong key length");
        } else if (bArr2.length == 32) {
            return coreDigest(3, bArr, bArr2);
        } else {
            throw new IllegalArgumentException("wrong address length");
        }
    }
}
