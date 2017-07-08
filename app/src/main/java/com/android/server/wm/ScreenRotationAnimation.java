package com.android.server.wm;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.android.server.DisplayThread;
import com.android.server.job.controllers.JobStatus;
import com.hisi.perfhub.PerfHub;
import java.io.PrintWriter;

class ScreenRotationAnimation {
    static final boolean DEBUG_HWANIMATION = false;
    static final boolean DEBUG_STATE = false;
    static final boolean DEBUG_TRANSFORMS = false;
    static final boolean HISI_ROT_ANI_OPT = false;
    static final int LANDSCAPE_OK = 0;
    static final int SCREEN_FREEZE_LAYER_BASE = 2010000;
    static final int SCREEN_FREEZE_LAYER_CUSTOM = 2010003;
    static final int SCREEN_FREEZE_LAYER_ENTER = 2010000;
    static final int SCREEN_FREEZE_LAYER_EXIT = 2010002;
    static final int SCREEN_FREEZE_LAYER_SCREENSHOT = 2010001;
    static final String TAG = null;
    static final boolean USE_CUSTOM_BLACK_FRAME = false;
    boolean TWO_PHASE_ANIMATION;
    boolean mAnimRunning;
    final Context mContext;
    int mCurRotation;
    Rect mCurrentDisplayRect;
    BlackFrame mCustomBlackFrame;
    final DisplayContent mDisplayContent;
    final Transformation mEnterTransformation;
    BlackFrame mEnteringBlackFrame;
    final Matrix mExitFrameFinalMatrix;
    final Transformation mExitTransformation;
    BlackFrame mExitingBlackFrame;
    boolean mFinishAnimReady;
    long mFinishAnimStartTime;
    Animation mFinishEnterAnimation;
    final Transformation mFinishEnterTransformation;
    Animation mFinishExitAnimation;
    final Transformation mFinishExitTransformation;
    Animation mFinishFrameAnimation;
    final Transformation mFinishFrameTransformation;
    boolean mForceDefaultOrientation;
    final Matrix mFrameInitialMatrix;
    final Transformation mFrameTransformation;
    long mHalfwayPoint;
    final H mHandler;
    int mHeight;
    private int mIsLandScape;
    Animation mLastRotateEnterAnimation;
    final Transformation mLastRotateEnterTransformation;
    Animation mLastRotateExitAnimation;
    final Transformation mLastRotateExitTransformation;
    Animation mLastRotateFrameAnimation;
    final Transformation mLastRotateFrameTransformation;
    private boolean mMoreFinishEnter;
    private boolean mMoreFinishExit;
    private boolean mMoreFinishFrame;
    private boolean mMoreRotateEnter;
    private boolean mMoreRotateExit;
    private boolean mMoreRotateFrame;
    private boolean mMoreStartEnter;
    private boolean mMoreStartExit;
    private boolean mMoreStartFrame;
    Rect mOriginalDisplayRect;
    int mOriginalHeight;
    int mOriginalRotation;
    int mOriginalWidth;
    private PerfHub mPerfHub;
    Animation mRotateEnterAnimation;
    final Transformation mRotateEnterTransformation;
    Animation mRotateExitAnimation;
    final Transformation mRotateExitTransformation;
    Animation mRotateFrameAnimation;
    final Transformation mRotateFrameTransformation;
    final Matrix mSnapshotFinalMatrix;
    final Matrix mSnapshotInitialMatrix;
    Animation mStartEnterAnimation;
    final Transformation mStartEnterTransformation;
    Animation mStartExitAnimation;
    final Transformation mStartExitTransformation;
    Animation mStartFrameAnimation;
    final Transformation mStartFrameTransformation;
    boolean mStarted;
    SurfaceControl mSurfaceControl;
    final float[] mTmpFloats;
    final Matrix mTmpMatrix;
    int mWidth;

    final class H extends Handler {
        public static final int FREEZE_TIMEOUT_VAL = 6000;
        public static final int SCREENSHOT_FREEZE_TIMEOUT = 2;

