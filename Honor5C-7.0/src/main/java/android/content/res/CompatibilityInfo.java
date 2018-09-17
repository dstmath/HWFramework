package android.content.res;

import android.bluetooth.BluetoothClass.Device;
import android.content.pm.ApplicationInfo;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.camera2.legacy.LegacyCameraDevice;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.opengl.GLES20;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.os.StrictMode;
import android.provider.DocumentsContract.Root;
import android.security.keymaster.KeymasterDefs;
import android.speech.tts.TextToSpeech.Engine;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager.LayoutParams;

public class CompatibilityInfo implements Parcelable {
    private static final int ALWAYS_NEEDS_COMPAT = 2;
    public static final Creator<CompatibilityInfo> CREATOR = null;
    static final boolean DEBUG = false;
    public static final CompatibilityInfo DEFAULT_COMPATIBILITY_INFO = null;
    public static final int DEFAULT_NORMAL_SHORT_DIMENSION = 320;
    public static final float MAXIMUM_ASPECT_RATIO = 1.7791667f;
    private static final int NEEDS_SCREEN_COMPAT = 8;
    private static final int NEVER_NEEDS_COMPAT = 4;
    public static final int SCALE_FORCE = 1;
    public static final int SCALE_GL = 1;
    public static final int SCALE_NATIVE = 8;
    public static final int SCALE_PACKAGE = 4;
    public static final int SCALE_SURFACE = 2;
    private static final int SCALING_REQUIRED = 1;
    public int appScaleOptFlags;
    public float appScaleRatio;
    public final int applicationDensity;
    public final float applicationInvertedScale;
    public final float applicationScale;
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
            if (scale != Engine.DEFAULT_VOLUME) {
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.res.CompatibilityInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.res.CompatibilityInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.content.res.CompatibilityInfo.<clinit>():void");
    }

    public CompatibilityInfo(ApplicationInfo appInfo, int screenLayout, int sw, boolean forceCompat) {
        int compatFlags = 0;
        this.appScaleRatio = Engine.DEFAULT_VOLUME;
        if (forceCompat) {
            this.appScaleOptFlags = SCALING_REQUIRED;
        } else {
            this.appScaleOptFlags = 0;
        }
        if (appInfo.requiresSmallestWidthDp == 0 && appInfo.compatibleWidthLimitDp == 0 && appInfo.largestWidthLimitDp == 0) {
            int sizeInfo = 0;
            boolean anyResizeable = DEBUG;
            if ((appInfo.flags & Process.PROC_CHAR) != 0) {
                sizeInfo = SCALE_NATIVE;
                anyResizeable = true;
                if (!forceCompat) {
                    sizeInfo = SCALE_NATIVE | 34;
                }
            }
            if ((appInfo.flags & Root.FLAG_REMOVABLE_SD) != 0) {
                anyResizeable = true;
                if (!forceCompat) {
                    sizeInfo |= 34;
                }
            }
            if ((appInfo.flags & StrictMode.DETECT_VM_REGISTRATION_LEAKS) != 0) {
                anyResizeable = true;
                sizeInfo |= SCALE_SURFACE;
            }
            if (forceCompat) {
                sizeInfo &= -3;
            }
            compatFlags = SCALE_NATIVE;
            switch (screenLayout & 15) {
                case Engine.DEFAULT_STREAM /*3*/:
                    if ((sizeInfo & SCALE_NATIVE) != 0) {
                        compatFlags = SCALE_NATIVE & -9;
                    }
                    if ((appInfo.flags & Process.PROC_CHAR) != 0) {
                        compatFlags |= SCALE_PACKAGE;
                        break;
                    }
                    break;
                case SCALE_PACKAGE /*4*/:
                    if ((sizeInfo & 32) != 0) {
                        compatFlags = SCALE_NATIVE & -9;
                    }
                    if ((appInfo.flags & Root.FLAG_REMOVABLE_SD) != 0) {
                        compatFlags |= SCALE_PACKAGE;
                        break;
                    }
                    break;
            }
            if ((KeymasterDefs.KM_ENUM & screenLayout) == 0) {
                compatFlags = (compatFlags & -9) | SCALE_PACKAGE;
            } else if ((sizeInfo & SCALE_SURFACE) != 0) {
                compatFlags &= -9;
            } else if (!anyResizeable) {
                compatFlags |= SCALE_SURFACE;
            }
            if ((appInfo.flags & Process.PROC_OUT_LONG) != 0) {
                this.applicationDensity = DisplayMetrics.DENSITY_DEVICE;
                this.applicationScale = Engine.DEFAULT_VOLUME;
                this.applicationInvertedScale = Engine.DEFAULT_VOLUME;
            } else {
                this.applicationDensity = Const.CODE_G3_RANGE_START;
                this.applicationScale = ((float) DisplayMetrics.DENSITY_DEVICE) / 160.0f;
                this.applicationInvertedScale = Engine.DEFAULT_VOLUME / this.applicationScale;
                compatFlags |= SCALING_REQUIRED;
            }
        } else {
            int required;
            int compat;
            if (appInfo.requiresSmallestWidthDp != 0) {
                required = appInfo.requiresSmallestWidthDp;
            } else {
                required = appInfo.compatibleWidthLimitDp;
            }
            if (required == 0) {
                required = appInfo.largestWidthLimitDp;
            }
            if (appInfo.compatibleWidthLimitDp != 0) {
                compat = appInfo.compatibleWidthLimitDp;
            } else {
                compat = required;
            }
            if (compat < required) {
                compat = required;
            }
            int largest = appInfo.largestWidthLimitDp;
            if (required > DEFAULT_NORMAL_SHORT_DIMENSION) {
                compatFlags = SCALE_PACKAGE;
            } else if (largest != 0 && sw > largest) {
                compatFlags = 10;
            } else if (compat >= sw) {
                compatFlags = SCALE_PACKAGE;
            } else if (forceCompat) {
                compatFlags = SCALE_NATIVE;
            }
            this.applicationDensity = DisplayMetrics.DENSITY_DEVICE;
            this.applicationScale = Engine.DEFAULT_VOLUME;
            this.applicationInvertedScale = Engine.DEFAULT_VOLUME;
        }
        this.mCompatibilityFlags = compatFlags;
    }

