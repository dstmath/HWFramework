package android.util;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;

public class FeatureFlagUtils {
    private static final Map<String, String> DEFAULT_FLAGS = new HashMap();
    public static final String FFLAG_OVERRIDE_PREFIX = "sys.fflag.override.";
    public static final String FFLAG_PREFIX = "sys.fflag.";

    static {
        DEFAULT_FLAGS.put("settings_battery_display_app_list", "false");
        DEFAULT_FLAGS.put("settings_zone_picker_v2", "true");
        DEFAULT_FLAGS.put("settings_about_phone_v2", "true");
        DEFAULT_FLAGS.put("settings_bluetooth_while_driving", "false");
        DEFAULT_FLAGS.put("settings_data_usage_v2", "true");
        DEFAULT_FLAGS.put("settings_audio_switcher", "true");
        DEFAULT_FLAGS.put("settings_systemui_theme", "true");
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
