package java.security.interfaces;

import java.math.BigInteger;
import java.security.PublicKey;

public interface RSAPublicKey extends PublicKey, RSAKey {
    public static final long serialVersionUID = -8727434096241101194L;

    BigInteger getPublicExponent();
}
