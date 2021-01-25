package ohos.media.camera.mode.tags.hisi.utils;

import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.adapter.utils.CustomConfigurationUtil;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class HisiColorCapabilityUtil {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(HisiColorCapabilityUtil.class);
    public static final int VALUE_BRIGHT = 1;
    public static final int VALUE_NORMAL = 0;
    public static final int VALUE_SOFT = 2;

    private HisiColorCapabilityUtil() {
    }

    public static boolean isAvailable(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null || !CustomConfigurationUtil.isDmSupported() || ((byte[]) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.SUPPORTED_COLOR_MODES)) == null) {
            return false;
        }
        return true;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v2, resolved type: byte[] */
    /* JADX DEBUG: Multi-variable search result rejected for r1v2, resolved type: int[] */
    /* JADX DEBUG: Multi-variable search result rejected for r2v1, resolved type: byte */
    /* JADX WARN: Multi-variable type inference failed */
    public static int[] getSupportedColorModes(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            LOGGER.warn("input value cameraAbility is null", new Object[0]);
            return new int[0];
        }
        byte[] bArr = (byte[]) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.SUPPORTED_COLOR_MODES);
        if (bArr == 0) {
            return new int[0];
        }
        int[] iArr = new int[bArr.length];
        for (int i = 0; i < bArr.length; i++) {
            iArr[i] = bArr[i];
        }
        return iArr;
    }
}
