package com.huawei.android.hardware.hwtouchweight;

import huawei.android.hardware.hwtouchweight.HwTouchWeightManager;

public class TouchWeightEx {
    public static void resetTouchWeight() {
        HwTouchWeightManager.getInstance().resetTouchWeight();
    }

    public static String getTouchWeightValue() {
        return HwTouchWeightManager.getInstance().getTouchWeightValue();
    }
}
