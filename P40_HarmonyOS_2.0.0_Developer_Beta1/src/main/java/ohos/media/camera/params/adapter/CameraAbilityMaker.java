package ohos.media.camera.params.adapter;

import android.hardware.camera2.CameraCharacteristics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.PropertyKey;
import ohos.media.camera.params.ResultKey;
import ohos.media.camera.zidl.CameraAbilityNative;
import ohos.media.camera.zidl.StreamConfigAbility;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class CameraAbilityMaker {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(CameraAbilityMaker.class);
    private static final List<ParameterKey.Key<?>> SUPPORTED_PARAMETER_KEYS;
    private static final List<PropertyKey.Key<?>> SUPPORTED_PROPERTY_KEYS;
    private static final List<ResultKey.Key<?>> SUPPORTED_RESULT_KEYS;
    private final CameraCharacteristics cameraCharacteristics;
    private final String cameraId;
    private final PropertyKeyMapper propertyKeyMapper;

    static {
        ArrayList arrayList = new ArrayList();
        arrayList.add(PropertyKey.SENSOR_ORIENTATION);
        arrayList.add(PropertyKey.PARTIAL_RESULT_COUNT);
        arrayList.add(PropertyKey.VIDEO_STABILIZATION_SUPPORT);
        arrayList.add(InnerPropertyKey.LENS_FACING);
        arrayList.add(InnerPropertyKey.MIRROR_FUNCTION);
        arrayList.add(InnerPropertyKey.ZOOM_RATIO);
        arrayList.add(InnerPropertyKey.FACE_DETECT_MODE);
        arrayList.add(InnerPropertyKey.AF_MODE);
        arrayList.add(InnerPropertyKey.AE_MODE);
        arrayList.add(InnerPropertyKey.LINK_TYPE);
        arrayList.add(InnerPropertyKey.FLASH_MODE);
        arrayList.add(InnerPropertyKey.AVAILABLE_CAPABILITIES);
        arrayList.add(InnerPropertyKey.LOGICAL_CAMERA_PHYSICAL_IDS);
        arrayList.add(InnerPropertyKey.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        arrayList.add(InnerPropertyKey.AUTO_ZOOM_SUPPORT);
        arrayList.add(InnerPropertyKey.FACE_AE_SUPPORT);
        arrayList.add(InnerPropertyKey.VENDOR_CUSTOM_SUPPORT);
        arrayList.add(InnerPropertyKey.CAPTURE_MIRROR_SUPPORTED);
        arrayList.add(InnerPropertyKey.FACE_BEAUTY_SUPPORTED);
        arrayList.add(InnerPropertyKey.FACE_BEAUTY_RANGE);
        arrayList.add(InnerPropertyKey.AVAILABLE_COLOR_EFFECT_MODES);
        arrayList.add(InnerPropertyKey.AVAILABLE_COLOR_EFFECT_RANGE);
        arrayList.add(InnerPropertyKey.VIDEO_BEAUTY_SUPPORTED);
        arrayList.add(InnerPropertyKey.AVAILABLE_CONTRAST);
        arrayList.add(InnerPropertyKey.AVAILABLE_SATURATION);
        arrayList.add(InnerPropertyKey.AVAILABLE_BRIGHTNESS);
        arrayList.add(InnerPropertyKey.AVAILABLE_DUAL_PRIMARY);
        arrayList.add(InnerPropertyKey.SUPPORTED_COLOR_MODES);
        arrayList.add(InnerPropertyKey.AF_TRIGGER_LOCK_SUPPORTED);
        arrayList.add(InnerPropertyKey.DM_WATERMARK_SUPPORTED);
        arrayList.add(InnerPropertyKey.DUAL_PRIMARY_SINGLE_REPROCESS);
        arrayList.add(InnerPropertyKey.SUPER_RESOLUTION_PICTURE_SIZE);
        arrayList.add(InnerPropertyKey.PORTRAIT_MODE_SUPPORTED);
        arrayList.add(InnerPropertyKey.BIG_APERTURE_RESOLUTION_SUPPORTED);
        arrayList.add(InnerPropertyKey.SMART_SUGGEST_SUPPORT);
        arrayList.add(InnerPropertyKey.SMART_CAPTURE_SUPPORT);
        arrayList.add(InnerPropertyKey.PORTRAIT_MOVIE_MODE_SUPPORTED);
        arrayList.add(InnerPropertyKey.SENSOR_HDR_SUPPORTED);
        arrayList.add(InnerPropertyKey.AVAILABLE_VIDEO_SENSOR_HDR_CONFIGURATIONS);
        arrayList.add(InnerPropertyKey.WIDE_ANGLE_ZOOM_CAPABILITY);
        arrayList.add(InnerPropertyKey.BEAUTY_SETTING_SUPPORTED);
        arrayList.add(InnerPropertyKey.BEAUTY_SETTING_DEFAULT_PARA);
        arrayList.add(InnerPropertyKey.BEAUTY_SETTING_SKIN_SMOOTH);
        arrayList.add(InnerPropertyKey.BEAUTY_SETTING_FRONT_SKIN_TONE);
        arrayList.add(InnerPropertyKey.BEAUTY_SETTING_REAR_SKIN_TONE);
        arrayList.add(InnerPropertyKey.BEAUTY_SETTING_FRONT_FACE_SLENDER);
        arrayList.add(InnerPropertyKey.BEAUTY_SETTING_REAR_FACE_SLENDER);
        arrayList.add(InnerPropertyKey.BEAUTY_SETTING_SKIN_SMOOTH_VALUES);
        arrayList.add(InnerPropertyKey.BEAUTY_SETTING_FACE_SLENDER_VALUES);
        arrayList.add(InnerPropertyKey.FULL_RESOLUTION_SUPPORT_FEATUREE);
        arrayList.add(InnerPropertyKey.AVAILABLE_VIDEO_STABILIZATION_CONFIGURATIONS);
        arrayList.add(InnerPropertyKey.AI_VIDEO_SUPPORT);
        arrayList.add(InnerPropertyKey.BODYSHAPING_MODE_SUPPORTED);
        arrayList.add(InnerPropertyKey.QUARTER_SIZE);
        arrayList.add(InnerPropertyKey.OVERDEFAULT_RESOLUTION_PICTURE_SIZE);
        arrayList.add(InnerPropertyKey.AVAILABLE_VIDEO_WIDE_CONFIGURATIONS);
        arrayList.add(InnerPropertyKey.AI_SHAPING_SUPPORT);
        arrayList.add(InnerPropertyKey.AI_SHAPING_VALUES);
        arrayList.add(InnerPropertyKey.BEAUTY_STABILIZATION_SUPPORTED);
        arrayList.add(InnerPropertyKey.HIGH_RESOLUTION_BEAUTY_SUPPORTED);
        arrayList.add(InnerPropertyKey.VIRTUAL_CAMERA_TYPE);
        arrayList.add(InnerPropertyKey.WIDE_ANGLE_SUPPORT);
        arrayList.add(InnerPropertyKey.TELE_MODE_SUPPORT);
        SUPPORTED_PROPERTY_KEYS = Collections.unmodifiableList(arrayList);
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(ParameterKey.IMAGE_COMPRESSION_QUALITY);
        arrayList2.add(ParameterKey.IMAGE_MIRROR);
        arrayList2.add(ParameterKey.VIDEO_STABILIZATION);
        arrayList2.add(ParameterKey.EXPOSURE_FPS_RANGE);
        arrayList2.add(ParameterKey.AUTO_ZOOM);
        arrayList2.add(ParameterKey.FACE_AE);
        arrayList2.add(ParameterKey.VENDOR_CUSTOM);
        arrayList2.add(InnerParameterKey.ZOOM_RATIO);
        arrayList2.add(InnerParameterKey.AE_MODE);
        arrayList2.add(InnerParameterKey.AE_REGION);
        arrayList2.add(InnerParameterKey.AF_TRIGGER);
        arrayList2.add(InnerParameterKey.AF_MODE);
        arrayList2.add(InnerParameterKey.AF_REGION);
        arrayList2.add(InnerParameterKey.AF_TRIGGER);
        arrayList2.add(InnerParameterKey.FLASH_MODE);
        arrayList2.add(InnerParameterKey.FACE_DETECTION_TYPE);
        arrayList2.add(InnerParameterKey.IMAGE_ROTATION);
        arrayList2.add(InnerParameterKey.LOCATION);
        arrayList2.add(InnerParameterKey.CAPTURE_MIRROR);
        arrayList2.add(InnerParameterKey.FACE_BEAUTY_MODE);
        arrayList2.add(InnerParameterKey.FACE_BEAUTY_LEVEL);
        arrayList2.add(InnerParameterKey.COLOR_EFFECT_MODE);
        arrayList2.add(InnerParameterKey.COLOR_EFFECT_LEVEL);
        arrayList2.add(InnerParameterKey.CONTRAST_VALUE);
        arrayList2.add(InnerParameterKey.SATURATION_VALUE);
        arrayList2.add(InnerParameterKey.BRIGHTNESS_VALUE);
        arrayList2.add(InnerParameterKey.METERING_MODE);
        arrayList2.add(InnerParameterKey.BURST_SNAPSHOT_MODE);
        arrayList2.add(InnerParameterKey.SMILE_DETECTION);
        arrayList2.add(InnerParameterKey.CAMERA_FLAG);
        arrayList2.add(InnerParameterKey.IMAGE_POST_PROCESS_MODE);
        arrayList2.add(InnerParameterKey.IMAGE_FOREGROUND_PROCESS_MODE);
        arrayList2.add(InnerParameterKey.BEST_SHOT_MODE);
        arrayList2.add(InnerParameterKey.MANUAL_FOCUS_VALUE);
        arrayList2.add(InnerParameterKey.DUAL_SENSOR_MODE);
        arrayList2.add(InnerParameterKey.API_VERSION);
        arrayList2.add(InnerParameterKey.HIGH_VIDEO_FPS);
        arrayList2.add(InnerParameterKey.AF_TRIGGER_LOCK);
        arrayList2.add(InnerParameterKey.DM_WATERMARK_MODE);
        arrayList2.add(InnerParameterKey.JPEG_FILE_NAME);
        arrayList2.add(InnerParameterKey.CAMERA_SCENE_MODE);
        arrayList2.add(InnerParameterKey.SMART_CAPTURE_ENABLE);
        arrayList2.add(InnerParameterKey.SENSOR_HDR_MODE);
        arrayList2.add(InnerParameterKey.NICE_FOOD_MODE);
        arrayList2.add(InnerParameterKey.BEAUTY_MULTI_SETTING_MODE);
        arrayList2.add(InnerParameterKey.CONTROL_AE_REGIONS);
        arrayList2.add(InnerParameterKey.CONTROL_AF_REGIONS);
        arrayList2.add(InnerParameterKey.CAMERA_SESSION_SCENE_MODE);
        arrayList2.add(InnerParameterKey.BODY_SHAPING_LEVEL);
        arrayList2.add(InnerParameterKey.MASTER_AI_ENABLE);
        arrayList2.add(InnerParameterKey.MASTER_AI_ENTER_MODE);
        arrayList2.add(InnerParameterKey.SMART_SUGGEST_RECORD_CLEAR);
        arrayList2.add(InnerParameterKey.SMART_SUGGEST_EXIT_MODE);
        arrayList2.add(InnerParameterKey.SMART_SUGGEST_CONFIRM);
        arrayList2.add(InnerParameterKey.SMART_SUGGEST_DISMISS);
        arrayList2.add(InnerParameterKey.VIDEO_DYNAMIC_FPS_MODE);
        arrayList2.add(InnerParameterKey.REAL_VIDEO_SIZE);
        arrayList2.add(InnerParameterKey.SCALER_CROP_REGION);
        arrayList2.add(InnerParameterKey.LENS_OPTICAL_STABILIZATION_MODE);
        arrayList2.add(InnerParameterKey.LENS_FOCUS_DISTANCE);
        arrayList2.add(InnerParameterKey.AE_PRECAPTURE_TRIGGER);
        arrayList2.add(InnerParameterKey.WATER_MARK);
        arrayList2.add(InnerParameterKey.SMART_CAPTURE);
        arrayList2.add(InnerParameterKey.COLOR_MODE);
        SUPPORTED_PARAMETER_KEYS = Collections.unmodifiableList(arrayList2);
        ArrayList arrayList3 = new ArrayList(6);
        arrayList3.add(ResultKey.VIDEO_STABILIZATION_STATE);
        arrayList3.add(ResultKey.EXPOSURE_FPS_RANGE_RESULT);
        arrayList3.add(ResultKey.AUTO_ZOOM_STATE);
        arrayList3.add(ResultKey.FACE_AE_STATE);
        arrayList3.add(ResultKey.VENDOR_CUSTOM_RESULT);
        arrayList3.add(InnerResultKey.AF_STATE);
        arrayList3.add(InnerResultKey.AE_STATE);
        arrayList3.add(InnerResultKey.FACE_DETECT);
        arrayList3.add(InnerResultKey.FACE_SMILE_SCORE);
        arrayList3.add(InnerResultKey.SMART_SUGGEST_HINT);
        SUPPORTED_RESULT_KEYS = Collections.unmodifiableList(arrayList3);
    }

    public CameraAbilityMaker(CameraCharacteristics cameraCharacteristics2, String str) {
        this.cameraId = str;
        this.cameraCharacteristics = cameraCharacteristics2;
        this.propertyKeyMapper = new PropertyKeyMapper(cameraCharacteristics2);
    }

    public Optional<CameraAbilityNative> makeCameraAbility() {
        LOGGER.info("Make camera ability for cameraId: %{public}s", this.cameraId);
        Optional<StreamConfigAbility> streamConfigAbility = new StreamConfigAbilityMapper(this.cameraCharacteristics).getStreamConfigAbility();
        if (!streamConfigAbility.isPresent()) {
            LOGGER.warn("There is no camera ability for cameraId: %{public}s", this.cameraId);
            return Optional.empty();
        }
        CameraAbilityNative cameraAbilityNative = new CameraAbilityNative(this.cameraId, streamConfigAbility.get());
        for (PropertyKey.Key<?> key : SUPPORTED_PROPERTY_KEYS) {
            this.propertyKeyMapper.getValue(key).ifPresent(new Consumer(cameraAbilityNative, key) {
                /* class ohos.media.camera.params.adapter.$$Lambda$CameraAbilityMaker$DARAoRDtw0AibZh1RwKsUsf54c */
                private final /* synthetic */ CameraAbilityNative f$1;
                private final /* synthetic */ PropertyKey.Key f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    CameraAbilityMaker.this.lambda$makeCameraAbility$0$CameraAbilityMaker(this.f$1, this.f$2, obj);
                }
            });
        }
        LOGGER.debug("Make camera ability supported properties size %{public}d", Integer.valueOf(cameraAbilityNative.getSupportedProperties().size()));
        LOGGER.debug("Make camera ability supported properties: %{public}s", cameraAbilityNative.getSupportedProperties());
        for (ParameterKey.Key<?> key2 : SUPPORTED_PARAMETER_KEYS) {
            cameraAbilityNative.addSupportedParameters(key2);
        }
        LOGGER.debug("Make camera ability supported parameters size %{public}d", Integer.valueOf(cameraAbilityNative.getSupportedParameters().size()));
        LOGGER.debug("Make camera ability supported parameters: %{public}s", cameraAbilityNative.getSupportedParameters());
        for (ResultKey.Key<?> key3 : SUPPORTED_RESULT_KEYS) {
            cameraAbilityNative.addSupportedResults(key3);
        }
        LOGGER.debug("Make camera ability supported results size %{public}d", Integer.valueOf(cameraAbilityNative.getSupportedResults().size()));
        LOGGER.debug("Make camera ability supported results: %{public}s", cameraAbilityNative.getSupportedResults());
        LOGGER.debug("Make camera ability success for cameraId: %{public}s", this.cameraId);
        return Optional.of(cameraAbilityNative);
    }

    public /* synthetic */ void lambda$makeCameraAbility$0$CameraAbilityMaker(CameraAbilityNative cameraAbilityNative, PropertyKey.Key key, Object obj) {
        cameraAbilityNative.setPropertyValue(key, obj);
        cameraAbilityNative.addSupportedProperties(key);
        LOGGER.debug("Map SUPPORTED_PROPERTY_KEYS, cameraId: %{public}s, key: %{public}s, value: %{public}s", this.cameraId, key.toString(), obj.toString());
    }
}
