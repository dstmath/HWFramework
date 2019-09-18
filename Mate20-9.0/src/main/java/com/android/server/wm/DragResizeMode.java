package com.android.server.wm;

import android.util.HwPCUtils;

class DragResizeMode {
    static final int DRAG_RESIZE_MODE_DOCKED_DIVIDER = 1;
    static final int DRAG_RESIZE_MODE_FREEFORM = 0;

    DragResizeMode() {
    }

    static boolean isModeAllowedForStack(TaskStack stack, int mode) {
        if (HwPCUtils.isExtDynamicStack(stack.mStackId)) {
            return true;
        }
        boolean z = false;
        switch (mode) {
            case 0:
                if (stack.getWindowingMode() == 5) {
                    z = true;
                }
                return z;
            case 1:
                return stack.inSplitScreenWindowingMode();
            default:
                return false;
        }
    }
}
