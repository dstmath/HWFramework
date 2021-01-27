package com.android.server.display;

import android.graphics.Rect;
import android.hardware.display.DisplayViewport;
import android.hardware.display.HwFoldScreenState;
import android.hardware.display.IHwFoldable;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.DisplayAddress;
import android.view.Surface;
import android.view.SurfaceControl;
import com.android.server.HwServiceFactory;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.hardware.display.DisplayViewportEx;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public abstract class DisplayDevice implements IHwFoldable {
    private static final float FLOAT_COMPARE_VALUE = 0.001f;
    public static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", ""));
    public static final int PHY_SCREEN_ROTATE = SystemProperties.getInt("hw_sc.display.phy_screen_rotate", 0);
    private static final String TAG = "DisplayDevice";
    private Rect mCurrentDisplayRect;
    private int mCurrentLayerStack = -1;
    private Rect mCurrentLayerStackRect;
    private int mCurrentOrientation = -1;
    private Surface mCurrentSurface;
    DisplayDeviceInfo mDebugLastLoggedDeviceInfo;
    private final DisplayAdapter mDisplayAdapter;
    private final IBinder mDisplayToken;
    protected FoldPolicy mHwFoldPolicy;
    private IBinder mSurfaceFlinger;
    private final String mUniqueId;

    public abstract DisplayDeviceInfo getDisplayDeviceInfoLocked();

    public abstract boolean hasStableUniqueId();

    public DisplayDevice(DisplayAdapter displayAdapter, IBinder displayToken, String uniqueId) {
        this.mDisplayAdapter = displayAdapter;
        this.mDisplayToken = displayToken;
        this.mUniqueId = uniqueId;
        if (HwFoldScreenState.isFoldScreenDevice()) {
            this.mHwFoldPolicy = HwServiceFactory.getHwFoldPolicy(displayAdapter.getContext());
        }
        this.mSurfaceFlinger = ServiceManager.getService("SurfaceFlinger");
        if (this.mSurfaceFlinger == null) {
            Slog.e(TAG, "getService SurfaceFlinger failed");
        }
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

    public void setAllowedDisplayModesLocked(int[] modes) {
    }

    public void setRequestedColorModeLocked(int colorMode) {
    }

    public void onOverlayChangedLocked() {
    }

    public final void setLayerStackLocked(SurfaceControl.Transaction t, int layerStack) {
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            int layerStackPC = HwPCUtils.getPCDisplayID();
            if (this.mCurrentLayerStack != layerStackPC) {
                this.mCurrentLayerStack = layerStackPC;
                t.setDisplayLayerStack(this.mDisplayToken, layerStackPC);
            }
        } else if (this.mCurrentLayerStack != layerStack) {
            this.mCurrentLayerStack = layerStack;
            t.setDisplayLayerStack(this.mDisplayToken, layerStack);
        }
    }

    public final void setProjectionLocked(SurfaceControl.Transaction t, int orientation, Rect layerStackRect, Rect displayRect, int displayId) {
        Rect rect;
        Rect rect2;
        Slog.i(TAG, "setProjectionLocked displayId " + displayId + " orientation(" + this.mCurrentOrientation + "->" + orientation + ") LayerStackRect(" + this.mCurrentLayerStackRect + "->" + layerStackRect + ") DisplayRect(" + this.mCurrentDisplayRect + "->" + displayRect + ")");
        boolean isDefaultDisplay = "local:0".equals(this.mUniqueId);
        if (IS_TABLET && isDefaultDisplay && PHY_SCREEN_ROTATE == 3) {
            orientation = (orientation + 3) % 4;
        }
        if (this.mCurrentOrientation != orientation || (rect = this.mCurrentLayerStackRect) == null || !rect.equals(layerStackRect) || (rect2 = this.mCurrentDisplayRect) == null || !rect2.equals(displayRect) || HwActivityTaskManager.isPCMultiCastMode()) {
            boolean isConsistentRect = isConsistentRect(layerStackRect, displayRect);
            this.mCurrentOrientation = orientation;
            if (this.mCurrentLayerStackRect == null) {
                this.mCurrentLayerStackRect = new Rect();
            }
            this.mCurrentLayerStackRect.set(layerStackRect);
            if (this.mCurrentDisplayRect == null) {
                this.mCurrentDisplayRect = new Rect();
            }
            this.mCurrentDisplayRect.set(displayRect);
            HwActivityTaskManager.adjustRectForPCCast(this.mUniqueId, layerStackRect, displayRect, this.mCurrentLayerStackRect, this.mCurrentDisplayRect);
            if (HwActivityTaskManager.isCastDisplay(this.mUniqueId, "padCast") && !isConsistentRect) {
                displayRect.scale(Settings.Global.getFloat(this.mDisplayAdapter.getContext().getContentResolver(), "multidisplay_scalingratio", 1.0f));
            }
            t.setDisplayProjection(this.mDisplayToken, orientation, layerStackRect, displayRect);
            return;
        }
        boolean forceUpdate = false;
        if (displayId == 0) {
            forceUpdate = isNeedRefreshOrientaion(orientation);
        }
        if (forceUpdate) {
            Slog.i(TAG, "setProjectionLocked forceUpdate");
            t.setDisplayProjection(this.mDisplayToken, orientation, layerStackRect, displayRect);
        }
    }

    private boolean isConsistentRect(Rect layerStackRect, Rect displayRect) {
        Rect rect = this.mCurrentDisplayRect;
        if (!(rect == null || !rect.equals(displayRect) || layerStackRect == null || this.mCurrentLayerStackRect == null)) {
            float ratio = layerStackRect.width() != 0 ? ((float) layerStackRect.height()) / ((float) layerStackRect.width()) : 0.0f;
            float currentRatio1 = this.mCurrentLayerStackRect.width() != 0 ? ((float) this.mCurrentLayerStackRect.height()) / ((float) this.mCurrentLayerStackRect.width()) : 0.0f;
            float currentRatio2 = this.mCurrentLayerStackRect.height() != 0 ? ((float) this.mCurrentLayerStackRect.width()) / ((float) this.mCurrentLayerStackRect.height()) : 0.0f;
            if (ratio != 0.0f && currentRatio1 != 0.0f && currentRatio2 != 0.0f && (Math.abs(ratio - currentRatio1) <= FLOAT_COMPARE_VALUE || Math.abs(ratio - currentRatio2) <= FLOAT_COMPARE_VALUE)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNeedRefreshOrientaion(int newOrientaion) {
        boolean z = false;
        if (this.mSurfaceFlinger != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                if (this.mSurfaceFlinger.transact(7100, data, reply, 0)) {
                    int orientation = reply.readInt();
                    Slog.i(TAG, "isNeedRefreshOrientaion orientation " + orientation);
                    if (orientation != newOrientaion) {
                        z = true;
                    }
                    reply.recycle();
                    data.recycle();
                    return z;
                }
                Slog.e(TAG, "isNeedRefreshOrientaion transact failed");
                reply.recycle();
                data.recycle();
            } catch (RemoteException exception) {
                Slog.e(TAG, "isNeedRefreshOrientaion binder exception " + exception.getMessage());
            } catch (Throwable th) {
                reply.recycle();
                data.recycle();
                throw th;
            }
        } else {
            Slog.w(TAG, "isNeedRefreshOrientaion mSurfaceFlinger is null");
        }
        return false;
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
            DisplayViewportEx viewportEx = new DisplayViewportEx();
            viewportEx.setDisplayViewport(viewport);
            this.mHwFoldPolicy.adjustViewportFrame(viewportEx, this.mCurrentLayerStackRect, this.mCurrentDisplayRect);
            Slog.d(TAG, "hwc adjustViewportFrame viewport=" + viewport + " mCurrentOrientation=" + this.mCurrentOrientation);
        }
        int i = this.mCurrentOrientation;
        boolean isRotated = true;
        if (!(i == 1 || i == 3)) {
            isRotated = false;
        }
        DisplayDeviceInfo info = getDisplayDeviceInfoLocked();
        viewport.deviceWidth = isRotated ? info.height : info.width;
        viewport.deviceHeight = isRotated ? info.width : info.height;
        viewport.uniqueId = info.uniqueId;
        if (info.address instanceof DisplayAddress.Physical) {
            viewport.physicalPort = Byte.valueOf(info.address.getPort());
        } else {
            viewport.physicalPort = null;
        }
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

    public int setDisplayState(int displayMode, int flodState) {
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
