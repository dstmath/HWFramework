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
        int i4;
        byte[] bArr2 = new byte[i3];
        byte[] bArr3 = new byte[this.hLen];
        byte[] bArr4 = new byte[4];
        this.digest.reset();
        int i5 = 0;
        while (true) {
            i4 = this.hLen;
            if (i5 >= i3 / i4) {
                break;
            }
            ItoOSP(i5, bArr4);
            this.digest.update(bArr, i, i2);
            this.digest.update(bArr4, 0, bArr4.length);
            this.digest.doFinal(bArr3, 0);
            int i6 = this.hLen;
            System.arraycopy(bArr3, 0, bArr2, i5 * i6, i6);
            i5++;
        }
        if (i4 * i5 < i3) {
            ItoOSP(i5, bArr4);
            this.digest.update(bArr, i, i2);
            this.digest.update(bArr4, 0, bArr4.length);
            this.digest.doFinal(bArr3, 0);
            int i7 = this.hLen;
            System.arraycopy(bArr3, 0, bArr2, i5 * i7, bArr2.length - (i5 * i7));
        }
        return bArr2;
    }

    @Override // org.bouncycastle.crypto.Signer
    public byte[] generateSignature() throws CryptoException {
        byte[] bArr = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(bArr, 0);
        byte[] bArr2 = new byte[8];
        LtoOSP((long) (this.messageLength * 8), bArr2);
        this.digest.update(bArr2, 0, bArr2.length);
        this.digest.update(this.mBuf, 0, this.messageLength);
        this.digest.update(bArr, 0, bArr.length);
        byte[] bArr3 = this.standardSalt;
        if (bArr3 == null) {
            bArr3 = new byte[this.saltLength];
            this.random.nextBytes(bArr3);
        }
        this.digest.update(bArr3, 0, bArr3.length);
        byte[] bArr4 = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(bArr4, 0);
        boolean z = true;
        int i = this.trailer == 188 ? 1 : 2;
        byte[] bArr5 = this.block;
        int length = bArr5.length;
        int i2 = this.messageLength;
        int length2 = ((((length - i2) - bArr3.length) - this.hLen) - i) - 1;
        bArr5[length2] = 1;
        int i3 = length2 + 1;
        System.arraycopy(this.mBuf, 0, bArr5, i3, i2);
        System.arraycopy(bArr3, 0, this.block, i3 + this.messageLength, bArr3.length);
        byte[] maskGeneratorFunction1 = maskGeneratorFunction1(bArr4, 0, bArr4.length, (this.block.length - this.hLen) - i);
        for (int i4 = 0; i4 != maskGeneratorFunction1.length; i4++) {
            byte[] bArr6 = this.block;
            bArr6[i4] = (byte) (bArr6[i4] ^ maskGeneratorFunction1[i4]);
        }
        byte[] bArr7 = this.block;
        int length3 = bArr7.length;
        int i5 = this.hLen;
        System.arraycopy(bArr4, 0, bArr7, (length3 - i5) - i, i5);
        int i6 = this.trailer;
        if (i6 == 188) {
            byte[] bArr8 = this.block;
            bArr8[bArr8.length - 1] = PSSSigner.TRAILER_IMPLICIT;
        } else {
            byte[] bArr9 = this.block;
            bArr9[bArr9.length - 2] = (byte) (i6 >>> 8);
            bArr9[bArr9.length - 1] = (byte) i6;
        }
        byte[] bArr10 = this.block;
        bArr10[0] = (byte) (bArr10[0] & Byte.MAX_VALUE);
        byte[] processBlock = this.cipher.processBlock(bArr10, 0, bArr10.length);
        int i7 = this.messageLength;
        this.recoveredMessage = new byte[i7];
        if (i7 > this.mBuf.length) {
            z = false;
        }
        this.fullMessage = z;
        byte[] bArr11 = this.mBuf;
        byte[] bArr12 = this.recoveredMessage;
        System.arraycopy(bArr11, 0, bArr12, 0, bArr12.length);
        clearBlock(this.mBuf);
        clearBlock(this.block);
        this.messageLength = 0;
        return processBlock;
    }

    @Override // org.bouncycastle.crypto.SignerWithRecovery
    public byte[] getRecoveredMessage() {
        return this.recoveredMessage;
    }

    @Override // org.bouncycastle.crypto.SignerWithRecovery
    public boolean hasFullMessage() {
        return this.fullMessage;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0064  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0074  */
    @Override // org.bouncycastle.crypto.Signer
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
            rSAKeyParameters = (RSAKeyParameters) parametersWithSalt.getParameters();
            this.standardSalt = parametersWithSalt.getSalt();
            byte[] bArr = this.standardSalt;
            i = bArr.length;
            if (bArr.length != this.saltLength) {
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

    @Override // org.bouncycastle.crypto.Signer
    public void reset() {
        this.digest.reset();
        this.messageLength = 0;
        byte[] bArr = this.mBuf;
        if (bArr != null) {
            clearBlock(bArr);
        }
        byte[] bArr2 = this.recoveredMessage;
        if (bArr2 != null) {
            clearBlock(bArr2);
            this.recoveredMessage = null;
        }
        this.fullMessage = false;
        if (this.preSig != null) {
            this.preSig = null;
            clearBlock(this.preBlock);
            this.preBlock = null;
        }
    }

    @Override // org.bouncycastle.crypto.Signer
    public void update(byte b) {
        if (this.preSig == null) {
            int i = this.messageLength;
            byte[] bArr = this.mBuf;
            if (i < bArr.length) {
                this.messageLength = i + 1;
                bArr[i] = b;
                return;
            }
        }
        this.digest.update(b);
    }

    @Override // org.bouncycastle.crypto.Signer
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

    @Override // org.bouncycastle.crypto.SignerWithRecovery
    public void updateWithRecoveredMessage(byte[] bArr) throws InvalidCipherTextException {
        byte[] processBlock = this.cipher.processBlock(bArr, 0, bArr.length);
        int length = processBlock.length;
        int i = this.keyBits;
        if (length < (i + 7) / 8) {
            byte[] bArr2 = new byte[((i + 7) / 8)];
            System.arraycopy(processBlock, 0, bArr2, bArr2.length - processBlock.length, processBlock.length);
            clearBlock(processBlock);
            processBlock = bArr2;
        }
        boolean z = true;
        int i2 = 2;
        if (((processBlock[processBlock.length - 1] & 255) ^ 188) == 0) {
            i2 = 1;
        } else {
            int i3 = ((processBlock[processBlock.length - 2] & 255) << 8) | (processBlock[processBlock.length - 1] & 255);
            Integer trailer2 = ISOTrailers.getTrailer(this.digest);
            if (trailer2 != null) {
                int intValue = trailer2.intValue();
                if (!(i3 == intValue || (intValue == 15052 && i3 == 16588))) {
                    throw new IllegalStateException("signer initialised with wrong digest for trailer " + i3);
                }
            } else {
                throw new IllegalArgumentException("unrecognised hash in signature");
            }
        }
        this.digest.doFinal(new byte[this.hLen], 0);
        int length2 = processBlock.length;
        int i4 = this.hLen;
        byte[] maskGeneratorFunction1 = maskGeneratorFunction1(processBlock, (length2 - i4) - i2, i4, (processBlock.length - i4) - i2);
        for (int i5 = 0; i5 != maskGeneratorFunction1.length; i5++) {
            processBlock[i5] = (byte) (processBlock[i5] ^ maskGeneratorFunction1[i5]);
        }
        processBlock[0] = (byte) (processBlock[0] & Byte.MAX_VALUE);
        int i6 = 0;
        while (i6 != processBlock.length && processBlock[i6] != 1) {
            i6++;
        }
        int i7 = i6 + 1;
        if (i7 >= processBlock.length) {
            clearBlock(processBlock);
        }
        if (i7 <= 1) {
            z = false;
        }
        this.fullMessage = z;
        this.recoveredMessage = new byte[((maskGeneratorFunction1.length - i7) - this.saltLength)];
        byte[] bArr3 = this.recoveredMessage;
        System.arraycopy(processBlock, i7, bArr3, 0, bArr3.length);
        byte[] bArr4 = this.recoveredMessage;
        System.arraycopy(bArr4, 0, this.mBuf, 0, bArr4.length);
        this.preSig = bArr;
        this.preBlock = processBlock;
        this.preMStart = i7;
        this.preTLength = i2;
    }

    @Override // org.bouncycastle.crypto.Signer
    public boolean verifySignature(byte[] bArr) {
        byte[] bArr2;
        byte[] bArr3 = new byte[this.hLen];
        this.digest.doFinal(bArr3, 0);
        byte[] bArr4 = this.preSig;
        if (bArr4 == null) {
            try {
                updateWithRecoveredMessage(bArr);
            } catch (Exception e) {
                return false;
            }
        } else if (!Arrays.areEqual(bArr4, bArr)) {
            throw new IllegalStateException("updateWithRecoveredMessage called on different signature");
        }
        byte[] bArr5 = this.preBlock;
        int i = this.preMStart;
        int i2 = this.preTLength;
        this.preSig = null;
        this.preBlock = null;
        byte[] bArr6 = new byte[8];
        LtoOSP((long) (this.recoveredMessage.length * 8), bArr6);
        this.digest.update(bArr6, 0, bArr6.length);
        byte[] bArr7 = this.recoveredMessage;
        if (bArr7.length != 0) {
            this.digest.update(bArr7, 0, bArr7.length);
        }
        this.digest.update(bArr3, 0, bArr3.length);
        byte[] bArr8 = this.standardSalt;
        if (bArr8 != null) {
            this.digest.update(bArr8, 0, bArr8.length);
        } else {
            this.digest.update(bArr5, i + this.recoveredMessage.length, this.saltLength);
        }
        byte[] bArr9 = new byte[this.digest.getDigestSize()];
        this.digest.doFinal(bArr9, 0);
        int length = (bArr5.length - i2) - bArr9.length;
        boolean z = true;
        for (int i3 = 0; i3 != bArr9.length; i3++) {
            if (bArr9[i3] != bArr5[length + i3]) {
                z = false;
            }
        }
        clearBlock(bArr5);
        clearBlock(bArr9);
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
