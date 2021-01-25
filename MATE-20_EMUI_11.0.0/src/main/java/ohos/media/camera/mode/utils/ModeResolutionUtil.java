package ohos.media.camera.mode.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.Mode;
import ohos.media.camera.mode.adapter.key.ModeCharacteristicKey;
import ohos.media.camera.mode.adapter.utils.CustomConfigurationUtil;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.impl.ModeImpl;
import ohos.media.camera.mode.tags.HuaweiTags;
import ohos.media.camera.params.PropertyKey;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.image.common.Size;
import ohos.media.recorder.Recorder;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class ModeResolutionUtil {
    private static final List<Size> AI_MOVIE_SUPPORTED_RESOLUTION_LIST = Arrays.asList(VIDEO_MEDIARECODER_SIZE_720P_16_9, VIDEO_MEDIARECODER_SIZE_1080P_16_9, VIDEO_MEDIARECODER_SIZE_720P_21_9, VIDEO_MEDIARECODER_SIZE_1080P_21_9, VIDEO_MEDIARECODER_SIZE_2160_1080, VIDEO_MEDIARECODER_SIZE_2336_1080);
    private static final int DEFAULT_MAP_SIZE = 16;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ModeResolutionUtil.class);
    private static final List<String> SPECIAL_PRODUCTS = new ArrayList();
    private static final Size VIDEO_MEDIARECODER_SIZE_1080P_16_9 = new Size(1920, 1080);
    private static final Size VIDEO_MEDIARECODER_SIZE_1080P_21_9 = new Size(2560, 1080);
    private static final Size VIDEO_MEDIARECODER_SIZE_2160_1080 = new Size(2160, 1080);
    private static final Size VIDEO_MEDIARECODER_SIZE_2336_1080 = new Size(2336, 1080);
    private static final Size VIDEO_MEDIARECODER_SIZE_4K = new Size(3840, 2160);
    private static final Size VIDEO_MEDIARECODER_SIZE_720P_16_9 = new Size(1280, 720);
    private static final Size VIDEO_MEDIARECODER_SIZE_720P_21_9 = new Size(1680, 720);
    private static final Size VIDEO_MEDIARECODER_SIZE_BASIC = new Size(2560, 1080);
    private static Map<Class<?>, Map<Integer, List<Size>>> allClassFpsOutputSizes;
    private static Map<Class<?>, List<Size>> allClassOutputSizes;
    private static Map<Integer, List<Size>> allFormatOutputSizes;
    private static double ratioThresholds = (CustomConfigurationUtil.isFoldDispProduct() ? 0.08d : 0.17d);
    private static Comparator<Size> sizeComparator = new Comparator<Size>() {
        /* class ohos.media.camera.mode.utils.ModeResolutionUtil.AnonymousClass1 */

        public int compare(Size size, Size size2) {
            if (size == null && size2 == null) {
                ModeResolutionUtil.LOGGER.error("o1 and o2 is null", new Object[0]);
                return 0;
            } else if (size == null) {
                ModeResolutionUtil.LOGGER.error("o1 is null", new Object[0]);
                return -1;
            } else if (size2 != null) {
                return (size.height * size.width) - (size2.height * size2.width);
            } else {
                ModeResolutionUtil.LOGGER.error("o2 is null", new Object[0]);
                return 1;
            }
        }
    };

    static {
        SPECIAL_PRODUCTS.add("WIN");
        SPECIAL_PRODUCTS.add("WLZ");
    }

    private ModeResolutionUtil() {
    }

    private static Map<Class<?>, List<Size>> getClassOutputSizes(@Mode.Type int i, CameraAbilityImpl cameraAbilityImpl) {
        HashMap hashMap;
        LOGGER.begin("getClassOutputSizes");
        if (i == 5 || i == 10) {
            HashMap hashMap2 = new HashMap(ModeImpl.VIDEO_SUPPORTED_CLASS.size());
            List<List<Size>> previewSupports = VideoResolutionUtil.getPreviewSupports(cameraAbilityImpl, ModeImpl.VIDEO_SUPPORTED_CLASS);
            int size = ModeImpl.VIDEO_SUPPORTED_CLASS.size();
            String productName = CustomConfigurationUtil.getProductName();
            if (("FRO".equals(productName) || "ELS".equals(productName)) && i == 10) {
                for (List<Size> list : previewSupports) {
                    list.remove(VIDEO_MEDIARECODER_SIZE_2336_1080);
                }
            }
            if (size != previewSupports.size()) {
                return hashMap2;
            }
            for (int i2 = 0; i2 < size; i2++) {
                hashMap2.put(ModeImpl.VIDEO_SUPPORTED_CLASS.get(i2), previewSupports.get(i2));
            }
            hashMap = hashMap2;
        } else {
            hashMap = new HashMap(ModeImpl.PHOTO_SUPPORTED_CLASS.size());
            for (Class<?> cls : ModeImpl.PHOTO_SUPPORTED_CLASS) {
                hashMap.put(cls, PhotoResolutionUtil.getPreviewSupports(cameraAbilityImpl, i, cls));
            }
        }
        LOGGER.end("getClassOutputSizes");
        return hashMap;
    }

    public static Map<Integer, List<Size>> getFormatOutputSizes(@Mode.Type int i, CameraAbilityImpl cameraAbilityImpl) {
        Object obj;
        LOGGER.begin("getFormatOutputSizes");
        HashMap hashMap = new HashMap(ModeImpl.SUPPORTED_FORMATS.size());
        if (!(i == 7 || i == 8)) {
            for (Integer num : ModeImpl.SUPPORTED_FORMATS) {
                int intValue = num.intValue();
                if (intValue != 3) {
                    obj = new ArrayList();
                } else if (i == 5 || i == 10) {
                    obj = VideoResolutionUtil.getCaptureSupports(cameraAbilityImpl);
                } else {
                    obj = PhotoResolutionUtil.getCaptureSupports(cameraAbilityImpl, i, DeviceUtil.getMaxScreenRatio());
                }
                hashMap.put(Integer.valueOf(intValue), obj);
            }
            LOGGER.end("getFormatOutputSizes");
        }
        return hashMap;
    }

    public static void addResolutionCapability(ModeAbilityImpl modeAbilityImpl, @Mode.Type int i, List<PropertyKey.Key<?>> list, CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("addResolutionCapability");
        allClassOutputSizes = getClassOutputSizes(i, cameraAbilityImpl);
        allFormatOutputSizes = getFormatOutputSizes(i, cameraAbilityImpl);
        addFunctionResolutionCapability(modeAbilityImpl, i, list, cameraAbilityImpl);
        addModeResolutionCapability(modeAbilityImpl, i, list, cameraAbilityImpl);
        allClassOutputSizes = null;
        allFormatOutputSizes = null;
        allClassFpsOutputSizes = null;
        LOGGER.end("addResolutionCapability");
    }

    private static void addFunctionResolutionCapability(ModeAbilityImpl modeAbilityImpl, @Mode.Type int i, List<PropertyKey.Key<?>> list, CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("addFunctionResolutionCapability");
        if (!(list == null || modeAbilityImpl == null || cameraAbilityImpl == null)) {
            modeAbilityImpl.setFunctionFormatOutputSizesMap(new HashMap<>(list.size()));
            Map<PropertyKey.Key<?>, Map<Class<?>, List<Size>>> hashMap = new HashMap<>(list.size());
            modeAbilityImpl.setFunctionClassOutputSizesMap(hashMap);
            List<Size> list2 = allClassOutputSizes.get(Recorder.class);
            for (PropertyKey.Key<?> key : list) {
                if (key.equals(ModeCharacteristicKey.BEAUTY_FUNCTION) && i == 5) {
                    boolean isSupported = HuaweiTags.isSupported(cameraAbilityImpl, InnerPropertyKey.BEAUTY_STABILIZATION_SUPPORTED);
                    boolean isSupported2 = HuaweiTags.isSupported(cameraAbilityImpl, InnerPropertyKey.HIGH_RESOLUTION_BEAUTY_SUPPORTED);
                    Map<Class<?>, List<Size>> hashMap2 = new HashMap<>(16);
                    if (isSupported) {
                        List<Size> arrayList = new ArrayList<>(list2.size());
                        arrayList.addAll(list2);
                        arrayList.removeAll(Arrays.asList(VIDEO_MEDIARECODER_SIZE_4K));
                        hashMap2.put(Recorder.class, arrayList);
                    } else if (isSupported2) {
                        hashMap2.put(Recorder.class, Arrays.asList(VIDEO_MEDIARECODER_SIZE_720P_16_9, VIDEO_MEDIARECODER_SIZE_1080P_16_9, VIDEO_MEDIARECODER_SIZE_2336_1080));
                    } else {
                        hashMap2.put(Recorder.class, Arrays.asList(VIDEO_MEDIARECODER_SIZE_720P_16_9));
                    }
                    hashMap.put(key, hashMap2);
                }
                if (key.equals(ModeCharacteristicKey.FILTER_EFFECT_FUNCTION) || key.equals(ModeCharacteristicKey.AI_MOVIE_FUNCTION)) {
                    Map<Class<?>, List<Size>> hashMap3 = new HashMap<>(1);
                    hashMap3.put(Recorder.class, AI_MOVIE_SUPPORTED_RESOLUTION_LIST);
                    hashMap.put(key, hashMap3);
                }
            }
            LOGGER.debug("functionClassOutputSizeMap: %{public}s", hashMap);
            LOGGER.end("addFunctionResolutionCapability");
        }
    }

    private static void addModeResolutionCapability(ModeAbilityImpl modeAbilityImpl, @Mode.Type int i, List<PropertyKey.Key<?>> list, CameraAbilityImpl cameraAbilityImpl) {
        LOGGER.begin("addModeResolutionCapability");
        if (!(modeAbilityImpl == null || cameraAbilityImpl == null)) {
            Map<PropertyKey.Key<?>, Map<Class<?>, List<Size>>> functionClassOutputSizesMap = modeAbilityImpl.getFunctionClassOutputSizesMap();
            HashMap hashMap = new HashMap(allClassOutputSizes);
            HashMap hashMap2 = new HashMap(allFormatOutputSizes);
            if (functionClassOutputSizesMap == null || functionClassOutputSizesMap.isEmpty()) {
                modeAbilityImpl.setClassOutputSizes(hashMap);
                modeAbilityImpl.setFormatOutputSizes(hashMap2);
                modeAbilityImpl.setClassFpsOutputSizes(allClassFpsOutputSizes);
                return;
            }
            List<Size> findLessOrEqualList = findLessOrEqualList(hashMap.get(Recorder.class), VIDEO_MEDIARECODER_SIZE_BASIC);
            if (i == 5 || i == 10) {
                ArrayList arrayList = null;
                for (Map.Entry<PropertyKey.Key<?>, Map<Class<?>, List<Size>>> entry : functionClassOutputSizesMap.entrySet()) {
                    List<Size> list2 = entry.getValue().get(Recorder.class);
                    if (list2 != null) {
                        if (arrayList == null) {
                            arrayList = new ArrayList(list2.size());
                            arrayList.addAll(list2);
                        } else {
                            arrayList.retainAll(list2);
                        }
                    }
                }
                if (CollectionUtil.isEmptyCollection(arrayList) || !isLessThan(VIDEO_MEDIARECODER_SIZE_BASIC, findMaxSize(arrayList))) {
                    if (functionClassOutputSizesMap.containsKey(ModeCharacteristicKey.AI_MOVIE_FUNCTION)) {
                        findLessOrEqualList.retainAll(AI_MOVIE_SUPPORTED_RESOLUTION_LIST);
                    }
                    hashMap.put(Recorder.class, removeUnsupportedStabilitySizes(findLessOrEqualList, i, cameraAbilityImpl));
                } else {
                    hashMap.put(Recorder.class, removeUnsupportedStabilitySizes(arrayList, i, cameraAbilityImpl));
                }
            }
            LOGGER.info("modeClassOutputSizes = %{public}s", hashMap);
            modeAbilityImpl.setClassOutputSizes(hashMap);
            modeAbilityImpl.setFormatOutputSizes(hashMap2);
            LOGGER.end("addModeResolutionCapability");
        }
    }

    private static Size findMaxSize(List<Size> list) {
        return (Size) Collections.max(list, sizeComparator);
    }

    private static List<Size> findLessOrEqualList(List<Size> list, Size size) {
        if (list == null) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList(list.size());
        for (Size size2 : list) {
            if (sizeComparator.compare(size2, size) <= 0) {
                arrayList.add(size2);
            }
        }
        return arrayList;
    }

    private static boolean isLessThan(Size size, Size size2) {
        return sizeComparator.compare(size, size2) < 0;
    }

    private static List<Size> removeUnsupportedStabilitySizes(List<Size> list, @Mode.Type int i, CameraAbilityImpl cameraAbilityImpl) {
        if (CameraUtil.isBackCamera(cameraAbilityImpl) && i == 5) {
            list.retainAll(VideoResolutionUtil.getVideoStabilizeSupports(cameraAbilityImpl, 30));
        }
        return list;
    }
}
