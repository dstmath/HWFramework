package com.android.server.accessibility;

import android.content.Context;

public class MagnificationGestureHandlerEx {
    private MagnificationGestureHandlerBridge mBridge = null;

    public MagnificationGestureHandlerEx(Context context, MagnificationControllerEx magnificationControllerEx, boolean isDetectControlGestures, boolean isTriggerable, int displayId) {
        this.mBridge = new MagnificationGestureHandlerBridge(context, magnificationControllerEx.getMagnificationController(), isDetectControlGestures, isTriggerable, displayId);
        this.mBridge.setMagnificationGestureHandlerEx(this);
    }

    public MagnificationGestureHandler getMagnificationGestureHandler() {
        return this.mBridge;
    }

    public boolean showMagnDialog(Context context) {
        return false;
    }
}
