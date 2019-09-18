package com.huawei.android.view;

import android.view.ViewConfiguration;

public class ViewConfigurationEx {
    public static int getDoubleTapMinTime() {
        return ViewConfiguration.getDoubleTapMinTime();
    }

    public static int getDoubleTapSlop() {
        return ViewConfiguration.getDoubleTapSlop();
    }

    public static int getScaledDoubleTapTouchSlop(ViewConfiguration configuration) {
        return configuration.getScaledDoubleTapTouchSlop();
    }
}
