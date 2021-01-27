package android.view;

import android.os.SystemProperties;

public abstract class AbsLayoutParams {
    public static final int BLUR_ALPHA_DYNAMIC_CHANGE = 8;
    public static final int BLUR_DYNAMIC_CHANGE = 1;
    public static final boolean BLUR_FEATURE_ENABLED;
    public static final int BLUR_FULL_WINDOW = 4;
    public static final int BLUR_STYLE_DEFAULT = -1;
    public static final int BLUR_TWICE = 2;
    public static final int FLAG_ALLOW_ALWAYS_SHOW_SYSTEM_NON_OVERLAY_WINDOWS = 1024;
    public static final int FLAG_ALLOW_SHOW_NON_SYSTEM_OVERLAY_WINDOWS = 128;
    public static final int FLAG_BLUR_BEHIND = 33554432;
    public static final int FLAG_BLUR_SELF = 67108864;
    public static final int FLAG_DESTORY_SURFACE = 2;
    public static final int FLAG_EMUI_LIGHT_STYLE = 16;
    public static final int FLAG_EXCLUDE_INJECT_EVENT = 262144;
    public static final int FLAG_EXCLUDE_TRANSFER_EVENT = 131072;
    public static final int FLAG_FLOAT_STATE_IME = 1048576;
    public static final int FLAG_GESTURE_NAV_DISABLE_BOTTOM = 524288;
    public static final int FLAG_GESTURE_NAV_DISABLE_LEFT = 4194304;
    public static final int FLAG_GESTURE_NAV_DISABLE_RIGHT = 8388608;
    public static final int FLAG_GESTURE_NAV_WINDOW = 2097152;
    public static final int FLAG_IGNORE_NAVIGATIONBAR_OR_STATURBAR = 256;
    public static final int FLAG_IGNORE_SNAPSHOT_IF_NOT_ONLY = 2048;
    public static final int FLAG_KEYEVENT_PASS_TO_USER_HOME = Integer.MIN_VALUE;
    public static final int FLAG_LAZY_MODE_OVERLAY = 64;
    public static final int FLAG_MMI_TEST_DEFAULT_SHAPE = 16384;
    public static final int FLAG_MMI_TEST_VOLUME_UP_DOWN = 8;
    public static final int FLAG_NOTCH_SUPPORT = 65536;
    public static final int FLAG_PRIVATE_SURFACE = 134217728;
    public static final int FLAG_SCALED_WINDOW = 32768;
    public static final int FLAG_SECURE_SCREENCAP = 8192;
    public static final int FLAG_SECURE_SCREENSHOT = 4096;
    public static final int FLAG_SEC_IME_RAISE = 32;
    public static final int FLAG_SHARE_DIALOG = 1;
    public static final int FLAG_STATUS_BAR_PANEL_EXPANDED = 4;
    public static final int FLAG_SUBSCREEN_GESTURE_NAV_AREA = 16777216;
    public static final int FLAG_WINDOW_CHANGED = 32768;
    public static final int FLAG_WINDOW_MAGIC_WIN = 1073741824;
    public static final int FLAG_WINDOW_NO_ACTION_BAR = 536870912;
    public static final int FLAG_WINDOW_TRANSLUCENT = 268435456;
    public static final int LAYOUT_IN_DISPLAY_SIDE_MODE_ALWAYS = 1;
    public static final int LAYOUT_IN_DISPLAY_SIDE_MODE_DEFAULT = 0;
    public static final int LAYOUT_IN_DISPLAY_SIDE_MODE_NEVER = 2;
    public static final int STYLE_BACKGROUND_LARGE_DARK = 106;
    public static final int STYLE_BACKGROUND_LARGE_LIGHT = 102;
    public static final int STYLE_BACKGROUND_MEDIUM_DARK = 105;
    public static final int STYLE_BACKGROUND_MEDIUM_LIGHT = 101;
    public static final int STYLE_BACKGROUND_SMALL_DARK = 104;
    public static final int STYLE_BACKGROUND_SMALL_LIGHT = 100;
    public static final int STYLE_BACKGROUND_XLARGE_DARK = 107;
    public static final int STYLE_BACKGROUND_XLARGE_LIGHT = 103;
    public static final int STYLE_CARD_DARK = 5;
    public static final int STYLE_CARD_DIM_BACK_DARK = 7;
    public static final int STYLE_CARD_DIM_BACK_LIGHT = 3;
    public static final int STYLE_CARD_LIGHT = 1;
    public static final int STYLE_CARD_THICK_DARK = 6;
    public static final int STYLE_CARD_THICK_LIGHT = 2;
    public static final int STYLE_CARD_THIN_DARK = 4;
    public static final int STYLE_CARD_THIN_LIGHT = 0;
    public static final int SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED = 512;

    static {
        boolean z = false;
        if (SystemProperties.getInt("emui_hwagp_window_blur_effect_enable", 0) != 0) {
            z = true;
        }
        BLUR_FEATURE_ENABLED = z;
    }
}
