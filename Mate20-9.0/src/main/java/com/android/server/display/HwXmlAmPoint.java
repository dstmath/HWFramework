package com.android.server.display;

public class HwXmlAmPoint {
    final float x;
    final float y;
    final float z;

    public HwXmlAmPoint(float inx, float iny, float inz) {
        this.x = inx;
        this.y = iny;
        this.z = inz;
    }

    public String toString() {
        return "Point(" + this.x + ", " + this.y + ", " + this.z + ")";
    }
}
