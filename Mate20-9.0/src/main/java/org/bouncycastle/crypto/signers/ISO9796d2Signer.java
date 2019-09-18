package org.bouncycastle.crypto.signers;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.SignerWithRecovery;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.util.Arrays;

public class ISO9796d2Signer implements SignerWithRecovery {
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
    private int keyBits;
    private byte[] mBuf;
    private int messageLength;
    private byte[] preBlock;
    private byte[] preSig;
    private byte[] recoveredMessage;
    private int trailer;

    public ISO9796d2Signer(AsymmetricBlockCipher asymmetricBlockCipher, Digest digest2) {
        this(asymmetricBlockCipher, digest2, false);
    }

    public ISO9796d2Signer(AsymmetricBlockCipher asymmetricBlockCipher, Digest digest2, boolean z) {
        int intValue;
        this.cipher = asymmetricBlockCipher;
        this.digest = digest2;
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

    private void clearBlock(byte[] bArr) {
        for (int i = 0; i != bArr.length; i++) {
            bArr[i] = 0;
        }
    }

    private boolean isSameAs(byte[] bArr, byte[] bArr2) {
        boolean z = true;
        if (this.messageLength > this.mBuf.length) {
            if (this.mBuf.length > bArr2.length) {
                z = false;
            }
            for (int i = 0; i != this.mBuf.length; i++) {
                if (bArr[i] != bArr2[i]) {
                    z = false;
                }
            }
        } else {
            if (this.messageLength != bArr2.length) {
                z = false;
            }
            for (int i2 = 0; i2 != bArr2.length; i2++) {
                if (bArr[i2] != bArr2[i2]) {
                    z = false;
                }
            }
        }
        return z;
    }

    private boolean returnFalse(byte[] bArr) {
        this.messageLength = 0;
        clearBlock(this.mBuf);
        clearBlock(bArr);
        return false;
    }

    public byte[] generateSignature() throws CryptoException {
        int i;
        int i2;
        byte b;
        int i3;
        byte[] bArr;
        int digestSize = this.digest.getDigestSize();
        boolean z = true;
        if (this.trailer == 188) {
            int length = (this.block.length - digestSize) - 1;
            this.digest.doFinal(this.block, length);
            this.block[this.block.length - 1] = PSSSigner.TRAILER_IMPLICIT;
            i = length;
            i2 = 8;
        } else {
            i2 = 16;
            i = (this.block.length - digestSize) - 2;
            this.digest.doFinal(this.block, i);
            this.block[this.block.length - 2] = (byte) (this.trailer >>> 8);
            this.block[this.block.length - 1] = (byte) this.trailer;
        }
        int i4 = ((((digestSize + this.messageLength) * 8) + i2) + 4) - this.keyBits;
        if (i4 > 0) {
            int i5 = this.messageLength - ((i4 + 7) / 8);
            b = 96;
            i3 = i - i5;
            System.arraycopy(this.mBuf, 0, this.block, i3, i5);
            bArr = new byte[i5];
        } else {
            b = 64;
            i3 = i - this.messageLength;
            System.arraycopy(this.mBuf, 0, this.block, i3, this.messageLength);
            bArr = new byte[this.messageLength];
        }
        this.recoveredMessage = bArr;
        int i6 = i3 - 1;
        if (i6 > 0) {
            for (int i7 = i6; i7 != 0; i7--) {
                this.block[i7] = -69;
            }
            byte[] bArr2 = this.block;
            bArr2[i6] = (byte) (bArr2[i6] ^ 1);
            this.block[0] = 11;
            byte[] bArr3 = this.block;
            bArr3[0] = (byte) (bArr3[0] | b);
        } else {
            this.block[0] = 10;
            byte[] bArr4 = this.block;
            bArr4[0] = (byte) (bArr4[0] | b);
        }
        byte[] processBlock = this.cipher.processBlock(this.block, 0, this.block.length);
        if ((b & 32) != 0) {
            z = false;
        }
        this.fullMessage = z;
        System.arraycopy(this.mBuf, 0, this.recoveredMessage, 0, this.recoveredMessage.length);
        this.messageLength = 0;
        clearBlock(this.mBuf);
        clearBlock(this.block);
        return processBlock;
    }

    public byte[] getRecoveredMessage() {
        return this.recoveredMessage;
    }

    public boolean hasFullMessage() {
        return this.fullMessage;
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        RSAKeyParameters rSAKeyParameters = (RSAKeyParameters) cipherParameters;
        this.cipher.init(z, rSAKeyParameters);
        this.keyBits = rSAKeyParameters.getModulus().bitLength();
        this.block = new byte[((this.keyBits + 7) / 8)];
        this.mBuf = new byte[(this.trailer == 188 ? (this.block.length - this.digest.getDigestSize()) - 2 : (this.block.length - this.digest.getDigestSize()) - 3)];
        reset();
    }

    public void reset() {
        this.digest.reset();
        this.messageLength = 0;
        clearBlock(this.mBuf);
        if (this.recoveredMessage != null) {
            clearBlock(this.recoveredMessage);
        }
        this.recoveredMessage = null;
        this.fullMessage = false;
        if (this.preSig != null) {
            this.preSig = null;
            clearBlock(this.preBlock);
            this.preBlock = null;
        }
    }

    public void update(byte b) {
        this.digest.update(b);
        if (this.messageLength < this.mBuf.length) {
            this.mBuf[this.messageLength] = b;
        }
        this.messageLength++;
    }

    public void update(byte[] bArr, int i, int i2) {
        while (i2 > 0 && this.messageLength < this.mBuf.length) {
            update(bArr[i]);
            i++;
            i2--;
        }
        this.digest.update(bArr, i, i2);
        this.messageLength += i2;
    }

    public void updateWithRecoveredMessage(byte[] bArr) throws InvalidCipherTextException {
        byte[] bArr2;
        int length;
        byte[] processBlock = this.cipher.processBlock(bArr, 0, bArr.length);
        if (((processBlock[0] & 192) ^ 64) != 0) {
            throw new InvalidCipherTextException("malformed signature");
        } else if (((processBlock[processBlock.length - 1] & 15) ^ 12) == 0) {
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
            int i2 = 0;
            while (i2 != processBlock.length && ((processBlock[i2] & 15) ^ 10) != 0) {
                i2++;
            }
            int i3 = i2 + 1;
            int length2 = ((processBlock.length - i) - this.digest.getDigestSize()) - i3;
            if (length2 > 0) {
                if ((processBlock[0] & 32) == 0) {
                    this.fullMessage = true;
                    this.recoveredMessage = new byte[length2];
                    bArr2 = this.recoveredMessage;
                    length = this.recoveredMessage.length;
                } else {
                    this.fullMessage = false;
                    this.recoveredMessage = new byte[length2];
                    bArr2 = this.recoveredMessage;
                    length = this.recoveredMessage.length;
                }
                System.arraycopy(processBlock, i3, bArr2, 0, length);
                this.preSig = bArr;
                this.preBlock = processBlock;
                this.digest.update(this.recoveredMessage, 0, this.recoveredMessage.length);
                this.messageLength = this.recoveredMessage.length;
                System.arraycopy(this.recoveredMessage, 0, this.mBuf, 0, this.recoveredMessage.length);
                return;
            }
            throw new InvalidCipherTextException("malformed block");
        } else {
            throw new InvalidCipherTextException("malformed signature");
        }
    }

    public boolean verifySignature(byte[] bArr) {
        byte[] bArr2;
        int i;
        byte[] bArr3;
        if (this.preSig == null) {
            try {
                bArr2 = this.cipher.processBlock(bArr, 0, bArr.length);
            } catch (Exception e) {
                return false;
            }
        } else if (Arrays.areEqual(this.preSig, bArr)) {
            bArr2 = this.preBlock;
            this.preSig = null;
            this.preBlock = null;
        } else {
            throw new IllegalStateException("updateWithRecoveredMessage called on different signature");
        }
        if (((bArr2[0] & 192) ^ 64) != 0) {
            return returnFalse(bArr2);
        }
        if (((bArr2[bArr2.length - 1] & 15) ^ 12) != 0) {
            return returnFalse(bArr2);
        }
        int i2 = 2;
        if (((bArr2[bArr2.length - 1] & 255) ^ PSSSigner.TRAILER_IMPLICIT) == 0) {
            i2 = 1;
        } else {
            byte b = ((bArr2[bArr2.length - 2] & 255) << 8) | (bArr2[bArr2.length - 1] & 255);
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
        int i3 = 0;
        while (i3 != bArr2.length && ((bArr2[i3] & 15) ^ 10) != 0) {
            i3++;
        }
        int i4 = i3 + 1;
        byte[] bArr4 = new byte[this.digest.getDigestSize()];
        int length = (bArr2.length - i2) - bArr4.length;
        int i5 = length - i4;
        if (i5 <= 0) {
            return returnFalse(bArr2);
        }
        if ((bArr2[0] & 32) == 0) {
            this.fullMessage = true;
            if (this.messageLength > i5) {
                return returnFalse(bArr2);
            }
            this.digest.reset();
            this.digest.update(bArr2, i4, i5);
            this.digest.doFinal(bArr4, 0);
            boolean z = true;
            for (int i6 = 0; i6 != bArr4.length; i6++) {
                int i7 = length + i6;
                bArr2[i7] = (byte) (bArr2[i7] ^ bArr4[i6]);
                if (bArr2[i7] != 0) {
                    z = false;
                }
            }
            if (!z) {
                return returnFalse(bArr2);
            }
            this.recoveredMessage = new byte[i5];
            bArr3 = this.recoveredMessage;
            i = this.recoveredMessage.length;
        } else {
            this.fullMessage = false;
            this.digest.doFinal(bArr4, 0);
            boolean z2 = true;
            for (int i8 = 0; i8 != bArr4.length; i8++) {
                int i9 = length + i8;
                bArr2[i9] = (byte) (bArr2[i9] ^ bArr4[i8]);
                if (bArr2[i9] != 0) {
                    z2 = false;
                }
            }
            if (!z2) {
                return returnFalse(bArr2);
            }
            this.recoveredMessage = new byte[i5];
            bArr3 = this.recoveredMessage;
            i = this.recoveredMessage.length;
        }
        System.arraycopy(bArr2, i4, bArr3, 0, i);
        if (this.messageLength != 0 && !isSameAs(this.mBuf, this.recoveredMessage)) {
            return returnFalse(bArr2);
        }
        clearBlock(this.mBuf);
        clearBlock(bArr2);
        this.messageLength = 0;
        return true;
    }
}
