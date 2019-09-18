package org.bouncycastle.jce.interfaces;

import java.math.BigInteger;
import javax.crypto.interfaces.DHPublicKey;

public interface ElGamalPublicKey extends ElGamalKey, DHPublicKey {
    BigInteger getY();
}
