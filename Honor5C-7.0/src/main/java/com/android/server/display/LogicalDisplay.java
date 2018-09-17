package com.android.server.display;

import android.graphics.Rect;
import android.view.Display.ColorTransform;
import android.view.Display.Mode;
import android.view.DisplayInfo;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import libcore.util.Objects;

final class LogicalDisplay {
    private static final int BLANK_LAYER_STACK = -1;
    private final DisplayInfo mBaseDisplayInfo;
    private final int mDisplayId;
    private int mDisplayOffsetX;
    private int mDisplayOffsetY;
    private boolean mHasContent;
    private DisplayInfo mInfo;
    private final int mLayerStack;
    private DisplayInfo mOverrideDisplayInfo;
    private DisplayDevice mPrimaryDisplayDevice;
    private DisplayDeviceInfo mPrimaryDisplayDeviceInfo;
    private int mRequestedColorTransformId;
    private int mRequestedModeId;
    private final Rect mTempDisplayRect;
    private final Rect mTempLayerStackRect;

    public LogicalDisplay(int displayId, int layerStack, DisplayDevice primaryDisplayDevice) {
        this.mBaseDisplayInfo = new DisplayInfo();
        this.mTempLayerStackRect = new Rect();
        this.mTempDisplayRect = new Rect();
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
                this.mInfo.logicalDensityDpi = this.mOverrideDisplayInfo.logicalDensityDpi;
                this.mInfo.physicalXDpi = this.mOverrideDisplayInfo.physicalXDpi;
                this.mInfo.physicalYDpi = this.mOverrideDisplayInfo.physicalYDpi;
            }
        }
        return this.mInfo;
    }

    public boolean setDisplayInfoOverrideFromWindowManagerLocked(DisplayInfo info) {
        if (info != null) {
            if (this.mOverrideDisplayInfo == null) {
                this.mOverrideDisplayInfo = new DisplayInfo(info);
                this.mInfo = null;
                return true;
            } else if (!this.mOverrideDisplayInfo.equals(info)) {
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
            if (devices.contains(this.mPrimaryDisplayDevice)) {
                DisplayDeviceInfo deviceInfo = this.mPrimaryDisplayDevice.getDisplayDeviceInfoLocked();
                if (!Objects.equal(this.mPrimaryDisplayDeviceInfo, deviceInfo)) {
                    DisplayInfo displayInfo;
                    this.mBaseDisplayInfo.layerStack = this.mLayerStack;
                    this.mBaseDisplayInfo.flags = 0;
                    if ((deviceInfo.flags & 8) != 0) {
                        displayInfo = this.mBaseDisplayInfo;
                        displayInfo.flags |= 1;
                    }
                    if ((deviceInfo.flags & 4) != 0) {
                        displayInfo = this.mBaseDisplayInfo;
                        displayInfo.flags |= 2;
                    }
                    if ((deviceInfo.flags & 16) != 0) {
                        displayInfo = this.mBaseDisplayInfo;
                        displayInfo.flags |= 4;
                    }
                    if ((deviceInfo.flags & 64) != 0) {
                        displayInfo = this.mBaseDisplayInfo;
                        displayInfo.flags |= 8;
                    }
                    if ((deviceInfo.flags & DumpState.DUMP_SHARED_USERS) != 0) {
                        displayInfo = this.mBaseDisplayInfo;
                        displayInfo.flags |= 16;
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
                    this.mBaseDisplayInfo.supportedModes = (Mode[]) Arrays.copyOf(deviceInfo.supportedModes, deviceInfo.supportedModes.length);
                    this.mBaseDisplayInfo.colorTransformId = deviceInfo.colorTransformId;
                    this.mBaseDisplayInfo.defaultColorTransformId = deviceInfo.defaultColorTransformId;
                    this.mBaseDisplayInfo.supportedColorTransforms = (ColorTransform[]) Arrays.copyOf(deviceInfo.supportedColorTransforms, deviceInfo.supportedColorTransforms.length);
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
                    this.mPrimaryDisplayDeviceInfo = deviceInfo;
                    this.mInfo = null;
                }
                return;
            }
            this.mPrimaryDisplayDevice = null;
        }
    }

    public void configureDisplayInTransactionLocked(DisplayDevice device, boolean isBlanked) {
        int displayRectWidth;
        int displayRectHeight;
        device.setLayerStackInTransactionLocked(isBlanked ? BLANK_LAYER_STACK : this.mLayerStack);
        if (device == this.mPrimaryDisplayDevice) {
            device.requestColorTransformAndModeInTransactionLocked(this.mRequestedColorTransformId, this.mRequestedModeId);
        } else {
            device.requestColorTransformAndModeInTransactionLocked(0, 0);
        }
        DisplayInfo displayInfo = getDisplayInfoLocked();
        DisplayDeviceInfo displayDeviceInfo = device.getDisplayDeviceInfoLocked();
        this.mTempLayerStackRect.set(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
        int orientation = 0;
        if ((displayDeviceInfo.flags & 2) != 0) {
            orientation = displayInfo.rotation;
        }
        orientation = (displayDeviceInfo.rotation + orientation) % 4;
        boolean rotated = orientation != 1 ? orientation == 3 : true;
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
        Rect rect = this.mTempDisplayRect;
        rect.left += this.mDisplayOffsetX;
        rect = this.mTempDisplayRect;
        rect.right += this.mDisplayOffsetX;
        rect = this.mTempDisplayRect;
        rect.top += this.mDisplayOffsetY;
        rect = this.mTempDisplayRect;
        rect.bottom += this.mDisplayOffsetY;
        device.setProjectionInTransactionLocked(orientation, this.mTempLayerStackRect, this.mTempDisplayRect);
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

    public void setRequestedColorTransformIdLocked(int colorTransformId) {
        this.mRequestedColorTransformId = colorTransformId;
    }

    public int getRequestedColorTransformIdLocked() {
        return this.mRequestedColorTransformId;
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
        pw.println("mRequestedColorTransformId=" + this.mRequestedColorTransformId);
        pw.println("mDisplayOffset=(" + this.mDisplayOffsetX + ", " + this.mDisplayOffsetY + ")");
        pw.println("mPrimaryDisplayDevice=" + (this.mPrimaryDisplayDevice != null ? this.mPrimaryDisplayDevice.getNameLocked() : "null"));
        pw.println("mBaseDisplayInfo=" + this.mBaseDisplayInfo);
        pw.println("mOverrideDisplayInfo=" + this.mOverrideDisplayInfo);
    }
}
