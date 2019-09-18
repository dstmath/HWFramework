package android.hardware.camera2.legacy;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.CameraInfo;
import android.hardware.camera2.CameraCharacteristics;
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
import android.util.Size;
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
    private static final int[] sAllowedTemplates = {1, 2, 3};
    private static final int[] sEffectModes = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    private static final String[] sLegacyEffectMode = {"none", Camera.Parameters.EFFECT_MONO, Camera.Parameters.EFFECT_NEGATIVE, Camera.Parameters.EFFECT_SOLARIZE, Camera.Parameters.EFFECT_SEPIA, Camera.Parameters.EFFECT_POSTERIZE, Camera.Parameters.EFFECT_WHITEBOARD, Camera.Parameters.EFFECT_BLACKBOARD, Camera.Parameters.EFFECT_AQUA};
    private static final String[] sLegacySceneModes = {"auto", "action", Camera.Parameters.SCENE_MODE_PORTRAIT, Camera.Parameters.SCENE_MODE_LANDSCAPE, Camera.Parameters.SCENE_MODE_NIGHT, Camera.Parameters.SCENE_MODE_NIGHT_PORTRAIT, Camera.Parameters.SCENE_MODE_THEATRE, Camera.Parameters.SCENE_MODE_BEACH, Camera.Parameters.SCENE_MODE_SNOW, Camera.Parameters.SCENE_MODE_SUNSET, Camera.Parameters.SCENE_MODE_STEADYPHOTO, Camera.Parameters.SCENE_MODE_FIREWORKS, Camera.Parameters.SCENE_MODE_SPORTS, Camera.Parameters.SCENE_MODE_PARTY, Camera.Parameters.SCENE_MODE_CANDLELIGHT, Camera.Parameters.SCENE_MODE_BARCODE, Camera.Parameters.SCENE_MODE_HDR};
    private static final int[] sSceneModes = {0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18};

    public static CameraCharacteristics createCharacteristics(Camera.Parameters parameters, Camera.CameraInfo info) {
        Preconditions.checkNotNull(parameters, "parameters must not be null");
        Preconditions.checkNotNull(info, "info must not be null");
        String paramStr = parameters.flatten();
        CameraInfo outerInfo = new CameraInfo();
        outerInfo.info = info;
        return createCharacteristics(paramStr, outerInfo);
    }

    public static CameraCharacteristics createCharacteristics(String parameters, CameraInfo info) {
        Preconditions.checkNotNull(parameters, "parameters must not be null");
        Preconditions.checkNotNull(info, "info must not be null");
        Preconditions.checkNotNull(info.info, "info.info must not be null");
        CameraMetadataNative m = new CameraMetadataNative();
        mapCharacteristicsFromInfo(m, info.info);
        Camera.Parameters params = Camera.getEmptyParameters();
        params.unflatten(parameters);
        mapCharacteristicsFromParameters(m, params);
        return new CameraCharacteristics(m);
    }

    private static void mapCharacteristicsFromInfo(CameraMetadataNative m, Camera.CameraInfo i) {
        m.set(CameraCharacteristics.LENS_FACING, Integer.valueOf(i.facing == 0 ? 1 : 0));
        m.set(CameraCharacteristics.SENSOR_ORIENTATION, Integer.valueOf(i.orientation));
    }

    private static void mapCharacteristicsFromParameters(CameraMetadataNative m, Camera.Parameters p) {
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
        m.set(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL, 2);
        mapScalerStreamConfigs(m, p);
        mapRequest(m, p);
    }

    private static void mapScalerStreamConfigs(CameraMetadataNative m, Camera.Parameters p) {
        CameraMetadataNative cameraMetadataNative = m;
        ArrayList<StreamConfiguration> availableStreamConfigs = new ArrayList<>();
        List<Camera.Size> previewSizes = p.getSupportedPreviewSizes();
        List<Camera.Size> jpegSizes = p.getSupportedPictureSizes();
        SizeAreaComparator areaComparator = new SizeAreaComparator();
        Collections.sort(previewSizes, areaComparator);
        Camera.Size maxJpegSize = SizeAreaComparator.findLargestByArea(jpegSizes);
        float jpegAspectRatio = (((float) maxJpegSize.width) * 1.0f) / ((float) maxJpegSize.height);
        while (!previewSizes.isEmpty()) {
            int index = previewSizes.size() - 1;
            Camera.Size size = previewSizes.get(index);
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
        cameraMetadataNative.set(CameraCharacteristics.SCALER_AVAILABLE_STREAM_CONFIGURATIONS, (StreamConfiguration[]) availableStreamConfigs.toArray(new StreamConfiguration[0]));
        cameraMetadataNative.set(CameraCharacteristics.SCALER_AVAILABLE_MIN_FRAME_DURATIONS, new StreamConfigurationDuration[0]);
        StreamConfigurationDuration[] jpegStalls = new StreamConfigurationDuration[jpegSizes.size()];
        int i = 0;
        long longestStallDuration = -1;
        for (Camera.Size s : jpegSizes) {
            long stallDuration = calculateJpegStallDuration(s);
            int i2 = i + 1;
            StreamConfigurationDuration streamConfigurationDuration = new StreamConfigurationDuration(33, s.width, s.height, stallDuration);
            jpegStalls[i] = streamConfigurationDuration;
            if (longestStallDuration < stallDuration) {
                longestStallDuration = stallDuration;
            }
            i = i2;
        }
        cameraMetadataNative.set(CameraCharacteristics.SCALER_AVAILABLE_STALL_DURATIONS, jpegStalls);
        cameraMetadataNative.set(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION, Long.valueOf(longestStallDuration));
    }

    private static void mapControlAe(CameraMetadataNative m, Camera.Parameters p) {
        CameraMetadataNative cameraMetadataNative = m;
        List<String> antiBandingModes = p.getSupportedAntibanding();
        char c = 0;
        if (antiBandingModes == null || antiBandingModes.size() <= 0) {
            cameraMetadataNative.set(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES, new int[0]);
        } else {
            int[] modes = new int[antiBandingModes.size()];
            int j = 0;
            for (String mode : antiBandingModes) {
                modes[j] = convertAntiBandingMode(mode);
                j++;
            }
            cameraMetadataNative.set(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES, Arrays.copyOf(modes, j));
        }
        List<int[]> fpsRanges = p.getSupportedPreviewFpsRange();
        if (fpsRanges != null) {
            int rangesSize = fpsRanges.size();
            if (rangesSize > 0) {
                Range<Integer>[] ranges = new Range[rangesSize];
                int i = 0;
                for (int[] r : fpsRanges) {
                    ranges[i] = Range.create(Integer.valueOf((int) Math.floor(((double) r[c]) / 1000.0d)), Integer.valueOf((int) Math.ceil(((double) r[1]) / 1000.0d)));
                    i++;
                    fpsRanges = fpsRanges;
                    c = 0;
                }
                cameraMetadataNative.set(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES, ranges);
                int[] aeAvail = ArrayUtils.convertStringListToIntArray(p.getSupportedFlashModes(), new String[]{"off", "auto", Camera.Parameters.FLASH_MODE_ON, Camera.Parameters.FLASH_MODE_RED_EYE, Camera.Parameters.FLASH_MODE_TORCH}, new int[]{1, 2, 3, 4});
                if (aeAvail == null || aeAvail.length == 0) {
                    aeAvail = new int[]{1};
                }
                cameraMetadataNative.set(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES, aeAvail);
                cameraMetadataNative.set(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE, Range.create(Integer.valueOf(p.getMinExposureCompensation()), Integer.valueOf(p.getMaxExposureCompensation())));
                cameraMetadataNative.set(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP, ParamsUtils.createRational(p.getExposureCompensationStep()));
                cameraMetadataNative.set(CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE, Boolean.valueOf(p.isAutoExposureLockSupported()));
                return;
            }
            List<int[]> list = fpsRanges;
            throw new AssertionError("At least one FPS range must be supported.");
        }
        throw new AssertionError("Supported FPS ranges cannot be null.");
    }

    private static void mapControlAf(CameraMetadataNative m, Camera.Parameters p) {
        List<Integer> afAvail = ArrayUtils.convertStringListToIntList(p.getSupportedFocusModes(), new String[]{"auto", Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE, Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO, Camera.Parameters.FOCUS_MODE_EDOF, Camera.Parameters.FOCUS_MODE_INFINITY, Camera.Parameters.FOCUS_MODE_MACRO, Camera.Parameters.FOCUS_MODE_FIXED}, new int[]{1, 4, 3, 5, 0, 2, 0});
        if (afAvail == null || afAvail.size() == 0) {
            Log.w(TAG, "No AF modes supported (HAL bug); defaulting to AF_MODE_OFF only");
            afAvail = new ArrayList<>(1);
            afAvail.add(0);
        }
        m.set(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES, ArrayUtils.toIntArray(afAvail));
    }

    private static void mapControlAwb(CameraMetadataNative m, Camera.Parameters p) {
        List<Integer> awbAvail = ArrayUtils.convertStringListToIntList(p.getSupportedWhiteBalance(), new String[]{"auto", Camera.Parameters.WHITE_BALANCE_INCANDESCENT, Camera.Parameters.WHITE_BALANCE_FLUORESCENT, Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT, Camera.Parameters.WHITE_BALANCE_DAYLIGHT, Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT, Camera.Parameters.WHITE_BALANCE_TWILIGHT, Camera.Parameters.WHITE_BALANCE_SHADE}, new int[]{1, 2, 3, 4, 5, 6, 7, 8});
        if (awbAvail == null || awbAvail.size() == 0) {
            Log.w(TAG, "No AWB modes supported (HAL bug); defaulting to AWB_MODE_AUTO only");
            awbAvail = new ArrayList<>(1);
            awbAvail.add(1);
        }
        m.set(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES, ArrayUtils.toIntArray(awbAvail));
        m.set(CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE, Boolean.valueOf(p.isAutoWhiteBalanceLockSupported()));
    }

    private static void mapControlOther(CameraMetadataNative m, Camera.Parameters p) {
        int[] supportedEffectModes;
        CameraMetadataNative cameraMetadataNative = m;
        cameraMetadataNative.set(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES, p.isVideoStabilizationSupported() ? new int[]{0, 1} : new int[]{0});
        cameraMetadataNative.set(CameraCharacteristics.CONTROL_MAX_REGIONS, new int[]{p.getMaxNumMeteringAreas(), 0, p.getMaxNumFocusAreas()});
        List<String> effectModes = p.getSupportedColorEffects();
        if (effectModes == null) {
            supportedEffectModes = new int[0];
        } else {
            supportedEffectModes = ArrayUtils.convertStringListToIntArray(effectModes, sLegacyEffectMode, sEffectModes);
        }
        cameraMetadataNative.set(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS, supportedEffectModes);
        int maxNumDetectedFaces = p.getMaxNumDetectedFaces();
        List<String> sceneModes = p.getSupportedSceneModes();
        List<Integer> supportedSceneModes = ArrayUtils.convertStringListToIntList(sceneModes, sLegacySceneModes, sSceneModes);
        if (sceneModes != null && sceneModes.size() == 1 && sceneModes.get(0).equals("auto")) {
            supportedSceneModes = null;
        }
        boolean sceneModeSupported = true;
        if (supportedSceneModes == null && maxNumDetectedFaces == 0) {
            sceneModeSupported = false;
        }
        if (sceneModeSupported) {
            if (supportedSceneModes == null) {
                supportedSceneModes = new ArrayList<>();
            }
            if (maxNumDetectedFaces > 0) {
                supportedSceneModes.add(1);
            }
            if (supportedSceneModes.contains(0)) {
                do {
                } while (supportedSceneModes.remove(new Integer(0)));
            }
            cameraMetadataNative.set(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES, ArrayUtils.toIntArray(supportedSceneModes));
        } else {
            cameraMetadataNative.set(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES, new int[]{0});
        }
        cameraMetadataNative.set(CameraCharacteristics.CONTROL_AVAILABLE_MODES, sceneModeSupported ? new int[]{1, 2} : new int[]{1});
    }

    private static void mapLens(CameraMetadataNative m, Camera.Parameters p) {
        if (Camera.Parameters.FOCUS_MODE_FIXED.equals(p.getFocusMode())) {
            m.set(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE, Float.valueOf(0.0f));
        }
        m.set(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS, new float[]{p.getFocalLength()});
    }

    private static void mapFlash(CameraMetadataNative m, Camera.Parameters p) {
        boolean flashAvailable = false;
        List<String> supportedFlashModes = p.getSupportedFlashModes();
        if (supportedFlashModes != null) {
            flashAvailable = !ListUtils.listElementsEqualTo(supportedFlashModes, "off");
        }
        m.set(CameraCharacteristics.FLASH_INFO_AVAILABLE, Boolean.valueOf(flashAvailable));
    }

    private static void mapJpeg(CameraMetadataNative m, Camera.Parameters p) {
        List<Camera.Size> thumbnailSizes = p.getSupportedJpegThumbnailSizes();
        if (thumbnailSizes != null) {
            Size[] sizes = ParameterUtils.convertSizeListToArray(thumbnailSizes);
            Arrays.sort(sizes, new SizeAreaComparator());
            m.set(CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES, sizes);
        }
    }

    private static void mapRequest(CameraMetadataNative m, Camera.Parameters p) {
        CameraMetadataNative cameraMetadataNative = m;
        cameraMetadataNative.set(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES, new int[]{0});
        List<CameraCharacteristics.Key<?>> characteristicsKeys = new ArrayList<>(Arrays.asList(new CameraCharacteristics.Key[]{CameraCharacteristics.COLOR_CORRECTION_AVAILABLE_ABERRATION_MODES, CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES, CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES, CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES, CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE, CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP, CameraCharacteristics.CONTROL_AE_LOCK_AVAILABLE, CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES, CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS, CameraCharacteristics.CONTROL_AVAILABLE_MODES, CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES, CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES, CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES, CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE, CameraCharacteristics.CONTROL_MAX_REGIONS, CameraCharacteristics.FLASH_INFO_AVAILABLE, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL, CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES, CameraCharacteristics.LENS_FACING, CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS, CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES, CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES, CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_STREAMS, CameraCharacteristics.REQUEST_PARTIAL_RESULT_COUNT, CameraCharacteristics.REQUEST_PIPELINE_MAX_DEPTH, CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM, CameraCharacteristics.SCALER_CROPPING_TYPE, CameraCharacteristics.SENSOR_AVAILABLE_TEST_PATTERN_MODES, CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE, CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE, CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE, CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE, CameraCharacteristics.SENSOR_ORIENTATION, CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES, CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT, CameraCharacteristics.SYNC_MAX_LATENCY}));
        if (cameraMetadataNative.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE) != null) {
            characteristicsKeys.add(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        }
        cameraMetadataNative.set(CameraCharacteristics.REQUEST_AVAILABLE_CHARACTERISTICS_KEYS, getTagsForKeys((CameraCharacteristics.Key<?>[]) (CameraCharacteristics.Key[]) characteristicsKeys.toArray(new CameraCharacteristics.Key[0])));
        ArrayList<CaptureRequest.Key<?>> availableKeys = new ArrayList<>(Arrays.asList(new CaptureRequest.Key[]{CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, CaptureRequest.CONTROL_AE_LOCK, CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AWB_LOCK, CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_CAPTURE_INTENT, CaptureRequest.CONTROL_EFFECT_MODE, CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.FLASH_MODE, CaptureRequest.JPEG_GPS_COORDINATES, CaptureRequest.JPEG_GPS_PROCESSING_METHOD, CaptureRequest.JPEG_GPS_TIMESTAMP, CaptureRequest.JPEG_ORIENTATION, CaptureRequest.JPEG_QUALITY, CaptureRequest.JPEG_THUMBNAIL_QUALITY, CaptureRequest.JPEG_THUMBNAIL_SIZE, CaptureRequest.LENS_FOCAL_LENGTH, CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.SCALER_CROP_REGION, CaptureRequest.STATISTICS_FACE_DETECT_MODE}));
        if (p.getMaxNumMeteringAreas() > 0) {
            availableKeys.add(CaptureRequest.CONTROL_AE_REGIONS);
        }
        if (p.getMaxNumFocusAreas() > 0) {
            availableKeys.add(CaptureRequest.CONTROL_AF_REGIONS);
        }
        CaptureRequest.Key<?>[] availableRequestKeys = new CaptureRequest.Key[availableKeys.size()];
        availableKeys.toArray(availableRequestKeys);
        cameraMetadataNative.set(CameraCharacteristics.REQUEST_AVAILABLE_REQUEST_KEYS, getTagsForKeys(availableRequestKeys));
        List<CaptureResult.Key<?>> availableKeys2 = new ArrayList<>(Arrays.asList(new CaptureResult.Key[]{CaptureResult.COLOR_CORRECTION_ABERRATION_MODE, CaptureResult.CONTROL_AE_ANTIBANDING_MODE, CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION, CaptureResult.CONTROL_AE_LOCK, CaptureResult.CONTROL_AE_MODE, CaptureResult.CONTROL_AF_MODE, CaptureResult.CONTROL_AF_STATE, CaptureResult.CONTROL_AWB_MODE, CaptureResult.CONTROL_AWB_LOCK, CaptureResult.CONTROL_MODE, CaptureResult.FLASH_MODE, CaptureResult.JPEG_GPS_COORDINATES, CaptureResult.JPEG_GPS_PROCESSING_METHOD, CaptureResult.JPEG_GPS_TIMESTAMP, CaptureResult.JPEG_ORIENTATION, CaptureResult.JPEG_QUALITY, CaptureResult.JPEG_THUMBNAIL_QUALITY, CaptureResult.LENS_FOCAL_LENGTH, CaptureResult.NOISE_REDUCTION_MODE, CaptureResult.REQUEST_PIPELINE_DEPTH, CaptureResult.SCALER_CROP_REGION, CaptureResult.SENSOR_TIMESTAMP, CaptureResult.STATISTICS_FACE_DETECT_MODE}));
        if (p.getMaxNumMeteringAreas() > 0) {
            availableKeys2.add(CaptureResult.CONTROL_AE_REGIONS);
        }
        if (p.getMaxNumFocusAreas() > 0) {
            availableKeys2.add(CaptureResult.CONTROL_AF_REGIONS);
        }
        CaptureResult.Key<?>[] availableResultKeys = new CaptureResult.Key[availableKeys2.size()];
        availableKeys2.toArray(availableResultKeys);
        cameraMetadataNative.set(CameraCharacteristics.REQUEST_AVAILABLE_RESULT_KEYS, getTagsForKeys(availableResultKeys));
        cameraMetadataNative.set(CameraCharacteristics.REQUEST_MAX_NUM_OUTPUT_STREAMS, new int[]{0, 3, 1});
        cameraMetadataNative.set(CameraCharacteristics.REQUEST_MAX_NUM_INPUT_STREAMS, 0);
        cameraMetadataNative.set(CameraCharacteristics.REQUEST_PARTIAL_RESULT_COUNT, 1);
        cameraMetadataNative.set(CameraCharacteristics.REQUEST_PIPELINE_MAX_DEPTH, (byte) 6);
    }

    private static void mapScaler(CameraMetadataNative m, Camera.Parameters p) {
        m.set(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM, Float.valueOf(ParameterUtils.getMaxZoomRatio(p)));
        m.set(CameraCharacteristics.SCALER_CROPPING_TYPE, 0);
    }

    private static void mapSensor(CameraMetadataNative m, Camera.Parameters p) {
        Size largestJpegSize = ParameterUtils.getLargestSupportedJpegSizeByArea(p);
        m.set(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE, ParamsUtils.createRect(largestJpegSize));
        m.set(CameraCharacteristics.SENSOR_AVAILABLE_TEST_PATTERN_MODES, new int[]{0});
        m.set(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE, largestJpegSize);
        float focalLength = p.getFocalLength();
        m.set(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE, new SizeF((float) Math.abs(((double) (2.0f * focalLength)) * Math.tan(((((double) p.getHorizontalViewAngle()) * 3.141592653589793d) / 180.0d) / 2.0d)), (float) Math.abs(((double) (2.0f * focalLength)) * Math.tan(((((double) p.getVerticalViewAngle()) * 3.141592653589793d) / 180.0d) / 2.0d))));
        m.set(CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE, 0);
    }

    private static void mapStatistics(CameraMetadataNative m, Camera.Parameters p) {
        m.set(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES, p.getMaxNumDetectedFaces() > 0 ? new int[]{0, 1} : new int[]{0});
        m.set(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT, Integer.valueOf(p.getMaxNumDetectedFaces()));
    }

    private static void mapSync(CameraMetadataNative m, Camera.Parameters p) {
        m.set(CameraCharacteristics.SYNC_MAX_LATENCY, -1);
    }

    private static void appendStreamConfig(ArrayList<StreamConfiguration> configs, int format, List<Camera.Size> sizes) {
        for (Camera.Size size : sizes) {
            configs.add(new StreamConfiguration(format, size.width, size.height, false));
        }
    }

    static int convertSceneModeFromLegacy(String mode) {
        if (mode == null) {
            return 0;
        }
        int index = ArrayUtils.getArrayIndex((T[]) sLegacySceneModes, mode);
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
        int index = ArrayUtils.getArrayIndex((T[]) sLegacyEffectMode, mode);
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

    /* JADX WARNING: Removed duplicated region for block: B:25:0x004e  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0065 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0066 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0067 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0068 A[RETURN] */
    private static int convertAntiBandingMode(String mode) {
        boolean z;
        if (mode != null) {
            int hashCode = mode.hashCode();
            if (hashCode == 109935) {
                if (mode.equals("off")) {
                    z = false;
                    switch (z) {
                        case false:
                            break;
                        case true:
                            break;
                        case true:
                            break;
                        case true:
                            break;
                    }
                }
            } else if (hashCode == 1628397) {
                if (mode.equals(Camera.Parameters.ANTIBANDING_50HZ)) {
                    z = true;
                    switch (z) {
                        case false:
                            break;
                        case true:
                            break;
                        case true:
                            break;
                        case true:
                            break;
                    }
                }
            } else if (hashCode == 1658188) {
                if (mode.equals(Camera.Parameters.ANTIBANDING_60HZ)) {
                    z = true;
                    switch (z) {
                        case false:
                            break;
                        case true:
                            break;
                        case true:
                            break;
                        case true:
                            break;
                    }
                }
            } else if (hashCode == 3005871 && mode.equals("auto")) {
                z = true;
                switch (z) {
                    case false:
                        return 0;
                    case true:
                        return 1;
                    case true:
                        return 2;
                    case true:
                        return 3;
                    default:
                        Log.w(TAG, "convertAntiBandingMode - Unknown antibanding mode " + mode);
                        return -1;
                }
            }
            z = true;
            switch (z) {
                case false:
                    break;
                case true:
                    break;
                case true:
                    break;
                case true:
                    break;
            }
        } else {
            return -1;
        }
    }

    static int convertAntiBandingModeOrDefault(String mode) {
        int antiBandingMode = convertAntiBandingMode(mode);
        if (antiBandingMode == -1) {
            return 0;
        }
        return antiBandingMode;
    }

    private static int[] convertAeFpsRangeToLegacy(Range<Integer> fpsRange) {
        return new int[]{fpsRange.getLower().intValue(), fpsRange.getUpper().intValue()};
    }

    private static long calculateJpegStallDuration(Camera.Size size) {
        return (((long) size.width) * ((long) size.height) * 71) + 200000000;
    }

    public static void convertRequestMetadata(LegacyRequest request) {
        LegacyRequestMapper.convertRequestMetadata(request);
    }

    public static CameraMetadataNative createRequestTemplate(CameraCharacteristics c, int templateId) {
        int captureIntent;
        int afMode;
        if (ArrayUtils.contains(sAllowedTemplates, templateId)) {
            CameraMetadataNative m = new CameraMetadataNative();
            m.set(CaptureRequest.CONTROL_AWB_MODE, 1);
            m.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, 3);
            m.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 0);
            m.set(CaptureRequest.CONTROL_AE_LOCK, false);
            m.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, 0);
            m.set(CaptureRequest.CONTROL_AF_TRIGGER, 0);
            m.set(CaptureRequest.CONTROL_AWB_MODE, 1);
            m.set(CaptureRequest.CONTROL_AWB_LOCK, false);
            Rect activeArray = (Rect) c.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            MeteringRectangle meteringRectangle = new MeteringRectangle(0, 0, activeArray.width() - 1, activeArray.height() - 1, 0);
            MeteringRectangle[] activeRegions = {meteringRectangle};
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
            m.set(CaptureRequest.CONTROL_AE_MODE, 1);
            m.set(CaptureRequest.CONTROL_MODE, 1);
            Float minimumFocusDistance = (Float) c.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
            if (minimumFocusDistance == null || minimumFocusDistance.floatValue() != 0.0f) {
                if (templateId == 3 || templateId == 4) {
                    if (ArrayUtils.contains((int[]) c.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES), 3)) {
                        afMode = 3;
                    }
                } else if ((templateId == 1 || templateId == 2) && ArrayUtils.contains((int[]) c.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES), 4)) {
                    afMode = 4;
                }
                afMode = 1;
            } else {
                afMode = 0;
            }
            m.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(afMode));
            Range<Integer>[] availableFpsRange = (Range[]) c.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            Range<Integer> bestRange = availableFpsRange[0];
            Range<Integer> bestRange2 = bestRange;
            for (Range<Integer> r : availableFpsRange) {
                if (bestRange2.getUpper().intValue() < r.getUpper().intValue()) {
                    bestRange2 = r;
                } else if (bestRange2.getUpper() == r.getUpper() && bestRange2.getLower().intValue() < r.getLower().intValue()) {
                    bestRange2 = r;
                }
            }
            m.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, bestRange2);
            m.set(CaptureRequest.CONTROL_SCENE_MODE, 0);
            m.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, 0);
            m.set(CaptureRequest.FLASH_MODE, 0);
            if (templateId == 2) {
                m.set(CaptureRequest.NOISE_REDUCTION_MODE, 2);
            } else {
                m.set(CaptureRequest.NOISE_REDUCTION_MODE, 1);
            }
            if (templateId == 2) {
                m.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, 2);
            } else {
                m.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, 1);
            }
            m.set(CaptureRequest.LENS_FOCAL_LENGTH, Float.valueOf(((float[]) c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS))[0]));
            Size[] sizes = (Size[]) c.get(CameraCharacteristics.JPEG_AVAILABLE_THUMBNAIL_SIZES);
            m.set(CaptureRequest.JPEG_THUMBNAIL_SIZE, sizes.length > 1 ? sizes[1] : sizes[0]);
            return m;
        }
        throw new IllegalArgumentException("templateId out of range");
    }

    private static int[] getTagsForKeys(CameraCharacteristics.Key<?>[] keys) {
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
                if (!supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                    param = Camera.Parameters.FOCUS_MODE_INFINITY;
                    break;
                } else {
                    param = Camera.Parameters.FOCUS_MODE_FIXED;
                    break;
                }
            case 1:
                param = "auto";
                break;
            case 2:
                param = Camera.Parameters.FOCUS_MODE_MACRO;
                break;
            case 3:
                param = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
                break;
            case 4:
                param = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
                break;
            case 5:
                param = Camera.Parameters.FOCUS_MODE_EDOF;
                break;
        }
        if (!supportedFocusModes.contains(param)) {
            String defaultMode = supportedFocusModes.get(0);
            Log.w(TAG, String.format("convertAfModeToLegacy - ignoring unsupported mode %d, defaulting to %s", new Object[]{Integer.valueOf(mode), defaultMode}));
            param = defaultMode;
        }
        return param;
    }
}
