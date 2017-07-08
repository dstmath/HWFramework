package android.view;

import android.app.ActivityThread;
import android.app.Application;
import android.content.res.CompatibilityInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.rog.AppRogInfo;
import android.util.DisplayMetrics;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.RILConstants;
import java.util.Arrays;

public final class Display {
    private static final int CACHED_APP_SIZE_DURATION_MILLIS = 20;
    private static final boolean DEBUG = false;
    public static final int DEFAULT_DISPLAY = 0;
    public static final int FLAG_PRESENTATION = 8;
    public static final int FLAG_PRIVATE = 4;
    public static final int FLAG_ROUND = 16;
    public static final int FLAG_SCALING_DISABLED = 1073741824;
    public static final int FLAG_SECURE = 2;
    public static final int FLAG_SUPPORTS_PROTECTED_BUFFERS = 1;
    public static final int INVALID_DISPLAY = -1;
    public static final int STATE_DOZE = 3;
    public static final int STATE_DOZE_SUSPEND = 4;
    public static final int STATE_OFF = 1;
    public static final int STATE_ON = 2;
    public static final int STATE_UNKNOWN = 0;
    private static final String TAG = "Display";
    public static final int TYPE_BUILT_IN = 1;
    public static final int TYPE_HDMI = 2;
    public static final int TYPE_OVERLAY = 4;
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_VIRTUAL = 5;
    public static final int TYPE_WIFI = 3;
    private final String mAddress;
    private int mCachedAppHeightCompat;
    private int mCachedAppWidthCompat;
    private final DisplayAdjustments mDisplayAdjustments;
    private final int mDisplayId;
    private DisplayInfo mDisplayInfo;
    private final int mFlags;
    private final DisplayManagerGlobal mGlobal;
    private boolean mIsValid;
    private long mLastCachedAppSizeUpdate;
    private final int mLayerStack;
    private final String mOwnerPackageName;
    private final int mOwnerUid;
    private boolean mRogEnable;
    private AppRogInfo mRogInfo;
    private final DisplayMetrics mTempMetrics;
    private final int mType;

    public static final class ColorTransform implements Parcelable {
        public static final Creator<ColorTransform> CREATOR = null;
        public static final ColorTransform[] EMPTY_ARRAY = null;
        private final int mColorTransform;
        private final int mId;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.Display.ColorTransform.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.Display.ColorTransform.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.Display.ColorTransform.<clinit>():void");
        }

        public ColorTransform(int id, int colorTransform) {
            this.mId = id;
            this.mColorTransform = colorTransform;
        }

        public int getId() {
            return this.mId;
        }

        public int getColorTransform() {
            return this.mColorTransform;
        }

        public boolean equals(Object other) {
            boolean z = true;
            if (this == other) {
                return true;
            }
            if (!(other instanceof ColorTransform)) {
                return Display.DEBUG;
            }
            ColorTransform that = (ColorTransform) other;
            if (this.mId != that.mId) {
                z = Display.DEBUG;
            } else if (this.mColorTransform != that.mColorTransform) {
                z = Display.DEBUG;
            }
            return z;
        }

        public int hashCode() {
            return ((this.mId + 17) * 17) + this.mColorTransform;
        }

        public String toString() {
            return "{" + "id=" + this.mId + ", colorTransform=" + this.mColorTransform + "}";
        }

        public int describeContents() {
            return Display.TYPE_UNKNOWN;
        }

        private ColorTransform(Parcel in) {
            this(in.readInt(), in.readInt());
        }

