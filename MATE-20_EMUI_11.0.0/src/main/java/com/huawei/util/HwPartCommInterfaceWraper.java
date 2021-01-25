package com.huawei.util;

import android.hdm.HwDeviceManager;
import android.util.CoordinationModeUtils;
import com.android.server.policy.HwPolicyFactory;

public class HwPartCommInterfaceWraper {
    public static final int DISABLED_ADB = 11;
    public static final int IS_DISABLE_SCREEN_TURN_OFF = 61;

    public static int getFoldScreenFullWidth() {
        return CoordinationModeUtils.getFoldScreenFullWidth();
    }

    public static int getFoldScreenMainWidth() {
        return CoordinationModeUtils.getFoldScreenMainWidth();
    }

    public static boolean disallowOp(int type) {
        return HwDeviceManager.disallowOp(type);
    }

    public static boolean isHwFastShutdownEnable() {
        return HwPolicyFactory.isHwFastShutdownEnable();
    }
}
