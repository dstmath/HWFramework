package org.bouncycastle.crypto;

public interface DigestDerivationFunction extends DerivationFunction {
    Digest getDigest();
}