        public void writeToParcel(Parcel out, int parcelableFlags) {
            out.writeInt(this.mId);
            out.writeInt(this.mColorTransform);
        }
    }

    public static final class HdrCapabilities implements Parcelable {
        public static final Creator<HdrCapabilities> CREATOR = null;
        public static final int HDR_TYPE_DOLBY_VISION = 1;
        public static final int HDR_TYPE_HDR10 = 2;
        public static final int HDR_TYPE_HLG = 3;
        public static final float INVALID_LUMINANCE = -1.0f;
        private float mMaxAverageLuminance;
        private float mMaxLuminance;
        private float mMinLuminance;
        private int[] mSupportedHdrTypes;

        /* renamed from: android.view.Display.HdrCapabilities.1 */
        static class AnonymousClass1 implements Creator<HdrCapabilities> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m9createFromParcel(Parcel source) {
                return createFromParcel(source);
            }

            public HdrCapabilities createFromParcel(Parcel source) {
                return new HdrCapabilities(source, null);
            }

            public /* bridge */ /* synthetic */ Object[] m10newArray(int size) {
                return newArray(size);
            }

            public HdrCapabilities[] newArray(int size) {
                return new HdrCapabilities[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.Display.HdrCapabilities.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.Display.HdrCapabilities.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.Display.HdrCapabilities.<clinit>():void");
        }

        /* synthetic */ HdrCapabilities(Parcel source, HdrCapabilities hdrCapabilities) {
            this(source);
        }

        public HdrCapabilities() {
            this.mSupportedHdrTypes = new int[Display.TYPE_UNKNOWN];
            this.mMaxLuminance = INVALID_LUMINANCE;
            this.mMaxAverageLuminance = INVALID_LUMINANCE;
            this.mMinLuminance = INVALID_LUMINANCE;
        }

        public HdrCapabilities(int[] supportedHdrTypes, float maxLuminance, float maxAverageLuminance, float minLuminance) {
            this.mSupportedHdrTypes = new int[Display.TYPE_UNKNOWN];
            this.mMaxLuminance = INVALID_LUMINANCE;
            this.mMaxAverageLuminance = INVALID_LUMINANCE;
            this.mMinLuminance = INVALID_LUMINANCE;
            this.mSupportedHdrTypes = supportedHdrTypes;
            this.mMaxLuminance = maxLuminance;
            this.mMaxAverageLuminance = maxAverageLuminance;
            this.mMinLuminance = minLuminance;
        }

        public int[] getSupportedHdrTypes() {
            return this.mSupportedHdrTypes;
        }

        public float getDesiredMaxLuminance() {
            return this.mMaxLuminance;
        }

        public float getDesiredMaxAverageLuminance() {
            return this.mMaxAverageLuminance;
        }

        public float getDesiredMinLuminance() {
            return this.mMinLuminance;
        }

        private HdrCapabilities(Parcel source) {
            this.mSupportedHdrTypes = new int[Display.TYPE_UNKNOWN];
            this.mMaxLuminance = INVALID_LUMINANCE;
            this.mMaxAverageLuminance = INVALID_LUMINANCE;
            this.mMinLuminance = INVALID_LUMINANCE;
            readFromParcel(source);
        }

        public void readFromParcel(Parcel source) {
            int types = source.readInt();
            this.mSupportedHdrTypes = new int[types];
            for (int i = Display.TYPE_UNKNOWN; i < types; i += HDR_TYPE_DOLBY_VISION) {
                this.mSupportedHdrTypes[i] = source.readInt();
            }
            this.mMaxLuminance = source.readFloat();
            this.mMaxAverageLuminance = source.readFloat();
            this.mMinLuminance = source.readFloat();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mSupportedHdrTypes.length);
            for (int i = Display.TYPE_UNKNOWN; i < this.mSupportedHdrTypes.length; i += HDR_TYPE_DOLBY_VISION) {
                dest.writeInt(this.mSupportedHdrTypes[i]);
            }
            dest.writeFloat(this.mMaxLuminance);
            dest.writeFloat(this.mMaxAverageLuminance);
            dest.writeFloat(this.mMinLuminance);
        }

        public int describeContents() {
            return Display.TYPE_UNKNOWN;
        }
    }

    public static final class Mode implements Parcelable {
        public static final Creator<Mode> CREATOR = null;
        public static final Mode[] EMPTY_ARRAY = null;
        private final int mHeight;
        private final int mModeId;
        private final float mRefreshRate;
        private final int mWidth;

        /* renamed from: android.view.Display.Mode.1 */
        static class AnonymousClass1 implements Creator<Mode> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m11createFromParcel(Parcel in) {
                return createFromParcel(in);
            }

            public Mode createFromParcel(Parcel in) {
                return new Mode(in, null);
            }

            public /* bridge */ /* synthetic */ Object[] m12newArray(int size) {
                return newArray(size);
            }

            public Mode[] newArray(int size) {
                return new Mode[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.Display.Mode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.Display.Mode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.view.Display.Mode.<clinit>():void");
        }

        /* synthetic */ Mode(Parcel in, Mode mode) {
            this(in);
        }

        public Mode(int modeId, int width, int height, float refreshRate) {
            this.mModeId = modeId;
            this.mWidth = width;
            this.mHeight = height;
            this.mRefreshRate = refreshRate;
        }

        public int getModeId() {
            return this.mModeId;
        }

        public int getPhysicalWidth() {
            return this.mWidth;
        }

        public int getPhysicalHeight() {
            return this.mHeight;
        }

        public float getRefreshRate() {
            return this.mRefreshRate;
        }

        public boolean matches(int width, int height, float refreshRate) {
            if (this.mWidth == width && this.mHeight == height && Float.floatToIntBits(this.mRefreshRate) == Float.floatToIntBits(refreshRate)) {
                return true;
            }
            return Display.DEBUG;
        }

        public boolean equals(Object other) {
            boolean z = Display.DEBUG;
            if (this == other) {
                return true;
            }
            if (!(other instanceof Mode)) {
                return Display.DEBUG;
            }
            Mode that = (Mode) other;
            if (this.mModeId == that.mModeId) {
                z = matches(that.mWidth, that.mHeight, that.mRefreshRate);
            }
            return z;
        }

        public int hashCode() {
            return ((((((this.mModeId + 17) * 17) + this.mWidth) * 17) + this.mHeight) * 17) + Float.floatToIntBits(this.mRefreshRate);
        }

        public String toString() {
            return "{" + "id=" + this.mModeId + ", width=" + this.mWidth + ", height=" + this.mHeight + ", fps=" + this.mRefreshRate + "}";
        }

        public int describeContents() {
            return Display.TYPE_UNKNOWN;
        }

        private Mode(Parcel in) {
            this(in.readInt(), in.readInt(), in.readInt(), in.readFloat());
        }

        public void writeToParcel(Parcel out, int parcelableFlags) {
            out.writeInt(this.mModeId);
            out.writeInt(this.mWidth);
            out.writeInt(this.mHeight);
            out.writeFloat(this.mRefreshRate);
        }
    }

    public Display(DisplayManagerGlobal global, int displayId, DisplayInfo displayInfo, DisplayAdjustments daj) {
        this.mTempMetrics = new DisplayMetrics();
        this.mGlobal = global;
        this.mDisplayId = displayId;
        this.mDisplayInfo = displayInfo;
        this.mDisplayAdjustments = new DisplayAdjustments(daj);
        this.mIsValid = true;
        this.mLayerStack = displayInfo.layerStack;
        this.mFlags = displayInfo.flags;
        this.mType = displayInfo.type;
        this.mAddress = displayInfo.address;
        this.mOwnerUid = displayInfo.ownerUid;
        this.mOwnerPackageName = displayInfo.ownerPackageName;
    }

    public int getDisplayId() {
        return this.mDisplayId;
    }

    public boolean isValid() {
        boolean z;
        synchronized (this) {
            updateDisplayInfoLocked();
            z = this.mIsValid;
        }
        return z;
    }

    public boolean getDisplayInfo(DisplayInfo outDisplayInfo) {
        boolean z;
        synchronized (this) {
            updateDisplayInfoLocked();
            outDisplayInfo.copyFrom(this.mDisplayInfo);
            z = this.mIsValid;
        }
        return z;
    }

    public int getLayerStack() {
        return this.mLayerStack;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public int getType() {
        return this.mType;
    }

    public String getAddress() {
        return this.mAddress;
    }

    public int getOwnerUid() {
        return this.mOwnerUid;
    }

    public String getOwnerPackageName() {
        return this.mOwnerPackageName;
    }

    public DisplayAdjustments getDisplayAdjustments() {
        return this.mDisplayAdjustments;
    }

    public String getName() {
        String str;
        synchronized (this) {
            updateDisplayInfoLocked();
            str = this.mDisplayInfo.name;
        }
        return str;
    }

    public void getSize(Point outSize) {
        synchronized (this) {
            updateDisplayInfoLocked();
            this.mDisplayInfo.getAppMetrics(this.mTempMetrics, this.mDisplayAdjustments);
            outSize.x = this.mTempMetrics.widthPixels;
            outSize.y = this.mTempMetrics.heightPixels;
            if (this.mRogInfo != null) {
                if (this.mRogEnable) {
                    outSize.x = (int) ((((float) this.mTempMetrics.noncompatWidthPixels) / this.mRogInfo.mRogScale) + 0.5f);
                    outSize.y = (int) ((((float) this.mTempMetrics.noncompatHeightPixels) / this.mRogInfo.mRogScale) + 0.5f);
                } else {
                    outSize.x = this.mTempMetrics.noncompatWidthPixels;
                    outSize.y = this.mTempMetrics.noncompatHeightPixels;
                }
            }
        }
    }

    public void getRectSize(Rect outSize) {
        synchronized (this) {
            updateDisplayInfoLocked();
            this.mDisplayInfo.getAppMetrics(this.mTempMetrics, this.mDisplayAdjustments);
            outSize.set(TYPE_UNKNOWN, TYPE_UNKNOWN, this.mTempMetrics.widthPixels, this.mTempMetrics.heightPixels);
            if (this.mRogInfo != null) {
                if (this.mRogEnable) {
                    outSize.set(TYPE_UNKNOWN, TYPE_UNKNOWN, (int) ((((float) this.mTempMetrics.noncompatWidthPixels) / this.mRogInfo.mRogScale) + 0.5f), (int) ((((float) this.mTempMetrics.noncompatHeightPixels) / this.mRogInfo.mRogScale) + 0.5f));
                } else {
                    outSize.set(TYPE_UNKNOWN, TYPE_UNKNOWN, this.mTempMetrics.noncompatWidthPixels, this.mTempMetrics.noncompatHeightPixels);
                }
            }
        }
    }

    public void getCurrentSizeRange(Point outSmallestSize, Point outLargestSize) {
        synchronized (this) {
            updateDisplayInfoLocked();
            outSmallestSize.x = this.mDisplayInfo.smallestNominalAppWidth;
            outSmallestSize.y = this.mDisplayInfo.smallestNominalAppHeight;
            outLargestSize.x = this.mDisplayInfo.largestNominalAppWidth;
            outLargestSize.y = this.mDisplayInfo.largestNominalAppHeight;
        }
    }

    public int getMaximumSizeDimension() {
        int max;
        synchronized (this) {
            updateDisplayInfoLocked();
            max = Math.max(this.mDisplayInfo.logicalWidth, this.mDisplayInfo.logicalHeight);
        }
        return max;
    }

    @Deprecated
    public int getWidth() {
        synchronized (this) {
            updateCachedAppSizeIfNeededLocked();
            int i;
            if (this.mRogInfo != null) {
                if (this.mRogEnable) {
                    i = (int) ((((float) this.mTempMetrics.noncompatWidthPixels) / this.mRogInfo.mRogScale) + 0.5f);
                } else {
                    i = this.mTempMetrics.noncompatWidthPixels;
                }
                return i;
            }
            i = this.mCachedAppWidthCompat;
            return i;
        }
    }

    @Deprecated
    public int getHeight() {
        synchronized (this) {
            updateCachedAppSizeIfNeededLocked();
            int i;
            if (this.mRogInfo != null) {
                if (this.mRogEnable) {
                    i = (int) ((((float) this.mTempMetrics.noncompatHeightPixels) / this.mRogInfo.mRogScale) + 0.5f);
                } else {
                    i = this.mTempMetrics.noncompatHeightPixels;
                }
                return i;
            }
            i = this.mCachedAppHeightCompat;
            return i;
        }
    }

    public void getOverscanInsets(Rect outRect) {
        synchronized (this) {
            updateDisplayInfoLocked();
            outRect.set(this.mDisplayInfo.overscanLeft, this.mDisplayInfo.overscanTop, this.mDisplayInfo.overscanRight, this.mDisplayInfo.overscanBottom);
        }
    }

    public int getRotation() {
        int i;
        synchronized (this) {
            updateDisplayInfoLocked();
            i = this.mDisplayInfo.rotation;
        }
        return i;
    }

    @Deprecated
    public int getOrientation() {
        return getRotation();
    }

    @Deprecated
    public int getPixelFormat() {
        return TYPE_BUILT_IN;
    }

    public float getRefreshRate() {
        float refreshRate;
        synchronized (this) {
            updateDisplayInfoLocked();
            refreshRate = this.mDisplayInfo.getMode().getRefreshRate();
        }
        return refreshRate;
    }

    @Deprecated
    public float[] getSupportedRefreshRates() {
        float[] defaultRefreshRates;
        synchronized (this) {
            updateDisplayInfoLocked();
            defaultRefreshRates = this.mDisplayInfo.getDefaultRefreshRates();
        }
        return defaultRefreshRates;
    }

    public Mode getMode() {
        Mode mode;
        synchronized (this) {
            updateDisplayInfoLocked();
            mode = this.mDisplayInfo.getMode();
        }
        return mode;
    }

    public Mode[] getSupportedModes() {
        Mode[] modeArr;
        synchronized (this) {
            updateDisplayInfoLocked();
            Mode[] modes = this.mDisplayInfo.supportedModes;
            modeArr = (Mode[]) Arrays.copyOf(modes, modes.length);
        }
        return modeArr;
    }

    public void requestColorTransform(ColorTransform colorTransform) {
        this.mGlobal.requestColorTransform(this.mDisplayId, colorTransform.getId());
    }

    public ColorTransform getColorTransform() {
        ColorTransform colorTransform;
        synchronized (this) {
            updateDisplayInfoLocked();
            colorTransform = this.mDisplayInfo.getColorTransform();
        }
        return colorTransform;
    }

    public ColorTransform getDefaultColorTransform() {
        ColorTransform defaultColorTransform;
        synchronized (this) {
            updateDisplayInfoLocked();
            defaultColorTransform = this.mDisplayInfo.getDefaultColorTransform();
        }
        return defaultColorTransform;
    }

    public HdrCapabilities getHdrCapabilities() {
        HdrCapabilities hdrCapabilities;
        synchronized (this) {
            updateDisplayInfoLocked();
            hdrCapabilities = this.mDisplayInfo.hdrCapabilities;
        }
        return hdrCapabilities;
    }

    public ColorTransform[] getSupportedColorTransforms() {
        ColorTransform[] colorTransformArr;
        synchronized (this) {
            updateDisplayInfoLocked();
            ColorTransform[] transforms = this.mDisplayInfo.supportedColorTransforms;
            colorTransformArr = (ColorTransform[]) Arrays.copyOf(transforms, transforms.length);
        }
        return colorTransformArr;
    }

    public long getAppVsyncOffsetNanos() {
        long j;
        synchronized (this) {
            updateDisplayInfoLocked();
            j = this.mDisplayInfo.appVsyncOffsetNanos;
        }
        return j;
    }

    public long getPresentationDeadlineNanos() {
        long j;
        synchronized (this) {
            updateDisplayInfoLocked();
            j = this.mDisplayInfo.presentationDeadlineNanos;
        }
        return j;
    }

    public void getMetrics(DisplayMetrics outMetrics) {
        synchronized (this) {
            updateDisplayInfoLocked();
            this.mDisplayInfo.getAppMetrics(outMetrics, this.mDisplayAdjustments);
            if (this.mRogInfo != null) {
                if (this.mRogEnable) {
                    outMetrics.heightPixels = (int) ((((float) outMetrics.noncompatHeightPixels) / this.mRogInfo.mRogScale) + 0.5f);
                    outMetrics.widthPixels = (int) ((((float) outMetrics.noncompatWidthPixels) / this.mRogInfo.mRogScale) + 0.5f);
                } else {
                    outMetrics.heightPixels = outMetrics.noncompatHeightPixels;
                    outMetrics.widthPixels = outMetrics.noncompatWidthPixels;
                }
            }
        }
    }

    public void getRealSize(Point outSize) {
        synchronized (this) {
            updateDisplayInfoLocked();
            Application a = ActivityThread.currentApplication();
            ActivityThread at = ActivityThread.currentActivityThread();
            if (a == null || a.mLoadedApk == null || a.mLoadedApk.getResources(at).getCompatibilityInfo().supportsScreen()) {
                outSize.x = this.mDisplayInfo.logicalWidth;
                outSize.y = this.mDisplayInfo.logicalHeight;
                return;
            }
            DisplayMetrics metrics = a.mLoadedApk.getResources(at).getDisplayMetrics();
            float ratio = metrics.noncompatDensity / metrics.density;
            outSize.x = (int) (((float) this.mDisplayInfo.logicalWidth) / ratio);
            outSize.y = (int) (((float) this.mDisplayInfo.logicalHeight) / ratio);
        }
    }

    public void getRealMetrics(DisplayMetrics outMetrics) {
        synchronized (this) {
            updateDisplayInfoLocked();
            Application a = ActivityThread.currentApplication();
            ActivityThread at = ActivityThread.currentActivityThread();
            if (a == null || a.mLoadedApk == null || a.mLoadedApk.getResources(at).getCompatibilityInfo().supportsScreen()) {
                this.mDisplayInfo.getLogicalMetrics(outMetrics, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, null);
                return;
            }
            this.mDisplayInfo.getLogicalMetrics(outMetrics, a.mLoadedApk.getResources(at).getCompatibilityInfo(), null);
        }
    }

    public int getState() {
        int i;
        synchronized (this) {
            updateDisplayInfoLocked();
            i = this.mIsValid ? this.mDisplayInfo.state : TYPE_UNKNOWN;
        }
        return i;
    }

    public boolean hasAccess(int uid) {
        return hasAccess(uid, this.mFlags, this.mOwnerUid);
    }

    public static boolean hasAccess(int uid, int flags, int ownerUid) {
        if ((flags & TYPE_OVERLAY) == 0 || uid == ownerUid || uid == RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED || uid == 0) {
            return true;
        }
        return DEBUG;
    }

    public boolean isPublicPresentation() {
        return (this.mFlags & 12) == FLAG_PRESENTATION ? true : DEBUG;
    }

    private void updateDisplayInfoLocked() {
        DisplayInfo newInfo = this.mGlobal.getDisplayInfo(this.mDisplayId);
        if (newInfo != null) {
            this.mDisplayInfo = newInfo;
            if (!this.mIsValid) {
                this.mIsValid = true;
            }
        } else if (this.mIsValid) {
            this.mIsValid = DEBUG;
        }
    }

    private void updateCachedAppSizeIfNeededLocked() {
        long now = SystemClock.uptimeMillis();
        if (now > this.mLastCachedAppSizeUpdate + 20) {
            updateDisplayInfoLocked();
            this.mDisplayInfo.getAppMetrics(this.mTempMetrics, this.mDisplayAdjustments);
            this.mCachedAppWidthCompat = this.mTempMetrics.widthPixels;
            this.mCachedAppHeightCompat = this.mTempMetrics.heightPixels;
            this.mLastCachedAppSizeUpdate = now;
        }
    }

    public String toString() {
        String str;
        synchronized (this) {
            updateDisplayInfoLocked();
            this.mDisplayInfo.getAppMetrics(this.mTempMetrics, this.mDisplayAdjustments);
            str = "Display id " + this.mDisplayId + ": " + this.mDisplayInfo + ", " + this.mTempMetrics + ", isValid=" + this.mIsValid;
        }
        return str;
    }

    public static String typeToString(int type) {
        switch (type) {
            case TYPE_UNKNOWN /*0*/:
                return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
            case TYPE_BUILT_IN /*1*/:
                return "BUILT_IN";
            case TYPE_HDMI /*2*/:
                return "HDMI";
            case TYPE_WIFI /*3*/:
                return "WIFI";
            case TYPE_OVERLAY /*4*/:
                return "OVERLAY";
            case TYPE_VIRTUAL /*5*/:
                return "VIRTUAL";
            default:
                return Integer.toString(type);
        }
    }

    public static String stateToString(int state) {
        switch (state) {
            case TYPE_UNKNOWN /*0*/:
                return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
            case TYPE_BUILT_IN /*1*/:
                return "OFF";
            case TYPE_HDMI /*2*/:
                return "ON";
            case TYPE_WIFI /*3*/:
                return "DOZE";
            case TYPE_OVERLAY /*4*/:
                return "DOZE_SUSPEND";
            default:
                return Integer.toString(state);
        }
    }

    public static boolean isSuspendedState(int state) {
        return (state == TYPE_BUILT_IN || state == TYPE_OVERLAY) ? true : DEBUG;
    }

    public void setRogInfo(AppRogInfo rogInfo, boolean rogEnable) {
        synchronized (this) {
            this.mRogEnable = rogEnable;
            this.mRogInfo = rogInfo;
        }
    }
}
