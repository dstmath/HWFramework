package com.android.org.bouncycastle.jcajce.provider.symmetric.util;

import com.android.org.bouncycastle.crypto.CipherKeyGenerator;
import com.android.org.bouncycastle.crypto.KeyGenerationParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyGeneratorSpi;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class BaseKeyGenerator extends KeyGeneratorSpi {
    protected String algName;
    protected int defaultKeySize;
    protected CipherKeyGenerator engine;
    protected int keySize;
    protected boolean uninitialised = true;

    protected BaseKeyGenerator(String algName, int defaultKeySize, CipherKeyGenerator engine) {
        this.algName = algName;
        this.defaultKeySize = defaultKeySize;
        this.keySize = defaultKeySize;
        this.engine = engine;
    }

    protected void engineInit(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
        throw new InvalidAlgorithmParameterException("Not Implemented");
    }

    protected void engineInit(SecureRandom random) {
        if (random != null) {
            this.engine.init(new KeyGenerationParameters(random, this.defaultKeySize));
            this.uninitialised = false;
        }
    }

    protected void engineInit(int keySize, SecureRandom random) {
        if (random == null) {
            try {
                random = new SecureRandom();
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterException(e.getMessage());
            }
        }
        this.engine.init(new KeyGenerationParameters(random, keySize));
        this.uninitialised = false;
    }

    protected SecretKey engineGenerateKey() {
        if (this.uninitialised) {
            this.engine.init(new KeyGenerationParameters(new SecureRandom(), this.defaultKeySize));
            this.uninitialised = false;
        }
        return new SecretKeySpec(this.engine.generateKey(), this.algName);
    }
}
