package javax.crypto.interfaces;

import java.math.BigInteger;
import java.security.PrivateKey;

public interface DHPrivateKey extends DHKey, PrivateKey {
    public static final long serialVersionUID = 2211791113380396553L;

    BigInteger getX();
}
