package java.security.spec;

import java.math.BigInteger;

public class RSAPrivateKeySpec implements KeySpec {
    private BigInteger modulus;
    private BigInteger privateExponent;

    public RSAPrivateKeySpec(BigInteger modulus, BigInteger privateExponent) {
        this.modulus = modulus;
        this.privateExponent = privateExponent;
    }

    public BigInteger getModulus() {
        return this.modulus;
    }

    public BigInteger getPrivateExponent() {
        return this.privateExponent;
    }
}
