package com.android.server.wm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.hardware.display.HwFoldScreenState;
import android.os.Trace;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.android.server.pm.DumpState;
import com.android.server.policy.WindowManagerPolicy;
import java.io.PrintWriter;
import java.lang.annotation.RCUnownedRef;
import java.lang.annotation.RCUnownedThisRef;

public class WindowStateAnimator {
    static final int COMMIT_DRAW_PENDING = 2;
    static final int DRAW_PENDING = 1;
    static final int HAS_DRAWN = 4;
    static final int NO_SURFACE = 0;
    static final int READY_TO_SHOW = 3;
    static final int STACK_CLIP_AFTER_ANIM = 0;
    static final int STACK_CLIP_BEFORE_ANIM = 1;
    static final int STACK_CLIP_NONE = 2;
    static final String TAG = "WindowManager";
    static final int WINDOW_FREEZE_LAYER = 2000000;
    float mAlpha = 0.0f;
    int mAnimLayer;
    boolean mAnimationIsEntrance;
    private boolean mAnimationStartDelayed;
    final WindowAnimator mAnimator;
    int mAttrType;
    private BlurParams mBlurParams;
    boolean mChildrenDetached = false;
    final Context mContext;
    private boolean mDestroyPreservedSurfaceUponRedraw;
    int mDrawState;
    float mDsDx = 1.0f;
    float mDsDy = 0.0f;
    float mDtDx = 0.0f;
    float mDtDy = 1.0f;
    boolean mEnterAnimationPending;
    boolean mEnteringAnimation;
    float mExtraHScale = 1.0f;
    float mExtraVScale = 1.0f;
    boolean mForceScaleUntilResize;
    boolean mHaveMatrix;
    public Object mInsetSurfaceLock = new Object();
    public InsetSurfaceOverlay mInsetSurfaceOverlay;
    final boolean mIsWallpaper;
    float mLastAlpha = 0.0f;
    Rect mLastClipRect = new Rect();
    private float mLastDsDx = 1.0f;
    private float mLastDsDy = 0.0f;
    private float mLastDtDx = 0.0f;
    private float mLastDtDy = 1.0f;
    Rect mLastFinalClipRect = new Rect();
    boolean mLastHidden;
    int mLastLayer;
    boolean mLazyIsEntering;
    boolean mLazyIsExiting;
    private boolean mOffsetPositionForStackResize;
    private WindowSurfaceController mPendingDestroySurface;
    boolean mPipAnimationStarted = false;
    final WindowManagerPolicy mPolicy;
    private final SurfaceControl.Transaction mReparentTransaction = new SurfaceControl.Transaction();
    boolean mReportSurfaceResized;
    final WindowManagerService mService;
    final Session mSession;
    float mShownAlpha = 0.0f;
    protected Point mShownPosition = new Point();
    WindowSurfaceController mSurfaceController;
    boolean mSurfaceDestroyDeferred;
    int mSurfaceFormat;
    boolean mSurfaceResized;
    private final Rect mSystemDecorRect = new Rect();
    private Rect mTmpAnimatingBounds = new Rect();
    Rect mTmpClipRect = new Rect();
    Rect mTmpFinalClipRect = new Rect();
    private final Point mTmpPos = new Point();
    private final Rect mTmpSize = new Rect();
    private Rect mTmpSourceBounds = new Rect();
    Rect mTmpStackBounds = new Rect();
    private final SurfaceControl.Transaction mTmpTransaction = new SurfaceControl.Transaction();
    final WallpaperController mWallpaperControllerLocked;
    @RCUnownedRef
    final WindowState mWin;
    int mXOffset = 0;
    int mYOffset = 0;

    @RCUnownedThisRef
    private class BlurParams {
        private static final int CHANGE_ALPHA = 4;
        private static final int CHANGE_BLANK = 16;
        private static final int CHANGE_MASK = 31;
        private static final int CHANGE_NONE = 0;
        private static final int CHANGE_RADIUS = 1;
        private static final int CHANGE_REGION = 8;
        private static final int CHANGE_ROUND = 2;
        float alpha;
        Rect blank;
        private int changes = 0;
        int radius;
        Region region;
        int rx;
        int ry;
        int surfaceHashCode;

        public BlurParams(int hashcode) {
            this.surfaceHashCode = hashcode;
        }

        public BlurParams(WindowManager.LayoutParams lp, int hashcode) {
            this.surfaceHashCode = hashcode;
            set(lp);
        }

        public boolean updateHashCode(int hashcode) {
            if (this.surfaceHashCode == hashcode) {
                return false;
            }
            this.surfaceHashCode = hashcode;
            return true;
        }

        public void set(WindowManager.LayoutParams lp) {
            setRadius(lp.blurRadius);
            setRound(lp.blurRoundx, lp.blurRoundy);
            setAlpha(lp.blurAlpha);
            setRegion(lp.blurRegion);
            setBlank(lp.blurBlankLeft, lp.blurBlankTop, lp.blurBlankRight, lp.blurBlankBottom);
        }

        public void setRadius(int radius2) {
            int newRadius = Math.max(0, Math.min(radius2, 100));
            if (this.radius != newRadius) {
                this.radius = newRadius;
                this.changes |= 1;
            }
        }

        public void setRound(int rx2, int ry2) {
            if (this.rx != rx2 || this.ry != ry2) {
                this.rx = rx2;
                this.ry = ry2;
                this.changes |= 2;
            }
        }

        public void setAlpha(float alpha2) {
            float newAlpha = Math.max(0.0f, Math.min(alpha2, 1.0f));
            if (this.alpha != newAlpha) {
                this.alpha = newAlpha;
                this.changes |= 4;
            }
        }

        public void setRegion(Region region2) {
            Region newRegion = region2 != null ? region2 : new Region();
            if (!newRegion.equals(this.region)) {
                this.region = newRegion;
                this.changes |= 8;
            }
        }

        public void setBlank(int l, int t, int r, int b) {
            Rect newBlank = new Rect(l, t, r, b);
            if (!newBlank.equals(this.blank)) {
                this.blank = newBlank;
                this.changes |= 16;
            }
        }

