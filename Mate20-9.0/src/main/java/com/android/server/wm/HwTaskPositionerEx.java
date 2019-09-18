package com.android.server.wm;

import android.freeform.HwFreeFormUtils;
import com.huawei.android.app.HwActivityManager;

public final class HwTaskPositionerEx implements IHwTaskPositionerEx {
    public static final String TAG = "HwTaskPositionerEx";
    final WindowManagerService mService;

    public HwTaskPositionerEx(WindowManagerService service) {
        this.mService = service;
    }

    public void updateFreeFormOutLine(int color) {
        if (HwFreeFormUtils.isFreeFormEnable()) {
            HwActivityManager.updateFreeFormOutLine(color);
        }
    }
}
