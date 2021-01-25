package com.android.server.wm;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.iawareperf.UniPerf;
import android.os.Bundle;
import android.os.IBinder;
import android.util.HwMwUtils;
import android.util.Log;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import com.huawei.android.app.HwActivityTaskManager;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public class ScreenRotationAnimation {
    private static final String ANIM_STR_CONST = "anim";
    static final boolean DEBUG_HWANIMATION = false;
    static final boolean DEBUG_STATE = false;
    static final boolean DEBUG_TRANSFORMS = false;
    private static final String ENTER_STR_CONST = "_enter";
    private static final String EXIT_STR_CONST = "_exit";
    private static final String HWEXT_STR_CONST = "androidhwext";
    static final int SCREEN_FREEZE_LAYER_BASE = 2010000;
    static final int SCREEN_FREEZE_LAYER_CUSTOM = 2010003;
    static final int SCREEN_FREEZE_LAYER_ENTER = 2010000;
    static final int SCREEN_FREEZE_LAYER_EXIT = 2010002;
    static final int SCREEN_FREEZE_LAYER_SCREENSHOT = 2010001;
    private static final String SCREEN_ROTATE_STR_CONST = "screen_rotate_";
    static final String TAG = "WindowManager";
    static final boolean TWO_PHASE_ANIMATION = false;
    static final boolean USE_CUSTOM_BLACK_FRAME = false;
    private static Rect sFullFoldDisplayModeRect;
    private static Rect sMainFoldDisplayModeRect;
    boolean mAnimRunning;
    protected Bundle mAnimationTypeInfo;
    final Context mContext;
    int mCurRotation;
    Rect mCurrentDisplayRect = new Rect();
    BlackFrame mCustomBlackFrame;
    final DisplayContent mDisplayContent;
    final Transformation mEnterTransformation = new Transformation();
    BlackFrame mEnteringBlackFrame;
    final Matrix mExitFrameFinalMatrix = new Matrix();
    final Transformation mExitTransformation = new Transformation();
    BlackFrame mExitingBlackFrame;
    boolean mFinishAnimReady;
    long mFinishAnimStartTime;
    Animation mFinishEnterAnimation;
    final Transformation mFinishEnterTransformation = new Transformation();
    Animation mFinishExitAnimation;
    final Transformation mFinishExitTransformation = new Transformation();
    Animation mFinishFrameAnimation;
    final Transformation mFinishFrameTransformation = new Transformation();
    Animation mFoldProjectionAnimation;
    final Transformation mFoldProjectionTransformation = new Transformation();
    boolean mForceDefaultOrientation;
    final Matrix mFrameInitialMatrix = new Matrix();
    final Transformation mFrameTransformation = new Transformation();
    long mHalfwayPoint;
    int mHeight;
    protected boolean mIsHwMagicWindow = false;
    private boolean mIsNeedLandAni = false;
    private boolean mIsSingleHandScreenShotAnim = false;
    Animation mLastRotateEnterAnimation;
    final Transformation mLastRotateEnterTransformation = new Transformation();
    Animation mLastRotateExitAnimation;
    final Transformation mLastRotateExitTransformation = new Transformation();
    Animation mLastRotateFrameAnimation;
    final Transformation mLastRotateFrameTransformation = new Transformation();
    private boolean mMoreFinishEnter;
    private boolean mMoreFinishExit;
    private boolean mMoreFinishFrame;
    private boolean mMoreFoldProjection;
    private boolean mMoreRotateEnter;
    private boolean mMoreRotateExit;
    private boolean mMoreRotateFrame;
    private boolean mMoreStartEnter;
    private boolean mMoreStartExit;
    private boolean mMoreStartFrame;
    Rect mOriginalDisplayRect = new Rect();
    int mOriginalHeight;
    int mOriginalRotation;
    int mOriginalWidth;
    Animation mRotateEnterAnimation;
    final Transformation mRotateEnterTransformation = new Transformation();
    Animation mRotateExitAnimation;
    final Transformation mRotateExitTransformation = new Transformation();
    Animation mRotateFrameAnimation;
    final Transformation mRotateFrameTransformation = new Transformation();
    private final WindowManagerService mService;
    final Matrix mSnapshotFinalMatrix = new Matrix();
    final Matrix mSnapshotInitialMatrix = new Matrix();
    Animation mStartEnterAnimation;
    final Transformation mStartEnterTransformation = new Transformation();
    Animation mStartExitAnimation;
    final Transformation mStartExitTransformation = new Transformation();
    Animation mStartFrameAnimation;
    final Transformation mStartFrameTransformation = new Transformation();
    boolean mStarted;
    SurfaceControl mSurfaceControl;
    final float[] mTmpFloats = new float[9];
    final Matrix mTmpMatrix = new Matrix();
    protected boolean mUsingFoldAnim = false;
    int mWidth;

    static {
        sFullFoldDisplayModeRect = HwFoldScreenState.getScreenPhysicalRect(1);
        sMainFoldDisplayModeRect = HwFoldScreenState.getScreenPhysicalRect(2);
        Log.i(TAG, "getScreenPhysicalRect: sFullFoldDisplayModeRect = " + sFullFoldDisplayModeRect + ", sMainFoldDisplayModeRect = " + sMainFoldDisplayModeRect);
        if (sFullFoldDisplayModeRect == null) {
            sFullFoldDisplayModeRect = new Rect();
        }
        if (sMainFoldDisplayModeRect == null) {
            sMainFoldDisplayModeRect = new Rect();
        }
    }

    public void printTo(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("mSurface=");
        pw.print(this.mSurfaceControl);
        pw.print(" mWidth=");
        pw.print(this.mWidth);
        pw.print(" mHeight=");
        pw.println(this.mHeight);
        pw.print(prefix);
        pw.print("mExitingBlackFrame=");
        pw.println(this.mExitingBlackFrame);
        BlackFrame blackFrame = this.mExitingBlackFrame;
        if (blackFrame != null) {
            blackFrame.printTo(prefix + "  ", pw);
        }
        pw.print(prefix);
        pw.print("mEnteringBlackFrame=");
        pw.println(this.mEnteringBlackFrame);
        BlackFrame blackFrame2 = this.mEnteringBlackFrame;
        if (blackFrame2 != null) {
            blackFrame2.printTo(prefix + "  ", pw);
        }
        pw.print(prefix);
        pw.print("mCurRotation=");
        pw.print(this.mCurRotation);
        pw.print(" mOriginalRotation=");
        pw.println(this.mOriginalRotation);
        pw.print(prefix);
        pw.print("mOriginalWidth=");
        pw.print(this.mOriginalWidth);
        pw.print(" mOriginalHeight=");
        pw.println(this.mOriginalHeight);
        pw.print(prefix);
        pw.print("mStarted=");
        pw.print(this.mStarted);
        pw.print(" mAnimRunning=");
        pw.print(this.mAnimRunning);
        pw.print(" mFinishAnimReady=");
        pw.print(this.mFinishAnimReady);
        pw.print(" mFinishAnimStartTime=");
        pw.println(this.mFinishAnimStartTime);
        pw.print(prefix);
        pw.print("mStartExitAnimation=");
        pw.print(this.mStartExitAnimation);
        pw.print(" ");
        this.mStartExitTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mStartEnterAnimation=");
        pw.print(this.mStartEnterAnimation);
        pw.print(" ");
        this.mStartEnterTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mStartFrameAnimation=");
        pw.print(this.mStartFrameAnimation);
        pw.print(" ");
        this.mStartFrameTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mFinishExitAnimation=");
        pw.print(this.mFinishExitAnimation);
        pw.print(" ");
        this.mFinishExitTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mFinishEnterAnimation=");
        pw.print(this.mFinishEnterAnimation);
        pw.print(" ");
        this.mFinishEnterTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mFinishFrameAnimation=");
        pw.print(this.mFinishFrameAnimation);
        pw.print(" ");
        this.mFinishFrameTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mRotateExitAnimation=");
        pw.print(this.mRotateExitAnimation);
        pw.print(" ");
        this.mRotateExitTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mRotateEnterAnimation=");
        pw.print(this.mRotateEnterAnimation);
        pw.print(" ");
        this.mRotateEnterTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mRotateFrameAnimation=");
        pw.print(this.mRotateFrameAnimation);
        pw.print(" ");
        this.mRotateFrameTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mExitTransformation=");
        this.mExitTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mEnterTransformation=");
        this.mEnterTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mFrameTransformation=");
        this.mFrameTransformation.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mFrameInitialMatrix=");
        this.mFrameInitialMatrix.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mSnapshotInitialMatrix=");
        this.mSnapshotInitialMatrix.printShortString(pw);
        pw.print(" mSnapshotFinalMatrix=");
        this.mSnapshotFinalMatrix.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mExitFrameFinalMatrix=");
        this.mExitFrameFinalMatrix.printShortString(pw);
        pw.println();
        pw.print(prefix);
        pw.print("mForceDefaultOrientation=");
        pw.print(this.mForceDefaultOrientation);
        if (this.mForceDefaultOrientation) {
            pw.print(" mOriginalDisplayRect=");
            pw.print(this.mOriginalDisplayRect.toShortString());
            pw.print(" mCurrentDisplayRect=");
            pw.println(this.mCurrentDisplayRect.toShortString());
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1133871366145L, this.mStarted);
        proto.write(1133871366146L, this.mAnimRunning);
        proto.end(token);
    }

    public ScreenRotationAnimation(Context context, DisplayContent displayContent, boolean fixedToUserRotation, boolean isSecure, WindowManagerService service) {
        int originalHeight;
        int originalWidth;
        Rect sourceCrop;
        SurfaceControl.ScreenshotGraphicBuffer gb;
        this.mService = service;
        this.mContext = context;
        this.mDisplayContent = displayContent;
        displayContent.getBounds(this.mOriginalDisplayRect);
        Display display = displayContent.getDisplay();
        int originalRotation = display.getRotation();
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        if (fixedToUserRotation) {
            this.mForceDefaultOrientation = true;
            originalWidth = displayContent.mBaseDisplayWidth;
            originalHeight = displayContent.mBaseDisplayHeight;
        } else {
            originalWidth = displayInfo.logicalWidth;
            originalHeight = displayInfo.logicalHeight;
        }
        if (originalRotation == 1 || originalRotation == 3) {
            this.mWidth = originalHeight;
            this.mHeight = originalWidth;
        } else {
            this.mWidth = originalWidth;
            this.mHeight = originalHeight;
        }
        if (this.mService.isScreenFolding()) {
            resizeForFoldScreen();
        }
        this.mOriginalRotation = originalRotation;
        this.mOriginalWidth = originalWidth;
        this.mOriginalHeight = originalHeight;
        SurfaceControl.Transaction t = this.mService.mTransactionFactory.make();
        try {
            if (!HwFoldScreenState.isInwardFoldDevice() || !this.mService.isScreenFolding() || this.mUsingFoldAnim) {
                this.mSurfaceControl = displayContent.makeOverlay().setName("ScreenshotSurface").setBufferSize(this.mWidth, this.mHeight).setSecure(isSecure).build();
                SurfaceControl.Transaction t2 = this.mService.mTransactionFactory.make();
                t2.setOverrideScalingMode(this.mSurfaceControl, 1);
                t2.apply(true);
                int displayId = display.getDisplayId();
                Surface surface = this.mService.mSurfaceFactory.make();
                surface.copyFrom(this.mSurfaceControl);
                SurfaceControl.ScreenshotGraphicBuffer gb2 = null;
                this.mIsNeedLandAni = this.mService.mHwWMSEx.isNeedLandAni();
                if (this.mIsNeedLandAni) {
                    this.mService.mHwWMSEx.hideAboveAppWindowsContainers();
                }
                boolean isPcCastMode = HwActivityTaskManager.isPCMultiCastMode();
                if (HwFoldScreenState.isFoldScreenDevice()) {
                    sourceCrop = createSourceCropForFoldScreen();
                } else if (isPcCastMode) {
                    sourceCrop = new Rect(0, 0, this.mWidth, this.mHeight);
                } else {
                    sourceCrop = null;
                }
                if (sourceCrop != null) {
                    IBinder displayToken = this.mService.mDisplayManagerInternal.getDisplayToken(displayId);
                    if (displayToken != null) {
                        gb2 = SurfaceControl.screenshotToBufferWithSecureLayersUnsafe(displayToken, sourceCrop, this.mWidth, this.mHeight, false, 0);
                    } else {
                        Slog.w(TAG, "displayToken is null,displayId:" + displayId);
                    }
                    gb = gb2;
                } else {
                    gb = this.mService.mDisplayManagerInternal.screenshot(displayId);
                }
                if (gb != null) {
                    if (isPcCastMode) {
                        this.mService.mAtmService.mHwATMSEx.captureScreenToPc(gb);
                    }
                    try {
                        surface.attachAndQueueBufferWithColorSpace(gb.getGraphicBuffer(), gb.getColorSpace());
                    } catch (RuntimeException e) {
                        Slog.w(TAG, "Failed to attach screenshot - " + e.getMessage());
                    }
                    if (gb.containsSecureLayers()) {
                        t.setSecure(this.mSurfaceControl, true);
                    }
                    t.setLayer(this.mSurfaceControl, SCREEN_FREEZE_LAYER_SCREENSHOT);
                    t.setAlpha(this.mSurfaceControl, 0.0f);
                    t.show(this.mSurfaceControl);
                } else {
                    Slog.w(TAG, "Unable to take screenshot of display " + displayId);
                }
                surface.destroy();
                setRotation(t, originalRotation);
                t.apply();
            }
            this.mSurfaceControl = displayContent.makeOverlay().setName("ScreenshotSurface").setBufferSize(this.mWidth, this.mHeight).setSecure(isSecure).setColorLayer().build();
            Slog.d(TAG, "Create ScreenshotSurface: mWidth:" + this.mWidth + ", mHeight:" + this.mHeight);
            t.setWindowCrop(this.mSurfaceControl, this.mWidth, this.mHeight);
            t.setLayer(this.mSurfaceControl, SCREEN_FREEZE_LAYER_SCREENSHOT);
            t.setAlpha(this.mSurfaceControl, 1.0f);
            t.show(this.mSurfaceControl);
            setRotation(t, originalRotation);
            t.apply();
        } catch (Surface.OutOfResourcesException e2) {
            Slog.w(TAG, "Unable to allocate freeze surface", e2);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasScreenshot() {
        return this.mSurfaceControl != null;
    }

    private void setSnapshotTransform(SurfaceControl.Transaction t, Matrix matrix, float alpha) {
        if (this.mSurfaceControl != null) {
            matrix.getValues(this.mTmpFloats);
            float[] fArr = this.mTmpFloats;
            float x = fArr[2];
            float y = fArr[5];
            if (this.mForceDefaultOrientation) {
                this.mDisplayContent.getBounds(this.mCurrentDisplayRect);
                x -= (float) this.mCurrentDisplayRect.left;
                y -= (float) this.mCurrentDisplayRect.top;
            }
            t.setPosition(this.mSurfaceControl, x, y);
            SurfaceControl surfaceControl = this.mSurfaceControl;
            float[] fArr2 = this.mTmpFloats;
            t.setMatrix(surfaceControl, fArr2[0], fArr2[3], fArr2[1], fArr2[4]);
            t.setAlpha(this.mSurfaceControl, alpha);
        }
    }

    public static void createRotationMatrix(int rotation, int width, int height, Matrix outMatrix) {
        if (rotation == 0) {
            outMatrix.reset();
        } else if (rotation == 1) {
            outMatrix.setRotate(90.0f, 0.0f, 0.0f);
            outMatrix.postTranslate((float) height, 0.0f);
        } else if (rotation == 2) {
            outMatrix.setRotate(180.0f, 0.0f, 0.0f);
            outMatrix.postTranslate((float) width, (float) height);
        } else if (rotation == 3) {
            outMatrix.setRotate(270.0f, 0.0f, 0.0f);
            outMatrix.postTranslate(0.0f, (float) width);
        }
    }

    private void setRotation(SurfaceControl.Transaction t, int rotation) {
        this.mCurRotation = rotation;
        createRotationMatrix(DisplayContent.deltaRotation(rotation, 0), this.mWidth, this.mHeight, this.mSnapshotInitialMatrix);
        setSnapshotTransform(t, this.mSnapshotInitialMatrix, 1.0f);
    }

    public boolean setRotation(SurfaceControl.Transaction t, int rotation, long maxAnimationDuration, float animationScale, int finalWidth, int finalHeight) {
        setRotation(t, rotation);
        return false;
    }

    private boolean startAnimation(SurfaceControl.Transaction t, long maxAnimationDuration, float animationScale, int finalWidth, int finalHeight, boolean dismissing, int exitAnim, int enterAnim) {
        boolean customAnim;
        Surface.OutOfResourcesException e;
        Rect inner;
        Rect outer;
        if (this.mSurfaceControl == null) {
            return false;
        }
        if (this.mStarted) {
            return true;
        }
        this.mStarted = true;
        if (this.mCurRotation != this.mDisplayContent.getRotation()) {
            Slog.i(TAG, "AMS rotation is not updated in time");
            this.mCurRotation = this.mDisplayContent.getRotation();
        }
        int delta = DisplayContent.deltaRotation(this.mCurRotation, this.mOriginalRotation);
        if (exitAnim == 0 || enterAnim == 0) {
            customAnim = false;
            Context context = this.mContext;
            if (context != null) {
                if (this.mUsingFoldAnim) {
                    int fromFoldMode = this.mAnimationTypeInfo.getInt(WindowManagerService.FROM_FOLD_MODE_KEY);
                    int toFoldMode = this.mAnimationTypeInfo.getInt(WindowManagerService.TO_FOLD_MODE_KEY);
                    this.mRotateExitAnimation = createScreenFoldAnimation(false, fromFoldMode, toFoldMode);
                    this.mRotateEnterAnimation = createScreenFoldAnimation(true, fromFoldMode, toFoldMode);
                    createFoldProjectionAnimationIfNeed(fromFoldMode, toFoldMode);
                } else {
                    Resources resources = context.getResources();
                    int rotateExitAnimationId = resources.getIdentifier(SCREEN_ROTATE_STR_CONST + delta + EXIT_STR_CONST, ANIM_STR_CONST, HWEXT_STR_CONST);
                    Resources resources2 = this.mContext.getResources();
                    int rotateEnterAnimationId = resources2.getIdentifier(SCREEN_ROTATE_STR_CONST + delta + ENTER_STR_CONST, ANIM_STR_CONST, HWEXT_STR_CONST);
                    if (!(rotateExitAnimationId == 0 || rotateEnterAnimationId == 0)) {
                        this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, rotateExitAnimationId);
                        this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, rotateEnterAnimationId);
                    }
                }
            }
            if (HwMwUtils.ENABLED) {
                HwMwUtils.performPolicy(70, new Object[]{this, Integer.valueOf(delta), Integer.valueOf(finalHeight)});
            }
            if (this.mIsNeedLandAni && this.mService.mHwWMSEx.isNeedLandAni() && delta != 0 && this.mDisplayContent.mDisplayId == 0) {
                this.mRotateExitAnimation = this.mService.mHwWMSEx.createLandOpenAnimation(false);
                this.mRotateEnterAnimation = this.mService.mHwWMSEx.createLandOpenAnimation(true);
                this.mService.mHwWMSEx.startLandOpenAnimation();
            }
            if (this.mRotateExitAnimation == null || this.mRotateEnterAnimation == null) {
                if (delta == 0) {
                    this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432856);
                    this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432855);
                } else if (delta == 1) {
                    this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432868);
                    this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432867);
                } else if (delta == 2) {
                    this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432859);
                    this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432858);
                } else if (delta == 3) {
                    this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432865);
                    this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432864);
                }
            }
        } else {
            customAnim = true;
            this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, exitAnim);
            this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, enterAnim);
        }
        this.mRotateEnterAnimation.initialize(finalWidth, finalHeight, this.mOriginalWidth, this.mOriginalHeight);
        this.mRotateExitAnimation.initialize(finalWidth, finalHeight, this.mOriginalWidth, this.mOriginalHeight);
        initializeFoldProjectionAnimation(finalWidth, finalHeight);
        this.mAnimRunning = false;
        this.mFinishAnimReady = false;
        this.mFinishAnimStartTime = -1;
        this.mRotateExitAnimation.restrictDuration(maxAnimationDuration);
        this.mRotateExitAnimation.scaleCurrentDuration(animationScale);
        this.mRotateEnterAnimation.restrictDuration(maxAnimationDuration);
        this.mRotateEnterAnimation.scaleCurrentDuration(animationScale);
        setFoldProjectionAnimationDuration(maxAnimationDuration, animationScale);
        this.mDisplayContent.getDisplay().getLayerStack();
        if (!this.mIsHwMagicWindow && !customAnim && this.mExitingBlackFrame == null && !this.mUsingFoldAnim) {
            if (!HwFoldScreenState.isInwardFoldDevice()) {
                try {
                    createRotationMatrix(delta, this.mOriginalWidth, this.mOriginalHeight, this.mFrameInitialMatrix);
                    if (!this.mForceDefaultOrientation) {
                        if (!HwActivityTaskManager.isPCMultiCastMode() || HwActivityTaskManager.getCurPCWindowAreaNum() <= 0) {
                            outer = new Rect((-this.mOriginalWidth) * 1, (-this.mOriginalHeight) * 1, this.mOriginalWidth * 2, this.mOriginalHeight * 2);
                            inner = new Rect(0, 0, this.mOriginalWidth, this.mOriginalHeight);
                            this.mExitingBlackFrame = new BlackFrame(t, outer, inner, SCREEN_FREEZE_LAYER_EXIT, this.mDisplayContent, this.mForceDefaultOrientation);
                            this.mExitingBlackFrame.setMatrix(t, this.mFrameInitialMatrix);
                        }
                    }
                    outer = this.mCurrentDisplayRect;
                    inner = this.mOriginalDisplayRect;
                    this.mExitingBlackFrame = new BlackFrame(t, outer, inner, SCREEN_FREEZE_LAYER_EXIT, this.mDisplayContent, this.mForceDefaultOrientation);
                    try {
                        this.mExitingBlackFrame.setMatrix(t, this.mFrameInitialMatrix);
                    } catch (Surface.OutOfResourcesException e2) {
                        e = e2;
                    }
                } catch (Surface.OutOfResourcesException e3) {
                    e = e3;
                    Slog.w(TAG, "Unable to allocate black surface", e);
                    return !customAnim ? true : true;
                }
            }
        }
        if (!customAnim && this.mEnteringBlackFrame == null) {
            try {
                Rect outer2 = new Rect((-finalWidth) * 1, (-finalHeight) * 1, finalWidth * 2, finalHeight * 2);
                if (HwActivityTaskManager.isPCMultiCastMode() && HwActivityTaskManager.getCurPCWindowAreaNum() > 0) {
                    outer2.set(0, 0, finalWidth, finalHeight);
                }
                this.mEnteringBlackFrame = new BlackFrame(t, outer2, new Rect(0, 0, finalWidth, finalHeight), 2010000, this.mDisplayContent, false);
                return true;
            } catch (Surface.OutOfResourcesException e4) {
                Slog.w(TAG, "Unable to allocate black surface", e4);
                return true;
            }
        }
    }

    public boolean dismiss(SurfaceControl.Transaction t, long maxAnimationDuration, float animationScale, int finalWidth, int finalHeight, int exitAnim, int enterAnim) {
        if (this.mSurfaceControl == null) {
            return false;
        }
        if (this.mIsSingleHandScreenShotAnim) {
            this.mIsSingleHandScreenShotAnim = false;
            return false;
        }
        if (!this.mStarted) {
            startAnimation(t, maxAnimationDuration, animationScale, finalWidth, finalHeight, true, exitAnim, enterAnim);
        }
        if (!this.mStarted) {
            return false;
        }
        this.mFinishAnimReady = true;
        return true;
    }

    public void kill() {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.remove();
            this.mSurfaceControl = null;
        }
        if (this.mIsNeedLandAni) {
            this.mService.mHwWMSEx.finishLandOpenAnimation();
        }
        BlackFrame blackFrame = this.mCustomBlackFrame;
        if (blackFrame != null) {
            blackFrame.kill();
            this.mCustomBlackFrame = null;
        }
        BlackFrame blackFrame2 = this.mExitingBlackFrame;
        if (blackFrame2 != null) {
            blackFrame2.kill();
            this.mExitingBlackFrame = null;
        }
        BlackFrame blackFrame3 = this.mEnteringBlackFrame;
        if (blackFrame3 != null) {
            blackFrame3.kill();
            this.mEnteringBlackFrame = null;
        }
        Animation animation = this.mRotateExitAnimation;
        if (animation != null) {
            animation.cancel();
            this.mRotateExitAnimation = null;
        }
        Animation animation2 = this.mRotateEnterAnimation;
        if (animation2 != null) {
            animation2.cancel();
            this.mRotateEnterAnimation = null;
        }
        Animation animation3 = this.mFoldProjectionAnimation;
        if (animation3 != null) {
            animation3.cancel();
            this.mFoldProjectionAnimation = null;
            if (this.mService.mIsPerfBoost) {
                this.mService.mIsPerfBoost = false;
                UniPerf.getInstance().uniPerfEvent(4105, "", new int[]{-1});
            }
        }
    }

    public boolean isAnimating() {
        return hasAnimations();
    }

    public boolean isRotating() {
        return this.mCurRotation != this.mOriginalRotation;
    }

    private boolean hasAnimations() {
        return (this.mRotateEnterAnimation == null && this.mRotateExitAnimation == null && this.mFoldProjectionAnimation == null) ? false : true;
    }

    private boolean stepAnimation(long now) {
        Animation animation;
        Animation animation2;
        if (now > this.mHalfwayPoint) {
            this.mHalfwayPoint = Long.MAX_VALUE;
        }
        long j = 0;
        if (this.mFinishAnimReady && this.mFinishAnimStartTime < 0) {
            this.mFinishAnimStartTime = now;
        }
        updateMoreFoldProjection(now);
        if (this.mFinishAnimReady) {
            j = now - this.mFinishAnimStartTime;
        }
        boolean more = false;
        this.mMoreRotateExit = false;
        Animation animation3 = this.mRotateExitAnimation;
        if (animation3 != null) {
            this.mMoreRotateExit = animation3.getTransformation(now, this.mRotateExitTransformation);
            if (this.mUsingFoldAnim) {
                stepAnimationToEnd(this.mRotateExitAnimation, now);
            }
        }
        this.mMoreRotateEnter = false;
        Animation animation4 = this.mRotateEnterAnimation;
        if (animation4 != null) {
            this.mMoreRotateEnter = animation4.getTransformation(now, this.mRotateEnterTransformation);
        }
        if (!this.mMoreRotateExit && (animation2 = this.mRotateExitAnimation) != null) {
            animation2.cancel();
            this.mRotateExitAnimation = null;
            this.mRotateExitTransformation.clear();
        }
        if (!this.mMoreRotateEnter && (animation = this.mRotateEnterAnimation) != null) {
            animation.cancel();
            this.mRotateEnterAnimation = null;
            this.mRotateEnterTransformation.clear();
        }
        clearFoldProjectionAnimation();
        this.mExitTransformation.set(this.mRotateExitTransformation);
        this.mEnterTransformation.set(this.mRotateEnterTransformation);
        if (this.mMoreRotateEnter || this.mMoreRotateExit || !this.mFinishAnimReady) {
            more = true;
        }
        this.mSnapshotFinalMatrix.setConcat(this.mExitTransformation.getMatrix(), this.mSnapshotInitialMatrix);
        return more;
    }

    /* access modifiers changed from: package-private */
    public void updateSurfaces(SurfaceControl.Transaction t) {
        if (this.mStarted) {
            SurfaceControl surfaceControl = this.mSurfaceControl;
            if (surfaceControl != null && !this.mMoreStartExit && !this.mMoreFinishExit && !this.mMoreRotateExit) {
                t.hide(surfaceControl);
            }
            BlackFrame blackFrame = this.mCustomBlackFrame;
            if (blackFrame != null) {
                if (this.mMoreStartFrame || this.mMoreFinishFrame || this.mMoreRotateFrame) {
                    this.mCustomBlackFrame.setMatrix(t, this.mFrameTransformation.getMatrix());
                } else {
                    blackFrame.hide(t);
                }
            }
            BlackFrame blackFrame2 = this.mExitingBlackFrame;
            if (blackFrame2 != null) {
                if (this.mMoreStartExit || this.mMoreFinishExit || this.mMoreRotateExit) {
                    this.mExitFrameFinalMatrix.setConcat(this.mExitTransformation.getMatrix(), this.mFrameInitialMatrix);
                    this.mExitingBlackFrame.setMatrix(t, this.mExitFrameFinalMatrix);
                    if (this.mForceDefaultOrientation) {
                        this.mExitingBlackFrame.setAlpha(t, this.mExitTransformation.getAlpha());
                    }
                } else {
                    blackFrame2.hide(t);
                }
            }
            BlackFrame blackFrame3 = this.mEnteringBlackFrame;
            if (blackFrame3 != null) {
                if (this.mMoreStartEnter || this.mMoreFinishEnter || this.mMoreRotateEnter) {
                    this.mEnteringBlackFrame.setMatrix(t, this.mEnterTransformation.getMatrix());
                } else {
                    blackFrame3.hide(t);
                }
            }
            t.setEarlyWakeup();
            setSnapshotTransform(t, this.mSnapshotFinalMatrix, this.mExitTransformation.getAlpha());
            if (HwFoldScreenState.isFoldScreenDevice()) {
                updateFoldProjection();
            }
        }
    }

    public boolean stepAnimationLocked(long now) {
        if (!hasAnimations()) {
            this.mFinishAnimReady = false;
            return false;
        }
        if (!this.mAnimRunning) {
            Animation animation = this.mRotateEnterAnimation;
            if (animation != null) {
                animation.setStartTime(now);
            }
            Animation animation2 = this.mRotateExitAnimation;
            if (animation2 != null) {
                animation2.setStartTime(now);
            }
            Animation animation3 = this.mFoldProjectionAnimation;
            if (animation3 != null) {
                animation3.setStartTime(now);
            }
            this.mAnimRunning = true;
            this.mHalfwayPoint = (this.mRotateEnterAnimation.getDuration() / 2) + now;
        }
        return stepAnimation(now);
    }

    public Transformation getEnterTransformation() {
        return this.mEnterTransformation;
    }

    private void resizeForFoldScreen() {
        Rect rect;
        if (HwFoldScreenState.isInwardFoldDevice()) {
            int maxSize = Math.max(Math.max(sFullFoldDisplayModeRect.width(), sMainFoldDisplayModeRect.width()), Math.max(sFullFoldDisplayModeRect.height(), sMainFoldDisplayModeRect.height()));
            this.mHeight = maxSize;
            this.mWidth = maxSize;
            return;
        }
        int fromFoldMode = this.mService.getFromFoldDisplayMode();
        int toFoldMode = this.mService.getToFoldDisplayMode();
        Log.i(TAG, "screen is folding and from fold display mode = " + fromFoldMode + ", toFoldMode = " + toFoldMode);
        if (fromFoldMode == 3 && toFoldMode == 1) {
            rect = sFullFoldDisplayModeRect;
        } else if (fromFoldMode == 2 || fromFoldMode == 3) {
            rect = sMainFoldDisplayModeRect;
        } else {
            rect = sFullFoldDisplayModeRect;
        }
        if (rect == null) {
            Log.w(TAG, "display mode rect is null, using empty rect!");
            rect = new Rect();
        }
        this.mWidth = rect.width();
        this.mHeight = rect.height();
    }

    private Rect createSourceCropForFoldScreen() {
        Rect sourceCrop = new Rect();
        sourceCrop.left = (!HwFoldScreenState.isOutFoldDevice() || (this.mService.isScreenFolding() ? this.mService.getFromFoldDisplayMode() : this.mService.getFoldDisplayMode()) != 2) ? 0 : sFullFoldDisplayModeRect.width() - sMainFoldDisplayModeRect.width();
        sourceCrop.top = 0;
        sourceCrop.right = sourceCrop.left + this.mWidth;
        sourceCrop.bottom = sourceCrop.top + this.mHeight;
        return sourceCrop;
    }

    public void setAnimationTypeInfo(Bundle animaitonTypeInfo) {
    }

    /* access modifiers changed from: protected */
    public void stepAnimationToEnd(Animation animation, long nowTime) {
    }

    public Bundle getAnimationTypeInfo() {
        return null;
    }

    /* access modifiers changed from: protected */
    public Animation createScreenFoldAnimation(boolean isEnter, int fromFoldMode, int toFoldMode) {
        return null;
    }

    public long getExitAnimDuration() {
        return 0;
    }

    private void createFoldProjectionAnimationIfNeed(int fromFoldMode, int toFoldMode) {
        if ((fromFoldMode == 1 && toFoldMode == 2) || (fromFoldMode == 2 && toFoldMode == 1)) {
            if (!this.mService.mIsPerfBoost) {
                this.mService.mIsPerfBoost = true;
                UniPerf.getInstance().uniPerfEvent(4105, "", new int[]{0});
            }
            this.mFoldProjectionAnimation = createFoldProjectionAnimation(fromFoldMode, toFoldMode);
        }
    }

    private void initializeFoldProjectionAnimation(int finalWidth, int finalHeight) {
        Animation animation = this.mFoldProjectionAnimation;
        if (animation != null) {
            animation.initialize(finalWidth, finalHeight, this.mOriginalWidth, this.mOriginalHeight);
        }
    }

    private void setFoldProjectionAnimationDuration(long maxAnimationDuration, float animationScale) {
        Animation animation = this.mFoldProjectionAnimation;
        if (animation != null) {
            animation.restrictDuration(maxAnimationDuration);
            this.mFoldProjectionAnimation.scaleCurrentDuration(animationScale);
        }
    }

    private void updateMoreFoldProjection(long now) {
        this.mMoreFoldProjection = false;
        Animation animation = this.mFoldProjectionAnimation;
        if (animation != null) {
            this.mMoreFoldProjection = animation.getTransformation(now, this.mFoldProjectionTransformation);
        }
    }

    private void clearFoldProjectionAnimation() {
        Animation animation;
        if (!this.mMoreFoldProjection && (animation = this.mFoldProjectionAnimation) != null) {
            animation.cancel();
            this.mFoldProjectionAnimation = null;
            this.mFoldProjectionTransformation.clear();
            if (this.mService.mIsPerfBoost) {
                this.mService.mIsPerfBoost = false;
                UniPerf.getInstance().uniPerfEvent(4105, "", new int[]{-1});
            }
        }
    }

    /* access modifiers changed from: protected */
    public Animation createFoldProjectionAnimation(int fromFoldMode, int toFoldMode) {
        return null;
    }

    /* access modifiers changed from: protected */
    public void updateFoldProjection() {
    }

    public void setIsSingleHandScreenShotAnim(boolean isSingleHandScreenShotAnim) {
        this.mIsSingleHandScreenShotAnim = isSingleHandScreenShotAnim;
    }
}
