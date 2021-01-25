package com.huawei.ace.activity;

import android.view.MotionEvent;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import ohos.com.sun.org.apache.xpath.internal.XPath;

public class AceEventProcessor {
    private static final int BYTES_PER_FIELD = 8;
    private static final int POINTER_DATA_FIELD_COUNT = 10;

    private static int actionMaskedToActionType(int i) {
        switch (i) {
            case 0:
            case 5:
                return 4;
            case 1:
            case 6:
                return 6;
            case 2:
                return 5;
            case 3:
                return 0;
            case 4:
            default:
                return -1;
            case 7:
            case 8:
                return 3;
        }
    }

    private static class ActionType {
        static final int ADD = 1;
        static final int CANCEL = 0;
        static final int DOWN = 4;
        static final int HOVER = 3;
        static final int MOVE = 5;
        static final int REMOVE = 2;
        static final int UP = 6;

        private ActionType() {
        }
    }

    public static ByteBuffer processTouchEvent(MotionEvent motionEvent) {
        int pointerCount = motionEvent.getPointerCount();
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(pointerCount * 10 * 8);
        allocateDirect.order(ByteOrder.LITTLE_ENDIAN);
        int actionMasked = motionEvent.getActionMasked();
        int actionMaskedToActionType = actionMaskedToActionType(motionEvent.getActionMasked());
        boolean z = true;
        if (!(actionMasked == 0 || actionMasked == 5 || actionMasked == 1 || actionMasked == 6)) {
            z = false;
        }
        if (z) {
            addEventToBuffer(motionEvent, motionEvent.getActionIndex(), actionMaskedToActionType, allocateDirect);
        } else {
            for (int i = 0; i < pointerCount; i++) {
                addEventToBuffer(motionEvent, i, actionMaskedToActionType, allocateDirect);
            }
        }
        if (allocateDirect.position() % 80 == 0) {
            return allocateDirect;
        }
        throw new AssertionError("Packet position is not on field boundary");
    }

    private static void addEventToBuffer(MotionEvent motionEvent, int i, int i2, ByteBuffer byteBuffer) {
        if (i2 != -1) {
            byteBuffer.putLong(motionEvent.getEventTime() * 1000);
            byteBuffer.putLong((long) i2);
            byteBuffer.putLong((long) motionEvent.getPointerId(i));
            byteBuffer.putDouble((double) motionEvent.getX(i));
            byteBuffer.putDouble((double) motionEvent.getY(i));
            byteBuffer.putDouble((double) motionEvent.getPressure(i));
            byteBuffer.putDouble(XPath.MATCH_SCORE_QNAME);
            byteBuffer.putDouble((double) motionEvent.getSize(i));
            byteBuffer.putLong((long) motionEvent.getSource());
            byteBuffer.putLong((long) motionEvent.getDeviceId());
        }
    }
}
