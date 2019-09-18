package org.bouncycastle.crypto.signers;

import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.SignerWithRecovery;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.params.ParametersWithSalt;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.util.Arrays;

public class ISO9796d2PSSSigner implements SignerWithRecovery {
    public static final int TRAILER_IMPLICIT = 188;
    public static final int TRAILER_RIPEMD128 = 13004;
    public static final int TRAILER_RIPEMD160 = 12748;
    public static final int TRAILER_SHA1 = 13260;
    public static final int TRAILER_SHA256 = 13516;
    public static final int TRAILER_SHA384 = 14028;
    public static final int TRAILER_SHA512 = 13772;
    public static final int TRAILER_WHIRLPOOL = 14284;
    private byte[] block;
    private AsymmetricBlockCipher cipher;
    private Digest digest;
    private boolean fullMessage;
    private int hLen;
    private int keyBits;
    private byte[] mBuf;
    private int messageLength;
    private byte[] preBlock;
    private int preMStart;
    private byte[] preSig;
    private int preTLength;
    private SecureRandom random;
    private byte[] recoveredMessage;
    private int saltLength;
    private byte[] standardSalt;
    private int trailer;

    public ISO9796d2PSSSigner(AsymmetricBlockCipher asymmetricBlockCipher, Digest digest2, int i) {
        this(asymmetricBlockCipher, digest2, i, false);
    }

    public ISO9796d2PSSSigner(AsymmetricBlockCipher asymmetricBlockCipher, Digest digest2, int i, boolean z) {
        int intValue;
        this.cipher = asymmetricBlockCipher;
        this.digest = digest2;
        this.hLen = digest2.getDigestSize();
        this.saltLength = i;
        if (z) {
            intValue = 188;
        } else {
            Integer trailer2 = ISOTrailers.getTrailer(digest2);
            if (trailer2 != null) {
                intValue = trailer2.intValue();
            } else {
                throw new IllegalArgumentException("no valid trailer for digest: " + digest2.getAlgorithmName());
            }
        }
        this.trailer = intValue;
    }

    private void ItoOSP(int i, byte[] bArr) {
        bArr[0] = (byte) (i >>> 24);
        bArr[1] = (byte) (i >>> 16);
        bArr[2] = (byte) (i >>> 8);
        bArr[3] = (byte) (i >>> 0);
    }

    private void LtoOSP(long j, byte[] bArr) {
        bArr[0] = (byte) ((int) (j >>> 56));
        bArr[1] = (byte) ((int) (j >>> 48));
        bArr[2] = (byte) ((int) (j >>> 40));
        bArr[3] = (byte) ((int) (j >>> 32));
        bArr[4] = (byte) ((int) (j >>> 24));
        bArr[5] = (byte) ((int) (j >>> 16));
        bArr[6] = (byte) ((int) (j >>> 8));
        bArr[7] = (byte) ((int) (j >>> 0));
    }

    private void clearBlock(byte[] bArr) {
        for (int i = 0; i != bArr.length; i++) {
            bArr[i] = 0;
        }
    }

    private boolean isSameAs(byte[] bArr, byte[] bArr2) {
        boolean z = this.messageLength == bArr2.length;
        for (int i = 0; i != bArr2.length; i++) {
            if (bArr[i] != bArr2[i]) {
                z = false;
            }
        }
        return z;
    }

    private byte[] maskGeneratorFunction1(byte[] bArr, int i, int i2, int i3) {
        byte[] bArr2 = new byte[i3];
        byte[] bArr3 = new byte[this.hLen];
        byte[] bArr4 = new byte[4];
        this.digest.reset();
        int i4 = 0;
        while (i4 < i3 / this.hLen) {
            ItoOSP(i4, bArr4);
            this.digest.update(bArr, i, i2);
            this.digest.update(bArr4, 0, bArr4.length);
            this.digest.doFinal(bArr3, 0);
            System.arraycopy(bArr3, 0, bArr2, this.hLen * i4, this.hLen);
            i4++;
        }
        if (this.hLen * i4 < i3) {
            ItoOSP(i4, bArr4);
            this.digest.update(bArr, i, i2);
            this.digest.update(bArr4, 0, bArr4.length);
            this.digest.doFinal(bArr3, 0);
            System.arraycopy(bArr3, 0, bArr2, this.hLen * i4, bArr2.length - (i4 * this.hLen));
        }
        return bArr2;
    }

