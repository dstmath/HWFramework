package com.android.server.accessibility;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;

/* access modifiers changed from: package-private */
public interface EventStreamTransformation {
    EventStreamTransformation getNext();

    void setNext(EventStreamTransformation eventStreamTransformation);

    default void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        EventStreamTransformation next = getNext();
        if (next != null) {
            next.onMotionEvent(event, rawEvent, policyFlags);
        }
    }

    default void onKeyEvent(KeyEvent event, int policyFlags) {
        EventStreamTransformation next = getNext();
        if (next != null) {
            next.onKeyEvent(event, policyFlags);
        }
    }

    default void onAccessibilityEvent(AccessibilityEvent event) {
        EventStreamTransformation next = getNext();
        if (next != null) {
            next.onAccessibilityEvent(event);
        }
    }

    default void clearEvents(int inputSource) {
        EventStreamTransformation next = getNext();
        if (next != null) {
            next.clearEvents(inputSource);
        }
    }

    default void onDestroy() {
    }
}
