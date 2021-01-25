package com.android.server.wm;

import android.graphics.Rect;
import android.view.WindowManager;

public class WindowStateCommonEx {
    private WindowState mWindowState;

    public WindowStateCommonEx() {
    }

    public WindowStateCommonEx(WindowState windowState) {
        this.mWindowState = windowState;
    }

    public void setWindowState(WindowState windowState) {
        this.mWindowState = windowState;
    }

    public WindowState getWindowState() {
        return this.mWindowState;
    }

    public int getDisplayId() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getDisplayId();
        }
        return 0;
    }

    public WindowManager.LayoutParams getAttrs() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getAttrs();
        }
        return null;
    }

    public Rect getVisibleFrameLw() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getVisibleFrameLw();
        }
        return null;
    }

    public Rect getDisplayFrameLw() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getDisplayFrameLw();
        }
        return null;
    }

    public int getPid() {
        return this.mWindowState.mSession.mPid;
    }

    public boolean isSessionNull() {
        return this.mWindowState.mSession == null;
    }

    public boolean isWindowStateNull() {
        return this.mWindowState == null;
    }

    public boolean inHwMagicWindowingMode() {
        return this.mWindowState.inHwMagicWindowingMode();
    }

    public float getMmUsedScaleFactor() {
        return this.mWindowState.mMwUsedScaleFactor;
    }

    public Rect getFrameLw() {
        return this.mWindowState.getFrameLw();
    }

    public String getOwningPackageName() {
        return this.mWindowState.getOwningPackage();
    }

    public String toString() {
        return this.mWindowState.toString();
    }
}
