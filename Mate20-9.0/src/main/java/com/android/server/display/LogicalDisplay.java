package com.android.server.display;

import android.graphics.Rect;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

final class LogicalDisplay {
    private static final int BLANK_LAYER_STACK = -1;
    private static final String TAG = "LogicalDisplay";
    private final DisplayInfo mBaseDisplayInfo = new DisplayInfo();
    private final int mDisplayId;
    private int mDisplayOffsetX;
    private int mDisplayOffsetY;
    private boolean mHasContent;
    private DisplayInfo mInfo;
    private final int mLayerStack;
    private DisplayInfo mOverrideDisplayInfo;
    private DisplayDevice mPrimaryDisplayDevice;
    private DisplayDeviceInfo mPrimaryDisplayDeviceInfo;
    private int mRequestedColorMode;
    private int mRequestedModeId;
    private final Rect mTempDisplayRect = new Rect();
    private final Rect mTempLayerStackRect = new Rect();

    public LogicalDisplay(int displayId, int layerStack, DisplayDevice primaryDisplayDevice) {
        this.mDisplayId = displayId;
        this.mLayerStack = layerStack;
        this.mPrimaryDisplayDevice = primaryDisplayDevice;
    }

    public int getDisplayIdLocked() {
        return this.mDisplayId;
    }

    public DisplayDevice getPrimaryDisplayDeviceLocked() {
        return this.mPrimaryDisplayDevice;
    }

    public DisplayInfo getDisplayInfoLocked() {
        if (this.mInfo == null) {
            this.mInfo = new DisplayInfo();
            this.mInfo.copyFrom(this.mBaseDisplayInfo);
            if (this.mOverrideDisplayInfo != null) {
                this.mInfo.appWidth = this.mOverrideDisplayInfo.appWidth;
                this.mInfo.appHeight = this.mOverrideDisplayInfo.appHeight;
                this.mInfo.smallestNominalAppWidth = this.mOverrideDisplayInfo.smallestNominalAppWidth;
                this.mInfo.smallestNominalAppHeight = this.mOverrideDisplayInfo.smallestNominalAppHeight;
                this.mInfo.largestNominalAppWidth = this.mOverrideDisplayInfo.largestNominalAppWidth;
                this.mInfo.largestNominalAppHeight = this.mOverrideDisplayInfo.largestNominalAppHeight;
                this.mInfo.logicalWidth = this.mOverrideDisplayInfo.logicalWidth;
                this.mInfo.logicalHeight = this.mOverrideDisplayInfo.logicalHeight;
                this.mInfo.overscanLeft = this.mOverrideDisplayInfo.overscanLeft;
                this.mInfo.overscanTop = this.mOverrideDisplayInfo.overscanTop;
                this.mInfo.overscanRight = this.mOverrideDisplayInfo.overscanRight;
                this.mInfo.overscanBottom = this.mOverrideDisplayInfo.overscanBottom;
                this.mInfo.rotation = this.mOverrideDisplayInfo.rotation;
                this.mInfo.displayCutout = this.mOverrideDisplayInfo.displayCutout;
                this.mInfo.logicalDensityDpi = this.mOverrideDisplayInfo.logicalDensityDpi;
                this.mInfo.physicalXDpi = this.mOverrideDisplayInfo.physicalXDpi;
                this.mInfo.physicalYDpi = this.mOverrideDisplayInfo.physicalYDpi;
            }
        }
        return this.mInfo;
    }

    /* access modifiers changed from: package-private */
    public void getNonOverrideDisplayInfoLocked(DisplayInfo outInfo) {
        outInfo.copyFrom(this.mBaseDisplayInfo);
    }

    public boolean setDisplayInfoOverrideFromWindowManagerLocked(DisplayInfo info) {
        if (info != null) {
            if (this.mOverrideDisplayInfo == null) {
                this.mOverrideDisplayInfo = new DisplayInfo(info);
                this.mInfo = null;
                return true;
            } else if (!this.mOverrideDisplayInfo.equals(info) || this.mPrimaryDisplayDevice.isFoldable()) {
                this.mOverrideDisplayInfo.copyFrom(info);
                this.mInfo = null;
                return true;
            }
        } else if (this.mOverrideDisplayInfo != null) {
            this.mOverrideDisplayInfo = null;
            this.mInfo = null;
            return true;
        }
        return false;
    }

    public boolean isValidLocked() {
        return this.mPrimaryDisplayDevice != null;
    }

