package com.android.org.bouncycastle.math.ec;

public class WNafPreCompInfo implements PreCompInfo {
    protected ECPoint[] preComp = null;
    protected ECPoint[] preCompNeg = null;
    protected ECPoint twice = null;

    public ECPoint[] getPreComp() {
        return this.preComp;
    }

    public void setPreComp(ECPoint[] preComp) {
        this.preComp = preComp;
    }

    public ECPoint[] getPreCompNeg() {
        return this.preCompNeg;
    }

    public void setPreCompNeg(ECPoint[] preCompNeg) {
        this.preCompNeg = preCompNeg;
    }

    public ECPoint getTwice() {
        return this.twice;
    }

    public void setTwice(ECPoint twice) {
        this.twice = twice;
    }
}
