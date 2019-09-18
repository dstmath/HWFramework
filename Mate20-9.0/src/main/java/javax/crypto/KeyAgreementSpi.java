package javax.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

public abstract class KeyAgreementSpi {
    /* access modifiers changed from: protected */
    public abstract Key engineDoPhase(Key key, boolean z) throws InvalidKeyException, IllegalStateException;

    /* access modifiers changed from: protected */
    public abstract int engineGenerateSecret(byte[] bArr, int i) throws IllegalStateException, ShortBufferException;

    /* access modifiers changed from: protected */
    public abstract SecretKey engineGenerateSecret(String str) throws IllegalStateException, NoSuchAlgorithmException, InvalidKeyException;

    /* access modifiers changed from: protected */
    public abstract byte[] engineGenerateSecret() throws IllegalStateException;

    /* access modifiers changed from: protected */
    public abstract void engineInit(Key key, SecureRandom secureRandom) throws InvalidKeyException;

    /* access modifiers changed from: protected */
    public abstract void engineInit(Key key, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidKeyException, InvalidAlgorithmParameterException;
}