    public void updateLocked(List<DisplayDevice> devices) {
        if (this.mPrimaryDisplayDevice != null) {
            if (!devices.contains(this.mPrimaryDisplayDevice)) {
                this.mPrimaryDisplayDevice = null;
                return;
            }
            DisplayDeviceInfo deviceInfo = this.mPrimaryDisplayDevice.getDisplayDeviceInfoLocked();
            if (!Objects.equals(this.mPrimaryDisplayDeviceInfo, deviceInfo)) {
                this.mBaseDisplayInfo.layerStack = this.mLayerStack;
                this.mBaseDisplayInfo.flags = 0;
                if ((deviceInfo.flags & 8) != 0) {
                    this.mBaseDisplayInfo.flags |= 1;
                }
                if ((deviceInfo.flags & 4) != 0) {
                    this.mBaseDisplayInfo.flags |= 2;
                }
                if ((deviceInfo.flags & 16) != 0) {
                    this.mBaseDisplayInfo.flags |= 4;
                    this.mBaseDisplayInfo.removeMode = 1;
                }
                if ((deviceInfo.flags & 1024) != 0) {
                    this.mBaseDisplayInfo.removeMode = 1;
                }
                if ((deviceInfo.flags & 64) != 0) {
                    this.mBaseDisplayInfo.flags |= 8;
                }
                if ((deviceInfo.flags & 256) != 0) {
                    this.mBaseDisplayInfo.flags |= 16;
                }
                if ((deviceInfo.flags & 512) != 0) {
                    this.mBaseDisplayInfo.flags |= 32;
                }
                this.mBaseDisplayInfo.type = deviceInfo.type;
                this.mBaseDisplayInfo.address = deviceInfo.address;
                this.mBaseDisplayInfo.name = deviceInfo.name;
                this.mBaseDisplayInfo.uniqueId = deviceInfo.uniqueId;
                this.mBaseDisplayInfo.appWidth = deviceInfo.width;
                this.mBaseDisplayInfo.appHeight = deviceInfo.height;
                this.mBaseDisplayInfo.logicalWidth = deviceInfo.width;
                this.mBaseDisplayInfo.logicalHeight = deviceInfo.height;
                this.mBaseDisplayInfo.rotation = 0;
                this.mBaseDisplayInfo.modeId = deviceInfo.modeId;
                this.mBaseDisplayInfo.defaultModeId = deviceInfo.defaultModeId;
                this.mBaseDisplayInfo.supportedModes = (Display.Mode[]) Arrays.copyOf(deviceInfo.supportedModes, deviceInfo.supportedModes.length);
                this.mBaseDisplayInfo.colorMode = deviceInfo.colorMode;
                this.mBaseDisplayInfo.supportedColorModes = Arrays.copyOf(deviceInfo.supportedColorModes, deviceInfo.supportedColorModes.length);
                this.mBaseDisplayInfo.hdrCapabilities = deviceInfo.hdrCapabilities;
                this.mBaseDisplayInfo.logicalDensityDpi = deviceInfo.densityDpi;
                this.mBaseDisplayInfo.physicalXDpi = deviceInfo.xDpi;
                this.mBaseDisplayInfo.physicalYDpi = deviceInfo.yDpi;
                this.mBaseDisplayInfo.appVsyncOffsetNanos = deviceInfo.appVsyncOffsetNanos;
                this.mBaseDisplayInfo.presentationDeadlineNanos = deviceInfo.presentationDeadlineNanos;
                this.mBaseDisplayInfo.state = deviceInfo.state;
                this.mBaseDisplayInfo.smallestNominalAppWidth = deviceInfo.width;
                this.mBaseDisplayInfo.smallestNominalAppHeight = deviceInfo.height;
                this.mBaseDisplayInfo.largestNominalAppWidth = deviceInfo.width;
                this.mBaseDisplayInfo.largestNominalAppHeight = deviceInfo.height;
                this.mBaseDisplayInfo.ownerUid = deviceInfo.ownerUid;
                this.mBaseDisplayInfo.ownerPackageName = deviceInfo.ownerPackageName;
                this.mBaseDisplayInfo.displayCutout = deviceInfo.displayCutout;
                this.mPrimaryDisplayDeviceInfo = deviceInfo;
                this.mInfo = null;
            }
        }
    }

