package ohos.media.camera.mode.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import ohos.agp.graphics.SurfaceOps;
import ohos.media.camera.device.impl.CameraAbilityImpl;
import ohos.media.camera.mode.adapter.utils.CustomConfigurationUtil;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;
import ohos.media.camera.mode.utils.SizeUtil;
import ohos.media.camera.params.Metadata;
import ohos.media.camera.params.PropertyKey;
import ohos.media.camera.params.adapter.InnerPropertyKey;
import ohos.media.image.common.Size;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;
import ohos.multimodalinput.event.KeyEvent;
import ohos.telephony.TelephonyUtils;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

public class PhotoResolutionUtil {
    private static final float COMMON_RATIO_TOLERANCE = 0.09f;
    private static final String ERROR_TRANSLATING = "Error translating ";
    private static final String FILTER_RULE_EXCLUDE_VALUE = "exclude_value";
    private static final String FILTER_RULE_MAX_SIZE = "max_size";
    private static final String FILTER_RULE_NECESSARY_VALUE = "necessary_value";
    private static final String FILTER_RULE_RATIO = "ratio";
    private static final String FILTER_RULE_RATIO_COUNT = "ratio_count";
    private static final String FILTER_RULE_SIZE = "size";
    private static final String FILTER_RULE_VALUE = "value";
    private static final Size FRONT_NECESSARY_VALUE = new Size(640, Metadata.FpsRange.FPS_480);
    private static final String GET_CAPTURE_SUPPORTS_FAIL_PARAM_IS_EMPTY = "getCaptureSupports fail, param is empty";
    private static final Size IGNORED_SIZE_OF_SUB_SENSOR = new Size(5120, 2880);
    private static final int INVALID_MINUS = -1;
    private static final int LARGEST_PREVIEW_SIZE = 65536;
    private static final Logger LOGGER = LoggerFactory.getCameraLogger(PhotoResolutionUtil.class);
    private static final int MAX_SCREEN_RATIO_INIT_VALUE = -1;
    private static final double MIN_RATIO = 0.99d;
    private static final Size[] PORTRAIT_MODE_PREVIEW_ARRAYS = {new Size(PREVIEW_SIZE_1280, 960), new Size(1536, 736)};
    private static final int PREFERRED_RESOLUTION = 720;
    private static final int PREVIEW_SIZE_1080P = 1088;
    private static final int PREVIEW_SIZE_1080P_LIMITED = 1000;
    private static final int PREVIEW_SIZE_1280 = 1280;
    private static final int PREVIEW_SIZE_720P = 730;
    private static final float RATIO_1_1 = 1.0f;
    private static final float RATIO_4_3 = 1.3333334f;
    private static final double RATIO_TOLERANCE = 0.05d;
    private static final double RATIO_TOLERANCE_1_1 = 0.01d;
    private static final Size RESOLUTION_40M = new Size(7296, 5472);
    private static final Size RESOLUTION_48M = new Size(8000, TelephonyUtils.MSG_ADD_OBSERVER);
    private static final int RESOLUTION_RATION_COUNT_2 = 2;
    private static final Size RESOLUTION_SIZE_1024_768 = new Size(1024, 768);
    private static final Size RESOLUTION_SIZE_1200_1200 = new Size(SystemAbilityDefinition.SUBSYS_DFX_SYS_ABILITY_ID_BEGIN, SystemAbilityDefinition.SUBSYS_DFX_SYS_ABILITY_ID_BEGIN);
    private static final Size RESOLUTION_SIZE_1280_720 = new Size(PREVIEW_SIZE_1280, PREFERRED_RESOLUTION);
    private static final Size RESOLUTION_SIZE_1280_800 = new Size(PREVIEW_SIZE_1280, 800);
    private static final Size RESOLUTION_SIZE_1280_960 = new Size(PREVIEW_SIZE_1280, 960);
    private static final Size RESOLUTION_SIZE_1440_720 = new Size(1440, PREFERRED_RESOLUTION);
    private static final Size RESOLUTION_SIZE_1536_1536 = new Size(1536, 1536);
    private static final Size RESOLUTION_SIZE_1536_736 = new Size(1536, 736);
    private static final Size RESOLUTION_SIZE_1600_1200 = new Size(SystemAbilityDefinition.SUBSYS_GLOBAL_SYS_ABILITY_ID_BEGIN, SystemAbilityDefinition.SUBSYS_DFX_SYS_ABILITY_ID_BEGIN);
    private static final Size RESOLUTION_SIZE_1632_1224 = new Size(1632, 1224);
    private static final Size RESOLUTION_SIZE_1728_1728 = new Size(1728, 1728);
    private static final Size RESOLUTION_SIZE_1824_1368 = new Size(1824, 1368);
    private static final Size RESOLUTION_SIZE_1840_1040 = new Size(1840, 1040);
    private static final Size RESOLUTION_SIZE_1920_1080 = new Size(1920, WIDE_VIEW_HEIGHT);
    private static final Size RESOLUTION_SIZE_1920_1440 = new Size(1920, 1440);
    private static final Size RESOLUTION_SIZE_1920_1920 = new Size(1920, 1920);
    private static final Size RESOLUTION_SIZE_1936_1936 = new Size(1936, 1936);
    private static final Size RESOLUTION_SIZE_1944_1944 = new Size(1944, 1944);
    private static final Size RESOLUTION_SIZE_1952_1952 = new Size(1952, 1952);
    private static final Size RESOLUTION_SIZE_2304_1104 = new Size(2304, 1104);
    private static final Size RESOLUTION_SIZE_2304_1136 = new Size(2304, 1136);
    private static final Size RESOLUTION_SIZE_2304_1728 = new Size(2304, 1728);
    private static final Size RESOLUTION_SIZE_2432_2432 = new Size(2432, 2432);
    private static final Size RESOLUTION_SIZE_2448_2448 = new Size(2448, 2448);
    private static final Size RESOLUTION_SIZE_2560_1232 = new Size(2560, 1232);
    private static final Size RESOLUTION_SIZE_2560_1264 = new Size(2560, 1264);
    private static final Size RESOLUTION_SIZE_2560_1280 = new Size(2560, PREVIEW_SIZE_1280);
    private static final Size RESOLUTION_SIZE_2560_1440 = new Size(2560, 1440);
    private static final Size RESOLUTION_SIZE_2560_1920 = new Size(2560, 1920);
    private static final Size RESOLUTION_SIZE_2592_1296 = new Size(2592, 1296);
    private static final Size RESOLUTION_SIZE_2592_1456 = new Size(2592, 1456);
    private static final Size RESOLUTION_SIZE_2592_1458 = new Size(2592, 1458);
    private static final Size RESOLUTION_SIZE_2592_1936 = new Size(2592, 1936);
    private static final Size RESOLUTION_SIZE_2592_1944 = new Size(2592, 1944);
    private static final Size RESOLUTION_SIZE_2592_1952 = new Size(2592, 1952);
    private static final Size RESOLUTION_SIZE_2672_2012 = new Size(2672, KeyEvent.KEY_DPAD_UP);
    private static final Size RESOLUTION_SIZE_2736_2736 = new Size(2736, 2736);
    private static final Size RESOLUTION_SIZE_2816_1584 = new Size(2816, 1584);
    private static final Size RESOLUTION_SIZE_2816_2112 = new Size(2816, KeyEvent.KEY_NUMPAD_9);
    private static final Size RESOLUTION_SIZE_2880_2152 = new Size(2880, 2152);
    private static final Size RESOLUTION_SIZE_2976_2976 = new Size(2976, 2976);
    private static final Size RESOLUTION_SIZE_2992_2992 = new Size(2992, 2992);
    private static final Size RESOLUTION_SIZE_3088_2736 = new Size(3088, 2736);
    private static final Size RESOLUTION_SIZE_3104_3104 = new Size(3104, 3104);
    private static final Size RESOLUTION_SIZE_3120_3120 = new Size(3120, 3120);
    private static final Size RESOLUTION_SIZE_3264_1504 = new Size(3264, 1504);
    private static final Size RESOLUTION_SIZE_3264_1568 = new Size(3264, 1568);
    private static final Size RESOLUTION_SIZE_3264_1600 = new Size(3264, SystemAbilityDefinition.SUBSYS_GLOBAL_SYS_ABILITY_ID_BEGIN);
    private static final Size RESOLUTION_SIZE_3264_1616 = new Size(3264, 1616);
    private static final Size RESOLUTION_SIZE_3264_1632 = new Size(3264, 1632);
    private static final Size RESOLUTION_SIZE_3264_1836 = new Size(3264, 1836);
    private static final Size RESOLUTION_SIZE_3264_1840 = new Size(3264, 1840);
    private static final Size RESOLUTION_SIZE_3264_2448 = new Size(3264, 2448);
    private static final Size RESOLUTION_SIZE_3328_1872 = new Size(3328, 1872);
    private static final Size RESOLUTION_SIZE_3456_3456 = new Size(3456, 3456);
    private static final Size RESOLUTION_SIZE_3648_1680 = new Size(3648, 1680);
    private static final Size RESOLUTION_SIZE_3648_1712 = new Size(3648, 1712);
    private static final Size RESOLUTION_SIZE_3648_1744 = new Size(3648, 1744);
    private static final Size RESOLUTION_SIZE_3648_1824 = new Size(3648, 1824);
    private static final Size RESOLUTION_SIZE_3648_2056 = new Size(3648, KeyEvent.KEY_GRAVE);
    private static final Size RESOLUTION_SIZE_3648_2736 = new Size(3648, 2736);
    private static final Size RESOLUTION_SIZE_3840_1808 = new Size(3840, 1808);
    private static final Size RESOLUTION_SIZE_3840_3840 = new Size(3840, 3840);
    private static final Size RESOLUTION_SIZE_3904_3456 = new Size(3904, 3456);
    private static final Size RESOLUTION_SIZE_3968_1920 = new Size(3968, 1920);
    private static final Size RESOLUTION_SIZE_3968_1984 = new Size(3968, 1984);
    private static final Size RESOLUTION_SIZE_3968_2240 = new Size(3968, 2240);
    private static final Size RESOLUTION_SIZE_3968_2976 = new Size(3968, 2976);
    private static final Size RESOLUTION_SIZE_4000_1840 = new Size(4000, 1840);
    private static final Size RESOLUTION_SIZE_4000_1872 = new Size(4000, 1872);
    private static final Size RESOLUTION_SIZE_4000_1920 = new Size(4000, 1920);
    private static final Size RESOLUTION_SIZE_4000_1936 = new Size(4000, 1936);
    private static final Size RESOLUTION_SIZE_4000_3000 = new Size(4000, 3000);
    private static final Size RESOLUTION_SIZE_4160_2000 = new Size(4160, 2000);
    private static final Size RESOLUTION_SIZE_4160_2016 = new Size(4160, KeyEvent.KEY_DPAD_CENTER);
    private static final Size RESOLUTION_SIZE_4160_2080 = new Size(4160, KeyEvent.KEY_BREAK);
    private static final Size RESOLUTION_SIZE_4160_2336 = new Size(4160, 2336);
    private static final Size RESOLUTION_SIZE_4160_2368 = new Size(4160, 2368);
    private static final Size RESOLUTION_SIZE_4160_3120 = new Size(4160, 3120);
    private static final Size RESOLUTION_SIZE_4192_2368 = new Size(4192, 2368);
    private static final Size RESOLUTION_SIZE_4192_3120 = new Size(4192, 3120);
    private static final Size RESOLUTION_SIZE_4208_2368 = new Size(4208, 2368);
    private static final Size RESOLUTION_SIZE_4208_3120 = new Size(4208, 3120);
    private static final Size RESOLUTION_SIZE_4224_4224 = new Size(4224, 4224);
    private static final Size RESOLUTION_SIZE_4304_4304 = new Size(4304, 4304);
    private static final Size RESOLUTION_SIZE_4608_2128 = new Size(4608, 2128);
    private static final Size RESOLUTION_SIZE_4608_2144 = new Size(4608, 2144);
    private static final Size RESOLUTION_SIZE_4608_2160 = new Size(4608, 2160);
    private static final Size RESOLUTION_SIZE_4608_2208 = new Size(4608, 2208);
    private static final Size RESOLUTION_SIZE_4608_2240 = new Size(4608, 2240);
    private static final Size RESOLUTION_SIZE_4608_2272 = new Size(4608, 2272);
    private static final Size RESOLUTION_SIZE_4608_2304 = new Size(4608, 2304);
    private static final Size RESOLUTION_SIZE_4608_2592 = new Size(4608, 2592);
    private static final Size RESOLUTION_SIZE_4608_3456 = new Size(4608, 3456);
    private static final Size RESOLUTION_SIZE_4896_4896 = new Size(4896, 4896);
    private static final Size RESOLUTION_SIZE_5120_2368 = new Size(5120, 2368);
    private static final Size RESOLUTION_SIZE_5120_2400 = new Size(5120, SystemAbilityDefinition.SUBSYS_IOT_SYS_ABILITY_ID_BEGIN);
    private static final Size RESOLUTION_SIZE_5120_2448 = new Size(5120, 2448);
    private static final Size RESOLUTION_SIZE_5120_2560 = new Size(5120, 2560);
    private static final Size RESOLUTION_SIZE_5120_2880 = new Size(5120, 2880);
    private static final Size RESOLUTION_SIZE_5120_3840 = new Size(5120, 3840);
    private static final Size RESOLUTION_SIZE_5152_2896 = new Size(5152, 2896);
    private static final Size RESOLUTION_SIZE_5632_2640 = new Size(5632, 2640);
    private static final Size RESOLUTION_SIZE_5632_2720 = new Size(5632, 2720);
    private static final Size RESOLUTION_SIZE_5632_2784 = new Size(5632, 2784);
    private static final Size RESOLUTION_SIZE_5632_2816 = new Size(5632, 2816);
    private static final Size RESOLUTION_SIZE_5632_3168 = new Size(5632, 3168);
    private static final Size RESOLUTION_SIZE_5632_4224 = new Size(5632, 4224);
    private static final Size RESOLUTION_SIZE_5760_2688 = new Size(5760, 2688);
    private static final Size RESOLUTION_SIZE_5760_4304 = new Size(5760, 4304);
    private static final Size RESOLUTION_SIZE_640_480 = new Size(640, Metadata.FpsRange.FPS_480);
    private static final Size RESOLUTION_SIZE_6528_3008 = new Size(6528, 3008);
    private static final Size RESOLUTION_SIZE_6528_3072 = new Size(6528, 3072);
    private static final Size RESOLUTION_SIZE_6528_3152 = new Size(6528, 3152);
    private static final Size RESOLUTION_SIZE_6528_4896 = new Size(6528, 4896);
    private static final float SCREEN_RATIO_TOLERANCE = 0.17f;
    private static final String SEPARATOR_COMMA = ",";
    private static final Size SIZE_3264_1520 = new Size(3264, 1520);
    private static final Size SIZE_5632_2624 = new Size(5632, 2624);
    private static final int SIZE_STR_SPLIT_LEN = 2;
    private static final String STRING_COLON = " : ";
    private static final String STRING_GET = "Get ";
    private static final String STRING_RULE_RATIO_4_3 = "|4:3";
    private static final String STRING_RULE_RATIO_4_3_1_1 = "|4:3,1:1";
    private static final String STRING_VERTICAL_LINE = "|";
    private static final double THRESHOLD_TWO_DOUBLE_EQUAL = 1.0E-8d;
    private static final int WIDE_VIEW_HEIGHT = 1080;
    private static final int WIDE_VIEW_WIDTH = 1088;
    private static Map<String, List<Size>> previewSupportsCache = new HashMap();
    private static List<Size> resolutionWhiteList = new ArrayList();

