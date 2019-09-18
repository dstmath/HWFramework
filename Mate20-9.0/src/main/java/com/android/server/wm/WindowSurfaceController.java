package com.android.server.wm;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Debug;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowContentFrameStats;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

class WindowSurfaceController {
    private static final boolean IS_DEBUG_VERSION;
    static final String TAG = "WindowManager";
    final WindowStateAnimator mAnimator;
    private float mBlurAlpha;
    private Rect mBlurBlank;
    private int mBlurRadius;
    private Region mBlurRegion;
    private int mBlurRoundx;
    private int mBlurRoundy;
    private boolean mHiddenForCrop = false;
    private boolean mHiddenForOtherReasons = true;
    private float mLastDsdx = 1.0f;
    private float mLastDsdy = 0.0f;
    private float mLastDtdx = 0.0f;
    private float mLastDtdy = 1.0f;
    private final WindowManagerService mService;
    private float mSurfaceAlpha = 0.0f;
    SurfaceControl mSurfaceControl;
    private int mSurfaceH = 0;
    int mSurfaceLayer = 0;
    boolean mSurfaceShown = false;
    private int mSurfaceW = 0;
    private float mSurfaceX = 0.0f;
    private float mSurfaceY = 0.0f;
    private final SurfaceControl.Transaction mTmpTransaction = new SurfaceControl.Transaction();
    private final Session mWindowSession;
    private final int mWindowType;
    private final String title;

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_DEBUG_VERSION = z;
    }

    public WindowSurfaceController(SurfaceSession s, String name, int w, int h, int format, int flags, WindowStateAnimator animator, int windowType, int ownerUid) {
        this.mAnimator = animator;
        this.mSurfaceW = w;
        this.mSurfaceH = h;
        this.title = name;
        this.mService = animator.mService;
        WindowState win = animator.mWin;
        this.mWindowType = windowType;
        this.mWindowSession = win.mSession;
        Trace.traceBegin(32, "new SurfaceControl");
        this.mSurfaceControl = win.makeSurface().setParent(win.getSurfaceControl()).setName(name).setSize(w, h).setFormat(format).setFlags(flags).setMetadata(windowType, ownerUid).build();
        Trace.traceEnd(32);
    }

    private void logSurface(String msg, RuntimeException where) {
        String str = "  SURFACE " + msg + ": " + this.title;
        if (where != null) {
            Slog.i(TAG, str, where);
        } else {
            Slog.i(TAG, str);
        }
    }

    /* access modifiers changed from: package-private */
    public void reparentChildrenInTransaction(WindowSurfaceController other) {
        if (this.mSurfaceControl != null && other.mSurfaceControl != null) {
            this.mSurfaceControl.reparentChildren(other.getHandle());
        }
    }

    /* access modifiers changed from: package-private */
    public void detachChildren() {
        if (this.mSurfaceControl != null) {
            this.mSurfaceControl.detachChildren();
        }
    }

    /* access modifiers changed from: package-private */
    public void hide(SurfaceControl.Transaction transaction, String reason) {
        this.mHiddenForOtherReasons = true;
        this.mAnimator.destroyPreservedSurfaceLocked();
        if (this.mSurfaceShown) {
            hideSurface(transaction);
        }
    }

    private void hideSurface(SurfaceControl.Transaction transaction) {
        if (this.mSurfaceControl != null) {
            setShown(false);
            try {
                transaction.hide(this.mSurfaceControl);
                synchronized (this.mAnimator.mInsetSurfaceLock) {
                    if (this.mAnimator.mInsetSurfaceOverlay != null) {
                        this.mAnimator.mInsetSurfaceOverlay.hide(transaction);
                    }
                }
            } catch (RuntimeException e) {
                Slog.w(TAG, "Exception hiding surface in " + this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void destroyNotInTransaction() {
        if (IS_DEBUG_VERSION) {
            Slog.i(TAG, "Destroying surface " + this + " called by " + Debug.getCallers(8));
        }
        try {
            this.mAnimator.mWin.mClient.updateSurfaceStatus(false);
        } catch (RemoteException e) {
            Slog.w(TAG, "Exception thrown when updateSurfaceStatus" + this + ": " + e);
        }
        try {
            if (this.mSurfaceControl != null) {
                this.mSurfaceControl.destroy();
            }
            setShown(false);
            this.mSurfaceControl = null;
            synchronized (this.mAnimator.mInsetSurfaceLock) {
                if (this.mAnimator.mInsetSurfaceOverlay != null) {
                    this.mAnimator.mInsetSurfaceOverlay.destroy();
                    this.mAnimator.mInsetSurfaceOverlay = null;
                }
            }
        } catch (RuntimeException e2) {
            try {
                Slog.w(TAG, "Error destroying surface in: " + this, e2);
                setShown(false);
                this.mSurfaceControl = null;
                synchronized (this.mAnimator.mInsetSurfaceLock) {
                    if (this.mAnimator.mInsetSurfaceOverlay != null) {
                        this.mAnimator.mInsetSurfaceOverlay.destroy();
                        this.mAnimator.mInsetSurfaceOverlay = null;
                    }
                }
            } catch (Throwable th) {
                setShown(false);
                this.mSurfaceControl = null;
                synchronized (this.mAnimator.mInsetSurfaceLock) {
                    if (this.mAnimator.mInsetSurfaceOverlay != null) {
                        this.mAnimator.mInsetSurfaceOverlay.destroy();
                        this.mAnimator.mInsetSurfaceOverlay = null;
                    }
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void disconnectInTransaction() {
        try {
            if (this.mSurfaceControl != null) {
                this.mSurfaceControl.disconnect();
            }
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error disconnecting surface in: " + this, e);
        }
    }

    /* access modifiers changed from: package-private */
    public void setCropInTransaction(Rect clipRect, boolean recoveringMemory) {
        try {
            if (clipRect.width() <= 0 || clipRect.height() <= 0) {
                this.mHiddenForCrop = true;
                this.mAnimator.destroyPreservedSurfaceLocked();
                updateVisibility();
                return;
            }
            this.mSurfaceControl.setWindowCrop(clipRect);
            this.mHiddenForCrop = false;
            updateVisibility();
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error setting crop surface of " + this + " crop=" + clipRect.toShortString(), e);
            if (!recoveringMemory) {
                this.mAnimator.reclaimSomeSurfaceMemory("crop", true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void clearCropInTransaction(boolean recoveringMemory) {
        try {
            this.mSurfaceControl.setWindowCrop(new Rect(0, 0, -1, -1));
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error setting clearing crop of " + this, e);
            if (!recoveringMemory) {
                this.mAnimator.reclaimSomeSurfaceMemory("crop", true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setFinalCropInTransaction(Rect clipRect) {
        try {
            this.mSurfaceControl.setFinalCrop(clipRect);
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error disconnecting surface in: " + this, e);
        }
    }

    /* access modifiers changed from: package-private */
    public void setLayerStackInTransaction(int layerStack) {
        if (this.mSurfaceControl != null) {
            this.mSurfaceControl.setLayerStack(layerStack);
        }
    }

    /* access modifiers changed from: package-private */
    public void setPositionInTransaction(float left, float top, boolean recoveringMemory) {
        setPosition(null, left, top, recoveringMemory);
    }

    /* access modifiers changed from: package-private */
    public void setPosition(SurfaceControl.Transaction t, float left, float top, boolean recoveringMemory) {
        if ((this.mSurfaceX == left && this.mSurfaceY == top) ? false : true) {
            this.mSurfaceX = left;
            this.mSurfaceY = top;
            if (t == null) {
                try {
                    this.mSurfaceControl.setPosition(left, top);
                } catch (RuntimeException e) {
                    Slog.w(TAG, "Error positioning surface of " + this + " pos=(" + left + "," + top + ")", e);
                    if (!recoveringMemory) {
                        this.mAnimator.reclaimSomeSurfaceMemory("position", true);
                    }
                }
            } else {
                t.setPosition(this.mSurfaceControl, left, top);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setGeometryAppliesWithResizeInTransaction(boolean recoveringMemory) {
        this.mSurfaceControl.setGeometryAppliesWithResize();
    }

    /* access modifiers changed from: package-private */
    public void setMatrixInTransaction(float dsdx, float dtdx, float dtdy, float dsdy, boolean recoveringMemory) {
        setMatrix(null, dsdx, dtdx, dtdy, dsdy, false);
    }

    /* access modifiers changed from: package-private */
    public void setMatrix(SurfaceControl.Transaction t, float dsdx, float dtdx, float dtdy, float dsdy, boolean recoveringMemory) {
        if ((this.mLastDsdx == dsdx && this.mLastDtdx == dtdx && this.mLastDtdy == dtdy && this.mLastDsdy == dsdy) ? false : true) {
            this.mLastDsdx = dsdx;
            this.mLastDtdx = dtdx;
            this.mLastDtdy = dtdy;
            this.mLastDsdy = dsdy;
            if (t == null) {
                try {
                    this.mSurfaceControl.setMatrix(dsdx, dtdx, dtdy, dsdy);
                } catch (RuntimeException e) {
                    Slog.e(TAG, "Error setting matrix on surface surface" + this.title + " MATRIX [" + dsdx + "," + dtdx + "," + dtdy + "," + dsdy + "]", null);
                    if (!recoveringMemory) {
                        this.mAnimator.reclaimSomeSurfaceMemory("matrix", true);
                    }
                }
            } else {
                t.setMatrix(this.mSurfaceControl, dsdx, dtdx, dtdy, dsdy);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean setSizeInTransaction(int width, int height, boolean recoveringMemory) {
        if (!((this.mSurfaceW == width && this.mSurfaceH == height) ? false : true)) {
            return false;
        }
        this.mSurfaceW = width;
        this.mSurfaceH = height;
        try {
            if (this.mAnimator.mWin.mAppToken != null) {
                logSurface("SIZE " + width + "x" + height, null);
            }
            this.mSurfaceControl.setSize(width, height);
            synchronized (this.mAnimator.mInsetSurfaceLock) {
                if (this.mAnimator.mInsetSurfaceOverlay != null && this.mService.mPolicy.isInputMethodMovedUp()) {
                    this.mAnimator.mInsetSurfaceOverlay.updateSurface(this.mAnimator.mWin);
                }
            }
            return true;
        } catch (RuntimeException e) {
            Slog.e(TAG, "Error resizing surface of " + this.title + " size=(" + width + "x" + height + ")", e);
            if (!recoveringMemory) {
                this.mAnimator.reclaimSomeSurfaceMemory("size", true);
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean prepareToShowInTransaction(float alpha, float dsdx, float dtdx, float dsdy, float dtdy, boolean recoveringMemory) {
        if (this.mSurfaceControl != null) {
            try {
                this.mSurfaceAlpha = alpha;
                this.mSurfaceControl.setAlpha(alpha);
                this.mLastDsdx = dsdx;
                this.mLastDtdx = dtdx;
                this.mLastDsdy = dsdy;
                this.mLastDtdy = dtdy;
                this.mSurfaceControl.setMatrix(dsdx, dtdx, dsdy, dtdy);
            } catch (RuntimeException e) {
                Slog.w(TAG, "Error updating surface in " + this.title, e);
                if (!recoveringMemory) {
                    this.mAnimator.reclaimSomeSurfaceMemory("update", true);
                }
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setTransparentRegionHint(Region region) {
        String str;
        if (this.mSurfaceControl == null) {
            Slog.w(TAG, "setTransparentRegionHint: null mSurface after mHasSurface true");
            return;
        }
        this.mService.openSurfaceTransaction();
        try {
            this.mSurfaceControl.setTransparentRegionHint(region);
        } finally {
            str = "setTransparentRegion";
            this.mService.closeSurfaceTransaction(str);
        }
    }

    /* access modifiers changed from: package-private */
    public void setOpaque(boolean isOpaque) {
        String str;
        if (this.mSurfaceControl != null) {
            this.mService.openSurfaceTransaction();
            try {
                this.mSurfaceControl.setOpaque(isOpaque);
            } finally {
                str = "setOpaqueLocked";
                this.mService.closeSurfaceTransaction(str);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setSecure(boolean isSecure) {
        String str;
        if (this.mSurfaceControl != null) {
            this.mService.openSurfaceTransaction();
            try {
                this.mSurfaceControl.setSecure(isSecure);
            } finally {
                str = "setSecure";
                this.mService.closeSurfaceTransaction(str);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void getContainerRect(Rect rect) {
        this.mAnimator.getContainerRect(rect);
    }

    /* access modifiers changed from: package-private */
    public boolean showRobustlyInTransaction() {
        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.v(TAG, "Showing " + this + " during relayout");
        }
        this.mHiddenForOtherReasons = false;
        return updateVisibility();
    }

    private boolean updateVisibility() {
        if (this.mHiddenForCrop || this.mHiddenForOtherReasons) {
            if (this.mSurfaceShown) {
                hideSurface(this.mTmpTransaction);
                SurfaceControl.mergeToGlobalTransaction(this.mTmpTransaction);
            }
            return false;
        } else if (!this.mSurfaceShown) {
            return showSurface();
        } else {
            return true;
        }
    }

    private boolean showSurface() {
        try {
            setShown(true);
            this.mSurfaceControl.show();
            synchronized (this.mAnimator.mInsetSurfaceLock) {
                if (this.mAnimator.mInsetSurfaceOverlay != null && this.mService.mPolicy.isInputMethodMovedUp()) {
                    this.mAnimator.mInsetSurfaceOverlay.show();
                }
            }
            return true;
        } catch (RuntimeException e) {
            Slog.w(TAG, "Failure showing surface " + this.mSurfaceControl + " in " + this, e);
            this.mAnimator.reclaimSomeSurfaceMemory("show", true);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void deferTransactionUntil(IBinder handle, long frame) {
        this.mSurfaceControl.deferTransactionUntil(handle, frame);
    }

    /* access modifiers changed from: package-private */
    public void forceScaleableInTransaction(boolean force) {
        this.mSurfaceControl.setOverrideScalingMode(force ? 1 : -1);
    }

    /* access modifiers changed from: package-private */
    public boolean clearWindowContentFrameStats() {
        if (this.mSurfaceControl == null) {
            return false;
        }
        return this.mSurfaceControl.clearContentFrameStats();
    }

    /* access modifiers changed from: package-private */
    public boolean getWindowContentFrameStats(WindowContentFrameStats outStats) {
        if (this.mSurfaceControl == null) {
            return false;
        }
        return this.mSurfaceControl.getContentFrameStats(outStats);
    }

    /* access modifiers changed from: package-private */
    public boolean hasSurface() {
        return this.mSurfaceControl != null;
    }

    /* access modifiers changed from: package-private */
    public IBinder getHandle() {
        if (this.mSurfaceControl == null) {
            return null;
        }
        return this.mSurfaceControl.getHandle();
    }

    /* access modifiers changed from: package-private */
    public void getSurface(Surface outSurface) {
        outSurface.copyFrom(this.mSurfaceControl);
    }

    /* access modifiers changed from: package-private */
    public int getLayer() {
        return this.mSurfaceLayer;
    }

    /* access modifiers changed from: package-private */
    public boolean getShown() {
        return this.mSurfaceShown;
    }

    /* access modifiers changed from: package-private */
    public void setShown(boolean surfaceShown) {
        this.mSurfaceShown = surfaceShown;
        this.mService.updateNonSystemOverlayWindowsVisibilityIfNeeded(this.mAnimator.mWin, surfaceShown);
        if (this.mWindowSession != null) {
            this.mWindowSession.onWindowSurfaceVisibilityChanged(this, this.mSurfaceShown, this.mWindowType);
        }
    }

    /* access modifiers changed from: package-private */
    public float getX() {
        return this.mSurfaceX;
    }

    /* access modifiers changed from: package-private */
    public float getY() {
        return this.mSurfaceY;
    }

    /* access modifiers changed from: package-private */
    public int getWidth() {
        return this.mSurfaceW;
    }

    /* access modifiers changed from: package-private */
    public int getHeight() {
        return this.mSurfaceH;
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1133871366145L, this.mSurfaceShown);
        proto.write(1120986464258L, this.mSurfaceLayer);
        proto.end(token);
    }

    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mSurface=");
            pw.println(this.mSurfaceControl);
        }
        pw.print(prefix);
        pw.print("Surface: shown=");
        pw.print(this.mSurfaceShown);
        pw.print(" layer=");
        pw.print(this.mSurfaceLayer);
        pw.print(" alpha=");
        pw.print(this.mSurfaceAlpha);
        pw.print(" rect=(");
        pw.print(this.mSurfaceX);
        pw.print(",");
        pw.print(this.mSurfaceY);
        pw.print(") ");
        pw.print(this.mSurfaceW);
        pw.print(" x ");
        pw.print(this.mSurfaceH);
        pw.print(" transform=(");
        pw.print(this.mLastDsdx);
        pw.print(", ");
        pw.print(this.mLastDtdx);
        pw.print(", ");
        pw.print(this.mLastDsdy);
        pw.print(", ");
        pw.print(this.mLastDtdy);
        pw.println(")");
        pw.print(prefix);
        pw.print("blurRadius=");
        pw.print(this.mBlurRadius);
        pw.print(" blurRound=(");
        pw.print(this.mBlurRoundx);
        pw.print(",");
        pw.print(this.mBlurRoundy);
        pw.print(")");
        pw.print(" blurAlpha=");
        pw.print(this.mBlurAlpha);
        pw.print(" blurRegion=");
        pw.print(this.mBlurRegion);
        pw.print(" blurBlank=");
        pw.println(this.mBlurBlank);
    }

    public String toString() {
        return this.mSurfaceControl.toString();
    }

    public void setBlurRadius(int radius) {
        SurfaceControl.openTransaction();
        try {
            this.mBlurRadius = radius;
            this.mSurfaceControl.setBlurRadius(radius);
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error creating surface in " + this, e);
            this.mAnimator.reclaimSomeSurfaceMemory("blur-radius", true);
        } catch (Throwable th) {
            SurfaceControl.closeTransaction();
            throw th;
        }
        SurfaceControl.closeTransaction();
    }

    public void setBlurRound(int rx, int ry) {
        SurfaceControl.openTransaction();
        try {
            this.mBlurRoundx = rx;
            this.mBlurRoundy = ry;
            this.mSurfaceControl.setBlurRound(rx, ry);
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error creating surface in " + this, e);
            this.mAnimator.reclaimSomeSurfaceMemory("blur-round", true);
        } catch (Throwable th) {
            SurfaceControl.closeTransaction();
            throw th;
        }
        SurfaceControl.closeTransaction();
    }

    public void setBlurAlpha(float alpha) {
        SurfaceControl.openTransaction();
        try {
            this.mBlurAlpha = alpha;
            this.mSurfaceControl.setBlurAlpha(alpha);
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error creating surface in " + this, e);
            this.mAnimator.reclaimSomeSurfaceMemory("blur-alpha", true);
        } catch (Throwable th) {
            SurfaceControl.closeTransaction();
            throw th;
        }
        SurfaceControl.closeTransaction();
    }

    public void setBlurRegion(Region region) {
        SurfaceControl.openTransaction();
        try {
            this.mBlurRegion = region;
            this.mSurfaceControl.setBlurRegion(region);
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error creating surface in " + this, e);
            this.mAnimator.reclaimSomeSurfaceMemory("blur-region", true);
        } catch (Throwable th) {
            SurfaceControl.closeTransaction();
            throw th;
        }
        SurfaceControl.closeTransaction();
    }

    public void setBlurBlank(Rect rect) {
        SurfaceControl.openTransaction();
        try {
            this.mBlurBlank = rect;
            this.mSurfaceControl.setBlurBlank(rect);
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error creating surface in " + this, e);
            this.mAnimator.reclaimSomeSurfaceMemory("blur-blank", true);
        } catch (Throwable th) {
            SurfaceControl.closeTransaction();
            throw th;
        }
        SurfaceControl.closeTransaction();
    }

    public void clearWindowClipFlag() {
        if (this.mSurfaceControl == null) {
            Slog.e(TAG, "mSurfaceControl is null!!!");
            return;
        }
        this.mSurfaceControl.setWindowClipFlag(0);
        this.mSurfaceControl.setWindowClipRound(0.0f, 0.0f);
    }

    public void setWindowClipFlag(int flag) {
        if (this.mSurfaceControl == null) {
            Slog.e(TAG, "mSurfaceControl is null!!!");
        } else {
            this.mSurfaceControl.setWindowClipFlag(flag);
        }
    }

    public void setWindowClipRound(float roundx, float roundy) {
        if (this.mSurfaceControl == null) {
            Slog.e(TAG, "mSurfaceControl is null!!!");
        } else {
            this.mSurfaceControl.setWindowClipRound(roundx, roundy);
        }
    }

    public void setWindowClipIcon(int iconViewWidth, int iconViewHeight, Bitmap icon) {
        if (this.mSurfaceControl == null) {
            Slog.e(TAG, "mSurfaceControl is null!!!");
        } else if (icon == null) {
            Slog.e(TAG, "icon is null!!");
        } else {
            int width = icon.getWidth();
            int height = icon.getHeight();
            int byteCount = icon.getRowBytes() * icon.getHeight();
            ByteBuffer byteBuffer = ByteBuffer.allocate(byteCount);
            icon.copyPixelsToBuffer(byteBuffer);
            this.mSurfaceControl.setWindowClipIcon(iconViewWidth, iconViewHeight, byteBuffer.array(), byteCount, width, height);
        }
    }

    /* access modifiers changed from: package-private */
    public void setSurfaceLowResolutionInfo(float ratio, int mode) {
        this.mSurfaceControl.setSurfaceLowResolutionInfo(ratio, mode);
    }

    /* access modifiers changed from: package-private */
    public void setSecureScreenShot(boolean isSecureScreenShot) {
        String str;
        if (this.mSurfaceControl != null) {
            this.mService.openSurfaceTransaction();
            try {
                this.mSurfaceControl.setSecureScreenShot(isSecureScreenShot);
            } finally {
                str = "setSecureScreenShot";
                this.mService.closeSurfaceTransaction(str);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setSecureScreenRecord(boolean isSecureScreenRecord) {
        String str;
        if (this.mSurfaceControl != null) {
            this.mService.openSurfaceTransaction();
            try {
                this.mSurfaceControl.setSecureScreenRecord(isSecureScreenRecord);
            } finally {
                str = "setSecureScreenRecord";
                this.mService.closeSurfaceTransaction(str);
            }
        }
    }
}
