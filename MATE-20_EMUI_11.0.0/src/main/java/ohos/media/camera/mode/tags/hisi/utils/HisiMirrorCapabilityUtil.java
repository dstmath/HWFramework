package ohos.media.camera.mode.tags.hisi.utils;

import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.tags.HuaweiTags;
import ohos.media.camera.mode.utils.CameraUtil;
import ohos.media.camera.params.adapter.InnerPropertyKey;

public class HisiMirrorCapabilityUtil {
    private HisiMirrorCapabilityUtil() {
    }

    public static boolean isAvailable(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl != null && CameraUtil.isFrontCamera(cameraAbilityImpl)) {
            return HuaweiTags.isSupported(cameraAbilityImpl, InnerPropertyKey.CAPTURE_MIRROR_SUPPORTED);
        }
        return false;
    }
}
