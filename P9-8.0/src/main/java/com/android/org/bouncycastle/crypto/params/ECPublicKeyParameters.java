package com.android.org.bouncycastle.crypto.params;

import com.android.org.bouncycastle.math.ec.ECPoint;

public class ECPublicKeyParameters extends ECKeyParameters {
    private final ECPoint Q;

    public ECPublicKeyParameters(ECPoint Q, ECDomainParameters params) {
        super(false, params);
        this.Q = validate(Q);
    }

    private ECPoint validate(ECPoint q) {
        if (q == null) {
            throw new IllegalArgumentException("point has null value");
        } else if (q.isInfinity()) {
            throw new IllegalArgumentException("point at infinity");
        } else {
            q = q.normalize();
            if (q.isValid()) {
                return q;
            }
            throw new IllegalArgumentException("point not on curve");
        }
    }

    public ECPoint getQ() {
        return this.Q;
    }
}
