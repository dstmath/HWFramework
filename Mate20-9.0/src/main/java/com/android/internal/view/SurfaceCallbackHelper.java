package com.android.internal.view;

import android.view.SurfaceHolder;

public class SurfaceCallbackHelper {
    int mFinishDrawingCollected = 0;
    int mFinishDrawingExpected = 0;
    private Runnable mFinishDrawingRunnable = new Runnable() {
        public void run() {
            synchronized (SurfaceCallbackHelper.this) {
                SurfaceCallbackHelper.this.mFinishDrawingCollected++;
                if (SurfaceCallbackHelper.this.mFinishDrawingCollected >= SurfaceCallbackHelper.this.mFinishDrawingExpected) {
                    SurfaceCallbackHelper.this.mRunnable.run();
                }
            }
        }
    };
    Runnable mRunnable;

    public SurfaceCallbackHelper(Runnable callbacksCollected) {
        this.mRunnable = callbacksCollected;
    }

    public void dispatchSurfaceRedrawNeededAsync(SurfaceHolder holder, SurfaceHolder.Callback[] callbacks) {
        int i;
        if (callbacks == null || callbacks.length == 0) {
            this.mRunnable.run();
            return;
        }
        synchronized (this) {
            this.mFinishDrawingExpected = callbacks.length;
            this.mFinishDrawingCollected = 0;
        }
        for (SurfaceHolder.Callback2 callback2 : callbacks) {
            if (callback2 instanceof SurfaceHolder.Callback2) {
                callback2.surfaceRedrawNeededAsync(holder, this.mFinishDrawingRunnable);
            } else {
                this.mFinishDrawingRunnable.run();
            }
        }
    }
}
