package com.android.org.bouncycastle.math.ec;

import java.math.BigInteger;

class ZTauElement {
    public final BigInteger u;
    public final BigInteger v;

    public ZTauElement(BigInteger u2, BigInteger v2) {
        this.u = u2;
        this.v = v2;
    }
}
