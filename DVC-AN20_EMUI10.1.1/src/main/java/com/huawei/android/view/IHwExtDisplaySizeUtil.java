package com.huawei.android.view;

import android.graphics.Rect;

public interface IHwExtDisplaySizeUtil {
    Rect getDisplaySafeInsets();

    Rect getDisplaySideSafeInsets();

    boolean hasNotchInScreen();

    boolean hasSideInScreen();
}
