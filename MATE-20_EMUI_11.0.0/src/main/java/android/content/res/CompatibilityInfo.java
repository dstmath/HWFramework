package android.content.res;

import android.annotation.UnsupportedAppUsage;
import android.common.HwFrameworkFactory;
import android.content.pm.ApplicationInfo;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;
import com.huawei.hwaps.IHwApsImpl;

public class CompatibilityInfo implements Parcelable {
    private static final int ALWAYS_NEEDS_COMPAT = 2;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public static final Parcelable.Creator<CompatibilityInfo> CREATOR = new Parcelable.Creator<CompatibilityInfo>() {
        /* class android.content.res.CompatibilityInfo.AnonymousClass2 */

        @Override // android.os.Parcelable.Creator
        public CompatibilityInfo createFromParcel(Parcel source) {
            return new CompatibilityInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public CompatibilityInfo[] newArray(int size) {
            return new CompatibilityInfo[size];
        }
    };
    @UnsupportedAppUsage
    public static final CompatibilityInfo DEFAULT_COMPATIBILITY_INFO = new CompatibilityInfo() {
        /* class android.content.res.CompatibilityInfo.AnonymousClass1 */
    };
    public static final int DEFAULT_NORMAL_SHORT_DIMENSION = 320;
    public static final float MAXIMUM_ASPECT_RATIO = 1.7791667f;
    private static final int NEEDS_COMPAT_RES = 16;
    private static final int NEEDS_SCREEN_COMPAT = 8;
    private static final int NEVER_NEEDS_COMPAT = 4;
    private static final int SCALING_REQUIRED = 1;
    public final int applicationDensity;
    public final float applicationInvertedScale;
    @UnsupportedAppUsage
    public final float applicationScale;
    public ApplicationInfo mAppInfo;
    private float mApsResolutionRatio;
    private final int mCompatibilityFlags;
    private IHwApsImpl mHwApsImpl;

    @UnsupportedAppUsage
    public CompatibilityInfo(ApplicationInfo appInfo, int screenLayout, int sw, boolean forceCompat) {
        int required;
        this.mApsResolutionRatio = 1.0f;
        this.mHwApsImpl = HwFrameworkFactory.getHwApsImpl();
        int compatFlags = 0;
        this.mAppInfo = appInfo;
        compatFlags = appInfo.targetSdkVersion < 26 ? 0 | 16 : compatFlags;
        if (appInfo.requiresSmallestWidthDp == 0 && appInfo.compatibleWidthLimitDp == 0 && appInfo.largestWidthLimitDp == 0) {
            int sizeInfo = 0;
            boolean anyResizeable = false;
            if ((appInfo.flags & 2048) != 0) {
                sizeInfo = 0 | 8;
                anyResizeable = true;
                if (!forceCompat) {
                    sizeInfo |= 34;
                }
            }
            if ((appInfo.flags & 524288) != 0) {
                anyResizeable = true;
                if (!forceCompat) {
                    sizeInfo |= 34;
                }
            }
            if ((appInfo.flags & 4096) != 0) {
                anyResizeable = true;
                sizeInfo |= 2;
            }
            sizeInfo = forceCompat ? sizeInfo & -3 : sizeInfo;
            compatFlags |= 8;
            int i = screenLayout & 15;
            if (i == 3) {
                compatFlags = (sizeInfo & 8) != 0 ? compatFlags & -9 : compatFlags;
                if ((appInfo.flags & 2048) != 0) {
                    compatFlags |= 4;
                }
            } else if (i == 4) {
                compatFlags = (sizeInfo & 32) != 0 ? compatFlags & -9 : compatFlags;
                if ((appInfo.flags & 524288) != 0) {
                    compatFlags |= 4;
                }
            }
            if ((268435456 & screenLayout) == 0) {
                compatFlags = (compatFlags & -9) | 4;
            } else if ((sizeInfo & 2) != 0) {
                compatFlags &= -9;
            } else if (!anyResizeable) {
                compatFlags |= 2;
            }
            if ((appInfo.flags & 8192) != 0) {
                this.applicationDensity = DisplayMetrics.DENSITY_DEVICE;
                this.applicationScale = 1.0f;
                this.applicationInvertedScale = 1.0f;
            } else {
                this.applicationDensity = 160;
                this.applicationScale = ((float) DisplayMetrics.DENSITY_DEVICE) / 160.0f;
                this.applicationInvertedScale = 1.0f / this.applicationScale;
                compatFlags |= 1;
            }
        } else {
            if (appInfo.requiresSmallestWidthDp != 0) {
                required = appInfo.requiresSmallestWidthDp;
            } else {
                required = appInfo.compatibleWidthLimitDp;
            }
            required = required == 0 ? appInfo.largestWidthLimitDp : required;
            int compat = appInfo.compatibleWidthLimitDp != 0 ? appInfo.compatibleWidthLimitDp : required;
            compat = compat < required ? required : compat;
            int largest = appInfo.largestWidthLimitDp;
            if (required > 320) {
                compatFlags |= 4;
            } else if (largest != 0 && sw > largest) {
                compatFlags |= 10;
            } else if (compat >= sw) {
                compatFlags |= 4;
            } else if (forceCompat) {
                compatFlags |= 8;
            }
            this.applicationDensity = DisplayMetrics.DENSITY_DEVICE;
            this.applicationScale = 1.0f;
            this.applicationInvertedScale = 1.0f;
        }
        IHwApsImpl iHwApsImpl = this.mHwApsImpl;
        if (iHwApsImpl != null) {
            float resolutionRatio = iHwApsImpl.getResolutionRatioByPkgName(appInfo.packageName);
            if (this.mHwApsImpl.isValidSdrRatio(resolutionRatio)) {
                compatFlags = 8;
                this.mApsResolutionRatio = resolutionRatio;
            }
        }
        this.mCompatibilityFlags = compatFlags;
    }

    private CompatibilityInfo(int compFlags, int dens, float scale, float invertedScale) {
        this.mApsResolutionRatio = 1.0f;
        this.mHwApsImpl = HwFrameworkFactory.getHwApsImpl();
        this.mCompatibilityFlags = compFlags;
        this.applicationDensity = dens;
        this.applicationScale = scale;
        this.applicationInvertedScale = invertedScale;
    }

    @UnsupportedAppUsage
    private CompatibilityInfo() {
        this(4, DisplayMetrics.DENSITY_DEVICE, 1.0f, 1.0f);
    }

    @UnsupportedAppUsage
    public boolean isScalingRequired() {
        return (this.mCompatibilityFlags & 1) != 0;
    }

    @UnsupportedAppUsage
    public boolean supportsScreen() {
        return (this.mCompatibilityFlags & 8) == 0;
    }

    public boolean neverSupportsScreen() {
        return (this.mCompatibilityFlags & 2) != 0;
    }

    public boolean alwaysSupportsScreen() {
        return (this.mCompatibilityFlags & 4) != 0;
    }

    public boolean needsCompatResources() {
        return (this.mCompatibilityFlags & 16) != 0;
    }

    @UnsupportedAppUsage
    public Translator getTranslator() {
        if (isScalingRequired()) {
            return new Translator(this);
        }
        return null;
    }

    public class Translator {
        @UnsupportedAppUsage
        public final float applicationInvertedScale;
        @UnsupportedAppUsage
        public final float applicationScale;
        private Rect mContentInsetsBuffer;
        private Region mTouchableAreaBuffer;
        private Rect mVisibleInsetsBuffer;

        Translator(float applicationScale2, float applicationInvertedScale2) {
            this.mContentInsetsBuffer = null;
            this.mVisibleInsetsBuffer = null;
            this.mTouchableAreaBuffer = null;
            this.applicationScale = applicationScale2;
            this.applicationInvertedScale = applicationInvertedScale2;
        }

        Translator(CompatibilityInfo this$02) {
            this(this$02.applicationScale, this$02.applicationInvertedScale);
        }

        @UnsupportedAppUsage
        public void translateRectInScreenToAppWinFrame(Rect rect) {
            rect.scale(this.applicationInvertedScale);
        }

        @UnsupportedAppUsage
        public void translateRegionInWindowToScreen(Region transparentRegion) {
            transparentRegion.scale(this.applicationScale);
        }

        @UnsupportedAppUsage
        public void translateCanvas(Canvas canvas) {
            if (this.applicationScale == 1.5f) {
                canvas.translate(0.0026143792f, 0.0026143792f);
            }
            float tinyOffset = this.applicationScale;
            canvas.scale(tinyOffset, tinyOffset);
        }

        @UnsupportedAppUsage
        public void translateEventInScreenToAppWindow(MotionEvent event) {
            event.scale(this.applicationInvertedScale);
        }

        @UnsupportedAppUsage
        public void translateWindowLayout(WindowManager.LayoutParams params) {
            params.scale(this.applicationScale);
        }

        @UnsupportedAppUsage
        public void translateRectInAppWindowToScreen(Rect rect) {
            rect.scale(this.applicationScale);
        }

        @UnsupportedAppUsage
        public void translateRectInScreenToAppWindow(Rect rect) {
            rect.scale(this.applicationInvertedScale);
        }

        public void translatePointInScreenToAppWindow(PointF point) {
            float scale = this.applicationInvertedScale;
            if (scale != 1.0f) {
                point.x *= scale;
                point.y *= scale;
            }
        }

        public void translateLayoutParamsInAppWindowToScreen(WindowManager.LayoutParams params) {
            params.scale(this.applicationScale);
        }

        @UnsupportedAppUsage
        public Rect getTranslatedContentInsets(Rect contentInsets) {
            if (this.mContentInsetsBuffer == null) {
                this.mContentInsetsBuffer = new Rect();
            }
            this.mContentInsetsBuffer.set(contentInsets);
            translateRectInAppWindowToScreen(this.mContentInsetsBuffer);
            return this.mContentInsetsBuffer;
        }

        public Rect getTranslatedVisibleInsets(Rect visibleInsets) {
            if (this.mVisibleInsetsBuffer == null) {
                this.mVisibleInsetsBuffer = new Rect();
            }
            this.mVisibleInsetsBuffer.set(visibleInsets);
            translateRectInAppWindowToScreen(this.mVisibleInsetsBuffer);
            return this.mVisibleInsetsBuffer;
        }

        public Region getTranslatedTouchableArea(Region touchableArea) {
            if (this.mTouchableAreaBuffer == null) {
                this.mTouchableAreaBuffer = new Region();
            }
            this.mTouchableAreaBuffer.set(touchableArea);
            this.mTouchableAreaBuffer.scale(this.applicationScale);
            return this.mTouchableAreaBuffer;
        }
    }

    public void applyToDisplayMetrics(DisplayMetrics inoutDm) {
        if (!supportsScreen()) {
            IHwApsImpl iHwApsImpl = this.mHwApsImpl;
            if (iHwApsImpl != null && !iHwApsImpl.checkAndApplyToDmByRatio(this.mApsResolutionRatio, inoutDm)) {
                computeCompatibleScaling(inoutDm, inoutDm);
            }
        } else {
            inoutDm.widthPixels = inoutDm.noncompatWidthPixels;
            inoutDm.heightPixels = inoutDm.noncompatHeightPixels;
        }
        if (isScalingRequired()) {
            float invertedRatio = this.applicationInvertedScale;
            inoutDm.density = inoutDm.noncompatDensity * invertedRatio;
            inoutDm.densityDpi = (int) ((((float) inoutDm.noncompatDensityDpi) * invertedRatio) + 0.5f);
            inoutDm.scaledDensity = inoutDm.noncompatScaledDensity * invertedRatio;
            inoutDm.xdpi = inoutDm.noncompatXdpi * invertedRatio;
            inoutDm.ydpi = inoutDm.noncompatYdpi * invertedRatio;
            inoutDm.widthPixels = (int) ((((float) inoutDm.widthPixels) * invertedRatio) + 0.5f);
            inoutDm.heightPixels = (int) ((((float) inoutDm.heightPixels) * invertedRatio) + 0.5f);
        }
    }

    public void applyToConfiguration(int displayDensity, Configuration inoutConfig) {
        IHwApsImpl iHwApsImpl;
        if (!supportsScreen() && (iHwApsImpl = this.mHwApsImpl) != null && !iHwApsImpl.isValidSdrRatio(this.mApsResolutionRatio)) {
            inoutConfig.screenLayout = (inoutConfig.screenLayout & -16) | 2;
            inoutConfig.screenWidthDp = inoutConfig.compatScreenWidthDp;
            inoutConfig.screenHeightDp = inoutConfig.compatScreenHeightDp;
            inoutConfig.smallestScreenWidthDp = inoutConfig.compatSmallestScreenWidthDp;
        }
        inoutConfig.densityDpi = displayDensity;
        IHwApsImpl iHwApsImpl2 = this.mHwApsImpl;
        if (iHwApsImpl2 != null) {
            iHwApsImpl2.applyToConfigurationByResolutionRatio(supportsScreen(), this.mApsResolutionRatio, inoutConfig);
        }
        if (isScalingRequired()) {
            inoutConfig.densityDpi = (int) ((((float) inoutConfig.densityDpi) * this.applicationInvertedScale) + 0.5f);
        }
    }

    @UnsupportedAppUsage
    public static float computeCompatibleScaling(DisplayMetrics dm, DisplayMetrics outDm) {
        int longSize;
        int shortSize;
        int newHeight;
        int newWidth;
        int width = dm.noncompatWidthPixels;
        int height = dm.noncompatHeightPixels;
        if (width < height) {
            shortSize = width;
            longSize = height;
        } else {
            shortSize = height;
            longSize = width;
        }
        int newShortSize = (int) ((dm.density * 320.0f) + 0.5f);
        float aspect = ((float) longSize) / ((float) shortSize);
        if (aspect > 1.7791667f) {
            aspect = 1.7791667f;
        }
        int newLongSize = (int) ((((float) newShortSize) * aspect) + 0.5f);
        if (width < height) {
            newWidth = newShortSize;
            newHeight = newLongSize;
        } else {
            newWidth = newLongSize;
            newHeight = newShortSize;
        }
        float sw = ((float) width) / ((float) newWidth);
        float sh = ((float) height) / ((float) newHeight);
        float scale = sw < sh ? sw : sh;
        if (scale < 1.0f) {
            scale = 1.0f;
        }
        if (outDm != null) {
            outDm.widthPixels = newWidth;
            outDm.heightPixels = newHeight;
        }
        return scale;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        try {
            CompatibilityInfo oc = (CompatibilityInfo) o;
            if (this.mCompatibilityFlags == oc.mCompatibilityFlags && this.applicationDensity == oc.applicationDensity && this.applicationScale == oc.applicationScale && this.applicationInvertedScale == oc.applicationInvertedScale && this.mApsResolutionRatio == oc.mApsResolutionRatio) {
                return true;
            }
            return false;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("{");
        sb.append(this.applicationDensity);
        sb.append("dpi");
        if (isScalingRequired()) {
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb.append(this.applicationScale);
            sb.append("x");
        }
        if (!supportsScreen()) {
            sb.append(" resizing");
        }
        if (neverSupportsScreen()) {
            sb.append(" never-compat");
        }
        if (alwaysSupportsScreen()) {
            sb.append(" always-compat");
        }
        if (Float.compare(this.mApsResolutionRatio, 1.0f) != 0) {
            sb.append(" apsResolutionRatio = " + this.mApsResolutionRatio);
        }
        sb.append("}");
        return sb.toString();
    }

    public int hashCode() {
        return (((((((((17 * 31) + this.mCompatibilityFlags) * 31) + this.applicationDensity) * 31) + Float.floatToIntBits(this.applicationScale)) * 31) + Float.floatToIntBits(this.applicationInvertedScale)) * 31) + Float.floatToIntBits(this.mApsResolutionRatio);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCompatibilityFlags);
        dest.writeInt(this.applicationDensity);
        dest.writeFloat(this.applicationScale);
        dest.writeFloat(this.applicationInvertedScale);
        dest.writeFloat(this.mApsResolutionRatio);
    }

    public static final CompatibilityInfo makeCompatibilityInfo(float ratio) {
        CompatibilityInfo ci = new CompatibilityInfo(8, DisplayMetrics.DENSITY_DEVICE, 1.0f, 1.0f);
        ci.mApsResolutionRatio = ratio;
        return ci;
    }

    private CompatibilityInfo(Parcel source) {
        this.mApsResolutionRatio = 1.0f;
        this.mHwApsImpl = HwFrameworkFactory.getHwApsImpl();
        this.mCompatibilityFlags = source.readInt();
        this.applicationDensity = source.readInt();
        this.applicationScale = source.readFloat();
        this.applicationInvertedScale = source.readFloat();
        this.mApsResolutionRatio = source.readFloat();
    }

    public float getSdrLowResolutionRatio() {
        return this.mApsResolutionRatio;
    }
}
