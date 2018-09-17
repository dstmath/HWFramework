package com.android.server.wm;

import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.Flog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.MagnificationSpec;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Transformation;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.HwServiceFactory;
import com.android.server.input.HwCircleAnimation;
import com.android.server.policy.SlideTouchEvent;
import com.android.server.security.trustcircle.lifecycle.LifeCycleStateMachine;
import huawei.com.android.server.policy.HwGlobalActionsView;

public class HwWindowStateAnimator extends WindowStateAnimator {
    private static final String TAG = "HwWindowStateAnimator";
    private static final int TYPE_LEFT = 1;
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_RIGHT = 2;
    private Display mDefaultDisplay;
    private DisplayInfo mDefaultDisplayInfo;
    int mHeight;
    final boolean mIsLazy;
    boolean mLazyIsLeft;
    boolean mLazyIsRight;
    float mLazyScale;
    int mPreOpenLazyMode;
    int mWidth;
    private final WindowManager mWindowManager;

    public HwWindowStateAnimator(WindowState win) {
        super(win);
        this.mDefaultDisplayInfo = new DisplayInfo();
        this.mIsLazy = win.toString().contains("hwSingleMode_window");
        this.mPreOpenLazyMode = this.mService.getLazyMode();
        this.mLazyScale = HwCircleAnimation.SMALL_ALPHA;
        this.mLazyIsExiting = false;
        this.mLazyIsEntering = false;
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        updatedisplayinfo();
    }

    public void updatedisplayinfo() {
        int i;
        this.mDefaultDisplay = this.mWindowManager.getDefaultDisplay();
        this.mDefaultDisplay.getDisplayInfo(this.mDefaultDisplayInfo);
        boolean isPortrait = this.mDefaultDisplayInfo.logicalHeight > this.mDefaultDisplayInfo.logicalWidth;
        this.mWidth = isPortrait ? this.mDefaultDisplayInfo.logicalWidth : this.mDefaultDisplayInfo.logicalHeight;
        if (isPortrait) {
            i = this.mDefaultDisplayInfo.logicalHeight;
        } else {
            i = this.mDefaultDisplayInfo.logicalWidth;
        }
        this.mHeight = i;
    }

    public int adjustAnimLayerIfCoverclosed(int type, int animLayer) {
        if (type != HwNetworkPropertyChecker.HW_DEFAULT_REEVALUATE_DELAY_MS || animLayer >= 400000 || HwServiceFactory.getCoverManagerService().isCoverOpen()) {
            return animLayer;
        }
        return 400000;
    }

    void computeShownFrameRightLocked() {
        computeShownFrameLocked(TYPE_RIGHT);
    }

