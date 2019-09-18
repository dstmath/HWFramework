package org.bouncycastle.jce.interfaces;

import java.math.BigInteger;
import java.security.PrivateKey;

public interface ECPrivateKey extends ECKey, PrivateKey {
    BigInteger getD();
}
