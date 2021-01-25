package ohos.sysappcomponents.settings;

import ohos.aafwk.ability.DataAbilityHelper;
import ohos.utils.net.Uri;

public class SystemSettings {
    private SystemSettings() {
    }

    public static Uri getUri(String str) {
        return SystemSettingsProxy.getUri(str);
    }

    public static String getValue(DataAbilityHelper dataAbilityHelper, String str) {
        return SystemSettingsProxy.getValue(dataAbilityHelper, str);
    }

    public static boolean setValue(DataAbilityHelper dataAbilityHelper, String str, String str2) {
        return SystemSettingsProxy.setValue(dataAbilityHelper, str, str2);
    }

    public static class TTS {
        public static final String DEFAULT_TTS_PITCH = "default_tts_pitch";
        public static final String DEFAULT_TTS_RATE = "default_tts_rate";
        public static final String DEFAULT_TTS_SYNTH = "default_tts_synth";
        public static final String ENABLED_TTS_PLUGINS = "enabled_tts_plugins";

        private TTS() {
        }
    }

    public static class Wireless {
        public static final String AIRPLANE_MODE_RADIOS = "airplane_mode_radios";
        public static final String BLUETOOTH_DISCOVERABILITY_STATUS = "bluetooth_discoverability_status";
        public static final String BLUETOOTH_DISCOVER_TIMEOUT = "bluetooth_discover_time";
        public static final String BLUETOOTH_RADIO = "bluetooth_radio";
        public static final String BLUETOOTH_STATUS = "bluetooth_status";
        public static final String CELL_RADIO = "cell_radio";
        public static final String NFC_RADIO = "nfc_radio";
        public static final String OWNER_LOCKDOWN_WIFI_CFG = "owner_lockdown_wifi_cfg";
        public static final String WIFI_DHCP_MAX_RETRY_COUNT = "wifi_dhcp_max_retry_count";
        public static final String WIFI_RADIO = "wifi_radio";
        public static final String WIFI_STATUS = "wifi_status";
        public static final String WIFI_TO_MOBILE_DATA_AWAKE_TIMEOUT = "wifi_to_mobile_data_awake_timeout";
        public static final String WIFI_WATCHDOG_STATUS = "wifi_watchdog_status";

        private Wireless() {
        }
    }

    public static class Network {
        public static final String DATA_ROAMING_STATUS = "data_roaming_status";
        public static final String HTTP_PROXY_CFG = "http_proxy_cfg";
        public static final String NETWORK_PREFERENCE_USAGE = "network_preference_usage";

        private Network() {
        }
    }

    public static class Input {
        public static final String ACTIVATED_INPUT_METHODS = "activated_input_methods";
        public static final String ACTIVATED_INPUT_METHOD_SUBMODE = "activated_input_method_submode";
        public static final String AUTO_CAPS_TEXT_INPUT = "auto_caps_text_input";
        public static final String AUTO_PUNCTUATE_TEXT_INPUT = "auto_punctuate_text_input";
        public static final String AUTO_REPLACE_TEXT_INPUT = "auto_replace_text_input";
        public static final String DEFAULT_INPUT_METHOD = "default_input_method";
        public static final String SELECTOR_VISIBILITY_FOR_INPUT_METHOD = "selector_visibility_for_input_method";
        public static final String SHOW_PASSWORD_TEXT_INPUT = "show_passward_text_input";

        private Input() {
        }
    }

    public static class Sound {
        public static final String AFFECTED_MODE_RINGER_STREAMS = "affected_mode_ringer_streams";
        public static final String AFFECTED_MUTE_STREAMS = "affected_mute_streams";
        public static final String DEFAULT_ALARM_ALERT = "default_alarm_alert";
        public static final String DEFAULT_NOTIFICATION_SOUND = "default_notification_sound";
        public static final String DEFAULT_RINGTONE = "default_ringtone";
        public static final String DTMF_TONE_TYPE_WHILE_DIALING = "dtmf_tone_type_while_dialing";
        public static final String DTMF_TONE_WHILE_DIALING = "dtmf_tone_while_dialing";
        public static final String HAPTIC_FEEDBACK_STATUS = "haptic_feedback_status";
        public static final String SOUND_EFFECTS_STATUS = "sound_effects_status";
        public static final String VIBRATE_STATUS = "vibrate_status";
        public static final String VIBRATE_WHILE_RINGING = "vibrate_while_ringing";

        private Sound() {
        }
    }

    public static class Display {
        public static final String ANIMATOR_DURATION_SCALE = "animator_duration_scale";
        public static final String AUTO_SCREEN_BRIGHTNESS = "auto_screen_brightness";
        public static final int AUTO_SCREEN_BRIGHTNESS_MODE = 1;
        public static final String DEFAULT_SCREEN_ROTATION = "default_screen_rotation";
        public static final String DISPLAY_INVERSION_STATUS = "display_inversion_status";
        public static final String FONT_SCALE = "font_scale";
        public static final int MANUAL_SCREEN_BRIGHTNESS_MODE = 0;
        public static final String SCREEN_BRIGHTNESS_STATUS = "screen_brightness_status";
        public static final String SCREEN_OFF_TIMEOUT = "screen_off_timeout";
        public static final String TRANSITION_ANIMATION_SCALE = "transition_animation_scale";
        public static final String WINDOW_ANIMATION_SCALE = "window_animation_scale";

        private Display() {
        }
    }

    public static class Date {
        public static final String AUTO_GAIN_TIME = "auto_gain_time";
        public static final String AUTO_GAIN_TIME_ZONE = "auto_gain_time_zone";
        public static final String DATE_FORMAT = "date_format";
        public static final String TIME_FORMAT = "time_format";

        private Date() {
        }
    }

    public static class Phone {
        public static final String RTT_CALLING_STATUS = "rtt_calling_status";

        private Phone() {
        }
    }

    public static class General {
        public static final String ACCELEROMETER_ROTATION_STATUS = "accelerometer_rotation_status";
        public static final String ACCESSIBILITY_STATUS = "accessibility_status";
        public static final String ACTIVATED_ACCESSIBILITY_SERVICES = "activated_accessibility_services";
        public static final String AIRPLANE_MODE = "airplane_status";
        public static final String BOOT_COUNTING = "boot_counting";
        public static final String CONTACT_METADATA_SYNC_STATUS = "contact_metadata_sync_status";
        public static final String DEBUGGER_WAITING = "debugger_waiting";
        public static final String DEBUG_APP_PACKAGE = "debug_app_package";
        public static final String DEVELOPMENT_SETTINGS_STATUS = "development_settings_status";
        public static final String DEVICE_NAME = "device_name";
        public static final String DEVICE_PROVISION_STATUS = "device_provision_status";
        public static final String END_BUTTON_ACTION = "end_button_action";
        public static final String GEOLOCATION_ORIGINS_ALLOWED = "geolocation_origins_allowed";
        public static final String HDC_STATUS = "hdc_status";
        public static final String SETUP_WIZARD_FINISHED = "setup_wizard_finished";
        public static final String SKIP_USE_HINTS = "skip_use_hints";
        public static final String TOUCH_EXPLORATION_STATUS = "touch_exploration_status";
        public static final String USB_STORAGE_STATUS = "usb_storage_status";

        private General() {
        }
    }
}
