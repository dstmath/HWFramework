package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.CipherParameters;

public class ECDHUPrivateParameters implements CipherParameters {
    private ECPrivateKeyParameters ephemeralPrivateKey;
    private ECPublicKeyParameters ephemeralPublicKey;
    private ECPrivateKeyParameters staticPrivateKey;

    public ECDHUPrivateParameters(ECPrivateKeyParameters eCPrivateKeyParameters, ECPrivateKeyParameters eCPrivateKeyParameters2) {
        this(eCPrivateKeyParameters, eCPrivateKeyParameters2, null);
    }

    public ECDHUPrivateParameters(ECPrivateKeyParameters eCPrivateKeyParameters, ECPrivateKeyParameters eCPrivateKeyParameters2, ECPublicKeyParameters eCPublicKeyParameters) {
        if (eCPrivateKeyParameters == null) {
            throw new NullPointerException("staticPrivateKey cannot be null");
        } else if (eCPrivateKeyParameters2 != null) {
            ECDomainParameters parameters = eCPrivateKeyParameters.getParameters();
            if (parameters.equals(eCPrivateKeyParameters2.getParameters())) {
                if (eCPublicKeyParameters == null) {
                    eCPublicKeyParameters = new ECPublicKeyParameters(parameters.getG().multiply(eCPrivateKeyParameters2.getD()), parameters);
                } else if (!parameters.equals(eCPublicKeyParameters.getParameters())) {
                    throw new IllegalArgumentException("ephemeral public key has different domain parameters");
                }
                this.staticPrivateKey = eCPrivateKeyParameters;
                this.ephemeralPrivateKey = eCPrivateKeyParameters2;
                this.ephemeralPublicKey = eCPublicKeyParameters;
                return;
            }
            throw new IllegalArgumentException("static and ephemeral private keys have different domain parameters");
        } else {
            throw new NullPointerException("ephemeralPrivateKey cannot be null");
        }
    }

    public ECPrivateKeyParameters getEphemeralPrivateKey() {
        return this.ephemeralPrivateKey;
    }

    public ECPublicKeyParameters getEphemeralPublicKey() {
        return this.ephemeralPublicKey;
    }

    public ECPrivateKeyParameters getStaticPrivateKey() {
        return this.staticPrivateKey;
    }
}
