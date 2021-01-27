package com.android.server.wm;

public class WindowSurfaceControllerEx {
    private WindowSurfaceController mWindowSurfaceController;

    public void setWindowSurfaceController(WindowSurfaceController windowSurfaceController) {
        this.mWindowSurfaceController = windowSurfaceController;
    }

    public WindowSurfaceController getWindowSurfaceController() {
        return this.mWindowSurfaceController;
    }
}
