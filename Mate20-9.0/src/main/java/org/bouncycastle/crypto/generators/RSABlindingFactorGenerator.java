package org.bouncycastle.crypto.generators;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;

public class RSABlindingFactorGenerator {
    private static BigInteger ONE = BigInteger.valueOf(1);
    private static BigInteger ZERO = BigInteger.valueOf(0);
    private RSAKeyParameters key;
    private SecureRandom random;

    public BigInteger generateBlindingFactor() {
        if (this.key != null) {
            BigInteger modulus = this.key.getModulus();
            int bitLength = modulus.bitLength() - 1;
            while (true) {
                BigInteger bigInteger = new BigInteger(bitLength, this.random);
                BigInteger gcd = bigInteger.gcd(modulus);
                if (!bigInteger.equals(ZERO) && !bigInteger.equals(ONE) && gcd.equals(ONE)) {
                    return bigInteger;
                }
            }
        } else {
            throw new IllegalStateException("generator not initialised");
        }
    }

    public void init(CipherParameters cipherParameters) {
        SecureRandom secureRandom;
        if (cipherParameters instanceof ParametersWithRandom) {
            ParametersWithRandom parametersWithRandom = (ParametersWithRandom) cipherParameters;
            this.key = (RSAKeyParameters) parametersWithRandom.getParameters();
            secureRandom = parametersWithRandom.getRandom();
        } else {
            this.key = (RSAKeyParameters) cipherParameters;
            secureRandom = CryptoServicesRegistrar.getSecureRandom();
        }
        this.random = secureRandom;
        if (this.key instanceof RSAPrivateCrtKeyParameters) {
            throw new IllegalArgumentException("generator requires RSA public key");
        }
    }
}
