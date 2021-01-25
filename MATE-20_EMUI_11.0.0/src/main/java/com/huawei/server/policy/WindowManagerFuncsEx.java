package com.huawei.server.policy;

import com.android.server.am.PointerEventListenerEx;
import com.android.server.policy.WindowManagerPolicy;

public class WindowManagerFuncsEx {
    private WindowManagerPolicy.WindowManagerFuncs mWindowManagerFuncs;

    public WindowManagerPolicy.WindowManagerFuncs getWindowManagerFuncs() {
        return this.mWindowManagerFuncs;
    }

    public void setWindowManagerFuncs(WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs) {
        this.mWindowManagerFuncs = windowManagerFuncs;
    }

    public void registerPointerEventListener(PointerEventListenerEx listener, int displayId) {
        WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs = this.mWindowManagerFuncs;
        if (windowManagerFuncs != null && listener != null) {
            windowManagerFuncs.registerPointerEventListener(listener.getPointerEventListenerBridge(), displayId);
        }
    }

    public void unregisterPointerEventListener(PointerEventListenerEx listener, int displayId) {
        WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs = this.mWindowManagerFuncs;
        if (windowManagerFuncs != null && listener != null) {
            windowManagerFuncs.unregisterPointerEventListener(listener.getPointerEventListenerBridge(), displayId);
        }
    }
}
