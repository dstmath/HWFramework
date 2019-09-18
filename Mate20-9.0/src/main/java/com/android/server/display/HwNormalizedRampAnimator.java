package com.android.server.display;

import android.os.Bundle;
import android.util.IntProperty;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwBrightnessXmlLoader;
import com.android.server.gesture.GestureNavConst;
import com.huawei.displayengine.DisplayEngineManager;

public final class HwNormalizedRampAnimator<T> extends RampAnimator<T> {
    private static final int DEFAULT_MAX_BRIGHTNESS = 255;
    private static final int DEFAULT_MIN_BRIGHTNESS = 4;
    private static final int HIGH_PRECISION_MAX_BRIGHTNESS = 10000;
    /* access modifiers changed from: private */
    public boolean DEBUG;
    /* access modifiers changed from: private */
    public boolean DEBUG_CONTROLLER = false;
    /* access modifiers changed from: private */
    public String TAG = "HwNormalizedRampAnimator";
    private boolean mBrightnessAdjustMode;
    /* access modifiers changed from: private */
    public final HwBrightnessXmlLoader.Data mData;
    /* access modifiers changed from: private */
    public DisplayEngineManager mDisplayEngineManager;
    private Bundle mHBMData;
    /* access modifiers changed from: private */
    public HwGradualBrightnessAlgo mHwGradualBrightnessAlgo;
    /* access modifiers changed from: private */
    public boolean mModeOffForRGBW;
    private final Runnable mNormalizedAnimationCallback;
    private boolean mProximityState;
    /* access modifiers changed from: private */
    public boolean mProximityStateRecovery;
    /* access modifiers changed from: private */
    public int mTargetValueChange;
    private int mTargetValueForRGBW;
    private int mTargetValueLast;

