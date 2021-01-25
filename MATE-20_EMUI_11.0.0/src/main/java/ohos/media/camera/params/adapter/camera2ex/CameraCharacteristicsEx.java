package ohos.media.camera.params.adapter.camera2ex;

import android.hardware.camera2.CameraCharacteristics;

public final class CameraCharacteristicsEx {
    public static final CameraCharacteristics.Key<Byte> HAUWEI_SMART_SUGGEST_SUPPORT = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.smartSuggestSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_AF_TRIGGER_LOCK_SUPPORTED = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.afTriggerLockSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_AISHAPING_SUPPORT = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.aiShapingSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<byte[]> HUAWEI_AISHAPING_VALUES = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.aiShapingRange", byte[].class);
    public static final CameraCharacteristics.Key<Integer> HUAWEI_AI_VIDEO_SUPPORT = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.aiMovieModeSupported", Integer.TYPE);
    public static final CameraCharacteristics.Key<byte[]> HUAWEI_AVAILABLE_BRIGHTNESS = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.availbaleBrightness", byte[].class);
    public static final CameraCharacteristics.Key<byte[]> HUAWEI_AVAILABLE_COLOR_EFFECT_MODES = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.availbaleEffectModes", byte[].class);
    public static final CameraCharacteristics.Key<int[]> HUAWEI_AVAILABLE_COLOR_EFFECT_RANGE = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.availbaleEffectRange", int[].class);
    public static final CameraCharacteristics.Key<byte[]> HUAWEI_AVAILABLE_CONTRAST = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.availbaleContrast", byte[].class);
    public static final CameraCharacteristics.Key<byte[]> HUAWEI_AVAILABLE_DUAL_PRIMARY = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.availbaleDualPrimary", byte[].class);
    public static final CameraCharacteristics.Key<byte[]> HUAWEI_AVAILABLE_SATURATION = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.availbaleSaturation", byte[].class);
    public static final CameraCharacteristics.Key<int[]> HUAWEI_AVAILABLE_VIDEO_SENSORHDR_CONFIGURATIONS = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.hwAvailableVideoSensorHdrConfigurations", int[].class);
    public static final CameraCharacteristics.Key<int[]> HUAWEI_AVAILABLE_VIDEO_STABILIZATION_CONFIGURATIONS = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.hwAvailableVideoStabilizationConfigurations", int[].class);
    public static final CameraCharacteristics.Key<int[]> HUAWEI_AVAILABLE_VIDEO_WIDE_CONFIGURATIONS = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.hwAvailableVideoWideConfigurations", int[].class);
    public static final CameraCharacteristics.Key<byte[]> HUAWEI_BEAUTY_SETTING_DEFAULT_PARA = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.specificBeautyDefaultParam", byte[].class);
    public static final CameraCharacteristics.Key<byte[]> HUAWEI_BEAUTY_SETTING_FACE_SLENDER_VALUES = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.specificBeautyFaceSlenderValues", byte[].class);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_BEAUTY_SETTING_FRONT_FACE_SLENDER = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.specificBeautyFrontFaceSlenderSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<byte[]> HUAWEI_BEAUTY_SETTING_FRONT_SKIN_TONE = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.specificBeautyFrontSkinToneRange", byte[].class);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_BEAUTY_SETTING_REAR_FACE_SLENDER = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.specificBeautyRearFaceSlenderSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<byte[]> HUAWEI_BEAUTY_SETTING_REAR_SKIN_TONE = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.specificBeautyRearSkinToneRange", byte[].class);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_BEAUTY_SETTING_SKIN_SMOOTH = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.specificBeautySkinSmoothSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<byte[]> HUAWEI_BEAUTY_SETTING_SKIN_SMOOTH_VALUES = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.specificBeautySkinSmoothRange", byte[].class);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_BEAUTY_SETTING_SUPPORTED = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.specificBeautyModeSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_BEAUTY_STABILIZATION_SUPPORTED = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.hwBeautyStabilizationSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<int[]> HUAWEI_BIG_APERTURE_RESOLUTION_SUPPORTED = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.bigApertureSupportedResolution", int[].class);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_BODYSHAPING_MODE_SUPPORTED = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.bodyShapingModeSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_CAPTURE_MIRROR_SUPPORTED = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.captureMirrorSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_DM_WATERMARK_SUPPORTED = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.dmWaterMarkSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_DUAL_PRIMARY_SINGLE_REPROCESS = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.dualPrimarySingleReprocess", Byte.TYPE);
    public static final CameraCharacteristics.Key<int[]> HUAWEI_FACE_BEAUTY_RANGE = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.faceBeautyRange", int[].class);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_FACE_BEAUTY_SUPPORTED = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.faceBeautySupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<Integer> HUAWEI_FULLRESOLUTION_SUPPORT_FEATUREE = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.hwFullResolutionSupportFeature", Integer.TYPE);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_HIGH_RESOLUTION_BEAUTY_SUPPORTED = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.videoHighResBeautySupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<int[]> HUAWEI_OVERDEFAULT_RESOLUTION_PICTURE_SIZE = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.overdefaultResolutionPictureSize", int[].class);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_PORTRAIT_MODE_SUPPORTED = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.portraitModeSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_PORTRAIT_MOVIE_MODE_SUPPORTED = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.portraitMovieModeSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<int[]> HUAWEI_QUARTER_SIZE = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.quartersizes", int[].class);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_SENSOR_HDR_SUPPORTED = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.sensorHdrSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_SMART_CAPTURE_SUPPORT = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.smartCaptureSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_SMILE_DETECTION_SUPPORTED = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.smileDetectionSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<int[]> HUAWEI_SUPER_RESOLUTION_PICTURE_SIZE = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.superResolutionPictureSize", int[].class);
    public static final CameraCharacteristics.Key<byte[]> HUAWEI_SUPPORTED_COLOR_MODES = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.supportedColorModes", byte[].class);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_TELE_MODE_SUPPORT = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.teleModeSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_VIDEO_BEAUTY_SUPPORTED = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.videoBeatySupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<Integer> HUAWEI_VIRTUAL_CAMERA_TYPE = new CameraCharacteristics.Key<>("com.huawei.virtualcamera.metadata.vitrualcamera-type", Integer.class);
    public static final CameraCharacteristics.Key<Byte> HUAWEI_WIDE_ANGLE_SUPPORT = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.wideModeSupported", Byte.TYPE);
    public static final CameraCharacteristics.Key<int[]> HUAWEI_WIDE_ANGLE_ZOOM_CAPABILITY = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.zoomCapability", int[].class);

    private CameraCharacteristicsEx() {
    }
}
