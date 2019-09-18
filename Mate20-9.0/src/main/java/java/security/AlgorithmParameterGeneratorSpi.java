package java.security;

import java.security.spec.AlgorithmParameterSpec;

public abstract class AlgorithmParameterGeneratorSpi {
    /* access modifiers changed from: protected */
    public abstract AlgorithmParameters engineGenerateParameters();

    /* access modifiers changed from: protected */
    public abstract void engineInit(int i, SecureRandom secureRandom);

    /* access modifiers changed from: protected */
    public abstract void engineInit(AlgorithmParameterSpec algorithmParameterSpec, SecureRandom secureRandom) throws InvalidAlgorithmParameterException;
}
