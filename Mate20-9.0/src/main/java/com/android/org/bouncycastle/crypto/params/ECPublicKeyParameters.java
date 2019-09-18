package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.math.ec.ECPoint;

public class ECPublicKeyParameters extends ECKeyParameters {
    private final ECPoint Q;

    public ECPublicKeyParameters(ECPoint Q2, ECDomainParameters params) {
        super(false, params);
        this.Q = validate(Q2);
    }

    private ECPoint validate(ECPoint q) {
        if (q == null) {
            throw new IllegalArgumentException("point has null value");
        } else if (!q.isInfinity()) {
            ECPoint q2 = q.normalize();
            if (q2.isValid()) {
                return q2;
            }
            throw new IllegalArgumentException("point not on curve");
        } else {
            throw new IllegalArgumentException("point at infinity");
        }
    }

    public ECPoint getQ() {
        return this.Q;
    }
}
