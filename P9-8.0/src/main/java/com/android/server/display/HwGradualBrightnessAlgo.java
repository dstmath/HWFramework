package com.android.server.display;

import android.animation.ValueAnimator;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwBrightnessXmlLoader.Data;

final class HwGradualBrightnessAlgo {
    private static final float CRITERION_TIME = 40.0f;
    private static final float DEFAULT_AMOUNT = 157.0f;
    private static final float FAST_TIME = 0.5f;
    private boolean DEBUG;
    private String TAG = "HwGradualBrightnessAlgo";
    private float mAnimatedStep;
    private boolean mAnimatedStepRoundEnabled;
    private float mAnimatedValue;
    private boolean mAnimationEnabled;
    private float mAnimationEqualRatioMa;
    private float mAnimationEqualRatioMb;
    private float mAnimationEqualRatioMq;
    private float mAnimationEqualRatioMq0;
    private float mAnimationEqualRatioMqDefault;
    public boolean mAutoBrightnessIntervened;
    public boolean mAutoBrightnessMode;
    private float mBrightenFixStepsThreshold;
    private float mBrightnessMax;
    private float mBrightnessMin;
    private boolean mCameraModeEnable;
    private boolean mCoverModeAnimationFast;
    private float mCoverModeAnimationTime;
    private int mCurrentValue;
    private float mDarkenFixStepsThreshold;
    private final Data mData;
    private float mDecreaseFixAmount;
    private float mDuration;
    private boolean mFirstRebootAnimationEnable;
    private boolean mFirstTimeCalculateAmount;
    public boolean mFirstValidAutoBrightness;
    private boolean mManualModeAnimationEnable;
    private boolean mManualPowerSavingAnimationEnable;
    private boolean mManualThermalModeAnimationEnable;
    private int mMillisecond;
    private boolean mOutdoorAnimationFlag;
    private boolean mPowerDimRecoveryState;
    private boolean mPowerDimState;
    private int mRate;
    private boolean mScreenLocked;
    private int mState;
    private float mStepAdjValue;
    private int mTargetValue;
    private boolean mfastAnimtionFlag;

    public HwGradualBrightnessAlgo(int deviceActualBrightnessLevel) {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(this.TAG, 4) : false : true;
        this.DEBUG = isLoggable;
        this.mAutoBrightnessIntervened = false;
        this.mFirstValidAutoBrightness = false;
        this.mFirstTimeCalculateAmount = false;
        this.mPowerDimState = false;
        this.mPowerDimRecoveryState = false;
        this.mAnimatedStepRoundEnabled = false;
        this.mDarkenFixStepsThreshold = 20.0f;
        this.mBrightenFixStepsThreshold = 2.0f;
        this.mDuration = 0.0f;
        this.mAnimatedStep = 1.0f;
        this.mStepAdjValue = 1.0f;
        this.mfastAnimtionFlag = false;
        this.mCoverModeAnimationFast = false;
        this.mCoverModeAnimationTime = 1.0f;
        this.mAnimationEqualRatioMqDefault = 0.99839747f;
        this.mBrightnessMin = 156.0f;
        this.mBrightnessMax = 10000.0f;
        this.mCameraModeEnable = false;
        this.mAnimationEnabled = false;
        this.mScreenLocked = false;
        this.mOutdoorAnimationFlag = false;
        this.mManualModeAnimationEnable = false;
        this.mManualPowerSavingAnimationEnable = false;
        this.mManualThermalModeAnimationEnable = false;
        this.mFirstRebootAnimationEnable = true;
        this.mData = HwBrightnessXmlLoader.getData(deviceActualBrightnessLevel);
        initAnimationEqualRatioPara();
        if (this.mData.rebootFirstBrightnessAnimationEnable) {
            this.mTargetValue = this.mData.rebootFirstBrightness;
            this.mCurrentValue = this.mData.rebootFirstBrightness;
            this.mAnimatedValue = (float) this.mData.rebootFirstBrightness;
        }
    }

