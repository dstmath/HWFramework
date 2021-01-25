package com.huawei.uifirst.smartslide;

import android.content.Context;
import android.util.Log;
import android.view.animation.AnimationUtils;
import java.util.HashMap;

public class SmartSlideOverScroller {
    private static final String DECELERATION_TIME_CONSTANT = "decelerationTimeConstant";
    private static final float DECELERATION_TIME_CONSTANT_DEFAULT = -0.405f;
    private static final String DECELERATION_TIME_SLOPE = "decelerationTimeSlope";
    private static final float DECELERATION_TIME_SLOPE_DEFAULT = 0.528f;
    private static final float DEFAULT_LCD_LENGTH = 6.0f;
    private static final float DEFAULT_MULTIPLE_FLING_LENGTH_THRESHOLD = 125.0f;
    private static final float DEFAULT_MULTIPLE_FLING_TIME_THRESHOLD = 400.0f;
    private static final float DEFAULT_MULTIPLE_VELOCITY = 1.5f;
    private static final float DEFAULT_SCREEN_DISPLAY_HEIGHT = 1920.0f;
    private static final float DEFAULT_SCREEN_PPI = 402.0f;
    private static final float DELTA = 1.0E-7f;
    private static final String EXP_COEFFICIENT = "expCoefficient";
    private static final String EXP_COFFICIENT_SLOW_DOWN = "expCofficientSlowDown";
    private static final float FLING_LENGTH_FACTOR = 0.31095f;
    private static final int FLING_TIMES_BIG_THAN_NINE = 9;
    private static final int FLING_TIMES_INIT = 1;
    private static final int FLING_TIMES_UNDER_FIVE = 5;
    private static final String FLING_TIME_THRESHOLD = "flingTimeThreshold";
    private static final float INCH_TO_MM_FACTOR = 25.4f;
    private static final String IS_ENABLE = "isEnable";
    private static final int LAST_FLING_TIME_INIT = 0;
    private static final float LAST_VELOCITY_GAIN_INIT = 1.0f;
    private static final String LOG_TAG = "OverScrollerOptimization";
    private static final int MAX_FLING_VELOCITY_GAIN = 16;
    private static final float MAX_MULTIPLE_LENGTH = 150.0f;
    private static final int NB_SAMPLES_OPTIMIZATION = 600;
    private static final float SLOPE_PARAMETER_GET_DISTANCE_DEFAULT = 4.2f;
    private static final float SLOPE_PARAMETER_SLOW_DOWN_DEFAULT = 6.5f;
    private static final int SLOPE_PARAMETER_THRESHOLD = 2;
    private static final float SPLINE_DISTANCE_COMPLETE = 0.9f;
    private static final int UNIT_CONVERSION = 1000;
    private static final float VELOCITY_GAIN_FACTOR = 0.45f;
    private static final float VELOCITY_GAIN_UNDER_FIVE = 1.0f;
    private static final String VELOCITY_MULTIPLIER = "velocityMultiplier";
    private static boolean isApplicationEnable = true;
    private static float slopeParameterGetDistance;
    private static float slopeParameterSlowDown;
    private HashMap<String, String> mConfigData;
    private float mDecelerationTimeConstantGetTime;
    private float mDecelerationTimeSlopeGetTime;
    private long mLastFlingTimeSave = 0;
    private double mLastVelocityGainSave;
    private float mLcdLength;
    private float mMultipleFlingLengthThreshold;
    private float mMultipleFlingTimeThreshold;
    private int mMultipleFlingTimes = 0;
    private float mMultipleVelocity;
    private float mScreenDisplayHeightPixels;
    private float mScreenPpi;
    private SmartSlideOverScrollerConfig mSmartSlideOverScrollerConfig;

    public SmartSlideOverScroller(Context context) {
        Log.i(LOG_TAG, "start init SmartSlideOverScroller and get the overscroller config");
        if (this.mSmartSlideOverScrollerConfig == null) {
            this.mSmartSlideOverScrollerConfig = new SmartSlideOverScrollerConfig(context);
        }
        this.mConfigData = new HashMap<>();
        initOverScrollerConfig(context);
    }

