package com.android.server.display;

import android.animation.ValueAnimator;
import android.util.IntProperty;
import android.util.Slog;
import android.view.Choreographer;
import com.android.server.LocalServices;
import com.android.server.lights.LightsManager;

public class RampAnimator<T> {
    public static final int DEFAULT_MAX_BRIGHTNESS = 255;
    public static final int HIGH_PRECISION_MAX_BRIGHTNESS = 10000;
    private boolean DEBUG = false;
    private String TAG = "RampAnimator";
    protected float mAnimatedValue;
    protected boolean mAnimating;
    private final Runnable mAnimationCallback = new Runnable() {
        public void run() {
            long frameTimeNanos = RampAnimator.this.mChoreographer.getFrameTimeNanos();
            float timeDelta = ((float) (frameTimeNanos - RampAnimator.this.mLastFrameTimeNanos)) * 1.0E-9f;
            RampAnimator.this.mLastFrameTimeNanos = frameTimeNanos;
            float scale = ValueAnimator.getDurationScale();
            if (scale == 0.0f) {
                RampAnimator.this.mAnimatedValue = (float) RampAnimator.this.mTargetValue;
            } else {
                float amount;
                if (!RampAnimator.this.mIsHighPrecision || Math.abs(RampAnimator.this.mTargetValue - RampAnimator.this.mCurrentValue) <= 40) {
                    amount = (((float) RampAnimator.this.mRate) * timeDelta) / scale;
                } else {
                    amount = Math.abs((((((float) (RampAnimator.this.mTargetValue - RampAnimator.this.mCurrentValue)) * timeDelta) * ((float) RampAnimator.this.mRate)) / 40.0f) / scale);
                }
                if (RampAnimator.this.mTargetValue > RampAnimator.this.mCurrentValue) {
                    RampAnimator.this.mAnimatedValue = Math.min(RampAnimator.this.mAnimatedValue + amount, (float) RampAnimator.this.mTargetValue);
                } else {
                    RampAnimator.this.mAnimatedValue = Math.max(RampAnimator.this.mAnimatedValue - amount, (float) RampAnimator.this.mTargetValue);
                }
            }
            int oldCurrentValue = RampAnimator.this.mCurrentValue;
            RampAnimator.this.mCurrentValue = Math.round(RampAnimator.this.mAnimatedValue);
            if (oldCurrentValue != RampAnimator.this.mCurrentValue) {
                RampAnimator.this.mProperty.setValue(RampAnimator.this.mObject, RampAnimator.this.mCurrentValue);
            }
            if (RampAnimator.this.mTargetValue != RampAnimator.this.mCurrentValue) {
                RampAnimator.this.postAnimationCallback();
                return;
            }
            RampAnimator.this.mAnimating = false;
            if (RampAnimator.this.mListener != null) {
                RampAnimator.this.mListener.onAnimationEnd();
            }
        }
    };
    private final Runnable mAnimationCallbackForNewPolicy = new Runnable() {
        public void run() {
            long frameTimeNanos = RampAnimator.this.mChoreographer.getFrameTimeNanos();
            float timeDelta = ((float) (frameTimeNanos - RampAnimator.this.mLastFrameTimeNanos)) * 1.0E-9f;
            RampAnimator.this.mLastFrameTimeNanos = frameTimeNanos;
            float scale = ValueAnimator.getDurationScale();
            if (scale == 0.0f) {
                RampAnimator.this.mAnimatedValue = (float) RampAnimator.this.mTargetValue;
            } else {
                float amount;
                float sigma = RampAnimator.this.mRate == 40 ? 1.5f : 1.0f;
                if (RampAnimator.this.mIsAutoBrightnessMode && (RampAnimator.this.mIsFirstValidAutoBrightness || RampAnimator.this.mTargetValue < RampAnimator.this.mCurrentValue || RampAnimator.this.mAutoBrightnessIntervened)) {
                    if (RampAnimator.this.mFirstTimeCalculateAmount) {
                        float duration = (RampAnimator.this.mIsFirstValidAutoBrightness || RampAnimator.this.mAutoBrightnessIntervened) ? 0.5f : 3.0f;
                        amount = (((float) Math.abs(RampAnimator.this.mCurrentValue - RampAnimator.this.mTargetValue)) / duration) * timeDelta;
                        if (RampAnimator.this.mTargetValue < 1254 && RampAnimator.this.mCurrentValue < 1300 && duration == 3.0f) {
                            amount = (((float) Math.abs(RampAnimator.this.mCurrentValue - RampAnimator.this.mTargetValue)) / 0.5f) * timeDelta;
                        }
                        RampAnimator.this.mDecreaseFixAmount = amount;
                        if (((double) timeDelta) > 0.016d) {
                            RampAnimator.this.mFirstTimeCalculateAmount = false;
                        }
                    } else {
                        amount = RampAnimator.this.mDecreaseFixAmount;
                    }
                } else if (RampAnimator.this.mTargetValue > 1254) {
                    amount = Math.abs(RampAnimator.this.mTargetValue - RampAnimator.this.mCurrentValue) < 40 ? ((((float) RampAnimator.this.mRate) * timeDelta) * sigma) / scale : ((((((float) Math.abs(RampAnimator.this.mTargetValue - RampAnimator.this.mCurrentValue)) * timeDelta) * ((float) RampAnimator.this.mRate)) * sigma) / 40.0f) / scale;
                } else if (RampAnimator.this.mFirstTimeCalculateAmount) {
                    if (RampAnimator.this.mIsFirstValidAutoBrightness || RampAnimator.this.mAutoBrightnessIntervened) {
                    }
                    amount = (((float) Math.abs(RampAnimator.this.mCurrentValue - RampAnimator.this.mTargetValue)) / 0.5f) * timeDelta;
                    RampAnimator.this.mDecreaseFixAmount = amount;
                    if (((double) timeDelta) > 0.016d) {
                        RampAnimator.this.mFirstTimeCalculateAmount = false;
                    }
                } else {
                    amount = RampAnimator.this.mDecreaseFixAmount;
                }
                if (RampAnimator.this.DEBUG) {
                    Slog.d(RampAnimator.this.TAG, "mIsAutoBrightnessMode=" + RampAnimator.this.mIsAutoBrightnessMode + ",mTargetValue=" + RampAnimator.this.mTargetValue + ",mCurrentValue=" + RampAnimator.this.mCurrentValue + ",amount=" + amount + ",mRate=" + RampAnimator.this.mRate + ",sigma=" + sigma + ",timeDelta=" + timeDelta + ",scale=" + scale + ",mIsFirstValidAutoBrightness=" + RampAnimator.this.mIsFirstValidAutoBrightness + ",mAutoBrightnessIntervened=" + RampAnimator.this.mAutoBrightnessIntervened);
                }
                if (RampAnimator.this.mTargetValue > RampAnimator.this.mCurrentValue) {
                    RampAnimator.this.mAnimatedValue = Math.min(RampAnimator.this.mAnimatedValue + amount, (float) RampAnimator.this.mTargetValue);
                } else {
                    RampAnimator.this.mAnimatedValue = Math.max(RampAnimator.this.mAnimatedValue - amount, (float) RampAnimator.this.mTargetValue);
                }
            }
            int oldCurrentValue = RampAnimator.this.mCurrentValue;
            RampAnimator.this.mCurrentValue = Math.round(RampAnimator.this.mAnimatedValue);
            if (oldCurrentValue != RampAnimator.this.mCurrentValue) {
                RampAnimator.this.mProperty.setValue(RampAnimator.this.mObject, RampAnimator.this.mCurrentValue);
            }
            if (RampAnimator.this.mTargetValue != RampAnimator.this.mCurrentValue) {
                RampAnimator.this.postAnimationCallback();
                return;
            }
            RampAnimator.this.mAnimating = false;
            RampAnimator.this.mIsFirstValidAutoBrightness = false;
            RampAnimator.this.mAutoBrightnessIntervened = false;
            if (RampAnimator.this.mListener != null) {
                RampAnimator.this.mListener.onAnimationEnd();
            }
        }
    };
    public boolean mAutoBrightnessIntervened = false;
    protected final Choreographer mChoreographer;
    protected int mCurrentValue;
    private float mDecreaseFixAmount;
    protected boolean mFirstTime = true;
    private boolean mFirstTimeCalculateAmount = false;
    public boolean mIsAutoBrightnessMode;
    public boolean mIsFirstValidAutoBrightness = false;
    private boolean mIsHighPrecision = false;
    public boolean mIsValidAutoBrightness = false;
    protected long mLastFrameTimeNanos;
    protected Listener mListener;
    protected final T mObject;
    protected final IntProperty<T> mProperty;
    protected int mRate;
    protected int mTargetValue;