    public void initAnimationEqualRatioPara() {
        this.mAnimationEqualRatioMa = (this.mData.screenBrightnessMaxNit - this.mData.screenBrightnessMinNit) / (this.mBrightnessMax - this.mBrightnessMin);
        this.mAnimationEqualRatioMb = this.mData.screenBrightnessMaxNit - (this.mBrightnessMax * this.mAnimationEqualRatioMa);
        float N_max = (this.mData.darkenGradualTimeMax * 60.0f) - 1.0f;
        if (Math.abs(N_max) < 1.0E-7f) {
            this.mAnimationEqualRatioMq0 = this.mAnimationEqualRatioMqDefault;
        } else {
            this.mAnimationEqualRatioMq0 = (float) Math.pow((double) (this.mData.screenBrightnessMinNit / this.mData.screenBrightnessMaxNit), (double) (1.0f / N_max));
        }
        if (this.DEBUG) {
            Slog.d(this.TAG, "Init AnimationEqualRatioPara: Ma=" + this.mAnimationEqualRatioMa + ",Mb=" + this.mAnimationEqualRatioMb + ",Nmax=" + N_max + ",Mq0=" + this.mAnimationEqualRatioMq0 + ",MaxNit=" + this.mData.screenBrightnessMaxNit + ",MinNit=" + this.mData.screenBrightnessMinNit);
        }
    }

    private float getAnimatedStepByEyeSensitiveCurve(float currentValue, float targetValue, float duration) {
        if (this.mData.animationEqualRatioEnable && currentValue > targetValue) {
            this.mAnimatedStep = getAnimatedStepByEqualRatio(currentValue, targetValue, duration);
            return this.mAnimatedStep;
        } else if (currentValue == 0.0f) {
            Slog.e(this.TAG, "currentValue is 0, set step to default value!");
            return DEFAULT_AMOUNT;
        } else {
            if (duration <= 0.116f && currentValue > targetValue) {
                Slog.e(this.TAG, "duration is not valid, set to 3.0!");
                duration = 3.0f;
            }
            if (this.mFirstTimeCalculateAmount) {
                float avgTime;
                float avgPara = ((((float) Math.pow((double) (targetValue / 10000.0f), 0.09000000357627869d)) * 0.0029f) * CRITERION_TIME) / duration;
                if (currentValue > targetValue) {
                    avgTime = ((float) (Math.log((double) (targetValue / currentValue)) / Math.log((double) (1.0f - avgPara)))) * 0.016540745f;
                    this.mStepAdjValue = avgTime < this.mData.darkenGradualTimeMin ? avgTime / this.mData.darkenGradualTimeMin : 1.0f;
                } else {
                    avgTime = ((float) (Math.log((double) (targetValue / currentValue)) / Math.log((double) (1.0f + avgPara)))) * 0.016540745f;
                    this.mStepAdjValue = avgTime < duration ? avgTime / duration : 1.0f;
                }
                if (this.DEBUG) {
                    Slog.d(this.TAG, "getAnimatedStep avgTime= " + avgTime + ",avgPara" + avgPara + ",mStepAdjValue=" + this.mStepAdjValue + ",duration=" + duration);
                }
            }
            this.mAnimatedStep = 0.0029f * currentValue;
            this.mAnimatedStep *= (float) Math.pow((double) (targetValue / 10000.0f), 0.09000000357627869d);
            if (duration >= 20.0f && duration < 30.0f) {
                duration += 1.0f;
            }
            this.mAnimatedStep = ((this.mAnimatedStep * this.mStepAdjValue) * CRITERION_TIME) / duration;
            this.mAnimatedStep = getMinAnimatedStepByEyeSensitiveCurve(this.mAnimatedStep);
            return this.mAnimatedStep;
        }
    }

