package javax.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

public abstract class KeyGeneratorSpi {
    /* access modifiers changed from: protected */
    public abstract SecretKey engineGenerateKey();

    /* access modifiers changed from: protected */
    public abstract void engineInit(int i, SecureRandom secureRandom);

    /* access modifiers changed from: protected */
    public abstract void engineInit(SecureRandom secureRandom);

    /* access modifiers changed from: protected */
    public abstract void engineInit(AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidAlgorithmParameterException;
}