    /* access modifiers changed from: private */
    public enum ModeRulesParamType {
        EXTRA_RULES,
        USE_SUPER_RESOLUTION,
        KEEP_ALL_FILTERED_SUPPORTS,
        SHOULD_ONLY_SHOW_SUPER_RESOLUTION,
        LIMIT_PREVIEW_SIZE_TO_720P,
        SPECIFIC_PREVIEW_SIZE,
        LIMIT_PREVIEW_SIZE_WIDTH_1280,
        MODE_NAME,
        SUPPORT_GUARD_RESOLUTION,
        SHOWN_AI_ULTRA_RESOLUTION
    }

    @Retention(RetentionPolicy.SOURCE)
    private @interface RatioIndex {
        public static final int FOUR_TO_THREE = 0;
        public static final int MAX = 3;
        public static final int ONE_TO_ONE = 2;
        public static final int SCREEN = 1;
    }

    private static List<Size> fillMonoSupports(CameraAbilityImpl cameraAbilityImpl, List<Size> list) {
        return list;
    }

    static {
        resolutionWhiteList.add(RESOLUTION_48M);
        resolutionWhiteList.add(RESOLUTION_40M);
        resolutionWhiteList.add(RESOLUTION_SIZE_6528_4896);
        resolutionWhiteList.add(RESOLUTION_SIZE_5760_4304);
        resolutionWhiteList.add(RESOLUTION_SIZE_5632_4224);
        resolutionWhiteList.add(RESOLUTION_SIZE_4896_4896);
        resolutionWhiteList.add(RESOLUTION_SIZE_6528_3152);
        resolutionWhiteList.add(RESOLUTION_SIZE_5120_3840);
        resolutionWhiteList.add(RESOLUTION_SIZE_6528_3072);
        resolutionWhiteList.add(RESOLUTION_SIZE_6528_3008);
        resolutionWhiteList.add(RESOLUTION_SIZE_4304_4304);
        resolutionWhiteList.add(RESOLUTION_SIZE_5632_3168);
        resolutionWhiteList.add(RESOLUTION_SIZE_4224_4224);
        resolutionWhiteList.add(RESOLUTION_SIZE_4608_3456);
        resolutionWhiteList.add(RESOLUTION_SIZE_5632_2816);
        resolutionWhiteList.add(RESOLUTION_SIZE_5632_2784);
        resolutionWhiteList.add(RESOLUTION_SIZE_5760_2688);
        resolutionWhiteList.add(RESOLUTION_SIZE_5632_2720);
        resolutionWhiteList.add(RESOLUTION_SIZE_5152_2896);
        resolutionWhiteList.add(RESOLUTION_SIZE_5632_2640);
        resolutionWhiteList.add(SIZE_5632_2624);
        resolutionWhiteList.add(RESOLUTION_SIZE_5120_2880);
        resolutionWhiteList.add(RESOLUTION_SIZE_3840_3840);
        resolutionWhiteList.add(RESOLUTION_SIZE_4208_3120);
        resolutionWhiteList.add(RESOLUTION_SIZE_5120_2560);
        resolutionWhiteList.add(RESOLUTION_SIZE_4192_3120);
        resolutionWhiteList.add(RESOLUTION_SIZE_4160_3120);
        resolutionWhiteList.add(RESOLUTION_SIZE_5120_2448);
        resolutionWhiteList.add(RESOLUTION_SIZE_5120_2448);
        resolutionWhiteList.add(RESOLUTION_SIZE_5120_2400);
        resolutionWhiteList.add(RESOLUTION_SIZE_4608_2592);
        resolutionWhiteList.add(RESOLUTION_SIZE_4000_3000);
        resolutionWhiteList.add(RESOLUTION_SIZE_3968_2976);
        resolutionWhiteList.add(RESOLUTION_SIZE_3648_2736);
        resolutionWhiteList.add(RESOLUTION_SIZE_3456_3456);
        resolutionWhiteList.add(RESOLUTION_SIZE_4608_2304);
        resolutionWhiteList.add(RESOLUTION_SIZE_4608_2272);
        resolutionWhiteList.add(RESOLUTION_SIZE_4608_2240);
        resolutionWhiteList.add(RESOLUTION_SIZE_4608_2208);
        resolutionWhiteList.add(RESOLUTION_SIZE_4608_2128);
        resolutionWhiteList.add(RESOLUTION_SIZE_5120_2368);
        resolutionWhiteList.add(RESOLUTION_SIZE_4608_2128);
        resolutionWhiteList.add(RESOLUTION_SIZE_4608_2160);
        resolutionWhiteList.add(RESOLUTION_SIZE_4208_2368);
        resolutionWhiteList.add(RESOLUTION_SIZE_4192_2368);
        resolutionWhiteList.add(RESOLUTION_SIZE_4608_2144);
        resolutionWhiteList.add(RESOLUTION_SIZE_4160_2368);
        resolutionWhiteList.add(RESOLUTION_SIZE_4160_2336);
        resolutionWhiteList.add(RESOLUTION_SIZE_3120_3120);
        resolutionWhiteList.add(RESOLUTION_SIZE_3104_3104);
        resolutionWhiteList.add(RESOLUTION_SIZE_2992_2992);
        resolutionWhiteList.add(RESOLUTION_SIZE_4160_2016);
        resolutionWhiteList.add(RESOLUTION_SIZE_4160_2080);
        resolutionWhiteList.add(RESOLUTION_SIZE_4160_2000);
        resolutionWhiteList.add(RESOLUTION_SIZE_3968_2240);
        resolutionWhiteList.add(RESOLUTION_SIZE_4160_2000);
        resolutionWhiteList.add(RESOLUTION_SIZE_4000_1936);
        resolutionWhiteList.add(RESOLUTION_SIZE_2976_2976);
        resolutionWhiteList.add(RESOLUTION_SIZE_3264_2448);
        resolutionWhiteList.add(RESOLUTION_SIZE_3968_1984);
        resolutionWhiteList.add(RESOLUTION_SIZE_3968_1920);
        resolutionWhiteList.add(RESOLUTION_SIZE_3648_2056);
        resolutionWhiteList.add(RESOLUTION_SIZE_2736_2736);
        resolutionWhiteList.add(RESOLUTION_SIZE_4000_1920);
        resolutionWhiteList.add(RESOLUTION_SIZE_4000_1872);
        resolutionWhiteList.add(RESOLUTION_SIZE_4000_1840);
        resolutionWhiteList.add(RESOLUTION_SIZE_3648_1744);
        resolutionWhiteList.add(RESOLUTION_SIZE_3648_1824);
        resolutionWhiteList.add(RESOLUTION_SIZE_3648_1712);
        resolutionWhiteList.add(RESOLUTION_SIZE_3648_1680);
        resolutionWhiteList.add(RESOLUTION_SIZE_3840_1808);
        resolutionWhiteList.add(RESOLUTION_SIZE_3328_1872);
        resolutionWhiteList.add(RESOLUTION_SIZE_2880_2152);
        resolutionWhiteList.add(RESOLUTION_SIZE_3264_1840);
        resolutionWhiteList.add(RESOLUTION_SIZE_3264_1836);
        resolutionWhiteList.add(RESOLUTION_SIZE_2448_2448);
        resolutionWhiteList.add(RESOLUTION_SIZE_2432_2432);
        resolutionWhiteList.add(RESOLUTION_SIZE_3264_1632);
        resolutionWhiteList.add(RESOLUTION_SIZE_3264_1616);
        resolutionWhiteList.add(RESOLUTION_SIZE_3264_1600);
        resolutionWhiteList.add(RESOLUTION_SIZE_3264_1568);
        resolutionWhiteList.add(RESOLUTION_SIZE_2816_2112);
        resolutionWhiteList.add(SIZE_3264_1520);
        resolutionWhiteList.add(RESOLUTION_SIZE_3264_1504);
        resolutionWhiteList.add(RESOLUTION_SIZE_2816_1584);
        resolutionWhiteList.add(RESOLUTION_SIZE_2672_2012);
        resolutionWhiteList.add(RESOLUTION_SIZE_2592_1952);
        resolutionWhiteList.add(RESOLUTION_SIZE_2592_1944);
        resolutionWhiteList.add(RESOLUTION_SIZE_2592_1936);
        resolutionWhiteList.add(RESOLUTION_SIZE_2560_1920);
        resolutionWhiteList.add(RESOLUTION_SIZE_2592_1458);
        resolutionWhiteList.add(RESOLUTION_SIZE_2592_1456);
        resolutionWhiteList.add(RESOLUTION_SIZE_1952_1952);
        resolutionWhiteList.add(RESOLUTION_SIZE_1944_1944);
        resolutionWhiteList.add(RESOLUTION_SIZE_2304_1728);
        resolutionWhiteList.add(RESOLUTION_SIZE_1936_1936);
        resolutionWhiteList.add(RESOLUTION_SIZE_1824_1368);
        resolutionWhiteList.add(RESOLUTION_SIZE_2560_1440);
        resolutionWhiteList.add(RESOLUTION_SIZE_2592_1296);
        resolutionWhiteList.add(RESOLUTION_SIZE_2560_1280);
        resolutionWhiteList.add(RESOLUTION_SIZE_2560_1264);
        resolutionWhiteList.add(RESOLUTION_SIZE_2560_1232);
        resolutionWhiteList.add(RESOLUTION_SIZE_2304_1136);
        resolutionWhiteList.add(RESOLUTION_SIZE_1920_1920);
        resolutionWhiteList.add(RESOLUTION_SIZE_1728_1728);
        resolutionWhiteList.add(RESOLUTION_SIZE_1920_1440);
        resolutionWhiteList.add(RESOLUTION_SIZE_2304_1104);
        resolutionWhiteList.add(RESOLUTION_SIZE_1920_1080);
        resolutionWhiteList.add(RESOLUTION_SIZE_1840_1040);
        resolutionWhiteList.add(RESOLUTION_SIZE_1536_1536);
        resolutionWhiteList.add(RESOLUTION_SIZE_1632_1224);
        resolutionWhiteList.add(RESOLUTION_SIZE_1600_1200);
        resolutionWhiteList.add(RESOLUTION_SIZE_1440_720);
        resolutionWhiteList.add(RESOLUTION_SIZE_1200_1200);
        resolutionWhiteList.add(RESOLUTION_SIZE_1280_960);
        resolutionWhiteList.add(RESOLUTION_SIZE_1280_800);
        resolutionWhiteList.add(RESOLUTION_SIZE_1440_720);
        resolutionWhiteList.add(RESOLUTION_SIZE_1280_720);
        resolutionWhiteList.add(RESOLUTION_SIZE_1024_768);
        resolutionWhiteList.add(RESOLUTION_SIZE_640_480);
        resolutionWhiteList.add(RESOLUTION_SIZE_3088_2736);
        resolutionWhiteList.add(RESOLUTION_SIZE_3904_3456);
    }

