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
        int i = this.bufOff;
        byte[] bArr = this.buf;
        if (i < bArr.length) {
            this.bufOff = i + 1;
            bArr[i] = b;
            return;
        }
        throw new DataLengthException("attempt to process message too long for cipher");
    }

    public void processBytes(byte[] bArr, int i, int i2) {
        if (i2 != 0) {
            if (i2 >= 0) {
                int i3 = this.bufOff;
                int i4 = i3 + i2;
                byte[] bArr2 = this.buf;
                if (i4 <= bArr2.length) {
                    System.arraycopy(bArr, i, bArr2, i3, i2);
                    this.bufOff += i2;
                    return;
                }
                throw new DataLengthException("attempt to process message too long for cipher");
            }
            throw new IllegalArgumentException("Can't have a negative input length!");
        }
    }

    public void reset() {
        if (this.buf != null) {
            int i = 0;
            while (true) {
                byte[] bArr = this.buf;
                if (i >= bArr.length) {
                    break;
                }
                bArr[i] = 0;
                i++;
            }
        }
        this.bufOff = 0;
    }
}
