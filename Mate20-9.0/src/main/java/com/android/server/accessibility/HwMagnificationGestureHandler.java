package com.android.server.accessibility;

import android.content.Context;
import android.view.MotionEvent;

public final class HwMagnificationGestureHandler extends MagnificationGestureHandler {
    public /* bridge */ /* synthetic */ void clearEvents(int x0) {
        HwMagnificationGestureHandler.super.clearEvents(x0);
    }

    public /* bridge */ /* synthetic */ EventStreamTransformation getNext() {
        return HwMagnificationGestureHandler.super.getNext();
    }

    public /* bridge */ /* synthetic */ void onDestroy() {
        HwMagnificationGestureHandler.super.onDestroy();
    }

    public /* bridge */ /* synthetic */ void onMotionEvent(MotionEvent x0, MotionEvent x1, int x2) {
        HwMagnificationGestureHandler.super.onMotionEvent(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ void setNext(EventStreamTransformation x0) {
        HwMagnificationGestureHandler.super.setNext(x0);
    }

    public /* bridge */ /* synthetic */ String toString() {
        return HwMagnificationGestureHandler.super.toString();
    }

    public HwMagnificationGestureHandler(Context context, MagnificationController magnificationController, boolean detectControlGestures, boolean triggerable) {
        super(context, magnificationController, detectControlGestures, triggerable);
    }

    public boolean showMagnDialog(Context context) {
        return false;
    }
}
