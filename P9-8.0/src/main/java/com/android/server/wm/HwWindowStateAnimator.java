package com.android.server.wm;

import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.HwPCUtils;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.MagnificationSpec;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Transformation;
import com.android.server.HwServiceFactory;
import com.huawei.displayengine.DisplayEngineManager;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsCompModeID;

public class HwWindowStateAnimator extends WindowStateAnimator {
    private static final int SCENE_POS_EXIT = -1;
    private static final int SCENE_POS_START = 1;
    private static final String TAG = "HwWindowStateAnimator";
    private static final int TYPE_LEFT = 1;
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_RIGHT = 2;
    private Display mDefaultDisplay;
    private DisplayInfo mDefaultDisplayInfo = new DisplayInfo();
    private DisplayEngineManager mDisplayEngineManager;
    int mHeight;
    private boolean mIsDefaultDisplay = true;
    final boolean mIsLazy;
    boolean mIsPCSreenScaleing = false;
    boolean mLazyIsLeft;
    boolean mLazyIsRight;
    float mLazyScale;
    float mPCScreenScale = 1.0f;
    int mPreOpenLazyMode;
    int mPrePCScreenDisplayMode;
    int mWidth;
    private final WindowManager mWindowManager;

    public HwWindowStateAnimator(WindowState win) {
        super(win);
        this.mIsLazy = win.toString().contains("hwSingleMode_window");
        int displayId = this.mWin.getDisplayId();
        if (!(displayId == -1 || displayId == 0)) {
            this.mIsDefaultDisplay = false;
        }
        this.mPreOpenLazyMode = this.mService.getLazyMode();
        if (!this.mIsDefaultDisplay) {
            this.mPreOpenLazyMode = 0;
        }
        this.mPrePCScreenDisplayMode = this.mService.getPCScreenDisplayMode();
        this.mLazyScale = 1.0f;
        this.mLazyIsExiting = false;
        this.mLazyIsEntering = false;
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mDisplayEngineManager = new DisplayEngineManager();
    }

    private void updatedisplayinfo() {
        this.mDefaultDisplay = this.mWindowManager.getDefaultDisplay();
        this.mDefaultDisplay.getDisplayInfo(this.mDefaultDisplayInfo);
        boolean isPortrait = this.mDefaultDisplayInfo.logicalHeight > this.mDefaultDisplayInfo.logicalWidth;
        this.mWidth = isPortrait ? this.mDefaultDisplayInfo.logicalWidth : this.mDefaultDisplayInfo.logicalHeight;
        this.mHeight = isPortrait ? this.mDefaultDisplayInfo.logicalHeight : this.mDefaultDisplayInfo.logicalWidth;
    }

    public int adjustAnimLayerIfCoverclosed(int type, int animLayer) {
        if (type != 2000 || animLayer >= 400000 || (HwServiceFactory.isCoverClosed() ^ 1)) {
            return animLayer;
        }
        return 400000;
    }

    void computeShownFrameRightLocked() {
        computeShownFrameLocked(2);
    }

    void computeShownFrameLeftLocked() {
        computeShownFrameLocked(1);
    }

