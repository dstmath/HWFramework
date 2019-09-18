package android.hardware.camera2;

import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.impl.PublicKey;
import android.hardware.camera2.impl.SyntheticKey;
import android.hardware.camera2.params.BlackLevelPattern;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.hardware.camera2.params.HighSpeedVideoConfiguration;
import android.hardware.camera2.params.ReprocessFormatsMap;
import android.hardware.camera2.params.StreamConfiguration;
import android.hardware.camera2.params.StreamConfigurationDuration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.camera2.utils.ArrayUtils;
import android.hardware.camera2.utils.TypeReference;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import android.util.SizeF;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CameraCharacteristics extends CameraMetadata<Key<?>> {
    @PublicKey
    public static final Key<int[]> COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES = new Key<>("android.colorCorrection.availableAberrationModes", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<int[]> CONTROL_AE_AVAILABLE_ANTIBANDING_MODES = new Key<>("android.control.aeAvailableAntibandingModes", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<int[]> CONTROL_AE_AVAILABLE_MODES = new Key<>("android.control.aeAvailableModes", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<Range<Integer>[]> CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES = new Key<>("android.control.aeAvailableTargetFpsRanges", (TypeReference<Range<Integer>[]>) new TypeReference<Range<Integer>[]>() {
    });
    @PublicKey
    public static final Key<Range<Integer>> CONTROL_AE_COMPENSATION_RANGE = new Key<>("android.control.aeCompensationRange", (TypeReference<Range<Integer>>) new TypeReference<Range<Integer>>() {
    });
    @PublicKey
    public static final Key<Rational> CONTROL_AE_COMPENSATION_STEP = new Key<>("android.control.aeCompensationStep", (Class<Rational>) Rational.class);
    @PublicKey
    public static final Key<Boolean> CONTROL_AE_LOCK_AVAILABLE = new Key<>("android.control.aeLockAvailable", (Class<Boolean>) Boolean.TYPE);
    @PublicKey
    public static final Key<int[]> CONTROL_AF_AVAILABLE_MODES = new Key<>("android.control.afAvailableModes", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<int[]> CONTROL_AVAILABLE_EFFECTS = new Key<>("android.control.availableEffects", (Class<int[]>) int[].class);
    public static final Key<HighSpeedVideoConfiguration[]> CONTROL_AVAILABLE_HIGH_SPEED_VIDEO_CONFIGURATIONS = new Key<>("android.control.availableHighSpeedVideoConfigurations", (Class<HighSpeedVideoConfiguration[]>) HighSpeedVideoConfiguration[].class);
    @PublicKey
    public static final Key<int[]> CONTROL_AVAILABLE_MODES = new Key<>("android.control.availableModes", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<int[]> CONTROL_AVAILABLE_SCENE_MODES = new Key<>("android.control.availableSceneModes", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<int[]> CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES = new Key<>("android.control.availableVideoStabilizationModes", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<int[]> CONTROL_AWB_AVAILABLE_MODES = new Key<>("android.control.awbAvailableModes", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<Boolean> CONTROL_AWB_LOCK_AVAILABLE = new Key<>("android.control.awbLockAvailable", (Class<Boolean>) Boolean.TYPE);
    public static final Key<int[]> CONTROL_MAX_REGIONS = new Key<>("android.control.maxRegions", (Class<int[]>) int[].class);
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> CONTROL_MAX_REGIONS_AE = new Key<>("android.control.maxRegionsAe", (Class<Integer>) Integer.TYPE);
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> CONTROL_MAX_REGIONS_AF = new Key<>("android.control.maxRegionsAf", (Class<Integer>) Integer.TYPE);
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> CONTROL_MAX_REGIONS_AWB = new Key<>("android.control.maxRegionsAwb", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Range<Integer>> CONTROL_POST_RAW_SENSITIVITY_BOOST_RANGE = new Key<>("android.control.postRawSensitivityBoostRange", (TypeReference<Range<Integer>>) new TypeReference<Range<Integer>>() {
    });
    public static final Key<StreamConfigurationDuration[]> DEPTH_AVAILABLE_DEPTH_MIN_FRAME_DURATIONS = new Key<>("android.depth.availableDepthMinFrameDurations", (Class<StreamConfigurationDuration[]>) StreamConfigurationDuration[].class);
    public static final Key<StreamConfigurationDuration[]> DEPTH_AVAILABLE_DEPTH_STALL_DURATIONS = new Key<>("android.depth.availableDepthStallDurations", (Class<StreamConfigurationDuration[]>) StreamConfigurationDuration[].class);
    public static final Key<StreamConfiguration[]> DEPTH_AVAILABLE_DEPTH_STREAM_CONFIGURATIONS = new Key<>("android.depth.availableDepthStreamConfigurations", (Class<StreamConfiguration[]>) StreamConfiguration[].class);
    @PublicKey
    public static final Key<Boolean> DEPTH_DEPTH_IS_EXCLUSIVE = new Key<>("android.depth.depthIsExclusive", (Class<Boolean>) Boolean.TYPE);
    @PublicKey
    public static final Key<int[]> DISTORTION_CORRECTION_AVAILABLE_MODES = new Key<>("android.distortionCorrection.availableModes", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<int[]> EDGE_AVAILABLE_EDGE_MODES = new Key<>("android.edge.availableEdgeModes", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<Boolean> FLASH_INFO_AVAILABLE = new Key<>("android.flash.info.available", (Class<Boolean>) Boolean.TYPE);
    @PublicKey
    public static final Key<int[]> HOT_PIXEL_AVAILABLE_HOT_PIXEL_MODES = new Key<>("android.hotPixel.availableHotPixelModes", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<Integer> INFO_SUPPORTED_HARDWARE_LEVEL = new Key<>("android.info.supportedHardwareLevel", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<String> INFO_VERSION = new Key<>("android.info.version", (Class<String>) String.class);
    @PublicKey
    public static final Key<Size[]> JPEG_AVAILABLE_THUMBNAIL_SIZES = new Key<>("android.jpeg.availableThumbnailSizes", (Class<Size[]>) Size[].class);
    public static final Key<int[]> LED_AVAILABLE_LEDS = new Key<>("android.led.availableLeds", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<float[]> LENS_DISTORTION = new Key<>("android.lens.distortion", (Class<float[]>) float[].class);
    @PublicKey
    public static final Key<Integer> LENS_FACING = new Key<>("android.lens.facing", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<float[]> LENS_INFO_AVAILABLE_APERTURES = new Key<>("android.lens.info.availableApertures", (Class<float[]>) float[].class);
    @PublicKey
    public static final Key<float[]> LENS_INFO_AVAILABLE_FILTER_DENSITIES = new Key<>("android.lens.info.availableFilterDensities", (Class<float[]>) float[].class);
    @PublicKey
    public static final Key<float[]> LENS_INFO_AVAILABLE_FOCAL_LENGTHS = new Key<>("android.lens.info.availableFocalLengths", (Class<float[]>) float[].class);
    @PublicKey
    public static final Key<int[]> LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION = new Key<>("android.lens.info.availableOpticalStabilization", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<Integer> LENS_INFO_FOCUS_DISTANCE_CALIBRATION = new Key<>("android.lens.info.focusDistanceCalibration", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Float> LENS_INFO_HYPERFOCAL_DISTANCE = new Key<>("android.lens.info.hyperfocalDistance", (Class<Float>) Float.TYPE);
    @PublicKey
    public static final Key<Float> LENS_INFO_MINIMUM_FOCUS_DISTANCE = new Key<>("android.lens.info.minimumFocusDistance", (Class<Float>) Float.TYPE);
    public static final Key<Size> LENS_INFO_SHADING_MAP_SIZE = new Key<>("android.lens.info.shadingMapSize", (Class<Size>) Size.class);
    @PublicKey
    public static final Key<float[]> LENS_INTRINSIC_CALIBRATION = new Key<>("android.lens.intrinsicCalibration", (Class<float[]>) float[].class);
    @PublicKey
    public static final Key<Integer> LENS_POSE_REFERENCE = new Key<>("android.lens.poseReference", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<float[]> LENS_POSE_ROTATION = new Key<>("android.lens.poseRotation", (Class<float[]>) float[].class);
    @PublicKey
    public static final Key<float[]> LENS_POSE_TRANSLATION = new Key<>("android.lens.poseTranslation", (Class<float[]>) float[].class);
    @PublicKey
    @Deprecated
    public static final Key<float[]> LENS_RADIAL_DISTORTION = new Key<>("android.lens.radialDistortion", (Class<float[]>) float[].class);
    public static final Key<byte[]> LOGICAL_MULTI_CAMERA_PHYSICAL_IDS = new Key<>("android.logicalMultiCamera.physicalIds", (Class<byte[]>) byte[].class);
    @PublicKey
    public static final Key<Integer> LOGICAL_MULTI_CAMERA_SENSOR_SYNC_TYPE = new Key<>("android.logicalMultiCamera.sensorSyncType", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<int[]> NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES = new Key<>("android.noiseReduction.availableNoiseReductionModes", (Class<int[]>) int[].class);
    @Deprecated
    public static final Key<Byte> QUIRKS_USE_PARTIAL_RESULT = new Key<>("android.quirks.usePartialResult", (Class<Byte>) Byte.TYPE);
    @PublicKey
    public static final Key<Integer> REPROCESS_MAX_CAPTURE_STALL = new Key<>("android.reprocess.maxCaptureStall", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<int[]> REQUEST_AVAILABLE_CAPABILITIES = new Key<>("android.request.availableCapabilities", (Class<int[]>) int[].class);
    public static final Key<int[]> REQUEST_AVAILABLE_CHARACTERISTICS_KEYS = new Key<>("android.request.availableCharacteristicsKeys", (Class<int[]>) int[].class);
    public static final Key<int[]> REQUEST_AVAILABLE_PHYSICAL_CAMERA_REQUEST_KEYS = new Key<>("android.request.availablePhysicalCameraRequestKeys", (Class<int[]>) int[].class);
    public static final Key<int[]> REQUEST_AVAILABLE_REQUEST_KEYS = new Key<>("android.request.availableRequestKeys", (Class<int[]>) int[].class);
    public static final Key<int[]> REQUEST_AVAILABLE_RESULT_KEYS = new Key<>("android.request.availableResultKeys", (Class<int[]>) int[].class);
    public static final Key<int[]> REQUEST_AVAILABLE_SESSION_KEYS = new Key<>("android.request.availableSessionKeys", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<Integer> REQUEST_MAX_NUM_INPUT_STREAMS = new Key<>("android.request.maxNumInputStreams", (Class<Integer>) Integer.TYPE);
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> REQUEST_MAX_NUM_OUTPUT_PROC = new Key<>("android.request.maxNumOutputProc", (Class<Integer>) Integer.TYPE);
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> REQUEST_MAX_NUM_OUTPUT_PROC_STALLING = new Key<>("android.request.maxNumOutputProcStalling", (Class<Integer>) Integer.TYPE);
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> REQUEST_MAX_NUM_OUTPUT_RAW = new Key<>("android.request.maxNumOutputRaw", (Class<Integer>) Integer.TYPE);
    public static final Key<int[]> REQUEST_MAX_NUM_OUTPUT_STREAMS = new Key<>("android.request.maxNumOutputStreams", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<Integer> REQUEST_PARTIAL_RESULT_COUNT = new Key<>("android.request.partialResultCount", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Byte> REQUEST_PIPELINE_MAX_DEPTH = new Key<>("android.request.pipelineMaxDepth", (Class<Byte>) Byte.TYPE);
    @Deprecated
    public static final Key<int[]> SCALER_AVAILABLE_FORMATS = new Key<>("android.scaler.availableFormats", (Class<int[]>) int[].class);
    public static final Key<ReprocessFormatsMap> SCALER_AVAILABLE_INPUT_OUTPUT_FORMATS_MAP = new Key<>("android.scaler.availableInputOutputFormatsMap", (Class<ReprocessFormatsMap>) ReprocessFormatsMap.class);
    @Deprecated
    public static final Key<long[]> SCALER_AVAILABLE_JPEG_MIN_DURATIONS = new Key<>("android.scaler.availableJpegMinDurations", (Class<long[]>) long[].class);
    @Deprecated
    public static final Key<Size[]> SCALER_AVAILABLE_JPEG_SIZES = new Key<>("android.scaler.availableJpegSizes", (Class<Size[]>) Size[].class);
    @PublicKey
    public static final Key<Float> SCALER_AVAILABLE_MAX_DIGITAL_ZOOM = new Key<>("android.scaler.availableMaxDigitalZoom", (Class<Float>) Float.TYPE);
    public static final Key<StreamConfigurationDuration[]> SCALER_AVAILABLE_MIN_FRAME_DURATIONS = new Key<>("android.scaler.availableMinFrameDurations", (Class<StreamConfigurationDuration[]>) StreamConfigurationDuration[].class);
    @Deprecated
    public static final Key<long[]> SCALER_AVAILABLE_PROCESSED_MIN_DURATIONS = new Key<>("android.scaler.availableProcessedMinDurations", (Class<long[]>) long[].class);
    @Deprecated
    public static final Key<Size[]> SCALER_AVAILABLE_PROCESSED_SIZES = new Key<>("android.scaler.availableProcessedSizes", (Class<Size[]>) Size[].class);
    public static final Key<StreamConfigurationDuration[]> SCALER_AVAILABLE_STALL_DURATIONS = new Key<>("android.scaler.availableStallDurations", (Class<StreamConfigurationDuration[]>) StreamConfigurationDuration[].class);
    public static final Key<StreamConfiguration[]> SCALER_AVAILABLE_STREAM_CONFIGURATIONS = new Key<>("android.scaler.availableStreamConfigurations", (Class<StreamConfiguration[]>) StreamConfiguration[].class);
    @PublicKey
    public static final Key<Integer> SCALER_CROPPING_TYPE = new Key<>("android.scaler.croppingType", (Class<Integer>) Integer.TYPE);
    @PublicKey
    @SyntheticKey
    public static final Key<StreamConfigurationMap> SCALER_STREAM_CONFIGURATION_MAP = new Key<>("android.scaler.streamConfigurationMap", (Class<StreamConfigurationMap>) StreamConfigurationMap.class);
    @PublicKey
    public static final Key<int[]> SENSOR_AVAILABLE_TEST_PATTERN_MODES = new Key<>("android.sensor.availableTestPatternModes", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<BlackLevelPattern> SENSOR_BLACK_LEVEL_PATTERN = new Key<>("android.sensor.blackLevelPattern", (Class<BlackLevelPattern>) BlackLevelPattern.class);
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_CALIBRATION_TRANSFORM1 = new Key<>("android.sensor.calibrationTransform1", (Class<ColorSpaceTransform>) ColorSpaceTransform.class);
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_CALIBRATION_TRANSFORM2 = new Key<>("android.sensor.calibrationTransform2", (Class<ColorSpaceTransform>) ColorSpaceTransform.class);
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_COLOR_TRANSFORM1 = new Key<>("android.sensor.colorTransform1", (Class<ColorSpaceTransform>) ColorSpaceTransform.class);
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_COLOR_TRANSFORM2 = new Key<>("android.sensor.colorTransform2", (Class<ColorSpaceTransform>) ColorSpaceTransform.class);
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_FORWARD_MATRIX1 = new Key<>("android.sensor.forwardMatrix1", (Class<ColorSpaceTransform>) ColorSpaceTransform.class);
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_FORWARD_MATRIX2 = new Key<>("android.sensor.forwardMatrix2", (Class<ColorSpaceTransform>) ColorSpaceTransform.class);
    @PublicKey
    public static final Key<Rect> SENSOR_INFO_ACTIVE_ARRAY_SIZE = new Key<>("android.sensor.info.activeArraySize", (Class<Rect>) Rect.class);
    @PublicKey
    public static final Key<Integer> SENSOR_INFO_COLOR_FILTER_ARRANGEMENT = new Key<>("android.sensor.info.colorFilterArrangement", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Range<Long>> SENSOR_INFO_EXPOSURE_TIME_RANGE = new Key<>("android.sensor.info.exposureTimeRange", (TypeReference<Range<Long>>) new TypeReference<Range<Long>>() {
    });
    @PublicKey
    public static final Key<Boolean> SENSOR_INFO_LENS_SHADING_APPLIED = new Key<>("android.sensor.info.lensShadingApplied", (Class<Boolean>) Boolean.TYPE);
    @PublicKey
    public static final Key<Long> SENSOR_INFO_MAX_FRAME_DURATION = new Key<>("android.sensor.info.maxFrameDuration", (Class<Long>) Long.TYPE);
    @PublicKey
    public static final Key<SizeF> SENSOR_INFO_PHYSICAL_SIZE = new Key<>("android.sensor.info.physicalSize", (Class<SizeF>) SizeF.class);
    @PublicKey
    public static final Key<Size> SENSOR_INFO_PIXEL_ARRAY_SIZE = new Key<>("android.sensor.info.pixelArraySize", (Class<Size>) Size.class);
    @PublicKey
    public static final Key<Rect> SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE = new Key<>("android.sensor.info.preCorrectionActiveArraySize", (Class<Rect>) Rect.class);
    @PublicKey
    public static final Key<Range<Integer>> SENSOR_INFO_SENSITIVITY_RANGE = new Key<>("android.sensor.info.sensitivityRange", (TypeReference<Range<Integer>>) new TypeReference<Range<Integer>>() {
    });
    @PublicKey
    public static final Key<Integer> SENSOR_INFO_TIMESTAMP_SOURCE = new Key<>("android.sensor.info.timestampSource", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> SENSOR_INFO_WHITE_LEVEL = new Key<>("android.sensor.info.whiteLevel", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> SENSOR_MAX_ANALOG_SENSITIVITY = new Key<>("android.sensor.maxAnalogSensitivity", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Rect[]> SENSOR_OPTICAL_BLACK_REGIONS = new Key<>("android.sensor.opticalBlackRegions", (Class<Rect[]>) Rect[].class);
    @PublicKey
    public static final Key<Integer> SENSOR_ORIENTATION = new Key<>(Sensor.STRING_TYPE_ORIENTATION, (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> SENSOR_REFERENCE_ILLUMINANT1 = new Key<>("android.sensor.referenceIlluminant1", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Byte> SENSOR_REFERENCE_ILLUMINANT2 = new Key<>("android.sensor.referenceIlluminant2", (Class<Byte>) Byte.TYPE);
    @PublicKey
    public static final Key<int[]> SHADING_AVAILABLE_MODES = new Key<>("android.shading.availableModes", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<int[]> STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES = new Key<>("android.statistics.info.availableFaceDetectModes", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<boolean[]> STATISTICS_INFO_AVAILABLE_HOT_PIXEL_MAP_MODES = new Key<>("android.statistics.info.availableHotPixelMapModes", (Class<boolean[]>) boolean[].class);
    @PublicKey
    public static final Key<int[]> STATISTICS_INFO_AVAILABLE_LENS_SHADING_MAP_MODES = new Key<>("android.statistics.info.availableLensShadingMapModes", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<int[]> STATISTICS_INFO_AVAILABLE_OIS_DATA_MODES = new Key<>("android.statistics.info.availableOisDataModes", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<Integer> STATISTICS_INFO_MAX_FACE_COUNT = new Key<>("android.statistics.info.maxFaceCount", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> SYNC_MAX_LATENCY = new Key<>("android.sync.maxLatency", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<int[]> TONEMAP_AVAILABLE_TONE_MAP_MODES = new Key<>("android.tonemap.availableToneMapModes", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<Integer> TONEMAP_MAX_CURVE_POINTS = new Key<>("android.tonemap.maxCurvePoints", (Class<Integer>) Integer.TYPE);
    private List<CaptureRequest.Key<?>> mAvailablePhysicalRequestKeys;
    private List<CaptureRequest.Key<?>> mAvailableRequestKeys;
    private List<CaptureResult.Key<?>> mAvailableResultKeys;
    private List<CaptureRequest.Key<?>> mAvailableSessionKeys;
    private List<Key<?>> mKeys;
    private final CameraMetadataNative mProperties;

    public static final class Key<T> {
        private final CameraMetadataNative.Key<T> mKey;

        public Key(String name, Class<T> type, long vendorId) {
            this.mKey = new CameraMetadataNative.Key<>(name, type, vendorId);
        }

        public Key(String name, String fallbackName, Class<T> type) {
            this.mKey = new CameraMetadataNative.Key<>(name, fallbackName, type);
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
            return String.format("CameraCharacteristics.Key(%s)", new Object[]{this.mKey.getName()});
        }

        public CameraMetadataNative.Key<T> getNativeKey() {
            return this.mKey;
        }

        private Key(CameraMetadataNative.Key<?> nativeKey) {
            this.mKey = nativeKey;
        }
    }

    public CameraCharacteristics(CameraMetadataNative properties) {
        this.mProperties = CameraMetadataNative.move(properties);
        setNativeInstance(this.mProperties);
    }

    public CameraMetadataNative getNativeCopy() {
        return new CameraMetadataNative(this.mProperties);
    }

    public <T> T get(Key<T> key) {
        return this.mProperties.get(key);
    }

    /* access modifiers changed from: protected */
    public <T> T getProtected(Key<?> key) {
        return this.mProperties.get(key);
    }

    /* access modifiers changed from: protected */
    public Class<Key<?>> getKeyClass() {
        return Key.class;
    }

    public List<Key<?>> getKeys() {
        if (this.mKeys != null) {
            return this.mKeys;
        }
        int[] filterTags = (int[]) get(REQUEST_AVAILABLE_CHARACTERISTICS_KEYS);
        if (filterTags != null) {
            this.mKeys = Collections.unmodifiableList(getKeys(getClass(), getKeyClass(), this, filterTags));
            return this.mKeys;
        }
        throw new AssertionError("android.request.availableCharacteristicsKeys must be non-null in the characteristics");
    }

    public List<CaptureRequest.Key<?>> getAvailableSessionKeys() {
        if (this.mAvailableSessionKeys == null) {
            Class<CaptureRequest.Key<?>> crKeyTyped = CaptureRequest.Key.class;
            int[] filterTags = (int[]) get(REQUEST_AVAILABLE_SESSION_KEYS);
            if (filterTags == null) {
                return null;
            }
            this.mAvailableSessionKeys = getAvailableKeyList(CaptureRequest.class, crKeyTyped, filterTags);
        }
        return this.mAvailableSessionKeys;
    }

    public List<CaptureRequest.Key<?>> getAvailablePhysicalCameraRequestKeys() {
        if (this.mAvailablePhysicalRequestKeys == null) {
            Class<CaptureRequest.Key<?>> crKeyTyped = CaptureRequest.Key.class;
            int[] filterTags = (int[]) get(REQUEST_AVAILABLE_PHYSICAL_CAMERA_REQUEST_KEYS);
            if (filterTags == null) {
                return null;
            }
            this.mAvailablePhysicalRequestKeys = getAvailableKeyList(CaptureRequest.class, crKeyTyped, filterTags);
        }
        return this.mAvailablePhysicalRequestKeys;
    }

    public List<CaptureRequest.Key<?>> getAvailableCaptureRequestKeys() {
        if (this.mAvailableRequestKeys == null) {
            Class<CaptureRequest.Key<?>> crKeyTyped = CaptureRequest.Key.class;
            int[] filterTags = (int[]) get(REQUEST_AVAILABLE_REQUEST_KEYS);
            if (filterTags != null) {
                this.mAvailableRequestKeys = getAvailableKeyList(CaptureRequest.class, crKeyTyped, filterTags);
            } else {
                throw new AssertionError("android.request.availableRequestKeys must be non-null in the characteristics");
            }
        }
        return this.mAvailableRequestKeys;
    }

    public List<CaptureResult.Key<?>> getAvailableCaptureResultKeys() {
        if (this.mAvailableResultKeys == null) {
            Class<CaptureResult.Key<?>> crKeyTyped = CaptureResult.Key.class;
            int[] filterTags = (int[]) get(REQUEST_AVAILABLE_RESULT_KEYS);
            if (filterTags != null) {
                this.mAvailableResultKeys = getAvailableKeyList(CaptureResult.class, crKeyTyped, filterTags);
            } else {
                throw new AssertionError("android.request.availableResultKeys must be non-null in the characteristics");
            }
        }
        return this.mAvailableResultKeys;
    }

    private <TKey> List<TKey> getAvailableKeyList(Class<?> metadataClass, Class<TKey> keyClass, int[] filterTags) {
        if (metadataClass.equals(CameraMetadata.class)) {
            throw new AssertionError("metadataClass must be a strict subclass of CameraMetadata");
        } else if (CameraMetadata.class.isAssignableFrom(metadataClass)) {
            return Collections.unmodifiableList(getKeys(metadataClass, keyClass, null, filterTags));
        } else {
            throw new AssertionError("metadataClass must be a subclass of CameraMetadata");
        }
    }

    public Set<String> getPhysicalCameraIds() {
        int[] availableCapabilities = (int[]) get(REQUEST_AVAILABLE_CAPABILITIES);
        if (availableCapabilities == null) {
            throw new AssertionError("android.request.availableCapabilities must be non-null in the characteristics");
        } else if (!ArrayUtils.contains(availableCapabilities, 11)) {
            return Collections.emptySet();
        } else {
            try {
                return Collections.unmodifiableSet(new HashSet(Arrays.asList(new String((byte[]) get(LOGICAL_MULTI_CAMERA_PHYSICAL_IDS), "UTF-8").split("\u0000"))));
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("android.logicalCam.physicalIds must be UTF-8 string");
            }
        }
    }
}
