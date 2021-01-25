package com.huawei.android.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Debug;
import android.provider.Settings;
import android.util.Slog;
import com.huawei.annotation.HwSystemApi;
import huawei.android.provider.HwSettings;

public final class SettingsEx {
    public static final String AUTHORITY = "settings";
    public static final String TAG = "SettingsEx";

    public static int getIntForUser(ContentResolver cr, String name, int def, int userHandle) {
        return Settings.System.getIntForUser(cr, name, def, userHandle);
    }

    public static final class System {
        public static final String AUTO_ANSWER_TIMEOUT = "auto_answer";
        public static final String AUTO_CONNECT_ATT = "auto_connect_att";
        public static final String AUTO_HIDE_NAVIGATIONBAR = "auto_hide_navigationbar_enable";
        public static final int AUTO_HIDE_NAVIGATIONBAR_DEFAULT = 0;
        public static final String AUTO_HIDE_NAVIGATIONBAR_TIMEOUT = "auto_hide_navigationbar_timeout";
        public static final int AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT = 2000;
        public static final String ENABLE_EXPAND_ON_HUAWEI_UNLOCK = "enable_expand_on_huawei_unlock";
        public static final String FIRST_DAY_OF_WEEK = "first_day_of_week";
        public static final String HIDE_VIRTUAL_KEY = "hide_virtual_key";
        public static final String HUAWEI_FORCEMINNAVIGATIONBAR = "forceMinNavigationBar";
        public static final String HUAWEI_MINNAVIGATIONBAR = "minNavigationBar";
        public static final String HUAWEI_NAVIGATIONBAR_STATUSCHANGE = "com.huawei.navigationbar.statuschange";
        public static final Uri HUAWEI_RINGTONE2_URI = HwSettings.System.HUAWEI_RINGTONE2_URI;
        @HwSystemApi
        public static final String KEY_CONTENT_HDB_ALLOWED = "hdb_enabled";
        public static final String MULTI_SIM_VOICE_CALL_SUBSCRIPTION = "multi_sim_voice_call";
        public static final String NAVIGATIONBAR_HEIGHT_MIN = "navigationbar_height_min";
        public static final int NAVIGATIONBAR_HEIGHT_MIN_DEFAULT = 0;
        public static final String NAVIGATIONBAR_IS_MIN = "navigationbar_is_min";
        public static final int NAVIGATIONBAR_IS_MIN_DEFAULT = 0;
        public static final String NAVIGATIONBAR_MIN_PROMPT = "navigationbar_min_prompt";
        public static final int NAVIGATIONBAR_MIN_PROMPT_DEFAULT = 0;
        public static final String NAVIGATIONBAR_WIDTH_MIN = "navigationbar_width_min";
        public static final int NAVIGATIONBAR_WIDTH_MIN_DEFAULT = 0;
        public static final String RINGTONE2 = "ringtone2";
        public static final String RTSP_MAX_PORT = "rtsp_max_udp_port";
        public static final String RTSP_MIN_PORT = "rtsp_min_udp_port";
        public static final String RTSP_PROXY_HOST = "rtsp_proxy_host";
        public static final String RTSP_PROXY_PORT = "rtsp_proxy_port";
        @HwSystemApi
        public static final String SCREEN_AUTO_BRIGHTNESS = "screen_auto_brightness";
        public static final String SHOW_HWLOCK_FIRST = "show_hwlock_first";
        public static final String SHOW_NAVIGATIONBAR_CHECKBOK = "show_navigationbar_checkbox";
        public static final int SHOW_NAVIGATIONBAR_CHECKBOK_DEFAULT = 0;
        public static final String SIMPLEUI_MODE = "simpleui_mode";
        public static final String SINGLE_HAND_MODE = "single_hand_mode";
        public static final int SINGLE_HAND_MODE_LEFT = 1;
        public static final int SINGLE_HAND_MODE_RIGHT = 2;
        public static final String SINGLE_HAND_SWITCH = "single_hand_switch";
        public static final int SINGLE_HAND_SWITCH_OFF = 0;
        public static final int SINGLE_HAND_SWITCH_ON = 1;
        public static final String VOLUME_FM = "volume_fm";
        public static final String WEEKEND = "weekend";

        public static String getScreenAutoBrightness() {
            return "screen_auto_brightness";
        }

        public static int getIntForUser(ContentResolver cr, String name, int def, int userHandle) {
            return Settings.System.getIntForUser(cr, name, def, userHandle);
        }

        public static boolean putIntForUser(ContentResolver cr, String name, int value, int userHandle) {
            return Settings.System.putIntForUser(cr, name, value, userHandle);
        }

        public static String getStringForUser(ContentResolver cr, String name, int userHandle) {
            return Settings.System.getStringForUser(cr, name, userHandle);
        }

