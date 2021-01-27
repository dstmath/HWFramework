package com.android.server.wm;

import android.view.animation.Animation;

public class ScreenRotationAnimationEx {
    private ScreenRotationAnimation mScreenRotationAnimation;

    public ScreenRotationAnimation getScreenRotationAnimation() {
        return this.mScreenRotationAnimation;
    }

    public void setScreenRotationAnimation(ScreenRotationAnimation screenRotationAnimation) {
        this.mScreenRotationAnimation = screenRotationAnimation;
    }

    public void resetScreenRotationAnimation(Object screenRotationAnimation) {
        if (screenRotationAnimation != null && (screenRotationAnimation instanceof ScreenRotationAnimation)) {
            this.mScreenRotationAnimation = (ScreenRotationAnimation) screenRotationAnimation;
        }
    }

    public boolean isHwMagicWindow() {
        ScreenRotationAnimation screenRotationAnimation = this.mScreenRotationAnimation;
        if (screenRotationAnimation != null) {
            return screenRotationAnimation.mIsHwMagicWindow;
        }
        return false;
    }

    public void setHwMagicWindow(boolean isHwMagicWindow) {
        ScreenRotationAnimation screenRotationAnimation = this.mScreenRotationAnimation;
        if (screenRotationAnimation != null) {
            screenRotationAnimation.mIsHwMagicWindow = isHwMagicWindow;
        }
    }

    public Animation getRotateExitAnimation() {
        ScreenRotationAnimation screenRotationAnimation = this.mScreenRotationAnimation;
        if (screenRotationAnimation != null) {
            return screenRotationAnimation.mRotateExitAnimation;
        }
        return null;
    }

    public void setRotateExitAnimation(Animation rotateExitAnimation) {
        ScreenRotationAnimation screenRotationAnimation = this.mScreenRotationAnimation;
        if (screenRotationAnimation != null) {
            screenRotationAnimation.mRotateExitAnimation = rotateExitAnimation;
        }
    }

    public Animation getRotateEnterAnimation() {
        ScreenRotationAnimation screenRotationAnimation = this.mScreenRotationAnimation;
        if (screenRotationAnimation != null) {
            return screenRotationAnimation.mRotateEnterAnimation;
        }
        return null;
    }

    public void setRotateEnterAnimation(Animation rotateEnterAnimation) {
        ScreenRotationAnimation screenRotationAnimation = this.mScreenRotationAnimation;
        if (screenRotationAnimation != null) {
            screenRotationAnimation.mRotateEnterAnimation = rotateEnterAnimation;
        }
    }

    public int getOriginalRotation() {
        ScreenRotationAnimation screenRotationAnimation = this.mScreenRotationAnimation;
        if (screenRotationAnimation != null) {
            return screenRotationAnimation.mOriginalRotation;
        }
        return 0;
    }

    public int getOriginalHeight() {
        ScreenRotationAnimation screenRotationAnimation = this.mScreenRotationAnimation;
        if (screenRotationAnimation != null) {
            return screenRotationAnimation.mOriginalHeight;
        }
        return 0;
    }
}
