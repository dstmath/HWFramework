package com.android.server.wifi.HwUtil;

import android.content.ComponentName;
import android.os.Bundle;
import huawei.android.app.admin.HwDevicePolicyManagerEx;

public class HwDevicePolicyManager implements IHwDevicePolicyManagerEx {
    private static HwDevicePolicyManagerEx mHwDevicePolicyManagerEx = null;

    public static synchronized HwDevicePolicyManager createHwDevicePolicyManager() {
        HwDevicePolicyManager hwDevicePolicyManager;
        synchronized (HwDevicePolicyManager.class) {
            hwDevicePolicyManager = new HwDevicePolicyManager();
        }
        return hwDevicePolicyManager;
    }

    public Bundle getPolicy(ComponentName who, String policyName) {
        return mHwDevicePolicyManagerEx.getPolicy(who, policyName);
    }

    private HwDevicePolicyManager() {
        mHwDevicePolicyManagerEx = new HwDevicePolicyManagerEx();
    }
}
