package com.android.server.accessibility;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;

public class KeyboardInterceptor implements EventStreamTransformation {
    private AccessibilityManagerService mAms;
    private EventStreamTransformation mNext;

    public KeyboardInterceptor(AccessibilityManagerService service) {
        this.mAms = service;
    }

    public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        if (this.mNext != null) {
            this.mNext.onMotionEvent(event, rawEvent, policyFlags);
        }
    }

    public void onKeyEvent(KeyEvent event, int policyFlags) {
        this.mAms.notifyKeyEvent(event, policyFlags);
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (this.mNext != null) {
            this.mNext.onAccessibilityEvent(event);
        }
    }

    public void setNext(EventStreamTransformation next) {
        this.mNext = next;
    }

    public void clearEvents(int inputSource) {
        if (this.mNext != null) {
            this.mNext.clearEvents(inputSource);
        }
    }

    public void onDestroy() {
    }
}
