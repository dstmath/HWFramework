package com.huawei.android.view;

import android.view.Window;

public class WindowEx {
    public static void setSplitActionBarAlways(Window window, boolean bAlwaysSplit) {
        window.setSplitActionBarAlways(bAlwaysSplit);
    }

    public static void setCloseOnTouchOutside(Window window, boolean close) {
        window.setCloseOnTouchOutside(close);
    }
}
