package org.bouncycastle.crypto.tls;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.Arrays;

public class SSL3Mac implements Mac {
    static final byte[] IPAD = genPad(IPAD_BYTE, 48);
    private static final byte IPAD_BYTE = 54;
    static final byte[] OPAD = genPad(OPAD_BYTE, 48);
    private static final byte OPAD_BYTE = 92;
    private Digest digest;
    private int padLength;
    private byte[] secret;

    public SSL3Mac(Digest digest2) {
        this.digest = digest2;
        this.padLength = digest2.getDigestSize() == 20 ? 40 : 48;
    }

    private static byte[] genPad(byte b, int i) {
        byte[] bArr = new byte[i];
        Arrays.fill(bArr, b);
        return bArr;
    }

    public int doFinal(byte[] bArr, int i) {
        byte[] bArr2 = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(bArr2, 0);
        this.digest.update(this.secret, 0, this.secret.length);
        this.digest.update(OPAD, 0, this.padLength);
        this.digest.update(bArr2, 0, bArr2.length);
        int doFinal = this.digest.doFinal(bArr, i);
        reset();
        return doFinal;
    }

    public String getAlgorithmName() {
        return this.digest.getAlgorithmName() + "/SSL3MAC";
    }

    public int getMacSize() {
        return this.digest.getDigestSize();
    }

    public Digest getUnderlyingDigest() {
        return this.digest;
    }

    public void init(CipherParameters cipherParameters) {
        this.secret = Arrays.clone(((KeyParameter) cipherParameters).getKey());
        reset();
    }

    public void reset() {
        this.digest.reset();
        this.digest.update(this.secret, 0, this.secret.length);
        this.digest.update(IPAD, 0, this.padLength);
    }

    public void update(byte b) {
        this.digest.update(b);
    }

    public void update(byte[] bArr, int i, int i2) {
        this.digest.update(bArr, i, i2);
    }
}
