package com.huawei.android.util;

import android.util.MathUtils;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class MathUtilsEx {
    public static float constrain(float amount, float low, float high) {
        return MathUtils.constrain(amount, low, high);
    }
}
