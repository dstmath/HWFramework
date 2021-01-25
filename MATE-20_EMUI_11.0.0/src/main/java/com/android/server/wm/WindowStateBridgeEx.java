package com.android.server.wm;

import android.graphics.Rect;
import android.view.WindowManager;
import android.view.animation.Animation;

public class WindowStateBridgeEx {
    private WindowStateBridge mWindowStateBridge;

    public WindowStateBridgeEx(WindowManagerServiceEx service, WindowStateEx windowState) {
        this.mWindowStateBridge = new WindowStateBridge(service, windowState);
        this.mWindowStateBridge.setWindowStateBridgeEx(this);
    }

    public WindowStateBridge getWindowStateBridge() {
        return this.mWindowStateBridge;
    }

    public Rect adjustImePosForFreeform(Rect contentFrame, Rect containingFrame) {
        return null;
    }

    public boolean isInHwFreeFormWorkspace() {
        return false;
    }

    public boolean isInHideCaptionList() {
        return false;
    }

    public int adjustTopForFreeform(Rect frame, Rect limitFrame, int minVisibleHeight) {
        return 0;
    }

    public void createMagicWindowDimmer() {
    }

    public void destoryMagicWindowDimmer() {
    }

    public boolean updateMagicWindowDimmer() {
        return false;
    }

    public void stopMagicWindowDimmer() {
    }

    public boolean isNeedMoveAnimation(WindowStateEx windowState) {
        return false;
    }

    public void setInputMethodWindowTop(int top) {
    }

    public void initializeHwAnim(Animation anim, int appWidth, int appHeight, int frameWidth) {
    }

    public int calculateInputMethodWindowHeight(int appHeight, int lazyMode, int frameBottom, int frameHeight, int rotation) {
        return 0;
    }

    public boolean isPopUpIme(int inputMethodWindowHeight, boolean isImeWithHwFlag, WindowManager.LayoutParams attrs) {
        return false;
    }
}
