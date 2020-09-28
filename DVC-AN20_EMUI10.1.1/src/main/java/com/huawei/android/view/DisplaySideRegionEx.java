package com.huawei.android.view;

import android.graphics.Rect;

public final class DisplaySideRegionEx {
    public static final int DIRECTION_BOTTOM = 3;
    public static final int DIRECTION_LEFT = 0;
    public static final int DIRECTION_RIGHT = 2;
    public static final int DIRECTION_TOP = 1;
    private IHwDisplaySideRegion mInner;

    public DisplaySideRegionEx(IHwDisplaySideRegion displaySideRegion) {
        this.mInner = displaySideRegion;
    }

    public Rect getSafeInsets() {
        return this.mInner.getSafeInsets();
    }

    public int getSideWidth(int direction) {
        if (direction == 0) {
            return this.mInner.getSafeInsets().left;
        }
        if (direction == 1) {
            return this.mInner.getSafeInsets().top;
        }
        if (direction == 2) {
            return this.mInner.getSafeInsets().right;
        }
        if (direction != 3) {
            return 0;
        }
        return this.mInner.getSafeInsets().bottom;
    }
}
