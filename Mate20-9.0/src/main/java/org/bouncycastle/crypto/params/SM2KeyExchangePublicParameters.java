package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.CipherParameters;

public class SM2KeyExchangePublicParameters implements CipherParameters {
    private final ECPublicKeyParameters ephemeralPublicKey;
    private final ECPublicKeyParameters staticPublicKey;

    public SM2KeyExchangePublicParameters(ECPublicKeyParameters eCPublicKeyParameters, ECPublicKeyParameters eCPublicKeyParameters2) {
        if (eCPublicKeyParameters == null) {
            throw new NullPointerException("staticPublicKey cannot be null");
        } else if (eCPublicKeyParameters2 == null) {
            throw new NullPointerException("ephemeralPublicKey cannot be null");
        } else if (eCPublicKeyParameters.getParameters().equals(eCPublicKeyParameters2.getParameters())) {
            this.staticPublicKey = eCPublicKeyParameters;
            this.ephemeralPublicKey = eCPublicKeyParameters2;
        } else {
            throw new IllegalArgumentException("Static and ephemeral public keys have different domain parameters");
        }
    }

    public ECPublicKeyParameters getEphemeralPublicKey() {
        return this.ephemeralPublicKey;
    }

    public ECPublicKeyParameters getStaticPublicKey() {
        return this.staticPublicKey;
    }
}
