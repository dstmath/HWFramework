package com.huawei.android.view;

import android.graphics.Rect;

public final class HwDisplaySideRegion implements IHwDisplaySideRegion {
    public static final HwDisplaySideRegion NO_SIDE = new HwDisplaySideRegion(ZERO_RECT);
    private static final String TAG = "HwDisplaySideRegion";
    private static final Rect ZERO_RECT = new Rect();
    private Rect mSafeInsets;

    public HwDisplaySideRegion(Rect safeInsets) {
        this.mSafeInsets = safeInsets != null ? new Rect(safeInsets) : ZERO_RECT;
    }

    public Rect getSafeInsets() {
        return this.mSafeInsets;
    }

    public void setSafeInsets(Rect rect) {
        this.mSafeInsets = rect;
    }
}
