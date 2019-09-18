package android.hardware.camera2;

import android.content.RestrictionsManager;
import android.graphics.Rect;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.impl.PublicKey;
import android.hardware.camera2.impl.SyntheticKey;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.RggbChannelVector;
import android.hardware.camera2.params.TonemapCurve;
import android.hardware.camera2.utils.HashCodeHelpers;
import android.hardware.camera2.utils.SurfaceUtils;
import android.hardware.camera2.utils.TypeReference;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArraySet;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseArray;
import android.view.Surface;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class CaptureRequest extends CameraMetadata<Key<?>> implements Parcelable {
    @PublicKey
    public static final Key<Boolean> BLACK_LEVEL_LOCK = new Key<>("android.blackLevel.lock", (Class<Boolean>) Boolean.TYPE);
    @PublicKey
    public static final Key<Integer> COLOR_CORRECTION_ABERRATION_MODE = new Key<>("android.colorCorrection.aberrationMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<RggbChannelVector> COLOR_CORRECTION_GAINS = new Key<>("android.colorCorrection.gains", (Class<RggbChannelVector>) RggbChannelVector.class);
    @PublicKey
    public static final Key<Integer> COLOR_CORRECTION_MODE = new Key<>("android.colorCorrection.mode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<ColorSpaceTransform> COLOR_CORRECTION_TRANSFORM = new Key<>("android.colorCorrection.transform", (Class<ColorSpaceTransform>) ColorSpaceTransform.class);
    @PublicKey
    public static final Key<Integer> CONTROL_AE_ANTIBANDING_MODE = new Key<>("android.control.aeAntibandingMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_AE_EXPOSURE_COMPENSATION = new Key<>("android.control.aeExposureCompensation", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Boolean> CONTROL_AE_LOCK = new Key<>("android.control.aeLock", (Class<Boolean>) Boolean.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_AE_MODE = new Key<>("android.control.aeMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_AE_PRECAPTURE_TRIGGER = new Key<>("android.control.aePrecaptureTrigger", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<MeteringRectangle[]> CONTROL_AE_REGIONS = new Key<>("android.control.aeRegions", (Class<MeteringRectangle[]>) MeteringRectangle[].class);
    @PublicKey
    public static final Key<Range<Integer>> CONTROL_AE_TARGET_FPS_RANGE = new Key<>("android.control.aeTargetFpsRange", (TypeReference<Range<Integer>>) new TypeReference<Range<Integer>>() {
    });
    @PublicKey
    public static final Key<Integer> CONTROL_AF_MODE = new Key<>("android.control.afMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<MeteringRectangle[]> CONTROL_AF_REGIONS = new Key<>("android.control.afRegions", (Class<MeteringRectangle[]>) MeteringRectangle[].class);
    @PublicKey
    public static final Key<Integer> CONTROL_AF_TRIGGER = new Key<>("android.control.afTrigger", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Boolean> CONTROL_AWB_LOCK = new Key<>("android.control.awbLock", (Class<Boolean>) Boolean.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_AWB_MODE = new Key<>("android.control.awbMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<MeteringRectangle[]> CONTROL_AWB_REGIONS = new Key<>("android.control.awbRegions", (Class<MeteringRectangle[]>) MeteringRectangle[].class);
    @PublicKey
    public static final Key<Integer> CONTROL_CAPTURE_INTENT = new Key<>("android.control.captureIntent", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_EFFECT_MODE = new Key<>("android.control.effectMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Boolean> CONTROL_ENABLE_ZSL = new Key<>("android.control.enableZsl", (Class<Boolean>) Boolean.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_MODE = new Key<>("android.control.mode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_POST_RAW_SENSITIVITY_BOOST = new Key<>("android.control.postRawSensitivityBoost", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_SCENE_MODE = new Key<>("android.control.sceneMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_VIDEO_STABILIZATION_MODE = new Key<>("android.control.videoStabilizationMode", (Class<Integer>) Integer.TYPE);
    public static final Parcelable.Creator<CaptureRequest> CREATOR = new Parcelable.Creator<CaptureRequest>() {
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
    public static final Key<Integer> DISTORTION_CORRECTION_MODE = new Key<>("android.distortionCorrection.mode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> EDGE_MODE = new Key<>("android.edge.mode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> FLASH_MODE = new Key<>("android.flash.mode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> HOT_PIXEL_MODE = new Key<>("android.hotPixel.mode", (Class<Integer>) Integer.TYPE);
    public static final Key<double[]> JPEG_GPS_COORDINATES = new Key<>("android.jpeg.gpsCoordinates", (Class<double[]>) double[].class);
    @PublicKey
    @SyntheticKey
    public static final Key<Location> JPEG_GPS_LOCATION = new Key<>("android.jpeg.gpsLocation", (Class<Location>) Location.class);
    public static final Key<String> JPEG_GPS_PROCESSING_METHOD = new Key<>("android.jpeg.gpsProcessingMethod", (Class<String>) String.class);
    public static final Key<Long> JPEG_GPS_TIMESTAMP = new Key<>("android.jpeg.gpsTimestamp", (Class<Long>) Long.TYPE);
    @PublicKey
    public static final Key<Integer> JPEG_ORIENTATION = new Key<>("android.jpeg.orientation", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Byte> JPEG_QUALITY = new Key<>("android.jpeg.quality", (Class<Byte>) Byte.TYPE);
    @PublicKey
    public static final Key<Byte> JPEG_THUMBNAIL_QUALITY = new Key<>("android.jpeg.thumbnailQuality", (Class<Byte>) Byte.TYPE);
    @PublicKey
    public static final Key<Size> JPEG_THUMBNAIL_SIZE = new Key<>("android.jpeg.thumbnailSize", (Class<Size>) Size.class);
    public static final Key<Boolean> LED_TRANSMIT = new Key<>("android.led.transmit", (Class<Boolean>) Boolean.TYPE);
    @PublicKey
    public static final Key<Float> LENS_APERTURE = new Key<>("android.lens.aperture", (Class<Float>) Float.TYPE);
    @PublicKey
    public static final Key<Float> LENS_FILTER_DENSITY = new Key<>("android.lens.filterDensity", (Class<Float>) Float.TYPE);
    @PublicKey
    public static final Key<Float> LENS_FOCAL_LENGTH = new Key<>("android.lens.focalLength", (Class<Float>) Float.TYPE);
    @PublicKey
    public static final Key<Float> LENS_FOCUS_DISTANCE = new Key<>("android.lens.focusDistance", (Class<Float>) Float.TYPE);
    @PublicKey
    public static final Key<Integer> LENS_OPTICAL_STABILIZATION_MODE = new Key<>("android.lens.opticalStabilizationMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> NOISE_REDUCTION_MODE = new Key<>("android.noiseReduction.mode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Float> REPROCESS_EFFECTIVE_EXPOSURE_FACTOR = new Key<>("android.reprocess.effectiveExposureFactor", (Class<Float>) Float.TYPE);
    public static final Key<Integer> REQUEST_ID = new Key<>(RestrictionsManager.REQUEST_KEY_ID, (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Rect> SCALER_CROP_REGION = new Key<>("android.scaler.cropRegion", (Class<Rect>) Rect.class);
    @PublicKey
    public static final Key<Long> SENSOR_EXPOSURE_TIME = new Key<>("android.sensor.exposureTime", (Class<Long>) Long.TYPE);
    @PublicKey
    public static final Key<Long> SENSOR_FRAME_DURATION = new Key<>("android.sensor.frameDuration", (Class<Long>) Long.TYPE);
    @PublicKey
    public static final Key<Integer> SENSOR_SENSITIVITY = new Key<>("android.sensor.sensitivity", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<int[]> SENSOR_TEST_PATTERN_DATA = new Key<>("android.sensor.testPatternData", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<Integer> SENSOR_TEST_PATTERN_MODE = new Key<>("android.sensor.testPatternMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> SHADING_MODE = new Key<>("android.shading.mode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> STATISTICS_FACE_DETECT_MODE = new Key<>("android.statistics.faceDetectMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Boolean> STATISTICS_HOT_PIXEL_MAP_MODE = new Key<>("android.statistics.hotPixelMapMode", (Class<Boolean>) Boolean.TYPE);
    @PublicKey
    public static final Key<Integer> STATISTICS_LENS_SHADING_MAP_MODE = new Key<>("android.statistics.lensShadingMapMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> STATISTICS_OIS_DATA_MODE = new Key<>("android.statistics.oisDataMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    @SyntheticKey
    public static final Key<TonemapCurve> TONEMAP_CURVE = new Key<>("android.tonemap.curve", (Class<TonemapCurve>) TonemapCurve.class);
    public static final Key<float[]> TONEMAP_CURVE_BLUE = new Key<>("android.tonemap.curveBlue", (Class<float[]>) float[].class);
    public static final Key<float[]> TONEMAP_CURVE_GREEN = new Key<>("android.tonemap.curveGreen", (Class<float[]>) float[].class);
    public static final Key<float[]> TONEMAP_CURVE_RED = new Key<>("android.tonemap.curveRed", (Class<float[]>) float[].class);
    @PublicKey
    public static final Key<Float> TONEMAP_GAMMA = new Key<>("android.tonemap.gamma", (Class<Float>) Float.TYPE);
    @PublicKey
    public static final Key<Integer> TONEMAP_MODE = new Key<>("android.tonemap.mode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> TONEMAP_PRESET_CURVE = new Key<>("android.tonemap.presetCurve", (Class<Integer>) Integer.TYPE);
    private static final ArraySet<Surface> mEmptySurfaceSet = new ArraySet<>();
    private final String TAG;
    /* access modifiers changed from: private */
    public boolean mIsPartOfCHSRequestList;
    private boolean mIsReprocess;
    private String mLogicalCameraId;
    /* access modifiers changed from: private */
    public CameraMetadataNative mLogicalCameraSettings;
    /* access modifiers changed from: private */
    public final HashMap<String, CameraMetadataNative> mPhysicalCameraSettings;
    private int mReprocessableSessionId;
    private int[] mStreamIdxArray;
    private boolean mSurfaceConverted;
    private int[] mSurfaceIdxArray;
    /* access modifiers changed from: private */
    public final ArraySet<Surface> mSurfaceSet;
    private final Object mSurfacesLock;
    /* access modifiers changed from: private */
    public Object mUserTag;

    public static final class Builder {
        private final CaptureRequest mRequest;

        public Builder(CameraMetadataNative template, boolean reprocess, int reprocessableSessionId, String logicalCameraId, Set<String> physicalCameraIdSet) {
            CaptureRequest captureRequest = new CaptureRequest(template, reprocess, reprocessableSessionId, logicalCameraId, physicalCameraIdSet);
            this.mRequest = captureRequest;
        }

        public void addTarget(Surface outputTarget) {
            this.mRequest.mSurfaceSet.add(outputTarget);
        }

        public void removeTarget(Surface outputTarget) {
            this.mRequest.mSurfaceSet.remove(outputTarget);
        }

        public <T> void set(Key<T> key, T value) {
            this.mRequest.mLogicalCameraSettings.set(key, value);
        }

        public <T> T get(Key<T> key) {
            return this.mRequest.mLogicalCameraSettings.get(key);
        }

        public <T> Builder setPhysicalCameraKey(Key<T> key, T value, String physicalCameraId) {
            if (this.mRequest.mPhysicalCameraSettings.containsKey(physicalCameraId)) {
                ((CameraMetadataNative) this.mRequest.mPhysicalCameraSettings.get(physicalCameraId)).set(key, value);
                return this;
            }
            throw new IllegalArgumentException("Physical camera id: " + physicalCameraId + " is not valid!");
        }

        public <T> T getPhysicalCameraKey(Key<T> key, String physicalCameraId) {
            if (this.mRequest.mPhysicalCameraSettings.containsKey(physicalCameraId)) {
                return ((CameraMetadataNative) this.mRequest.mPhysicalCameraSettings.get(physicalCameraId)).get(key);
            }
            throw new IllegalArgumentException("Physical camera id: " + physicalCameraId + " is not valid!");
        }

        public void setTag(Object tag) {
            Object unused = this.mRequest.mUserTag = tag;
        }

        public void setPartOfCHSRequestList(boolean partOfCHSList) {
            boolean unused = this.mRequest.mIsPartOfCHSRequestList = partOfCHSList;
        }

        public CaptureRequest build() {
            return new CaptureRequest();
        }

        public boolean isEmpty() {
            return this.mRequest.mLogicalCameraSettings.isEmpty();
        }
    }

    public static final class Key<T> {
        private final CameraMetadataNative.Key<T> mKey;

        public Key(String name, Class<T> type, long vendorId) {
            this.mKey = new CameraMetadataNative.Key<>(name, type, vendorId);
        }

        public Key(String name, Class<T> type) {
            this.mKey = new CameraMetadataNative.Key<>(name, type);
        }

        public Key(String name, TypeReference<T> typeReference) {
            this.mKey = new CameraMetadataNative.Key<>(name, typeReference);
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
            return (o instanceof Key) && ((Key) o).mKey.equals(this.mKey);
        }

        public String toString() {
            return String.format("CaptureRequest.Key(%s)", new Object[]{this.mKey.getName()});
        }

        public CameraMetadataNative.Key<T> getNativeKey() {
            return this.mKey;
        }

        Key(CameraMetadataNative.Key<?> nativeKey) {
            this.mKey = nativeKey;
        }
    }

    private CaptureRequest() {
        this.TAG = "CaptureRequest-JV";
        this.mSurfaceSet = new ArraySet<>();
        this.mSurfacesLock = new Object();
        this.mSurfaceConverted = false;
        this.mPhysicalCameraSettings = new HashMap<>();
        this.mIsPartOfCHSRequestList = false;
        this.mIsReprocess = false;
        this.mReprocessableSessionId = -1;
    }

    private CaptureRequest(CaptureRequest source) {
        this.TAG = "CaptureRequest-JV";
        this.mSurfaceSet = new ArraySet<>();
        this.mSurfacesLock = new Object();
        this.mSurfaceConverted = false;
        this.mPhysicalCameraSettings = new HashMap<>();
        this.mIsPartOfCHSRequestList = false;
        this.mLogicalCameraId = new String(source.mLogicalCameraId);
        for (Map.Entry<String, CameraMetadataNative> entry : source.mPhysicalCameraSettings.entrySet()) {
            this.mPhysicalCameraSettings.put(new String(entry.getKey()), new CameraMetadataNative(entry.getValue()));
        }
        this.mLogicalCameraSettings = this.mPhysicalCameraSettings.get(this.mLogicalCameraId);
        setNativeInstance(this.mLogicalCameraSettings);
        this.mSurfaceSet.addAll(source.mSurfaceSet);
        this.mIsReprocess = source.mIsReprocess;
        this.mIsPartOfCHSRequestList = source.mIsPartOfCHSRequestList;
        this.mReprocessableSessionId = source.mReprocessableSessionId;
        this.mUserTag = source.mUserTag;
    }

    private CaptureRequest(CameraMetadataNative settings, boolean isReprocess, int reprocessableSessionId, String logicalCameraId, Set<String> physicalCameraIdSet) {
        this.TAG = "CaptureRequest-JV";
        this.mSurfaceSet = new ArraySet<>();
        this.mSurfacesLock = new Object();
        this.mSurfaceConverted = false;
        this.mPhysicalCameraSettings = new HashMap<>();
        this.mIsPartOfCHSRequestList = false;
        if (physicalCameraIdSet == null || !isReprocess) {
            this.mLogicalCameraId = logicalCameraId;
            this.mLogicalCameraSettings = CameraMetadataNative.move(settings);
            this.mPhysicalCameraSettings.put(this.mLogicalCameraId, this.mLogicalCameraSettings);
            if (physicalCameraIdSet != null) {
                for (String physicalId : physicalCameraIdSet) {
                    this.mPhysicalCameraSettings.put(physicalId, new CameraMetadataNative(this.mLogicalCameraSettings));
                }
            }
            setNativeInstance(this.mLogicalCameraSettings);
            this.mIsReprocess = isReprocess;
            if (!isReprocess) {
                this.mReprocessableSessionId = -1;
            } else if (reprocessableSessionId != -1) {
                this.mReprocessableSessionId = reprocessableSessionId;
            } else {
                throw new IllegalArgumentException("Create a reprocess capture request with an invalid session ID: " + reprocessableSessionId);
            }
        } else {
            throw new IllegalArgumentException("Create a reprocess capture request with with more than one physical camera is not supported!");
        }
    }

    public <T> T get(Key<T> key) {
        return this.mLogicalCameraSettings.get(key);
    }

    /* access modifiers changed from: protected */
    public <T> T getProtected(Key<?> key) {
        return this.mLogicalCameraSettings.get(key);
    }

    /* access modifiers changed from: protected */
    public Class<Key<?>> getKeyClass() {
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
        return new CameraMetadataNative(this.mLogicalCameraSettings);
    }

    public int getReprocessableSessionId() {
        if (this.mIsReprocess && this.mReprocessableSessionId != -1) {
            return this.mReprocessableSessionId;
        }
        throw new IllegalStateException("Getting the reprocessable session ID for a non-reprocess capture request is illegal.");
    }

    public boolean equals(Object other) {
        return (other instanceof CaptureRequest) && equals((CaptureRequest) other);
    }

    private boolean equals(CaptureRequest other) {
        return other != null && Objects.equals(this.mUserTag, other.mUserTag) && this.mSurfaceSet.equals(other.mSurfaceSet) && this.mPhysicalCameraSettings.equals(other.mPhysicalCameraSettings) && this.mLogicalCameraId.equals(other.mLogicalCameraId) && this.mLogicalCameraSettings.equals(other.mLogicalCameraSettings) && this.mIsReprocess == other.mIsReprocess && this.mReprocessableSessionId == other.mReprocessableSessionId;
    }

    public int hashCode() {
        return HashCodeHelpers.hashCodeGeneric(this.mPhysicalCameraSettings, this.mSurfaceSet, this.mUserTag);
    }

    /* access modifiers changed from: private */
    public void readFromParcel(Parcel in) {
        int physicalCameraCount = in.readInt();
        if (physicalCameraCount > 0) {
            this.mLogicalCameraId = in.readString();
            this.mLogicalCameraSettings = new CameraMetadataNative();
            this.mLogicalCameraSettings.readFromParcel(in);
            setNativeInstance(this.mLogicalCameraSettings);
            this.mPhysicalCameraSettings.put(this.mLogicalCameraId, this.mLogicalCameraSettings);
            boolean z = true;
            for (int i = 1; i < physicalCameraCount; i++) {
                String physicalId = in.readString();
                CameraMetadataNative physicalCameraSettings = new CameraMetadataNative();
                physicalCameraSettings.readFromParcel(in);
                this.mPhysicalCameraSettings.put(physicalId, physicalCameraSettings);
            }
            if (in.readInt() == 0) {
                z = false;
            }
            this.mIsReprocess = z;
            this.mReprocessableSessionId = -1;
            synchronized (this.mSurfacesLock) {
                this.mSurfaceSet.clear();
                Parcelable[] parcelableArray = in.readParcelableArray(Surface.class.getClassLoader());
                if (parcelableArray != null) {
                    for (Parcelable p : parcelableArray) {
                        this.mSurfaceSet.add((Surface) p);
                    }
                }
                if (in.readInt() != 0) {
                    throw new RuntimeException("Reading cached CaptureRequest is not supported");
                }
            }
            return;
        }
        throw new RuntimeException("Physical camera count" + physicalCameraCount + " should always be positive");
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPhysicalCameraSettings.size());
        dest.writeString(this.mLogicalCameraId);
        this.mLogicalCameraSettings.writeToParcel(dest, flags);
        for (Map.Entry<String, CameraMetadataNative> entry : this.mPhysicalCameraSettings.entrySet()) {
            if (!entry.getKey().equals(this.mLogicalCameraId)) {
                dest.writeString(entry.getKey());
                entry.getValue().writeToParcel(dest, flags);
            }
        }
        dest.writeInt(this.mIsReprocess ? 1 : 0);
        synchronized (this.mSurfacesLock) {
            ArraySet<Surface> surfaces = this.mSurfaceConverted ? mEmptySurfaceSet : this.mSurfaceSet;
            dest.writeParcelableArray((Surface[]) surfaces.toArray(new Surface[surfaces.size()]), flags);
            int i = 0;
            if (this.mSurfaceConverted) {
                dest.writeInt(this.mStreamIdxArray.length);
                while (true) {
                    int i2 = i;
                    if (i2 >= this.mStreamIdxArray.length) {
                        break;
                    }
                    dest.writeInt(this.mStreamIdxArray[i2]);
                    dest.writeInt(this.mSurfaceIdxArray[i2]);
                    i = i2 + 1;
                }
            } else {
                dest.writeInt(0);
            }
        }
    }

    public boolean containsTarget(Surface surface) {
        return this.mSurfaceSet.contains(surface);
    }

    public Collection<Surface> getTargets() {
        return Collections.unmodifiableCollection(this.mSurfaceSet);
    }

    public String getLogicalCameraId() {
        return this.mLogicalCameraId;
    }

    public void convertSurfaceToStreamId(SparseArray<OutputConfiguration> configuredOutputs) {
        SparseArray<OutputConfiguration> sparseArray = configuredOutputs;
        synchronized (this.mSurfacesLock) {
            if (this.mSurfaceConverted) {
                Log.v("CaptureRequest-JV", "Cannot convert already converted surfaces!");
                return;
            }
            this.mStreamIdxArray = new int[this.mSurfaceSet.size()];
            this.mSurfaceIdxArray = new int[this.mSurfaceSet.size()];
            int i = 0;
            Iterator<Surface> it = this.mSurfaceSet.iterator();
            while (it.hasNext()) {
                Surface s = it.next();
                int streamId = 0;
                boolean streamFound = false;
                int i2 = i;
                int j = 0;
                while (true) {
                    if (j >= configuredOutputs.size()) {
                        break;
                    }
                    int streamId2 = sparseArray.keyAt(j);
                    int surfaceId = 0;
                    Iterator<Surface> it2 = sparseArray.valueAt(j).getSurfaces().iterator();
                    while (true) {
                        if (!it2.hasNext()) {
                            break;
                        } else if (s == it2.next()) {
                            streamFound = true;
                            this.mStreamIdxArray[i2] = streamId2;
                            this.mSurfaceIdxArray[i2] = surfaceId;
                            i2++;
                            break;
                        } else {
                            surfaceId++;
                        }
                    }
                    if (streamFound) {
                        break;
                    }
                    j++;
                }
                if (!streamFound) {
                    long reqSurfaceId = SurfaceUtils.getSurfaceId(s);
                    while (true) {
                        int j2 = streamId;
                        if (j2 >= configuredOutputs.size()) {
                            break;
                        }
                        int streamId3 = sparseArray.keyAt(j2);
                        int surfaceId2 = 0;
                        Iterator<Surface> it3 = sparseArray.valueAt(j2).getSurfaces().iterator();
                        while (true) {
                            if (!it3.hasNext()) {
                                break;
                            } else if (reqSurfaceId == SurfaceUtils.getSurfaceId(it3.next())) {
                                streamFound = true;
                                this.mStreamIdxArray[i2] = streamId3;
                                this.mSurfaceIdxArray[i2] = surfaceId2;
                                i2++;
                                break;
                            } else {
                                surfaceId2++;
                            }
                        }
                        if (streamFound) {
                            break;
                        }
                        streamId = j2 + 1;
                    }
                }
                i = i2;
                if (!streamFound) {
                    this.mStreamIdxArray = null;
                    this.mSurfaceIdxArray = null;
                    throw new IllegalArgumentException("CaptureRequest contains unconfigured Input/Output Surface!");
                }
            }
            this.mSurfaceConverted = true;
        }
    }

    public void recoverStreamIdToSurface() {
        synchronized (this.mSurfacesLock) {
            if (!this.mSurfaceConverted) {
                Log.v("CaptureRequest-JV", "Cannot convert already converted surfaces!");
                return;
            }
            this.mStreamIdxArray = null;
            this.mSurfaceIdxArray = null;
            this.mSurfaceConverted = false;
        }
    }
}
