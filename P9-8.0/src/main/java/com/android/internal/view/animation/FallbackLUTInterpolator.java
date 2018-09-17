package com.android.internal.view.animation;

import android.animation.TimeInterpolator;
import android.util.TimeUtils;
import android.view.Choreographer;

@HasNativeInterpolator
public class FallbackLUTInterpolator implements NativeInterpolatorFactory, TimeInterpolator {
    private static final int MAX_SAMPLE_POINTS = 300;
    private final float[] mLut;
    private TimeInterpolator mSourceInterpolator;

    public FallbackLUTInterpolator(TimeInterpolator interpolator, long duration) {
        this.mSourceInterpolator = interpolator;
        this.mLut = createLUT(interpolator, duration);
    }

    private static float[] createLUT(TimeInterpolator interpolator, long duration) {
        int numAnimFrames = Math.min(Math.max(2, (int) Math.ceil(((double) duration) / ((double) ((int) (Choreographer.getInstance().getFrameIntervalNanos() / TimeUtils.NANOS_PER_MS))))), 300);
        float[] values = new float[numAnimFrames];
        float lastFrame = (float) (numAnimFrames - 1);
        for (int i = 0; i < numAnimFrames; i++) {
            values[i] = interpolator.getInterpolation(((float) i) / lastFrame);
        }
        return values;
    }

    public long createNativeInterpolator() {
        return NativeInterpolatorFactoryHelper.createLutInterpolator(this.mLut);
    }

    public static long createNativeInterpolator(TimeInterpolator interpolator, long duration) {
        return NativeInterpolatorFactoryHelper.createLutInterpolator(createLUT(interpolator, duration));
    }

    public float getInterpolation(float input) {
        return this.mSourceInterpolator.getInterpolation(input);
    }
}