        public void refresh(WindowSurfaceController srufacecontroller, boolean force) {
            if (force || (this.changes & 1) == 1) {
                srufacecontroller.setBlurRadius(this.radius);
            }
            if (force || (this.changes & 2) == 2) {
                srufacecontroller.setBlurRound(this.rx, this.ry);
            }
            if (force || (this.changes & 4) == 4) {
                srufacecontroller.setBlurAlpha(this.alpha);
            }
            if (force || (this.changes & 8) == 8) {
                srufacecontroller.setBlurRegion(this.region);
            }
            if (force || (this.changes & 16) == 16) {
                srufacecontroller.setBlurBlank(this.blank);
            }
            this.changes = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public String drawStateToString() {
        switch (this.mDrawState) {
            case 0:
                return "NO_SURFACE";
            case 1:
                return "DRAW_PENDING";
            case 2:
                return "COMMIT_DRAW_PENDING";
            case 3:
                return "READY_TO_SHOW";
            case 4:
                return "HAS_DRAWN";
            default:
                return Integer.toString(this.mDrawState);
        }
    }

    WindowStateAnimator(WindowState win) {
        WindowManagerService service = win.mService;
        this.mService = service;
        this.mAnimator = service.mAnimator;
        this.mPolicy = service.mPolicy;
        this.mContext = service.mContext;
        this.mWin = win;
        this.mSession = win.mSession;
        this.mAttrType = win.mAttrs.type;
        this.mIsWallpaper = win.mIsWallpaper;
        this.mWallpaperControllerLocked = this.mService.mRoot.mWallpaperController;
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimationSet() {
        return this.mWin.isAnimating();
    }

    /* access modifiers changed from: package-private */
    public void cancelExitAnimationForNextAnimationLocked() {
        this.mWin.cancelAnimation();
        this.mWin.destroySurfaceUnchecked();
    }

    /* access modifiers changed from: package-private */
    public void onAnimationFinished() {
        if (this.mAnimator.mWindowDetachedWallpaper == this.mWin) {
            this.mAnimator.mWindowDetachedWallpaper = null;
        }
        this.mWin.checkPolicyVisibilityChange();
        DisplayContent displayContent = this.mWin.getDisplayContent();
        if (this.mAttrType == 2000 && this.mWin.mPolicyVisibility && displayContent != null) {
            displayContent.setLayoutNeeded();
        }
        this.mWin.onExitAnimationDone();
        int displayId = this.mWin.getDisplayId();
        int pendingLayoutChanges = 8;
        if (displayContent.mWallpaperController.isWallpaperTarget(this.mWin)) {
            pendingLayoutChanges = 8 | 4;
        }
        this.mAnimator.setPendingLayoutChanges(displayId, pendingLayoutChanges);
        if (this.mWin.mAppToken != null) {
            this.mWin.mAppToken.updateReportedVisibilityLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void hide(SurfaceControl.Transaction transaction, String reason) {
        if (!this.mLastHidden) {
            this.mLastHidden = true;
            markPreservedSurfaceForDestroy();
            if (this.mSurfaceController != null) {
                this.mSurfaceController.hide(transaction, reason);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void hide(String reason) {
        hide(this.mTmpTransaction, reason);
        SurfaceControl.mergeToGlobalTransaction(this.mTmpTransaction);
    }

    /* access modifiers changed from: package-private */
    public boolean finishDrawingLocked() {
        boolean startingWindow = this.mWin.mAttrs.type == 3;
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW && startingWindow) {
            Slog.v(TAG, "Finishing drawing window " + this.mWin + ": mDrawState=" + drawStateToString());
        }
        if (this.mDrawState != 1) {
            return false;
        }
        printDrawStateChanged("DRAW_PENDING", "COMMIT_DRAW_PENDING", "finishDrawing");
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW && startingWindow) {
            Slog.v(TAG, "Draw state now committed in " + this.mWin);
        }
        this.mDrawState = 2;
        return true;
    }

    /* access modifiers changed from: package-private */
    public void printDrawStateChanged(String oldState, String newState, String reason) {
        Flog.i(307, "DrawState change: " + this.mWin + " from:" + oldState + " to:" + newState + " reason:" + reason);
    }

    /* access modifiers changed from: package-private */
    public boolean commitFinishDrawingLocked() {
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW_VERBOSE && this.mWin.mAttrs.type == 3) {
            Slog.i(TAG, "commitFinishDrawingLocked: " + this.mWin + " cur mDrawState=" + drawStateToString());
        }
        if (this.mDrawState != 2 && this.mDrawState != 3) {
            return false;
        }
        if (this.mDrawState == 2) {
            printDrawStateChanged("COMMIT_DRAW_PENDING", "READY_TO_SHOW", "commitFinishDrawing");
        }
        this.mDrawState = 3;
        boolean result = false;
        AppWindowToken atoken = this.mWin.mAppToken;
        if (atoken == null || atoken.allDrawn || this.mWin.mAttrs.type == 3) {
            result = this.mWin.performShowLocked();
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public void preserveSurfaceLocked() {
        if (this.mDestroyPreservedSurfaceUponRedraw) {
            this.mSurfaceDestroyDeferred = false;
            destroySurfaceLocked();
            this.mSurfaceDestroyDeferred = true;
        } else if (this.mWin == null || !HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(this.mWin.getDisplayId()) || this.mSurfaceController != null) {
            if (this.mSurfaceController != null) {
                this.mSurfaceController.mSurfaceControl.setLayer(1);
            }
            this.mDestroyPreservedSurfaceUponRedraw = true;
            this.mSurfaceDestroyDeferred = true;
            destroySurfaceLocked();
        } else {
            HwPCUtils.log(TAG, "surface is null");
        }
    }

    /* access modifiers changed from: package-private */
    public void destroyPreservedSurfaceLocked() {
        if (this.mDestroyPreservedSurfaceUponRedraw) {
            if (!(this.mSurfaceController == null || this.mPendingDestroySurface == null || (this.mWin.mAppToken != null && this.mWin.mAppToken.isRelaunching()))) {
                this.mReparentTransaction.reparentChildren(this.mPendingDestroySurface.mSurfaceControl, this.mSurfaceController.mSurfaceControl.getHandle()).apply();
            }
            destroyDeferredSurfaceLocked();
            this.mDestroyPreservedSurfaceUponRedraw = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void markPreservedSurfaceForDestroy() {
        if (this.mDestroyPreservedSurfaceUponRedraw && !this.mService.mDestroyPreservedSurface.contains(this.mWin)) {
            this.mService.mDestroyPreservedSurface.add(this.mWin);
        }
    }

    private int getLayerStack() {
        return this.mWin.getDisplayContent().getDisplay().getLayerStack();
    }

    /* access modifiers changed from: package-private */
    public void resetDrawState() {
        this.mDrawState = 1;
        if (this.mWin.mAppToken != null) {
            if (!this.mWin.mAppToken.isSelfAnimating()) {
                this.mWin.mAppToken.clearAllDrawn();
            } else {
                this.mWin.mAppToken.deferClearAllDrawn = true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01dc, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x01dd, code lost:
        r1 = r18;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x01dc A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:54:0x0156] */
    public WindowSurfaceController createSurfaceLocked(int windowType, int ownerUid) {
        boolean z;
        int flags;
        WindowState w = this.mWin;
        if (this.mSurfaceController != null) {
            return this.mSurfaceController;
        }
        this.mChildrenDetached = false;
        int windowType2 = (this.mWin.mAttrs.privateFlags & DumpState.DUMP_DEXOPT) != 0 ? 441731 : windowType;
        w.setHasSurface(false);
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.i(TAG, "createSurface " + this + ": mDrawState=DRAW_PENDING");
        }
        printDrawStateChanged(drawStateToString(), "DRAW_PENDING", "createSurface");
        resetDrawState();
        this.mService.makeWindowFreezingScreenIfNeededLocked(w);
        int flags2 = 4;
        WindowManager.LayoutParams attrs = w.mAttrs;
        if (this.mService.isSecureLocked(w)) {
            flags2 = 4 | 128;
        }
        int flags3 = flags2;
        this.mTmpSize.set(0, 0, 0, 0);
        calculateSurfaceBounds(w, attrs);
        int width = this.mTmpSize.width();
        int height = this.mTmpSize.height();
        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.v(TAG, "Creating surface in session " + this.mSession.mSurfaceSession + " window " + this + " w=" + width + " h=" + height + " x=" + this.mTmpSize.left + " y=" + this.mTmpSize.top + " format=" + attrs.format + " flags=" + flags3);
        }
        this.mLastClipRect.set(0, 0, 0, 0);
        try {
            int format = (attrs.flags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0 ? -3 : attrs.format;
            if (!PixelFormat.formatHasAlpha(attrs.format)) {
                try {
                    if (attrs.surfaceInsets.left == 0 && attrs.surfaceInsets.top == 0 && attrs.surfaceInsets.right == 0 && attrs.surfaceInsets.bottom == 0 && !w.isDragResizing()) {
                        flags3 |= 1024;
                    }
                } catch (Surface.OutOfResourcesException e) {
                    z = true;
                    int i = height;
                    int i2 = width;
                    Slog.w(TAG, "OutOfResourcesException creating surface");
                    this.mService.mRoot.reclaimSomeSurfaceMemory(this, "create", z);
                    this.mDrawState = 0;
                    return null;
                } catch (Exception e2) {
                    e = e2;
                    int i3 = height;
                    int i4 = width;
                    Slog.e(TAG, "Exception creating surface (parent dead?)", e);
                    this.mDrawState = 0;
                    return null;
                }
            }
            if ((attrs.flags & 4) != 0) {
                flags = 65536 | flags3;
            } else {
                flags = flags3;
            }
            try {
                r1 = r1;
                int format2 = format;
                int i5 = height;
                int width2 = width;
                try {
                    WindowSurfaceController windowSurfaceController = new WindowSurfaceController(this.mSession.mSurfaceSession, attrs.getTitle().toString(), width, height, format, flags, this, windowType2, ownerUid);
                    this.mSurfaceController = windowSurfaceController;
                    synchronized (this.mInsetSurfaceLock) {
                        try {
                            WindowManagerService windowManagerService = this.mService;
                            if (WindowManagerService.mSupporInputMethodFilletAdaptation) {
                                try {
                                    if (this.mWin.getAttrs().type == 2011 && this.mInsetSurfaceOverlay == null) {
                                        int insetSurfaceHeight = this.mService.mPolicy.getDefaultNavBarHeight() / 2;
                                        InsetSurfaceOverlay insetSurfaceOverlay = new InsetSurfaceOverlay(this.mWin.getDisplayContent(), 0, width2, insetSurfaceHeight, this.mWin.mFrame.left, (this.mWin.mFrame.bottom - insetSurfaceHeight) - this.mWin.mLastSurfacePosition.y, this.mWin.getSurfaceControl());
                                        this.mInsetSurfaceOverlay = insetSurfaceOverlay;
                                        if (this.mService.mPolicy.isInputMethodMovedUp()) {
                                            this.mInsetSurfaceOverlay.createSurface();
                                        }
                                    }
                                } catch (Throwable th) {
                                    th = th;
                                    int i6 = format2;
                                    z = true;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th2) {
                                            th = th2;
                                        }
                                    }
                                    throw th;
                                }
                            }
                            setOffsetPositionForStackResize(false);
                            this.mSurfaceFormat = format2;
                            z = true;
                            w.setHasSurface(true);
                            this.mLastHidden = true;
                            return this.mSurfaceController;
                        } catch (Throwable th3) {
                            th = th3;
                            int i7 = format2;
                            z = true;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                } catch (Surface.OutOfResourcesException e3) {
                    z = true;
                } catch (Exception e4) {
                }
            } catch (Surface.OutOfResourcesException e5) {
                z = true;
                int i8 = height;
                int i9 = width;
                int i10 = flags;
                Slog.w(TAG, "OutOfResourcesException creating surface");
                this.mService.mRoot.reclaimSomeSurfaceMemory(this, "create", z);
                this.mDrawState = 0;
                return null;
            } catch (Exception e6) {
                e = e6;
                int i11 = height;
                int i12 = width;
                int i13 = flags;
                Slog.e(TAG, "Exception creating surface (parent dead?)", e);
                this.mDrawState = 0;
                return null;
            }
        } catch (Surface.OutOfResourcesException e7) {
            z = true;
            int i14 = height;
            int i15 = width;
            Slog.w(TAG, "OutOfResourcesException creating surface");
            this.mService.mRoot.reclaimSomeSurfaceMemory(this, "create", z);
            this.mDrawState = 0;
            return null;
        } catch (Exception e8) {
            e = e8;
            int i16 = height;
            int i17 = width;
            Slog.e(TAG, "Exception creating surface (parent dead?)", e);
            this.mDrawState = 0;
            return null;
        }
        Slog.w(TAG, "OutOfResourcesException creating surface");
        this.mService.mRoot.reclaimSomeSurfaceMemory(this, "create", z);
        this.mDrawState = 0;
        return null;
    }

    private void calculateSurfaceBounds(WindowState w, WindowManager.LayoutParams attrs) {
        if ((attrs.flags & 16384) != 0) {
            this.mTmpSize.right = this.mTmpSize.left + w.mRequestedWidth;
            this.mTmpSize.bottom = this.mTmpSize.top + w.mRequestedHeight;
        } else if (w.isDragResizing()) {
            if (w.getResizeMode() == 0) {
                this.mTmpSize.left = 0;
                this.mTmpSize.top = 0;
            }
            DisplayInfo displayInfo = w.getDisplayInfo();
            this.mTmpSize.right = this.mTmpSize.left + displayInfo.logicalWidth;
            this.mTmpSize.bottom = this.mTmpSize.top + displayInfo.logicalHeight;
        } else {
            this.mTmpSize.right = this.mTmpSize.left + w.mCompatFrame.width();
            this.mTmpSize.bottom = this.mTmpSize.top + w.mCompatFrame.height();
        }
        if (this.mTmpSize.width() < 1) {
            this.mTmpSize.right = this.mTmpSize.left + 1;
        }
        if (this.mTmpSize.height() < 1) {
            this.mTmpSize.bottom = this.mTmpSize.top + 1;
        }
        this.mTmpSize.left -= attrs.surfaceInsets.left;
        this.mTmpSize.top -= attrs.surfaceInsets.top;
        this.mTmpSize.right += attrs.surfaceInsets.right;
        this.mTmpSize.bottom += attrs.surfaceInsets.bottom;
    }

    /* access modifiers changed from: package-private */
    public boolean hasSurface() {
        return this.mSurfaceController != null && this.mSurfaceController.hasSurface();
    }

    /* access modifiers changed from: package-private */
    public void destroySurfaceLocked() {
        AppWindowToken wtoken = this.mWin.mAppToken;
        if (wtoken != null && this.mWin == wtoken.startingWindow) {
            wtoken.startingDisplayed = false;
        }
        if (this.mSurfaceController != null) {
            if (!this.mDestroyPreservedSurfaceUponRedraw) {
                this.mWin.mHidden = true;
            }
            try {
                if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                    WindowManagerService.logWithStack(TAG, "Window " + this + " destroying surface " + this.mSurfaceController + ", session " + this.mSession);
                }
                if (!this.mSurfaceDestroyDeferred) {
                    destroySurface();
                } else if (!(this.mSurfaceController == null || this.mPendingDestroySurface == this.mSurfaceController)) {
                    if (this.mPendingDestroySurface != null) {
                        this.mPendingDestroySurface.destroyNotInTransaction();
                    }
                    this.mPendingDestroySurface = this.mSurfaceController;
                }
                if (!this.mDestroyPreservedSurfaceUponRedraw) {
                    this.mWallpaperControllerLocked.hideWallpapers(this.mWin);
                }
            } catch (RuntimeException e) {
                Slog.w(TAG, "Exception thrown when destroying Window " + this + " surface " + this.mSurfaceController + " session " + this.mSession + ": " + e.toString());
            }
            this.mWin.setHasSurface(false);
            if (this.mSurfaceController != null) {
                this.mSurfaceController.setShown(false);
            }
            this.mSurfaceController = null;
            this.mDrawState = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void destroyDeferredSurfaceLocked() {
        try {
            if (this.mPendingDestroySurface != null) {
                this.mPendingDestroySurface.destroyNotInTransaction();
                if (!this.mDestroyPreservedSurfaceUponRedraw) {
                    this.mWallpaperControllerLocked.hideWallpapers(this.mWin);
                }
            }
        } catch (RuntimeException e) {
            Slog.w(TAG, "Exception thrown when destroying Window " + this + " surface " + this.mPendingDestroySurface + " session " + this.mSession + ": " + e.toString());
        }
        this.mSurfaceDestroyDeferred = false;
        this.mPendingDestroySurface = null;
    }

    /* access modifiers changed from: package-private */
    public void computeShownFrameLocked() {
        ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(this.mWin.getDisplayId());
        boolean screenAnimation = screenRotationAnimation != null && screenRotationAnimation.isAnimating();
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
                    tmpMatrix.setScale((2.0f / w) + 1.0f, 1.0f + (2.0f / h), w / 2.0f, h / 2.0f);
                }
            } else {
                tmpMatrix.reset();
            }
            tmpMatrix.postScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
            tmpMatrix.postTranslate((float) this.mWin.mAttrs.surfaceInsets.left, (float) this.mWin.mAttrs.surfaceInsets.top);
            this.mHaveMatrix = true;
            tmpMatrix.getValues(tmpFloats);
            this.mDsDx = tmpFloats[0];
            this.mDtDx = tmpFloats[3];
            this.mDtDy = tmpFloats[1];
            this.mDsDy = tmpFloats[4];
            this.mShownAlpha = this.mAlpha;
            if ((!this.mService.mLimitedAlphaCompositing || !PixelFormat.formatHasAlpha(this.mWin.mAttrs.format) || this.mWin.isIdentityMatrix(this.mDsDx, this.mDtDx, this.mDtDy, this.mDsDy)) && screenAnimation) {
                this.mShownAlpha *= screenRotationAnimation.getEnterTransformation().getAlpha();
            }
        } else if ((!this.mIsWallpaper || !this.mService.mRoot.mWallpaperActionPending) && !this.mWin.isDragResizeChanged()) {
            this.mShownAlpha = this.mAlpha;
            this.mHaveMatrix = false;
            this.mDsDx = this.mWin.mGlobalScale;
            this.mDtDx = 0.0f;
            this.mDtDy = 0.0f;
            this.mDsDy = this.mWin.mGlobalScale;
        }
    }

    private boolean calculateCrop(Rect clipRect) {
        WindowState w = this.mWin;
        DisplayContent displayContent = w.getDisplayContent();
        clipRect.setEmpty();
        boolean isFreeformResizing = false;
        if (displayContent == null || w.inPinnedWindowingMode() || w.mAttrs.type == 2013) {
            return false;
        }
        w.calculatePolicyCrop(this.mSystemDecorRect);
        Task task = w.getTask();
        if (w.fillsDisplay() || (task != null && task.isFullscreen())) {
        }
        if (w.isDragResizing() && w.getResizeMode() == 0) {
            isFreeformResizing = true;
        }
        clipRect.set(this.mSystemDecorRect);
        if (isFreeformResizing && !w.isChildWindow()) {
            clipRect.offset(w.mFrame.left, w.mFrame.top);
        }
        w.expandForSurfaceInsets(clipRect);
        clipRect.offset(w.mAttrs.surfaceInsets.left, w.mAttrs.surfaceInsets.top);
        w.transformClipRectFromScreenToSurfaceSpace(clipRect);
        return true;
    }

    private void applyCrop(Rect clipRect, boolean recoveringMemory) {
        if (clipRect == null) {
            this.mSurfaceController.clearCropInTransaction(recoveringMemory);
        } else if (!clipRect.equals(this.mLastClipRect)) {
            this.mLastClipRect.set(clipRect);
            this.mSurfaceController.setCropInTransaction(clipRect, recoveringMemory);
        }
    }

    /* access modifiers changed from: package-private */
    public void setSurfaceBoundariesLocked(boolean recoveringMemory) {
        float surfaceWidth;
        boolean wasForceScaled;
        boolean wasSeamlesslyRotated;
        Rect clipRect;
        Rect clipRect2;
        int lazyMode;
        boolean allowStretching;
        int posY;
        Rect clipRect3;
        boolean z = recoveringMemory;
        if (this.mSurfaceController != null) {
            WindowState w = this.mWin;
            WindowManager.LayoutParams attrs = this.mWin.getAttrs();
            Task task = w.getTask();
            this.mTmpSize.set(0, 0, 0, 0);
            calculateSurfaceBounds(w, attrs);
            float[] pos = {0.0f, 0.0f};
            this.mExtraHScale = 1.0f;
            this.mExtraVScale = 1.0f;
            boolean wasForceScaled2 = this.mForceScaleUntilResize;
            boolean wasSeamlesslyRotated2 = w.mSeamlesslyRotated;
            boolean relayout = !w.mRelayoutCalled || w.mInRelayout;
            if (relayout) {
                this.mService.mDeferRelayoutWindow.remove(w);
                this.mSurfaceResized = this.mSurfaceController.setSizeInTransaction(this.mTmpSize.width(), this.mTmpSize.height(), z);
            } else {
                this.mSurfaceResized = false;
            }
            this.mForceScaleUntilResize = this.mForceScaleUntilResize && !this.mSurfaceResized;
            this.mService.markForSeamlessRotation(w, w.mSeamlesslyRotated && !this.mSurfaceResized);
            Rect clipRect4 = null;
            if (calculateCrop(this.mTmpClipRect)) {
                clipRect4 = this.mTmpClipRect;
            }
            float surfaceWidth2 = (float) this.mSurfaceController.getWidth();
            float surfaceHeight = (float) this.mSurfaceController.getHeight();
            Rect insets = attrs.surfaceInsets;
            if (isForceScaled()) {
                int hInsets = insets.left + insets.right;
                int vInsets = insets.top + insets.bottom;
                float surfaceContentWidth = surfaceWidth2 - ((float) hInsets);
                int i = hInsets;
                float surfaceContentHeight = surfaceHeight - ((float) vInsets);
                int i2 = vInsets;
                if (this.mForceScaleUntilResize == 0) {
                    float[] fArr = pos;
                    this.mSurfaceController.forceScaleableInTransaction(true);
                } else {
                    float[] fArr2 = pos;
                }
                int posX = 0;
                wasSeamlesslyRotated = wasSeamlesslyRotated2;
                wasForceScaled = wasForceScaled2;
                task.mStack.getDimBounds(this.mTmpStackBounds);
                task.mStack.getFinalAnimationSourceHintBounds(this.mTmpSourceBounds);
                if (!this.mTmpSourceBounds.isEmpty() || ((this.mWin.mLastRelayoutContentInsets.width() <= 0 && this.mWin.mLastRelayoutContentInsets.height() <= 0) || task.mStack.lastAnimatingBoundsWasToFullscreen())) {
                    allowStretching = false;
                } else {
                    this.mTmpSourceBounds.set(task.mStack.mPreAnimationBounds);
                    this.mTmpSourceBounds.inset(this.mWin.mLastRelayoutContentInsets);
                    allowStretching = true;
                }
                Rect rect = clipRect4;
                this.mTmpStackBounds.intersectUnchecked(w.mParentFrame);
                this.mTmpSourceBounds.intersectUnchecked(w.mParentFrame);
                this.mTmpAnimatingBounds.intersectUnchecked(w.mParentFrame);
                if (!this.mTmpSourceBounds.isEmpty()) {
                    task.mStack.getFinalAnimationBounds(this.mTmpAnimatingBounds);
                    float finalWidth = (float) this.mTmpAnimatingBounds.width();
                    float initialWidth = (float) this.mTmpSourceBounds.width();
                    Task task2 = task;
                    boolean z2 = relayout;
                    float tw = (surfaceContentWidth - ((float) this.mTmpStackBounds.width())) / (surfaceContentWidth - ((float) this.mTmpAnimatingBounds.width()));
                    float th = tw;
                    float f = finalWidth;
                    this.mExtraHScale = (initialWidth + ((finalWidth - initialWidth) * tw)) / initialWidth;
                    if (allowStretching) {
                        boolean z3 = allowStretching;
                        float initialHeight = (float) this.mTmpSourceBounds.height();
                        float f2 = initialWidth;
                        float f3 = th;
                        th = (surfaceContentHeight - ((float) this.mTmpStackBounds.height())) / (surfaceContentHeight - ((float) this.mTmpAnimatingBounds.height()));
                        this.mExtraVScale = (((((float) this.mTmpAnimatingBounds.height()) - initialHeight) * tw) + initialHeight) / initialHeight;
                    } else {
                        float f4 = initialWidth;
                        float f5 = th;
                        this.mExtraVScale = this.mExtraHScale;
                    }
                    int posX2 = 0 - ((int) ((this.mExtraHScale * tw) * ((float) this.mTmpSourceBounds.left)));
                    int posY2 = 0 - ((int) ((this.mExtraVScale * th) * ((float) this.mTmpSourceBounds.top)));
                    clipRect3 = this.mTmpClipRect;
                    posY = posY2;
                    surfaceWidth = surfaceWidth2;
                    clipRect3.set((int) (((float) (insets.left + this.mTmpSourceBounds.left)) * tw), (int) (((float) (insets.top + this.mTmpSourceBounds.top)) * th), insets.left + ((int) (surfaceWidth2 - ((surfaceWidth2 - ((float) this.mTmpSourceBounds.right)) * tw))), insets.top + ((int) (surfaceHeight - ((surfaceHeight - ((float) this.mTmpSourceBounds.bottom)) * th))));
                    posX = posX2;
                } else {
                    surfaceWidth = surfaceWidth2;
                    Task task3 = task;
                    boolean z4 = allowStretching;
                    boolean z5 = relayout;
                    if (!w.mEnforceSizeCompat) {
                        this.mExtraHScale = ((float) this.mTmpStackBounds.width()) / surfaceContentWidth;
                        this.mExtraVScale = ((float) this.mTmpStackBounds.height()) / surfaceContentHeight;
                    }
                    clipRect3 = null;
                    posY = 0;
                }
                this.mSurfaceController.setPositionInTransaction((float) Math.floor((double) ((int) (((float) (posX - ((int) (((float) attrs.x) * (1.0f - this.mExtraHScale))))) + (((float) insets.left) * (1.0f - this.mExtraHScale))))), (float) Math.floor((double) ((int) (((float) (posY - ((int) (((float) attrs.y) * (1.0f - this.mExtraVScale))))) + (((float) insets.top) * (1.0f - this.mExtraVScale))))), z);
                if (!this.mPipAnimationStarted) {
                    this.mForceScaleUntilResize = true;
                    this.mPipAnimationStarted = true;
                }
                clipRect = clipRect3;
            } else {
                Rect clipRect5 = clipRect4;
                surfaceWidth = surfaceWidth2;
                Task task4 = task;
                float[] fArr3 = pos;
                wasForceScaled = wasForceScaled2;
                wasSeamlesslyRotated = wasSeamlesslyRotated2;
                boolean relayout2 = relayout;
                this.mPipAnimationStarted = false;
                if (!w.mSeamlesslyRotated) {
                    int xOffset = this.mXOffset;
                    int yOffset = this.mYOffset;
                    if (this.mOffsetPositionForStackResize) {
                        if (relayout2) {
                            setOffsetPositionForStackResize(false);
                            this.mSurfaceController.deferTransactionUntil(this.mSurfaceController.getHandle(), this.mWin.getFrameNumber());
                            clipRect2 = clipRect5;
                        } else {
                            TaskStack stack = this.mWin.getStack();
                            this.mTmpPos.x = 0;
                            this.mTmpPos.y = 0;
                            if (stack != null) {
                                stack.getRelativePosition(this.mTmpPos);
                            }
                            xOffset = -this.mTmpPos.x;
                            yOffset = -this.mTmpPos.y;
                            if (clipRect5 != null) {
                                clipRect2 = clipRect5;
                                clipRect2.right += this.mTmpPos.x;
                                clipRect2.bottom += this.mTmpPos.y;
                            }
                        }
                        lazyMode = this.mService.getLazyMode();
                        int displayId = this.mWin.getDisplayId();
                        if (!(displayId == -1 || displayId == 0)) {
                            lazyMode = 0;
                        }
                        if (lazyMode == 0 && this.mWin.mIsWallpaper) {
                            this.mSurfaceController.setPositionInTransaction(((float) xOffset) * this.mWin.mLazyScale, ((float) yOffset) * this.mWin.mLazyScale, z);
                        } else if (!this.mWin.inFreeformWindowingMode() && this.mWin.isDragResizing()) {
                            this.mSurfaceController.setPositionInTransaction((float) (xOffset - this.mWin.mFrame.left), (float) (yOffset - this.mWin.mFrame.top), z);
                        } else if (HwFoldScreenState.isFoldScreenDevice() || !this.mService.isInSubFoldScaleMode() || !this.mWin.mIsWallpaper) {
                            this.mSurfaceController.setPositionInTransaction((float) xOffset, (float) yOffset, z);
                        } else {
                            this.mSurfaceController.setPositionInTransaction(((float) xOffset) * this.mService.mSubFoldModeScale, ((float) yOffset) * this.mService.mSubFoldModeScale, z);
                        }
                        this.mWin.updateSurfacePosition(this.mShownPosition.x, this.mShownPosition.y);
                        if (HwFoldScreenState.isFoldScreenDevice() && this.mService.isInSubFoldScaleMode()) {
                            this.mWin.updateSurfacePositionBySubFoldMode(this.mShownPosition.x, this.mShownPosition.y);
                        }
                    }
                    clipRect2 = clipRect5;
                    lazyMode = this.mService.getLazyMode();
                    int displayId2 = this.mWin.getDisplayId();
                    lazyMode = 0;
                    if (lazyMode == 0) {
                    }
                    if (!this.mWin.inFreeformWindowingMode()) {
                    }
                    if (HwFoldScreenState.isFoldScreenDevice()) {
                    }
                    this.mSurfaceController.setPositionInTransaction((float) xOffset, (float) yOffset, z);
                    this.mWin.updateSurfacePosition(this.mShownPosition.x, this.mShownPosition.y);
                    this.mWin.updateSurfacePositionBySubFoldMode(this.mShownPosition.x, this.mShownPosition.y);
                } else {
                    clipRect2 = clipRect5;
                }
                clipRect = clipRect2;
            }
            if ((wasForceScaled && !this.mForceScaleUntilResize) || (wasSeamlesslyRotated && !w.mSeamlesslyRotated)) {
                this.mSurfaceController.setGeometryAppliesWithResizeInTransaction(true);
                this.mSurfaceController.forceScaleableInTransaction(false);
            }
            if (!w.mSeamlesslyRotated) {
                applyCrop(clipRect, z);
                setSurfaceLowResolutionInfo();
                Rect rect2 = insets;
                float f6 = surfaceHeight;
                float f7 = surfaceWidth;
                this.mSurfaceController.setMatrixInTransaction(this.mDsDx * w.mHScale * this.mExtraHScale, this.mDtDx * w.mVScale * this.mExtraVScale, this.mDtDy * w.mHScale * this.mExtraHScale, this.mDsDy * w.mVScale * this.mExtraVScale, z);
            } else {
                float f8 = surfaceHeight;
                float f9 = surfaceWidth;
            }
            if (this.mSurfaceResized) {
                this.mReportSurfaceResized = true;
                this.mAnimator.setPendingLayoutChanges(w.getDisplayId(), 4);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void getContainerRect(Rect rect) {
        Task task = this.mWin.getTask();
        if (task != null) {
            task.getDimBounds(rect);
            return;
        }
        rect.bottom = 0;
        rect.right = 0;
        rect.top = 0;
        rect.left = 0;
    }

    /* access modifiers changed from: package-private */
    public void prepareSurfaceLocked(boolean recoveringMemory) {
        WindowState w = this.mWin;
        if (!hasSurface()) {
            if (w.getOrientationChanging() && w.isGoneForLayoutLw()) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v(TAG, "Orientation change skips hidden " + w);
                }
                w.setOrientationChanging(false);
            }
        } else if (!w.getOrientationChanging() || !isEvilWindow(w)) {
            boolean displayed = false;
            hwPrepareSurfaceLocked();
            setSurfaceBoundariesLocked(recoveringMemory);
            if (this.mIsWallpaper && !this.mWin.mWallpaperVisible) {
                hide("prepareSurfaceLocked");
            } else if (w.isParentWindowHidden() || !w.isOnScreen()) {
                hide("prepareSurfaceLocked");
                this.mWallpaperControllerLocked.hideWallpapers(w);
                if (w.getOrientationChanging() && w.isGoneForLayoutLw()) {
                    w.setOrientationChanging(false);
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.v(TAG, "Orientation change skips hidden " + w);
                    }
                }
            } else if (this.mLastLayer == this.mAnimLayer && this.mLastAlpha == this.mShownAlpha && this.mLastDsDx == this.mDsDx && this.mLastDtDx == this.mDtDx && this.mLastDsDy == this.mDsDy && this.mLastDtDy == this.mDtDy && w.mLastHScale == w.mHScale && w.mLastVScale == w.mVScale && !this.mLastHidden) {
                displayed = true;
            } else {
                displayed = true;
                this.mLastAlpha = this.mShownAlpha;
                this.mLastLayer = this.mAnimLayer;
                this.mLastDsDx = this.mDsDx;
                this.mLastDtDx = this.mDtDx;
                this.mLastDsDy = this.mDsDy;
                this.mLastDtDy = this.mDtDy;
                w.mLastHScale = w.mHScale;
                w.mLastVScale = w.mVScale;
                if (this.mSurfaceController.prepareToShowInTransaction(this.mShownAlpha, this.mExtraHScale * this.mDsDx * w.mHScale, this.mExtraVScale * this.mDtDx * w.mVScale, this.mExtraHScale * this.mDtDy * w.mHScale, this.mExtraVScale * this.mDsDy * w.mVScale, recoveringMemory) && this.mDrawState == 4 && this.mLastHidden) {
                    if (showSurfaceRobustlyLocked()) {
                        markPreservedSurfaceForDestroy();
                        this.mAnimator.requestRemovalOfReplacedWindows(w);
                        this.mLastHidden = false;
                        if (this.mIsWallpaper) {
                            w.dispatchWallpaperVisibility(true);
                        }
                        if (!w.getDisplayContent().getLastHasContent()) {
                            this.mAnimator.setPendingLayoutChanges(w.getDisplayId(), 8);
                        }
                    } else {
                        w.setOrientationChanging(false);
                    }
                }
                if (hasSurface()) {
                    w.mToken.hasVisible = true;
                }
            }
            if (w.getOrientationChanging()) {
                if (!w.isDrawnLw()) {
                    this.mAnimator.mBulkUpdateParams &= -9;
                    this.mAnimator.mLastWindowFreezeSource = w;
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.v(TAG, "Orientation continue waiting for draw in " + w);
                    }
                } else {
                    w.setOrientationChanging(false);
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.v(TAG, "Orientation change complete in " + w);
                    }
                }
            }
            if (displayed) {
                w.mToken.hasVisible = true;
            }
        } else {
            Slog.v(TAG, "Orientation change skips evil window " + w);
            w.setOrientationChanging(false);
        }
    }

    /* access modifiers changed from: package-private */
    public void setTransparentRegionHintLocked(Region region) {
        if (this.mSurfaceController == null) {
            Slog.w(TAG, "setTransparentRegionHint: null mSurface after mHasSurface true");
        } else {
            this.mSurfaceController.setTransparentRegionHint(region);
        }
    }

    /* Debug info: failed to restart local var, previous not found, register: 5 */
    /* access modifiers changed from: package-private */
    public boolean setWallpaperOffset(int dx, int dy) {
        if (this.mService.getLazyMode() != 0) {
            dx = (int) (((float) dx) * this.mWin.mLazyScale);
            dy = (int) (((float) dy) * this.mWin.mLazyScale);
        }
        if (this.mXOffset == dx && this.mYOffset == dy) {
            return false;
        }
        this.mXOffset = dx;
        this.mYOffset = dy;
        try {
            this.mService.openSurfaceTransaction();
            this.mSurfaceController.setPositionInTransaction((float) dx, (float) dy, false);
            applyCrop(null, false);
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error positioning surface of " + this.mWin + " pos=(" + dx + "," + dy + ")", e);
        } catch (Throwable th) {
        }
        this.mService.closeSurfaceTransaction("setWallpaperOffset");
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean tryChangeFormatInPlaceLocked() {
        if (this.mSurfaceController == null) {
            return false;
        }
        WindowManager.LayoutParams attrs = this.mWin.getAttrs();
        if (((attrs.flags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0 ? -3 : attrs.format) != this.mSurfaceFormat) {
            return false;
        }
        setOpaqueLocked(!PixelFormat.formatHasAlpha(attrs.format));
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setOpaqueLocked(boolean isOpaque) {
        if (this.mSurfaceController != null) {
            this.mSurfaceController.setOpaque(isOpaque);
        }
    }

    /* access modifiers changed from: package-private */
    public void setSecureLocked(boolean isSecure) {
        if (this.mSurfaceController != null) {
            this.mSurfaceController.setSecure(isSecure);
        }
    }

    private boolean showSurfaceRobustlyLocked() {
        if (this.mWin.getWindowConfiguration().windowsAreScaleable()) {
            this.mSurfaceController.forceScaleableInTransaction(true);
        }
        if (!this.mSurfaceController.showRobustlyInTransaction()) {
            return false;
        }
        if (this.mPendingDestroySurface != null && this.mDestroyPreservedSurfaceUponRedraw) {
            this.mPendingDestroySurface.mSurfaceControl.hide();
            this.mPendingDestroySurface.reparentChildrenInTransaction(this.mSurfaceController);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void applyEnterAnimationLocked() {
        int transit;
        if (!this.mWin.mSkipEnterAnimationForSeamlessReplacement) {
            if (this.mEnterAnimationPending) {
                this.mEnterAnimationPending = false;
                transit = 1;
            } else {
                transit = 3;
            }
            applyAnimationLocked(transit, true);
            if (this.mService.mAccessibilityController != null && (this.mWin.getDisplayId() == 0 || (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()))) {
                this.mService.mAccessibilityController.onWindowTransitionLocked(this.mWin, transit);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean applyAnimationLocked(int transit, boolean isEntrance) {
        if (this.mWin != null && this.mWin.getName().contains("DockedStackDivider")) {
            Slog.v(TAG, "skip DockedStackDivider Exiting Anim.");
            return false;
        } else if (this.mWin != null && HwPCUtils.isValidExtDisplayId(this.mWin.getDisplayId()) && "com.huawei.desktop.explorer".equals(this.mWin.mAttrs.packageName) && this.mWin.getName().contains("BootWindow") && transit == 2) {
            Slog.v(TAG, "skip BootWindow Exiting Anim.");
            return false;
        } else if (HwPCUtils.enabledInPad() && this.mWin != null && this.mWin.getName().contains("StatusBar") && transit == 2) {
            Slog.v(TAG, "skip StatusBar Exiting Anim.");
            return false;
        } else if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && this.mWin != null && ((this.mWin.isInputMethodWindow() || this.mWin.getName().contains("ExitDesktopAlertDialog")) && transit == 2)) {
            Slog.v(TAG, "skip Exiting Anim in PC mode.");
            return false;
        } else if (this.mWin.isSelfAnimating() && this.mAnimationIsEntrance == isEntrance) {
            return true;
        } else {
            if (isEntrance && this.mWin.mAttrs.type == 2011) {
                this.mWin.getDisplayContent().adjustForImeIfNeeded();
                this.mWin.setDisplayLayoutNeeded();
                this.mService.mWindowPlacerLocked.requestTraversal();
            }
            Trace.traceBegin(32, "WSA#applyAnimationLocked");
            if (this.mWin.mToken.okToAnimate()) {
                int anim = this.mPolicy.selectAnimationLw(this.mWin, transit);
                int attr = -1;
                Animation a = null;
                if (anim != 0) {
                    a = anim != -1 ? AnimationUtils.loadAnimation(this.mContext, anim) : null;
                } else {
                    switch (transit) {
                        case 1:
                            attr = 0;
                            break;
                        case 2:
                            attr = 1;
                            break;
                        case 3:
                            attr = 2;
                            break;
                        case 4:
                            attr = 3;
                            break;
                    }
                    if (attr >= 0) {
                        a = this.mService.mAppTransition.loadAnimationAttr(this.mWin.mAttrs, attr, 0);
                    }
                }
                if (a != null) {
                    this.mWin.startAnimation(a);
                    this.mAnimationIsEntrance = isEntrance;
                }
            } else {
                this.mWin.cancelAnimation();
            }
            if (!isEntrance && this.mWin.mAttrs.type == 2011) {
                this.mWin.getDisplayContent().adjustForImeIfNeeded();
            }
            Trace.traceEnd(32);
            return isAnimationSet();
        }
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        this.mLastClipRect.writeToProto(proto, 1146756268033L);
        if (this.mSurfaceController != null) {
            this.mSurfaceController.writeToProto(proto, 1146756268034L);
        }
        proto.write(1159641169923L, this.mDrawState);
        this.mSystemDecorRect.writeToProto(proto, 1146756268036L);
        proto.end(token);
    }

    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        if (this.mAnimationIsEntrance) {
            pw.print(prefix);
            pw.print(" mAnimationIsEntrance=");
            pw.print(this.mAnimationIsEntrance);
        }
        if (this.mSurfaceController != null) {
            this.mSurfaceController.dump(pw, prefix, dumpAll);
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mDrawState=");
            pw.print(drawStateToString());
            pw.print(prefix);
            pw.print(" mLastHidden=");
            pw.println(this.mLastHidden);
            pw.print(prefix);
            pw.print("mSystemDecorRect=");
            this.mSystemDecorRect.printShortString(pw);
            pw.print(" mLastClipRect=");
            this.mLastClipRect.printShortString(pw);
            if (!this.mLastFinalClipRect.isEmpty()) {
                pw.print(" mLastFinalClipRect=");
                this.mLastFinalClipRect.printShortString(pw);
            }
            pw.println();
        }
        if (this.mPendingDestroySurface != null) {
            pw.print(prefix);
            pw.print("mPendingDestroySurface=");
            pw.println(this.mPendingDestroySurface);
        }
        if (this.mSurfaceResized || this.mSurfaceDestroyDeferred) {
            pw.print(prefix);
            pw.print("mSurfaceResized=");
            pw.print(this.mSurfaceResized);
            pw.print(" mSurfaceDestroyDeferred=");
            pw.println(this.mSurfaceDestroyDeferred);
        }
        if (!(this.mShownAlpha == 1.0f && this.mAlpha == 1.0f && this.mLastAlpha == 1.0f)) {
            pw.print(prefix);
            pw.print("mShownAlpha=");
            pw.print(this.mShownAlpha);
            pw.print(" mAlpha=");
            pw.print(this.mAlpha);
            pw.print(" mLastAlpha=");
            pw.println(this.mLastAlpha);
        }
        if (this.mHaveMatrix || this.mWin.mGlobalScale != 1.0f) {
            pw.print(prefix);
            pw.print("mGlobalScale=");
            pw.print(this.mWin.mGlobalScale);
            pw.print(" mDsDx=");
            pw.print(this.mDsDx);
            pw.print(" mDtDx=");
            pw.print(this.mDtDx);
            pw.print(" mDtDy=");
            pw.print(this.mDtDy);
            pw.print(" mDsDy=");
            pw.println(this.mDsDy);
        }
        if (this.mAnimationStartDelayed) {
            pw.print(prefix);
            pw.print("mAnimationStartDelayed=");
            pw.print(this.mAnimationStartDelayed);
        }
        pw.print(prefix);
        pw.print("mTmpSize=");
        this.mTmpSize.printShortString(pw);
        pw.println();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("WindowStateAnimator{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(this.mWin.mAttrs.getTitle());
        sb.append('}');
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public void reclaimSomeSurfaceMemory(String operation, boolean secure) {
        this.mService.mRoot.reclaimSomeSurfaceMemory(this, operation, secure);
    }

    /* access modifiers changed from: package-private */
    public boolean getShown() {
        if (this.mSurfaceController != null) {
            return this.mSurfaceController.getShown();
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void destroySurface() {
        try {
            if (this.mSurfaceController != null) {
                this.mSurfaceController.destroyNotInTransaction();
            }
        } catch (RuntimeException e) {
            Slog.w(TAG, "Exception thrown when destroying surface " + this + " surface " + this.mSurfaceController + " session " + this.mSession + ": " + e);
        } catch (Throwable th) {
            this.mWin.setHasSurface(false);
            this.mSurfaceController = null;
            printDrawStateChanged(drawStateToString(), "NO_SURFACE", "destroySurface");
            this.mDrawState = 0;
            throw th;
        }
        this.mWin.setHasSurface(false);
        this.mSurfaceController = null;
        printDrawStateChanged(drawStateToString(), "NO_SURFACE", "destroySurface");
        this.mDrawState = 0;
    }

    /* access modifiers changed from: package-private */
    public void seamlesslyRotateWindow(SurfaceControl.Transaction t, int oldRotation, int newRotation) {
        WindowState w = this.mWin;
        if (!w.isVisibleNow() || w.mIsWallpaper) {
            SurfaceControl.Transaction transaction = t;
            return;
        }
        Rect cropRect = this.mService.mTmpRect;
        Rect displayRect = this.mService.mTmpRect2;
        RectF frameRect = this.mService.mTmpRectF;
        Matrix transform = this.mService.mTmpTransform;
        float x = (float) w.mFrame.left;
        float y = (float) w.mFrame.top;
        float width = (float) w.mFrame.width();
        float height = (float) w.mFrame.height();
        this.mService.getDefaultDisplayContentLocked().getBounds(displayRect);
        float displayWidth = (float) displayRect.width();
        float displayHeight = (float) displayRect.height();
        float f = displayHeight;
        float f2 = displayWidth;
        DisplayContent.createRotationMatrix(DisplayContent.deltaRotation(newRotation, oldRotation), x, y, displayWidth, displayHeight, transform);
        this.mService.markForSeamlessRotation(w, true);
        transform.getValues(this.mService.mTmpFloats);
        float DsDx = this.mService.mTmpFloats[0];
        float DtDx = this.mService.mTmpFloats[3];
        float DtDy = this.mService.mTmpFloats[1];
        float DsDy = this.mService.mTmpFloats[4];
        Rect rect = cropRect;
        Rect rect2 = displayRect;
        RectF rectF = frameRect;
        SurfaceControl.Transaction transaction2 = t;
        this.mSurfaceController.setPosition(transaction2, this.mService.mTmpFloats[2], this.mService.mTmpFloats[5], false);
        this.mSurfaceController.setMatrix(transaction2, DsDx * w.mHScale, DtDx * w.mVScale, DtDy * w.mHScale, DsDy * w.mVScale, false);
    }

    /* access modifiers changed from: package-private */
    public boolean isForceScaled() {
        Task task = this.mWin.getTask();
        if (task == null || !task.mStack.isForceScaled()) {
            return this.mForceScaleUntilResize;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void detachChildren() {
        if (this.mSurfaceController != null) {
            this.mSurfaceController.detachChildren();
        }
        this.mChildrenDetached = true;
    }

    /* access modifiers changed from: package-private */
    public int getLayer() {
        return this.mLastLayer;
    }

    /* access modifiers changed from: package-private */
    public void setOffsetPositionForStackResize(boolean offsetPositionForStackResize) {
        this.mOffsetPositionForStackResize = offsetPositionForStackResize;
    }

    public int adjustAnimLayerIfCoverclosed(int type, int animLayer) {
        return animLayer;
    }

    public void hwPrepareSurfaceLocked() {
        computeShownFrameLocked();
    }

    /* access modifiers changed from: package-private */
    public void computeShownFrameRightLocked() {
    }

    /* access modifiers changed from: package-private */
    public void computeShownFrameLeftLocked() {
    }

    public float[] getPCDisplayModeSurfacePos(Rect tmpSize) {
        return new float[0];
    }

    public void updateBlurLayer(WindowManager.LayoutParams lp) {
        boolean forceRefresh;
        if (this.mSurfaceController != null) {
            int surfaceHashCode = this.mSurfaceController.hashCode();
            if (this.mBlurParams == null) {
                forceRefresh = true;
                this.mBlurParams = new BlurParams(lp, surfaceHashCode);
            } else {
                forceRefresh = this.mBlurParams.updateHashCode(surfaceHashCode);
                this.mBlurParams.set(lp);
            }
            this.mBlurParams.refresh(this.mSurfaceController, forceRefresh);
        }
    }

    /* access modifiers changed from: package-private */
    public void setWindowClipFlag(int flag) {
        if (this.mSurfaceController == null) {
            Slog.e(TAG, "mWindowSurfaceController is null!!");
        } else {
            this.mSurfaceController.setWindowClipFlag(flag);
        }
    }

    /* access modifiers changed from: package-private */
    public void setWindowClipRound(float roundx, float roundy) {
        if (this.mSurfaceController == null) {
            Slog.e(TAG, "mWindowSurfaceController is null!!");
        } else {
            this.mSurfaceController.setWindowClipRound(roundx, roundy);
        }
    }

    /* access modifiers changed from: package-private */
    public void setWindowClipIcon(int iconViewWidth, int iconViewHeight, Bitmap icon) {
        if (this.mSurfaceController == null) {
            Slog.e(TAG, "mWindowSurfaceController is null!!");
        } else {
            this.mSurfaceController.setWindowClipIcon(iconViewWidth, iconViewHeight, icon);
        }
    }

    private void setSurfaceLowResolutionInfo() {
        WindowState w = this.mWin;
        this.mSurfaceController.setSurfaceLowResolutionInfo(w.mGlobalScale, w.getLowResolutionMode());
    }

    /* access modifiers changed from: package-private */
    public boolean isEvilWindow(WindowState win) {
        return false;
    }
}
