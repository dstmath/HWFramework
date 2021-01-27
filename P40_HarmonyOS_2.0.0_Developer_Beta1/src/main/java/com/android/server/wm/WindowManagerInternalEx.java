package com.android.server.wm;

import android.view.MotionEvent;
import com.android.server.LocalServices;

public class WindowManagerInternalEx {
    private WindowManagerInternal mWindowManagerInternal = ((WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class));

    public void setFocusedDisplay(int displayId, boolean findTopTask, String reason) {
        WindowManagerInternal windowManagerInternal = this.mWindowManagerInternal;
        if (windowManagerInternal != null) {
            windowManagerInternal.setFocusedDisplay(displayId, findTopTask, reason);
        }
    }

    public int getFocusedDisplayId() {
        WindowManagerInternal windowManagerInternal = this.mWindowManagerInternal;
        if (windowManagerInternal == null) {
            return 0;
        }
        return windowManagerInternal.getFocusedDisplayId();
    }

    public void showOrHideInsetSurface(MotionEvent event) {
        WindowManagerInternal windowManagerInternal = this.mWindowManagerInternal;
        if (windowManagerInternal != null) {
            windowManagerInternal.showOrHideInsetSurface(event);
        }
    }

    public boolean isStackVisibleLw(int windowingMode) {
        WindowManagerInternal windowManagerInternal = this.mWindowManagerInternal;
        if (windowManagerInternal == null) {
            return false;
        }
        return windowManagerInternal.isStackVisibleLw(windowingMode);
    }

    public int getFocusedAppWindowMode() {
        WindowManagerInternal windowManagerInternal = this.mWindowManagerInternal;
        if (windowManagerInternal == null) {
            return 1;
        }
        return windowManagerInternal.getFocusedAppWindowMode();
    }

    public void setFoldSwitchState(boolean isState) {
        WindowManagerInternal windowManagerInternal = this.mWindowManagerInternal;
        if (windowManagerInternal != null) {
            windowManagerInternal.setFoldSwitchState(isState);
        }
    }

    public void unFreezeFoldRotation() {
        WindowManagerInternal windowManagerInternal = this.mWindowManagerInternal;
        if (windowManagerInternal != null) {
            windowManagerInternal.unFreezeFoldRotation();
        }
    }
}
