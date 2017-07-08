package com.android.server.wm;

class DragResizeMode {
    static final int DRAG_RESIZE_MODE_DOCKED_DIVIDER = 1;
    static final int DRAG_RESIZE_MODE_FREEFORM = 0;

    DragResizeMode() {
    }

    static boolean isModeAllowedForStack(int stackId, int mode) {
        boolean z = true;
        switch (mode) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                if (stackId != 2) {
                    z = false;
                }
                return z;
            case DRAG_RESIZE_MODE_DOCKED_DIVIDER /*1*/:
                if (!(stackId == 3 || stackId == DRAG_RESIZE_MODE_DOCKED_DIVIDER || stackId == 0)) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }
}
