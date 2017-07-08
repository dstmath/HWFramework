package android.hardware.camera2.legacy;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraCharacteristics.Key;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfiguration;
import android.hardware.camera2.params.StreamConfigurationDuration;
import android.hardware.camera2.utils.ArrayUtils;
import android.hardware.camera2.utils.ListUtils;
import android.hardware.camera2.utils.ParamsUtils;
import android.hardware.camera2.utils.SizeAreaComparator;
import android.provider.CalendarContract.CalendarCache;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import android.util.Log;
import android.util.Range;
import android.util.SizeF;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LegacyMetadataMapper {
    private static final long APPROXIMATE_CAPTURE_DELAY_MS = 200;
    private static final long APPROXIMATE_JPEG_ENCODE_TIME_MS = 600;
    private static final long APPROXIMATE_SENSOR_AREA_PX = 8388608;
    private static final boolean DEBUG = false;
    public static final int HAL_PIXEL_FORMAT_BGRA_8888 = 5;
    public static final int HAL_PIXEL_FORMAT_BLOB = 33;
    public static final int HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED = 34;
    public static final int HAL_PIXEL_FORMAT_RGBA_8888 = 1;
    private static final float LENS_INFO_MINIMUM_FOCUS_DISTANCE_FIXED_FOCUS = 0.0f;
    static final boolean LIE_ABOUT_AE_MAX_REGIONS = false;
    static final boolean LIE_ABOUT_AE_STATE = false;
    static final boolean LIE_ABOUT_AF = false;
    static final boolean LIE_ABOUT_AF_MAX_REGIONS = false;
    static final boolean LIE_ABOUT_AWB = false;
    static final boolean LIE_ABOUT_AWB_STATE = false;
    private static final long NS_PER_MS = 1000000;
    private static final float PREVIEW_ASPECT_RATIO_TOLERANCE = 0.01f;
    private static final int REQUEST_MAX_NUM_INPUT_STREAMS_COUNT = 0;
    private static final int REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC = 3;
    private static final int REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL = 1;
    private static final int REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW = 0;
    private static final int REQUEST_PIPELINE_MAX_DEPTH_HAL1 = 3;
    private static final int REQUEST_PIPELINE_MAX_DEPTH_OURS = 3;
    private static final String TAG = "LegacyMetadataMapper";
    static final int UNKNOWN_MODE = -1;
    private static final int[] sAllowedTemplates = null;
    private static final int[] sEffectModes = null;
    private static final String[] sLegacyEffectMode = null;
    private static final String[] sLegacySceneModes = null;
    private static final int[] sSceneModes = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.camera2.legacy.LegacyMetadataMapper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.camera2.legacy.LegacyMetadataMapper.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.camera2.legacy.LegacyMetadataMapper.<clinit>():void");
    }

    public static CameraCharacteristics createCharacteristics(Parameters parameters, CameraInfo info) {
        Preconditions.checkNotNull(parameters, "parameters must not be null");
        Preconditions.checkNotNull(info, "info must not be null");
        String paramStr = parameters.flatten();
        android.hardware.CameraInfo outerInfo = new android.hardware.CameraInfo();
        outerInfo.info = info;
        return createCharacteristics(paramStr, outerInfo);
    }

    public static CameraCharacteristics createCharacteristics(String parameters, android.hardware.CameraInfo info) {
        Preconditions.checkNotNull(parameters, "parameters must not be null");
        Preconditions.checkNotNull(info, "info must not be null");
        Preconditions.checkNotNull(info.info, "info.info must not be null");
        CameraMetadataNative m = new CameraMetadataNative();
        mapCharacteristicsFromInfo(m, info.info);
        Parameters params = Camera.getEmptyParameters();
        params.unflatten(parameters);
        mapCharacteristicsFromParameters(m, params);
        return new CameraCharacteristics(m);
    }

    private static void mapCharacteristicsFromInfo(CameraMetadataNative m, CameraInfo i) {
        int i2 = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW;
        Key key = CameraCharacteristics.LENS_FACING;
        if (i.facing == 0) {
            i2 = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL;
        }
        m.set(key, Integer.valueOf(i2));
        m.set(CameraCharacteristics.SENSOR_ORIENTATION, Integer.valueOf(i.orientation));
    }

    private static void mapCharacteristicsFromParameters(CameraMetadataNative m, Parameters p) {
        m.set(CameraCharacteristics.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES, new int[]{REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL, 2});
        mapControlAe(m, p);
        mapControlAf(m, p);
        mapControlAwb(m, p);
        mapControlOther(m, p);
        mapLens(m, p);
        mapFlash(m, p);
        mapJpeg(m, p);
        m.set(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES, new int[]{REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL, 2});
        mapScaler(m, p);
        mapSensor(m, p);
        mapStatistics(m, p);
        mapSync(m, p);
        m.set(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL, Integer.valueOf(2));
        mapScalerStreamConfigs(m, p);
        mapRequest(m, p);
    }

    private static void mapScalerStreamConfigs(CameraMetadataNative m, Parameters p) {
        ArrayList<StreamConfiguration> availableStreamConfigs = new ArrayList();
        List<Size> previewSizes = p.getSupportedPreviewSizes();
        List<Size> jpegSizes = p.getSupportedPictureSizes();
        SizeAreaComparator areaComparator = new SizeAreaComparator();
        Collections.sort(previewSizes, areaComparator);
        Size maxJpegSize = SizeAreaComparator.findLargestByArea(jpegSizes);
        float jpegAspectRatio = (((float) maxJpegSize.width) * Engine.DEFAULT_VOLUME) / ((float) maxJpegSize.height);
        while (!previewSizes.isEmpty()) {
            int index = previewSizes.size() + UNKNOWN_MODE;
            Size size = (Size) previewSizes.get(index);
            if (Math.abs(jpegAspectRatio - ((((float) size.width) * Engine.DEFAULT_VOLUME) / ((float) size.height))) < PREVIEW_ASPECT_RATIO_TOLERANCE) {
                break;
            }
            previewSizes.remove(index);
        }
        if (previewSizes.isEmpty()) {
            Log.w(TAG, "mapScalerStreamConfigs - failed to find any preview size matching JPEG aspect ratio " + jpegAspectRatio);
            previewSizes = p.getSupportedPreviewSizes();
        }
        Collections.sort(previewSizes, Collections.reverseOrder(areaComparator));
        appendStreamConfig(availableStreamConfigs, HAL_PIXEL_FORMAT_IMPLEMENTATION_DEFINED, previewSizes);
        appendStreamConfig(availableStreamConfigs, 35, previewSizes);
        for (Integer intValue : p.getSupportedPreviewFormats()) {
            int format = intValue.intValue();
            if (ImageFormat.isPublicFormat(format) && format != 17) {
                appendStreamConfig(availableStreamConfigs, format, previewSizes);
            }
        }
        appendStreamConfig(availableStreamConfigs, HAL_PIXEL_FORMAT_BLOB, p.getSupportedPictureSizes());
        m.set(CameraCharacteristics.SCALER_AVAILABLE_STREAM_CONFIGURATIONS, (StreamConfiguration[]) availableStreamConfigs.toArray(new StreamConfiguration[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW]));
        m.set(CameraCharacteristics.SCALER_AVAILABLE_MIN_FRAME_DURATIONS, new StreamConfigurationDuration[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW]);
        Object jpegStalls = new StreamConfigurationDuration[jpegSizes.size()];
        int i = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW;
        long longestStallDuration = -1;
        for (Size s : jpegSizes) {
            long stallDuration = calculateJpegStallDuration(s);
            int i2 = i + REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL;
            jpegStalls[i] = new StreamConfigurationDuration(HAL_PIXEL_FORMAT_BLOB, s.width, s.height, stallDuration);
            if (longestStallDuration < stallDuration) {
                longestStallDuration = stallDuration;
            }
            i = i2;
        }
        m.set(CameraCharacteristics.SCALER_AVAILABLE_STALL_DURATIONS, jpegStalls);
        m.set(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION, Long.valueOf(longestStallDuration));
    }

    private static void mapControlAe(CameraMetadataNative m, Parameters p) {
        List<String> antiBandingModes = p.getSupportedAntibanding();
        if (antiBandingModes == null || antiBandingModes.size() <= 0) {
            Object obj = new int[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW];
            m.set(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES, obj);
        } else {
            int[] modes = new int[antiBandingModes.size()];
            int j = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW;
            for (String mode : antiBandingModes) {
                int j2 = j + REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL;
                modes[j] = convertAntiBandingMode(mode);
                j = j2;
            }
            m.set(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES, Arrays.copyOf(modes, j));
        }
        List<int[]> fpsRanges = p.getSupportedPreviewFpsRange();
        if (fpsRanges == null) {
            throw new AssertionError("Supported FPS ranges cannot be null.");
        }
        int rangesSize = fpsRanges.size();
        if (rangesSize <= 0) {
            throw new AssertionError("At least one FPS range must be supported.");
        }
        Object ranges = new Range[rangesSize];
        int i = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW;
        for (int[] r : fpsRanges) {
            int i2 = i + REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL;
            ranges[i] = Range.create(Integer.valueOf((int) Math.floor(((double) r[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW]) / 1000.0d)), Integer.valueOf((int) Math.ceil(((double) r[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL]) / 1000.0d)));
            i = i2;
        }
        m.set(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES, ranges);
        List<String> flashModes = p.getSupportedFlashModes();
        String[] flashModeStrings = new String[HAL_PIXEL_FORMAT_BGRA_8888];
        flashModeStrings[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW] = Parameters.FLASH_MODE_OFF;
        flashModeStrings[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL] = CalendarCache.TIMEZONE_TYPE_AUTO;
        flashModeStrings[2] = Parameters.FLASH_MODE_ON;
        flashModeStrings[REQUEST_PIPELINE_MAX_DEPTH_OURS] = Parameters.FLASH_MODE_RED_EYE;
        flashModeStrings[4] = Parameters.FLASH_MODE_TORCH;
        Object aeAvail = ArrayUtils.convertStringListToIntArray(flashModes, flashModeStrings, new int[]{REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL, 2, REQUEST_PIPELINE_MAX_DEPTH_OURS, 4});
        if (aeAvail == null || aeAvail.length == 0) {
            aeAvail = new int[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL];
            aeAvail[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW] = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL;
        }
        m.set(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES, aeAvail);
        int min = p.getMinExposureCompensation();
        int max = p.getMaxExposureCompensation();
        m.set(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE, Range.create(Integer.valueOf(min), Integer.valueOf(max)));
        float step = p.getExposureCompensationStep();
        m.set(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP, ParamsUtils.createRational(step));
        boolean aeLockAvailable = p.isAutoExposureLockSupported();
        m.set(CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE, Boolean.valueOf(aeLockAvailable));
    }

    private static void mapControlAf(CameraMetadataNative m, Parameters p) {
        List<Integer> afAvail = ArrayUtils.convertStringListToIntList(p.getSupportedFocusModes(), new String[]{CalendarCache.TIMEZONE_TYPE_AUTO, Parameters.FOCUS_MODE_CONTINUOUS_PICTURE, Parameters.FOCUS_MODE_CONTINUOUS_VIDEO, Parameters.FOCUS_MODE_EDOF, Parameters.FOCUS_MODE_INFINITY, Parameters.FOCUS_MODE_MACRO, Parameters.FOCUS_MODE_FIXED}, new int[]{REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL, 4, REQUEST_PIPELINE_MAX_DEPTH_OURS, HAL_PIXEL_FORMAT_BGRA_8888, REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW, 2, REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW});
        if (afAvail == null || afAvail.size() == 0) {
            Log.w(TAG, "No AF modes supported (HAL bug); defaulting to AF_MODE_OFF only");
            afAvail = new ArrayList(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL);
            afAvail.add(Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW));
        }
        m.set(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES, ArrayUtils.toIntArray(afAvail));
    }

    private static void mapControlAwb(CameraMetadataNative m, Parameters p) {
        List<Integer> awbAvail = ArrayUtils.convertStringListToIntList(p.getSupportedWhiteBalance(), new String[]{CalendarCache.TIMEZONE_TYPE_AUTO, Parameters.WHITE_BALANCE_INCANDESCENT, Parameters.WHITE_BALANCE_FLUORESCENT, Parameters.WHITE_BALANCE_WARM_FLUORESCENT, Parameters.WHITE_BALANCE_DAYLIGHT, Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT, Parameters.WHITE_BALANCE_TWILIGHT, Parameters.WHITE_BALANCE_SHADE}, new int[]{REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL, 2, REQUEST_PIPELINE_MAX_DEPTH_OURS, 4, HAL_PIXEL_FORMAT_BGRA_8888, 6, 7, 8});
        if (awbAvail == null || awbAvail.size() == 0) {
            Log.w(TAG, "No AWB modes supported (HAL bug); defaulting to AWB_MODE_AUTO only");
            awbAvail = new ArrayList(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL);
            awbAvail.add(Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL));
        }
        m.set(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES, ArrayUtils.toIntArray(awbAvail));
        m.set(CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE, Boolean.valueOf(p.isAutoWhiteBalanceLockSupported()));
    }

    private static void mapControlOther(CameraMetadataNative m, Parameters p) {
        Object stabModes;
        Object supportedEffectModes;
        Object obj;
        if (p.isVideoStabilizationSupported()) {
            stabModes = new int[]{REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW, REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL};
        } else {
            stabModes = new int[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL];
            stabModes[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW] = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW;
        }
        m.set(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES, stabModes);
        Object maxRegions = new int[REQUEST_PIPELINE_MAX_DEPTH_OURS];
        maxRegions[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW] = p.getMaxNumMeteringAreas();
        maxRegions[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL] = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW;
        maxRegions[2] = p.getMaxNumFocusAreas();
        m.set(CameraCharacteristics.CONTROL_MAX_REGIONS, maxRegions);
        List<String> effectModes = p.getSupportedColorEffects();
        if (effectModes == null) {
            supportedEffectModes = new int[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW];
        } else {
            supportedEffectModes = ArrayUtils.convertStringListToIntArray(effectModes, sLegacyEffectMode, sEffectModes);
        }
        m.set(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS, supportedEffectModes);
        int maxNumDetectedFaces = p.getMaxNumDetectedFaces();
        List<String> sceneModes = p.getSupportedSceneModes();
        List supportedSceneModes = ArrayUtils.convertStringListToIntList(sceneModes, sLegacySceneModes, sSceneModes);
        if (sceneModes != null && sceneModes.size() == REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL && sceneModes.get(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW) == CalendarCache.TIMEZONE_TYPE_AUTO) {
            supportedSceneModes = null;
        }
        boolean sceneModeSupported = true;
        if (supportedSceneModes == null && maxNumDetectedFaces == 0) {
            sceneModeSupported = LIE_ABOUT_AWB_STATE;
        }
        if (sceneModeSupported) {
            if (supportedSceneModes == null) {
                supportedSceneModes = new ArrayList();
            }
            if (maxNumDetectedFaces > 0) {
                supportedSceneModes.add(Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL));
            }
            if (supportedSceneModes.contains(Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW))) {
                do {
                } while (supportedSceneModes.remove(new Integer(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW)));
            }
            m.set(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES, ArrayUtils.toIntArray(supportedSceneModes));
        } else {
            Key key = CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES;
            Object obj2 = new int[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL];
            obj2[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW] = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW;
            m.set(key, obj2);
        }
        Key key2 = CameraCharacteristics.CONTROL_AVAILABLE_MODES;
        if (sceneModeSupported) {
            obj = new int[]{REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL, 2};
        } else {
            obj = new int[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL];
            obj[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW] = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL;
        }
        m.set(key2, obj);
    }

    private static void mapLens(CameraMetadataNative m, Parameters p) {
        if (Parameters.FOCUS_MODE_FIXED.equals(p.getFocusMode())) {
            m.set(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE, Float.valueOf(LENS_INFO_MINIMUM_FOCUS_DISTANCE_FIXED_FOCUS));
        }
        Object focalLengths = new float[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL];
        focalLengths[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW] = p.getFocalLength();
        m.set(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS, focalLengths);
    }

    private static void mapFlash(CameraMetadataNative m, Parameters p) {
        boolean flashAvailable = LIE_ABOUT_AWB_STATE;
        List<String> supportedFlashModes = p.getSupportedFlashModes();
        if (supportedFlashModes != null) {
            flashAvailable = ListUtils.listElementsEqualTo(supportedFlashModes, Parameters.FLASH_MODE_OFF) ? LIE_ABOUT_AWB_STATE : true;
        }
        m.set(CameraCharacteristics.FLASH_INFO_AVAILABLE, Boolean.valueOf(flashAvailable));
    }

    private static void mapJpeg(CameraMetadataNative m, Parameters p) {
        List<Size> thumbnailSizes = p.getSupportedJpegThumbnailSizes();
        if (thumbnailSizes != null) {
            Object sizes = ParameterUtils.convertSizeListToArray(thumbnailSizes);
            Arrays.sort(sizes, new SizeAreaComparator());
            m.set(CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES, sizes);
        }
    }

    private static void mapRequest(CameraMetadataNative m, Parameters p) {
        Object capabilities = new int[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL];
        capabilities[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW] = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW;
        m.set(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES, capabilities);
        List<Key<?>> characteristicsKeys = new ArrayList(Arrays.asList(new Key[]{CameraCharacteristics.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES, CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES, CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES, CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES, CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE, CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP, CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE, CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES, CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS, CameraCharacteristics.CONTROL_AVAILABLE_MODES, CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES, CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES, CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES, CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE, CameraCharacteristics.CONTROL_MAX_REGIONS, CameraCharacteristics.FLASH_INFO_AVAILABLE, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL, CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES, CameraCharacteristics.LENS_FACING, CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS, CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES, CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES, CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_STREAMS, CameraCharacteristics.REQUEST_PARTIAL_RESULT_COUNT, CameraCharacteristics.REQUEST_PIPELINE_MAX_DEPTH, CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM, CameraCharacteristics.SCALER_CROPPING_TYPE, CameraCharacteristics.SENSOR_AVAILABLE_TEST_PATTERN_MODES, CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE, CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE, CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE, CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE, CameraCharacteristics.SENSOR_ORIENTATION, CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES, CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT, CameraCharacteristics.SYNC_MAX_LATENCY}));
        if (m.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE) != null) {
            characteristicsKeys.add(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        }
        m.set(CameraCharacteristics.REQUEST_AVAILABLE_CHARACTERISTICS_KEYS, getTagsForKeys((Key[]) characteristicsKeys.toArray(new Key[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW])));
        ArrayList<CaptureRequest.Key<?>> availableKeys = new ArrayList(Arrays.asList(new CaptureRequest.Key[]{CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, CaptureRequest.CONTROL_AE_LOCK, CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AWB_LOCK, CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_CAPTURE_INTENT, CaptureRequest.CONTROL_EFFECT_MODE, CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.FLASH_MODE, CaptureRequest.JPEG_GPS_COORDINATES, CaptureRequest.JPEG_GPS_PROCESSING_METHOD, CaptureRequest.JPEG_GPS_TIMESTAMP, CaptureRequest.JPEG_ORIENTATION, CaptureRequest.JPEG_QUALITY, CaptureRequest.JPEG_THUMBNAIL_QUALITY, CaptureRequest.JPEG_THUMBNAIL_SIZE, CaptureRequest.LENS_FOCAL_LENGTH, CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.SCALER_CROP_REGION, CaptureRequest.STATISTICS_FACE_DETECT_MODE}));
        if (p.getMaxNumMeteringAreas() > 0) {
            availableKeys.add(CaptureRequest.CONTROL_AE_REGIONS);
        }
        if (p.getMaxNumFocusAreas() > 0) {
            availableKeys.add(CaptureRequest.CONTROL_AF_REGIONS);
        }
        CaptureRequest.Key[] availableRequestKeys = new CaptureRequest.Key[availableKeys.size()];
        availableKeys.toArray(availableRequestKeys);
        m.set(CameraCharacteristics.REQUEST_AVAILABLE_REQUEST_KEYS, getTagsForKeys(availableRequestKeys));
        List<CaptureResult.Key<?>> availableKeys2 = new ArrayList(Arrays.asList(new CaptureResult.Key[]{CaptureResult.COLOR_CORRECTION_ABERRATION_MODE, CaptureResult.CONTROL_AE_ANTIBANDING_MODE, CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION, CaptureResult.CONTROL_AE_LOCK, CaptureResult.CONTROL_AE_MODE, CaptureResult.CONTROL_AF_MODE, CaptureResult.CONTROL_AF_STATE, CaptureResult.CONTROL_AWB_MODE, CaptureResult.CONTROL_AWB_LOCK, CaptureResult.CONTROL_MODE, CaptureResult.FLASH_MODE, CaptureResult.JPEG_GPS_COORDINATES, CaptureResult.JPEG_GPS_PROCESSING_METHOD, CaptureResult.JPEG_GPS_TIMESTAMP, CaptureResult.JPEG_ORIENTATION, CaptureResult.JPEG_QUALITY, CaptureResult.JPEG_THUMBNAIL_QUALITY, CaptureResult.LENS_FOCAL_LENGTH, CaptureResult.NOISE_REDUCTION_MODE, CaptureResult.REQUEST_PIPELINE_DEPTH, CaptureResult.SCALER_CROP_REGION, CaptureResult.SENSOR_TIMESTAMP, CaptureResult.STATISTICS_FACE_DETECT_MODE}));
        if (p.getMaxNumMeteringAreas() > 0) {
            availableKeys2.add(CaptureResult.CONTROL_AE_REGIONS);
        }
        if (p.getMaxNumFocusAreas() > 0) {
            availableKeys2.add(CaptureResult.CONTROL_AF_REGIONS);
        }
        CaptureResult.Key[] availableResultKeys = new CaptureResult.Key[availableKeys2.size()];
        availableKeys2.toArray(availableResultKeys);
        m.set(CameraCharacteristics.REQUEST_AVAILABLE_RESULT_KEYS, getTagsForKeys(availableResultKeys));
        m.set(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_STREAMS, new int[]{REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW, REQUEST_PIPELINE_MAX_DEPTH_OURS, REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL});
        m.set(CameraCharacteristics.REQUEST_MAX_NUM_INPUT_STREAMS, Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW));
        m.set(CameraCharacteristics.REQUEST_PARTIAL_RESULT_COUNT, Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL));
        m.set(CameraCharacteristics.REQUEST_PIPELINE_MAX_DEPTH, Byte.valueOf((byte) 6));
    }

    private static void mapScaler(CameraMetadataNative m, Parameters p) {
        m.set(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM, Float.valueOf(ParameterUtils.getMaxZoomRatio(p)));
        m.set(CameraCharacteristics.SCALER_CROPPING_TYPE, Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW));
    }

    private static void mapSensor(CameraMetadataNative m, Parameters p) {
        Object largestJpegSize = ParameterUtils.getLargestSupportedJpegSizeByArea(p);
        m.set(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE, ParamsUtils.createRect((android.util.Size) largestJpegSize));
        Key key = CameraCharacteristics.SENSOR_AVAILABLE_TEST_PATTERN_MODES;
        Object obj = new int[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL];
        obj[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW] = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW;
        m.set(key, obj);
        m.set(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE, largestJpegSize);
        float focalLength = p.getFocalLength();
        float width = (float) Math.abs(((double) (2.0f * focalLength)) * Math.tan(((((double) p.getHorizontalViewAngle()) * 3.141592653589793d) / 180.0d) / 2.0d));
        m.set(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE, new SizeF(width, (float) Math.abs(((double) (2.0f * focalLength)) * Math.tan(((((double) p.getVerticalViewAngle()) * 3.141592653589793d) / 180.0d) / 2.0d))));
        m.set(CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE, Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW));
    }

    private static void mapStatistics(CameraMetadataNative m, Parameters p) {
        Object fdModes;
        if (p.getMaxNumDetectedFaces() > 0) {
            fdModes = new int[]{REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW, REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL};
        } else {
            fdModes = new int[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL];
            fdModes[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW] = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW;
        }
        m.set(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES, fdModes);
        m.set(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT, Integer.valueOf(p.getMaxNumDetectedFaces()));
    }

    private static void mapSync(CameraMetadataNative m, Parameters p) {
        m.set(CameraCharacteristics.SYNC_MAX_LATENCY, Integer.valueOf(UNKNOWN_MODE));
    }

    private static void appendStreamConfig(ArrayList<StreamConfiguration> configs, int format, List<Size> sizes) {
        for (Size size : sizes) {
            configs.add(new StreamConfiguration(format, size.width, size.height, LIE_ABOUT_AWB_STATE));
        }
    }

    static int convertSceneModeFromLegacy(String mode) {
        if (mode == null) {
            return REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW;
        }
        int index = ArrayUtils.getArrayIndex(sLegacySceneModes, (Object) mode);
        if (index < 0) {
            return UNKNOWN_MODE;
        }
        return sSceneModes[index];
    }

    static String convertSceneModeToLegacy(int mode) {
        if (mode == REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL) {
            return CalendarCache.TIMEZONE_TYPE_AUTO;
        }
        int index = ArrayUtils.getArrayIndex(sSceneModes, mode);
        if (index < 0) {
            return null;
        }
        return sLegacySceneModes[index];
    }

    static int convertEffectModeFromLegacy(String mode) {
        if (mode == null) {
            return REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW;
        }
        int index = ArrayUtils.getArrayIndex(sLegacyEffectMode, (Object) mode);
        if (index < 0) {
            return UNKNOWN_MODE;
        }
        return sEffectModes[index];
    }

    static String convertEffectModeToLegacy(int mode) {
        int index = ArrayUtils.getArrayIndex(sEffectModes, mode);
        if (index < 0) {
            return null;
        }
        return sLegacyEffectMode[index];
    }

    private static int convertAntiBandingMode(String mode) {
        if (mode == null) {
            return UNKNOWN_MODE;
        }
        if (mode.equals(Parameters.FLASH_MODE_OFF)) {
            return REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW;
        }
        if (mode.equals(Parameters.ANTIBANDING_50HZ)) {
            return REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL;
        }
        if (mode.equals(Parameters.ANTIBANDING_60HZ)) {
            return 2;
        }
        if (mode.equals(CalendarCache.TIMEZONE_TYPE_AUTO)) {
            return REQUEST_PIPELINE_MAX_DEPTH_OURS;
        }
        Log.w(TAG, "convertAntiBandingMode - Unknown antibanding mode " + mode);
        return UNKNOWN_MODE;
    }

    static int convertAntiBandingModeOrDefault(String mode) {
        int antiBandingMode = convertAntiBandingMode(mode);
        if (antiBandingMode == UNKNOWN_MODE) {
            return REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW;
        }
        return antiBandingMode;
    }

    private static int[] convertAeFpsRangeToLegacy(Range<Integer> fpsRange) {
        return new int[]{((Integer) fpsRange.getLower()).intValue(), ((Integer) fpsRange.getUpper()).intValue()};
    }

    private static long calculateJpegStallDuration(Size size) {
        return (71 * (((long) size.width) * ((long) size.height))) + 200000000;
    }

    public static void convertRequestMetadata(LegacyRequest request) {
        LegacyRequestMapper.convertRequestMetadata(request);
    }

    public static CameraMetadataNative createRequestTemplate(CameraCharacteristics c, int templateId) {
        if (ArrayUtils.contains(sAllowedTemplates, templateId)) {
            int captureIntent;
            int afMode;
            Object obj;
            CameraMetadataNative m = new CameraMetadataNative();
            m.set(CaptureRequest.CONTROL_AWB_MODE, Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL));
            m.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, Integer.valueOf(REQUEST_PIPELINE_MAX_DEPTH_OURS));
            m.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW));
            m.set(CaptureRequest.CONTROL_AE_LOCK, Boolean.valueOf(LIE_ABOUT_AWB_STATE));
            m.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW));
            m.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW));
            m.set(CaptureRequest.CONTROL_AWB_MODE, Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL));
            m.set(CaptureRequest.CONTROL_AWB_LOCK, Boolean.valueOf(LIE_ABOUT_AWB_STATE));
            Rect activeArray = (Rect) c.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            Object activeRegions = new MeteringRectangle[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL];
            activeRegions[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW] = new MeteringRectangle(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW, REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW, activeArray.width() + UNKNOWN_MODE, activeArray.height() + UNKNOWN_MODE, REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW);
            m.set(CaptureRequest.CONTROL_AE_REGIONS, activeRegions);
            m.set(CaptureRequest.CONTROL_AWB_REGIONS, activeRegions);
            m.set(CaptureRequest.CONTROL_AF_REGIONS, activeRegions);
            switch (templateId) {
                case REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL /*1*/:
                    captureIntent = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL;
                    break;
                case AudioState.ROUTE_BLUETOOTH /*2*/:
                    captureIntent = 2;
                    break;
                case REQUEST_PIPELINE_MAX_DEPTH_OURS /*3*/:
                    captureIntent = REQUEST_PIPELINE_MAX_DEPTH_OURS;
                    break;
                default:
                    throw new AssertionError("Impossible; keep in sync with sAllowedTemplates");
            }
            m.set(CaptureRequest.CONTROL_CAPTURE_INTENT, Integer.valueOf(captureIntent));
            m.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL));
            m.set(CaptureRequest.CONTROL_MODE, Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL));
            Float minimumFocusDistance = (Float) c.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
            if (minimumFocusDistance == null || minimumFocusDistance.floatValue() != LENS_INFO_MINIMUM_FOCUS_DISTANCE_FIXED_FOCUS) {
                afMode = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL;
                if (templateId == REQUEST_PIPELINE_MAX_DEPTH_OURS || templateId == 4) {
                    if (ArrayUtils.contains((int[]) c.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES), (int) REQUEST_PIPELINE_MAX_DEPTH_OURS)) {
                        afMode = REQUEST_PIPELINE_MAX_DEPTH_OURS;
                    }
                } else if (templateId == REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL || templateId == 2) {
                    if (ArrayUtils.contains((int[]) c.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES), 4)) {
                        afMode = 4;
                    }
                }
            } else {
                afMode = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW;
            }
            m.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(afMode));
            Range[] availableFpsRange = (Range[]) c.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            Object bestRange = availableFpsRange[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW];
            int length = availableFpsRange.length;
            for (int i = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW; i < length; i += REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL) {
                Range<Integer> r = availableFpsRange[i];
                if (((Integer) bestRange.getUpper()).intValue() < ((Integer) r.getUpper()).intValue()) {
                    bestRange = r;
                } else if (bestRange.getUpper() == r.getUpper() && ((Integer) bestRange.getLower()).intValue() < ((Integer) r.getLower()).intValue()) {
                    Range<Integer> bestRange2 = r;
                }
            }
            m.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, bestRange);
            m.set(CaptureRequest.CONTROL_SCENE_MODE, Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW));
            m.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW));
            m.set(CaptureRequest.FLASH_MODE, Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW));
            if (templateId == 2) {
                m.set(CaptureRequest.NOISE_REDUCTION_MODE, Integer.valueOf(2));
            } else {
                m.set(CaptureRequest.NOISE_REDUCTION_MODE, Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL));
            }
            if (templateId == 2) {
                m.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, Integer.valueOf(2));
            } else {
                m.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, Integer.valueOf(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL));
            }
            m.set(CaptureRequest.LENS_FOCAL_LENGTH, Float.valueOf(((float[]) c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS))[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW]));
            android.util.Size[] sizes = (android.util.Size[]) c.get(CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES);
            CaptureRequest.Key key = CaptureRequest.JPEG_THUMBNAIL_SIZE;
            if (sizes.length > REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL) {
                obj = sizes[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL];
            } else {
                obj = sizes[REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW];
            }
            m.set(key, obj);
            return m;
        }
        throw new IllegalArgumentException("templateId out of range");
    }

    private static int[] getTagsForKeys(Key<?>[] keys) {
        int[] tags = new int[keys.length];
        for (int i = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW; i < keys.length; i += REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL) {
            tags[i] = keys[i].getNativeKey().getTag();
        }
        return tags;
    }

    private static int[] getTagsForKeys(CaptureRequest.Key<?>[] keys) {
        int[] tags = new int[keys.length];
        for (int i = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW; i < keys.length; i += REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL) {
            tags[i] = keys[i].getNativeKey().getTag();
        }
        return tags;
    }

    private static int[] getTagsForKeys(CaptureResult.Key<?>[] keys) {
        int[] tags = new int[keys.length];
        for (int i = REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW; i < keys.length; i += REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL) {
            tags[i] = keys[i].getNativeKey().getTag();
        }
        return tags;
    }

    static String convertAfModeToLegacy(int mode, List<String> supportedFocusModes) {
        if (supportedFocusModes == null || supportedFocusModes.isEmpty()) {
            Log.w(TAG, "No focus modes supported; API1 bug");
            return null;
        }
        String param = null;
        switch (mode) {
            case REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW /*0*/:
                if (!supportedFocusModes.contains(Parameters.FOCUS_MODE_FIXED)) {
                    param = Parameters.FOCUS_MODE_INFINITY;
                    break;
                }
                param = Parameters.FOCUS_MODE_FIXED;
                break;
            case REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_PROC_STALL /*1*/:
                param = CalendarCache.TIMEZONE_TYPE_AUTO;
                break;
            case AudioState.ROUTE_BLUETOOTH /*2*/:
                param = Parameters.FOCUS_MODE_MACRO;
                break;
            case REQUEST_PIPELINE_MAX_DEPTH_OURS /*3*/:
                param = Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
                break;
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                param = Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
                break;
            case HAL_PIXEL_FORMAT_BGRA_8888 /*5*/:
                param = Parameters.FOCUS_MODE_EDOF;
                break;
        }
        if (!supportedFocusModes.contains(param)) {
            String defaultMode = (String) supportedFocusModes.get(REQUEST_MAX_NUM_OUTPUT_STREAMS_COUNT_RAW);
            Log.w(TAG, String.format("convertAfModeToLegacy - ignoring unsupported mode %d, defaulting to %s", new Object[]{Integer.valueOf(mode), defaultMode}));
            param = defaultMode;
        }
        return param;
    }
}
