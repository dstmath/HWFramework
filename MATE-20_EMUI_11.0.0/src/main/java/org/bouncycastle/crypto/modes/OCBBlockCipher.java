package org.bouncycastle.crypto.modes;

import java.util.Vector;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

public class OCBBlockCipher implements AEADBlockCipher {
    private static final int BLOCK_SIZE = 16;
    private byte[] Checksum;
    private byte[] KtopInput = null;
    private Vector L;
    private byte[] L_Asterisk;
    private byte[] L_Dollar;
    private byte[] OffsetHASH;
    private byte[] OffsetMAIN = new byte[16];
    private byte[] OffsetMAIN_0 = new byte[16];
    private byte[] Stretch = new byte[24];
    private byte[] Sum;
    private boolean forEncryption;
    private byte[] hashBlock;
    private long hashBlockCount;
    private int hashBlockPos;
    private BlockCipher hashCipher;
    private byte[] initialAssociatedText;
    private byte[] macBlock;
    private int macSize;
    private byte[] mainBlock;
    private long mainBlockCount;
    private int mainBlockPos;
    private BlockCipher mainCipher;

    public OCBBlockCipher(BlockCipher blockCipher, BlockCipher blockCipher2) {
        if (blockCipher == null) {
            throw new IllegalArgumentException("'hashCipher' cannot be null");
        } else if (blockCipher.getBlockSize() != 16) {
            throw new IllegalArgumentException("'hashCipher' must have a block size of 16");
        } else if (blockCipher2 == null) {
            throw new IllegalArgumentException("'mainCipher' cannot be null");
        } else if (blockCipher2.getBlockSize() != 16) {
            throw new IllegalArgumentException("'mainCipher' must have a block size of 16");
        } else if (blockCipher.getAlgorithmName().equals(blockCipher2.getAlgorithmName())) {
            this.hashCipher = blockCipher;
            this.mainCipher = blockCipher2;
        } else {
            throw new IllegalArgumentException("'hashCipher' and 'mainCipher' must be the same algorithm");
        }
    }

    protected static byte[] OCB_double(byte[] bArr) {
        byte[] bArr2 = new byte[16];
        bArr2[15] = (byte) ((135 >>> ((1 - shiftLeft(bArr, bArr2)) << 3)) ^ bArr2[15]);
        return bArr2;
    }

    protected static void OCB_extend(byte[] bArr, int i) {
        bArr[i] = Byte.MIN_VALUE;
        while (true) {
            i++;
            if (i < 16) {
                bArr[i] = 0;
            } else {
                return;
            }
        }
    }

    protected static int OCB_ntz(long j) {
        if (j == 0) {
            return 64;
        }
        int i = 0;
        while ((1 & j) == 0) {
            i++;
            j >>>= 1;
        }
        return i;
    }

    protected static int shiftLeft(byte[] bArr, byte[] bArr2) {
        int i = 16;
        int i2 = 0;
        while (true) {
            i--;
            if (i < 0) {
                return i2;
            }
            int i3 = bArr[i] & 255;
            bArr2[i] = (byte) (i2 | (i3 << 1));
            i2 = (i3 >>> 7) & 1;
        }
    }

    protected static void xor(byte[] bArr, byte[] bArr2) {
        for (int i = 15; i >= 0; i--) {
            bArr[i] = (byte) (bArr[i] ^ bArr2[i]);
        }
    }

