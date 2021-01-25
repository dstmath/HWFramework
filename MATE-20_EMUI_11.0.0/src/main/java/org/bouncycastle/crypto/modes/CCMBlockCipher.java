package org.bouncycastle.crypto.modes;

import java.io.ByteArrayOutputStream;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;
import org.bouncycastle.util.Arrays;

public class CCMBlockCipher implements AEADBlockCipher {
    private ExposedByteArrayOutputStream associatedText = new ExposedByteArrayOutputStream();
    private int blockSize;
    private BlockCipher cipher;
    private ExposedByteArrayOutputStream data = new ExposedByteArrayOutputStream();
    private boolean forEncryption;
    private byte[] initialAssociatedText;
    private CipherParameters keyParam;
    private byte[] macBlock;
    private int macSize;
    private byte[] nonce;

    /* access modifiers changed from: private */
    public class ExposedByteArrayOutputStream extends ByteArrayOutputStream {
        public ExposedByteArrayOutputStream() {
        }

        public byte[] getBuffer() {
            return this.buf;
        }
    }

    public CCMBlockCipher(BlockCipher blockCipher) {
        this.cipher = blockCipher;
        this.blockSize = blockCipher.getBlockSize();
        int i = this.blockSize;
        this.macBlock = new byte[i];
        if (i != 16) {
            throw new IllegalArgumentException("cipher required with a block size of 16.");
        }
    }

    private int calculateMac(byte[] bArr, int i, int i2, byte[] bArr2) {
        CBCBlockCipherMac cBCBlockCipherMac = new CBCBlockCipherMac(this.cipher, this.macSize * 8);
        cBCBlockCipherMac.init(this.keyParam);
        byte[] bArr3 = new byte[16];
        if (hasAssociatedText()) {
            bArr3[0] = (byte) (bArr3[0] | 64);
        }
        int i3 = 2;
        bArr3[0] = (byte) (bArr3[0] | ((((cBCBlockCipherMac.getMacSize() - 2) / 2) & 7) << 3));
        byte b = bArr3[0];
        byte[] bArr4 = this.nonce;
        bArr3[0] = (byte) (b | (((15 - bArr4.length) - 1) & 7));
        System.arraycopy(bArr4, 0, bArr3, 1, bArr4.length);
        int i4 = i2;
        int i5 = 1;
        while (i4 > 0) {
            bArr3[bArr3.length - i5] = (byte) (i4 & GF2Field.MASK);
            i4 >>>= 8;
            i5++;
        }
        cBCBlockCipherMac.update(bArr3, 0, bArr3.length);
        if (hasAssociatedText()) {
            int associatedTextLength = getAssociatedTextLength();
            if (associatedTextLength < 65280) {
                cBCBlockCipherMac.update((byte) (associatedTextLength >> 8));
                cBCBlockCipherMac.update((byte) associatedTextLength);
            } else {
                cBCBlockCipherMac.update((byte) -1);
                cBCBlockCipherMac.update((byte) -2);
                cBCBlockCipherMac.update((byte) (associatedTextLength >> 24));
                cBCBlockCipherMac.update((byte) (associatedTextLength >> 16));
                cBCBlockCipherMac.update((byte) (associatedTextLength >> 8));
                cBCBlockCipherMac.update((byte) associatedTextLength);
                i3 = 6;
            }
            byte[] bArr5 = this.initialAssociatedText;
            if (bArr5 != null) {
                cBCBlockCipherMac.update(bArr5, 0, bArr5.length);
            }
            if (this.associatedText.size() > 0) {
                cBCBlockCipherMac.update(this.associatedText.getBuffer(), 0, this.associatedText.size());
            }
            int i6 = (i3 + associatedTextLength) % 16;
            if (i6 != 0) {
                while (i6 != 16) {
                    cBCBlockCipherMac.update((byte) 0);
                    i6++;
                }
            }
        }
        cBCBlockCipherMac.update(bArr, i, i2);
        return cBCBlockCipherMac.doFinal(bArr2, 0);
    }

