package com.android.server.display;

import android.animation.ValueAnimator;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwBrightnessXmlLoader;
import java.util.HashMap;
import java.util.Map;

/* access modifiers changed from: package-private */
public final class HwGradualBrightnessAlgo {
    private static final float BRIGHTEN_FIX_STEPS_THRESHOLD = 2.0f;
    private static final float CALI_LOWER_BOUND_TIME = 20.0f;
    private static final float CALI_UPPER_BOUND_TIME = 30.0f;
    private static final float CRITERION_TIME = 40.0f;
    private static final float DARKEN_FIX_STEPS_THRESHOLD = 20.0f;
    private static final float DEFAULT_AMOUNT = 157.0f;
    private static final double DEFAULT_ANIMATION_EQUAL_RATIO_MQ = 0.998397453939675d;
    private static final float DEFAULT_BRIGHT_TIME = 3.0f;
    private static final float EYE_SENSITIVE_DEFAULT_STEP = 1.0f;
    private static final float EYE_SENSITIVE_MIN_STEP = 0.25f;
    private static final float EYE_SENSITIVE_SUB_MIN_STEP = 0.5f;
    private static final float FAST_TIME = 0.5f;
    private static final int FAST_TIME_MILLIS = 500;
    private static final int FRAME_RATE_LEVEL_0 = 60;
    private static final float FRAME_RATE_LEVEL_0_TIME_DELTA_MIN = 0.016f;
    private static final int FRAME_RATE_LEVEL_1 = 90;
    private static final float FRAME_RATE_LEVEL_1_TIME_DELTA_MIN = 0.011f;
    private static final int FRAME_RATE_LEVEL_2 = 120;
    private static final float FRAME_RATE_LEVEL_2_TIME_DELTA_MIN = 0.008f;
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final float JND_ADJ_PARA = 0.09f;
    private static final float JND_CURVE_PARA = 0.0029f;
    private static final float MAX_BRIGHTNESS = 10000.0f;
    private static final float MIN_BRIGHTNESS = 156.0f;
    private static final int POLICY_BRIGHT = 3;
    private static final int POLICY_DIM = 2;
    private static final float SMALL_VALUE = 1.0E-6f;
    private static final String TAG = "HwGradualBrightnessAlgo";
    private static final float TIME_DELTA = 0.016666668f;
    private static final float TIME_MILLIS_FACTOR = 1000.0f;
    private float mAnimatedStep = 1.0f;
    private float mAnimatedValue;
    private float mAnimationEqualRatioMa;
    private float mAnimationEqualRatioMb;
    private double mAnimationEqualRatioMq;
    private double mAnimationEqualRatioMq0 = 0.9983974695205688d;
    private float mAnimationEqualRatioNormalModeHighMa;
    private float mAnimationEqualRatioNormalModeHighMb;
    private float mAnimationEqualRatioNormalModeHighMq = 0.99839747f;
    private float mAnimationEqualRatioNormalModeHighMq0 = 0.99839747f;
    private float mAnimationEqualRatioNormalModeMa;
    private float mAnimationEqualRatioNormalModeMb;
    private float mAnimationEqualRatioNormalModeMq = 0.99839747f;
    private float mAnimationEqualRatioNormalModeMq0 = 0.99839747f;
    private float mAutoDuration = 0.0f;
    private StepType mAutoStepType = StepType.LINEAR;
    private Map<BrightnessModeState, BrightenAnimationTime> mBrightenAnimationMap = new HashMap();
    private int mBrightnessModeAnimationTime = 500;
    private float mCoverModeAnimationTime = 1.0f;
    private int mCurrentValue;
    private final HwBrightnessXmlLoader.Data mData = HwBrightnessXmlLoader.getData();
    private float mDecreaseFixAmount;
    private float mDuration = 0.0f;
    private int mFrameRate = 60;
    private float mHighEqualDarkenMaxFrameNum = 60.0f;
    private float mHighEqualDarkenMinFrameNum = 60.0f;
    private boolean mIsAnimatedStepRoundEnabled = false;
    private boolean mIsAnimationEnable = false;
    private boolean mIsAutoBrightnessIntervened = false;
    private boolean mIsAutoBrightnessMode;
    private boolean mIsBrightnessModeAnimationEnable = false;
    private boolean mIsCameraModeEnable = false;
    private boolean mIsChangeToAutoBrightnessMode = false;
    private boolean mIsCoverModeAnimationEnable = false;
    private boolean mIsDarkAdaptAnimationDimmingEnable;
    private boolean mIsFastAnimtionFlag = false;
    private boolean mIsFastDarkenDimmingEnable = false;
    private boolean mIsFirstRebootAnimationEnable = true;
    private boolean mIsFirstTimeCalculateAmount = false;
    private boolean mIsFirstValidAutoBrightness = false;
    private boolean mIsFrontCameraDimmingEnable = false;
    private boolean mIsGameModeEnable = false;
    private boolean mIsKeyguardUnlockedFastDarkenDimmingEnable = false;
    private boolean mIsManualModeAnimationEnable = false;
    private boolean mIsManualPowerSavingAnimationEnable = false;
    private boolean mIsManualThermalModeAnimationEnable = false;
    private boolean mIsNightUpPowerOnWithDimmingEnable = false;
    private boolean mIsOutdoorAnimationFlag = false;
    private boolean mIsPowerDimRecoveryState = false;
    private boolean mIsPowerDimState = false;
    private boolean mIsReadingModeEnable = false;
    private boolean mIsScreenLocked = false;
    private float mLowEqualDarkenMaxFrameNum = 60.0f;
    private float mLowEqualDarkenMinFrameNum = 60.0f;
    private float mManualDuration = 0.0f;
    private float mMaxDarkenAnimatingStepForHbm;
    private float mMaxDarkenFrameNum = 60.0f;
    private int mMillisecond;
    private float mMinDarkenAnimatingStepForHbm;
    private float mMinDarkenFrameNum = 60.0f;
    private int mRate;
    private int mState;
    private float mStepAdjValue = 1.0f;
    private int mTargetValue;
    private float mTimeDelta = TIME_DELTA;
    private float mTimeDeltaMinValue = FRAME_RATE_LEVEL_0_TIME_DELTA_MIN;

    /* access modifiers changed from: private */
    public interface BrightenAnimationTime {
        float getBrightenAnimationTime();
    }

    /* access modifiers changed from: private */
    public enum BrightnessModeState {
        REBOOT_FIRST_ANIMATION,
        BRIGHTNESS_MODE_ANIMATION,
        APP_SET_ANIMATION,
        COVER_MODE_FAST_RESEPONSE,
        POWERSAVING_USE_MANUAL,
        FRONT_CAMERA,
        FIRST_BRIGHTNESS_ANIMATION,
        KEYGUARD_UNLOCK,
        FAST_DARK,
        GAME_MODE,
        CAMERA_MODE,
        READING_MODE,
        DEFAULT_MDOE
    }

    /* access modifiers changed from: private */
    public enum StepType {
        LINEAR,
        CURVE
    }

