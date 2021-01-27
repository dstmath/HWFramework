package com.android.server.wm;

import android.content.Context;
import android.os.Bundle;
import android.util.proto.ProtoOutputStream;
import android.view.SurfaceControl;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import java.io.PrintWriter;

public class ScreenRotationAnimationBridge extends ScreenRotationAnimation {
    private ScreenRotationAnimationBridgeEx mScreenRotationAnimEx;

    public /* bridge */ /* synthetic */ boolean dismiss(SurfaceControl.Transaction x0, long x1, float x2, int x3, int x4, int x5, int x6) {
        return ScreenRotationAnimationBridge.super.dismiss(x0, x1, x2, x3, x4, x5, x6);
    }

    public /* bridge */ /* synthetic */ void printTo(String x0, PrintWriter x1) {
        ScreenRotationAnimationBridge.super.printTo(x0, x1);
    }

    public /* bridge */ /* synthetic */ void setIsSingleHandScreenShotAnim(boolean x0) {
        ScreenRotationAnimationBridge.super.setIsSingleHandScreenShotAnim(x0);
    }

    public /* bridge */ /* synthetic */ boolean setRotation(SurfaceControl.Transaction x0, int x1, long x2, float x3, int x4, int x5) {
        return ScreenRotationAnimationBridge.super.setRotation(x0, x1, x2, x3, x4, x5);
    }

    public /* bridge */ /* synthetic */ boolean stepAnimationLocked(long x0) {
        return ScreenRotationAnimationBridge.super.stepAnimationLocked(x0);
    }

    public /* bridge */ /* synthetic */ void writeToProto(ProtoOutputStream x0, long x1) {
        ScreenRotationAnimationBridge.super.writeToProto(x0, x1);
    }

    public ScreenRotationAnimationBridge(Context context, DisplayContentEx displayContentEx, boolean isFixedToUserRotation, boolean isSecure, WindowManagerServiceEx serviceEx) {
        super(context, displayContentEx.getDisplayContent(), isFixedToUserRotation, isSecure, serviceEx.getWindowManagerService());
    }

    public void setScreenRotationAnimationEx(ScreenRotationAnimationBridgeEx screenRotationAnimationEx) {
        this.mScreenRotationAnimEx = screenRotationAnimationEx;
    }

    public Bundle getAnimationTypeInfo() {
        return this.mScreenRotationAnimEx.getAnimationTypeInfo();
    }

    public long getExitAnimDuration() {
        return this.mScreenRotationAnimEx.getExitAnimDuration();
    }

    public void kill() {
        this.mScreenRotationAnimEx.kill();
    }

    public void aospKill() {
        ScreenRotationAnimationBridge.super.kill();
    }

    public void setAnimationTypeInfo(Bundle animaitonTypeInfo) {
        this.mScreenRotationAnimEx.setAnimationTypeInfo(animaitonTypeInfo);
    }

    /* access modifiers changed from: protected */
    public Animation createScreenFoldAnimation(boolean isEnter, int fromFoldMode, int toFoldMode) {
        return this.mScreenRotationAnimEx.createScreenFoldAnimation(isEnter, fromFoldMode, toFoldMode);
    }

    /* access modifiers changed from: protected */
    public void stepAnimationToEnd(Animation animation, long nowTime) {
        this.mScreenRotationAnimEx.stepAnimationToEnd(animation, nowTime);
    }

    /* access modifiers changed from: protected */
    public Animation createFoldProjectionAnimation(int fromFoldMode, int toFoldMode) {
        return this.mScreenRotationAnimEx.createFoldProjectionAnimation(fromFoldMode, toFoldMode);
    }

    /* access modifiers changed from: protected */
    public void updateFoldProjection() {
        this.mScreenRotationAnimEx.updateFoldProjection();
    }

    public String getTag() {
        return "WindowManager";
    }

    public void setAospAnimationTypeInfo(Bundle animationTypeInfo) {
        this.mAnimationTypeInfo = animationTypeInfo;
    }

    public Bundle getAospAnimationTypeInfo() {
        return this.mAnimationTypeInfo;
    }

    public void setUsingFoldAnim(boolean isUsingFoldAnim) {
        this.mUsingFoldAnim = isUsingFoldAnim;
    }

    public boolean isUsingFoldAnim() {
        return this.mUsingFoldAnim;
    }

    public void setRotateExitAnimation(Animation animation) {
        this.mRotateExitAnimation = animation;
    }

    public Animation getRotateExitAnimation() {
        return this.mRotateExitAnimation;
    }

    public DisplayContent getDisplayContent() {
        return this.mDisplayContent;
    }

    public boolean isStarted() {
        return this.mStarted;
    }

    public void setStartState(boolean isStarted) {
        this.mStarted = isStarted;
    }

    public Animation getFoldProjectionAnimation() {
        return this.mFoldProjectionAnimation;
    }

    public void setFoldProjectionAnimation(Animation animation) {
        this.mFoldProjectionAnimation = animation;
    }

    public Transformation getFoldProjectionTransformation() {
        return this.mFoldProjectionTransformation;
    }

    public boolean isAnimating() {
        return ScreenRotationAnimationBridge.super.isAnimating();
    }

    public boolean isRotating() {
        return ScreenRotationAnimationBridge.super.isRotating();
    }

    public Transformation getEnterTransformation() {
        return ScreenRotationAnimationBridge.super.getEnterTransformation();
    }
}