    public byte[] generateSignature() throws CryptoException {
        byte[] bArr;
        byte[] bArr2 = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(bArr2, 0);
        byte[] bArr3 = new byte[8];
        LtoOSP((long) (this.messageLength * 8), bArr3);
        this.digest.update(bArr3, 0, bArr3.length);
        this.digest.update(this.mBuf, 0, this.messageLength);
        this.digest.update(bArr2, 0, bArr2.length);
        if (this.standardSalt != null) {
            bArr = this.standardSalt;
        } else {
            bArr = new byte[this.saltLength];
            this.random.nextBytes(bArr);
        }
        this.digest.update(bArr, 0, bArr.length);
        byte[] bArr4 = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(bArr4, 0);
        boolean z = true;
        int i = this.trailer == 188 ? 1 : 2;
        int length = ((((this.block.length - this.messageLength) - bArr.length) - this.hLen) - i) - 1;
        this.block[length] = 1;
        int i2 = length + 1;
        System.arraycopy(this.mBuf, 0, this.block, i2, this.messageLength);
        System.arraycopy(bArr, 0, this.block, i2 + this.messageLength, bArr.length);
        byte[] maskGeneratorFunction1 = maskGeneratorFunction1(bArr4, 0, bArr4.length, (this.block.length - this.hLen) - i);
        for (int i3 = 0; i3 != maskGeneratorFunction1.length; i3++) {
            byte[] bArr5 = this.block;
            bArr5[i3] = (byte) (bArr5[i3] ^ maskGeneratorFunction1[i3]);
        }
        System.arraycopy(bArr4, 0, this.block, (this.block.length - this.hLen) - i, this.hLen);
        if (this.trailer == 188) {
            this.block[this.block.length - 1] = PSSSigner.TRAILER_IMPLICIT;
        } else {
            this.block[this.block.length - 2] = (byte) (this.trailer >>> 8);
            this.block[this.block.length - 1] = (byte) this.trailer;
        }
        byte[] bArr6 = this.block;
        bArr6[0] = (byte) (bArr6[0] & Byte.MAX_VALUE);
        byte[] processBlock = this.cipher.processBlock(this.block, 0, this.block.length);
        this.recoveredMessage = new byte[this.messageLength];
        if (this.messageLength > this.mBuf.length) {
            z = false;
        }
        this.fullMessage = z;
        System.arraycopy(this.mBuf, 0, this.recoveredMessage, 0, this.recoveredMessage.length);
        clearBlock(this.mBuf);
        clearBlock(this.block);
        this.messageLength = 0;
        return processBlock;
    }

    public byte[] getRecoveredMessage() {
        return this.recoveredMessage;
    }

    public boolean hasFullMessage() {
        return this.fullMessage;
    }

