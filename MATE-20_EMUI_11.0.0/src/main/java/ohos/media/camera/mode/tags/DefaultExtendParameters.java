package ohos.media.camera.mode.tags;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.PropertyKey;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class DefaultExtendParameters {
    private static final int CENTER_IN_SUPPORT = 2;
    private static final Map<ParameterKey.Key<?>, Object> EXTEND_PARAMS = new ConcurrentHashMap(100);
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(DefaultExtendParameters.class);
    private static final int MAX_PARAMS_NUM = 100;

    static {
        EXTEND_PARAMS.put(InnerParameterKey.FACE_BEAUTY_MODE, (byte) 0);
        EXTEND_PARAMS.put(InnerParameterKey.DM_WATERMARK_MODE, (byte) 0);
        EXTEND_PARAMS.put(InnerParameterKey.COLOR_MODE, (byte) 0);
        EXTEND_PARAMS.put(InnerParameterKey.COLOR_EFFECT_MODE, (byte) 0);
        EXTEND_PARAMS.put(InnerParameterKey.BEAUTY_MULTI_SETTING_MODE, (byte) 0);
        EXTEND_PARAMS.put(InnerParameterKey.SMART_CAPTURE_ENABLE, (byte) 0);
        EXTEND_PARAMS.put(InnerParameterKey.SENSOR_HDR_MODE, (byte) 0);
        EXTEND_PARAMS.put(InnerParameterKey.MASTER_AI_ENABLE, (byte) 0);
        EXTEND_PARAMS.put(InnerParameterKey.MASTER_AI_ENTER_MODE, 0);
        EXTEND_PARAMS.put(ParameterKey.AI_MOVIE, (byte) 0);
        EXTEND_PARAMS.put(InnerParameterKey.IMAGE_POST_PROCESS_MODE, (byte) 0);
        EXTEND_PARAMS.put(InnerParameterKey.CAMERA_FLAG, (byte) 0);
        EXTEND_PARAMS.put(InnerParameterKey.BURST_SNAPSHOT_MODE, (byte) 0);
        EXTEND_PARAMS.put(InnerParameterKey.BEST_SHOT_MODE, (byte) 0);
    }

    private DefaultExtendParameters() {
    }

    public static void applyToBuilder(CameraAbilityImpl cameraAbilityImpl, CaptureParameters captureParameters) {
        if (!(cameraAbilityImpl == null || captureParameters == null)) {
            for (Map.Entry<ParameterKey.Key<?>, Object> entry : EXTEND_PARAMS.entrySet()) {
                setToBuilder(captureParameters, entry.getKey(), entry.getValue());
            }
            setToCenterInSupports(cameraAbilityImpl, captureParameters, InnerParameterKey.CONTRAST_VALUE, InnerPropertyKey.AVAILABLE_CONTRAST);
            setToCenterInSupports(cameraAbilityImpl, captureParameters, InnerParameterKey.SATURATION_VALUE, InnerPropertyKey.AVAILABLE_SATURATION);
            setToCenterInSupports(cameraAbilityImpl, captureParameters, InnerParameterKey.BRIGHTNESS_VALUE, InnerPropertyKey.AVAILABLE_BRIGHTNESS);
        }
    }

    private static void setToCenterInSupports(CameraAbilityImpl cameraAbilityImpl, CaptureParameters captureParameters, ParameterKey.Key<Byte> key, PropertyKey.Key<byte[]> key2) {
        List<?> propertyRange;
        if (cameraAbilityImpl != null && (propertyRange = cameraAbilityImpl.getPropertyRange(key2)) != null && !propertyRange.isEmpty()) {
            setToBuilder(captureParameters, key, propertyRange.get((propertyRange.size() - 1) / 2));
        }
    }

    private static <T> void setToBuilder(CaptureParameters captureParameters, ParameterKey.Key<T> key, Object obj) {
        Logger logger = LOGGER;
        logger.begin("setToBuilder:key: " + key + " value: " + obj);
        captureParameters.addParameter(key, obj);
    }
}
