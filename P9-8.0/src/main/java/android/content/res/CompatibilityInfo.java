package android.content.res;

import android.aps.IApsManager;
import android.content.pm.ApplicationInfo;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.camera2.params.TonemapCurve;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.WindowManager.LayoutParams;
import com.android.server.LocalServices;

public class CompatibilityInfo implements Parcelable {
    private static final int ALWAYS_NEEDS_COMPAT = 2;
    public static final Creator<CompatibilityInfo> CREATOR = new Creator<CompatibilityInfo>() {
        public CompatibilityInfo createFromParcel(Parcel source) {
            return new CompatibilityInfo(source, null);
        }

        public CompatibilityInfo[] newArray(int size) {
            return new CompatibilityInfo[size];
        }
    };
    static final boolean DEBUG = false;
    public static final CompatibilityInfo DEFAULT_COMPATIBILITY_INFO = new CompatibilityInfo() {
    };
    public static final int DEFAULT_NORMAL_SHORT_DIMENSION = 320;
    public static final float MAXIMUM_ASPECT_RATIO = 1.7791667f;
    private static final int NEEDS_COMPAT_RES = 16;
    private static final int NEEDS_SCREEN_COMPAT = 8;
    private static final int NEVER_NEEDS_COMPAT = 4;
    private static final int SCALING_REQUIRED = 1;
    public final int applicationDensity;
    public final float applicationInvertedScale;
    public final float applicationScale;
    private float mApsResolutionRatio;
    private final int mCompatibilityFlags;

    public class Translator {
        public final float applicationInvertedScale;
        public final float applicationScale;
        private Rect mContentInsetsBuffer;
        private Region mTouchableAreaBuffer;
        private Rect mVisibleInsetsBuffer;

        Translator(float applicationScale, float applicationInvertedScale) {
            this.mContentInsetsBuffer = null;
            this.mVisibleInsetsBuffer = null;
            this.mTouchableAreaBuffer = null;
            this.applicationScale = applicationScale;
            this.applicationInvertedScale = applicationInvertedScale;
        }

        Translator(CompatibilityInfo this$0) {
            this(this$0.applicationScale, this$0.applicationInvertedScale);
        }

        public void translateRectInScreenToAppWinFrame(Rect rect) {
            rect.scale(this.applicationInvertedScale);
        }

        public void translateRegionInWindowToScreen(Region transparentRegion) {
            transparentRegion.scale(this.applicationScale);
        }

        public void translateCanvas(Canvas canvas) {
            if (this.applicationScale == 1.5f) {
                canvas.translate(0.0026143792f, 0.0026143792f);
            }
            canvas.scale(this.applicationScale, this.applicationScale);
        }

        public void translateEventInScreenToAppWindow(MotionEvent event) {
            event.scale(this.applicationInvertedScale);
        }

        public void translateWindowLayout(LayoutParams params) {
            params.scale(this.applicationScale);
        }

        public void translateRectInAppWindowToScreen(Rect rect) {
            rect.scale(this.applicationScale);
        }

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

        public void translateLayoutParamsInAppWindowToScreen(LayoutParams params) {
            params.scale(this.applicationScale);
        }

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

    /* synthetic */ CompatibilityInfo(Parcel source, CompatibilityInfo -this1) {
        this(source);
    }

