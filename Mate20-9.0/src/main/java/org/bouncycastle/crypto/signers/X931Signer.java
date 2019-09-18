package org.bouncycastle.crypto.signers;

import java.math.BigInteger;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;

public class X931Signer implements Signer {
    public static final int TRAILER_IMPLICIT = 188;
    public static final int TRAILER_RIPEMD128 = 13004;
    public static final int TRAILER_RIPEMD160 = 12748;
    public static final int TRAILER_SHA1 = 13260;
    public static final int TRAILER_SHA224 = 14540;
    public static final int TRAILER_SHA256 = 13516;
    public static final int TRAILER_SHA384 = 14028;
    public static final int TRAILER_SHA512 = 13772;
    public static final int TRAILER_WHIRLPOOL = 14284;
    private byte[] block;
    private AsymmetricBlockCipher cipher;
    private Digest digest;
    private RSAKeyParameters kParam;
    private int keyBits;
    private int trailer;

    public X931Signer(AsymmetricBlockCipher asymmetricBlockCipher, Digest digest2) {
        this(asymmetricBlockCipher, digest2, false);
    }

    public X931Signer(AsymmetricBlockCipher asymmetricBlockCipher, Digest digest2, boolean z) {
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

    private void createSignatureBlock(int i) {
        int i2;
        int digestSize = this.digest.getDigestSize();
        if (i == 188) {
            i2 = (this.block.length - digestSize) - 1;
            this.digest.doFinal(this.block, i2);
            this.block[this.block.length - 1] = PSSSigner.TRAILER_IMPLICIT;
        } else {
            int length = (this.block.length - digestSize) - 2;
            this.digest.doFinal(this.block, length);
            this.block[this.block.length - 2] = (byte) (i >>> 8);
            this.block[this.block.length - 1] = (byte) i;
            i2 = length;
        }
        this.block[0] = 107;
        for (int i3 = i2 - 2; i3 != 0; i3--) {
            this.block[i3] = -69;
        }
        this.block[i2 - 1] = -70;
    }

    public byte[] generateSignature() throws CryptoException {
        createSignatureBlock(this.trailer);
        BigInteger bigInteger = new BigInteger(1, this.cipher.processBlock(this.block, 0, this.block.length));
        clearBlock(this.block);
        return BigIntegers.asUnsignedByteArray((this.kParam.getModulus().bitLength() + 7) / 8, bigInteger.min(this.kParam.getModulus().subtract(bigInteger)));
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        this.kParam = (RSAKeyParameters) cipherParameters;
        this.cipher.init(z, this.kParam);
        this.keyBits = this.kParam.getModulus().bitLength();
        this.block = new byte[((this.keyBits + 7) / 8)];
        reset();
    }

    public void reset() {
        this.digest.reset();
    }

    public void update(byte b) {
        this.digest.update(b);
    }

    public void update(byte[] bArr, int i, int i2) {
        this.digest.update(bArr, i, i2);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x002d, code lost:
        if ((r4.intValue() & 15) == 12) goto L_0x002f;
     */
    public boolean verifySignature(byte[] bArr) {
        boolean z = false;
        try {
            this.block = this.cipher.processBlock(bArr, 0, bArr.length);
            BigInteger bigInteger = new BigInteger(1, this.block);
            if ((bigInteger.intValue() & 15) != 12) {
                bigInteger = this.kParam.getModulus().subtract(bigInteger);
            }
            createSignatureBlock(this.trailer);
            byte[] asUnsignedByteArray = BigIntegers.asUnsignedByteArray(this.block.length, bigInteger);
            z = Arrays.constantTimeAreEqual(this.block, asUnsignedByteArray);
            if (this.trailer == 15052 && !z) {
                this.block[this.block.length - 2] = 64;
                z = Arrays.constantTimeAreEqual(this.block, asUnsignedByteArray);
            }
            clearBlock(this.block);
            clearBlock(asUnsignedByteArray);
            return z;
        } catch (Exception e) {
            return false;
        }
    }
}
