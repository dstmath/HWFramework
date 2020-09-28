package android.view;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityThread;
import android.app.Application;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.ColorSpace;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.CoordinationModeUtils;
import android.util.DisplayMetrics;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import com.android.internal.telephony.IccCardConstants;
import com.huawei.android.fsm.HwFoldScreenManager;
import com.huawei.android.view.HwWindowManager;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
    public static final int FLAG_SHOULD_SHOW_SYSTEM_DECORATIONS = 64;
    public static final int FLAG_SUPPORTS_PROTECTED_BUFFERS = 1;
    public static final int INVALID_DISPLAY = -1;
    public static final int REMOVE_MODE_DESTROY_CONTENT = 1;
    public static final int REMOVE_MODE_MOVE_CONTENT_TO_PRIMARY = 0;
    private static final String SIDE_PROP = SystemProperties.get("ro.config.hw_curved_side_disp", "");
    public static final int STATE_DOZE = 3;
    public static final int STATE_DOZE_SUSPEND = 4;
    public static final int STATE_OFF = 1;
    public static final int STATE_ON = 2;
    public static final int STATE_ON_SUSPEND = 6;
    public static final int STATE_UNKNOWN = 0;
    public static final int STATE_VR = 5;
    private static final String TAG = "Display";
    public static final int TYPE_BUILT_IN = 1;
    @UnsupportedAppUsage
    public static final int TYPE_HDMI = 2;
    public static final int TYPE_OVERLAY = 4;
    @UnsupportedAppUsage
    public static final int TYPE_UNKNOWN = 0;
    @UnsupportedAppUsage
    public static final int TYPE_VIRTUAL = 5;
    @UnsupportedAppUsage
    public static final int TYPE_WIFI = 3;
    private final DisplayAddress mAddress;
    private int mCachedAppHeightCompat;
    private int mCachedAppWidthCompat;
    private DisplayAdjustments mDisplayAdjustments;
    private final int mDisplayId;
    @UnsupportedAppUsage
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

    public Display(DisplayManagerGlobal global, int displayId, DisplayInfo displayInfo, DisplayAdjustments daj) {
        this(global, displayId, displayInfo, daj, null);
    }

    public Display(DisplayManagerGlobal global, int displayId, DisplayInfo displayInfo, Resources res) {
        this(global, displayId, displayInfo, null, res);
    }

    private Display(DisplayManagerGlobal global, int displayId, DisplayInfo displayInfo, DisplayAdjustments daj, Resources res) {
        DisplayAdjustments displayAdjustments;
        this.mTempMetrics = new DisplayMetrics();
        this.mGlobal = global;
        this.mDisplayId = displayId;
        this.mDisplayInfo = displayInfo;
        this.mResources = res;
        Resources resources = this.mResources;
        if (resources != null) {
            displayAdjustments = new DisplayAdjustments(resources.getConfiguration());
        } else {
            displayAdjustments = daj != null ? new DisplayAdjustments(daj) : null;
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

    public String getUniqueId() {
        return this.mDisplayInfo.uniqueId;
    }

    public boolean isValid() {
        boolean z;
        synchronized (this) {
            updateDisplayInfoLocked();
            z = this.mIsValid;
        }
        return z;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28)
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

    @UnsupportedAppUsage
    public int getType() {
        return this.mType;
    }

    @UnsupportedAppUsage
    public DisplayAddress getAddress() {
        return this.mAddress;
    }

    public int getOwnerUid() {
        return this.mOwnerUid;
    }

    @UnsupportedAppUsage
    public String getOwnerPackageName() {
        return this.mOwnerPackageName;
    }

    @UnsupportedAppUsage
    public DisplayAdjustments getDisplayAdjustments() {
        Resources resources = this.mResources;
        if (resources != null) {
            DisplayAdjustments currentAdjustements = resources.getDisplayAdjustments();
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

    @UnsupportedAppUsage
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

    public DisplayCutout getCutout() {
        DisplayCutout displayCutout;
        synchronized (this) {
            updateDisplayInfoLocked();
            displayCutout = this.mDisplayInfo.displayCutout;
        }
        return displayCutout;
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

    public ColorSpace getPreferredWideGamutColorSpace() {
        synchronized (this) {
            updateDisplayInfoLocked();
            if (!this.mDisplayInfo.isWideColorGamut()) {
                return null;
            }
            return this.mGlobal.getPreferredWideGamutColorSpace();
        }
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

    public void getRealSize(Point outSize) {
        String pkgName;
        synchronized (this) {
            updateDisplayInfoLocked();
            Application app = ActivityThread.currentApplication();
            if (!HwFoldScreenManager.isFoldable() || app == null || app.getApplicationInfo() == null || (pkgName = app.getApplicationInfo().packageName) == null || !pkgName.equals("com.huawei.camera") || HwFoldScreenManager.getDisplayMode() != 4 || !(this.mDisplayInfo.rotation == 0 || this.mDisplayInfo.rotation == 2)) {
                outSize.x = this.mDisplayInfo.logicalWidth;
                outSize.y = this.mDisplayInfo.logicalHeight;
                CompatibilityInfo ci = getDisplayAdjustments().getCompatibilityInfo();
                if (!ci.supportsScreen()) {
                    float ratio = ci.getSdrLowResolutionRatio();
                    outSize.x = (int) ((((float) outSize.x) * ratio) + 0.5f);
                    outSize.y = (int) ((((float) outSize.y) * ratio) + 0.5f);
                }
                if (!TextUtils.isEmpty(SIDE_PROP)) {
                    int displaySideWidthPixels = 0;
                    DisplayCutout displayCutout = getCutout();
                    if (!(displayCutout == null || displayCutout.getDisplaySideSafeInsets() == null)) {
                        Rect displaySideSafeInsets = displayCutout.getDisplaySideSafeInsets();
                        displaySideWidthPixels = displaySideSafeInsets.left + displaySideSafeInsets.right + displaySideSafeInsets.top + displaySideSafeInsets.bottom;
                    }
                    if (!(app == null || app.mLoadedApk == null)) {
                        String packagename = app.mLoadedApk.getPackageName();
                        int isSystem = app.getApplicationInfo().flags & 1;
                        if (outSize.x < outSize.y) {
                            boolean isExpand = HwWindowManager.isAppNeedExpand(packagename);
                            if (isSystem == 0 && !isExpand) {
                                outSize.x -= displaySideWidthPixels;
                            }
                        } else if (outSize.x > outSize.y && isSystem == 0) {
                            outSize.y -= displaySideWidthPixels;
                        }
                    }
                }
                return;
            }
            outSize.x = CoordinationModeUtils.getFoldScreenMainWidth();
            outSize.y = this.mDisplayInfo.logicalHeight;
        }
    }

    public void getRealMetrics(DisplayMetrics outMetrics) {
        synchronized (this) {
            updateDisplayInfoLocked();
            Application app = ActivityThread.currentApplication();
            if (app == null || app.mLoadedApk == null || app.mLoadedApk.getResources().getCompatibilityInfo().supportsScreen()) {
                this.mDisplayInfo.getLogicalMetrics(outMetrics, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, null);
            } else {
                this.mDisplayInfo.getLogicalMetrics(outMetrics, app.mLoadedApk.getResources().getCompatibilityInfo(), null);
            }
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
        return hasAccess(uid, this.mFlags, this.mOwnerUid, this.mDisplayId);
    }

    public static boolean hasAccess(int uid, int flags, int ownerUid, int displayId) {
        return (flags & 4) == 0 || uid == ownerUid || uid == 1000 || uid == 0 || DisplayManagerGlobal.getInstance().isUidPresentOnDisplay(uid, displayId);
    }

    public boolean isPublicPresentation() {
        return (this.mFlags & 12) == 8;
    }

    private void updateDisplayInfoLocked() {
        ActivityThread thread;
        Configuration overrideConfig;
        DisplayInfo newInfo = this.mGlobal.getDisplayInfo(this.mDisplayId);
        if (newInfo != null) {
            this.mDisplayInfo = newInfo;
            if (!this.mIsValid) {
                this.mIsValid = true;
            }
        } else if (this.mIsValid) {
            this.mIsValid = false;
        }
        if (HwPCUtils.enabled() && (thread = ActivityThread.currentActivityThread()) != null && (overrideConfig = thread.getOverrideConfig()) != null && !overrideConfig.equals(Configuration.EMPTY) && HwPCUtils.isValidExtDisplayId(thread.getDisplayId())) {
            float density = ((float) overrideConfig.densityDpi) * 0.00625f;
            DisplayInfo displayInfo = this.mDisplayInfo;
            int i = (int) (((float) overrideConfig.screenWidthDp) * density);
            displayInfo.appWidth = i;
            displayInfo.logicalWidth = i;
            DisplayInfo displayInfo2 = this.mDisplayInfo;
            int i2 = (int) (((float) overrideConfig.screenHeightDp) * density);
            displayInfo2.appHeight = i2;
            displayInfo2.logicalHeight = i2;
            DisplayInfo displayInfo3 = this.mDisplayInfo;
            displayInfo3.rotation = displayInfo3.appWidth <= this.mDisplayInfo.appHeight ? 0 : 1;
        }
        if (HwMwUtils.ENABLED) {
            HwMwUtils.performPolicy(81, this.mDisplayInfo);
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
        if (type == 0) {
            return IccCardConstants.INTENT_VALUE_ICC_UNKNOWN;
        }
        if (type == 1) {
            return "BUILT_IN";
        }
        if (type == 2) {
            return "HDMI";
        }
        if (type == 3) {
            return "WIFI";
        }
        if (type == 4) {
            return "OVERLAY";
        }
        if (type != 5) {
            return Integer.toString(type);
        }
        return "VIRTUAL";
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
            case 6:
                return "ON_SUSPEND";
            default:
                return Integer.toString(state);
        }
    }

    public static boolean isSuspendedState(int state) {
        return state == 1 || state == 4 || state == 6;
    }

    public static boolean isDozeState(int state) {
        return state == 3 || state == 4;
    }

    public static final class Mode implements Parcelable {
        public static final Parcelable.Creator<Mode> CREATOR = new Parcelable.Creator<Mode>() {
            /* class android.view.Display.Mode.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public Mode createFromParcel(Parcel in) {
                return new Mode(in);
            }

            @Override // android.os.Parcelable.Creator
            public Mode[] newArray(int size) {
                return new Mode[size];
            }
        };
        public static final Mode[] EMPTY_ARRAY = new Mode[0];
        private final int mHeight;
        private final int mModeId;
        private final float mRefreshRate;
        private final int mWidth;

        @UnsupportedAppUsage
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
            return this.mWidth == width && this.mHeight == height && Float.floatToIntBits(this.mRefreshRate) == Float.floatToIntBits(refreshRate);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Mode)) {
                return false;
            }
            Mode that = (Mode) other;
            if (this.mModeId != that.mModeId || !matches(that.mWidth, that.mHeight, that.mRefreshRate)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return (((((((1 * 17) + this.mModeId) * 17) + this.mWidth) * 17) + this.mHeight) * 17) + Float.floatToIntBits(this.mRefreshRate);
        }

        public String toString() {
            return "{" + "id=" + this.mModeId + ", width=" + this.mWidth + ", height=" + this.mHeight + ", fps=" + this.mRefreshRate + "}";
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        private Mode(Parcel in) {
            this(in.readInt(), in.readInt(), in.readInt(), in.readFloat());
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int parcelableFlags) {
            out.writeInt(this.mModeId);
            out.writeInt(this.mWidth);
            out.writeInt(this.mHeight);
            out.writeFloat(this.mRefreshRate);
        }
    }

    public static final class HdrCapabilities implements Parcelable {
        public static final Parcelable.Creator<HdrCapabilities> CREATOR = new Parcelable.Creator<HdrCapabilities>() {
            /* class android.view.Display.HdrCapabilities.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public HdrCapabilities createFromParcel(Parcel source) {
                return new HdrCapabilities(source);
            }

            @Override // android.os.Parcelable.Creator
            public HdrCapabilities[] newArray(int size) {
                return new HdrCapabilities[size];
            }
        };
        public static final int HDR_TYPE_DOLBY_VISION = 1;
        public static final int HDR_TYPE_HDR10 = 2;
        public static final int HDR_TYPE_HDR10_PLUS = 4;
        public static final int HDR_TYPE_HLG = 3;
        public static final float INVALID_LUMINANCE = -1.0f;
        private float mMaxAverageLuminance;
        private float mMaxLuminance;
        private float mMinLuminance;
        private int[] mSupportedHdrTypes;

        @Retention(RetentionPolicy.SOURCE)
        public @interface HdrType {
        }

        public HdrCapabilities() {
            this.mSupportedHdrTypes = new int[0];
            this.mMaxLuminance = -1.0f;
            this.mMaxAverageLuminance = -1.0f;
            this.mMinLuminance = -1.0f;
        }

        @UnsupportedAppUsage
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
            if (this == other) {
                return true;
            }
            if (!(other instanceof HdrCapabilities)) {
                return false;
            }
            HdrCapabilities that = (HdrCapabilities) other;
            if (Arrays.equals(this.mSupportedHdrTypes, that.mSupportedHdrTypes) && this.mMaxLuminance == that.mMaxLuminance && this.mMaxAverageLuminance == that.mMaxAverageLuminance && this.mMinLuminance == that.mMinLuminance) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (((((((23 * 17) + Arrays.hashCode(this.mSupportedHdrTypes)) * 17) + Float.floatToIntBits(this.mMaxLuminance)) * 17) + Float.floatToIntBits(this.mMaxAverageLuminance)) * 17) + Float.floatToIntBits(this.mMinLuminance);
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

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mSupportedHdrTypes.length);
            int i = 0;
            while (true) {
                int[] iArr = this.mSupportedHdrTypes;
                if (i < iArr.length) {
                    dest.writeInt(iArr[i]);
                    i++;
                } else {
                    dest.writeFloat(this.mMaxLuminance);
                    dest.writeFloat(this.mMaxAverageLuminance);
                    dest.writeFloat(this.mMinLuminance);
                    return;
                }
            }
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }
    }
}
