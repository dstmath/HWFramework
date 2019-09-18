package org.bouncycastle.math.ec;

public class SimpleLookupTable implements ECLookupTable {
    private final ECPoint[] points;

    public SimpleLookupTable(ECPoint[] eCPointArr, int i, int i2) {
        this.points = copy(eCPointArr, i, i2);
    }

    private static ECPoint[] copy(ECPoint[] eCPointArr, int i, int i2) {
        ECPoint[] eCPointArr2 = new ECPoint[i2];
        for (int i3 = 0; i3 < i2; i3++) {
            eCPointArr2[i3] = eCPointArr[i + i3];
        }
        return eCPointArr2;
    }

    public int getSize() {
        return this.points.length;
    }

    public ECPoint lookup(int i) {
        return this.points[i];
    }
}
