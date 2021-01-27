package com.android.systemui.shared.system;

import android.app.AppGlobals;
import android.app.admin.DevicePolicyManager;

public class DevicePolicyManagerWrapper {
    private static final DevicePolicyManager DEVICE_POLICY_MANAGER;
    private static final DevicePolicyManagerWrapper sInstance = new DevicePolicyManagerWrapper();

    static {
        DevicePolicyManager devicePolicyManager;
        if (AppGlobals.getInitialApplication() != null) {
            devicePolicyManager = (DevicePolicyManager) AppGlobals.getInitialApplication().getSystemService(DevicePolicyManager.class);
        } else {
            devicePolicyManager = null;
        }
        DEVICE_POLICY_MANAGER = devicePolicyManager;
    }

    private DevicePolicyManagerWrapper() {
    }

    public static DevicePolicyManagerWrapper getInstance() {
        return sInstance;
    }

    public boolean isLockTaskPermitted(String pkg) {
        DevicePolicyManager devicePolicyManager = DEVICE_POLICY_MANAGER;
        return devicePolicyManager != null && devicePolicyManager.isLockTaskPermitted(pkg);
    }
}
