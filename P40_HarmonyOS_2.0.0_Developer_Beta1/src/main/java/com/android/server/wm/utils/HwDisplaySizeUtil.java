package com.android.server.wm.utils;

import android.common.HwFrameworkFactory;
import android.graphics.Rect;
import android.os.IBinder;
import android.view.SurfaceControl;
import com.android.server.wm.DisplayContent;
import com.android.server.wm.WindowManagerService;

public class HwDisplaySizeUtil {
    private static final int DIVIDER = 2;
    private static final int PHYSICAL_HEIGHT = 2400;
    private static HwDisplaySizeUtil mInstance;
    private int mPhysicalHeight = 0;
    private final WindowManagerService mWindowManagerService;

    private HwDisplaySizeUtil(WindowManagerService wms) {
        this.mWindowManagerService = wms;
    }

    private int getPhysicalHeight() {
        SurfaceControl.PhysicalDisplayInfo[] configs;
        if (this.mPhysicalHeight == 0) {
            IBinder displayToken = SurfaceControl.getInternalDisplayToken();
            if (displayToken == null || (configs = SurfaceControl.getDisplayConfigs(displayToken)) == null || configs.length == 0) {
                return PHYSICAL_HEIGHT;
            }
            this.mPhysicalHeight = configs[0].height;
        }
        return this.mPhysicalHeight;
    }

    public static synchronized HwDisplaySizeUtil getInstance(WindowManagerService wms) {
        HwDisplaySizeUtil hwDisplaySizeUtil;
        synchronized (HwDisplaySizeUtil.class) {
            if (mInstance == null) {
                mInstance = new HwDisplaySizeUtil(wms);
            }
            hwDisplaySizeUtil = mInstance;
        }
        return hwDisplaySizeUtil;
    }

    public static boolean hasSideInScreen() {
        return HwFrameworkFactory.getHwExtDisplaySizeUtil().hasSideInScreen();
    }

    public int getSafeSideWidth() {
        Rect sideSafeRect = HwFrameworkFactory.getHwExtDisplaySizeUtil().getDisplaySideSafeInsets();
        int safeWidth = sideSafeRect.left + sideSafeRect.right;
        DisplayContent dc = this.mWindowManagerService.getDefaultDisplayContentLocked();
        if (dc == null) {
            return 0;
        }
        int displayHeight = dc.getBaseDisplayHeight();
        if (displayHeight == 0) {
            displayHeight = getPhysicalHeight();
        }
        return (int) (((float) (safeWidth / 2)) * ((((float) displayHeight) * 1.0f) / ((float) getPhysicalHeight())));
    }
}
