package android.hardware.camera2;

import android.graphics.Rect;
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
    public static final Key<int[]> COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES = null;
    @PublicKey
    public static final Key<int[]> CONTROL_AE_AVAILABLE_ANTIBANDING_MODES = null;
    @PublicKey
    public static final Key<int[]> CONTROL_AE_AVAILABLE_MODES = null;
    @PublicKey
    public static final Key<Range<Integer>[]> CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES = null;
    @PublicKey
    public static final Key<Range<Integer>> CONTROL_AE_COMPENSATION_RANGE = null;
    @PublicKey
    public static final Key<Rational> CONTROL_AE_COMPENSATION_STEP = null;
    @PublicKey
    public static final Key<Boolean> CONTROL_AE_LOCK_AVAILABLE = null;
    @PublicKey
    public static final Key<int[]> CONTROL_AF_AVAILABLE_MODES = null;
    @PublicKey
    public static final Key<int[]> CONTROL_AVAILABLE_EFFECTS = null;
    public static final Key<HighSpeedVideoConfiguration[]> CONTROL_AVAILABLE_HIGH_SPEED_VIDEO_CONFIGURATIONS = null;
    @PublicKey
    public static final Key<int[]> CONTROL_AVAILABLE_MODES = null;
    @PublicKey
    public static final Key<int[]> CONTROL_AVAILABLE_SCENE_MODES = null;
    @PublicKey
    public static final Key<int[]> CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES = null;
    @PublicKey
    public static final Key<int[]> CONTROL_AWB_AVAILABLE_MODES = null;
    @PublicKey
    public static final Key<Boolean> CONTROL_AWB_LOCK_AVAILABLE = null;
    public static final Key<int[]> CONTROL_MAX_REGIONS = null;
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> CONTROL_MAX_REGIONS_AE = null;
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> CONTROL_MAX_REGIONS_AF = null;
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> CONTROL_MAX_REGIONS_AWB = null;
    @PublicKey
    public static final Key<Range<Integer>> CONTROL_POST_RAW_SENSITIVITY_BOOST_RANGE = null;
    public static final Key<StreamConfigurationDuration[]> DEPTH_AVAILABLE_DEPTH_MIN_FRAME_DURATIONS = null;
    public static final Key<StreamConfigurationDuration[]> DEPTH_AVAILABLE_DEPTH_STALL_DURATIONS = null;
    public static final Key<StreamConfiguration[]> DEPTH_AVAILABLE_DEPTH_STREAM_CONFIGURATIONS = null;
    @PublicKey
    public static final Key<Boolean> DEPTH_DEPTH_IS_EXCLUSIVE = null;
    @PublicKey
    public static final Key<int[]> EDGE_AVAILABLE_EDGE_MODES = null;
    @PublicKey
    public static final Key<Boolean> FLASH_INFO_AVAILABLE = null;
    @PublicKey
    public static final Key<int[]> HOT_PIXEL_AVAILABLE_HOT_PIXEL_MODES = null;
    @PublicKey
    public static final Key<Integer> INFO_SUPPORTED_HARDWARE_LEVEL = null;
    @PublicKey
    public static final Key<Size[]> JPEG_AVAILABLE_THUMBNAIL_SIZES = null;
    public static final Key<int[]> LED_AVAILABLE_LEDS = null;
    @PublicKey
    public static final Key<Integer> LENS_FACING = null;
    @PublicKey
    public static final Key<float[]> LENS_INFO_AVAILABLE_APERTURES = null;
    @PublicKey
    public static final Key<float[]> LENS_INFO_AVAILABLE_FILTER_DENSITIES = null;
    @PublicKey
    public static final Key<float[]> LENS_INFO_AVAILABLE_FOCAL_LENGTHS = null;
    @PublicKey
    public static final Key<int[]> LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION = null;
    @PublicKey
    public static final Key<Integer> LENS_INFO_FOCUS_DISTANCE_CALIBRATION = null;
    @PublicKey
    public static final Key<Float> LENS_INFO_HYPERFOCAL_DISTANCE = null;
    @PublicKey
    public static final Key<Float> LENS_INFO_MINIMUM_FOCUS_DISTANCE = null;
    public static final Key<Size> LENS_INFO_SHADING_MAP_SIZE = null;
    @PublicKey
    public static final Key<float[]> LENS_INTRINSIC_CALIBRATION = null;
    @PublicKey
    public static final Key<float[]> LENS_POSE_ROTATION = null;
    @PublicKey
    public static final Key<float[]> LENS_POSE_TRANSLATION = null;
    @PublicKey
    public static final Key<float[]> LENS_RADIAL_DISTORTION = null;
    @PublicKey
    public static final Key<int[]> NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES = null;
    @Deprecated
    public static final Key<Byte> QUIRKS_USE_PARTIAL_RESULT = null;
    @PublicKey
    public static final Key<Integer> REPROCESS_MAX_CAPTURE_STALL = null;
    @PublicKey
    public static final Key<int[]> REQUEST_AVAILABLE_CAPABILITIES = null;
    public static final Key<int[]> REQUEST_AVAILABLE_CHARACTERISTICS_KEYS = null;
    public static final Key<int[]> REQUEST_AVAILABLE_REQUEST_KEYS = null;
    public static final Key<int[]> REQUEST_AVAILABLE_RESULT_KEYS = null;
    @PublicKey
    public static final Key<Integer> REQUEST_MAX_NUM_INPUT_STREAMS = null;
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> REQUEST_MAX_NUM_OUTPUT_PROC = null;
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> REQUEST_MAX_NUM_OUTPUT_PROC_STALLING = null;
    @PublicKey
    @SyntheticKey
    public static final Key<Integer> REQUEST_MAX_NUM_OUTPUT_RAW = null;
    public static final Key<int[]> REQUEST_MAX_NUM_OUTPUT_STREAMS = null;
    @PublicKey
    public static final Key<Integer> REQUEST_PARTIAL_RESULT_COUNT = null;
    @PublicKey
    public static final Key<Byte> REQUEST_PIPELINE_MAX_DEPTH = null;
    @Deprecated
    public static final Key<int[]> SCALER_AVAILABLE_FORMATS = null;
    public static final Key<ReprocessFormatsMap> SCALER_AVAILABLE_INPUT_OUTPUT_FORMATS_MAP = null;
    @Deprecated
    public static final Key<long[]> SCALER_AVAILABLE_JPEG_MIN_DURATIONS = null;
    @Deprecated
    public static final Key<Size[]> SCALER_AVAILABLE_JPEG_SIZES = null;
    @PublicKey
    public static final Key<Float> SCALER_AVAILABLE_MAX_DIGITAL_ZOOM = null;
    public static final Key<StreamConfigurationDuration[]> SCALER_AVAILABLE_MIN_FRAME_DURATIONS = null;
    @Deprecated
    public static final Key<long[]> SCALER_AVAILABLE_PROCESSED_MIN_DURATIONS = null;
    @Deprecated
    public static final Key<Size[]> SCALER_AVAILABLE_PROCESSED_SIZES = null;
    public static final Key<StreamConfigurationDuration[]> SCALER_AVAILABLE_STALL_DURATIONS = null;
    public static final Key<StreamConfiguration[]> SCALER_AVAILABLE_STREAM_CONFIGURATIONS = null;
    @PublicKey
    public static final Key<Integer> SCALER_CROPPING_TYPE = null;
    @PublicKey
    @SyntheticKey
    public static final Key<StreamConfigurationMap> SCALER_STREAM_CONFIGURATION_MAP = null;
    @PublicKey
    public static final Key<int[]> SENSOR_AVAILABLE_TEST_PATTERN_MODES = null;
    @PublicKey
    public static final Key<BlackLevelPattern> SENSOR_BLACK_LEVEL_PATTERN = null;
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_CALIBRATION_TRANSFORM1 = null;
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_CALIBRATION_TRANSFORM2 = null;
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_COLOR_TRANSFORM1 = null;
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_COLOR_TRANSFORM2 = null;
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_FORWARD_MATRIX1 = null;
    @PublicKey
    public static final Key<ColorSpaceTransform> SENSOR_FORWARD_MATRIX2 = null;
    @PublicKey
    public static final Key<Rect> SENSOR_INFO_ACTIVE_ARRAY_SIZE = null;
    @PublicKey
    public static final Key<Integer> SENSOR_INFO_COLOR_FILTER_ARRANGEMENT = null;
    @PublicKey
    public static final Key<Range<Long>> SENSOR_INFO_EXPOSURE_TIME_RANGE = null;
    @PublicKey
    public static final Key<Boolean> SENSOR_INFO_LENS_SHADING_APPLIED = null;
    @PublicKey
    public static final Key<Long> SENSOR_INFO_MAX_FRAME_DURATION = null;
    @PublicKey
    public static final Key<SizeF> SENSOR_INFO_PHYSICAL_SIZE = null;
    @PublicKey
    public static final Key<Size> SENSOR_INFO_PIXEL_ARRAY_SIZE = null;
    @PublicKey
    public static final Key<Rect> SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE = null;
    @PublicKey
    public static final Key<Range<Integer>> SENSOR_INFO_SENSITIVITY_RANGE = null;
    @PublicKey
    public static final Key<Integer> SENSOR_INFO_TIMESTAMP_SOURCE = null;
    @PublicKey
    public static final Key<Integer> SENSOR_INFO_WHITE_LEVEL = null;
    @PublicKey
    public static final Key<Integer> SENSOR_MAX_ANALOG_SENSITIVITY = null;
    @PublicKey
    public static final Key<Rect[]> SENSOR_OPTICAL_BLACK_REGIONS = null;
    @PublicKey
    public static final Key<Integer> SENSOR_ORIENTATION = null;
    @PublicKey
    public static final Key<Integer> SENSOR_REFERENCE_ILLUMINANT1 = null;
    @PublicKey
    public static final Key<Byte> SENSOR_REFERENCE_ILLUMINANT2 = null;
    @PublicKey
    public static final Key<int[]> SHADING_AVAILABLE_MODES = null;
    @PublicKey
    public static final Key<int[]> STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES = null;
    @PublicKey
    public static final Key<boolean[]> STATISTICS_INFO_AVAILABLE_HOT_PIXEL_MAP_MODES = null;
    @PublicKey
    public static final Key<int[]> STATISTICS_INFO_AVAILABLE_LENS_SHADING_MAP_MODES = null;
    @PublicKey
    public static final Key<Integer> STATISTICS_INFO_MAX_FACE_COUNT = null;
    @PublicKey
    public static final Key<Integer> SYNC_MAX_LATENCY = null;
    @PublicKey
    public static final Key<int[]> TONEMAP_AVAILABLE_TONE_MAP_MODES = null;
    @PublicKey
    public static final Key<Integer> TONEMAP_MAX_CURVE_POINTS = null;
    private List<android.hardware.camera2.CaptureRequest.Key<?>> mAvailableRequestKeys;
    private List<android.hardware.camera2.CaptureResult.Key<?>> mAvailableResultKeys;
    private List<Key<?>> mKeys;
    private final CameraMetadataNative mProperties;

    public static final class Key<T> {
        private final android.hardware.camera2.impl.CameraMetadataNative.Key<T> mKey;

        public Key(String name, Class<T> type) {
            this.mKey = new android.hardware.camera2.impl.CameraMetadataNative.Key(name, (Class) type);
        }

        public Key(String name, TypeReference<T> typeReference) {
            this.mKey = new android.hardware.camera2.impl.CameraMetadataNative.Key(name, (TypeReference) typeReference);
        }

        public String getName() {
            return this.mKey.getName();
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.camera2.CameraCharacteristics.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.camera2.CameraCharacteristics.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.CameraCharacteristics.<clinit>():void");
    }

    public CameraCharacteristics(CameraMetadataNative properties) {
        this.mProperties = CameraMetadataNative.move(properties);
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
        this.mKeys = Collections.unmodifiableList(CameraMetadata.getKeysStatic(getClass(), getKeyClass(), this, filterTags));
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
            return Collections.unmodifiableList(CameraMetadata.getKeysStatic(metadataClass, keyClass, null, filterTags));
        } else {
            throw new AssertionError("metadataClass must be a subclass of CameraMetadata");
        }
    }
}
