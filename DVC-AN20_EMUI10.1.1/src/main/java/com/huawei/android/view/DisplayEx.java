package com.huawei.android.view;

import android.view.Display;
import android.view.DisplayInfo;
import com.huawei.annotation.HwSystemApi;

public class DisplayEx {
    @HwSystemApi
    public static final int INVALID_DISPLAY = -1;
    @HwSystemApi
    public static final int TYPE_BUILT_IN = 1;
    @HwSystemApi
    public static final int TYPE_HDMI = 2;
    @HwSystemApi
    public static final int TYPE_OVERLAY = 4;
    @HwSystemApi
    public static final int TYPE_UNKNOWN = 0;
    @HwSystemApi
    public static final int TYPE_VIRTUAL = 5;
    @HwSystemApi
    public static final int TYPE_WIFI = 3;

    public static void getScreentRange(Display display, int[] range) {
        if (display != null) {
            DisplayInfo outDisplayInfo = new DisplayInfo();
            display.getDisplayInfo(outDisplayInfo);
            range[0] = outDisplayInfo.logicalWidth;
            range[1] = outDisplayInfo.logicalHeight;
        }
    }

    @HwSystemApi
    public static int getType(Display display) {
        if (display != null) {
            return display.getType();
        }
        return 0;
    }

    @HwSystemApi
    public static String getOwnerPackageName(Display display) {
        if (display != null) {
            return display.getOwnerPackageName();
        }
        return "";
    }

    @HwSystemApi
    public static boolean getDisplayInfo(Display display, DisplayInfoEx displayInfoEx) {
        if (display == null || displayInfoEx == null) {
            return false;
        }
        DisplayInfo displayInfo = new DisplayInfo();
        if (displayInfoEx.getDisplayInfo() != null) {
            displayInfo = displayInfoEx.getDisplayInfo();
        }
        if (!display.getDisplayInfo(displayInfo)) {
            return false;
        }
        displayInfoEx.setDisplayInfo(displayInfo);
        return true;
    }
}
