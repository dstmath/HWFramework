package com.android.server.display;

import android.util.IntProperty;
import android.util.Log;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.display.HwBrightnessXmlLoader.Data;
import com.android.server.lights.LightsManager;
import com.huawei.displayengine.DisplayEngineManager;

public final class HwNormalizedRampAnimator<T> extends RampAnimator<T> {
    private static final int DEFAULT_MAX_BRIGHTNESS = 255;
    private static final int DEFAULT_MIN_BRIGHTNESS = 4;
    private static final int HIGH_PRECISION_MAX_BRIGHTNESS = 10000;
    private boolean DEBUG;
    private boolean DEBUG_CONTROLLER = false;
    private String TAG = "HwNormalizedRampAnimator";
    private boolean mBrightnessAdjustMode;
    private final Data mData;
    private final int mDeviceActualBrightnessLevel;
    private DisplayEngineManager mDisplayEngineManager;
    private HwGradualBrightnessAlgo mHwGradualBrightnessAlgo;
    private boolean mModeOffForRGBW;
    private final Runnable mNormalizedAnimationCallback;
    private boolean mProximityState;
    private boolean mProximityStateRecovery;
    private int mTargetValueChange;
    private int mTargetValueForRGBW;

    private static int getDeviceActualBrightnessLevel() {
        try {
            return ((LightsManager) LocalServices.getService(LightsManager.class)).getLight(0).getDeviceActualBrightnessLevel();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public HwNormalizedRampAnimator(T object, IntProperty<T> property) {
        super(object, property);
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(this.TAG, 4) : false : true;
        this.DEBUG = isLoggable;
        this.mBrightnessAdjustMode = false;
        this.mProximityState = false;
        this.mProximityStateRecovery = false;
        this.mModeOffForRGBW = true;
        this.mNormalizedAnimationCallback = new Runnable() {
            public void run() {
                HwNormalizedRampAnimator.this.mAnimatedValue = HwNormalizedRampAnimator.this.mHwGradualBrightnessAlgo.getAnimatedValue();
                HwNormalizedRampAnimator.this.mHwGradualBrightnessAlgo.updateCurrentBrightnessValue(HwNormalizedRampAnimator.this.mAnimatedValue);
                int oldCurrentValue = HwNormalizedRampAnimator.this.mCurrentValue;
                HwNormalizedRampAnimator.this.mCurrentValue = Math.round(HwNormalizedRampAnimator.this.mAnimatedValue);
                if (HwNormalizedRampAnimator.this.mData.animatingForRGBWEnable && HwNormalizedRampAnimator.this.mModeOffForRGBW && (HwNormalizedRampAnimator.this.mTargetValueChange != HwNormalizedRampAnimator.this.mTargetValue || (HwNormalizedRampAnimator.this.mProximityStateRecovery && HwNormalizedRampAnimator.this.mTargetValue != HwNormalizedRampAnimator.this.mCurrentValue))) {
                    HwNormalizedRampAnimator.this.mModeOffForRGBW = false;
                    HwNormalizedRampAnimator.this.mDisplayEngineManager.setScene(21, 16);
                    if (HwNormalizedRampAnimator.this.DEBUG) {
                        Slog.d(HwNormalizedRampAnimator.this.TAG, "send DE_ACTION_MODE_ON For RGBW");
                    }
                    HwNormalizedRampAnimator.this.mTargetValueChange = HwNormalizedRampAnimator.this.mTargetValue;
                    HwNormalizedRampAnimator.this.mProximityStateRecovery = false;
                }
                if (oldCurrentValue != HwNormalizedRampAnimator.this.mCurrentValue) {
                    HwNormalizedRampAnimator.this.mProperty.setValue(HwNormalizedRampAnimator.this.mObject, HwNormalizedRampAnimator.this.mCurrentValue);
                    if (HwNormalizedRampAnimator.this.DEBUG && HwNormalizedRampAnimator.this.DEBUG_CONTROLLER) {
                        Slog.d(HwNormalizedRampAnimator.this.TAG, "mCurrentValue=" + HwNormalizedRampAnimator.this.mCurrentValue);
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
                    HwNormalizedRampAnimator.this.mModeOffForRGBW = true;
                    HwNormalizedRampAnimator.this.mDisplayEngineManager.setScene(21, 17);
                    if (HwNormalizedRampAnimator.this.DEBUG) {
                        Slog.i(HwNormalizedRampAnimator.this.TAG, "send DE_ACTION_MODE_Off For RGBW");
                    }
                }
            }
        };
        this.mDeviceActualBrightnessLevel = getDeviceActualBrightnessLevel();
        this.mHwGradualBrightnessAlgo = new HwGradualBrightnessAlgo(this.mDeviceActualBrightnessLevel);
        this.mData = HwBrightnessXmlLoader.getData(this.mDeviceActualBrightnessLevel);
        this.mDisplayEngineManager = new DisplayEngineManager();
        if (this.mData.rebootFirstBrightnessAnimationEnable) {
            this.mFirstTime = false;
        }
    }

    public boolean animateTo(int target, int rate) {
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
                Slog.d(this.TAG, "DarkLightLevel targetIn255 =" + target + ",targetOut255=" + targetOut + ",ratio=" + ratio);
            }
        }
        return super.animateTo((int) ((10000.0f * targetOut) / 255.0f), rate);
    }

    protected void notifyAlgoUpdateCurrentValue() {
        this.mHwGradualBrightnessAlgo.updateTargetAndRate(this.mTargetValue, this.mRate);
        this.mHwGradualBrightnessAlgo.updateCurrentBrightnessValue((float) this.mCurrentValue);
    }

    public void updateBrightnessRampPara(boolean automode, int updateAutoBrightnessCount, boolean intervened, int state) {
        boolean z = true;
        if (this.DEBUG && this.DEBUG_CONTROLLER) {
            Slog.d(this.TAG, "automode=" + automode + ",updateBrightnessCount=" + updateAutoBrightnessCount + ",intervened=" + intervened + ",state=" + state);
        }
        this.mBrightnessAdjustMode = automode;
        if (this.mBrightnessAdjustMode) {
            HwGradualBrightnessAlgo hwGradualBrightnessAlgo = this.mHwGradualBrightnessAlgo;
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

    protected void postAnimationCallback() {
        this.mHwGradualBrightnessAlgo.updateTargetAndRate(this.mTargetValue, this.mRate);
        this.mChoreographer.postCallback(1, this.mNormalizedAnimationCallback, null);
    }

    protected void cancelAnimationCallback() {
        this.mChoreographer.removeCallbacks(1, this.mNormalizedAnimationCallback, null);
    }

    public void updateProximityState(boolean proximityState) {
        if (this.mData.animatingForRGBWEnable && (proximityState ^ 1) != 0 && this.mProximityState) {
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
                Slog.d(this.TAG, " proximityState=" + proximityState + ",mTargetValue=" + this.mTargetValue + ",mCurrentValue=" + this.mCurrentValue);
            }
        }
    }

    public int getCurrentBrightness() {
        return (this.mCurrentValue * 255) / 10000;
    }
}
