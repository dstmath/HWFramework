package org.bouncycastle.crypto.ec;

import java.math.BigInteger;

public interface ECPairFactorTransform extends ECPairTransform {
    BigInteger getTransformValue();
}
