package com.android.org.bouncycastle.jce.spec;

import com.android.org.bouncycastle.math.ec.ECPoint;

public class ECPublicKeySpec extends ECKeySpec {
    private ECPoint q;

    public ECPublicKeySpec(ECPoint q, ECParameterSpec spec) {
        super(spec);
        if (q.getCurve() != null) {
            this.q = q.normalize();
        } else {
            this.q = q;
        }
    }

    public ECPoint getQ() {
        return this.q;
    }
}
