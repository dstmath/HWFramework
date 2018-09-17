package android.hardware.camera2;

import android.content.RestrictionsManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.impl.CaptureResultExtras;
import android.hardware.camera2.impl.PublicKey;
import android.hardware.camera2.impl.SyntheticKey;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.LensShadingMap;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.RggbChannelVector;
import android.hardware.camera2.params.TonemapCurve;
import android.hardware.camera2.utils.TypeReference;
import android.location.Location;
import android.util.Pair;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import java.util.List;

public class CaptureResult extends CameraMetadata<Key<?>> {
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
    public static final Key<Integer> CONTROL_AE_STATE = new Key("android.control.aeState", Integer.TYPE);
    @PublicKey
    public static final Key<Range<Integer>> CONTROL_AE_TARGET_FPS_RANGE = new Key("android.control.aeTargetFpsRange", new TypeReference<Range<Integer>>() {
    });
    @PublicKey
    public static final Key<Integer> CONTROL_AF_MODE = new Key("android.control.afMode", Integer.TYPE);
    @PublicKey
    public static final Key<MeteringRectangle[]> CONTROL_AF_REGIONS = new Key("android.control.afRegions", MeteringRectangle[].class);
    @PublicKey
    public static final Key<Integer> CONTROL_AF_STATE = new Key("android.control.afState", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_AF_TRIGGER = new Key("android.control.afTrigger", Integer.TYPE);
    @PublicKey
    public static final Key<Boolean> CONTROL_AWB_LOCK = new Key("android.control.awbLock", Boolean.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_AWB_MODE = new Key("android.control.awbMode", Integer.TYPE);
    @PublicKey
    public static final Key<MeteringRectangle[]> CONTROL_AWB_REGIONS = new Key("android.control.awbRegions", MeteringRectangle[].class);
    @PublicKey
    public static final Key<Integer> CONTROL_AWB_STATE = new Key("android.control.awbState", Integer.TYPE);
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
    @PublicKey
    public static final Key<Integer> EDGE_MODE = new Key("android.edge.mode", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> FLASH_MODE = new Key("android.flash.mode", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> FLASH_STATE = new Key("android.flash.state", Integer.TYPE);
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
    public static final Key<Pair<Float, Float>> LENS_FOCUS_RANGE = new Key("android.lens.focusRange", new TypeReference<Pair<Float, Float>>() {
    });
    @PublicKey
    public static final Key<float[]> LENS_INTRINSIC_CALIBRATION = new Key("android.lens.intrinsicCalibration", float[].class);
    @PublicKey
    public static final Key<Integer> LENS_OPTICAL_STABILIZATION_MODE = new Key("android.lens.opticalStabilizationMode", Integer.TYPE);
    @PublicKey
    public static final Key<float[]> LENS_POSE_ROTATION = new Key("android.lens.poseRotation", float[].class);
    @PublicKey
    public static final Key<float[]> LENS_POSE_TRANSLATION = new Key("android.lens.poseTranslation", float[].class);
    @PublicKey
    public static final Key<float[]> LENS_RADIAL_DISTORTION = new Key("android.lens.radialDistortion", float[].class);
    @PublicKey
    public static final Key<Integer> LENS_STATE = new Key("android.lens.state", Integer.TYPE);
    @PublicKey
    public static final Key<Integer> NOISE_REDUCTION_MODE = new Key("android.noiseReduction.mode", Integer.TYPE);
    @Deprecated
    public static final Key<Boolean> QUIRKS_PARTIAL_RESULT = new Key("android.quirks.partialResult", Boolean.TYPE);
    @PublicKey
    public static final Key<Float> REPROCESS_EFFECTIVE_EXPOSURE_FACTOR = new Key("android.reprocess.effectiveExposureFactor", Float.TYPE);
    @Deprecated
    public static final Key<Integer> REQUEST_FRAME_COUNT = new Key("android.request.frameCount", Integer.TYPE);
    public static final Key<Integer> REQUEST_ID = new Key(RestrictionsManager.REQUEST_KEY_ID, Integer.TYPE);
    @PublicKey
    public static final Key<Byte> REQUEST_PIPELINE_DEPTH = new Key("android.request.pipelineDepth", Byte.TYPE);
    @PublicKey
    public static final Key<Rect> SCALER_CROP_REGION = new Key("android.scaler.cropRegion", Rect.class);
    @PublicKey
    public static final Key<float[]> SENSOR_DYNAMIC_BLACK_LEVEL = new Key("android.sensor.dynamicBlackLevel", float[].class);
    @PublicKey
    public static final Key<Integer> SENSOR_DYNAMIC_WHITE_LEVEL = new Key("android.sensor.dynamicWhiteLevel", Integer.TYPE);
    @PublicKey
    public static final Key<Long> SENSOR_EXPOSURE_TIME = new Key("android.sensor.exposureTime", Long.TYPE);
    @PublicKey
    public static final Key<Long> SENSOR_FRAME_DURATION = new Key("android.sensor.frameDuration", Long.TYPE);
    @PublicKey
    public static final Key<Float> SENSOR_GREEN_SPLIT = new Key("android.sensor.greenSplit", Float.TYPE);
    @PublicKey
    public static final Key<Rational[]> SENSOR_NEUTRAL_COLOR_POINT = new Key("android.sensor.neutralColorPoint", Rational[].class);
    @PublicKey
    public static final Key<Pair<Double, Double>[]> SENSOR_NOISE_PROFILE = new Key("android.sensor.noiseProfile", new TypeReference<Pair<Double, Double>[]>() {
    });
    @PublicKey
    public static final Key<Long> SENSOR_ROLLING_SHUTTER_SKEW = new Key("android.sensor.rollingShutterSkew", Long.TYPE);
    @PublicKey
    public static final Key<Integer> SENSOR_SENSITIVITY = new Key("android.sensor.sensitivity", Integer.TYPE);
    @PublicKey
    public static final Key<int[]> SENSOR_TEST_PATTERN_DATA = new Key("android.sensor.testPatternData", int[].class);
    @PublicKey
    public static final Key<Integer> SENSOR_TEST_PATTERN_MODE = new Key("android.sensor.testPatternMode", Integer.TYPE);
    @PublicKey
    public static final Key<Long> SENSOR_TIMESTAMP = new Key("android.sensor.timestamp", Long.TYPE);
    @PublicKey
    public static final Key<Integer> SHADING_MODE = new Key("android.shading.mode", Integer.TYPE);
    @PublicKey
    @SyntheticKey
    public static final Key<Face[]> STATISTICS_FACES = new Key("android.statistics.faces", Face[].class);
    @PublicKey
    public static final Key<Integer> STATISTICS_FACE_DETECT_MODE = new Key("android.statistics.faceDetectMode", Integer.TYPE);
    public static final Key<int[]> STATISTICS_FACE_IDS = new Key("android.statistics.faceIds", int[].class);
    public static final Key<int[]> STATISTICS_FACE_LANDMARKS = new Key("android.statistics.faceLandmarks", int[].class);
    public static final Key<Rect[]> STATISTICS_FACE_RECTANGLES = new Key("android.statistics.faceRectangles", Rect[].class);
    public static final Key<byte[]> STATISTICS_FACE_SCORES = new Key("android.statistics.faceScores", byte[].class);
    @PublicKey
    public static final Key<Point[]> STATISTICS_HOT_PIXEL_MAP = new Key("android.statistics.hotPixelMap", Point[].class);
    @PublicKey
    public static final Key<Boolean> STATISTICS_HOT_PIXEL_MAP_MODE = new Key("android.statistics.hotPixelMapMode", Boolean.TYPE);
    @PublicKey
    public static final Key<LensShadingMap> STATISTICS_LENS_SHADING_CORRECTION_MAP = new Key("android.statistics.lensShadingCorrectionMap", LensShadingMap.class);
    public static final Key<float[]> STATISTICS_LENS_SHADING_MAP = new Key("android.statistics.lensShadingMap", float[].class);
    @PublicKey
    public static final Key<Integer> STATISTICS_LENS_SHADING_MAP_MODE = new Key("android.statistics.lensShadingMapMode", Integer.TYPE);
    @Deprecated
    public static final Key<float[]> STATISTICS_PREDICTED_COLOR_GAINS = new Key("android.statistics.predictedColorGains", float[].class);
    @Deprecated
    public static final Key<Rational[]> STATISTICS_PREDICTED_COLOR_TRANSFORM = new Key("android.statistics.predictedColorTransform", Rational[].class);
    @PublicKey
    public static final Key<Integer> STATISTICS_SCENE_FLICKER = new Key("android.statistics.sceneFlicker", Integer.TYPE);
    public static final Key<Long> SYNC_FRAME_NUMBER = new Key("android.sync.frameNumber", Long.TYPE);
    private static final String TAG = "CaptureResult";
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
    private static final boolean VERBOSE = false;
    private final long mFrameNumber;
    private final CaptureRequest mRequest;
    private final CameraMetadataNative mResults;
    private final int mSequenceId;

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
            return String.format("CaptureResult.Key(%s)", new Object[]{this.mKey.getName()});
        }

        public android.hardware.camera2.impl.CameraMetadataNative.Key<T> getNativeKey() {
            return this.mKey;
        }

        Key(android.hardware.camera2.impl.CameraMetadataNative.Key<?> nativeKey) {
            this.mKey = nativeKey;
        }
    }

    public CaptureResult(CameraMetadataNative results, CaptureRequest parent, CaptureResultExtras extras) {
        if (results == null) {
            throw new IllegalArgumentException("results was null");
        } else if (parent == null) {
            throw new IllegalArgumentException("parent was null");
        } else if (extras == null) {
            throw new IllegalArgumentException("extras was null");
        } else {
            this.mResults = CameraMetadataNative.move(results);
            if (this.mResults.isEmpty()) {
                throw new AssertionError("Results must not be empty");
            }
            setNativeInstance(this.mResults);
            this.mRequest = parent;
            this.mSequenceId = extras.getRequestId();
            this.mFrameNumber = extras.getFrameNumber();
        }
    }

    public CameraMetadataNative getNativeCopy() {
        return new CameraMetadataNative(this.mResults);
    }

    public CaptureResult(CameraMetadataNative results, int sequenceId) {
        if (results == null) {
            throw new IllegalArgumentException("results was null");
        }
        this.mResults = CameraMetadataNative.move(results);
        if (this.mResults.isEmpty()) {
            throw new AssertionError("Results must not be empty");
        }
        setNativeInstance(this.mResults);
        this.mRequest = null;
        this.mSequenceId = sequenceId;
        this.mFrameNumber = -1;
    }

    public <T> T get(Key<T> key) {
        return this.mResults.get((Key) key);
    }

    protected <T> T getProtected(Key<?> key) {
        return this.mResults.get((Key) key);
    }

    protected Class<Key<?>> getKeyClass() {
        return Key.class;
    }

    public void dumpToLog() {
        this.mResults.dumpToLog();
    }

    public void releaeNativeMetadata() {
        this.mResults.releaeNativeMetadata();
    }

    public List<Key<?>> getKeys() {
        return super.getKeys();
    }

    public CaptureRequest getRequest() {
        return this.mRequest;
    }

    public long getFrameNumber() {
        return this.mFrameNumber;
    }

    public int getSequenceId() {
        return this.mSequenceId;
    }
}
