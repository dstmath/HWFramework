package android.view;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityThread;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import android.view.DisplayCutout;
import com.huawei.android.view.HwWindowManager;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public final class DisplayInfo implements Parcelable {
    public static final Parcelable.Creator<DisplayInfo> CREATOR = new Parcelable.Creator<DisplayInfo>() {
        /* class android.view.DisplayInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DisplayInfo createFromParcel(Parcel source) {
            return new DisplayInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public DisplayInfo[] newArray(int size) {
            return new DisplayInfo[size];
        }
    };
    private static final String STR_SIDE_PROP = SystemProperties.get("ro.config.hw_curved_side_disp", "");
    public DisplayAddress address;
    public int appHeight;
    public long appVsyncOffsetNanos;
    public int appWidth;
    public int colorMode;
    public int defaultModeId;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    public DisplayCutout displayCutout;
    public int displayId;
    public int flags;
    public Display.HdrCapabilities hdrCapabilities;
    public int largestNominalAppHeight;
    public int largestNominalAppWidth;
    public int layerStack;
    public int logicalDensityDpi;
    @UnsupportedAppUsage
    public int logicalHeight;
    @UnsupportedAppUsage
    public int logicalWidth;
    public int modeId;
    public String name;
    public int overscanBottom;
    public int overscanLeft;
    public int overscanRight;
    public int overscanTop;
    public String ownerPackageName;
    public int ownerUid;
    public float physicalXDpi;
    public float physicalYDpi;
    public long presentationDeadlineNanos;
    public int removeMode;
    @UnsupportedAppUsage
    public int rotation;
    public int smallestNominalAppHeight;
    public int smallestNominalAppWidth;
    public int state;
    public int[] supportedColorModes;
    public Display.Mode[] supportedModes;
    public int type;
    public String uniqueId;

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769467)
    public DisplayInfo() {
        this.supportedModes = Display.Mode.EMPTY_ARRAY;
        this.supportedColorModes = new int[]{0};
        this.removeMode = 0;
    }

    public DisplayInfo(DisplayInfo other) {
        this.supportedModes = Display.Mode.EMPTY_ARRAY;
        this.supportedColorModes = new int[]{0};
        this.removeMode = 0;
        copyFrom(other);
    }

    private DisplayInfo(Parcel source) {
        this.supportedModes = Display.Mode.EMPTY_ARRAY;
        this.supportedColorModes = new int[]{0};
        this.removeMode = 0;
        readFromParcel(source);
    }

    public boolean equals(Object o) {
        return (o instanceof DisplayInfo) && equals((DisplayInfo) o);
    }

    public boolean equals(DisplayInfo other) {
        return other != null && this.layerStack == other.layerStack && this.flags == other.flags && this.type == other.type && this.displayId == other.displayId && Objects.equals(this.address, other.address) && Objects.equals(this.uniqueId, other.uniqueId) && this.appWidth == other.appWidth && this.appHeight == other.appHeight && this.smallestNominalAppWidth == other.smallestNominalAppWidth && this.smallestNominalAppHeight == other.smallestNominalAppHeight && this.largestNominalAppWidth == other.largestNominalAppWidth && this.largestNominalAppHeight == other.largestNominalAppHeight && this.logicalWidth == other.logicalWidth && this.logicalHeight == other.logicalHeight && this.overscanLeft == other.overscanLeft && this.overscanTop == other.overscanTop && this.overscanRight == other.overscanRight && this.overscanBottom == other.overscanBottom && Objects.equals(this.displayCutout, other.displayCutout) && this.rotation == other.rotation && this.modeId == other.modeId && this.defaultModeId == other.defaultModeId && this.colorMode == other.colorMode && Arrays.equals(this.supportedColorModes, other.supportedColorModes) && Objects.equals(this.hdrCapabilities, other.hdrCapabilities) && this.logicalDensityDpi == other.logicalDensityDpi && this.physicalXDpi == other.physicalXDpi && this.physicalYDpi == other.physicalYDpi && this.appVsyncOffsetNanos == other.appVsyncOffsetNanos && this.presentationDeadlineNanos == other.presentationDeadlineNanos && this.state == other.state && this.ownerUid == other.ownerUid && Objects.equals(this.ownerPackageName, other.ownerPackageName) && this.removeMode == other.removeMode;
    }

    public int hashCode() {
        return 0;
    }

    public void copyFrom(DisplayInfo other) {
        this.layerStack = other.layerStack;
        this.flags = other.flags;
        this.type = other.type;
        this.displayId = other.displayId;
        this.address = other.address;
        this.name = other.name;
        this.uniqueId = other.uniqueId;
        this.appWidth = other.appWidth;
        this.appHeight = other.appHeight;
        this.smallestNominalAppWidth = other.smallestNominalAppWidth;
        this.smallestNominalAppHeight = other.smallestNominalAppHeight;
        this.largestNominalAppWidth = other.largestNominalAppWidth;
        this.largestNominalAppHeight = other.largestNominalAppHeight;
        this.logicalWidth = other.logicalWidth;
        this.logicalHeight = other.logicalHeight;
        this.overscanLeft = other.overscanLeft;
        this.overscanTop = other.overscanTop;
        this.overscanRight = other.overscanRight;
        this.overscanBottom = other.overscanBottom;
        this.displayCutout = other.displayCutout;
        this.rotation = other.rotation;
        this.modeId = other.modeId;
        this.defaultModeId = other.defaultModeId;
        Display.Mode[] modeArr = other.supportedModes;
        this.supportedModes = (Display.Mode[]) Arrays.copyOf(modeArr, modeArr.length);
        this.colorMode = other.colorMode;
        int[] iArr = other.supportedColorModes;
        this.supportedColorModes = Arrays.copyOf(iArr, iArr.length);
        this.hdrCapabilities = other.hdrCapabilities;
        this.logicalDensityDpi = other.logicalDensityDpi;
        this.physicalXDpi = other.physicalXDpi;
        this.physicalYDpi = other.physicalYDpi;
        this.appVsyncOffsetNanos = other.appVsyncOffsetNanos;
        this.presentationDeadlineNanos = other.presentationDeadlineNanos;
        this.state = other.state;
        this.ownerUid = other.ownerUid;
        this.ownerPackageName = other.ownerPackageName;
        this.removeMode = other.removeMode;
    }

    public void readFromParcel(Parcel source) {
        this.layerStack = source.readInt();
        this.flags = source.readInt();
        this.type = source.readInt();
        this.displayId = source.readInt();
        this.address = (DisplayAddress) source.readParcelable(null);
        this.name = source.readString();
        this.appWidth = source.readInt();
        this.appHeight = source.readInt();
        this.smallestNominalAppWidth = source.readInt();
        this.smallestNominalAppHeight = source.readInt();
        this.largestNominalAppWidth = source.readInt();
        this.largestNominalAppHeight = source.readInt();
        this.logicalWidth = source.readInt();
        this.logicalHeight = source.readInt();
        this.overscanLeft = source.readInt();
        this.overscanTop = source.readInt();
        this.overscanRight = source.readInt();
        this.overscanBottom = source.readInt();
        this.displayCutout = DisplayCutout.ParcelableWrapper.readCutoutFromParcel(source);
        this.rotation = source.readInt();
        this.modeId = source.readInt();
        this.defaultModeId = source.readInt();
        int nModes = source.readInt();
        this.supportedModes = new Display.Mode[nModes];
        for (int i = 0; i < nModes; i++) {
            this.supportedModes[i] = Display.Mode.CREATOR.createFromParcel(source);
        }
        this.colorMode = source.readInt();
        int nColorModes = source.readInt();
        this.supportedColorModes = new int[nColorModes];
        for (int i2 = 0; i2 < nColorModes; i2++) {
            this.supportedColorModes[i2] = source.readInt();
        }
        this.hdrCapabilities = (Display.HdrCapabilities) source.readParcelable(null);
        this.logicalDensityDpi = source.readInt();
        this.physicalXDpi = source.readFloat();
        this.physicalYDpi = source.readFloat();
        this.appVsyncOffsetNanos = source.readLong();
        this.presentationDeadlineNanos = source.readLong();
        this.state = source.readInt();
        this.ownerUid = source.readInt();
        this.ownerPackageName = source.readString();
        this.uniqueId = source.readString();
        this.removeMode = source.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags2) {
        dest.writeInt(this.layerStack);
        dest.writeInt(this.flags);
        dest.writeInt(this.type);
        dest.writeInt(this.displayId);
        dest.writeParcelable(this.address, flags2);
        dest.writeString(this.name);
        dest.writeInt(this.appWidth);
        dest.writeInt(this.appHeight);
        dest.writeInt(this.smallestNominalAppWidth);
        dest.writeInt(this.smallestNominalAppHeight);
        dest.writeInt(this.largestNominalAppWidth);
        dest.writeInt(this.largestNominalAppHeight);
        dest.writeInt(this.logicalWidth);
        dest.writeInt(this.logicalHeight);
        dest.writeInt(this.overscanLeft);
        dest.writeInt(this.overscanTop);
        dest.writeInt(this.overscanRight);
        dest.writeInt(this.overscanBottom);
        DisplayCutout.ParcelableWrapper.writeCutoutToParcel(this.displayCutout, dest, flags2);
        dest.writeInt(this.rotation);
        dest.writeInt(this.modeId);
        dest.writeInt(this.defaultModeId);
        dest.writeInt(this.supportedModes.length);
        int i = 0;
        while (true) {
            Display.Mode[] modeArr = this.supportedModes;
            if (i >= modeArr.length) {
                break;
            }
            modeArr[i].writeToParcel(dest, flags2);
            i++;
        }
        dest.writeInt(this.colorMode);
        dest.writeInt(this.supportedColorModes.length);
        int i2 = 0;
        while (true) {
            int[] iArr = this.supportedColorModes;
            if (i2 < iArr.length) {
                dest.writeInt(iArr[i2]);
                i2++;
            } else {
                dest.writeParcelable(this.hdrCapabilities, flags2);
                dest.writeInt(this.logicalDensityDpi);
                dest.writeFloat(this.physicalXDpi);
                dest.writeFloat(this.physicalYDpi);
                dest.writeLong(this.appVsyncOffsetNanos);
                dest.writeLong(this.presentationDeadlineNanos);
                dest.writeInt(this.state);
                dest.writeInt(this.ownerUid);
                dest.writeString(this.ownerPackageName);
                dest.writeString(this.uniqueId);
                dest.writeInt(this.removeMode);
                return;
            }
        }
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public Display.Mode getMode() {
        return findMode(this.modeId);
    }

    public Display.Mode getDefaultMode() {
        return findMode(this.defaultModeId);
    }

    private Display.Mode findMode(int id) {
        int i = 0;
        while (true) {
            Display.Mode[] modeArr = this.supportedModes;
            if (i >= modeArr.length) {
                throw new IllegalStateException("Unable to locate mode " + id);
            } else if (modeArr[i].getModeId() == id) {
                return this.supportedModes[i];
            } else {
                i++;
            }
        }
    }

    public int findDefaultModeByRefreshRate(float refreshRate) {
        Display.Mode[] modes = this.supportedModes;
        Display.Mode defaultMode = getDefaultMode();
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].matches(defaultMode.getPhysicalWidth(), defaultMode.getPhysicalHeight(), refreshRate)) {
                return modes[i].getModeId();
            }
        }
        return 0;
    }

    public float[] getDefaultRefreshRates() {
        Display.Mode[] modes = this.supportedModes;
        ArraySet<Float> rates = new ArraySet<>();
        Display.Mode defaultMode = getDefaultMode();
        for (Display.Mode mode : modes) {
            if (mode.getPhysicalWidth() == defaultMode.getPhysicalWidth() && mode.getPhysicalHeight() == defaultMode.getPhysicalHeight()) {
                rates.add(Float.valueOf(mode.getRefreshRate()));
            }
        }
        float[] result = new float[rates.size()];
        int i = 0;
        Iterator<Float> it = rates.iterator();
        while (it.hasNext()) {
            result[i] = it.next().floatValue();
            i++;
        }
        return result;
    }

    public void getAppMetrics(DisplayMetrics outMetrics) {
        getAppMetrics(outMetrics, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, null);
    }

    public void getAppMetrics(DisplayMetrics outMetrics, DisplayAdjustments displayAdjustments) {
        getMetricsWithSize(outMetrics, displayAdjustments.getCompatibilityInfo(), displayAdjustments.getConfiguration(), this.appWidth, this.appHeight);
    }

    public void getAppMetrics(DisplayMetrics outMetrics, CompatibilityInfo ci, Configuration configuration) {
        getMetricsWithSize(outMetrics, ci, configuration, this.appWidth, this.appHeight);
    }

    public void getLogicalMetrics(DisplayMetrics outMetrics, CompatibilityInfo compatInfo, Configuration configuration) {
        getMetricsWithSize(outMetrics, compatInfo, configuration, this.logicalWidth, this.logicalHeight);
    }

    public int getNaturalWidth() {
        int i = this.rotation;
        return (i == 0 || i == 2) ? this.logicalWidth : this.logicalHeight;
    }

    public int getNaturalHeight() {
        int i = this.rotation;
        return (i == 0 || i == 2) ? this.logicalHeight : this.logicalWidth;
    }

    public boolean isHdr() {
        Display.HdrCapabilities hdrCapabilities2 = this.hdrCapabilities;
        int[] types = hdrCapabilities2 != null ? hdrCapabilities2.getSupportedHdrTypes() : null;
        return types != null && types.length > 0;
    }

    public boolean isWideColorGamut() {
        int[] iArr = this.supportedColorModes;
        for (int colorMode2 : iArr) {
            if (colorMode2 == 6 || colorMode2 > 7) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAccess(int uid) {
        return Display.hasAccess(uid, this.flags, this.ownerUid, this.displayId);
    }

    private void getMetricsWithSize(DisplayMetrics outMetrics, CompatibilityInfo compatInfo, Configuration configuration, int width, int height) {
        int i = this.logicalDensityDpi;
        outMetrics.noncompatDensityDpi = i;
        outMetrics.densityDpi = i;
        float f = ((float) i) * 0.00625f;
        outMetrics.noncompatDensity = f;
        outMetrics.density = f;
        float f2 = outMetrics.density;
        outMetrics.noncompatScaledDensity = f2;
        outMetrics.scaledDensity = f2;
        float f3 = this.physicalXDpi;
        outMetrics.noncompatXdpi = f3;
        outMetrics.xdpi = f3;
        float f4 = this.physicalYDpi;
        outMetrics.noncompatYdpi = f4;
        outMetrics.ydpi = f4;
        Rect appBounds = configuration != null ? configuration.windowConfiguration.getAppBounds() : null;
        int width2 = appBounds != null ? appBounds.width() : width;
        int height2 = appBounds != null ? appBounds.height() : height;
        outMetrics.widthPixels = width2;
        outMetrics.noncompatWidthPixels = width2;
        outMetrics.heightPixels = height2;
        outMetrics.noncompatHeightPixels = height2;
        if (!TextUtils.isEmpty(STR_SIDE_PROP)) {
            int displaySideWidthPixels = 0;
            DisplayCutout displayCutout2 = this.displayCutout;
            if (!(displayCutout2 == null || displayCutout2.getDisplaySideSafeInsets() == null)) {
                Rect displaySideSafeInsets = this.displayCutout.getDisplaySideSafeInsets();
                displaySideWidthPixels = displaySideSafeInsets.left + displaySideSafeInsets.right + displaySideSafeInsets.top + displaySideSafeInsets.bottom;
            }
            String packagename = ActivityThread.currentPackageName();
            int appInfoFlags = ActivityThread.currentAppInfoFlag();
            if (!(packagename == null || appInfoFlags == -1)) {
                int isSystem = appInfoFlags & 1;
                if (outMetrics.widthPixels < outMetrics.heightPixels) {
                    if ((isSystem == 0 || Display.mSpecialPackage.contains(packagename)) && !HwWindowManager.isAppNeedExpand(packagename)) {
                        outMetrics.noncompatWidthPixels -= displaySideWidthPixels;
                        outMetrics.widthPixels -= displaySideWidthPixels;
                    }
                } else if (outMetrics.widthPixels > outMetrics.heightPixels && (isSystem == 0 || Display.mSpecialPackage.contains(packagename))) {
                    outMetrics.noncompatHeightPixels -= displaySideWidthPixels;
                    outMetrics.heightPixels -= displaySideWidthPixels;
                }
            }
            if (!compatInfo.equals(CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO)) {
                compatInfo.applyToDisplayMetrics(outMetrics);
            }
        } else if (!compatInfo.equals(CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO)) {
            compatInfo.applyToDisplayMetrics(outMetrics);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DisplayInfo{\"");
        sb.append(this.name);
        sb.append(", displayId ");
        sb.append(this.displayId);
        sb.append("\", uniqueId \"");
        sb.append(this.uniqueId);
        sb.append("\", app ");
        sb.append(this.appWidth);
        sb.append(" x ");
        sb.append(this.appHeight);
        sb.append(", real ");
        sb.append(this.logicalWidth);
        sb.append(" x ");
        sb.append(this.logicalHeight);
        if (!(this.overscanLeft == 0 && this.overscanTop == 0 && this.overscanRight == 0 && this.overscanBottom == 0)) {
            sb.append(", overscan (");
            sb.append(this.overscanLeft);
            sb.append(SmsManager.REGEX_PREFIX_DELIMITER);
            sb.append(this.overscanTop);
            sb.append(SmsManager.REGEX_PREFIX_DELIMITER);
            sb.append(this.overscanRight);
            sb.append(SmsManager.REGEX_PREFIX_DELIMITER);
            sb.append(this.overscanBottom);
            sb.append(")");
        }
        sb.append(", largest app ");
        sb.append(this.largestNominalAppWidth);
        sb.append(" x ");
        sb.append(this.largestNominalAppHeight);
        sb.append(", smallest app ");
        sb.append(this.smallestNominalAppWidth);
        sb.append(" x ");
        sb.append(this.smallestNominalAppHeight);
        sb.append(", mode ");
        sb.append(this.modeId);
        sb.append(", defaultMode ");
        sb.append(this.defaultModeId);
        sb.append(", modes ");
        sb.append(Arrays.toString(this.supportedModes));
        sb.append(", colorMode ");
        sb.append(this.colorMode);
        sb.append(", supportedColorModes ");
        sb.append(Arrays.toString(this.supportedColorModes));
        sb.append(", hdrCapabilities ");
        sb.append(this.hdrCapabilities);
        sb.append(", rotation ");
        sb.append(this.rotation);
        sb.append(", density ");
        sb.append(this.logicalDensityDpi);
        sb.append(" (");
        sb.append(this.physicalXDpi);
        sb.append(" x ");
        sb.append(this.physicalYDpi);
        sb.append(") dpi, layerStack ");
        sb.append(this.layerStack);
        sb.append(", appVsyncOff ");
        sb.append(this.appVsyncOffsetNanos);
        sb.append(", presDeadline ");
        sb.append(this.presentationDeadlineNanos);
        sb.append(", type ");
        sb.append(Display.typeToString(this.type));
        if (this.address != null) {
            sb.append(", address ");
            sb.append(this.address);
        }
        sb.append(", state ");
        sb.append(Display.stateToString(this.state));
        if (!(this.ownerUid == 0 && this.ownerPackageName == null)) {
            sb.append(", owner ");
            sb.append(this.ownerPackageName);
            sb.append(" (uid ");
            sb.append(this.ownerUid);
            sb.append(")");
        }
        sb.append(flagsToString(this.flags));
        sb.append(", removeMode ");
        sb.append(this.removeMode);
        sb.append("}");
        return sb.toString();
    }

    public void writeToProto(ProtoOutputStream protoOutputStream, long fieldId) {
        long token = protoOutputStream.start(fieldId);
        protoOutputStream.write(1120986464257L, this.logicalWidth);
        protoOutputStream.write(1120986464258L, this.logicalHeight);
        protoOutputStream.write(1120986464259L, this.appWidth);
        protoOutputStream.write(1120986464260L, this.appHeight);
        protoOutputStream.write(1138166333445L, this.name);
        protoOutputStream.end(token);
    }

    private static String flagsToString(int flags2) {
        StringBuilder result = new StringBuilder();
        if ((flags2 & 2) != 0) {
            result.append(", FLAG_SECURE");
        }
        if ((flags2 & 1) != 0) {
            result.append(", FLAG_SUPPORTS_PROTECTED_BUFFERS");
        }
        if ((flags2 & 4) != 0) {
            result.append(", FLAG_PRIVATE");
        }
        if ((flags2 & 8) != 0) {
            result.append(", FLAG_PRESENTATION");
        }
        if ((1073741824 & flags2) != 0) {
            result.append(", FLAG_SCALING_DISABLED");
        }
        if ((flags2 & 16) != 0) {
            result.append(", FLAG_ROUND");
        }
        return result.toString();
    }
}
