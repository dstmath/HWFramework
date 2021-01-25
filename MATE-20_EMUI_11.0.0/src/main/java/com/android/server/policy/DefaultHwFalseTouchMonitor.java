package com.android.server.policy;

import android.view.KeyEvent;
import android.view.MotionEvent;
import com.android.server.am.PointerEventListenerEx;
import com.android.server.policy.WindowManagerPolicyEx;

public class DefaultHwFalseTouchMonitor {
    protected DefaultHwFalseTouchMonitor() {
    }

    public static DefaultHwFalseTouchMonitor getInstance() {
        return new DefaultHwFalseTouchMonitor();
    }

    public DefaultMotionEventListener getEventListener() {
        return null;
    }

    public boolean isFalseTouchFeatureOn() {
        return false;
    }

    public void handleFocusChanged(WindowManagerPolicyEx.WindowStateEx lastFocus, WindowManagerPolicyEx.WindowStateEx newFocus) {
    }

    public void handleKeyEvent(KeyEvent keyEvent) {
    }

    public static class DefaultMotionEventListener extends PointerEventListenerEx {
        @Override // com.android.server.am.PointerEventListenerEx
        public void onPointerEvent(MotionEvent motionEvent) {
        }
    }
}
