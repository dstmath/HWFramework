package com.android.internal.graphics;

import android.animation.AnimationHandler;
import android.view.Choreographer;

public final class SfVsyncFrameCallbackProvider implements AnimationHandler.AnimationFrameCallbackProvider {
    private final Choreographer mChoreographer;

    public SfVsyncFrameCallbackProvider() {
        this.mChoreographer = Choreographer.getSfInstance();
    }

    public SfVsyncFrameCallbackProvider(Choreographer choreographer) {
        this.mChoreographer = choreographer;
    }

    @Override // android.animation.AnimationHandler.AnimationFrameCallbackProvider
    public void postFrameCallback(Choreographer.FrameCallback callback) {
        this.mChoreographer.postFrameCallback(callback);
    }

    @Override // android.animation.AnimationHandler.AnimationFrameCallbackProvider
    public void postCommitCallback(Runnable runnable) {
        this.mChoreographer.postCallback(4, runnable, null);
    }

    @Override // android.animation.AnimationHandler.AnimationFrameCallbackProvider
    public long getFrameTime() {
        return this.mChoreographer.getFrameTime();
    }

    @Override // android.animation.AnimationHandler.AnimationFrameCallbackProvider
    public long getFrameDelay() {
        return Choreographer.getFrameDelay();
    }

    @Override // android.animation.AnimationHandler.AnimationFrameCallbackProvider
    public void setFrameDelay(long delay) {
        Choreographer.setFrameDelay(delay);
    }
}
