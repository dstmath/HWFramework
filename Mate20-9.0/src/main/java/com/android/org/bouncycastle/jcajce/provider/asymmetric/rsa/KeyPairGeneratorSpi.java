package com.android.org.bouncycastle.jcajce.provider.asymmetric.rsa;

import com.android.org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import com.android.org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import com.android.org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import com.android.org.bouncycastle.crypto.params.RSAKeyParameters;
import com.android.org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import com.android.org.bouncycastle.jcajce.provider.asymmetric.util.PrimeCertaintyCalculator;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;

public class KeyPairGeneratorSpi extends KeyPairGenerator {
    static final BigInteger defaultPublicExponent = BigInteger.valueOf(65537);
    RSAKeyPairGenerator engine;
    RSAKeyGenerationParameters param;

    public KeyPairGeneratorSpi(String algorithmName) {
        super(algorithmName);
    }

    public KeyPairGeneratorSpi() {
        super("RSA");
        this.engine = new RSAKeyPairGenerator();
        this.param = new RSAKeyGenerationParameters(defaultPublicExponent, new SecureRandom(), 2048, PrimeCertaintyCalculator.getDefaultCertainty(2048));
        this.engine.init(this.param);
    }

    public void initialize(int strength, SecureRandom random) {
        this.param = new RSAKeyGenerationParameters(defaultPublicExponent, random != null ? random : new SecureRandom(), strength, PrimeCertaintyCalculator.getDefaultCertainty(strength));
        this.engine.init(this.param);
    }

    public void initialize(AlgorithmParameterSpec params, SecureRandom random) throws InvalidAlgorithmParameterException {
        if (params instanceof RSAKeyGenParameterSpec) {
            RSAKeyGenParameterSpec rsaParams = (RSAKeyGenParameterSpec) params;
            this.param = new RSAKeyGenerationParameters(rsaParams.getPublicExponent(), random != null ? random : new SecureRandom(), rsaParams.getKeysize(), PrimeCertaintyCalculator.getDefaultCertainty(2048));
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
