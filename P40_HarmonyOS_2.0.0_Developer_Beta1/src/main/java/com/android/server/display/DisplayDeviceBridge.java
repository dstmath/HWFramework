package com.android.server.display;

import android.graphics.Rect;
import android.os.IBinder;
import android.view.SurfaceControl;
import java.io.PrintWriter;

public abstract class DisplayDeviceBridge extends DisplayDevice {
    private DisplayDeviceExt mDisplayDeviceExt;

    public /* bridge */ /* synthetic */ void applyPendingDisplayDeviceInfoChangesLocked() {
        DisplayDeviceBridge.super.applyPendingDisplayDeviceInfoChangesLocked();
    }

    public /* bridge */ /* synthetic */ void dumpLocked(PrintWriter x0) {
        DisplayDeviceBridge.super.dumpLocked(x0);
    }

    public /* bridge */ /* synthetic */ int getDisplayState() {
        return DisplayDeviceBridge.super.getDisplayState();
    }

    public /* bridge */ /* synthetic */ Rect getScreenDispRect(int x0) {
        return DisplayDeviceBridge.super.getScreenDispRect(x0);
    }

    public /* bridge */ /* synthetic */ boolean isFoldable() {
        return DisplayDeviceBridge.super.isFoldable();
    }

    public /* bridge */ /* synthetic */ void onOverlayChangedLocked() {
        DisplayDeviceBridge.super.onOverlayChangedLocked();
    }

    public /* bridge */ /* synthetic */ void performTraversalLocked(SurfaceControl.Transaction x0) {
        DisplayDeviceBridge.super.performTraversalLocked(x0);
    }

    public /* bridge */ /* synthetic */ Runnable requestDisplayStateLocked(int x0, int x1) {
        return DisplayDeviceBridge.super.requestDisplayStateLocked(x0, x1);
    }

    public /* bridge */ /* synthetic */ void setAllowedDisplayModesLocked(int[] x0) {
        DisplayDeviceBridge.super.setAllowedDisplayModesLocked(x0);
    }

    public /* bridge */ /* synthetic */ int setDisplayState(int x0, int x1) {
        return DisplayDeviceBridge.super.setDisplayState(x0, x1);
    }

    public /* bridge */ /* synthetic */ void setRequestedColorModeLocked(int x0) {
        DisplayDeviceBridge.super.setRequestedColorModeLocked(x0);
    }

    public /* bridge */ /* synthetic */ void updateDesityforRog() {
        DisplayDeviceBridge.super.updateDesityforRog();
    }

    public DisplayDeviceBridge(DisplayAdapter displayAdapter, IBinder displayToken, String uniqueId) {
        super(displayAdapter, displayToken, uniqueId);
    }

    public DisplayDeviceBridge(DisplayAdapterEx displayAdapter, IBinder displayToken, String uniqueId) {
        super(DisplayAdapterBridgeUtils.createDisplayAdapterBridge(displayAdapter), displayToken, uniqueId);
    }

    public void setDisplayDeviceExt(DisplayDeviceExt displayDeviceExt) {
        this.mDisplayDeviceExt = displayDeviceExt;
    }

    public boolean hasStableUniqueId() {
        return false;
    }

    public DisplayDeviceInfo getDisplayDeviceInfoLocked() {
        DisplayDeviceInfoEx displayDeviceInfoEx;
        DisplayDeviceExt displayDeviceExt = this.mDisplayDeviceExt;
        if (displayDeviceExt == null || (displayDeviceInfoEx = displayDeviceExt.getDisplayDeviceInfoLocked()) == null) {
            return null;
        }
        return displayDeviceInfoEx.getDisplayInfo();
    }
}
