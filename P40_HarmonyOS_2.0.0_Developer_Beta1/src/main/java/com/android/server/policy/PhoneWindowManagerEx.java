package com.android.server.policy;

import com.android.server.policy.WindowManagerPolicy;
import com.android.server.policy.WindowManagerPolicyEx;

public class PhoneWindowManagerEx {
    private PhoneWindowManager mPolicy;

    public void setPhoneWindowManager(PhoneWindowManager policy) {
        this.mPolicy = policy;
    }

    public boolean isShowing() {
        PhoneWindowManager phoneWindowManager = this.mPolicy;
        if (phoneWindowManager != null) {
            return phoneWindowManager.mKeyguardDelegate.isShowing();
        }
        return false;
    }

    public boolean isOccluded() {
        PhoneWindowManager phoneWindowManager = this.mPolicy;
        if (phoneWindowManager != null) {
            return phoneWindowManager.mKeyguardDelegate.isOccluded();
        }
        return false;
    }

    public int getNavigationBarBottom() {
        if (this.mPolicy != null) {
            return 4;
        }
        return 0;
    }

    public boolean isKeyguardLocked() {
        PhoneWindowManager phoneWindowManager = this.mPolicy;
        if (phoneWindowManager != null) {
            return phoneWindowManager.isKeyguardLocked();
        }
        return false;
    }

    public static WindowManagerPolicyEx.WindowStateEx getFocusedWindow(HwPhoneWindowManager policy) {
        WindowManagerPolicy.WindowState state;
        if (policy == null || (state = policy.getFocusedWindow()) == null) {
            return null;
        }
        WindowManagerPolicyEx.WindowStateEx stateEx = new WindowManagerPolicyEx.WindowStateEx();
        stateEx.setWindowState(state);
        return stateEx;
    }
}