    /* access modifiers changed from: protected */
    public void clear(byte[] bArr) {
        if (bArr != null) {
            Arrays.fill(bArr, (byte) 0);
        }
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int doFinal(byte[] bArr, int i) throws IllegalStateException, InvalidCipherTextException {
        byte[] bArr2;
        if (!this.forEncryption) {
            int i2 = this.mainBlockPos;
            int i3 = this.macSize;
            if (i2 >= i3) {
                this.mainBlockPos = i2 - i3;
                bArr2 = new byte[i3];
                System.arraycopy(this.mainBlock, this.mainBlockPos, bArr2, 0, i3);
            } else {
                throw new InvalidCipherTextException("data too short");
            }
        } else {
            bArr2 = null;
        }
        int i4 = this.hashBlockPos;
        if (i4 > 0) {
            OCB_extend(this.hashBlock, i4);
            updateHASH(this.L_Asterisk);
        }
        int i5 = this.mainBlockPos;
        if (i5 > 0) {
            if (this.forEncryption) {
                OCB_extend(this.mainBlock, i5);
                xor(this.Checksum, this.mainBlock);
            }
            xor(this.OffsetMAIN, this.L_Asterisk);
            byte[] bArr3 = new byte[16];
            this.hashCipher.processBlock(this.OffsetMAIN, 0, bArr3, 0);
            xor(this.mainBlock, bArr3);
            int length = bArr.length;
            int i6 = this.mainBlockPos;
            if (length >= i + i6) {
                System.arraycopy(this.mainBlock, 0, bArr, i, i6);
                if (!this.forEncryption) {
                    OCB_extend(this.mainBlock, this.mainBlockPos);
                    xor(this.Checksum, this.mainBlock);
                }
            } else {
                throw new OutputLengthException("Output buffer too short");
            }
        }
        xor(this.Checksum, this.OffsetMAIN);
        xor(this.Checksum, this.L_Dollar);
        BlockCipher blockCipher = this.hashCipher;
        byte[] bArr4 = this.Checksum;
        blockCipher.processBlock(bArr4, 0, bArr4, 0);
        xor(this.Checksum, this.Sum);
        int i7 = this.macSize;
        this.macBlock = new byte[i7];
        System.arraycopy(this.Checksum, 0, this.macBlock, 0, i7);
        int i8 = this.mainBlockPos;
        if (this.forEncryption) {
            int length2 = bArr.length;
            int i9 = i + i8;
            int i10 = this.macSize;
            if (length2 >= i9 + i10) {
                System.arraycopy(this.macBlock, 0, bArr, i9, i10);
                i8 += this.macSize;
            } else {
                throw new OutputLengthException("Output buffer too short");
            }
        } else if (!Arrays.constantTimeAreEqual(this.macBlock, bArr2)) {
            throw new InvalidCipherTextException("mac check in OCB failed");
        }
        reset(false);
        return i8;
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public String getAlgorithmName() {
        return this.mainCipher.getAlgorithmName() + "/OCB";
    }

    /* access modifiers changed from: protected */
    public byte[] getLSub(int i) {
        while (i >= this.L.size()) {
            Vector vector = this.L;
            vector.addElement(OCB_double((byte[]) vector.lastElement()));
        }
        return (byte[]) this.L.elementAt(i);
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public byte[] getMac() {
        byte[] bArr = this.macBlock;
        return bArr == null ? new byte[this.macSize] : Arrays.clone(bArr);
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int getOutputSize(int i) {
        int i2 = i + this.mainBlockPos;
        if (this.forEncryption) {
            return i2 + this.macSize;
        }
        int i3 = this.macSize;
        if (i2 < i3) {
            return 0;
        }
        return i2 - i3;
    }

    @Override // org.bouncycastle.crypto.modes.AEADBlockCipher
    public BlockCipher getUnderlyingCipher() {
        return this.mainCipher;
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int getUpdateOutputSize(int i) {
        int i2 = i + this.mainBlockPos;
        if (!this.forEncryption) {
            int i3 = this.macSize;
            if (i2 < i3) {
                return 0;
            }
            i2 -= i3;
        }
        return i2 - (i2 % 16);
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        KeyParameter keyParameter;
        byte[] bArr;
        boolean z2 = this.forEncryption;
        this.forEncryption = z;
        this.macBlock = null;
        if (cipherParameters instanceof AEADParameters) {
            AEADParameters aEADParameters = (AEADParameters) cipherParameters;
            bArr = aEADParameters.getNonce();
            this.initialAssociatedText = aEADParameters.getAssociatedText();
            int macSize2 = aEADParameters.getMacSize();
            if (macSize2 < 64 || macSize2 > 128 || macSize2 % 8 != 0) {
                throw new IllegalArgumentException("Invalid value for MAC size: " + macSize2);
            }
            this.macSize = macSize2 / 8;
            keyParameter = aEADParameters.getKey();
        } else if (cipherParameters instanceof ParametersWithIV) {
            ParametersWithIV parametersWithIV = (ParametersWithIV) cipherParameters;
            bArr = parametersWithIV.getIV();
            this.initialAssociatedText = null;
            this.macSize = 16;
            keyParameter = (KeyParameter) parametersWithIV.getParameters();
        } else {
            throw new IllegalArgumentException("invalid parameters passed to OCB");
        }
        this.hashBlock = new byte[16];
        this.mainBlock = new byte[(z ? 16 : this.macSize + 16)];
        if (bArr == null) {
            bArr = new byte[0];
        }
        if (bArr.length <= 15) {
            if (keyParameter != null) {
                this.hashCipher.init(true, keyParameter);
                this.mainCipher.init(z, keyParameter);
                this.KtopInput = null;
            } else if (z2 != z) {
                throw new IllegalArgumentException("cannot change encrypting state without providing key.");
            }
            this.L_Asterisk = new byte[16];
            BlockCipher blockCipher = this.hashCipher;
            byte[] bArr2 = this.L_Asterisk;
            blockCipher.processBlock(bArr2, 0, bArr2, 0);
            this.L_Dollar = OCB_double(this.L_Asterisk);
            this.L = new Vector();
            this.L.addElement(OCB_double(this.L_Dollar));
            int processNonce = processNonce(bArr);
            int i = processNonce % 8;
            int i2 = processNonce / 8;
            if (i == 0) {
                System.arraycopy(this.Stretch, i2, this.OffsetMAIN_0, 0, 16);
            } else {
                int i3 = i2;
                for (int i4 = 0; i4 < 16; i4++) {
                    byte[] bArr3 = this.Stretch;
                    i3++;
                    this.OffsetMAIN_0[i4] = (byte) (((bArr3[i3] & 255) >>> (8 - i)) | ((bArr3[i3] & 255) << i));
                }
            }
            this.hashBlockPos = 0;
            this.mainBlockPos = 0;
            this.hashBlockCount = 0;
            this.mainBlockCount = 0;
            this.OffsetHASH = new byte[16];
            this.Sum = new byte[16];
            System.arraycopy(this.OffsetMAIN_0, 0, this.OffsetMAIN, 0, 16);
            this.Checksum = new byte[16];
            byte[] bArr4 = this.initialAssociatedText;
            if (bArr4 != null) {
                processAADBytes(bArr4, 0, bArr4.length);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("IV must be no more than 15 bytes");
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public void processAADByte(byte b) {
        byte[] bArr = this.hashBlock;
        int i = this.hashBlockPos;
        bArr[i] = b;
        int i2 = i + 1;
        this.hashBlockPos = i2;
        if (i2 == bArr.length) {
            processHashBlock();
        }
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public void processAADBytes(byte[] bArr, int i, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            byte[] bArr2 = this.hashBlock;
            int i4 = this.hashBlockPos;
            bArr2[i4] = bArr[i + i3];
            int i5 = i4 + 1;
            this.hashBlockPos = i5;
            if (i5 == bArr2.length) {
                processHashBlock();
            }
        }
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int processByte(byte b, byte[] bArr, int i) throws DataLengthException {
        byte[] bArr2 = this.mainBlock;
        int i2 = this.mainBlockPos;
        bArr2[i2] = b;
        int i3 = i2 + 1;
        this.mainBlockPos = i3;
        if (i3 != bArr2.length) {
            return 0;
        }
        processMainBlock(bArr, i);
        return 16;
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public int processBytes(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws DataLengthException {
        if (bArr.length >= i + i2) {
            int i4 = 0;
            for (int i5 = 0; i5 < i2; i5++) {
                byte[] bArr3 = this.mainBlock;
                int i6 = this.mainBlockPos;
                bArr3[i6] = bArr[i + i5];
                int i7 = i6 + 1;
                this.mainBlockPos = i7;
                if (i7 == bArr3.length) {
                    processMainBlock(bArr2, i3 + i4);
                    i4 += 16;
                }
            }
            return i4;
        }
        throw new DataLengthException("Input buffer too short");
    }

    /* access modifiers changed from: protected */
    public void processHashBlock() {
        long j = this.hashBlockCount + 1;
        this.hashBlockCount = j;
        updateHASH(getLSub(OCB_ntz(j)));
        this.hashBlockPos = 0;
    }

    /* access modifiers changed from: protected */
    public void processMainBlock(byte[] bArr, int i) {
        if (bArr.length >= i + 16) {
            if (this.forEncryption) {
                xor(this.Checksum, this.mainBlock);
                this.mainBlockPos = 0;
            }
            byte[] bArr2 = this.OffsetMAIN;
            long j = this.mainBlockCount + 1;
            this.mainBlockCount = j;
            xor(bArr2, getLSub(OCB_ntz(j)));
            xor(this.mainBlock, this.OffsetMAIN);
            BlockCipher blockCipher = this.mainCipher;
            byte[] bArr3 = this.mainBlock;
            blockCipher.processBlock(bArr3, 0, bArr3, 0);
            xor(this.mainBlock, this.OffsetMAIN);
            System.arraycopy(this.mainBlock, 0, bArr, i, 16);
            if (!this.forEncryption) {
                xor(this.Checksum, this.mainBlock);
                byte[] bArr4 = this.mainBlock;
                System.arraycopy(bArr4, 16, bArr4, 0, this.macSize);
                this.mainBlockPos = this.macSize;
                return;
            }
            return;
        }
        throw new OutputLengthException("Output buffer too short");
    }

    /* access modifiers changed from: protected */
    public int processNonce(byte[] bArr) {
        byte[] bArr2 = new byte[16];
        int i = 0;
        System.arraycopy(bArr, 0, bArr2, bArr2.length - bArr.length, bArr.length);
        bArr2[0] = (byte) (this.macSize << 4);
        int length = 15 - bArr.length;
        bArr2[length] = (byte) (bArr2[length] | 1);
        int i2 = bArr2[15] & 63;
        bArr2[15] = (byte) (bArr2[15] & 192);
        byte[] bArr3 = this.KtopInput;
        if (bArr3 == null || !Arrays.areEqual(bArr2, bArr3)) {
            byte[] bArr4 = new byte[16];
            this.KtopInput = bArr2;
            this.hashCipher.processBlock(this.KtopInput, 0, bArr4, 0);
            System.arraycopy(bArr4, 0, this.Stretch, 0, 16);
            while (i < 8) {
                byte[] bArr5 = this.Stretch;
                int i3 = i + 16;
                byte b = bArr4[i];
                i++;
                bArr5[i3] = (byte) (b ^ bArr4[i]);
            }
        }
        return i2;
    }

    @Override // org.bouncycastle.crypto.modes.AEADCipher
    public void reset() {
        reset(true);
    }

    /* access modifiers changed from: protected */
    public void reset(boolean z) {
        this.hashCipher.reset();
        this.mainCipher.reset();
        clear(this.hashBlock);
        clear(this.mainBlock);
        this.hashBlockPos = 0;
        this.mainBlockPos = 0;
        this.hashBlockCount = 0;
        this.mainBlockCount = 0;
        clear(this.OffsetHASH);
        clear(this.Sum);
        System.arraycopy(this.OffsetMAIN_0, 0, this.OffsetMAIN, 0, 16);
        clear(this.Checksum);
        if (z) {
            this.macBlock = null;
        }
        byte[] bArr = this.initialAssociatedText;
        if (bArr != null) {
            processAADBytes(bArr, 0, bArr.length);
        }
    }

    /* access modifiers changed from: protected */
    public void updateHASH(byte[] bArr) {
        xor(this.OffsetHASH, bArr);
        xor(this.hashBlock, this.OffsetHASH);
        BlockCipher blockCipher = this.hashCipher;
        byte[] bArr2 = this.hashBlock;
        blockCipher.processBlock(bArr2, 0, bArr2, 0);
        xor(this.Sum, this.hashBlock);
    }
}
