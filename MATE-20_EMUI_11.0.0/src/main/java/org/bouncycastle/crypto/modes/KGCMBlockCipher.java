package org.bouncycastle.crypto.modes;

import java.io.ByteArrayOutputStream;
import org.bouncycastle.asn1.cmc.BodyPartID;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.modes.kgcm.KGCMMultiplier;
import org.bouncycastle.crypto.modes.kgcm.Tables16kKGCMMultiplier_512;
import org.bouncycastle.crypto.modes.kgcm.Tables4kKGCMMultiplier_128;
import org.bouncycastle.crypto.modes.kgcm.Tables8kKGCMMultiplier_256;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class KGCMBlockCipher implements AEADBlockCipher {
    private static final int MIN_MAC_BITS = 64;
    private ExposedByteArrayOutputStream associatedText = new ExposedByteArrayOutputStream();
    private long[] b;
    private final int blockSize;
    private BufferedBlockCipher ctrEngine;
    private ExposedByteArrayOutputStream data = new ExposedByteArrayOutputStream();
    private BlockCipher engine;
    private boolean forEncryption;
    private byte[] initialAssociatedText;
    private byte[] iv;
    private byte[] macBlock;
    private int macSize;
    private KGCMMultiplier multiplier;

    /* access modifiers changed from: private */
    public class ExposedByteArrayOutputStream extends ByteArrayOutputStream {
        public ExposedByteArrayOutputStream() {
        }

        public byte[] getBuffer() {
            return this.buf;
        }
    }

    public KGCMBlockCipher(BlockCipher blockCipher) {
        this.engine = blockCipher;
        this.ctrEngine = new BufferedBlockCipher(new KCTRBlockCipher(this.engine));
        this.macSize = -1;
        this.blockSize = this.engine.getBlockSize();
        int i = this.blockSize;
        this.initialAssociatedText = new byte[i];
        this.iv = new byte[i];
        this.multiplier = createDefaultMultiplier(i);
        this.b = new long[(this.blockSize >>> 3)];
        this.macBlock = null;
    }

    private void calculateMac(byte[] bArr, int i, int i2, int i3) {
        int i4 = i + i2;
        while (i < i4) {
            xorWithInput(this.b, bArr, i);
            this.multiplier.multiplyH(this.b);
            i += this.blockSize;
        }
        long j = (BodyPartID.bodyIdMax & ((long) i2)) << 3;
        long[] jArr = this.b;
        jArr[0] = ((((long) i3) & BodyPartID.bodyIdMax) << 3) ^ jArr[0];
        int i5 = this.blockSize >>> 4;
        jArr[i5] = jArr[i5] ^ j;
        this.macBlock = Pack.longToLittleEndian(jArr);
        BlockCipher blockCipher = this.engine;
        byte[] bArr2 = this.macBlock;
        blockCipher.processBlock(bArr2, 0, bArr2, 0);
    }

    private static KGCMMultiplier createDefaultMultiplier(int i) {
        if (i == 16) {
            return new Tables4kKGCMMultiplier_128();
        }
        if (i == 32) {
            return new Tables8kKGCMMultiplier_256();
        }
        if (i == 64) {
            return new Tables16kKGCMMultiplier_512();
        }
        throw new IllegalArgumentException("Only 128, 256, and 512 -bit block sizes supported");
    }

    private void processAAD(byte[] bArr, int i, int i2) {
        int i3 = i2 + i;
        while (i < i3) {
            xorWithInput(this.b, bArr, i);
            this.multiplier.multiplyH(this.b);
            i += this.blockSize;
        }
    }

    private static void xorWithInput(long[] jArr, byte[] bArr, int i) {
        for (int i2 = 0; i2 < jArr.length; i2++) {
            jArr[i2] = jArr[i2] ^ Pack.littleEndianToLong(bArr, i);
            i += 8;
        }
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int doFinal(byte[] bArr, int i) throws IllegalStateException, InvalidCipherTextException {
        int i2;
        int size = this.data.size();
        if (this.forEncryption || size >= this.macSize) {
            byte[] bArr2 = new byte[this.blockSize];
            this.engine.processBlock(bArr2, 0, bArr2, 0);
            long[] jArr = new long[(this.blockSize >>> 3)];
            Pack.littleEndianToLong(bArr2, 0, jArr);
            this.multiplier.init(jArr);
            Arrays.fill(bArr2, (byte) 0);
            Arrays.fill(jArr, 0);
            int size2 = this.associatedText.size();
            if (size2 > 0) {
                processAAD(this.associatedText.getBuffer(), 0, size2);
            }
            if (!this.forEncryption) {
                int i3 = size - this.macSize;
                if (bArr.length - i >= i3) {
                    calculateMac(this.data.getBuffer(), 0, i3, size2);
                    int processBytes = this.ctrEngine.processBytes(this.data.getBuffer(), 0, i3, bArr, i);
                    i2 = processBytes + this.ctrEngine.doFinal(bArr, i + processBytes);
                } else {
                    throw new OutputLengthException("Output buffer too short");
                }
            } else if ((bArr.length - i) - this.macSize >= size) {
                int processBytes2 = this.ctrEngine.processBytes(this.data.getBuffer(), 0, size, bArr, i);
                i2 = processBytes2 + this.ctrEngine.doFinal(bArr, i + processBytes2);
                calculateMac(bArr, i, size, size2);
            } else {
                throw new OutputLengthException("Output buffer too short");
            }
            byte[] bArr3 = this.macBlock;
            if (bArr3 == null) {
                throw new IllegalStateException("mac is not calculated");
            } else if (this.forEncryption) {
                System.arraycopy(bArr3, 0, bArr, i + i2, this.macSize);
                reset();
                return i2 + this.macSize;
            } else {
                byte[] bArr4 = new byte[this.macSize];
                byte[] buffer = this.data.getBuffer();
                int i4 = this.macSize;
                System.arraycopy(buffer, size - i4, bArr4, 0, i4);
                int i5 = this.macSize;
                byte[] bArr5 = new byte[i5];
                System.arraycopy(this.macBlock, 0, bArr5, 0, i5);
                if (Arrays.constantTimeAreEqual(bArr4, bArr5)) {
                    reset();
                    return i2;
                }
                throw new InvalidCipherTextException("mac verification failed");
            }
        } else {
            throw new InvalidCipherTextException("data too short");
        }
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public String getAlgorithmName() {
        return this.engine.getAlgorithmName() + "/KGCM";
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public byte[] getMac() {
        int i = this.macSize;
        byte[] bArr = new byte[i];
        System.arraycopy(this.macBlock, 0, bArr, 0, i);
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
        return this.engine;
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int getUpdateOutputSize(int i) {
        return 0;
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        KeyParameter keyParameter;
        this.forEncryption = z;
        if (cipherParameters instanceof AEADParameters) {
            AEADParameters aEADParameters = (AEADParameters) cipherParameters;
            byte[] nonce = aEADParameters.getNonce();
            byte[] bArr = this.iv;
            Arrays.fill(bArr, (byte) 0);
            System.arraycopy(nonce, 0, this.iv, bArr.length - nonce.length, nonce.length);
            this.initialAssociatedText = aEADParameters.getAssociatedText();
            int macSize2 = aEADParameters.getMacSize();
            if (macSize2 < 64 || macSize2 > (this.blockSize << 3) || (macSize2 & 7) != 0) {
                throw new IllegalArgumentException("Invalid value for MAC size: " + macSize2);
            }
            this.macSize = macSize2 >>> 3;
            keyParameter = aEADParameters.getKey();
            byte[] bArr2 = this.initialAssociatedText;
            if (bArr2 != null) {
                processAADBytes(bArr2, 0, bArr2.length);
            }
        } else if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            byte[] iv2 = parametersWithIV.getIV();
            byte[] bArr3 = this.iv;
            Arrays.fill(bArr3, (byte) 0);
            System.arraycopy(iv2, 0, this.iv, bArr3.length - iv2.length, iv2.length);
            this.initialAssociatedText = null;
            this.macSize = this.blockSize;
            keyParameter = (KeyParameter) parametersWithIV.getParameters();
        } else {
            throw new IllegalArgumentException("Invalid parameter passed");
        }
        this.macBlock = new byte[this.blockSize];
        this.ctrEngine.init(true, new ParametersWithIV(keyParameter, this.iv));
        this.engine.init(true, keyParameter);
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public void processAADByte(byte b2) {
        this.associatedText.write(b2);
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public void processAADBytes(byte[] bArr, int i, int i2) {
        this.associatedText.write(bArr, i, i2);
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int processByte(byte b2, byte[] bArr, int i) throws DataLengthException, IllegalStateException {
        this.data.write(b2);
        return 0;
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws DataLengthException, IllegalStateException {
        if (bArr.length >= i + i2) {
            this.data.write(bArr, i, i2);
            return 0;
        }
        throw new DataLengthException("input buffer too short");
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public void reset() {
        Arrays.fill(this.b, 0);
        this.engine.reset();
        this.data.reset();
        this.associatedText.reset();
        byte[] bArr = this.initialAssociatedText;
        if (bArr != null) {
            processAADBytes(bArr, 0, bArr.length);
        }
    }
}
