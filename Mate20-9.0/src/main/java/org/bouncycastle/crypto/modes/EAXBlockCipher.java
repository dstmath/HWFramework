package org.bouncycastle.crypto.modes;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.macs.CMac;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

public class EAXBlockCipher implements AEADBlockCipher {
    private static final byte cTAG = 2;
    private static final byte hTAG = 1;
    private static final byte nTAG = 0;
    private byte[] associatedTextMac = new byte[this.mac.getMacSize()];
    private int blockSize;
    private byte[] bufBlock;
    private int bufOff;
    private SICBlockCipher cipher;
    private boolean cipherInitialized;
    private boolean forEncryption;
    private byte[] initialAssociatedText;
    private Mac mac;
    private byte[] macBlock = new byte[this.blockSize];
    private int macSize;
    private byte[] nonceMac = new byte[this.mac.getMacSize()];

    public EAXBlockCipher(BlockCipher blockCipher) {
        this.blockSize = blockCipher.getBlockSize();
        this.mac = new CMac(blockCipher);
        this.cipher = new SICBlockCipher(blockCipher);
    }

    private void calculateMac() {
        byte[] bArr = new byte[this.blockSize];
        this.mac.doFinal(bArr, 0);
        for (int i = 0; i < this.macBlock.length; i++) {
            this.macBlock[i] = (byte) ((this.nonceMac[i] ^ this.associatedTextMac[i]) ^ bArr[i]);
        }
    }

    private void initCipher() {
        if (!this.cipherInitialized) {
            this.cipherInitialized = true;
            this.mac.doFinal(this.associatedTextMac, 0);
            byte[] bArr = new byte[this.blockSize];
            bArr[this.blockSize - 1] = cTAG;
            this.mac.update(bArr, 0, this.blockSize);
        }
    }

    private int process(byte b, byte[] bArr, int i) {
        int i2;
        byte[] bArr2 = this.bufBlock;
        int i3 = this.bufOff;
        this.bufOff = i3 + 1;
        bArr2[i3] = b;
        if (this.bufOff != this.bufBlock.length) {
            return 0;
        }
        if (bArr.length >= this.blockSize + i) {
            if (this.forEncryption) {
                i2 = this.cipher.processBlock(this.bufBlock, 0, bArr, i);
                this.mac.update(bArr, i, this.blockSize);
            } else {
                this.mac.update(this.bufBlock, 0, this.blockSize);
                i2 = this.cipher.processBlock(this.bufBlock, 0, bArr, i);
            }
            this.bufOff = 0;
            if (!this.forEncryption) {
                System.arraycopy(this.bufBlock, this.blockSize, this.bufBlock, 0, this.macSize);
                this.bufOff = this.macSize;
            }
            return i2;
        }
        throw new OutputLengthException("Output buffer is too short");
    }

    private void reset(boolean z) {
        this.cipher.reset();
        this.mac.reset();
        this.bufOff = 0;
        Arrays.fill(this.bufBlock, (byte) 0);
        if (z) {
            Arrays.fill(this.macBlock, (byte) 0);
        }
        byte[] bArr = new byte[this.blockSize];
        bArr[this.blockSize - 1] = hTAG;
        this.mac.update(bArr, 0, this.blockSize);
        this.cipherInitialized = false;
        if (this.initialAssociatedText != null) {
            processAADBytes(this.initialAssociatedText, 0, this.initialAssociatedText.length);
        }
    }

    private boolean verifyMac(byte[] bArr, int i) {
        byte b = 0;
        for (int i2 = 0; i2 < this.macSize; i2++) {
            b |= this.macBlock[i2] ^ bArr[i + i2];
        }
        return b == 0;
    }

