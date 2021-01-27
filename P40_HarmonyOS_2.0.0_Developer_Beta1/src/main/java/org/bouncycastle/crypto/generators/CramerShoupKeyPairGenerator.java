package org.bouncycastle.crypto.generators;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.CramerShoupKeyGenerationParameters;
import org.bouncycastle.crypto.params.CramerShoupParameters;
import org.bouncycastle.crypto.params.CramerShoupPrivateKeyParameters;
import org.bouncycastle.crypto.params.CramerShoupPublicKeyParameters;
import org.bouncycastle.util.BigIntegers;

public class CramerShoupKeyPairGenerator implements AsymmetricCipherKeyPairGenerator {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private CramerShoupKeyGenerationParameters param;

    private CramerShoupPublicKeyParameters calculatePublicKey(CramerShoupParameters cramerShoupParameters, CramerShoupPrivateKeyParameters cramerShoupPrivateKeyParameters) {
        BigInteger g1 = cramerShoupParameters.getG1();
        BigInteger g2 = cramerShoupParameters.getG2();
        BigInteger p = cramerShoupParameters.getP();
        return new CramerShoupPublicKeyParameters(cramerShoupParameters, g1.modPow(cramerShoupPrivateKeyParameters.getX1(), p).multiply(g2.modPow(cramerShoupPrivateKeyParameters.getX2(), p)), g1.modPow(cramerShoupPrivateKeyParameters.getY1(), p).multiply(g2.modPow(cramerShoupPrivateKeyParameters.getY2(), p)), g1.modPow(cramerShoupPrivateKeyParameters.getZ(), p));
    }

    private CramerShoupPrivateKeyParameters generatePrivateKey(SecureRandom secureRandom, CramerShoupParameters cramerShoupParameters) {
        BigInteger p = cramerShoupParameters.getP();
        return new CramerShoupPrivateKeyParameters(cramerShoupParameters, generateRandomElement(p, secureRandom), generateRandomElement(p, secureRandom), generateRandomElement(p, secureRandom), generateRandomElement(p, secureRandom), generateRandomElement(p, secureRandom));
    }

    private BigInteger generateRandomElement(BigInteger bigInteger, SecureRandom secureRandom) {
        BigInteger bigInteger2 = ONE;
        return BigIntegers.createRandomInRange(bigInteger2, bigInteger.subtract(bigInteger2), secureRandom);
    }

    @Override // org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
    public AsymmetricCipherKeyPair generateKeyPair() {
        CramerShoupParameters parameters = this.param.getParameters();
        CramerShoupPrivateKeyParameters generatePrivateKey = generatePrivateKey(this.param.getRandom(), parameters);
        CramerShoupPublicKeyParameters calculatePublicKey = calculatePublicKey(parameters, generatePrivateKey);
        generatePrivateKey.setPk(calculatePublicKey);
        return new AsymmetricCipherKeyPair((AsymmetricKeyParameter) calculatePublicKey, (AsymmetricKeyParameter) generatePrivateKey);
    }

    @Override // org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
    public void init(KeyGenerationParameters keyGenerationParameters) {
        this.param = (CramerShoupKeyGenerationParameters) keyGenerationParameters;
    }
}
