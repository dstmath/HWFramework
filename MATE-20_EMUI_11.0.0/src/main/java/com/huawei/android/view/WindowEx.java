package com.huawei.android.view;

import android.view.Window;

public class WindowEx {
    public static void setSplitActionBarAlways(Window window, boolean isAlwaysSplit) {
        window.setSplitActionBarAlways(isAlwaysSplit);
    }

    public static void setCloseOnTouchOutside(Window window, boolean isClose) {
        window.setCloseOnTouchOutside(isClose);
    }
}