    static {
        boolean z = false;
        if (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3))) {
            z = true;
        }
        HWDEBUG = z;
    }

    HwGradualBrightnessAlgo() {
        initAnimationEqualRatioPara();
        if (this.mData.rebootFirstBrightnessAnimationEnable) {
            this.mTargetValue = this.mData.rebootFirstBrightness;
            this.mCurrentValue = this.mData.rebootFirstBrightness;
            this.mAnimatedValue = (float) this.mData.rebootFirstBrightness;
        }
        initBrightenAnimationMap();
    }

    private void initAnimationEqualRatioPara() {
        if (Math.abs(9844.0f) >= SMALL_VALUE) {
            this.mAnimationEqualRatioMa = (this.mData.screenBrightnessMaxNit - this.mData.screenBrightnessMinNit) / 9844.0f;
            this.mAnimationEqualRatioMb = this.mData.screenBrightnessMaxNit - (this.mAnimationEqualRatioMa * MAX_BRIGHTNESS);
            updateAnimationEqualRatioPara();
            if (this.mData.isLowEqualDarkenEnable) {
                if (this.mData.isHighEqualDarkenEnable) {
                    initHighAnimationEqualRatioPara();
                    updateHighEqualParaFromFrameRate();
                }
                initLowAnimationEqualRatioPara();
                updateLowEqualParaFromFrameRate();
            }
            if (this.mData.isDarkenAnimatingStepForHbm) {
                this.mMaxDarkenAnimatingStepForHbm = this.mData.maxDarkenAnimatingStepForHbm;
                this.mMinDarkenAnimatingStepForHbm = this.mData.minDarkenAnimatingStepForHbm;
                Slog.i(TAG, "update mMaxDarkenAnimatingStepForHbm=" + this.mMaxDarkenAnimatingStepForHbm + ",mMinDarkenAnimatingStepForHbm=" + this.mMinDarkenAnimatingStepForHbm);
            }
            if (HWFLOW) {
                Slog.i(TAG, "Init AnimationEqualRatioPara: Ma=" + this.mAnimationEqualRatioMa + ",Mb=" + this.mAnimationEqualRatioMb + ",mMaxDarkenFrameNum=" + this.mMaxDarkenFrameNum + ",Mq0=" + this.mAnimationEqualRatioMq0 + ",MaxNit=" + this.mData.screenBrightnessMaxNit + ",MinNit=" + this.mData.screenBrightnessMinNit);
                return;
            }
            return;
        }
        Slog.w(TAG, "MAX_BRIGHTNESS <= MIN_BRIGHTNESS");
    }

    private void updateDarkenAnimatingStepForHbm() {
        if (this.mFrameRate <= 0) {
            this.mMaxDarkenAnimatingStepForHbm = this.mData.maxDarkenAnimatingStepForHbm;
            this.mMinDarkenAnimatingStepForHbm = this.mData.minDarkenAnimatingStepForHbm;
        } else {
            this.mMaxDarkenAnimatingStepForHbm = (this.mData.maxDarkenAnimatingStepForHbm * 60.0f) / ((float) this.mFrameRate);
            this.mMinDarkenAnimatingStepForHbm = (this.mData.minDarkenAnimatingStepForHbm * 60.0f) / ((float) this.mFrameRate);
        }
        if (HWFLOW) {
            Slog.i(TAG, "update mMaxDarkenAnimatingStepForHbm=" + this.mMaxDarkenAnimatingStepForHbm + ",mMinDarkenAnimatingStepForHbm=" + this.mMinDarkenAnimatingStepForHbm);
        }
    }

    private void initLowAnimationEqualRatioPara() {
        if (Math.abs(this.mData.screenBrightnessNormalModeMaxLevel - MIN_BRIGHTNESS) < SMALL_VALUE || this.mData.screenBrightnessNormalModeMaxLevel < MIN_BRIGHTNESS) {
            Slog.w(TAG, "mData.screenBrightnessNormalModeMaxLevel <= MIN_BRIGHTNESS");
            return;
        }
        this.mAnimationEqualRatioNormalModeMa = (this.mData.screenBrightnessNormalModeMaxNit - this.mData.screenBrightnessMinNit) / (this.mData.screenBrightnessNormalModeMaxLevel - MIN_BRIGHTNESS);
        this.mAnimationEqualRatioNormalModeMb = this.mData.screenBrightnessNormalModeMaxNit - (this.mData.screenBrightnessNormalModeMaxLevel * this.mAnimationEqualRatioNormalModeMa);
        if (HWFLOW) {
            Slog.i(TAG, "NormalModeMa=" + this.mAnimationEqualRatioNormalModeMa + ",NormalModeMb=" + this.mAnimationEqualRatioNormalModeMb + ",NormalModeMaxNit=" + this.mData.screenBrightnessNormalModeMaxNit + ",NormalModeMaxLevel=" + this.mData.screenBrightnessNormalModeMaxLevel);
        }
    }

    private void initHighAnimationEqualRatioPara() {
        if (Math.abs(this.mData.screenBrightnessNormalModeMaxLevel - this.mData.lowEqualDarkenLevel) < SMALL_VALUE || this.mData.screenBrightnessNormalModeMaxLevel < this.mData.lowEqualDarkenLevel) {
            Slog.w(TAG, "mData.screenBrightnessNormalModeMaxLevel <= mData.lowEqualDarkenLevel");
            return;
        }
        this.mAnimationEqualRatioNormalModeHighMa = (this.mData.screenBrightnessNormalModeMaxNit - this.mData.lowEqualDarkenNit) / (this.mData.screenBrightnessNormalModeMaxLevel - this.mData.lowEqualDarkenLevel);
        this.mAnimationEqualRatioNormalModeHighMb = this.mData.screenBrightnessNormalModeMaxNit - (this.mData.screenBrightnessNormalModeMaxLevel * this.mAnimationEqualRatioNormalModeHighMa);
        if (HWFLOW) {
            Slog.i(TAG, "init NormalModeHighMa=" + this.mAnimationEqualRatioNormalModeHighMb + ",NormalModeHighMb=" + this.mAnimationEqualRatioNormalModeHighMb + ",NormalModeMaxNit=" + this.mData.screenBrightnessNormalModeMaxNit + ",NormalModeMaxLevel=" + this.mData.screenBrightnessNormalModeMaxLevel);
        }
    }

    private void initBrightenAnimationMap() {
        this.mBrightenAnimationMap.clear();
        this.mBrightenAnimationMap.put(BrightnessModeState.REBOOT_FIRST_ANIMATION, new RebootFirstAnimation());
        this.mBrightenAnimationMap.put(BrightnessModeState.BRIGHTNESS_MODE_ANIMATION, new BrightessModeAnimation());
        this.mBrightenAnimationMap.put(BrightnessModeState.APP_SET_ANIMATION, new AppSetAnimation());
        this.mBrightenAnimationMap.put(BrightnessModeState.POWERSAVING_USE_MANUAL, new PowerSavingUseManualAnimation());
        this.mBrightenAnimationMap.put(BrightnessModeState.COVER_MODE_FAST_RESEPONSE, new CoverModeFastAnimation());
        this.mBrightenAnimationMap.put(BrightnessModeState.FRONT_CAMERA, new FrontCameraAnimation());
        this.mBrightenAnimationMap.put(BrightnessModeState.FIRST_BRIGHTNESS_ANIMATION, new FirstBrightnessAnimation());
        this.mBrightenAnimationMap.put(BrightnessModeState.KEYGUARD_UNLOCK, new KeyguardUnlockAnimation());
        this.mBrightenAnimationMap.put(BrightnessModeState.FAST_DARK, new FastDarkAnimation());
        this.mBrightenAnimationMap.put(BrightnessModeState.GAME_MODE, new GameModeAnimation());
        this.mBrightenAnimationMap.put(BrightnessModeState.CAMERA_MODE, new CameraModeAnimation());
        this.mBrightenAnimationMap.put(BrightnessModeState.READING_MODE, new ReadingModeAnimation());
        this.mBrightenAnimationMap.put(BrightnessModeState.DEFAULT_MDOE, new DefaultModeAnimation());
    }

    private void updateAnimationEqualRatioPara() {
        double d;
        this.mMaxDarkenFrameNum = (this.mData.darkenGradualTimeMax * ((float) this.mFrameRate)) - 1.0f;
        this.mMinDarkenFrameNum = (this.mData.darkenGradualTimeMin * ((float) this.mFrameRate)) - 1.0f;
        if (Math.abs(this.mMaxDarkenFrameNum) < SMALL_VALUE || Math.abs(this.mData.screenBrightnessMaxNit) < SMALL_VALUE) {
            d = 0.9983974695205688d;
        } else {
            d = (double) ((float) Math.pow((double) (this.mData.screenBrightnessMinNit / this.mData.screenBrightnessMaxNit), (double) (1.0f / this.mMaxDarkenFrameNum)));
        }
        this.mAnimationEqualRatioMq0 = d;
        if (this.mMaxDarkenFrameNum < SMALL_VALUE) {
            Slog.w(TAG, "mMaxDarkenFrameNum < SMALL_VALUE,setDefault");
            this.mMaxDarkenFrameNum = 60.0f;
        }
        if (this.mMinDarkenFrameNum < SMALL_VALUE) {
            Slog.w(TAG, "mMinDarkenFrameNum < SMALL_VALUE,setDefault");
            this.mMinDarkenFrameNum = 60.0f;
        }
        if (HWFLOW) {
            Slog.i(TAG, "updateAnimationEqualRatioPara: mMaxDarkenFrameNum=" + this.mMaxDarkenFrameNum + ",mq0=" + this.mAnimationEqualRatioMq0);
        }
    }

    private void updateLowEqualParaFromFrameRate() {
        float f;
        this.mLowEqualDarkenMaxFrameNum = (this.mData.lowEqualDarkenMaxTime * ((float) this.mFrameRate)) - 1.0f;
        this.mLowEqualDarkenMinFrameNum = (this.mData.lowEqualDarkenMinTime * ((float) this.mFrameRate)) - 1.0f;
        if (this.mLowEqualDarkenMaxFrameNum < SMALL_VALUE) {
            Slog.w(TAG, "mLowEqualDarkenMaxFrameNum < SMALL_VALUE,setDefault");
            this.mLowEqualDarkenMaxFrameNum = 60.0f;
        }
        if (this.mLowEqualDarkenMinFrameNum < SMALL_VALUE) {
            Slog.w(TAG, "mLowEqualDarkenMinFrameNum < SMALL_VALUE,setDefault");
            this.mLowEqualDarkenMinFrameNum = 60.0f;
        }
        if (Math.abs(this.mLowEqualDarkenMaxFrameNum) < SMALL_VALUE || Math.abs(this.mData.lowEqualDarkenNit) < SMALL_VALUE) {
            f = 0.99839747f;
        } else {
            f = (float) Math.pow((double) (this.mData.screenBrightnessMinNit / this.mData.lowEqualDarkenNit), (double) (1.0f / this.mLowEqualDarkenMaxFrameNum));
        }
        this.mAnimationEqualRatioNormalModeMq0 = f;
        if (HWFLOW) {
            Slog.i(TAG, "mLowEqualDarkenMaxFrameNum=" + this.mLowEqualDarkenMaxFrameNum + ",mLowEqualDarkenMinFrameNum=" + this.mLowEqualDarkenMinFrameNum + ",normalMq=" + this.mAnimationEqualRatioNormalModeMq0 + ",mFrameRate=" + this.mFrameRate);
        }
    }

    private void updateHighEqualParaFromFrameRate() {
        float f;
        this.mHighEqualDarkenMaxFrameNum = (this.mData.highEqualDarkenMaxTime * ((float) this.mFrameRate)) - 1.0f;
        this.mHighEqualDarkenMinFrameNum = (this.mData.highEqualDarkenMinTime * ((float) this.mFrameRate)) - 1.0f;
        if (this.mHighEqualDarkenMaxFrameNum < SMALL_VALUE) {
            Slog.w(TAG, "mHighEqualDarkenMaxFrameNum < SMALL_VALUE,setDefault");
            this.mHighEqualDarkenMaxFrameNum = 60.0f;
        }
        if (this.mHighEqualDarkenMinFrameNum < SMALL_VALUE) {
            Slog.w(TAG, "mHighEqualDarkenMinFrameNum < SMALL_VALUE,setDefault");
            this.mHighEqualDarkenMinFrameNum = 60.0f;
        }
        if (Math.abs(this.mHighEqualDarkenMaxFrameNum) < SMALL_VALUE || Math.abs(this.mData.screenBrightnessNormalModeMaxNit) < SMALL_VALUE) {
            f = 0.99839747f;
        } else {
            f = (float) Math.pow((double) (this.mData.lowEqualDarkenNit / this.mData.screenBrightnessNormalModeMaxNit), (double) (1.0f / this.mHighEqualDarkenMaxFrameNum));
        }
        this.mAnimationEqualRatioNormalModeHighMq0 = f;
        if (HWFLOW) {
            Slog.i(TAG, "mHighEqualDarkenMaxFrameNum=" + this.mHighEqualDarkenMaxFrameNum + ",mHighEqualDarkenMinFrameNum=" + this.mHighEqualDarkenMinFrameNum + ",normalMq=" + this.mAnimationEqualRatioNormalModeHighMq0 + ",mFrameRate=" + this.mFrameRate);
        }
    }

    private void updateLowEqualDarkenPara(float currentValue, float targetValue) {
        if (this.mData.isLowEqualDarkenEnable && currentValue >= targetValue && targetValue <= this.mData.lowEqualDarkenLevel && Math.abs(targetValue - this.mData.lowEqualDarkenLevel) >= SMALL_VALUE) {
            float currentValueNit = getCurrentBrightnessNit(currentValue);
            float targetValueNit = getCurrentBrightnessNit(targetValue);
            if (targetValueNit < SMALL_VALUE || currentValueNit < SMALL_VALUE || this.mAnimationEqualRatioNormalModeMq0 < SMALL_VALUE || ((float) this.mFrameRate) < SMALL_VALUE || this.mLowEqualDarkenMinFrameNum < SMALL_VALUE) {
                Slog.w(TAG, "LowEqualDarken updateLowEqualDarkenPara no update,targetValueNit=" + targetValueNit + ",currentValueNit=" + currentValueNit);
                return;
            }
            float lowEqualDarkenNitTh = this.mData.lowEqualDarkenNit;
            if (currentValueNit < this.mData.lowEqualDarkenNit) {
                lowEqualDarkenNitTh = currentValueNit;
            }
            if (lowEqualDarkenNitTh < SMALL_VALUE) {
                Slog.w(TAG, "LowEqualDarken lowEqualDarkenNitTh<min, no update");
                return;
            }
            float avgTime = ((float) ((Math.log((double) (targetValueNit / lowEqualDarkenNitTh)) / Math.log((double) this.mAnimationEqualRatioNormalModeMq0)) + 1.0d)) / ((float) this.mFrameRate);
            if (avgTime >= this.mData.lowEqualDarkenMinTime || targetValue <= this.mData.lowEqualDarkenMinLevel) {
                this.mAnimationEqualRatioNormalModeMq = this.mAnimationEqualRatioNormalModeMq0;
            } else {
                Slog.i(TAG, "LowEqualDarken avgTime=" + avgTime + "-->Time=" + this.mData.lowEqualDarkenMinTime);
                avgTime = this.mData.lowEqualDarkenMinTime;
                this.mAnimationEqualRatioNormalModeMq = (float) Math.pow((double) (targetValueNit / lowEqualDarkenNitTh), (double) (1.0f / this.mLowEqualDarkenMinFrameNum));
            }
            if (HWFLOW) {
                Slog.i(TAG, "LowEqualDarken avgTime=" + avgTime + ",currentValueNit=" + currentValueNit + ",targetValueNit=" + targetValueNit + ",frameRate=" + this.mFrameRate + ",lowEqualDarkenNitTh=" + lowEqualDarkenNitTh + ",MinLevel=" + this.mData.lowEqualDarkenMinLevel);
            }
        }
    }

    private float getCurrentBrightnessNit(float level) {
        float currentNit;
        if (this.mData.isLowPowerMappingEnable && level < this.mData.lowPowerMappingLevel) {
            currentNit = (float) ((Math.pow((double) ((level - MIN_BRIGHTNESS) / (this.mData.lowPowerMappingLevel - MIN_BRIGHTNESS)), (double) this.mData.lowPowerMappingLevelRatio) * ((double) (this.mData.lowPowerMappingNit - this.mData.screenBrightnessMinNit))) + ((double) this.mData.screenBrightnessMinNit));
            if (HWFLOW) {
                Slog.i(TAG, "BrightnessNit powerMapping currentNit=" + currentNit + ",level=" + level);
            }
        } else if (this.mData.isHighEqualDarkenEnable) {
            currentNit = (this.mAnimationEqualRatioNormalModeHighMa * level) + this.mAnimationEqualRatioNormalModeHighMb;
            if (HWFLOW) {
                Slog.i(TAG, "BrightnessNit linearMapping high currentNit=" + currentNit + ",level=" + level);
            }
        } else {
            currentNit = (this.mAnimationEqualRatioNormalModeMa * level) + this.mAnimationEqualRatioNormalModeMb;
            if (HWFLOW) {
                Slog.i(TAG, "BrightnessNit linearMapping low currentNit=" + currentNit + ",level=" + level);
            }
        }
        return currentNit;
    }

    private void updateHighEqualDarkenPara(float currentValue, float targetValue) {
        if (checkHighEqualDarkenPara(currentValue, targetValue)) {
            float currentValueNit = this.mData.screenBrightnessNormalModeMaxNit;
            if (currentValue < this.mData.screenBrightnessNormalModeMaxLevel) {
                currentValueNit = getCurrentBrightnessNit(currentValue);
            }
            float targetValueNit = this.mData.lowEqualDarkenNit;
            if (targetValue > this.mData.lowEqualDarkenLevel) {
                targetValueNit = getCurrentBrightnessNit(targetValue);
            }
            if (targetValueNit < SMALL_VALUE || currentValueNit < SMALL_VALUE || this.mAnimationEqualRatioNormalModeHighMq0 < SMALL_VALUE || ((float) this.mFrameRate) < SMALL_VALUE || this.mHighEqualDarkenMinFrameNum < SMALL_VALUE) {
                Slog.w(TAG, "HighEqualDarken updateLowEqualDarkenPara no update,targetValueNit=" + targetValueNit + ",currentValueNit=" + currentValueNit);
                return;
            }
            float lowEqualDarkenNitTh = this.mData.lowEqualDarkenNit;
            if (targetValueNit > this.mData.lowEqualDarkenNit) {
                lowEqualDarkenNitTh = targetValueNit;
            }
            if (lowEqualDarkenNitTh < SMALL_VALUE) {
                Slog.w(TAG, "HighEqualDarken lowEqualDarkenNitTh<min, no update");
                return;
            }
            float avgTime = ((float) ((Math.log((double) (lowEqualDarkenNitTh / currentValueNit)) / Math.log((double) this.mAnimationEqualRatioNormalModeHighMq0)) + 1.0d)) / ((float) this.mFrameRate);
            if (avgTime >= this.mData.highEqualDarkenMinTime || targetValue <= this.mData.lowEqualDarkenMinLevel) {
                this.mAnimationEqualRatioNormalModeHighMq = this.mAnimationEqualRatioNormalModeHighMq0;
            } else {
                Slog.i(TAG, "HighEqualDarken avgTime=" + avgTime + "-->Time=" + this.mData.highEqualDarkenMinTime);
                avgTime = this.mData.highEqualDarkenMinTime;
                this.mAnimationEqualRatioNormalModeHighMq = (float) Math.pow((double) (lowEqualDarkenNitTh / currentValueNit), (double) (1.0f / this.mHighEqualDarkenMinFrameNum));
            }
            if (HWFLOW) {
                Slog.i(TAG, "HighEqualDarken avgTime=" + avgTime + ",currentValueNit=" + currentValueNit + ",targetValueNit=" + targetValueNit + ",frameRate=" + this.mFrameRate + ",lowEqualDarkenNitTh=" + lowEqualDarkenNitTh + ",MinLevel=" + this.mData.lowEqualDarkenMinLevel);
            }
        }
    }

    private boolean checkHighEqualDarkenPara(float currentValue, float targetValue) {
        if (this.mData.isHighEqualDarkenEnable && currentValue >= targetValue && targetValue <= this.mData.screenBrightnessNormalModeMaxLevel && Math.abs(targetValue - this.mData.screenBrightnessNormalModeMaxLevel) >= SMALL_VALUE && currentValue >= this.mData.lowEqualDarkenLevel && Math.abs(currentValue - this.mData.lowEqualDarkenLevel) >= SMALL_VALUE) {
            return true;
        }
        return false;
    }

    private float getAnimatedStepByEyeSensitiveCurve(float currentValue, float targetValue, float duration) {
        float durationTmp = duration;
        if (this.mData.animationEqualRatioEnable && currentValue > targetValue) {
            this.mAnimatedStep = getAnimatedStepByEqualRatio(currentValue, targetValue, durationTmp);
            return this.mAnimatedStep;
        } else if (currentValue == 0.0f) {
            Slog.i(TAG, "currentValue is 0, set to target value!");
            return targetValue;
        } else {
            if (durationTmp <= 0.116f && currentValue > targetValue) {
                Slog.e(TAG, "duration is not valid, set to 3.0!");
                durationTmp = 3.0f;
            }
            if (this.mIsFirstTimeCalculateAmount) {
                this.mStepAdjValue = getFirstTimeAmountByEyeSensitiveCurve(currentValue, targetValue, durationTmp);
                updateHighEqualDarkenPara(currentValue, targetValue);
                updateLowEqualDarkenPara(currentValue, targetValue);
            }
            this.mAnimatedStep = JND_CURVE_PARA * currentValue;
            this.mAnimatedStep *= (float) Math.pow((double) (targetValue / MAX_BRIGHTNESS), 0.09000000357627869d);
            if (durationTmp >= 20.0f && durationTmp < CALI_UPPER_BOUND_TIME) {
                durationTmp += 1.0f;
            }
            if (durationTmp < SMALL_VALUE) {
                if (HWFLOW) {
                    Slog.w(TAG, "durationTmp < SMALL_VALUE, DEFAULT_AMOUNT");
                }
                this.mAnimatedStep = DEFAULT_AMOUNT;
            } else {
                this.mAnimatedStep = ((this.mAnimatedStep * this.mStepAdjValue) * CRITERION_TIME) / durationTmp;
            }
            this.mAnimatedStep = getMinAnimatedStepByEyeSensitiveCurve(this.mAnimatedStep);
            this.mAnimatedStep = updateAnimatedStepForKeyguardLocked(this.mAnimatedStep, currentValue, targetValue);
            this.mAnimatedStep = updateAnimatedStepFromSpecialStrategy(this.mAnimatedStep, currentValue, targetValue);
            this.mAnimatedStep = updateAnimatedStepFromHbmQuit(this.mAnimatedStep, currentValue, targetValue);
            return this.mAnimatedStep;
        }
    }

    private float updateAnimatedStepFromSpecialStrategy(float animatedStep, float currentValue, float targetValue) {
        if (this.mData.isLowEqualDarkenEnable && currentValue > targetValue && targetValue < this.mData.screenBrightnessNormalModeMaxLevel && this.mAnimatedValue > this.mData.lowEqualDarkenLevel && Math.abs(this.mAnimationEqualRatioNormalModeHighMa) > SMALL_VALUE) {
            float f = this.mAnimationEqualRatioNormalModeHighMq;
            float animatedStepOut = ((1.0f - f) * this.mAnimatedValue) + (((1.0f - f) * this.mAnimationEqualRatioNormalModeHighMb) / this.mAnimationEqualRatioNormalModeHighMa);
            if (animatedStepOut >= SMALL_VALUE) {
                return animatedStepOut;
            }
            Slog.e(TAG, "Error: the animate step is invalid,defaultAnimatedStep=157.0");
            return DEFAULT_AMOUNT;
        } else if (this.mData.isLowEqualDarkenEnable && currentValue > targetValue && this.mAnimatedValue < this.mData.lowEqualDarkenLevel && Math.abs(this.mAnimationEqualRatioNormalModeMa) > SMALL_VALUE) {
            float f2 = this.mAnimationEqualRatioNormalModeMq;
            float animatedStepOut2 = ((1.0f - f2) * this.mAnimatedValue) + (((1.0f - f2) * this.mAnimationEqualRatioNormalModeMb) / this.mAnimationEqualRatioNormalModeMa);
            if (animatedStepOut2 >= SMALL_VALUE) {
                return animatedStepOut2;
            }
            Slog.e(TAG, "Error: the animate step is invalid,defaultAnimatedStep=157.0");
            return DEFAULT_AMOUNT;
        } else if (currentValue <= targetValue || targetValue >= ((float) this.mData.darkenTargetFor255) || animatedStep >= this.mData.minAnimatingStep) {
            return animatedStep;
        } else {
            if (this.mIsFirstTimeCalculateAmount && this.mData.minAnimatingStep > 0.0f) {
                float linearAvgTime = ((currentValue - targetValue) / this.mData.minAnimatingStep) * this.mTimeDelta;
                if (HWFLOW) {
                    Slog.i(TAG, "DarkenByEyeSensitive ResetAnimatedStep=" + this.mAnimatedStep + "-->animatedStepOut=" + this.mData.minAnimatingStep + ",linearAvgTime=" + linearAvgTime);
                }
            }
            return this.mData.minAnimatingStep;
        }
    }

    private float updateAnimatedStepFromHbmQuit(float animatedStep, float currentValue, float targetValue) {
        float animatedStepOut = animatedStep;
        if (!this.mData.isDarkenAnimatingStepForHbm || currentValue <= targetValue || currentValue <= this.mData.minDarkenBrightnessLevelForHbm || targetValue >= this.mData.minDarkenBrightnessLevelForHbm) {
            return animatedStepOut;
        }
        float f = this.mMinDarkenAnimatingStepForHbm;
        if (animatedStepOut < f && f > 0.0f) {
            animatedStepOut = this.mMinDarkenAnimatingStepForHbm;
            if (HWDEBUG) {
                Slog.i(TAG, "updateAnimatedStepFromHbmQuit animatedStep=" + animatedStep + "-->animatedStep=" + animatedStepOut);
            }
        } else if (animatedStepOut > this.mMaxDarkenAnimatingStepForHbm) {
            animatedStepOut = this.mMaxDarkenAnimatingStepForHbm;
            if (HWDEBUG) {
                Slog.i(TAG, "updateAnimatedStepFromHbmQuit animatedStep=" + animatedStep + "-->animatedStep=" + animatedStepOut);
            }
        } else if (HWDEBUG) {
            Slog.i(TAG, "AnimatedStepFromHbmQuit animatedStep unchanged");
        }
        return animatedStepOut;
    }

    private float updateAnimatedStepForKeyguardLocked(float animatedStep, float currentValue, float targetValue) {
        if (!this.mIsScreenLocked || currentValue <= targetValue || targetValue >= this.mData.darkenTargetForKeyguard || animatedStep >= this.mData.minAnimatingStepForKeyguard) {
            return animatedStep;
        }
        if (HWDEBUG) {
            Slog.i(TAG, "mIsScreenLocked animatedStep=" + animatedStep + ",minStep=" + this.mData.minAnimatingStepForKeyguard);
        }
        return this.mData.minAnimatingStepForKeyguard;
    }

    private float getAnimatedStepByEqualRatio(float currentValue, float targetValue, float duration) {
        float f = this.mAnimationEqualRatioMa;
        float f2 = this.mAnimationEqualRatioMb;
        float targetValueNit = (f * targetValue) + f2;
        float currentValueNit = (f * currentValue) + f2;
        if (targetValueNit < SMALL_VALUE || currentValueNit < SMALL_VALUE || this.mAnimationEqualRatioMq0 < 9.999999974752427E-7d || ((float) this.mFrameRate) < SMALL_VALUE || Math.abs(f) < SMALL_VALUE || this.mMinDarkenFrameNum < SMALL_VALUE) {
            Slog.e(TAG, "Error: the screen brightness is minus");
            return DEFAULT_AMOUNT;
        }
        if (this.mIsFirstTimeCalculateAmount) {
            float avgTime = ((float) ((Math.log((double) (targetValueNit / currentValueNit)) / Math.log(this.mAnimationEqualRatioMq0)) + 1.0d)) / ((float) this.mFrameRate);
            if (avgTime < this.mData.darkenGradualTimeMin) {
                this.mAnimationEqualRatioMq = (double) ((float) Math.pow((double) (targetValueNit / currentValueNit), (double) (1.0f / this.mMinDarkenFrameNum)));
            } else {
                this.mAnimationEqualRatioMq = this.mAnimationEqualRatioMq0;
            }
            if (HWFLOW) {
                Slog.i(TAG, "avgTime=" + avgTime + ",Mq=" + this.mAnimationEqualRatioMq + ",Ma=" + this.mAnimationEqualRatioMa + ",Mb=" + this.mAnimationEqualRatioMb + ",mAnimatedValue=" + this.mAnimatedValue);
            }
        }
        if (currentValue > targetValue) {
            double d = this.mAnimationEqualRatioMq;
            this.mAnimatedStep = ((float) ((1.0d - d) * ((double) this.mAnimatedValue))) + ((float) (((1.0d - d) * ((double) this.mAnimationEqualRatioMb)) / ((double) this.mAnimationEqualRatioMa)));
        }
        if (this.mAnimatedStep < SMALL_VALUE) {
            Slog.e(TAG, "Error: the animate step is invalid,mAnimatedStep=157.0");
            this.mAnimatedStep = DEFAULT_AMOUNT;
        }
        return this.mAnimatedStep;
    }

    private float getFirstTimeAmountByEyeSensitiveCurve(float currentValue, float targetValue, float duration) {
        float avgTime;
        float stepAdjValue;
        if (currentValue >= SMALL_VALUE || duration >= SMALL_VALUE) {
            float avgPara = ((((float) Math.pow((double) (targetValue / MAX_BRIGHTNESS), 0.09000000357627869d)) * JND_CURVE_PARA) * CRITERION_TIME) / duration;
            float f = 1.0f;
            if (Math.abs(1.0f - avgPara) >= SMALL_VALUE || Math.abs(1.0f - avgPara) >= SMALL_VALUE) {
                if (currentValue > targetValue) {
                    avgTime = ((float) (Math.log((double) (targetValue / currentValue)) / Math.log((double) (1.0f - avgPara)))) * this.mTimeDelta;
                    float avgDimmingTimeMin = this.mData.darkenGradualTimeMin;
                    if (targetValue >= ((float) this.mData.darkenTargetFor255) && targetValue < this.mData.darkenNoFlickerTarget && avgTime < this.mData.darkenNoFlickerTargetGradualTimeMin) {
                        avgDimmingTimeMin = this.mData.darkenNoFlickerTargetGradualTimeMin;
                    }
                    if (avgDimmingTimeMin < SMALL_VALUE) {
                        Slog.w(TAG, "warning: getFirstTimeAmountByEyeSensitiveCurve avgDimmingTimeMin error");
                        stepAdjValue = DEFAULT_AMOUNT;
                    } else {
                        if (avgTime < avgDimmingTimeMin) {
                            f = avgTime / avgDimmingTimeMin;
                        }
                        stepAdjValue = f;
                        Slog.i(TAG, "DarkenByEyeSensitive avgTime=" + avgTime + ",avgDimmingTimeMin=" + avgDimmingTimeMin);
                    }
                } else {
                    avgTime = this.mTimeDelta * ((float) (Math.log((double) (targetValue / currentValue)) / Math.log((double) (avgPara + 1.0f))));
                    if (avgTime < duration) {
                        f = avgTime / duration;
                    }
                    stepAdjValue = f;
                }
                if (HWFLOW) {
                    Slog.i(TAG, "getAnimatedStep avgTime= " + avgTime + ",avgPara" + avgPara + ",stepAdjValue=" + stepAdjValue + ",duration=" + duration);
                }
                return stepAdjValue;
            }
            Slog.w(TAG, "warning: getFirstTimeAmountByEyeSensitiveCurve avgPara error");
            return DEFAULT_AMOUNT;
        }
        Slog.w(TAG, "warning: getFirstTimeAmountByEyeSensitiveCurve the screen brightness is minus");
        return DEFAULT_AMOUNT;
    }

    private float getMinAnimatedStepByEyeSensitiveCurve(float animatedStep) {
        if (animatedStep >= 1.0f && this.mIsAnimatedStepRoundEnabled) {
            return (float) Math.round(animatedStep);
        }
        if (animatedStep < 1.0f && animatedStep >= 0.5f && Math.abs(this.mStepAdjValue - 1.0f) < SMALL_VALUE) {
            return 0.5f;
        }
        if (animatedStep >= 0.5f || Math.abs(this.mStepAdjValue - 1.0f) >= SMALL_VALUE) {
            return animatedStep;
        }
        return EYE_SENSITIVE_MIN_STEP;
    }

    public float getAnimatedValue() {
        float amount;
        float f;
        if (ValueAnimator.getDurationScale() == 0.0f || this.mRate == 0) {
            this.mAnimatedValue = (float) this.mTargetValue;
        } else {
            if (this.mIsAutoBrightnessMode) {
                amount = getAutoModeAnimtionAmount();
                this.mDuration = this.mAutoDuration;
            } else {
                amount = getManualModeAnimtionAmount();
                this.mDuration = this.mManualDuration;
            }
            int i = this.mTargetValue;
            if (i > this.mCurrentValue) {
                float f2 = this.mAnimatedValue;
                this.mAnimatedValue = f2 + amount < ((float) i) ? f2 + amount : (float) i;
            } else {
                float f3 = this.mAnimatedValue;
                if (f3 - amount > ((float) i)) {
                    f = f3 - amount;
                } else {
                    f = (float) i;
                }
                this.mAnimatedValue = f;
            }
        }
        return this.mAnimatedValue;
    }

    private StepType getAutoModeAnimtionStepType(float duration) {
        if (this.mIsDarkAdaptAnimationDimmingEnable) {
            return StepType.LINEAR;
        }
        if (this.mCurrentValue < this.mData.linearDimmingValueTh && this.mTargetValue < this.mData.darkenTargetFor255) {
            if (HWFLOW) {
                Slog.i(TAG, "setDimming-LINEAR,mCurrentValue=" + this.mCurrentValue + ",mTargetValue=" + this.mTargetValue);
            }
            return StepType.LINEAR;
        } else if (!this.mData.useVariableStep || ((duration < 20.0f || this.mTargetValue >= this.mCurrentValue) && (duration < 2.0f || this.mTargetValue < this.mCurrentValue))) {
            return StepType.LINEAR;
        } else {
            return StepType.CURVE;
        }
    }

    private float getAutoModeAnimtionAmount() {
        float amount;
        if (!this.mIsFirstTimeCalculateAmount) {
            return getAmount();
        }
        float duration = getAnimationTimeFromBrightnessModeState();
        if (duration <= 0.0f) {
            Slog.w(TAG, "setDimTime=FAST_TIME, duration=" + duration);
            duration = 0.5f;
        }
        this.mAutoStepType = getAutoModeAnimtionStepType(duration);
        if (this.mAutoStepType == StepType.LINEAR) {
            amount = resetDarkenLinearAmount((((float) Math.abs(this.mCurrentValue - this.mTargetValue)) / duration) * this.mTimeDelta);
        } else {
            amount = getAnimatedStepByEyeSensitiveCurve(this.mAnimatedValue, (float) this.mTargetValue, duration);
        }
        this.mAutoDuration = duration;
        this.mDecreaseFixAmount = amount;
        if (this.mCurrentValue == 0) {
            Slog.i(TAG, "mCurrentValue=0,return DEFAULT_AMOUNT,mTargetValue=" + this.mTargetValue);
            return DEFAULT_AMOUNT;
        }
        if (this.mTimeDelta > this.mTimeDeltaMinValue) {
            this.mIsFirstTimeCalculateAmount = false;
        }
        if (HWFLOW) {
            Slog.i(TAG, "AutoMode=" + this.mIsAutoBrightnessMode + ",Target=" + this.mTargetValue + ",Current=" + this.mCurrentValue + ",amount=" + amount + ",duration=" + duration + ",StepType=" + this.mAutoStepType + ",mTimeDelta=" + this.mTimeDelta);
        }
        this.mIsDarkAdaptAnimationDimmingEnable = false;
        return amount;
    }

    private boolean getFrontCameraDimmingEnable() {
        if (this.mData.frontCameraMaxBrightnessEnable && this.mIsFrontCameraDimmingEnable && !this.mIsFastAnimtionFlag && !this.mIsScreenLocked) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public class RebootFirstAnimation implements BrightenAnimationTime {
        private BrightnessModeState mode;

        private RebootFirstAnimation() {
            this.mode = BrightnessModeState.REBOOT_FIRST_ANIMATION;
        }

        @Override // com.android.server.display.HwGradualBrightnessAlgo.BrightenAnimationTime
        public float getBrightenAnimationTime() {
            HwGradualBrightnessAlgo.this.mIsFirstRebootAnimationEnable = false;
            return HwGradualBrightnessAlgo.this.mData.rebootFirstBrightnessAutoTime;
        }
    }

    /* access modifiers changed from: private */
    public class BrightessModeAnimation implements BrightenAnimationTime {
        private BrightnessModeState mode;

        private BrightessModeAnimation() {
            this.mode = BrightnessModeState.BRIGHTNESS_MODE_ANIMATION;
        }

        @Override // com.android.server.display.HwGradualBrightnessAlgo.BrightenAnimationTime
        public float getBrightenAnimationTime() {
            return ((float) HwGradualBrightnessAlgo.this.mBrightnessModeAnimationTime) / HwGradualBrightnessAlgo.TIME_MILLIS_FACTOR;
        }
    }

    /* access modifiers changed from: private */
    public class AppSetAnimation implements BrightenAnimationTime {
        private BrightnessModeState mode;

        private AppSetAnimation() {
            this.mode = BrightnessModeState.APP_SET_ANIMATION;
        }

        @Override // com.android.server.display.HwGradualBrightnessAlgo.BrightenAnimationTime
        public float getBrightenAnimationTime() {
            return ((float) HwGradualBrightnessAlgo.this.mMillisecond) / HwGradualBrightnessAlgo.TIME_MILLIS_FACTOR;
        }
    }

    /* access modifiers changed from: private */
    public class PowerSavingUseManualAnimation implements BrightenAnimationTime {
        private BrightnessModeState mode;

        private PowerSavingUseManualAnimation() {
            this.mode = BrightnessModeState.POWERSAVING_USE_MANUAL;
        }

        @Override // com.android.server.display.HwGradualBrightnessAlgo.BrightenAnimationTime
        public float getBrightenAnimationTime() {
            return HwGradualBrightnessAlgo.this.getPowerSavingManaulUseTime();
        }
    }

    /* access modifiers changed from: private */
    public class CoverModeFastAnimation implements BrightenAnimationTime {
        private BrightnessModeState mode;

        private CoverModeFastAnimation() {
            this.mode = BrightnessModeState.COVER_MODE_FAST_RESEPONSE;
        }

        @Override // com.android.server.display.HwGradualBrightnessAlgo.BrightenAnimationTime
        public float getBrightenAnimationTime() {
            return HwGradualBrightnessAlgo.this.mCoverModeAnimationTime;
        }
    }

    /* access modifiers changed from: private */
    public class FrontCameraAnimation implements BrightenAnimationTime {
        private BrightnessModeState mode;

        private FrontCameraAnimation() {
            this.mode = BrightnessModeState.FRONT_CAMERA;
        }

        @Override // com.android.server.display.HwGradualBrightnessAlgo.BrightenAnimationTime
        public float getBrightenAnimationTime() {
            return HwGradualBrightnessAlgo.this.getFrontCameraTime();
        }
    }

    /* access modifiers changed from: private */
    public class FirstBrightnessAnimation implements BrightenAnimationTime {
        private BrightnessModeState mode;

        private FirstBrightnessAnimation() {
            this.mode = BrightnessModeState.FIRST_BRIGHTNESS_ANIMATION;
        }

        @Override // com.android.server.display.HwGradualBrightnessAlgo.BrightenAnimationTime
        public float getBrightenAnimationTime() {
            return HwGradualBrightnessAlgo.this.getPowerOnTime();
        }
    }

    /* access modifiers changed from: private */
    public class KeyguardUnlockAnimation implements BrightenAnimationTime {
        private BrightnessModeState mode;

        private KeyguardUnlockAnimation() {
            this.mode = BrightnessModeState.KEYGUARD_UNLOCK;
        }

        @Override // com.android.server.display.HwGradualBrightnessAlgo.BrightenAnimationTime
        public float getBrightenAnimationTime() {
            return HwGradualBrightnessAlgo.this.mData.keyguardUnlockedDimmingTime;
        }
    }

    /* access modifiers changed from: private */
    public class FastDarkAnimation implements BrightenAnimationTime {
        private BrightnessModeState mode;

        private FastDarkAnimation() {
            this.mode = BrightnessModeState.FAST_DARK;
        }

        @Override // com.android.server.display.HwGradualBrightnessAlgo.BrightenAnimationTime
        public float getBrightenAnimationTime() {
            return HwGradualBrightnessAlgo.this.mData.resetAmbientLuxFastDarkenDimmingTime;
        }
    }

    /* access modifiers changed from: private */
    public class GameModeAnimation implements BrightenAnimationTime {
        private BrightnessModeState mode;

        private GameModeAnimation() {
            this.mode = BrightnessModeState.GAME_MODE;
        }

        @Override // com.android.server.display.HwGradualBrightnessAlgo.BrightenAnimationTime
        public float getBrightenAnimationTime() {
            return HwGradualBrightnessAlgo.this.getGameModeAnimationTime();
        }
    }

    /* access modifiers changed from: private */
    public class CameraModeAnimation implements BrightenAnimationTime {
        private BrightnessModeState mode;

        private CameraModeAnimation() {
            this.mode = BrightnessModeState.CAMERA_MODE;
        }

        @Override // com.android.server.display.HwGradualBrightnessAlgo.BrightenAnimationTime
        public float getBrightenAnimationTime() {
            return HwGradualBrightnessAlgo.this.mData.cameraAnimationTime;
        }
    }

    /* access modifiers changed from: private */
    public class ReadingModeAnimation implements BrightenAnimationTime {
        private BrightnessModeState mode;

        private ReadingModeAnimation() {
            this.mode = BrightnessModeState.READING_MODE;
        }

        @Override // com.android.server.display.HwGradualBrightnessAlgo.BrightenAnimationTime
        public float getBrightenAnimationTime() {
            return HwGradualBrightnessAlgo.this.mData.readingAnimationTime;
        }
    }

    /* access modifiers changed from: private */
    public class DefaultModeAnimation implements BrightenAnimationTime {
        private BrightnessModeState mode;

        private DefaultModeAnimation() {
            this.mode = BrightnessModeState.DEFAULT_MDOE;
        }

        @Override // com.android.server.display.HwGradualBrightnessAlgo.BrightenAnimationTime
        public float getBrightenAnimationTime() {
            return HwGradualBrightnessAlgo.this.getDefaultModeTime();
        }
    }

    private BrightnessModeState getBrightnessModeState() {
        if (this.mData.rebootFirstBrightnessAnimationEnable && this.mIsFirstRebootAnimationEnable) {
            return BrightnessModeState.REBOOT_FIRST_ANIMATION;
        }
        if (this.mIsBrightnessModeAnimationEnable) {
            return BrightnessModeState.BRIGHTNESS_MODE_ANIMATION;
        }
        if (this.mIsAnimationEnable) {
            return BrightnessModeState.APP_SET_ANIMATION;
        }
        if (this.mIsCoverModeAnimationEnable) {
            return BrightnessModeState.COVER_MODE_FAST_RESEPONSE;
        }
        if (this.mData.autoPowerSavingUseManualAnimationTimeEnable && this.mIsManualPowerSavingAnimationEnable) {
            return BrightnessModeState.POWERSAVING_USE_MANUAL;
        }
        if (getFrontCameraDimmingEnable()) {
            return BrightnessModeState.FRONT_CAMERA;
        }
        if (this.mIsFirstValidAutoBrightness || this.mIsAutoBrightnessIntervened || this.mIsFastAnimtionFlag) {
            return BrightnessModeState.FIRST_BRIGHTNESS_ANIMATION;
        }
        if (isKeyguardUnlockedFastDarkenEnable()) {
            return BrightnessModeState.KEYGUARD_UNLOCK;
        }
        if (isFastDarkenDimmingEnable()) {
            return BrightnessModeState.FAST_DARK;
        }
        if (this.mIsGameModeEnable) {
            return BrightnessModeState.GAME_MODE;
        }
        if (this.mIsCameraModeEnable) {
            return BrightnessModeState.CAMERA_MODE;
        }
        if (this.mIsReadingModeEnable) {
            return BrightnessModeState.READING_MODE;
        }
        return BrightnessModeState.DEFAULT_MDOE;
    }

    private boolean isKeyguardUnlockedFastDarkenEnable() {
        if (!this.mIsKeyguardUnlockedFastDarkenDimmingEnable || this.mTargetValue >= this.mCurrentValue || this.mData.keyguardUnlockedDimmingTime <= 0.0f) {
            return false;
        }
        return true;
    }

    private boolean isFastDarkenDimmingEnable() {
        if (!this.mIsFastDarkenDimmingEnable || this.mTargetValue >= this.mCurrentValue || this.mData.resetAmbientLuxFastDarkenDimmingTime <= 0.0f) {
            return false;
        }
        Slog.i(TAG, "mIsFastDarkenDimmingEnable enable=" + this.mIsFastDarkenDimmingEnable);
        return true;
    }

    private float getAnimationTimeFromBrightnessModeState() {
        if (this.mBrightenAnimationMap == null) {
            Slog.w(TAG, "mBrightenAnimationMap== null, FAST_TIME");
            return 0.5f;
        }
        BrightnessModeState animationMode = getBrightnessModeState();
        BrightenAnimationTime animationTimeClass = this.mBrightenAnimationMap.get(animationMode);
        float animationTime = 0.5f;
        if (animationTimeClass != null) {
            animationTime = animationTimeClass.getBrightenAnimationTime();
        }
        if (HWFLOW) {
            Slog.i(TAG, "AnimationMode=" + animationMode + ",animationTime=" + animationTime);
        }
        return animationTime;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float getPowerSavingManaulUseTime() {
        float animationTime = this.mTargetValue < this.mCurrentValue ? this.mData.manualPowerSavingAnimationDarkenTime : this.mData.manualPowerSavingAnimationBrightenTime;
        this.mIsManualPowerSavingAnimationEnable = false;
        return animationTime;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float getGameModeAnimationTime() {
        float duration;
        int i = this.mTargetValue;
        if (i >= this.mCurrentValue) {
            duration = this.mData.gameModeBrightenAnimationTime;
        } else if (i >= this.mData.gameModeDarkentenLongTarget || this.mCurrentValue <= this.mData.gameModeDarkentenLongTarget) {
            duration = this.mData.gameModeDarkentenAnimationTime;
        } else {
            Slog.i(TAG, "GameModeEnable mTargetValue=" + this.mTargetValue + ",mCurrentValue=" + this.mCurrentValue);
            duration = this.mData.gameModeDarkentenLongAnimationTime;
        }
        Slog.i(TAG, "GameModeEnable AnimationTime=" + duration);
        return duration;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float getPowerOnTime() {
        float duration = 0.5f;
        if (this.mIsFirstValidAutoBrightness && !this.mIsAutoBrightnessIntervened && this.mIsNightUpPowerOnWithDimmingEnable && this.mData.nightUpModeEnable && this.mIsFastAnimtionFlag) {
            duration = this.mData.nightUpModePowOnDimTime;
            Slog.i(TAG, "NightUpBrightMode dimmingTime duration=" + duration);
        }
        if (this.mIsAutoBrightnessIntervened) {
            duration = this.mData.seekBarDimTime;
        }
        if (this.mIsFirstValidAutoBrightness && this.mData.resetAmbientLuxGraTime > 0.0f) {
            duration = this.mData.resetAmbientLuxGraTime;
            if (HWFLOW) {
                Slog.i(TAG, "resetAmbientLuxGraTime=" + duration);
            }
        }
        if (HWFLOW) {
            Slog.i(TAG, "mIsFirstValidAutoBrightness=" + this.mIsFirstValidAutoBrightness + ",mIsAutoBrightnessIntervened=" + this.mIsAutoBrightnessIntervened + ",mIsFastAnimtionFlag=" + this.mIsFastAnimtionFlag);
        }
        return duration;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float getFrontCameraTime() {
        float animationTime = this.mTargetValue < this.mCurrentValue ? this.mData.frontCameraDimmingDarkenTime : this.mData.frontCameraDimmingBrightenTime;
        this.mIsManualPowerSavingAnimationEnable = false;
        return animationTime;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float getDefaultModeTime() {
        return this.mTargetValue < this.mCurrentValue ? getAutoModeDarkTime() : getAutoModeBrightTime();
    }

    private float resetDarkenLinearAmount(float linearAmount) {
        int i = this.mCurrentValue;
        int i2 = this.mTargetValue;
        if (i <= i2 || i2 >= this.mData.darkenTargetFor255 || linearAmount >= this.mData.minAnimatingStep || this.mData.minAnimatingStep <= 0.0f) {
            return linearAmount;
        }
        float avgTime = (((float) (this.mCurrentValue - this.mTargetValue)) / this.mData.minAnimatingStep) * this.mTimeDelta;
        Slog.i(TAG, "resetDarkenLinearAmount=" + linearAmount + "-->amount=" + this.mData.minAnimatingStep + ",resetAvgTime=" + avgTime);
        return this.mData.minAnimatingStep;
    }

    /* access modifiers changed from: package-private */
    public float getDuration() {
        return this.mDuration;
    }

    private float getAmount() {
        if (this.mAutoStepType == StepType.LINEAR) {
            return this.mDecreaseFixAmount;
        }
        return getAnimatedStepByEyeSensitiveCurve(this.mAnimatedValue, (float) this.mTargetValue, this.mAutoDuration);
    }

    private float getAutoModeDarkTime() {
        float duration = this.mData.useVariableStep ? this.mData.darkenGradualTimeMax : this.mData.darkenGradualTime;
        if (this.mIsDarkAdaptAnimationDimmingEnable) {
            duration = (float) this.mData.unadapt2AdaptingDimSec;
            if (HWFLOW) {
                Slog.i(TAG, "unadapt2AdaptingDimSec = " + duration);
            }
        }
        if (this.mIsOutdoorAnimationFlag && this.mData.outdoorAnimationDarkenTime > 0.0f) {
            duration = this.mData.outdoorAnimationDarkenTime;
            if (HWFLOW) {
                Slog.i(TAG, "outdoorAnimationDarkenTime = " + duration);
            }
        }
        if (this.mIsScreenLocked && this.mData.keyguardAnimationDarkenTime > 0.0f) {
            duration = this.mData.keyguardAnimationDarkenTime;
            if (HWFLOW) {
                Slog.i(TAG, "keyguardAnimationDarkenTime=" + duration);
            }
        }
        if (this.mTargetValue < this.mData.darkenTargetFor255 && this.mCurrentValue < this.mData.darkenCurrentFor255 && (Math.abs(duration - this.mData.darkenGradualTime) < SMALL_VALUE || Math.abs(duration - this.mData.darkenGradualTimeMax) < SMALL_VALUE)) {
            duration = 0.5f;
        }
        return updateAnimationTimFromDimState(duration);
    }

    private float updateAnimationTimFromDimState(float duration) {
        float durationTmp = duration;
        if (this.mIsPowerDimState) {
            if (this.mIsScreenLocked && this.mCurrentValue < this.mData.keyguardFastDimBrightness) {
                durationTmp = this.mData.keyguardFastDimTime;
                Slog.i(TAG, "mIsScreenLocked,mCurrentValue=" + this.mCurrentValue);
            } else if (this.mData.darkenGradualTime > this.mData.dimTime || (this.mData.useVariableStep && this.mData.darkenGradualTimeMax > this.mData.dimTime)) {
                durationTmp = this.mData.dimTime;
            } else {
                durationTmp = duration;
            }
            if (HWFLOW) {
                Slog.i(TAG, "The Dim state");
            }
        }
        if (this.mIsPowerDimRecoveryState) {
            this.mIsPowerDimRecoveryState = false;
            durationTmp = 0.5f;
            if (HWFLOW) {
                Slog.i(TAG, "The Dim state Recovery");
            }
        }
        return durationTmp;
    }

    private float getAutoModeBrightTime() {
        float duration = this.mData.brightenGradualTime;
        if (this.mIsPowerDimRecoveryState) {
            this.mIsPowerDimRecoveryState = false;
            if (HWFLOW) {
                Slog.i(TAG, "The Dim state Recovery");
            }
            return 0.5f;
        } else if (this.mIsScreenLocked && this.mData.keyguardAnimationBrightenTime > 0.0f) {
            float duration2 = this.mData.keyguardAnimationBrightenTime;
            if (HWFLOW) {
                Slog.i(TAG, "keyguardAnimationBrightenTime=" + duration2);
            }
            return duration2;
        } else if (this.mIsOutdoorAnimationFlag && this.mData.outdoorAnimationBrightenTime > 0.0f) {
            float duration3 = this.mData.outdoorAnimationBrightenTime;
            if (HWFLOW) {
                Slog.i(TAG, "outdoorAnimationBrightenTime=" + duration3);
            }
            return duration3;
        } else if (this.mTargetValue < this.mData.darkenTargetFor255) {
            return getFlickerBrightenDimmingTime();
        } else {
            if (this.mCurrentValue < this.mData.brightenTimeLongCurrentTh && this.mTargetValue - this.mCurrentValue > this.mData.brightenTimeLongAmountMin) {
                duration = this.mData.brightenGradualTimeLong;
                if (HWFLOW) {
                    Slog.i(TAG, "mCurrentValue=" + this.mCurrentValue + ",LongCurrentTh=" + this.mData.brightenTimeLongCurrentTh + ",brightenGradualTimeLong=" + duration);
                }
            }
            return duration;
        }
    }

    private float getFlickerBrightenDimmingTime() {
        float dimmingTime;
        if (this.mData.brightenFlickerTargetMin > 156 && this.mData.brightenFlickerGradualTimeMax - this.mData.brightenFlickerGradualTimeMin > 0.0f) {
            if (this.mTargetValue - this.mCurrentValue <= this.mData.brightenFlickerAmountMin || this.mTargetValue <= this.mData.brightenFlickerTargetMin || ((float) (this.mData.darkenTargetFor255 - this.mData.brightenFlickerTargetMin)) <= SMALL_VALUE) {
                dimmingTime = this.mData.brightenFlickerGradualTimeMin;
            } else {
                dimmingTime = ((((float) (this.mTargetValue - this.mData.brightenFlickerTargetMin)) * (this.mData.brightenFlickerGradualTimeMax - this.mData.brightenFlickerGradualTimeMin)) / ((float) (this.mData.darkenTargetFor255 - this.mData.brightenFlickerTargetMin))) + this.mData.brightenFlickerGradualTimeMin;
            }
            Slog.i(TAG, "FlickerBrightenTime=" + dimmingTime + ",deltaAmount=" + (this.mTargetValue - this.mCurrentValue));
            return dimmingTime;
        } else if (this.mData.targetFor255BrightenTime <= 0.0f || this.mData.targetFor255BrightenTime > this.mData.brightenGradualTime) {
            return 0.5f;
        } else {
            return this.mData.targetFor255BrightenTime;
        }
    }

    private float getManualModeTime() {
        float duration;
        float f;
        if (this.mIsFirstRebootAnimationEnable && this.mData.rebootFirstBrightnessAnimationEnable) {
            this.mIsFirstRebootAnimationEnable = false;
            duration = this.mData.rebootFirstBrightnessManualTime;
            Slog.i(TAG, "The mIsFirstRebootAnimationEnable state,duration=" + duration);
        } else if (this.mIsManualModeAnimationEnable) {
            if (this.mTargetValue < this.mCurrentValue) {
                duration = this.mData.manualAnimationDarkenTime;
            } else {
                duration = this.mData.manualAnimationBrightenTime;
            }
            if (HWFLOW) {
                Slog.i(TAG, "The mIsManualModeAnimationEnable state,duration=" + duration);
            }
        } else {
            duration = updateAnimationTimeFromManualPowerSaving(this.mData.seekBarDimTime);
        }
        float duration2 = updateAnimationTimeFromFrontCamera(duration);
        if (this.mIsAnimationEnable) {
            duration2 = ((float) this.mMillisecond) / TIME_MILLIS_FACTOR;
            if (HWFLOW) {
                Slog.i(TAG, "The mIsAnimationEnable state,duration=" + duration2);
            }
        }
        if (this.mIsManualThermalModeAnimationEnable) {
            if (this.mTargetValue < this.mCurrentValue) {
                f = this.mData.manualPowerSavingAnimationDarkenTime;
            } else {
                f = this.mData.manualPowerSavingAnimationBrightenTime;
            }
            duration2 = f;
            this.mIsManualThermalModeAnimationEnable = false;
            if (HWFLOW) {
                Slog.i(TAG, "The mIsManualThermalModeAnimationEnable state,duration=" + duration2);
            }
        }
        if (this.mIsBrightnessModeAnimationEnable) {
            duration2 = ((float) this.mBrightnessModeAnimationTime) / TIME_MILLIS_FACTOR;
            this.mIsBrightnessModeAnimationEnable = false;
            if (HWFLOW) {
                Slog.i(TAG, "The manual mIsBrightnessModeAnimationEnable state,duration=" + duration2);
            }
        }
        return updateAnimationTimFromDimState(duration2);
    }

    private float updateAnimationTimeFromManualPowerSaving(float duration) {
        float durationTmp;
        float durationTmp2 = duration;
        if (this.mData.isSeekBarManualDimTimeEnable) {
            if (this.mTargetValue < this.mCurrentValue) {
                durationTmp = this.mData.seekBarManualDarkenDimTime;
            } else {
                durationTmp = this.mData.seekBarManualBrightenDimTime;
            }
            Slog.i(TAG, "The seekBarManual state,durationTmp=" + durationTmp);
            return durationTmp;
        }
        if (this.mIsManualPowerSavingAnimationEnable) {
            if (this.mTargetValue < this.mCurrentValue) {
                durationTmp2 = this.mData.manualPowerSavingAnimationDarkenTime;
            } else {
                durationTmp2 = this.mData.manualPowerSavingAnimationBrightenTime;
            }
            this.mIsManualPowerSavingAnimationEnable = false;
            if (HWFLOW) {
                Slog.i(TAG, "The mIsManualPowerSavingAnimationEnable state,duration=" + durationTmp2);
            }
        }
        return durationTmp2;
    }

    private float updateAnimationTimeFromFrontCamera(float duration) {
        float durationTmp = duration;
        if (this.mData.frontCameraMaxBrightnessEnable && this.mIsFrontCameraDimmingEnable) {
            if (this.mTargetValue < this.mCurrentValue) {
                durationTmp = this.mData.frontCameraDimmingDarkenTime;
            } else {
                durationTmp = this.mData.frontCameraDimmingBrightenTime;
            }
            if (HWFLOW) {
                Slog.i(TAG, "frontCameraDimming duration=" + durationTmp);
            }
        }
        return durationTmp;
    }

    private float getManualModeAnimtionAmount() {
        if (!this.mIsFirstTimeCalculateAmount) {
            return this.mDecreaseFixAmount;
        }
        float duration = getManualModeTime();
        if (duration <= 0.0f) {
            Slog.w(TAG, "setDimTime=FAST_TIME, duration=" + duration);
            duration = 0.5f;
        }
        this.mManualDuration = duration;
        float f = this.mTimeDelta;
        float amount = (((float) Math.abs(this.mCurrentValue - this.mTargetValue)) / duration) * f;
        this.mDecreaseFixAmount = amount;
        if (f > this.mTimeDeltaMinValue) {
            this.mIsFirstTimeCalculateAmount = false;
        }
        if (!HWFLOW) {
            return amount;
        }
        Slog.i(TAG, "AutoMode=" + this.mIsAutoBrightnessMode + ",Target=" + this.mTargetValue + ",Current=" + this.mCurrentValue + ",amount=" + amount + ",duration=" + duration);
        return amount;
    }

    /* access modifiers changed from: package-private */
    public void updateTargetAndRate(int target, int rate) {
        if (this.mTargetValue != target) {
            this.mIsFirstTimeCalculateAmount = true;
        }
        if (this.mIsChangeToAutoBrightnessMode) {
            if (!this.mIsFirstTimeCalculateAmount && this.mTargetValue != this.mCurrentValue) {
                this.mIsFirstTimeCalculateAmount = true;
                Slog.i(TAG, "set mIsFirstTimeCalculateAmount mTargetValue=" + this.mTargetValue + ",mCurrentValue=" + this.mCurrentValue);
            }
            this.mIsChangeToAutoBrightnessMode = false;
        }
        this.mTargetValue = target;
        this.mRate = rate;
    }

    /* access modifiers changed from: package-private */
    public void updateCurrentBrightnessValue(float currentValue) {
        this.mCurrentValue = Math.round(currentValue);
        this.mAnimatedValue = currentValue;
    }

    /* access modifiers changed from: package-private */
    public void setPowerDimState(int state) {
        this.mIsPowerDimState = state == 2;
        if (this.mIsPowerDimState) {
            this.mIsFirstValidAutoBrightness = false;
        }
        if (this.mState == 2 && state == 3) {
            this.mIsPowerDimRecoveryState = true;
        }
        this.mState = state;
    }

    /* access modifiers changed from: package-private */
    public void updateAdjustMode(boolean isAutoBrightnessMode) {
        if (isAutoBrightnessMode != this.mIsAutoBrightnessMode && isAutoBrightnessMode) {
            this.mIsChangeToAutoBrightnessMode = true;
            if (HWFLOW) {
                Slog.i(TAG, "updateAdjustMode, isAutoBrightnessMode=" + isAutoBrightnessMode + ",mIsChangeToAutoBrightnessMode=" + this.mIsChangeToAutoBrightnessMode);
            }
        }
        this.mIsAutoBrightnessMode = isAutoBrightnessMode;
    }

    /* access modifiers changed from: package-private */
    public void autoModeIsIntervened(boolean isAutoBrightnessIntervened) {
        this.mIsAutoBrightnessIntervened = isAutoBrightnessIntervened;
    }

    /* access modifiers changed from: package-private */
    public void setFirstValidAutoBrightness(boolean isFirstValidAutoBrightness) {
        this.mIsFirstValidAutoBrightness = isFirstValidAutoBrightness;
    }

    /* access modifiers changed from: package-private */
    public void updateFastAnimationFlag(boolean isFastAnimtionFlag) {
        this.mIsFastAnimtionFlag = isFastAnimtionFlag;
    }

    /* access modifiers changed from: package-private */
    public void updateCoverModeFastAnimationFlag(boolean isCoverModeAnimationEnable) {
        this.mIsCoverModeAnimationEnable = isCoverModeAnimationEnable;
    }

    /* access modifiers changed from: package-private */
    public void updateCameraModeChangeAnimationEnable(boolean isCameraModeEnable) {
        this.mIsCameraModeEnable = isCameraModeEnable;
    }

    /* access modifiers changed from: package-private */
    public void updateGameModeChangeAnimationEnable(boolean isGameModeEnable) {
        this.mIsGameModeEnable = isGameModeEnable;
    }

    /* access modifiers changed from: package-private */
    public void updateReadingModeChangeAnimationEnable(boolean isReadingModeEnable) {
        this.mIsReadingModeEnable = isReadingModeEnable;
    }

    /* access modifiers changed from: package-private */
    public void setBrightnessAnimationTime(boolean isAnimationEnable, int millisecond) {
        this.mIsAnimationEnable = isAnimationEnable;
        this.mMillisecond = millisecond;
        if (this.mMillisecond < 0) {
            this.mIsAnimationEnable = false;
            Slog.e(TAG, "error input mMillisecond=" + this.mMillisecond + ",set mIsAnimationEnable=" + this.mIsAnimationEnable);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateScreenLockedAnimationEnable(boolean isScreenLockedEnable) {
        this.mIsScreenLocked = isScreenLockedEnable;
    }

    /* access modifiers changed from: package-private */
    public void updateOutdoorAnimationFlag(boolean isOutdoorAnimationFlag) {
        this.mIsOutdoorAnimationFlag = isOutdoorAnimationFlag;
    }

    /* access modifiers changed from: package-private */
    public void updateManualModeAnimationEnable(boolean isManualModeAnimationEnable) {
        this.mIsManualModeAnimationEnable = isManualModeAnimationEnable;
    }

    /* access modifiers changed from: package-private */
    public void updateManualPowerSavingAnimationEnable(boolean isManualPowerSavingAnimationEnable) {
        this.mIsManualPowerSavingAnimationEnable = isManualPowerSavingAnimationEnable;
    }

    /* access modifiers changed from: package-private */
    public void updateManualThermalModeAnimationEnable(boolean isManualThermalModeAnimationEnable) {
        this.mIsManualThermalModeAnimationEnable = isManualThermalModeAnimationEnable;
    }

    /* access modifiers changed from: package-private */
    public void updateBrightnessModeAnimationEnable(boolean isAnimationEnable, int time) {
        this.mBrightnessModeAnimationTime = time;
        if (time < 0) {
            this.mIsBrightnessModeAnimationEnable = false;
            Slog.e(TAG, "error input time,time=,set mIsBrightnessModeAnimationEnable=" + this.mIsBrightnessModeAnimationEnable);
        }
        this.mIsBrightnessModeAnimationEnable = isAnimationEnable;
    }

    /* access modifiers changed from: package-private */
    public void updateDarkAdaptAnimationDimmingEnable(boolean isDarkAdaptAnimationDimmingEnable) {
        this.mIsDarkAdaptAnimationDimmingEnable = isDarkAdaptAnimationDimmingEnable;
    }

    /* access modifiers changed from: package-private */
    public void updateFastDarkenDimmingEnable(boolean isFastDarkenDimmingEnable) {
        if (isFastDarkenDimmingEnable != this.mIsFastDarkenDimmingEnable && HWFLOW) {
            Slog.i(TAG, "updateFastDarkenDimmingEnable enable=" + isFastDarkenDimmingEnable);
        }
        this.mIsFastDarkenDimmingEnable = isFastDarkenDimmingEnable;
    }

    /* access modifiers changed from: package-private */
    public void updateKeyguardUnlockedFastDarkenDimmingEnable(boolean isKeyguardUnlockedFastDarkenDimmingEnable) {
        if (isKeyguardUnlockedFastDarkenDimmingEnable != this.mIsKeyguardUnlockedFastDarkenDimmingEnable && HWFLOW) {
            Slog.i(TAG, "updateKeyguardUnlockedFastDarkenDimmingEnable enable=" + isKeyguardUnlockedFastDarkenDimmingEnable);
        }
        this.mIsKeyguardUnlockedFastDarkenDimmingEnable = isKeyguardUnlockedFastDarkenDimmingEnable;
    }

    public void updateNightUpPowerOnWithDimmingEnable(boolean isNightUpPowerOnWithDimmingEnable) {
        if (isNightUpPowerOnWithDimmingEnable != this.mIsNightUpPowerOnWithDimmingEnable && isNightUpPowerOnWithDimmingEnable && HWFLOW) {
            Slog.i(TAG, "NightUpBrightMode mIsNightUpPowerOnWithDimmingEnable=" + this.mIsNightUpPowerOnWithDimmingEnable + "-->enable=" + isNightUpPowerOnWithDimmingEnable);
        }
        this.mIsNightUpPowerOnWithDimmingEnable = isNightUpPowerOnWithDimmingEnable;
    }

    public void updateFrameRate(int frameRate) {
        if (frameRate <= 0) {
            Slog.w(TAG, "setDefault frameRate, frameRate=" + frameRate);
            this.mTimeDeltaMinValue = FRAME_RATE_LEVEL_0_TIME_DELTA_MIN;
            this.mTimeDelta = TIME_DELTA;
            this.mFrameRate = 60;
            updateAnimationParaFromFrameRate();
            return;
        }
        if (frameRate == 60) {
            this.mTimeDeltaMinValue = FRAME_RATE_LEVEL_0_TIME_DELTA_MIN;
        } else if (frameRate == FRAME_RATE_LEVEL_1) {
            this.mTimeDeltaMinValue = FRAME_RATE_LEVEL_1_TIME_DELTA_MIN;
        } else if (frameRate == 120) {
            this.mTimeDeltaMinValue = FRAME_RATE_LEVEL_2_TIME_DELTA_MIN;
        } else {
            Slog.w(TAG, "updateFrameRate, frameRate not match, brightness dimmingTime is not accurate");
        }
        this.mTimeDelta = 1.0f / ((float) frameRate);
        if (frameRate != this.mFrameRate) {
            if (HWFLOW) {
                Slog.i(TAG, "updateFrameRate, mTimeDeltaMinValue=" + this.mTimeDeltaMinValue + ",mTimeDelta=" + this.mTimeDelta + ",frameRate=" + frameRate);
            }
            this.mFrameRate = frameRate;
            updateAnimationParaFromFrameRate();
        }
    }

    private void updateAnimationParaFromFrameRate() {
        if (this.mData.animationEqualRatioEnable) {
            updateAnimationEqualRatioPara();
        }
        if (this.mData.isLowEqualDarkenEnable) {
            if (this.mData.isHighEqualDarkenEnable) {
                updateHighEqualParaFromFrameRate();
            }
            updateLowEqualParaFromFrameRate();
        }
        if (this.mData.isDarkenAnimatingStepForHbm) {
            updateDarkenAnimatingStepForHbm();
        }
    }

    public void updateFrontCameraDimmingEnable(boolean isFrontCameraDimmingEnable) {
        if (isFrontCameraDimmingEnable != this.mIsFrontCameraDimmingEnable) {
            if (HWFLOW) {
                Slog.i(TAG, "updateFrontCameraDimmingEnable=" + isFrontCameraDimmingEnable);
            }
            this.mIsFrontCameraDimmingEnable = isFrontCameraDimmingEnable;
        }
    }

    /* access modifiers changed from: package-private */
    public void clearAnimatedValuePara() {
        this.mIsFirstValidAutoBrightness = false;
        this.mIsAutoBrightnessIntervened = false;
        this.mIsPowerDimState = false;
        this.mIsPowerDimRecoveryState = false;
        this.mIsFastAnimtionFlag = false;
        this.mIsCoverModeAnimationEnable = false;
        this.mIsCameraModeEnable = false;
        this.mIsReadingModeEnable = false;
        this.mIsOutdoorAnimationFlag = false;
        this.mIsManualModeAnimationEnable = false;
        this.mIsManualPowerSavingAnimationEnable = false;
        this.mIsManualThermalModeAnimationEnable = false;
        this.mIsFirstRebootAnimationEnable = false;
        this.mIsBrightnessModeAnimationEnable = false;
        this.mIsDarkAdaptAnimationDimmingEnable = false;
        this.mIsGameModeEnable = false;
        this.mIsFrontCameraDimmingEnable = false;
    }
}
