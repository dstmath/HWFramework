package com.android.server.wm;

import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.display.HwFoldScreenState;
import android.os.Debug;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Flog;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowContentFrameStats;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

/* access modifiers changed from: package-private */
public class WindowSurfaceController {
    private static final boolean IS_DEBUG_VERSION;
    static final String TAG = "WindowManager";
    private boolean isFirstTime = true;
    final WindowStateAnimator mAnimator;
    private boolean mHiddenForCrop = false;
    private boolean mHiddenForOtherReasons = true;
    private float mLastDsdx = 1.0f;
    private float mLastDsdy = 0.0f;
    private float mLastDtdx = 0.0f;
    private float mLastDtdy = 1.0f;
    private final WindowManagerService mService;
    private float mSurfaceAlpha = 0.0f;
    SurfaceControl mSurfaceControl;
    private Rect mSurfaceCrop = new Rect(0, 0, -1, -1);
    private int mSurfaceH = 0;
    private int mSurfaceLayer = 0;
    private boolean mSurfaceShown = false;
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
        this.mSurfaceControl = win.makeSurface().setParent(win.getSurfaceControl()).setName(name).setBufferSize(w, h).setFormat(format).setFlags(flags).setMetadata(2, windowType).setMetadata(1, ownerUid).build();
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
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null && other.mSurfaceControl != null) {
            surfaceControl.reparentChildren(other.getHandle());
        }
    }

    /* access modifiers changed from: package-private */
    public void detachChildren() {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.detachChildren();
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

    private boolean isExpandInputMethodWindowShow() {
        DisplayContent displayContent = this.mService.getDefaultDisplayContentLocked();
        WindowState inputMethodWindow = displayContent.mInputMethodWindow;
        DisplayPolicy displayPolicy = displayContent.getDisplayPolicy();
        if (displayPolicy == null || inputMethodWindow == null || displayPolicy.getHwDisplayPolicyEx().isNeedExceptDisplaySide(inputMethodWindow.getAttrs(), inputMethodWindow, displayContent.getDisplayPolicy().getDisplayRotation())) {
            return false;
        }
        return true;
    }

    private void hideSurface(SurfaceControl.Transaction transaction) {
        if (this.mSurfaceControl != null) {
            setShown(false);
            try {
                transaction.hide(this.mSurfaceControl);
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
            if (this.mSurfaceControl != null) {
                this.mSurfaceControl.remove();
            }
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error destroying surface in: " + this, e);
        } catch (Throwable th) {
            setShown(false);
            this.mSurfaceControl = null;
            throw th;
        }
        setShown(false);
        this.mSurfaceControl = null;
    }

    /* access modifiers changed from: package-private */
    public void setCropInTransaction(Rect clipRect, boolean recoveringMemory) {
        if (HwFoldScreenState.isFoldScreenDevice() && this.title.contains("com.huawei.camera")) {
            logSurface("CROP " + clipRect.toShortString(), null);
        }
        try {
            if (clipRect.width() <= 0 || clipRect.height() <= 0) {
                this.mHiddenForCrop = true;
                this.mAnimator.destroyPreservedSurfaceLocked();
                updateVisibility();
                return;
            }
            if (!clipRect.equals(this.mSurfaceCrop)) {
                this.mSurfaceControl.setWindowCrop(clipRect);
                this.mSurfaceCrop.set(clipRect);
            }
            this.mHiddenForCrop = false;
            if (updateVisibility() && this.isFirstTime && this.title.contains("NavigationBar")) {
                this.isFirstTime = false;
                this.mAnimator.mLastHidden = false;
            }
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
            Rect clipRect = new Rect(0, 0, -1, -1);
            if (!this.mSurfaceCrop.equals(clipRect)) {
                this.mSurfaceControl.setWindowCrop(clipRect);
                this.mSurfaceCrop.set(clipRect);
            }
        } catch (RuntimeException e) {
            Slog.w(TAG, "Error setting clearing crop of " + this, e);
            if (!recoveringMemory) {
                this.mAnimator.reclaimSomeSurfaceMemory("crop", true);
            }
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
            try {
                if (HwFoldScreenState.isFoldScreenDevice() && this.title.contains("com.huawei.camera")) {
                    logSurface("POS (setPositionInTransaction) @ (" + left + "," + top + ")", null);
                }
                if (t == null) {
                    this.mSurfaceControl.setPosition(left, top);
                } else {
                    t.setPosition(this.mSurfaceControl, left, top);
                }
            } catch (RuntimeException e) {
                Slog.w(TAG, "Error positioning surface of " + this + " pos=(" + left + "," + top + ")", e);
                if (!recoveringMemory) {
                    this.mAnimator.reclaimSomeSurfaceMemory("position", true);
                }
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
                    Slog.e(TAG, "Error setting matrix on surface surface" + this.title + " MATRIX [" + dsdx + "," + dtdx + "," + dtdy + "," + dsdy + "]", (Throwable) null);
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
    public boolean setBufferSizeInTransaction(int width, int height, boolean recoveringMemory) {
        if (!((this.mSurfaceW == width && this.mSurfaceH == height) ? false : true)) {
            return false;
        }
        this.mSurfaceW = width;
        this.mSurfaceH = height;
        try {
            if (this.mAnimator.mWin.mAppToken != null) {
                logSurface("SIZE " + width + "x" + height, null);
            }
            this.mSurfaceControl.setBufferSize(width, height);
            if (HwFoldScreenState.isFoldScreenDevice()) {
                Slog.w(TAG, "setBufferSize " + this.title + " " + width + "x" + height);
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
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            try {
                this.mSurfaceAlpha = alpha;
                surfaceControl.setAlpha(alpha);
                this.mLastDsdx = dsdx;
                this.mLastDtdx = dtdx;
                this.mLastDsdy = dsdy;
                this.mLastDtdy = dtdy;
                this.mSurfaceControl.setMatrix(dsdx, dtdx, dsdy, dtdy);
            } catch (RuntimeException e) {
                Slog.w(TAG, "Error updating surface in " + this.title, e);
                if (recoveringMemory) {
                    return false;
                }
                this.mAnimator.reclaimSomeSurfaceMemory("update", true);
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setTransparentRegionHint(Region region) {
        if (this.mSurfaceControl == null) {
            Slog.w(TAG, "setTransparentRegionHint: null mSurface after mHasSurface true");
            return;
        }
        this.mService.openSurfaceTransaction();
        try {
            this.mSurfaceControl.setTransparentRegionHint(region);
        } finally {
            this.mService.closeSurfaceTransaction("setTransparentRegion");
        }
    }

    /* access modifiers changed from: package-private */
    public void setOpaque(boolean isOpaque) {
        if (this.mSurfaceControl != null) {
            this.mService.openSurfaceTransaction();
            try {
                this.mSurfaceControl.setOpaque(isOpaque);
            } finally {
                this.mService.closeSurfaceTransaction("setOpaqueLocked");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setSecure(boolean isSecure) {
        if (this.mSurfaceControl != null) {
            this.mService.openSurfaceTransaction();
            try {
                this.mSurfaceControl.setSecure(isSecure);
            } finally {
                this.mService.closeSurfaceTransaction("setSecure");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setColorSpaceAgnostic(boolean agnostic) {
        if (this.mSurfaceControl != null) {
            this.mService.openSurfaceTransaction();
            try {
                this.mSurfaceControl.setColorSpaceAgnostic(agnostic);
            } finally {
                this.mService.closeSurfaceTransaction("setColorSpaceAgnostic");
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
            if (!this.mSurfaceShown) {
                return false;
            }
            hideSurface(this.mTmpTransaction);
            SurfaceControl.mergeToGlobalTransaction(this.mTmpTransaction);
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
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl == null) {
            return false;
        }
        return surfaceControl.clearContentFrameStats();
    }

    /* access modifiers changed from: package-private */
    public boolean getWindowContentFrameStats(WindowContentFrameStats outStats) {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl == null) {
            return false;
        }
        return surfaceControl.getContentFrameStats(outStats);
    }

    /* access modifiers changed from: package-private */
    public boolean hasSurface() {
        return this.mSurfaceControl != null;
    }

    /* access modifiers changed from: package-private */
    public IBinder getHandle() {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl == null) {
            return null;
        }
        return surfaceControl.getHandle();
    }

    /* access modifiers changed from: package-private */
    public void getSurfaceControl(SurfaceControl outSurfaceControl) {
        outSurfaceControl.copyFrom(this.mSurfaceControl);
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
        StringBuilder sb = new StringBuilder();
        sb.append(this.mAnimator.mWin != null ? this.mAnimator.mWin : this);
        sb.append(" setShown ");
        sb.append(surfaceShown);
        Flog.i(307, sb.toString());
        this.mSurfaceShown = surfaceShown;
        this.mService.updateNonSystemOverlayWindowsVisibilityIfNeeded(this.mAnimator.mWin, surfaceShown);
        Session session = this.mWindowSession;
        if (session != null) {
            session.onWindowSurfaceVisibilityChanged(this, this.mSurfaceShown, this.mWindowType);
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
    }

    public String toString() {
        return this.mSurfaceControl.toString();
    }

    /* access modifiers changed from: package-private */
    public void setSecureScreenShot(boolean isSecureScreenShot) {
        if (this.mSurfaceControl != null) {
            this.mService.openSurfaceTransaction();
            try {
                this.mSurfaceControl.setSecureScreenShot(isSecureScreenShot);
            } finally {
                this.mService.closeSurfaceTransaction("setSecureScreenShot");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setSecureScreenRecord(boolean isSecureScreenRecord) {
        if (this.mSurfaceControl != null) {
            this.mService.openSurfaceTransaction();
            try {
                this.mSurfaceControl.setSecureScreenRecord(isSecureScreenRecord);
            } finally {
                this.mService.closeSurfaceTransaction("setSecureScreenRecord");
            }
        }
    }

    public void setWindowIconInfo(int iconType, int iconViewWidth, int iconViewHeight, Bitmap icon) {
        if (this.mSurfaceControl == null) {
            Slog.e(TAG, "mSurfaceControl is null!!!");
        } else if (icon == null) {
            Slog.e(TAG, "icon is null!!");
            this.mSurfaceControl.setWindowIconInfo(0, 0, 0, new byte[1], 1, 0, 0, 0);
        } else {
            int width = icon.getWidth();
            int height = icon.getHeight();
            ColorSpace colorSpace = icon.getColorSpace();
            if (colorSpace == null) {
                colorSpace = ColorSpace.get(ColorSpace.Named.SRGB);
            }
            int colorSpaceId = colorSpace.getId();
            Slog.i(TAG, "colorSpace = " + colorSpace);
            int byteCount = icon.getRowBytes() * icon.getHeight();
            ByteBuffer byteBuffer = ByteBuffer.allocate(byteCount);
            icon.copyPixelsToBuffer(byteBuffer);
            this.mSurfaceControl.setWindowIconInfo(iconType, iconViewWidth, iconViewHeight, byteBuffer.array(), byteCount, width, height, colorSpaceId);
        }
    }

    /* access modifiers changed from: package-private */
    public void setBlurStyle(int behindLayerBlurStyle, int frontLayerBlurStyle) {
        if (this.mSurfaceControl != null) {
            this.mService.openSurfaceTransaction();
            try {
                this.mSurfaceControl.setBehindLayerBlurStyle(behindLayerBlurStyle);
                this.mSurfaceControl.setFrontLayerBlurStyle(frontLayerBlurStyle);
            } finally {
                this.mService.closeSurfaceTransaction("setBlurStyle");
            }
        }
    }
}