        public H(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCREENSHOT_FREEZE_TIMEOUT /*2*/:
                    if (ScreenRotationAnimation.this.mSurfaceControl != null && ScreenRotationAnimation.this.isAnimating()) {
                        Slog.e(ScreenRotationAnimation.TAG, "Exceeded Freeze timeout. Destroy layers");
                        ScreenRotationAnimation.this.kill();
                    } else if (ScreenRotationAnimation.this.mSurfaceControl != null) {
                        Slog.e(ScreenRotationAnimation.TAG, "No animation, exceeded freeze timeout. Destroy Screenshot layer");
                        ScreenRotationAnimation.this.mSurfaceControl.destroy();
                        ScreenRotationAnimation.this.mSurfaceControl = null;
                    }
                default:
                    Slog.e(ScreenRotationAnimation.TAG, "No Valid Message To Handle");
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.ScreenRotationAnimation.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.ScreenRotationAnimation.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.ScreenRotationAnimation.<clinit>():void");
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

    public ScreenRotationAnimation(Context context, DisplayContent displayContent, SurfaceSession session, boolean inTransaction, boolean forceDefaultOrientation, boolean isSecure) {
        int originalWidth;
        int originalHeight;
        this.TWO_PHASE_ANIMATION = HISI_ROT_ANI_OPT;
        this.mOriginalDisplayRect = new Rect();
        this.mCurrentDisplayRect = new Rect();
        this.mStartExitTransformation = new Transformation();
        this.mStartEnterTransformation = new Transformation();
        this.mStartFrameTransformation = new Transformation();
        this.mFinishExitTransformation = new Transformation();
        this.mFinishEnterTransformation = new Transformation();
        this.mFinishFrameTransformation = new Transformation();
        this.mRotateExitTransformation = new Transformation();
        this.mRotateEnterTransformation = new Transformation();
        this.mRotateFrameTransformation = new Transformation();
        this.mLastRotateExitTransformation = new Transformation();
        this.mLastRotateEnterTransformation = new Transformation();
        this.mLastRotateFrameTransformation = new Transformation();
        this.mExitTransformation = new Transformation();
        this.mEnterTransformation = new Transformation();
        this.mFrameTransformation = new Transformation();
        this.mFrameInitialMatrix = new Matrix();
        this.mSnapshotInitialMatrix = new Matrix();
        this.mSnapshotFinalMatrix = new Matrix();
        this.mExitFrameFinalMatrix = new Matrix();
        this.mTmpMatrix = new Matrix();
        this.mTmpFloats = new float[9];
        this.mHandler = new H(DisplayThread.get().getLooper());
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
        this.TWO_PHASE_ANIMATION = HISI_ROT_ANI_OPT;
        if (HISI_ROT_ANI_OPT) {
            WindowList windows = displayContent.getWindowList();
            int i = windows.size() - 1;
            while (i >= 0) {
                WindowState win = (WindowState) windows.get(i);
                if (win.mAttrs.type == 3) {
                    this.mIsLandScape = -1;
                    if (this.mPerfHub == null) {
                        this.mPerfHub = new PerfHub();
                    }
                    this.mIsLandScape = this.mPerfHub.perfEvent(4103, "pkg_name=" + win.mAttrs.packageName, new int[LANDSCAPE_OK]);
                    if (this.mIsLandScape == 0) {
                        this.TWO_PHASE_ANIMATION = true;
                        Slog.v(TAG, "RotationAnimation optimize");
                    }
                } else {
                    i--;
                }
            }
        }
        if (!inTransaction) {
            SurfaceControl.openTransaction();
        }
        int flags = 4;
        if (isSecure) {
            flags = 132;
        }
        try {
            this.mSurfaceControl = new SurfaceControl(session, "ScreenshotSurface", this.mWidth, this.mHeight, -1, flags);
            Surface sur = new Surface();
            sur.copyFrom(this.mSurfaceControl);
            SurfaceControl.screenshot(SurfaceControl.getBuiltInDisplay(LANDSCAPE_OK), sur);
            this.mSurfaceControl.setLayerStack(display.getLayerStack());
            this.mSurfaceControl.setLayer(SCREEN_FREEZE_LAYER_SCREENSHOT);
            this.mSurfaceControl.setAlpha(0.0f);
            this.mSurfaceControl.show();
            sur.destroy();
            this.mHandler.removeMessages(2);
            this.mHandler.sendEmptyMessageDelayed(2, 6000);
        } catch (OutOfResourcesException e) {
            Slog.w(TAG, "Unable to allocate freeze surface", e);
        } catch (Throwable th) {
            if (!inTransaction) {
                SurfaceControl.closeTransaction();
            }
        }
        setRotationInTransaction(originalRotation);
        if (!inTransaction) {
            SurfaceControl.closeTransaction();
        }
    }

    boolean hasScreenshot() {
        return this.mSurfaceControl != null ? true : HISI_ROT_ANI_OPT;
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
            this.mSurfaceControl.setMatrix(this.mTmpFloats[LANDSCAPE_OK], this.mTmpFloats[3], this.mTmpFloats[1], this.mTmpFloats[4]);
            this.mSurfaceControl.setAlpha(alpha);
        }
    }

