package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.ExtendedDigest;

public class NonMemoableDigest implements ExtendedDigest {
    private ExtendedDigest baseDigest;

    public NonMemoableDigest(ExtendedDigest extendedDigest) {
        if (extendedDigest != null) {
            this.baseDigest = extendedDigest;
            return;
        }
        throw new IllegalArgumentException("baseDigest must not be null");
    }

    public int doFinal(byte[] bArr, int i) {
        return this.baseDigest.doFinal(bArr, i);
    }

    public String getAlgorithmName() {
        return this.baseDigest.getAlgorithmName();
    }

    public int getByteLength() {
        return this.baseDigest.getByteLength();
    }

    public int getDigestSize() {
        return this.baseDigest.getDigestSize();
    }

    public void reset() {
        this.baseDigest.reset();
    }

    public void update(byte b) {
        this.baseDigest.update(b);
    }

    public void update(byte[] bArr, int i, int i2) {
        this.baseDigest.update(bArr, i, i2);
    }
}
