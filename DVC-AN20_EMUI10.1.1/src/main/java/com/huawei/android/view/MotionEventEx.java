package com.huawei.android.view;

import android.view.MotionEvent;
import com.huawei.annotation.HwSystemApi;

public class MotionEventEx {
    public static final void setDownTime(MotionEvent event) {
        event.setDownTime(event.getEventTime());
    }

    @HwSystemApi
    public static MotionEvent copy(MotionEvent event) {
        return event.copy();
    }

    @HwSystemApi
    public static MotionEvent obtain(long downTime, long eventTime, int action, int pointerCount, MotionEvent.PointerProperties[] pointerProperties, MotionEvent.PointerCoords[] pointerCoords, int metaState, int buttonState, float xPrecision, float yPrecision, int deviceId, int edgeFlags, int source, int displayId, int flags) {
        return MotionEvent.obtain(downTime, eventTime, action, pointerCount, pointerProperties, pointerCoords, metaState, buttonState, xPrecision, yPrecision, deviceId, edgeFlags, source, displayId, flags);
    }

    @HwSystemApi
    public static void setActionButton(MotionEvent motionEvent, int button) {
        if (motionEvent != null) {
            motionEvent.setActionButton(button);
        }
    }

    @HwSystemApi
    public static int getDisplayId(MotionEvent motionEvent) {
        if (motionEvent != null) {
            return motionEvent.getDisplayId();
        }
        return -1;
    }
}
