package com.android.org.bouncycastle.crypto.modes;

import com.android.org.bouncycastle.crypto.BlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.InvalidCipherTextException;
import com.android.org.bouncycastle.crypto.Mac;
import com.android.org.bouncycastle.crypto.OutputLengthException;
import com.android.org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import com.android.org.bouncycastle.crypto.params.AEADParameters;
import com.android.org.bouncycastle.crypto.params.ParametersWithIV;
import com.android.org.bouncycastle.util.Arrays;
import java.io.ByteArrayOutputStream;

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

    private class ExposedByteArrayOutputStream extends ByteArrayOutputStream {
        public ExposedByteArrayOutputStream() {
        }

        public byte[] getBuffer() {
            return this.buf;
        }
    }

    public CCMBlockCipher(BlockCipher c) {
        this.cipher = c;
        this.blockSize = c.getBlockSize();
        this.macBlock = new byte[this.blockSize];
        if (this.blockSize != 16) {
            throw new IllegalArgumentException("cipher required with a block size of 16.");
        }
    }

    public BlockCipher getUnderlyingCipher() {
        return this.cipher;
    }

    public void init(boolean forEncryption2, CipherParameters params) throws IllegalArgumentException {
        CipherParameters cipherParameters;
        this.forEncryption = forEncryption2;
        if (params instanceof AEADParameters) {
            AEADParameters param = (AEADParameters) params;
            this.nonce = param.getNonce();
            this.initialAssociatedText = param.getAssociatedText();
            this.macSize = param.getMacSize() / 8;
            cipherParameters = param.getKey();
        } else if (params instanceof ParametersWithIV) {
            ParametersWithIV param2 = (ParametersWithIV) params;
            this.nonce = param2.getIV();
            this.initialAssociatedText = null;
            this.macSize = this.macBlock.length / 2;
            cipherParameters = param2.getParameters();
        } else {
            throw new IllegalArgumentException("invalid parameters passed to CCM: " + params.getClass().getName());
        }
        if (cipherParameters != null) {
            this.keyParam = cipherParameters;
        }
        if (this.nonce == null || this.nonce.length < 7 || this.nonce.length > 13) {
            throw new IllegalArgumentException("nonce must have length from 7 to 13 octets");
        }
        reset();
    }

    public String getAlgorithmName() {
        return this.cipher.getAlgorithmName() + "/CCM";
    }

    public void processAADByte(byte in) {
        this.associatedText.write(in);
    }

    public void processAADBytes(byte[] in, int inOff, int len) {
        this.associatedText.write(in, inOff, len);
    }

    public int processByte(byte in, byte[] out, int outOff) throws DataLengthException, IllegalStateException {
        this.data.write(in);
        return 0;
    }

    public int processBytes(byte[] in, int inOff, int inLen, byte[] out, int outOff) throws DataLengthException, IllegalStateException {
        if (in.length >= inOff + inLen) {
            this.data.write(in, inOff, inLen);
            return 0;
        }
        throw new DataLengthException("Input buffer too short");
    }

    public int doFinal(byte[] out, int outOff) throws IllegalStateException, InvalidCipherTextException {
        int len = processPacket(this.data.getBuffer(), 0, this.data.size(), out, outOff);
        reset();
        return len;
    }

    public void reset() {
        this.cipher.reset();
        this.associatedText.reset();
        this.data.reset();
    }

    public byte[] getMac() {
        byte[] mac = new byte[this.macSize];
        System.arraycopy(this.macBlock, 0, mac, 0, mac.length);
        return mac;
    }

    public int getUpdateOutputSize(int len) {
        return 0;
    }

    public int getOutputSize(int len) {
        int totalData = this.data.size() + len;
        if (this.forEncryption) {
            return this.macSize + totalData;
        }
        return totalData < this.macSize ? 0 : totalData - this.macSize;
    }

    public byte[] processPacket(byte[] in, int inOff, int inLen) throws IllegalStateException, InvalidCipherTextException {
        byte[] output;
        if (this.forEncryption) {
            output = new byte[(this.macSize + inLen)];
        } else if (inLen >= this.macSize) {
            output = new byte[(inLen - this.macSize)];
        } else {
            throw new InvalidCipherTextException("data too short");
        }
        processPacket(in, inOff, inLen, output, 0);
        return output;
    }

    public int processPacket(byte[] in, int inOff, int inLen, byte[] output, int outOff) throws IllegalStateException, InvalidCipherTextException, DataLengthException {
        int outputLen;
        byte[] bArr = in;
        int i = inOff;
        int i2 = inLen;
        byte[] bArr2 = output;
        int i3 = outOff;
        if (this.keyParam != null) {
            int n = this.nonce.length;
            int q = 15 - n;
            if (q >= 4 || i2 < (1 << (8 * q))) {
                byte[] iv = new byte[this.blockSize];
                iv[0] = (byte) ((q - 1) & 7);
                System.arraycopy(this.nonce, 0, iv, 1, this.nonce.length);
                BlockCipher ctrCipher = new SICBlockCipher(this.cipher);
                ctrCipher.init(this.forEncryption, new ParametersWithIV(this.keyParam, iv));
                int inIndex = i;
                int outIndex = i3;
                if (this.forEncryption) {
                    outputLen = this.macSize + i2;
                    if (bArr2.length >= outputLen + i3) {
                        calculateMac(bArr, i, i2, this.macBlock);
                        byte[] encMac = new byte[this.blockSize];
                        ctrCipher.processBlock(this.macBlock, 0, encMac, 0);
                        while (inIndex < (i + i2) - this.blockSize) {
                            ctrCipher.processBlock(bArr, inIndex, bArr2, outIndex);
                            outIndex += this.blockSize;
                            inIndex += this.blockSize;
                        }
                        byte[] block = new byte[this.blockSize];
                        int i4 = n;
                        System.arraycopy(bArr, inIndex, block, 0, (i2 + i) - inIndex);
                        ctrCipher.processBlock(block, 0, block, 0);
                        System.arraycopy(block, 0, bArr2, outIndex, (i2 + i) - inIndex);
                        int i5 = q;
                        System.arraycopy(encMac, 0, bArr2, i3 + i2, this.macSize);
                    } else {
                        int i6 = q;
                        throw new OutputLengthException("Output buffer too short.");
                    }
                } else {
                    int i7 = q;
                    if (i2 >= this.macSize) {
                        outputLen = i2 - this.macSize;
                        if (bArr2.length >= outputLen + i3) {
                            byte b = 0;
                            System.arraycopy(bArr, i + outputLen, this.macBlock, 0, this.macSize);
                            ctrCipher.processBlock(this.macBlock, 0, this.macBlock, 0);
                            int i8 = this.macSize;
                            while (i8 != this.macBlock.length) {
                                this.macBlock[i8] = b;
                                i8++;
                                b = 0;
                            }
                            while (inIndex < (i + outputLen) - this.blockSize) {
                                ctrCipher.processBlock(bArr, inIndex, bArr2, outIndex);
                                outIndex += this.blockSize;
                                inIndex += this.blockSize;
                            }
                            byte[] block2 = new byte[this.blockSize];
                            System.arraycopy(bArr, inIndex, block2, 0, outputLen - (inIndex - i));
                            ctrCipher.processBlock(block2, 0, block2, 0);
                            System.arraycopy(block2, 0, bArr2, outIndex, outputLen - (inIndex - i));
                            byte[] calculatedMacBlock = new byte[this.blockSize];
                            calculateMac(bArr2, i3, outputLen, calculatedMacBlock);
                            if (!Arrays.constantTimeAreEqual(this.macBlock, calculatedMacBlock)) {
                                throw new InvalidCipherTextException("mac check in CCM failed");
                            }
                        } else {
                            throw new OutputLengthException("Output buffer too short.");
                        }
                    } else {
                        throw new InvalidCipherTextException("data too short");
                    }
                }
                return outputLen;
            }
            throw new IllegalStateException("CCM packet too large for choice of q.");
        }
        throw new IllegalStateException("CCM cipher unitialized.");
    }

    private int calculateMac(byte[] data2, int dataOff, int dataLen, byte[] macBlock2) {
        int extra;
        Mac cMac = new CBCBlockCipherMac(this.cipher, this.macSize * 8);
        cMac.init(this.keyParam);
        byte[] b0 = new byte[16];
        if (hasAssociatedText()) {
            b0[0] = (byte) (b0[0] | 64);
        }
        b0[0] = (byte) (b0[0] | ((((cMac.getMacSize() - 2) / 2) & 7) << 3));
        int count = 1;
        b0[0] = (byte) (b0[0] | (((15 - this.nonce.length) - 1) & 7));
        System.arraycopy(this.nonce, 0, b0, 1, this.nonce.length);
        int q = dataLen;
        while (true) {
            int count2 = count;
            if (q <= 0) {
                break;
            }
            b0[b0.length - count2] = (byte) (q & 255);
            q >>>= 8;
            count = count2 + 1;
        }
        cMac.update(b0, 0, b0.length);
        if (hasAssociatedText()) {
            int textLength = getAssociatedTextLength();
            if (textLength < 65280) {
                cMac.update((byte) (textLength >> 8));
                cMac.update((byte) textLength);
                extra = 2;
            } else {
                cMac.update((byte) -1);
                cMac.update((byte) -2);
                cMac.update((byte) (textLength >> 24));
                cMac.update((byte) (textLength >> 16));
                cMac.update((byte) (textLength >> 8));
                cMac.update((byte) textLength);
                extra = 6;
            }
            if (this.initialAssociatedText != null) {
                cMac.update(this.initialAssociatedText, 0, this.initialAssociatedText.length);
            }
            if (this.associatedText.size() > 0) {
                cMac.update(this.associatedText.getBuffer(), 0, this.associatedText.size());
            }
            int extra2 = (extra + textLength) % 16;
            if (extra2 != 0) {
                for (int i = extra2; i != 16; i++) {
                    cMac.update((byte) 0);
                }
            }
        }
        cMac.update(data2, dataOff, dataLen);
        return cMac.doFinal(macBlock2, 0);
    }

    private int getAssociatedTextLength() {
        return this.associatedText.size() + (this.initialAssociatedText == null ? 0 : this.initialAssociatedText.length);
    }

    private boolean hasAssociatedText() {
        return getAssociatedTextLength() > 0;
    }
}
