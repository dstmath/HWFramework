package com.android.server.policy;

import android.graphics.Rect;
import android.view.WindowManager;
import com.android.server.LocalServices;
import com.android.server.am.PointerEventListenerEx;
import com.android.server.policy.WindowManagerPolicy;

public class WindowManagerPolicyEx {
    public static final int FLAG_INJECT_DOWN_WITH_BATCH_MOVE = 262144;
    public static final int FLAG_TRANSFER_EVENT = 524288;
    public static final int OFF_BECAUSE_OF_PROX_SENSOR = 6;
    public static final int TRANSIT_ACTIVITY_OPEN = 6;
    public static final int USER_ROTATION_FREE = 0;
    public static final int USER_ROTATION_LOCKED = 1;
    private WindowManagerPolicy mWindowManagerPolicy;

    public WindowManagerPolicy getWindowManagerPolicy() {
        return this.mWindowManagerPolicy;
    }

    public void setWindowManagerPolicy(WindowManagerPolicy windowManagerPolicy) {
        this.mWindowManagerPolicy = windowManagerPolicy;
    }

    public boolean isKeyguardOccluded() {
        return this.mWindowManagerPolicy.isKeyguardOccluded();
    }

    public static WindowManagerPolicyEx getInstance() {
        WindowManagerPolicyEx policyEx = new WindowManagerPolicyEx();
        policyEx.setWindowManagerPolicy((WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class));
        return policyEx;
    }

    public HwPhoneWindowManager getHwPhoneWindowManager() {
        HwPhoneWindowManager policy = this.mWindowManagerPolicy;
        if (policy instanceof HwPhoneWindowManager) {
            return policy;
        }
        return null;
    }

    public static class WindowStateEx {
        private WindowManagerPolicy.WindowState mWindowState;

        public void setWindowState(WindowManagerPolicy.WindowState windowState) {
            this.mWindowState = windowState;
        }

        public WindowManagerPolicy.WindowState getWindowState() {
            return this.mWindowState;
        }

        public WindowManager.LayoutParams getAttrs() {
            WindowManagerPolicy.WindowState windowState = this.mWindowState;
            if (windowState != null) {
                return windowState.getAttrs();
            }
            return null;
        }

        public Rect getDisplayFrameLw() {
            WindowManagerPolicy.WindowState windowState = this.mWindowState;
            if (windowState != null) {
                return windowState.getDisplayFrameLw();
            }
            return null;
        }

        public String getOwningPackage() {
            WindowManagerPolicy.WindowState windowState = this.mWindowState;
            if (windowState != null) {
                return windowState.getOwningPackage();
            }
            return "";
        }

        public int getOwningUid() {
            WindowManagerPolicy.WindowState windowState = this.mWindowState;
            if (windowState != null) {
                return windowState.getOwningUid();
            }
            return -1;
        }

        public int getHwGestureNavOptions() {
            WindowManagerPolicy.WindowState windowState = this.mWindowState;
            if (windowState != null) {
                return windowState.getHwGestureNavOptions();
            }
            return -1;
        }

        public boolean isWindowUsingNotch() {
            WindowManagerPolicy.WindowState windowState = this.mWindowState;
            if (windowState != null) {
                return windowState.isWindowUsingNotch();
            }
            return false;
        }

        public int getWindowingMode() {
            WindowManagerPolicy.WindowState windowState = this.mWindowState;
            if (windowState != null) {
                return windowState.getWindowingMode();
            }
            return -1;
        }

        public String toString() {
            WindowManagerPolicy.WindowState windowState = this.mWindowState;
            if (windowState != null) {
                return windowState.toString();
            }
            return "";
        }
    }

    public static class WindowManagerFuncsEx {
        private WindowManagerPolicy.WindowManagerFuncs mWindowManagerFuncs;

        public void setWindowManagerFuncs(WindowManagerPolicy.WindowManagerFuncs funcs) {
            this.mWindowManagerFuncs = funcs;
        }

        public void registerPointerEventListener(PointerEventListenerEx listener, int displayId) {
            WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs = this.mWindowManagerFuncs;
            if (windowManagerFuncs != null) {
                windowManagerFuncs.registerPointerEventListener(listener.getPointerEventListenerBridge(), displayId);
            }
        }

        public void unregisterPointerEventListener(PointerEventListenerEx listener, int displayId) {
            WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs = this.mWindowManagerFuncs;
            if (windowManagerFuncs != null) {
                windowManagerFuncs.unregisterPointerEventListener(listener.getPointerEventListenerBridge(), displayId);
            }
        }

        public void lockDeviceNow() {
            WindowManagerPolicy.WindowManagerFuncs windowManagerFuncs = this.mWindowManagerFuncs;
            if (windowManagerFuncs != null) {
                windowManagerFuncs.lockDeviceNow();
            }
        }
    }

    public static WindowStateEx getFocusedWindow() {
        WindowManagerPolicy.WindowState windowstate;
        HwPhoneWindowManager policy = getInstance().getHwPhoneWindowManager();
        if (policy == null || (windowstate = policy.getFocusedWindow()) == null) {
            return null;
        }
        WindowStateEx windowStateEx = new WindowStateEx();
        windowStateEx.setWindowState(windowstate);
        return windowStateEx;
    }

    public static WindowStateEx getInputMethodWindow() {
        WindowManagerPolicy.WindowState windowstate;
        HwPhoneWindowManager policy = getInstance().getHwPhoneWindowManager();
        if (policy == null || (windowstate = policy.getInputMethodWindow()) == null) {
            return null;
        }
        WindowStateEx windowStateEx = new WindowStateEx();
        windowStateEx.setWindowState(windowstate);
        return windowStateEx;
    }

    public static WindowStateEx getNavigationBar() {
        WindowManagerPolicy.WindowState windowstate;
        HwPhoneWindowManager policy = getInstance().getHwPhoneWindowManager();
        if (policy == null || (windowstate = policy.getNavigationBar()) == null) {
            return null;
        }
        WindowStateEx windowStateEx = new WindowStateEx();
        windowStateEx.setWindowState(windowstate);
        return windowStateEx;
    }

    public boolean performHapticFeedback(int uid, String packageName, int effectId, boolean isAlways, String reason) {
        WindowManagerPolicy windowManagerPolicy = this.mWindowManagerPolicy;
        if (windowManagerPolicy != null) {
            return windowManagerPolicy.performHapticFeedback(uid, packageName, effectId, isAlways, reason);
        }
        return false;
    }

    public boolean isKeyguardLocked() {
        WindowManagerPolicy windowManagerPolicy = this.mWindowManagerPolicy;
        if (windowManagerPolicy != null) {
            return windowManagerPolicy.isKeyguardLocked();
        }
        return false;
    }

    public void notifyVolumePanelStatus(boolean isVolumePanelVisible) {
        WindowManagerPolicy windowManagerPolicy = this.mWindowManagerPolicy;
        if (windowManagerPolicy != null) {
            windowManagerPolicy.notifyVolumePanelStatus(isVolumePanelVisible);
        }
    }
}
