package com.huawei.android.app.admin;

import android.content.ComponentName;
import android.os.Bundle;
import huawei.android.app.admin.HwDevicePolicyManagerEx;

public class DeviceLocationManager {
    private static final String DISALLOW_PASSIVE_LOCATION = "passive_location_disallow_item";
    private static final String TAG = DeviceLocationManager.class.getSimpleName();
    private final HwDevicePolicyManagerEx mDpm = new HwDevicePolicyManagerEx();

    public boolean allowPassiveLocation(ComponentName admin, boolean setflag) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("value", setflag ^ 1);
        return this.mDpm.setPolicy(admin, DISALLOW_PASSIVE_LOCATION, bundle);
    }

    public boolean getPassiveLocationPolicy(ComponentName admin) {
        Bundle bundle = this.mDpm.getPolicy(admin, DISALLOW_PASSIVE_LOCATION);
        if (bundle == null) {
            return true;
        }
        return bundle.getBoolean("value") ^ 1;
    }
}
