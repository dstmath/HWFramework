package com.android.server.wm.utils;

import com.android.server.wm.WindowManagerServiceEx;

public class HwDisplaySizeUtilEx {
    private static HwDisplaySizeUtil sHwDisplaySizeUtil;
    private static volatile HwDisplaySizeUtilEx sInstance;

    private HwDisplaySizeUtilEx(WindowManagerServiceEx windowManagerServiceEx) {
        if (windowManagerServiceEx != null) {
            sHwDisplaySizeUtil = HwDisplaySizeUtil.getInstance(windowManagerServiceEx.getWindowManagerService());
        }
    }

    public static boolean hasSideInScreen() {
        return HwDisplaySizeUtil.hasSideInScreen();
    }

    public static HwDisplaySizeUtilEx getInstance(WindowManagerServiceEx windowManagerServiceEx) {
        if (sInstance == null) {
            synchronized (HwDisplaySizeUtilEx.class) {
                if (sInstance == null) {
                    sInstance = new HwDisplaySizeUtilEx(windowManagerServiceEx);
                }
            }
        }
        return sInstance;
    }

    public int getSafeSideWidth() {
        return sHwDisplaySizeUtil.getSafeSideWidth();
    }
}
