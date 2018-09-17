package com.android.org.bouncycastle.crypto.generators;

import com.android.org.bouncycastle.crypto.params.DHParameters;
import java.math.BigInteger;
import java.security.SecureRandom;

public class DHParametersGenerator {
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private int certainty;
    private SecureRandom random;
    private int size;

    public void init(int size, int certainty, SecureRandom random) {
        this.size = size;
        this.certainty = certainty;
        this.random = random;
    }

    public DHParameters generateParameters() {
        BigInteger[] safePrimes = DHParametersHelper.generateSafePrimes(this.size, this.certainty, this.random);
        BigInteger p = safePrimes[0];
        BigInteger q = safePrimes[1];
        return new DHParameters(p, DHParametersHelper.selectGenerator(p, q, this.random), q, TWO, null);
    }
}
