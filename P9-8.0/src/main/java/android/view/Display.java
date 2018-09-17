package android.view;

import android.app.ActivityThread;
import android.app.Application;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.LogException;
import com.android.internal.R;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.telephony.IccCardConstants;
import java.util.Arrays;

public final class Display {
    private static final int CACHED_APP_SIZE_DURATION_MILLIS = 20;
    public static final int COLOR_MODE_ADOBE_RGB = 8;
    public static final int COLOR_MODE_BT601_525 = 3;
    public static final int COLOR_MODE_BT601_525_UNADJUSTED = 4;
    public static final int COLOR_MODE_BT601_625 = 1;
    public static final int COLOR_MODE_BT601_625_UNADJUSTED = 2;
    public static final int COLOR_MODE_BT709 = 5;
    public static final int COLOR_MODE_DCI_P3 = 6;
    public static final int COLOR_MODE_DEFAULT = 0;
    public static final int COLOR_MODE_DISPLAY_P3 = 9;
    public static final int COLOR_MODE_INVALID = -1;
    public static final int COLOR_MODE_SRGB = 7;
    private static final boolean DEBUG = false;
    public static final int DEFAULT_DISPLAY = 0;
    public static final int FLAG_CAN_SHOW_WITH_INSECURE_KEYGUARD = 32;
    public static final int FLAG_PRESENTATION = 8;
    public static final int FLAG_PRIVATE = 4;
    public static final int FLAG_ROUND = 16;
    public static final int FLAG_SCALING_DISABLED = 1073741824;
    public static final int FLAG_SECURE = 2;
    public static final int FLAG_SUPPORTS_PROTECTED_BUFFERS = 1;
    public static final int INVALID_DISPLAY = -1;
    protected static final boolean IS_NOTCH_PROP = (!SystemProperties.get("ro.config.hw_notch_size", LogException.NO_VALUE).equals(LogException.NO_VALUE));
    public static final int REMOVE_MODE_DESTROY_CONTENT = 1;
    public static final int REMOVE_MODE_MOVE_CONTENT_TO_PRIMARY = 0;
    public static final int STATE_DOZE = 3;
    public static final int STATE_DOZE_SUSPEND = 4;
    public static final int STATE_OFF = 1;
    public static final int STATE_ON = 2;
    public static final int STATE_UNKNOWN = 0;
    public static final int STATE_VR = 5;
    private static final String TAG = "Display";
    public static final int TYPE_BUILT_IN = 1;
    public static final int TYPE_HDMI = 2;
    public static final int TYPE_OVERLAY = 4;
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_VIRTUAL = 5;
    public static final int TYPE_WIFI = 3;
    private static String[] thirdAppNeedNotchAreaLandscape = new String[]{"com.gameloft.android.GloftDMKF", "com.gameloft.android.GloftMC4M", "com.gameloft.android.GloftSMIF", "com.gameloft.android.GloftRF16", "com.gameloft.android.LATAM.GloftPDMF", "com.gameloft.android.GloftSCRT", "com.gameloft.android.GloftMBCF", "com.gameloft.android.GloftMOTR", "com.gameloft.android.GloftPDMF", "com.gameloft.android.GloftR8HP"};
    private static String[] thirdAppNeedNotchAreaPortrait = new String[]{"com.gameloft.android.GloftDBMF", "com.episodeinteractive.android.catalog", "com.gameloft.android.LATAM.GloftBUB3", "com.zenstudios.ZenPinball"};
    private final String mAddress;
    private int mCachedAppHeightCompat;
    private int mCachedAppWidthCompat;
    private DisplayAdjustments mDisplayAdjustments;
    private final int mDisplayId;
    private DisplayInfo mDisplayInfo;
    private final int mFlags;
    private final DisplayManagerGlobal mGlobal;
    private boolean mIsValid;
    private long mLastCachedAppSizeUpdate;
    private final int mLayerStack;
    private final String mOwnerPackageName;
    private final int mOwnerUid;
    private final Resources mResources;
    private final DisplayMetrics mTempMetrics;
    private final int mType;

