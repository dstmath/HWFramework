package android.animation;

import android.view.animation.AnimationUtils;

public class TimeAnimator extends ValueAnimator {
    private TimeListener mListener;
    private long mPreviousTime = -1;

    public interface TimeListener {
        void onTimeUpdate(TimeAnimator timeAnimator, long j, long j2);
    }

    @Override // android.animation.ValueAnimator, android.animation.Animator
    public void start() {
        this.mPreviousTime = -1;
        super.start();
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.ValueAnimator
    public boolean animateBasedOnTime(long currentTime) {
        if (this.mListener == null) {
            return false;
        }
        long totalTime = currentTime - this.mStartTime;
        long j = this.mPreviousTime;
        long deltaTime = j < 0 ? 0 : currentTime - j;
        this.mPreviousTime = currentTime;
        this.mListener.onTimeUpdate(this, totalTime, deltaTime);
        return false;
    }

    @Override // android.animation.ValueAnimator
    public void setCurrentPlayTime(long playTime) {
        long currentTime = AnimationUtils.currentAnimationTimeMillis();
        this.mStartTime = Math.max(this.mStartTime, currentTime - playTime);
        this.mStartTimeCommitted = true;
        animateBasedOnTime(currentTime);
    }

    public void setTimeListener(TimeListener listener) {
        this.mListener = listener;
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.ValueAnimator
    public void animateValue(float fraction) {
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.ValueAnimator
    public void initAnimation() {
    }
}
