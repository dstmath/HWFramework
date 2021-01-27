package com.huawei.aod;

import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

public class AodThemeConst {
    public static final int ANALOG_CLOCK_STYLE = 1000;
    public static final int ANALOG_CLOCK_THEME = 3;
    public static final int ANALOG_DOUBLE_CLOCK1_BASE_INDEX = 0;
    public static final int ANALOG_DOUBLE_CLOCK1_HOUR_INDEX = 1;
    public static final int ANALOG_DOUBLE_CLOCK1_MINUTE_INDEX = 2;
    public static final int ANALOG_DOUBLE_CLOCK1_SECOND_INDEX = 6;
    public static final int ANALOG_DOUBLE_CLOCK2_BASE_INDEX = 3;
    public static final int ANALOG_DOUBLE_CLOCK2_HOUR_INDEX = 4;
    public static final int ANALOG_DOUBLE_CLOCK2_MINUTE_INDEX = 5;
    public static final int ANALOG_DOUBLE_CLOCK2_SECOND_INDEX = 7;
    public static final int ANALOG_DOUBLE_CLOCK_RES_SIZE = 6;
    public static final int ANALOG_DOUBLE_HOUR1_RES_INDEX = 0;
    public static final int ANALOG_DOUBLE_HOUR2_RES_INDEX = 2;
    public static final int ANALOG_DOUBLE_MINUTE1_RES_INDEX = 1;
    public static final int ANALOG_DOUBLE_MINUTE2_RES_INDEX = 3;
    public static final int ANALOG_DOUBLE_RES_CLOCK1_HOUR = 0;
    public static final int ANALOG_DOUBLE_RES_CLOCK1_MINUTE = 1;
    public static final int ANALOG_DOUBLE_RES_CLOCK2_HOUR = 2;
    public static final int ANALOG_DOUBLE_RES_CLOCK2_MINUTE = 3;
    public static final int ANALOG_DOUBLE_RES_SIZE = 6;
    public static final int ANALOG_DOUBLE_SECOND1_RES_INDEX = 4;
    public static final int ANALOG_DOUBLE_SECOND2_RES_INDEX = 5;
    public static final int ANALOG_SINGLE_CLOCK_BASE_INDEX = 0;
    public static final int ANALOG_SINGLE_CLOCK_HOUR_INDEX = 1;
    public static final int ANALOG_SINGLE_CLOCK_MINUTE_INDEX = 2;
    public static final int ANALOG_SINGLE_CLOCK_RES_SIZE = 4;
    public static final int ANALOG_SINGLE_CLOCK_RES_SIZE_NO_SECOND = 3;
    public static final int ANALOG_SINGLE_CLOCK_SECOND_INDEX = 3;
    public static final int ANALOG_SINGLE_HOUR_RES_INDEX = 0;
    public static final int ANALOG_SINGLE_MINUTE_RES_INDEX = 1;
    public static final int ANALOG_SINGLE_RES_SIZE = 3;
    public static final int ANALOG_SINGLE_SECOND_RES_INDEX = 2;
    public static final String ANIMATION_NUM = "animation_num";
    public static final String ANIMATION_RES = "animation_res";
    public static final String ANIM_DUAL_RES_DIR = "/data/user_de/0/com.huawei.aod/bitmap/animation_res_dual/";
    public static final String ANIM_FILE_NAME = "animation";
    public static final String ANIM_FILE_TILE = ".txt";
    public static final int ANIM_MAX = 60;
    public static final String ANIM_RES_DIR = "/data/user_de/0/com.huawei.aod/bitmap/animation_res/";
    public static final String ANIM_ROTATION_DUAL_RES_DIR = "/data/user_de/0/com.huawei.aod/bitmap/animation_rotation_res_dual/";
    public static final String ANIM_ROTATION_RES_DIR = "/data/user_de/0/com.huawei.aod/bitmap/animation_rotation_res/";
    public static final String AOD_THEME_VERSION = "aod_theme_version";
    public static final float ART_PATTERN_SCALE = 0.75f;
    public static final int ART_SIGNATURES_STYLE_CIRCLE = 106;
    public static final int ART_SIGNATURES_STYLE_DYNAMIC = 110;
    public static final int ART_SIGNATURES_STYLE_FIRST_COMMON = 108;
    public static final int ART_SIGNATURES_STYLE_GRAFFITI = 107;
    public static final int ART_SIGNATURES_STYLE_HAND_WRITING = 105;
    public static final int ART_SIGNATURES_STYLE_SECOND_COMMON = 109;
    public static final String ART_SIGNATURE_RES_NUM = "bitmap_length";
    public static final int BACKGROUND_RES_CLOCK_THEME = 4;
    public static final String BITMAP_ART_FILE_NAME = "art_signature";
    public static final String BYTE_COUNT = "bitmap_byte_count";
    public static final String BYTE_COUNT_DUAL = "bitmap_byte_count_dual";
    public static final int COLOR_CLOCK_THEME = 2;
    public static final int CONST_VALUE_TWO = 2;
    public static final int DIGITAL_CLOCK_HORIZONCAL_STYLE1 = 11;
    public static final int DIGITAL_CLOCK_HORIZONCAL_STYLE2 = 12;
    public static final int DIGITAL_CLOCK_HORIZONCAL_STYLE3 = 13;
    public static final int DIGITAL_CLOCK_HORIZONCAL_STYLE4 = 14;
    public static final int DIGITAL_CLOCK_HORIZONCAL_STYLE5 = 16;
    public static final int DIGITAL_CLOCK_USER_CUSTOM_PICTURE = 15;
    public static final int DIGITAL_CLOCK_VERTICAL_STYLE1 = 101;
    public static final int DIGITAL_CLOCK_VERTICAL_STYLE2 = 102;
    public static final int DIGITAL_CLOCK_VERTICAL_STYLE2_SMALL = 1021;
    public static final int DIGITAL_CLOCK_VERTICAL_STYLE3 = 103;
    public static final int DIGITAL_CLOCK_VERTICAL_STYLE4 = 104;
    public static final int DIGITAL_COLOR_CLOCK_DEFAULT_HORIZONCAL_STYLE = 20;
    public static final int EXTEND_ANALOG_CLOCK = 3;
    public static final int EXTEND_CHINA_CLOCK = 6;
    public static final int EXTEND_DIGITAL_CLOCK = 2;
    public static final int EXTEND_FOREST_CLOCK = 7;
    public static final int EXTEND_PATTERN_CLOCK = 4;
    public static final int EXTEND_WHITE_CLOCK = 5;
    public static final int FILE_END = -1;
    public static final String FILE_UNDERLINE = "_";
    public static final String IS_ANIMATIONABLE = "is_animationable";
    public static final String PERSONALITY_STYLE = "personalityPictureClock";
    public static final String[] PNG_TYPES = {".png", ".PNG"};
    public static final String PRODUCT_CUSTOMIZED = "product_customized";
    public static final String SEPATATE_OPERATOR = ",";
    public static final String SHOW_PIC_INDEX = "show_pic_index";
    private static final int SIGNATURE_COUNT = 6;
    public static final String SPLASH = "/";
    public static final String STATIC_BUTTERFLY_STYLE = "staticButterflyClock";
    public static final String STATIC_FISH_STYLE = "staticFishClock";
    private static final int SUPPORT_AOD3_PLATFORM_COUNT = 8;
    public static final String THEME_ANALOG_DOUBLE_CLOCK_RES_KEY = "analog_dual_res";
    public static final String THEME_ANALOG_SINGLE_CLOCK_RES_KEY = "analog_single_res";
    public static final String THEME_BACKGROUND_KEY = "bg_res";
    public static final String THEME_DEFINE_KEY = "theme_define";
    public static final int THEME_DEFINE_USE_RES_ID = 0;
    public static final int THEME_DEFINE_USE_RES_NAME = 1;
    public static final String THEME_DIGIT_DOUBLE_CLOCK_RES_KEY = "digit_dual_res";
    public static final String THEME_DIGIT_SINGLE_CLOCK_RES_KEY = "digit_single_res";
    public static final String THEME_NAME_KEY = "name";
    public static final String THEME_PREFERENCE = "com.huawei.aod.theme_preferences";
    public static final String THEME_PRELOAD_KEY = "preseted";
    public static final String THEME_STYLE_KEY = "style";
    public static final String THEME_STYLE_NAME = "style_name";
    public static final String USER_DOWNLOAD_DIR = "/data/user_de/0/com.huawei.aod/downloadtheme/res/drawable-xxhdpi/";
    public static final String VERTICAL_BG_FG_TYPE = "bg_fg_type";
    public static final int VERTICAL_CLOCK_STYLE = 5;
    public static final String VMALL_CUSTOMIZED = "vmall_customized";
    private static List<Integer> sIsNeedReloadBitmapTypes = new ArrayList<Integer>(6) {
        /* class com.huawei.aod.AodThemeConst.AnonymousClass2 */

        {
            add(8);
        }
    };
    private static List<Integer> sIsNeedReloadClockTypes = new ArrayList<Integer>(6) {
        /* class com.huawei.aod.AodThemeConst.AnonymousClass3 */

        {
            add(15);
            add(102);
            add(1021);
            add(105);
            add(106);
            add(107);
            add(108);
            add(109);
            add(110);
        }
    };
    private static List<Integer> sIsSignatureStyle = new ArrayList<Integer>(6) {
        /* class com.huawei.aod.AodThemeConst.AnonymousClass1 */

        {
            add(105);
            add(106);
            add(107);
            add(108);
            add(109);
            add(110);
        }
    };
    private static List<String> sSupportAod3Platforms = new ArrayList<String>(8) {
        /* class com.huawei.aod.AodThemeConst.AnonymousClass4 */

        {
            add("BALTIMORE");
            add("KIRIN990");
            add("KIRIN990E");
            add("KIRIN985");
            add("KIRIN820");
            add("KIRIN9000");
            add("KIRIN9000E");
            add("DENVER");
        }
    };

    private AodThemeConst() {
    }

    public static List<Integer> getSignatureStyles() {
        return sIsSignatureStyle;
    }

    public static boolean isAodSignature(int type) {
        return getSignatureStyles().contains(Integer.valueOf(type));
    }

    public static List<Integer> getNeedReloadBitmapTypes() {
        return sIsNeedReloadBitmapTypes;
    }

    public static boolean isNeedReloadBitmapType(int bitmapType) {
        return getNeedReloadBitmapTypes().contains(Integer.valueOf(bitmapType));
    }

    public static List<Integer> getNeedReloadClockTypes() {
        return sIsNeedReloadClockTypes;
    }

    public static boolean isNeedReloadClockType(int clockType) {
        return getNeedReloadClockTypes().contains(Integer.valueOf(clockType));
    }

    public static List<String> getSupportAod3Platforms() {
        return sSupportAod3Platforms;
    }

    public static boolean isSupportAod3Platform(String platform) {
        if (TextUtils.isEmpty(platform)) {
            return false;
        }
        return getSupportAod3Platforms().contains(platform);
    }
}
