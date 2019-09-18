package org.bouncycastle.pqc.jcajce.provider.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.ShortBufferException;
import org.bouncycastle.crypto.CryptoServicesRegistrar;

public abstract class AsymmetricHybridCipher extends CipherSpiExt {
    protected AlgorithmParameterSpec paramSpec;

    /* access modifiers changed from: protected */
    public abstract int decryptOutputSize(int i);

    public final int doFinal(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws ShortBufferException, BadPaddingException {
        if (bArr2.length >= getOutputSize(i2)) {
            byte[] doFinal = doFinal(bArr, i, i2);
            System.arraycopy(doFinal, 0, bArr2, i3, doFinal.length);
            return doFinal.length;
        }
        throw new ShortBufferException("Output buffer too short.");
    }

    public abstract byte[] doFinal(byte[] bArr, int i, int i2) throws BadPaddingException;

    /* access modifiers changed from: protected */
    public abstract int encryptOutputSize(int i);

    public final int getBlockSize() {
        return 0;
    }

    public final byte[] getIV() {
        return null;
    }

    public final int getOutputSize(int i) {
        return this.opMode == 1 ? encryptOutputSize(i) : decryptOutputSize(i);
    }

    public final AlgorithmParameterSpec getParameters() {
        return this.paramSpec;
    }

    /* access modifiers changed from: protected */
    public abstract void initCipherDecrypt(Key key, AlgorithmParameterSpec algorithmParameterSpec) throws InvalidKeyException, InvalidAlgorithmParameterException;

    /* access modifiers changed from: protected */
    public abstract void initCipherEncrypt(Key key, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException;

    public final void initDecrypt(Key key) throws InvalidKeyException {
        try {
            initDecrypt(key, null);
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidParameterException("This cipher needs algorithm parameters for initialization (cannot be null).");
        }
    }

    public final void initDecrypt(Key key, AlgorithmParameterSpec algorithmParameterSpec) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.opMode = 2;
        initCipherDecrypt(key, algorithmParameterSpec);
    }

    public final void initEncrypt(Key key) throws InvalidKeyException {
        try {
            initEncrypt(key, null, CryptoServicesRegistrar.getSecureRandom());
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidParameterException("This cipher needs algorithm parameters for initialization (cannot be null).");
        }
    }

    public final void initEncrypt(Key key, SecureRandom secureRandom) throws InvalidKeyException {
        try {
            initEncrypt(key, null, secureRandom);
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidParameterException("This cipher needs algorithm parameters for initialization (cannot be null).");
        }
    }

    public final void initEncrypt(Key key, AlgorithmParameterSpec algorithmParameterSpec) throws InvalidKeyException, InvalidAlgorithmParameterException {
        initEncrypt(key, algorithmParameterSpec, CryptoServicesRegistrar.getSecureRandom());
    }

    public final void initEncrypt(Key key, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {
        this.opMode = 1;
        initCipherEncrypt(key, algorithmParameterSpec, secureRandom);
    }

    /* access modifiers changed from: protected */
    public final void setMode(String str) {
    }

    /* access modifiers changed from: protected */
    public final void setPadding(String str) {
    }

    public final int update(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws ShortBufferException {
        if (bArr2.length >= getOutputSize(i2)) {
            byte[] update = update(bArr, i, i2);
            System.arraycopy(update, 0, bArr2, i3, update.length);
            return update.length;
        }
        throw new ShortBufferException("output");
    }

    public abstract byte[] update(byte[] bArr, int i, int i2);
}
