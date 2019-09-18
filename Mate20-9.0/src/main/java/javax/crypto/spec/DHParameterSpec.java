package javax.crypto.spec;

import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;

public class DHParameterSpec implements AlgorithmParameterSpec {
    private BigInteger g;
    private int l;
    private BigInteger p;

    public DHParameterSpec(BigInteger p2, BigInteger g2) {
        this.p = p2;
        this.g = g2;
        this.l = 0;
    }

    public DHParameterSpec(BigInteger p2, BigInteger g2, int l2) {
        this.p = p2;
        this.g = g2;
        this.l = l2;
    }

    public BigInteger getP() {
        return this.p;
    }

    public BigInteger getG() {
        return this.g;
    }

    public int getL() {
        return this.l;
    }
}
