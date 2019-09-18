package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

public class G3413CFBBlockCipher extends StreamBlockCipher {
    private byte[] R;
    private byte[] R_init;
    private int blockSize;
    private int byteCount;
    private BlockCipher cipher;
    private boolean forEncryption;
    private byte[] gamma;
    private byte[] inBuf;
    private boolean initialized;
    private int m;
    private final int s;

    public G3413CFBBlockCipher(BlockCipher blockCipher) {
        this(blockCipher, blockCipher.getBlockSize() * 8);
    }

    public G3413CFBBlockCipher(BlockCipher blockCipher, int i) {
        super(blockCipher);
        this.initialized = false;
        if (i < 0 || i > blockCipher.getBlockSize() * 8) {
            throw new IllegalArgumentException("Parameter bitBlockSize must be in range 0 < bitBlockSize <= " + (blockCipher.getBlockSize() * 8));
        }
        this.blockSize = blockCipher.getBlockSize();
        this.cipher = blockCipher;
        this.s = i / 8;
        this.inBuf = new byte[getBlockSize()];
    }

    private void initArrays() {
        this.R = new byte[this.m];
        this.R_init = new byte[this.m];
    }

    private void setupDefaultParams() {
        this.m = 2 * this.blockSize;
    }

    /* access modifiers changed from: protected */
    public byte calculateByte(byte b) {
        if (this.byteCount == 0) {
            this.gamma = createGamma();
        }
        byte b2 = (byte) (this.gamma[this.byteCount] ^ b);
        byte[] bArr = this.inBuf;
        int i = this.byteCount;
        this.byteCount = i + 1;
        if (this.forEncryption) {
            b = b2;
        }
        bArr[i] = b;
        if (this.byteCount == getBlockSize()) {
            this.byteCount = 0;
            generateR(this.inBuf);
        }
        return b2;
    }

    /* access modifiers changed from: package-private */
    public byte[] createGamma() {
        byte[] MSB = GOST3413CipherUtil.MSB(this.R, this.blockSize);
        byte[] bArr = new byte[MSB.length];
        this.cipher.processBlock(MSB, 0, bArr, 0);
        return GOST3413CipherUtil.MSB(bArr, this.s);
    }

    /* access modifiers changed from: package-private */
    public void generateR(byte[] bArr) {
        byte[] LSB = GOST3413CipherUtil.LSB(this.R, this.m - this.s);
        System.arraycopy(LSB, 0, this.R, 0, LSB.length);
        System.arraycopy(bArr, 0, this.R, LSB.length, this.m - LSB.length);
    }

    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName() + "/CFB" + (this.blockSize * 8);
    }

    public int getBlockSize() {
        return this.s;
    }

    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        BlockCipher blockCipher;
        this.forEncryption = z;
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            byte[] iv = parametersWithIV.getIV();
            if (iv.length >= this.blockSize) {
                this.m = iv.length;
                initArrays();
                this.R_init = Arrays.clone(iv);
                System.arraycopy(this.R_init, 0, this.R, 0, this.R_init.length);
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
        System.arraycopy(this.R_init, 0, this.R, 0, this.R_init.length);
        if (cipherParameters != null) {
            blockCipher = this.cipher;
        }
        this.initialized = true;
        blockCipher.init(true, cipherParameters);
        this.initialized = true;
    }

    public int processBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        processBytes(bArr, i, getBlockSize(), bArr2, i2);
        return getBlockSize();
    }

    public void reset() {
        this.byteCount = 0;
        Arrays.clear(this.inBuf);
        Arrays.clear(this.gamma);
        if (this.initialized) {
            System.arraycopy(this.R_init, 0, this.R, 0, this.R_init.length);
            this.cipher.reset();
        }
    }
}
