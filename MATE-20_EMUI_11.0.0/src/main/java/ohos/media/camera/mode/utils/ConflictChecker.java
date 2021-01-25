package ohos.media.camera.mode.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import ohos.media.camera.mode.adapter.key.ModeCharacteristicKey;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.PropertyKey;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.image.common.Size;
import ohos.media.recorder.Recorder;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class ConflictChecker {
    private static final int DEFAULT_MAP_SIZE = 16;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ConflictChecker.class);
    private static Map<ParameterKey.Key<?>, PropertyKey.Key<?>> keyFunctionTypeMap = new HashMap(16);
    private static Map<ParameterKey.Key<?>, Object> keyOffValueMap = new HashMap(16);

    static {
        keyFunctionTypeMap.put(ParameterKey.AI_MOVIE, ModeCharacteristicKey.AI_MOVIE_FUNCTION);
        keyFunctionTypeMap.put(InnerParameterKey.SMART_CAPTURE, ModeCharacteristicKey.SMART_CAPTURE_FUNCTION);
        keyFunctionTypeMap.put(ParameterKey.FILTER_EFFECT, ModeCharacteristicKey.FILTER_EFFECT_FUNCTION);
        keyFunctionTypeMap.put(ParameterKey.BOKEH_APERTURE, ModeCharacteristicKey.BOKEHSPOT_FUNCTION);
        keyFunctionTypeMap.put(ParameterKey.PORTRAIT_FAIRLIGHT, ModeCharacteristicKey.FAIRLIGHT_FUNCTION);
        keyFunctionTypeMap.put(ParameterKey.VIDEO_STABILIZATION, ModeCharacteristicKey.VIDEO_STABILIZATION_FUNCTION);
        keyOffValueMap.put(ParameterKey.AI_MOVIE, (byte) 0);
        keyOffValueMap.put(ParameterKey.FILTER_EFFECT, (byte) 0);
        keyOffValueMap.put(InnerParameterKey.SMART_CAPTURE, 0);
        keyOffValueMap.put(ParameterKey.BOKEH_APERTURE, (byte) 0);
        keyOffValueMap.put(ParameterKey.PORTRAIT_FAIRLIGHT, (byte) 0);
        keyOffValueMap.put(ParameterKey.VIDEO_STABILIZATION, false);
    }

    private ConflictChecker() {
    }

    public static boolean checkBeautyConflict(ConflictParam conflictParam, int i) {
        if (KitUtil.isHwApp()) {
            return false;
        }
        if (conflictParam == null) {
            LOGGER.warn("checkBeautyConflict: conflictParam is null!", new Object[0]);
            return false;
        }
        boolean z = i != 0;
        Size videoSize = conflictParam.getVideoSize();
        ModeAbilityImpl modeAbility = conflictParam.getModeAbility();
        Map<PropertyKey.Key<?>, Boolean> currentFunctionStatus = conflictParam.getCurrentFunctionStatus();
        if (z) {
            if (videoSize == null || isOutputSizeSupported(videoSize, Recorder.class, modeAbility, ModeCharacteristicKey.BEAUTY_FUNCTION)) {
                PropertyKey.Key<?> queryFunctionConflict = queryFunctionConflict(ModeCharacteristicKey.BEAUTY_FUNCTION, currentFunctionStatus);
                if (queryFunctionConflict != null) {
                    LOGGER.warn("checkBeautyConflict: func = BEAUTY_FUNCTION, conflictFunc = %{public}s", queryFunctionConflict);
                    return true;
                }
            } else {
                LOGGER.warn("checkBeautyConflict: func = BEAUTY_FUNCTION, videoSize = %{public}s", videoSize);
                return true;
            }
        }
        return false;
    }

    public static <T> boolean checkKeyConflict(ConflictParam conflictParam, ParameterKey.Key<T> key, T t) {
        PropertyKey.Key<?> mapKeyToFunction;
        if (key == null) {
            LOGGER.error("checkKeyConflict : key is null", new Object[0]);
            return false;
        } else if (t == null) {
            LOGGER.error("checkKeyConflict : value is null", new Object[0]);
            return false;
        } else {
            if (!(KitUtil.isHwApp() || (mapKeyToFunction = mapKeyToFunction(key)) == null || conflictParam == null)) {
                boolean z = !t.equals(getKeyOffValue(key));
                Size videoSize = conflictParam.getVideoSize();
                ModeAbilityImpl modeAbility = conflictParam.getModeAbility();
                Map<PropertyKey.Key<?>, Boolean> currentFunctionStatus = conflictParam.getCurrentFunctionStatus();
                if (z) {
                    if (videoSize == null || isOutputSizeSupported(videoSize, Recorder.class, modeAbility, mapKeyToFunction)) {
                        PropertyKey.Key<?> queryFunctionConflict = queryFunctionConflict(mapKeyToFunction, currentFunctionStatus);
                        if (queryFunctionConflict != null) {
                            LOGGER.warn("checkKeyConflict: func = %{public}s, conflictFunc = %{public}s", mapKeyToFunction, queryFunctionConflict);
                            return true;
                        }
                    } else {
                        LOGGER.warn("checkKeyConflict: func = %{public}s, videoSize = %{public}s", mapKeyToFunction, videoSize);
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static <T> PropertyKey.Key<?> mapKeyToFunction(ParameterKey.Key<T> key) {
        return keyFunctionTypeMap.get(key);
    }

    public static Object getKeyOffValue(ParameterKey.Key<?> key) {
        return keyOffValueMap.get(key);
    }

    private static boolean isFunctionResolutionContained(List<Size> list, Size size) {
        return list == null || list.contains(size);
    }

    private static PropertyKey.Key<?> queryFunctionConflict(PropertyKey.Key<?> key, Map<PropertyKey.Key<?>, Boolean> map) {
        Map.Entry<PropertyKey.Key<?>, Boolean> next;
        if (!(map == null || key == null)) {
            Iterator<Map.Entry<PropertyKey.Key<?>, Boolean>> it = map.entrySet().iterator();
            while (it.hasNext() && (next = it.next()) != null) {
                if (FunctionConflictUtil.isFunctionConflict(key, next.getKey()) && next.getValue().booleanValue()) {
                    LOGGER.info("queryFunctionConflict: func = %{public}s, entry = %{public}s", key, next.toString());
                    return next.getKey();
                }
            }
            return null;
        }
        return null;
    }

    private static boolean isOutputSizeSupported(Size size, Class<?> cls, ModeAbilityImpl modeAbilityImpl, PropertyKey.Key<?> key) {
        if (size == null || cls == null || modeAbilityImpl == null || key == null) {
            return false;
        }
        Map<Class<?>, List<Size>> map = modeAbilityImpl.getFunctionClassOutputSizesMap().get(key);
        if (map == null) {
            return true;
        }
        List<Size> list = map.get(cls);
        LOGGER.info("isOutputSizeSupported: size = %{public}s, functionResolutionList = %{public}s", size, list);
        return isFunctionResolutionContained(list, size);
    }

    public static class ConflictParam {
        private Size captureSize;
        private Map<PropertyKey.Key<?>, Boolean> currentFunctionStatus;
        private ModeAbilityImpl modeAbility;
        private Size previewSize;
        private Size videoSize;

        private ConflictParam(Size size, Size size2, Size size3, ModeAbilityImpl modeAbilityImpl, Map<PropertyKey.Key<?>, Boolean> map) {
            this.previewSize = size;
            this.captureSize = size2;
            this.videoSize = size3;
            this.modeAbility = modeAbilityImpl;
            this.currentFunctionStatus = map;
        }

        public static ConflictParam create(Size size, Size size2, Size size3, ModeAbilityImpl modeAbilityImpl, Map<PropertyKey.Key<?>, Boolean> map) {
            return new ConflictParam(size, size2, size3, modeAbilityImpl, map);
        }

        public Size getPreviewSize() {
            return this.previewSize;
        }

        public void setPreviewSize(Size size) {
            this.previewSize = size;
        }

        public Size getCaptureSize() {
            return this.captureSize;
        }

        public void setCaptureSize(Size size) {
            this.captureSize = size;
        }

        public Size getVideoSize() {
            return this.videoSize;
        }

        public void setVideoSize(Size size) {
            this.videoSize = size;
        }

        public ModeAbilityImpl getModeAbility() {
            return this.modeAbility;
        }

        public void setModeAbility(ModeAbilityImpl modeAbilityImpl) {
            this.modeAbility = modeAbilityImpl;
        }

        public Map<PropertyKey.Key<?>, Boolean> getCurrentFunctionStatus() {
            return this.currentFunctionStatus;
        }

        public void setCurrentFunctionStatus(Map<PropertyKey.Key<?>, Boolean> map) {
            this.currentFunctionStatus = map;
        }
    }
}
