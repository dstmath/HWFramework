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

    public static MotionEvent obtain(long downTime, long eventTime, int action, float xCoordinate, float yCoordinate, float pressure, float size, int metaState, float xPrecision, float yPrecision, int deviceId, int edgeFlags, int source, int displayId) {
        return MotionEvent.obtain(downTime, eventTime, action, xCoordinate, yCoordinate, pressure, size, metaState, xPrecision, yPrecision, deviceId, edgeFlags, source, displayId);
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

    @HwSystemApi
    public static class PointerPropertiesEx {
        public static MotionEvent.PointerProperties[] createArray(int size) {
            return MotionEvent.PointerProperties.createArray(size);
        }
    }

    @HwSystemApi
    public static class PointerCoordsEx {
        public static MotionEvent.PointerCoords[] createArray(int size) {
            return MotionEvent.PointerCoords.createArray(size);
        }
    }

    @HwSystemApi
    public static float getRawX(MotionEvent motionEvent, int pointerIndex) {
        if (motionEvent != null) {
            return motionEvent.getRawX(pointerIndex);
        }
        return 0.0f;
    }

    @HwSystemApi
    public static float getRawY(MotionEvent motionEvent, int pointerIndex) {
        if (motionEvent != null) {
            return motionEvent.getRawY(pointerIndex);
        }
        return 0.0f;
    }
}