    private float getAnimatedStepByEqualRatio(float currentValue, float targetValue, float duration) {
        if ((this.mAnimationEqualRatioMa * targetValue) + this.mAnimationEqualRatioMb < 1.0E-12f || (this.mAnimationEqualRatioMa * currentValue) + this.mAnimationEqualRatioMb < 1.0E-12f || this.mAnimationEqualRatioMq0 < 1.0E-12f) {
            Slog.e(this.TAG, "Error: the screen brightness is minus");
            return DEFAULT_AMOUNT;
        }
        if (this.mFirstTimeCalculateAmount) {
            float avgTime = ((float) ((Math.log((double) (((this.mAnimationEqualRatioMa * targetValue) + this.mAnimationEqualRatioMb) / ((this.mAnimationEqualRatioMa * currentValue) + this.mAnimationEqualRatioMb))) / Math.log((double) this.mAnimationEqualRatioMq0)) + 1.0d)) / 60.0f;
            if (avgTime < this.mData.darkenGradualTimeMin) {
                this.mAnimationEqualRatioMq = (float) Math.pow((double) (((this.mAnimationEqualRatioMa * targetValue) + this.mAnimationEqualRatioMb) / ((this.mAnimationEqualRatioMa * currentValue) + this.mAnimationEqualRatioMb)), (double) (1.0f / ((this.mData.darkenGradualTimeMin * 60.0f) - 1.0f)));
            } else {
                this.mAnimationEqualRatioMq = this.mAnimationEqualRatioMq0;
            }
            if (this.DEBUG) {
                Slog.i(this.TAG, "avgTime=" + avgTime + ",Mq=" + this.mAnimationEqualRatioMq + ",Ma=" + this.mAnimationEqualRatioMa + ",Mb=" + this.mAnimationEqualRatioMb + ",mAnimatedValue=" + this.mAnimatedValue);
            }
        }
        if (currentValue > targetValue) {
            this.mAnimatedStep = ((1.0f - this.mAnimationEqualRatioMq) * this.mAnimatedValue) + (((1.0f - this.mAnimationEqualRatioMq) * this.mAnimationEqualRatioMb) / this.mAnimationEqualRatioMa);
        }
        if (this.mAnimatedStep < 1.0E-12f) {
            Slog.e(this.TAG, "Error: the animate step is invalid,mAnimatedStep=157.0");
            this.mAnimatedStep = DEFAULT_AMOUNT;
        }
        return this.mAnimatedStep;
    }

    private float getMinAnimatedStepByEyeSensitiveCurve(float animatedStep) {
        float minAnimatedStep = animatedStep;
        if (animatedStep >= 1.0f && this.mAnimatedStepRoundEnabled) {
            return (float) Math.round(animatedStep);
        }
        if (animatedStep < 1.0f && animatedStep >= 0.5f && this.mStepAdjValue == 1.0f) {
            return 0.5f;
        }
        if (animatedStep >= 0.5f || this.mStepAdjValue != 1.0f) {
            return minAnimatedStep;
        }
        return 0.25f;
    }

    public float getAnimatedValue() {
        if (ValueAnimator.getDurationScale() == 0.0f || this.mRate == 0) {
            this.mAnimatedValue = (float) this.mTargetValue;
        } else {
            float amount;
            if (this.mAutoBrightnessMode) {
                amount = getAutoModeAnimtionAmount();
            } else {
                amount = getManualModeAnimtionAmount();
            }
            if (this.mTargetValue > this.mCurrentValue) {
                this.mAnimatedValue = this.mAnimatedValue + amount < ((float) this.mTargetValue) ? this.mAnimatedValue + amount : (float) this.mTargetValue;
            } else {
                this.mAnimatedValue = this.mAnimatedValue - amount > ((float) this.mTargetValue) ? this.mAnimatedValue - amount : (float) this.mTargetValue;
            }
        }
        return this.mAnimatedValue;
    }

