package android.util;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;

public class FeatureFlagUtils {
    private static final Map<String, String> DEFAULT_FLAGS = new HashMap();
    public static final String DYNAMIC_SYSTEM = "settings_dynamic_system";
    public static final String FFLAG_OVERRIDE_PREFIX = "sys.fflag.override.";
    public static final String FFLAG_PREFIX = "sys.fflag.";
    public static final String HEARING_AID_SETTINGS = "settings_bluetooth_hearing_aid";
    public static final String PERSIST_PREFIX = "persist.sys.fflag.override.";
    public static final String PIXEL_WALLPAPER_CATEGORY_SWITCH = "settings_pixel_wallpaper_category_switch";
    public static final String SCREENRECORD_LONG_PRESS = "settings_screenrecord_long_press";
    public static final String SEAMLESS_TRANSFER = "settings_seamless_transfer";

    static {
        DEFAULT_FLAGS.put("settings_audio_switcher", "true");
        DEFAULT_FLAGS.put("settings_mobile_network_v2", "true");
        DEFAULT_FLAGS.put("settings_network_and_internet_v2", "true");
        DEFAULT_FLAGS.put("settings_systemui_theme", "true");
        DEFAULT_FLAGS.put(DYNAMIC_SYSTEM, "false");
        DEFAULT_FLAGS.put(SEAMLESS_TRANSFER, "false");
        DEFAULT_FLAGS.put(HEARING_AID_SETTINGS, "false");
        DEFAULT_FLAGS.put(SCREENRECORD_LONG_PRESS, "false");
        DEFAULT_FLAGS.put(PIXEL_WALLPAPER_CATEGORY_SWITCH, "false");
        DEFAULT_FLAGS.put("settings_wifi_details_datausage_header", "false");
    }

    public static boolean isEnabled(Context context, String feature) {
        if (context != null) {
            String value = Settings.Global.getString(context.getContentResolver(), feature);
            if (!TextUtils.isEmpty(value)) {
                return Boolean.parseBoolean(value);
            }
        }
        String value2 = SystemProperties.get(FFLAG_OVERRIDE_PREFIX + feature);
        if (!TextUtils.isEmpty(value2)) {
            return Boolean.parseBoolean(value2);
        }
        return Boolean.parseBoolean(getAllFeatureFlags().get(feature));
    }

    public static void setEnabled(Context context, String feature, boolean enabled) {
        SystemProperties.set(FFLAG_OVERRIDE_PREFIX + feature, enabled ? "true" : "false");
    }

    public static Map<String, String> getAllFeatureFlags() {
        return DEFAULT_FLAGS;
    }
}
