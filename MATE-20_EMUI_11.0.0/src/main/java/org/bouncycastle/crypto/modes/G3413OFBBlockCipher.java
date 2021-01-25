package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

public class G3413OFBBlockCipher extends StreamBlockCipher {
    private byte[] R;
    private byte[] R_init;
    private byte[] Y;
    private int blockSize;
    private int byteCount;
    private BlockCipher cipher;
    private boolean initialized = false;
    private int m;

    public G3413OFBBlockCipher(BlockCipher blockCipher) {
        super(blockCipher);
        this.blockSize = blockCipher.getBlockSize();
        this.cipher = blockCipher;
        this.Y = new byte[this.blockSize];
    }

    private void generateR() {
        byte[] LSB = GOST3413CipherUtil.LSB(this.R, this.m - this.blockSize);
        System.arraycopy(LSB, 0, this.R, 0, LSB.length);
        System.arraycopy(this.Y, 0, this.R, LSB.length, this.m - LSB.length);
    }

    private void generateY() {
        this.cipher.processBlock(GOST3413CipherUtil.MSB(this.R, this.blockSize), 0, this.Y, 0);
    }

    private void initArrays() {
        int i = this.m;
        this.R = new byte[i];
        this.R_init = new byte[i];
    }

    private void setupDefaultParams() {
        this.m = this.blockSize * 2;
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.crypto.StreamBlockCipher
    public byte calculateByte(byte b) {
        if (this.byteCount == 0) {
            generateY();
        }
        byte[] bArr = this.Y;
        int i = this.byteCount;
        byte b2 = (byte) (b ^ bArr[i]);
        this.byteCount = i + 1;
        if (this.byteCount == getBlockSize()) {
            this.byteCount = 0;
            generateR();
        }
        return b2;
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName() + "/OFB";
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public int getBlockSize() {
        return this.blockSize;
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        BlockCipher blockCipher;
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            byte[] iv = parametersWithIV.getIV();
            if (iv.length >= this.blockSize) {
                this.m = iv.length;
                initArrays();
                this.R_init = Arrays.clone(iv);
                byte[] bArr = this.R_init;
                System.arraycopy(bArr, 0, this.R, 0, bArr.length);
                if (parametersWithIV.getParameters() != null) {
                    blockCipher = this.cipher;
                    cipherParameters = parametersWithIV.getParameters();
                }
                this.initialized = true;
            }
            throw new IllegalArgumentException("Parameter m must blockSize <= m");
        }
        setupDefaultParams();
        initArrays();
        byte[] bArr2 = this.R_init;
        System.arraycopy(bArr2, 0, this.R, 0, bArr2.length);
        if (cipherParameters != null) {
            blockCipher = this.cipher;
        }
        this.initialized = true;
        blockCipher.init(true, cipherParameters);
        this.initialized = true;
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public int processBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        processBytes(bArr, i, this.blockSize, bArr2, i2);
        return this.blockSize;
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public void reset() {
        if (this.initialized) {
            byte[] bArr = this.R_init;
            System.arraycopy(bArr, 0, this.R, 0, bArr.length);
            Arrays.clear(this.Y);
            this.byteCount = 0;
            this.cipher.reset();
        }
    }
}
