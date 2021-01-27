package com.android.server.am;

import android.view.MotionEvent;
import android.view.WindowManagerPolicyConstants;

public class PointerEventListenerBridge implements WindowManagerPolicyConstants.PointerEventListener {
    private PointerEventListenerEx pointerEventListenerEx;

    public void setPointerEventListenerEx(PointerEventListenerEx pointerEventListenerEx2) {
        this.pointerEventListenerEx = pointerEventListenerEx2;
    }

    public void onPointerEvent(MotionEvent motionEvent) {
        PointerEventListenerEx pointerEventListenerEx2 = this.pointerEventListenerEx;
        if (pointerEventListenerEx2 != null) {
            pointerEventListenerEx2.onPointerEvent(motionEvent);
        }
    }
}
