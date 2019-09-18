package com.android.server.wm;

import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.os.Bundle;
import android.util.HwPCUtils;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.WindowManager;
import com.android.server.HwServiceFactory;
import com.android.server.gesture.GestureNavConst;
import com.android.server.mtm.iaware.appmng.appfreeze.AwareAppFreezeMng;
import com.huawei.displayengine.DisplayEngineManager;

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
    boolean mLazyIsLeft;
    boolean mLazyIsRight;
    float mLazyScale;
    int mPreOpenLazyMode;
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
        if (type != 2000 || animLayer >= 400000 || (!HwServiceFactory.isCoverClosed())) {
            return animLayer;
        }
        return 400000;
    }

    /* access modifiers changed from: package-private */
    public void computeShownFrameRightLocked() {
        computeShownFrameLocked(2);
    }

    /* access modifiers changed from: package-private */
    public void computeShownFrameLeftLocked() {
        computeShownFrameLocked(1);
    }

    /* access modifiers changed from: package-private */
    public void computeShownFrameLocked(int type) {
        int i = type;
        if (i == 1 || i == 2) {
            updatedisplayinfo();
        }
        ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(this.mWin.getDisplayId());
        boolean screenAnimation = screenRotationAnimation != null && screenRotationAnimation.isAnimating();
        float ratio = 1.0f;
        float pendingX = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        float pendingY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        if (HwFoldScreenState.isFoldScreenDevice() && this.mService.isInSubFoldScaleMode() && !this.mWin.toString().contains("FoldScreen_SubScreenViewEntry")) {
            updatedisplayinfo();
            ratio = this.mService.mSubFoldModeScale;
            int rotation = this.mWin.getDisplayContent().getRotation();
            if (rotation == 0) {
                pendingY = ((float) this.mHeight) * (1.0f - ratio);
            } else if (rotation == 1) {
                pendingY = ((float) this.mWidth) * (1.0f - ratio);
                pendingX = ((float) this.mHeight) * (1.0f - ratio);
            } else if (rotation == 2) {
                pendingX = ((float) this.mWidth) * (1.0f - ratio);
            }
        }
        if (i == 1) {
            ratio = this.mLazyScale;
            pendingY = ((float) this.mHeight) * (1.0f - this.mLazyScale);
        } else if (i == 2) {
            ratio = this.mLazyScale;
            pendingX = ((float) this.mWidth) * (1.0f - this.mLazyScale);
            pendingY = ((float) this.mHeight) * (1.0f - this.mLazyScale);
        }
        if (screenAnimation) {
            Rect frame = this.mWin.mFrame;
            float[] tmpFloats = this.mService.mTmpFloats;
            Matrix tmpMatrix = this.mWin.mTmpMatrix;
            if (screenRotationAnimation.isRotating()) {
                float w = (float) frame.width();
                float h = (float) frame.height();
                if (w < 1.0f || h < 1.0f) {
                    tmpMatrix.reset();
                } else {
                    tmpMatrix.setScale(1.0f + (2.0f / w), 1.0f + (2.0f / h), w / 2.0f, h / 2.0f);
                }
            } else {
                tmpMatrix.reset();
            }
            tmpMatrix.postScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
            tmpMatrix.postTranslate((float) this.mWin.mAttrs.surfaceInsets.left, (float) this.mWin.mAttrs.surfaceInsets.top);
            this.mHaveMatrix = true;
            tmpMatrix.getValues(tmpFloats);
            this.mDsDx = tmpFloats[0] * ratio;
            this.mDtDx = tmpFloats[3] * ratio;
            this.mDtDy = tmpFloats[1] * ratio;
            this.mDsDy = tmpFloats[4] * ratio;
            this.mShownAlpha = this.mAlpha;
            if ((!this.mService.mLimitedAlphaCompositing || !PixelFormat.formatHasAlpha(this.mWin.mAttrs.format) || this.mWin.isIdentityMatrix(this.mDsDx, this.mDtDx, this.mDtDy, this.mDsDy)) && screenAnimation) {
                this.mShownAlpha *= screenRotationAnimation.getEnterTransformation().getAlpha();
            }
        } else if ((!this.mIsWallpaper || !this.mService.mRoot.mWallpaperActionPending) && !this.mWin.isDragResizeChanged()) {
            this.mShownPosition.set((int) ((((float) this.mWin.mFrame.left) * ratio) + pendingX), (int) ((((float) this.mWin.mFrame.top) * ratio) + pendingY));
            this.mShownAlpha = this.mAlpha;
            this.mHaveMatrix = false;
            this.mDsDx = this.mWin.mGlobalScale * ratio;
            this.mDtDx = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.mDtDy = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.mDsDy = this.mWin.mGlobalScale * ratio;
        }
    }

    /* access modifiers changed from: package-private */
    public void computeShownFrameLocked() {
        hwPrepareSurfaceLocked();
    }

    /* access modifiers changed from: package-private */
    public boolean isChildWindowAnimating() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public void computeShownFrameNormalLocked() {
        computeShownFrameLocked(0);
    }

    public void hwPrepareSurfaceLocked() {
        int openLazyMode = this.mService.getLazyMode();
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
                this.mAnimLayer = 10000 + this.mAnimator.offsetLayer;
            }
            if (this.mWin.toString().contains("hwSingleMode_windowbg_hint")) {
                this.mAnimLayer = 810000;
            }
        } else if (isOrientationLandscape(requestedOrientation)) {
            computeShownFrameNormalLocked();
            this.mPreOpenLazyMode = 0;
        } else {
            computeShownFrameLockedByLazyMode(openLazyMode);
        }
        if (this.mWin.mAttrs.type == 2000 && this.mPreOpenLazyMode != 0 && openLazyMode != 0 && (this.mLazyIsLeft || this.mLazyIsRight)) {
            this.mDsDy += 2.0f / ((float) this.mWin.mFrame.height());
        }
        if (isMultiWindowInSingleHandMode()) {
            this.mWin.getDisplayContent().mDividerControllerLocked.adjustBoundsForSingleHand();
        }
        traceLogForLazyMode(openLazyMode);
        if (HwPCUtils.isPcCastModeInServer() && !this.mIsDefaultDisplay && HwPCUtils.isValidExtDisplayId(this.mWin.getDisplayId())) {
            computeShownPCFrameLocked();
        }
    }

    private boolean floatEqualCompare(float f) {
        return ((double) Math.abs(this.mLazyScale - f)) < 1.0E-6d;
    }

    private boolean isOrientationLandscape(int requestedOrientation) {
        return requestedOrientation == 0 || requestedOrientation == 6 || requestedOrientation == 8 || requestedOrientation == 11;
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
        } else if (this.mPreOpenLazyMode != 1 || !this.mLazyIsEntering) {
            this.mLazyScale = 0.75f;
            this.mLazyIsLeft = true;
            this.mAnimator.offsetLayer = 800000;
            computeShownFrameLeftLocked();
        } else {
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
        }
    }

    private void handleRightScale() {
        if (this.mPreOpenLazyMode == 0) {
            this.mPreOpenLazyMode = 2;
            this.mLazyIsEntering = true;
            this.mLazyIsExiting = false;
            this.mLazyScale = 0.95f;
            computeShownFrameRightLocked();
        } else if (this.mPreOpenLazyMode != 2 || !this.mLazyIsEntering) {
            this.mLazyScale = 0.75f;
            this.mLazyIsRight = true;
            this.mAnimator.offsetLayer = 800000;
            computeShownFrameRightLocked();
        } else {
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
        }
    }

    private boolean isMultiWindowInSingleHandMode() {
        return (this.mWin.mAttrs.type == 2034 && this.mLazyIsEntering && floatEqualCompare(0.8f)) || (this.mLazyIsExiting && floatEqualCompare(0.95f));
    }

    private void traceLogForLazyMode(int openLazyMode) {
    }

    private void computeShownPCFrameLocked() {
        if (this.mService == null || this.mService.mHwWMSEx == null) {
            HwPCUtils.log(TAG, "fail to computeShownPCFrameLocked, service is down");
            return;
        }
        float pcScreenScale = this.mService.mHwWMSEx.getPCScreenScale();
        this.mWin.getPendingTransaction().setMatrix(this.mWin.getSurfaceControl(), pcScreenScale, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, pcScreenScale);
    }

    public float[] getPCDisplayModeSurfacePos(Rect tmpSize) {
        WindowState w = this.mWin;
        float posTop = (float) tmpSize.left;
        float posTop2 = (float) tmpSize.top;
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(w.getDisplayId())) {
            int mode = this.mService.getPCScreenDisplayMode();
            if (mode != 0) {
                float pcDisplayScale = mode == 1 ? 0.95f : 0.9f;
                Rect surfaceInsets = this.mWin.mAttrs.surfaceInsets;
                Point outPoint = new Point();
                this.mService.getBaseDisplaySize(w.getDisplayId(), outPoint);
                float scale = (1.0f - pcDisplayScale) / 2.0f;
                if (this.mDsDx == 1.0f && this.mDsDy == 1.0f) {
                    float f = ((float) outPoint.x) * scale;
                    posTop2 = (((float) outPoint.y) * scale) + (posTop2 * pcDisplayScale);
                    posTop = f + (posTop * pcDisplayScale);
                } else if (surfaceInsets.left > 0 && surfaceInsets.top > 0) {
                    float tmpScale = 1.0f - pcDisplayScale;
                    posTop += ((float) surfaceInsets.left) * tmpScale;
                    posTop2 += ((float) surfaceInsets.top) * tmpScale;
                }
                HwPCUtils.log(TAG, "getPCDisplayModeSurfacePos name:" + w.mLastTitle + " posLeft:" + posTop + " posTop:" + posTop2);
            }
        }
        return new float[]{posTop, posTop2};
    }

    private boolean ignoreParentClipRect(WindowManager.LayoutParams lp) {
        return (lp.privateFlags & 1073741824) != 0;
    }

    /* access modifiers changed from: package-private */
    public WindowSurfaceController createSurfaceLocked(int windowType, int ownerUid) {
        WindowSurfaceController surfaceController = HwWindowStateAnimator.super.createSurfaceLocked(windowType, ownerUid);
        sendMessageToDESceneHandler(1);
        return surfaceController;
    }

    /* access modifiers changed from: package-private */
    public void destroySurfaceLocked() {
        sendMessageToDESceneHandler(-1);
        HwWindowStateAnimator.super.destroySurfaceLocked();
    }

    private void sendMessageToDESceneHandler(int pos) {
        WindowState ws = this.mWin;
        WindowManager.LayoutParams attrs = ws.mAttrs;
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

    private int checkWindowType(WindowState win) {
        if (24 == win.mAppOp) {
            return 1;
        }
        if (2005 == win.getAttrs().type) {
            return 2;
        }
        return -1;
    }

    public boolean isEvilWindow(WindowState win) {
        if (win == null) {
            return false;
        }
        return AwareAppFreezeMng.getInstance().isEvilWindow(win.mSession.mPid, System.identityHashCode(win), checkWindowType(win));
    }
}