    /* JADX WARNING: type inference failed for: r0v2, types: [org.bouncycastle.crypto.CipherParameters] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0066  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x007a  */
    public void init(boolean z, CipherParameters cipherParameters) {
        RSAKeyParameters rSAKeyParameters;
        SecureRandom secureRandom;
        int i = this.saltLength;
        if (cipherParameters instanceof ParametersWithRandom) {
            ParametersWithRandom parametersWithRandom = (ParametersWithRandom) cipherParameters;
            rSAKeyParameters = (RSAKeyParameters) parametersWithRandom.getParameters();
            if (z) {
                secureRandom = parametersWithRandom.getRandom();
            }
            this.cipher.init(z, rSAKeyParameters);
            this.keyBits = rSAKeyParameters.getModulus().bitLength();
            this.block = new byte[((this.keyBits + 7) / 8)];
            this.mBuf = new byte[(this.trailer == 188 ? (((this.block.length - this.digest.getDigestSize()) - i) - 1) - 1 : (((this.block.length - this.digest.getDigestSize()) - i) - 1) - 2)];
            reset();
        }
        if (cipherParameters instanceof ParametersWithSalt) {
            ParametersWithSalt parametersWithSalt = (ParametersWithSalt) cipherParameters;
            rSAKeyParameters = parametersWithSalt.getParameters();
            this.standardSalt = parametersWithSalt.getSalt();
            i = this.standardSalt.length;
            if (this.standardSalt.length != this.saltLength) {
                throw new IllegalArgumentException("Fixed salt is of wrong length");
            }
        } else {
            rSAKeyParameters = (RSAKeyParameters) cipherParameters;
            if (z) {
                secureRandom = CryptoServicesRegistrar.getSecureRandom();
            }
        }
        this.cipher.init(z, rSAKeyParameters);
        this.keyBits = rSAKeyParameters.getModulus().bitLength();
        this.block = new byte[((this.keyBits + 7) / 8)];
        this.mBuf = new byte[(this.trailer == 188 ? (((this.block.length - this.digest.getDigestSize()) - i) - 1) - 1 : (((this.block.length - this.digest.getDigestSize()) - i) - 1) - 2)];
        reset();
        this.random = secureRandom;
        this.cipher.init(z, rSAKeyParameters);
        this.keyBits = rSAKeyParameters.getModulus().bitLength();
        this.block = new byte[((this.keyBits + 7) / 8)];
        this.mBuf = new byte[(this.trailer == 188 ? (((this.block.length - this.digest.getDigestSize()) - i) - 1) - 1 : (((this.block.length - this.digest.getDigestSize()) - i) - 1) - 2)];
        reset();
    }

    public void reset() {
        this.digest.reset();
        this.messageLength = 0;
        if (this.mBuf != null) {
            clearBlock(this.mBuf);
        }
        if (this.recoveredMessage != null) {
            clearBlock(this.recoveredMessage);
            this.recoveredMessage = null;
        }
        this.fullMessage = false;
        if (this.preSig != null) {
            this.preSig = null;
            clearBlock(this.preBlock);
            this.preBlock = null;
        }
    }

    public void update(byte b) {
        if (this.preSig != null || this.messageLength >= this.mBuf.length) {
            this.digest.update(b);
            return;
        }
        byte[] bArr = this.mBuf;
        int i = this.messageLength;
        this.messageLength = i + 1;
        bArr[i] = b;
    }

    public void update(byte[] bArr, int i, int i2) {
        if (this.preSig == null) {
            while (i2 > 0 && this.messageLength < this.mBuf.length) {
                update(bArr[i]);
                i++;
                i2--;
            }
        }
        if (i2 > 0) {
            this.digest.update(bArr, i, i2);
        }
    }

