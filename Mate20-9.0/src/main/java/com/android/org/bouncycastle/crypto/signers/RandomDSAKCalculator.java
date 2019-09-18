package com.android.org.bouncycastle.crypto.signers;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RandomDSAKCalculator implements DSAKCalculator {
    private static final BigInteger ZERO = BigInteger.valueOf(0);
    private BigInteger q;
    private SecureRandom random;

    public boolean isDeterministic() {
        return false;
    }

    public void init(BigInteger n, SecureRandom random2) {
        this.q = n;
        this.random = random2;
    }

    public void init(BigInteger n, BigInteger d, byte[] message) {
        throw new IllegalStateException("Operation not supported");
    }

    public BigInteger nextK() {
        int qBitLength = this.q.bitLength();
        while (true) {
            BigInteger k = new BigInteger(qBitLength, this.random);
            if (!k.equals(ZERO) && k.compareTo(this.q) < 0) {
                return k;
            }
        }
    }
}
