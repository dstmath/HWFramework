package org.bouncycastle.crypto.macs;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.params.ParametersWithIV;

class MacCFBBlockCipher {
    private byte[] IV;
    private int blockSize;
    private byte[] cfbOutV;
    private byte[] cfbV;
    private BlockCipher cipher = null;

    public MacCFBBlockCipher(BlockCipher blockCipher, int i) {
        this.cipher = blockCipher;
        this.blockSize = i / 8;
        this.IV = new byte[blockCipher.getBlockSize()];
        this.cfbV = new byte[blockCipher.getBlockSize()];
        this.cfbOutV = new byte[blockCipher.getBlockSize()];
    }

    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName() + "/CFB" + (this.blockSize * 8);
    }

    public int getBlockSize() {
        return this.blockSize;
    }

    /* access modifiers changed from: package-private */
    public void getMacBlock(byte[] bArr) {
        this.cipher.processBlock(this.cfbV, 0, bArr, 0);
    }

    public void init(CipherParameters cipherParameters) throws IllegalArgumentException {
        BlockCipher blockCipher;
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            byte[] iv = parametersWithIV.getIV();
            if (iv.length < this.IV.length) {
                System.arraycopy(iv, 0, this.IV, this.IV.length - iv.length, iv.length);
            } else {
                System.arraycopy(iv, 0, this.IV, 0, this.IV.length);
            }
            reset();
            blockCipher = this.cipher;
            cipherParameters = parametersWithIV.getParameters();
        } else {
            reset();
            blockCipher = this.cipher;
        }
        blockCipher.init(true, cipherParameters);
    }

    public int processBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        if (this.blockSize + i > bArr.length) {
            throw new DataLengthException("input buffer too short");
        } else if (this.blockSize + i2 <= bArr2.length) {
            this.cipher.processBlock(this.cfbV, 0, this.cfbOutV, 0);
            for (int i3 = 0; i3 < this.blockSize; i3++) {
                bArr2[i2 + i3] = (byte) (this.cfbOutV[i3] ^ bArr[i + i3]);
            }
            System.arraycopy(this.cfbV, this.blockSize, this.cfbV, 0, this.cfbV.length - this.blockSize);
            System.arraycopy(bArr2, i2, this.cfbV, this.cfbV.length - this.blockSize, this.blockSize);
            return this.blockSize;
        } else {
            throw new OutputLengthException("output buffer too short");
        }
    }

    public void reset() {
        System.arraycopy(this.IV, 0, this.cfbV, 0, this.IV.length);
        this.cipher.reset();
    }
}
