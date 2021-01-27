package com.android.server.wm;

public class WindowStateAnimatorBridge extends WindowStateAnimator {
    private WindowStateAnimatorBridgeEx mWindowStateAnimatorEx;

    public WindowStateAnimatorBridge(WindowStateEx windowStateEx) {
        super(windowStateEx.getWindowState());
    }

    public void setWindowStateAnimatorEx(WindowStateAnimatorBridgeEx windowStateAnimatorEx) {
        this.mWindowStateAnimatorEx = windowStateAnimatorEx;
    }

    public WindowManagerServiceEx getWindowManagerServiceEx() {
        return new WindowManagerServiceEx(this.mService);
    }

    public WindowStateEx getWindowStateEx() {
        return new WindowStateEx(this.mWin);
    }

    public int adjustAnimLayerIfCoverclosed(int type, int animLayer) {
        return this.mWindowStateAnimatorEx.adjustAnimLayerIfCoverclosed(type, animLayer);
    }

    /* access modifiers changed from: protected */
    public WindowSurfaceControllerEx aospCreateSurfaceLocked(int windowType, int ownerUid) {
        WindowSurfaceControllerEx windowSurfaceControllerEx = new WindowSurfaceControllerEx();
        windowSurfaceControllerEx.setWindowSurfaceController(WindowStateAnimatorBridge.super.createSurfaceLocked(windowType, ownerUid));
        return windowSurfaceControllerEx;
    }

    /* access modifiers changed from: protected */
    public WindowSurfaceController createSurfaceLocked(int windowType, int ownerUid) {
        WindowSurfaceControllerEx windowSurfaceController = this.mWindowStateAnimatorEx.createSurfaceLocked(windowType, ownerUid);
        if (windowSurfaceController == null) {
            return null;
        }
        return windowSurfaceController.getWindowSurfaceController();
    }

    /* access modifiers changed from: protected */
    public void destroySurfaceLocked() {
        this.mWindowStateAnimatorEx.destroySurfaceLocked();
    }

    /* access modifiers changed from: protected */
    public void aospDestroySurfaceLocked() {
        WindowStateAnimatorBridge.super.destroySurfaceLocked();
    }

    public boolean isEvilWindow(WindowState win) {
        if (win == null) {
            return this.mWindowStateAnimatorEx.isEvilWindow(null);
        }
        WindowStateEx windowStateEx = new WindowStateEx();
        windowStateEx.setWindowState(win);
        return this.mWindowStateAnimatorEx.isEvilWindow(windowStateEx);
    }
}
