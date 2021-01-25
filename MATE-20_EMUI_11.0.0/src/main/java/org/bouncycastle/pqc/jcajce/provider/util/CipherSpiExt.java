package org.bouncycastle.pqc.jcajce.provider.util;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

public abstract class CipherSpiExt extends CipherSpi {
    public static final int DECRYPT_MODE = 2;
    public static final int ENCRYPT_MODE = 1;
    protected int opMode;

    public abstract int doFinal(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException;

    public final byte[] doFinal() throws IllegalBlockSizeException, BadPaddingException {
        return doFinal(null, 0, 0);
    }

    public final byte[] doFinal(byte[] bArr) throws IllegalBlockSizeException, BadPaddingException {
        return doFinal(bArr, 0, bArr.length);
    }

    public abstract byte[] doFinal(byte[] bArr, int i, int i2) throws IllegalBlockSizeException, BadPaddingException;

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineDoFinal(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        return doFinal(bArr, i, i2, bArr2, i3);
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final byte[] engineDoFinal(byte[] bArr, int i, int i2) throws IllegalBlockSizeException, BadPaddingException {
        return doFinal(bArr, i, i2);
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineGetBlockSize() {
        return getBlockSize();
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final byte[] engineGetIV() {
        return getIV();
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineGetKeySize(Key key) throws InvalidKeyException {
        if (key instanceof Key) {
            return getKeySize(key);
        }
        throw new InvalidKeyException("Unsupported key.");
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineGetOutputSize(int i) {
        return getOutputSize(i);
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final AlgorithmParameters engineGetParameters() {
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final void engineInit(int i, Key key, AlgorithmParameters algorithmParameters, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (algorithmParameters == null) {
            engineInit(i, key, secureRandom);
        } else {
            engineInit(i, key, (AlgorithmParameterSpec) null, secureRandom);
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final void engineInit(int i, Key key, SecureRandom secureRandom) throws InvalidKeyException {
        try {
            engineInit(i, key, (AlgorithmParameterSpec) null, secureRandom);
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidParameterException(e.getMessage());
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public void engineInit(int i, Key key, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (algorithmParameterSpec != null && !(algorithmParameterSpec instanceof AlgorithmParameterSpec)) {
            throw new InvalidAlgorithmParameterException();
        } else if (key == null || !(key instanceof Key)) {
            throw new InvalidKeyException();
        } else {
            this.opMode = i;
            if (i == 1) {
                initEncrypt(key, algorithmParameterSpec, secureRandom);
            } else if (i == 2) {
                initDecrypt(key, algorithmParameterSpec);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final void engineSetMode(String str) throws NoSuchAlgorithmException {
        setMode(str);
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final void engineSetPadding(String str) throws NoSuchPaddingException {
        setPadding(str);
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final int engineUpdate(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws ShortBufferException {
        return update(bArr, i, i2, bArr2, i3);
    }

    /* access modifiers changed from: protected */
    @Override // javax.crypto.CipherSpi
    public final byte[] engineUpdate(byte[] bArr, int i, int i2) {
        return update(bArr, i, i2);
    }

    public abstract int getBlockSize();

    public abstract byte[] getIV();

    public abstract int getKeySize(Key key) throws InvalidKeyException;

    public abstract String getName();

    public abstract int getOutputSize(int i);

    public abstract AlgorithmParameterSpec getParameters();

    public abstract void initDecrypt(Key key, AlgorithmParameterSpec algorithmParameterSpec) throws InvalidKeyException, InvalidAlgorithmParameterException;

    public abstract void initEncrypt(Key key, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException;

    /* access modifiers changed from: protected */
    public abstract void setMode(String str) throws NoSuchAlgorithmException;

    /* access modifiers changed from: protected */
    public abstract void setPadding(String str) throws NoSuchPaddingException;

    public abstract int update(byte[] bArr, int i, int i2, byte[] bArr2, int i3) throws ShortBufferException;

    public final byte[] update(byte[] bArr) {
        return update(bArr, 0, bArr.length);
    }

    public abstract byte[] update(byte[] bArr, int i, int i2);
}
