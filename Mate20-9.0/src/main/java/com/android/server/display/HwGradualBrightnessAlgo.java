package com.android.server.display;

import android.animation.ValueAnimator;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwBrightnessXmlLoader;
import com.android.server.gesture.GestureNavConst;

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
    public boolean mAutoBrightnessModeInUse;
    private float mAutoDuration;
    private StepType mAutoStepType;
    private float mBrightenFixStepsThreshold;
    private float mBrightnessMax;
    private float mBrightnessMin;
    private boolean mBrightnessModeAnimationEnable;
    private int mBrightnessModeAnimationTime;
    private boolean mCameraModeEnable;
    private boolean mCoverModeAnimationFast;
    private float mCoverModeAnimationTime;
    private int mCurrentValue;
    private boolean mDarkAdaptAnimationDimmingEnable;
    private float mDarkenFixStepsThreshold;
    private final HwBrightnessXmlLoader.Data mData;
    private float mDecreaseFixAmount;
    private float mDuration;
    private boolean mFastDarkenDimmingEnable;
    private boolean mFirstRebootAnimationEnable;
    private boolean mFirstTimeCalculateAmount;
    public boolean mFirstValidAutoBrightness;
    private boolean mGameModeEnable;
    private float mManualDuration;
    private boolean mManualModeAnimationEnable;
    private boolean mManualPowerSavingAnimationEnable;
    private boolean mManualThermalModeAnimationEnable;
    private int mMillisecond;
    private boolean mOutdoorAnimationFlag;
    private boolean mPowerDimRecoveryState;
    private boolean mPowerDimState;
    private int mRate;
    private boolean mReadingModeEnable;
    private boolean mScreenLocked;
    private int mState;
    private float mStepAdjValue;
    private int mTargetValue;
    private boolean mfastAnimtionFlag;

    private enum StepType {
        LINEAR,
        CURVE
    }

    public HwGradualBrightnessAlgo() {
        this.DEBUG = Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(this.TAG, 4));
        this.mAutoBrightnessIntervened = false;
        this.mFirstValidAutoBrightness = false;
        this.mFirstTimeCalculateAmount = false;
        this.mPowerDimState = false;
        this.mPowerDimRecoveryState = false;
        this.mAnimatedStepRoundEnabled = false;
        this.mDarkenFixStepsThreshold = 20.0f;
        this.mBrightenFixStepsThreshold = 2.0f;
        this.mDuration = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mAutoDuration = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mManualDuration = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mAnimatedStep = 1.0f;
        this.mStepAdjValue = 1.0f;
        this.mfastAnimtionFlag = false;
        this.mCoverModeAnimationFast = false;
        this.mCoverModeAnimationTime = 1.0f;
        this.mAnimationEqualRatioMqDefault = 0.99839747f;
        this.mBrightnessMin = 156.0f;
        this.mBrightnessMax = 10000.0f;
        this.mCameraModeEnable = false;
        this.mGameModeEnable = false;
        this.mReadingModeEnable = false;
        this.mAnimationEnabled = false;
        this.mScreenLocked = false;
        this.mOutdoorAnimationFlag = false;
        this.mManualModeAnimationEnable = false;
        this.mManualPowerSavingAnimationEnable = false;
        this.mManualThermalModeAnimationEnable = false;
        this.mFirstRebootAnimationEnable = true;
        this.mBrightnessModeAnimationEnable = false;
        this.mBrightnessModeAnimationTime = 500;
        this.mFastDarkenDimmingEnable = false;
        this.mAutoStepType = StepType.LINEAR;
        this.mData = HwBrightnessXmlLoader.getData();
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
            String str = this.TAG;
            Slog.d(str, "Init AnimationEqualRatioPara: Ma=" + this.mAnimationEqualRatioMa + ",Mb=" + this.mAnimationEqualRatioMb + ",Nmax=" + N_max + ",Mq0=" + this.mAnimationEqualRatioMq0 + ",MaxNit=" + this.mData.screenBrightnessMaxNit + ",MinNit=" + this.mData.screenBrightnessMinNit);
        }
    }

    private float getAnimatedStepByEyeSensitiveCurve(float currentValue, float targetValue, float duration) {
        float duration2;
        float avgTime;
        if (this.mData.animationEqualRatioEnable && currentValue > targetValue) {
            this.mAnimatedStep = getAnimatedStepByEqualRatio(currentValue, targetValue, duration);
            return this.mAnimatedStep;
        } else if (currentValue == GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            Slog.i(this.TAG, "currentValue is 0, set to target value!");
            return targetValue;
        } else {
            if (duration > 0.116f || currentValue <= targetValue) {
                duration2 = duration;
            } else {
                Slog.e(this.TAG, "duration is not valid, set to 3.0!");
                duration2 = 3.0f;
            }
            if (this.mFirstTimeCalculateAmount) {
                float avgPara = ((((float) Math.pow((double) (targetValue / 10000.0f), 0.09000000357627869d)) * 0.0029f) * CRITERION_TIME) / duration2;
                if (currentValue > targetValue) {
                    avgTime = ((float) (Math.log((double) (targetValue / currentValue)) / Math.log((double) (1.0f - avgPara)))) * 0.016540745f;
                    this.mStepAdjValue = avgTime < this.mData.darkenGradualTimeMin ? avgTime / this.mData.darkenGradualTimeMin : 1.0f;
                } else {
                    avgTime = ((float) (Math.log((double) (targetValue / currentValue)) / Math.log((double) (1.0f + avgPara)))) * 0.016540745f;
                    this.mStepAdjValue = avgTime < duration2 ? avgTime / duration2 : 1.0f;
                }
                if (this.DEBUG) {
                    Slog.d(this.TAG, "getAnimatedStep avgTime= " + avgTime + ",avgPara" + avgPara + ",mStepAdjValue=" + this.mStepAdjValue + ",duration=" + duration2);
                }
            }
            this.mAnimatedStep = 0.0029f * currentValue;
            this.mAnimatedStep *= (float) Math.pow((double) (targetValue / 10000.0f), 0.09000000357627869d);
            if (duration2 >= 20.0f && duration2 < 30.0f) {
                duration2 += 1.0f;
            }
            this.mAnimatedStep = ((this.mAnimatedStep * this.mStepAdjValue) * CRITERION_TIME) / duration2;
            this.mAnimatedStep = getMinAnimatedStepByEyeSensitiveCurve(this.mAnimatedStep);
            if (currentValue > targetValue && targetValue < ((float) this.mData.darkenTargetFor255) && this.mAnimatedStep < this.mData.minAnimatingStep) {
                this.mAnimatedStep = this.mData.minAnimatingStep;
            }
            return this.mAnimatedStep;
        }
    }

    private float getAnimatedStepByEqualRatio(float currentValue, float targetValue, float duration) {
        if ((this.mAnimationEqualRatioMa * targetValue) + this.mAnimationEqualRatioMb < 1.0E-12f || (this.mAnimationEqualRatioMa * currentValue) + this.mAnimationEqualRatioMb < 1.0E-12f || this.mAnimationEqualRatioMq0 < 1.0E-12f) {
            Slog.e(this.TAG, "Error: the screen brightness is minus");
            return DEFAULT_AMOUNT;
        }
        if (this.mFirstTimeCalculateAmount) {
            float avgTime = ((float) (1.0d + (Math.log((double) (((this.mAnimationEqualRatioMa * targetValue) + this.mAnimationEqualRatioMb) / ((this.mAnimationEqualRatioMa * currentValue) + this.mAnimationEqualRatioMb))) / Math.log((double) this.mAnimationEqualRatioMq0)))) / 60.0f;
            if (avgTime < this.mData.darkenGradualTimeMin) {
                this.mAnimationEqualRatioMq = (float) Math.pow((double) (((this.mAnimationEqualRatioMa * targetValue) + this.mAnimationEqualRatioMb) / ((this.mAnimationEqualRatioMa * currentValue) + this.mAnimationEqualRatioMb)), (double) (1.0f / ((this.mData.darkenGradualTimeMin * 60.0f) - 1.0f)));
            } else {
                this.mAnimationEqualRatioMq = this.mAnimationEqualRatioMq0;
            }
            if (this.DEBUG) {
                String str = this.TAG;
                Slog.i(str, "avgTime=" + avgTime + ",Mq=" + this.mAnimationEqualRatioMq + ",Ma=" + this.mAnimationEqualRatioMa + ",Mb=" + this.mAnimationEqualRatioMb + ",mAnimatedValue=" + this.mAnimatedValue);
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
        if (minAnimatedStep >= 1.0f && this.mAnimatedStepRoundEnabled) {
            return (float) Math.round(minAnimatedStep);
        }
        if (minAnimatedStep < 1.0f && minAnimatedStep >= 0.5f && this.mStepAdjValue == 1.0f) {
            return 0.5f;
        }
        if (minAnimatedStep >= 0.5f || this.mStepAdjValue != 1.0f) {
            return minAnimatedStep;
        }
        return 0.25f;
    }

    public float getAnimatedValue() {
        float amount;
        if (ValueAnimator.getDurationScale() == GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO || this.mRate == 0) {
            this.mAnimatedValue = (float) this.mTargetValue;
        } else {
            if (this.mFirstTimeCalculateAmount) {
                if (this.mAutoBrightnessMode) {
                    amount = getAutoModeAnimtionAmount();
                    this.mDuration = this.mAutoDuration;
                } else {
                    amount = getManualModeAnimtionAmount();
                    this.mDuration = this.mManualDuration;
                }
                this.mAutoBrightnessModeInUse = this.mAutoBrightnessMode;
            } else {
                amount = this.mAutoBrightnessModeInUse ? getAutoModeAnimtionAmount() : getManualModeAnimtionAmount();
            }
            if (this.mTargetValue > this.mCurrentValue) {
                this.mAnimatedValue = this.mAnimatedValue + amount < ((float) this.mTargetValue) ? this.mAnimatedValue + amount : (float) this.mTargetValue;
            } else {
                this.mAnimatedValue = this.mAnimatedValue - amount > ((float) this.mTargetValue) ? this.mAnimatedValue - amount : (float) this.mTargetValue;
            }
        }
        return this.mAnimatedValue;
    }

    private StepType getAutoModeAnimtionStepType(float duration) {
        if (this.mDarkAdaptAnimationDimmingEnable) {
            return StepType.LINEAR;
        }
        if (!this.mData.useVariableStep || ((duration < this.mDarkenFixStepsThreshold || this.mTargetValue >= this.mCurrentValue) && (duration < this.mBrightenFixStepsThreshold || this.mTargetValue < this.mCurrentValue))) {
            return StepType.LINEAR;
        }
        return StepType.CURVE;
    }

    public float getAutoModeAnimtionAmount() {
        float duration;
        float amount;
        float duration2;
        float duration3;
        if (!this.mFirstTimeCalculateAmount) {
            return getAmount();
        }
        if (!this.mFirstRebootAnimationEnable || !this.mData.rebootFirstBrightnessAnimationEnable) {
            if (this.mBrightnessModeAnimationEnable) {
                duration3 = ((float) this.mBrightnessModeAnimationTime) / 1000.0f;
                this.mBrightnessModeAnimationEnable = false;
                String str = this.TAG;
                Slog.d(str, "The Auto mBrightnessModeAnimationEnable state,duration=" + duration3);
            } else if (this.mAnimationEnabled) {
                duration3 = ((float) this.mMillisecond) / 1000.0f;
                if (this.DEBUG) {
                    String str2 = this.TAG;
                    Slog.d(str2, "The mAnimationEnabled state,duration=" + duration3);
                }
            } else if (this.mCoverModeAnimationFast) {
                duration = this.mCoverModeAnimationTime;
                if (this.DEBUG) {
                    String str3 = this.TAG;
                    Slog.i(str3, "LabcCoverMode mCoverModeFast=" + this.mCoverModeAnimationFast);
                }
            } else if (this.mData.autoPowerSavingUseManualAnimationTimeEnable && this.mManualPowerSavingAnimationEnable) {
                if (this.mTargetValue < this.mCurrentValue) {
                    duration = this.mData.manualPowerSavingAnimationDarkenTime;
                } else {
                    duration = this.mData.manualPowerSavingAnimationBrightenTime;
                }
                this.mManualPowerSavingAnimationEnable = false;
                if (this.DEBUG) {
                    String str4 = this.TAG;
                    Slog.d(str4, "The autoPowerSavingUseManualAnimationTimeEnable state,duration=" + duration);
                }
            } else if (this.mFirstValidAutoBrightness || this.mAutoBrightnessIntervened || this.mfastAnimtionFlag) {
                float duration4 = 0.5f;
                if (this.mFirstValidAutoBrightness && this.mData.resetAmbientLuxGraTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                    duration4 = this.mData.resetAmbientLuxGraTime;
                    if (this.DEBUG) {
                        String str5 = this.TAG;
                        Slog.i(str5, "resetAmbientLuxGraTime=" + duration4);
                    }
                }
                if (this.DEBUG) {
                    String str6 = this.TAG;
                    Slog.i(str6, "mFirstValidAuto=" + this.mFirstValidAutoBrightness + ",mAutoIntervened=" + this.mAutoBrightnessIntervened + "mfastAnimtionFlag=" + this.mfastAnimtionFlag);
                }
            } else if (this.mFastDarkenDimmingEnable && this.mTargetValue < this.mCurrentValue && this.mData.resetAmbientLuxFastDarkenDimmingTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                duration = this.mData.resetAmbientLuxFastDarkenDimmingTime;
                if (this.DEBUG) {
                    String str7 = this.TAG;
                    Slog.i(str7, "mFastDarkenDimmingEnable AnimationTime=" + duration);
                }
            } else if (this.mGameModeEnable) {
                if (this.mTargetValue >= this.mCurrentValue) {
                    duration2 = this.mData.gameModeBrightenAnimationTime;
                } else if (this.mTargetValue >= this.mData.gameModeDarkentenLongTarget || this.mCurrentValue <= this.mData.gameModeDarkentenLongTarget) {
                    duration2 = this.mData.gameModeDarkentenAnimationTime;
                } else {
                    String str8 = this.TAG;
                    Slog.i(str8, "GameModeEnable mTargetValue=" + this.mTargetValue + ",mCurrentValue=" + this.mCurrentValue);
                    duration2 = this.mData.gameModeDarkentenLongAnimationTime;
                }
                String str9 = this.TAG;
                Slog.i(str9, "GameModeEnable AnimationTime=" + duration);
            } else if (this.mCameraModeEnable) {
                duration = this.mData.cameraAnimationTime;
                String str10 = this.TAG;
                Slog.i(str10, "CameraMode AnimationTime=" + this.mData.cameraAnimationTime);
            } else if (this.mReadingModeEnable) {
                duration = this.mData.readingAnimationTime;
                String str11 = this.TAG;
                Slog.i(str11, "ReadingMode AnimationTime=" + this.mData.readingAnimationTime);
            } else {
                duration = this.mTargetValue < this.mCurrentValue ? getAutoModeDarkTime() : getAutoModeBrightTime();
            }
            duration = duration3;
        } else {
            duration = this.mData.rebootFirstBrightnessAutoTime;
            this.mFirstRebootAnimationEnable = false;
            String str12 = this.TAG;
            Slog.i(str12, "The mFirstRebootAnimationEnable state,duration=" + duration);
        }
        this.mAutoStepType = getAutoModeAnimtionStepType(duration);
        if (this.mAutoStepType == StepType.LINEAR) {
            amount = (((float) Math.abs(this.mCurrentValue - this.mTargetValue)) / duration) * 0.016540745f;
        } else {
            amount = getAnimatedStepByEyeSensitiveCurve(this.mAnimatedValue, (float) this.mTargetValue, duration);
        }
        this.mAutoDuration = duration;
        this.mDecreaseFixAmount = amount;
        this.mFirstTimeCalculateAmount = false;
        if (this.DEBUG) {
            String str13 = this.TAG;
            Slog.d(str13, "AutoMode=" + this.mAutoBrightnessMode + ",Target=" + this.mTargetValue + ",Current=" + this.mCurrentValue + ",amount=" + amount + ",duration=" + duration + ",StepType=" + this.mAutoStepType);
        }
        this.mDarkAdaptAnimationDimmingEnable = false;
        return amount;
    }

    public float getDuration() {
        return this.mDuration;
    }

    private float getAmount() {
        if (this.mAutoStepType == StepType.LINEAR) {
            return this.mDecreaseFixAmount;
        }
        return getAnimatedStepByEyeSensitiveCurve(this.mAnimatedValue, (float) this.mTargetValue, this.mAutoDuration);
    }

    public float getAutoModeDarkTime() {
        float duration;
        if (this.mData.useVariableStep) {
            duration = this.mData.darkenGradualTimeMax;
        } else {
            duration = this.mData.darkenGradualTime;
        }
        if (this.mDarkAdaptAnimationDimmingEnable) {
            duration = (float) this.mData.unadapt2AdaptingDimSec;
            if (this.DEBUG) {
                String str = this.TAG;
                Slog.i(str, "unadapt2AdaptingDimSec = " + duration);
            }
        }
        if (this.mOutdoorAnimationFlag && this.mData.outdoorAnimationDarkenTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            duration = this.mData.outdoorAnimationDarkenTime;
            if (this.DEBUG) {
                String str2 = this.TAG;
                Slog.i(str2, "outdoorAnimationDarkenTime = " + duration);
            }
        }
        if (this.mScreenLocked && this.mData.keyguardAnimationDarkenTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            duration = this.mData.keyguardAnimationDarkenTime;
            if (this.DEBUG) {
                String str3 = this.TAG;
                Slog.i(str3, "keyguardAnimationDarkenTime=" + duration);
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
            if (this.mOutdoorAnimationFlag && this.mData.outdoorAnimationBrightenTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                duration = this.mData.outdoorAnimationBrightenTime;
                if (this.DEBUG) {
                    String str = this.TAG;
                    Slog.i(str, "outdoorAnimationBrightenTime=" + duration);
                }
            }
            if (this.mScreenLocked && this.mData.keyguardAnimationBrightenTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                duration = this.mData.keyguardAnimationBrightenTime;
                if (this.DEBUG) {
                    String str2 = this.TAG;
                    Slog.i(str2, "keyguardAnimationBrightenTime=" + duration);
                }
            }
        }
        if (this.mTargetValue < this.mData.darkenTargetFor255) {
            return 0.5f;
        }
        return duration;
    }

    public float getManualModeTime() {
        float duration;
        float duration2;
        float duration3 = 0.5f;
        if (this.mFirstRebootAnimationEnable && this.mData.rebootFirstBrightnessAnimationEnable) {
            this.mFirstRebootAnimationEnable = false;
            duration3 = this.mData.rebootFirstBrightnessManualTime;
            String str = this.TAG;
            Slog.i(str, "The mFirstRebootAnimationEnable state,duration=" + duration3);
        } else if (this.mManualModeAnimationEnable) {
            if (this.mTargetValue < this.mCurrentValue) {
                duration3 = this.mData.manualAnimationDarkenTime;
            } else {
                duration3 = this.mData.manualAnimationBrightenTime;
            }
            if (this.DEBUG) {
                String str2 = this.TAG;
                Slog.d(str2, "The mManualModeAnimationEnable state,duration=" + duration3);
            }
        } else if (this.mManualPowerSavingAnimationEnable) {
            if (this.mTargetValue < this.mCurrentValue) {
                duration2 = this.mData.manualPowerSavingAnimationDarkenTime;
            } else {
                duration2 = this.mData.manualPowerSavingAnimationBrightenTime;
            }
            this.mManualPowerSavingAnimationEnable = false;
            if (this.DEBUG) {
                String str3 = this.TAG;
                Slog.d(str3, "The mManualPowerSavingAnimationEnable state,duration=" + duration3);
            }
        }
        if (this.mAnimationEnabled) {
            duration3 = ((float) this.mMillisecond) / 1000.0f;
            if (this.DEBUG) {
                String str4 = this.TAG;
                Slog.d(str4, "The mAnimationEnabled state,duration=" + duration3);
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
                String str5 = this.TAG;
                Slog.d(str5, "The mManualThermalModeAnimationEnable state,duration=" + duration3);
            }
        }
        if (this.mBrightnessModeAnimationEnable) {
            duration3 = ((float) this.mBrightnessModeAnimationTime) / 1000.0f;
            this.mBrightnessModeAnimationEnable = false;
            if (this.DEBUG) {
                String str6 = this.TAG;
                Slog.d(str6, "The manual mBrightnessModeAnimationEnable state,duration=" + duration3);
            }
        }
        if (this.mPowerDimState) {
            if (this.mData.darkenGradualTime > this.mData.dimTime || (this.mData.useVariableStep && this.mData.darkenGradualTimeMax > this.mData.dimTime)) {
                duration3 = this.mData.dimTime;
            }
            if (this.DEBUG) {
                Slog.d(this.TAG, "The Dim state");
            }
        }
        if (this.mPowerDimRecoveryState) {
            this.mPowerDimRecoveryState = false;
            duration3 = this.mData.manualFastTimeFor255;
            if (this.DEBUG) {
                Slog.d(this.TAG, "The Dim state Recovery");
            }
        }
        return duration3;
    }

    public float getManualModeAnimtionAmount() {
        if (!this.mFirstTimeCalculateAmount) {
            return this.mDecreaseFixAmount;
        }
        float duration = getManualModeTime();
        this.mManualDuration = duration;
        float amount = (((float) Math.abs(this.mCurrentValue - this.mTargetValue)) / duration) * 0.016540745f;
        this.mDecreaseFixAmount = amount;
        this.mFirstTimeCalculateAmount = false;
        if (!this.DEBUG) {
            return amount;
        }
        String str = this.TAG;
        Slog.d(str, "AutoMode=" + this.mAutoBrightnessMode + ",Target=" + this.mTargetValue + ",Current=" + this.mCurrentValue + ",amount=" + amount + ",duration=" + duration);
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

    public void updateGameModeChangeAnimationEnable(boolean gameModeEnable) {
        this.mGameModeEnable = gameModeEnable;
    }

    public void updateReadingModeChangeAnimationEnable(boolean readingModeEnable) {
        this.mReadingModeEnable = readingModeEnable;
    }

    public void setBrightnessAnimationTime(boolean animationEnabled, int millisecond) {
        this.mAnimationEnabled = animationEnabled;
        this.mMillisecond = millisecond;
        if (this.mMillisecond < 0) {
            this.mAnimationEnabled = false;
            String str = this.TAG;
            Slog.e(str, "error input mMillisecond=" + this.mMillisecond + ",set mAnimationEnabled=" + this.mAnimationEnabled);
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

    public void updateBrightnessModeAnimationEnable(boolean animationEnable, int time) {
        this.mBrightnessModeAnimationTime = time;
        if (time < 0) {
            this.mBrightnessModeAnimationEnable = false;
            String str = this.TAG;
            Slog.e(str, "error input time,time=,set mBrightnessModeAnimationEnable=" + this.mBrightnessModeAnimationEnable);
        }
        this.mBrightnessModeAnimationEnable = animationEnable;
    }

    public void updateDarkAdaptAnimationDimmingEnable(boolean enable) {
        this.mDarkAdaptAnimationDimmingEnable = enable;
    }

    public void updateFastDarkenDimmingEnable(boolean enable) {
        if (enable != this.mFastDarkenDimmingEnable && this.DEBUG) {
            String str = this.TAG;
            Slog.i(str, "updateFastDarkenDimmingEnable enable=" + enable);
        }
        this.mFastDarkenDimmingEnable = enable;
    }

    public void clearAnimatedValuePara() {
        this.mFirstValidAutoBrightness = false;
        this.mAutoBrightnessIntervened = false;
        this.mPowerDimState = false;
        this.mPowerDimRecoveryState = false;
        this.mfastAnimtionFlag = false;
        this.mCoverModeAnimationFast = false;
        this.mCameraModeEnable = false;
        this.mReadingModeEnable = false;
        this.mOutdoorAnimationFlag = false;
        this.mScreenLocked = false;
        this.mManualModeAnimationEnable = false;
        this.mManualPowerSavingAnimationEnable = false;
        this.mManualThermalModeAnimationEnable = false;
        this.mFirstRebootAnimationEnable = false;
        this.mBrightnessModeAnimationEnable = false;
        this.mDarkAdaptAnimationDimmingEnable = false;
        this.mGameModeEnable = false;
    }
}