    private void initOverScrollerConfig(Context context) {
        float f = SLOPE_PARAMETER_SLOW_DOWN_DEFAULT;
        float f2 = DEFAULT_MULTIPLE_FLING_TIME_THRESHOLD;
        float f3 = SLOPE_PARAMETER_GET_DISTANCE_DEFAULT;
        float f4 = DEFAULT_MULTIPLE_FLING_LENGTH_THRESHOLD;
        float f5 = DEFAULT_SCREEN_PPI;
        float f6 = DEFAULT_LCD_LENGTH;
        float f7 = DEFAULT_SCREEN_DISPLAY_HEIGHT;
        if (context != null) {
            this.mConfigData = this.mSmartSlideOverScrollerConfig.getOverScrollerConfig(context);
            this.mScreenDisplayHeightPixels = this.mSmartSlideOverScrollerConfig.getScreenHeight();
            float f8 = this.mScreenDisplayHeightPixels;
            if (f8 >= 0.0f) {
                f7 = f8;
            }
            this.mScreenDisplayHeightPixels = f7;
            this.mLcdLength = this.mSmartSlideOverScrollerConfig.getScreenSize();
            float f9 = this.mLcdLength;
            if (f9 > 0.0f) {
                f6 = f9;
            }
            this.mLcdLength = f6;
            this.mScreenPpi = this.mSmartSlideOverScrollerConfig.getScreenPpiByResources();
            float f10 = this.mScreenPpi;
            if (f10 > 0.0f) {
                f5 = f10;
            }
            this.mScreenPpi = f5;
            this.mMultipleFlingLengthThreshold = this.mSmartSlideOverScrollerConfig.getScreenPpiByResources() * FLING_LENGTH_FACTOR;
            if (!judgeMultipleFlingLengthThreshold()) {
                f4 = this.mMultipleFlingLengthThreshold;
            }
            this.mMultipleFlingLengthThreshold = f4;
            slopeParameterGetDistance = Float.parseFloat(this.mConfigData.get(EXP_COEFFICIENT));
            float f11 = slopeParameterGetDistance;
            boolean isSlowDownReasonable = false;
            if (f11 >= 2.1999998f && f11 <= 6.2f) {
                f3 = slopeParameterGetDistance;
            }
            slopeParameterGetDistance = f3;
            this.mDecelerationTimeSlopeGetTime = Float.parseFloat(this.mConfigData.get(DECELERATION_TIME_SLOPE));
            this.mDecelerationTimeConstantGetTime = Float.parseFloat(this.mConfigData.get(DECELERATION_TIME_CONSTANT));
            this.mMultipleFlingTimeThreshold = Float.parseFloat(this.mConfigData.get(FLING_TIME_THRESHOLD));
            float f12 = this.mMultipleFlingTimeThreshold;
            if (f12 > 0.0f) {
                f2 = f12;
            }
            this.mMultipleFlingTimeThreshold = f2;
            slopeParameterSlowDown = Float.parseFloat(this.mConfigData.get(EXP_COFFICIENT_SLOW_DOWN));
            float f13 = slopeParameterSlowDown;
            if (f13 >= 4.5f && f13 <= 8.5f) {
                isSlowDownReasonable = true;
            }
            if (isSlowDownReasonable) {
                f = slopeParameterSlowDown;
            }
            slopeParameterSlowDown = f;
            this.mMultipleVelocity = Float.parseFloat(this.mConfigData.get(VELOCITY_MULTIPLIER));
            float f14 = this.mMultipleVelocity;
            if (f14 < DEFAULT_MULTIPLE_VELOCITY) {
                f14 = 1.5f;
            }
            this.mMultipleVelocity = f14;
            isApplicationEnable = Boolean.parseBoolean(this.mConfigData.get(IS_ENABLE));
            return;
        }
        this.mScreenDisplayHeightPixels = DEFAULT_SCREEN_DISPLAY_HEIGHT;
        this.mLcdLength = DEFAULT_LCD_LENGTH;
        this.mScreenPpi = DEFAULT_SCREEN_PPI;
        this.mMultipleFlingLengthThreshold = DEFAULT_MULTIPLE_FLING_LENGTH_THRESHOLD;
        slopeParameterGetDistance = SLOPE_PARAMETER_GET_DISTANCE_DEFAULT;
        this.mDecelerationTimeSlopeGetTime = DECELERATION_TIME_SLOPE_DEFAULT;
        this.mDecelerationTimeConstantGetTime = DECELERATION_TIME_CONSTANT_DEFAULT;
        this.mMultipleFlingTimeThreshold = DEFAULT_MULTIPLE_FLING_TIME_THRESHOLD;
        slopeParameterSlowDown = SLOPE_PARAMETER_SLOW_DOWN_DEFAULT;
        this.mMultipleVelocity = DEFAULT_MULTIPLE_VELOCITY;
        isApplicationEnable = true;
    }

