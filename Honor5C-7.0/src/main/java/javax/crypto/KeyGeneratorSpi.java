package javax.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

public abstract class KeyGeneratorSpi {
    protected abstract SecretKey engineGenerateKey();

    protected abstract void engineInit(int i, SecureRandom secureRandom);

    protected abstract void engineInit(SecureRandom secureRandom);

    protected abstract void engineInit(AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidAlgorithmParameterException;
}
