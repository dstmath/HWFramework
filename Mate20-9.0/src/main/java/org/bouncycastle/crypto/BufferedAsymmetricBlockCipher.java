package org.bouncycastle.crypto;

public class BufferedAsymmetricBlockCipher {
    protected byte[] buf;
    protected int bufOff;
    private final AsymmetricBlockCipher cipher;

    public BufferedAsymmetricBlockCipher(AsymmetricBlockCipher asymmetricBlockCipher) {
        this.cipher = asymmetricBlockCipher;
    }

    public byte[] doFinal() throws InvalidCipherTextException {
        byte[] processBlock = this.cipher.processBlock(this.buf, 0, this.bufOff);
        reset();
        return processBlock;
    }

    public int getBufferPosition() {
        return this.bufOff;
    }

    public int getInputBlockSize() {
        return this.cipher.getInputBlockSize();
    }

    public int getOutputBlockSize() {
        return this.cipher.getOutputBlockSize();
    }

    public AsymmetricBlockCipher getUnderlyingCipher() {
        return this.cipher;
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        reset();
        this.cipher.init(z, cipherParameters);
        this.buf = new byte[(this.cipher.getInputBlockSize() + (z ? 1 : 0))];
        this.bufOff = 0;
    }

    public void processByte(byte b) {
        if (this.bufOff < this.buf.length) {
            byte[] bArr = this.buf;
            int i = this.bufOff;
            this.bufOff = i + 1;
            bArr[i] = b;
            return;
        }
        throw new DataLengthException("attempt to process message too long for cipher");
    }

    public void processBytes(byte[] bArr, int i, int i2) {
        if (i2 != 0) {
            if (i2 < 0) {
                throw new IllegalArgumentException("Can't have a negative input length!");
            } else if (this.bufOff + i2 <= this.buf.length) {
                System.arraycopy(bArr, i, this.buf, this.bufOff, i2);
                this.bufOff += i2;
            } else {
                throw new DataLengthException("attempt to process message too long for cipher");
            }
        }
    }

    public void reset() {
        if (this.buf != null) {
            for (int i = 0; i < this.buf.length; i++) {
                this.buf[i] = 0;
            }
        }
        this.bufOff = 0;
    }
}
