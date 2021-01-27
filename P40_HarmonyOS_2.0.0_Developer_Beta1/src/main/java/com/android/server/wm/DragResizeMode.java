package com.android.server.wm;

import android.util.HwPCUtils;

/* access modifiers changed from: package-private */
public class DragResizeMode {
    static final int DRAG_RESIZE_MODE_DOCKED_DIVIDER = 1;
    static final int DRAG_RESIZE_MODE_FREEFORM = 0;

    DragResizeMode() {
    }

    static boolean isModeAllowedForStack(TaskStack stack, int mode) {
        if (HwPCUtils.isExtDynamicStack(stack.mStackId)) {
            return true;
        }
        if (mode != 0) {
            if (mode != 1) {
                return false;
            }
            if (stack.inSplitScreenWindowingMode() || stack.inHwSplitScreenWindowingMode()) {
                return true;
            }
            return false;
        } else if (stack.getWindowingMode() == 5 || stack.inHwFreeFormWindowingMode()) {
            return true;
        } else {
            return false;
        }
    }
}
