package android.hardware.camera2;

import android.graphics.Rect;
import android.hardware.Sensor;
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
import android.hardware.camera2.utils.TypeReference;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import android.util.SizeF;
import java.util.Collections;
import java.util.List;

public final class CameraCharacteristics extends CameraMetadata<Key<?>> {
    @PublicKey
    public static final Key<int[]> COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES = new Key("android.colorCorrection.availableAberrationModes", int[].class);
    @PublicKey
    public static final Key<int[]> CONTROL_AE_AVAILABLE_ANTIBANDING_MODES = new Key("android.control.aeAvailableAntibandingModes", int[].class);
    @PublicKey
    public static final Key<int[]> CONTROL_AE_AVAILABLE_MODES = new Key("android.control.aeAvailableModes", int[].class);
    @PublicKey
    public static final Key<Range<Integer>[]> CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES = new Key("android.control.aeAvailableTargetFpsRanges", new TypeReference<Range<Integer>[]>() {
    });
    @PublicKey
    public static final Key<Range<Integer>> CONTROL_AE_COMPENSATION_RANGE = new Key("android.control.aeCompensationRange", new TypeReference<Range<Integer>>() {
    });
    @PublicKey
    public static final Key<Rational> CONTROL_AE_COMPENSATION_STEP = new Key("android.control.aeCompensationStep", Rational.class);
    @PublicKey
    public static final Key<Boolean> CONTROL_AE_LOCK_AVAILABLE = new Key("android.control.aeLockAvailable", Boolean.TYPE);
    @PublicKey
    public static final Key<int[]> CONTROL_AF_AVAILABLE_MODES = new Key("android.control.afAvailableModes", int[].class);
    @PublicKey
    public static final Key<int[]> CONTROL_AVAILABLE_EFFECTS = new Key("android.control.availableEffects", int[].class);
    public static final Key<HighSpeedVideoConfiguration[]> CONTROL_AVAILABLE_HIGH_SPEED_VIDEO_CONFIGURATIONS = new Key("android.control.availableHighSpeedVideoConfigurations", HighSpeedVideoConfiguration[].class);
    @PublicKey
    public static final Key<int[]> CONTROL_AVAILABLE_MODES = new Key("android.control.availableModes", int[].class);
    @PublicKey
    public static final Key<int[]> CONTROL_AVAILABLE_SCENE_MODES = new Key("android.control.availableSceneModes", int[].class);
    @PublicKey
    public static final Key<int[]> CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES = new Key("android.control.availableVideoStabilizationModes", int[].class);
    @PublicKey
    public static final Key<int[]> CONTROL_AWB_AVAILABLE_MODES = new Key("android.control.awbAvailableModes", int[].class);
    @PublicKey
    public static final Key<Boolean> CONTROL_AWB_LOCK_AVAILABLE = new Key("android.control.awbLockAvailable", Boolean.TYPE);
    public static final Key<int[]> CONTROL_MAX_REGIONS = new Key("android.control.maxRegions", int[].class);
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> CONTROL_MAX_REGIONS_AE = new Key("android.control.maxRegionsAe", Integer.TYPE);
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> CONTROL_MAX_REGIONS_AF = new Key("android.control.maxRegionsAf", Integer.TYPE);
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> CONTROL_MAX_REGIONS_AWB = new Key("android.control.maxRegionsAwb", Integer.TYPE);
    @PublicKey
    public static final Key<Range<Integer>> CONTROL_POST_RAW_SENSITIVITY_BOOST_RANGE = new Key("android.control.postRawSensitivityBoostRange", new TypeReference<Range<Integer>>() {
    });
    public static final Key<StreamConfigurationDuration[]> DEPTH_AVAILABLE_DEPTH_MIN_FRAME_DURATIONS = new Key("android.depth.availableDepthMinFrameDurations", StreamConfigurationDuration[].class);
    public static final Key<StreamConfigurationDuration[]> DEPTH_AVAILABLE_DEPTH_STALL_DURATIONS = new Key("android.depth.availableDepthStallDurations", StreamConfigurationDuration[].class);
    public static final Key<StreamConfiguration[]> DEPTH_AVAILABLE_DEPTH_STREAM_CONFIGURATIONS = new Key("android.depth.availableDepthStreamConfigurations", StreamConfiguration[].class);
    @PublicKey
    public static final Key<Boolean> DEPTH_DEPTH_IS_EXCLUSIVE = new Key("android.depth.depthIsExclusive", Boolean.TYPE);
    @PublicKey
    public static final Key<int[]> EDGE_AVAILABLE_EDGE_MODES = new Key("android.edge.availableEdgeModes", int[].class);
    @PublicKey
    public static final Key<Boolean> FLASH_INFO_AVAILABLE = new Key("android.flash.info.available", Boolean.TYPE);
    @PublicKey
    public static final Key<int[]> HOT_PIXEL_AVAILABLE_HOT_PIXEL_MODES = new Key("android.hotPixel.availableHotPixelModes", int[].class);
    @PublicKey
    public static final Key<Integer> INFO_SUPPORTED_HARDWARE_LEVEL = new Key("android.info.supportedHardwareLevel", Integer.TYPE);
    @PublicKey
    public static final Key<Size[]> JPEG_AVAILABLE_THUMBNAIL_SIZES = new Key("android.jpeg.availableThumbnailSizes", Size[].class);
    public static final Key<int[]> LED_AVAILABLE_LEDS = new Key("android.led.availableLeds", int[].class);
    @PublicKey
    public static final Key<Integer> LENS_FACING = new Key("android.lens.facing", Integer.TYPE);
    @PublicKey
    public static final Key<float[]> LENS_INFO_AVAILABLE_APERTURES = new Key("android.lens.info.availableApertures", float[].class);
    @PublicKey
    public static final Key<float[]> LENS_INFO_AVAILABLE_FILTER_DENSITIES = new Key("android.lens.info.availableFilterDensities", float[].class);
    @PublicKey
    public static final Key<float[]> LENS_INFO_AVAILABLE_FOCAL_LENGTHS = new Key("android.lens.info.availableFocalLengths", float[].class);
    @PublicKey
    public static final Key<int[]> LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION = new Key("android.lens.info.availableOpticalStabilization", int[].class);
    @PublicKey
    public static final Key<Integer> LENS_INFO_FOCUS_DISTANCE_CALIBRATION = new Key("android.lens.info.focusDistanceCalibration", Integer.TYPE);
    @PublicKey
    public static final Key<Float> LENS_INFO_HYPERFOCAL_DISTANCE = new Key("android.lens.info.hyperfocalDistance", Float.TYPE);
    @PublicKey
    public static final Key<Float> LENS_INFO_MINIMUM_FOCUS_DISTANCE = new Key("android.lens.info.minimumFocusDistance", Float.TYPE);
    public static final Key<Size> LENS_INFO_SHADING_MAP_SIZE = new Key("android.lens.info.shadingMapSize", Size.class);
    @PublicKey
    public static final Key<float[]> LENS_INTRINSIC_CALIBRATION = new Key("android.lens.intrinsicCalibration", float[].class);
    @PublicKey
    public static final Key<float[]> LENS_POSE_ROTATION = new Key("android.lens.poseRotation", float[].class);
    @PublicKey
    public static final Key<float[]> LENS_POSE_TRANSLATION = new Key("android.lens.poseTranslation", float[].class);
    @PublicKey
    public static final Key<float[]> LENS_RADIAL_DISTORTION = new Key("android.lens.radialDistortion", float[].class);
    @PublicKey
    public static final Key<int[]> NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES = new Key("android.noiseReduction.availableNoiseReductionModes", int[].class);
    @Deprecated
    public static final Key<Byte> QUIRKS_USE_PARTIAL_RESULT = new Key("android.quirks.usePartialResult", Byte.TYPE);
    @PublicKey
    public static final Key<Integer> REPROCESS_MAX_CAPTURE_STALL = new Key("android.reprocess.maxCaptureStall", Integer.TYPE);
    @PublicKey
    public static final Key<int[]> REQUEST_AVAILABLE_CAPABILITIES = new Key("android.request.availableCapabilities", int[].class);
    public static final Key<int[]> REQUEST_AVAILABLE_CHARACTERISTICS_KEYS = new Key("android.request.availableCharacteristicsKeys", int[].class);
    public static final Key<int[]> REQUEST_AVAILABLE_REQUEST_KEYS = new Key("android.request.availableRequestKeys", int[].class);
    public static final Key<int[]> REQUEST_AVAILABLE_RESULT_KEYS = new Key("android.request.availableResultKeys", int[].class);
    @PublicKey
    public static final Key<Integer> REQUEST_MAX_NUM_INPUT_STREAMS = new Key("android.request.maxNumInputStreams", Integer.TYPE);
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> REQUEST_MAX_NUM_OUTPUT_PROC = new Key("android.request.maxNumOutputProc", Integer.TYPE);
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> REQUEST_MAX_NUM_OUTPUT_PROC_STALLING = new Key("android.request.maxNumOutputProcStalling", Integer.TYPE);
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> REQUEST_MAX_NUM_OUTPUT_RAW = new Key("android.request.maxNumOutputRaw", Integer.TYPE);
    public static final Key<int[]> REQUEST_MAX_NUM_OUTPUT_STREAMS = new Key("android.request.maxNumOutputStreams", int[].class);
    @PublicKey
    public static final Key<Integer> REQUEST_PARTIAL_RESULT_COUNT = new Key("android.request.partialResultCount", Integer.TYPE);
    @PublicKey
    public static final Key<Byte> REQUEST_PIPELINE_MAX_DEPTH = new Key("android.request.pipelineMaxDepth", Byte.TYPE);
    @Deprecated
    public static final Key<int[]> SCALER_AVAILABLE_FORMATS = new Key("android.scaler.availableFormats", int[].class);
    public static final Key<ReprocessFormatsMap> SCALER_AVAILABLE_INPUT_OUTPUT_FORMATS_MAP = new Key("android.scaler.availableInputOutputFormatsMap", ReprocessFormatsMap.class);
    @Deprecated
    public static final Key<long[]> SCALER_AVAILABLE_JPEG_MIN_DURATIONS = new Key("android.scaler.availableJpegMinDurations", long[].class);
    @Deprecated
    public static final Key<Size[]> SCALER_AVAILABLE_JPEG_SIZES = new Key("android.scaler.availableJpegSizes", Size[].class);
    @PublicKey
    public static final Key<Float> SCALER_AVAILABLE_MAX_DIGITAL_ZOOM = new Key("android.scaler.availableMaxDigitalZoom", Float.TYPE);
    public static final Key<StreamConfigurationDuration[]> SCALER_AVAILABLE_MIN_FRAME_DURATIONS = new Key("android.scaler.availableMinFrameDurations", StreamConfigurationDuration[].class);
    @Deprecated
    public static final Key<long[]> SCALER_AVAILABLE_PROCESSED_MIN_DURATIONS = new Key("android.scaler.availableProcessedMinDurations", long[].class);
    @Deprecated
    public static final Key<Size[]> SCALER_AVAILABLE_PROCESSED_SIZES = new Key("android.scaler.availableProcessedSizes", Size[].class);
    public static final Key<StreamConfigurationDuration[]> SCALER_AVAILABLE_STALL_DURATIONS = new Key("android.scaler.availableStallDurations", StreamConfigurationDuration[].class);
    public static final Key<StreamConfiguration[]> SCALER_AVAILABLE_STREAM_CONFIGURATIONS = new Key("android.scaler.availableStreamConfigurations", StreamConfiguration[].class);
    @PublicKey
    public static final Key<Integer> SCALER_CROPPING_TYPE = new Key("android.scaler.croppingType", Integer.TYPE);
    @PublicKey
    @SyntheticKey
    public static final Key<StreamConfigurationMap> SCALER_STREAM_CONFIGURATION_MAP = new Key("android.scaler.streamConfigurationMap", StreamConfigurationMap.class);
    @PublicKey
    public static final Key<int[]> SENSOR_AVAILABLE_TEST_PATTERN_MODES = new Key("android.sensor.availableTestPatternModes", int[].class);
    @PublicKey
    public static final Key<BlackLevelPattern> SENSOR_BLACK_LEVEL_PATTERN = new Key("android.sensor.blackLevelPattern", BlackLevelPattern.class);
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_CALIBRATION_TRANSFORM1 = new Key("android.sensor.calibrationTransform1", ColorSpaceTransform.class);
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_CALIBRATION_TRANSFORM2 = new Key("android.sensor.calibrationTransform2", ColorSpaceTransform.class);
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_COLOR_TRANSFORM1 = new Key("android.sensor.colorTransform1", ColorSpaceTransform.class);
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_COLOR_TRANSFORM2 = new Key("android.sensor.colorTransform2", ColorSpaceTransform.class);
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_FORWARD_MATRIX1 = new Key("android.sensor.forwardMatrix1", ColorSpaceTransform.class);
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_FORWARD_MATRIX2 = new Key("android.sensor.forwardMatrix2", ColorSpaceTransform.class);
    @PublicKey
    public static final Key<Rect> SENSOR_INFO_ACTIVE_ARRAY_SIZE = new Key("android.sensor.info.activeArraySize", Rect.class);
    @PublicKey
    public static final Key<Integer> SENSOR_INFO_COLOR_FILTER_ARRANGEMENT = new Key("android.sensor.info.colorFilterArrangement", Integer.TYPE);
    @PublicKey
    public static final Key<Range<Long>> SENSOR_INFO_EXPOSURE_TIME_RANGE = new Key("android.sensor.info.exposureTimeRange", new TypeReference<Range<Long>>() {
    });
    @PublicKey
    public static final Key<Boolean> SENSOR_INFO_LENS_SHADING_APPLIED = new Key("android.sensor.info.lensShadingApplied", Boolean.TYPE);
    @PublicKey
    public static final Key<Long> SENSOR_INFO_MAX_FRAME_DURATION = new Key("android.sensor.info.maxFrameDuration", Long.TYPE);
    @PublicKey
    public static final Key<SizeF> SENSOR_INFO_PHYSICAL_SIZE = new Key("android.sensor.info.physicalSize", SizeF.class);
    @PublicKey
    public static final Key<Size> SENSOR_INFO_PIXEL_ARRAY_SIZE = new Key("android.sensor.info.pixelArraySize", Size.class);
    @PublicKey
    public static final Key<Rect> SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE = new Key("android.sensor.info.preCorrectionActiveArraySize", Rect.class);
    @PublicKey
    public static final Key<Range<Integer>> SENSOR_INFO_SENSITIVITY_RANGE = new Key("android.sensor.info.sensitivityRange", new TypeReference<Range<Integer>>() {
    });
    @PublicKey
    public static final Key<Integer> SENSOR_INFO_TIMESTAMP_SOURCE = new Key("android.sensor.info.timestampSource", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> SENSOR_INFO_WHITE_LEVEL = new Key("android.sensor.info.whiteLevel", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> SENSOR_MAX_ANALOG_SENSITIVITY = new Key("android.sensor.maxAnalogSensitivity", Integer.TYPE);
    @PublicKey
    public static final Key<Rect[]> SENSOR_OPTICAL_BLACK_REGIONS = new Key("android.sensor.opticalBlackRegions", Rect[].class);
    @PublicKey
    public static final Key<Integer> SENSOR_ORIENTATION = new Key(Sensor.STRING_TYPE_ORIENTATION, Integer.TYPE);
    @PublicKey
    public static final Key<Integer> SENSOR_REFERENCE_ILLUMINANT1 = new Key("android.sensor.referenceIlluminant1", Integer.TYPE);
    @PublicKey
    public static final Key<Byte> SENSOR_REFERENCE_ILLUMINANT2 = new Key("android.sensor.referenceIlluminant2", Byte.TYPE);
    @PublicKey
    public static final Key<int[]> SHADING_AVAILABLE_MODES = new Key("android.shading.availableModes", int[].class);
    @PublicKey
    public static final Key<int[]> STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES = new Key("android.statistics.info.availableFaceDetectModes", int[].class);
    @PublicKey
    public static final Key<boolean[]> STATISTICS_INFO_AVAILABLE_HOT_PIXEL_MAP_MODES = new Key("android.statistics.info.availableHotPixelMapModes", boolean[].class);
    @PublicKey
    public static final Key<int[]> STATISTICS_INFO_AVAILABLE_LENS_SHADING_MAP_MODES = new Key("android.statistics.info.availableLensShadingMapModes", int[].class);
    @PublicKey
    public static final Key<Integer> STATISTICS_INFO_MAX_FACE_COUNT = new Key("android.statistics.info.maxFaceCount", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> SYNC_MAX_LATENCY = new Key("android.sync.maxLatency", Integer.TYPE);
    @PublicKey
    public static final Key<int[]> TONEMAP_AVAILABLE_TONE_MAP_MODES = new Key("android.tonemap.availableToneMapModes", int[].class);
    @PublicKey
    public static final Key<Integer> TONEMAP_MAX_CURVE_POINTS = new Key("android.tonemap.maxCurvePoints", Integer.TYPE);
    private List<android.hardware.camera2.CaptureRequest.Key<?>> mAvailableRequestKeys;
    private List<android.hardware.camera2.CaptureResult.Key<?>> mAvailableResultKeys;
    private List<Key<?>> mKeys;
    private final CameraMetadataNative mProperties;

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
            return String.format("CameraCharacteristics.Key(%s)", new Object[]{this.mKey.getName()});
        }

        public android.hardware.camera2.impl.CameraMetadataNative.Key<T> getNativeKey() {
            return this.mKey;
        }

        private Key(android.hardware.camera2.impl.CameraMetadataNative.Key<?> nativeKey) {
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
        return this.mProperties.get((Key) key);
    }

    protected <T> T getProtected(Key<?> key) {
        return this.mProperties.get((Key) key);
    }

    protected Class<Key<?>> getKeyClass() {
        return Key.class;
    }

    public List<Key<?>> getKeys() {
        if (this.mKeys != null) {
            return this.mKeys;
        }
        int[] filterTags = (int[]) get(REQUEST_AVAILABLE_CHARACTERISTICS_KEYS);
        if (filterTags == null) {
            throw new AssertionError("android.request.availableCharacteristicsKeys must be non-null in the characteristics");
        }
        this.mKeys = Collections.unmodifiableList(getKeys(getClass(), getKeyClass(), this, filterTags));
        return this.mKeys;
    }

    public List<android.hardware.camera2.CaptureRequest.Key<?>> getAvailableCaptureRequestKeys() {
        if (this.mAvailableRequestKeys == null) {
            Class<android.hardware.camera2.CaptureRequest.Key<?>> crKeyTyped = android.hardware.camera2.CaptureRequest.Key.class;
            int[] filterTags = (int[]) get(REQUEST_AVAILABLE_REQUEST_KEYS);
            if (filterTags == null) {
                throw new AssertionError("android.request.availableRequestKeys must be non-null in the characteristics");
            }
            this.mAvailableRequestKeys = getAvailableKeyList(CaptureRequest.class, crKeyTyped, filterTags);
        }
        return this.mAvailableRequestKeys;
    }

    public List<android.hardware.camera2.CaptureResult.Key<?>> getAvailableCaptureResultKeys() {
        if (this.mAvailableResultKeys == null) {
            Class<android.hardware.camera2.CaptureResult.Key<?>> crKeyTyped = android.hardware.camera2.CaptureResult.Key.class;
            int[] filterTags = (int[]) get(REQUEST_AVAILABLE_RESULT_KEYS);
            if (filterTags == null) {
                throw new AssertionError("android.request.availableResultKeys must be non-null in the characteristics");
            }
            this.mAvailableResultKeys = getAvailableKeyList(CaptureResult.class, crKeyTyped, filterTags);
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
}
