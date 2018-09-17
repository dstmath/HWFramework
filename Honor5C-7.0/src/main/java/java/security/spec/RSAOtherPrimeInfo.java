package java.security.spec;

import java.math.BigInteger;

public class RSAOtherPrimeInfo {
    private BigInteger crtCoefficient;
    private BigInteger prime;
    private BigInteger primeExponent;

    public RSAOtherPrimeInfo(BigInteger prime, BigInteger primeExponent, BigInteger crtCoefficient) {
        if (prime == null) {
            throw new NullPointerException("the prime parameter must be non-null");
        } else if (primeExponent == null) {
            throw new NullPointerException("the primeExponent parameter must be non-null");
        } else if (crtCoefficient == null) {
            throw new NullPointerException("the crtCoefficient parameter must be non-null");
        } else {
            this.prime = prime;
            this.primeExponent = primeExponent;
            this.crtCoefficient = crtCoefficient;
        }
    }

    public final BigInteger getPrime() {
        return this.prime;
    }

    public final BigInteger getExponent() {
        return this.primeExponent;
    }

    public final BigInteger getCrtCoefficient() {
        return this.crtCoefficient;
    }
}
