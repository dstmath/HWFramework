package com.android.internal.view;

import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;

public class OneShotPreDrawListener implements OnPreDrawListener, OnAttachStateChangeListener {
    private final boolean mReturnValue;
    private final Runnable mRunnable;
    private final View mView;
    private ViewTreeObserver mViewTreeObserver;

    private OneShotPreDrawListener(View view, boolean returnValue, Runnable runnable) {
        this.mView = view;
        this.mViewTreeObserver = view.getViewTreeObserver();
        this.mRunnable = runnable;
        this.mReturnValue = returnValue;
    }

    public static OneShotPreDrawListener add(View view, Runnable runnable) {
        return add(view, true, runnable);
    }

    public static OneShotPreDrawListener add(View view, boolean returnValue, Runnable runnable) {
        OneShotPreDrawListener listener = new OneShotPreDrawListener(view, returnValue, runnable);
        view.getViewTreeObserver().addOnPreDrawListener(listener);
        view.addOnAttachStateChangeListener(listener);
        return listener;
    }

    public boolean onPreDraw() {
        removeListener();
        this.mRunnable.run();
        return this.mReturnValue;
    }

    public void removeListener() {
        if (this.mViewTreeObserver.isAlive()) {
            this.mViewTreeObserver.removeOnPreDrawListener(this);
        } else {
            this.mView.getViewTreeObserver().removeOnPreDrawListener(this);
        }
        this.mView.removeOnAttachStateChangeListener(this);
    }

    public void onViewAttachedToWindow(View v) {
        this.mViewTreeObserver = v.getViewTreeObserver();
    }

    public void onViewDetachedFromWindow(View v) {
        removeListener();
    }
}
