package com.android.server.display;

import android.os.SystemClock;
import android.util.IntProperty;
import android.util.Log;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.lights.LightsManager;
import com.android.server.wifipro.WifiProCHRManager;

public final class HwNormalizedRampAnimator<T> extends RampAnimator<T> {
    private static final int DEFAULT_MAX_BRIGHTNESS = 255;
    private static final int HIGH_PRECISION_MAX_BRIGHTNESS = 10000;
    private boolean DEBUG;
    private boolean DEBUG_CONTROLLER;
    private String TAG;
    private boolean mBrightnessAdjustMode;
    private final int mDeviceActualBrightnessLevel;
    private String mGradualBrightness;
    private int mGradualBrightnessNum;
    private HwGradualBrightnessAlgo mHwGradualBrightnessAlgo;
    private final Runnable mNormalizedAnimationCallback;

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
        this.TAG = "HwNormalizedRampAnimator";
        this.DEBUG_CONTROLLER = false;
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(this.TAG, 4) : false : true;
        this.DEBUG = isLoggable;
        this.mGradualBrightness = "bright trace:";
        this.mGradualBrightnessNum = 0;
        this.mBrightnessAdjustMode = false;
        this.mNormalizedAnimationCallback = new Runnable() {
            public void run() {
                HwNormalizedRampAnimator.this.mAnimatedValue = HwNormalizedRampAnimator.this.mHwGradualBrightnessAlgo.getAnimatedValue();
                HwNormalizedRampAnimator.this.mHwGradualBrightnessAlgo.updateCurrentBrightnessValue(HwNormalizedRampAnimator.this.mAnimatedValue);
                int oldCurrentValue = HwNormalizedRampAnimator.this.mCurrentValue;
                HwNormalizedRampAnimator.this.mCurrentValue = Math.round(HwNormalizedRampAnimator.this.mAnimatedValue);
                if (oldCurrentValue != HwNormalizedRampAnimator.this.mCurrentValue) {
                    HwNormalizedRampAnimator.this.mProperty.setValue(HwNormalizedRampAnimator.this.mObject, HwNormalizedRampAnimator.this.mCurrentValue);
                    HwNormalizedRampAnimator.this.remeberBrightnessChanges();
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
            }
        };
        this.mDeviceActualBrightnessLevel = getDeviceActualBrightnessLevel();
        this.mHwGradualBrightnessAlgo = new HwGradualBrightnessAlgo(this.mDeviceActualBrightnessLevel);
    }

    public boolean animateTo(int target, int rate) {
        target = (target * HIGH_PRECISION_MAX_BRIGHTNESS) / DEFAULT_MAX_BRIGHTNESS;
        if (this.DEBUG && target == 0 && this.mGradualBrightnessNum > 0) {
            Slog.d(this.TAG, this.mGradualBrightness);
            this.mGradualBrightness = "bright trace:";
            this.mGradualBrightnessNum = 0;
        }
        return super.animateTo(target, rate);
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
            if (updateAutoBrightnessCount != 1) {
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

    protected void postAnimationCallback() {
        this.mHwGradualBrightnessAlgo.updateTargetAndRate(this.mTargetValue, this.mRate);
        this.mChoreographer.postCallback(1, this.mNormalizedAnimationCallback, null);
    }

    protected void cancelAnimationCallback() {
        this.mChoreographer.removeCallbacks(1, this.mNormalizedAnimationCallback, null);
    }

    private void remeberBrightnessChanges() {
        if (this.DEBUG && this.mGradualBrightnessNum == WifiProCHRManager.WIFI_PORTAL_SAMPLES_COLLECTE) {
            Slog.d(this.TAG, this.mGradualBrightness);
            this.mGradualBrightness = "bright trace:";
            this.mGradualBrightnessNum = 0;
        }
        this.mGradualBrightnessNum++;
        this.mGradualBrightness += this.mCurrentValue + "/" + SystemClock.uptimeMillis();
        if (this.mGradualBrightnessNum <= 0 || this.mGradualBrightnessNum % 40 == 0) {
            this.mGradualBrightness += "\n";
        } else {
            this.mGradualBrightness += ",";
        }
    }

    public void updateProximityState(boolean proximityState) {
        if (proximityState && this.mAnimating && this.mTargetValue < this.mCurrentValue) {
            this.mAnimating = false;
            cancelAnimationCallback();
            this.mTargetValue = this.mCurrentValue;
            this.mHwGradualBrightnessAlgo.clearAnimatedValuePara();
            if (this.mListener != null) {
                this.mListener.onAnimationEnd();
            }
            if (this.DEBUG) {
                Slog.d(this.TAG, " proximityState=" + proximityState + ",mTargetValue=" + this.mTargetValue + ",mCurrentValue=" + this.mCurrentValue);
            }
        }
    }

    public int getCurrentBrightness() {
        return (this.mCurrentValue * DEFAULT_MAX_BRIGHTNESS) / HIGH_PRECISION_MAX_BRIGHTNESS;
    }
}
