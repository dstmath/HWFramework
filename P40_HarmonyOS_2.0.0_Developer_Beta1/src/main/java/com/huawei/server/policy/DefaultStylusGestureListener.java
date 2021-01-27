package com.huawei.server.policy;

import android.content.Context;
import android.view.MotionEvent;

public class DefaultStylusGestureListener {
    protected DefaultStylusGestureListener(Context context) {
    }

    public static synchronized DefaultStylusGestureListener getInstance(Context context) {
        DefaultStylusGestureListener defaultStylusGestureListener;
        synchronized (DefaultStylusGestureListener.class) {
            defaultStylusGestureListener = new DefaultStylusGestureListener(context);
        }
        return defaultStylusGestureListener;
    }

    public void updateConfiguration() {
    }

    public void onPointerEvent(MotionEvent motionEvent) {
    }

    public void onRotationChange(int newRotation) {
    }

    public void onScreenTurnedOff() {
    }

    public void onKeyEvent(int keyCode, boolean isDown) {
    }

    public void setToolType() {
    }
}