    public float getScreenPpi() {
        return this.mScreenPpi;
    }

    public boolean getAppEnable() {
        return isApplicationEnable;
    }

    public int fling(int velocityX, int velocityY, float oldVelocityX, float oldVelocityY, float slidingDistance) {
        long time = AnimationUtils.currentAnimationTimeMillis();
        int newVelocityY = velocityY;
        if (this.mLastFlingTimeSave == 0) {
            this.mMultipleFlingTimes = 1;
            this.mLastVelocityGainSave = 1.0d;
            this.mLastFlingTimeSave = time;
            return (int) (this.mMultipleVelocity * ((float) velocityY));
        }
        float delValueX = Math.abs(Math.signum((float) velocityX) - Math.signum(oldVelocityX));
        float delValueY = Math.abs(Math.signum((float) velocityY) - Math.signum(oldVelocityY));
        if (delValueX > DELTA || delValueY > DELTA) {
            this.mMultipleFlingTimes = 1;
            this.mLastFlingTimeSave = 0;
            this.mLastVelocityGainSave = 1.0d;
        }
        if (((float) Math.abs(time - this.mLastFlingTimeSave)) > this.mMultipleFlingTimeThreshold) {
            this.mMultipleFlingTimes = 1;
            this.mLastFlingTimeSave = 0;
            this.mLastVelocityGainSave = 1.0d;
        } else if (Math.abs(slidingDistance) >= this.mMultipleFlingLengthThreshold) {
            this.mMultipleFlingTimes++;
            this.mLastFlingTimeSave = time;
        }
        if (Math.signum((float) velocityX) == Math.signum(oldVelocityX)) {
            if (Math.signum((float) velocityY) == Math.signum(oldVelocityY)) {
                newVelocityY = calculateVelocityY(velocityY, slidingDistance);
            }
        }
        return (int) (this.mMultipleVelocity * ((float) newVelocityY));
    }

    private int calculateVelocityY(int velocityY, float slidingDistance) {
        int i = this.mMultipleFlingTimes;
        if (i < 5) {
            int newVelocityY = (int) (((double) velocityY) * 1.0d);
            this.mLastVelocityGainSave = 1.0d;
            return newVelocityY;
        } else if (i > 9) {
            double velocityGain = (double) ((float) (((double) ((Math.abs(slidingDistance) * ((float) (this.mMultipleFlingTimes - 1))) / this.mScreenDisplayHeightPixels)) + this.mLastVelocityGainSave));
            double velocityGain2 = 16.0d > velocityGain ? velocityGain : 16.0d;
            int newVelocityY2 = (int) (((double) velocityY) * velocityGain2);
            this.mLastVelocityGainSave = velocityGain2;
            return newVelocityY2;
        } else {
            double velocityGainMax = ((((double) ((i - 3) * (i + 2))) / 2.0d) * ((double) VELOCITY_GAIN_FACTOR)) + 1.0d;
            double velocityGain3 = (double) ((float) (((double) ((Math.abs(slidingDistance) * ((float) (this.mMultipleFlingTimes - 1))) / this.mScreenDisplayHeightPixels)) + this.mLastVelocityGainSave));
            double velocityGain4 = velocityGainMax > velocityGain3 ? velocityGain3 : velocityGainMax;
            int newVelocityY3 = (int) (((double) velocityY) * velocityGain4);
            this.mLastVelocityGainSave = velocityGain4;
            return newVelocityY3;
        }
    }