    public int doFinal(byte[] bArr, int i) throws IllegalStateException, InvalidCipherTextException {
        initCipher();
        int i2 = this.bufOff;
        byte[] bArr2 = new byte[this.bufBlock.length];
        this.bufOff = 0;
        if (this.forEncryption) {
            int i3 = i + i2;
            if (bArr.length >= this.macSize + i3) {
                this.cipher.processBlock(this.bufBlock, 0, bArr2, 0);
                System.arraycopy(bArr2, 0, bArr, i, i2);
                this.mac.update(bArr2, 0, i2);
                calculateMac();
                System.arraycopy(this.macBlock, 0, bArr, i3, this.macSize);
                reset(false);
                return i2 + this.macSize;
            }
            throw new OutputLengthException("Output buffer too short");
        } else if (i2 < this.macSize) {
            throw new InvalidCipherTextException("data too short");
        } else if (bArr.length >= (i + i2) - this.macSize) {
            if (i2 > this.macSize) {
                this.mac.update(this.bufBlock, 0, i2 - this.macSize);
                this.cipher.processBlock(this.bufBlock, 0, bArr2, 0);
                System.arraycopy(bArr2, 0, bArr, i, i2 - this.macSize);
            }
            calculateMac();
            if (verifyMac(this.bufBlock, i2 - this.macSize)) {
                reset(false);
                return i2 - this.macSize;
            }
            throw new InvalidCipherTextException("mac check in EAX failed");
        } else {
            throw new OutputLengthException("Output buffer too short");
        }
    }

    public String getAlgorithmName() {
        return this.cipher.getUnderlyingCipher().getAlgorithmName() + "/EAX";
    }

    public int getBlockSize() {
        return this.cipher.getBlockSize();
    }

    public byte[] getMac() {
        byte[] bArr = new byte[this.macSize];
        System.arraycopy(this.macBlock, 0, bArr, 0, this.macSize);
        return bArr;
    }

    public int getOutputSize(int i) {
        int i2 = i + this.bufOff;
        if (this.forEncryption) {
            return i2 + this.macSize;
        }
        if (i2 < this.macSize) {
            return 0;
        }
        return i2 - this.macSize;
    }

    public BlockCipher getUnderlyingCipher() {
        return this.cipher.getUnderlyingCipher();
    }

    public int getUpdateOutputSize(int i) {
        int i2 = i + this.bufOff;
        if (!this.forEncryption) {
            if (i2 < this.macSize) {
                return 0;
            }
            i2 -= this.macSize;
        }
        return i2 - (i2 % this.blockSize);
    }

    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        CipherParameters cipherParameters2;
        byte[] bArr;
        this.forEncryption = z;
        if (cipherParameters instanceof AEADParameters) {
            AEADParameters aEADParameters = (AEADParameters) cipherParameters;
            bArr = aEADParameters.getNonce();
            this.initialAssociatedText = aEADParameters.getAssociatedText();
            this.macSize = aEADParameters.getMacSize() / 8;
            cipherParameters2 = aEADParameters.getKey();
        } else if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            bArr = parametersWithIV.getIV();
            this.initialAssociatedText = null;
            this.macSize = this.mac.getMacSize() / 2;
            cipherParameters2 = parametersWithIV.getParameters();
        } else {
            throw new IllegalArgumentException("invalid parameters passed to EAX");
        }
        this.bufBlock = new byte[(z ? this.blockSize : this.blockSize + this.macSize)];
        byte[] bArr2 = new byte[this.blockSize];
        this.mac.init(cipherParameters2);
        bArr2[this.blockSize - 1] = 0;
        this.mac.update(bArr2, 0, this.blockSize);
        this.mac.update(bArr, 0, bArr.length);
        this.mac.doFinal(this.nonceMac, 0);
        this.cipher.init(true, new ParametersWithIV(null, this.nonceMac));
        reset();
    }

    public void processAADByte(byte b) {
        if (!this.cipherInitialized) {
            this.mac.update(b);
            return;
        }
        throw new IllegalStateException("AAD data cannot be added after encryption/decryption processing has begun.");
    }

    public void processAADBytes(byte[] bArr, int i, int i2) {
        if (!this.cipherInitialized) {
            this.mac.update(bArr, i, i2);
            return;
        }
        throw new IllegalStateException("AAD data cannot be added after encryption/decryption processing has begun.");
    }

    public int processByte(byte b, byte[] bArr, int i) throws DataLengthException {
        initCipher();
        return process(b, bArr, i);
    }

    public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws DataLengthException {
        initCipher();
        if (bArr.length >= i + i2) {
            int i4 = 0;
            for (int i5 = 0; i5 != i2; i5++) {
                i4 += process(bArr[i + i5], bArr2, i3 + i4);
            }
            return i4;
        }
        throw new DataLengthException("Input buffer too short");
    }

    public void reset() {
        reset(true);
    }
}
