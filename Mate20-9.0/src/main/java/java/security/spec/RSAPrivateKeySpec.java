package java.security.spec;

import java.math.BigInteger;

public class RSAPrivateKeySpec implements KeySpec {
    private BigInteger modulus;
    private BigInteger privateExponent;

    public RSAPrivateKeySpec(BigInteger modulus2, BigInteger privateExponent2) {
        this.modulus = modulus2;
        this.privateExponent = privateExponent2;
    }

    public BigInteger getModulus() {
        return this.modulus;
    }

    public BigInteger getPrivateExponent() {
        return this.privateExponent;
    }
}
