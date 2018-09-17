package com.android.internal.view;

import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceHolder.Callback2;

public class SurfaceCallbackHelper {
    int mFinishDrawingCollected = 0;
    int mFinishDrawingExpected = 0;
    private Runnable mFinishDrawingRunnable = new Runnable() {
        public void run() {
            synchronized (SurfaceCallbackHelper.this) {
                SurfaceCallbackHelper surfaceCallbackHelper = SurfaceCallbackHelper.this;
                surfaceCallbackHelper.mFinishDrawingCollected++;
                if (SurfaceCallbackHelper.this.mFinishDrawingCollected < SurfaceCallbackHelper.this.mFinishDrawingExpected) {
                    return;
                }
                SurfaceCallbackHelper.this.mRunnable.run();
            }
        }
    };
    Runnable mRunnable;

    public SurfaceCallbackHelper(Runnable callbacksCollected) {
        this.mRunnable = callbacksCollected;
    }

    public void dispatchSurfaceRedrawNeededAsync(SurfaceHolder holder, Callback[] callbacks) {
        if (callbacks == null || callbacks.length == 0) {
            this.mRunnable.run();
            return;
        }
        synchronized (this) {
            this.mFinishDrawingExpected = callbacks.length;
            this.mFinishDrawingCollected = 0;
        }
        for (Callback c : callbacks) {
            if (c instanceof Callback2) {
                ((Callback2) c).surfaceRedrawNeededAsync(holder, this.mFinishDrawingRunnable);
            } else {
                this.mFinishDrawingRunnable.run();
            }
        }
    }
}
