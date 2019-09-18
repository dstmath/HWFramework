package com.android.server.display;

import android.animation.ValueAnimator;
import android.util.IntProperty;
import android.view.Choreographer;

public class RampAnimator<T> {
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
                float amount = (((float) RampAnimator.this.mRate) * timeDelta) / scale;
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
    protected final Choreographer mChoreographer;
    protected int mCurrentValue;
    protected boolean mFirstTime = true;
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
    }

    public boolean animateTo(int target, int rate) {
        boolean z = false;
        if (!this.mFirstTime && rate > 0) {
            if (!this.mAnimating || rate > this.mRate || ((target <= this.mCurrentValue && this.mCurrentValue <= this.mTargetValue) || (this.mTargetValue <= this.mCurrentValue && this.mCurrentValue <= target))) {
                this.mRate = rate;
            }
            if (this.mTargetValue != target) {
                z = true;
            }
            boolean changed = z;
            this.mTargetValue = target;
            if (!this.mAnimating && target != this.mCurrentValue) {
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

    /* access modifiers changed from: protected */
    public void postAnimationCallback() {
        this.mChoreographer.postCallback(1, this.mAnimationCallback, null);
    }

    /* access modifiers changed from: protected */
    public void cancelAnimationCallback() {
        this.mChoreographer.removeCallbacks(1, this.mAnimationCallback, null);
    }

    /* access modifiers changed from: protected */
    public void notifyAlgoUpdateCurrentValue() {
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

    public void updateGameModeChangeAnimationEnable(boolean gameModeEnable) {
    }

    public void updateReadingModeChangeAnimationEnable(boolean cameraModeEnable) {
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

    public void updateBrightnessModeAnimationEnable(boolean animationEnable, int time) {
    }

    public void updateDarkAdaptAnimationDimmingEnable(boolean enable) {
    }

    public void updateFastDarkenDimmingEnable(boolean enable) {
    }
}
