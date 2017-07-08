package com.android.org.bouncycastle.math.ec;

public class FixedPointPreCompInfo implements PreCompInfo {
    protected ECPoint[] preComp;
    protected int width;

    public FixedPointPreCompInfo() {
        this.preComp = null;
        this.width = -1;
    }

    public ECPoint[] getPreComp() {
        return this.preComp;
    }

    public void setPreComp(ECPoint[] preComp) {
        this.preComp = preComp;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