    public HwNormalizedRampAnimator(T object, IntProperty<T> property) {
        super(object, property);
        this.DEBUG = Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(this.TAG, 4));
        this.mBrightnessAdjustMode = false;
        this.mProximityState = false;
        this.mProximityStateRecovery = false;
        this.mModeOffForRGBW = true;
        this.mTargetValueLast = -1;
        this.mNormalizedAnimationCallback = new Runnable() {
            public void run() {
                HwNormalizedRampAnimator.this.mAnimatedValue = HwNormalizedRampAnimator.this.mHwGradualBrightnessAlgo.getAnimatedValue();
                HwNormalizedRampAnimator.this.updateHBMData(HwNormalizedRampAnimator.this.mTargetValue, HwNormalizedRampAnimator.this.mRate, HwNormalizedRampAnimator.this.mHwGradualBrightnessAlgo.getDuration());
                HwNormalizedRampAnimator.this.mHwGradualBrightnessAlgo.updateCurrentBrightnessValue(HwNormalizedRampAnimator.this.mAnimatedValue);
                int oldCurrentValue = HwNormalizedRampAnimator.this.mCurrentValue;
                HwNormalizedRampAnimator.this.mCurrentValue = Math.round(HwNormalizedRampAnimator.this.mAnimatedValue);
                if (HwNormalizedRampAnimator.this.mData.animatingForRGBWEnable && HwNormalizedRampAnimator.this.mModeOffForRGBW && (HwNormalizedRampAnimator.this.mTargetValueChange != HwNormalizedRampAnimator.this.mTargetValue || (HwNormalizedRampAnimator.this.mProximityStateRecovery && HwNormalizedRampAnimator.this.mTargetValue != HwNormalizedRampAnimator.this.mCurrentValue))) {
                    boolean unused = HwNormalizedRampAnimator.this.mModeOffForRGBW = false;
                    HwNormalizedRampAnimator.this.mDisplayEngineManager.setScene(21, 16);
                    if (HwNormalizedRampAnimator.this.DEBUG) {
                        Slog.d(HwNormalizedRampAnimator.this.TAG, "send DE_ACTION_MODE_ON For RGBW");
                    }
                    int unused2 = HwNormalizedRampAnimator.this.mTargetValueChange = HwNormalizedRampAnimator.this.mTargetValue;
                    boolean unused3 = HwNormalizedRampAnimator.this.mProximityStateRecovery = false;
                }
                if (oldCurrentValue != HwNormalizedRampAnimator.this.mCurrentValue) {
                    HwNormalizedRampAnimator.this.mProperty.setValue(HwNormalizedRampAnimator.this.mObject, HwNormalizedRampAnimator.this.mCurrentValue);
                    if (HwNormalizedRampAnimator.this.DEBUG && HwNormalizedRampAnimator.this.DEBUG_CONTROLLER) {
                        String access$800 = HwNormalizedRampAnimator.this.TAG;
                        Slog.d(access$800, "mCurrentValue=" + HwNormalizedRampAnimator.this.mCurrentValue);
                    }
                }
                if (HwNormalizedRampAnimator.this.mTargetValue != HwNormalizedRampAnimator.this.mCurrentValue) {
                    HwNormalizedRampAnimator.this.postAnimationCallback();
                    return;
                }
                HwNormalizedRampAnimator.this.mAnimating = false;
                HwNormalizedRampAnimator.this.mHwGradualBrightnessAlgo.clearAnimatedValuePara();
                if (HwNormalizedRampAnimator.this.mListener != null) {
                    HwNormalizedRampAnimator.this.mListener.onAnimationEnd();
                }
                if (HwNormalizedRampAnimator.this.mData.animatingForRGBWEnable) {
                    boolean unused4 = HwNormalizedRampAnimator.this.mModeOffForRGBW = true;
                    HwNormalizedRampAnimator.this.mDisplayEngineManager.setScene(21, 17);
                    if (HwNormalizedRampAnimator.this.DEBUG) {
                        Slog.i(HwNormalizedRampAnimator.this.TAG, "send DE_ACTION_MODE_Off For RGBW");
                    }
                }
            }
        };
        this.mHwGradualBrightnessAlgo = new HwGradualBrightnessAlgo();
        this.mData = HwBrightnessXmlLoader.getData();
        this.mDisplayEngineManager = new DisplayEngineManager();
        if (this.mData.rebootFirstBrightnessAnimationEnable) {
            this.mFirstTime = false;
        }
        this.mHBMData = new Bundle();
        updateHBMData(this.mTargetValue, this.mRate, this.mHwGradualBrightnessAlgo.getDuration());
    }

    public boolean animateTo(int target, int rate) {
        if (this.mTargetValueLast == 0 && target > 0) {
            Slog.w(this.TAG, "animateTo: target changing from zero to non-zero with dimming, reset rate to 0!");
            rate = 0;
        }
        this.mTargetValueLast = target;
        if (this.mData.animatingForRGBWEnable && rate <= 0 && target == 0 && this.mTargetValueForRGBW >= 4) {
            this.mModeOffForRGBW = true;
            this.mDisplayEngineManager.setScene(21, 17);
            if (this.DEBUG) {
                Slog.d(this.TAG, "send DE_ACTION_MODE_off For RGBW");
            }
        }
        this.mTargetValueForRGBW = target;
        float targetOut = (float) target;
        if (target > 4 && this.mData.darkLightLevelMaxThreshold > 4 && target > this.mData.darkLightLevelMinThreshold && target < this.mData.darkLightLevelMaxThreshold) {
            float ratio = (float) Math.pow((double) ((targetOut - 4.0f) / ((float) (this.mData.darkLightLevelMaxThreshold - 4))), (double) this.mData.darkLightLevelRatio);
            targetOut = 4.0f + (((float) (this.mData.darkLightLevelMaxThreshold - 4)) * ratio);
            if (this.DEBUG) {
                String str = this.TAG;
                Slog.d(str, "DarkLightLevel targetIn255 =" + target + ",targetOut255=" + targetOut + ",ratio=" + ratio);
            }
        }
        int target2 = (int) ((10000.0f * targetOut) / 255.0f);
        boolean ret = HwNormalizedRampAnimator.super.animateTo(target2, rate);
        if (rate == 0) {
            updateHBMData(target2, rate, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        }
        return ret;
    }

    /* access modifiers changed from: protected */
    public void notifyAlgoUpdateCurrentValue() {
        this.mHwGradualBrightnessAlgo.updateTargetAndRate(this.mTargetValue, this.mRate);
        this.mHwGradualBrightnessAlgo.updateCurrentBrightnessValue((float) this.mCurrentValue);
    }

    public void updateBrightnessRampPara(boolean automode, int updateAutoBrightnessCount, boolean intervened, int state) {
        if (this.DEBUG && this.DEBUG_CONTROLLER) {
            String str = this.TAG;
            Slog.d(str, "automode=" + automode + ",updateBrightnessCount=" + updateAutoBrightnessCount + ",intervened=" + intervened + ",state=" + state);
        }
        this.mBrightnessAdjustMode = automode;
        if (this.mBrightnessAdjustMode) {
            HwGradualBrightnessAlgo hwGradualBrightnessAlgo = this.mHwGradualBrightnessAlgo;
            boolean z = true;
            if (!(updateAutoBrightnessCount == 1 || updateAutoBrightnessCount == 0)) {
                z = false;
            }
            hwGradualBrightnessAlgo.isFirstValidAutoBrightness(z);
        }
        this.mHwGradualBrightnessAlgo.updateAdjustMode(automode);
        this.mHwGradualBrightnessAlgo.autoModeIsIntervened(intervened);
        this.mHwGradualBrightnessAlgo.setPowerDimState(state);
    }

    public void updateFastAnimationFlag(boolean fastAnimtionFlag) {
        this.mHwGradualBrightnessAlgo.updateFastAnimationFlag(fastAnimtionFlag);
    }

    public void updateCoverModeFastAnimationFlag(boolean coverModeAmitionFast) {
        this.mHwGradualBrightnessAlgo.updateCoverModeFastAnimationFlag(coverModeAmitionFast);
    }

    public void updateCameraModeChangeAnimationEnable(boolean cameraModeEnable) {
        this.mHwGradualBrightnessAlgo.updateCameraModeChangeAnimationEnable(cameraModeEnable);
    }

    public void updateGameModeChangeAnimationEnable(boolean gameModeEnable) {
        this.mHwGradualBrightnessAlgo.updateGameModeChangeAnimationEnable(gameModeEnable);
    }

    public void updateReadingModeChangeAnimationEnable(boolean readingModeEnable) {
        this.mHwGradualBrightnessAlgo.updateReadingModeChangeAnimationEnable(readingModeEnable);
    }

    public void setBrightnessAnimationTime(boolean animationEnabled, int millisecond) {
        if (this.mHwGradualBrightnessAlgo != null) {
            this.mHwGradualBrightnessAlgo.setBrightnessAnimationTime(animationEnabled, millisecond);
        } else {
            Slog.e(this.TAG, "mHwGradualBrightnessAlgo=null,can not setBrightnessAnimationTime");
        }
    }

    public void updateScreenLockedAnimationEnable(boolean screenLockedEnable) {
        this.mHwGradualBrightnessAlgo.updateScreenLockedAnimationEnable(screenLockedEnable);
    }

    public void updateOutdoorAnimationFlag(boolean specialAnimtionFlag) {
        this.mHwGradualBrightnessAlgo.updateOutdoorAnimationFlag(specialAnimtionFlag);
    }

    public void updatemManualModeAnimationEnable(boolean manualModeAnimationEnable) {
        this.mHwGradualBrightnessAlgo.updatemManualModeAnimationEnable(manualModeAnimationEnable);
    }

    public void updateManualPowerSavingAnimationEnable(boolean manualPowerSavingAnimationEnable) {
        this.mHwGradualBrightnessAlgo.updateManualPowerSavingAnimationEnable(manualPowerSavingAnimationEnable);
    }

    public void updateManualThermalModeAnimationEnable(boolean manualThermalModeAnimationEnable) {
        this.mHwGradualBrightnessAlgo.updateManualThermalModeAnimationEnable(manualThermalModeAnimationEnable);
    }

    public void updateBrightnessModeAnimationEnable(boolean animationEnable, int time) {
        this.mHwGradualBrightnessAlgo.updateBrightnessModeAnimationEnable(animationEnable, time);
    }

    public void updateDarkAdaptAnimationDimmingEnable(boolean enable) {
        this.mHwGradualBrightnessAlgo.updateDarkAdaptAnimationDimmingEnable(enable);
    }

    public void updateFastDarkenDimmingEnable(boolean enable) {
        this.mHwGradualBrightnessAlgo.updateFastDarkenDimmingEnable(enable);
    }

    /* access modifiers changed from: protected */
    public void postAnimationCallback() {
        this.mHwGradualBrightnessAlgo.updateTargetAndRate(this.mTargetValue, this.mRate);
        this.mChoreographer.postCallback(1, this.mNormalizedAnimationCallback, null);
    }

    /* access modifiers changed from: protected */
    public void cancelAnimationCallback() {
        this.mChoreographer.removeCallbacks(1, this.mNormalizedAnimationCallback, null);
    }

    public void updateProximityState(boolean proximityState) {
        if (this.mData.animatingForRGBWEnable && !proximityState && this.mProximityState) {
            this.mProximityStateRecovery = true;
        }
        this.mProximityState = proximityState;
        if (proximityState && this.mAnimating && this.mTargetValue < this.mCurrentValue) {
            this.mAnimating = false;
            cancelAnimationCallback();
            this.mTargetValue = this.mCurrentValue;
            this.mHwGradualBrightnessAlgo.clearAnimatedValuePara();
            if (this.mListener != null) {
                this.mListener.onAnimationEnd();
            }
            if (this.mData.animatingForRGBWEnable) {
                this.mModeOffForRGBW = true;
                this.mDisplayEngineManager.setScene(21, 17);
                if (this.DEBUG) {
                    Slog.d(this.TAG, "send DE_ACTION_MODE_OFF For RGBW");
                }
            }
            if (this.DEBUG) {
                String str = this.TAG;
                Slog.d(str, " proximityState=" + proximityState + ",mTargetValue=" + this.mTargetValue + ",mCurrentValue=" + this.mCurrentValue);
            }
        }
    }

    public int getCurrentBrightness() {
        return (this.mCurrentValue * 255) / 10000;
    }

    /* access modifiers changed from: private */
    public void updateHBMData(int target, int rate, float duration) {
        if (this.mHBMData.getInt("target") != target || this.mHBMData.getInt("rate") != rate || ((double) Math.abs(this.mHBMData.getFloat("duration") - duration)) > 1.0E-6d) {
            this.mHBMData.putInt("target", target);
            this.mHBMData.putInt("rate", rate);
            this.mHBMData.putFloat("duration", duration);
            String str = this.TAG;
            Slog.d(str, "hbm_dimming target=" + this.mHBMData.getInt("target") + " rate=" + this.mHBMData.getInt("rate") + " duration=" + this.mHBMData.getFloat("duration"));
            this.mDisplayEngineManager.setDataToFilter("HBM", this.mHBMData);
        }
    }
}
