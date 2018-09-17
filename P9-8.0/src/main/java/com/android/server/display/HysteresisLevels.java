package com.android.server.display;

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
        return lux * (1.0f + getReferenceLevel(lux, this.mBrightLevels));
    }

    public float getDarkeningThreshold(float lux) {
        return lux * (1.0f - getReferenceLevel(lux, this.mDarkLevels));
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
}
