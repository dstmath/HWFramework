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
    private static final int[] sAllowedTemplates = new int[]{1, 2, 3};
    private static final int[] sEffectModes = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
    private static final String[] sLegacyEffectMode = new String[]{"none", Parameters.EFFECT_MONO, Parameters.EFFECT_NEGATIVE, Parameters.EFFECT_SOLARIZE, Parameters.EFFECT_SEPIA, Parameters.EFFECT_POSTERIZE, Parameters.EFFECT_WHITEBOARD, Parameters.EFFECT_BLACKBOARD, Parameters.EFFECT_AQUA};
    private static final String[] sLegacySceneModes = new String[]{"auto", "action", Parameters.SCENE_MODE_PORTRAIT, Parameters.SCENE_MODE_LANDSCAPE, Parameters.SCENE_MODE_NIGHT, Parameters.SCENE_MODE_NIGHT_PORTRAIT, Parameters.SCENE_MODE_THEATRE, Parameters.SCENE_MODE_BEACH, Parameters.SCENE_MODE_SNOW, Parameters.SCENE_MODE_SUNSET, Parameters.SCENE_MODE_STEADYPHOTO, Parameters.SCENE_MODE_FIREWORKS, Parameters.SCENE_MODE_SPORTS, Parameters.SCENE_MODE_PARTY, Parameters.SCENE_MODE_CANDLELIGHT, Parameters.SCENE_MODE_BARCODE, Parameters.SCENE_MODE_HDR};
    private static final int[] sSceneModes = new int[]{0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18};

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
        int i2 = 0;
        Key key = CameraCharacteristics.LENS_FACING;
        if (i.facing == 0) {
            i2 = 1;
        }
        m.set(key, Integer.valueOf(i2));
        m.set(CameraCharacteristics.SENSOR_ORIENTATION, Integer.valueOf(i.orientation));
    }

    private static void mapCharacteristicsFromParameters(CameraMetadataNative m, Parameters p) {
        m.set(CameraCharacteristics.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES, new int[]{1, 2});
        mapControlAe(m, p);
        mapControlAf(m, p);
        mapControlAwb(m, p);
        mapControlOther(m, p);
        mapLens(m, p);
        mapFlash(m, p);
        mapJpeg(m, p);
        m.set(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES, new int[]{1, 2});
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
        float jpegAspectRatio = (((float) maxJpegSize.width) * 1.0f) / ((float) maxJpegSize.height);
        while (!previewSizes.isEmpty()) {
            int index = previewSizes.size() - 1;
            Size size = (Size) previewSizes.get(index);
            if (Math.abs(jpegAspectRatio - ((((float) size.width) * 1.0f) / ((float) size.height))) < PREVIEW_ASPECT_RATIO_TOLERANCE) {
                break;
            }
            previewSizes.remove(index);
        }
        if (previewSizes.isEmpty()) {
            Log.w(TAG, "mapScalerStreamConfigs - failed to find any preview size matching JPEG aspect ratio " + jpegAspectRatio);
            previewSizes = p.getSupportedPreviewSizes();
        }
        Collections.sort(previewSizes, Collections.reverseOrder(areaComparator));
        appendStreamConfig(availableStreamConfigs, 34, previewSizes);
        appendStreamConfig(availableStreamConfigs, 35, previewSizes);
        for (Integer intValue : p.getSupportedPreviewFormats()) {
            int format = intValue.intValue();
            if (ImageFormat.isPublicFormat(format) && format != 17) {
                appendStreamConfig(availableStreamConfigs, format, previewSizes);
            }
        }
        appendStreamConfig(availableStreamConfigs, 33, p.getSupportedPictureSizes());
        m.set(CameraCharacteristics.SCALER_AVAILABLE_STREAM_CONFIGURATIONS, (StreamConfiguration[]) availableStreamConfigs.toArray(new StreamConfiguration[0]));
        m.set(CameraCharacteristics.SCALER_AVAILABLE_MIN_FRAME_DURATIONS, new StreamConfigurationDuration[0]);
        Object jpegStalls = new StreamConfigurationDuration[jpegSizes.size()];
        int i = 0;
        long longestStallDuration = -1;
        for (Size s : jpegSizes) {
            long stallDuration = calculateJpegStallDuration(s);
            int i2 = i + 1;
            jpegStalls[i] = new StreamConfigurationDuration(33, s.width, s.height, stallDuration);
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
            m.set(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES, new int[0]);
        } else {
            int[] modes = new int[antiBandingModes.size()];
            int j = 0;
            for (String mode : antiBandingModes) {
                int j2 = j + 1;
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
        int i = 0;
        for (int[] r : fpsRanges) {
            int i2 = i + 1;
            ranges[i] = Range.create(Integer.valueOf((int) Math.floor(((double) r[0]) / 1000.0d)), Integer.valueOf((int) Math.ceil(((double) r[1]) / 1000.0d)));
            i = i2;
        }
        m.set(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES, ranges);
        Object aeAvail = ArrayUtils.convertStringListToIntArray(p.getSupportedFlashModes(), new String[]{"off", "auto", Parameters.FLASH_MODE_ON, Parameters.FLASH_MODE_RED_EYE, Parameters.FLASH_MODE_TORCH}, new int[]{1, 2, 3, 4});
        if (aeAvail == null || aeAvail.length == 0) {
            aeAvail = new int[]{1};
        }
        m.set(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES, aeAvail);
        int min = p.getMinExposureCompensation();
        int max = p.getMaxExposureCompensation();
        m.set(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE, (Object) Range.create(Integer.valueOf(min), Integer.valueOf(max)));
        float step = p.getExposureCompensationStep();
        m.set(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP, (Object) ParamsUtils.createRational(step));
        boolean aeLockAvailable = p.isAutoExposureLockSupported();
        m.set(CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE, (Object) Boolean.valueOf(aeLockAvailable));
    }

    private static void mapControlAf(CameraMetadataNative m, Parameters p) {
        List<Integer> afAvail = ArrayUtils.convertStringListToIntList(p.getSupportedFocusModes(), new String[]{"auto", Parameters.FOCUS_MODE_CONTINUOUS_PICTURE, Parameters.FOCUS_MODE_CONTINUOUS_VIDEO, Parameters.FOCUS_MODE_EDOF, Parameters.FOCUS_MODE_INFINITY, Parameters.FOCUS_MODE_MACRO, Parameters.FOCUS_MODE_FIXED}, new int[]{1, 4, 3, 5, 0, 2, 0});
        if (afAvail == null || afAvail.size() == 0) {
            Log.w(TAG, "No AF modes supported (HAL bug); defaulting to AF_MODE_OFF only");
            afAvail = new ArrayList(1);
            afAvail.add(Integer.valueOf(0));
        }
        m.set(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES, ArrayUtils.toIntArray(afAvail));
    }

    private static void mapControlAwb(CameraMetadataNative m, Parameters p) {
        List<Integer> awbAvail = ArrayUtils.convertStringListToIntList(p.getSupportedWhiteBalance(), new String[]{"auto", Parameters.WHITE_BALANCE_INCANDESCENT, Parameters.WHITE_BALANCE_FLUORESCENT, Parameters.WHITE_BALANCE_WARM_FLUORESCENT, Parameters.WHITE_BALANCE_DAYLIGHT, Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT, Parameters.WHITE_BALANCE_TWILIGHT, Parameters.WHITE_BALANCE_SHADE}, new int[]{1, 2, 3, 4, 5, 6, 7, 8});
        if (awbAvail == null || awbAvail.size() == 0) {
            Log.w(TAG, "No AWB modes supported (HAL bug); defaulting to AWB_MODE_AUTO only");
            awbAvail = new ArrayList(1);
            awbAvail.add(Integer.valueOf(1));
        }
        m.set(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES, ArrayUtils.toIntArray(awbAvail));
        m.set(CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE, Boolean.valueOf(p.isAutoWhiteBalanceLockSupported()));
    }

    private static void mapControlOther(CameraMetadataNative m, Parameters p) {
        Object supportedEffectModes;
        m.set(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES, p.isVideoStabilizationSupported() ? new int[]{0, 1} : new int[]{0});
        m.set(CameraCharacteristics.CONTROL_MAX_REGIONS, new int[]{p.getMaxNumMeteringAreas(), 0, p.getMaxNumFocusAreas()});
        List<String> effectModes = p.getSupportedColorEffects();
        if (effectModes == null) {
            supportedEffectModes = new int[0];
        } else {
            supportedEffectModes = ArrayUtils.convertStringListToIntArray(effectModes, sLegacyEffectMode, sEffectModes);
        }
        m.set(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS, supportedEffectModes);
        int maxNumDetectedFaces = p.getMaxNumDetectedFaces();
        List<String> sceneModes = p.getSupportedSceneModes();
        List supportedSceneModes = ArrayUtils.convertStringListToIntList(sceneModes, sLegacySceneModes, sSceneModes);
        if (sceneModes != null && sceneModes.size() == 1 && ((String) sceneModes.get(0)).equals("auto")) {
            supportedSceneModes = null;
        }
        boolean sceneModeSupported = true;
        if (supportedSceneModes == null && maxNumDetectedFaces == 0) {
            sceneModeSupported = false;
        }
        if (sceneModeSupported) {
            if (supportedSceneModes == null) {
                supportedSceneModes = new ArrayList();
            }
            if (maxNumDetectedFaces > 0) {
                supportedSceneModes.add(Integer.valueOf(1));
            }
            if (supportedSceneModes.contains(Integer.valueOf(0))) {
                do {
                } while (supportedSceneModes.remove(new Integer(0)));
            }
            m.set(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES, ArrayUtils.toIntArray(supportedSceneModes));
        } else {
            m.set(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES, new int[]{null});
        }
        m.set(CameraCharacteristics.CONTROL_AVAILABLE_MODES, sceneModeSupported ? new int[]{1, 2} : new int[]{1});
    }

    private static void mapLens(CameraMetadataNative m, Parameters p) {
        if (Parameters.FOCUS_MODE_FIXED.equals(p.getFocusMode())) {
            m.set(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE, Float.valueOf(0.0f));
        }
        m.set(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS, new float[]{p.getFocalLength()});
    }

    private static void mapFlash(CameraMetadataNative m, Parameters p) {
        boolean flashAvailable = false;
        List<String> supportedFlashModes = p.getSupportedFlashModes();
        if (supportedFlashModes != null) {
            flashAvailable = ListUtils.listElementsEqualTo(supportedFlashModes, "off") ^ 1;
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
        m.set(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES, new int[]{0});
        List<Key<?>> characteristicsKeys = new ArrayList(Arrays.asList(new Key[]{CameraCharacteristics.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES, CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES, CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES, CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES, CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE, CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP, CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE, CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES, CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS, CameraCharacteristics.CONTROL_AVAILABLE_MODES, CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES, CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES, CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES, CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE, CameraCharacteristics.CONTROL_MAX_REGIONS, CameraCharacteristics.FLASH_INFO_AVAILABLE, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL, CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES, CameraCharacteristics.LENS_FACING, CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS, CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES, CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES, CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_STREAMS, CameraCharacteristics.REQUEST_PARTIAL_RESULT_COUNT, CameraCharacteristics.REQUEST_PIPELINE_MAX_DEPTH, CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM, CameraCharacteristics.SCALER_CROPPING_TYPE, CameraCharacteristics.SENSOR_AVAILABLE_TEST_PATTERN_MODES, CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE, CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE, CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE, CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE, CameraCharacteristics.SENSOR_ORIENTATION, CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES, CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT, CameraCharacteristics.SYNC_MAX_LATENCY}));
        if (m.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE) != null) {
            characteristicsKeys.add(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        }
        m.set(CameraCharacteristics.REQUEST_AVAILABLE_CHARACTERISTICS_KEYS, getTagsForKeys((Key[]) characteristicsKeys.toArray(new Key[0])));
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
        m.set(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_STREAMS, new int[]{0, 3, 1});
        m.set(CameraCharacteristics.REQUEST_MAX_NUM_INPUT_STREAMS, Integer.valueOf(0));
        m.set(CameraCharacteristics.REQUEST_PARTIAL_RESULT_COUNT, Integer.valueOf(1));
        m.set(CameraCharacteristics.REQUEST_PIPELINE_MAX_DEPTH, Byte.valueOf((byte) 6));
    }

    private static void mapScaler(CameraMetadataNative m, Parameters p) {
        m.set(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM, Float.valueOf(ParameterUtils.getMaxZoomRatio(p)));
        m.set(CameraCharacteristics.SCALER_CROPPING_TYPE, Integer.valueOf(0));
    }

    private static void mapSensor(CameraMetadataNative m, Parameters p) {
        Object largestJpegSize = ParameterUtils.getLargestSupportedJpegSizeByArea(p);
        m.set(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE, ParamsUtils.createRect((android.util.Size) largestJpegSize));
        m.set(CameraCharacteristics.SENSOR_AVAILABLE_TEST_PATTERN_MODES, new int[]{null});
        m.set(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE, largestJpegSize);
        float focalLength = p.getFocalLength();
        float width = (float) Math.abs(((double) (2.0f * focalLength)) * Math.tan(((((double) p.getHorizontalViewAngle()) * 3.141592653589793d) / 180.0d) / 2.0d));
        m.set(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE, new SizeF(width, (float) Math.abs(((double) (2.0f * focalLength)) * Math.tan(((((double) p.getVerticalViewAngle()) * 3.141592653589793d) / 180.0d) / 2.0d))));
        m.set(CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE, Integer.valueOf(0));
    }

    private static void mapStatistics(CameraMetadataNative m, Parameters p) {
        m.set(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES, p.getMaxNumDetectedFaces() > 0 ? new int[]{0, 1} : new int[]{0});
        m.set(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT, Integer.valueOf(p.getMaxNumDetectedFaces()));
    }

    private static void mapSync(CameraMetadataNative m, Parameters p) {
        m.set(CameraCharacteristics.SYNC_MAX_LATENCY, Integer.valueOf(-1));
    }

    private static void appendStreamConfig(ArrayList<StreamConfiguration> configs, int format, List<Size> sizes) {
        for (Size size : sizes) {
            configs.add(new StreamConfiguration(format, size.width, size.height, false));
        }
    }

    static int convertSceneModeFromLegacy(String mode) {
        if (mode == null) {
            return 0;
        }
        int index = ArrayUtils.getArrayIndex(sLegacySceneModes, (Object) mode);
        if (index < 0) {
            return -1;
        }
        return sSceneModes[index];
    }

    static String convertSceneModeToLegacy(int mode) {
        if (mode == 1) {
            return "auto";
        }
        int index = ArrayUtils.getArrayIndex(sSceneModes, mode);
        if (index < 0) {
            return null;
        }
        return sLegacySceneModes[index];
    }

    static int convertEffectModeFromLegacy(String mode) {
        if (mode == null) {
            return 0;
        }
        int index = ArrayUtils.getArrayIndex(sLegacyEffectMode, (Object) mode);
        if (index < 0) {
            return -1;
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
            return -1;
        }
        if (mode.equals("off")) {
            return 0;
        }
        if (mode.equals(Parameters.ANTIBANDING_50HZ)) {
            return 1;
        }
        if (mode.equals(Parameters.ANTIBANDING_60HZ)) {
            return 2;
        }
        if (mode.equals("auto")) {
            return 3;
        }
        Log.w(TAG, "convertAntiBandingMode - Unknown antibanding mode " + mode);
        return -1;
    }

    static int convertAntiBandingModeOrDefault(String mode) {
        int antiBandingMode = convertAntiBandingMode(mode);
        if (antiBandingMode == -1) {
            return 0;
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
            m.set(CaptureRequest.CONTROL_AWB_MODE, Integer.valueOf(1));
            m.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, Integer.valueOf(3));
            m.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, Integer.valueOf(0));
            m.set(CaptureRequest.CONTROL_AE_LOCK, Boolean.valueOf(false));
            m.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, Integer.valueOf(0));
            m.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(0));
            m.set(CaptureRequest.CONTROL_AWB_MODE, Integer.valueOf(1));
            m.set(CaptureRequest.CONTROL_AWB_LOCK, Boolean.valueOf(false));
            Rect activeArray = (Rect) c.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            Object activeRegions = new MeteringRectangle[]{new MeteringRectangle(0, 0, activeArray.width() - 1, activeArray.height() - 1, 0)};
            m.set(CaptureRequest.CONTROL_AE_REGIONS, activeRegions);
            m.set(CaptureRequest.CONTROL_AWB_REGIONS, activeRegions);
            m.set(CaptureRequest.CONTROL_AF_REGIONS, activeRegions);
            switch (templateId) {
                case 1:
                    captureIntent = 1;
                    break;
                case 2:
                    captureIntent = 2;
                    break;
                case 3:
                    captureIntent = 3;
                    break;
                default:
                    throw new AssertionError("Impossible; keep in sync with sAllowedTemplates");
            }
            m.set(CaptureRequest.CONTROL_CAPTURE_INTENT, Integer.valueOf(captureIntent));
            m.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
            m.set(CaptureRequest.CONTROL_MODE, Integer.valueOf(1));
            Float minimumFocusDistance = (Float) c.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
            if (minimumFocusDistance == null || minimumFocusDistance.floatValue() != 0.0f) {
                afMode = 1;
                if (templateId == 3 || templateId == 4) {
                    if (ArrayUtils.contains((int[]) c.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES), 3)) {
                        afMode = 3;
                    }
                } else if (templateId == 1 || templateId == 2) {
                    if (ArrayUtils.contains((int[]) c.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES), 4)) {
                        afMode = 4;
                    }
                }
            } else {
                afMode = 0;
            }
            m.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(afMode));
            Range[] availableFpsRange = (Range[]) c.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            Object bestRange = availableFpsRange[0];
            for (Range<Integer> r : availableFpsRange) {
                if (((Integer) bestRange.getUpper()).intValue() < ((Integer) r.getUpper()).intValue()) {
                    bestRange = r;
                } else if (bestRange.getUpper() == r.getUpper() && ((Integer) bestRange.getLower()).intValue() < ((Integer) r.getLower()).intValue()) {
                    Range<Integer> bestRange2 = r;
                }
            }
            m.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, bestRange2);
            m.set(CaptureRequest.CONTROL_SCENE_MODE, Integer.valueOf(0));
            m.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, Integer.valueOf(0));
            m.set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
            if (templateId == 2) {
                m.set(CaptureRequest.NOISE_REDUCTION_MODE, Integer.valueOf(2));
            } else {
                m.set(CaptureRequest.NOISE_REDUCTION_MODE, Integer.valueOf(1));
            }
            if (templateId == 2) {
                m.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, Integer.valueOf(2));
            } else {
                m.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, Integer.valueOf(1));
            }
            m.set(CaptureRequest.LENS_FOCAL_LENGTH, Float.valueOf(((float[]) c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS))[0]));
            android.util.Size[] sizes = (android.util.Size[]) c.get(CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES);
            CaptureRequest.Key key = CaptureRequest.JPEG_THUMBNAIL_SIZE;
            if (sizes.length > 1) {
                obj = sizes[1];
            } else {
                obj = sizes[0];
            }
            m.set(key, obj);
            return m;
        }
        throw new IllegalArgumentException("templateId out of range");
    }

    private static int[] getTagsForKeys(Key<?>[] keys) {
        int[] tags = new int[keys.length];
        for (int i = 0; i < keys.length; i++) {
            tags[i] = keys[i].getNativeKey().getTag();
        }
        return tags;
    }

    private static int[] getTagsForKeys(CaptureRequest.Key<?>[] keys) {
        int[] tags = new int[keys.length];
        for (int i = 0; i < keys.length; i++) {
            tags[i] = keys[i].getNativeKey().getTag();
        }
        return tags;
    }

    private static int[] getTagsForKeys(CaptureResult.Key<?>[] keys) {
        int[] tags = new int[keys.length];
        for (int i = 0; i < keys.length; i++) {
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
            case 0:
                if (!supportedFocusModes.contains(Parameters.FOCUS_MODE_FIXED)) {
                    param = Parameters.FOCUS_MODE_INFINITY;
                    break;
                }
                param = Parameters.FOCUS_MODE_FIXED;
                break;
            case 1:
                param = "auto";
                break;
            case 2:
                param = Parameters.FOCUS_MODE_MACRO;
                break;
            case 3:
                param = Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
                break;
            case 4:
                param = Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
                break;
            case 5:
                param = Parameters.FOCUS_MODE_EDOF;
                break;
        }
        if (!supportedFocusModes.contains(param)) {
            String defaultMode = (String) supportedFocusModes.get(0);
            Log.w(TAG, String.format("convertAfModeToLegacy - ignoring unsupported mode %d, defaulting to %s", new Object[]{Integer.valueOf(mode), defaultMode}));
            param = defaultMode;
        }
        return param;
    }
}
