package com.android.server.wm;

import android.view.WindowManager;

public class DefaultHwHiCarMultiWindowManager {
    public String getCurrentImePkg() {
        return "";
    }

    public int getInputMethodWidth() {
        return 0;
    }

    public void onMoveTaskToBack(int taskId) {
    }

    public boolean isRotationLandscape() {
        return false;
    }

    public int getAppDockWidth() {
        return 0;
    }

    public int getAppDockHeight() {
        return 0;
    }

    public boolean isHiCarNavigationBar(WindowManager.LayoutParams attrs) {
        return false;
    }
}
