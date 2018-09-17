package java.security.interfaces;

import java.math.BigInteger;

public interface DSAParams {
    BigInteger getG();

    BigInteger getP();

    BigInteger getQ();
}
