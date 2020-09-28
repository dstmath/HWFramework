package com.huawei.android.view;

import android.view.InputEventConsistencyVerifier;
import android.view.MotionEvent;

public class InputEventConsistencyVerifierEx {
    InputEventConsistencyVerifier mInputEventConsistencyVerifier;

    public InputEventConsistencyVerifierEx(Object caller, int flags) {
        this.mInputEventConsistencyVerifier = new InputEventConsistencyVerifier(caller, flags);
    }

    public static boolean isInstrumentationEnabled() {
        return InputEventConsistencyVerifier.isInstrumentationEnabled();
    }

    public void onTouchEvent(MotionEvent event, int nestingLevel) {
        InputEventConsistencyVerifier inputEventConsistencyVerifier = this.mInputEventConsistencyVerifier;
        if (inputEventConsistencyVerifier != null) {
            inputEventConsistencyVerifier.onTouchEvent(event, nestingLevel);
        }
    }

    public void onUnhandledEvent(MotionEvent event, int nestingLevel) {
        InputEventConsistencyVerifier inputEventConsistencyVerifier = this.mInputEventConsistencyVerifier;
        if (inputEventConsistencyVerifier != null) {
            inputEventConsistencyVerifier.onUnhandledEvent(event, nestingLevel);
        }
    }

    public void onGenericMotionEvent(MotionEvent event, int nestingLevel) {
        InputEventConsistencyVerifier inputEventConsistencyVerifier = this.mInputEventConsistencyVerifier;
        if (inputEventConsistencyVerifier != null) {
            inputEventConsistencyVerifier.onGenericMotionEvent(event, nestingLevel);
        }
    }
}
