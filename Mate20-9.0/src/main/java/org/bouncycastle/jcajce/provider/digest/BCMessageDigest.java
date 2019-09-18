package org.bouncycastle.jcajce.provider.digest;

import java.security.MessageDigest;
import org.bouncycastle.crypto.Digest;

public class BCMessageDigest extends MessageDigest {
    protected Digest digest;

    protected BCMessageDigest(Digest digest2) {
        super(digest2.getAlgorithmName());
        this.digest = digest2;
    }

    public byte[] engineDigest() {
        byte[] bArr = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(bArr, 0);
        return bArr;
    }

    public void engineReset() {
        this.digest.reset();
    }

    public void engineUpdate(byte b) {
        this.digest.update(b);
    }

    public void engineUpdate(byte[] bArr, int i, int i2) {
        this.digest.update(bArr, i, i2);
    }
}
