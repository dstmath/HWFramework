package com.android.server.wm;

import android.graphics.Rect;
import android.view.WindowManager;
import android.view.animation.Animation;

public class WindowStateBridge implements IHwWindowStateEx {
    private WindowStateBridgeEx mWindowStateBridgeEx;

    public WindowStateBridge(WindowManagerServiceEx windowManagerServiceEx, WindowStateEx windowStateEx) {
    }

    public void setWindowStateBridgeEx(WindowStateBridgeEx bridgeEx) {
        this.mWindowStateBridgeEx = bridgeEx;
    }

    public Rect adjustImePosForFreeform(Rect contentFrame, Rect containingFrame) {
        return this.mWindowStateBridgeEx.adjustImePosForFreeform(contentFrame, containingFrame);
    }

    public boolean isInHwFreeFormWorkspace() {
        return this.mWindowStateBridgeEx.isInHwFreeFormWorkspace();
    }

    public boolean isInHideCaptionList() {
        return this.mWindowStateBridgeEx.isInHideCaptionList();
    }

    public int adjustTopForFreeform(Rect frame, Rect limitFrame, int minVisibleHeight) {
        return this.mWindowStateBridgeEx.adjustTopForFreeform(frame, limitFrame, minVisibleHeight);
    }

    public void createMagicWindowDimmer() {
        this.mWindowStateBridgeEx.createMagicWindowDimmer();
    }

    public void destoryMagicWindowDimmer() {
        this.mWindowStateBridgeEx.destoryMagicWindowDimmer();
    }

    public boolean updateMagicWindowDimmer() {
        return this.mWindowStateBridgeEx.updateMagicWindowDimmer();
    }

    public void stopMagicWindowDimmer() {
        this.mWindowStateBridgeEx.stopMagicWindowDimmer();
    }

    public boolean isNeedMoveAnimation(WindowState windowState) {
        return this.mWindowStateBridgeEx.isNeedMoveAnimation(new WindowStateEx(windowState));
    }

    public void setInputMethodWindowTop(int top) {
        this.mWindowStateBridgeEx.setInputMethodWindowTop(top);
    }

    public void initializeHwAnim(Animation anim, int appWidth, int appHeight, int frameWidth) {
        this.mWindowStateBridgeEx.initializeHwAnim(anim, appWidth, appHeight, frameWidth);
    }

    public int calculateInputMethodWindowHeight(int appHeight, int lazyMode, int frameBottom, int frameHeight, int rotation) {
        return this.mWindowStateBridgeEx.calculateInputMethodWindowHeight(appHeight, lazyMode, frameBottom, frameHeight, rotation);
    }

    public boolean isPopUpIme(int inputMethodWindowHeight, boolean isImeWithHwFlag, WindowManager.LayoutParams attrs) {
        return this.mWindowStateBridgeEx.isPopUpIme(inputMethodWindowHeight, isImeWithHwFlag, attrs);
    }
}
