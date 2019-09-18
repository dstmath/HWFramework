package org.bouncycastle.crypto.modes;

import java.io.ByteArrayOutputStream;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

public class KCCMBlockCipher implements AEADBlockCipher {
    private static final int BITS_IN_BYTE = 8;
    private static final int BYTES_IN_INT = 4;
    private static final int MAX_MAC_BIT_LENGTH = 512;
    private static final int MIN_MAC_BIT_LENGTH = 64;
    private byte[] G1;
    private int Nb_;
    private ExposedByteArrayOutputStream associatedText;
    private byte[] buffer;
    private byte[] counter;
    private ExposedByteArrayOutputStream data;
    private BlockCipher engine;
    private boolean forEncryption;
    private byte[] initialAssociatedText;
    private byte[] mac;
    private byte[] macBlock;
    private int macSize;
    private byte[] nonce;
    private byte[] s;

    private class ExposedByteArrayOutputStream extends ByteArrayOutputStream {
        public ExposedByteArrayOutputStream() {
        }

        public byte[] getBuffer() {
            return this.buf;
        }
    }

    public KCCMBlockCipher(BlockCipher blockCipher) {
        this(blockCipher, 4);
    }

    public KCCMBlockCipher(BlockCipher blockCipher, int i) {
        this.associatedText = new ExposedByteArrayOutputStream();
        this.data = new ExposedByteArrayOutputStream();
        this.Nb_ = 4;
        this.engine = blockCipher;
        this.macSize = blockCipher.getBlockSize();
        this.nonce = new byte[blockCipher.getBlockSize()];
        this.initialAssociatedText = new byte[blockCipher.getBlockSize()];
        this.mac = new byte[blockCipher.getBlockSize()];
        this.macBlock = new byte[blockCipher.getBlockSize()];
        this.G1 = new byte[blockCipher.getBlockSize()];
        this.buffer = new byte[blockCipher.getBlockSize()];
        this.s = new byte[blockCipher.getBlockSize()];
        this.counter = new byte[blockCipher.getBlockSize()];
        setNb(i);
    }

    private void CalculateMac(byte[] bArr, int i, int i2) {
        while (i2 > 0) {
            for (int i3 = 0; i3 < this.engine.getBlockSize(); i3++) {
                byte[] bArr2 = this.macBlock;
                bArr2[i3] = (byte) (bArr2[i3] ^ bArr[i + i3]);
            }
            this.engine.processBlock(this.macBlock, 0, this.macBlock, 0);
            i2 -= this.engine.getBlockSize();
            i += this.engine.getBlockSize();
        }
    }