    public static final class ColorTransform implements Parcelable {
        public static final Creator<ColorTransform> CREATOR = new Creator<ColorTransform>() {
            public ColorTransform createFromParcel(Parcel in) {
                return new ColorTransform(in, null);
            }

            public ColorTransform[] newArray(int size) {
                return new ColorTransform[size];
            }
        };
        public static final ColorTransform[] EMPTY_ARRAY = new ColorTransform[0];
        private final int mColorTransform;
        private final int mId;

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
                return false;
            }
            ColorTransform that = (ColorTransform) other;
            if (this.mId != that.mId) {
                z = false;
            } else if (this.mColorTransform != that.mColorTransform) {
                z = false;
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
            return 0;
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
        public static final Creator<HdrCapabilities> CREATOR = new Creator<HdrCapabilities>() {
            public HdrCapabilities createFromParcel(Parcel source) {
                return new HdrCapabilities(source, null);
            }

            public HdrCapabilities[] newArray(int size) {
                return new HdrCapabilities[size];
            }
        };
        public static final int HDR_TYPE_DOLBY_VISION = 1;
        public static final int HDR_TYPE_HDR10 = 2;
        public static final int HDR_TYPE_HLG = 3;
        public static final float INVALID_LUMINANCE = -1.0f;
        private float mMaxAverageLuminance;
        private float mMaxLuminance;
        private float mMinLuminance;
        private int[] mSupportedHdrTypes;

        /* synthetic */ HdrCapabilities(Parcel source, HdrCapabilities -this1) {
            this(source);
        }

        public HdrCapabilities() {
            this.mSupportedHdrTypes = new int[0];
            this.mMaxLuminance = -1.0f;
            this.mMaxAverageLuminance = -1.0f;
            this.mMinLuminance = -1.0f;
        }

        public HdrCapabilities(int[] supportedHdrTypes, float maxLuminance, float maxAverageLuminance, float minLuminance) {
            this.mSupportedHdrTypes = new int[0];
            this.mMaxLuminance = -1.0f;
            this.mMaxAverageLuminance = -1.0f;
            this.mMinLuminance = -1.0f;
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

        public boolean equals(Object other) {
            boolean z = true;
            if (this == other) {
                return true;
            }
            if (!(other instanceof HdrCapabilities)) {
                return false;
            }
            HdrCapabilities that = (HdrCapabilities) other;
            if (!Arrays.equals(this.mSupportedHdrTypes, that.mSupportedHdrTypes) || this.mMaxLuminance != that.mMaxLuminance || this.mMaxAverageLuminance != that.mMaxAverageLuminance) {
                z = false;
            } else if (this.mMinLuminance != that.mMinLuminance) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return ((((((Arrays.hashCode(this.mSupportedHdrTypes) + MetricsEvent.ACTION_WINDOW_DOCK_UNRESIZABLE) * 17) + Float.floatToIntBits(this.mMaxLuminance)) * 17) + Float.floatToIntBits(this.mMaxAverageLuminance)) * 17) + Float.floatToIntBits(this.mMinLuminance);
        }

        private HdrCapabilities(Parcel source) {
            this.mSupportedHdrTypes = new int[0];
            this.mMaxLuminance = -1.0f;
            this.mMaxAverageLuminance = -1.0f;
            this.mMinLuminance = -1.0f;
            readFromParcel(source);
        }

        public void readFromParcel(Parcel source) {
            int types = source.readInt();
            this.mSupportedHdrTypes = new int[types];
            for (int i = 0; i < types; i++) {
                this.mSupportedHdrTypes[i] = source.readInt();
            }
            this.mMaxLuminance = source.readFloat();
            this.mMaxAverageLuminance = source.readFloat();
            this.mMinLuminance = source.readFloat();
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mSupportedHdrTypes.length);
            for (int writeInt : this.mSupportedHdrTypes) {
                dest.writeInt(writeInt);
            }
            dest.writeFloat(this.mMaxLuminance);
            dest.writeFloat(this.mMaxAverageLuminance);
            dest.writeFloat(this.mMinLuminance);
        }

        public int describeContents() {
            return 0;
        }
    }

