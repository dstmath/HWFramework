package org.bouncycastle.crypto.engines;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;

public class RSAEngine implements AsymmetricBlockCipher {
    private RSACoreEngine core;

    public int getInputBlockSize() {
        return this.core.getInputBlockSize();
    }

    public int getOutputBlockSize() {
        return this.core.getOutputBlockSize();
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        if (this.core == null) {
            this.core = new RSACoreEngine();
        }
        this.core.init(z, cipherParameters);
    }

    public byte[] processBlock(byte[] bArr, int i, int i2) {
        if (this.core != null) {
            return this.core.convertOutput(this.core.processBlock(this.core.convertInput(bArr, i, i2)));
        }
        throw new IllegalStateException("RSA engine not initialised");
    }
}
