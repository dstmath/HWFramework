package ohos.media.camera.mode.tags.hisi.utils;

import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;
import ohos.media.camera.mode.utils.StringUtil;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class HisiFlashCapabilityUtil {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(HisiFlashCapabilityUtil.class);
    public static final int VALUE_AUTO = 0;
    public static final int VALUE_OFF = 1;
    public static final int VALUE_ON = 2;
    public static final int VALUE_TORCH = 3;

    private HisiFlashCapabilityUtil() {
    }

    public static boolean isAvailable(CameraAbilityImpl cameraAbilityImpl) {
        Integer[] numArr;
        if (cameraAbilityImpl == null || (numArr = (Integer[]) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.FLASH_MODE)) == null || numArr.length <= 0) {
            return false;
        }
        return true;
    }

    public static int[] getSupportedFlashModes(String str, CameraAbilityImpl cameraAbilityImpl) {
        if (StringUtil.isEmptyString(str)) {
            LOGGER.warn("the mode value is null", new Object[0]);
            return new int[0];
        } else if (isSpecificMode(str)) {
            return new int[]{1, 3};
        } else {
            if (ConstantValue.MODE_NAME_PRO_PHOTO_MODE.equals(str)) {
                return new int[]{1, 2, 3};
            }
            return new int[]{0, 1, 2, 3};
        }
    }

    private static boolean isSpecificMode(String str) {
        return ConstantValue.MODE_NAME_HDR_PHOTO.equals(str) || (ConstantValue.MODE_NAME_SUPER_SLOW_MOTION.equals(str) || ConstantValue.MODE_NAME_SLOW_MOTION.equals(str)) || (ConstantValue.MODE_NAME_NORMAL_VIDEO.equals(str) || ConstantValue.MODE_NAME_PRO_VIDEO_MODE.equals(str));
    }

    public static int getAeMode(int i) {
        if (!(i == 0 || i == 1 || i == 2)) {
        }
        LOGGER.debug("getAeMode %{public}d", 1);
        return 1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:5:0x000a, code lost:
        if (r4 != 3) goto L_0x000c;
     */
    public static int getFlashMode(int i) {
        int i2 = 3;
        if (!(i == 0 || i == 1)) {
            if (i == 2) {
                i2 = 0;
            }
            LOGGER.debug("getFlashMode %{public}d", Integer.valueOf(i2));
            return i2;
        }
        i2 = 1;
        LOGGER.debug("getFlashMode %{public}d", Integer.valueOf(i2));
        return i2;
    }
}
