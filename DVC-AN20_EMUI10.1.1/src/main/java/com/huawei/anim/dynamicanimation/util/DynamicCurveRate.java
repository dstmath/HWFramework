package com.huawei.anim.dynamicanimation.util;

public class DynamicCurveRate implements IFollowHandRate {
    private static final float DEFAULT_K = 1.848f;
    private static final String TAG = DynamicCurveRate.class.getSimpleName();
    private float k;
    private float maxDeltaX;
    private float maximumX;

    public DynamicCurveRate(float maxDeltaX2, float k2) {
        this.maximumX = 0.75f;
        this.maxDeltaX = maxDeltaX2;
        this.k = k2;
    }

    public DynamicCurveRate(float maxDeltaX2) {
        this(maxDeltaX2, DEFAULT_K);
    }

    @Override // com.huawei.anim.dynamicanimation.util.IFollowHandRate
    public float getRate(float input) {
        if (input >= 0.0f) {
            float coeff = input / this.maxDeltaX;
            float coeff2 = 1.0f;
            if (coeff <= 1.0f) {
                coeff2 = coeff;
            }
            float x = this.maximumX * coeff2;
            float rate = (float) Math.exp(-((double) (this.k * x)));
            String str = TAG;
            LogX.d(str, "getRate: x=" + x + ",rate=" + rate + ",input=" + input);
            return rate;
        }
        throw new IllegalArgumentException("input can not less than zero!!");
    }

    public DynamicCurveRate setMaxDeltaX(float maxDeltaX2) {
        this.maxDeltaX = maxDeltaX2;
        return this;
    }

    public DynamicCurveRate setK(float k2) {
        this.k = k2;
        return this;
    }
}
