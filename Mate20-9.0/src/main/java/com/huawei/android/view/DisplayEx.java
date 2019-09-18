package com.huawei.android.view;

import android.view.Display;
import android.view.DisplayInfo;

public class DisplayEx {
    public static void getScreentRange(Display display, int[] range) {
        if (display != null) {
            DisplayInfo outDisplayInfo = new DisplayInfo();
            display.getDisplayInfo(outDisplayInfo);
            range[0] = outDisplayInfo.logicalWidth;
            range[1] = outDisplayInfo.logicalHeight;
        }
    }
}
