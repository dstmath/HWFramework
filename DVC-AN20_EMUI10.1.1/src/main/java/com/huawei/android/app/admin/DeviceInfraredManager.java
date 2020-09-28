package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import huawei.android.app.admin.HwDevicePolicyManagerEx;

public class DeviceInfraredManager {
    private static final String INFRARED_ISALLOW_FORBID_KEY = "infrared_item_policy_value";
    private static final String INFRARED_ISALLOW_ITEM_POLICY_NAME = "infrared_item_policy_name";
    private static final String TAG = DeviceInfraredManager.class.getSimpleName();
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public boolean setInfrardDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(INFRARED_ISALLOW_FORBID_KEY, disabled);
        return this.mDpm.setPolicy(admin, INFRARED_ISALLOW_ITEM_POLICY_NAME, bundle);
    }
}