    private PhotoResolutionUtil() {
    }

    private static <T> Boolean isValid(CameraAbilityImpl cameraAbilityImpl, Class<?> cls) {
        if (cameraAbilityImpl == null) {
            LOGGER.error("cameraAbility is null", new Object[0]);
            return false;
        } else if (cls != null) {
            return true;
        } else {
            LOGGER.error("getPreviewSupports:cameraAbility is null", new Object[0]);
            return false;
        }
    }

    public static <T> List<Size> getPreviewSupports(CameraAbilityImpl cameraAbilityImpl, int i, Class<T> cls) {
        if (!isValid(cameraAbilityImpl, cls).booleanValue()) {
            return Collections.emptyList();
        }
        String modeNameById = ModeNameUtil.getModeNameById(i);
        if (modeNameById.isEmpty()) {
            LOGGER.error("can not find modeName %{public}d", Integer.valueOf(i));
            return Collections.emptyList();
        }
        List<Size> supportedSizes = cameraAbilityImpl.getSupportedSizes(cls);
        if (supportedSizes == null) {
            LOGGER.error("can not find clazz's output size %{public}s", cls.getName());
            return Collections.emptyList();
        }
        String str = cameraAbilityImpl.getCameraId() + "_" + modeNameById + "_" + Arrays.toString(supportedSizes.toArray());
        LOGGER.info("Query key %{public}s", str);
        if (previewSupportsCache.containsKey(str)) {
            LOGGER.info("Hit key %{public}s", str);
            return previewSupportsCache.get(str);
        }
        LOGGER.info("Missing key %{public}s", str);
        Map<ModeRulesParamType, Object> modeRulesParams = getModeRulesParams(cameraAbilityImpl, modeNameById, -1.0d);
        if (modeRulesParams.isEmpty()) {
            LOGGER.error(GET_CAPTURE_SUPPORTS_FAIL_PARAM_IS_EMPTY, new Object[0]);
            return Collections.emptyList();
        }
        boolean z = (modeRulesParams.get(ModeRulesParamType.LIMIT_PREVIEW_SIZE_TO_720P) instanceof Boolean) && ((Boolean) modeRulesParams.get(ModeRulesParamType.LIMIT_PREVIEW_SIZE_TO_720P)).booleanValue();
        LOGGER.debug("getPreviewSupports devicePreviewSupports=%{public}s", Arrays.toString(supportedSizes.toArray()));
        if (z) {
            supportedSizes.removeIf(new Predicate<Size>() {
                /* class ohos.media.camera.mode.utils.PhotoResolutionUtil.AnonymousClass1 */

                public boolean test(Size size) {
                    return size.width > PhotoResolutionUtil.PREVIEW_SIZE_720P && size.height > PhotoResolutionUtil.PREVIEW_SIZE_720P;
                }
            });
        }
        Size[] sizeArr = (Size[]) modeRulesParams.getOrDefault(ModeRulesParamType.SPECIFIC_PREVIEW_SIZE, new Size[0]);
        if (!(sizeArr == null || sizeArr.length == 0)) {
            ArrayList arrayList = new ArrayList(sizeArr.length);
            for (Size size : sizeArr) {
                if (supportedSizes.contains(size)) {
                    arrayList.add(size);
                }
            }
            supportedSizes = arrayList;
        }
        List<Size> reducePreviewSize = reducePreviewSize(supportedSizes, getCaptureSupports(cameraAbilityImpl, i, DeviceUtil.getMaxScreenRatio()), modeNameById, modeRulesParams);
        previewSupportsCache.put(str, reducePreviewSize);
        return reducePreviewSize;
    }