    public float getAutoModeAnimtionAmount() {
        if (!this.mFirstTimeCalculateAmount) {
            return getAmount();
        }
        float duration;
        float amount;
        if (this.mFirstRebootAnimationEnable && this.mData.rebootFirstBrightnessAnimationEnable) {
            duration = this.mData.rebootFirstBrightnessAutoTime;
            this.mFirstRebootAnimationEnable = false;
            Slog.i(this.TAG, "The mFirstRebootAnimationEnable state,duration=" + duration);
        } else if (this.mAnimationEnabled) {
            duration = ((float) this.mMillisecond) / 1000.0f;
            if (this.DEBUG) {
                Slog.d(this.TAG, "The mAnimationEnabled state,duration=" + duration);
            }
        } else if (this.mCoverModeAnimationFast) {
            duration = this.mCoverModeAnimationTime;
            if (this.DEBUG) {
                Slog.i(this.TAG, "LabcCoverMode mCoverModeFast=" + this.mCoverModeAnimationFast);
            }
        } else if (this.mData.autoPowerSavingUseManualAnimationTimeEnable && this.mManualPowerSavingAnimationEnable) {
            if (this.mTargetValue < this.mCurrentValue) {
                duration = this.mData.manualPowerSavingAnimationDarkenTime;
            } else {
                duration = this.mData.manualPowerSavingAnimationBrightenTime;
            }
            this.mManualPowerSavingAnimationEnable = false;
            if (this.DEBUG) {
                Slog.d(this.TAG, "The autoPowerSavingUseManualAnimationTimeEnable state,duration=" + duration);
            }
        } else if (this.mFirstValidAutoBrightness || this.mAutoBrightnessIntervened || this.mfastAnimtionFlag) {
            duration = 0.5f;
            if (this.DEBUG) {
                Slog.i(this.TAG, "mFirstValidAuto=" + this.mFirstValidAutoBrightness + ",mAutoIntervened=" + this.mAutoBrightnessIntervened + "mfastAnimtionFlag=" + this.mfastAnimtionFlag);
            }
        } else if (this.mCameraModeEnable) {
            duration = this.mData.cameraAnimationTime;
            Slog.i(this.TAG, "CameraMode AnimationTime=" + this.mData.cameraAnimationTime);
        } else {
            duration = this.mTargetValue < this.mCurrentValue ? getAutoModeDarkTime() : getAutoModeBrightTime();
        }
        if (this.mData.useVariableStep && duration >= this.mDarkenFixStepsThreshold && this.mTargetValue < this.mCurrentValue) {
            amount = getAnimatedStepByEyeSensitiveCurve(this.mAnimatedValue, (float) this.mTargetValue, duration);
        } else if (!this.mData.useVariableStep || duration < this.mBrightenFixStepsThreshold || this.mTargetValue < this.mCurrentValue) {
            amount = (((float) Math.abs(this.mCurrentValue - this.mTargetValue)) / duration) * 0.016540745f;
        } else {
            amount = getAnimatedStepByEyeSensitiveCurve(this.mAnimatedValue, (float) this.mTargetValue, duration);
        }
        this.mDuration = duration;
        this.mDecreaseFixAmount = amount;
        this.mFirstTimeCalculateAmount = false;
        if (!this.DEBUG) {
            return amount;
        }
        Slog.d(this.TAG, "AutoMode=" + this.mAutoBrightnessMode + ",Target=" + this.mTargetValue + ",Current=" + this.mCurrentValue + ",amount=" + amount + ",duration=" + duration);
        return amount;
    }

    private float getAmount() {
        if (this.mData.useVariableStep && this.mDuration >= this.mDarkenFixStepsThreshold && this.mTargetValue < this.mCurrentValue) {
            return getAnimatedStepByEyeSensitiveCurve(this.mAnimatedValue, (float) this.mTargetValue, this.mDuration);
        }
        if (!this.mData.useVariableStep || this.mDuration < this.mBrightenFixStepsThreshold || this.mTargetValue < this.mCurrentValue) {
            return this.mDecreaseFixAmount;
        }
        return getAnimatedStepByEyeSensitiveCurve(this.mAnimatedValue, (float) this.mTargetValue, this.mDuration);
    }

    public float getAutoModeDarkTime() {
        float duration;
        if (this.mData.useVariableStep) {
            duration = this.mData.darkenGradualTimeMax;
        } else {
            duration = this.mData.darkenGradualTime;
        }
        if (this.mOutdoorAnimationFlag && this.mData.outdoorAnimationDarkenTime > 0.0f) {
            duration = this.mData.outdoorAnimationDarkenTime;
            if (this.DEBUG) {
                Slog.i(this.TAG, "outdoorAnimationDarkenTime = " + duration);
            }
        }
        if (this.mScreenLocked && this.mData.keyguardAnimationDarkenTime > 0.0f) {
            duration = this.mData.keyguardAnimationDarkenTime;
            if (this.DEBUG) {
                Slog.i(this.TAG, "keyguardAnimationDarkenTime=" + duration);
            }
        }
        if (this.mTargetValue < this.mData.darkenTargetFor255 && this.mCurrentValue < this.mData.darkenCurrentFor255 && (Math.abs(duration - this.mData.darkenGradualTime) < 1.0E-7f || Math.abs(duration - this.mData.darkenGradualTimeMax) < 1.0E-7f)) {
            duration = 0.5f;
        }
        if (this.mPowerDimState) {
            if (this.mData.darkenGradualTime > this.mData.dimTime || (this.mData.useVariableStep && this.mData.darkenGradualTimeMax > this.mData.dimTime)) {
                duration = this.mData.dimTime;
            }
            if (this.DEBUG) {
                Slog.d(this.TAG, "The Dim state");
            }
        }
        if (this.mPowerDimRecoveryState) {
            this.mPowerDimRecoveryState = false;
            duration = 0.5f;
            if (this.DEBUG) {
                Slog.d(this.TAG, "The Dim state Recovery");
            }
        }
        return duration;
    }