    private void ProcessBlock(byte[] bArr, int i, int i2, byte[] bArr2, int i3) {
        for (int i4 = 0; i4 < this.counter.length; i4++) {
            byte[] bArr3 = this.s;
            bArr3[i4] = (byte) (bArr3[i4] + this.counter[i4]);
        }
        this.engine.processBlock(this.s, 0, this.buffer, 0);
        for (int i5 = 0; i5 < this.engine.getBlockSize(); i5++) {
            bArr2[i3 + i5] = (byte) (this.buffer[i5] ^ bArr[i + i5]);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0045 A[LOOP:0: B:22:0x003e->B:24:0x0045, LOOP_END] */
    private byte getFlag(boolean z, int i) {
        String binaryString;
        String str;
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(z ? "1" : "0");
        if (i == 8) {
            str = "010";
        } else if (i == 16) {
            str = "011";
        } else if (i == 32) {
            str = "100";
        } else if (i != 48) {
            if (i == 64) {
                str = "110";
            }
            binaryString = Integer.toBinaryString(this.Nb_ - 1);
            while (binaryString.length() < 4) {
                binaryString = new StringBuffer(binaryString).insert(0, "0").toString();
            }
            stringBuffer.append(binaryString);
            return (byte) Integer.parseInt(stringBuffer.toString(), 2);
        } else {
            str = "101";
        }
        stringBuffer.append(str);
        binaryString = Integer.toBinaryString(this.Nb_ - 1);
        while (binaryString.length() < 4) {
        }
        stringBuffer.append(binaryString);
        return (byte) Integer.parseInt(stringBuffer.toString(), 2);
    }

    private void intToBytes(int i, byte[] bArr, int i2) {
        bArr[i2 + 3] = (byte) (i >> 24);
        bArr[i2 + 2] = (byte) (i >> 16);
        bArr[i2 + 1] = (byte) (i >> 8);
        bArr[i2] = (byte) i;
    }

    private void processAAD(byte[] bArr, int i, int i2, int i3) {
        if (i2 - i < this.engine.getBlockSize()) {
            throw new IllegalArgumentException("authText buffer too short");
        } else if (i2 % this.engine.getBlockSize() == 0) {
            System.arraycopy(this.nonce, 0, this.G1, 0, (this.nonce.length - this.Nb_) - 1);
            intToBytes(i3, this.buffer, 0);
            System.arraycopy(this.buffer, 0, this.G1, (this.nonce.length - this.Nb_) - 1, 4);
            this.G1[this.G1.length - 1] = getFlag(true, this.macSize);
            this.engine.processBlock(this.G1, 0, this.macBlock, 0);
            intToBytes(i2, this.buffer, 0);
            if (i2 <= this.engine.getBlockSize() - this.Nb_) {
                for (int i4 = 0; i4 < i2; i4++) {
                    byte[] bArr2 = this.buffer;
                    int i5 = this.Nb_ + i4;
                    bArr2[i5] = (byte) (bArr2[i5] ^ bArr[i + i4]);
                }
                for (int i6 = 0; i6 < this.engine.getBlockSize(); i6++) {
                    byte[] bArr3 = this.macBlock;
                    bArr3[i6] = (byte) (bArr3[i6] ^ this.buffer[i6]);
                }
                this.engine.processBlock(this.macBlock, 0, this.macBlock, 0);
                return;
            }
            for (int i7 = 0; i7 < this.engine.getBlockSize(); i7++) {
                byte[] bArr4 = this.macBlock;
                bArr4[i7] = (byte) (bArr4[i7] ^ this.buffer[i7]);
            }
            this.engine.processBlock(this.macBlock, 0, this.macBlock, 0);
            while (i2 != 0) {
                for (int i8 = 0; i8 < this.engine.getBlockSize(); i8++) {
                    byte[] bArr5 = this.macBlock;
                    bArr5[i8] = (byte) (bArr5[i8] ^ bArr[i8 + i]);
                }
                this.engine.processBlock(this.macBlock, 0, this.macBlock, 0);
                i += this.engine.getBlockSize();
                i2 -= this.engine.getBlockSize();
            }
        } else {
            throw new IllegalArgumentException("padding not supported");
        }
    }

    private void setNb(int i) {
        if (i == 4 || i == 6 || i == 8) {
            this.Nb_ = i;
            return;
        }
        throw new IllegalArgumentException("Nb = 4 is recommended by DSTU7624 but can be changed to only 6 or 8 in this implementation");
    }

    public int doFinal(byte[] bArr, int i) throws IllegalStateException, InvalidCipherTextException {
        int processPacket = processPacket(this.data.getBuffer(), 0, this.data.size(), bArr, i);
        reset();
        return processPacket;
    }

    public String getAlgorithmName() {
        return this.engine.getAlgorithmName() + "/KCCM";
    }

    public byte[] getMac() {
        return Arrays.clone(this.mac);
    }

    public int getOutputSize(int i) {
        return i + this.macSize;
    }

    public BlockCipher getUnderlyingCipher() {
        return this.engine;
    }

    public int getUpdateOutputSize(int i) {
        return i;
    }

    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        CipherParameters cipherParameters2;
        if (cipherParameters instanceof AEADParameters) {
            AEADParameters aEADParameters = (AEADParameters) cipherParameters;
            if (aEADParameters.getMacSize() > 512 || aEADParameters.getMacSize() < 64 || aEADParameters.getMacSize() % 8 != 0) {
                throw new IllegalArgumentException("Invalid mac size specified");
            }
            this.nonce = aEADParameters.getNonce();
            this.macSize = aEADParameters.getMacSize() / 8;
            this.initialAssociatedText = aEADParameters.getAssociatedText();
            cipherParameters2 = aEADParameters.getKey();
        } else if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            this.nonce = parametersWithIV.getIV();
            this.macSize = this.engine.getBlockSize();
            this.initialAssociatedText = null;
            cipherParameters2 = parametersWithIV.getParameters();
        } else {
            throw new IllegalArgumentException("Invalid parameters specified");
        }
        this.mac = new byte[this.macSize];
        this.forEncryption = z;
        this.engine.init(true, cipherParameters2);
        this.counter[0] = 1;
        if (this.initialAssociatedText != null) {
            processAADBytes(this.initialAssociatedText, 0, this.initialAssociatedText.length);
        }
    }

    public void processAADByte(byte b) {
        this.associatedText.write(b);
    }

    public void processAADBytes(byte[] bArr, int i, int i2) {
        this.associatedText.write(bArr, i, i2);
    }

    public int processByte(byte b, byte[] bArr, int i) throws DataLengthException, IllegalStateException {
        this.data.write(b);
        return 0;
    }

