package com.android.server.gesture;

import android.content.Context;

public class DefaultDeviceStateController {
    public DefaultDeviceStateController(Context context) {
    }

    public static DefaultDeviceStateController getInstance(Context context) {
        return new DefaultDeviceStateController(context);
    }

    public String getCurrentHomeActivity() {
        return null;
    }

    public void addCallback(DeviceChangedListener listener) {
    }

    public void removeCallback(DeviceChangedListener listener) {
    }

    public boolean isCurrentUserSetup() {
        return false;
    }

    public int getCurrentUser() {
        return 0;
    }

    public boolean isDeviceProvisioned() {
        return false;
    }

    public boolean isOOBEActivityEnabled() {
        return false;
    }

    public boolean isSetupWizardEnabled() {
        return false;
    }

    public static abstract class DeviceChangedListener {
        public void onDeviceProvisionedChanged(boolean isProvisioned) {
        }

        public void onUserSwitched(int newUserId) {
        }

        public void onUserSetupChanged(boolean isSetup) {
        }

        public void onConfigurationChanged() {
        }

        public void onPreferredActivityChanged(boolean isPrefer) {
        }
    }
}
