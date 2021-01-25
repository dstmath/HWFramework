package com.android.server.display;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.util.IntProperty;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwBrightnessXmlLoader;
import com.huawei.displayengine.DisplayEngineManager;
import com.huawei.displayengine.IDisplayEngineCallback;
import huawei.com.android.server.fingerprint.FingerViewController;
import java.lang.reflect.InvocationTargetException;

public final class HwNormalizedRampAnimator<T> extends RampAnimator<T> {
    private static final int DEFAULT_MAX_BRIGHTNESS = 255;
    private static final int DEFAULT_MIN_BRIGHTNESS = 4;
    private static final int FAILED_RETURN_VALUE = -1;
    private static final float HBM_DURATION_DEV = 1.0E-6f;
    private static final int HIGH_PRECISION_MAX_BRIGHTNESS = 10000;
    private static final boolean HWDEBUG;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int MSG_UPDATE_BRIGHTNESS_ALPHA = 1;
    private static final int NIGHT_UP_POWER_ON_DEFFAULT_RATE = 100;
    private static final String TAG = "HwNormalizedRampAnimator";
    private int mBrightnessLevel = -1;
    private final Context mContext;
    private final HwBrightnessXmlLoader.Data mData;
    private final IDisplayEngineCallback mDisplayEngineCallback = new IDisplayEngineCallback.Stub() {
        /* class com.android.server.display.HwNormalizedRampAnimator.AnonymousClass3 */

        @Override // com.huawei.displayengine.IDisplayEngineCallback
        public void onEvent(int event, int extra) {
            if (event == 1) {
                Slog.i(HwNormalizedRampAnimator.TAG, "onEvent, frameRate=" + extra);
                HwNormalizedRampAnimator.this.updateFrameRate(extra);
            }
        }

        @Override // com.huawei.displayengine.IDisplayEngineCallback
        public void onEventWithData(int event, PersistableBundle data) {
        }
    };
    private DisplayEngineManager mDisplayEngineManager;
    private FingerViewController mFingerViewController = null;
    private Handler mHandler = new Handler() {
        /* class com.android.server.display.HwNormalizedRampAnimator.AnonymousClass2 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                Slog.e(HwNormalizedRampAnimator.TAG, "Invalid message");
                return;
            }
            HwNormalizedRampAnimator hwNormalizedRampAnimator = HwNormalizedRampAnimator.this;
            hwNormalizedRampAnimator.updateFingerGradualBrightness(hwNormalizedRampAnimator.mBrightnessLevel);
        }
    };
    private Bundle mHbmData;
    private HwGradualBrightnessAlgo mHwGradualBrightnessAlgo;
    private boolean mIsAutoBrightnessMode = false;
    private boolean mIsHbmAheadEnable = SystemProperties.getBoolean("ro.config.fp_hbm_ahead", false);
    private boolean mIsKeyguradLocked = false;
    private boolean mIsModeOffForRgbw = true;
    private boolean mIsNightUpPowerOnWithDimmingEnable = false;
    private boolean mIsProximityPositiveState = false;
    private boolean mIsProximityPositiveStateRecovery = false;
    private final Runnable mNormalizedAnimationCallback = new Runnable() {
        /* class com.android.server.display.HwNormalizedRampAnimator.AnonymousClass1 */

        /* JADX DEBUG: Multi-variable search result rejected for r1v30, resolved type: android.util.IntProperty */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // java.lang.Runnable
        public void run() {
            HwNormalizedRampAnimator hwNormalizedRampAnimator = HwNormalizedRampAnimator.this;
            hwNormalizedRampAnimator.mAnimatedValue = hwNormalizedRampAnimator.mHwGradualBrightnessAlgo.getAnimatedValue();
            HwNormalizedRampAnimator hwNormalizedRampAnimator2 = HwNormalizedRampAnimator.this;
            hwNormalizedRampAnimator2.updateHbmData(hwNormalizedRampAnimator2.mTargetValue, HwNormalizedRampAnimator.this.mRate, HwNormalizedRampAnimator.this.mHwGradualBrightnessAlgo.getDuration());
            HwNormalizedRampAnimator.this.mHwGradualBrightnessAlgo.updateCurrentBrightnessValue(HwNormalizedRampAnimator.this.mAnimatedValue);
            int oldCurrentValue = HwNormalizedRampAnimator.this.mCurrentValue;
            HwNormalizedRampAnimator hwNormalizedRampAnimator3 = HwNormalizedRampAnimator.this;
            hwNormalizedRampAnimator3.mCurrentValue = Math.round(hwNormalizedRampAnimator3.mAnimatedValue);
            if (HwNormalizedRampAnimator.this.mData.animatingForRGBWEnable && HwNormalizedRampAnimator.this.mIsModeOffForRgbw && (HwNormalizedRampAnimator.this.mTargetValueChange != HwNormalizedRampAnimator.this.mTargetValue || (HwNormalizedRampAnimator.this.mIsProximityPositiveStateRecovery && HwNormalizedRampAnimator.this.mTargetValue != HwNormalizedRampAnimator.this.mCurrentValue))) {
                HwNormalizedRampAnimator.this.mIsModeOffForRgbw = false;
                HwNormalizedRampAnimator.this.mDisplayEngineManager.setScene(21, 16);
                if (HwNormalizedRampAnimator.HWFLOW) {
                    Slog.i(HwNormalizedRampAnimator.TAG, "send DE_ACTION_MODE_ON For RGBW");
                }
                HwNormalizedRampAnimator hwNormalizedRampAnimator4 = HwNormalizedRampAnimator.this;
                hwNormalizedRampAnimator4.mTargetValueChange = hwNormalizedRampAnimator4.mTargetValue;
                HwNormalizedRampAnimator.this.mIsProximityPositiveStateRecovery = false;
            }
            if (oldCurrentValue != HwNormalizedRampAnimator.this.mCurrentValue) {
                HwNormalizedRampAnimator hwNormalizedRampAnimator5 = HwNormalizedRampAnimator.this;
                hwNormalizedRampAnimator5.updateBrightnessViewAlpha(hwNormalizedRampAnimator5.mCurrentValue);
                HwNormalizedRampAnimator.this.mProperty.setValue(HwNormalizedRampAnimator.this.mObject, HwNormalizedRampAnimator.this.mCurrentValue);
                if (HwNormalizedRampAnimator.HWDEBUG) {
                    Slog.d(HwNormalizedRampAnimator.TAG, "mCurrentValue=" + HwNormalizedRampAnimator.this.mCurrentValue);
                }
            }
            if (HwNormalizedRampAnimator.this.mTargetValue != HwNormalizedRampAnimator.this.mCurrentValue) {
                HwNormalizedRampAnimator.this.postAnimationCallback();
                return;
            }
            HwNormalizedRampAnimator hwNormalizedRampAnimator6 = HwNormalizedRampAnimator.this;
            hwNormalizedRampAnimator6.mAnimating = false;
            hwNormalizedRampAnimator6.mHwGradualBrightnessAlgo.clearAnimatedValuePara();
            if (HwNormalizedRampAnimator.this.mListener != null) {
                HwNormalizedRampAnimator.this.mListener.onAnimationEnd();
            }
            if (HwNormalizedRampAnimator.this.mData.animatingForRGBWEnable) {
                HwNormalizedRampAnimator.this.mIsModeOffForRgbw = true;
                HwNormalizedRampAnimator.this.mDisplayEngineManager.setScene(21, 17);
                if (HwNormalizedRampAnimator.HWFLOW) {
                    Slog.i(HwNormalizedRampAnimator.TAG, "send DE_ACTION_MODE_Off For RGBW");
                }
            }
        }
    };
    private int mTargetValueChange;
    private int mTargetValueForRgbw;
    private int mTargetValueLast = -1;

