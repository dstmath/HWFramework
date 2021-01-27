package com.android.server.display;

import android.os.IBinder;
import android.view.Surface;
import com.android.server.display.SurfaceControlExt;

public class DisplayDeviceExt {
    private static final String TAG = "DisplayDeviceExt";
    private DisplayDeviceBridge mDisplayDeviceBridge;

    public DisplayDeviceExt(DisplayAdapterEx displayAdapter, IBinder displayToken, String uniqueId) {
        this.mDisplayDeviceBridge = new DisplayDeviceBridge(displayAdapter, displayToken, uniqueId) {
            /* class com.android.server.display.DisplayDeviceExt.AnonymousClass1 */

            @Override // com.android.server.display.DisplayDeviceBridge
            public boolean hasStableUniqueId() {
                return DisplayDeviceExt.this.hasStableUniqueId();
            }

            @Override // com.android.server.display.DisplayDeviceBridge
            public DisplayDeviceInfo getDisplayDeviceInfoLocked() {
                DisplayDeviceInfoEx displayDeviceInfoEx = DisplayDeviceExt.this.getDisplayDeviceInfoLocked();
                if (displayDeviceInfoEx == null) {
                    return null;
                }
                return displayDeviceInfoEx.getDisplayInfo();
            }
        };
        this.mDisplayDeviceBridge.setDisplayDeviceExt(this);
    }

    public DisplayDeviceBridge getDisplayDeviceBridge() {
        return this.mDisplayDeviceBridge;
    }

    public boolean hasStableUniqueId() {
        return false;
    }

    public final void setSurfaceLocked(SurfaceControlExt.TransactionEx transaction, Surface surface) {
        DisplayDeviceBridge displayDeviceBridge = this.mDisplayDeviceBridge;
        if (displayDeviceBridge != null) {
            displayDeviceBridge.setSurfaceLocked(transaction.getTransaction(), surface);
        }
    }

    public final IBinder getDisplayTokenLocked() {
        DisplayDeviceBridge displayDeviceBridge = this.mDisplayDeviceBridge;
        if (displayDeviceBridge != null) {
            return displayDeviceBridge.getDisplayTokenLocked();
        }
        return null;
    }

    public void performTraversalLocked(SurfaceControlExt.TransactionEx t) {
    }

    public DisplayDeviceInfoEx getDisplayDeviceInfoLocked() {
        return null;
    }
}
