package ohos.media.camera.params.adapter.camera2ex;

import android.hardware.camera2.CaptureResult;

public final class CaptureResultEx {
    public static final CaptureResult.Key<int[]> HAUWEI_FAIR_LIGHT_VALID_FACE_CHECK = new CaptureResult.Key<>("com.huawei.capture.metadata.fairlightValidFaceCheck", int[].class);
    public static final CaptureResult.Key<Byte> HAUWEI_SMART_FOCUS_LOST = new CaptureResult.Key<>("com.huawei.capture.metadata.smartFocusLost", Byte.TYPE);
    public static final CaptureResult.Key<int[]> HAUWEI_SMART_FOCUS_UPLOAD_LOCATION = new CaptureResult.Key<>("com.huawei.capture.metadata.smartFocusUploadLocation", int[].class);
    public static final CaptureResult.Key<Integer> HAUWEI_SMART_SUGGEST_HINT = new CaptureResult.Key<>("com.huawei.capture.metadata.smartSuggestHint", Integer.TYPE);
    public static final CaptureResult.Key<int[]> HUAWEI_AI_VIDEO_COLORRETENTION_VALUE = new CaptureResult.Key<>("com.huawei.capture.metadata.colorRsvRGB", int[].class);
    public static final CaptureResult.Key<Integer> HUAWEI_ALGO_AE_LV = new CaptureResult.Key<>("com.huawei.capture.metadata.hw_algo_ae_lv", Integer.TYPE);
    public static final CaptureResult.Key<int[]> HUAWEI_CONTROL_AE_REGIONS = new CaptureResult.Key<>("com.huawei.capture.metadata.hwAeRegions", int[].class);
    public static final CaptureResult.Key<int[]> HUAWEI_CONTROL_AF_REGIONS = new CaptureResult.Key<>("com.huawei.capture.metadata.hwAfRegions", int[].class);
    public static final CaptureResult.Key<Integer> HUAWEI_DUAL_SENSOR_ABNORMAL = new CaptureResult.Key<>("com.huawei.capture.metadata.dualSensorAbnormal", Integer.TYPE);
    public static final CaptureResult.Key<Byte> HUAWEI_EXPOSURE_MODE_PREVIEW_STATE = new CaptureResult.Key<>("com.huawei.capture.metadata.hw-exposure-mode-preview-state", Byte.TYPE);
    public static final CaptureResult.Key<Integer> HUAWEI_EXPOSURE_STATE_HINT = new CaptureResult.Key<>("com.huawei.capture.metadata.exposureStateHint", Integer.TYPE);
    public static final CaptureResult.Key<int[]> HUAWEI_FACE_DISPLAY = new CaptureResult.Key<>("com.huawei.capture.metadata.hwFaceDisplay", int[].class);
    public static final CaptureResult.Key<int[]> HUAWEI_FACE_INFOS = new CaptureResult.Key<>("com.huawei.capture.metadata.hwFaceInfos", int[].class);
    public static final CaptureResult.Key<int[]> HUAWEI_FACE_RECTS = new CaptureResult.Key<>("com.huawei.capture.metadata.hwFaceRects", int[].class);
    public static final CaptureResult.Key<Byte> HUAWEI_FIRST_VALID_FRAME_INFO = new CaptureResult.Key<>("com.huawei.capture.metadata.hwFirstValidFrame", Byte.TYPE);
    public static final CaptureResult.Key<Integer> HUAWEI_FOCUS_VCM_VALUE = new CaptureResult.Key<>("com.huawei.capture.metadata.focusVcmValue", Integer.TYPE);
    public static final CaptureResult.Key<Integer> HUAWEI_FRAME_LUMINANCE = new CaptureResult.Key<>("com.huawei.capture.metadata.frameLuminance", Integer.TYPE);
    public static final CaptureResult.Key<Integer> HUAWEI_FRAME_LUMINATION = new CaptureResult.Key<>("com.huawei.capture.metadata.hw_algo_mean_y", Integer.TYPE);
    public static final CaptureResult.Key<Byte> HUAWEI_FRONTGESTURE_INFO = new CaptureResult.Key<>("com.huawei.capture.metadata.hwFrontgesture", Byte.TYPE);
    public static final CaptureResult.Key<Integer> HUAWEI_FRONT_FLASH_LEVEL = new CaptureResult.Key<>("com.huawei.capture.metadata.frontFlashLevel", Integer.TYPE);
    public static final CaptureResult.Key<Integer> HUAWEI_FRONT_FLASH_MODE = new CaptureResult.Key<>("com.huawei.capture.metadata.frontFlashMode", Integer.TYPE);
    public static final CaptureResult.Key<Integer> HUAWEI_HINT_USER = new CaptureResult.Key<>("com.huawei.capture.metadata.hintUser", Integer.TYPE);
    public static final CaptureResult.Key<Integer> HUAWEI_HINT_USER_VALUE = new CaptureResult.Key<>("com.huawei.capture.metadata.hintUserValue", Integer.TYPE);
    public static final CaptureResult.Key<Integer> HUAWEI_ISO_STATE = new CaptureResult.Key<>("com.huawei.capture.metadata.isoState", Integer.TYPE);
    public static final CaptureResult.Key<int[]> HUAWEI_LASER_DATA = new CaptureResult.Key<>("com.huawei.capture.metadata.cameraLaserData", int[].class);
    public static final CaptureResult.Key<Integer> HUAWEI_LCD_FLASH_COMPENSATE_VALUE = new CaptureResult.Key<>("com.huawei.capture.metadata.lcdFlashCompensateCCT", Integer.TYPE);
    public static final CaptureResult.Key<Integer> HUAWEI_LIGHT_PAINTING_EXPOSURE_TIME = new CaptureResult.Key<>("com.huawei.capture.metadata.lightPaintingExposureTime", Integer.TYPE);
    public static final CaptureResult.Key<Byte> HUAWEI_MAKE_UP_ABNORMAL_INFO = new CaptureResult.Key<>("com.huawei.capture.metadata.makeUpAbnormalInfo", Byte.TYPE);
    public static final CaptureResult.Key<int[]> HUAWEI_MD_ROI_READ = new CaptureResult.Key<>("com.huawei.capture.metadata.mdRoiArea", int[].class);
    public static final CaptureResult.Key<Byte> HUAWEI_NEED_LCD_COMPENSATE = new CaptureResult.Key<>("com.huawei.capture.metadata.needLcdCompensate", Byte.TYPE);
    public static final CaptureResult.Key<Byte> HUAWEI_OPTICAL_SWITCH_STATUS = new CaptureResult.Key<>("com.huawei.capture.metadata.opticalSwitchStatus", Byte.TYPE);
    public static final CaptureResult.Key<Byte> HUAWEI_PORTRAIT_DISTANCE_FLAG = new CaptureResult.Key<>("com.huawei.capture.metadata.portraitDistanceFlag", Byte.TYPE);
    public static final CaptureResult.Key<Byte> HUAWEI_PREVIEW_CAMERA_PHYSICAL_ID = new CaptureResult.Key<>("com.huawei.capture.metadata.previewCameraPhysicalId", Byte.TYPE);
    public static final CaptureResult.Key<Byte> HUAWEI_REMOSAIC_CAPTURE_PROCESS_STATUS = new CaptureResult.Key<>("com.huawei.capture.metadata.quadrawCaptureStatus", Byte.TYPE);
    public static final CaptureResult.Key<int[]> HUAWEI_SCENE_MEMORY_AVAILABLE = new CaptureResult.Key<>("com.huawei.capture.metadata.sceneMemoryAvailable", int[].class);
    public static final CaptureResult.Key<Byte> HUAWEI_SENSOR_DISTANCE_HINT = new CaptureResult.Key<>("com.huawei.capture.metadata.faceDistanceFlag", Byte.TYPE);
    public static final CaptureResult.Key<Byte> HUAWEI_SENSOR_HDR_HINT = new CaptureResult.Key<>("com.huawei.capture.metadata.sensorHdrHint", Byte.TYPE);
    public static final CaptureResult.Key<Byte> HUAWEI_SMART_CAPTURE_NUMBERS_QCOM = new CaptureResult.Key<>("com.huawei.capture.metadata.smartSceneCapNumber", Byte.TYPE);
    public static final CaptureResult.Key<int[]> HUAWEI_SMART_SUGGEST_SCENE_ARRAY = new CaptureResult.Key<>("com.huawei.capture.metadata.smartSuggestSceneArray", int[].class);
    public static final CaptureResult.Key<int[]> HUAWEI_SMART_ZOOM_TARGET = new CaptureResult.Key<>("com.huawei.capture.metadata.smartZoomTarget", int[].class);
    public static final CaptureResult.Key<int[]> HUAWEI_SMART_ZOOM_TRACKING_MANUAL_REGION_RESPONSE = new CaptureResult.Key<>("com.huawei.capture.metadata.trackingManualRegionResponse", int[].class);
    public static final CaptureResult.Key<int[]> HUAWEI_SMART_ZOOM_TRACKING_REGION = new CaptureResult.Key<>("com.huawei.capture.metadata.smartZoomRegion", int[].class);
    public static final CaptureResult.Key<Byte> HUAWEI_SUPER_SLOW_MOTION_STATUS = new CaptureResult.Key<>("com.huawei.capture.metadata.superSlowMotionStatus", Byte.TYPE);
    public static final CaptureResult.Key<Integer> HUAWEI_THERMAL_DUAL_TO_SINGLE = new CaptureResult.Key<>("com.huawei.capture.metadata.hwThermalDual2single", Integer.TYPE);
    public static final CaptureResult.Key<int[]> HUAWEI_VIDEO_BOKEH_AF_REGION = new CaptureResult.Key<>("com.huawei.capture.metadata.hwVideoBokehAfRegion", int[].class);

    private CaptureResultEx() {
    }
}
