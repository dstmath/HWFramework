package com.huawei.android.app.admin;

import android.content.ComponentName;
import huawei.android.app.admin.HwDevicePolicyManagerEx;

public class DevicePhoneManager {
    private static final String TAG = "DevicePhoneManager";
    private final HwDevicePolicyManagerEx mDpm;

    public DevicePhoneManager() {
        this.mDpm = new HwDevicePolicyManagerEx();
    }

    public void hangupCalling(ComponentName admin) {
        this.mDpm.hangupCalling(admin);
    }
}