    public static List<Size> getPreviewSupports(CameraAbilityImpl cameraAbilityImpl, int i) {
        if (cameraAbilityImpl == null) {
            LOGGER.error("cameraAbility is null", new Object[0]);
            return Collections.emptyList();
        }
        String modeNameById = ModeNameUtil.getModeNameById(i);
        if (modeNameById.isEmpty()) {
            LOGGER.error("can not find modeName %{public}d", Integer.valueOf(i));
            return Collections.emptyList();
        }
        Map<ModeRulesParamType, Object> modeRulesParams = getModeRulesParams(cameraAbilityImpl, modeNameById, -1.0d);
        if (modeRulesParams.isEmpty()) {
            LOGGER.error(GET_CAPTURE_SUPPORTS_FAIL_PARAM_IS_EMPTY, new Object[0]);
            return Collections.emptyList();
        }
        boolean z = (modeRulesParams.get(ModeRulesParamType.LIMIT_PREVIEW_SIZE_TO_720P) instanceof Boolean) && ((Boolean) modeRulesParams.get(ModeRulesParamType.LIMIT_PREVIEW_SIZE_TO_720P)).booleanValue();
        List<Size> supportedSizes = cameraAbilityImpl.getSupportedSizes(SurfaceOps.class);
        if (supportedSizes == null) {
            LOGGER.error("can not find klass's output size : SurfaceOps", new Object[0]);
            return Collections.emptyList();
        }
        LOGGER.debug("getPreviewSupports devicePreviewSupports=%{public}s", Arrays.toString(supportedSizes.toArray()));
        if (z) {
            supportedSizes.removeIf(new Predicate<Size>() {
                /* class ohos.media.camera.mode.utils.PhotoResolutionUtil.AnonymousClass2 */

                public boolean test(Size size) {
                    if (size == null) {
                        PhotoResolutionUtil.LOGGER.error("size is null", new Object[0]);
                        return false;
                    } else if (size.width <= PhotoResolutionUtil.PREVIEW_SIZE_720P || size.height <= PhotoResolutionUtil.PREVIEW_SIZE_720P) {
                        return false;
                    } else {
                        return true;
                    }
                }
            });
        }
        Size[] sizeArr = (Size[]) modeRulesParams.getOrDefault(ModeRulesParamType.SPECIFIC_PREVIEW_SIZE, new Size[0]);
        if (sizeArr == null || sizeArr.length == 0) {
            return supportedSizes;
        }
        ArrayList arrayList = new ArrayList(sizeArr.length);
        for (Size size : sizeArr) {
            if (supportedSizes.contains(size)) {
                arrayList.add(size);
            }
        }
        return arrayList;
    }

