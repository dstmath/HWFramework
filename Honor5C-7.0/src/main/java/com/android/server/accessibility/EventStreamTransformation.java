package com.android.server.accessibility;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;

interface EventStreamTransformation {
    void clearEvents(int i);

    void onAccessibilityEvent(AccessibilityEvent accessibilityEvent);

    void onDestroy();

    void onKeyEvent(KeyEvent keyEvent, int i);

    void onMotionEvent(MotionEvent motionEvent, MotionEvent motionEvent2, int i);

    void setNext(EventStreamTransformation eventStreamTransformation);
}
