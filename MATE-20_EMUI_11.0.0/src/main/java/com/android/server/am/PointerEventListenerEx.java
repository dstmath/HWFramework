package com.android.server.am;

import android.view.MotionEvent;
import android.view.WindowManagerPolicyConstants;

public class PointerEventListenerEx {
    private PointerEventListenerBridge mBridge = new PointerEventListenerBridge();

    public PointerEventListenerEx() {
        this.mBridge.setPointerEventListenerEx(this);
    }

    public void onPointerEvent(MotionEvent motionEvent) {
    }

    public WindowManagerPolicyConstants.PointerEventListener getPointerEventListenerBridge() {
        return this.mBridge;
    }
}
