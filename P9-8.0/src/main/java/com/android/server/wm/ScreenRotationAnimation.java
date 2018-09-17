package com.android.server.wm;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import com.android.server.job.controllers.JobStatus;
import java.io.PrintWriter;

class ScreenRotationAnimation {
    static final boolean DEBUG_HWANIMATION = false;
    static final boolean DEBUG_STATE = false;
    static final boolean DEBUG_TRANSFORMS = false;
    static final boolean HISI_ROT_ANI_OPT = SystemProperties.getBoolean("build.hisi_rot_ani_opt", false);
    static final int LANDSCAPE_OK = 0;
    static final int SCREEN_FREEZE_LAYER_BASE = 2010000;
    static final int SCREEN_FREEZE_LAYER_CUSTOM = 2010003;
    static final int SCREEN_FREEZE_LAYER_ENTER = 2010000;
    static final int SCREEN_FREEZE_LAYER_EXIT = 2010002;
    static final int SCREEN_FREEZE_LAYER_SCREENSHOT = 2010001;
    static final String TAG = "WindowManager";
    static final boolean USE_CUSTOM_BLACK_FRAME = false;
    boolean TWO_PHASE_ANIMATION = false;
    boolean mAnimRunning;
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
    boolean mForceDefaultOrientation;
    final Matrix mFrameInitialMatrix = new Matrix();
    final Transformation mFrameTransformation = new Transformation();
    long mHalfwayPoint;
    int mHeight;
    private int mIsLandScape;
    Animation mLastRotateEnterAnimation;
    final Transformation mLastRotateEnterTransformation = new Transformation();
    Animation mLastRotateExitAnimation;
    final Transformation mLastRotateExitTransformation = new Transformation();
    Animation mLastRotateFrameAnimation;
    final Transformation mLastRotateFrameTransformation = new Transformation();
    private boolean mMoreFinishEnter;
    private boolean mMoreFinishExit;
    private boolean mMoreFinishFrame;
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
    int mWidth;

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
        if (this.mExitingBlackFrame != null) {
            this.mExitingBlackFrame.printTo(prefix + "  ", pw);
        }
        pw.print(prefix);
        pw.print("mEnteringBlackFrame=");
        pw.println(this.mEnteringBlackFrame);
        if (this.mEnteringBlackFrame != null) {
            this.mEnteringBlackFrame.printTo(prefix + "  ", pw);
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
        this.mEnterTransformation.printShortString(pw);
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

    public ScreenRotationAnimation(Context context, DisplayContent displayContent, SurfaceSession session, boolean inTransaction, boolean forceDefaultOrientation, boolean isSecure, WindowManagerService service) {
        int originalWidth;
        int originalHeight;
        this.mService = service;
        this.mContext = context;
        this.mDisplayContent = displayContent;
        displayContent.getLogicalDisplayRect(this.mOriginalDisplayRect);
        Display display = displayContent.getDisplay();
        int originalRotation = display.getRotation();
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        if (forceDefaultOrientation) {
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
        this.mOriginalRotation = originalRotation;
        this.mOriginalWidth = originalWidth;
        this.mOriginalHeight = originalHeight;
        this.TWO_PHASE_ANIMATION = false;
        if (!inTransaction) {
            this.mService.openSurfaceTransaction();
        }
        int flags = 4;
        if (isSecure) {
            flags = 132;
        }
        try {
            this.mSurfaceControl = new SurfaceControl(session, "ScreenshotSurface", this.mWidth, this.mHeight, -1, flags);
            Surface sur = new Surface();
            sur.copyFrom(this.mSurfaceControl);
            SurfaceControl.screenshot_ext_hw(SurfaceControl.getBuiltInDisplay(0), sur);
            this.mSurfaceControl.setLayerStack(display.getLayerStack());
            this.mSurfaceControl.setLayer(SCREEN_FREEZE_LAYER_SCREENSHOT);
            this.mSurfaceControl.setAlpha(0.0f);
            this.mSurfaceControl.show();
            sur.destroy();
        } catch (OutOfResourcesException e) {
            Slog.w(TAG, "Unable to allocate freeze surface", e);
        } catch (Throwable th) {
            if (!inTransaction) {
                this.mService.closeSurfaceTransaction();
            }
        }
        setRotationInTransaction(originalRotation);
        if (!inTransaction) {
            this.mService.closeSurfaceTransaction();
        }
    }

    boolean hasScreenshot() {
        return this.mSurfaceControl != null;
    }

    private void setSnapshotTransformInTransaction(Matrix matrix, float alpha) {
        if (this.mSurfaceControl != null) {
            matrix.getValues(this.mTmpFloats);
            float x = this.mTmpFloats[2];
            float y = this.mTmpFloats[5];
            if (this.mForceDefaultOrientation) {
                this.mDisplayContent.getLogicalDisplayRect(this.mCurrentDisplayRect);
                x -= (float) this.mCurrentDisplayRect.left;
                y -= (float) this.mCurrentDisplayRect.top;
            }
            this.mSurfaceControl.setPosition(x, y);
            this.mSurfaceControl.setMatrix(this.mTmpFloats[0], this.mTmpFloats[3], this.mTmpFloats[1], this.mTmpFloats[4]);
            this.mSurfaceControl.setAlpha(alpha);
        }
    }

    public static void createRotationMatrix(int rotation, int width, int height, Matrix outMatrix) {
        switch (rotation) {
            case 0:
                outMatrix.reset();
                return;
            case 1:
                outMatrix.setRotate(90.0f, 0.0f, 0.0f);
                outMatrix.postTranslate((float) height, 0.0f);
                return;
            case 2:
                outMatrix.setRotate(180.0f, 0.0f, 0.0f);
                outMatrix.postTranslate((float) width, (float) height);
                return;
            case 3:
                outMatrix.setRotate(270.0f, 0.0f, 0.0f);
                outMatrix.postTranslate(0.0f, (float) width);
                return;
            default:
                return;
        }
    }

    private void setRotationInTransaction(int rotation) {
        this.mCurRotation = rotation;
        createRotationMatrix(DisplayContent.deltaRotation(rotation, 0), this.mWidth, this.mHeight, this.mSnapshotInitialMatrix);
        setSnapshotTransformInTransaction(this.mSnapshotInitialMatrix, 1.0f);
    }

    public boolean setRotationInTransaction(int rotation, SurfaceSession session, long maxAnimationDuration, float animationScale, int finalWidth, int finalHeight) {
        setRotationInTransaction(rotation);
        if (this.TWO_PHASE_ANIMATION) {
            return startAnimation(session, maxAnimationDuration, animationScale, finalWidth, finalHeight, false, 0, 0);
        }
        return false;
    }

    private boolean startAnimation(SurfaceSession session, long maxAnimationDuration, float animationScale, int finalWidth, int finalHeight, boolean dismissing, int exitAnim, int enterAnim) {
        if (this.mSurfaceControl == null) {
            return false;
        }
        if (this.mStarted) {
            return true;
        }
        boolean customAnim;
        this.mStarted = true;
        boolean firstStart = false;
        Log.d(TAG, "startAnimation begin");
        int delta = DisplayContent.deltaRotation(this.mCurRotation, this.mOriginalRotation);
        if (this.TWO_PHASE_ANIMATION && this.mFinishExitAnimation == null && !(dismissing && delta == 0)) {
            firstStart = true;
            this.mStartExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432704);
            this.mStartEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432703);
            this.mFinishExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432695);
            this.mFinishEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432694);
        }
        if (exitAnim == 0 || enterAnim == 0) {
            customAnim = false;
            Context hwextContext = null;
            try {
                hwextContext = this.mContext.createPackageContext("androidhwext", 0);
            } catch (NameNotFoundException e) {
            }
            if (hwextContext != null) {
                int rotateExitAnimationId = hwextContext.getResources().getIdentifier("screen_rotate_" + delta + "_exit", "anim", "androidhwext");
                int rotateEnterAnimationId = hwextContext.getResources().getIdentifier("screen_rotate_" + delta + "_enter", "anim", "androidhwext");
                if (!(rotateExitAnimationId == 0 || rotateEnterAnimationId == 0)) {
                    this.mRotateExitAnimation = AnimationUtils.loadAnimation(hwextContext, rotateExitAnimationId);
                    this.mRotateEnterAnimation = AnimationUtils.loadAnimation(hwextContext, rotateEnterAnimationId);
                }
            }
            if (this.mRotateExitAnimation == null || this.mRotateEnterAnimation == null) {
                switch (delta) {
                    case 0:
                        this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432689);
                        this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432688);
                        break;
                    case 1:
                        this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432701);
                        this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432700);
                        break;
                    case 2:
                        this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432692);
                        this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432691);
                        break;
                    case 3:
                        this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432698);
                        this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432697);
                        break;
                }
            }
        }
        customAnim = true;
        this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, exitAnim);
        this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, enterAnim);
        if (this.TWO_PHASE_ANIMATION && firstStart) {
            int halfWidth = (this.mOriginalWidth + finalWidth) / 2;
            int halfHeight = (this.mOriginalHeight + finalHeight) / 2;
            this.mStartEnterAnimation.initialize(finalWidth, finalHeight, halfWidth, halfHeight);
            this.mStartExitAnimation.initialize(halfWidth, halfHeight, this.mOriginalWidth, this.mOriginalHeight);
            this.mFinishEnterAnimation.initialize(finalWidth, finalHeight, halfWidth, halfHeight);
            this.mFinishExitAnimation.initialize(halfWidth, halfHeight, this.mOriginalWidth, this.mOriginalHeight);
        }
        this.mRotateEnterAnimation.initialize(finalWidth, finalHeight, this.mOriginalWidth, this.mOriginalHeight);
        this.mRotateExitAnimation.initialize(finalWidth, finalHeight, this.mOriginalWidth, this.mOriginalHeight);
        this.mAnimRunning = false;
        this.mFinishAnimReady = false;
        this.mFinishAnimStartTime = -1;
        if (this.TWO_PHASE_ANIMATION && firstStart) {
            this.mStartExitAnimation.restrictDuration(maxAnimationDuration);
            this.mStartExitAnimation.scaleCurrentDuration(animationScale);
            this.mStartEnterAnimation.restrictDuration(maxAnimationDuration);
            this.mStartEnterAnimation.scaleCurrentDuration(animationScale);
            this.mFinishExitAnimation.restrictDuration(maxAnimationDuration);
            this.mFinishExitAnimation.scaleCurrentDuration(animationScale);
            this.mFinishEnterAnimation.restrictDuration(maxAnimationDuration);
            this.mFinishEnterAnimation.scaleCurrentDuration(animationScale);
        }
        this.mRotateExitAnimation.restrictDuration(maxAnimationDuration);
        this.mRotateExitAnimation.scaleCurrentDuration(animationScale);
        this.mRotateEnterAnimation.restrictDuration(maxAnimationDuration);
        this.mRotateEnterAnimation.scaleCurrentDuration(animationScale);
        int layerStack = this.mDisplayContent.getDisplay().getLayerStack();
        if (!customAnim && this.mExitingBlackFrame == null) {
            this.mService.openSurfaceTransaction();
            try {
                Rect outer;
                Rect inner;
                createRotationMatrix(delta, this.mOriginalWidth, this.mOriginalHeight, this.mFrameInitialMatrix);
                if (this.mForceDefaultOrientation) {
                    outer = this.mCurrentDisplayRect;
                    inner = this.mOriginalDisplayRect;
                } else {
                    outer = new Rect((-this.mOriginalWidth) * 1, (-this.mOriginalHeight) * 1, this.mOriginalWidth * 2, this.mOriginalHeight * 2);
                    inner = new Rect(0, 0, this.mOriginalWidth, this.mOriginalHeight);
                }
                this.mExitingBlackFrame = new BlackFrame(session, outer, inner, SCREEN_FREEZE_LAYER_EXIT, layerStack, this.mForceDefaultOrientation);
                this.mExitingBlackFrame.setMatrix(this.mFrameInitialMatrix);
            } catch (OutOfResourcesException e2) {
                Slog.w(TAG, "Unable to allocate black surface", e2);
            } finally {
                this.mService.closeSurfaceTransaction();
            }
        }
        if (customAnim && this.mEnteringBlackFrame == null) {
            this.mService.openSurfaceTransaction();
            try {
                this.mEnteringBlackFrame = new BlackFrame(session, new Rect((-finalWidth) * 1, (-finalHeight) * 1, finalWidth * 2, finalHeight * 2), new Rect(0, 0, finalWidth, finalHeight), 2010000, layerStack, false);
            } catch (OutOfResourcesException e22) {
                Slog.w(TAG, "Unable to allocate black surface", e22);
            } finally {
                this.mService.closeSurfaceTransaction();
            }
        }
        Log.d(TAG, "startAnimation end");
        return true;
    }

    public boolean dismiss(SurfaceSession session, long maxAnimationDuration, float animationScale, int finalWidth, int finalHeight, int exitAnim, int enterAnim) {
        if (this.mSurfaceControl == null) {
            return false;
        }
        if (!this.mStarted) {
            startAnimation(session, maxAnimationDuration, animationScale, finalWidth, finalHeight, true, exitAnim, enterAnim);
        }
        if (!this.mStarted) {
            return false;
        }
        this.mFinishAnimReady = true;
        return true;
    }

    public void kill() {
        if (this.mSurfaceControl != null) {
            this.mSurfaceControl.destroy();
            this.mSurfaceControl = null;
        }
        if (this.mCustomBlackFrame != null) {
            this.mCustomBlackFrame.kill();
            this.mCustomBlackFrame = null;
        }
        if (this.mExitingBlackFrame != null) {
            this.mExitingBlackFrame.kill();
            this.mExitingBlackFrame = null;
        }
        if (this.mEnteringBlackFrame != null) {
            this.mEnteringBlackFrame.kill();
            this.mEnteringBlackFrame = null;
        }
        if (this.TWO_PHASE_ANIMATION) {
            if (this.mStartExitAnimation != null) {
                this.mStartExitAnimation.cancel();
                this.mStartExitAnimation = null;
            }
            if (this.mStartEnterAnimation != null) {
                this.mStartEnterAnimation.cancel();
                this.mStartEnterAnimation = null;
            }
            if (this.mFinishExitAnimation != null) {
                this.mFinishExitAnimation.cancel();
                this.mFinishExitAnimation = null;
            }
            if (this.mFinishEnterAnimation != null) {
                this.mFinishEnterAnimation.cancel();
                this.mFinishEnterAnimation = null;
            }
        }
        if (this.mRotateExitAnimation != null) {
            this.mRotateExitAnimation.cancel();
            this.mRotateExitAnimation = null;
        }
        if (this.mRotateEnterAnimation != null) {
            this.mRotateEnterAnimation.cancel();
            this.mRotateEnterAnimation = null;
        }
    }

    public boolean isAnimating() {
        if (hasAnimations()) {
            return true;
        }
        return this.TWO_PHASE_ANIMATION ? this.mFinishAnimReady : false;
    }

    public boolean isRotating() {
        return this.mCurRotation != this.mOriginalRotation;
    }

    private boolean hasAnimations() {
        if ((!this.TWO_PHASE_ANIMATION || (this.mStartEnterAnimation == null && this.mStartExitAnimation == null && this.mFinishEnterAnimation == null && this.mFinishExitAnimation == null)) && this.mRotateEnterAnimation == null && this.mRotateExitAnimation == null) {
            return false;
        }
        return true;
    }

    private boolean stepAnimation(long now) {
        boolean more;
        if (now > this.mHalfwayPoint) {
            this.mHalfwayPoint = JobStatus.NO_LATEST_RUNTIME;
        }
        if (this.mFinishAnimReady && this.mFinishAnimStartTime < 0) {
            this.mFinishAnimStartTime = now;
        }
        if (this.TWO_PHASE_ANIMATION) {
            this.mMoreStartExit = false;
            if (this.mStartExitAnimation != null) {
                this.mMoreStartExit = this.mStartExitAnimation.getTransformation(now, this.mStartExitTransformation);
            }
            this.mMoreStartEnter = false;
            if (this.mStartEnterAnimation != null) {
                this.mMoreStartEnter = this.mStartEnterAnimation.getTransformation(now, this.mStartEnterTransformation);
            }
        }
        long finishNow = this.mFinishAnimReady ? now - this.mFinishAnimStartTime : 0;
        if (this.TWO_PHASE_ANIMATION) {
            this.mMoreFinishExit = false;
            if (this.mFinishExitAnimation != null) {
                this.mMoreFinishExit = this.mFinishExitAnimation.getTransformation(finishNow, this.mFinishExitTransformation);
            }
            this.mMoreFinishEnter = false;
            if (this.mFinishEnterAnimation != null) {
                this.mMoreFinishEnter = this.mFinishEnterAnimation.getTransformation(finishNow, this.mFinishEnterTransformation);
            }
        }
        this.mMoreRotateExit = false;
        if (this.mRotateExitAnimation != null) {
            this.mMoreRotateExit = this.mRotateExitAnimation.getTransformation(now, this.mRotateExitTransformation);
        }
        this.mMoreRotateEnter = false;
        if (this.mRotateEnterAnimation != null) {
            this.mMoreRotateEnter = this.mRotateEnterAnimation.getTransformation(now, this.mRotateEnterTransformation);
        }
        if (!(this.mMoreRotateExit || (this.TWO_PHASE_ANIMATION && (this.mMoreStartExit || (this.mMoreFinishExit ^ 1) == 0)))) {
            if (this.TWO_PHASE_ANIMATION) {
                if (this.mStartExitAnimation != null) {
                    this.mStartExitAnimation.cancel();
                    this.mStartExitAnimation = null;
                    this.mStartExitTransformation.clear();
                }
                if (this.mFinishExitAnimation != null) {
                    this.mFinishExitAnimation.cancel();
                    this.mFinishExitAnimation = null;
                    this.mFinishExitTransformation.clear();
                }
            }
            if (this.mRotateExitAnimation != null) {
                this.mRotateExitAnimation.cancel();
                this.mRotateExitAnimation = null;
                this.mRotateExitTransformation.clear();
            }
        }
        if (!(this.mMoreRotateEnter || (this.TWO_PHASE_ANIMATION && (this.mMoreStartEnter || (this.mMoreFinishEnter ^ 1) == 0)))) {
            if (this.TWO_PHASE_ANIMATION) {
                if (this.mStartEnterAnimation != null) {
                    this.mStartEnterAnimation.cancel();
                    this.mStartEnterAnimation = null;
                    this.mStartEnterTransformation.clear();
                }
                if (this.mFinishEnterAnimation != null) {
                    this.mFinishEnterAnimation.cancel();
                    this.mFinishEnterAnimation = null;
                    this.mFinishEnterTransformation.clear();
                }
            }
            if (this.mRotateEnterAnimation != null) {
                this.mRotateEnterAnimation.cancel();
                this.mRotateEnterAnimation = null;
                this.mRotateEnterTransformation.clear();
            }
        }
        this.mExitTransformation.set(this.mRotateExitTransformation);
        this.mEnterTransformation.set(this.mRotateEnterTransformation);
        if (this.TWO_PHASE_ANIMATION && !HISI_ROT_ANI_OPT) {
            this.mExitTransformation.compose(this.mStartExitTransformation);
            this.mExitTransformation.compose(this.mFinishExitTransformation);
            this.mEnterTransformation.compose(this.mStartEnterTransformation);
            this.mEnterTransformation.compose(this.mFinishEnterTransformation);
        }
        if ((this.TWO_PHASE_ANIMATION && (this.mMoreStartEnter || this.mMoreStartExit || this.mMoreFinishEnter || this.mMoreFinishExit)) || this.mMoreRotateEnter || this.mMoreRotateExit) {
            more = true;
        } else {
            more = this.mFinishAnimReady ^ 1;
        }
        this.mSnapshotFinalMatrix.setConcat(this.mExitTransformation.getMatrix(), this.mSnapshotInitialMatrix);
        return more;
    }

    void updateSurfacesInTransaction() {
        if (this.mStarted) {
            if (!(this.mSurfaceControl == null || this.mMoreStartExit || (this.mMoreFinishExit ^ 1) == 0 || (this.mMoreRotateExit ^ 1) == 0)) {
                this.mSurfaceControl.hide();
            }
            if (this.mCustomBlackFrame != null) {
                if (this.mMoreStartFrame || (this.mMoreFinishFrame ^ 1) == 0 || (this.mMoreRotateFrame ^ 1) == 0) {
                    this.mCustomBlackFrame.setMatrix(this.mFrameTransformation.getMatrix());
                } else {
                    this.mCustomBlackFrame.hide();
                }
            }
            if (this.mExitingBlackFrame != null) {
                if (this.mMoreStartExit || (this.mMoreFinishExit ^ 1) == 0 || (this.mMoreRotateExit ^ 1) == 0) {
                    this.mExitFrameFinalMatrix.setConcat(this.mExitTransformation.getMatrix(), this.mFrameInitialMatrix);
                    this.mExitingBlackFrame.setMatrix(this.mExitFrameFinalMatrix);
                    if (this.mForceDefaultOrientation) {
                        this.mExitingBlackFrame.setAlpha(this.mExitTransformation.getAlpha());
                    }
                } else {
                    this.mExitingBlackFrame.hide();
                }
            }
            if (this.mEnteringBlackFrame != null) {
                if (this.mMoreStartEnter || (this.mMoreFinishEnter ^ 1) == 0 || (this.mMoreRotateEnter ^ 1) == 0) {
                    this.mEnteringBlackFrame.setMatrix(this.mEnterTransformation.getMatrix());
                } else {
                    this.mEnteringBlackFrame.hide();
                }
            }
            setSnapshotTransformInTransaction(this.mSnapshotFinalMatrix, this.mExitTransformation.getAlpha());
        }
    }

    public boolean stepAnimationLocked(long now) {
        if (hasAnimations()) {
            if (!this.mAnimRunning) {
                if (this.TWO_PHASE_ANIMATION) {
                    if (this.mStartEnterAnimation != null) {
                        this.mStartEnterAnimation.setStartTime(now);
                    }
                    if (this.mStartExitAnimation != null) {
                        this.mStartExitAnimation.setStartTime(now);
                    }
                    if (this.mFinishEnterAnimation != null) {
                        this.mFinishEnterAnimation.setStartTime(0);
                    }
                    if (this.mFinishExitAnimation != null) {
                        this.mFinishExitAnimation.setStartTime(0);
                    }
                }
                if (this.mRotateEnterAnimation != null) {
                    this.mRotateEnterAnimation.setStartTime(now);
                }
                if (this.mRotateExitAnimation != null) {
                    this.mRotateExitAnimation.setStartTime(now);
                }
                this.mAnimRunning = true;
                this.mHalfwayPoint = (this.mRotateEnterAnimation.getDuration() / 2) + now;
            }
            return stepAnimation(now);
        }
        this.mFinishAnimReady = false;
        return false;
    }

    public Transformation getEnterTransformation() {
        return this.mEnterTransformation;
    }
}
