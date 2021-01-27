package com.android.server.wm;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import com.android.server.HwServiceFactory;

public class WindowStateAnimatorBridgeEx {
    protected static final int DE_MESSAGE_ID_CUSTOM = 2;
    protected static final int TOP_LAYER = 400000;
    private final WindowAnimator mAnimator = this.mService.getWindowAnimator();
    protected final Context mContext = this.mService.getContext();
    protected final WindowManagerServiceEx mService = this.mWindowStateAnimatorBridge.getWindowManagerServiceEx();
    protected final WindowStateEx mWin = this.mWindowStateAnimatorBridge.getWindowStateEx();
    private WindowStateAnimatorBridge mWindowStateAnimatorBridge;

    public WindowStateAnimatorBridgeEx(WindowStateEx windowStateEx) {
        this.mWindowStateAnimatorBridge = new WindowStateAnimatorBridge(windowStateEx);
        this.mWindowStateAnimatorBridge.setWindowStateAnimatorEx(this);
    }

    public WindowStateAnimatorBridge getWindowStateAnimatorBridge() {
        return this.mWindowStateAnimatorBridge;
    }

    public int adjustAnimLayerIfCoverclosed(int type, int animLayer) {
        return 0;
    }

    public boolean isCoverClosed() {
        return HwServiceFactory.isCoverClosed();
    }

    /* access modifiers changed from: protected */
    public boolean isScreenAnimation(int displayId) {
        ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
        return screenRotationAnimation != null && screenRotationAnimation.isAnimating();
    }

    /* access modifiers changed from: protected */
    public boolean isRotating(int displayId) {
        ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
        return screenRotationAnimation != null && screenRotationAnimation.isRotating();
    }

    public float getAlphaFromEnterTransformation(int displayId) {
        ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
        if (screenRotationAnimation == null || screenRotationAnimation.getEnterTransformation() == null) {
            return 0.0f;
        }
        return screenRotationAnimation.getEnterTransformation().getAlpha();
    }

    public ScreenRotationAnimationBridge getScreenRotationAnimationBridge(int displayId) {
        ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
        if (screenRotationAnimation == null || !(screenRotationAnimation instanceof ScreenRotationAnimationBridge)) {
            return null;
        }
        return (ScreenRotationAnimationBridge) screenRotationAnimation;
    }

    public boolean isLazyIsExiting() {
        return this.mWindowStateAnimatorBridge.mLazyIsExiting;
    }

    public void setLazyIsExiting(boolean isLazyExiting) {
        this.mWindowStateAnimatorBridge.mLazyIsExiting = isLazyExiting;
    }

    public boolean isLazyIsEntering() {
        return this.mWindowStateAnimatorBridge.mLazyIsEntering;
    }

    public void setLazyIsEntering(boolean isLazyEntering) {
        this.mWindowStateAnimatorBridge.mLazyIsEntering = isLazyEntering;
    }

    public boolean isHaveMatrix() {
        return this.mWindowStateAnimatorBridge.mHaveMatrix;
    }

    public void setHaveMatrix(boolean isHaveMatrix) {
        this.mWindowStateAnimatorBridge.mHaveMatrix = isHaveMatrix;
    }

    public void setDsDx(float dsDx) {
        this.mWindowStateAnimatorBridge.mDsDx = dsDx;
    }

    public float getDsDx() {
        return this.mWindowStateAnimatorBridge.mDsDx;
    }

    public void setDtDx(float dtDx) {
        this.mWindowStateAnimatorBridge.mDtDx = dtDx;
    }

    public float getDtDx() {
        return this.mWindowStateAnimatorBridge.mDtDx;
    }

    public void setDsDy(float dsDy) {
        this.mWindowStateAnimatorBridge.mDsDy = dsDy;
    }

    public float getDsDy() {
        return this.mWindowStateAnimatorBridge.mDsDy;
    }

    public void setDtDy(float dtDy) {
        this.mWindowStateAnimatorBridge.mDtDy = dtDy;
    }

    public float getDtDy() {
        return this.mWindowStateAnimatorBridge.mDtDy;
    }

    public Point getShownPosition() {
        return this.mWindowStateAnimatorBridge.mShownPosition;
    }

    public float getShownAlpha() {
        return this.mWindowStateAnimatorBridge.mShownAlpha;
    }

    public void setShownAlpha(float shownAlpha) {
        this.mWindowStateAnimatorBridge.mShownAlpha = shownAlpha;
    }

    public float getAlpha() {
        return this.mWindowStateAnimatorBridge.mAlpha;
    }

    public void setAlpha(float alpha) {
        this.mWindowStateAnimatorBridge.mAlpha = alpha;
    }

    public boolean isWallpaper() {
        return this.mWindowStateAnimatorBridge.mIsWallpaper;
    }

    public int getSurfaceFormat() {
        return this.mWindowStateAnimatorBridge.mSurfaceFormat;
    }

    /* access modifiers changed from: protected */
    public WindowSurfaceControllerEx createSurfaceLocked(int windowType, int ownerUid) {
        return null;
    }

    /* access modifiers changed from: protected */
    public WindowSurfaceControllerEx aospCreateSurfaceLocked(int windowType, int ownerUid) {
        return this.mWindowStateAnimatorBridge.aospCreateSurfaceLocked(windowType, ownerUid);
    }

    /* access modifiers changed from: package-private */
    public void destroySurfaceLocked() {
    }

    /* access modifiers changed from: protected */
    public void aospDestroySurfaceLocked() {
        this.mWindowStateAnimatorBridge.aospDestroySurfaceLocked();
    }

    public Rect getParentWindowContainerBounds() {
        WindowContainer parentWindowContainer = this.mWin.getParent();
        if (parentWindowContainer != null) {
            return parentWindowContainer.getBounds();
        }
        return null;
    }

    public boolean isEvilWindow(WindowStateEx win) {
        return false;
    }

    public void setOffsetLayer(int offsetLayer) {
        this.mAnimator.offsetLayer = offsetLayer;
    }
}
