package ohos.media.camera.mode.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.Mode;
import ohos.media.camera.mode.adapter.key.ModeCharacteristicKey;
import ohos.media.camera.mode.impl.ModeAbilityImpl;
import ohos.media.camera.mode.tags.hisi.HisiPortraitTags;
import ohos.media.camera.mode.tags.hisi.HisiVideoTags;
import ohos.media.camera.mode.tags.hisi.utils.HisiColorCapabilityUtil;
import ohos.media.camera.mode.tags.hisi.utils.HisiFilterCapabilityUtil;
import ohos.media.camera.mode.tags.hisi.utils.HisiFlashCapabilityUtil;
import ohos.media.camera.mode.tags.hisi.utils.HisiMirrorCapabilityUtil;
import ohos.media.camera.mode.tags.hisi.utils.HisiSensorHdrCapabilityUtil;
import ohos.media.camera.mode.tags.hisi.utils.HisiSmartCaptureCapabilityUtil;
import ohos.media.camera.mode.tags.hisi.utils.HisiWaterMarkCapabilityUtil;
import ohos.media.camera.mode.tags.hisi.utils.HisiZoomCapabilityUtil;
import ohos.media.camera.params.ParameterKey;
import ohos.media.camera.params.PropertyKey;
import ohos.media.camera.params.adapter.InnerParameterKey;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.image.common.Size;
import ohos.media.recorder.Recorder;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class ModeFunctionUtil {
    private static final String ABILITY_WARNING_INFO = "the value modeAbility is null";
    private static final int COORDINATE_HALF_LENGTH = 1000;
    private static final int DEFAULT_MAP_SIZE = 16;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(ModeFunctionUtil.class);
    private static final String MODE_ABILITY_WARNING_INFO = "the value modeAbility is null";
    private static Processor beautyProcessor = $$Lambda$ModeFunctionUtil$UNVug8ZY7yIOY6UaQRYYEvfuJU.INSTANCE;
    private static Processor flashProcessor = $$Lambda$ModeFunctionUtil$nn2WcbgVnsgm1uNJ7qGRyQlb34.INSTANCE;
    private static Map<PropertyKey.Key<?>, Processor> functionTypeProcessorsMap = new HashMap(16);
    private static Processor zoomProcessor = $$Lambda$ModeFunctionUtil$5f4daakcHACEHBsxQZFedqT7Gcc.INSTANCE;

    /* access modifiers changed from: package-private */
    public interface Processor {
        void process(ModeAbilityImpl modeAbilityImpl, @Mode.Type int i, List<PropertyKey.Key<?>> list, CameraAbilityImpl cameraAbilityImpl);
    }

    static {
        functionTypeProcessorsMap.put(ModeCharacteristicKey.BEAUTY_FUNCTION, beautyProcessor);
        functionTypeProcessorsMap.put(InnerPropertyKey.FACE_DETECT_MODE, $$Lambda$ModeFunctionUtil$ciVvgjk4nG5c73re_PC3wYunx6s.INSTANCE);
        functionTypeProcessorsMap.put(ModeCharacteristicKey.FACE_DETECTION_FUNCTION, $$Lambda$ModeFunctionUtil$g13UC4Ls8jHyifxt_90ZdZm2A6w.INSTANCE);
        functionTypeProcessorsMap.put(ModeCharacteristicKey.SMILE_DETECTION_FUNCTION, $$Lambda$ModeFunctionUtil$bWorJXx4P4FpQbwvdnEgec3SFs.INSTANCE);
        functionTypeProcessorsMap.put(ModeCharacteristicKey.ZOOM_FUNCTION, zoomProcessor);
        functionTypeProcessorsMap.put(ModeCharacteristicKey.FLASH_MODE_FUNCTION, flashProcessor);
        functionTypeProcessorsMap.put(ModeCharacteristicKey.SENSOR_HDR_FUNCTION, $$Lambda$ModeFunctionUtil$6h_YnPHvc9lwvN7WEobaXnCdbXQ.INSTANCE);
        functionTypeProcessorsMap.put(ModeCharacteristicKey.WATER_MARK_FUNCTION, $$Lambda$ModeFunctionUtil$o29hqQo31qMsRTiXpxUQT2NJPmg.INSTANCE);
        functionTypeProcessorsMap.put(ModeCharacteristicKey.MIRROR_FUNCTION, $$Lambda$ModeFunctionUtil$7CqQhDO5yYgDRZzwU3etRExX2W4.INSTANCE);
        functionTypeProcessorsMap.put(ModeCharacteristicKey.AI_MOVIE_FUNCTION, $$Lambda$ModeFunctionUtil$9PP8ja_oUJTuMNTknEjsoP59E.INSTANCE);
        functionTypeProcessorsMap.put(ModeCharacteristicKey.LOCATION_FUNCTION, $$Lambda$ModeFunctionUtil$8HeFw5BZZYSiDF3SHV_pOr0jYo4.INSTANCE);
        functionTypeProcessorsMap.put(ModeCharacteristicKey.SMART_CAPTURE_FUNCTION, $$Lambda$ModeFunctionUtil$26KYFEAGv3rS7vf_afh8JWqPkU.INSTANCE);
        functionTypeProcessorsMap.put(ModeCharacteristicKey.SCENE_DETECTION_FUNCTION, $$Lambda$ModeFunctionUtil$7LcNaGJaHukQPR9mDCPZItVrMSQ.INSTANCE);
        functionTypeProcessorsMap.put(ModeCharacteristicKey.FILTER_EFFECT_FUNCTION, $$Lambda$ModeFunctionUtil$AnXxcdbN5OxzVryUEkGmASvng.INSTANCE);
        functionTypeProcessorsMap.put(ModeCharacteristicKey.VIDEO_STABILIZATION_FUNCTION, $$Lambda$ModeFunctionUtil$givNn2loC3GsFLOm3OS1BZimiHE.INSTANCE);
        functionTypeProcessorsMap.put(ModeCharacteristicKey.COLOR_MODE_FUNCTION, $$Lambda$ModeFunctionUtil$W7rMRe_63vP8bzUshnohWhgTkYg.INSTANCE);
    }

    static /* synthetic */ void lambda$static$0(ModeAbilityImpl modeAbilityImpl, int i, List list, CameraAbilityImpl cameraAbilityImpl) {
        if (i == 5) {
            List<Size> resolution = getResolution(modeAbilityImpl, ModeCharacteristicKey.BEAUTY_FUNCTION, Recorder.class);
            Map<Integer, List<Size>> supportedVideoSizes = modeAbilityImpl.getSupportedVideoSizes(Recorder.class);
            List<Size> arrayList = new ArrayList<>();
            if (supportedVideoSizes != null && supportedVideoSizes.containsKey(30)) {
                arrayList = supportedVideoSizes.get(30);
            }
            if (!resolution.containsAll(arrayList)) {
                LOGGER.info("Do not open beauty because size miss, funSizeList = %{public}s", resolution);
            } else {
                addVideoBeautyCapability(modeAbilityImpl, cameraAbilityImpl);
            }
        } else {
            addBeautyCapability(modeAbilityImpl, cameraAbilityImpl);
        }
    }

    private ModeFunctionUtil() {
    }

    public static void addBeautyCapability(ModeAbilityImpl modeAbilityImpl, CameraAbilityImpl cameraAbilityImpl) {
        if (modeAbilityImpl == null || cameraAbilityImpl == null) {
            LOGGER.error("addBeautyCapability: null modeAbility or null cameraAbility!", new Object[0]);
            return;
        }
        int[] supportBeautyTypes = HisiPortraitTags.getSupportBeautyTypes(cameraAbilityImpl);
        if (supportBeautyTypes.length != 0) {
            modeAbilityImpl.put(ModeCharacteristicKey.BEAUTY_FUNCTION, true);
            modeAbilityImpl.put(ModeCharacteristicKey.BEAUTY_TYPES, supportBeautyTypes);
            if (HisiPortraitTags.isUnifySkinSmoothAvailable(cameraAbilityImpl)) {
                modeAbilityImpl.put(ModeCharacteristicKey.SKIN_SMOOTH_RANGE, HisiPortraitTags.getUnifySkinSmoothRange(cameraAbilityImpl));
            }
            if (HisiPortraitTags.isUnifyFaceSlenderAvailable(cameraAbilityImpl)) {
                modeAbilityImpl.put(ModeCharacteristicKey.FACE_SLENDER_RANGE, HisiPortraitTags.getUnifyFaceSlenderRange(cameraAbilityImpl));
            }
            if (HisiPortraitTags.isSkinToneAvailable(cameraAbilityImpl)) {
                modeAbilityImpl.put(ModeCharacteristicKey.SKIN_TONE_RANGE, HisiPortraitTags.getSkinToneRange(cameraAbilityImpl));
            }
            if (HisiPortraitTags.isBodyShapingAvailable(cameraAbilityImpl)) {
                modeAbilityImpl.put(ModeCharacteristicKey.BODY_SHAPING_RANGE, HisiPortraitTags.getBodyShapingRange(cameraAbilityImpl));
            }
        }
    }

    public static void addVideoBeautyCapability(ModeAbilityImpl modeAbilityImpl, CameraAbilityImpl cameraAbilityImpl) {
        if (modeAbilityImpl == null) {
            LOGGER.error("addVideoBeautyCapability:ModeAbilityImpl is null!", new Object[0]);
        } else if (cameraAbilityImpl == null) {
            LOGGER.error("addVideoBeautyCapability:cameraAbility is null!", new Object[0]);
        } else {
            int[] supportBeautyTypes = HisiVideoTags.getSupportBeautyTypes(cameraAbilityImpl);
            if (supportBeautyTypes.length != 0) {
                modeAbilityImpl.put(ModeCharacteristicKey.BEAUTY_FUNCTION, true);
                modeAbilityImpl.put(ModeCharacteristicKey.BEAUTY_TYPES, supportBeautyTypes);
                if (HisiVideoTags.isUnifySkinSmoothAvailable(cameraAbilityImpl)) {
                    modeAbilityImpl.put(ModeCharacteristicKey.SKIN_SMOOTH_RANGE, HisiVideoTags.getUnifySkinSmoothRange(cameraAbilityImpl));
                }
                if (HisiVideoTags.isBodyShapingAvailable(cameraAbilityImpl)) {
                    modeAbilityImpl.put(ModeCharacteristicKey.BODY_SHAPING_RANGE, HisiVideoTags.getBodyShapingRange(cameraAbilityImpl));
                }
            }
        }
    }

    public static void addFaceDetectionCapability(ModeAbilityImpl modeAbilityImpl, CameraAbilityImpl cameraAbilityImpl) {
        if (modeAbilityImpl == null) {
            LOGGER.error("addFaceDetectionCapability:modeAbility is null", new Object[0]);
        } else if (cameraAbilityImpl == null) {
            LOGGER.error("addFaceDetectionCapability:cameraAbility is null", new Object[0]);
        } else {
            modeAbilityImpl.put(ModeCharacteristicKey.FACE_DETECTION_FUNCTION, true);
        }
    }

    public static void addFaceDetectModeCapability(ModeAbilityImpl modeAbilityImpl, CameraAbilityImpl cameraAbilityImpl) {
        if (modeAbilityImpl == null || cameraAbilityImpl == null) {
            LOGGER.error("addFaceDetectModeCapability: null modeAbility or null cameraAbility!", new Object[0]);
            return;
        }
        modeAbilityImpl.addAvailableParameterKey(InnerParameterKey.FACE_DETECTION_TYPE);
        modeAbilityImpl.put(InnerPropertyKey.FACE_DETECT_MODE, new Integer[]{1, 2, 0});
    }

    public static void addSmileDetectionCapability(ModeAbilityImpl modeAbilityImpl, CameraAbilityImpl cameraAbilityImpl) {
        if (modeAbilityImpl == null) {
            LOGGER.error("the value modeAbility is null", new Object[0]);
            return;
        }
        modeAbilityImpl.put(ModeCharacteristicKey.FACE_DETECTION_FUNCTION, true);
        modeAbilityImpl.put(ModeCharacteristicKey.SMILE_DETECTION_FUNCTION, true);
    }

    public static void addZoomCapability(ModeAbilityImpl modeAbilityImpl, CameraAbilityImpl cameraAbilityImpl, String str) {
        if (modeAbilityImpl == null) {
            LOGGER.error("the value modeAbility is null", new Object[0]);
        } else if (cameraAbilityImpl == null) {
            LOGGER.error("the value modeAbility is null", new Object[0]);
        } else if (StringUtil.isEmptyString(str)) {
            LOGGER.error("modeName value is null", new Object[0]);
        } else {
            modeAbilityImpl.put(ModeCharacteristicKey.ZOOM_FUNCTION, true);
            modeAbilityImpl.put(ModeCharacteristicKey.ZOOM_RANGE, HisiZoomCapabilityUtil.getZoomLevelRange(str, cameraAbilityImpl));
        }
    }

    public static void addColorModeCapability(ModeAbilityImpl modeAbilityImpl, CameraAbilityImpl cameraAbilityImpl) {
        if (modeAbilityImpl == null || cameraAbilityImpl == null) {
            LOGGER.error("addColorModeCapability: null modeAbility or null cameraAbility!", new Object[0]);
        } else if (HisiColorCapabilityUtil.isAvailable(cameraAbilityImpl)) {
            modeAbilityImpl.put(ModeCharacteristicKey.COLOR_MODE_FUNCTION, true);
            modeAbilityImpl.put(ModeCharacteristicKey.COLOR_MODE_RANGE, HisiColorCapabilityUtil.getSupportedColorModes(cameraAbilityImpl));
        }
    }

    public static void addFlashModeCapability(ModeAbilityImpl modeAbilityImpl, CameraAbilityImpl cameraAbilityImpl, String str) {
        if (modeAbilityImpl == null || cameraAbilityImpl == null || StringUtil.isEmptyString(str)) {
            LOGGER.error("addFlashModeCapability: null modeAbility or null cameraAbility or null modeName!", new Object[0]);
        } else if (HisiFlashCapabilityUtil.isAvailable(cameraAbilityImpl)) {
            modeAbilityImpl.put(ModeCharacteristicKey.FLASH_MODE_FUNCTION, true);
            modeAbilityImpl.put(ModeCharacteristicKey.FLASH_MODE_RANGE, HisiFlashCapabilityUtil.getSupportedFlashModes(str, cameraAbilityImpl));
        }
    }

    public static void addSensorHdrCapability(ModeAbilityImpl modeAbilityImpl, CameraAbilityImpl cameraAbilityImpl) {
        if (modeAbilityImpl == null || cameraAbilityImpl == null) {
            LOGGER.error("addSensorHdrCapability: null modeAbility or null cameraAbility!", new Object[0]);
        } else if (HisiSensorHdrCapabilityUtil.isAvailable(cameraAbilityImpl)) {
            modeAbilityImpl.addAvailableParameterKey(ParameterKey.SENSOR_HDR);
            modeAbilityImpl.put(ModeCharacteristicKey.SENSOR_HDR_FUNCTION, true);
        }
    }

    public static void addWaterMarkCapability(ModeAbilityImpl modeAbilityImpl, CameraAbilityImpl cameraAbilityImpl) {
        if (modeAbilityImpl == null || cameraAbilityImpl == null) {
            LOGGER.error("addWaterMarkCapability: null modeAbility or null cameraAbility!", new Object[0]);
        } else if (KitUtil.isHwApp() && HisiWaterMarkCapabilityUtil.isAvailable(cameraAbilityImpl)) {
            modeAbilityImpl.addAvailableParameterKey(InnerParameterKey.WATER_MARK);
            modeAbilityImpl.put(ModeCharacteristicKey.WATER_MARK_FUNCTION, true);
        }
    }

    public static void addMirrorCapability(ModeAbilityImpl modeAbilityImpl, CameraAbilityImpl cameraAbilityImpl) {
        if (modeAbilityImpl == null) {
            LOGGER.error("the value modeAbility is null", new Object[0]);
        } else if (cameraAbilityImpl == null) {
            LOGGER.error("the value modeAbility is null", new Object[0]);
        } else if (HisiMirrorCapabilityUtil.isAvailable(cameraAbilityImpl)) {
            modeAbilityImpl.addAvailableParameterKey(ParameterKey.IMAGE_MIRROR);
            modeAbilityImpl.put(ModeCharacteristicKey.MIRROR_FUNCTION, true);
        }
    }

    public static void addLocationCapability(ModeAbilityImpl modeAbilityImpl, CameraAbilityImpl cameraAbilityImpl) {
        if (modeAbilityImpl == null) {
            LOGGER.error("the value modeAbility is null", new Object[0]);
        } else {
            modeAbilityImpl.put(ModeCharacteristicKey.LOCATION_FUNCTION, true);
        }
    }

    public static void addSmartCaptureCapability(ModeAbilityImpl modeAbilityImpl, CameraAbilityImpl cameraAbilityImpl) {
        if (modeAbilityImpl == null) {
            LOGGER.error("addSmartCaptureCapability:modeAbility is null", new Object[0]);
        } else if (cameraAbilityImpl == null) {
            LOGGER.error("addSmartCaptureCapability:cameraAbility is null", new Object[0]);
        } else if (KitUtil.isHwApp() && HisiSmartCaptureCapabilityUtil.isSmartCaptureOneAvailable(cameraAbilityImpl)) {
            modeAbilityImpl.addAvailableParameterKey(InnerParameterKey.SMART_CAPTURE);
            modeAbilityImpl.put(ModeCharacteristicKey.SMART_CAPTURE_FUNCTION, true);
        }
    }

    /* access modifiers changed from: private */
    public static void addSceneDetectionCapability(ModeAbilityImpl modeAbilityImpl, CameraAbilityImpl cameraAbilityImpl) {
        if (HisiSmartCaptureCapabilityUtil.isMasterAiAvailable(cameraAbilityImpl)) {
            modeAbilityImpl.put(ModeCharacteristicKey.SCENE_DETECTION_FUNCTION, true);
            modeAbilityImpl.addAvailableParameterKey(ParameterKey.SCENE_EFFECT_ENABLE);
        }
    }

    public static void addVideoAiMovieCapability(ModeAbilityImpl modeAbilityImpl, CameraAbilityImpl cameraAbilityImpl) {
        if (modeAbilityImpl != null && cameraAbilityImpl != null && HisiVideoTags.isSupportAiMovie(cameraAbilityImpl)) {
            modeAbilityImpl.addAvailableParameterKey(ParameterKey.AI_MOVIE);
            modeAbilityImpl.put(ModeCharacteristicKey.AI_MOVIE_FUNCTION, true);
            Byte[] supportAiMovieRange = HisiVideoTags.getSupportAiMovieRange(cameraAbilityImpl);
            if (supportAiMovieRange.length != 0) {
                modeAbilityImpl.put(ModeCharacteristicKey.AI_MOVIE_RANGE, supportAiMovieRange);
            }
        }
    }

    public static void addFilterEffectCapability(ModeAbilityImpl modeAbilityImpl, CameraAbilityImpl cameraAbilityImpl) {
        if (modeAbilityImpl != null && cameraAbilityImpl != null && HisiFilterCapabilityUtil.isAvailable(cameraAbilityImpl)) {
            modeAbilityImpl.addAvailableParameterKey(ParameterKey.FILTER_EFFECT);
            modeAbilityImpl.addAvailableParameterKey(ParameterKey.FILTER_LEVEL);
            modeAbilityImpl.put(ModeCharacteristicKey.FILTER_EFFECT_FUNCTION, true);
            byte[] supportedFilterRange = HisiFilterCapabilityUtil.getSupportedFilterRange(cameraAbilityImpl);
            if (!(supportedFilterRange == null || supportedFilterRange.length == 0)) {
                modeAbilityImpl.put(ModeCharacteristicKey.FILTER_EFFECT_TYPE, supportedFilterRange);
            }
            modeAbilityImpl.put(ModeCharacteristicKey.FILTER_EFFECT_LEVEL, HisiFilterCapabilityUtil.getFilterLevelRange(cameraAbilityImpl));
        }
    }

    public static synchronized void addFunctionCapability(ModeAbilityImpl modeAbilityImpl, @Mode.Type int i, List<PropertyKey.Key<?>> list, CameraAbilityImpl cameraAbilityImpl) {
        synchronized (ModeFunctionUtil.class) {
            LOGGER.begin("addFunctionCapability");
            if (!(list == null || modeAbilityImpl == null)) {
                if (cameraAbilityImpl != null) {
                    for (PropertyKey.Key<?> key : list) {
                        Processor processor = functionTypeProcessorsMap.get(key);
                        if (processor != null) {
                            processor.process(modeAbilityImpl, i, list, cameraAbilityImpl);
                        }
                    }
                    modeAbilityImpl.setConflictFunctions(FunctionConflictUtil.getConflictFunctions(list));
                    LOGGER.end("addFunctionCapability");
                }
            }
        }
    }

    public static void addVideoStabilizationCapability(ModeAbilityImpl modeAbilityImpl, CameraAbilityImpl cameraAbilityImpl) {
        if (modeAbilityImpl != null && cameraAbilityImpl != null) {
            modeAbilityImpl.addAvailableParameterKey(ParameterKey.VIDEO_STABILIZATION);
            modeAbilityImpl.put(ModeCharacteristicKey.VIDEO_STABILIZATION_FUNCTION, true);
        }
    }

    private static List<Size> getResolution(ModeAbilityImpl modeAbilityImpl, PropertyKey.Key<?> key, Class<?> cls) {
        Map<Class<?>, List<Size>> map;
        List<Size> list;
        if (modeAbilityImpl == null || key == null || cls == null) {
            return Collections.emptyList();
        }
        Map<PropertyKey.Key<?>, Map<Class<?>, List<Size>>> functionClassOutputSizesMap = modeAbilityImpl.getFunctionClassOutputSizesMap();
        if (functionClassOutputSizesMap == null || (map = functionClassOutputSizesMap.get(key)) == null || (list = map.get(cls)) == null) {
            return Collections.emptyList();
        }
        return list;
    }
}
