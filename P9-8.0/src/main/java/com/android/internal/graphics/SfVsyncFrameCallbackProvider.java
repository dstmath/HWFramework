package com.android.internal.graphics;

import android.animation.AnimationHandler.AnimationFrameCallbackProvider;
import android.view.Choreographer;
import android.view.Choreographer.FrameCallback;

public final class SfVsyncFrameCallbackProvider implements AnimationFrameCallbackProvider {
    private final Choreographer mChoreographer = Choreographer.getSfInstance();

    public void postFrameCallback(FrameCallback callback) {
        this.mChoreographer.postFrameCallback(callback);
    }

    public void postCommitCallback(Runnable runnable) {
        this.mChoreographer.postCallback(3, runnable, null);
    }

    public long getFrameTime() {
        return this.mChoreographer.getFrameTime();
    }

    public long getFrameDelay() {
        return Choreographer.getFrameDelay();
    }

    public void setFrameDelay(long delay) {
        Choreographer.setFrameDelay(delay);
    }
}