    public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws DataLengthException, IllegalStateException {
        if (bArr.length >= i + i2) {
            this.data.write(bArr, i, i2);
            return 0;
        }
        throw new DataLengthException("input buffer too short");
    }

    public int processPacket(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws IllegalStateException, InvalidCipherTextException {
        byte[] buffer2;
        int size;
        int size2;
        if (bArr.length - i < i2) {
            throw new DataLengthException("input buffer too short");
        } else if (bArr2.length - i3 >= i2) {
            if (this.associatedText.size() > 0) {
                if (this.forEncryption) {
                    buffer2 = this.associatedText.getBuffer();
                    size = this.associatedText.size();
                    size2 = this.data.size();
                } else {
                    buffer2 = this.associatedText.getBuffer();
                    size = this.associatedText.size();
                    size2 = this.data.size() - this.macSize;
                }
                processAAD(buffer2, 0, size, size2);
            }
            if (this.forEncryption) {
                if (i2 % this.engine.getBlockSize() == 0) {
                    CalculateMac(bArr, i, i2);
                    this.engine.processBlock(this.nonce, 0, this.s, 0);
                    int i4 = i3;
                    int i5 = i;
                    int i6 = i2;
                    while (i6 > 0) {
                        ProcessBlock(bArr, i5, i2, bArr2, i4);
                        i6 -= this.engine.getBlockSize();
                        i5 += this.engine.getBlockSize();
                        i4 += this.engine.getBlockSize();
                    }
                    for (int i7 = 0; i7 < this.counter.length; i7++) {
                        byte[] bArr3 = this.s;
                        bArr3[i7] = (byte) (bArr3[i7] + this.counter[i7]);
                    }
                    this.engine.processBlock(this.s, 0, this.buffer, 0);
                    for (int i8 = 0; i8 < this.macSize; i8++) {
                        bArr2[i4 + i8] = (byte) (this.buffer[i8] ^ this.macBlock[i8]);
                    }
                    System.arraycopy(this.macBlock, 0, this.mac, 0, this.macSize);
                    reset();
                    return i2 + this.macSize;
                }
                throw new DataLengthException("partial blocks not supported");
            } else if ((i2 - this.macSize) % this.engine.getBlockSize() == 0) {
                this.engine.processBlock(this.nonce, 0, this.s, 0);
                int blockSize = i2 / this.engine.getBlockSize();
                int i9 = i3;
                int i10 = i;
                for (int i11 = 0; i11 < blockSize; i11++) {
                    ProcessBlock(bArr, i10, i2, bArr2, i9);
                    i10 += this.engine.getBlockSize();
                    i9 += this.engine.getBlockSize();
                }
                if (i2 > i10) {
                    for (int i12 = 0; i12 < this.counter.length; i12++) {
                        byte[] bArr4 = this.s;
                        bArr4[i12] = (byte) (bArr4[i12] + this.counter[i12]);
                    }
                    this.engine.processBlock(this.s, 0, this.buffer, 0);
                    for (int i13 = 0; i13 < this.macSize; i13++) {
                        bArr2[i9 + i13] = (byte) (this.buffer[i13] ^ bArr[i10 + i13]);
                    }
                    i9 += this.macSize;
                }
                for (int i14 = 0; i14 < this.counter.length; i14++) {
                    byte[] bArr5 = this.s;
                    bArr5[i14] = (byte) (bArr5[i14] + this.counter[i14]);
                }
                this.engine.processBlock(this.s, 0, this.buffer, 0);
                System.arraycopy(bArr2, i9 - this.macSize, this.buffer, 0, this.macSize);
                CalculateMac(bArr2, 0, i9 - this.macSize);
                System.arraycopy(this.macBlock, 0, this.mac, 0, this.macSize);
                byte[] bArr6 = new byte[this.macSize];
                System.arraycopy(this.buffer, 0, bArr6, 0, this.macSize);
                if (Arrays.constantTimeAreEqual(this.mac, bArr6)) {
                    reset();
                    return i2 - this.macSize;
                }
                throw new InvalidCipherTextException("mac check failed");
            } else {
                throw new DataLengthException("partial blocks not supported");
            }
        } else {
            throw new OutputLengthException("output buffer too short");
        }
    }

    public void reset() {
        Arrays.fill(this.G1, (byte) 0);
        Arrays.fill(this.buffer, (byte) 0);
        Arrays.fill(this.counter, (byte) 0);
        Arrays.fill(this.macBlock, (byte) 0);
        this.counter[0] = 1;
        this.data.reset();
        this.associatedText.reset();
        if (this.initialAssociatedText != null) {
            processAADBytes(this.initialAssociatedText, 0, this.initialAssociatedText.length);
        }
    }
}
