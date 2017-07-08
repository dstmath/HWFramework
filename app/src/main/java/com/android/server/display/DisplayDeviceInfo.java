package com.android.server.display;

import android.view.Display;
import android.view.Display.ColorTransform;
import android.view.Display.HdrCapabilities;
import android.view.Display.Mode;
import com.android.server.wm.WindowState;
import java.util.Arrays;
import libcore.util.Objects;

final class DisplayDeviceInfo {
    public static final int DIFF_OTHER = 2;
    public static final int DIFF_STATE = 1;
    public static final int FLAG_DEFAULT_DISPLAY = 1;
    public static final int FLAG_NEVER_BLANK = 32;
    public static final int FLAG_OWN_CONTENT_ONLY = 128;
    public static final int FLAG_PRESENTATION = 64;
    public static final int FLAG_PRIVATE = 16;
    public static final int FLAG_ROTATES_WITH_CONTENT = 2;
    public static final int FLAG_ROUND = 256;
    public static final int FLAG_SECURE = 4;
    public static final int FLAG_SUPPORTS_PROTECTED_BUFFERS = 8;
    public static final int TOUCH_EXTERNAL = 2;
    public static final int TOUCH_INTERNAL = 1;
    public static final int TOUCH_NONE = 0;
    public String address;
    public long appVsyncOffsetNanos;
    public int colorTransformId;
    public int defaultColorTransformId;
    public int defaultModeId;
    public int densityDpi;
    public int flags;
    public HdrCapabilities hdrCapabilities;
    public int height;
    public int modeId;
    public String name;
    public String ownerPackageName;
    public int ownerUid;
    public long presentationDeadlineNanos;
    public int rotation;
    public int state;
    public ColorTransform[] supportedColorTransforms;
    public Mode[] supportedModes;
    public int touch;
    public int type;
    public String uniqueId;
    public int width;
    public float xDpi;
    public float yDpi;

    DisplayDeviceInfo() {
        this.supportedModes = Mode.EMPTY_ARRAY;
        this.supportedColorTransforms = ColorTransform.EMPTY_ARRAY;
        this.rotation = 0;
        this.state = TOUCH_EXTERNAL;
    }

    public void setAssumedDensityForExternalDisplay(int width, int height) {
        this.densityDpi = (Math.min(width, height) * 320) / 1080;
        this.xDpi = (float) this.densityDpi;
        this.yDpi = (float) this.densityDpi;
    }

    public boolean equals(Object o) {
        return o instanceof DisplayDeviceInfo ? equals((DisplayDeviceInfo) o) : false;
    }

    public boolean equals(DisplayDeviceInfo other) {
        return other != null && diff(other) == 0;
    }

    public int diff(DisplayDeviceInfo other) {
        int diff = 0;
        if (this.state != other.state) {
            diff = TOUCH_INTERNAL;
        }
        if (Objects.equal(this.name, other.name) && Objects.equal(this.uniqueId, other.uniqueId) && this.width == other.width && this.height == other.height && this.modeId == other.modeId && this.defaultModeId == other.defaultModeId && Arrays.equals(this.supportedModes, other.supportedModes) && this.colorTransformId == other.colorTransformId && this.defaultColorTransformId == other.defaultColorTransformId && Arrays.equals(this.supportedColorTransforms, other.supportedColorTransforms) && Objects.equal(this.hdrCapabilities, other.hdrCapabilities) && this.densityDpi == other.densityDpi && this.xDpi == other.xDpi && this.yDpi == other.yDpi && this.appVsyncOffsetNanos == other.appVsyncOffsetNanos && this.presentationDeadlineNanos == other.presentationDeadlineNanos && this.flags == other.flags && this.touch == other.touch && this.rotation == other.rotation && this.type == other.type && Objects.equal(this.address, other.address) && this.ownerUid == other.ownerUid && Objects.equal(this.ownerPackageName, other.ownerPackageName)) {
            return diff;
        }
        return diff | TOUCH_EXTERNAL;
    }

    public int hashCode() {
        return 0;
    }

