package org.bouncycastle.crypto.params;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;

public class SM2KeyExchangePrivateParameters implements CipherParameters {
    private final ECPrivateKeyParameters ephemeralPrivateKey;
    private final ECPoint ephemeralPublicPoint;
    private final boolean initiator;
    private final ECPrivateKeyParameters staticPrivateKey;
    private final ECPoint staticPublicPoint;

    public SM2KeyExchangePrivateParameters(boolean z, ECPrivateKeyParameters eCPrivateKeyParameters, ECPrivateKeyParameters eCPrivateKeyParameters2) {
        if (eCPrivateKeyParameters == null) {
            throw new NullPointerException("staticPrivateKey cannot be null");
        } else if (eCPrivateKeyParameters2 != null) {
            ECDomainParameters parameters = eCPrivateKeyParameters.getParameters();
            if (parameters.equals(eCPrivateKeyParameters2.getParameters())) {
                FixedPointCombMultiplier fixedPointCombMultiplier = new FixedPointCombMultiplier();
                this.initiator = z;
                this.staticPrivateKey = eCPrivateKeyParameters;
                this.staticPublicPoint = fixedPointCombMultiplier.multiply(parameters.getG(), eCPrivateKeyParameters.getD()).normalize();
                this.ephemeralPrivateKey = eCPrivateKeyParameters2;
                this.ephemeralPublicPoint = fixedPointCombMultiplier.multiply(parameters.getG(), eCPrivateKeyParameters2.getD()).normalize();
                return;
            }
            throw new IllegalArgumentException("Static and ephemeral private keys have different domain parameters");
        } else {
            throw new NullPointerException("ephemeralPrivateKey cannot be null");
        }
    }

    public ECPrivateKeyParameters getEphemeralPrivateKey() {
        return this.ephemeralPrivateKey;
    }

    public ECPoint getEphemeralPublicPoint() {
        return this.ephemeralPublicPoint;
    }

    public ECPrivateKeyParameters getStaticPrivateKey() {
        return this.staticPrivateKey;
    }

    public ECPoint getStaticPublicPoint() {
        return this.staticPublicPoint;
    }

    public boolean isInitiator() {
        return this.initiator;
    }
}
