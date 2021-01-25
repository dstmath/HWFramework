package com.huawei.server.policy;

import com.android.server.LocalServices;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.WindowStateEx;

public class PhoneWindowManagerEx {
    private PhoneWindowManager mPhoneWindowManager = ((PhoneWindowManager) LocalServices.getService(WindowManagerPolicy.class));

    public WindowStateEx getTopFullscreenWindow() {
        PhoneWindowManager phoneWindowManager = this.mPhoneWindowManager;
        if (phoneWindowManager == null || phoneWindowManager.getPhoneWindowManagerEx() == null || this.mPhoneWindowManager.getPhoneWindowManagerEx().getTopFullscreenWindow() == null) {
            return null;
        }
        WindowStateEx windowStateEx = new WindowStateEx();
        windowStateEx.setWindowState(this.mPhoneWindowManager.getPhoneWindowManagerEx().getTopFullscreenWindow());
        return windowStateEx;
    }

    public boolean isPhoneWindowManagerExEmpty() {
        PhoneWindowManager phoneWindowManager = this.mPhoneWindowManager;
        return phoneWindowManager == null || phoneWindowManager.getPhoneWindowManagerEx() == null;
    }

    public boolean isKeyguardShowingAndNotOccluded() {
        PhoneWindowManager phoneWindowManager = this.mPhoneWindowManager;
        if (phoneWindowManager == null) {
            return false;
        }
        return phoneWindowManager.isKeyguardShowingAndNotOccluded();
    }

    public void setAodState(int aodState) {
        PhoneWindowManager phoneWindowManager = this.mPhoneWindowManager;
        if (phoneWindowManager != null) {
            phoneWindowManager.setAodState(aodState);
        }
    }
}
