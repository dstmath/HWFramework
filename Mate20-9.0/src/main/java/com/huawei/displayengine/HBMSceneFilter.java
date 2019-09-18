package com.huawei.displayengine;

import android.os.Bundle;
import android.rms.iaware.DataContract;

public class HBMSceneFilter {
    private static final String TAG = "DE J HBMSceneFilter";
    private int mBacklightLevel = 0;
    private boolean mDimming = true;
    private int mDimmingThreshould = 3921;
    private DisplayEngineManager mDisplayEngineManager = new DisplayEngineManager();
    private float mDurationThreshould = 0.5f;
    private volatile boolean mInitialized = false;
    private Object mLock = new Object();
    private float mSpeedThreshould = 0.0f;
    private boolean mSupport = false;
    private int mThreshould = 7254;

    private void initialize() {
        if (!this.mInitialized) {
            synchronized (this.mLock) {
                if (!this.mInitialized) {
                    byte[] info = new byte[3];
                    if (this.mDisplayEngineManager.getEffect(25, 1, info, info.length) != 0) {
                        DElog.w(TAG, "Failed to get HBM information and use default value!");
                    } else {
                        boolean z = false;
                        if (info[0] == 1) {
                            z = true;
                        }
                        this.mSupport = z;
                        this.mThreshould = ((info[1] & 255) * 10000) / 255;
                        this.mDimmingThreshould = ((info[2] & 255) * 10000) / 255;
                        this.mSpeedThreshould = ((float) (this.mThreshould - this.mDimmingThreshould)) / this.mDurationThreshould;
                        DElog.i(TAG, "Get information from hal: mSupport=" + this.mSupport + " mThreshould=" + this.mThreshould + " mDimmingThreshould=" + this.mDimmingThreshould + " mSpeedThreshould=" + this.mSpeedThreshould);
                    }
                    this.mInitialized = true;
                }
            }
        }
    }

    public boolean check(int scene, int action) {
        initialize();
        if (scene == 26 && this.mSupport) {
            this.mBacklightLevel = action >> 16;
        }
        return false;
    }

    public int setData(Bundle data) {
        initialize();
        if (this.mSupport) {
            int target = data.getInt("target");
            int rate = data.getInt("rate");
            float duration = data.getFloat(DataContract.DevStatusProperty.VIBRATOR_DURATION);
            DElog.d(TAG, "hbm_dimming: target=" + target + " rate=" + rate + " duration=" + duration + " backlight10000=" + this.mBacklightLevel);
            if (target >= this.mThreshould) {
                if (!this.mDimming) {
                    DElog.i(TAG, "hbm_dimming on: target=" + target);
                    this.mDisplayEngineManager.setScene(28, 16);
                    this.mDimming = true;
                }
            } else if (this.mBacklightLevel >= this.mThreshould) {
                boolean dimming = true;
                if (rate == 0 || ((double) Math.abs(duration)) < 1.0E-6d) {
                    dimming = false;
                } else if (target < this.mDimmingThreshould) {
                    float speed = ((float) (this.mBacklightLevel - target)) / duration;
                    DElog.i(TAG, "gz hbm_dimming check speed=" + speed + " mSpeedThreshould=" + this.mSpeedThreshould);
                    if (speed > this.mSpeedThreshould) {
                        dimming = false;
                    }
                }
                if (this.mDimming && !dimming) {
                    DElog.i(TAG, "gz hbm_dimming off: target=" + target + " rate=" + rate + " duration=" + duration + " backlight10000=" + this.mBacklightLevel);
                    this.mDisplayEngineManager.setScene(28, 17);
                    this.mDimming = false;
                }
            }
        }
        return 0;
    }
}
