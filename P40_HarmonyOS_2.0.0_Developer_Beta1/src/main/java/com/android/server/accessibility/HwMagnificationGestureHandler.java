package com.android.server.accessibility;

import android.content.Context;

public final class HwMagnificationGestureHandler extends MagnificationGestureHandlerEx {
    public HwMagnificationGestureHandler(Context context, MagnificationControllerEx magnificationControllerEx, boolean isDetectControlGestures, boolean isTriggerable, int displayId) {
        super(context, magnificationControllerEx, isDetectControlGestures, isTriggerable, displayId);
    }

    public boolean showMagnDialog(Context context) {
        return false;
    }
}
