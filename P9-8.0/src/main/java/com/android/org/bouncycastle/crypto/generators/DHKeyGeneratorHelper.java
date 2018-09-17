package com.android.org.bouncycastle.crypto.generators;

import com.android.org.bouncycastle.crypto.params.DHParameters;
import com.android.org.bouncycastle.math.ec.WNafUtil;
import com.android.org.bouncycastle.util.BigIntegers;
import java.math.BigInteger;
import java.security.SecureRandom;

class DHKeyGeneratorHelper {
    static final DHKeyGeneratorHelper INSTANCE = new DHKeyGeneratorHelper();
    private static final BigInteger ONE = BigInteger.valueOf(1);
    private static final BigInteger TWO = BigInteger.valueOf(2);

    private DHKeyGeneratorHelper() {
    }

    BigInteger calculatePrivate(DHParameters dhParams, SecureRandom random) {
        int limit = dhParams.getL();
        int minWeight;
        BigInteger x;
        if (limit != 0) {
            minWeight = limit >>> 2;
            do {
                x = new BigInteger(limit, random).setBit(limit - 1);
            } while (WNafUtil.getNafWeight(x) < minWeight);
            return x;
        }
        BigInteger min = TWO;
        int m = dhParams.getM();
        if (m != 0) {
            min = ONE.shiftLeft(m - 1);
        }
        BigInteger q = dhParams.getQ();
        if (q == null) {
            q = dhParams.getP();
        }
        BigInteger max = q.subtract(TWO);
        minWeight = max.bitLength() >>> 2;
        do {
            x = BigIntegers.createRandomInRange(min, max, random);
        } while (WNafUtil.getNafWeight(x) < minWeight);
        return x;
    }

    BigInteger calculatePublic(DHParameters dhParams, BigInteger x) {
        return dhParams.getG().modPow(x, dhParams.getP());
    }
}