    private int getAssociatedTextLength() {
        int size = this.associatedText.size();
        byte[] bArr = this.initialAssociatedText;
        return size + (bArr == null ? 0 : bArr.length);
    }

    private int getMacSize(boolean z, int i) {
        if (!z || (i >= 32 && i <= 128 && (i & 15) == 0)) {
            return i >>> 3;
        }
        throw new IllegalArgumentException("tag length in octets must be one of {4,6,8,10,12,14,16}");
    }

    private boolean hasAssociatedText() {
        return getAssociatedTextLength() > 0;
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int doFinal(byte[] bArr, int i) throws IllegalStateException, InvalidCipherTextException {
        int processPacket = processPacket(this.data.getBuffer(), 0, this.data.size(), bArr, i);
        reset();
        return processPacket;
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName() + "/CCM";
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public byte[] getMac() {
        byte[] bArr = new byte[this.macSize];
        System.arraycopy(this.macBlock, 0, bArr, 0, bArr.length);
        return bArr;
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int getOutputSize(int i) {
        int size = i + this.data.size();
        if (this.forEncryption) {
            return size + this.macSize;
        }
        int i2 = this.macSize;
        if (size < i2) {
            return 0;
        }
        return size - i2;
    }

    @Override // org.bouncycastle.crypto.modes.AEADBlockCipher
    public BlockCipher getUnderlyingCipher() {
        return this.cipher;
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int getUpdateOutputSize(int i) {
        return 0;
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        CipherParameters cipherParameters2;
        this.forEncryption = z;
        if (cipherParameters instanceof AEADParameters) {
            AEADParameters aEADParameters = (AEADParameters) cipherParameters;
            this.nonce = aEADParameters.getNonce();
            this.initialAssociatedText = aEADParameters.getAssociatedText();
            this.macSize = getMacSize(z, aEADParameters.getMacSize());
            cipherParameters2 = aEADParameters.getKey();
        } else if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            this.nonce = parametersWithIV.getIV();
            this.initialAssociatedText = null;
            this.macSize = getMacSize(z, 64);
            cipherParameters2 = parametersWithIV.getParameters();
        } else {
            throw new IllegalArgumentException("invalid parameters passed to CCM: " + cipherParameters.getClass().getName());
        }
        if (cipherParameters2 != null) {
            this.keyParam = cipherParameters2;
        }
        byte[] bArr = this.nonce;
        if (bArr == null || bArr.length < 7 || bArr.length > 13) {
            throw new IllegalArgumentException("nonce must have length from 7 to 13 octets");
        }
        reset();
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public void processAADByte(byte b) {
        this.associatedText.write(b);
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public void processAADBytes(byte[] bArr, int i, int i2) {
        this.associatedText.write(bArr, i, i2);
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int processByte(byte b, byte[] bArr, int i) throws DataLengthException, IllegalStateException {
        this.data.write(b);
        return 0;
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws DataLengthException, IllegalStateException {
        if (bArr.length >= i + i2) {
            this.data.write(bArr, i, i2);
            return 0;
        }
        throw new DataLengthException("Input buffer too short");
    }

    public int processPacket(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws IllegalStateException, InvalidCipherTextException, DataLengthException {
        int i4;
        if (this.keyParam != null) {
            int length = 15 - this.nonce.length;
            if (length >= 4 || i2 < (1 << (length * 8))) {
                byte[] bArr3 = new byte[this.blockSize];
                bArr3[0] = (byte) ((length - 1) & 7);
                byte[] bArr4 = this.nonce;
                System.arraycopy(bArr4, 0, bArr3, 1, bArr4.length);
                SICBlockCipher sICBlockCipher = new SICBlockCipher(this.cipher);
                sICBlockCipher.init(this.forEncryption, new ParametersWithIV(this.keyParam, bArr3));
                if (this.forEncryption) {
                    int i5 = this.macSize + i2;
                    if (bArr2.length >= i5 + i3) {
                        calculateMac(bArr, i, i2, this.macBlock);
                        byte[] bArr5 = new byte[this.blockSize];
                        sICBlockCipher.processBlock(this.macBlock, 0, bArr5, 0);
                        int i6 = i;
                        int i7 = i3;
                        while (true) {
                            int i8 = i + i2;
                            int i9 = this.blockSize;
                            if (i6 < i8 - i9) {
                                sICBlockCipher.processBlock(bArr, i6, bArr2, i7);
                                int i10 = this.blockSize;
                                i7 += i10;
                                i6 += i10;
                            } else {
                                byte[] bArr6 = new byte[i9];
                                int i11 = i8 - i6;
                                System.arraycopy(bArr, i6, bArr6, 0, i11);
                                sICBlockCipher.processBlock(bArr6, 0, bArr6, 0);
                                System.arraycopy(bArr6, 0, bArr2, i7, i11);
                                System.arraycopy(bArr5, 0, bArr2, i3 + i2, this.macSize);
                                return i5;
                            }
                        }
                    } else {
                        throw new OutputLengthException("Output buffer too short.");
                    }
                } else {
                    int i12 = this.macSize;
                    if (i2 >= i12) {
                        int i13 = i2 - i12;
                        if (bArr2.length >= i13 + i3) {
                            int i14 = i + i13;
                            System.arraycopy(bArr, i14, this.macBlock, 0, i12);
                            byte[] bArr7 = this.macBlock;
                            sICBlockCipher.processBlock(bArr7, 0, bArr7, 0);
                            int i15 = this.macSize;
                            while (true) {
                                byte[] bArr8 = this.macBlock;
                                if (i15 == bArr8.length) {
                                    break;
                                }
                                bArr8[i15] = 0;
                                i15++;
                            }
                            int i16 = i;
                            int i17 = i3;
                            while (true) {
                                i4 = this.blockSize;
                                if (i16 >= i14 - i4) {
                                    break;
                                }
                                sICBlockCipher.processBlock(bArr, i16, bArr2, i17);
                                int i18 = this.blockSize;
                                i17 += i18;
                                i16 += i18;
                            }
                            byte[] bArr9 = new byte[i4];
                            int i19 = i13 - (i16 - i);
                            System.arraycopy(bArr, i16, bArr9, 0, i19);
                            sICBlockCipher.processBlock(bArr9, 0, bArr9, 0);
                            System.arraycopy(bArr9, 0, bArr2, i17, i19);
                            byte[] bArr10 = new byte[this.blockSize];
                            calculateMac(bArr2, i3, i13, bArr10);
                            if (Arrays.constantTimeAreEqual(this.macBlock, bArr10)) {
                                return i13;
                            }
                            throw new InvalidCipherTextException("mac check in CCM failed");
                        }
                        throw new OutputLengthException("Output buffer too short.");
                    }
                    throw new InvalidCipherTextException("data too short");
                }
            } else {
                throw new IllegalStateException("CCM packet too large for choice of q.");
            }
        } else {
            throw new IllegalStateException("CCM cipher unitialized.");
        }
    }

    public byte[] processPacket(byte[] bArr, int i, int i2) throws IllegalStateException, InvalidCipherTextException {
        int i3;
        if (this.forEncryption) {
            i3 = this.macSize + i2;
        } else {
            int i4 = this.macSize;
            if (i2 >= i4) {
                i3 = i2 - i4;
            } else {
                throw new InvalidCipherTextException("data too short");
            }
        }
        byte[] bArr2 = new byte[i3];
        processPacket(bArr, i, i2, bArr2, 0);
        return bArr2;
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public void reset() {
        this.cipher.reset();
        this.associatedText.reset();
        this.data.reset();
    }
}
