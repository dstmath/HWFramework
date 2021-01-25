package ohos.media.camera.mode.tags.hisi.utils;

import java.util.Map;
import ohos.agp.utils.Rect;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.adapter.utils.CameraSceneModeUtil;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;
import ohos.media.camera.mode.utils.CameraUtil;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class HisiZoomCapabilityUtil {
    private static final float DEFAULT_ZOOM = 1.0f;
    private static final int HALF = 2;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(HisiZoomCapabilityUtil.class);
    private static final int ZOOM_BEAN_LENGTH = 9;
    private static final int ZOOM_DIVISOR_INDEX = 0;
    private static final int ZOOM_DIVISO_BIAS = 1;
    private static final int ZOOM_MAX_INDEX = 3;
    private static final int ZOOM_MIN_INDEX = 2;
    private static final int ZOOM_MODE_INDEX = 0;
    private static final int ZOOM_TYPE_INDEX = 1;

    private HisiZoomCapabilityUtil() {
    }

    public static Float[] getZoomLevelRange(String str, CameraAbilityImpl cameraAbilityImpl) {
        Float valueOf = Float.valueOf(1.0f);
        if (str == null || cameraAbilityImpl == null) {
            return new Float[]{valueOf, valueOf};
        }
        if (CameraUtil.isFrontCamera(cameraAbilityImpl)) {
            return new Float[]{valueOf, valueOf};
        }
        Map<String, Integer> sceneModeMap = CameraSceneModeUtil.getSceneModeMap();
        if (sceneModeMap == null || !sceneModeMap.containsKey(str)) {
            return new Float[]{valueOf, valueOf};
        }
        int intValue = sceneModeMap.get(str).intValue();
        int[] iArr = (int[]) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.WIDE_ANGLE_ZOOM_CAPABILITY);
        if (iArr == null || iArr.length <= 0) {
            return new Float[]{valueOf, valueOf};
        }
        int i = iArr[0];
        int length = (iArr.length - 1) / 9;
        for (int i2 = 0; i2 < length; i2++) {
            int i3 = (i2 * 9) + 1;
            if (intValue == iArr[i3 + 0]) {
                float f = (float) i;
                float f2 = ((float) iArr[i3 + 2]) / f;
                float f3 = ((float) iArr[i3 + 3]) / f;
                LOGGER.debug("modeId is %{public}d range is %{public}d %{public}d", Integer.valueOf(intValue), Float.valueOf(f2), Float.valueOf(f3));
                return new Float[]{Float.valueOf(f2), Float.valueOf(f3)};
            }
        }
        return new Float[]{valueOf, valueOf};
    }

    public static Rect getCenterZoomRect(float f, CameraAbilityImpl cameraAbilityImpl) {
        if (cameraAbilityImpl != null) {
            return getCenterZoomRect(getSensorArray(cameraAbilityImpl), f, 100.0f, ConstantValue.MIN_ZOOM_VALUE);
        }
        LOGGER.warn("cameraAbility is null.", new Object[0]);
        return null;
    }

    private static Rect getCenterZoomRect(Rect rect, float f, float f2, float f3) {
        if (rect == null) {
            LOGGER.warn("activeArray is null.", new Object[0]);
            return null;
        }
        int i = (rect.left + rect.right) / 2;
        int i2 = (rect.top + rect.bottom) / 2;
        int i3 = rect.right - rect.left;
        int i4 = rect.bottom - rect.top;
        float f4 = (float) i3;
        double d = (double) ((f4 / f) / 2.0f);
        int floor = (int) Math.floor(d);
        double d2 = (double) ((((float) i4) / f) / 2.0f);
        int floor2 = (int) Math.floor(d2);
        if ((f4 / 2.0f) / ((float) floor) > f2) {
            floor = (int) Math.ceil(d);
            floor2 = (int) Math.ceil(d2);
        }
        LOGGER.debug("CorpRegionZoomRatio = %{public}f", Float.valueOf(f4 / ((float) (floor * 2))));
        return new Rect(i - floor, i2 - floor2, i + floor, i2 + floor2);
    }

    private static Rect getSensorArray(CameraAbilityImpl cameraAbilityImpl) {
        return (Rect) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
    }
}