    void computeShownFrameLeftLocked() {
        computeShownFrameLocked(TYPE_LEFT);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void computeShownFrameLocked(int type) {
        Transformation transformation;
        Transformation transformation2;
        WindowState wallpaperTarget;
        WindowStateAnimator wallpaperAnimator;
        AppWindowAnimator wpAppAnimator;
        int displayId;
        ScreenRotationAnimation screenRotationAnimation;
        boolean isAnimating;
        float ratio;
        float pendingX;
        float pendingY;
        MagnificationSpec spec;
        Rect frame;
        float[] tmpFloats;
        Matrix tmpMatrix;
        float x;
        float y;
        boolean selfTransformation = this.mHasLocalTransformation;
        if (this.mAttachedWinAnimator != null) {
            if (this.mAttachedWinAnimator.mHasLocalTransformation) {
                transformation = this.mAttachedWinAnimator.mTransformation;
                if (this.mAppAnimator != null) {
                    if (this.mAppAnimator.hasTransformation) {
                        transformation2 = this.mAppAnimator.transformation;
                        if (type == TYPE_LEFT || type == TYPE_RIGHT) {
                            updatedisplayinfo();
                        }
                        wallpaperTarget = this.mWallpaperControllerLocked.getWallpaperTarget();
                        if (this.mIsWallpaper && wallpaperTarget != null) {
                            if (this.mService.mAnimateWallpaperWithTarget) {
                                wallpaperAnimator = wallpaperTarget.mWinAnimator;
                                if (wallpaperAnimator.mHasLocalTransformation && wallpaperAnimator.mAnimation != null) {
                                    if (!wallpaperAnimator.mAnimation.getDetachWallpaper()) {
                                        transformation = wallpaperAnimator.mTransformation;
                                    }
                                }
                                wpAppAnimator = wallpaperTarget.mAppToken != null ? null : wallpaperTarget.mAppToken.mAppAnimator;
                                if (!(wpAppAnimator == null || !wpAppAnimator.hasTransformation || wpAppAnimator.animation == null)) {
                                    if (!wpAppAnimator.animation.getDetachWallpaper()) {
                                        transformation2 = wpAppAnimator.transformation;
                                    }
                                }
                            }
                        }
                        displayId = this.mWin.getDisplayId();
                        screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
                        isAnimating = screenRotationAnimation == null ? screenRotationAnimation.isAnimating() : false;
                        ratio = HwCircleAnimation.SMALL_ALPHA;
                        pendingX = 0.0f;
                        pendingY = 0.0f;
                        if (type != TYPE_LEFT) {
                            ratio = this.mLazyScale;
                            pendingY = ((float) this.mHeight) * (HwCircleAnimation.SMALL_ALPHA - this.mLazyScale);
                        } else if (type == TYPE_RIGHT) {
                            ratio = this.mLazyScale;
                            pendingX = ((float) this.mWidth) * (HwCircleAnimation.SMALL_ALPHA - this.mLazyScale);
                            pendingY = ((float) this.mHeight) * (HwCircleAnimation.SMALL_ALPHA - this.mLazyScale);
                        }
                        this.mHasClipRect = false;
                        if (!selfTransformation && transformation == null && transformation2 == null && !isAnimating) {
                            if (this.mHScale == this.mWin.mHScale) {
                                if (this.mVScale == this.mWin.mVScale) {
                                    if (this.mIsWallpaper) {
                                        if (this.mService.mWindowPlacerLocked.mWallpaperActionPending) {
                                            return;
                                        }
                                    }
                                    if (!this.mWin.isDragResizeChanged()) {
                                        spec = null;
                                        if (this.mService.mAccessibilityController != null && displayId == 0) {
                                            spec = this.mService.mAccessibilityController.getMagnificationSpecForWindowLocked(this.mWin);
                                        }
                                        if (spec == null) {
                                            frame = this.mWin.mFrame;
                                            tmpFloats = this.mService.mTmpFloats;
                                            tmpMatrix = this.mWin.mTmpMatrix;
                                            tmpMatrix.setScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
                                            tmpMatrix.postTranslate((float) (frame.left + this.mWin.mXOffset), (float) (frame.top + this.mWin.mYOffset));
                                            if (!spec.isNop()) {
                                                tmpMatrix.postScale(spec.scale, spec.scale);
                                                tmpMatrix.postTranslate(spec.offsetX, spec.offsetY);
                                            }
                                            tmpMatrix.getValues(tmpFloats);
                                            this.mHaveMatrix = true;
                                            this.mDsDx = tmpFloats[TYPE_NORMAL] * ratio;
                                            this.mDtDx = tmpFloats[3] * ratio;
                                            this.mDsDy = tmpFloats[TYPE_LEFT] * ratio;
                                            this.mDtDy = tmpFloats[4] * ratio;
                                            this.mWin.mShownPosition.set((int) ((tmpFloats[TYPE_RIGHT] * ratio) + pendingX), (int) ((tmpFloats[5] * ratio) + pendingY));
                                            this.mShownAlpha = this.mAlpha;
                                        } else {
                                            this.mWin.mShownPosition.set((int) ((((float) this.mWin.mFrame.left) * ratio) + pendingX), (int) ((((float) this.mWin.mFrame.top) * ratio) + pendingY));
                                            if (this.mWin.mXOffset == 0) {
                                            }
                                            this.mWin.mShownPosition.offset((int) (((float) this.mWin.mXOffset) * this.mLazyScale), (int) (((float) this.mWin.mYOffset) * this.mLazyScale));
                                            this.mShownAlpha = this.mAlpha;
                                            this.mHaveMatrix = false;
                                            this.mDsDx = this.mWin.mGlobalScale * ratio;
                                            this.mDtDx = 0.0f;
                                            this.mDsDy = 0.0f;
                                            this.mDtDy = this.mWin.mGlobalScale * ratio;
                                        }
                                        return;
                                    }
                                    return;
                                }
                            }
                        }
                        frame = this.mWin.mFrame;
                        tmpFloats = this.mService.mTmpFloats;
                        tmpMatrix = this.mWin.mTmpMatrix;
                        if ((isAnimating || !screenRotationAnimation.isRotating()) && !isChildWindowAnimating()) {
                            tmpMatrix.reset();
                        } else {
                            float w = (float) frame.width();
                            float h = (float) frame.height();
                            if (w < HwCircleAnimation.SMALL_ALPHA || h < HwCircleAnimation.SMALL_ALPHA) {
                                tmpMatrix.reset();
                            } else {
                                tmpMatrix.setScale((4.0f / w) + HwCircleAnimation.SMALL_ALPHA, (4.0f / h) + HwCircleAnimation.SMALL_ALPHA, w / 2.0f, h / 2.0f);
                            }
                        }
                        tmpMatrix.postScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
                        if (selfTransformation) {
                            tmpMatrix.postConcat(this.mTransformation.getMatrix());
                        }
                        tmpMatrix.postTranslate((float) (frame.left + this.mWin.mXOffset), (float) (frame.top + this.mWin.mYOffset));
                        if (transformation != null) {
                            tmpMatrix.postConcat(transformation.getMatrix());
                        }
                        if (transformation2 != null) {
                            tmpMatrix.postConcat(transformation2.getMatrix());
                        }
                        if (isAnimating) {
                            tmpMatrix.postConcat(screenRotationAnimation.getEnterTransformation().getMatrix());
                        }
                        if (this.mService.mAccessibilityController != null && displayId == 0) {
                            spec = this.mService.mAccessibilityController.getMagnificationSpecForWindowLocked(this.mWin);
                            if (!(spec == null || spec.isNop())) {
                                tmpMatrix.postScale(spec.scale, spec.scale);
                                tmpMatrix.postTranslate(spec.offsetX, spec.offsetY);
                            }
                        }
                        this.mHaveMatrix = true;
                        tmpMatrix.getValues(tmpFloats);
                        this.mDsDx = tmpFloats[TYPE_NORMAL] * ratio;
                        this.mDtDx = tmpFloats[3] * ratio;
                        this.mDsDy = tmpFloats[TYPE_LEFT] * ratio;
                        this.mDtDy = tmpFloats[4] * ratio;
                        x = (tmpFloats[TYPE_RIGHT] * ratio) + pendingX;
                        y = (tmpFloats[5] * ratio) + pendingY;
                        this.mWin.mShownPosition.set((int) x, (int) y);
                        this.mShownAlpha = this.mAlpha;
                        if (this.mService.mLimitedAlphaCompositing) {
                            if (PixelFormat.formatHasAlpha(this.mWin.mAttrs.format)) {
                                if (this.mWin.isIdentityMatrix(this.mDsDx, this.mDtDx, this.mDsDy, this.mDtDy)) {
                                    if (floatEqualCompare(x, (float) frame.left)) {
                                    }
                                }
                            }
                        }
                        if (selfTransformation) {
                            this.mShownAlpha *= this.mTransformation.getAlpha();
                        }
                        if (transformation != null) {
                            this.mShownAlpha *= transformation.getAlpha();
                        }
                        if (transformation2 != null) {
                            this.mShownAlpha *= transformation2.getAlpha();
                            if (transformation2.hasClipRect()) {
                                if (!ignoreParentClipRect(this.mWin.getAttrs())) {
                                    this.mClipRect.set(transformation2.getClipRect());
                                    this.mHScale = this.mWin.mHScale;
                                    this.mVScale = this.mWin.mVScale;
                                    this.mHasClipRect = true;
                                    if (this.mWin.layoutInParentFrame()) {
                                        this.mClipRect.offset(this.mWin.mContainingFrame.left - this.mWin.mFrame.left, this.mWin.mContainingFrame.top - this.mWin.mFrame.top);
                                    }
                                }
                            }
                        }
                        if (isAnimating) {
                            this.mShownAlpha *= screenRotationAnimation.getEnterTransformation().getAlpha();
                        }
                    }
                }
                transformation2 = null;
                updatedisplayinfo();
                wallpaperTarget = this.mWallpaperControllerLocked.getWallpaperTarget();
                if (this.mService.mAnimateWallpaperWithTarget) {
                    wallpaperAnimator = wallpaperTarget.mWinAnimator;
                    if (wallpaperAnimator.mAnimation.getDetachWallpaper()) {
                        transformation = wallpaperAnimator.mTransformation;
                    }
                    if (wallpaperTarget.mAppToken != null) {
                    }
                    if (wpAppAnimator.animation.getDetachWallpaper()) {
                        transformation2 = wpAppAnimator.transformation;
                    }
                }
                displayId = this.mWin.getDisplayId();
                screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
                if (screenRotationAnimation == null) {
                }
                ratio = HwCircleAnimation.SMALL_ALPHA;
                pendingX = 0.0f;
                pendingY = 0.0f;
                if (type != TYPE_LEFT) {
                    ratio = this.mLazyScale;
                    pendingY = ((float) this.mHeight) * (HwCircleAnimation.SMALL_ALPHA - this.mLazyScale);
                } else if (type == TYPE_RIGHT) {
                    ratio = this.mLazyScale;
                    pendingX = ((float) this.mWidth) * (HwCircleAnimation.SMALL_ALPHA - this.mLazyScale);
                    pendingY = ((float) this.mHeight) * (HwCircleAnimation.SMALL_ALPHA - this.mLazyScale);
                }
                this.mHasClipRect = false;
                if (this.mHScale == this.mWin.mHScale) {
                    if (this.mVScale == this.mWin.mVScale) {
                        if (this.mIsWallpaper) {
                            if (this.mService.mWindowPlacerLocked.mWallpaperActionPending) {
                                return;
                            }
                        }
                        if (!this.mWin.isDragResizeChanged()) {
                            spec = null;
                            spec = this.mService.mAccessibilityController.getMagnificationSpecForWindowLocked(this.mWin);
                            if (spec == null) {
                                this.mWin.mShownPosition.set((int) ((((float) this.mWin.mFrame.left) * ratio) + pendingX), (int) ((((float) this.mWin.mFrame.top) * ratio) + pendingY));
                                if (this.mWin.mXOffset == 0) {
                                }
                                this.mWin.mShownPosition.offset((int) (((float) this.mWin.mXOffset) * this.mLazyScale), (int) (((float) this.mWin.mYOffset) * this.mLazyScale));
                                this.mShownAlpha = this.mAlpha;
                                this.mHaveMatrix = false;
                                this.mDsDx = this.mWin.mGlobalScale * ratio;
                                this.mDtDx = 0.0f;
                                this.mDsDy = 0.0f;
                                this.mDtDy = this.mWin.mGlobalScale * ratio;
                            } else {
                                frame = this.mWin.mFrame;
                                tmpFloats = this.mService.mTmpFloats;
                                tmpMatrix = this.mWin.mTmpMatrix;
                                tmpMatrix.setScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
                                tmpMatrix.postTranslate((float) (frame.left + this.mWin.mXOffset), (float) (frame.top + this.mWin.mYOffset));
                                if (spec.isNop()) {
                                    tmpMatrix.postScale(spec.scale, spec.scale);
                                    tmpMatrix.postTranslate(spec.offsetX, spec.offsetY);
                                }
                                tmpMatrix.getValues(tmpFloats);
                                this.mHaveMatrix = true;
                                this.mDsDx = tmpFloats[TYPE_NORMAL] * ratio;
                                this.mDtDx = tmpFloats[3] * ratio;
                                this.mDsDy = tmpFloats[TYPE_LEFT] * ratio;
                                this.mDtDy = tmpFloats[4] * ratio;
                                this.mWin.mShownPosition.set((int) ((tmpFloats[TYPE_RIGHT] * ratio) + pendingX), (int) ((tmpFloats[5] * ratio) + pendingY));
                                this.mShownAlpha = this.mAlpha;
                            }
                            return;
                        }
                        return;
                    }
                }
                frame = this.mWin.mFrame;
                tmpFloats = this.mService.mTmpFloats;
                tmpMatrix = this.mWin.mTmpMatrix;
                if (isAnimating) {
                }
                tmpMatrix.reset();
                tmpMatrix.postScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
                if (selfTransformation) {
                    tmpMatrix.postConcat(this.mTransformation.getMatrix());
                }
                tmpMatrix.postTranslate((float) (frame.left + this.mWin.mXOffset), (float) (frame.top + this.mWin.mYOffset));
                if (transformation != null) {
                    tmpMatrix.postConcat(transformation.getMatrix());
                }
                if (transformation2 != null) {
                    tmpMatrix.postConcat(transformation2.getMatrix());
                }
                if (isAnimating) {
                    tmpMatrix.postConcat(screenRotationAnimation.getEnterTransformation().getMatrix());
                }
                spec = this.mService.mAccessibilityController.getMagnificationSpecForWindowLocked(this.mWin);
                tmpMatrix.postScale(spec.scale, spec.scale);
                tmpMatrix.postTranslate(spec.offsetX, spec.offsetY);
                this.mHaveMatrix = true;
                tmpMatrix.getValues(tmpFloats);
                this.mDsDx = tmpFloats[TYPE_NORMAL] * ratio;
                this.mDtDx = tmpFloats[3] * ratio;
                this.mDsDy = tmpFloats[TYPE_LEFT] * ratio;
                this.mDtDy = tmpFloats[4] * ratio;
                x = (tmpFloats[TYPE_RIGHT] * ratio) + pendingX;
                y = (tmpFloats[5] * ratio) + pendingY;
                this.mWin.mShownPosition.set((int) x, (int) y);
                this.mShownAlpha = this.mAlpha;
                if (this.mService.mLimitedAlphaCompositing) {
                    if (PixelFormat.formatHasAlpha(this.mWin.mAttrs.format)) {
                        if (this.mWin.isIdentityMatrix(this.mDsDx, this.mDtDx, this.mDsDy, this.mDtDy)) {
                            if (floatEqualCompare(x, (float) frame.left)) {
                            }
                        }
                    }
                }
                if (selfTransformation) {
                    this.mShownAlpha *= this.mTransformation.getAlpha();
                }
                if (transformation != null) {
                    this.mShownAlpha *= transformation.getAlpha();
                }
                if (transformation2 != null) {
                    this.mShownAlpha *= transformation2.getAlpha();
                    if (transformation2.hasClipRect()) {
                        if (ignoreParentClipRect(this.mWin.getAttrs())) {
                            this.mClipRect.set(transformation2.getClipRect());
                            this.mHScale = this.mWin.mHScale;
                            this.mVScale = this.mWin.mVScale;
                            this.mHasClipRect = true;
                            if (this.mWin.layoutInParentFrame()) {
                                this.mClipRect.offset(this.mWin.mContainingFrame.left - this.mWin.mFrame.left, this.mWin.mContainingFrame.top - this.mWin.mFrame.top);
                            }
                        }
                    }
                }
                if (isAnimating) {
                    this.mShownAlpha *= screenRotationAnimation.getEnterTransformation().getAlpha();
                }
            }
        }
        transformation = null;
        if (this.mAppAnimator != null) {
            if (this.mAppAnimator.hasTransformation) {
                transformation2 = this.mAppAnimator.transformation;
                updatedisplayinfo();
                wallpaperTarget = this.mWallpaperControllerLocked.getWallpaperTarget();
                if (this.mService.mAnimateWallpaperWithTarget) {
                    wallpaperAnimator = wallpaperTarget.mWinAnimator;
                    if (wallpaperAnimator.mAnimation.getDetachWallpaper()) {
                        transformation = wallpaperAnimator.mTransformation;
                    }
                    if (wallpaperTarget.mAppToken != null) {
                    }
                    if (wpAppAnimator.animation.getDetachWallpaper()) {
                        transformation2 = wpAppAnimator.transformation;
                    }
                }
                displayId = this.mWin.getDisplayId();
                screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
                if (screenRotationAnimation == null) {
                }
                ratio = HwCircleAnimation.SMALL_ALPHA;
                pendingX = 0.0f;
                pendingY = 0.0f;
                if (type != TYPE_LEFT) {
                    ratio = this.mLazyScale;
                    pendingY = ((float) this.mHeight) * (HwCircleAnimation.SMALL_ALPHA - this.mLazyScale);
                } else if (type == TYPE_RIGHT) {
                    ratio = this.mLazyScale;
                    pendingX = ((float) this.mWidth) * (HwCircleAnimation.SMALL_ALPHA - this.mLazyScale);
                    pendingY = ((float) this.mHeight) * (HwCircleAnimation.SMALL_ALPHA - this.mLazyScale);
                }
                this.mHasClipRect = false;
                if (this.mHScale == this.mWin.mHScale) {
                    if (this.mVScale == this.mWin.mVScale) {
                        if (this.mIsWallpaper) {
                            if (this.mService.mWindowPlacerLocked.mWallpaperActionPending) {
                                return;
                            }
                        }
                        if (!this.mWin.isDragResizeChanged()) {
                            spec = null;
                            spec = this.mService.mAccessibilityController.getMagnificationSpecForWindowLocked(this.mWin);
                            if (spec == null) {
                                frame = this.mWin.mFrame;
                                tmpFloats = this.mService.mTmpFloats;
                                tmpMatrix = this.mWin.mTmpMatrix;
                                tmpMatrix.setScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
                                tmpMatrix.postTranslate((float) (frame.left + this.mWin.mXOffset), (float) (frame.top + this.mWin.mYOffset));
                                if (spec.isNop()) {
                                    tmpMatrix.postScale(spec.scale, spec.scale);
                                    tmpMatrix.postTranslate(spec.offsetX, spec.offsetY);
                                }
                                tmpMatrix.getValues(tmpFloats);
                                this.mHaveMatrix = true;
                                this.mDsDx = tmpFloats[TYPE_NORMAL] * ratio;
                                this.mDtDx = tmpFloats[3] * ratio;
                                this.mDsDy = tmpFloats[TYPE_LEFT] * ratio;
                                this.mDtDy = tmpFloats[4] * ratio;
                                this.mWin.mShownPosition.set((int) ((tmpFloats[TYPE_RIGHT] * ratio) + pendingX), (int) ((tmpFloats[5] * ratio) + pendingY));
                                this.mShownAlpha = this.mAlpha;
                            } else {
                                this.mWin.mShownPosition.set((int) ((((float) this.mWin.mFrame.left) * ratio) + pendingX), (int) ((((float) this.mWin.mFrame.top) * ratio) + pendingY));
                                if (this.mWin.mXOffset == 0) {
                                }
                                this.mWin.mShownPosition.offset((int) (((float) this.mWin.mXOffset) * this.mLazyScale), (int) (((float) this.mWin.mYOffset) * this.mLazyScale));
                                this.mShownAlpha = this.mAlpha;
                                this.mHaveMatrix = false;
                                this.mDsDx = this.mWin.mGlobalScale * ratio;
                                this.mDtDx = 0.0f;
                                this.mDsDy = 0.0f;
                                this.mDtDy = this.mWin.mGlobalScale * ratio;
                            }
                            return;
                        }
                        return;
                    }
                }
                frame = this.mWin.mFrame;
                tmpFloats = this.mService.mTmpFloats;
                tmpMatrix = this.mWin.mTmpMatrix;
                if (isAnimating) {
                }
                tmpMatrix.reset();
                tmpMatrix.postScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
                if (selfTransformation) {
                    tmpMatrix.postConcat(this.mTransformation.getMatrix());
                }
                tmpMatrix.postTranslate((float) (frame.left + this.mWin.mXOffset), (float) (frame.top + this.mWin.mYOffset));
                if (transformation != null) {
                    tmpMatrix.postConcat(transformation.getMatrix());
                }
                if (transformation2 != null) {
                    tmpMatrix.postConcat(transformation2.getMatrix());
                }
                if (isAnimating) {
                    tmpMatrix.postConcat(screenRotationAnimation.getEnterTransformation().getMatrix());
                }
                spec = this.mService.mAccessibilityController.getMagnificationSpecForWindowLocked(this.mWin);
                tmpMatrix.postScale(spec.scale, spec.scale);
                tmpMatrix.postTranslate(spec.offsetX, spec.offsetY);
                this.mHaveMatrix = true;
                tmpMatrix.getValues(tmpFloats);
                this.mDsDx = tmpFloats[TYPE_NORMAL] * ratio;
                this.mDtDx = tmpFloats[3] * ratio;
                this.mDsDy = tmpFloats[TYPE_LEFT] * ratio;
                this.mDtDy = tmpFloats[4] * ratio;
                x = (tmpFloats[TYPE_RIGHT] * ratio) + pendingX;
                y = (tmpFloats[5] * ratio) + pendingY;
                this.mWin.mShownPosition.set((int) x, (int) y);
                this.mShownAlpha = this.mAlpha;
                if (this.mService.mLimitedAlphaCompositing) {
                    if (PixelFormat.formatHasAlpha(this.mWin.mAttrs.format)) {
                        if (this.mWin.isIdentityMatrix(this.mDsDx, this.mDtDx, this.mDsDy, this.mDtDy)) {
                            if (floatEqualCompare(x, (float) frame.left)) {
                            }
                        }
                    }
                }
                if (selfTransformation) {
                    this.mShownAlpha *= this.mTransformation.getAlpha();
                }
                if (transformation != null) {
                    this.mShownAlpha *= transformation.getAlpha();
                }
                if (transformation2 != null) {
                    this.mShownAlpha *= transformation2.getAlpha();
                    if (transformation2.hasClipRect()) {
                        if (ignoreParentClipRect(this.mWin.getAttrs())) {
                            this.mClipRect.set(transformation2.getClipRect());
                            this.mHScale = this.mWin.mHScale;
                            this.mVScale = this.mWin.mVScale;
                            this.mHasClipRect = true;
                            if (this.mWin.layoutInParentFrame()) {
                                this.mClipRect.offset(this.mWin.mContainingFrame.left - this.mWin.mFrame.left, this.mWin.mContainingFrame.top - this.mWin.mFrame.top);
                            }
                        }
                    }
                }
                if (isAnimating) {
                    this.mShownAlpha *= screenRotationAnimation.getEnterTransformation().getAlpha();
                }
            }
        }
        transformation2 = null;
        updatedisplayinfo();
        wallpaperTarget = this.mWallpaperControllerLocked.getWallpaperTarget();
        if (this.mService.mAnimateWallpaperWithTarget) {
            wallpaperAnimator = wallpaperTarget.mWinAnimator;
            if (wallpaperAnimator.mAnimation.getDetachWallpaper()) {
                transformation = wallpaperAnimator.mTransformation;
            }
            if (wallpaperTarget.mAppToken != null) {
            }
            if (wpAppAnimator.animation.getDetachWallpaper()) {
                transformation2 = wpAppAnimator.transformation;
            }
        }
        displayId = this.mWin.getDisplayId();
        screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(displayId);
        if (screenRotationAnimation == null) {
        }
        ratio = HwCircleAnimation.SMALL_ALPHA;
        pendingX = 0.0f;
        pendingY = 0.0f;
        if (type != TYPE_LEFT) {
            ratio = this.mLazyScale;
            pendingY = ((float) this.mHeight) * (HwCircleAnimation.SMALL_ALPHA - this.mLazyScale);
        } else if (type == TYPE_RIGHT) {
            ratio = this.mLazyScale;
            pendingX = ((float) this.mWidth) * (HwCircleAnimation.SMALL_ALPHA - this.mLazyScale);
            pendingY = ((float) this.mHeight) * (HwCircleAnimation.SMALL_ALPHA - this.mLazyScale);
        }
        this.mHasClipRect = false;
        if (this.mHScale == this.mWin.mHScale) {
            if (this.mVScale == this.mWin.mVScale) {
                if (this.mIsWallpaper) {
                    if (this.mService.mWindowPlacerLocked.mWallpaperActionPending) {
                        return;
                    }
                }
                if (!this.mWin.isDragResizeChanged()) {
                    spec = null;
                    spec = this.mService.mAccessibilityController.getMagnificationSpecForWindowLocked(this.mWin);
                    if (spec == null) {
                        this.mWin.mShownPosition.set((int) ((((float) this.mWin.mFrame.left) * ratio) + pendingX), (int) ((((float) this.mWin.mFrame.top) * ratio) + pendingY));
                        if (this.mWin.mXOffset == 0) {
                        }
                        this.mWin.mShownPosition.offset((int) (((float) this.mWin.mXOffset) * this.mLazyScale), (int) (((float) this.mWin.mYOffset) * this.mLazyScale));
                        this.mShownAlpha = this.mAlpha;
                        this.mHaveMatrix = false;
                        this.mDsDx = this.mWin.mGlobalScale * ratio;
                        this.mDtDx = 0.0f;
                        this.mDsDy = 0.0f;
                        this.mDtDy = this.mWin.mGlobalScale * ratio;
                    } else {
                        frame = this.mWin.mFrame;
                        tmpFloats = this.mService.mTmpFloats;
                        tmpMatrix = this.mWin.mTmpMatrix;
                        tmpMatrix.setScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
                        tmpMatrix.postTranslate((float) (frame.left + this.mWin.mXOffset), (float) (frame.top + this.mWin.mYOffset));
                        if (spec.isNop()) {
                            tmpMatrix.postScale(spec.scale, spec.scale);
                            tmpMatrix.postTranslate(spec.offsetX, spec.offsetY);
                        }
                        tmpMatrix.getValues(tmpFloats);
                        this.mHaveMatrix = true;
                        this.mDsDx = tmpFloats[TYPE_NORMAL] * ratio;
                        this.mDtDx = tmpFloats[3] * ratio;
                        this.mDsDy = tmpFloats[TYPE_LEFT] * ratio;
                        this.mDtDy = tmpFloats[4] * ratio;
                        this.mWin.mShownPosition.set((int) ((tmpFloats[TYPE_RIGHT] * ratio) + pendingX), (int) ((tmpFloats[5] * ratio) + pendingY));
                        this.mShownAlpha = this.mAlpha;
                    }
                    return;
                }
                return;
            }
        }
        frame = this.mWin.mFrame;
        tmpFloats = this.mService.mTmpFloats;
        tmpMatrix = this.mWin.mTmpMatrix;
        if (isAnimating) {
        }
        tmpMatrix.reset();
        tmpMatrix.postScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
        if (selfTransformation) {
            tmpMatrix.postConcat(this.mTransformation.getMatrix());
        }
        tmpMatrix.postTranslate((float) (frame.left + this.mWin.mXOffset), (float) (frame.top + this.mWin.mYOffset));
        if (transformation != null) {
            tmpMatrix.postConcat(transformation.getMatrix());
        }
        if (transformation2 != null) {
            tmpMatrix.postConcat(transformation2.getMatrix());
        }
        if (isAnimating) {
            tmpMatrix.postConcat(screenRotationAnimation.getEnterTransformation().getMatrix());
        }
        spec = this.mService.mAccessibilityController.getMagnificationSpecForWindowLocked(this.mWin);
        tmpMatrix.postScale(spec.scale, spec.scale);
        tmpMatrix.postTranslate(spec.offsetX, spec.offsetY);
        this.mHaveMatrix = true;
        tmpMatrix.getValues(tmpFloats);
        this.mDsDx = tmpFloats[TYPE_NORMAL] * ratio;
        this.mDtDx = tmpFloats[3] * ratio;
        this.mDsDy = tmpFloats[TYPE_LEFT] * ratio;
        this.mDtDy = tmpFloats[4] * ratio;
        x = (tmpFloats[TYPE_RIGHT] * ratio) + pendingX;
        y = (tmpFloats[5] * ratio) + pendingY;
        this.mWin.mShownPosition.set((int) x, (int) y);
        this.mShownAlpha = this.mAlpha;
        if (this.mService.mLimitedAlphaCompositing) {
            if (PixelFormat.formatHasAlpha(this.mWin.mAttrs.format)) {
                if (this.mWin.isIdentityMatrix(this.mDsDx, this.mDtDx, this.mDsDy, this.mDtDy)) {
                    if (floatEqualCompare(x, (float) frame.left)) {
                    }
                }
            }
        }
        if (selfTransformation) {
            this.mShownAlpha *= this.mTransformation.getAlpha();
        }
        if (transformation != null) {
            this.mShownAlpha *= transformation.getAlpha();
        }
        if (transformation2 != null) {
            this.mShownAlpha *= transformation2.getAlpha();
            if (transformation2.hasClipRect()) {
                if (ignoreParentClipRect(this.mWin.getAttrs())) {
                    this.mClipRect.set(transformation2.getClipRect());
                    this.mHScale = this.mWin.mHScale;
                    this.mVScale = this.mWin.mVScale;
                    this.mHasClipRect = true;
                    if (this.mWin.layoutInParentFrame()) {
                        this.mClipRect.offset(this.mWin.mContainingFrame.left - this.mWin.mFrame.left, this.mWin.mContainingFrame.top - this.mWin.mFrame.top);
                    }
                }
            }
        }
        if (isAnimating) {
            this.mShownAlpha *= screenRotationAnimation.getEnterTransformation().getAlpha();
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
            win = win.mAttachedWindow;
        }
        WindowStateAnimator winAnimator = win.mWinAnimator;
        if (isFullScreen || !winAnimator.mAnimating) {
            return false;
        }
        return this.mWin.mLayoutAttached;
    }

    void computeShownFrameNormalLocked() {
        computeShownFrameLocked(TYPE_NORMAL);
    }

    public void hwPrepareSurfaceLocked() {
        int openLazyMode = this.mService.getLazyMode();
        int requestedOrientation = -1;
        if (this.mWin.mAppToken != null) {
            requestedOrientation = this.mWin.mAppToken.requestedOrientation;
        }
        if (this.mIsLazy) {
            computeShownFrameNormalLocked();
            if (this.mWin.toString().contains("hwSingleMode_windowbg")) {
                this.mAnimLayer = this.mAnimator.offsetLayer + LifeCycleStateMachine.TIME_OUT_TIME;
            }
            if (this.mWin.toString().contains("hwSingleMode_windowbg_hint")) {
                this.mAnimLayer = 810000;
            }
        } else if (isOrientationLandscape(requestedOrientation)) {
            computeShownFrameNormalLocked();
            this.mPreOpenLazyMode = TYPE_NORMAL;
        } else {
            computeShownFrameLockedByLazyMode(openLazyMode);
        }
        if (this.mWin.mAttrs.type == HwNetworkPropertyChecker.HW_DEFAULT_REEVALUATE_DELAY_MS && this.mPreOpenLazyMode != 0 && openLazyMode != 0 && (this.mLazyIsLeft || this.mLazyIsRight)) {
            this.mDtDy += 2.0f / ((float) this.mWin.mFrame.height());
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
        if (openLazyMode == 0 && this.mPreOpenLazyMode == TYPE_LEFT) {
            this.mPreOpenLazyMode = TYPE_NORMAL;
            this.mLazyIsExiting = true;
            this.mLazyIsEntering = false;
            this.mAnimator.offsetLayer = TYPE_NORMAL;
            this.mLazyScale = 0.8f;
            computeShownFrameLeftLocked();
        } else if (openLazyMode == 0 && this.mPreOpenLazyMode == TYPE_RIGHT) {
            this.mPreOpenLazyMode = TYPE_NORMAL;
            this.mLazyIsExiting = true;
            this.mLazyIsEntering = false;
            this.mLazyScale = 0.8f;
            this.mAnimator.offsetLayer = TYPE_NORMAL;
            computeShownFrameRightLocked();
        } else if (openLazyMode == 0 && this.mLazyIsExiting && this.mPreOpenLazyMode == 0) {
            if (this.mLazyIsLeft) {
                setLeftLazyScale();
            } else if (this.mLazyIsRight) {
                setRightLazyScale();
            } else {
                this.mLazyScale = HwCircleAnimation.SMALL_ALPHA;
                this.mLazyIsExiting = false;
                this.mLazyIsEntering = false;
                this.mLazyIsRight = false;
                this.mLazyIsLeft = false;
                this.mPreOpenLazyMode = TYPE_NORMAL;
                computeShownFrameNormalLocked();
            }
        } else if (openLazyMode == TYPE_LEFT) {
            handleLeftScale();
        } else if (openLazyMode == TYPE_RIGHT) {
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
            this.mLazyScale = HwCircleAnimation.SMALL_ALPHA;
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
            this.mLazyScale = HwCircleAnimation.SMALL_ALPHA;
            this.mLazyIsExiting = false;
            this.mLazyIsRight = false;
            computeShownFrameNormalLocked();
        }
    }

    private void handleLeftScale() {
        if (this.mPreOpenLazyMode == 0) {
            this.mPreOpenLazyMode = TYPE_LEFT;
            this.mLazyIsEntering = true;
            this.mLazyIsExiting = false;
            this.mLazyScale = 0.95f;
            computeShownFrameLeftLocked();
        } else if (this.mPreOpenLazyMode == TYPE_LEFT && this.mLazyIsEntering) {
            if (floatEqualCompare(0.95f)) {
                this.mLazyScale = 0.9f;
            } else if (floatEqualCompare(0.9f)) {
                this.mLazyScale = 0.85f;
            } else if (floatEqualCompare(0.85f)) {
                this.mLazyScale = 0.8f;
            } else if (floatEqualCompare(0.8f)) {
                this.mLazyScale = SlideTouchEvent.SCALE;
                this.mLazyIsEntering = false;
                this.mLazyIsLeft = true;
            }
            computeShownFrameLeftLocked();
        } else {
            this.mLazyScale = SlideTouchEvent.SCALE;
            this.mLazyIsLeft = true;
            this.mAnimator.offsetLayer = 800000;
            computeShownFrameLeftLocked();
        }
    }

    private void handleRightScale() {
        if (this.mPreOpenLazyMode == 0) {
            this.mPreOpenLazyMode = TYPE_RIGHT;
            this.mLazyIsEntering = true;
            this.mLazyIsExiting = false;
            this.mLazyScale = 0.95f;
            computeShownFrameRightLocked();
        } else if (this.mPreOpenLazyMode == TYPE_RIGHT && this.mLazyIsEntering) {
            if (floatEqualCompare(0.95f)) {
                this.mLazyScale = 0.9f;
            } else if (floatEqualCompare(0.9f)) {
                this.mLazyScale = 0.85f;
            } else if (floatEqualCompare(0.85f)) {
                this.mLazyScale = 0.8f;
            } else if (floatEqualCompare(0.8f)) {
                this.mLazyScale = SlideTouchEvent.SCALE;
                this.mLazyIsEntering = false;
                this.mLazyIsRight = true;
            }
            computeShownFrameRightLocked();
        } else {
            this.mLazyScale = SlideTouchEvent.SCALE;
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
        if (this.mWin.mAppToken != null && openLazyMode != 0 && floatEqualCompare(SlideTouchEvent.SCALE)) {
            Flog.i(HwGlobalActionsView.VIBRATE_DELAY, "win=" + this.mWin + ",openLazyMode=" + openLazyMode + ",mPreOpenLazyMode=" + this.mPreOpenLazyMode + ",shownPosition=" + this.mWin.mShownPosition + ",mLazyIsEntering=" + this.mLazyIsEntering + ",mLazyIsExiting=" + this.mLazyIsExiting + ",mLazyIsLeft=" + this.mLazyIsLeft + ",mLazyIsRight=" + this.mLazyIsRight + ",MATRIX [" + this.mDsDx + "," + this.mDtDx + "," + this.mDsDy + "," + this.mDtDy + "]");
        }
    }

    private boolean ignoreParentClipRect(LayoutParams lp) {
        return (lp.privateFlags & 2097152) != 0;
    }
}
