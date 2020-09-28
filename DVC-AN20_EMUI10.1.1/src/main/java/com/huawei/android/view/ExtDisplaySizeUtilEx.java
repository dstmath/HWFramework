package com.huawei.android.view;

import android.common.HwFrameworkFactory;
import android.graphics.Rect;

public class ExtDisplaySizeUtilEx {
    public static Rect getDisplaySafeInsets() {
        return HwFrameworkFactory.getHwExtDisplaySizeUtil().getDisplaySafeInsets();
    }
}
