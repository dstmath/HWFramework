package com.android.server.wm;

public class WindowStateAnimatorEx {
    private WindowStateAnimator mWindowStateAnimator;

    public void setWindowStateAnimator(WindowStateAnimator windowStateAnimator) {
        this.mWindowStateAnimator = windowStateAnimator;
    }

    public WindowStateAnimator getWindowStateAnimator() {
        return this.mWindowStateAnimator;
    }
}