    public CompatibilityInfo(ApplicationInfo appInfo, int screenLayout, int sw, boolean forceCompat) {
        this.mApsResolutionRatio = 1.0f;
        int compatFlags = 0;
        if (appInfo.targetSdkVersion < 26) {
            compatFlags = 16;
        }
        if (appInfo.requiresSmallestWidthDp == 0 && appInfo.compatibleWidthLimitDp == 0 && appInfo.largestWidthLimitDp == 0) {
            int sizeInfo = 0;
            boolean anyResizeable = false;
            if ((appInfo.flags & 2048) != 0) {
                sizeInfo = 8;
                anyResizeable = true;
                if (!forceCompat) {
                    sizeInfo = 8 | 34;
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
            if (forceCompat) {
                sizeInfo &= -3;
            }
            compatFlags |= 8;
            switch (screenLayout & 15) {
                case 3:
                    if ((sizeInfo & 8) != 0) {
                        compatFlags &= -9;
                    }
                    if ((appInfo.flags & 2048) != 0) {
                        compatFlags |= 4;
                        break;
                    }
                    break;
                case 4:
                    if ((sizeInfo & 32) != 0) {
                        compatFlags &= -9;
                    }
                    if ((appInfo.flags & 524288) != 0) {
                        compatFlags |= 4;
                        break;
                    }
                    break;
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
            int required;
            if (appInfo.requiresSmallestWidthDp != 0) {
                required = appInfo.requiresSmallestWidthDp;
            } else {
                required = appInfo.compatibleWidthLimitDp;
            }
            if (required == 0) {
                required = appInfo.largestWidthLimitDp;
            }
            int compat = appInfo.compatibleWidthLimitDp != 0 ? appInfo.compatibleWidthLimitDp : required;
            if (compat < required) {
                compat = required;
            }
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
        float resolutionRatio = -1.0f;
        try {
            IApsManager apsManager = (IApsManager) LocalServices.getService(IApsManager.class);
            if (apsManager != null) {
                resolutionRatio = apsManager.getResolution(appInfo.packageName);
            }
        } catch (Exception e) {
            Slog.e("SDR", "APS: SDR: Apsmanager.getResolution, Exception is thrown!", e);
        }
        if (isValidResolutionScaleRatio(resolutionRatio)) {
            compatFlags = 8;
            this.mApsResolutionRatio = resolutionRatio;
        }
        this.mCompatibilityFlags = compatFlags;
    }

    private CompatibilityInfo(int compFlags, int dens, float scale, float invertedScale) {
        this.mApsResolutionRatio = 1.0f;
        this.mCompatibilityFlags = compFlags;
        this.applicationDensity = dens;
        this.applicationScale = scale;
        this.applicationInvertedScale = invertedScale;
    }

    private CompatibilityInfo() {
        this(4, DisplayMetrics.DENSITY_DEVICE, 1.0f, 1.0f);
    }

    public boolean isScalingRequired() {
        return (this.mCompatibilityFlags & 1) != 0;
    }

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

    public Translator getTranslator() {
        return isScalingRequired() ? new Translator(this) : null;
    }

    private boolean isValidResolutionScaleRatio(float ratio) {
        return TonemapCurve.LEVEL_BLACK < ratio && ratio < 1.0f;
    }

    public void applyToDisplayMetrics(DisplayMetrics inoutDm) {
        if (supportsScreen()) {
            inoutDm.widthPixels = inoutDm.noncompatWidthPixels;
            inoutDm.heightPixels = inoutDm.noncompatHeightPixels;
        } else if (isValidResolutionScaleRatio(this.mApsResolutionRatio)) {
            if (Float.compare(inoutDm.density, inoutDm.noncompatDensity) == 0) {
                inoutDm.widthPixels = (int) ((((float) inoutDm.noncompatWidthPixels) * this.mApsResolutionRatio) + 0.5f);
                inoutDm.heightPixels = (int) ((((float) inoutDm.noncompatHeightPixels) * this.mApsResolutionRatio) + 0.5f);
            }
            inoutDm.density = inoutDm.noncompatDensity * this.mApsResolutionRatio;
            inoutDm.densityDpi = (int) ((inoutDm.density * 160.0f) + 0.5f);
            inoutDm.scaledDensity = inoutDm.noncompatScaledDensity * this.mApsResolutionRatio;
            inoutDm.xdpi = inoutDm.noncompatXdpi * this.mApsResolutionRatio;
            inoutDm.ydpi = inoutDm.noncompatYdpi * this.mApsResolutionRatio;
        } else {
            computeCompatibleScaling(inoutDm, inoutDm);
        }
        if (isScalingRequired()) {
            float f = this.applicationInvertedScale;
            f = this.applicationInvertedScale;
            inoutDm.density = inoutDm.noncompatDensity * f;
            inoutDm.densityDpi = (int) ((((float) inoutDm.noncompatDensityDpi) * f) + 0.5f);
            inoutDm.scaledDensity = inoutDm.noncompatScaledDensity * f;
            inoutDm.xdpi = inoutDm.noncompatXdpi * f;
            inoutDm.ydpi = inoutDm.noncompatYdpi * f;
            inoutDm.widthPixels = (int) ((((float) inoutDm.widthPixels) * f) + 0.5f);
            inoutDm.heightPixels = (int) ((((float) inoutDm.heightPixels) * f) + 0.5f);
        }
    }

    public void applyToConfiguration(int displayDensity, Configuration inoutConfig) {
        if (!(supportsScreen() || isValidResolutionScaleRatio(this.mApsResolutionRatio))) {
            inoutConfig.screenLayout = (inoutConfig.screenLayout & -16) | 2;
            inoutConfig.screenWidthDp = inoutConfig.compatScreenWidthDp;
            inoutConfig.screenHeightDp = inoutConfig.compatScreenHeightDp;
            inoutConfig.smallestScreenWidthDp = inoutConfig.compatSmallestScreenWidthDp;
        }
        inoutConfig.densityDpi = displayDensity;
        if (!supportsScreen() && isValidResolutionScaleRatio(this.mApsResolutionRatio) && inoutConfig.densityDpi == DisplayMetrics.DENSITY_DEVICE) {
            inoutConfig.densityDpi = (int) ((((float) DisplayMetrics.DENSITY_DEVICE) * this.mApsResolutionRatio) + 0.5f);
        }
        if (isScalingRequired()) {
            inoutConfig.densityDpi = (int) ((((float) inoutConfig.densityDpi) * this.applicationInvertedScale) + 0.5f);
        }
    }

    public static float computeCompatibleScaling(DisplayMetrics dm, DisplayMetrics outDm) {
        int shortSize;
        int longSize;
        int newWidth;
        int newHeight;
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
            return this.mCompatibilityFlags == oc.mCompatibilityFlags && this.applicationDensity == oc.applicationDensity && this.applicationScale == oc.applicationScale && this.applicationInvertedScale == oc.applicationInvertedScale && this.mApsResolutionRatio == oc.mApsResolutionRatio;
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
            sb.append(" always-compat");
        }
        if (alwaysSupportsScreen()) {
            sb.append(" never-compat");
        }
        if (Float.compare(this.mApsResolutionRatio, 1.0f) != 0) {
            sb.append(" apsResolutionRatio = ").append(this.mApsResolutionRatio);
        }
        sb.append("}");
        return sb.toString();
    }

    public int hashCode() {
        return ((((((((this.mCompatibilityFlags + 527) * 31) + this.applicationDensity) * 31) + Float.floatToIntBits(this.applicationScale)) * 31) + Float.floatToIntBits(this.applicationInvertedScale)) * 31) + Float.floatToIntBits(this.mApsResolutionRatio);
    }

    public int describeContents() {
        return 0;
    }

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
        this.mCompatibilityFlags = source.readInt();
        this.applicationDensity = source.readInt();
        this.applicationScale = source.readFloat();
        this.applicationInvertedScale = source.readFloat();
        this.mApsResolutionRatio = source.readFloat();
    }
}
