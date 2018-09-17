package java.security;

import java.security.spec.AlgorithmParameterSpec;

public abstract class AlgorithmParameterGeneratorSpi {
    protected abstract AlgorithmParameters engineGenerateParameters();

    protected abstract void engineInit(int i, SecureRandom secureRandom);

    protected abstract void engineInit(AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidAlgorithmParameterException;
}
