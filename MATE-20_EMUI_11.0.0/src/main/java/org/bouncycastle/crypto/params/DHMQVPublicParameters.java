package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.CipherParameters;

public class DHMQVPublicParameters implements CipherParameters {
    private DHPublicKeyParameters ephemeralPublicKey;
    private DHPublicKeyParameters staticPublicKey;

    public DHMQVPublicParameters(DHPublicKeyParameters dHPublicKeyParameters, DHPublicKeyParameters dHPublicKeyParameters2) {
        if (dHPublicKeyParameters == null) {
            throw new NullPointerException("staticPublicKey cannot be null");
        } else if (dHPublicKeyParameters2 == null) {
            throw new NullPointerException("ephemeralPublicKey cannot be null");
        } else if (dHPublicKeyParameters.getParameters().equals(dHPublicKeyParameters2.getParameters())) {
            this.staticPublicKey = dHPublicKeyParameters;
            this.ephemeralPublicKey = dHPublicKeyParameters2;
        } else {
            throw new IllegalArgumentException("Static and ephemeral public keys have different domain parameters");
        }
    }

    public DHPublicKeyParameters getEphemeralPublicKey() {
        return this.ephemeralPublicKey;
    }

    public DHPublicKeyParameters getStaticPublicKey() {
        return this.staticPublicKey;
    }
}
