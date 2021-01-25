package com.android.server.wm;

import android.content.Context;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ScreenRotationAnimationBridgeEx {
    private ScreenRotationAnimationBridge mScreenRotationAnimationBridge;
    private WindowManagerService mWindowManagerService = null;

    public ScreenRotationAnimationBridgeEx(Context context, DisplayContentEx displayContentEx, boolean isFixedToUserRotation, boolean isSecure, WindowManagerServiceEx serviceEx) {
        this.mWindowManagerService = serviceEx.getWindowManagerService();
        this.mScreenRotationAnimationBridge = new ScreenRotationAnimationBridge(context, displayContentEx, isFixedToUserRotation, isSecure, serviceEx);
        this.mScreenRotationAnimationBridge.setScreenRotationAnimationEx(this);
    }

    public void kill() {
    }

    public void aospKill() {
        this.mScreenRotationAnimationBridge.aospKill();
    }

    /* access modifiers changed from: protected */
    public Animation createScreenFoldAnimation(boolean isEnter, int fromFoldMode, int toFoldMode) {
        return null;
    }

    /* access modifiers changed from: protected */
    public void stepAnimationToEnd(Animation animation, long nowTime) {
    }

    /* access modifiers changed from: protected */
    public Animation createFoldProjectionAnimation(int fromFoldMode, int toFoldMode) {
        return null;
    }

    public void setAnimationTypeInfo(Bundle animaitonTypeInfo) {
    }

    public Bundle getAnimationTypeInfo() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void updateFoldProjection() {
    }

    public long getExitAnimDuration() {
        return 0;
    }

    public WindowStateEx getWindowStateEx() {
        Task topTask;
        WindowState primaryWindow;
        TaskStack primaryStack = this.mScreenRotationAnimationBridge.getDisplayContent().getSplitScreenPrimaryStack();
        if (primaryStack == null || (topTask = primaryStack.getTopChild()) == null || (primaryWindow = topTask.getTopVisibleAppMainWindow()) == null) {
            return null;
        }
        return new WindowStateEx(primaryWindow);
    }

    public WindowStateEx getFocusedWindowStateEx() {
        WindowState focusedWindow = this.mWindowManagerService.getFocusedWindow();
        if (focusedWindow != null) {
            return new WindowStateEx(focusedWindow);
        }
        return null;
    }

    public DisplayContentEx getDisplayContentEx() {
        return new DisplayContentEx(this.mScreenRotationAnimationBridge.getDisplayContent());
    }

    public boolean isStackVisible(int windowMode) {
        return this.mScreenRotationAnimationBridge.getDisplayContent().isStackVisible(windowMode);
    }

    public int getDisplayId() {
        return this.mScreenRotationAnimationBridge.getDisplayContent().getDisplayId();
    }

    public int getDisplayRotation() {
        return this.mScreenRotationAnimationBridge.getDisplayContent().getRotation();
    }

    public ScreenRotationAnimationBridge getScreenRotationAnimationBridge() {
        return this.mScreenRotationAnimationBridge;
    }

    public boolean isStarted() {
        return this.mScreenRotationAnimationBridge.isStarted();
    }

    public void setStartState(boolean isStarted) {
        this.mScreenRotationAnimationBridge.setStartState(isStarted);
    }

    public Animation getFoldProjectionAnimation() {
        return this.mScreenRotationAnimationBridge.getFoldProjectionAnimation();
    }

    public boolean isFoldProjectionAnimationNull() {
        return this.mScreenRotationAnimationBridge.getFoldProjectionAnimation() == null;
    }

    public void setFoldProjectionAnimation(Animation animation) {
        this.mScreenRotationAnimationBridge.setFoldProjectionAnimation(animation);
    }

    public Transformation getFoldProjectionTransformation() {
        return this.mScreenRotationAnimationBridge.getFoldProjectionTransformation();
    }

    public void getValuesFromMatrix(float[] transFloats) {
        this.mScreenRotationAnimationBridge.getFoldProjectionTransformation().getMatrix().getValues(transFloats);
    }

    public String getTag() {
        return this.mScreenRotationAnimationBridge.getTag();
    }

    public void setAospAnimationTypeInfo(Bundle animationTypeInfo) {
        this.mScreenRotationAnimationBridge.setAospAnimationTypeInfo(animationTypeInfo);
    }

    public Bundle getAospAnimationTypeInfo() {
        return this.mScreenRotationAnimationBridge.getAospAnimationTypeInfo();
    }

    public void setUsingFoldAnim(boolean isUsingFoldAnim) {
        this.mScreenRotationAnimationBridge.setUsingFoldAnim(isUsingFoldAnim);
    }

    public boolean isUsingFoldAnim() {
        return this.mScreenRotationAnimationBridge.isUsingFoldAnim();
    }

    public void setRotateExitAnimation(Animation animation) {
        this.mScreenRotationAnimationBridge.setRotateExitAnimation(animation);
    }

    public Animation getRotateExitAnimation() {
        return this.mScreenRotationAnimationBridge.getRotateExitAnimation();
    }
}
