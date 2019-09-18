package org.bouncycastle.crypto.params;

import org.bouncycastle.math.ec.ECPoint;

public class ECPublicKeyParameters extends ECKeyParameters {
    private final ECPoint Q;

    public ECPublicKeyParameters(ECPoint eCPoint, ECDomainParameters eCDomainParameters) {
        super(false, eCDomainParameters);
        this.Q = ECDomainParameters.validate(eCDomainParameters.getCurve(), eCPoint);
    }

    public ECPoint getQ() {
        return this.Q;
    }
}
