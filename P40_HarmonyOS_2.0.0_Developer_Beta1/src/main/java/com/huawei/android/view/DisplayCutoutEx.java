package com.huawei.android.view;

import android.content.res.Resources;
import android.view.DisplayCutout;

public class DisplayCutoutEx {
    public static DisplayCutout fromResourcesRectApproximation(Resources res, int displayWidth, int displayHeight) {
        return DisplayCutout.fromResourcesRectApproximation(res, displayWidth, displayHeight);
    }
}
