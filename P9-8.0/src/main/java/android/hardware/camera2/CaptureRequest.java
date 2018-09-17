package android.hardware.camera2;

import android.content.RestrictionsManager;
import android.graphics.Rect;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.impl.PublicKey;
import android.hardware.camera2.impl.SyntheticKey;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.RggbChannelVector;
import android.hardware.camera2.params.TonemapCurve;
import android.hardware.camera2.utils.HashCodeHelpers;
import android.hardware.camera2.utils.TypeReference;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class CaptureRequest extends CameraMetadata<Key<?>> implements Parcelable {
    @PublicKey
    public static final Key<Boolean> BLACK_LEVEL_LOCK = new Key("android.blackLevel.lock", Boolean.TYPE);
    @PublicKey
    public static final Key<Integer> COLOR_CORRECTION_ABERRATION_MODE = new Key("android.colorCorrection.aberrationMode", Integer.TYPE);
    @PublicKey
    public static final Key<RggbChannelVector> COLOR_CORRECTION_GAINS = new Key("android.colorCorrection.gains", RggbChannelVector.class);
    @PublicKey
    public static final Key<Integer> COLOR_CORRECTION_MODE = new Key("android.colorCorrection.mode", Integer.TYPE);
    @PublicKey
    public static final Key<ColorSpaceTransform> COLOR_CORRECTION_TRANSFORM = new Key("android.colorCorrection.transform", ColorSpaceTransform.class);
    @PublicKey
    public static final Key<Integer> CONTROL_AE_ANTIBANDING_MODE = new Key("android.control.aeAntibandingMode", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_AE_EXPOSURE_COMPENSATION = new Key("android.control.aeExposureCompensation", Integer.TYPE);
    @PublicKey
    public static final Key<Boolean> CONTROL_AE_LOCK = new Key("android.control.aeLock", Boolean.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_AE_MODE = new Key("android.control.aeMode", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_AE_PRECAPTURE_TRIGGER = new Key("android.control.aePrecaptureTrigger", Integer.TYPE);
    @PublicKey
    public static final Key<MeteringRectangle[]> CONTROL_AE_REGIONS = new Key("android.control.aeRegions", MeteringRectangle[].class);
    @PublicKey
    public static final Key<Range<Integer>> CONTROL_AE_TARGET_FPS_RANGE = new Key("android.control.aeTargetFpsRange", new TypeReference<Range<Integer>>() {
    });
    @PublicKey
    public static final Key<Integer> CONTROL_AF_MODE = new Key("android.control.afMode", Integer.TYPE);
    @PublicKey
    public static final Key<MeteringRectangle[]> CONTROL_AF_REGIONS = new Key("android.control.afRegions", MeteringRectangle[].class);
    @PublicKey
    public static final Key<Integer> CONTROL_AF_TRIGGER = new Key("android.control.afTrigger", Integer.TYPE);
    @PublicKey
    public static final Key<Boolean> CONTROL_AWB_LOCK = new Key("android.control.awbLock", Boolean.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_AWB_MODE = new Key("android.control.awbMode", Integer.TYPE);
    @PublicKey
    public static final Key<MeteringRectangle[]> CONTROL_AWB_REGIONS = new Key("android.control.awbRegions", MeteringRectangle[].class);
    @PublicKey
    public static final Key<Integer> CONTROL_CAPTURE_INTENT = new Key("android.control.captureIntent", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_EFFECT_MODE = new Key("android.control.effectMode", Integer.TYPE);
    @PublicKey
    public static final Key<Boolean> CONTROL_ENABLE_ZSL = new Key("android.control.enableZsl", Boolean.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_MODE = new Key("android.control.mode", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_POST_RAW_SENSITIVITY_BOOST = new Key("android.control.postRawSensitivityBoost", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_SCENE_MODE = new Key("android.control.sceneMode", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_VIDEO_STABILIZATION_MODE = new Key("android.control.videoStabilizationMode", Integer.TYPE);
    public static final Creator<CaptureRequest> CREATOR = new Creator<CaptureRequest>() {
        public CaptureRequest createFromParcel(Parcel in) {
            CaptureRequest request = new CaptureRequest();
            request.readFromParcel(in);
            return request;
        }

        public CaptureRequest[] newArray(int size) {
            return new CaptureRequest[size];
        }
    };
    @PublicKey
    public static final Key<Integer> EDGE_MODE = new Key("android.edge.mode", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> FLASH_MODE = new Key("android.flash.mode", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> HOT_PIXEL_MODE = new Key("android.hotPixel.mode", Integer.TYPE);
    public static final Key<double[]> JPEG_GPS_COORDINATES = new Key("android.jpeg.gpsCoordinates", double[].class);
    @PublicKey
    @SyntheticKey
    public static final Key<Location> JPEG_GPS_LOCATION = new Key("android.jpeg.gpsLocation", Location.class);
    public static final Key<String> JPEG_GPS_PROCESSING_METHOD = new Key("android.jpeg.gpsProcessingMethod", String.class);
    public static final Key<Long> JPEG_GPS_TIMESTAMP = new Key("android.jpeg.gpsTimestamp", Long.TYPE);
    @PublicKey
    public static final Key<Integer> JPEG_ORIENTATION = new Key("android.jpeg.orientation", Integer.TYPE);
    @PublicKey
    public static final Key<Byte> JPEG_QUALITY = new Key("android.jpeg.quality", Byte.TYPE);
    @PublicKey
    public static final Key<Byte> JPEG_THUMBNAIL_QUALITY = new Key("android.jpeg.thumbnailQuality", Byte.TYPE);
    @PublicKey
    public static final Key<Size> JPEG_THUMBNAIL_SIZE = new Key("android.jpeg.thumbnailSize", Size.class);
    public static final Key<Boolean> LED_TRANSMIT = new Key("android.led.transmit", Boolean.TYPE);
    @PublicKey
    public static final Key<Float> LENS_APERTURE = new Key("android.lens.aperture", Float.TYPE);
    @PublicKey
    public static final Key<Float> LENS_FILTER_DENSITY = new Key("android.lens.filterDensity", Float.TYPE);
    @PublicKey
    public static final Key<Float> LENS_FOCAL_LENGTH = new Key("android.lens.focalLength", Float.TYPE);
    @PublicKey
    public static final Key<Float> LENS_FOCUS_DISTANCE = new Key("android.lens.focusDistance", Float.TYPE);
    @PublicKey
    public static final Key<Integer> LENS_OPTICAL_STABILIZATION_MODE = new Key("android.lens.opticalStabilizationMode", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> NOISE_REDUCTION_MODE = new Key("android.noiseReduction.mode", Integer.TYPE);
    @PublicKey
    public static final Key<Float> REPROCESS_EFFECTIVE_EXPOSURE_FACTOR = new Key("android.reprocess.effectiveExposureFactor", Float.TYPE);
    public static final Key<Integer> REQUEST_ID = new Key(RestrictionsManager.REQUEST_KEY_ID, Integer.TYPE);
    @PublicKey
    public static final Key<Rect> SCALER_CROP_REGION = new Key("android.scaler.cropRegion", Rect.class);
    @PublicKey
    public static final Key<Long> SENSOR_EXPOSURE_TIME = new Key("android.sensor.exposureTime", Long.TYPE);
    @PublicKey
    public static final Key<Long> SENSOR_FRAME_DURATION = new Key("android.sensor.frameDuration", Long.TYPE);
    @PublicKey
    public static final Key<Integer> SENSOR_SENSITIVITY = new Key("android.sensor.sensitivity", Integer.TYPE);
    @PublicKey
    public static final Key<int[]> SENSOR_TEST_PATTERN_DATA = new Key("android.sensor.testPatternData", int[].class);
    @PublicKey
    public static final Key<Integer> SENSOR_TEST_PATTERN_MODE = new Key("android.sensor.testPatternMode", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> SHADING_MODE = new Key("android.shading.mode", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> STATISTICS_FACE_DETECT_MODE = new Key("android.statistics.faceDetectMode", Integer.TYPE);
    @PublicKey
    public static final Key<Boolean> STATISTICS_HOT_PIXEL_MAP_MODE = new Key("android.statistics.hotPixelMapMode", Boolean.TYPE);
    @PublicKey
    public static final Key<Integer> STATISTICS_LENS_SHADING_MAP_MODE = new Key("android.statistics.lensShadingMapMode", Integer.TYPE);
    @PublicKey
    @SyntheticKey
    public static final Key<TonemapCurve> TONEMAP_CURVE = new Key("android.tonemap.curve", TonemapCurve.class);
    public static final Key<float[]> TONEMAP_CURVE_BLUE = new Key("android.tonemap.curveBlue", float[].class);
    public static final Key<float[]> TONEMAP_CURVE_GREEN = new Key("android.tonemap.curveGreen", float[].class);
    public static final Key<float[]> TONEMAP_CURVE_RED = new Key("android.tonemap.curveRed", float[].class);
    @PublicKey
    public static final Key<Float> TONEMAP_GAMMA = new Key("android.tonemap.gamma", Float.TYPE);
    @PublicKey
    public static final Key<Integer> TONEMAP_MODE = new Key("android.tonemap.mode", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> TONEMAP_PRESET_CURVE = new Key("android.tonemap.presetCurve", Integer.TYPE);
    private boolean mIsPartOfCHSRequestList;
    private boolean mIsReprocess;
    private int mReprocessableSessionId;
    private final CameraMetadataNative mSettings;
    private final HashSet<Surface> mSurfaceSet;
    private Object mUserTag;

    public static final class Builder {
        private final CaptureRequest mRequest;

        public Builder(CameraMetadataNative template, boolean reprocess, int reprocessableSessionId) {
            this.mRequest = new CaptureRequest(template, reprocess, reprocessableSessionId, null);
        }

        public void addTarget(Surface outputTarget) {
            this.mRequest.mSurfaceSet.add(outputTarget);
        }

        public void removeTarget(Surface outputTarget) {
            this.mRequest.mSurfaceSet.remove(outputTarget);
        }

        public <T> void set(Key<T> key, T value) {
            this.mRequest.mSettings.set((Key) key, (Object) value);
        }

        public <T> T get(Key<T> key) {
            return this.mRequest.mSettings.get((Key) key);
        }

        public void setTag(Object tag) {
            this.mRequest.mUserTag = tag;
        }

        public void setPartOfCHSRequestList(boolean partOfCHSList) {
            this.mRequest.mIsPartOfCHSRequestList = partOfCHSList;
        }

        public CaptureRequest build() {
            return new CaptureRequest(this.mRequest, null, null);
        }

        public boolean isEmpty() {
            return this.mRequest.mSettings.isEmpty();
        }
    }

    public static final class Key<T> {
        private final android.hardware.camera2.impl.CameraMetadataNative.Key<T> mKey;

        public Key(String name, Class<T> type, long vendorId) {
            this.mKey = new android.hardware.camera2.impl.CameraMetadataNative.Key(name, type, vendorId);
        }

        public Key(String name, Class<T> type) {
            this.mKey = new android.hardware.camera2.impl.CameraMetadataNative.Key(name, (Class) type);
        }

        public Key(String name, TypeReference<T> typeReference) {
            this.mKey = new android.hardware.camera2.impl.CameraMetadataNative.Key(name, (TypeReference) typeReference);
        }

        public String getName() {
            return this.mKey.getName();
        }

        public long getVendorId() {
            return this.mKey.getVendorId();
        }

        public final int hashCode() {
            return this.mKey.hashCode();
        }

        public final boolean equals(Object o) {
            return o instanceof Key ? ((Key) o).mKey.equals(this.mKey) : false;
        }

        public String toString() {
            return String.format("CaptureRequest.Key(%s)", new Object[]{this.mKey.getName()});
        }

        public android.hardware.camera2.impl.CameraMetadataNative.Key<T> getNativeKey() {
            return this.mKey;
        }

        Key(android.hardware.camera2.impl.CameraMetadataNative.Key<?> nativeKey) {
            this.mKey = nativeKey;
        }
    }

    /* synthetic */ CaptureRequest(CaptureRequest -this0, CaptureRequest -this1) {
        this();
    }

    /* synthetic */ CaptureRequest(CameraMetadataNative settings, boolean isReprocess, int reprocessableSessionId, CaptureRequest -this3) {
        this(settings, isReprocess, reprocessableSessionId);
    }

    private CaptureRequest() {
        this.mIsPartOfCHSRequestList = false;
        this.mSettings = new CameraMetadataNative();
        setNativeInstance(this.mSettings);
        this.mSurfaceSet = new HashSet();
        this.mIsReprocess = false;
        this.mReprocessableSessionId = -1;
    }

    private CaptureRequest(CaptureRequest source) {
        this.mIsPartOfCHSRequestList = false;
        this.mSettings = new CameraMetadataNative(source.mSettings);
        setNativeInstance(this.mSettings);
        this.mSurfaceSet = (HashSet) source.mSurfaceSet.clone();
        this.mIsReprocess = source.mIsReprocess;
        this.mIsPartOfCHSRequestList = source.mIsPartOfCHSRequestList;
        this.mReprocessableSessionId = source.mReprocessableSessionId;
        this.mUserTag = source.mUserTag;
    }

    private CaptureRequest(CameraMetadataNative settings, boolean isReprocess, int reprocessableSessionId) {
        this.mIsPartOfCHSRequestList = false;
        this.mSettings = CameraMetadataNative.move(settings);
        setNativeInstance(this.mSettings);
        this.mSurfaceSet = new HashSet();
        this.mIsReprocess = isReprocess;
        if (!isReprocess) {
            this.mReprocessableSessionId = -1;
        } else if (reprocessableSessionId == -1) {
            throw new IllegalArgumentException("Create a reprocess capture request with an invalid session ID: " + reprocessableSessionId);
        } else {
            this.mReprocessableSessionId = reprocessableSessionId;
        }
    }

    public <T> T get(Key<T> key) {
        return this.mSettings.get((Key) key);
    }

    protected <T> T getProtected(Key<?> key) {
        return this.mSettings.get((Key) key);
    }

    protected Class<Key<?>> getKeyClass() {
        return Key.class;
    }

    public List<Key<?>> getKeys() {
        return super.getKeys();
    }

    public Object getTag() {
        return this.mUserTag;
    }

    public boolean isReprocess() {
        return this.mIsReprocess;
    }

    public boolean isPartOfCRequestList() {
        return this.mIsPartOfCHSRequestList;
    }

    public CameraMetadataNative getNativeCopy() {
        return new CameraMetadataNative(this.mSettings);
    }

    public int getReprocessableSessionId() {
        if (this.mIsReprocess && this.mReprocessableSessionId != -1) {
            return this.mReprocessableSessionId;
        }
        throw new IllegalStateException("Getting the reprocessable session ID for a non-reprocess capture request is illegal.");
    }

    public boolean equals(Object other) {
        if (other instanceof CaptureRequest) {
            return equals((CaptureRequest) other);
        }
        return false;
    }

    private boolean equals(CaptureRequest other) {
        return other != null && Objects.equals(this.mUserTag, other.mUserTag) && this.mSurfaceSet.equals(other.mSurfaceSet) && this.mSettings.equals(other.mSettings) && this.mIsReprocess == other.mIsReprocess && this.mReprocessableSessionId == other.mReprocessableSessionId;
    }

    public int hashCode() {
        return HashCodeHelpers.hashCodeGeneric(this.mSettings, this.mSurfaceSet, this.mUserTag);
    }

    private void readFromParcel(Parcel in) {
        boolean z = false;
        this.mSettings.readFromParcel(in);
        setNativeInstance(this.mSettings);
        this.mSurfaceSet.clear();
        Parcelable[] parcelableArray = in.readParcelableArray(Surface.class.getClassLoader());
        if (parcelableArray != null) {
            for (Parcelable p : parcelableArray) {
                this.mSurfaceSet.add((Surface) p);
            }
            if (in.readInt() != 0) {
                z = true;
            }
            this.mIsReprocess = z;
            this.mReprocessableSessionId = -1;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        this.mSettings.writeToParcel(dest, flags);
        dest.writeParcelableArray((Surface[]) this.mSurfaceSet.toArray(new Surface[this.mSurfaceSet.size()]), flags);
        dest.writeInt(this.mIsReprocess ? 1 : 0);
    }

    public boolean containsTarget(Surface surface) {
        return this.mSurfaceSet.contains(surface);
    }

    public Collection<Surface> getTargets() {
        return Collections.unmodifiableCollection(this.mSurfaceSet);
    }
}
