package com.android.server.accessibility;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;

interface EventStreamTransformation {
    EventStreamTransformation getNext();

    void setNext(EventStreamTransformation eventStreamTransformation);

    void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        EventStreamTransformation next = getNext();
        if (next != null) {
            next.onMotionEvent(event, rawEvent, policyFlags);
        }
    }

    void onKeyEvent(KeyEvent event, int policyFlags) {
        EventStreamTransformation next = getNext();
        if (next != null) {
            next.onKeyEvent(event, policyFlags);
        }
    }

    void onAccessibilityEvent(AccessibilityEvent event) {
        EventStreamTransformation next = getNext();
        if (next != null) {
            next.onAccessibilityEvent(event);
        }
    }

    void clearEvents(int inputSource) {
        EventStreamTransformation next = getNext();
        if (next != null) {
            next.clearEvents(inputSource);
        }
    }

    void onDestroy() {
    }
}
