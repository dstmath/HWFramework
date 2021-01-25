package ohos.media.camera.mode.tags.hisi.utils;

import java.util.Arrays;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.adapter.utils.constant.ValueSet;
import ohos.media.camera.mode.tags.CaptureParameters;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class HisiFilterCapabilityUtil {
    private static final int FILTER_RANGE_LENGTH = 2;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(HisiFilterCapabilityUtil.class);

    private HisiFilterCapabilityUtil() {
    }

    public static boolean isAvailable(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            LOGGER.warn("isAvailable return false, cameraAbility == null", new Object[0]);
            return false;
        }
        byte[] bArr = (byte[]) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.AVAILABLE_COLOR_EFFECT_MODES);
        if (bArr == null || bArr.length < 1) {
            return false;
        }
        LOGGER.debug("supportedByDevice = %{public}s", Arrays.toString(bArr));
        return true;
    }

    public static byte[] getSupportedFilterRange(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl != null) {
            return (byte[]) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.AVAILABLE_COLOR_EFFECT_MODES);
        }
        LOGGER.warn("isAvailable return false, cameraAbility == null", new Object[0]);
        return new byte[0];
    }

    public static int[] getFilterLevelRange(CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl == null) {
            LOGGER.warn("getFilterLevelRange: cameraAbility is null", new Object[0]);
            return new int[0];
        }
        int[] iArr = new int[2];
        int[] iArr2 = (int[]) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.AVAILABLE_COLOR_EFFECT_RANGE);
        if (iArr2 != null && iArr2.length >= 2) {
            iArr[ValueSet.MIN.getValue()] = iArr2[0];
            iArr[ValueSet.MAX.getValue()] = iArr2[iArr.length - 1];
            LOGGER.debug("getFilterLevelRange %{public}s", Arrays.toString(iArr));
        }
        return iArr;
    }

    public static boolean isFilterEffectValid(int i, CameraAbilityImpl cameraAbilityImpl) {
        boolean z = false;
        if (cameraAbilityImpl == null) {
            LOGGER.warn("isFilterLevelValid: cameraAbility is null", new Object[0]);
            return false;
        }
        LOGGER.begin("isFilterEffectValid " + i);
        byte[] supportedFilterRange = getSupportedFilterRange(cameraAbilityImpl);
        if (!(supportedFilterRange == null || supportedFilterRange.length == 0)) {
            int length = supportedFilterRange.length;
            int i2 = 0;
            while (true) {
                if (i2 >= length) {
                    break;
                } else if (supportedFilterRange[i2] == i) {
                    z = true;
                    break;
                } else {
                    i2++;
                }
            }
            LOGGER.end("isFilterEffectValid " + z);
        }
        return z;
    }

    public static boolean isFilterLevelValid(int i, CameraAbilityImpl cameraAbilityImpl) {
        boolean z = false;
        if (cameraAbilityImpl == null) {
            LOGGER.warn("isFilterLevelValid: cameraAbility is null", new Object[0]);
            return false;
        }
        LOGGER.begin("isFilterLevelValid " + i);
        int[] filterLevelRange = getFilterLevelRange(cameraAbilityImpl);
        if (filterLevelRange.length == 2 && i >= filterLevelRange[ValueSet.MIN.getValue()] && i <= filterLevelRange[ValueSet.MAX.getValue()]) {
            z = true;
        }
        LOGGER.end("isFilterLevelValid " + z);
        return z;
    }

    public static void enableFilterEffect(CaptureParameters captureParameters, byte b, int i) {
        if (captureParameters != null) {
            captureParameters.addParameter(InnerParameterKey.COLOR_EFFECT_MODE, Byte.valueOf(b));
            captureParameters.addParameter(InnerParameterKey.COLOR_EFFECT_LEVEL, Integer.valueOf(i));
        }
    }
}
