package com.android.server.wm;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.SurfaceControl;
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
            BlackFrame blackFrame = this.mExitingBlackFrame;
            blackFrame.printTo(prefix + "  ", pw);
        }
        pw.print(prefix);
        pw.print("mEnteringBlackFrame=");
        pw.println(this.mEnteringBlackFrame);
        if (this.mEnteringBlackFrame != null) {
            BlackFrame blackFrame2 = this.mEnteringBlackFrame;
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

    public ScreenRotationAnimation(Context context, DisplayContent displayContent, boolean forceDefaultOrientation, boolean isSecure, WindowManagerService service) {
        int originalHeight;
        int originalWidth;
        DisplayContent displayContent2 = displayContent;
        this.mService = service;
        this.mContext = context;
        this.mDisplayContent = displayContent2;
        displayContent2.getBounds(this.mOriginalDisplayRect);
        int originalRotation = displayContent.getDisplay().getRotation();
        DisplayInfo displayInfo = displayContent.getDisplayInfo();
        if (forceDefaultOrientation) {
            this.mForceDefaultOrientation = true;
            originalWidth = displayContent2.mBaseDisplayWidth;
            originalHeight = displayContent2.mBaseDisplayHeight;
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
        SurfaceControl.Transaction t = new SurfaceControl.Transaction();
        try {
            try {
                this.mSurfaceControl = displayContent.makeOverlay().setName("ScreenshotSurface").setSize(this.mWidth, this.mHeight).setSecure(isSecure).build();
                IBinder displayHandle = SurfaceControl.getBuiltInDisplay(0);
                if (displayHandle != null) {
                    Surface sur = new Surface();
                    sur.copyFrom(this.mSurfaceControl);
                    SurfaceControl.screenshot_ext_hw(displayHandle, sur);
                    IBinder iBinder = displayHandle;
                    t.setLayer(this.mSurfaceControl, SCREEN_FREEZE_LAYER_SCREENSHOT);
                    t.setAlpha(this.mSurfaceControl, 0.0f);
                    t.show(this.mSurfaceControl);
                    sur.destroy();
                } else {
                    Slog.w(TAG, "Built-in display 0 is null.");
                }
            } catch (Surface.OutOfResourcesException e) {
                e = e;
                Slog.w(TAG, "Unable to allocate freeze surface", e);
                setRotation(t, originalRotation);
                t.apply();
            }
        } catch (Surface.OutOfResourcesException e2) {
            e = e2;
            boolean z = isSecure;
            Slog.w(TAG, "Unable to allocate freeze surface", e);
            setRotation(t, originalRotation);
            t.apply();
        }
        setRotation(t, originalRotation);
        t.apply();
    }

    /* access modifiers changed from: package-private */
    public boolean hasScreenshot() {
        return this.mSurfaceControl != null;
    }

    private void setSnapshotTransform(SurfaceControl.Transaction t, Matrix matrix, float alpha) {
        if (this.mSurfaceControl != null) {
            matrix.getValues(this.mTmpFloats);
            float x = this.mTmpFloats[2];
            float y = this.mTmpFloats[5];
            if (this.mForceDefaultOrientation) {
                this.mDisplayContent.getBounds(this.mCurrentDisplayRect);
                x -= (float) this.mCurrentDisplayRect.left;
                y -= (float) this.mCurrentDisplayRect.top;
            }
            t.setPosition(this.mSurfaceControl, x, y);
            t.setMatrix(this.mSurfaceControl, this.mTmpFloats[0], this.mTmpFloats[3], this.mTmpFloats[1], this.mTmpFloats[4]);
            t.setAlpha(this.mSurfaceControl, alpha);
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

    private void setRotation(SurfaceControl.Transaction t, int rotation) {
        this.mCurRotation = rotation;
        createRotationMatrix(DisplayContent.deltaRotation(rotation, 0), this.mWidth, this.mHeight, this.mSnapshotInitialMatrix);
        setSnapshotTransform(t, this.mSnapshotInitialMatrix, 1.0f);
    }

    public boolean setRotation(SurfaceControl.Transaction t, int rotation, long maxAnimationDuration, float animationScale, int finalWidth, int finalHeight) {
        setRotation(t, rotation);
        if (this.TWO_PHASE_ANIMATION) {
            return startAnimation(t, maxAnimationDuration, animationScale, finalWidth, finalHeight, false, 0, 0);
        }
        return false;
    }

    private boolean startAnimation(SurfaceControl.Transaction t, long maxAnimationDuration, float animationScale, int finalWidth, int finalHeight, boolean dismissing, int exitAnim, int enterAnim) {
        boolean customAnim;
        SurfaceControl.Transaction transaction;
        Rect outer;
        Rect inner;
        long j = maxAnimationDuration;
        float f = animationScale;
        int i = finalWidth;
        int i2 = finalHeight;
        int i3 = exitAnim;
        int i4 = enterAnim;
        if (this.mSurfaceControl == null) {
            return false;
        }
        if (this.mStarted) {
            return true;
        }
        this.mStarted = true;
        boolean firstStart = false;
        Log.d(TAG, "startAnimation begin");
        int delta = DisplayContent.deltaRotation(this.mCurRotation, this.mOriginalRotation);
        if (this.TWO_PHASE_ANIMATION && this.mFinishExitAnimation == null && (!dismissing || delta != 0)) {
            firstStart = true;
            this.mStartExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432714);
            this.mStartEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432713);
            this.mFinishExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432705);
            this.mFinishEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432704);
        }
        boolean firstStart2 = firstStart;
        if (i3 == 0 || i4 == 0) {
            customAnim = false;
            Context hwextContext = null;
            try {
                hwextContext = this.mContext.createPackageContext("androidhwext", 0);
            } catch (PackageManager.NameNotFoundException e) {
            }
            if (hwextContext != null) {
                int rotateExitAnimationId = hwextContext.getResources().getIdentifier("screen_rotate_" + delta + "_exit", "anim", "androidhwext");
                int rotateEnterAnimationId = hwextContext.getResources().getIdentifier("screen_rotate_" + delta + "_enter", "anim", "androidhwext");
                if (!(rotateExitAnimationId == 0 || rotateEnterAnimationId == 0)) {
                    this.mRotateExitAnimation = AnimationUtils.loadAnimation(hwextContext, rotateExitAnimationId);
                    this.mRotateEnterAnimation = AnimationUtils.loadAnimation(hwextContext, rotateEnterAnimationId);
                }
                int i5 = rotateExitAnimationId;
            }
            if (this.mRotateExitAnimation == null || this.mRotateEnterAnimation == null) {
                switch (delta) {
                    case 0:
                        this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432699);
                        this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432698);
                        break;
                    case 1:
                        this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432711);
                        this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432710);
                        break;
                    case 2:
                        this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432702);
                        this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432701);
                        break;
                    case 3:
                        this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432708);
                        this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432707);
                        break;
                }
            }
        } else {
            this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, i3);
            this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, i4);
            customAnim = true;
        }
        if (this.TWO_PHASE_ANIMATION && firstStart2) {
            int halfWidth = (this.mOriginalWidth + i) / 2;
            int halfHeight = (this.mOriginalHeight + i2) / 2;
            this.mStartEnterAnimation.initialize(i, i2, halfWidth, halfHeight);
            this.mStartExitAnimation.initialize(halfWidth, halfHeight, this.mOriginalWidth, this.mOriginalHeight);
            this.mFinishEnterAnimation.initialize(i, i2, halfWidth, halfHeight);
            this.mFinishExitAnimation.initialize(halfWidth, halfHeight, this.mOriginalWidth, this.mOriginalHeight);
        }
        this.mRotateEnterAnimation.initialize(i, i2, this.mOriginalWidth, this.mOriginalHeight);
        this.mRotateExitAnimation.initialize(i, i2, this.mOriginalWidth, this.mOriginalHeight);
        this.mAnimRunning = false;
        this.mFinishAnimReady = false;
        this.mFinishAnimStartTime = -1;
        if (this.TWO_PHASE_ANIMATION && firstStart2) {
            this.mStartExitAnimation.restrictDuration(j);
            this.mStartExitAnimation.scaleCurrentDuration(f);
            this.mStartEnterAnimation.restrictDuration(j);
            this.mStartEnterAnimation.scaleCurrentDuration(f);
            this.mFinishExitAnimation.restrictDuration(j);
            this.mFinishExitAnimation.scaleCurrentDuration(f);
            this.mFinishEnterAnimation.restrictDuration(j);
            this.mFinishEnterAnimation.scaleCurrentDuration(f);
        }
        this.mRotateExitAnimation.restrictDuration(j);
        this.mRotateExitAnimation.scaleCurrentDuration(f);
        this.mRotateEnterAnimation.restrictDuration(j);
        this.mRotateEnterAnimation.scaleCurrentDuration(f);
        int layerStack = this.mDisplayContent.getDisplay().getLayerStack();
        if (customAnim || this.mExitingBlackFrame != null) {
            transaction = t;
        } else {
            try {
                createRotationMatrix(delta, this.mOriginalWidth, this.mOriginalHeight, this.mFrameInitialMatrix);
                if (this.mForceDefaultOrientation) {
                    outer = this.mCurrentDisplayRect;
                    inner = this.mOriginalDisplayRect;
                } else {
                    outer = new Rect((-this.mOriginalWidth) * 1, (-this.mOriginalHeight) * 1, this.mOriginalWidth * 2, this.mOriginalHeight * 2);
                    inner = new Rect(0, 0, this.mOriginalWidth, this.mOriginalHeight);
                }
                BlackFrame blackFrame = new BlackFrame(t, outer, inner, SCREEN_FREEZE_LAYER_EXIT, this.mDisplayContent, this.mForceDefaultOrientation);
                this.mExitingBlackFrame = blackFrame;
                transaction = t;
                try {
                    this.mExitingBlackFrame.setMatrix(transaction, this.mFrameInitialMatrix);
                } catch (Surface.OutOfResourcesException e2) {
                    e = e2;
                }
            } catch (Surface.OutOfResourcesException e3) {
                e = e3;
                transaction = t;
                Slog.w(TAG, "Unable to allocate black surface", e);
                try {
                    BlackFrame blackFrame2 = new BlackFrame(transaction, new Rect((-i) * 1, (-i2) * 1, i * 2, i2 * 2), new Rect(0, 0, i, i2), 2010000, this.mDisplayContent, false);
                    this.mEnteringBlackFrame = blackFrame2;
                } catch (Surface.OutOfResourcesException e4) {
                    Slog.w(TAG, "Unable to allocate black surface", e4);
                }
                Log.d(TAG, "startAnimation end");
                return true;
            }
        }
        if (customAnim && this.mEnteringBlackFrame == null) {
            BlackFrame blackFrame22 = new BlackFrame(transaction, new Rect((-i) * 1, (-i2) * 1, i * 2, i2 * 2), new Rect(0, 0, i, i2), 2010000, this.mDisplayContent, false);
            this.mEnteringBlackFrame = blackFrame22;
        }
        Log.d(TAG, "startAnimation end");
        return true;
    }

    public boolean dismiss(SurfaceControl.Transaction t, long maxAnimationDuration, float animationScale, int finalWidth, int finalHeight, int exitAnim, int enterAnim) {
        if (this.mSurfaceControl == null) {
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
        return hasAnimations() || (this.TWO_PHASE_ANIMATION && this.mFinishAnimReady);
    }

    public boolean isRotating() {
        return this.mCurRotation != this.mOriginalRotation;
    }

    private boolean hasAnimations() {
        return ((!this.TWO_PHASE_ANIMATION || (this.mStartEnterAnimation == null && this.mStartExitAnimation == null && this.mFinishEnterAnimation == null && this.mFinishExitAnimation == null)) && this.mRotateEnterAnimation == null && this.mRotateExitAnimation == null) ? false : true;
    }

    private boolean stepAnimation(long now) {
        if (now > this.mHalfwayPoint) {
            this.mHalfwayPoint = JobStatus.NO_LATEST_RUNTIME;
        }
        if (this.mFinishAnimReady && this.mFinishAnimStartTime < 0) {
            this.mFinishAnimStartTime = now;
        }
        boolean z = false;
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
        if (!this.mMoreRotateExit && (!this.TWO_PHASE_ANIMATION || (!this.mMoreStartExit && !this.mMoreFinishExit))) {
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
        if (!this.mMoreRotateEnter && (!this.TWO_PHASE_ANIMATION || (!this.mMoreStartEnter && !this.mMoreFinishEnter))) {
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
        if ((this.TWO_PHASE_ANIMATION && (this.mMoreStartEnter || this.mMoreStartExit || this.mMoreFinishEnter || this.mMoreFinishExit)) || this.mMoreRotateEnter || this.mMoreRotateExit || !this.mFinishAnimReady) {
            z = true;
        }
        boolean more = z;
        this.mSnapshotFinalMatrix.setConcat(this.mExitTransformation.getMatrix(), this.mSnapshotInitialMatrix);
        return more;
    }

    /* access modifiers changed from: package-private */
    public void updateSurfaces(SurfaceControl.Transaction t) {
        if (this.mStarted) {
            if (this.mSurfaceControl != null && !this.mMoreStartExit && !this.mMoreFinishExit && !this.mMoreRotateExit) {
                t.hide(this.mSurfaceControl);
            }
            if (this.mCustomBlackFrame != null) {
                if (this.mMoreStartFrame || this.mMoreFinishFrame || this.mMoreRotateFrame) {
                    this.mCustomBlackFrame.setMatrix(t, this.mFrameTransformation.getMatrix());
                } else {
                    this.mCustomBlackFrame.hide(t);
                }
            }
            if (this.mExitingBlackFrame != null) {
                if (this.mMoreStartExit || this.mMoreFinishExit || this.mMoreRotateExit) {
                    this.mExitFrameFinalMatrix.setConcat(this.mExitTransformation.getMatrix(), this.mFrameInitialMatrix);
                    this.mExitingBlackFrame.setMatrix(t, this.mExitFrameFinalMatrix);
                    if (this.mForceDefaultOrientation) {
                        this.mExitingBlackFrame.setAlpha(t, this.mExitTransformation.getAlpha());
                    }
                } else {
                    this.mExitingBlackFrame.hide(t);
                }
            }
            if (this.mEnteringBlackFrame != null) {
                if (this.mMoreStartEnter || this.mMoreFinishEnter || this.mMoreRotateEnter) {
                    this.mEnteringBlackFrame.setMatrix(t, this.mEnterTransformation.getMatrix());
                } else {
                    this.mEnteringBlackFrame.hide(t);
                }
            }
            setSnapshotTransform(t, this.mSnapshotFinalMatrix, this.mExitTransformation.getAlpha());
        }
    }

    public boolean stepAnimationLocked(long now) {
        if (!hasAnimations()) {
            this.mFinishAnimReady = false;
            return false;
        }
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

    public Transformation getEnterTransformation() {
        return this.mEnterTransformation;
    }
}
