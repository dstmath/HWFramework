package com.huawei.hwtransition.interpolator;

import android.animation.TimeInterpolator;

public class ZInterpolator implements TimeInterpolator {
    private float focalLength;

    public ZInterpolator(float foc) {
        this.focalLength = foc;
    }

    public float getInterpolation(float input) {
        return (1.0f - (this.focalLength / (this.focalLength + input))) / (1.0f - (this.focalLength / (this.focalLength + 1.0f)));
    }
}
