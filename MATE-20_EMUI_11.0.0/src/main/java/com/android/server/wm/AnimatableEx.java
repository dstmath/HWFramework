package com.android.server.wm;

import android.view.SurfaceControl;
import com.android.server.wm.SurfaceAnimator;

public class AnimatableEx {
    private AnimatableBridge mBridge = new AnimatableBridge();

    public AnimatableEx() {
        this.mBridge.setAnimatableEx(this);
    }

    public SurfaceControl.Transaction getPendingTransaction() {
        return null;
    }

    public void commitPendingTransaction() {
    }

    public void onAnimationLeashCreated(SurfaceControl.Transaction t, SurfaceControl leash) {
    }

    public void onAnimationLeashLost(SurfaceControl.Transaction t) {
    }

    public SurfaceControl.Builder makeAnimationLeash() {
        return null;
    }

    public SurfaceControl getAnimationLeashParent() {
        return null;
    }

    public SurfaceControl getSurfaceControl() {
        return null;
    }

    public SurfaceControl getParentSurfaceControl() {
        return null;
    }

    public int getSurfaceWidth() {
        return 0;
    }

    public int getSurfaceHeight() {
        return 0;
    }

    public SurfaceAnimator.Animatable getAnimatableBridge() {
        return this.mBridge;
    }
}
