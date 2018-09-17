package java.security.interfaces;

import java.math.BigInteger;
import java.security.spec.RSAOtherPrimeInfo;

public interface RSAMultiPrimePrivateCrtKey extends RSAPrivateKey {
    public static final long serialVersionUID = 618058533534628008L;

    BigInteger getCrtCoefficient();

    RSAOtherPrimeInfo[] getOtherPrimeInfo();

    BigInteger getPrimeExponentP();

    BigInteger getPrimeExponentQ();

    BigInteger getPrimeP();

    BigInteger getPrimeQ();

    BigInteger getPublicExponent();
}
