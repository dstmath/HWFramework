package com.android.server.wm;

import android.util.HwPCUtils;

class DragResizeMode {
    static final int DRAG_RESIZE_MODE_DOCKED_DIVIDER = 1;
    static final int DRAG_RESIZE_MODE_FREEFORM = 0;

    DragResizeMode() {
    }

    static boolean isModeAllowedForStack(int stackId, int mode) {
        boolean z = true;
        if (HwPCUtils.isPcDynamicStack(stackId)) {
            return true;
        }
        switch (mode) {
            case 0:
                if (stackId != 2) {
                    z = false;
                }
                return z;
            case 1:
                if (!(stackId == 3 || stackId == 1 || stackId == 0 || stackId == 5)) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }
}
