package java.security.spec;

import java.math.BigInteger;

public class RSAOtherPrimeInfo {
    private BigInteger crtCoefficient;
    private BigInteger prime;
    private BigInteger primeExponent;

    public RSAOtherPrimeInfo(BigInteger prime2, BigInteger primeExponent2, BigInteger crtCoefficient2) {
        if (prime2 == null) {
            throw new NullPointerException("the prime parameter must be non-null");
        } else if (primeExponent2 == null) {
            throw new NullPointerException("the primeExponent parameter must be non-null");
        } else if (crtCoefficient2 != null) {
            this.prime = prime2;
            this.primeExponent = primeExponent2;
            this.crtCoefficient = crtCoefficient2;
        } else {
            throw new NullPointerException("the crtCoefficient parameter must be non-null");
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
