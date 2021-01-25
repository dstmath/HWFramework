package ohos.media.camera.mode.tags;

import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.params.Metadata;
import ohos.media.camera.params.PropertyKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class HuaweiTags {
    public static final byte DEFAULT_JPEG_QUALITY = 90;
    public static final int DEFAULT_ORIENTATION = 90;
    private static final int HEX_MAX = 255;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(HuaweiTags.class);

    private HuaweiTags() {
    }

    public static <T> boolean isSupported(CameraAbilityImpl cameraAbilityImpl, PropertyKey.Key<T> key) {
        if (cameraAbilityImpl == null) {
            return false;
        }
        try {
            Object propertyValue = cameraAbilityImpl.getPropertyValue(key);
            if (propertyValue == null) {
                return false;
            }
            if (propertyValue instanceof Byte) {
                if (((Byte) propertyValue).byteValue() == 1) {
                    return true;
                }
                return false;
            } else if (!(propertyValue instanceof Integer) || ((Integer) propertyValue).intValue() != 1) {
                return false;
            } else {
                return true;
            }
        } catch (IllegalArgumentException unused) {
            LOGGER.warn("invalid tag！", new Object[0]);
            return false;
        }
    }

    public static int[] getTagSupportValues(CameraAbilityImpl cameraAbilityImpl, PropertyKey.Key<byte[]> key) {
        if (cameraAbilityImpl == null) {
            return new int[0];
        }
        byte[] bArr = (byte[]) cameraAbilityImpl.getPropertyValue(key);
        if (bArr == null) {
            return new int[0];
        }
        int[] iArr = new int[bArr.length];
        for (int i = 0; i < bArr.length; i++) {
            iArr[i] = bArr[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
        }
        return iArr;
    }
}
