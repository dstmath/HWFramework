package com.android.server.wm;

import android.view.SurfaceControl;
import com.android.server.wm.SurfaceAnimator;

public class AnimatableBridge implements SurfaceAnimator.Animatable {
    private AnimatableEx mAnimatableEx;

    public void setAnimatableEx(AnimatableEx animatableEx) {
        this.mAnimatableEx = animatableEx;
    }

    public SurfaceControl.Transaction getPendingTransaction() {
        AnimatableEx animatableEx = this.mAnimatableEx;
        if (animatableEx != null) {
            return animatableEx.getPendingTransaction();
        }
        return null;
    }

    public void commitPendingTransaction() {
        AnimatableEx animatableEx = this.mAnimatableEx;
        if (animatableEx != null) {
            animatableEx.commitPendingTransaction();
        }
    }

    public void onAnimationLeashCreated(SurfaceControl.Transaction t, SurfaceControl leash) {
        AnimatableEx animatableEx = this.mAnimatableEx;
        if (animatableEx != null) {
            animatableEx.onAnimationLeashCreated(t, leash);
        }
    }

    public void onAnimationLeashLost(SurfaceControl.Transaction t) {
        AnimatableEx animatableEx = this.mAnimatableEx;
        if (animatableEx != null) {
            animatableEx.onAnimationLeashLost(t);
        }
    }

    public SurfaceControl.Builder makeAnimationLeash() {
        AnimatableEx animatableEx = this.mAnimatableEx;
        if (animatableEx != null) {
            return animatableEx.makeAnimationLeash();
        }
        return null;
    }

    public SurfaceControl getAnimationLeashParent() {
        AnimatableEx animatableEx = this.mAnimatableEx;
        if (animatableEx != null) {
            return animatableEx.getAnimationLeashParent();
        }
        return null;
    }

    public SurfaceControl getSurfaceControl() {
        AnimatableEx animatableEx = this.mAnimatableEx;
        if (animatableEx != null) {
            return animatableEx.getSurfaceControl();
        }
        return null;
    }

    public SurfaceControl getParentSurfaceControl() {
        AnimatableEx animatableEx = this.mAnimatableEx;
        if (animatableEx != null) {
            return animatableEx.getParentSurfaceControl();
        }
        return null;
    }

    public int getSurfaceWidth() {
        AnimatableEx animatableEx = this.mAnimatableEx;
        if (animatableEx != null) {
            return animatableEx.getSurfaceWidth();
        }
        return 0;
    }

    public int getSurfaceHeight() {
        AnimatableEx animatableEx = this.mAnimatableEx;
        if (animatableEx != null) {
            return animatableEx.getSurfaceHeight();
        }
        return 0;
    }
}
