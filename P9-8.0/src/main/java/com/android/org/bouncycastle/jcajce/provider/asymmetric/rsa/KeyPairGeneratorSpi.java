package com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa;

import com.android.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import com.android.org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import com.android.org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import com.android.org.bouncycastle.crypto.params.RSAKeyParameters;
import com.android.org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;

public class KeyPairGeneratorSpi extends KeyPairGenerator {
    static final BigInteger defaultPublicExponent = BigInteger.valueOf(65537);
    static final int defaultTests = 112;
    RSAKeyPairGenerator engine;
    RSAKeyGenerationParameters param;

    public KeyPairGeneratorSpi(String algorithmName) {
        super(algorithmName);
    }

    public KeyPairGeneratorSpi() {
        super("RSA");
        this.engine = new RSAKeyPairGenerator();
        this.param = new RSAKeyGenerationParameters(defaultPublicExponent, new SecureRandom(), 2048, defaultTests);
        this.engine.init(this.param);
    }

    public void initialize(int strength, SecureRandom random) {
        BigInteger bigInteger = defaultPublicExponent;
        if (random == null) {
            random = new SecureRandom();
        }
        this.param = new RSAKeyGenerationParameters(bigInteger, random, strength, defaultTests);
        this.engine.init(this.param);
    }

    public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
        if (params instanceof RSAKeyGenParameterSpec) {
            RSAKeyGenParameterSpec rsaParams = (RSAKeyGenParameterSpec) params;
            BigInteger publicExponent = rsaParams.getPublicExponent();
            if (random == null) {
                random = new SecureRandom();
            }
            this.param = new RSAKeyGenerationParameters(publicExponent, random, rsaParams.getKeysize(), defaultTests);
            this.engine.init(this.param);
            return;
        }
        throw new InvalidAlgorithmParameterException("parameter object not a RSAKeyGenParameterSpec");
    }

    public KeyPair generateKeyPair() {
        AsymmetricCipherKeyPair pair = this.engine.generateKeyPair();
        return new KeyPair(new BCRSAPublicKey((RSAKeyParameters) pair.getPublic()), new BCRSAPrivateCrtKey((RSAPrivateCrtKeyParameters) pair.getPrivate()));
    }
}