    public double getSplineFlingDistance(int velocity) {
        float f = slopeParameterGetDistance;
        return ((double) (((float) Math.abs(velocity)) / f)) * (1.0d - Math.exp(((double) (-f)) * ((double) getSplineFlingDuration(velocity))));
    }

    public double getDistanceDiff(int velocity) {
        float f = slopeParameterGetDistance;
        float f2 = slopeParameterGetDistance;
        return ((double) Math.signum((float) velocity)) * ((((double) (((float) Math.abs(velocity)) / f)) * (1.0d - Math.exp((double) ((-f) * ((float) getSplineFlingDuration(velocity)))))) - (((double) (((float) Math.abs(velocity)) / f2)) * (1.0d - Math.exp((double) ((-f2) * ((float) getSplineFlingDuration(velocity)))))));
    }

    public int getSplineFlingDuration(int velocity) {
        double flingTime = (((double) this.mDecelerationTimeSlopeGetTime) * Math.log((double) ((((float) Math.abs(velocity)) / this.mScreenPpi) * INCH_TO_MM_FACTOR))) + ((double) this.mDecelerationTimeConstantGetTime);
        if (flingTime < 0.0d) {
            flingTime = 1.0d;
        }
        return (int) (1000.0d * flingTime);
    }

    public double getUpdateDistance(long currentTime, int splineDuration, int splineDistance, double splineDistanceDiff) {
        float exp = ((float) Math.exp((double) (((-slopeParameterSlowDown) * 595.0f) / 600.0f))) + 1.0f;
        float distanceCoef = exp - ((float) Math.exp((double) ((-slopeParameterSlowDown) * (((float) currentTime) / ((float) splineDuration)))));
        double distance = ((double) (((float) splineDistance) * distanceCoef)) - (splineDistanceDiff / 600.0d);
        if (Math.abs(distance) < ((double) (((float) Math.abs(splineDistance)) * SPLINE_DISTANCE_COMPLETE))) {
            return distance;
        }
        if (distanceCoef >= 1.0f) {
            distanceCoef = 1.0f;
        }
        return (double) (((float) splineDistance) * distanceCoef);
    }

    public float getUpdateVelocity(long currentTime, int splineDuration, int velocity) {
        return ((float) velocity) * ((float) Math.exp((double) ((-slopeParameterSlowDown) * (((float) currentTime) / ((float) splineDuration)))));
    }

    public int getAdjustDuratuion(int adjustDistance, int splineDuration, int splineDistance, double splineDistanceDiff) {
        if (splineDistance == 0) {
            return 0;
        }
        double distanceCoef = (((double) adjustDistance) + (splineDistanceDiff / 600.0d)) / ((double) splineDistance);
        if (distanceCoef >= 1.0d) {
            distanceCoef = 1.0d;
        }
        return (int) ((Math.log(((double) (((float) Math.exp((double) (((-slopeParameterSlowDown) * 595.0f) / 600.0f))) + 1.0f)) - distanceCoef) / ((double) (-slopeParameterSlowDown))) * ((double) splineDuration));
    }

    private boolean judgeMultipleFlingLengthThreshold() {
        float f = this.mMultipleFlingLengthThreshold;
        if (f <= 0.0f || f >= MAX_MULTIPLE_LENGTH) {
            return true;
        }
        return false;
    }
}
