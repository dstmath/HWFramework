package org.bouncycastle.math.ec;

public class FixedPointPreCompInfo implements PreCompInfo {
    protected ECLookupTable lookupTable = null;
    protected ECPoint offset = null;
    protected int width = -1;

    public ECLookupTable getLookupTable() {
        return this.lookupTable;
    }

    public ECPoint getOffset() {
        return this.offset;
    }

    public int getWidth() {
        return this.width;
    }

    public void setLookupTable(ECLookupTable eCLookupTable) {
        this.lookupTable = eCLookupTable;
    }

    public void setOffset(ECPoint eCPoint) {
        this.offset = eCPoint;
    }

    public void setWidth(int i) {
        this.width = i;
    }
}
