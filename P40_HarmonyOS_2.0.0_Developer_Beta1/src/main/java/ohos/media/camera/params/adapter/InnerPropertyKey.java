package ohos.media.camera.params.adapter;

import ohos.agp.utils.Rect;
import ohos.media.camera.params.PropertyKey;

public final class InnerPropertyKey {
    public static final PropertyKey.Key<Integer[]> AE_MODE = new PropertyKey.Key<>("ohos.camera.aeMode", Integer[].class);
    public static final PropertyKey.Key<Integer[]> AF_MODE = new PropertyKey.Key<>("ohos.camera.afMode", Integer[].class);
    public static final PropertyKey.Key<Byte> AF_TRIGGER_LOCK_SUPPORTED = new PropertyKey.Key<>("ohos.camera.afTriggerLockSupported", Byte.class);
    public static final PropertyKey.Key<Byte> AI_SHAPING_SUPPORT = new PropertyKey.Key<>("ohos.camera.aiShapingSupported", Byte.class);
    public static final PropertyKey.Key<byte[]> AI_SHAPING_VALUES = new PropertyKey.Key<>("ohos.camera.aiShapingRange", byte[].class);
    public static final PropertyKey.Key<Integer> AI_VIDEO_SUPPORT = new PropertyKey.Key<>("ohos.camera.aiMovieModeSupported", Integer.class);
    public static final PropertyKey.Key<byte[]> AUTO_ZOOM_SUPPORT = new PropertyKey.Key<>("ohos.camera.autoZoomSupported", byte[].class);
    public static final PropertyKey.Key<byte[]> AVAILABLE_BRIGHTNESS = new PropertyKey.Key<>("ohos.camera.availableBrightness", byte[].class);
    public static final PropertyKey.Key<int[]> AVAILABLE_CAPABILITIES = new PropertyKey.Key<>("ohos.camera.availableCapabilities", int[].class);
    public static final PropertyKey.Key<byte[]> AVAILABLE_COLOR_EFFECT_MODES = new PropertyKey.Key<>("ohos.camera.availableEffectModes", byte[].class);
    public static final PropertyKey.Key<int[]> AVAILABLE_COLOR_EFFECT_RANGE = new PropertyKey.Key<>("ohos.camera.availableEffectRange", int[].class);
    public static final PropertyKey.Key<byte[]> AVAILABLE_CONTRAST = new PropertyKey.Key<>("ohos.camera.availableContrast", byte[].class);
    public static final PropertyKey.Key<byte[]> AVAILABLE_DUAL_PRIMARY = new PropertyKey.Key<>("ohos.camera.availableDualPrimary", byte[].class);
    public static final PropertyKey.Key<byte[]> AVAILABLE_SATURATION = new PropertyKey.Key<>("ohos.camera.availableSaturation", byte[].class);
    public static final PropertyKey.Key<int[]> AVAILABLE_VIDEO_SENSOR_HDR_CONFIGURATIONS = new PropertyKey.Key<>("ohos.camera.availableVideoSensorHdrConfigurations", int[].class);
    public static final PropertyKey.Key<int[]> AVAILABLE_VIDEO_STABILIZATION_CONFIGURATIONS = new PropertyKey.Key<>("ohos.camera.availableVideoStabilizationConfigurations", int[].class);
    public static final PropertyKey.Key<int[]> AVAILABLE_VIDEO_WIDE_CONFIGURATIONS = new PropertyKey.Key<>("ohos.camera.hwAvailableVideoWideConfigurations", int[].class);
    public static final PropertyKey.Key<byte[]> BEAUTY_SETTING_DEFAULT_PARA = new PropertyKey.Key<>("ohos.camera.specificBeautyDefaultParam", byte[].class);
    public static final PropertyKey.Key<byte[]> BEAUTY_SETTING_FACE_SLENDER_VALUES = new PropertyKey.Key<>("ohos.camera.specificBeautyFaceSlenderValues", byte[].class);
    public static final PropertyKey.Key<Byte> BEAUTY_SETTING_FRONT_FACE_SLENDER = new PropertyKey.Key<>("ohos.camera.specificBeautyFrontFaceSlenderSupported", Byte.class);
    public static final PropertyKey.Key<byte[]> BEAUTY_SETTING_FRONT_SKIN_TONE = new PropertyKey.Key<>("ohos.camera.specificBeautyFrontSkinToneRange", byte[].class);
    public static final PropertyKey.Key<Byte> BEAUTY_SETTING_REAR_FACE_SLENDER = new PropertyKey.Key<>("ohos.camera.specificBeautyRearFaceSlenderSupported", Byte.class);
    public static final PropertyKey.Key<byte[]> BEAUTY_SETTING_REAR_SKIN_TONE = new PropertyKey.Key<>("ohos.camera.specificBeautyRearSkinToneRange", byte[].class);
    public static final PropertyKey.Key<Byte> BEAUTY_SETTING_SKIN_SMOOTH = new PropertyKey.Key<>("ohos.camera.specificBeautySkinSmoothSupported", Byte.class);
    public static final PropertyKey.Key<byte[]> BEAUTY_SETTING_SKIN_SMOOTH_VALUES = new PropertyKey.Key<>("ohos.camera.specificBeautySkinSmoothRange", byte[].class);
    public static final PropertyKey.Key<Byte> BEAUTY_SETTING_SUPPORTED = new PropertyKey.Key<>("ohos.camera.specificBeautyModeSupported", Byte.class);
    public static final PropertyKey.Key<Byte> BEAUTY_STABILIZATION_SUPPORTED = new PropertyKey.Key<>("ohos.camera.beautyStabilizationSupported", Byte.class);
    public static final PropertyKey.Key<int[]> BIG_APERTURE_RESOLUTION_SUPPORTED = new PropertyKey.Key<>("ohos.camera.bigApertureSupportedResolution", int[].class);
    public static final PropertyKey.Key<Byte> BODYSHAPING_MODE_SUPPORTED = new PropertyKey.Key<>("ohos.camera.bodyShapingModeSupported", Byte.class);
    public static final PropertyKey.Key<Byte> CAPTURE_MIRROR_SUPPORTED = new PropertyKey.Key<>("ohos.camera.captureMirrorSupported", Byte.class);
    public static final PropertyKey.Key<Byte> DM_WATERMARK_SUPPORTED = new PropertyKey.Key<>("ohos.camera.dmWaterMarkSupported", Byte.class);
    public static final PropertyKey.Key<Byte> DUAL_PRIMARY_SINGLE_REPROCESS = new PropertyKey.Key<>("ohos.camera.dualPrimarySingleReprocess", Byte.class);
    public static final PropertyKey.Key<byte[]> FACE_AE_SUPPORT = new PropertyKey.Key<>("ohos.camera.faceAESupported", byte[].class);
    public static final PropertyKey.Key<int[]> FACE_BEAUTY_RANGE = new PropertyKey.Key<>("ohos.camera.faceBeautyRange", int[].class);
    public static final PropertyKey.Key<Byte> FACE_BEAUTY_SUPPORTED = new PropertyKey.Key<>("ohos.camera.faceBeautySupported", Byte.class);
    public static final PropertyKey.Key<Integer[]> FACE_DETECT_MODE = new PropertyKey.Key<>("ohos.camera.faceDetectMode", Integer[].class);
    public static final PropertyKey.Key<Integer[]> FLASH_MODE = new PropertyKey.Key<>("ohos.camera.flashMode", Integer[].class);
    public static final PropertyKey.Key<Integer> FULL_RESOLUTION_SUPPORT_FEATUREE = new PropertyKey.Key<>("ohos.camera.fullResolutionSupportFeature", Integer.class);
    public static final PropertyKey.Key<Byte> HIGH_RESOLUTION_BEAUTY_SUPPORTED = new PropertyKey.Key<>("ohos.camera.videoHighResBeautySupported", Byte.class);
    public static final PropertyKey.Key<Integer> LENS_FACING = new PropertyKey.Key<>("ohos.camera.lensFacing", Integer.class);
    public static final PropertyKey.Key<Integer> LINK_TYPE = new PropertyKey.Key<>("ohos.camera.linkType", Integer.class);
    public static final PropertyKey.Key<byte[]> LOGICAL_CAMERA_PHYSICAL_IDS = new PropertyKey.Key<>("ohos.camera.availableCapabilities", byte[].class);
    public static final PropertyKey.Key<Boolean> MIRROR_FUNCTION = new PropertyKey.Key<>("ohos.camera.mirrorFunction", Boolean.class);
    public static final PropertyKey.Key<int[]> OVERDEFAULT_RESOLUTION_PICTURE_SIZE = new PropertyKey.Key<>("ohos.camera.overdefaultResolutionPictureSize", int[].class);
    public static final PropertyKey.Key<Byte> PORTRAIT_MODE_SUPPORTED = new PropertyKey.Key<>("ohos.camera.portraitModeSupported", Byte.class);
    public static final PropertyKey.Key<Byte> PORTRAIT_MOVIE_MODE_SUPPORTED = new PropertyKey.Key<>("ohos.camera.portraitMovieModeSupported", Byte.class);
    public static final PropertyKey.Key<int[]> QUARTER_SIZE = new PropertyKey.Key<>("ohos.camera.quarterSizes", int[].class);
    public static final PropertyKey.Key<Byte> SENSOR_HDR_SUPPORTED = new PropertyKey.Key<>("ohos.camera.sensorHdrSupported", Byte.class);
    public static final PropertyKey.Key<Rect> SENSOR_INFO_ACTIVE_ARRAY_SIZE = new PropertyKey.Key<>("ohos.camera.activeSensorArraySize", Rect.class);
    public static final PropertyKey.Key<Byte> SMART_CAPTURE_SUPPORT = new PropertyKey.Key<>("ohos.camera.smartCaptureSupported", Byte.class);
    public static final PropertyKey.Key<Byte> SMART_SUGGEST_SUPPORT = new PropertyKey.Key<>("ohos.camera.smartSuggestSupported", Byte.class);
    public static final PropertyKey.Key<int[]> SUPER_RESOLUTION_PICTURE_SIZE = new PropertyKey.Key<>("ohos.camera.superResolutionPictureSize", int[].class);
    public static final PropertyKey.Key<byte[]> SUPPORTED_COLOR_MODES = new PropertyKey.Key<>("ohos.camera.supportedColorModes", byte[].class);
    public static final PropertyKey.Key<Byte> TELE_MODE_SUPPORT = new PropertyKey.Key<>("ohos.camera.teleModeSupported", Byte.class);
    public static final PropertyKey.Key<byte[]> VENDOR_CUSTOM_SUPPORT = new PropertyKey.Key<>("ohos.camera.vendorCustomSupported", byte[].class);
    public static final PropertyKey.Key<Byte> VIDEO_BEAUTY_SUPPORTED = new PropertyKey.Key<>("ohos.camera.videoBeautySupported", Byte.class);
    public static final PropertyKey.Key<Integer> VIRTUAL_CAMERA_TYPE = new PropertyKey.Key<>("ohos.camera.virtualCameraType", Integer.class);
    public static final PropertyKey.Key<Byte> WIDE_ANGLE_SUPPORT = new PropertyKey.Key<>("ohos.camera.wideModeSupported", Byte.class);
    public static final PropertyKey.Key<int[]> WIDE_ANGLE_ZOOM_CAPABILITY = new PropertyKey.Key<>("ohos.camera.zoomCapability", int[].class);
    public static final PropertyKey.Key<Float> ZOOM_RATIO = new PropertyKey.Key<>("ohos.camera.zoomRatio", Float.class);

    private InnerPropertyKey() {
    }
}