    public void copyFrom(DisplayDeviceInfo other) {
        this.name = other.name;
        this.uniqueId = other.uniqueId;
        this.width = other.width;
        this.height = other.height;
        this.modeId = other.modeId;
        this.defaultModeId = other.defaultModeId;
        this.supportedModes = other.supportedModes;
        this.colorTransformId = other.colorTransformId;
        this.defaultColorTransformId = other.defaultColorTransformId;
        this.supportedColorTransforms = other.supportedColorTransforms;
        this.hdrCapabilities = other.hdrCapabilities;
        this.densityDpi = other.densityDpi;
        this.xDpi = other.xDpi;
        this.yDpi = other.yDpi;
        this.appVsyncOffsetNanos = other.appVsyncOffsetNanos;
        this.presentationDeadlineNanos = other.presentationDeadlineNanos;
        this.flags = other.flags;
        this.touch = other.touch;
        this.rotation = other.rotation;
        this.type = other.type;
        this.address = other.address;
        this.state = other.state;
        this.ownerUid = other.ownerUid;
        this.ownerPackageName = other.ownerPackageName;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DisplayDeviceInfo{\"");
        sb.append(this.name).append("\": uniqueId=\"").append(this.uniqueId).append("\", ");
        sb.append(this.width).append(" x ").append(this.height);
        sb.append(", modeId ").append(this.modeId);
        sb.append(", defaultModeId ").append(this.defaultModeId);
        sb.append(", supportedModes ").append(Arrays.toString(this.supportedModes));
        sb.append(", colorTransformId ").append(this.colorTransformId);
        sb.append(", defaultColorTransformId ").append(this.defaultColorTransformId);
        sb.append(", supportedColorTransforms ").append(Arrays.toString(this.supportedColorTransforms));
        sb.append(", HdrCapabilities ").append(this.hdrCapabilities);
        sb.append(", density ").append(this.densityDpi);
        sb.append(", ").append(this.xDpi).append(" x ").append(this.yDpi).append(" dpi");
        sb.append(", appVsyncOff ").append(this.appVsyncOffsetNanos);
        sb.append(", presDeadline ").append(this.presentationDeadlineNanos);
        sb.append(", touch ").append(touchToString(this.touch));
        sb.append(", rotation ").append(this.rotation);
        sb.append(", type ").append(Display.typeToString(this.type));
        if (this.address != null) {
            sb.append(", address ").append(this.address);
        }
        sb.append(", state ").append(Display.stateToString(this.state));
        if (!(this.ownerUid == 0 && this.ownerPackageName == null)) {
            sb.append(", owner ").append(this.ownerPackageName);
            sb.append(" (uid ").append(this.ownerUid).append(")");
        }
        sb.append(flagsToString(this.flags));
        sb.append("}");
        return sb.toString();
    }

    private static String touchToString(int touch) {
        switch (touch) {
            case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                return "NONE";
            case TOUCH_INTERNAL /*1*/:
                return "INTERNAL";
            case TOUCH_EXTERNAL /*2*/:
                return "EXTERNAL";
            default:
                return Integer.toString(touch);
        }
    }

    private static String flagsToString(int flags) {
        StringBuilder msg = new StringBuilder();
        if ((flags & TOUCH_INTERNAL) != 0) {
            msg.append(", FLAG_DEFAULT_DISPLAY");
        }
        if ((flags & TOUCH_EXTERNAL) != 0) {
            msg.append(", FLAG_ROTATES_WITH_CONTENT");
        }
        if ((flags & FLAG_SECURE) != 0) {
            msg.append(", FLAG_SECURE");
        }
        if ((flags & FLAG_SUPPORTS_PROTECTED_BUFFERS) != 0) {
            msg.append(", FLAG_SUPPORTS_PROTECTED_BUFFERS");
        }
        if ((flags & FLAG_PRIVATE) != 0) {
            msg.append(", FLAG_PRIVATE");
        }
        if ((flags & FLAG_NEVER_BLANK) != 0) {
            msg.append(", FLAG_NEVER_BLANK");
        }
        if ((flags & FLAG_PRESENTATION) != 0) {
            msg.append(", FLAG_PRESENTATION");
        }
        if ((flags & FLAG_OWN_CONTENT_ONLY) != 0) {
            msg.append(", FLAG_OWN_CONTENT_ONLY");
        }
        if ((flags & FLAG_ROUND) != 0) {
            msg.append(", FLAG_ROUND");
        }
        return msg.toString();
    }
}
