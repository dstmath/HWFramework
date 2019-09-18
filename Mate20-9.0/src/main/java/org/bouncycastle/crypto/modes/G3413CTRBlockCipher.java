package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

public class G3413CTRBlockCipher extends StreamBlockCipher {
    private byte[] CTR;
    private byte[] IV;
    private final int blockSize;
    private byte[] buf;
    private int byteCount;
    private final BlockCipher cipher;
    private boolean initialized;
    private final int s;

    public G3413CTRBlockCipher(BlockCipher blockCipher) {
        this(blockCipher, blockCipher.getBlockSize() * 8);
    }

    public G3413CTRBlockCipher(BlockCipher blockCipher, int i) {
        super(blockCipher);
        this.byteCount = 0;
        if (i < 0 || i > blockCipher.getBlockSize() * 8) {
            throw new IllegalArgumentException("Parameter bitBlockSize must be in range 0 < bitBlockSize <= " + (blockCipher.getBlockSize() * 8));
        }
        this.cipher = blockCipher;
        this.blockSize = blockCipher.getBlockSize();
        this.s = i / 8;
        this.CTR = new byte[this.blockSize];
    }

    private byte[] generateBuf() {
        byte[] bArr = new byte[this.CTR.length];
        this.cipher.processBlock(this.CTR, 0, bArr, 0);
        return GOST3413CipherUtil.MSB(bArr, this.s);
    }

    private void generateCRT() {
        byte[] bArr = this.CTR;
        int length = this.CTR.length - 1;
        bArr[length] = (byte) (bArr[length] + 1);
    }

    private void initArrays() {
        this.IV = new byte[(this.blockSize / 2)];
        this.CTR = new byte[this.blockSize];
        this.buf = new byte[this.s];
    }

    /* access modifiers changed from: protected */
    public byte calculateByte(byte b) {
        if (this.byteCount == 0) {
            this.buf = generateBuf();
        }
        byte b2 = (byte) (b ^ this.buf[this.byteCount]);
        this.byteCount++;
        if (this.byteCount == this.s) {
            this.byteCount = 0;
            generateCRT();
        }
        return b2;
    }

    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName() + "/GCTR";
    }

    public int getBlockSize() {
        return this.s;
    }

    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        BlockCipher blockCipher;
        if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            initArrays();
            this.IV = Arrays.clone(parametersWithIV.getIV());
            if (this.IV.length == this.blockSize / 2) {
                System.arraycopy(this.IV, 0, this.CTR, 0, this.IV.length);
                for (int length = this.IV.length; length < this.blockSize; length++) {
                    this.CTR[length] = 0;
                }
                if (parametersWithIV.getParameters() != null) {
                    blockCipher = this.cipher;
                    cipherParameters = parametersWithIV.getParameters();
                }
                this.initialized = true;
            }
            throw new IllegalArgumentException("Parameter IV length must be == blockSize/2");
        }
        initArrays();
        if (cipherParameters != null) {
            blockCipher = this.cipher;
        }
        this.initialized = true;
        blockCipher.init(true, cipherParameters);
        this.initialized = true;
    }

    public int processBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        processBytes(bArr, i, this.s, bArr2, i2);
        return this.s;
    }

    public void reset() {
        if (this.initialized) {
            System.arraycopy(this.IV, 0, this.CTR, 0, this.IV.length);
            for (int length = this.IV.length; length < this.blockSize; length++) {
                this.CTR[length] = 0;
            }
            this.byteCount = 0;
            this.cipher.reset();
        }
    }
}
