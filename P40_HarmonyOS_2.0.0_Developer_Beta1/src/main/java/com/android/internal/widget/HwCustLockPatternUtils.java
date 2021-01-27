package com.android.internal.widget;

import android.app.admin.DevicePolicyManager;
import android.content.Context;

public class HwCustLockPatternUtils {
    public boolean getPowerBtnInstantlyLockDefault() {
        return true;
    }

    public boolean isForbiddenSimplePwdFeatureEnable() {
        return false;
    }

    public boolean currentpwdSimpleCheck(String password) {
        return false;
    }

    public void initHwCustLockPatternUtils(DevicePolicyManager mDevicePolicyManager, Context context) {
    }

    public void saveCurrentPwdStatus(boolean flag) {
    }

    public boolean needLimit() {
        return false;
    }
}
