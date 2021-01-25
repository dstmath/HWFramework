package org.bouncycastle.crypto;

import java.math.BigInteger;

public interface DSAExt extends DSA {
    BigInteger getOrder();
}
