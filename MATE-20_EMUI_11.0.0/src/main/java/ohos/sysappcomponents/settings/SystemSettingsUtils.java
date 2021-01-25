package ohos.sysappcomponents.settings;

import android.content.ContentResolver;
import android.content.Context;
import java.util.HashMap;
import java.util.Map;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.sysappcomponents.settings.SystemSettings;

public class SystemSettingsUtils {
    private static final HashMap<String, String> A2Z_NAME_MAP = new HashMap<>();
    private static final String GLOBAL = "global";
    private static final String SECURE = "secure";
    private static final String SYSTEM = "system";
    private static final HashMap<String, String> Z2A_NAME_MAP = new HashMap<>();
    private static final HashMap<String, String> Z_NAME_SOURCE = new HashMap<>();

    static {
        Z_NAME_SOURCE.put(SystemSettings.Input.AUTO_CAPS_TEXT_INPUT, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Input.AUTO_PUNCTUATE_TEXT_INPUT, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Input.AUTO_REPLACE_TEXT_INPUT, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Input.SHOW_PASSWORD_TEXT_INPUT, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.General.SETUP_WIZARD_FINISHED, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.General.END_BUTTON_ACTION, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.General.ACCELEROMETER_ROTATION_STATUS, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Date.DATE_FORMAT, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Date.TIME_FORMAT, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Display.FONT_SCALE, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Display.SCREEN_BRIGHTNESS_STATUS, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Display.AUTO_SCREEN_BRIGHTNESS, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Display.SCREEN_OFF_TIMEOUT, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Display.DEFAULT_SCREEN_ROTATION, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Wireless.BLUETOOTH_DISCOVERABILITY_STATUS, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Wireless.BLUETOOTH_DISCOVER_TIMEOUT, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Sound.VIBRATE_WHILE_RINGING, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Sound.DEFAULT_ALARM_ALERT, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Sound.DTMF_TONE_TYPE_WHILE_DIALING, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Sound.DTMF_TONE_WHILE_DIALING, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Sound.AFFECTED_MODE_RINGER_STREAMS, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Sound.AFFECTED_MUTE_STREAMS, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Sound.DEFAULT_NOTIFICATION_SOUND, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Sound.DEFAULT_RINGTONE, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Sound.SOUND_EFFECTS_STATUS, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Sound.VIBRATE_STATUS, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.Sound.HAPTIC_FEEDBACK_STATUS, SYSTEM);
        Z_NAME_SOURCE.put(SystemSettings.TTS.DEFAULT_TTS_PITCH, SECURE);
        Z_NAME_SOURCE.put(SystemSettings.TTS.DEFAULT_TTS_RATE, SECURE);
        Z_NAME_SOURCE.put(SystemSettings.TTS.DEFAULT_TTS_SYNTH, SECURE);
        Z_NAME_SOURCE.put(SystemSettings.TTS.ENABLED_TTS_PLUGINS, SECURE);
        Z_NAME_SOURCE.put(SystemSettings.Input.ACTIVATED_INPUT_METHOD_SUBMODE, SECURE);
        Z_NAME_SOURCE.put(SystemSettings.Input.ACTIVATED_INPUT_METHODS, SECURE);
        Z_NAME_SOURCE.put(SystemSettings.Input.DEFAULT_INPUT_METHOD, SECURE);
        Z_NAME_SOURCE.put(SystemSettings.Input.SELECTOR_VISIBILITY_FOR_INPUT_METHOD, SECURE);
        Z_NAME_SOURCE.put(SystemSettings.General.ACCESSIBILITY_STATUS, SECURE);
        Z_NAME_SOURCE.put(SystemSettings.General.ACTIVATED_ACCESSIBILITY_SERVICES, SECURE);
        Z_NAME_SOURCE.put(SystemSettings.General.GEOLOCATION_ORIGINS_ALLOWED, SECURE);
        Z_NAME_SOURCE.put(SystemSettings.General.SKIP_USE_HINTS, SECURE);
        Z_NAME_SOURCE.put(SystemSettings.General.TOUCH_EXPLORATION_STATUS, SECURE);
        Z_NAME_SOURCE.put(SystemSettings.Display.DISPLAY_INVERSION_STATUS, SECURE);
        Z_NAME_SOURCE.put(SystemSettings.Sound.DEFAULT_ALARM_ALERT, SECURE);
        Z_NAME_SOURCE.put(SystemSettings.General.AIRPLANE_MODE, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.General.DEVICE_PROVISION_STATUS, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.General.HDC_STATUS, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.General.BOOT_COUNTING, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.General.CONTACT_METADATA_SYNC_STATUS, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.General.DEBUG_APP_PACKAGE, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.General.DEVELOPMENT_SETTINGS_STATUS, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.General.DEVICE_NAME, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.General.USB_STORAGE_STATUS, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.General.DEBUGGER_WAITING, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Network.DATA_ROAMING_STATUS, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Network.HTTP_PROXY_CFG, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Network.NETWORK_PREFERENCE_USAGE, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Date.AUTO_GAIN_TIME, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Date.AUTO_GAIN_TIME_ZONE, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Display.ANIMATOR_DURATION_SCALE, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Display.TRANSITION_ANIMATION_SCALE, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Display.WINDOW_ANIMATION_SCALE, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Wireless.AIRPLANE_MODE_RADIOS, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Wireless.BLUETOOTH_STATUS, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Wireless.BLUETOOTH_RADIO, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Wireless.CELL_RADIO, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Wireless.NFC_RADIO, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Wireless.WIFI_RADIO, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Wireless.OWNER_LOCKDOWN_WIFI_CFG, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Wireless.WIFI_DHCP_MAX_RETRY_COUNT, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Wireless.WIFI_TO_MOBILE_DATA_AWAKE_TIMEOUT, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Wireless.WIFI_STATUS, GLOBAL);
        Z_NAME_SOURCE.put(SystemSettings.Wireless.WIFI_WATCHDOG_STATUS, GLOBAL);
        Z2A_NAME_MAP.put(SystemSettings.Sound.VIBRATE_WHILE_RINGING, "vibrate_when_ringing");
        Z2A_NAME_MAP.put(SystemSettings.Sound.HAPTIC_FEEDBACK_STATUS, "haptic_feedback_enabled");
        Z2A_NAME_MAP.put(SystemSettings.General.ACCELEROMETER_ROTATION_STATUS, "accelerometer_rotation");
        Z2A_NAME_MAP.put(SystemSettings.Sound.DEFAULT_ALARM_ALERT, "alarm_alert");
        Z2A_NAME_MAP.put(SystemSettings.Wireless.BLUETOOTH_DISCOVERABILITY_STATUS, "bluetooth_discoverability");
        Z2A_NAME_MAP.put(SystemSettings.Wireless.BLUETOOTH_DISCOVER_TIMEOUT, "bluetooth_discoverability_timeout");
        Z2A_NAME_MAP.put(SystemSettings.Date.DATE_FORMAT, SystemSettings.Date.DATE_FORMAT);
        Z2A_NAME_MAP.put(SystemSettings.Sound.DTMF_TONE_TYPE_WHILE_DIALING, "dtmf_tone_type");
        Z2A_NAME_MAP.put(SystemSettings.Sound.DTMF_TONE_WHILE_DIALING, "dtmf_tone");
        Z2A_NAME_MAP.put(SystemSettings.General.END_BUTTON_ACTION, "end_button_behavior");
        Z2A_NAME_MAP.put(SystemSettings.Display.FONT_SCALE, SystemSettings.Display.FONT_SCALE);
        Z2A_NAME_MAP.put(SystemSettings.Sound.AFFECTED_MODE_RINGER_STREAMS, "mode_ringer_streams_affected");
        Z2A_NAME_MAP.put(SystemSettings.Sound.AFFECTED_MUTE_STREAMS, "mute_streams_affected");
        Z2A_NAME_MAP.put(SystemSettings.Sound.DEFAULT_NOTIFICATION_SOUND, "notification_sound");
        Z2A_NAME_MAP.put(SystemSettings.Sound.DEFAULT_RINGTONE, "ringtone");
        Z2A_NAME_MAP.put(SystemSettings.Display.SCREEN_BRIGHTNESS_STATUS, "screen_brightness");
        Z2A_NAME_MAP.put(SystemSettings.Display.AUTO_SCREEN_BRIGHTNESS, "screen_brightness_mode");
        Z2A_NAME_MAP.put(SystemSettings.Display.SCREEN_OFF_TIMEOUT, SystemSettings.Display.SCREEN_OFF_TIMEOUT);
        Z2A_NAME_MAP.put(SystemSettings.General.SETUP_WIZARD_FINISHED, "setup_wizard_has_run");
        Z2A_NAME_MAP.put(SystemSettings.Sound.SOUND_EFFECTS_STATUS, "sound_effects_enabled");
        Z2A_NAME_MAP.put(SystemSettings.Input.AUTO_CAPS_TEXT_INPUT, "auto_caps");
        Z2A_NAME_MAP.put(SystemSettings.Input.AUTO_PUNCTUATE_TEXT_INPUT, "auto_punctuate");
        Z2A_NAME_MAP.put(SystemSettings.Input.AUTO_REPLACE_TEXT_INPUT, "auto_replace");
        Z2A_NAME_MAP.put(SystemSettings.Input.SHOW_PASSWORD_TEXT_INPUT, "show_password");
        Z2A_NAME_MAP.put(SystemSettings.Date.TIME_FORMAT, "time_12_24");
        Z2A_NAME_MAP.put(SystemSettings.Display.DEFAULT_SCREEN_ROTATION, "user_rotation");
        Z2A_NAME_MAP.put(SystemSettings.Sound.VIBRATE_STATUS, "vibrate_on");
        Z2A_NAME_MAP.put(SystemSettings.General.AIRPLANE_MODE, "airplane_mode_on");
        Z2A_NAME_MAP.put(SystemSettings.General.DEVICE_PROVISION_STATUS, "device_provisioned");
        Z2A_NAME_MAP.put(SystemSettings.General.HDC_STATUS, "adb_enabled");
        Z2A_NAME_MAP.put(SystemSettings.Wireless.AIRPLANE_MODE_RADIOS, SystemSettings.Wireless.AIRPLANE_MODE_RADIOS);
        Z2A_NAME_MAP.put(SystemSettings.Display.ANIMATOR_DURATION_SCALE, SystemSettings.Display.ANIMATOR_DURATION_SCALE);
        Z2A_NAME_MAP.put(SystemSettings.Date.AUTO_GAIN_TIME, "auto_time");
        Z2A_NAME_MAP.put(SystemSettings.Date.AUTO_GAIN_TIME_ZONE, "auto_time_zone");
        Z2A_NAME_MAP.put(SystemSettings.Wireless.BLUETOOTH_STATUS, "bluetooth_on");
        Z2A_NAME_MAP.put(SystemSettings.General.BOOT_COUNTING, "boot_count");
        Z2A_NAME_MAP.put(SystemSettings.General.CONTACT_METADATA_SYNC_STATUS, "contact_metadata_sync_enabled");
        Z2A_NAME_MAP.put(SystemSettings.Network.DATA_ROAMING_STATUS, "data_roaming");
        Z2A_NAME_MAP.put(SystemSettings.General.DEBUG_APP_PACKAGE, "debug_app");
        Z2A_NAME_MAP.put(SystemSettings.General.DEVELOPMENT_SETTINGS_STATUS, "development_settings_enabled");
        Z2A_NAME_MAP.put(SystemSettings.General.DEVICE_NAME, SystemSettings.General.DEVICE_NAME);
        Z2A_NAME_MAP.put(SystemSettings.Network.HTTP_PROXY_CFG, "http_proxy");
        Z2A_NAME_MAP.put(SystemSettings.Network.NETWORK_PREFERENCE_USAGE, "network_preference");
        Z2A_NAME_MAP.put(SystemSettings.Wireless.BLUETOOTH_RADIO, "bluetooth");
        Z2A_NAME_MAP.put(SystemSettings.Wireless.CELL_RADIO, "cell");
        Z2A_NAME_MAP.put(SystemSettings.Wireless.NFC_RADIO, "nfc");
        Z2A_NAME_MAP.put(SystemSettings.Wireless.WIFI_RADIO, "wifi");
        Z2A_NAME_MAP.put(SystemSettings.Display.TRANSITION_ANIMATION_SCALE, SystemSettings.Display.TRANSITION_ANIMATION_SCALE);
        Z2A_NAME_MAP.put(SystemSettings.General.USB_STORAGE_STATUS, "usb_mass_storage_enabled");
        Z2A_NAME_MAP.put(SystemSettings.General.DEBUGGER_WAITING, "wait_for_debugger");
        Z2A_NAME_MAP.put(SystemSettings.Wireless.OWNER_LOCKDOWN_WIFI_CFG, "wifi_device_owner_configs_lockdown");
        Z2A_NAME_MAP.put(SystemSettings.Wireless.WIFI_DHCP_MAX_RETRY_COUNT, "wifi_max_dhcp_retry_count");
        Z2A_NAME_MAP.put(SystemSettings.Wireless.WIFI_TO_MOBILE_DATA_AWAKE_TIMEOUT, "wifi_mobile_data_transition_wakelock_timeout_ms");
        Z2A_NAME_MAP.put(SystemSettings.Wireless.WIFI_STATUS, "wifi_on");
        Z2A_NAME_MAP.put(SystemSettings.Wireless.WIFI_WATCHDOG_STATUS, "wifi_watchdog_on");
        Z2A_NAME_MAP.put(SystemSettings.Display.WINDOW_ANIMATION_SCALE, SystemSettings.Display.WINDOW_ANIMATION_SCALE);
        Z2A_NAME_MAP.put(SystemSettings.Display.DISPLAY_INVERSION_STATUS, "accessibility_display_inversion_enabled");
        Z2A_NAME_MAP.put(SystemSettings.General.ACCESSIBILITY_STATUS, "accessibility_enabled");
        Z2A_NAME_MAP.put(SystemSettings.General.GEOLOCATION_ORIGINS_ALLOWED, "allowed_geolocation_origins");
        Z2A_NAME_MAP.put(SystemSettings.Input.DEFAULT_INPUT_METHOD, SystemSettings.Input.DEFAULT_INPUT_METHOD);
        Z2A_NAME_MAP.put(SystemSettings.General.ACTIVATED_ACCESSIBILITY_SERVICES, "enabled_accessibility_services");
        Z2A_NAME_MAP.put(SystemSettings.Input.ACTIVATED_INPUT_METHODS, "enabled_input_methods");
        Z2A_NAME_MAP.put(SystemSettings.Input.SELECTOR_VISIBILITY_FOR_INPUT_METHOD, "input_method_selector_visibility");
        Z2A_NAME_MAP.put(SystemSettings.Phone.RTT_CALLING_STATUS, "rtt_calling_mode");
        Z2A_NAME_MAP.put(SystemSettings.Input.ACTIVATED_INPUT_METHOD_SUBMODE, "selected_input_method_subtype");
        Z2A_NAME_MAP.put(SystemSettings.General.SKIP_USE_HINTS, "skip_first_use_hints");
        Z2A_NAME_MAP.put(SystemSettings.General.TOUCH_EXPLORATION_STATUS, "touch_exploration_enabled");
        Z2A_NAME_MAP.put(SystemSettings.TTS.DEFAULT_TTS_PITCH, "tts_default_pitch");
        Z2A_NAME_MAP.put(SystemSettings.TTS.DEFAULT_TTS_RATE, "tts_default_rate");
        Z2A_NAME_MAP.put(SystemSettings.TTS.DEFAULT_TTS_SYNTH, "tts_default_synth");
        Z2A_NAME_MAP.put(SystemSettings.TTS.ENABLED_TTS_PLUGINS, "tts_enabled_plugins");
        for (Map.Entry<String, String> entry : Z2A_NAME_MAP.entrySet()) {
            A2Z_NAME_MAP.put(entry.getValue(), entry.getKey());
        }
    }

    private SystemSettingsUtils() {
    }

    public static ContentResolver creatFromDataAbilityHelper(DataAbilityHelper dataAbilityHelper) {
        Object hostContext = dataAbilityHelper.getContext().getHostContext();
        Context context = hostContext instanceof Context ? (Context) hostContext : null;
        if (context == null) {
            return null;
        }
        return context.getContentResolver();
    }

    public static String getMappedName(String str) {
        if (Z2A_NAME_MAP.containsKey(str)) {
            return Z2A_NAME_MAP.get(str);
        }
        return A2Z_NAME_MAP.containsKey(str) ? A2Z_NAME_MAP.get(str) : str;
    }

    public static String getSettingsSource(String str) {
        return Z_NAME_SOURCE.getOrDefault(str, "");
    }
}