    public void configureDisplayLocked(SurfaceControl.Transaction t, DisplayDevice device, boolean isBlanked) {
        int displayRectHeight;
        int displayRectWidth;
        SurfaceControl.Transaction transaction = t;
        DisplayDevice displayDevice = device;
        displayDevice.setLayerStackLocked(transaction, isBlanked ? -1 : this.mLayerStack);
        boolean rotated = false;
        if (displayDevice == this.mPrimaryDisplayDevice) {
            displayDevice.requestDisplayModesLocked(this.mRequestedColorMode, this.mRequestedModeId);
        } else {
            displayDevice.requestDisplayModesLocked(0, 0);
        }
        DisplayInfo displayInfo = getDisplayInfoLocked();
        DisplayDeviceInfo displayDeviceInfo = device.getDisplayDeviceInfoLocked();
        this.mTempLayerStackRect.set(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
        int orientation = 0;
        if ((displayDeviceInfo.flags & 2) != 0) {
            orientation = displayInfo.rotation;
        }
        int orientation2 = (displayDeviceInfo.rotation + orientation) % 4;
        if (orientation2 == 1 || orientation2 == 3) {
            rotated = true;
        }
        int physWidth = rotated ? displayDeviceInfo.height : displayDeviceInfo.width;
        int physHeight = rotated ? displayDeviceInfo.width : displayDeviceInfo.height;
        if ((displayInfo.flags & 1073741824) != 0) {
            displayRectWidth = displayInfo.logicalWidth;
            displayRectHeight = displayInfo.logicalHeight;
        } else if (displayInfo.logicalHeight * physWidth < displayInfo.logicalWidth * physHeight) {
            displayRectWidth = physWidth;
            displayRectHeight = (displayInfo.logicalHeight * physWidth) / displayInfo.logicalWidth;
        } else {
            displayRectWidth = (displayInfo.logicalWidth * physHeight) / displayInfo.logicalHeight;
            displayRectHeight = physHeight;
        }
        int displayRectTop = (physHeight - displayRectHeight) / 2;
        int displayRectLeft = (physWidth - displayRectWidth) / 2;
        this.mTempDisplayRect.set(displayRectLeft, displayRectTop, displayRectLeft + displayRectWidth, displayRectTop + displayRectHeight);
        this.mTempDisplayRect.left += this.mDisplayOffsetX;
        this.mTempDisplayRect.right += this.mDisplayOffsetX;
        this.mTempDisplayRect.top += this.mDisplayOffsetY;
        this.mTempDisplayRect.bottom += this.mDisplayOffsetY;
        if (this.mDisplayId != 0 || !device.isFoldable()) {
        } else {
            Rect tmpDisplayRect = displayDevice.getScreenDispRect(orientation2);
            StringBuilder sb = new StringBuilder();
            DisplayInfo displayInfo2 = displayInfo;
            sb.append("mTempLayerStackRect=");
            sb.append(this.mTempLayerStackRect);
            sb.append(" mTempDisplayRect=");
            sb.append(this.mTempDisplayRect);
            sb.append(" tmpDisplayRect=");
            sb.append(tmpDisplayRect);
            Slog.d(TAG, sb.toString());
            if (tmpDisplayRect != null && !tmpDisplayRect.isEmpty()) {
                this.mTempDisplayRect.set(tmpDisplayRect);
            }
        }
        displayDevice.setProjectionLocked(transaction, orientation2, this.mTempLayerStackRect, this.mTempDisplayRect);
    }

    public boolean hasContentLocked() {
        return this.mHasContent;
    }

    public void setHasContentLocked(boolean hasContent) {
        this.mHasContent = hasContent;
    }

    public void setRequestedModeIdLocked(int modeId) {
        this.mRequestedModeId = modeId;
    }

    public int getRequestedModeIdLocked() {
        return this.mRequestedModeId;
    }

    public void setRequestedColorModeLocked(int colorMode) {
        this.mRequestedColorMode = colorMode;
    }

    public int getRequestedColorModeLocked() {
        return this.mRequestedColorMode;
    }

    public int getDisplayOffsetXLocked() {
        return this.mDisplayOffsetX;
    }

    public int getDisplayOffsetYLocked() {
        return this.mDisplayOffsetY;
    }

    public void setDisplayOffsetsLocked(int x, int y) {
        this.mDisplayOffsetX = x;
        this.mDisplayOffsetY = y;
    }

    public void dumpLocked(PrintWriter pw) {
        pw.println("mDisplayId=" + this.mDisplayId);
        pw.println("mLayerStack=" + this.mLayerStack);
        pw.println("mHasContent=" + this.mHasContent);
        pw.println("mRequestedMode=" + this.mRequestedModeId);
        pw.println("mRequestedColorMode=" + this.mRequestedColorMode);
        pw.println("mDisplayOffset=(" + this.mDisplayOffsetX + ", " + this.mDisplayOffsetY + ")");
        StringBuilder sb = new StringBuilder();
        sb.append("mPrimaryDisplayDevice=");
        sb.append(this.mPrimaryDisplayDevice != null ? this.mPrimaryDisplayDevice.getNameLocked() : "null");
        pw.println(sb.toString());
        pw.println("mBaseDisplayInfo=" + this.mBaseDisplayInfo);
        pw.println("mOverrideDisplayInfo=" + this.mOverrideDisplayInfo);
    }
}