        @HwSystemApi
        public static long getLongForUser(ContentResolver cr, String name, long def, int userHandle) {
            return Settings.System.getLongForUser(cr, name, def, userHandle);
        }

        @HwSystemApi
        public static boolean putLongForUser(ContentResolver cr, String name, long value, int userHandle) {
            return Settings.System.putLongForUser(cr, name, value, userHandle);
        }

        @HwSystemApi
        public static float getFloatForUser(ContentResolver cr, String name, float def, int userHandle) {
            return Settings.System.getFloatForUser(cr, name, def, userHandle);
        }
    }

    public static final class Systemex {
        @HwSystemApi
        public static final String ACCELEROMETER_ROTATION = "accelerometer_rotation";
        public static final String ATTWIFI_HOTSPOT = "attwifi_hotspot";
        public static final Uri CONTENT_URI = HwSettings.Systemex.CONTENT_URI;
        public static final String MULTI_SIM_DATA_CALL_SUBSCRIPTION = "multi_sim_data_call";
        @HwSystemApi
        public static final String SCREEN_OFF_TIMEOUT = "screen_off_timeout";
        public static final String SHOW_BROADCAST_SSID_CONFIG = "show_broadcast_ssid_config";
        @HwSystemApi
        public static final String SHOW_TOUCHES = "show_touches";
        public static final String SYS_PROP_SETTINGEX_VERSION = "sys.settings_system_version";
        @HwSystemApi
        public static final String USER_ROTATION = "user_rotation";

        public static synchronized String getString(ContentResolver resolver, String name) {
            String string;
            synchronized (Systemex.class) {
                Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.getString deprecated use Settings.System.getString instead ;callers=" + Debug.getCallers(3));
                string = Settings.System.getString(resolver, name);
            }
            return string;
        }

        public static boolean putString(ContentResolver resolver, String name, String value) {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.putString deprecated use Settings.System.putString instead ;callers=" + Debug.getCallers(3));
            return Settings.System.putString(resolver, name, value);
        }

        public static Uri getUriFor(String name) {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.getUriFor deprecated use Settings.System.getUriFor instead ;callers=" + Debug.getCallers(3));
            return Settings.System.getUriFor(name);
        }

        public static int getInt(ContentResolver cr, String name, int def) {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.getInt deprecated use Settings.System.getInt instead ;callers=" + Debug.getCallers(3));
            return Settings.System.getInt(cr, name, def);
        }

        public static int getInt(ContentResolver cr, String name) throws Settings.SettingNotFoundException {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.getInt deprecated use Settings.System.getInt instead ;callers=" + Debug.getCallers(3));
            return Settings.System.getInt(cr, name);
        }

        public static boolean putInt(ContentResolver cr, String name, int value) {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.putInt deprecated use Settings.System.putInt instead ;callers=" + Debug.getCallers(3));
            return Settings.System.putInt(cr, name, value);
        }

        public static long getLong(ContentResolver cr, String name, long def) {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.getLong deprecated use Settings.System.getLong instead ;callers=" + Debug.getCallers(3));
            return Settings.System.getLong(cr, name, def);
        }

        public static long getLong(ContentResolver cr, String name) throws Settings.SettingNotFoundException {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.getLong deprecated use Settings.System.getLong instead ;callers=" + Debug.getCallers(3));
            return Settings.System.getLong(cr, name);
        }

        public static boolean putLong(ContentResolver cr, String name, long value) {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.putLong deprecated use Settings.System.putLong instead ;callers=" + Debug.getCallers(3));
            return Settings.System.putLong(cr, name, value);
        }

        public static float getFloat(ContentResolver cr, String name, float def) {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.getFloat deprecated use Settings.System.getFloat instead ;callers=" + Debug.getCallers(3));
            return Settings.System.getFloat(cr, name, def);
        }

        public static float getFloat(ContentResolver cr, String name) throws Settings.SettingNotFoundException {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.getFloat deprecated use Settings.System.getFloat instead ;callers=" + Debug.getCallers(3));
            return Settings.System.getFloat(cr, name);
        }

        public static boolean putFloat(ContentResolver cr, String name, float value) {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.putFloat deprecated use Settings.System.putFloat instead ;callers=" + Debug.getCallers(3));
            return Settings.System.putFloat(cr, name, value);
        }

        public static int getIntForUser(ContentResolver cr, String name, int def, int userHandle) throws Settings.SettingNotFoundException {
            return Settings.System.getIntForUser(cr, name, def, userHandle);
        }

        public static boolean putIntForUser(ContentResolver cr, String name, int value, int userHandle) {
            return Settings.System.putIntForUser(cr, name, value, userHandle);
        }
    }

