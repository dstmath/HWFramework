package android.view;

import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.PtmLog;
import android.view.Display.ColorTransform;
import android.view.Display.HdrCapabilities;
import android.view.Display.Mode;
import android.view.inputmethod.EditorInfo;
import java.util.Arrays;
import libcore.util.Objects;

public final class DisplayInfo implements Parcelable {
    public static final Creator<DisplayInfo> CREATOR = null;
    public String address;
    public int appHeight;
    public long appVsyncOffsetNanos;
    public int appWidth;
    public int colorTransformId;
    public int defaultColorTransformId;
    public int defaultModeId;
    public int flags;
    public HdrCapabilities hdrCapabilities;
    public int largestNominalAppHeight;
    public int largestNominalAppWidth;
    public int layerStack;
    public int logicalDensityDpi;
    public int logicalHeight;
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
    public int rotation;
    public int smallestNominalAppHeight;
    public int smallestNominalAppWidth;
    public int state;
    public ColorTransform[] supportedColorTransforms;
    public Mode[] supportedModes;
    public int type;
    public String uniqueId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.DisplayInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.DisplayInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.DisplayInfo.<clinit>():void");
    }

    public DisplayInfo() {
        this.supportedModes = Mode.EMPTY_ARRAY;
        this.supportedColorTransforms = ColorTransform.EMPTY_ARRAY;
    }

    public DisplayInfo(DisplayInfo other) {
        this.supportedModes = Mode.EMPTY_ARRAY;
        this.supportedColorTransforms = ColorTransform.EMPTY_ARRAY;
        copyFrom(other);
    }

    private DisplayInfo(Parcel source) {
        this.supportedModes = Mode.EMPTY_ARRAY;
        this.supportedColorTransforms = ColorTransform.EMPTY_ARRAY;
        readFromParcel(source);
    }

    public boolean equals(Object o) {
        return o instanceof DisplayInfo ? equals((DisplayInfo) o) : false;
    }

    public boolean equals(DisplayInfo other) {
        return (other != null && this.layerStack == other.layerStack && this.flags == other.flags && this.type == other.type && Objects.equal(this.address, other.address) && Objects.equal(this.uniqueId, other.uniqueId) && this.appWidth == other.appWidth && this.appHeight == other.appHeight && this.smallestNominalAppWidth == other.smallestNominalAppWidth && this.smallestNominalAppHeight == other.smallestNominalAppHeight && this.largestNominalAppWidth == other.largestNominalAppWidth && this.largestNominalAppHeight == other.largestNominalAppHeight && this.logicalWidth == other.logicalWidth && this.logicalHeight == other.logicalHeight && this.overscanLeft == other.overscanLeft && this.overscanTop == other.overscanTop && this.overscanRight == other.overscanRight && this.overscanBottom == other.overscanBottom && this.rotation == other.rotation && this.modeId == other.modeId && this.defaultModeId == other.defaultModeId && this.colorTransformId == other.colorTransformId && this.defaultColorTransformId == other.defaultColorTransformId && Objects.equal(this.hdrCapabilities, other.hdrCapabilities) && this.logicalDensityDpi == other.logicalDensityDpi && this.physicalXDpi == other.physicalXDpi && this.physicalYDpi == other.physicalYDpi && this.appVsyncOffsetNanos == other.appVsyncOffsetNanos && this.presentationDeadlineNanos == other.presentationDeadlineNanos && this.state == other.state && this.ownerUid == other.ownerUid) ? Objects.equal(this.ownerPackageName, other.ownerPackageName) : false;
    }

    public int hashCode() {
        return 0;
    }

    public void copyFrom(DisplayInfo other) {
        this.layerStack = other.layerStack;
        this.flags = other.flags;
        this.type = other.type;
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
        this.rotation = other.rotation;
        this.modeId = other.modeId;
        this.defaultModeId = other.defaultModeId;
        this.supportedModes = (Mode[]) Arrays.copyOf(other.supportedModes, other.supportedModes.length);
        this.colorTransformId = other.colorTransformId;
        this.defaultColorTransformId = other.defaultColorTransformId;
        this.supportedColorTransforms = (ColorTransform[]) Arrays.copyOf(other.supportedColorTransforms, other.supportedColorTransforms.length);
        this.hdrCapabilities = other.hdrCapabilities;
        this.logicalDensityDpi = other.logicalDensityDpi;
        this.physicalXDpi = other.physicalXDpi;
        this.physicalYDpi = other.physicalYDpi;
        this.appVsyncOffsetNanos = other.appVsyncOffsetNanos;
        this.presentationDeadlineNanos = other.presentationDeadlineNanos;
        this.state = other.state;
        this.ownerUid = other.ownerUid;
        this.ownerPackageName = other.ownerPackageName;
    }

    public void readFromParcel(Parcel source) {
        int i;
        this.layerStack = source.readInt();
        this.flags = source.readInt();
        this.type = source.readInt();
        this.address = source.readString();
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
        this.rotation = source.readInt();
        this.modeId = source.readInt();
        this.defaultModeId = source.readInt();
        int nModes = source.readInt();
        this.supportedModes = new Mode[nModes];
        for (i = 0; i < nModes; i++) {
            this.supportedModes[i] = (Mode) Mode.CREATOR.createFromParcel(source);
        }
        this.colorTransformId = source.readInt();
        this.defaultColorTransformId = source.readInt();
        int nColorTransforms = source.readInt();
        this.supportedColorTransforms = new ColorTransform[nColorTransforms];
        for (i = 0; i < nColorTransforms; i++) {
            this.supportedColorTransforms[i] = (ColorTransform) ColorTransform.CREATOR.createFromParcel(source);
        }
        this.hdrCapabilities = (HdrCapabilities) source.readParcelable(null);
        this.logicalDensityDpi = source.readInt();
        this.physicalXDpi = source.readFloat();
        this.physicalYDpi = source.readFloat();
        this.appVsyncOffsetNanos = source.readLong();
        this.presentationDeadlineNanos = source.readLong();
        this.state = source.readInt();
        this.ownerUid = source.readInt();
        this.ownerPackageName = source.readString();
        this.uniqueId = source.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.layerStack);
        dest.writeInt(this.flags);
        dest.writeInt(this.type);
        dest.writeString(this.address);
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
        dest.writeInt(this.rotation);
        dest.writeInt(this.modeId);
        dest.writeInt(this.defaultModeId);
        dest.writeInt(this.supportedModes.length);
        for (Mode writeToParcel : this.supportedModes) {
            writeToParcel.writeToParcel(dest, flags);
        }
        dest.writeInt(this.colorTransformId);
        dest.writeInt(this.defaultColorTransformId);
        dest.writeInt(this.supportedColorTransforms.length);
        for (ColorTransform writeToParcel2 : this.supportedColorTransforms) {
            writeToParcel2.writeToParcel(dest, flags);
        }
        dest.writeParcelable(this.hdrCapabilities, flags);
        dest.writeInt(this.logicalDensityDpi);
        dest.writeFloat(this.physicalXDpi);
        dest.writeFloat(this.physicalYDpi);
        dest.writeLong(this.appVsyncOffsetNanos);
        dest.writeLong(this.presentationDeadlineNanos);
        dest.writeInt(this.state);
        dest.writeInt(this.ownerUid);
        dest.writeString(this.ownerPackageName);
        dest.writeString(this.uniqueId);
    }

    public int describeContents() {
        return 0;
    }

    public Mode getMode() {
        return findMode(this.modeId);
    }

    public Mode getDefaultMode() {
        return findMode(this.defaultModeId);
    }

    private Mode findMode(int id) {
        for (int i = 0; i < this.supportedModes.length; i++) {
            if (this.supportedModes[i].getModeId() == id) {
                return this.supportedModes[i];
            }
        }
        throw new IllegalStateException("Unable to locate mode " + id);
    }

    public int findDefaultModeByRefreshRate(float refreshRate) {
        Mode[] modes = this.supportedModes;
        Mode defaultMode = getDefaultMode();
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].matches(defaultMode.getPhysicalWidth(), defaultMode.getPhysicalHeight(), refreshRate)) {
                return modes[i].getModeId();
            }
        }
        return 0;
    }

    public float[] getDefaultRefreshRates() {
        Mode[] modes = this.supportedModes;
        ArraySet<Float> rates = new ArraySet();
        Mode defaultMode = getDefaultMode();
        for (Mode mode : modes) {
            if (mode.getPhysicalWidth() == defaultMode.getPhysicalWidth() && mode.getPhysicalHeight() == defaultMode.getPhysicalHeight()) {
                rates.add(Float.valueOf(mode.getRefreshRate()));
            }
        }
        float[] result = new float[rates.size()];
        int i = 0;
        for (Float rate : rates) {
            int i2 = i + 1;
            result[i] = rate.floatValue();
            i = i2;
        }
        return result;
    }

    public ColorTransform getColorTransform() {
        return findColorTransform(this.colorTransformId);
    }

    public ColorTransform getDefaultColorTransform() {
        return findColorTransform(this.defaultColorTransformId);
    }

    private ColorTransform findColorTransform(int colorTransformId) {
        for (ColorTransform colorTransform : this.supportedColorTransforms) {
            if (colorTransform.getId() == colorTransformId) {
                return colorTransform;
            }
        }
        throw new IllegalStateException("Unable to locate color transform: " + colorTransformId);
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
        return (this.rotation == 0 || this.rotation == 2) ? this.logicalWidth : this.logicalHeight;
    }

    public int getNaturalHeight() {
        return (this.rotation == 0 || this.rotation == 2) ? this.logicalHeight : this.logicalWidth;
    }

    public boolean hasAccess(int uid) {
        return Display.hasAccess(uid, this.flags, this.ownerUid);
    }

    private void getMetricsWithSize(DisplayMetrics outMetrics, CompatibilityInfo compatInfo, Configuration configuration, int width, int height) {
        int i = this.logicalDensityDpi;
        outMetrics.noncompatDensityDpi = i;
        outMetrics.densityDpi = i;
        float f = ((float) this.logicalDensityDpi) * DisplayMetrics.DENSITY_DEFAULT_SCALE;
        outMetrics.noncompatDensity = f;
        outMetrics.density = f;
        f = outMetrics.density;
        outMetrics.noncompatScaledDensity = f;
        outMetrics.scaledDensity = f;
        f = this.physicalXDpi;
        outMetrics.noncompatXdpi = f;
        outMetrics.xdpi = f;
        f = this.physicalYDpi;
        outMetrics.noncompatYdpi = f;
        outMetrics.ydpi = f;
        if (!(configuration == null || configuration.screenWidthDp == 0)) {
            width = (int) ((((float) configuration.screenWidthDp) * outMetrics.density) + 0.5f);
        }
        if (!(configuration == null || configuration.screenHeightDp == 0)) {
            height = (int) ((((float) configuration.screenHeightDp) * outMetrics.density) + 0.5f);
        }
        outMetrics.widthPixels = width;
        outMetrics.noncompatWidthPixels = width;
        outMetrics.heightPixels = height;
        outMetrics.noncompatHeightPixels = height;
        if (!compatInfo.equals(CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO)) {
            compatInfo.applyToDisplayMetrics(outMetrics);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DisplayInfo{\"");
        sb.append(this.name);
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
        if (this.overscanLeft == 0 && this.overscanTop == 0 && this.overscanRight == 0) {
            if (this.overscanBottom != 0) {
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
            sb.append(", colorTransformId ");
            sb.append(this.colorTransformId);
            sb.append(", defaultColorTransformId ");
            sb.append(this.defaultColorTransformId);
            sb.append(", supportedColorTransforms ");
            sb.append(Arrays.toString(this.supportedColorTransforms));
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
                sb.append(", address ").append(this.address);
            }
            sb.append(", state ");
            sb.append(Display.stateToString(this.state));
            if (!(this.ownerUid == 0 && this.ownerPackageName == null)) {
                sb.append(", owner ").append(this.ownerPackageName);
                sb.append(" (uid ").append(this.ownerUid).append(")");
            }
            sb.append(flagsToString(this.flags));
            sb.append("}");
            return sb.toString();
        }
        sb.append(", overscan (");
        sb.append(this.overscanLeft);
        sb.append(PtmLog.PAIRE_DELIMETER);
        sb.append(this.overscanTop);
        sb.append(PtmLog.PAIRE_DELIMETER);
        sb.append(this.overscanRight);
        sb.append(PtmLog.PAIRE_DELIMETER);
        sb.append(this.overscanBottom);
        sb.append(")");
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
        sb.append(", colorTransformId ");
        sb.append(this.colorTransformId);
        sb.append(", defaultColorTransformId ");
        sb.append(this.defaultColorTransformId);
        sb.append(", supportedColorTransforms ");
        sb.append(Arrays.toString(this.supportedColorTransforms));
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
            sb.append(", address ").append(this.address);
        }
        sb.append(", state ");
        sb.append(Display.stateToString(this.state));
        sb.append(", owner ").append(this.ownerPackageName);
        sb.append(" (uid ").append(this.ownerUid).append(")");
        sb.append(flagsToString(this.flags));
        sb.append("}");
        return sb.toString();
    }

    private static String flagsToString(int flags) {
        StringBuilder result = new StringBuilder();
        if ((flags & 2) != 0) {
            result.append(", FLAG_SECURE");
        }
        if ((flags & 1) != 0) {
            result.append(", FLAG_SUPPORTS_PROTECTED_BUFFERS");
        }
        if ((flags & 4) != 0) {
            result.append(", FLAG_PRIVATE");
        }
        if ((flags & 8) != 0) {
            result.append(", FLAG_PRESENTATION");
        }
        if ((EditorInfo.IME_FLAG_NO_ENTER_ACTION & flags) != 0) {
            result.append(", FLAG_SCALING_DISABLED");
        }
        if ((flags & 16) != 0) {
            result.append(", FLAG_ROUND");
        }
        return result.toString();
    }
}
