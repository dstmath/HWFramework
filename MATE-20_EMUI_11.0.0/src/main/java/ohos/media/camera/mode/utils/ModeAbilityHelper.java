package ohos.media.camera.mode.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.Mode;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.params.PropertyKey;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class ModeAbilityHelper {
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ModeAbilityHelper.class);
    private static List<PropertyKey.Key<?>> commonCameraPropertyKeyList = new ArrayList(Arrays.asList(PropertyKey.SENSOR_ORIENTATION));
    private static Map<String, ModeAbilityImpl> modeAbilityCache = new HashMap();

    private ModeAbilityHelper() {
    }

    public static synchronized ModeAbilityImpl createModeAbility(@Mode.Type int i, int[] iArr, List<PropertyKey.Key<?>> list, CameraAbilityImpl cameraAbilityImpl) {
        synchronized (ModeAbilityHelper.class) {
            if (cameraAbilityImpl == null) {
                return null;
            }
            String str = cameraAbilityImpl.getCameraId() + "_" + i;
            if (modeAbilityCache.containsKey(str)) {
                return modeAbilityCache.get(str);
            }
            LOGGER.begin("createModeAbility of mode " + ModeNameUtil.getModeNameById(i));
            ModeAbilityImpl modeAbilityImpl = new ModeAbilityImpl(cameraAbilityImpl, iArr);
            modeAbilityImpl.put(commonCameraPropertyKeyList);
            ModeResolutionUtil.addResolutionCapability(modeAbilityImpl, i, list, cameraAbilityImpl);
            ModeFunctionUtil.addFunctionCapability(modeAbilityImpl, i, list, cameraAbilityImpl);
            modeAbilityImpl.dump();
            LOGGER.end("createModeAbility of mode " + ModeNameUtil.getModeNameById(i));
            modeAbilityCache.put(str, modeAbilityImpl);
            return modeAbilityImpl;
        }
    }

    public static synchronized ModeAbilityImpl fetchModeAbility(String str, int i) {
        ModeAbilityImpl modeAbilityImpl;
        synchronized (ModeAbilityHelper.class) {
            Map<String, ModeAbilityImpl> map = modeAbilityCache;
            modeAbilityImpl = map.get(str + "_" + i);
        }
        return modeAbilityImpl;
    }

    private static List<PropertyKey.Key<?>> getCommonPropertyKeys() {
        return commonCameraPropertyKeyList;
    }
}