    private CompatibilityInfo(int compFlags, int dens, float scale, float invertedScale) {
        this.mCompatibilityFlags = compFlags;
        this.applicationDensity = dens;
        this.applicationScale = scale;
        this.applicationInvertedScale = invertedScale;
    }

    private CompatibilityInfo(int compFlags, int dens, float scale, float invertedScale, int flags) {
        this.mCompatibilityFlags = compFlags;
        this.applicationDensity = dens;
        this.applicationScale = scale;
        this.applicationInvertedScale = invertedScale;
        this.appScaleOptFlags = flags;
    }

    private CompatibilityInfo() {
        this((int) SCALE_PACKAGE, DisplayMetrics.DENSITY_DEVICE, (float) Engine.DEFAULT_VOLUME, (float) Engine.DEFAULT_VOLUME);
    }

    public boolean isScalingRequired() {
        return (this.mCompatibilityFlags & SCALING_REQUIRED) != 0 ? true : DEBUG;
    }

    public boolean supportsScreen() {
        return (this.mCompatibilityFlags & SCALE_NATIVE) == 0 ? true : DEBUG;
    }

    public boolean neverSupportsScreen() {
        return (this.mCompatibilityFlags & SCALE_SURFACE) != 0 ? true : DEBUG;
    }

    public boolean alwaysSupportsScreen() {
        return (this.mCompatibilityFlags & SCALE_PACKAGE) != 0 ? true : DEBUG;
    }

    public Translator getTranslator() {
        return isScalingRequired() ? new Translator(this) : null;
    }

    public boolean realNeedCompat() {
        return true;
    }