    public static void createRotationMatrix(int rotation, int width, int height, Matrix outMatrix) {
        switch (rotation) {
            case LANDSCAPE_OK /*0*/:
                outMatrix.reset();
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                outMatrix.setRotate(90.0f, 0.0f, 0.0f);
                outMatrix.postTranslate((float) height, 0.0f);
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                outMatrix.setRotate(180.0f, 0.0f, 0.0f);
                outMatrix.postTranslate((float) width, (float) height);
            case com.android.server.wm.WindowManagerService.H.REPORT_LOSING_FOCUS /*3*/:
                outMatrix.setRotate(270.0f, 0.0f, 0.0f);
                outMatrix.postTranslate(0.0f, (float) width);
            default:
        }
    }

    private void setRotationInTransaction(int rotation) {
        this.mCurRotation = rotation;
        createRotationMatrix(DisplayContent.deltaRotation(rotation, LANDSCAPE_OK), this.mWidth, this.mHeight, this.mSnapshotInitialMatrix);
        setSnapshotTransformInTransaction(this.mSnapshotInitialMatrix, 1.0f);
    }

    public boolean setRotationInTransaction(int rotation, SurfaceSession session, long maxAnimationDuration, float animationScale, int finalWidth, int finalHeight) {
        setRotationInTransaction(rotation);
        if (this.TWO_PHASE_ANIMATION) {
            return startAnimation(session, maxAnimationDuration, animationScale, finalWidth, finalHeight, HISI_ROT_ANI_OPT, LANDSCAPE_OK, LANDSCAPE_OK);
        }
        return HISI_ROT_ANI_OPT;
    }

