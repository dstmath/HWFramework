package com.android.org.bouncycastle.math.ec;

public class FixedPointPreCompInfo implements PreCompInfo {
    protected ECPoint offset = null;
    protected ECPoint[] preComp = null;
    protected int width = -1;

    public ECPoint getOffset() {
        return this.offset;
    }

    public void setOffset(ECPoint offset2) {
        this.offset = offset2;
    }

    public ECPoint[] getPreComp() {
        return this.preComp;
    }

    public void setPreComp(ECPoint[] preComp2) {
        this.preComp = preComp2;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width2) {
        this.width = width2;
    }
}
