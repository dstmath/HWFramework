package com.huawei.android.hardware.display;

import android.hardware.display.DisplayManagerGlobal;

public class DisplayManagerGlobalEx {
    public static void setIAwareCacheEnable(boolean cache) {
        DisplayManagerGlobal.getInstance().setIAwareCacheEnable(cache);
    }
}
