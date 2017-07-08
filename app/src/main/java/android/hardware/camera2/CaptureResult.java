package android.hardware.camera2;

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
    public static final Key<Boolean> BLACK_LEVEL_LOCK = null;
    @PublicKey
    public static final Key<Integer> COLOR_CORRECTION_ABERRATION_MODE = null;
    @PublicKey
    public static final Key<RggbChannelVector> COLOR_CORRECTION_GAINS = null;
    @PublicKey
    public static final Key<Integer> COLOR_CORRECTION_MODE = null;
    @PublicKey
    public static final Key<ColorSpaceTransform> COLOR_CORRECTION_TRANSFORM = null;
    @PublicKey
    public static final Key<Integer> CONTROL_AE_ANTIBANDING_MODE = null;
    @PublicKey
    public static final Key<Integer> CONTROL_AE_EXPOSURE_COMPENSATION = null;
    @PublicKey
    public static final Key<Boolean> CONTROL_AE_LOCK = null;
    @PublicKey
    public static final Key<Integer> CONTROL_AE_MODE = null;
    @PublicKey
    public static final Key<Integer> CONTROL_AE_PRECAPTURE_TRIGGER = null;
    @PublicKey
    public static final Key<MeteringRectangle[]> CONTROL_AE_REGIONS = null;
    @PublicKey
    public static final Key<Integer> CONTROL_AE_STATE = null;
    @PublicKey
    public static final Key<Range<Integer>> CONTROL_AE_TARGET_FPS_RANGE = null;
    @PublicKey
    public static final Key<Integer> CONTROL_AF_MODE = null;
    @PublicKey
    public static final Key<MeteringRectangle[]> CONTROL_AF_REGIONS = null;
    @PublicKey
    public static final Key<Integer> CONTROL_AF_STATE = null;
    @PublicKey
    public static final Key<Integer> CONTROL_AF_TRIGGER = null;
    @PublicKey
    public static final Key<Boolean> CONTROL_AWB_LOCK = null;
    @PublicKey
    public static final Key<Integer> CONTROL_AWB_MODE = null;
    @PublicKey
    public static final Key<MeteringRectangle[]> CONTROL_AWB_REGIONS = null;
    @PublicKey
    public static final Key<Integer> CONTROL_AWB_STATE = null;
    @PublicKey
    public static final Key<Integer> CONTROL_CAPTURE_INTENT = null;
    @PublicKey
    public static final Key<Integer> CONTROL_EFFECT_MODE = null;
    @PublicKey
    public static final Key<Integer> CONTROL_MODE = null;
    @PublicKey
    public static final Key<Integer> CONTROL_POST_RAW_SENSITIVITY_BOOST = null;
    @PublicKey
    public static final Key<Integer> CONTROL_SCENE_MODE = null;
    @PublicKey
    public static final Key<Integer> CONTROL_VIDEO_STABILIZATION_MODE = null;
    @PublicKey
    public static final Key<Integer> EDGE_MODE = null;
    @PublicKey
    public static final Key<Integer> FLASH_MODE = null;
    @PublicKey
    public static final Key<Integer> FLASH_STATE = null;
    @PublicKey
    public static final Key<Integer> HOT_PIXEL_MODE = null;
    public static final Key<double[]> JPEG_GPS_COORDINATES = null;
    @PublicKey
    @SyntheticKey
    public static final Key<Location> JPEG_GPS_LOCATION = null;
    public static final Key<String> JPEG_GPS_PROCESSING_METHOD = null;
    public static final Key<Long> JPEG_GPS_TIMESTAMP = null;
    @PublicKey
    public static final Key<Integer> JPEG_ORIENTATION = null;
    @PublicKey
    public static final Key<Byte> JPEG_QUALITY = null;
    @PublicKey
    public static final Key<Byte> JPEG_THUMBNAIL_QUALITY = null;
    @PublicKey
    public static final Key<Size> JPEG_THUMBNAIL_SIZE = null;
    public static final Key<Boolean> LED_TRANSMIT = null;
    @PublicKey
    public static final Key<Float> LENS_APERTURE = null;
    @PublicKey
    public static final Key<Float> LENS_FILTER_DENSITY = null;
    @PublicKey
    public static final Key<Float> LENS_FOCAL_LENGTH = null;
    @PublicKey
    public static final Key<Float> LENS_FOCUS_DISTANCE = null;
    @PublicKey
    public static final Key<Pair<Float, Float>> LENS_FOCUS_RANGE = null;
    @PublicKey
    public static final Key<float[]> LENS_INTRINSIC_CALIBRATION = null;
    @PublicKey
    public static final Key<Integer> LENS_OPTICAL_STABILIZATION_MODE = null;
    @PublicKey
    public static final Key<float[]> LENS_POSE_ROTATION = null;
    @PublicKey
    public static final Key<float[]> LENS_POSE_TRANSLATION = null;
    @PublicKey
    public static final Key<float[]> LENS_RADIAL_DISTORTION = null;
    @PublicKey
    public static final Key<Integer> LENS_STATE = null;
    @PublicKey
    public static final Key<Integer> NOISE_REDUCTION_MODE = null;
    @Deprecated
    public static final Key<Boolean> QUIRKS_PARTIAL_RESULT = null;
    @PublicKey
    public static final Key<Float> REPROCESS_EFFECTIVE_EXPOSURE_FACTOR = null;
    @Deprecated
    public static final Key<Integer> REQUEST_FRAME_COUNT = null;
    public static final Key<Integer> REQUEST_ID = null;
    @PublicKey
    public static final Key<Byte> REQUEST_PIPELINE_DEPTH = null;
    @PublicKey
    public static final Key<Rect> SCALER_CROP_REGION = null;
    @PublicKey
    public static final Key<float[]> SENSOR_DYNAMIC_BLACK_LEVEL = null;
    @PublicKey
    public static final Key<Integer> SENSOR_DYNAMIC_WHITE_LEVEL = null;
    @PublicKey
    public static final Key<Long> SENSOR_EXPOSURE_TIME = null;
    @PublicKey
    public static final Key<Long> SENSOR_FRAME_DURATION = null;
    @PublicKey
    public static final Key<Float> SENSOR_GREEN_SPLIT = null;
    @PublicKey
    public static final Key<Rational[]> SENSOR_NEUTRAL_COLOR_POINT = null;
    @PublicKey
    public static final Key<Pair<Double, Double>[]> SENSOR_NOISE_PROFILE = null;
    @PublicKey
    public static final Key<Long> SENSOR_ROLLING_SHUTTER_SKEW = null;
    @PublicKey
    public static final Key<Integer> SENSOR_SENSITIVITY = null;
    @PublicKey
    public static final Key<int[]> SENSOR_TEST_PATTERN_DATA = null;
    @PublicKey
    public static final Key<Integer> SENSOR_TEST_PATTERN_MODE = null;
    @PublicKey
    public static final Key<Long> SENSOR_TIMESTAMP = null;
    @PublicKey
    public static final Key<Integer> SHADING_MODE = null;
    @PublicKey
    @SyntheticKey
    public static final Key<Face[]> STATISTICS_FACES = null;
    @PublicKey
    public static final Key<Integer> STATISTICS_FACE_DETECT_MODE = null;
    public static final Key<int[]> STATISTICS_FACE_IDS = null;
    public static final Key<int[]> STATISTICS_FACE_LANDMARKS = null;
    public static final Key<Rect[]> STATISTICS_FACE_RECTANGLES = null;
    public static final Key<byte[]> STATISTICS_FACE_SCORES = null;
    @PublicKey
    public static final Key<Point[]> STATISTICS_HOT_PIXEL_MAP = null;
    @PublicKey
    public static final Key<Boolean> STATISTICS_HOT_PIXEL_MAP_MODE = null;
    @PublicKey
    public static final Key<LensShadingMap> STATISTICS_LENS_SHADING_CORRECTION_MAP = null;
    public static final Key<float[]> STATISTICS_LENS_SHADING_MAP = null;
    @PublicKey
    public static final Key<Integer> STATISTICS_LENS_SHADING_MAP_MODE = null;
    @Deprecated
    public static final Key<float[]> STATISTICS_PREDICTED_COLOR_GAINS = null;
    @Deprecated
    public static final Key<Rational[]> STATISTICS_PREDICTED_COLOR_TRANSFORM = null;
    @PublicKey
    public static final Key<Integer> STATISTICS_SCENE_FLICKER = null;
    public static final Key<Long> SYNC_FRAME_NUMBER = null;
    private static final String TAG = "CaptureResult";
    @PublicKey
    @SyntheticKey
    public static final Key<TonemapCurve> TONEMAP_CURVE = null;
    public static final Key<float[]> TONEMAP_CURVE_BLUE = null;
    public static final Key<float[]> TONEMAP_CURVE_GREEN = null;
    public static final Key<float[]> TONEMAP_CURVE_RED = null;
    @PublicKey
    public static final Key<Float> TONEMAP_GAMMA = null;
    @PublicKey
    public static final Key<Integer> TONEMAP_MODE = null;
    @PublicKey
    public static final Key<Integer> TONEMAP_PRESET_CURVE = null;
    private static final boolean VERBOSE = false;
    private final long mFrameNumber;
    private final CaptureRequest mRequest;
    private final CameraMetadataNative mResults;
    private final int mSequenceId;

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
            return String.format("CaptureResult.Key(%s)", new Object[]{this.mKey.getName()});
        }

        public android.hardware.camera2.impl.CameraMetadataNative.Key<T> getNativeKey() {
            return this.mKey;
        }

        Key(android.hardware.camera2.impl.CameraMetadataNative.Key<?> nativeKey) {
            this.mKey = nativeKey;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.camera2.CaptureResult.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.camera2.CaptureResult.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.CaptureResult.<clinit>():void");
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
