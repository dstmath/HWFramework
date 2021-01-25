package ohos.media.camera.mode.tags.hisi.utils;

import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.tags.CaptureParameters;
import ohos.media.camera.mode.utils.CameraUtil;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class HisiSmartCaptureCapabilityUtil {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(HisiSmartCaptureCapabilityUtil.class);

    private HisiSmartCaptureCapabilityUtil() {
    }

    public static boolean isSmartCaptureOneAvailable(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            return false;
        }
        if (!CameraUtil.isFrontCamera(cameraAbilityImpl)) {
            LOGGER.debug("isSmartCaptureOneAvailable, is not front camera", new Object[0]);
            return false;
        }
        Byte b = (Byte) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.SMART_CAPTURE_SUPPORT);
        LOGGER.debug("isSmartCaptureOneAvailable, type is %{public}s", b);
        if (b == null) {
            return false;
        }
        if (b.byteValue() == 1 || b.byteValue() == 2) {
            return true;
        }
        return false;
    }

    public static boolean isMasterAiAvailable(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            return false;
        }
        if (CameraUtil.isFrontCamera(cameraAbilityImpl)) {
            LOGGER.debug("isMasterAiAvailable, is front camera", new Object[0]);
            return false;
        }
        Byte b = (Byte) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.SMART_SUGGEST_SUPPORT);
        LOGGER.debug("isMasterAiAvailable, type is %{public}s", b);
        if (b == null || (b.byteValue() & 2) == 0) {
            return false;
        }
        return true;
    }

    public static void setSmartCaptureParameter(CaptureParameters captureParameters, byte b, byte b2) {
        if (captureParameters != null) {
            captureParameters.addParameter(InnerParameterKey.SMART_CAPTURE_ENABLE, Byte.valueOf(b));
            if (b == 0 && b2 == 0) {
                captureParameters.addParameter(InnerParameterKey.MASTER_AI_ENABLE, (byte) 0);
            } else {
                captureParameters.addParameter(InnerParameterKey.MASTER_AI_ENABLE, (byte) 1);
            }
        }
    }
}