    public interface Listener {
        void onAnimationEnd();
    }

    public RampAnimator(T object, IntProperty<T> property) {
        this.mObject = object;
        this.mProperty = property;
        this.mChoreographer = Choreographer.getInstance();
        this.mIsHighPrecision = ((LightsManager) LocalServices.getService(LightsManager.class)).getLight(0).isHighPrecision();
    }

    public boolean animateTo(int target, int rate) {
        if (!this.mFirstTime && rate > 0) {
            if (!this.mAnimating || rate > this.mRate || ((target <= this.mCurrentValue && this.mCurrentValue <= this.mTargetValue) || (this.mTargetValue <= this.mCurrentValue && this.mCurrentValue <= target))) {
                this.mRate = rate;
            }
            boolean changed = this.mTargetValue != target;
            updateFirstTimeCalculateAmountFlag(changed);
            this.mTargetValue = target;
            if (!(this.mAnimating || target == this.mCurrentValue)) {
                this.mAnimating = true;
                this.mAnimatedValue = (float) this.mCurrentValue;
                this.mLastFrameTimeNanos = System.nanoTime();
                postAnimationCallback();
            }
            return changed;
        } else if (!this.mFirstTime && target == this.mCurrentValue) {
            return false;
        } else {
            this.mFirstTime = false;
            this.mRate = 0;
            this.mTargetValue = target;
            this.mCurrentValue = target;
            this.mProperty.setValue(this.mObject, target);
            notifyAlgoUpdateCurrentValue();
            if (this.mAnimating) {
                this.mAnimating = false;
                cancelAnimationCallback();
            }
            if (this.mListener != null) {
                this.mListener.onAnimationEnd();
            }
            return true;
        }
    }

