package org.bouncycastle.math.ec;

import java.math.BigInteger;

class ZTauElement {
    public final BigInteger u;
    public final BigInteger v;

    public ZTauElement(BigInteger bigInteger, BigInteger bigInteger2) {
        this.u = bigInteger;
        this.v = bigInteger2;
    }
}
