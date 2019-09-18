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

    public RSAMultiPrimePrivateCrtKeySpec(BigInteger modulus, BigInteger publicExponent2, BigInteger privateExponent, BigInteger primeP2, BigInteger primeQ2, BigInteger primeExponentP2, BigInteger primeExponentQ2, BigInteger crtCoefficient2, RSAOtherPrimeInfo[] otherPrimeInfo2) {
        super(modulus, privateExponent);
        if (modulus == null) {
            throw new NullPointerException("the modulus parameter must be non-null");
        } else if (publicExponent2 == null) {
            throw new NullPointerException("the publicExponent parameter must be non-null");
        } else if (privateExponent == null) {
            throw new NullPointerException("the privateExponent parameter must be non-null");
        } else if (primeP2 == null) {
            throw new NullPointerException("the primeP parameter must be non-null");
        } else if (primeQ2 == null) {
            throw new NullPointerException("the primeQ parameter must be non-null");
        } else if (primeExponentP2 == null) {
            throw new NullPointerException("the primeExponentP parameter must be non-null");
        } else if (primeExponentQ2 == null) {
            throw new NullPointerException("the primeExponentQ parameter must be non-null");
        } else if (crtCoefficient2 != null) {
            this.publicExponent = publicExponent2;
            this.primeP = primeP2;
            this.primeQ = primeQ2;
            this.primeExponentP = primeExponentP2;
            this.primeExponentQ = primeExponentQ2;
            this.crtCoefficient = crtCoefficient2;
            if (otherPrimeInfo2 == null) {
                this.otherPrimeInfo = null;
            } else if (otherPrimeInfo2.length != 0) {
                this.otherPrimeInfo = (RSAOtherPrimeInfo[]) otherPrimeInfo2.clone();
            } else {
                throw new IllegalArgumentException("the otherPrimeInfo parameter must not be empty");
            }
        } else {
            throw new NullPointerException("the crtCoefficient parameter must be non-null");
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
