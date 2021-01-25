package org.bouncycastle.crypto.generators;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHValidationParameters;

public class DHParametersGenerator {
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private int certainty;
    private SecureRandom random;
    private int size;

    public DHParameters generateParameters() {
        BigInteger[] generateSafePrimes = DHParametersHelper.generateSafePrimes(this.size, this.certainty, this.random);
        BigInteger bigInteger = generateSafePrimes[0];
        BigInteger bigInteger2 = generateSafePrimes[1];
        return new DHParameters(bigInteger, DHParametersHelper.selectGenerator(bigInteger, bigInteger2, this.random), bigInteger2, TWO, (DHValidationParameters) null);
    }

    public void init(int i, int i2, SecureRandom secureRandom) {
        this.size = i;
        this.certainty = i2;
        this.random = secureRandom;
    }
}