    public static final class Mode implements Parcelable {
        public static final Creator<Mode> CREATOR = new Creator<Mode>() {
            public Mode createFromParcel(Parcel in) {
                return new Mode(in, null);
            }

            public Mode[] newArray(int size) {
                return new Mode[size];
            }
        };
        public static final Mode[] EMPTY_ARRAY = new Mode[0];
        private final int mHeight;
        private final int mModeId;
        private final float mRefreshRate;
        private final int mWidth;

        /* synthetic */ Mode(Parcel in, Mode -this1) {
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
            return false;
        }

        public boolean equals(Object other) {
            boolean z = false;
            if (this == other) {
                return true;
            }
            if (!(other instanceof Mode)) {
                return false;
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
            return 0;
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
        this(global, displayId, displayInfo, daj, null);
    }

    public Display(DisplayManagerGlobal global, int displayId, DisplayInfo displayInfo, Resources res) {
        this(global, displayId, displayInfo, null, res);
    }

    private Display(DisplayManagerGlobal global, int displayId, DisplayInfo displayInfo, DisplayAdjustments daj, Resources res) {
        DisplayAdjustments displayAdjustments = null;
        this.mTempMetrics = new DisplayMetrics();
        this.mGlobal = global;
        this.mDisplayId = displayId;
        this.mDisplayInfo = displayInfo;
        this.mResources = res;
        if (this.mResources != null) {
            displayAdjustments = new DisplayAdjustments(this.mResources.getConfiguration());
        } else if (daj != null) {
            displayAdjustments = new DisplayAdjustments(daj);
        }
        this.mDisplayAdjustments = displayAdjustments;
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
        if (this.mResources != null) {
            DisplayAdjustments currentAdjustements = this.mResources.getDisplayAdjustments();
            if (!this.mDisplayAdjustments.equals(currentAdjustements)) {
                this.mDisplayAdjustments = new DisplayAdjustments(currentAdjustements);
            }
        }
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
            this.mDisplayInfo.getAppMetrics(this.mTempMetrics, getDisplayAdjustments());
            outSize.x = this.mTempMetrics.widthPixels;
            outSize.y = this.mTempMetrics.heightPixels;
        }
    }

