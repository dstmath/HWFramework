package com.huawei.aod;

import android.os.SystemProperties;

public class AodConst {
    public static final int AOD_QUIT_COMMON = 0;
    public static final int AOD_QUIT_EXT_TO_MAX_PANEL = 2;
    public static final int AOD_QUIT_MAX_TO_EXT_PANEL = 1;
    public static final int BYTES_COUNT_FOR_ONE_PIX = 2;
    public static final int DISPLAY_DEFAULT_CONFIG = SystemProperties.getInt("hw_mc.aod.display_default_config", 1);
    public static final int FINGERPRINT_PICTURE_COUNT = 4;
    public static final int FINGER_LOGO_RECT_INDEX_DUAL = 9;
    public static final int FINGER_LOGO_RECT_INDEX_SINGLE = 5;
    public static final int MAGIC_BYTE_COUNT = 4;
    public static final int NOT_OPEN_DEFAULT = 0;
    public static final int PANEL_FOLD_EXT_PANEL = 1;
    public static final int PANEL_FOLD_MAIN_PANEL = 0;
    public static final int PANEL_FOLD_MAX_PANEL = 2;
    public static final int TEXT_NUMBER_COUNT = 10;
}
