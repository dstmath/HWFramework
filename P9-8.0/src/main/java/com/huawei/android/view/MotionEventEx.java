package com.huawei.android.view;

import android.view.MotionEvent;

public class MotionEventEx {
    public static final void setDownTime(MotionEvent event) {
        event.setDownTime(event.getEventTime());
    }
}