    public boolean isAnimating() {
        return this.mAnimating;
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    protected void postAnimationCallback() {
        if (this.mIsHighPrecision) {
            this.mChoreographer.postCallback(1, this.mAnimationCallbackForNewPolicy, null);
        } else {
            this.mChoreographer.postCallback(1, this.mAnimationCallback, null);
        }
    }

    protected void cancelAnimationCallback() {
        if (this.mIsHighPrecision) {
            this.mChoreographer.removeCallbacks(1, this.mAnimationCallbackForNewPolicy, null);
        } else {
            this.mChoreographer.removeCallbacks(1, this.mAnimationCallback, null);
        }
    }

    private void updateFirstTimeCalculateAmountFlag(boolean changed) {
        if (changed && this.mIsHighPrecision) {
            this.mFirstTimeCalculateAmount = true;
        }
    }

    protected void notifyAlgoUpdateCurrentValue() {
    }

    public void updateBrightnessRampPara(boolean automode, int updateAutoBrightnessCount, boolean intervened, int state) {
    }

    public void updateProximityState(boolean proximityState) {
    }

    public int getCurrentBrightness() {
        return this.mCurrentValue;
    }

    public void updateFastAnimationFlag(boolean fastAnimtionFlag) {
    }

    public void updateCoverModeFastAnimationFlag(boolean coverModeAmitionFast) {
    }

    public void updateCameraModeChangeAnimationEnable(boolean cameraModeEnable) {
    }

    public void setBrightnessAnimationTime(boolean animationEnabled, int millisecond) {
    }

    public void updateScreenLockedAnimationEnable(boolean screenLockedEnable) {
    }

    public void updateOutdoorAnimationFlag(boolean outdoorAnimationFlag) {
    }

    public void updatemManualModeAnimationEnable(boolean manualModeAnimationEnable) {
    }

    public void updateManualPowerSavingAnimationEnable(boolean manualPowerSavingAnimationEnable) {
    }

    public void updateManualThermalModeAnimationEnable(boolean manualThermalModeAnimationEnable) {
    }
}
