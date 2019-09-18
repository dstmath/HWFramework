package org.bouncycastle.crypto.signers;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RandomDSAKCalculator implements DSAKCalculator {
    private static final BigInteger ZERO = BigInteger.valueOf(0);
    private BigInteger q;
    private SecureRandom random;

    public void init(BigInteger bigInteger, BigInteger bigInteger2, byte[] bArr) {
        throw new IllegalStateException("Operation not supported");
    }

    public void init(BigInteger bigInteger, SecureRandom secureRandom) {
        this.q = bigInteger;
        this.random = secureRandom;
    }

    public boolean isDeterministic() {
        return false;
    }

    public BigInteger nextK() {
        int bitLength = this.q.bitLength();
        while (true) {
            BigInteger bigInteger = new BigInteger(bitLength, this.random);
            if (!bigInteger.equals(ZERO) && bigInteger.compareTo(this.q) < 0) {
                return bigInteger;
            }
        }
    }
}
