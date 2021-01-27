package com.huawei.agpengine.util;

import com.huawei.agpengine.math.Vector3;

public class BoundingBox {
    private Vector3 mMax;
    private Vector3 mMin;

    public BoundingBox(Vector3 min, Vector3 max) {
        setAabbMinMax(min, max);
    }

    public BoundingBox() {
        this(Vector3.ZERO, Vector3.ZERO);
    }

    public final void setAabbMinMax(Vector3 min, Vector3 max) {
        if (min == null || max == null) {
            throw new NullPointerException("Arguments must not be null.");
        }
        this.mMin = min;
        this.mMax = max;
    }

    public Vector3 getAabbMin() {
        return this.mMin;
    }

    public Vector3 getAabbMax() {
        return this.mMax;
    }
}
