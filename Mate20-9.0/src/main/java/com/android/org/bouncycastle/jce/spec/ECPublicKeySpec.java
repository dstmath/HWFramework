package com.android.org.bouncycastle.jce.spec;

import com.android.org.bouncycastle.math.ec.ECPoint;

public class ECPublicKeySpec extends ECKeySpec {
    private ECPoint q;

    public ECPublicKeySpec(ECPoint q2, ECParameterSpec spec) {
        super(spec);
        if (q2.getCurve() != null) {
            this.q = q2.normalize();
        } else {
            this.q = q2;
        }
    }

    public ECPoint getQ() {
        return this.q;
    }
}
