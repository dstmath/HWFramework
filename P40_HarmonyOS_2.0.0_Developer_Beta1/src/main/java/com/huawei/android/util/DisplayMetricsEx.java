package com.huawei.android.util;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DisplayMetricsEx {
    public static int getNoncompatDensityDpi(Resources res) {
        return res.getDisplayMetrics().noncompatDensityDpi;
    }

    public static int getNoncompatDensityDpi(DisplayMetrics dm) {
        return dm.noncompatDensityDpi;
    }

    public static float getDensityDefaultScale() {
        return 0.00625f;
    }
}
