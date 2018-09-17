package com.android.server.policy;

import android.view.animation.Interpolator;

public class LogDecelerateInterpolator implements Interpolator {
    private int mBase;
    private int mDrift;
    private final float mLogScale = (1.0f / computeLog(1.0f, this.mBase, this.mDrift));

    public LogDecelerateInterpolator(int base, int drift) {
        this.mBase = base;
        this.mDrift = drift;
    }

    private static float computeLog(float t, int base, int drift) {
        return (((float) (-Math.pow((double) base, (double) (-t)))) + 1.0f) + (((float) drift) * t);
    }

    public float getInterpolation(float t) {
        return computeLog(t, this.mBase, this.mDrift) * this.mLogScale;
    }
}
