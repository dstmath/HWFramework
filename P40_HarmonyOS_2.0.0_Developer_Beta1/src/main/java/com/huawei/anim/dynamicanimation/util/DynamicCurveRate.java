package com.huawei.anim.dynamicanimation.util;

import android.util.Log;

public class DynamicCurveRate implements FollowHandRate {
    private static final String a = "DynamicCurveRate";
    private static final float b = 1.848f;
    private static final float c = 0.75f;
    private float d;
    private float e;
    private float f;

    public DynamicCurveRate(float f2, float f3) {
        this.f = 0.75f;
        this.d = f2;
        this.e = f3;
    }

    public DynamicCurveRate(float f2) {
        this(f2, b);
    }

    @Override // com.huawei.anim.dynamicanimation.util.FollowHandRate
    public float getRate(float f2) {
        if (f2 >= 0.0f) {
            float f3 = f2 / this.d;
            if (f3 > 1.0f) {
                f3 = 1.0f;
            }
            float f4 = f3 * this.f;
            float exp = (float) Math.exp(-((double) (this.e * f4)));
            Log.i(a, "getRate: x=" + f4 + ",rate=" + exp + ",input=" + f2);
            return exp;
        }
        throw new IllegalArgumentException("input can not less than zero!!");
    }

    public DynamicCurveRate setmMaxDeltaX(float f2) {
        this.d = f2;
        return this;
    }

    public DynamicCurveRate setK(float f2) {
        this.e = f2;
        return this;
    }
}
