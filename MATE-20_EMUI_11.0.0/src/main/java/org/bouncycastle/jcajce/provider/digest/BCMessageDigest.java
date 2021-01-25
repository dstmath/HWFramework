package org.bouncycastle.jcajce.provider.digest;

import java.security.MessageDigest;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Xof;

public class BCMessageDigest extends MessageDigest {
    protected Digest digest;
    protected int digestSize;

    protected BCMessageDigest(Digest digest2) {
        super(digest2.getAlgorithmName());
        this.digest = digest2;
        this.digestSize = digest2.getDigestSize();
    }

    protected BCMessageDigest(Xof xof, int i) {
        super(xof.getAlgorithmName());
        this.digest = xof;
        this.digestSize = i / 8;
    }

    @Override // java.security.MessageDigestSpi
    public byte[] engineDigest() {
        byte[] bArr = new byte[this.digestSize];
        this.digest.doFinal(bArr, 0);
        return bArr;
    }

    @Override // java.security.MessageDigestSpi
    public int engineGetDigestLength() {
        return this.digestSize;
    }

    @Override // java.security.MessageDigestSpi
    public void engineReset() {
        this.digest.reset();
    }

    @Override // java.security.MessageDigestSpi
    public void engineUpdate(byte b) {
        this.digest.update(b);
    }

    @Override // java.security.MessageDigestSpi
    public void engineUpdate(byte[] bArr, int i, int i2) {
        this.digest.update(bArr, i, i2);
    }
}
