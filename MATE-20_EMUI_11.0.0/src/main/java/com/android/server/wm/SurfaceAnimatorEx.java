package com.android.server.wm;

import android.view.SurfaceControl;

public class SurfaceAnimatorEx {
    private SurfaceAnimator mSurfaceAnimator;

    public SurfaceAnimatorEx() {
    }

    public SurfaceAnimatorEx(AnimatableEx animatable, Runnable animationFinishedCallback, WindowManagerServiceEx service) {
        this.mSurfaceAnimator = new SurfaceAnimator(animatable.getAnimatableBridge(), animationFinishedCallback, service.getWindowManagerService());
    }

    public SurfaceAnimator getSurfaceAnimator() {
        return this.mSurfaceAnimator;
    }

    public void setSurfaceAnimator(SurfaceAnimator surfaceAnimator) {
        this.mSurfaceAnimator = surfaceAnimator;
    }

    public void cancelAnimation() {
        SurfaceAnimator surfaceAnimator = this.mSurfaceAnimator;
        if (surfaceAnimator != null) {
            surfaceAnimator.cancelAnimation();
        }
    }

    public void startAnimation(SurfaceControl.Transaction t, AnimationAdapterEx anim, boolean hidden) {
        SurfaceAnimator surfaceAnimator = this.mSurfaceAnimator;
        if (surfaceAnimator != null) {
            surfaceAnimator.startAnimation(t, anim.getAnimationAdapter(), hidden);
        }
    }
}
