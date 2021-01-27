package com.huawei.uifirst.smartslide;

import android.os.SystemProperties;
import huawei.android.provider.HanziToPinyin;
import java.math.BigDecimal;

public class HighFreqSmartSlideModel {
    private static final float DEFAULT_VALUE_THRESHOLD = 0.75f;
    private static final float FRICTION_GAIN_BASE = 0.5f;
    private static final float FRICTION_GAIN_THRESHOLD = 0.1f;
    private static final float FRICTION_SCALE = -4.2f;
    private static final float INIT_VELOCITY_GAIN_BASE = 1.0f;
    private static final String TAG = "HighFreqSmartSlideModel";
    private static final float VELOCITY_THRESHOLD_MULTIPLIER = 62.5f;
    private float mDistanceThreshold = Float.MIN_VALUE;
    private float mEstimateDistance;
    private float mEstimateTime;
    private float mFriction = -2.1f;
    private float mFrictionGain = 0.5f;
    private float mHighVelocityFrictionGain = 0.5f;
    private float mHighVelocityInitVelocityGain = 1.0f;
    private float mHighVelocityLevel = 0.0f;
    private float mInitVelocity;
    private float mInitVelocityGain = 1.0f;
    private boolean mIsOptimizeEnable = false;
    private float mLowVelocityFrictionGain = 0.5f;
    private float mLowVelocityInitVelocityGain = 1.0f;
    private float mLowVelocityLevel = 0.0f;
    private float mMediumVelocityFrictionGain = 0.5f;
    private float mMediumVelocityInitVelocityGain = 1.0f;
    private float mMediumVelocityLevel = 0.0f;
    private float mSignum;
    private float mVelocityThreshold = Float.MIN_VALUE;

    public HighFreqSmartSlideModel() {
        int highScreenFreqLevel = SystemProperties.getInt("persist.sys.hw_screen_freq", 0);
        String modelParams = SystemProperties.get("persist.agp.slide_model_parms", "0 0.6 0.8 5000.0 0.6 0.8 15000.0 0.6 0.8");
        if (!(highScreenFreqLevel <= 0 || modelParams == null || modelParams.length() == 0)) {
            this.mIsOptimizeEnable = parseFlingModelParms(modelParams);
        }
        setThreshold(DEFAULT_VALUE_THRESHOLD);
    }

    public boolean isOptimizeEnable() {
        return this.mIsOptimizeEnable;
    }

    public int getInitVelocity() {
        return (int) (this.mSignum * this.mInitVelocity);
    }

    public double getSplineFlingDistance(int velocity) {
        initFlingModel((float) velocity);
        return new BigDecimal(String.valueOf(getEstimateDistance())).doubleValue();
    }

    public int getSplineFlingDuration(int velocity) {
        initFlingModel((float) velocity);
        return (int) getEstimateTime();
    }

    public double getUpdateDistance(long currentTime, int splineDuration, int splineDistance, double splineDistanceDiff) {
        if (splineDuration == 0) {
            return 0.0d;
        }
        double updateDistance = new BigDecimal(String.valueOf(getDistance(((getEstimateTime() / 1000.0f) * ((float) currentTime)) / ((float) splineDuration)))).doubleValue();
        if (Math.abs(updateDistance) < 1.0d) {
            return (double) (this.mSignum * 1.0f);
        }
        return updateDistance;
    }

    public float getUpdateVelocity(long currentTime, int splineDuration, int velocity) {
        if (splineDuration == 0) {
            return 0.0f;
        }
        initFlingModel((float) velocity);
        return getVelocity(((getEstimateTime() / 1000.0f) * ((float) currentTime)) / ((float) splineDuration));
    }

    public int getAdjustDuratuion(int adjustDistance, int splineDuration, int splineDistance, double splineDistanceDiff) {
        if (splineDistance == 0) {
            return 0;
        }
        return (int) getDuratuion((float) splineDuration);
    }