    static {
        boolean z = false;
        if (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3))) {
            z = true;
        }
        HWDEBUG = z;
    }

    public HwNormalizedRampAnimator(T object, IntProperty<T> property, Context context) {
        super(object, property);
        this.mContext = context;
        this.mHwGradualBrightnessAlgo = new HwGradualBrightnessAlgo();
        this.mData = HwBrightnessXmlLoader.getData();
        this.mDisplayEngineManager = new DisplayEngineManager();
        if (this.mData.rebootFirstBrightnessAnimationEnable) {
            this.mFirstTime = false;
        }
        this.mHbmData = new Bundle();
        updateHbmData(this.mTargetValue, this.mRate, this.mHwGradualBrightnessAlgo.getDuration());
        this.mDisplayEngineManager.registerCallback(this.mDisplayEngineCallback);
    }

    /* access modifiers changed from: package-private */
    public void handleBrightnessViewAlphaEnable(int target, int rate) {
        if (target == 0) {
            this.mBrightnessLevel = 0;
        }
        if (((target > 0 && this.mBrightnessLevel == 0) || (target > 0 && rate == 0)) && target != this.mBrightnessLevel) {
            this.mHandler.sendEmptyMessage(1);
            if (HWFLOW) {
                Slog.i(TAG, "BrightnessViewAlpha mBrightnessLevel=" + this.mBrightnessLevel + "-->target=" + target + ",rate=" + rate);
            }
            this.mBrightnessLevel = target;
        }
    }

    /* access modifiers changed from: package-private */
    public float calculateTargetOut(int target) {
        float targetOut = (float) target;
        if (target > 4 && this.mData.darkLightLevelMaxThreshold > 4 && target > this.mData.darkLightLevelMinThreshold && target < this.mData.darkLightLevelMaxThreshold) {
            if (this.mData.darkLightLevelMaxThreshold == 4) {
                Slog.w(TAG, "mData.darkLightLevelMaxThreshold == DEFAULT_MIN_BRIGHTNESS");
                return (float) target;
            }
            float ratio = (float) Math.pow((double) ((targetOut - 4.0f) / ((float) (this.mData.darkLightLevelMaxThreshold - 4))), (double) this.mData.darkLightLevelRatio);
            targetOut = (((float) (this.mData.darkLightLevelMaxThreshold - 4)) * ratio) + 4.0f;
            if (HWFLOW) {
                Slog.i(TAG, "DarkLightLevel targetIn255=" + target + ",targetOut255=" + targetOut + ",ratio=" + ratio);
            }
        }
        return targetOut;
    }

    public boolean animateTo(int target, int rate) {
        int rateTemp = rate;
        if (this.mTargetValueLast == 0 && target > 0) {
            Slog.w(TAG, "animateTo: target changing from zero to non-zero with dimming, reset rate to 0!");
            rateTemp = 0;
        }
        if (this.mData.nightUpModeEnable && target != 0 && rateTemp == 0 && this.mIsNightUpPowerOnWithDimmingEnable) {
            rateTemp = 100;
            if (HWFLOW) {
                Slog.i(TAG, "NightUpBrightMode set nightUpModeEnable rate=100,target=" + target);
            }
        }
        this.mTargetValueLast = target;
        if (this.mData.updateBrightnessViewAlphaEnable || this.mIsHbmAheadEnable) {
            handleBrightnessViewAlphaEnable(target, rateTemp);
        }
        if (this.mData.animatingForRGBWEnable && rateTemp <= 0 && target == 0 && this.mTargetValueForRgbw >= 4) {
            this.mIsModeOffForRgbw = true;
            this.mDisplayEngineManager.setScene(21, 17);
            if (HWFLOW) {
                Slog.i(TAG, "send DE_ACTION_MODE_off For RGBW");
            }
        }
        this.mTargetValueForRgbw = target;
        int targetTemp = (int) ((10000.0f * calculateTargetOut(target)) / 255.0f);
        boolean ret = HwNormalizedRampAnimator.super.animateTo(targetTemp, rateTemp);
        if (rateTemp == 0) {
            updateHbmData(targetTemp, rateTemp, 0.0f);
        }
        return ret;
    }

    /* access modifiers changed from: protected */
    public void notifyAlgoUpdateCurrentValue() {
        this.mHwGradualBrightnessAlgo.updateTargetAndRate(this.mTargetValue, this.mRate);
        this.mHwGradualBrightnessAlgo.updateCurrentBrightnessValue((float) this.mCurrentValue);
    }

    public void updateBrightnessRampPara(boolean isAutoMode, int updateAutoBrightnessCount, boolean isIntervened, int state) {
        if (HWDEBUG) {
            Slog.d(TAG, "isAutoMode=" + isAutoMode + ",updateBrightnessCount=" + updateAutoBrightnessCount + ",isIntervened=" + isIntervened + ",state=" + state);
        }
        this.mIsAutoBrightnessMode = isAutoMode;
        if (this.mIsAutoBrightnessMode) {
            HwGradualBrightnessAlgo hwGradualBrightnessAlgo = this.mHwGradualBrightnessAlgo;
            boolean z = true;
            if (!(updateAutoBrightnessCount == 1 || updateAutoBrightnessCount == 0)) {
                z = false;
            }
            hwGradualBrightnessAlgo.setFirstValidAutoBrightness(z);
        }
        this.mHwGradualBrightnessAlgo.updateAdjustMode(isAutoMode);
        this.mHwGradualBrightnessAlgo.autoModeIsIntervened(isIntervened);
        this.mHwGradualBrightnessAlgo.setPowerDimState(state);
    }

    public void updateFastAnimationFlag(boolean isFastAnimtionFlag) {
        this.mHwGradualBrightnessAlgo.updateFastAnimationFlag(isFastAnimtionFlag);
    }

    public void updateCoverModeFastAnimationFlag(boolean isCoverModeAnimationEnable) {
        this.mHwGradualBrightnessAlgo.updateCoverModeFastAnimationFlag(isCoverModeAnimationEnable);
    }

    public void updateCameraModeChangeAnimationEnable(boolean isCameraModeEnable) {
        this.mHwGradualBrightnessAlgo.updateCameraModeChangeAnimationEnable(isCameraModeEnable);
    }

    public void updateGameModeChangeAnimationEnable(boolean isGameModeEnable) {
        this.mHwGradualBrightnessAlgo.updateGameModeChangeAnimationEnable(isGameModeEnable);
    }

    public void updateReadingModeChangeAnimationEnable(boolean isReadingModeEnable) {
        this.mHwGradualBrightnessAlgo.updateReadingModeChangeAnimationEnable(isReadingModeEnable);
    }

    public void setBrightnessAnimationTime(boolean isAnimationEnable, int millisecond) {
        HwGradualBrightnessAlgo hwGradualBrightnessAlgo = this.mHwGradualBrightnessAlgo;
        if (hwGradualBrightnessAlgo != null) {
            hwGradualBrightnessAlgo.setBrightnessAnimationTime(isAnimationEnable, millisecond);
        } else {
            Slog.e(TAG, "mHwGradualBrightnessAlgo=null,can not setBrightnessAnimationTime");
        }
    }

    public void updateScreenLockedAnimationEnable(boolean isKeyguradLocked) {
        if ((this.mData.updateBrightnessViewAlphaEnable || this.mIsHbmAheadEnable) && isKeyguradLocked != this.mIsKeyguradLocked) {
            if (HWDEBUG) {
                Slog.i(TAG, "mIsKeyguradLocked= " + this.mIsKeyguradLocked + "-->isKeyguradLocked=" + isKeyguradLocked + ",mBrightnessLevel=" + this.mBrightnessLevel);
            }
            if (isKeyguradLocked && this.mBrightnessLevel > 0) {
                sendEmptyMessage(isKeyguradLocked);
            }
            this.mIsKeyguradLocked = isKeyguradLocked;
        }
        this.mHwGradualBrightnessAlgo.updateScreenLockedAnimationEnable(isKeyguradLocked);
    }

    /* access modifiers changed from: package-private */
    public void sendEmptyMessage(boolean isKeyguradLocked) {
        if (HWFLOW) {
            Slog.i(TAG, "BrightnessViewAlpha updateAlpha mIsKeyguradLocked= " + this.mIsKeyguradLocked + "-->isKeyguradLocked=" + isKeyguradLocked + ",mBrightnessLevel=" + this.mBrightnessLevel);
        }
        this.mHandler.sendEmptyMessage(1);
    }

    public void updateOutdoorAnimationFlag(boolean isOutdoorAnimationEnable) {
        this.mHwGradualBrightnessAlgo.updateOutdoorAnimationFlag(isOutdoorAnimationEnable);
    }

    public void updatemManualModeAnimationEnable(boolean isManualModeAnimationEnable) {
        this.mHwGradualBrightnessAlgo.updateManualModeAnimationEnable(isManualModeAnimationEnable);
    }

    public void updateManualPowerSavingAnimationEnable(boolean isManualPowerSavingAnimationEnable) {
        this.mHwGradualBrightnessAlgo.updateManualPowerSavingAnimationEnable(isManualPowerSavingAnimationEnable);
    }

    public void updateManualThermalModeAnimationEnable(boolean isManualThermalModeAnimationEnable) {
        this.mHwGradualBrightnessAlgo.updateManualThermalModeAnimationEnable(isManualThermalModeAnimationEnable);
    }

    public void updateBrightnessModeAnimationEnable(boolean isAnimationEnable, int time) {
        this.mHwGradualBrightnessAlgo.updateBrightnessModeAnimationEnable(isAnimationEnable, time);
    }

    public void updateDarkAdaptAnimationDimmingEnable(boolean isDarkAdaptAnimationDimmingEnable) {
        this.mHwGradualBrightnessAlgo.updateDarkAdaptAnimationDimmingEnable(isDarkAdaptAnimationDimmingEnable);
    }

    public void updateFastDarkenDimmingEnable(boolean isFastDarkenDimmingEnable) {
        this.mHwGradualBrightnessAlgo.updateFastDarkenDimmingEnable(isFastDarkenDimmingEnable);
    }

    public void updateKeyguardUnlockedFastDarkenDimmingEnable(boolean isKeyguardUnlockedAnimationEnable) {
        this.mHwGradualBrightnessAlgo.updateKeyguardUnlockedFastDarkenDimmingEnable(isKeyguardUnlockedAnimationEnable);
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

    public void updateProximityState(boolean isProximityState) {
        if (this.mData.animatingForRGBWEnable && !isProximityState && this.mIsProximityPositiveState) {
            this.mIsProximityPositiveStateRecovery = true;
        }
        this.mIsProximityPositiveState = isProximityState;
        if (isProximityState && this.mAnimating && this.mTargetValue < this.mCurrentValue) {
            this.mAnimating = false;
            cancelAnimationCallback();
            this.mTargetValue = this.mCurrentValue;
            this.mHwGradualBrightnessAlgo.clearAnimatedValuePara();
            if (this.mListener != null) {
                this.mListener.onAnimationEnd();
            }
            if (this.mData.animatingForRGBWEnable) {
                this.mIsModeOffForRgbw = true;
                this.mDisplayEngineManager.setScene(21, 17);
                if (HWFLOW) {
                    Slog.i(TAG, "send DE_ACTION_MODE_OFF For RGBW");
                }
            }
            if (HWFLOW) {
                Slog.i(TAG, "isProximityState=" + isProximityState + ",mTargetValue=" + this.mTargetValue + ",mCurrentValue=" + this.mCurrentValue);
            }
        }
    }

    public int getCurrentBrightness() {
        return (this.mCurrentValue * 255) / 10000;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateHbmData(int target, int rate, float duration) {
        Bundle bundle = this.mHbmData;
        if (bundle == null) {
            Slog.w(TAG, "mHbmData == null, no updateHbmData");
        } else if (bundle.getInt("target") != target || this.mHbmData.getInt("rate") != rate || Math.abs(this.mHbmData.getFloat("duration") - duration) > HBM_DURATION_DEV) {
            this.mHbmData.putInt("target", target);
            this.mHbmData.putInt("rate", rate);
            this.mHbmData.putFloat("duration", duration);
            Slog.i(TAG, "hbm_dimming target=" + this.mHbmData.getInt("target") + " rate=" + this.mHbmData.getInt("rate") + " duration=" + this.mHbmData.getFloat("duration"));
            this.mDisplayEngineManager.setDataToFilter("HBM", this.mHbmData);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBrightnessViewAlpha(int brightness) {
        int brightnessLevel;
        if ((this.mData.updateBrightnessViewAlphaEnable || this.mIsHbmAheadEnable) && (brightnessLevel = (int) Math.ceil((double) ((((float) brightness) * 255.0f) / 10000.0f))) != this.mBrightnessLevel) {
            if (HWDEBUG) {
                Slog.i(TAG, "BrightnessViewAlpha mBrightnessLevel=" + this.mBrightnessLevel + "-->brightnessLevel=" + brightnessLevel + ",brightness=" + brightness + ",locked=" + this.mIsKeyguradLocked);
            }
            this.mBrightnessLevel = brightnessLevel;
            if (brightnessLevel > 0 && this.mIsKeyguradLocked) {
                this.mHandler.sendEmptyMessage(1);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFingerGradualBrightness(int brightness) {
        if (this.mFingerViewController == null) {
            this.mFingerViewController = FingerViewController.getInstance(this.mContext);
        }
        if (HWDEBUG) {
            Slog.i(TAG, "BrightnessViewAlpha brightnessForAlpha=" + brightness);
        }
        try {
            Class.forName("huawei.com.android.server.fingerprint.FingerViewController").getDeclaredMethod("setHighlightViewAlpha", Integer.TYPE).invoke(this.mFingerViewController, Integer.valueOf(brightness));
        } catch (ClassNotFoundException e) {
            Slog.e(TAG, "BrightnessViewAlpha setHighlightViewAlpha ClassNotFoundException");
        } catch (NoSuchMethodException e2) {
            Slog.e(TAG, "BrightnessViewAlpha setHighlightViewAlpha NoSuchMethodException");
        } catch (SecurityException e3) {
            Slog.e(TAG, "BrightnessViewAlpha setHighlightViewAlpha SecurityException");
        } catch (IllegalAccessException e4) {
            Slog.e(TAG, "BrightnessViewAlpha setHighlightViewAlpha IllegalAccessException");
        } catch (IllegalArgumentException e5) {
            Slog.e(TAG, "BrightnessViewAlpha setHighlightViewAlpha IllegalArgumentException");
        } catch (InvocationTargetException e6) {
            Slog.e(TAG, "BrightnessViewAlpha setHighlightViewAlpha InvocationTargetException");
        }
    }

    public void updateNightUpPowerOnWithDimmingEnable(boolean isNightUpPowerOnWithDimmingEnable) {
        if (this.mHwGradualBrightnessAlgo != null && this.mData.nightUpModeEnable) {
            this.mIsNightUpPowerOnWithDimmingEnable = isNightUpPowerOnWithDimmingEnable;
            this.mHwGradualBrightnessAlgo.updateNightUpPowerOnWithDimmingEnable(isNightUpPowerOnWithDimmingEnable);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFrameRate(int frameRate) {
        HwGradualBrightnessAlgo hwGradualBrightnessAlgo = this.mHwGradualBrightnessAlgo;
        if (hwGradualBrightnessAlgo != null) {
            hwGradualBrightnessAlgo.updateFrameRate(frameRate);
        }
    }

    public void updateFrontCameraDimmingEnable(boolean isFrontCameraDimmingEnable) {
        if (this.mHwGradualBrightnessAlgo != null && this.mData.frontCameraMaxBrightnessEnable) {
            this.mHwGradualBrightnessAlgo.updateFrontCameraDimmingEnable(isFrontCameraDimmingEnable);
        }
    }
}
