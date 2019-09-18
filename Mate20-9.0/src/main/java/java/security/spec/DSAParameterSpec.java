package java.security.spec;

import java.math.BigInteger;
import java.security.interfaces.DSAParams;

public class DSAParameterSpec implements AlgorithmParameterSpec, DSAParams {
    BigInteger g;
    BigInteger p;
    BigInteger q;

    public DSAParameterSpec(BigInteger p2, BigInteger q2, BigInteger g2) {
        this.p = p2;
        this.q = q2;
        this.g = g2;
    }

    public BigInteger getP() {
        return this.p;
    }

    public BigInteger getQ() {
        return this.q;
    }

    public BigInteger getG() {
        return this.g;
    }
}
