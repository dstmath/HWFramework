package com.huawei.android.app.admin;

import android.content.ComponentName;

public class DevicePolicyManagerCustEx {
    public static void setAllowSimplePassword(ComponentName who, boolean mode) {
        HwCustDevicePolicyManagerEx.setAllowSimplePassword(who, mode);
    }

    public static boolean getAllowSimplePassword(ComponentName who) {
        return HwCustDevicePolicyManagerEx.getAllowSimplePassword(who);
    }

    public static void saveCurrentPwdStatus(boolean isCurrentPwdSimple) {
        HwCustDevicePolicyManagerEx.saveCurrentPwdStatus(isCurrentPwdSimple);
    }
}
