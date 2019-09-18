package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import huawei.android.app.admin.HwDevicePolicyManagerEx;

public class DeviceP2PManager {
    private static final String DEVICE_P2P_POLICY_ITEM_NAME = "wifi_p2p_item_policy_name";
    private static final String DEVICE_P2P_POLICY_ITEM_VALUE = "wifi_p2p_policy_item_value";
    private static final String TAG = DeviceP2PManager.class.getSimpleName();
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public boolean setWifiP2PDisabled(ComponentName admin, boolean disabled) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(DEVICE_P2P_POLICY_ITEM_VALUE, disabled);
        return this.mDpm.setPolicy(admin, DEVICE_P2P_POLICY_ITEM_NAME, bundle);
    }

    public boolean isWifiP2PDisabled(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DEVICE_P2P_POLICY_ITEM_NAME);
        if (bundle == null) {
            return false;
        }
        return bundle.getBoolean(DEVICE_P2P_POLICY_ITEM_VALUE);
    }
}
