package com.android.server.wm;

import android.graphics.Rect;
import android.view.SurfaceControl;

public class WindowContainerEx {
    private WindowContainer mWindowContainer;
    private WindowManagerServiceEx mWindowManagerServiceEx;

    public WindowContainerEx() {
    }

    public WindowContainerEx(WindowContainer windowContainer) {
        this.mWindowContainer = windowContainer;
    }

    public WindowContainer getWindowContainer() {
        return this.mWindowContainer;
    }

    public void setWindowContainer(WindowContainer windowContainer) {
        this.mWindowContainer = windowContainer;
    }

    public WindowManagerServiceEx getWmService() {
        WindowContainer windowContainer;
        if (!(this.mWindowManagerServiceEx != null || (windowContainer = this.mWindowContainer) == null || windowContainer.mWmService == null)) {
            this.mWindowManagerServiceEx = new WindowManagerServiceEx();
            this.mWindowManagerServiceEx.setWindowManagerService(this.mWindowContainer.mWmService);
        }
        return this.mWindowManagerServiceEx;
    }

    public SurfaceControl.Builder makeChildSurface(WindowContainerEx wcChild) {
        WindowContainer windowContainer = this.mWindowContainer;
        WindowContainer windowContainer2 = null;
        if (windowContainer == null) {
            return null;
        }
        if (wcChild != null) {
            windowContainer2 = wcChild.getWindowContainer();
        }
        return windowContainer.makeChildSurface(windowContainer2);
    }

    public SurfaceControl getSurfaceControl() {
        WindowContainer windowContainer = this.mWindowContainer;
        if (windowContainer != null) {
            return windowContainer.getSurfaceControl();
        }
        return null;
    }

    public SurfaceControl.Transaction getPendingTransaction() {
        WindowContainer windowContainer = this.mWindowContainer;
        if (windowContainer != null) {
            return windowContainer.getPendingTransaction();
        }
        return null;
    }

    public void commitPendingTransaction() {
        WindowContainer windowContainer = this.mWindowContainer;
        if (windowContainer != null) {
            windowContainer.commitPendingTransaction();
        }
    }

    public SurfaceControl.Builder makeAnimationLeash() {
        WindowContainer windowContainer = this.mWindowContainer;
        if (windowContainer != null) {
            return windowContainer.makeAnimationLeash();
        }
        return null;
    }

    public Rect getBounds() {
        WindowContainer windowContainer = this.mWindowContainer;
        if (windowContainer != null) {
            return windowContainer.getBounds();
        }
        return null;
    }
}