    public void updateWithRecoveredMessage(byte[] bArr) throws InvalidCipherTextException {
        byte[] processBlock = this.cipher.processBlock(bArr, 0, bArr.length);
        if (processBlock.length < (this.keyBits + 7) / 8) {
            byte[] bArr2 = new byte[((this.keyBits + 7) / 8)];
            System.arraycopy(processBlock, 0, bArr2, bArr2.length - processBlock.length, processBlock.length);
            clearBlock(processBlock);
            processBlock = bArr2;
        }
        boolean z = true;
        int i = 2;
        if (((processBlock[processBlock.length - 1] & 255) ^ PSSSigner.TRAILER_IMPLICIT) == 0) {
            i = 1;
        } else {
            byte b = ((processBlock[processBlock.length - 2] & 255) << 8) | (processBlock[processBlock.length - 1] & 255);
            Integer trailer2 = ISOTrailers.getTrailer(this.digest);
            if (trailer2 != null) {
                int intValue = trailer2.intValue();
                if (!(b == intValue || (intValue == 15052 && b == 16588))) {
                    throw new IllegalStateException("signer initialised with wrong digest for trailer " + b);
                }
            } else {
                throw new IllegalArgumentException("unrecognised hash in signature");
            }
        }
        this.digest.doFinal(new byte[this.hLen], 0);
        byte[] maskGeneratorFunction1 = maskGeneratorFunction1(processBlock, (processBlock.length - this.hLen) - i, this.hLen, (processBlock.length - this.hLen) - i);
        for (int i2 = 0; i2 != maskGeneratorFunction1.length; i2++) {
            processBlock[i2] = (byte) (processBlock[i2] ^ maskGeneratorFunction1[i2]);
        }
        processBlock[0] = (byte) (processBlock[0] & Byte.MAX_VALUE);
        int i3 = 0;
        while (i3 != processBlock.length && processBlock[i3] != 1) {
            i3++;
        }
        int i4 = i3 + 1;
        if (i4 >= processBlock.length) {
            clearBlock(processBlock);
        }
        if (i4 <= 1) {
            z = false;
        }
        this.fullMessage = z;
        this.recoveredMessage = new byte[((maskGeneratorFunction1.length - i4) - this.saltLength)];
        System.arraycopy(processBlock, i4, this.recoveredMessage, 0, this.recoveredMessage.length);
        System.arraycopy(this.recoveredMessage, 0, this.mBuf, 0, this.recoveredMessage.length);
        this.preSig = bArr;
        this.preBlock = processBlock;
        this.preMStart = i4;
        this.preTLength = i;
    }

    public boolean verifySignature(byte[] bArr) {
        byte[] bArr2;
        byte[] bArr3 = new byte[this.hLen];
        this.digest.doFinal(bArr3, 0);
        if (this.preSig == null) {
            try {
                updateWithRecoveredMessage(bArr);
            } catch (Exception e) {
                return false;
            }
        } else if (!Arrays.areEqual(this.preSig, bArr)) {
            throw new IllegalStateException("updateWithRecoveredMessage called on different signature");
        }
        byte[] bArr4 = this.preBlock;
        int i = this.preMStart;
        int i2 = this.preTLength;
        this.preSig = null;
        this.preBlock = null;
        byte[] bArr5 = new byte[8];
        LtoOSP((long) (this.recoveredMessage.length * 8), bArr5);
        this.digest.update(bArr5, 0, bArr5.length);
        if (this.recoveredMessage.length != 0) {
            this.digest.update(this.recoveredMessage, 0, this.recoveredMessage.length);
        }
        this.digest.update(bArr3, 0, bArr3.length);
        if (this.standardSalt != null) {
            this.digest.update(this.standardSalt, 0, this.standardSalt.length);
        } else {
            this.digest.update(bArr4, i + this.recoveredMessage.length, this.saltLength);
        }
        byte[] bArr6 = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(bArr6, 0);
        int length = (bArr4.length - i2) - bArr6.length;
        boolean z = true;
        for (int i3 = 0; i3 != bArr6.length; i3++) {
            if (bArr6[i3] != bArr4[length + i3]) {
                z = false;
            }
        }
        clearBlock(bArr4);
        clearBlock(bArr6);
        if (!z) {
            this.fullMessage = false;
            this.messageLength = 0;
            bArr2 = this.recoveredMessage;
        } else if (this.messageLength == 0 || isSameAs(this.mBuf, this.recoveredMessage)) {
            this.messageLength = 0;
            clearBlock(this.mBuf);
            return true;
        } else {
            this.messageLength = 0;
            bArr2 = this.mBuf;
        }
        clearBlock(bArr2);
        return false;
    }
}
