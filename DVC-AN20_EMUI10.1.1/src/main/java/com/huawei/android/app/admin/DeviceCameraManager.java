package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import huawei.android.app.admin.HwDevicePolicyManagerEx;

public class DeviceCameraManager {
    private static final String POLICY_DISABLE_VIDEO = "disable-video";
    private static final String STATE_VALUE = "value";
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public boolean setVideoDisabled(ComponentName admin, boolean disable) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", disable);
        return this.mDpm.setPolicy(admin, POLICY_DISABLE_VIDEO, bundle);
    }

    public boolean isVideoDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, POLICY_DISABLE_VIDEO);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean("value");
    }
}
