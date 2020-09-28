package com.huawei.displayengine;

import android.os.Bundle;

public class HbmSceneFilter {
    private static final int BL_PRECISION_HIGH = 10000;
    private static final int BL_PRECISION_LOW = 255;
    private static final int BYTE_MASK = 255;
    private static final float FLOAT_MIN_VALUE = 1.0E-6f;
    private static final int HBM_INFO_DIMMING_SUPPORT = 0;
    private static final int HBM_INFO_DIMMING_THRESHOULD = 2;
    private static final int HBM_INFO_THRESHOULD = 1;
    private static final String TAG = "DE J HbmSceneFilter";
    private int mBacklightLevel = 0;
    private boolean mDimming = true;
    private int mDimmingThreshould = 3921;
    private DisplayEngineManager mDisplayEngineManager = new DisplayEngineManager();
    private float mDurationThreshould = 0.5f;
    private volatile boolean mInitialized = false;
    private final Object mLock = new Object();
    private float mSpeedThreshould = 0.0f;
    private boolean mSupport = false;
    private int mThreshould = 7254;

    private void initializeInternal() {
        if (!this.mInitialized) {
            byte[] info = new byte[3];
            if (this.mDisplayEngineManager.getEffect(25, 1, info, info.length) != 0) {
                DeLog.w(TAG, "Failed to get HBM information and use default value!");
            } else {
                boolean z = false;
                if (info[0] == 1) {
                    z = true;
                }
                this.mSupport = z;
                this.mThreshould = ((info[1] & 255) * 10000) / 255;
                this.mDimmingThreshould = ((info[2] & 255) * 10000) / 255;
                this.mSpeedThreshould = ((float) (this.mThreshould - this.mDimmingThreshould)) / this.mDurationThreshould;
                DeLog.i(TAG, "Get information from hal: mSupport=" + this.mSupport + " mThreshould=" + this.mThreshould + " mDimmingThreshould=" + this.mDimmingThreshould + " mSpeedThreshould=" + this.mSpeedThreshould);
            }
            this.mInitialized = true;
        }
    }

    private void initialize() {
        if (!this.mInitialized) {
            synchronized (this.mLock) {
                initializeInternal();
            }
        }
    }

    public boolean check(int scene, int action) {
        initialize();
        if (scene != 26 || !this.mSupport) {
            return false;
        }
        this.mBacklightLevel = action >> 16;
        return false;
    }

    private void disableDimmingIfNecessary(int target, int rate, float duration) {
        if (this.mBacklightLevel >= this.mThreshould) {
            boolean dimming = true;
            if (rate == 0 || Math.abs(duration) < FLOAT_MIN_VALUE) {
                dimming = false;
            } else if (target < this.mDimmingThreshould) {
                float speed = ((float) (this.mBacklightLevel - target)) / duration;
                DeLog.i(TAG, "hbm_dimming check speed=" + speed + " mSpeedThreshould=" + this.mSpeedThreshould);
                if (speed > this.mSpeedThreshould) {
                    dimming = false;
                }
            }
            if (this.mDimming && !dimming) {
                DeLog.i(TAG, "hbm_dimming off: target=" + target + " rate=" + rate + " duration=" + duration + " backlight10000=" + this.mBacklightLevel);
                this.mDisplayEngineManager.setScene(28, 17);
                this.mDimming = false;
            }
        }
    }

    public int setData(Bundle data) {
        if (data == null) {
            DeLog.e(TAG, "Invalid input: data is null!");
            return -1;
        }
        initialize();
        if (!this.mSupport) {
            return 0;
        }
        int target = data.getInt("target");
        int rate = data.getInt("rate");
        float duration = data.getFloat("duration");
        DeLog.d(TAG, "hbm_dimming: target=" + target + " rate=" + rate + " duration=" + duration + " backlight10000=" + this.mBacklightLevel);
        if (target < this.mThreshould) {
            disableDimmingIfNecessary(target, rate, duration);
            return 0;
        } else if (this.mDimming) {
            return 0;
        } else {
            DeLog.i(TAG, "hbm_dimming on: target=" + target);
            this.mDisplayEngineManager.setScene(28, 16);
            this.mDimming = true;
            return 0;
        }
    }
}
