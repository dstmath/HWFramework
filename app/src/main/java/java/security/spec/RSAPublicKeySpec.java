package java.security.spec;

import java.math.BigInteger;

public class RSAPublicKeySpec implements KeySpec {
    private BigInteger modulus;
    private BigInteger publicExponent;

    public RSAPublicKeySpec(BigInteger modulus, BigInteger publicExponent) {
        this.modulus = modulus;
        this.publicExponent = publicExponent;
    }

    public BigInteger getModulus() {
        return this.modulus;
    }

    public BigInteger getPublicExponent() {
        return this.publicExponent;
    }
}
