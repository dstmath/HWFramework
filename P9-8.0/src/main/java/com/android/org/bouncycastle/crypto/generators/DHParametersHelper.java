package com.android.org.bouncycastle.crypto.generators;

import com.android.org.bouncycastle.math.ec.WNafUtil;
import com.android.org.bouncycastle.util.BigIntegers;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.logging.Logger;

class DHParametersHelper {
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static final BigInteger TWO = BigInteger.valueOf(2);
    private static final Logger logger = Logger.getLogger(DHParametersHelper.class.getName());

    DHParametersHelper() {
    }

    static BigInteger[] generateSafePrimes(int size, int certainty, SecureRandom random) {
        logger.info("Generating safe primes. This may take a long time.");
        long start = System.currentTimeMillis();
        int tries = 0;
        int qLength = size - 1;
        int minWeight = size >>> 2;
        while (true) {
            tries++;
            BigInteger q = new BigInteger(qLength, 2, random);
            BigInteger p = q.shiftLeft(1).add(ONE);
            if (p.isProbablePrime(certainty) && ((certainty <= 2 || (q.isProbablePrime(certainty - 2) ^ 1) == 0) && WNafUtil.getNafWeight(p) >= minWeight)) {
                logger.info("Generated safe primes: " + tries + " tries took " + (System.currentTimeMillis() - start) + "ms");
                return new BigInteger[]{p, q};
            }
        }
    }

    static BigInteger selectGenerator(BigInteger p, BigInteger q, SecureRandom random) {
        BigInteger g;
        BigInteger pMinusTwo = p.subtract(TWO);
        do {
            g = BigIntegers.createRandomInRange(TWO, pMinusTwo, random).modPow(TWO, p);
        } while (g.equals(ONE));
        return g;
    }
}
