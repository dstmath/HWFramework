package com.huawei.hwtransition.interpolator;

import android.animation.TimeInterpolator;

public class SimplePendulumDamperInterpolator implements TimeInterpolator {
    private float mBaseNumber;
    private float mPeriod;
    private float mPowerIndex;

    public SimplePendulumDamperInterpolator() {
        this.mBaseNumber = 2.0f;
        this.mPowerIndex = -2.0f;
        this.mPeriod = 1.0f;
    }

    public SimplePendulumDamperInterpolator(float base, float power, float period) {
        this.mBaseNumber = base;
        this.mPowerIndex = power;
        this.mPeriod = period;
    }

    public float getInterpolation(float t) {
        return (float) (Math.pow((double) this.mBaseNumber, (double) (this.mPowerIndex * t)) * Math.sin(((((double) ((t - (this.mPeriod / 4.0f)) * 2.0f)) * 3.141592653589793d) / ((double) this.mPeriod)) + 4.71238898038469d));
    }
}
