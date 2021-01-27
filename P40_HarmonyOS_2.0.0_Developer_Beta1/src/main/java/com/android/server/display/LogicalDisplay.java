package com.android.server.display;

import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.os.SystemProperties;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import com.android.server.wm.utils.InsetUtils;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/* access modifiers changed from: package-private */
public final class LogicalDisplay {
    private static final int BLANK_LAYER_STACK = -1;
    private static final String TAG = "LogicalDisplay";
    private int[] mAllowedDisplayModes = new int[0];
    private final DisplayInfo mBaseDisplayInfo = new DisplayInfo();
    private final int mDisplayId;
    private int mDisplayOffsetX;
    private int mDisplayOffsetY;
    private boolean mDisplayScalingDisabled;
    private boolean mHasContent;
    private DisplayInfo mInfo;
    private final int mLayerStack;
    private DisplayInfo mOverrideDisplayInfo;
    private DisplayDevice mPrimaryDisplayDevice;
    private DisplayDeviceInfo mPrimaryDisplayDeviceInfo;
    private int mRequestedColorMode;
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
            DisplayInfo displayInfo = this.mOverrideDisplayInfo;
            if (displayInfo != null) {
                this.mInfo.appWidth = displayInfo.appWidth;
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
                if (!HwFoldScreenState.isInwardFoldDevice()) {
                    this.mInfo.physicalXDpi = this.mOverrideDisplayInfo.physicalXDpi;
                    this.mInfo.physicalYDpi = this.mOverrideDisplayInfo.physicalYDpi;
                }
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
            DisplayInfo displayInfo = this.mOverrideDisplayInfo;
            if (displayInfo == null) {
                this.mOverrideDisplayInfo = new DisplayInfo(info);
                this.mInfo = null;
                return true;
            } else if (displayInfo.equals(info)) {
                return false;
            } else {
                this.mOverrideDisplayInfo.copyFrom(info);
                this.mInfo = null;
                return true;
            }
        } else if (this.mOverrideDisplayInfo == null) {
            return false;
        } else {
            this.mOverrideDisplayInfo = null;
            this.mInfo = null;
            return true;
        }
    }

    public boolean isValidLocked() {
        return this.mPrimaryDisplayDevice != null;
    }

    public void updateLocked(List<DisplayDevice> devices) {
        DisplayDevice displayDevice = this.mPrimaryDisplayDevice;
        if (displayDevice != null) {
            if (!devices.contains(displayDevice)) {
                this.mPrimaryDisplayDevice = null;
                return;
            }
            DisplayDeviceInfo deviceInfo = this.mPrimaryDisplayDevice.getDisplayDeviceInfoLocked();
            if (!Objects.equals(this.mPrimaryDisplayDeviceInfo, deviceInfo)) {
                DisplayInfo displayInfo = this.mBaseDisplayInfo;
                displayInfo.layerStack = this.mLayerStack;
                boolean maskCutout = false;
                displayInfo.flags = 0;
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
                if ((deviceInfo.flags & 4096) != 0) {
                    this.mBaseDisplayInfo.flags |= 64;
                }
                Rect maskingInsets = getMaskingInsets(deviceInfo);
                int maskedWidth = (deviceInfo.width - maskingInsets.left) - maskingInsets.right;
                int maskedHeight = (deviceInfo.height - maskingInsets.top) - maskingInsets.bottom;
                this.mBaseDisplayInfo.type = deviceInfo.type;
                this.mBaseDisplayInfo.address = deviceInfo.address;
                this.mBaseDisplayInfo.name = deviceInfo.name;
                this.mBaseDisplayInfo.uniqueId = deviceInfo.uniqueId;
                DisplayInfo displayInfo2 = this.mBaseDisplayInfo;
                displayInfo2.appWidth = maskedWidth;
                displayInfo2.appHeight = maskedHeight;
                displayInfo2.logicalWidth = maskedWidth;
                displayInfo2.logicalHeight = maskedHeight;
                displayInfo2.rotation = 0;
                displayInfo2.modeId = deviceInfo.modeId;
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
                DisplayInfo displayInfo3 = this.mBaseDisplayInfo;
                displayInfo3.smallestNominalAppWidth = maskedWidth;
                displayInfo3.smallestNominalAppHeight = maskedHeight;
                displayInfo3.largestNominalAppWidth = maskedWidth;
                displayInfo3.largestNominalAppHeight = maskedHeight;
                displayInfo3.ownerUid = deviceInfo.ownerUid;
                this.mBaseDisplayInfo.ownerPackageName = deviceInfo.ownerPackageName;
                if ((deviceInfo.flags & 2048) != 0) {
                    maskCutout = true;
                }
                this.mBaseDisplayInfo.displayCutout = maskCutout ? null : deviceInfo.displayCutout;
                this.mBaseDisplayInfo.displayId = this.mDisplayId;
                this.mPrimaryDisplayDeviceInfo = deviceInfo;
                this.mInfo = null;
            }
        }
    }

    private float getPCScreenDisplayScaleMode() {
        String strMode = SystemProperties.get("hw.pc.display.mode");
        if (strMode.equals("minor")) {
            return 0.95f;
        }
        if (strMode.equals("smaller")) {
            return 0.9f;
        }
        return 1.0f;
    }

    public Rect getInsets() {
        return getMaskingInsets(this.mPrimaryDisplayDeviceInfo);
    }

    private static Rect getMaskingInsets(DisplayDeviceInfo deviceInfo) {
        if (!((deviceInfo.flags & 2048) != 0) || deviceInfo.displayCutout == null) {
            return new Rect();
        }
        return deviceInfo.displayCutout.getSafeInsets();
    }

    public void configureDisplayLocked(SurfaceControl.Transaction t, DisplayDevice device, boolean isBlanked) {
        int displayRectWidth;
        int displayRectHeight;
        int displayRectTop;
        int displayRectLeft;
        device.setLayerStackLocked(t, isBlanked ? -1 : this.mLayerStack);
        if (device == this.mPrimaryDisplayDevice) {
            device.setAllowedDisplayModesLocked(this.mAllowedDisplayModes);
            device.setRequestedColorModeLocked(this.mRequestedColorMode);
        } else {
            device.setAllowedDisplayModesLocked(new int[]{0});
            device.setRequestedColorModeLocked(0);
        }
        DisplayInfo displayInfo = getDisplayInfoLocked();
        DisplayDeviceInfo displayDeviceInfo = device.getDisplayDeviceInfoLocked();
        if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(this.mDisplayId)) {
            this.mTempLayerStackRect.set(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
        } else {
            float scaleMode = getPCScreenDisplayScaleMode();
            int widthOffset = (int) (((((float) displayInfo.logicalWidth) * (1.0f - scaleMode)) / 2.0f) + 0.5f);
            int heighOffset = (int) (((((float) displayInfo.logicalHeight) * (1.0f - scaleMode)) / 2.0f) + 0.5f);
            HwPCUtils.log(TAG, "scaleMode=" + scaleMode + " widthOffset=" + widthOffset + " heighOffset=" + heighOffset);
            this.mTempLayerStackRect.set(-widthOffset, -heighOffset, displayInfo.logicalWidth + widthOffset, displayInfo.logicalHeight + heighOffset);
        }
        int orientation = 0;
        if ((displayDeviceInfo.flags & 2) != 0) {
            orientation = displayInfo.rotation;
        }
        int orientation2 = (displayDeviceInfo.rotation + orientation) % 4;
        boolean rotated = orientation2 == 1 || orientation2 == 3;
        int physWidth = rotated ? displayDeviceInfo.height : displayDeviceInfo.width;
        int physHeight = rotated ? displayDeviceInfo.width : displayDeviceInfo.height;
        Rect maskingInsets = getMaskingInsets(displayDeviceInfo);
        InsetUtils.rotateInsets(maskingInsets, orientation2);
        int physWidth2 = physWidth - (maskingInsets.left + maskingInsets.right);
        int physHeight2 = physHeight - (maskingInsets.top + maskingInsets.bottom);
        if ((displayInfo.flags & 1073741824) != 0 || this.mDisplayScalingDisabled) {
            displayRectWidth = displayInfo.logicalWidth;
            displayRectHeight = displayInfo.logicalHeight;
        } else if (displayInfo.logicalHeight * physWidth2 < displayInfo.logicalWidth * physHeight2) {
            displayRectWidth = physWidth2;
            displayRectHeight = (displayInfo.logicalHeight * physWidth2) / displayInfo.logicalWidth;
        } else {
            displayRectWidth = (displayInfo.logicalWidth * physHeight2) / displayInfo.logicalHeight;
            displayRectHeight = physHeight2;
        }
        int displayRectTop2 = (physHeight2 - displayRectHeight) / 2;
        int displayRectLeft2 = (physWidth2 - displayRectWidth) / 2;
        this.mTempDisplayRect.set(displayRectLeft2, displayRectTop2, displayRectLeft2 + displayRectWidth, displayRectTop2 + displayRectHeight);
        this.mTempDisplayRect.offset(maskingInsets.left, maskingInsets.top);
        if (orientation2 == 0) {
            this.mTempDisplayRect.offset(this.mDisplayOffsetX, this.mDisplayOffsetY);
        } else if (orientation2 == 1) {
            this.mTempDisplayRect.offset(this.mDisplayOffsetY, -this.mDisplayOffsetX);
        } else if (orientation2 == 2) {
            this.mTempDisplayRect.offset(-this.mDisplayOffsetX, -this.mDisplayOffsetY);
        } else {
            this.mTempDisplayRect.offset(-this.mDisplayOffsetY, this.mDisplayOffsetX);
        }
        if (this.mDisplayId != 0 || !device.isFoldable()) {
            displayRectLeft = displayRectLeft2;
            displayRectTop = displayRectTop2;
            if (DisplayDevice.IS_TABLET && DisplayDevice.PHY_SCREEN_ROTATE == 3) {
                if (this.mDisplayId != 0) {
                    this.mTempDisplayRect.set(0, 0, physWidth2, physHeight2);
                } else if (device == this.mPrimaryDisplayDevice) {
                    this.mTempDisplayRect.set(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
                }
            }
        } else {
            Rect tmpDisplayRect = device.getScreenDispRect(orientation2);
            displayRectLeft = displayRectLeft2;
            StringBuilder sb = new StringBuilder();
            displayRectTop = displayRectTop2;
            sb.append("mTempLayerStackRect=");
            sb.append(this.mTempLayerStackRect);
            sb.append(" mTempDisplayRect=");
            sb.append(this.mTempDisplayRect);
            sb.append(" tmpDisplayRect=");
            sb.append(tmpDisplayRect);
            sb.append(" orientation = ");
            sb.append(orientation2);
            Slog.d(TAG, sb.toString());
            if (tmpDisplayRect != null && !tmpDisplayRect.isEmpty()) {
                this.mTempDisplayRect.set(tmpDisplayRect);
            }
        }
        Slog.d(TAG, "configureDisplayLocked mTempLayerStackRect=" + this.mTempLayerStackRect + " mTempDisplayRect=" + this.mTempDisplayRect + " orientation = " + orientation2);
        device.setProjectionLocked(t, orientation2, this.mTempLayerStackRect, this.mTempDisplayRect, this.mDisplayId);
    }

    public boolean hasContentLocked() {
        return this.mHasContent;
    }

    public void setHasContentLocked(boolean hasContent) {
        this.mHasContent = hasContent;
    }

    public void setAllowedDisplayModesLocked(int[] modes) {
        this.mAllowedDisplayModes = modes;
    }

    public int[] getAllowedDisplayModesLocked() {
        return this.mAllowedDisplayModes;
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

    public boolean isDisplayScalingDisabled() {
        return this.mDisplayScalingDisabled;
    }

    public void setDisplayScalingDisabledLocked(boolean disableScaling) {
        this.mDisplayScalingDisabled = disableScaling;
    }

    public void dumpLocked(PrintWriter pw) {
        pw.println("mDisplayId=" + this.mDisplayId);
        pw.println("mLayerStack=" + this.mLayerStack);
        pw.println("mHasContent=" + this.mHasContent);
        pw.println("mAllowedDisplayModes=" + Arrays.toString(this.mAllowedDisplayModes));
        pw.println("mRequestedColorMode=" + this.mRequestedColorMode);
        pw.println("mDisplayOffset=(" + this.mDisplayOffsetX + ", " + this.mDisplayOffsetY + ")");
        StringBuilder sb = new StringBuilder();
        sb.append("mDisplayScalingDisabled=");
        sb.append(this.mDisplayScalingDisabled);
        pw.println(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append("mPrimaryDisplayDevice=");
        DisplayDevice displayDevice = this.mPrimaryDisplayDevice;
        sb2.append(displayDevice != null ? displayDevice.getNameLocked() : "null");
        pw.println(sb2.toString());
        pw.println("mBaseDisplayInfo=" + this.mBaseDisplayInfo);
        pw.println("mOverrideDisplayInfo=" + this.mOverrideDisplayInfo);
    }
}