    public float getAutoModeBrightTime() {
        float duration;
        if (this.mPowerDimRecoveryState) {
            this.mPowerDimRecoveryState = false;
            duration = 0.5f;
            if (this.DEBUG) {
                Slog.d(this.TAG, "The Dim state Recovery");
            }
        } else {
            duration = this.mData.brightenGradualTime;
            if (this.mOutdoorAnimationFlag && this.mData.outdoorAnimationBrightenTime > 0.0f) {
                duration = this.mData.outdoorAnimationBrightenTime;
                if (this.DEBUG) {
                    Slog.i(this.TAG, "outdoorAnimationBrightenTime=" + duration);
                }
            }
            if (this.mScreenLocked && this.mData.keyguardAnimationBrightenTime > 0.0f) {
                duration = this.mData.keyguardAnimationBrightenTime;
                if (this.DEBUG) {
                    Slog.i(this.TAG, "keyguardAnimationBrightenTime=" + duration);
                }
            }
        }
        if (this.mTargetValue < this.mData.darkenTargetFor255) {
            return 0.5f;
        }
        return duration;
    }

    public float getManualModeTime() {
        float duration = 0.5f;
        if (this.mFirstRebootAnimationEnable && this.mData.rebootFirstBrightnessAnimationEnable) {
            this.mFirstRebootAnimationEnable = false;
            duration = this.mData.rebootFirstBrightnessManualTime;
            Slog.i(this.TAG, "The mFirstRebootAnimationEnable state,duration=" + duration);
        } else if (this.mManualModeAnimationEnable) {
            if (this.mTargetValue < this.mCurrentValue) {
                duration = this.mData.manualAnimationDarkenTime;
            } else {
                duration = this.mData.manualAnimationBrightenTime;
            }
            if (this.DEBUG) {
                Slog.d(this.TAG, "The mManualModeAnimationEnable state,duration=" + duration);
            }
        } else if (this.mManualPowerSavingAnimationEnable) {
            if (this.mTargetValue < this.mCurrentValue) {
                duration = this.mData.manualPowerSavingAnimationDarkenTime;
            } else {
                duration = this.mData.manualPowerSavingAnimationBrightenTime;
            }
            this.mManualPowerSavingAnimationEnable = false;
            if (this.DEBUG) {
                Slog.d(this.TAG, "The mManualPowerSavingAnimationEnable state,duration=" + duration);
            }
        }
        if (this.mAnimationEnabled) {
            duration = ((float) this.mMillisecond) / 1000.0f;
            if (this.DEBUG) {
                Slog.d(this.TAG, "The mAnimationEnabled state,duration=" + duration);
            }
        }
        if (this.mManualThermalModeAnimationEnable) {
            if (this.mTargetValue < this.mCurrentValue) {
                duration = this.mData.manualThermalModeAnimationDarkenTime;
            } else {
                duration = this.mData.manualThermalModeAnimationBrightenTime;
            }
            this.mManualThermalModeAnimationEnable = false;
            if (this.DEBUG) {
                Slog.d(this.TAG, "The mManualThermalModeAnimationEnable state,duration=" + duration);
            }
        }
        if (this.mPowerDimState) {
            if (this.mData.darkenGradualTime > this.mData.dimTime || (this.mData.useVariableStep && this.mData.darkenGradualTimeMax > this.mData.dimTime)) {
                duration = this.mData.dimTime;
            }
            if (this.DEBUG) {
                Slog.d(this.TAG, "The Dim state");
            }
        }
        if (this.mPowerDimRecoveryState) {
            this.mPowerDimRecoveryState = false;
            duration = this.mData.manualFastTimeFor255;
            if (this.DEBUG) {
                Slog.d(this.TAG, "The Dim state Recovery");
            }
        }
        return duration;
    }

