package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

public class KCTRBlockCipher extends StreamBlockCipher {
    private int byteCount;
    private BlockCipher engine;
    private boolean initialised;
    private byte[] iv;
    private byte[] ofbOutV;
    private byte[] ofbV;

    public KCTRBlockCipher(BlockCipher blockCipher) {
        super(blockCipher);
        this.engine = blockCipher;
        this.iv = new byte[blockCipher.getBlockSize()];
        this.ofbV = new byte[blockCipher.getBlockSize()];
        this.ofbOutV = new byte[blockCipher.getBlockSize()];
    }

    private void checkCounter() {
    }

    private void incrementCounterAt(int i) {
        while (i < this.ofbV.length) {
            byte[] bArr = this.ofbV;
            int i2 = i + 1;
            byte b = (byte) (bArr[i] + 1);
            bArr[i] = b;
            if (b == 0) {
                i = i2;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public byte calculateByte(byte b) {
        if (this.byteCount == 0) {
            incrementCounterAt(0);
            checkCounter();
            this.engine.processBlock(this.ofbV, 0, this.ofbOutV, 0);
            byte[] bArr = this.ofbOutV;
            int i = this.byteCount;
            this.byteCount = i + 1;
            return (byte) (b ^ bArr[i]);
        }
        byte[] bArr2 = this.ofbOutV;
        int i2 = this.byteCount;
        this.byteCount = i2 + 1;
        byte b2 = (byte) (b ^ bArr2[i2]);
        if (this.byteCount == this.ofbV.length) {
            this.byteCount = 0;
        }
        return b2;
    }

    public String getAlgorithmName() {
        return this.engine.getAlgorithmName() + "/KCTR";
    }

    public int getBlockSize() {
        return this.engine.getBlockSize();
    }

    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        this.initialised = true;
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            byte[] iv2 = parametersWithIV.getIV();
            int length = this.iv.length - iv2.length;
            Arrays.fill(this.iv, (byte) 0);
            System.arraycopy(iv2, 0, this.iv, length, iv2.length);
            CipherParameters parameters = parametersWithIV.getParameters();
            if (parameters != null) {
                this.engine.init(true, parameters);
            }
            reset();
            return;
        }
        throw new IllegalArgumentException("invalid parameter passed");
    }

    public int processBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        if (bArr.length - i < getBlockSize()) {
            throw new DataLengthException("input buffer too short");
        } else if (bArr2.length - i2 >= getBlockSize()) {
            processBytes(bArr, i, getBlockSize(), bArr2, i2);
            return getBlockSize();
        } else {
            throw new OutputLengthException("output buffer too short");
        }
    }

    public void reset() {
        if (this.initialised) {
            this.engine.processBlock(this.iv, 0, this.ofbV, 0);
        }
        this.engine.reset();
        this.byteCount = 0;
    }
}
