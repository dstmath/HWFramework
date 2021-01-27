package com.huawei.android.hardware.display;

import android.hardware.display.DisplayManagerGlobal;
import android.view.Display;

public class DisplayManagerGlobalEx {
    public static void setIAwareCacheEnable(boolean cache) {
        DisplayManagerGlobal.getInstance().setIAwareCacheEnable(cache);
    }

    public static Display getRealDisplay(int displayId) {
        return DisplayManagerGlobal.getInstance().getRealDisplay(displayId);
    }
}
