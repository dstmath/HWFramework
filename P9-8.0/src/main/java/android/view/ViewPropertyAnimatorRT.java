package android.view;

import android.animation.TimeInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import com.android.internal.view.animation.FallbackLUTInterpolator;
import java.util.ArrayList;

class ViewPropertyAnimatorRT {
    private static final Interpolator sLinearInterpolator = new LinearInterpolator();
    private RenderNodeAnimator[] mAnimators = new RenderNodeAnimator[12];
    private final View mView;

    ViewPropertyAnimatorRT(View view) {
        this.mView = view;
    }

    public boolean startAnimation(ViewPropertyAnimator parent) {
        cancelAnimators(parent.mPendingAnimations);
        if (!canHandleAnimator(parent)) {
            return false;
        }
        doStartAnimation(parent);
        return true;
    }

    public void cancelAll() {
        for (int i = 0; i < this.mAnimators.length; i++) {
            if (this.mAnimators[i] != null) {
                this.mAnimators[i].cancel();
                this.mAnimators[i] = null;
            }
        }
    }

    private void doStartAnimation(ViewPropertyAnimator parent) {
        int size = parent.mPendingAnimations.size();
        long startDelay = parent.getStartDelay();
        long duration = parent.getDuration();
        TimeInterpolator interpolator = parent.getInterpolator();
        if (interpolator == null) {
            interpolator = sLinearInterpolator;
        }
        if (!RenderNodeAnimator.isNativeInterpolator(interpolator)) {
            interpolator = new FallbackLUTInterpolator(interpolator, duration);
        }
        for (int i = 0; i < size; i++) {
            NameValuesHolder holder = (NameValuesHolder) parent.mPendingAnimations.get(i);
            int property = RenderNodeAnimator.mapViewPropertyToRenderProperty(holder.mNameConstant);
            RenderNodeAnimator animator = new RenderNodeAnimator(property, holder.mFromValue + holder.mDeltaValue);
            animator.setStartDelay(startDelay);
            animator.setDuration(duration);
            animator.setInterpolator(interpolator);
            animator.setTarget(this.mView);
            animator.start();
            this.mAnimators[property] = animator;
        }
        parent.mPendingAnimations.clear();
    }

    private boolean canHandleAnimator(ViewPropertyAnimator parent) {
        if (parent.getUpdateListener() == null && parent.getListener() == null && this.mView.isHardwareAccelerated() && !parent.hasActions()) {
            return true;
        }
        return false;
    }

    private void cancelAnimators(ArrayList<NameValuesHolder> mPendingAnimations) {
        int size = mPendingAnimations.size();
        for (int i = 0; i < size; i++) {
            int property = RenderNodeAnimator.mapViewPropertyToRenderProperty(((NameValuesHolder) mPendingAnimations.get(i)).mNameConstant);
            if (this.mAnimators[property] != null) {
                this.mAnimators[property].cancel();
                this.mAnimators[property] = null;
            }
        }
    }
}