    private boolean parseFlingModelParms(String modelParams) {
        String[] arrs = modelParams.split(HanziToPinyin.Token.SEPARATOR);
        if (arrs.length == 9) {
            int schemeSwitch = Integer.parseInt(arrs[0]);
            this.mLowVelocityLevel = 0.0f;
            this.mLowVelocityFrictionGain = Float.parseFloat(arrs[1]);
            this.mLowVelocityInitVelocityGain = Float.parseFloat(arrs[2]);
            this.mMediumVelocityLevel = Float.parseFloat(arrs[3]);
            this.mMediumVelocityFrictionGain = Float.parseFloat(arrs[4]);
            this.mMediumVelocityInitVelocityGain = Float.parseFloat(arrs[5]);
            this.mHighVelocityLevel = Float.parseFloat(arrs[6]);
            this.mHighVelocityFrictionGain = Float.parseFloat(arrs[7]);
            this.mHighVelocityInitVelocityGain = Float.parseFloat(arrs[8]);
            if (schemeSwitch > 0) {
                return true;
            }
        }
        return false;
    }

    private void initFlingModel(float velocity) {
        adjustGainCeof(velocity);
        setInitVelocity(velocity);
    }

    private void adjustGainCeof(float velocity) {
        float velocityGain;
        float frictionGain;
        float velocityAbs = Math.abs(velocity);
        if (velocityAbs < this.mMediumVelocityLevel) {
            frictionGain = this.mLowVelocityFrictionGain;
            velocityGain = this.mLowVelocityInitVelocityGain;
        } else if (velocityAbs < this.mHighVelocityLevel) {
            frictionGain = this.mMediumVelocityFrictionGain;
            velocityGain = this.mMediumVelocityInitVelocityGain;
        } else {
            frictionGain = this.mHighVelocityFrictionGain;
            velocityGain = this.mHighVelocityInitVelocityGain;
        }
        setFriction(frictionGain);
        setInitVelocityGain(velocityGain);
    }

    private void setInitVelocity(float initVelocity) {
        this.mSignum = Math.signum(initVelocity);
        this.mInitVelocity = Math.abs(initVelocity) * this.mInitVelocityGain;
        float abs = Math.abs(this.mInitVelocity);
        float f = this.mVelocityThreshold;
        if (abs < f) {
            this.mInitVelocity = f;
        }
    }

    private void setFriction(float frictionGain) {
        if (frictionGain < FRICTION_GAIN_THRESHOLD) {
            frictionGain = 0.5f;
        }
        this.mFriction = FRICTION_SCALE * frictionGain;
        this.mFrictionGain = frictionGain;
    }

    private void setInitVelocityGain(float velocityGain) {
        this.mInitVelocityGain = velocityGain;
    }

    private void setThreshold(float value) {
        this.mDistanceThreshold = Math.abs(value);
        this.mVelocityThreshold = this.mDistanceThreshold * VELOCITY_THRESHOLD_MULTIPLIER;
    }

    private float getEstimateTime() {
        recalculateEstimateValue();
        return this.mEstimateTime;
    }

    private float getEstimateDistance() {
        recalculateEstimateValue();
        return this.mEstimateDistance;
    }

    private float getDistance(float deltaTime) {
        return this.mSignum * ((float) (new BigDecimal(String.valueOf(this.mInitVelocity / this.mFriction)).doubleValue() * (Math.exp(new BigDecimal(String.valueOf(this.mFriction * deltaTime)).doubleValue()) - 1.0d)));
    }

    private float getDuratuion(float distance) {
        return (float) (Math.log(new BigDecimal(String.valueOf((this.mFriction * distance) / this.mInitVelocity)).doubleValue() + 1.0d) / new BigDecimal(String.valueOf(this.mFriction)).doubleValue());
    }

    private float getVelocity(float deltaTime) {
        return this.mSignum * ((float) (new BigDecimal(String.valueOf(this.mInitVelocity)).doubleValue() * Math.exp(new BigDecimal(String.valueOf(this.mFriction * deltaTime)).doubleValue())));
    }

    private float getFriction() {
        return this.mFriction;
    }

    private float getInitVelocityGain() {
        return this.mInitVelocityGain;
    }

    private void recalculateEstimateValue() {
        this.mEstimateTime = ((float) (Math.log(new BigDecimal(String.valueOf(this.mVelocityThreshold / this.mInitVelocity)).doubleValue()) / new BigDecimal(String.valueOf(this.mFriction)).doubleValue())) * 1000.0f;
        this.mEstimateTime = Math.max(this.mEstimateTime, 0.0f);
        this.mEstimateDistance = Math.abs(getDistance(this.mEstimateTime / 1000.0f));
        this.mEstimateDistance = Math.max(this.mEstimateDistance, 1.0f);
    }
}
