package java.security.spec;

import java.math.BigInteger;

public class RSAPrivateCrtKeySpec extends RSAPrivateKeySpec {
    private final BigInteger crtCoefficient;
    private final BigInteger primeExponentP;
    private final BigInteger primeExponentQ;
    private final BigInteger primeP;
    private final BigInteger primeQ;
    private final BigInteger publicExponent;

    public RSAPrivateCrtKeySpec(BigInteger modulus, BigInteger publicExponent2, BigInteger privateExponent, BigInteger primeP2, BigInteger primeQ2, BigInteger primeExponentP2, BigInteger primeExponentQ2, BigInteger crtCoefficient2) {
        super(modulus, privateExponent);
        this.publicExponent = publicExponent2;
        this.primeP = primeP2;
        this.primeQ = primeQ2;
        this.primeExponentP = primeExponentP2;
        this.primeExponentQ = primeExponentQ2;
        this.crtCoefficient = crtCoefficient2;
    }

    public BigInteger getPublicExponent() {
        return this.publicExponent;
    }

    public BigInteger getPrimeP() {
        return this.primeP;
    }

    public BigInteger getPrimeQ() {
        return this.primeQ;
    }

    public BigInteger getPrimeExponentP() {
        return this.primeExponentP;
    }

    public BigInteger getPrimeExponentQ() {
        return this.primeExponentQ;
    }

    public BigInteger getCrtCoefficient() {
        return this.crtCoefficient;
    }
}