    public static List<Size> getCaptureSupports(CameraAbilityImpl cameraAbilityImpl, int i, double d) {
        if (cameraAbilityImpl == null) {
            LOGGER.error("init characteristicsChanged == null", new Object[0]);
            return Collections.emptyList();
        }
        Map<ModeRulesParamType, Object> modeRulesParams = getModeRulesParams(cameraAbilityImpl, ModeNameUtil.getModeNameById(i), d);
        if (modeRulesParams.isEmpty()) {
            LOGGER.error(GET_CAPTURE_SUPPORTS_FAIL_PARAM_IS_EMPTY, new Object[0]);
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        boolean z = (modeRulesParams.get(ModeRulesParamType.SUPPORT_GUARD_RESOLUTION) instanceof Boolean) && ((Boolean) modeRulesParams.get(ModeRulesParamType.SUPPORT_GUARD_RESOLUTION)).booleanValue();
        List<Size> fillMonoSupports = fillMonoSupports(cameraAbilityImpl, arrayList);
        fillDeviceCaptureSupports(cameraAbilityImpl, fillMonoSupports, z);
        LOGGER.debug("getCaptureSupports deviceCaptureSupports=%{public}s", Arrays.toString(fillMonoSupports.toArray()));
        List<Size> superResolution = getSuperResolution(cameraAbilityImpl);
        ArrayList arrayList2 = new ArrayList();
        List arrayList3 = new ArrayList(fillMonoSupports);
        List list = (List) modeRulesParams.get(ModeRulesParamType.EXTRA_RULES);
        boolean z2 = (modeRulesParams.get(ModeRulesParamType.KEEP_ALL_FILTERED_SUPPORTS) instanceof Boolean) && ((Boolean) modeRulesParams.get(ModeRulesParamType.KEEP_ALL_FILTERED_SUPPORTS)).booleanValue();
        if ((modeRulesParams.get(ModeRulesParamType.SHOULD_ONLY_SHOW_SUPER_RESOLUTION) instanceof Boolean) && ((Boolean) modeRulesParams.get(ModeRulesParamType.SHOULD_ONLY_SHOW_SUPER_RESOLUTION)).booleanValue()) {
            arrayList3.clear();
            superResolution = filterRuleProcessExtraRule(superResolution, list);
        } else if (!z) {
            arrayList3 = filterRuleProcess(arrayList3, list, z2, cameraAbilityImpl, d);
        } else {
            int size = arrayList3.size();
            arrayList3 = filterRuleProcessExtraRule(arrayList3, list);
            if (arrayList3.size() != size) {
                arrayList3.clear();
                arrayList3.add((Size) arrayList3.get(0));
            }
        }
        fillSuperResolution(modeRulesParams, superResolution, arrayList2, arrayList3);
        if (modeRulesParams.get(ModeRulesParamType.SHOWN_AI_ULTRA_RESOLUTION) != null) {
            arrayList2.add((Size) modeRulesParams.get(ModeRulesParamType.SHOWN_AI_ULTRA_RESOLUTION));
        }
        arrayList2.addAll(arrayList3);
        return arrayList2;
    }

    public static List<Size> getDeviceSupported(CameraAbilityImpl cameraAbilityImpl, int i, double d) {
        boolean z = false;
        if (cameraAbilityImpl == null) {
            LOGGER.error("init characteristicsChanged == null", new Object[0]);
            return Collections.emptyList();
        }
        Map<ModeRulesParamType, Object> modeRulesParams = getModeRulesParams(cameraAbilityImpl, ModeNameUtil.getModeNameById(i), d);
        if (modeRulesParams.isEmpty()) {
            LOGGER.error(GET_CAPTURE_SUPPORTS_FAIL_PARAM_IS_EMPTY, new Object[0]);
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        if ((modeRulesParams.get(ModeRulesParamType.SUPPORT_GUARD_RESOLUTION) instanceof Boolean) && ((Boolean) modeRulesParams.get(ModeRulesParamType.SUPPORT_GUARD_RESOLUTION)).booleanValue()) {
            z = true;
        }
        List<Size> fillMonoSupports = fillMonoSupports(cameraAbilityImpl, arrayList);
        fillDeviceCaptureSupports(cameraAbilityImpl, fillMonoSupports, z);
        return fillMonoSupports;
    }

    private static void fillSuperResolution(Map<ModeRulesParamType, Object> map, List<Size> list, List<Size> list2, List<Size> list3) {
        boolean z = (map.get(ModeRulesParamType.USE_SUPER_RESOLUTION) instanceof Boolean) && ((Boolean) map.get(ModeRulesParamType.USE_SUPER_RESOLUTION)).booleanValue();
        for (Size size : list) {
            if (list3.contains(size) || z) {
                list3.remove(size);
                list2.add(size);
            }
        }
    }

    private static void fillDeviceCaptureSupports(CameraAbilityImpl cameraAbilityImpl, List<Size> list, boolean z) {
        if (list.isEmpty()) {
            boolean z2 = false;
            if (z) {
                parseQuadSupports(cameraAbilityImpl, list, null);
                if (!list.isEmpty()) {
                    z2 = true;
                }
            }
            if (!z2) {
                list.addAll(cameraAbilityImpl.getSupportedSizes(3));
            }
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private static Map<ModeRulesParamType, Object> getModeRulesParams(CameraAbilityImpl cameraAbilityImpl, String str, double d) {
        char c;
        boolean z;
        boolean z2;
        boolean z3;
        Size[] sizeArr;
        int[] iArr;
        if (cameraAbilityImpl == null || str == null) {
            LOGGER.error("Input param is null", new Object[0]);
            return Collections.emptyMap();
        }
        EnumMap enumMap = new EnumMap(ModeRulesParamType.class);
        enumMap.put((EnumMap) ModeRulesParamType.MODE_NAME, (ModeRulesParamType) str);
        boolean isFrontCamera = CameraUtil.isFrontCamera(cameraAbilityImpl);
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        List<Size> overDefaultResolution = getOverDefaultResolution(cameraAbilityImpl);
        switch (str.hashCode()) {
            case -2131603407:
                if (str.equals(ConstantValue.MODE_NAME_LIVE_PHOTO)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case -1956720919:
                if (str.equals(ConstantValue.MODE_NAME_WIDE_APERTURE_PHOTO)) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case -1750012447:
                if (str.equals(ConstantValue.MODE_NAME_APERTURE_WHITEBLACK)) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case -1385830297:
                if (str.equals(ConstantValue.MODE_NAME_AR_3DOBJECT_PHOTO_MODE)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -1199789962:
                if (str.equals(ConstantValue.MODE_NAME_PORTRAIT_MOVIE)) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case -1069301315:
                if (str.equals(ConstantValue.MODE_NAME_BACK_PANORAMA)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case -897511597:
                if (str.equals(ConstantValue.MODE_NAME_FRONT_PANORAMA)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -708692573:
                if (str.equals(ConstantValue.MODE_NAME_ARGESTURE_PHOTO_MODE)) {
                    c = '\t';
                    break;
                }
                c = 65535;
                break;
            case -668461467:
                if (str.equals(ConstantValue.MODE_NAME_AR_CARTOON_PHOTO_MODE)) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case -294701011:
                if (str.equals(ConstantValue.MODE_NAME_WATER_MARK)) {
                    c = '\f';
                    break;
                }
                c = 65535;
                break;
            case 385745345:
                if (str.equals(ConstantValue.MODE_NAME_PANORAMA_3D)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 598141481:
                if (str.equals(ConstantValue.MODE_NAME_NORMAL_PHOTO)) {
                    c = '\r';
                    break;
                }
                c = 65535;
                break;
            case 713081807:
                if (str.equals(ConstantValue.MODE_NAME_REFOCUS)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 732323375:
                if (str.equals(ConstantValue.MODE_NAME_D3D_MODEL)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1101540961:
                if (str.equals(ConstantValue.MODE_NAME_ARTIST_FLITER)) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case 1475453089:
                if (str.equals(ConstantValue.MODE_NAME_MAKE_UP)) {
                    c = '\n';
                    break;
                }
                c = 65535;
                break;
            case 1899593560:
                if (str.equals(ConstantValue.MODE_NAME_SMART_WIDE_APERTURE_PHOTO)) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case 1940863557:
                if (str.equals(ConstantValue.MODE_NAME_AR_STAR_PHOTO_MODE)) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        Size[] sizeArr2 = null;
        switch (c) {
            case 0:
                String needCreateRatioRule = needCreateRatioRule(cameraAbilityImpl, 2, d);
                if (needCreateRatioRule != null) {
                    arrayList.add(needCreateRatioRule);
                }
                z3 = false;
                z2 = false;
                z = false;
                sizeArr = sizeArr2;
                break;
            case 1:
                arrayList.add("value|1920x1080");
                z3 = false;
                z2 = false;
                z = false;
                sizeArr = sizeArr2;
                break;
            case 2:
                arrayList.add("value|4000x3000,1920x1080");
                z3 = false;
                z2 = false;
                z = false;
                sizeArr = sizeArr2;
                break;
            case 3:
                arrayList.add("value|3264x2448,2880x2152,2816x2112,2560x1920,2304x1728");
                z3 = false;
                z2 = false;
                z = false;
                sizeArr = sizeArr2;
                break;
            case 4:
                arrayList.add("value|4000x3000,3264x2448,2816x2112,2560x1920,2304x1728");
                z3 = false;
                z2 = false;
                z = false;
                sizeArr = sizeArr2;
                break;
            case 5:
                arrayList.add("value|4160x2336,3264x1840,3264x1836,3264x1632");
                z3 = false;
                z2 = false;
                z = false;
                sizeArr = sizeArr2;
                break;
            case 6:
            case 7:
            case '\b':
            case '\t':
                arrayList.add("value|1280x720");
                z3 = false;
                z = false;
                z2 = true;
                sizeArr = sizeArr2;
                break;
            case '\n':
                z3 = false;
                z = false;
                z2 = true;
                sizeArr = sizeArr2;
                break;
            case 11:
            default:
                z3 = false;
                z2 = false;
                z = false;
                sizeArr = sizeArr2;
                break;
            case '\f':
                arrayList.add("ratio|4:3,1:1");
                z3 = false;
                z2 = false;
                z = false;
                sizeArr = sizeArr2;
                break;
            case '\r':
                getPhotoNecessarySizes(arrayList2, overDefaultResolution);
                z2 = false;
                z = false;
                z3 = true;
                sizeArr = sizeArr2;
                break;
            case 14:
            case 15:
            case 16:
                if (!SizeUtil.isFullResolutionSupported(cameraAbilityImpl, SizeUtil.FullResolutionMode.MODE_APERATURE)) {
                    sizeArr2 = getApertureModeNormalResolutionSizesAndRules(cameraAbilityImpl, isFrontCamera, arrayList);
                    z3 = false;
                    z2 = false;
                    z = false;
                    sizeArr = sizeArr2;
                    break;
                } else {
                    z3 = false;
                    z2 = false;
                    z = true;
                    sizeArr = sizeArr2;
                }
            case 17:
                arrayList.add("ratio|18:9");
                sizeArr = new Size[]{RESOLUTION_SIZE_1536_736};
                z3 = false;
                z2 = false;
                z = false;
                break;
        }
        List<Size> superResolution = getSuperResolution(cameraAbilityImpl);
        boolean z4 = isFrontCamera && z3 && !CollectionUtil.isEmptyCollection(superResolution);
        ArrayList arrayList3 = new ArrayList();
        enumMap.put((EnumMap) ModeRulesParamType.SHOULD_ONLY_SHOW_SUPER_RESOLUTION, (ModeRulesParamType) Boolean.valueOf(z4));
        processDefaultNecessaryValue(isFrontCamera, arrayList, z3, superResolution, arrayList2);
        processExcludeValue(arrayList, arrayList2, arrayList3, overDefaultResolution);
        if (isFrontCamera && !ConstantValue.MODE_NAME_FRONT_PANORAMA.equals(str) && !ConstantValue.MODE_NAME_COSPLAY_PHOTO_MODE.equals(str) && !ConstantValue.MODE_NAME_BACKGROUND_PHOTO_MODE.equals(str) && (iArr = (int[]) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.QUARTER_SIZE)) != null && iArr.length > 0) {
            enumMap.put((EnumMap) ModeRulesParamType.SUPPORT_GUARD_RESOLUTION, (ModeRulesParamType) true);
        }
        enumMap.put((EnumMap) ModeRulesParamType.EXTRA_RULES, (ModeRulesParamType) arrayList);
        enumMap.put((EnumMap) ModeRulesParamType.USE_SUPER_RESOLUTION, (ModeRulesParamType) Boolean.valueOf(z3));
        enumMap.put((EnumMap) ModeRulesParamType.LIMIT_PREVIEW_SIZE_TO_720P, (ModeRulesParamType) Boolean.valueOf(z2));
        enumMap.put((EnumMap) ModeRulesParamType.SPECIFIC_PREVIEW_SIZE, (ModeRulesParamType) sizeArr);
        enumMap.put((EnumMap) ModeRulesParamType.LIMIT_PREVIEW_SIZE_WIDTH_1280, (ModeRulesParamType) Boolean.valueOf(z));
        LOGGER.debug("getModeRulesParams rules=%{public}s, sr=%{public}b, keepAll=%{public}s, onlyShowSr=%{public}b, limit720=%{public}b, preview=%{public}s, mode=%{public}s", arrayList, Boolean.valueOf(z3), enumMap.get(ModeRulesParamType.KEEP_ALL_FILTERED_SUPPORTS), Boolean.valueOf(z4), Boolean.valueOf(z2), Arrays.toString(sizeArr), str);
        return enumMap;
    }

    private static Size[] getApertureModeNormalResolutionSizesAndRules(CameraAbilityImpl cameraAbilityImpl, boolean z, List<String> list) {
        Size[] sizeArr = {RESOLUTION_SIZE_1280_960, RESOLUTION_SIZE_1280_720};
        String supportedPicSizeInWideApertureMode = CustomConfigurationUtil.getSupportedPicSizeInWideApertureMode();
        if (supportedPicSizeInWideApertureMode.isEmpty() && !z) {
            Optional<String> cameraBigApertureSpecificResolution = CameraUtil.getCameraBigApertureSpecificResolution(cameraAbilityImpl);
            if (cameraBigApertureSpecificResolution.isPresent()) {
                supportedPicSizeInWideApertureMode = cameraBigApertureSpecificResolution.get();
            }
        }
        if (!supportedPicSizeInWideApertureMode.isEmpty()) {
            list.add("value|" + supportedPicSizeInWideApertureMode);
        } else {
            list.add("value|4608x3456,3968x2976,3968x2240,3648x2736,3264x2448,3328x1872,3264x1840,3264x1836");
        }
        return sizeArr;
    }

    private static void getPhotoNecessarySizes(List<Size> list, List<Size> list2) {
        if (!CollectionUtil.isEmptyCollection(list2)) {
            list.addAll(list2);
        }
    }

    private static void parseQuadSupports(CameraAbilityImpl cameraAbilityImpl, List<Size> list, List<Size> list2) {
        int[] iArr;
        int i;
        if (!(cameraAbilityImpl == null || (iArr = (int[]) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.QUARTER_SIZE)) == null || iArr.length <= 0)) {
            for (int i2 = 0; i2 < iArr.length; i2++) {
                if (i2 == 0 || (i = i2 % 4) == 0) {
                    if (list != null) {
                        list.add(new Size(iArr[i2], iArr[i2 + 1]));
                    }
                } else if (i2 != 2 && i != 2) {
                    LOGGER.debug("other conditions.", new Object[0]);
                } else if (list2 != null) {
                    list2.add(new Size(iArr[i2], iArr[i2 + 1]));
                }
            }
        }
    }

    private static List<Size> getOverDefaultResolution(CameraAbilityImpl cameraAbilityImpl) {
        ArrayList arrayList = new ArrayList();
        int[] iArr = (int[]) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.OVERDEFAULT_RESOLUTION_PICTURE_SIZE);
        if (iArr != null && iArr.length > 0) {
            int length = iArr.length >> 1;
            for (int i = 0; i < length; i++) {
                int i2 = i << 1;
                arrayList.add(new Size(iArr[i2], iArr[i2 + 1]));
            }
        }
        return arrayList;
    }

    private static String needCreateRatioRule(CameraAbilityImpl cameraAbilityImpl, int i, double d) {
        double optimalAvailableResolutionRatio = getOptimalAvailableResolutionRatio(cameraAbilityImpl, d);
        Integer num = (Integer) cameraAbilityImpl.getPropertyValue(PropertyKey.SENSOR_ORIENTATION);
        if (num == null) {
            LOGGER.warn("PropertyKey.SENSOR_ORIENTATION returns null", new Object[0]);
            return null;
        }
        boolean z = num.intValue() == 90 || num.intValue() == 270;
        boolean z2 = false;
        boolean z3 = false;
        boolean z4 = false;
        for (Size size : cameraAbilityImpl.getSupportedSizes(i)) {
            if ((!z && size.width == PREFERRED_RESOLUTION) || (z && size.height == PREFERRED_RESOLUTION)) {
                int i2 = size.width;
                int i3 = size.height;
                float f = i2 > i3 ? (((float) i2) * 1.0f) / ((float) i3) : (((float) i3) * 1.0f) / ((float) i2);
                if (Math.abs(f - 1.3333334f) < COMMON_RATIO_TOLERANCE) {
                    z2 = true;
                } else if (Math.abs(f - 1.0f) < COMMON_RATIO_TOLERANCE) {
                    z3 = true;
                } else if (Math.abs(((double) f) - optimalAvailableResolutionRatio) < 0.17000000178813934d) {
                    z4 = true;
                } else {
                    LOGGER.debug("sizeRatio is other value.", new Object[0]);
                }
                if (CustomConfigurationUtil.isFoldDispProduct()) {
                    z4 = true;
                }
                if (z2 && z3 && z4) {
                    return null;
                }
            }
        }
        StringBuilder sb = new StringBuilder("ratio|");
        if (z2) {
            sb.append("4:3,");
        }
        if (z3) {
            sb.append("1:1,");
        }
        if (z4) {
            sb.append(optimalAvailableResolutionRatio);
            sb.append(":1,");
        }
        if (sb.length() > 6) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private static double getOptimalAvailableResolutionRatio(CameraAbilityImpl cameraAbilityImpl, double d) {
        if (isResolutionRatioSupportScreenRatio(cameraAbilityImpl, d)) {
            return d;
        }
        return 1.7777777910232544d;
    }

    private static boolean isResolutionRatioSupportScreenRatio(CameraAbilityImpl cameraAbilityImpl, double d) {
        if (cameraAbilityImpl == null || Math.abs(d - -1.0d) < THRESHOLD_TWO_DOUBLE_EQUAL) {
            return false;
        }
        List<Size> supportedSizes = cameraAbilityImpl.getSupportedSizes(3);
        if (supportedSizes.isEmpty()) {
            return false;
        }
        for (Size size : supportedSizes) {
            if (Math.abs(((double) (((float) size.width) / ((float) size.height))) - d) < 0.17d) {
                return true;
            }
        }
        return false;
    }

    private static void processDefaultNecessaryValue(boolean z, List<String> list, boolean z2, List<Size> list2, List<Size> list3) {
        boolean z3 = false;
        if (!z || z2) {
            list3.addAll(list2);
        }
        if (!CollectionUtil.isEmptyCollection(list3)) {
            Iterator<String> it = list.iterator();
            while (true) {
                if (it.hasNext()) {
                    if (it.next().contains(FILTER_RULE_NECESSARY_VALUE)) {
                        z3 = true;
                        break;
                    }
                } else {
                    break;
                }
            }
            if (!z3) {
                list.add("necessary_value|" + SizeUtil.convertSizeListToString(list3));
            }
        }
    }

    private static List<Size> getSuperResolution(CameraAbilityImpl cameraAbilityImpl) {
        ArrayList arrayList = new ArrayList();
        int[] iArr = (int[]) cameraAbilityImpl.getPropertyValue(InnerPropertyKey.SUPER_RESOLUTION_PICTURE_SIZE);
        if (iArr != null && iArr.length > 0) {
            int length = iArr.length >> 1;
            for (int i = 0; i < length; i++) {
                int i2 = i << 1;
                arrayList.add(new Size(iArr[i2], iArr[i2 + 1]));
            }
        }
        return arrayList;
    }

    private static void processExcludeValue(List<String> list, List<Size> list2, List<Size> list3, List<Size> list4) {
        if (!list2.contains(RESOLUTION_40M)) {
            list3.add(RESOLUTION_40M);
        }
        if (!list2.contains(RESOLUTION_48M)) {
            list3.add(RESOLUTION_48M);
        }
        if (!CollectionUtil.isEmptyCollection(list4)) {
            for (Size size : list4) {
                if (!list2.contains(size)) {
                    list3.add(size);
                }
            }
        }
        if (!CollectionUtil.isEmptyCollection(list3)) {
            list.add("exclude_value|" + SizeUtil.convertSizeListToString(list3));
        }
    }

    private static void filterSupportsByWhiteList(List<Size> list) {
        ArrayList arrayList = new ArrayList(list);
        list.clear();
        for (Size size : resolutionWhiteList) {
            if (arrayList.contains(size)) {
                list.add(size);
            }
        }
    }

    private static List<Size> filterRuleProcessExtraRule(List<Size> list, List<String> list2) {
        filterExtraRules(list, list2);
        return list;
    }

    private static List<Size> filterRuleProcess(List<Size> list, List<String> list2, boolean z, CameraAbilityImpl cameraAbilityImpl, double d) {
        filterSupportsByWhiteList(list);
        ArrayList arrayList = new ArrayList(list);
        ArrayList arrayList2 = new ArrayList(list);
        int[] ratioCountArrayFromExtraRule = getRatioCountArrayFromExtraRule(CameraUtil.isFrontCamera(cameraAbilityImpl), list2);
        List<Size> necessarySize = getNecessarySize(list, list2);
        if (necessarySize == null || !necessarySize.contains(FRONT_NECESSARY_VALUE)) {
            filterSupportsByRatio(arrayList, ratioCountArrayFromExtraRule, null, cameraAbilityImpl, d);
        } else {
            filterSupportsByRatio(arrayList, ratioCountArrayFromExtraRule, Collections.singletonList(FRONT_NECESSARY_VALUE), cameraAbilityImpl, d);
        }
        filterExtraRules(arrayList2, list2);
        if (arrayList2.size() == 1) {
            return arrayList2;
        }
        filterSupportsByRatio(arrayList2, ratioCountArrayFromExtraRule, necessarySize, cameraAbilityImpl, d);
        if (!z && arrayList2.size() < arrayList.size()) {
            arrayList2.clear();
            arrayList2.add((Size) arrayList2.get(0));
        }
        return arrayList2;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005c, code lost:
        if (r2.equals(ohos.media.camera.mode.utils.PhotoResolutionUtil.FILTER_RULE_RATIO) != false) goto L_0x006a;
     */
    private static void filterExtraRules(List<Size> list, List<String> list2) {
        if (!(CollectionUtil.isEmptyCollection(list2) || CollectionUtil.isEmptyCollection(list))) {
            for (String str : list2) {
                List<String> split = StringUtil.split(str, STRING_VERTICAL_LINE);
                boolean z = false;
                String str2 = split.get(0);
                switch (str2.hashCode()) {
                    case 3530753:
                        if (str2.equals(FILTER_RULE_SIZE)) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case 108285963:
                        break;
                    case 111972721:
                        if (str2.equals(FILTER_RULE_VALUE)) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case 408072700:
                        if (str2.equals(FILTER_RULE_MAX_SIZE)) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case 926844620:
                        if (str2.equals(FILTER_RULE_EXCLUDE_VALUE)) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    default:
                        z = true;
                        break;
                }
                if (!z) {
                    filterExtraRulesByRatio(list, split.get(1));
                } else if (z) {
                    filterExtraRulesBySize(list, split.get(1));
                } else if (z) {
                    filterExtraRulesByValue(list, split.get(1));
                } else if (z) {
                    filterExtraRulesByMaxWidth(list, split.get(1));
                } else if (z) {
                    filterExcludeSize(list, split.get(1));
                }
            }
        }
    }

    private static void filterExtraRulesByRatio(List<Size> list, String str) {
        if (!(CollectionUtil.isEmptyCollection(list) || str == null)) {
            List<String> split = StringUtil.split(str, SEPARATOR_COMMA);
            ArrayList arrayList = new ArrayList(split.size());
            for (String str2 : split) {
                double convertRatioStringToRatio = StringUtil.convertRatioStringToRatio(str2);
                if (convertRatioStringToRatio > MIN_RATIO) {
                    arrayList.add(Double.valueOf(convertRatioStringToRatio));
                }
            }
            Iterator<Size> it = list.iterator();
            double d = CustomConfigurationUtil.isFoldDispProduct() ? 0.08d : 0.17d;
            while (it.hasNext()) {
                boolean z = true;
                Size next = it.next();
                Iterator it2 = arrayList.iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        break;
                    }
                    if (Math.abs(SizeUtil.convertSizeToRatio(next) - ((Double) it2.next()).doubleValue()) < d) {
                        z = false;
                        break;
                    }
                }
                if (z) {
                    it.remove();
                }
            }
        }
    }

    private static void filterExtraRulesBySize(List<Size> list, String str) {
        if (!CollectionUtil.isEmptyCollection(list) && str != null) {
            List<String> split = StringUtil.split(str, SEPARATOR_COMMA);
            Iterator<Size> it = list.iterator();
            while (it.hasNext()) {
                Size next = it.next();
                boolean z = false;
                Iterator<String> it2 = split.iterator();
                while (true) {
                    if (it2.hasNext()) {
                        if (SizeUtil.isSizeMatched(next, it2.next().replace("M", ""))) {
                            z = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (!z) {
                    it.remove();
                }
            }
        }
    }

    private static void filterExtraRulesByValue(List<Size> list, String str) {
        if (!(CollectionUtil.isEmptyCollection(list) || str == null)) {
            List<String> split = StringUtil.split(str, SEPARATOR_COMMA);
            ArrayList arrayList = new ArrayList(split.size());
            for (String str2 : split) {
                SizeUtil.convertSizeStringToSize(str2).ifPresent(new Consumer(arrayList) {
                    /* class ohos.media.camera.mode.utils.$$Lambda$PhotoResolutionUtil$HPbfCeaNW1qurS71MGqPI1Vrrn0 */
                    private final /* synthetic */ List f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        this.f$0.add((Size) obj);
                    }
                });
            }
            Iterator<Size> it = list.iterator();
            while (it.hasNext()) {
                if (!CollectionUtil.contains((List<Size>) arrayList, it.next())) {
                    it.remove();
                }
            }
        }
    }

    private static void filterExtraRulesByMaxWidth(List<Size> list, String str) {
        int i;
        if (!CollectionUtil.isEmptyCollection(list) && str != null) {
            List<String> split = StringUtil.split(str, SEPARATOR_COMMA);
            List<String> split2 = StringUtil.split(split.size() > 0 ? split.get(0) : "", "x");
            int i2 = -1;
            if (split2.size() >= 2) {
                try {
                    i = Integer.valueOf(split2.get(0)).intValue();
                    try {
                        i2 = Integer.valueOf(split2.get(1)).intValue();
                    } catch (NumberFormatException unused) {
                    }
                } catch (NumberFormatException unused2) {
                    i = -1;
                    LOGGER.error("NumberFormatException values[0]: %{public}s, values[1]: %{public}s", split2.get(0), split2.get(1));
                    if (i >= 0) {
                        return;
                    }
                }
            } else {
                i = -1;
            }
            if (i >= 0 && i2 >= 0) {
                Iterator<Size> it = list.iterator();
                while (it.hasNext()) {
                    Size next = it.next();
                    if (next.width > i || next.height > i2) {
                        it.remove();
                    }
                }
            }
        }
    }

    private static void filterExcludeSize(List<Size> list, String str) {
        LOGGER.debug("filterExcludeSize", new Object[0]);
        if (!(CollectionUtil.isEmptyCollection(list) || str == null)) {
            List<String> split = StringUtil.split(str, SEPARATOR_COMMA);
            ArrayList arrayList = new ArrayList(split.size());
            for (String str2 : split) {
                SizeUtil.convertSizeStringToSize(str2).ifPresent(new Consumer(arrayList) {
                    /* class ohos.media.camera.mode.utils.$$Lambda$PhotoResolutionUtil$BSlzOOi89gYNK_H6gQhAZQ8bSM */
                    private final /* synthetic */ List f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        this.f$0.add((Size) obj);
                    }
                });
            }
            Iterator<Size> it = list.iterator();
            while (it.hasNext()) {
                if (CollectionUtil.contains((List<Size>) arrayList, it.next())) {
                    it.remove();
                }
            }
        }
    }

    private static void filterSupportsByRatio(List<Size> list, int[] iArr, List<Size> list2, CameraAbilityImpl cameraAbilityImpl, double d) {
        double optimalAvailableResolutionRatio = getOptimalAvailableResolutionRatio(cameraAbilityImpl, d);
        Iterator<Size> it = list.iterator();
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        Size size = null;
        boolean z = true;
        while (it.hasNext()) {
            Size next = it.next();
            if (Math.abs(SizeUtil.convertSizeToRatio(next) - 1.3333333730697632d) < 0.17d) {
                if (!CollectionUtil.contains(list2, next) && (i = i + 1) > iArr[0]) {
                    it.remove();
                }
            } else if (Math.abs(SizeUtil.convertSizeToRatio(next) - 1.0d) < RATIO_TOLERANCE_1_1) {
                if (!CollectionUtil.contains(list2, next) && (i2 = i2 + 1) > iArr[2]) {
                    it.remove();
                }
            } else if (Math.abs(SizeUtil.convertSizeToRatio(next) - optimalAvailableResolutionRatio) < 0.17d) {
                if (!CollectionUtil.contains(list2, next) && (i3 = i3 + 1) > iArr[1]) {
                    it.remove();
                }
                z = false;
            } else {
                if (Math.abs(optimalAvailableResolutionRatio - 2.0d) < 0.17d && Math.abs(SizeUtil.convertSizeToRatio(next) - 1.7777777910232544d) < RATIO_TOLERANCE && !CollectionUtil.contains(list2, next)) {
                    if (size == null) {
                        size = next;
                    } else {
                        it.remove();
                    }
                }
                it.remove();
            }
        }
        if (!z && size != null) {
            list.remove(size);
        }
    }

    private static List<Size> getNecessarySize(List<Size> list, List<String> list2) {
        if (CollectionUtil.isEmptyCollection(list2) || CollectionUtil.isEmptyCollection(list)) {
            return Collections.emptyList();
        }
        String str = null;
        for (String str2 : list2) {
            List<String> split = StringUtil.split(str2, STRING_VERTICAL_LINE);
            boolean z = false;
            String str3 = split.get(0);
            if (str3.hashCode() != -1206709119 || !str3.equals(FILTER_RULE_NECESSARY_VALUE)) {
                z = true;
            }
            if (!z) {
                str = split.get(1);
            }
        }
        if (str == null) {
            return Collections.emptyList();
        }
        List<String> split2 = StringUtil.split(str, SEPARATOR_COMMA);
        ArrayList arrayList = new ArrayList(split2.size());
        for (String str4 : split2) {
            if (SizeUtil.convertSizeStringToSize(str4).isPresent()) {
                Size size = SizeUtil.convertSizeStringToSize(str4).get();
                if (list.contains(size)) {
                    arrayList.add(size);
                }
            }
        }
        return arrayList;
    }

    private static int[] getRatioCountArrayFromExtraRule(boolean z, List<String> list) {
        int[] iArr;
        String str;
        if (CustomConfigurationUtil.needReduceResolution()) {
            iArr = new int[]{1, 1, 1};
        } else if (z) {
            iArr = new int[]{1, 1, 1};
        } else {
            iArr = new int[]{2, 2, 1};
        }
        if (CollectionUtil.isEmptyCollection(list)) {
            return iArr;
        }
        for (String str2 : list) {
            List<String> split = StringUtil.split(str2, STRING_VERTICAL_LINE);
            if (FILTER_RULE_RATIO_COUNT.equals(split.get(0)) && (str = split.get(1)) != null) {
                List<String> split2 = StringUtil.split(str, SEPARATOR_COMMA);
                if (split2.size() >= 3) {
                    iArr = new int[3];
                    try {
                        iArr[0] = Integer.valueOf(split2.get(0)).intValue();
                        iArr[1] = Integer.valueOf(split2.get(1)).intValue();
                        iArr[2] = Integer.valueOf(split2.get(2)).intValue();
                    } catch (NumberFormatException unused) {
                        LOGGER.error("NumberFormatException ratioCounts[0]: %{public}s ,ratioCounts[1]: %{public}s, ratioCounts[2]: %{public}s", split2.get(0), split2.get(1), split2.get(2));
                    }
                }
            }
        }
        return iArr;
    }

    private static List<Size> reducePreviewSize(List<Size> list, List<Size> list2, String str, Map<ModeRulesParamType, Object> map) {
        ArrayList arrayList = new ArrayList();
        if (list2 == null) {
            return list;
        }
        for (Size size : list2) {
            Size optimalPreviewSize = getOptimalPreviewSize(list, ((double) size.width) / ((double) size.height), str, map);
            if (optimalPreviewSize != null) {
                arrayList.add(optimalPreviewSize);
            }
        }
        LOGGER.info("reducePreviewSize: %{public}s", arrayList);
        return arrayList;
    }

    private static Size getOptimalPreviewSize(List<Size> list, double d, String str, Map<ModeRulesParamType, Object> map) {
        if (list == null) {
            return null;
        }
        Object obj = map.get(ModeRulesParamType.LIMIT_PREVIEW_SIZE_TO_720P);
        boolean booleanValue = (obj == null || !(obj instanceof Boolean)) ? false : ((Boolean) obj).booleanValue();
        Object obj2 = map.get(ModeRulesParamType.LIMIT_PREVIEW_SIZE_WIDTH_1280);
        boolean booleanValue2 = (obj2 == null || !(obj2 instanceof Boolean)) ? false : ((Boolean) obj2).booleanValue();
        Size previewSizeUnderMaxValueWithRatio = getPreviewSizeUnderMaxValueWithRatio(list, d, getCustomizePreviewSizeByHardwareFeature(booleanValue, str), booleanValue2);
        if (previewSizeUnderMaxValueWithRatio == null) {
            previewSizeUnderMaxValueWithRatio = getPreviewSizeUnderMaxValueWithRatio(list, d, 65536, booleanValue2);
        }
        if (previewSizeUnderMaxValueWithRatio == null) {
            previewSizeUnderMaxValueWithRatio = getPreviewSizeUnderMaxValueWithoutRatio(list, getCustomizePreviewSizeByHardwareFeature(booleanValue, str));
        }
        if (previewSizeUnderMaxValueWithRatio == null) {
            previewSizeUnderMaxValueWithRatio = getPreviewSizeUnderMaxValueWithoutRatio(list, 65536);
        }
        if (previewSizeUnderMaxValueWithRatio != null) {
            LOGGER.debug("use previewSize %{public}s", previewSizeUnderMaxValueWithRatio);
        }
        return previewSizeUnderMaxValueWithRatio;
    }

    private static int getCustomizePreviewSizeByHardwareFeature(boolean z, String str) {
        Size screenSize = DeviceUtil.getScreenSize();
        if (!z && Math.min(screenSize.width, screenSize.height) >= 1000) {
            return 1088;
        }
        LOGGER.info("hard condition ,use 720p", new Object[0]);
        return PREVIEW_SIZE_720P;
    }

    private static Size getPreviewSizeUnderMaxValueWithRatio(List<Size> list, double d, int i, boolean z) {
        Size size = null;
        if (list == null) {
            return null;
        }
        double d2 = Double.MAX_VALUE;
        double d3 = CustomConfigurationUtil.isFoldDispProduct() ? 0.08d : 0.17d;
        for (Size size2 : list) {
            if (size2.width != 1088 || size2.height != WIDE_VIEW_HEIGHT) {
                if (size2.height <= i && Math.abs((((double) size2.width) / ((double) size2.height)) - d) <= d3) {
                    if (z && size2.width <= PREVIEW_SIZE_1280) {
                        return size2;
                    }
                    if (((double) Math.abs(size2.height - 1088)) < d2) {
                        d2 = (double) Math.abs(size2.height - 1088);
                        size = size2;
                    }
                }
            }
        }
        return size;
    }

    private static Size getPreviewSizeUnderMaxValueWithoutRatio(List<Size> list, int i) {
        Size size = null;
        if (list == null) {
            return null;
        }
        double d = Double.MAX_VALUE;
        for (Size size2 : list) {
            if (size2.height <= i && ((double) Math.abs(size2.height - 1088)) < d) {
                d = (double) Math.abs(size2.height - 1088);
                size = size2;
            }
        }
        return size;
    }
}