    public float getManualModeAnimtionAmount() {
        if (!this.mFirstTimeCalculateAmount) {
            return this.mDecreaseFixAmount;
        }
        float duration = getManualModeTime();
        float amount = (((float) Math.abs(this.mCurrentValue - this.mTargetValue)) / duration) * 0.016540745f;
        this.mDecreaseFixAmount = amount;
        this.mFirstTimeCalculateAmount = false;
        if (!this.DEBUG) {
            return amount;
        }
        Slog.d(this.TAG, "AutoMode=" + this.mAutoBrightnessMode + ",Target=" + this.mTargetValue + ",Current=" + this.mCurrentValue + ",amount=" + amount + ",duration=" + duration);
        return amount;
    }

    public void updateTargetAndRate(int target, int rate) {
        if (this.mTargetValue != target) {
            this.mFirstTimeCalculateAmount = true;
        }
        this.mTargetValue = target;
        this.mRate = rate;
    }

    public void updateCurrentBrightnessValue(float currentValue) {
        this.mCurrentValue = Math.round(currentValue);
        this.mAnimatedValue = currentValue;
    }

    public void setPowerDimState(int state) {
        this.mPowerDimState = state == 2;
        if (this.mPowerDimState) {
            this.mFirstValidAutoBrightness = false;
        }
        if (this.mState == 2 && state == 3) {
            this.mPowerDimRecoveryState = true;
        }
        this.mState = state;
    }

    public void updateAdjustMode(boolean automode) {
        this.mAutoBrightnessMode = automode;
    }

    public void autoModeIsIntervened(boolean intervened) {
        this.mAutoBrightnessIntervened = intervened;
    }

    public void isFirstValidAutoBrightness(boolean firstValidAutoBrightness) {
        this.mFirstValidAutoBrightness = firstValidAutoBrightness;
    }

    public void updateFastAnimationFlag(boolean fastAnimtionFlag) {
        this.mfastAnimtionFlag = fastAnimtionFlag;
    }

    public void updateCoverModeFastAnimationFlag(boolean coverModeAmitionFast) {
        this.mCoverModeAnimationFast = coverModeAmitionFast;
    }

    public void updateCameraModeChangeAnimationEnable(boolean cameraModeEnable) {
        this.mCameraModeEnable = cameraModeEnable;
    }

    public void setBrightnessAnimationTime(boolean animationEnabled, int millisecond) {
        this.mAnimationEnabled = animationEnabled;
        this.mMillisecond = millisecond;
        if (this.mMillisecond < 0) {
            this.mAnimationEnabled = false;
            Slog.e(this.TAG, "error input mMillisecond=" + this.mMillisecond + ",set mAnimationEnabled=" + this.mAnimationEnabled);
        }
    }

    public void updateScreenLockedAnimationEnable(boolean screenLockedEnable) {
        this.mScreenLocked = screenLockedEnable;
    }

    public void updateOutdoorAnimationFlag(boolean outdoorAnimationFlag) {
        this.mOutdoorAnimationFlag = outdoorAnimationFlag;
    }

    public void updatemManualModeAnimationEnable(boolean manualModeAnimationEnable) {
        this.mManualModeAnimationEnable = manualModeAnimationEnable;
    }

    public void updateManualPowerSavingAnimationEnable(boolean manualPowerSavingAnimationEnable) {
        this.mManualPowerSavingAnimationEnable = manualPowerSavingAnimationEnable;
    }

    public void updateManualThermalModeAnimationEnable(boolean manualThermalModeAnimationEnable) {
        this.mManualThermalModeAnimationEnable = manualThermalModeAnimationEnable;
    }

    public void clearAnimatedValuePara() {
        this.mFirstValidAutoBrightness = false;
        this.mAutoBrightnessIntervened = false;
        this.mPowerDimState = false;
        this.mPowerDimRecoveryState = false;
        this.mfastAnimtionFlag = false;
        this.mCoverModeAnimationFast = false;
        this.mCameraModeEnable = false;
        this.mOutdoorAnimationFlag = false;
        this.mScreenLocked = false;
        this.mManualModeAnimationEnable = false;
        this.mManualPowerSavingAnimationEnable = false;
        this.mManualThermalModeAnimationEnable = false;
        this.mFirstRebootAnimationEnable = false;
    }
}
