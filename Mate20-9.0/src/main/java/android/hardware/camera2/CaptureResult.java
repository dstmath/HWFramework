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
import android.hardware.camera2.params.OisSample;
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
    public static final Key<Integer> CONTROL_AE_STATE = new Key<>("android.control.aeState", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Range<Integer>> CONTROL_AE_TARGET_FPS_RANGE = new Key<>("android.control.aeTargetFpsRange", (TypeReference<Range<Integer>>) new TypeReference<Range<Integer>>() {
    });
    @PublicKey
    public static final Key<Integer> CONTROL_AF_MODE = new Key<>("android.control.afMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<MeteringRectangle[]> CONTROL_AF_REGIONS = new Key<>("android.control.afRegions", (Class<MeteringRectangle[]>) MeteringRectangle[].class);
    @PublicKey
    public static final Key<Integer> CONTROL_AF_SCENE_CHANGE = new Key<>("android.control.afSceneChange", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_AF_STATE = new Key<>("android.control.afState", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_AF_TRIGGER = new Key<>("android.control.afTrigger", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Boolean> CONTROL_AWB_LOCK = new Key<>("android.control.awbLock", (Class<Boolean>) Boolean.TYPE);
    @PublicKey
    public static final Key<Integer> CONTROL_AWB_MODE = new Key<>("android.control.awbMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<MeteringRectangle[]> CONTROL_AWB_REGIONS = new Key<>("android.control.awbRegions", (Class<MeteringRectangle[]>) MeteringRectangle[].class);
    @PublicKey
    public static final Key<Integer> CONTROL_AWB_STATE = new Key<>("android.control.awbState", (Class<Integer>) Integer.TYPE);
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
    @PublicKey
    public static final Key<Integer> DISTORTION_CORRECTION_MODE = new Key<>("android.distortionCorrection.mode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> EDGE_MODE = new Key<>("android.edge.mode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> FLASH_MODE = new Key<>("android.flash.mode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> FLASH_STATE = new Key<>("android.flash.state", (Class<Integer>) Integer.TYPE);
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
    public static final Key<float[]> LENS_DISTORTION = new Key<>("android.lens.distortion", (Class<float[]>) float[].class);
    @PublicKey
    public static final Key<Float> LENS_FILTER_DENSITY = new Key<>("android.lens.filterDensity", (Class<Float>) Float.TYPE);
    @PublicKey
    public static final Key<Float> LENS_FOCAL_LENGTH = new Key<>("android.lens.focalLength", (Class<Float>) Float.TYPE);
    @PublicKey
    public static final Key<Float> LENS_FOCUS_DISTANCE = new Key<>("android.lens.focusDistance", (Class<Float>) Float.TYPE);
    @PublicKey
    public static final Key<Pair<Float, Float>> LENS_FOCUS_RANGE = new Key<>("android.lens.focusRange", (TypeReference<Pair<Float, Float>>) new TypeReference<Pair<Float, Float>>() {
    });
    @PublicKey
    public static final Key<float[]> LENS_INTRINSIC_CALIBRATION = new Key<>("android.lens.intrinsicCalibration", (Class<float[]>) float[].class);
    @PublicKey
    public static final Key<Integer> LENS_OPTICAL_STABILIZATION_MODE = new Key<>("android.lens.opticalStabilizationMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<float[]> LENS_POSE_ROTATION = new Key<>("android.lens.poseRotation", (Class<float[]>) float[].class);
    @PublicKey
    public static final Key<float[]> LENS_POSE_TRANSLATION = new Key<>("android.lens.poseTranslation", (Class<float[]>) float[].class);
    @PublicKey
    @Deprecated
    public static final Key<float[]> LENS_RADIAL_DISTORTION = new Key<>("android.lens.radialDistortion", (Class<float[]>) float[].class);
    @PublicKey
    public static final Key<Integer> LENS_STATE = new Key<>("android.lens.state", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> NOISE_REDUCTION_MODE = new Key<>("android.noiseReduction.mode", (Class<Integer>) Integer.TYPE);
    @Deprecated
    public static final Key<Boolean> QUIRKS_PARTIAL_RESULT = new Key<>("android.quirks.partialResult", (Class<Boolean>) Boolean.TYPE);
    @PublicKey
    public static final Key<Float> REPROCESS_EFFECTIVE_EXPOSURE_FACTOR = new Key<>("android.reprocess.effectiveExposureFactor", (Class<Float>) Float.TYPE);
    @Deprecated
    public static final Key<Integer> REQUEST_FRAME_COUNT = new Key<>("android.request.frameCount", (Class<Integer>) Integer.TYPE);
    public static final Key<Integer> REQUEST_ID = new Key<>(RestrictionsManager.REQUEST_KEY_ID, (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Byte> REQUEST_PIPELINE_DEPTH = new Key<>("android.request.pipelineDepth", (Class<Byte>) Byte.TYPE);
    @PublicKey
    public static final Key<Rect> SCALER_CROP_REGION = new Key<>("android.scaler.cropRegion", (Class<Rect>) Rect.class);
    @PublicKey
    public static final Key<float[]> SENSOR_DYNAMIC_BLACK_LEVEL = new Key<>("android.sensor.dynamicBlackLevel", (Class<float[]>) float[].class);
    @PublicKey
    public static final Key<Integer> SENSOR_DYNAMIC_WHITE_LEVEL = new Key<>("android.sensor.dynamicWhiteLevel", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Long> SENSOR_EXPOSURE_TIME = new Key<>("android.sensor.exposureTime", (Class<Long>) Long.TYPE);
    @PublicKey
    public static final Key<Long> SENSOR_FRAME_DURATION = new Key<>("android.sensor.frameDuration", (Class<Long>) Long.TYPE);
    @PublicKey
    public static final Key<Float> SENSOR_GREEN_SPLIT = new Key<>("android.sensor.greenSplit", (Class<Float>) Float.TYPE);
    @PublicKey
    public static final Key<Rational[]> SENSOR_NEUTRAL_COLOR_POINT = new Key<>("android.sensor.neutralColorPoint", (Class<Rational[]>) Rational[].class);
    @PublicKey
    public static final Key<Pair<Double, Double>[]> SENSOR_NOISE_PROFILE = new Key<>("android.sensor.noiseProfile", (TypeReference<Pair<Double, Double>[]>) new TypeReference<Pair<Double, Double>[]>() {
    });
    @PublicKey
    public static final Key<Long> SENSOR_ROLLING_SHUTTER_SKEW = new Key<>("android.sensor.rollingShutterSkew", (Class<Long>) Long.TYPE);
    @PublicKey
    public static final Key<Integer> SENSOR_SENSITIVITY = new Key<>("android.sensor.sensitivity", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<int[]> SENSOR_TEST_PATTERN_DATA = new Key<>("android.sensor.testPatternData", (Class<int[]>) int[].class);
    @PublicKey
    public static final Key<Integer> SENSOR_TEST_PATTERN_MODE = new Key<>("android.sensor.testPatternMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Long> SENSOR_TIMESTAMP = new Key<>("android.sensor.timestamp", (Class<Long>) Long.TYPE);
    @PublicKey
    public static final Key<Integer> SHADING_MODE = new Key<>("android.shading.mode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    @SyntheticKey
    public static final Key<Face[]> STATISTICS_FACES = new Key<>("android.statistics.faces", (Class<Face[]>) Face[].class);
    @PublicKey
    public static final Key<Integer> STATISTICS_FACE_DETECT_MODE = new Key<>("android.statistics.faceDetectMode", (Class<Integer>) Integer.TYPE);
    public static final Key<int[]> STATISTICS_FACE_IDS = new Key<>("android.statistics.faceIds", (Class<int[]>) int[].class);
    public static final Key<int[]> STATISTICS_FACE_LANDMARKS = new Key<>("android.statistics.faceLandmarks", (Class<int[]>) int[].class);
    public static final Key<Rect[]> STATISTICS_FACE_RECTANGLES = new Key<>("android.statistics.faceRectangles", (Class<Rect[]>) Rect[].class);
    public static final Key<byte[]> STATISTICS_FACE_SCORES = new Key<>("android.statistics.faceScores", (Class<byte[]>) byte[].class);
    @PublicKey
    public static final Key<Point[]> STATISTICS_HOT_PIXEL_MAP = new Key<>("android.statistics.hotPixelMap", (Class<Point[]>) Point[].class);
    @PublicKey
    public static final Key<Boolean> STATISTICS_HOT_PIXEL_MAP_MODE = new Key<>("android.statistics.hotPixelMapMode", (Class<Boolean>) Boolean.TYPE);
    @PublicKey
    public static final Key<LensShadingMap> STATISTICS_LENS_SHADING_CORRECTION_MAP = new Key<>("android.statistics.lensShadingCorrectionMap", (Class<LensShadingMap>) LensShadingMap.class);
    public static final Key<float[]> STATISTICS_LENS_SHADING_MAP = new Key<>("android.statistics.lensShadingMap", (Class<float[]>) float[].class);
    @PublicKey
    public static final Key<Integer> STATISTICS_LENS_SHADING_MAP_MODE = new Key<>("android.statistics.lensShadingMapMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    public static final Key<Integer> STATISTICS_OIS_DATA_MODE = new Key<>("android.statistics.oisDataMode", (Class<Integer>) Integer.TYPE);
    @PublicKey
    @SyntheticKey
    public static final Key<OisSample[]> STATISTICS_OIS_SAMPLES = new Key<>("android.statistics.oisSamples", (Class<OisSample[]>) OisSample[].class);
    public static final Key<long[]> STATISTICS_OIS_TIMESTAMPS = new Key<>("android.statistics.oisTimestamps", (Class<long[]>) long[].class);
    public static final Key<float[]> STATISTICS_OIS_X_SHIFTS = new Key<>("android.statistics.oisXShifts", (Class<float[]>) float[].class);
    public static final Key<float[]> STATISTICS_OIS_Y_SHIFTS = new Key<>("android.statistics.oisYShifts", (Class<float[]>) float[].class);
    @Deprecated
    public static final Key<float[]> STATISTICS_PREDICTED_COLOR_GAINS = new Key<>("android.statistics.predictedColorGains", (Class<float[]>) float[].class);
    @Deprecated
    public static final Key<Rational[]> STATISTICS_PREDICTED_COLOR_TRANSFORM = new Key<>("android.statistics.predictedColorTransform", (Class<Rational[]>) Rational[].class);
    @PublicKey
    public static final Key<Integer> STATISTICS_SCENE_FLICKER = new Key<>("android.statistics.sceneFlicker", (Class<Integer>) Integer.TYPE);
    public static final Key<Long> SYNC_FRAME_NUMBER = new Key<>("android.sync.frameNumber", (Class<Long>) Long.TYPE);
    private static final String TAG = "CaptureResult";
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
    private static final boolean VERBOSE = false;
    private final long mFrameNumber;
    private final CaptureRequest mRequest;
    private final CameraMetadataNative mResults;
    private final int mSequenceId;

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
            return String.format("CaptureResult.Key(%s)", new Object[]{this.mKey.getName()});
        }

        public CameraMetadataNative.Key<T> getNativeKey() {
            return this.mKey;
        }

        Key(CameraMetadataNative.Key<?> nativeKey) {
            this.mKey = nativeKey;
        }
    }

    public CaptureResult(CameraMetadataNative results, CaptureRequest parent, CaptureResultExtras extras) {
        if (results == null) {
            throw new IllegalArgumentException("results was null");
        } else if (parent == null) {
            throw new IllegalArgumentException("parent was null");
        } else if (extras != null) {
            this.mResults = CameraMetadataNative.move(results);
            if (!this.mResults.isEmpty()) {
                setNativeInstance(this.mResults);
                this.mRequest = parent;
                this.mSequenceId = extras.getRequestId();
                this.mFrameNumber = extras.getFrameNumber();
                return;
            }
            throw new AssertionError("Results must not be empty");
        } else {
            throw new IllegalArgumentException("extras was null");
        }
    }

    public CameraMetadataNative getNativeCopy() {
        return new CameraMetadataNative(this.mResults);
    }

    public CaptureResult(CameraMetadataNative results, int sequenceId) {
        if (results != null) {
            this.mResults = CameraMetadataNative.move(results);
            if (!this.mResults.isEmpty()) {
                setNativeInstance(this.mResults);
                this.mRequest = null;
                this.mSequenceId = sequenceId;
                this.mFrameNumber = -1;
                return;
            }
            throw new AssertionError("Results must not be empty");
        }
        throw new IllegalArgumentException("results was null");
    }

    public <T> T get(Key<T> key) {
        return this.mResults.get(key);
    }

    /* access modifiers changed from: protected */
    public <T> T getProtected(Key<?> key) {
        return this.mResults.get(key);
    }

    /* access modifiers changed from: protected */
    public Class<Key<?>> getKeyClass() {
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