    public void applyToDisplayMetrics(DisplayMetrics inoutDm) {
        float invertedRatio = Engine.DEFAULT_VOLUME;
        if (supportsScreen() || !realNeedCompat()) {
            inoutDm.widthPixels = inoutDm.noncompatWidthPixels;
            inoutDm.heightPixels = inoutDm.noncompatHeightPixels;
        } else if (this.appScaleOptFlags != SCALING_REQUIRED) {
            invertedRatio = computeCompatibleScaling(inoutDm, inoutDm);
        } else if (Float.compare(inoutDm.density, inoutDm.noncompatDensity) == 0) {
            invertedRatio = computeForceCompatibleScaling(inoutDm, inoutDm);
        } else if (Float.compare(inoutDm.density, 0.0f) != 0) {
            invertedRatio = inoutDm.noncompatDensity / inoutDm.density;
        }
        if (isScalingRequired()) {
            invertedRatio = this.applicationInvertedScale;
            inoutDm.density = inoutDm.noncompatDensity * invertedRatio;
            inoutDm.densityDpi = (int) ((((float) inoutDm.noncompatDensityDpi) * invertedRatio) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
            inoutDm.scaledDensity = inoutDm.noncompatScaledDensity * invertedRatio;
            inoutDm.xdpi = inoutDm.noncompatXdpi * invertedRatio;
            inoutDm.ydpi = inoutDm.noncompatYdpi * invertedRatio;
            inoutDm.widthPixels = (int) ((((float) inoutDm.widthPixels) * invertedRatio) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
            inoutDm.heightPixels = (int) ((((float) inoutDm.heightPixels) * invertedRatio) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
        } else if (invertedRatio != Engine.DEFAULT_VOLUME) {
            this.appScaleRatio = invertedRatio;
            invertedRatio = Engine.DEFAULT_VOLUME / invertedRatio;
            inoutDm.density = inoutDm.noncompatDensity * invertedRatio;
            inoutDm.densityDpi = (int) ((inoutDm.density * 160.0f) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
            inoutDm.scaledDensity = inoutDm.noncompatScaledDensity * invertedRatio;
            inoutDm.xdpi = inoutDm.noncompatXdpi * invertedRatio;
            inoutDm.ydpi = inoutDm.noncompatYdpi * invertedRatio;
        }
    }

    public void applyToConfiguration(int displayDensity, Configuration inoutConfig) {
        inoutConfig.densityDpi = displayDensity;
        if (isScalingRequired()) {
            inoutConfig.densityDpi = (int) ((((float) inoutConfig.densityDpi) * this.applicationInvertedScale) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
        }
    }

    public void applyToConfigurationExt(DisplayMetrics metrics, int displayDensity, Configuration inoutConfig) {
        if (!isScalingRequired()) {
            boolean noNeedToChange = (metrics == null || inoutConfig.densityDpi == metrics.noncompatDensityDpi) ? DEBUG : true;
            if (this.appScaleOptFlags != 0 && this.appScaleRatio != Engine.DEFAULT_VOLUME && !noNeedToChange) {
                inoutConfig.densityDpi = (int) ((((float) inoutConfig.densityDpi) * (Engine.DEFAULT_VOLUME / this.appScaleRatio)) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
            }
        }
    }

    public static float computeCompatibleScaling(DisplayMetrics dm, DisplayMetrics outDm) {
        if (dm.noncompatDensity != dm.density && dm.noncompatDensity != 0.0f && dm.density != 0.0f) {
            return Engine.DEFAULT_VOLUME;
        }
        int shortSize;
        int longSize;
        int width = dm.noncompatWidthPixels;
        int height = dm.noncompatHeightPixels;
        if (width < height) {
            shortSize = width;
            longSize = height;
        } else {
            shortSize = height;
            longSize = width;
        }
        return computeScale(320.0f, longSize, shortSize, dm, outDm);
    }

    public static float computeForceCompatibleScaling(DisplayMetrics dm, DisplayMetrics outDm) {
        if (dm.noncompatDensity != dm.density && dm.noncompatDensity != 0.0f && dm.density != 0.0f) {
            return Engine.DEFAULT_VOLUME;
        }
        int shortSize;
        int longSize;
        int width = dm.noncompatWidthPixels;
        int height = dm.noncompatHeightPixels;
        if (width < height) {
            shortSize = width;
            longSize = height;
        } else {
            shortSize = height;
            longSize = width;
        }
        float factor = 320.0f;
        if (longSize < Device.AUDIO_VIDEO_VIDEO_MONITOR) {
            factor = 320.0f;
        } else if (longSize <= 1200) {
            factor = 276.0f;
        } else if (longSize <= GLES20.GL_INVALID_ENUM) {
            factor = 288.0f;
        } else if (longSize <= LegacyCameraDevice.MAX_DIMEN_FOR_ROUNDING) {
            factor = 240.0f;
        } else if (longSize <= 2560) {
            factor = 270.0f;
        }
        return computeScale(factor, longSize, shortSize, dm, outDm);
    }

    public static float computeScale(float factor, int longSize, int shortSize, DisplayMetrics dm, DisplayMetrics outDm) {
        int newWidth;
        int newHeight;
        float scale;
        int width = dm.noncompatWidthPixels;
        int height = dm.noncompatHeightPixels;
        int newShortSize = (int) ((dm.density * factor) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
        float aspect = ((float) longSize) / ((float) shortSize);
        if (aspect > MAXIMUM_ASPECT_RATIO) {
            aspect = MAXIMUM_ASPECT_RATIO;
        }
        int newLongSize = (int) ((((float) newShortSize) * aspect) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
        if (width < height) {
            newWidth = newShortSize;
            newHeight = newLongSize;
        } else {
            newWidth = newLongSize;
            newHeight = newShortSize;
        }
        float sw = ((float) width) / ((float) newWidth);
        float sh = ((float) height) / ((float) newHeight);
        if (sw < sh) {
            scale = sw;
        } else {
            scale = sh;
        }
        if (scale < Engine.DEFAULT_VOLUME) {
            scale = Engine.DEFAULT_VOLUME;
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
            return (this.mCompatibilityFlags == oc.mCompatibilityFlags && this.applicationDensity == oc.applicationDensity && this.applicationScale == oc.applicationScale && this.applicationInvertedScale == oc.applicationInvertedScale && this.appScaleOptFlags == oc.appScaleOptFlags) ? true : DEBUG;
        } catch (ClassCastException e) {
            return DEBUG;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(KeymasterDefs.KM_ALGORITHM_HMAC);
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
        if (this.appScaleOptFlags != 0) {
            sb.append(" enabledAppScaleOpt");
        }
        sb.append("}");
        return sb.toString();
    }

    public int hashCode() {
        return ((((((((this.mCompatibilityFlags + 527) * 31) + this.applicationDensity) * 31) + Float.floatToIntBits(this.applicationScale)) * 31) + Float.floatToIntBits(this.applicationInvertedScale)) * 31) + this.appScaleOptFlags;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mCompatibilityFlags);
        dest.writeInt(this.applicationDensity);
        dest.writeInt(this.appScaleOptFlags);
        dest.writeFloat(this.applicationScale);
        dest.writeFloat(this.applicationInvertedScale);
    }

    public static final CompatibilityInfo makeNewCompatibilityInfo(int flags) {
        return new CompatibilityInfo(11, Const.CODE_G3_RANGE_START, 1.33125f, 0.75117373f, flags);
    }

    public static final CompatibilityInfo makeNewPackageCompatibilityInfo(int flags) {
        return new CompatibilityInfo(11, Const.CODE_G3_RANGE_START, ((float) DisplayMetrics.DENSITY_DEVICE) / 160.0f, 160.0f / ((float) DisplayMetrics.DENSITY_DEVICE), flags);
    }

    public static final CompatibilityInfo makeNoneCompatibilityInfo(int flags) {
        return new CompatibilityInfo(SCALE_PACKAGE, DisplayMetrics.DENSITY_DEVICE, Engine.DEFAULT_VOLUME, Engine.DEFAULT_VOLUME, flags);
    }

    public static final CompatibilityInfo makeCompatibilityInfo(int flags) {
        return new CompatibilityInfo(SCALE_NATIVE, DisplayMetrics.DENSITY_DEVICE, Engine.DEFAULT_VOLUME, Engine.DEFAULT_VOLUME, flags);
    }

    private CompatibilityInfo(Parcel source) {
        this.mCompatibilityFlags = source.readInt();
        this.applicationDensity = source.readInt();
        this.appScaleOptFlags = source.readInt();
        this.applicationScale = source.readFloat();
        this.applicationInvertedScale = source.readFloat();
    }
}
