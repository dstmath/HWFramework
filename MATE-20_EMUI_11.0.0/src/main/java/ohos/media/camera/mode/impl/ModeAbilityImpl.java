package ohos.media.camera.mode.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import ohos.agp.graphics.SurfaceOps;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.ModeAbility;
import ohos.media.camera.mode.adapter.key.ModeCharacteristicKey;
import ohos.media.camera.mode.adapter.key.ModeRequestKey;
import ohos.media.camera.mode.utils.CameraUtil;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.PropertyKey;
import ohos.media.image.common.Size;
import ohos.media.recorder.Recorder;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.utils.Pair;

public class ModeAbilityImpl implements ModeAbility {
    private static final float[] DEFAULT_ZOOM_RANGES = {1.0f, 1.0f};
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ModeAbilityImpl.class);
    private static final List<Class<?>> SUPPORTED_PREVIEW_CLASS_LIST = Collections.singletonList(SurfaceOps.class);
    private static final List<Class<?>> SUPPORTED_VIDEO_CLASSES_LIST = Collections.singletonList(Recorder.class);
    private static final int ZOOM_RANGE_MAX = 1;
    private static final int ZOOM_RANGE_MIN = 0;
    private static Map<PropertyKey.Key<?>, Integer> conflictFunctionToActionMap = initConflictFunctionToActionMap();
    private final List<Integer> actionList;
    private List<ParameterKey.Key<?>> availableParameterKeys = new ArrayList();
    private Map<PropertyKey.Key<?>, Object> availableProperties = new HashMap();
    private final CameraAbilityImpl cameraAbility;
    private Map<Class<?>, Map<Integer, List<Size>>> classFpsOutputSizesMap = new HashMap();
    private Map<Class<?>, List<Size>> classOutputSizesMaps = new HashMap();
    private Set<PropertyKey.Key<?>> conflictFunctions;
    private Map<Integer, List<Size>> formatOutputSizesMaps = new HashMap();
    private Map<PropertyKey.Key<?>, Map<Class<?>, List<Size>>> functionClassOutputSizesMap = new HashMap();
    private Map<PropertyKey.Key<?>, Map<Integer, List<Size>>> functionFormatOutputSizesMap = new HashMap();
    private int maxPreviewSurfaceNumber;

    public ModeAbilityImpl(CameraAbilityImpl cameraAbilityImpl, int[] iArr) {
        this.actionList = (List) Arrays.stream(iArr).boxed().collect(Collectors.toList());
        this.cameraAbility = cameraAbilityImpl;
    }

    private static Map<PropertyKey.Key<?>, Integer> initConflictFunctionToActionMap() {
        HashMap hashMap = new HashMap(6);
        hashMap.put(ModeCharacteristicKey.AI_MOVIE_FUNCTION, 1);
        hashMap.put(ModeCharacteristicKey.BEAUTY_FUNCTION, 2);
        hashMap.put(ModeCharacteristicKey.FILTER_EFFECT_FUNCTION, 3);
        hashMap.put(ModeCharacteristicKey.BOKEHSPOT_FUNCTION, 4);
        hashMap.put(ModeCharacteristicKey.FAIRLIGHT_FUNCTION, 5);
        hashMap.put(ModeCharacteristicKey.VIDEO_STABILIZATION_FUNCTION, 6);
        return hashMap;
    }

    public void setFormatOutputSizes(Map<Integer, List<Size>> map) {
        if (map != null) {
            this.formatOutputSizesMaps = map;
        }
    }

    public void setClassOutputSizes(Map<Class<?>, List<Size>> map) {
        this.classOutputSizesMaps = map;
    }

    public void setClassFpsOutputSizes(Map<Class<?>, Map<Integer, List<Size>>> map) {
        this.classFpsOutputSizesMap.clear();
        if (map == null) {
            this.classFpsOutputSizesMap = Collections.emptyMap();
        } else {
            this.classFpsOutputSizesMap = map;
        }
    }

    public Map<PropertyKey.Key<?>, Map<Integer, List<Size>>> getFunctionFormatOutputSizesMap() {
        return this.functionFormatOutputSizesMap;
    }

    public void setFunctionFormatOutputSizesMap(Map<PropertyKey.Key<?>, Map<Integer, List<Size>>> map) {
        this.functionFormatOutputSizesMap = map;
    }

    public Map<PropertyKey.Key<?>, Map<Class<?>, List<Size>>> getFunctionClassOutputSizesMap() {
        return this.functionClassOutputSizesMap;
    }

    public void setFunctionClassOutputSizesMap(Map<PropertyKey.Key<?>, Map<Class<?>, List<Size>>> map) {
        this.functionClassOutputSizesMap = map;
    }

    public void addAvailableParameterKey(ParameterKey.Key<?> key) {
        if (key == null) {
            LOGGER.warn("addAvailableParameterKey invalid key null!", new Object[0]);
        } else if (!this.availableParameterKeys.contains(key)) {
            this.availableParameterKeys.add(key);
        }
    }

    public <T> void put(PropertyKey.Key<T> key, T t) {
        if (key == null) {
            LOGGER.warn("Put Invalid key null!", new Object[0]);
        } else {
            this.availableProperties.put(key, t);
        }
    }

    public <T> void put(PropertyKey.Key<T> key) {
        if (key == null) {
            LOGGER.warn("Key is null", new Object[0]);
            return;
        }
        try {
            this.availableProperties.put(key, this.cameraAbility.getPropertyValue(key));
        } catch (IllegalArgumentException unused) {
            LOGGER.warn("Key id invalid : %{public}s", key);
        }
    }

    public void put(List<PropertyKey.Key<?>> list) {
        if (list != null) {
            for (PropertyKey.Key<?> key : list) {
                put(key);
            }
        }
    }

    public void setConflictFunctions(Set<PropertyKey.Key<?>> set) {
        this.conflictFunctions = set;
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public boolean isPreviewSupported() {
        return this.actionList.contains(1);
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public int getMaxPreviewSurfaceNumber() {
        return this.maxPreviewSurfaceNumber;
    }

    public void setMaxPreviewSurfaceNumber(int i) {
        this.maxPreviewSurfaceNumber = i;
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public boolean isCaptureSupported() {
        return this.actionList.contains(2);
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public boolean isBurstSupported() {
        if (CameraUtil.isFrontCamera(this.cameraAbility)) {
            return false;
        }
        return this.actionList.contains(3);
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public boolean isVideoSupported() {
        return this.actionList.contains(4);
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public <T> List<Size> getSupportedPreviewSizes(Class<T> cls) {
        if (!SUPPORTED_PREVIEW_CLASS_LIST.contains(cls)) {
            return Collections.emptyList();
        }
        if (this.classOutputSizesMaps.containsKey(cls)) {
            return this.classOutputSizesMaps.get(cls);
        }
        return Collections.emptyList();
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public List<Size> getSupportedCaptureSizes(int i) {
        if (this.formatOutputSizesMaps.containsKey(Integer.valueOf(i))) {
            return this.formatOutputSizesMaps.get(Integer.valueOf(i));
        }
        return Collections.emptyList();
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public <T> Map<Integer, List<Size>> getSupportedVideoSizes(Class<T> cls) {
        Map<Integer, List<Size>> map;
        if (!SUPPORTED_VIDEO_CLASSES_LIST.contains(cls)) {
            return Collections.emptyMap();
        }
        HashMap hashMap = new HashMap();
        if (this.classOutputSizesMaps.containsKey(cls)) {
            hashMap.put(30, this.classOutputSizesMaps.get(cls));
        }
        if (this.classFpsOutputSizesMap.containsKey(cls) && (map = this.classFpsOutputSizesMap.get(cls)) != null && !map.isEmpty()) {
            hashMap.putAll(map);
        }
        return hashMap;
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public int[] getSupportedAutoFocus() {
        return this.cameraAbility.getSupportedAfMode();
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public float[] getSupportedZoom() {
        Float[] fArr = (Float[]) getSupported(ModeCharacteristicKey.ZOOM_RANGE);
        return (fArr == null || fArr.length != DEFAULT_ZOOM_RANGES.length) ? (float[]) DEFAULT_ZOOM_RANGES.clone() : new float[]{fArr[0].floatValue(), fArr[1].floatValue()};
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public int[] getSupportedFlashMode() {
        int[] iArr = (int[]) getSupported(ModeCharacteristicKey.FLASH_MODE_RANGE);
        return iArr != null ? iArr : new int[0];
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public int[] getSupportedFaceDetection() {
        Boolean bool = (Boolean) getSupported(ModeCharacteristicKey.SMILE_DETECTION_FUNCTION);
        if (bool != null && bool.booleanValue()) {
            return new int[]{1, 2};
        }
        Boolean bool2 = (Boolean) getSupported(ModeCharacteristicKey.FACE_DETECTION_FUNCTION);
        return (bool2 == null || !bool2.booleanValue()) ? new int[0] : new int[]{1};
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public int[] getSupportedBeauty(int i) {
        PropertyKey.Key<int[]> key;
        if (i == 1) {
            key = ModeCharacteristicKey.SKIN_SMOOTH_RANGE;
        } else if (i == 2) {
            key = ModeCharacteristicKey.FACE_SLENDER_RANGE;
        } else if (i == 3) {
            key = ModeCharacteristicKey.SKIN_TONE_RANGE;
        } else if (i != 4) {
            return new int[0];
        } else {
            key = ModeCharacteristicKey.BODY_SHAPING_RANGE;
        }
        int[] iArr = (int[]) getSupported(key);
        return iArr != null ? iArr : new int[0];
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public int[] getSupportedColorMode() {
        int[] iArr = (int[]) getSupported(ModeCharacteristicKey.COLOR_MODE_RANGE);
        return iArr != null ? iArr : new int[0];
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public boolean getSupportedSceneDetection() {
        Boolean bool = (Boolean) getSupported(ModeCharacteristicKey.SCENE_DETECTION_FUNCTION);
        return bool != null && bool.booleanValue();
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public List<ParameterKey.Key<?>> getSupportedParameters() {
        return Collections.unmodifiableList(this.availableParameterKeys);
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public <T> List<T> getParameterRange(ParameterKey.Key<T> key) {
        Pair<Boolean, Pair<PropertyKey.Key<?>, ModeRequestKey.CheckValid<?>>> rangeKey = ModeRequestKey.getRangeKey(key);
        int i = 0;
        if (rangeKey == null) {
            LOGGER.warn("Cannot find key：%{public}s", key);
            return Collections.emptyList();
        }
        Object supported = getSupported(rangeKey.s.f);
        if (supported == null) {
            return Collections.emptyList();
        }
        if (rangeKey.f.booleanValue()) {
            ArrayList arrayList = new ArrayList();
            if (supported instanceof byte[]) {
                byte[] bArr = (byte[]) supported;
                int length = bArr.length;
                while (i < length) {
                    arrayList.add(Byte.valueOf(bArr[i]));
                    i++;
                }
            } else if (supported instanceof int[]) {
                int[] iArr = (int[]) supported;
                int length2 = iArr.length;
                while (i < length2) {
                    arrayList.add(Integer.valueOf(iArr[i]));
                    i++;
                }
            } else if (supported instanceof float[]) {
                float[] fArr = (float[]) supported;
                int length3 = fArr.length;
                while (i < length3) {
                    arrayList.add(Float.valueOf(fArr[i]));
                    i++;
                }
            } else if ((supported instanceof Float) || (supported instanceof Long) || (supported instanceof Integer)) {
                arrayList.add(supported);
            } else {
                Object[] objArr = (Object[]) supported;
                int length4 = objArr.length;
                while (i < length4) {
                    arrayList.add(objArr[i]);
                    i++;
                }
            }
            return arrayList;
        } else if (((Boolean) supported).booleanValue()) {
            return Arrays.asList(true, false);
        } else {
            return Arrays.asList(false);
        }
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public List<PropertyKey.Key<?>> getSupportedProperties() {
        return Collections.unmodifiableList(new ArrayList(this.availableProperties.keySet()));
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public <T> T getPropertyValue(PropertyKey.Key<T> key) {
        if (key == null) {
            LOGGER.warn("Get invalid key null!", new Object[0]);
            return null;
        } else if (this.availableProperties.containsKey(key)) {
            T t = (T) this.availableProperties.get(key);
            LOGGER.debug("get key: %{public}s, value: %{public}s", key, t);
            return t;
        } else {
            throw new IllegalArgumentException(key + " was not supported!");
        }
    }

    @Override // ohos.media.camera.mode.ModeAbility
    public Set<Integer> getConflictActions() {
        return (Set) this.conflictFunctions.stream().map($$Lambda$ModeAbilityImpl$2oJWi6eypFJWEUp03KFFj8p93UQ.INSTANCE).collect(Collectors.toSet());
    }

    private <T> T getSupported(PropertyKey.Key<T> key) {
        T t;
        if (key == null || !this.availableProperties.containsKey(key) || (t = (T) this.availableProperties.get(key)) == null) {
            return null;
        }
        return t;
    }

    public void dump() {
        LOGGER.debug("formatOutputSizesMaps: %{public}s", this.formatOutputSizesMaps.toString());
        LOGGER.debug("classOutputSizesMaps: %{public}s", this.classOutputSizesMaps.toString());
        LOGGER.debug("functionFormatOutputSizesMap: %{public}s", this.functionFormatOutputSizesMap.toString());
        LOGGER.debug("functionClassOutputSizesMap: %{public}s", this.functionClassOutputSizesMap.toString());
    }
}
