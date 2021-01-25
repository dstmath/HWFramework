package ohos.media.camera.mode.tags.hisi.utils;

import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class HisiSensorHdrCapabilityUtil {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(HisiSensorHdrCapabilityUtil.class);

    private HisiSensorHdrCapabilityUtil() {
    }

    public static boolean isAvailable(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            LOGGER.warn("SensorHDR support return false, cameraAbility == null", new Object[0]);
            return false;
        }
        Byte b = (Byte) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.SENSOR_HDR_SUPPORTED);
        if (b == null || b.byteValue() != 1) {
            return false;
        }
        return true;
    }
}
