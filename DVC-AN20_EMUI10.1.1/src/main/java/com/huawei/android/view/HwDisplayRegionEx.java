package com.huawei.android.view;

import android.graphics.Rect;
import android.view.WindowInsets;
import java.util.List;

public class HwDisplayRegionEx {
    public static final int DISPLAY_AREA_TYPE_CUTOUT = 3;
    public static final int DISPLAY_AREA_TYPE_ROUND_CORNER = 1;
    public static final int DISPLAY_AREA_TYPE_SIDE = 2;
    public static final int DISPLAY_AREA_TYPE_UNION = 0;
    private static final String TAG = "HwDisplayRegionEx";
    private static HwDisplayRegionEx sInstance = null;
    private static WindowInsets sWindowInsets;

    private HwDisplayRegionEx() {
    }

    public static synchronized HwDisplayRegionEx getDisplayRegion(WindowInsets windowInsets) {
        HwDisplayRegionEx hwDisplayRegionEx;
        synchronized (HwDisplayRegionEx.class) {
            if (sInstance == null) {
                sInstance = new HwDisplayRegionEx();
            }
            if (windowInsets != sWindowInsets) {
                sWindowInsets = windowInsets;
            }
            hwDisplayRegionEx = sInstance;
        }
        return hwDisplayRegionEx;
    }

    public Rect getSafeInsets(int type) {
        return HwWindowManager.getSafeInsets(type, sWindowInsets);
    }

    public List<Rect> getBounds(int type) {
        return HwWindowManager.getBounds(type, sWindowInsets);
    }
}
