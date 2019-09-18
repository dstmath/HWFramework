package com.android.org.bouncycastle.math.ec;

public class WNafPreCompInfo implements PreCompInfo {
    protected ECPoint[] preComp = null;
    protected ECPoint[] preCompNeg = null;
    protected ECPoint twice = null;

    public ECPoint[] getPreComp() {
        return this.preComp;
    }

    public void setPreComp(ECPoint[] preComp2) {
        this.preComp = preComp2;
    }

    public ECPoint[] getPreCompNeg() {
        return this.preCompNeg;
    }

    public void setPreCompNeg(ECPoint[] preCompNeg2) {
        this.preCompNeg = preCompNeg2;
    }

    public ECPoint getTwice() {
        return this.twice;
    }

    public void setTwice(ECPoint twice2) {
        this.twice = twice2;
    }
}
