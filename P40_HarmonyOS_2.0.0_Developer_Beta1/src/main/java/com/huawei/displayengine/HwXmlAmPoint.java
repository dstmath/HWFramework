package com.huawei.displayengine;

public class HwXmlAmPoint {
    public final float x;
    public final float y;
    public final float z;

    public HwXmlAmPoint(float x2, float y2, float z2) {
        this.x = x2;
        this.y = y2;
        this.z = z2;
    }

    public String toString() {
        return "Point(" + this.x + ", " + this.y + ", " + this.z + ")";
    }
}
