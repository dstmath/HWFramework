package com.android.server.accessibility;

import android.content.Context;
import android.view.MotionEvent;

public class MagnificationGestureHandlerBridge extends MagnificationGestureHandler {
    private MagnificationGestureHandlerEx mMagnificationGestureHandlerEx;

    public /* bridge */ /* synthetic */ void clearEvents(int x0) {
        MagnificationGestureHandlerBridge.super.clearEvents(x0);
    }

    public /* bridge */ /* synthetic */ EventStreamTransformation getNext() {
        return MagnificationGestureHandlerBridge.super.getNext();
    }

    public /* bridge */ /* synthetic */ void onDestroy() {
        MagnificationGestureHandlerBridge.super.onDestroy();
    }

    public /* bridge */ /* synthetic */ void onMotionEvent(MotionEvent x0, MotionEvent x1, int x2) {
        MagnificationGestureHandlerBridge.super.onMotionEvent(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ void setNext(EventStreamTransformation x0) {
        MagnificationGestureHandlerBridge.super.setNext(x0);
    }

    public /* bridge */ /* synthetic */ String toString() {
        return MagnificationGestureHandlerBridge.super.toString();
    }

    public MagnificationGestureHandlerBridge(Context context, MagnificationController magnificationController, boolean isDetectControlGestures, boolean isTriggerable, int displayId) {
        super(context, magnificationController, isDetectControlGestures, isTriggerable, displayId);
    }

    public void setMagnificationGestureHandlerEx(MagnificationGestureHandlerEx magnificationGestureHandlerEx) {
        this.mMagnificationGestureHandlerEx = magnificationGestureHandlerEx;
    }
}
