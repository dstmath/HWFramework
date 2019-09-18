package org.bouncycastle.crypto;

public interface MacDerivationFunction extends DerivationFunction {
    Mac getMac();
}
