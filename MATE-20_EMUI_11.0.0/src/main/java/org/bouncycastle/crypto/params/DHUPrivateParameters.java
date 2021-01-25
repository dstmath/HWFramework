package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.CipherParameters;

public class DHUPrivateParameters implements CipherParameters {
    private DHPrivateKeyParameters ephemeralPrivateKey;
    private DHPublicKeyParameters ephemeralPublicKey;
    private DHPrivateKeyParameters staticPrivateKey;

    public DHUPrivateParameters(DHPrivateKeyParameters dHPrivateKeyParameters, DHPrivateKeyParameters dHPrivateKeyParameters2) {
        this(dHPrivateKeyParameters, dHPrivateKeyParameters2, null);
    }

    public DHUPrivateParameters(DHPrivateKeyParameters dHPrivateKeyParameters, DHPrivateKeyParameters dHPrivateKeyParameters2, DHPublicKeyParameters dHPublicKeyParameters) {
        if (dHPrivateKeyParameters == null) {
            throw new NullPointerException("staticPrivateKey cannot be null");
        } else if (dHPrivateKeyParameters2 != null) {
            DHParameters parameters = dHPrivateKeyParameters.getParameters();
            if (parameters.equals(dHPrivateKeyParameters2.getParameters())) {
                if (dHPublicKeyParameters == null) {
                    dHPublicKeyParameters = new DHPublicKeyParameters(parameters.getG().modPow(dHPrivateKeyParameters2.getX(), parameters.getP()), parameters);
                } else if (!parameters.equals(dHPublicKeyParameters.getParameters())) {
                    throw new IllegalArgumentException("ephemeral public key has different domain parameters");
                }
                this.staticPrivateKey = dHPrivateKeyParameters;
                this.ephemeralPrivateKey = dHPrivateKeyParameters2;
                this.ephemeralPublicKey = dHPublicKeyParameters;
                return;
            }
            throw new IllegalArgumentException("static and ephemeral private keys have different domain parameters");
        } else {
            throw new NullPointerException("ephemeralPrivateKey cannot be null");
        }
    }

    public DHPrivateKeyParameters getEphemeralPrivateKey() {
        return this.ephemeralPrivateKey;
    }

    public DHPublicKeyParameters getEphemeralPublicKey() {
        return this.ephemeralPublicKey;
    }

    public DHPrivateKeyParameters getStaticPrivateKey() {
        return this.staticPrivateKey;
    }
}
