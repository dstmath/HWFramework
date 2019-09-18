package java.security.spec;

import java.math.BigInteger;

public class RSAPublicKeySpec implements KeySpec {
    private BigInteger modulus;
    private BigInteger publicExponent;

    public RSAPublicKeySpec(BigInteger modulus2, BigInteger publicExponent2) {
        this.modulus = modulus2;
        this.publicExponent = publicExponent2;
    }

    public BigInteger getModulus() {
        return this.modulus;
    }

    public BigInteger getPublicExponent() {
        return this.publicExponent;
    }
}
