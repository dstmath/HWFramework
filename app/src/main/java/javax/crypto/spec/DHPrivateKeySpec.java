package javax.crypto.spec;

import java.math.BigInteger;
import java.security.spec.KeySpec;

public class DHPrivateKeySpec implements KeySpec {
    private BigInteger g;
    private BigInteger p;
    private BigInteger x;

    public DHPrivateKeySpec(BigInteger x, BigInteger p, BigInteger g) {
        this.x = x;
        this.p = p;
        this.g = g;
    }

    public BigInteger getX() {
        return this.x;
    }

    public BigInteger getP() {
        return this.p;
    }

    public BigInteger getG() {
        return this.g;
    }
}
