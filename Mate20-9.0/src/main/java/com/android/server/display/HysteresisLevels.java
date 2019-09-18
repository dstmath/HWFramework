package com.android.server.display;

import java.io.PrintWriter;
import java.util.Arrays;

public class HysteresisLevels {
    private static final boolean DEBUG = false;
    private static final float DEFAULT_BRIGHTENING_HYSTERESIS = 0.1f;
    private static final float DEFAULT_DARKENING_HYSTERESIS = 0.2f;
    private static final String TAG = "HysteresisLevels";
    private final float[] mBrightLevels;
    private final float[] mDarkLevels;
    private final float[] mLuxLevels;

    public HysteresisLevels(int[] brightLevels, int[] darkLevels, int[] luxLevels) {
        if (brightLevels.length == darkLevels.length && darkLevels.length == luxLevels.length + 1) {
            this.mBrightLevels = setArrayFormat(brightLevels, 1000.0f);
            this.mDarkLevels = setArrayFormat(darkLevels, 1000.0f);
            this.mLuxLevels = setArrayFormat(luxLevels, 1.0f);
            return;
        }
        throw new IllegalArgumentException("Mismatch between hysteresis array lengths.");
    }

    public float getBrighteningThreshold(float lux) {
        return (1.0f + getReferenceLevel(lux, this.mBrightLevels)) * lux;
    }

    public float getDarkeningThreshold(float lux) {
        return (1.0f - getReferenceLevel(lux, this.mDarkLevels)) * lux;
    }

    private float getReferenceLevel(float lux, float[] referenceLevels) {
        int index = 0;
        while (this.mLuxLevels.length > index && lux >= this.mLuxLevels[index]) {
            index++;
        }
        return referenceLevels[index];
    }

    private float[] setArrayFormat(int[] configArray, float divideFactor) {
        float[] levelArray = new float[configArray.length];
        for (int index = 0; levelArray.length > index; index++) {
            levelArray[index] = ((float) configArray[index]) / divideFactor;
        }
        return levelArray;
    }

    public void dump(PrintWriter pw) {
        pw.println(TAG);
        pw.println("  mBrightLevels=" + Arrays.toString(this.mBrightLevels));
        pw.println("  mDarkLevels=" + Arrays.toString(this.mDarkLevels));
        pw.println("  mLuxLevels=" + Arrays.toString(this.mLuxLevels));
    }
}