    /* JADX WARNING: Missing block: B:80:0x031a, code:
            if (floatEqualCompare(r24, (float) r8.top) != false) goto L_0x031c;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void computeShownFrameLocked(int type) {
        boolean selfTransformation = this.mHasLocalTransformation;
        Transformation attachedTransformation = (this.mParentWinAnimator == null || !this.mParentWinAnimator.mHasLocalTransformation) ? null : this.mParentWinAnimator.mTransformation;
        Transformation appTransformation = (this.mAppAnimator == null || !this.mAppAnimator.hasTransformation) ? null : this.mAppAnimator.transformation;
        if (type == 1 || type == 2) {
            updatedisplayinfo();
        }
        WindowState wallpaperTarget = this.mWallpaperControllerLocked.getWallpaperTarget();
        if (this.mIsWallpaper && wallpaperTarget != null && this.mService.mAnimateWallpaperWithTarget) {
            WindowStateAnimator wallpaperAnimator = wallpaperTarget.mWinAnimator;
            if (!(!wallpaperAnimator.mHasLocalTransformation || wallpaperAnimator.mAnimation == null || (wallpaperAnimator.mAnimation.getDetachWallpaper() ^ 1) == 0)) {
                attachedTransformation = wallpaperAnimator.mTransformation;
            }
            AppWindowAnimator wpAppAnimator = wallpaperTarget.mAppToken == null ? null : wallpaperTarget.mAppToken.mAppAnimator;
            if (!(wpAppAnimator == null || !wpAppAnimator.hasTransformation || wpAppAnimator.animation == null || (wpAppAnimator.animation.getDetachWallpaper() ^ 1) == 0)) {
                appTransformation = wpAppAnimator.transformation;
            }
        }
        ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(this.mWin.getDisplayId());
        boolean screenAnimation = screenRotationAnimation != null ? screenRotationAnimation.isAnimating() : false;
        float ratio = 1.0f;
        float pendingX = 0.0f;
        float pendingY = 0.0f;
        if (type == 1) {
            ratio = this.mLazyScale;
            pendingY = ((float) this.mHeight) * (1.0f - this.mLazyScale);
        } else if (type == 2) {
            ratio = this.mLazyScale;
            pendingX = ((float) this.mWidth) * (1.0f - this.mLazyScale);
            pendingY = ((float) this.mHeight) * (1.0f - this.mLazyScale);
        }
        this.mHasClipRect = false;
        Rect frame;
        float[] tmpFloats;
        Matrix tmpMatrix;
        MagnificationSpec spec;
        if (selfTransformation || attachedTransformation != null || appTransformation != null || screenAnimation) {
            frame = this.mWin.mFrame;
            tmpFloats = this.mService.mTmpFloats;
            tmpMatrix = this.mWin.mTmpMatrix;
            if ((screenAnimation && screenRotationAnimation.isRotating()) || isChildWindowAnimating()) {
                float w = (float) frame.width();
                float h = (float) frame.height();
                if (w < 1.0f || h < 1.0f) {
                    tmpMatrix.reset();
                } else {
                    tmpMatrix.setScale((4.0f / w) + 1.0f, (4.0f / h) + 1.0f, w / 2.0f, h / 2.0f);
                }
            } else {
                tmpMatrix.reset();
            }
            tmpMatrix.postScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
            if (selfTransformation) {
                tmpMatrix.postConcat(this.mTransformation.getMatrix());
            }
            tmpMatrix.postTranslate((float) (frame.left + this.mWin.mXOffset), (float) (frame.top + this.mWin.mYOffset));
            if (attachedTransformation != null) {
                tmpMatrix.postConcat(attachedTransformation.getMatrix());
            }
            if (appTransformation != null) {
                tmpMatrix.postConcat(appTransformation.getMatrix());
            }
            if (screenAnimation) {
                tmpMatrix.postConcat(screenRotationAnimation.getEnterTransformation().getMatrix());
            }
            spec = getMagnificationSpec();
            if (spec != null) {
                applyMagnificationSpec(spec, tmpMatrix);
            }
            this.mHaveMatrix = true;
            tmpMatrix.getValues(tmpFloats);
            this.mDsDx = tmpFloats[0] * ratio;
            this.mDtDx = tmpFloats[3] * ratio;
            this.mDtDy = tmpFloats[1] * ratio;
            this.mDsDy = tmpFloats[4] * ratio;
            float x = (tmpFloats[2] * ratio) + pendingX;
            float y = (tmpFloats[5] * ratio) + pendingY;
            this.mWin.mShownPosition.set((int) x, (int) y);
            this.mShownAlpha = this.mAlpha;
            if (this.mService.mLimitedAlphaCompositing && PixelFormat.formatHasAlpha(this.mWin.mAttrs.format)) {
                if (this.mWin.isIdentityMatrix(this.mDsDx, this.mDtDx, this.mDtDy, this.mDsDy)) {
                    if (floatEqualCompare(x, (float) frame.left)) {
                    }
                }
            }
            if (selfTransformation) {
                this.mShownAlpha *= this.mTransformation.getAlpha();
            }
            if (attachedTransformation != null) {
                this.mShownAlpha *= attachedTransformation.getAlpha();
            }
            if (appTransformation != null) {
                this.mShownAlpha *= appTransformation.getAlpha();
                if (appTransformation.hasClipRect() && (ignoreParentClipRect(this.mWin.getAttrs()) ^ 1) != 0) {
                    this.mClipRect.set(appTransformation.getClipRect());
                    this.mHasClipRect = true;
                    if (this.mWin.layoutInParentFrame()) {
                        this.mClipRect.offset(this.mWin.mContainingFrame.left - this.mWin.mFrame.left, this.mWin.mContainingFrame.top - this.mWin.mFrame.top);
                    }
                }
            }
            if (screenAnimation) {
                this.mShownAlpha *= screenRotationAnimation.getEnterTransformation().getAlpha();
            }
        } else if ((!this.mIsWallpaper || !this.mService.mRoot.mWallpaperActionPending) && !this.mWin.isDragResizeChanged()) {
            spec = getMagnificationSpec();
            if (spec != null) {
                frame = this.mWin.mFrame;
                tmpFloats = this.mService.mTmpFloats;
                tmpMatrix = this.mWin.mTmpMatrix;
                tmpMatrix.setScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
                tmpMatrix.postTranslate((float) (frame.left + this.mWin.mXOffset), (float) (frame.top + this.mWin.mYOffset));
                applyMagnificationSpec(spec, tmpMatrix);
                tmpMatrix.getValues(tmpFloats);
                this.mHaveMatrix = true;
                this.mDsDx = tmpFloats[0] * ratio;
                this.mDtDx = tmpFloats[3] * ratio;
                this.mDtDy = tmpFloats[1] * ratio;
                this.mDsDy = tmpFloats[4] * ratio;
                this.mWin.mShownPosition.set((int) ((tmpFloats[2] * ratio) + pendingX), (int) ((tmpFloats[5] * ratio) + pendingY));
                this.mShownAlpha = this.mAlpha;
            } else {
                this.mWin.mShownPosition.set((int) ((((float) this.mWin.mFrame.left) * ratio) + pendingX), (int) ((((float) this.mWin.mFrame.top) * ratio) + pendingY));
                if (!(this.mWin.mXOffset == 0 && this.mWin.mYOffset == 0)) {
                    this.mWin.mShownPosition.offset((int) (((float) this.mWin.mXOffset) * this.mLazyScale), (int) (((float) this.mWin.mYOffset) * this.mLazyScale));
                }
                this.mShownAlpha = this.mAlpha;
                this.mHaveMatrix = false;
                this.mDsDx = this.mWin.mGlobalScale * ratio;
                this.mDtDx = 0.0f;
                this.mDtDy = 0.0f;
                this.mDsDy = this.mWin.mGlobalScale * ratio;
            }
        }
    }

    void computeShownFrameLocked() {
        hwPrepareSurfaceLocked();
    }

    boolean isChildWindowAnimating() {
        boolean isFullScreen = true;
        if (this.mWin.mFrame.left > 0 || this.mWin.mFrame.top > 0) {
            isFullScreen = false;
        }
        WindowState win = this.mWin;
        while (win.isChildWindow()) {
            win = win.getParentWindow();
        }
        WindowStateAnimator winAnimator = win.mWinAnimator;
        if (isFullScreen || !winAnimator.mAnimating) {
            return false;
        }
        return this.mWin.mLayoutAttached;
    }

    void computeShownFrameNormalLocked() {
        computeShownFrameLocked(0);
    }

    public void hwPrepareSurfaceLocked() {
        int openLazyMode = this.mService.getLazyMode();
        int pcScreenDpMode = 0;
        if (HwPCUtils.isPcCastModeInServer() && (this.mIsDefaultDisplay ^ 1) != 0) {
            pcScreenDpMode = this.mService.getPCScreenDisplayMode();
        }
        if (!this.mIsDefaultDisplay) {
            openLazyMode = 0;
        }
        int requestedOrientation = -1;
        if (this.mWin.mAppToken != null) {
            requestedOrientation = this.mWin.mAppToken.mOrientation;
        }
        if (this.mIsLazy) {
            computeShownFrameNormalLocked();
            if (this.mWin.toString().contains("hwSingleMode_windowbg")) {
                this.mAnimLayer = this.mAnimator.offsetLayer + 10000;
            }
            if (this.mWin.toString().contains("hwSingleMode_windowbg_hint")) {
                this.mAnimLayer = 810000;
            }
        } else if (isOrientationLandscape(requestedOrientation)) {
            computeShownFrameNormalLocked();
            this.mPreOpenLazyMode = 0;
        } else if (!HwPCUtils.isPcCastModeInServer() || (this.mIsDefaultDisplay ^ 1) == 0) {
            computeShownFrameLockedByLazyMode(openLazyMode);
        } else {
            computeShownFrameLockedByPCScreenDpMode(pcScreenDpMode);
        }
        if (this.mWin.mAttrs.type == 2000 && this.mPreOpenLazyMode != 0 && openLazyMode != 0 && (this.mLazyIsLeft || this.mLazyIsRight)) {
            this.mDsDy += 2.0f / ((float) this.mWin.mFrame.height());
        }
        if (isMultiWindowInSingleHandMode()) {
            this.mWin.getDisplayContent().mDividerControllerLocked.adjustBoundsForSingleHand();
        }
        traceLogForLazyMode(openLazyMode);
    }

    private boolean floatEqualCompare(float f) {
        return ((double) Math.abs(this.mLazyScale - f)) < 1.0E-6d;
    }

    private boolean floatEqualCompare(float f1, float f2) {
        return ((double) Math.abs(f1 - f2)) < 1.0E-6d;
    }

    private boolean isOrientationLandscape(int requestedOrientation) {
        if (requestedOrientation == 0 || requestedOrientation == 6 || requestedOrientation == 8 || requestedOrientation == 11) {
            return true;
        }
        return false;
    }

    private void computeShownFrameLockedByLazyMode(int openLazyMode) {
        if (openLazyMode == 0 && this.mPreOpenLazyMode == 1) {
            this.mPreOpenLazyMode = 0;
            this.mLazyIsExiting = true;
            this.mLazyIsEntering = false;
            this.mAnimator.offsetLayer = 0;
            this.mLazyScale = 0.8f;
            computeShownFrameLeftLocked();
        } else if (openLazyMode == 0 && this.mPreOpenLazyMode == 2) {
            this.mPreOpenLazyMode = 0;
            this.mLazyIsExiting = true;
            this.mLazyIsEntering = false;
            this.mLazyScale = 0.8f;
            this.mAnimator.offsetLayer = 0;
            computeShownFrameRightLocked();
        } else if (openLazyMode == 0 && this.mLazyIsExiting && this.mPreOpenLazyMode == 0) {
            if (this.mLazyIsLeft) {
                setLeftLazyScale();
            } else if (this.mLazyIsRight) {
                setRightLazyScale();
            } else {
                this.mLazyScale = 1.0f;
                this.mLazyIsExiting = false;
                this.mLazyIsEntering = false;
                this.mLazyIsRight = false;
                this.mLazyIsLeft = false;
                this.mPreOpenLazyMode = 0;
                computeShownFrameNormalLocked();
            }
        } else if (openLazyMode == 1) {
            handleLeftScale();
        } else if (openLazyMode == 2) {
            handleRightScale();
        } else {
            computeShownFrameNormalLocked();
        }
    }

    private void setLeftLazyScale() {
        if (floatEqualCompare(0.8f)) {
            this.mLazyScale = 0.85f;
            computeShownFrameLeftLocked();
        } else if (floatEqualCompare(0.85f)) {
            this.mLazyScale = 0.9f;
            computeShownFrameLeftLocked();
        } else if (floatEqualCompare(0.9f)) {
            this.mLazyScale = 0.95f;
            computeShownFrameLeftLocked();
        } else if (floatEqualCompare(0.95f)) {
            this.mLazyScale = 1.0f;
            this.mLazyIsExiting = false;
            this.mLazyIsLeft = false;
            computeShownFrameNormalLocked();
        }
    }

    private void setRightLazyScale() {
        if (floatEqualCompare(0.8f)) {
            this.mLazyScale = 0.85f;
            computeShownFrameRightLocked();
        } else if (floatEqualCompare(0.85f)) {
            this.mLazyScale = 0.9f;
            computeShownFrameRightLocked();
        } else if (floatEqualCompare(0.9f)) {
            this.mLazyScale = 0.95f;
            computeShownFrameRightLocked();
        } else if (floatEqualCompare(0.95f)) {
            this.mLazyScale = 1.0f;
            this.mLazyIsExiting = false;
            this.mLazyIsRight = false;
            computeShownFrameNormalLocked();
        }
    }

    private void handleLeftScale() {
        if (this.mPreOpenLazyMode == 0) {
            this.mPreOpenLazyMode = 1;
            this.mLazyIsEntering = true;
            this.mLazyIsExiting = false;
            this.mLazyScale = 0.95f;
            computeShownFrameLeftLocked();
        } else if (this.mPreOpenLazyMode == 1 && this.mLazyIsEntering) {
            if (floatEqualCompare(0.95f)) {
                this.mLazyScale = 0.9f;
            } else if (floatEqualCompare(0.9f)) {
                this.mLazyScale = 0.85f;
            } else if (floatEqualCompare(0.85f)) {
                this.mLazyScale = 0.8f;
            } else if (floatEqualCompare(0.8f)) {
                this.mLazyScale = 0.75f;
                this.mLazyIsEntering = false;
                this.mLazyIsLeft = true;
            }
            computeShownFrameLeftLocked();
        } else {
            this.mLazyScale = 0.75f;
            this.mLazyIsLeft = true;
            this.mAnimator.offsetLayer = 800000;
            computeShownFrameLeftLocked();
        }
    }

    private void handleRightScale() {
        if (this.mPreOpenLazyMode == 0) {
            this.mPreOpenLazyMode = 2;
            this.mLazyIsEntering = true;
            this.mLazyIsExiting = false;
            this.mLazyScale = 0.95f;
            computeShownFrameRightLocked();
        } else if (this.mPreOpenLazyMode == 2 && this.mLazyIsEntering) {
            if (floatEqualCompare(0.95f)) {
                this.mLazyScale = 0.9f;
            } else if (floatEqualCompare(0.9f)) {
                this.mLazyScale = 0.85f;
            } else if (floatEqualCompare(0.85f)) {
                this.mLazyScale = 0.8f;
            } else if (floatEqualCompare(0.8f)) {
                this.mLazyScale = 0.75f;
                this.mLazyIsEntering = false;
                this.mLazyIsRight = true;
            }
            computeShownFrameRightLocked();
        } else {
            this.mLazyScale = 0.75f;
            this.mLazyIsRight = true;
            this.mAnimator.offsetLayer = 800000;
            computeShownFrameRightLocked();
        }
    }

    private boolean isMultiWindowInSingleHandMode() {
        if (this.mWin.mAttrs.type == 2034 && this.mLazyIsEntering && floatEqualCompare(0.8f)) {
            return true;
        }
        return this.mLazyIsExiting ? floatEqualCompare(0.95f) : false;
    }

    private void traceLogForLazyMode(int openLazyMode) {
    }

    private void computeShownFrameLockedByPCScreenDpMode(int curMode) {
        float f = 0.95f;
        if (curMode == 0 && (this.mPrePCScreenDisplayMode == 1 || this.mPrePCScreenDisplayMode == 2)) {
            if (!this.mIsPCSreenScaleing) {
                if (this.mPrePCScreenDisplayMode != 1) {
                    f = 0.9f;
                }
                this.mPCScreenScale = f;
                this.mIsPCSreenScaleing = true;
            }
            handlePCWindowNormalScale();
        } else if ((curMode == 2 || curMode == 1) && this.mPrePCScreenDisplayMode == 0) {
            if (!this.mIsPCSreenScaleing) {
                this.mPCScreenScale = 1.0f;
                this.mIsPCSreenScaleing = true;
            }
            handlePCWindowLessenScale(curMode);
        } else if (curMode == 1 && this.mPrePCScreenDisplayMode == 2) {
            if (!this.mIsPCSreenScaleing) {
                this.mPCScreenScale = 0.9f;
                this.mIsPCSreenScaleing = true;
            }
            handlePCWindowSmall2MinorScale();
        } else if (curMode == 2 && this.mPrePCScreenDisplayMode == 1) {
            if (!this.mIsPCSreenScaleing) {
                this.mPCScreenScale = 0.95f;
                this.mIsPCSreenScaleing = true;
            }
            handlePCWindowLessenScale(curMode);
        } else if ((curMode == 1 || curMode == 2) && this.mPrePCScreenDisplayMode == curMode) {
            if (curMode != 1) {
                f = 0.9f;
            }
            this.mPCScreenScale = f;
            handlePCWindowLessenScale(curMode);
        } else if (curMode == 0 && this.mPrePCScreenDisplayMode == 0) {
            this.mPCScreenScale = 1.0f;
            handlePCWindowNormalScale();
        }
    }

    private void handlePCWindowNormalScale() {
        if (this.mPCScreenScale >= 1.0f || floatEqualCompare(this.mPCScreenScale, 1.0f)) {
            this.mIsPCSreenScaleing = false;
            this.mPrePCScreenDisplayMode = 0;
            computeShownPCFrameLocked(0);
            return;
        }
        this.mPCScreenScale = increaseScale(this.mPCScreenScale);
        computeShownPCFrameLocked(1);
    }

    private void handlePCWindowLessenScale(int mode) {
        float scle = mode == 1 ? 0.95f : 0.9f;
        if (this.mPCScreenScale <= scle || floatEqualCompare(this.mPCScreenScale, scle)) {
            this.mIsPCSreenScaleing = false;
            this.mPrePCScreenDisplayMode = mode;
            computeShownPCFrameLocked(mode);
            return;
        }
        this.mPCScreenScale = lessenScale(this.mPCScreenScale);
        computeShownPCFrameLocked(mode);
    }

    private void handlePCWindowSmall2MinorScale() {
        if (this.mPCScreenScale <= 0.95f || floatEqualCompare(this.mPCScreenScale, 0.95f)) {
            this.mPrePCScreenDisplayMode = 1;
            computeShownPCFrameLocked(1);
            return;
        }
        this.mPCScreenScale = increaseScale(this.mPCScreenScale);
        computeShownPCFrameLocked(1);
    }

    private float lessenScale(float scale) {
        return scale - 0.05f;
    }

    private float increaseScale(float scale) {
        return 0.05f + scale;
    }

    /* JADX WARNING: Missing block: B:54:0x02ad, code:
            if (floatEqualCompare(r21, (float) r8.top) != false) goto L_0x02af;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void computeShownPCFrameLocked(int mode) {
        boolean selfTransformation = this.mHasLocalTransformation;
        Transformation attachedTransformation = (this.mParentWinAnimator == null || !this.mParentWinAnimator.mHasLocalTransformation) ? null : this.mParentWinAnimator.mTransformation;
        Transformation appTransformation = (this.mAppAnimator == null || !this.mAppAnimator.hasTransformation) ? null : this.mAppAnimator.transformation;
        if (mode != 0) {
            updatePcDisplayinfo();
        }
        ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(this.mWin.getDisplayId());
        boolean screenAnimation = screenRotationAnimation != null ? screenRotationAnimation.isAnimating() : false;
        float ratio = 1.0f;
        float pendingX = 0.0f;
        float pendingY = 0.0f;
        if (mode != 0) {
            ratio = this.mPCScreenScale;
            pendingX = (((float) this.mWidth) * (1.0f - this.mPCScreenScale)) / 2.0f;
            pendingY = (((float) this.mHeight) * (1.0f - this.mPCScreenScale)) / 2.0f;
        }
        this.mHasClipRect = false;
        Rect frame;
        float[] tmpFloats;
        Matrix tmpMatrix;
        MagnificationSpec spec;
        if (selfTransformation || attachedTransformation != null || appTransformation != null || screenAnimation) {
            frame = this.mWin.mFrame;
            tmpFloats = this.mService.mTmpFloats;
            tmpMatrix = this.mWin.mTmpMatrix;
            if ((screenAnimation && screenRotationAnimation.isRotating()) || isChildWindowAnimating()) {
                float w = (float) frame.width();
                float h = (float) frame.height();
                if (w < 1.0f || h < 1.0f) {
                    tmpMatrix.reset();
                } else {
                    tmpMatrix.setScale((4.0f / w) + 1.0f, (4.0f / h) + 1.0f, w / 2.0f, h / 2.0f);
                }
            } else {
                tmpMatrix.reset();
            }
            tmpMatrix.postScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
            if (selfTransformation) {
                tmpMatrix.postConcat(this.mTransformation.getMatrix());
            }
            tmpMatrix.postTranslate((float) (frame.left + this.mWin.mXOffset), (float) (frame.top + this.mWin.mYOffset));
            if (attachedTransformation != null) {
                tmpMatrix.postConcat(attachedTransformation.getMatrix());
            }
            if (appTransformation != null) {
                tmpMatrix.postConcat(appTransformation.getMatrix());
            }
            if (screenAnimation) {
                tmpMatrix.postConcat(screenRotationAnimation.getEnterTransformation().getMatrix());
            }
            spec = getMagnificationSpec();
            if (spec != null) {
                applyMagnificationSpec(spec, tmpMatrix);
            }
            this.mHaveMatrix = true;
            tmpMatrix.getValues(tmpFloats);
            this.mDsDx = tmpFloats[0] * ratio;
            this.mDtDx = tmpFloats[3] * ratio;
            this.mDtDy = tmpFloats[1] * ratio;
            this.mDsDy = tmpFloats[4] * ratio;
            float x = (tmpFloats[2] * ratio) + pendingX;
            float y = (tmpFloats[5] * ratio) + pendingY;
            this.mWin.mShownPosition.set((int) x, (int) y);
            this.mShownAlpha = this.mAlpha;
            if (this.mService.mLimitedAlphaCompositing && PixelFormat.formatHasAlpha(this.mWin.mAttrs.format)) {
                if (this.mWin.isIdentityMatrix(this.mDsDx, this.mDtDx, this.mDtDy, this.mDsDy)) {
                    if (floatEqualCompare(x, (float) frame.left)) {
                    }
                }
            }
            if (selfTransformation) {
                this.mShownAlpha *= this.mTransformation.getAlpha();
            }
            if (attachedTransformation != null) {
                this.mShownAlpha *= attachedTransformation.getAlpha();
            }
            if (appTransformation != null) {
                this.mShownAlpha *= appTransformation.getAlpha();
                if (appTransformation.hasClipRect() && (ignoreParentClipRect(this.mWin.getAttrs()) ^ 1) != 0) {
                    this.mClipRect.set(appTransformation.getClipRect());
                    this.mHasClipRect = true;
                    if (this.mWin.layoutInParentFrame()) {
                        this.mClipRect.offset(this.mWin.mContainingFrame.left - this.mWin.mFrame.left, this.mWin.mContainingFrame.top - this.mWin.mFrame.top);
                    }
                }
            }
            if (screenAnimation) {
                this.mShownAlpha *= screenRotationAnimation.getEnterTransformation().getAlpha();
            }
        } else if ((!this.mIsWallpaper || !this.mService.mRoot.mWallpaperActionPending) && !this.mWin.isDragResizeChanged()) {
            spec = getMagnificationSpec();
            if (spec != null) {
                frame = this.mWin.mFrame;
                tmpFloats = this.mService.mTmpFloats;
                tmpMatrix = this.mWin.mTmpMatrix;
                tmpMatrix.setScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
                tmpMatrix.postTranslate((float) (frame.left + this.mWin.mXOffset), (float) (frame.top + this.mWin.mYOffset));
                applyMagnificationSpec(spec, tmpMatrix);
                tmpMatrix.getValues(tmpFloats);
                this.mHaveMatrix = true;
                this.mDsDx = tmpFloats[0] * ratio;
                this.mDtDx = tmpFloats[3] * ratio;
                this.mDtDy = tmpFloats[1] * ratio;
                this.mDsDy = tmpFloats[4] * ratio;
                this.mWin.mShownPosition.set((int) ((tmpFloats[2] * ratio) + pendingX), (int) ((tmpFloats[5] * ratio) + pendingY));
                this.mShownAlpha = this.mAlpha;
            } else {
                this.mWin.mShownPosition.set((int) ((((float) this.mWin.mFrame.left) * ratio) + pendingX), (int) ((((float) this.mWin.mFrame.top) * ratio) + pendingY));
                if (!(this.mWin.mXOffset == 0 && this.mWin.mYOffset == 0)) {
                    this.mWin.mShownPosition.offset((int) (((float) this.mWin.mXOffset) * this.mPCScreenScale), (int) (((float) this.mWin.mYOffset) * this.mPCScreenScale));
                }
                this.mShownAlpha = this.mAlpha;
                this.mHaveMatrix = false;
                this.mDsDx = this.mWin.mGlobalScale * ratio;
                this.mDtDx = 0.0f;
                this.mDtDy = 0.0f;
                this.mDsDy = this.mWin.mGlobalScale * ratio;
            }
        }
    }

    private void updatePcDisplayinfo() {
        this.mDefaultDisplayInfo = this.mWin.getDisplayInfo();
        this.mWidth = this.mDefaultDisplayInfo.logicalWidth;
        this.mHeight = this.mDefaultDisplayInfo.logicalHeight;
    }

    public float[] getPCDisplayModeSurfacePos(Rect tmpSize) {
        WindowState w = this.mWin;
        float posLeft = (float) tmpSize.left;
        float posTop = (float) tmpSize.top;
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(w.getDisplayId())) {
            int mode = this.mService.getPCScreenDisplayMode();
            if (mode != 0) {
                float pcDisplayScale = mode == 1 ? 0.95f : 0.9f;
                Rect surfaceInsets = this.mWin.mAttrs.surfaceInsets;
                HwPCUtils.log(TAG, "getPCDisplayModeSurfacePos name:" + w.mLastTitle + " mDsDx:" + this.mDsDx + " mDsDy:" + this.mDsDy + " tmpSize:" + tmpSize + " tmpSize width:" + tmpSize.width() + " height:" + tmpSize.height() + " surfaceInsets:" + surfaceInsets);
                Point outPoint = new Point();
                this.mService.getBaseDisplaySize(w.getDisplayId(), outPoint);
                float scale = (1.0f - pcDisplayScale) / 2.0f;
                if (this.mDsDx == 1.0f && this.mDsDy == 1.0f) {
                    posLeft = (((float) outPoint.x) * scale) + (posLeft * pcDisplayScale);
                    posTop = (((float) outPoint.y) * scale) + (posTop * pcDisplayScale);
                    this.mDsDx = pcDisplayScale;
                    this.mDsDy = pcDisplayScale;
                } else if (surfaceInsets.left > 0 && surfaceInsets.top > 0) {
                    float tmpScale = 1.0f - pcDisplayScale;
                    posLeft += ((float) surfaceInsets.left) * tmpScale;
                    posTop += ((float) surfaceInsets.top) * tmpScale;
                }
                HwPCUtils.log(TAG, "getPCDisplayModeSurfacePos name:" + w.mLastTitle + " posLeft:" + posLeft + " posTop:" + posTop);
            }
        }
        return new float[]{posLeft, posTop};
    }

    private boolean ignoreParentClipRect(LayoutParams lp) {
        return (lp.privateFlags & HighBitsCompModeID.MODE_EYE_PROTECT) != 0;
    }

    WindowSurfaceController createSurfaceLocked(int windowType, int ownerUid) {
        WindowSurfaceController surfaceController = super.createSurfaceLocked(windowType, ownerUid);
        sendMessageToDESceneHandler(1);
        return surfaceController;
    }

    void destroySurfaceLocked() {
        sendMessageToDESceneHandler(-1);
        super.destroySurfaceLocked();
    }

    private void sendMessageToDESceneHandler(int pos) {
        WindowState ws = this.mWin;
        LayoutParams attrs = ws.mAttrs;
        WindowManagerService service = ws.mService;
        String SurName = attrs.getTitle().toString();
        DisplayContent displayContent = service.getDefaultDisplayContentLocked();
        int initScreenWidth = displayContent.mInitialDisplayWidth;
        int initScreenHeight = displayContent.mInitialDisplayHeight;
        Bundle data = new Bundle();
        data.putInt("Position", pos);
        data.putString("SurfaceName", SurName);
        data.putInt("FrameLeft", ws.mFrame.left);
        data.putInt("FrameRight", ws.mFrame.right);
        data.putInt("FrameTop", ws.mFrame.top);
        data.putInt("FrameBottom", ws.mFrame.bottom);
        data.putInt("SourceWidth", ws.mRequestedWidth);
        data.putInt("SourceHeight", ws.mRequestedHeight);
        data.putInt("DisplayWidth", initScreenWidth);
        data.putInt("DisplayHeight", initScreenHeight);
        data.putInt("Layer", ws.mLayer);
        data.putInt("AnimLayer", this.mAnimLayer);
        data.putInt("BaseLayer", ws.mBaseLayer);
        data.putInt("SubLayer", ws.mSubLayer);
        data.putInt("LastLayer", this.mLastLayer);
        data.putInt("SurfaceFormat", this.mSurfaceFormat);
        data.putString("AttachWinName", null);
        this.mDisplayEngineManager.sendMessage(2, data);
    }
}
