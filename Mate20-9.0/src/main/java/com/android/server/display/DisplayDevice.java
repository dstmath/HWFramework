package com.android.server.display;

import android.graphics.Rect;
import android.hardware.display.DisplayViewport;
import android.hardware.display.HwFoldScreenState;
import android.hardware.display.IHwFoldable;
import android.os.IBinder;
import android.os.SystemProperties;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.Surface;
import android.view.SurfaceControl;
import java.io.PrintWriter;

abstract class DisplayDevice implements IHwFoldable {
    private static final String TAG = "DisplayDevice";
    private static boolean mIsRotateScreen = (!SystemProperties.get("ro.config.hw_fold_disp").isEmpty());
    private Rect mCurrentDisplayRect;
    private int mCurrentLayerStack;
    private Rect mCurrentLayerStackRect;
    private int mCurrentOrientation;
    private Surface mCurrentSurface;
    DisplayDeviceInfo mDebugLastLoggedDeviceInfo;
    private final DisplayAdapter mDisplayAdapter;
    private final IBinder mDisplayToken;
    protected HwFoldScreenState mHwFoldScreenState;
    private final String mUniqueId;

    public abstract DisplayDeviceInfo getDisplayDeviceInfoLocked();

    public abstract boolean hasStableUniqueId();

    public DisplayDevice(DisplayAdapter displayAdapter, IBinder displayToken, String uniqueId) {
        this.mCurrentLayerStack = -1;
        this.mCurrentOrientation = -1;
        this.mDisplayAdapter = displayAdapter;
        this.mDisplayToken = displayToken;
        this.mUniqueId = uniqueId;
    }

    public DisplayDevice(DisplayAdapter displayAdapter, IBinder displayToken, String uniqueId, HwFoldScreenState foldScreenState) {
        this(displayAdapter, displayToken, uniqueId);
        this.mHwFoldScreenState = foldScreenState;
    }

    public final DisplayAdapter getAdapterLocked() {
        return this.mDisplayAdapter;
    }

    public final IBinder getDisplayTokenLocked() {
        return this.mDisplayToken;
    }

    public final String getNameLocked() {
        return getDisplayDeviceInfoLocked().name;
    }

    public final String getUniqueId() {
        return this.mUniqueId;
    }

    public void updateDesityforRog() {
    }

    public void applyPendingDisplayDeviceInfoChangesLocked() {
    }

    public void performTraversalLocked(SurfaceControl.Transaction t) {
    }

    public Runnable requestDisplayStateLocked(int state, int brightness) {
        return null;
    }

    public void requestDisplayModesLocked(int colorMode, int modeId) {
    }

    public void onOverlayChangedLocked() {
    }

    public final void setLayerStackLocked(SurfaceControl.Transaction t, int layerStack) {
        if (this.mCurrentLayerStack != layerStack) {
            this.mCurrentLayerStack = layerStack;
            t.setDisplayLayerStack(this.mDisplayToken, layerStack);
        }
    }

    public final void setProjectionLocked(SurfaceControl.Transaction t, int orientation, Rect layerStackRect, Rect displayRect) {
        boolean ifDefault = this.mUniqueId.equals("local:0");
        HwPCUtils.log(TAG, "setProjectionLocked getContext " + this.mDisplayAdapter.getContext() + " getName " + this.mDisplayAdapter.getName() + " getHandler " + this.mDisplayAdapter.getHandler() + " getSyncRoot " + this.mDisplayAdapter.getSyncRoot());
        if (ifDefault) {
            HwPCUtils.log(TAG, "setProjectionLocked ifDefault is true mIsRotateScreen " + mIsRotateScreen);
            if (mIsRotateScreen) {
                orientation = (orientation + 3) % 4;
            }
        }
        if (this.mCurrentOrientation != orientation || this.mCurrentLayerStackRect == null || !this.mCurrentLayerStackRect.equals(layerStackRect) || this.mCurrentDisplayRect == null || !this.mCurrentDisplayRect.equals(displayRect) || isFoldable()) {
            this.mCurrentOrientation = orientation;
            if (this.mCurrentLayerStackRect == null) {
                this.mCurrentLayerStackRect = new Rect();
            }
            this.mCurrentLayerStackRect.set(layerStackRect);
            if (this.mCurrentDisplayRect == null) {
                this.mCurrentDisplayRect = new Rect();
            }
            this.mCurrentDisplayRect.set(displayRect);
            t.setDisplayProjection(this.mDisplayToken, orientation, layerStackRect, displayRect);
        }
    }

    public final void setSurfaceLocked(SurfaceControl.Transaction t, Surface surface) {
        if (this.mCurrentSurface != surface) {
            this.mCurrentSurface = surface;
            t.setDisplaySurface(this.mDisplayToken, surface);
        }
    }

    public final void populateViewportLocked(DisplayViewport viewport) {
        viewport.orientation = this.mCurrentOrientation;
        if (this.mCurrentLayerStackRect != null) {
            viewport.logicalFrame.set(this.mCurrentLayerStackRect);
        } else {
            viewport.logicalFrame.setEmpty();
        }
        if (this.mCurrentDisplayRect != null) {
            viewport.physicalFrame.set(this.mCurrentDisplayRect);
        } else {
            viewport.physicalFrame.setEmpty();
        }
        if (isFoldable()) {
            this.mHwFoldScreenState.adjustViewportFrame(viewport, this.mCurrentLayerStackRect, this.mCurrentDisplayRect);
            Slog.d(TAG, "adjustViewportFrame viewport=" + viewport + " mCurrentOrientation=" + this.mCurrentOrientation);
        }
        boolean z = true;
        if (!(this.mCurrentOrientation == 1 || this.mCurrentOrientation == 3)) {
            z = false;
        }
        boolean isRotated = z;
        DisplayDeviceInfo info = getDisplayDeviceInfoLocked();
        viewport.deviceWidth = isRotated ? info.height : info.width;
        viewport.deviceHeight = isRotated ? info.width : info.height;
    }

    public boolean isFoldable() {
        return false;
    }

    public Rect getScreenDispRect(int orientation) {
        return null;
    }

    public int getDisplayState() {
        return 0;
    }

    public int setDisplayState(int state) {
        return 0;
    }

    public void dumpLocked(PrintWriter pw) {
        pw.println("mAdapter=" + this.mDisplayAdapter.getName());
        pw.println("mUniqueId=" + this.mUniqueId);
        pw.println("mDisplayToken=" + this.mDisplayToken);
        pw.println("mCurrentLayerStack=" + this.mCurrentLayerStack);
        pw.println("mCurrentOrientation=" + this.mCurrentOrientation);
        pw.println("mCurrentLayerStackRect=" + this.mCurrentLayerStackRect);
        pw.println("mCurrentDisplayRect=" + this.mCurrentDisplayRect);
        pw.println("mCurrentSurface=" + this.mCurrentSurface);
    }
}
