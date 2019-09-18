package org.bouncycastle.crypto.macs;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.params.KeyParameter;

public class OldHMac implements Mac {
    private static final int BLOCK_LENGTH = 64;
    private static final byte IPAD = 54;
    private static final byte OPAD = 92;
    private Digest digest;
    private int digestSize;
    private byte[] inputPad = new byte[64];
    private byte[] outputPad = new byte[64];

    public OldHMac(Digest digest2) {
        this.digest = digest2;
        this.digestSize = digest2.getDigestSize();
    }

    public int doFinal(byte[] bArr, int i) {
        byte[] bArr2 = new byte[this.digestSize];
        this.digest.doFinal(bArr2, 0);
        this.digest.update(this.outputPad, 0, this.outputPad.length);
        this.digest.update(bArr2, 0, bArr2.length);
        int doFinal = this.digest.doFinal(bArr, i);
        reset();
        return doFinal;
    }

    public String getAlgorithmName() {
        return this.digest.getAlgorithmName() + "/HMAC";
    }

    public int getMacSize() {
        return this.digestSize;
    }

    public Digest getUnderlyingDigest() {
        return this.digest;
    }

    public void init(CipherParameters cipherParameters) {
        this.digest.reset();
        byte[] key = ((KeyParameter) cipherParameters).getKey();
        if (key.length > 64) {
            this.digest.update(key, 0, key.length);
            this.digest.doFinal(this.inputPad, 0);
            for (int i = this.digestSize; i < this.inputPad.length; i++) {
                this.inputPad[i] = 0;
            }
        } else {
            System.arraycopy(key, 0, this.inputPad, 0, key.length);
            for (int length = key.length; length < this.inputPad.length; length++) {
                this.inputPad[length] = 0;
            }
        }
        this.outputPad = new byte[this.inputPad.length];
        System.arraycopy(this.inputPad, 0, this.outputPad, 0, this.inputPad.length);
        for (int i2 = 0; i2 < this.inputPad.length; i2++) {
            byte[] bArr = this.inputPad;
            bArr[i2] = (byte) (bArr[i2] ^ IPAD);
        }
        for (int i3 = 0; i3 < this.outputPad.length; i3++) {
            byte[] bArr2 = this.outputPad;
            bArr2[i3] = (byte) (bArr2[i3] ^ OPAD);
        }
        this.digest.update(this.inputPad, 0, this.inputPad.length);
    }

    public void reset() {
        this.digest.reset();
        this.digest.update(this.inputPad, 0, this.inputPad.length);
    }

    public void update(byte b) {
        this.digest.update(b);
    }

    public void update(byte[] bArr, int i, int i2) {
        this.digest.update(bArr, i, i2);
    }
}