    public void getRectSize(Rect outSize) {
        synchronized (this) {
            updateDisplayInfoLocked();
            this.mDisplayInfo.getAppMetrics(this.mTempMetrics, getDisplayAdjustments());
            outSize.set(0, 0, this.mTempMetrics.widthPixels, this.mTempMetrics.heightPixels);
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
        int i;
        synchronized (this) {
            updateCachedAppSizeIfNeededLocked();
            i = this.mCachedAppWidthCompat;
        }
        return i;
    }

    @Deprecated
    public int getHeight() {
        int i;
        synchronized (this) {
            updateCachedAppSizeIfNeededLocked();
            i = this.mCachedAppHeightCompat;
        }
        return i;
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
        return 1;
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

    public void requestColorMode(int colorMode) {
        this.mGlobal.requestColorMode(this.mDisplayId, colorMode);
    }

    public int getColorMode() {
        int i;
        synchronized (this) {
            updateDisplayInfoLocked();
            i = this.mDisplayInfo.colorMode;
        }
        return i;
    }

    public int getRemoveMode() {
        return this.mDisplayInfo.removeMode;
    }

    public HdrCapabilities getHdrCapabilities() {
        HdrCapabilities hdrCapabilities;
        synchronized (this) {
            updateDisplayInfoLocked();
            hdrCapabilities = this.mDisplayInfo.hdrCapabilities;
        }
        return hdrCapabilities;
    }

    public boolean isHdr() {
        boolean isHdr;
        synchronized (this) {
            updateDisplayInfoLocked();
            isHdr = this.mDisplayInfo.isHdr();
        }
        return isHdr;
    }

    public boolean isWideColorGamut() {
        boolean isWideColorGamut;
        synchronized (this) {
            updateDisplayInfoLocked();
            isWideColorGamut = this.mDisplayInfo.isWideColorGamut();
        }
        return isWideColorGamut;
    }

    public int[] getSupportedColorModes() {
        int[] copyOf;
        synchronized (this) {
            updateDisplayInfoLocked();
            int[] colorModes = this.mDisplayInfo.supportedColorModes;
            copyOf = Arrays.copyOf(colorModes, colorModes.length);
        }
        return copyOf;
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
            this.mDisplayInfo.getAppMetrics(outMetrics, getDisplayAdjustments());
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0051, code:
            return;
     */
    /* JADX WARNING: Missing block: B:32:0x009e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getRealSize(Point outSize) {
        int i = 0;
        synchronized (this) {
            updateDisplayInfoLocked();
            Application a = ActivityThread.currentApplication();
            if (a == null || a.mLoadedApk == null || (a.mLoadedApk.getResources().getCompatibilityInfo().supportsScreen() ^ 1) == 0) {
                outSize.x = this.mDisplayInfo.logicalWidth;
                outSize.y = this.mDisplayInfo.logicalHeight;
                if (IS_NOTCH_PROP) {
                    int notchPropSize = 0;
                    String packageName = LogException.NO_VALUE;
                    if (!(a == null || a.mLoadedApk == null)) {
                        notchPropSize = a.mLoadedApk.getResources().getDimensionPixelSize(R.dimen.status_bar_height);
                        if (a.getApplicationInfo() != null) {
                            packageName = a.getApplicationInfo().packageName;
                        }
                    }
                    int rotation = getRotation();
                    String[] strArr;
                    int length;
                    if (rotation == 0) {
                        strArr = thirdAppNeedNotchAreaPortrait;
                        length = strArr.length;
                        while (i < length) {
                            if (strArr[i].equals(packageName)) {
                                outSize.y -= notchPropSize;
                                break;
                            }
                            i++;
                        }
                    } else if (rotation == 1 || rotation == 3) {
                        strArr = thirdAppNeedNotchAreaLandscape;
                        length = strArr.length;
                        while (i < length) {
                            if (strArr[i].equals(packageName)) {
                                outSize.x -= notchPropSize;
                                break;
                            }
                            i++;
                        }
                    }
                }
            } else {
                DisplayMetrics metrics = a.mLoadedApk.getResources().getDisplayMetrics();
                if (metrics.noncompatWidthPixels != 0) {
                    float ratio = (((float) metrics.widthPixels) * 1.0f) / ((float) metrics.noncompatWidthPixels);
                    outSize.x = (int) ((((float) this.mDisplayInfo.logicalWidth) * ratio) + 0.5f);
                    outSize.y = (int) ((((float) this.mDisplayInfo.logicalHeight) * ratio) + 0.5f);
                }
            }
        }
    }

    public void getRealMetrics(DisplayMetrics outMetrics) {
        synchronized (this) {
            updateDisplayInfoLocked();
            Application a = ActivityThread.currentApplication();
            if (a == null || a.mLoadedApk == null || (a.mLoadedApk.getResources().getCompatibilityInfo().supportsScreen() ^ 1) == 0) {
                this.mDisplayInfo.getLogicalMetrics(outMetrics, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, null);
                return;
            }
            this.mDisplayInfo.getLogicalMetrics(outMetrics, a.mLoadedApk.getResources().getCompatibilityInfo(), null);
        }
    }

    public int getState() {
        int i;
        synchronized (this) {
            updateDisplayInfoLocked();
            i = this.mIsValid ? this.mDisplayInfo.state : 0;
        }
        return i;
    }

    public boolean hasAccess(int uid) {
        return hasAccess(uid, this.mFlags, this.mOwnerUid);
    }

    public static boolean hasAccess(int uid, int flags, int ownerUid) {
        if ((flags & 4) == 0 || uid == ownerUid || uid == 1000 || uid == 0) {
            return true;
        }
        return false;
    }

    public boolean isPublicPresentation() {
        return (this.mFlags & 12) == 8;
    }

    private void updateDisplayInfoLocked() {
        int i = 0;
        DisplayInfo newInfo = this.mGlobal.getDisplayInfo(this.mDisplayId);
        if (newInfo != null) {
            this.mDisplayInfo = newInfo;
            if (!this.mIsValid) {
                this.mIsValid = true;
            }
        } else if (this.mIsValid) {
            this.mIsValid = false;
        }
        if (HwPCUtils.enabled()) {
            ActivityThread thread = ActivityThread.currentActivityThread();
            if (thread != null && HwPCUtils.isValidExtDisplayId(thread.getDisplayId())) {
                Configuration overrideConfig = thread.getOverrideConfig();
                if (overrideConfig != null && (overrideConfig.equals(Configuration.EMPTY) ^ 1) != 0) {
                    float density = ((float) overrideConfig.densityDpi) * 0.00625f;
                    DisplayInfo displayInfo = this.mDisplayInfo;
                    int i2 = (int) (((float) overrideConfig.screenWidthDp) * density);
                    this.mDisplayInfo.appWidth = i2;
                    displayInfo.logicalWidth = i2;
                    displayInfo = this.mDisplayInfo;
                    i2 = (int) (((float) overrideConfig.screenHeightDp) * density);
                    this.mDisplayInfo.appHeight = i2;
                    displayInfo.logicalHeight = i2;
                    displayInfo = this.mDisplayInfo;
                    if (this.mDisplayInfo.appWidth > this.mDisplayInfo.appHeight) {
                        i = 1;
                    }
                    displayInfo.rotation = i;
                }
            }
        }
    }

    private void updateCachedAppSizeIfNeededLocked() {
        long now = SystemClock.uptimeMillis();
        if (now > this.mLastCachedAppSizeUpdate + 20) {
            updateDisplayInfoLocked();
            this.mDisplayInfo.getAppMetrics(this.mTempMetrics, getDisplayAdjustments());
            this.mCachedAppWidthCompat = this.mTempMetrics.widthPixels;
            this.mCachedAppHeightCompat = this.mTempMetrics.heightPixels;
            this.mLastCachedAppSizeUpdate = now;
        }
    }

    public String toString() {
        String str;
        synchronized (this) {
            updateDisplayInfoLocked();
            this.mDisplayInfo.getAppMetrics(this.mTempMetrics, getDisplayAdjustments());
            str = "Display id " + this.mDisplayId + ": " + this.mDisplayInfo + ", " + this.mTempMetrics + ", isValid=" + this.mIsValid;
        }
        return str;
    }

    public static String typeToString(int type) {
        switch (type) {
            case 0:
                return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
            case 1:
                return "BUILT_IN";
            case 2:
                return "HDMI";
            case 3:
                return "WIFI";
            case 4:
                return "OVERLAY";
            case 5:
                return "VIRTUAL";
            default:
                return Integer.toString(type);
        }
    }

    public static String stateToString(int state) {
        switch (state) {
            case 0:
                return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
            case 1:
                return "OFF";
            case 2:
                return "ON";
            case 3:
                return "DOZE";
            case 4:
                return "DOZE_SUSPEND";
            case 5:
                return "VR";
            default:
                return Integer.toString(state);
        }
    }

    public static boolean isSuspendedState(int state) {
        return state == 1 || state == 4;
    }
}
