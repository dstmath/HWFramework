package com.huawei.android.view;

import android.graphics.Rect;

public interface IHwDisplaySideRegion {
    Rect getSafeInsets();

    void setSafeInsets(Rect rect);
}