    public static final class Secure {
        public static final String ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED = "accessibility_display_magnification_enabled";
        @HwSystemApi
        public static final String ASSISTANT = "assistant";
        @HwSystemApi
        public static final String DISPLAY_DENSITY_FORCED = "display_density_forced";
        @HwSystemApi
        public static final String ENABLED_ACCESSIBILITY_SERVICES = "enabled_accessibility_services";
        public static final String LOCK_SCREEN_LOCK_AFTER_TIMEOUT = "lock_screen_lock_after_timeout";
        public static final String MANAGED_PROFILE_CONTACT_REMOTE_SEARCH = "managed_profile_contact_remote_search";
        public static final String PREFERRED_TTY_MODE = "preferred_tty_mode";
        @HwSystemApi
        public static final String SCREENSAVER_ENABLED = "screensaver_enabled";
        @HwSystemApi
        public static final String SHOW_IME_WITH_HARD_KEYBOARD = "show_ime_with_hard_keyboard";
        @HwSystemApi
        public static final String UI_NIGHT_MODE = "ui_night_mode";
        @HwSystemApi
        public static final String USER_SETUP_COMPLETE = "user_setup_complete";
        public static final String WIFI_AP_IGNOREBROADCASTSSID = "wifi_ap_ignorebroadcastssid";
        public static final String WIFI_P2P_ON = "wifi_p2p_on";

        public static int getIntForUser(ContentResolver cr, String name, int def, int userHandle) {
            return Settings.Secure.getIntForUser(cr, name, def, userHandle);
        }

        public static boolean putIntForUser(ContentResolver cr, String name, int value, int userHandle) {
            return Settings.Secure.putIntForUser(cr, name, value, userHandle);
        }

        @HwSystemApi
        public static boolean putInt(ContentResolver cr, String name, int value) {
            return Settings.Secure.putInt(cr, name, value);
        }

        @HwSystemApi
        public static int getInt(ContentResolver cr, String name, int def) {
            return Settings.Secure.getInt(cr, name, def);
        }

        public static String getStringForUser(ContentResolver cr, String name, int userHandle) {
            return Settings.Secure.getStringForUser(cr, name, userHandle);
        }

        public static boolean putStringForUser(ContentResolver cr, String name, String value, int userHandle) {
            return Settings.Secure.putStringForUser(cr, name, value, userHandle);
        }

        public static String getUserSetupComplete() {
            return USER_SETUP_COMPLETE;
        }

        @HwSystemApi
        public static int getIntForUser(ContentResolver cr, String name, int userHandle) throws Settings.SettingNotFoundException {
            return Settings.Secure.getIntForUser(cr, name, userHandle);
        }

        @HwSystemApi
        public static float getFloatForUser(ContentResolver cr, String name, float def, int userHandle) {
            return Settings.Secure.getFloatForUser(cr, name, def, userHandle);
        }

        @HwSystemApi
        public static boolean putFloatForUser(ContentResolver cr, String name, float value, int userHandle) {
            return Settings.Secure.putFloatForUser(cr, name, value, userHandle);
        }

        @HwSystemApi
        public static long getLongForUser(ContentResolver cr, String name, long def, int userHandle) {
            return Settings.Secure.getLongForUser(cr, name, def, userHandle);
        }

        @HwSystemApi
        public static boolean putLongForUser(ContentResolver cr, String name, long value, int userHandle) {
            return Settings.Secure.putLongForUser(cr, name, value, userHandle);
        }
    }

    public static final class Global {
        public static final String ENHANCED_4G_MODE_ENABLED = "volte_vt_enabled";
        public static final String MOBILE_DATA = "mobile_data";
        @HwSystemApi
        public static final String MOBILE_DATA_ALWAYS_ON = "mobile_data_always_on";
        public static final String MULTI_SIM_DATA_CALL_SUBSCRIPTION = "multi_sim_data_call";
        public static final String MULTI_SIM_SMS_SUBSCRIPTION = "multi_sim_sms";
        public static final String MULTI_SIM_VOICE_CALL_SUBSCRIPTION = "multi_sim_voice_call";
        public static final String NEW_CONTACT_AGGREGATOR = "new_contact_aggregator";
        public static final String PREFERRED_NETWORK_MODE = "preferred_network_mode";
        public static final String SINGLE_HAND_MODE = "single_hand_mode";
        public static final String SINGLE_HAND_OLD_GESTURE_DISABLE = "single_hand_old_gesture_disable";
        public static final String VT_IMS_ENABLED = "vt_ims_enabled";
        public static final String WFC_IMS_ENABLED = "wfc_ims_enabled";
        public static final String WIFI_SCAN_ALWAYS_AVAILABLE = "wifi_scan_always_enabled";

        public static int getZenModeOff() {
            return 0;
        }

        public static String getZen_Mode() {
            return "zen_mode";
        }
    }
}