    private boolean startAnimation(SurfaceSession session, long maxAnimationDuration, float animationScale, int finalWidth, int finalHeight, boolean dismissing, int exitAnim, int enterAnim) {
        if (this.mSurfaceControl == null) {
            return HISI_ROT_ANI_OPT;
        }
        if (this.mStarted) {
            return true;
        }
        this.mStarted = true;
        boolean firstStart = HISI_ROT_ANI_OPT;
        Log.d(TAG, "startAnimation begin");
        int delta = DisplayContent.deltaRotation(this.mCurRotation, this.mOriginalRotation);
        if (this.TWO_PHASE_ANIMATION && this.mFinishExitAnimation == null && !(dismissing && delta == 0)) {
            firstStart = true;
            this.mStartExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432702);
            this.mStartEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432701);
            this.mFinishExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432693);
            this.mFinishEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432692);
        }
        if (exitAnim == 0 || enterAnim == 0) {
            boolean customAnim = HISI_ROT_ANI_OPT;
            Context hwextContext = null;
            try {
                hwextContext = this.mContext.createPackageContext("androidhwext", LANDSCAPE_OK);
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
                    case LANDSCAPE_OK /*0*/:
                        this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432687);
                        this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432686);
                        break;
                    case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                        this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432699);
                        this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432698);
                        break;
                    case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                        this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432690);
                        this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432689);
                        break;
                    case com.android.server.wm.WindowManagerService.H.REPORT_LOSING_FOCUS /*3*/:
                        this.mRotateExitAnimation = AnimationUtils.loadAnimation(this.mContext, 17432696);
                        this.mRotateEnterAnimation = AnimationUtils.loadAnimation(this.mContext, 17432695);
                        break;
                    default:
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
        this.mAnimRunning = HISI_ROT_ANI_OPT;
        this.mFinishAnimReady = HISI_ROT_ANI_OPT;
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
            SurfaceControl.openTransaction();
            try {
                Rect outer;
                Rect inner;
                createRotationMatrix(delta, this.mOriginalWidth, this.mOriginalHeight, this.mFrameInitialMatrix);
                if (this.mForceDefaultOrientation) {
                    outer = this.mCurrentDisplayRect;
                    inner = this.mOriginalDisplayRect;
                } else {
                    outer = new Rect((-this.mOriginalWidth) * 1, (-this.mOriginalHeight) * 1, this.mOriginalWidth * 2, this.mOriginalHeight * 2);
                    inner = new Rect(LANDSCAPE_OK, LANDSCAPE_OK, this.mOriginalWidth, this.mOriginalHeight);
                }
                this.mExitingBlackFrame = new BlackFrame(session, outer, inner, SCREEN_FREEZE_LAYER_EXIT, layerStack, this.mForceDefaultOrientation);
                this.mExitingBlackFrame.setMatrix(this.mFrameInitialMatrix);
            } catch (OutOfResourcesException e2) {
                Slog.w(TAG, "Unable to allocate black surface", e2);
            } finally {
                SurfaceControl.closeTransaction();
            }
        }
        if (customAnim && this.mEnteringBlackFrame == null) {
            SurfaceControl.openTransaction();
            try {
                this.mEnteringBlackFrame = new BlackFrame(session, new Rect((-finalWidth) * 1, (-finalHeight) * 1, finalWidth * 2, finalHeight * 2), new Rect(LANDSCAPE_OK, LANDSCAPE_OK, finalWidth, finalHeight), SCREEN_FREEZE_LAYER_ENTER, layerStack, HISI_ROT_ANI_OPT);
            } catch (OutOfResourcesException e22) {
                Slog.w(TAG, "Unable to allocate black surface", e22);
            } finally {
                SurfaceControl.closeTransaction();
            }
        }
        Log.d(TAG, "startAnimation end");
        return true;
    }

    public boolean dismiss(SurfaceSession session, long maxAnimationDuration, float animationScale, int finalWidth, int finalHeight, int exitAnim, int enterAnim) {
        if (this.mSurfaceControl == null) {
            return HISI_ROT_ANI_OPT;
        }
        if (!this.mStarted) {
            startAnimation(session, maxAnimationDuration, animationScale, finalWidth, finalHeight, true, exitAnim, enterAnim);
        }
        if (!this.mStarted) {
            return HISI_ROT_ANI_OPT;
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
        return this.TWO_PHASE_ANIMATION ? this.mFinishAnimReady : HISI_ROT_ANI_OPT;
    }

    public boolean isRotating() {
        return this.mCurRotation != this.mOriginalRotation ? true : HISI_ROT_ANI_OPT;
    }

    private boolean hasAnimations() {
        if ((!this.TWO_PHASE_ANIMATION || (this.mStartEnterAnimation == null && this.mStartExitAnimation == null && this.mFinishEnterAnimation == null && this.mFinishExitAnimation == null)) && this.mRotateEnterAnimation == null && this.mRotateExitAnimation == null) {
            return HISI_ROT_ANI_OPT;
        }
        return true;
    }

    private boolean stepAnimation(long now) {
        if (now > this.mHalfwayPoint) {
            this.mHalfwayPoint = JobStatus.NO_LATEST_RUNTIME;
        }
        if (this.mFinishAnimReady && this.mFinishAnimStartTime < 0) {
            this.mFinishAnimStartTime = now;
        }
        if (this.TWO_PHASE_ANIMATION) {
            this.mMoreStartExit = HISI_ROT_ANI_OPT;
            if (this.mStartExitAnimation != null) {
                this.mMoreStartExit = this.mStartExitAnimation.getTransformation(now, this.mStartExitTransformation);
            }
            this.mMoreStartEnter = HISI_ROT_ANI_OPT;
            if (this.mStartEnterAnimation != null) {
                this.mMoreStartEnter = this.mStartEnterAnimation.getTransformation(now, this.mStartEnterTransformation);
            }
        }
        long finishNow = this.mFinishAnimReady ? now - this.mFinishAnimStartTime : 0;
        if (this.TWO_PHASE_ANIMATION) {
            this.mMoreFinishExit = HISI_ROT_ANI_OPT;
            if (this.mFinishExitAnimation != null) {
                this.mMoreFinishExit = this.mFinishExitAnimation.getTransformation(finishNow, this.mFinishExitTransformation);
            }
            this.mMoreFinishEnter = HISI_ROT_ANI_OPT;
            if (this.mFinishEnterAnimation != null) {
                this.mMoreFinishEnter = this.mFinishEnterAnimation.getTransformation(finishNow, this.mFinishEnterTransformation);
            }
        }
        this.mMoreRotateExit = HISI_ROT_ANI_OPT;
        if (this.mRotateExitAnimation != null) {
            this.mMoreRotateExit = this.mRotateExitAnimation.getTransformation(now, this.mRotateExitTransformation);
        }
        this.mMoreRotateEnter = HISI_ROT_ANI_OPT;
        if (this.mRotateEnterAnimation != null) {
            this.mMoreRotateEnter = this.mRotateEnterAnimation.getTransformation(now, this.mRotateEnterTransformation);
        }
        if (!(this.mMoreRotateExit || (this.TWO_PHASE_ANIMATION && (this.mMoreStartExit || this.mMoreFinishExit)))) {
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
        if (!(this.mMoreRotateEnter || (this.TWO_PHASE_ANIMATION && (this.mMoreStartEnter || this.mMoreFinishEnter)))) {
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
        boolean more = ((this.TWO_PHASE_ANIMATION && (this.mMoreStartEnter || this.mMoreStartExit || this.mMoreFinishEnter || this.mMoreFinishExit)) || this.mMoreRotateEnter || this.mMoreRotateExit) ? true : this.mFinishAnimReady ? HISI_ROT_ANI_OPT : true;
        this.mSnapshotFinalMatrix.setConcat(this.mExitTransformation.getMatrix(), this.mSnapshotInitialMatrix);
        return more;
    }

    void updateSurfacesInTransaction() {
        if (this.mStarted) {
            if (!(this.mSurfaceControl == null || this.mMoreStartExit || this.mMoreFinishExit || this.mMoreRotateExit)) {
                this.mSurfaceControl.hide();
            }
            if (this.mCustomBlackFrame != null) {
                if (this.mMoreStartFrame || this.mMoreFinishFrame || this.mMoreRotateFrame) {
                    this.mCustomBlackFrame.setMatrix(this.mFrameTransformation.getMatrix());
                } else {
                    this.mCustomBlackFrame.hide();
                }
            }
            if (this.mExitingBlackFrame != null) {
                if (this.mMoreStartExit || this.mMoreFinishExit || this.mMoreRotateExit) {
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
                if (this.mMoreStartEnter || this.mMoreFinishEnter || this.mMoreRotateEnter) {
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
        this.mFinishAnimReady = HISI_ROT_ANI_OPT;
        return HISI_ROT_ANI_OPT;
    }

    public Transformation getEnterTransformation() {
        return this.mEnterTransformation;
    }
}
