package java.security.spec;

import java.math.BigInteger;

public class RSAMultiPrimePrivateCrtKeySpec extends RSAPrivateKeySpec {
    private final BigInteger crtCoefficient;
    private final RSAOtherPrimeInfo[] otherPrimeInfo;
    private final BigInteger primeExponentP;
    private final BigInteger primeExponentQ;
    private final BigInteger primeP;
    private final BigInteger primeQ;
    private final BigInteger publicExponent;

    public RSAMultiPrimePrivateCrtKeySpec(BigInteger modulus, BigInteger publicExponent, BigInteger privateExponent, BigInteger primeP, BigInteger primeQ, BigInteger primeExponentP, BigInteger primeExponentQ, BigInteger crtCoefficient, RSAOtherPrimeInfo[] otherPrimeInfo) {
        super(modulus, privateExponent);
        if (modulus == null) {
            throw new NullPointerException("the modulus parameter must be non-null");
        } else if (publicExponent == null) {
            throw new NullPointerException("the publicExponent parameter must be non-null");
        } else if (privateExponent == null) {
            throw new NullPointerException("the privateExponent parameter must be non-null");
        } else if (primeP == null) {
            throw new NullPointerException("the primeP parameter must be non-null");
        } else if (primeQ == null) {
            throw new NullPointerException("the primeQ parameter must be non-null");
        } else if (primeExponentP == null) {
            throw new NullPointerException("the primeExponentP parameter must be non-null");
        } else if (primeExponentQ == null) {
            throw new NullPointerException("the primeExponentQ parameter must be non-null");
        } else if (crtCoefficient == null) {
            throw new NullPointerException("the crtCoefficient parameter must be non-null");
        } else {
            this.publicExponent = publicExponent;
            this.primeP = primeP;
            this.primeQ = primeQ;
            this.primeExponentP = primeExponentP;
            this.primeExponentQ = primeExponentQ;
            this.crtCoefficient = crtCoefficient;
            if (otherPrimeInfo == null) {
                this.otherPrimeInfo = null;
            } else if (otherPrimeInfo.length == 0) {
                throw new IllegalArgumentException("the otherPrimeInfo parameter must not be empty");
            } else {
                this.otherPrimeInfo = (RSAOtherPrimeInfo[]) otherPrimeInfo.clone();
            }
        }
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

    public RSAOtherPrimeInfo[] getOtherPrimeInfo() {
        if (this.otherPrimeInfo == null) {
            return null;
        }
        return (RSAOtherPrimeInfo[]) this.otherPrimeInfo.clone();
    }
}
