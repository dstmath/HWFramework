package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.CipherParameters;

public class XDHUPublicParameters implements CipherParameters {
    private AsymmetricKeyParameter ephemeralPublicKey;
    private AsymmetricKeyParameter staticPublicKey;

    public XDHUPublicParameters(AsymmetricKeyParameter asymmetricKeyParameter, AsymmetricKeyParameter asymmetricKeyParameter2) {
        if (asymmetricKeyParameter == null) {
            throw new NullPointerException("staticPublicKey cannot be null");
        } else if (!(asymmetricKeyParameter instanceof X448PublicKeyParameters) && !(asymmetricKeyParameter instanceof X25519PublicKeyParameters)) {
            throw new IllegalArgumentException("only X25519 and X448 paramaters can be used");
        } else if (asymmetricKeyParameter2 == null) {
            throw new NullPointerException("ephemeralPublicKey cannot be null");
        } else if (asymmetricKeyParameter.getClass().isAssignableFrom(asymmetricKeyParameter2.getClass())) {
            this.staticPublicKey = asymmetricKeyParameter;
            this.ephemeralPublicKey = asymmetricKeyParameter2;
        } else {
            throw new IllegalArgumentException("static and ephemeral public keys have different domain parameters");
        }
    }

    public AsymmetricKeyParameter getEphemeralPublicKey() {
        return this.ephemeralPublicKey;
    }

    public AsymmetricKeyParameter getStaticPublicKey() {
        return this.staticPublicKey;
    }
}
