package android.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Debug;
import android.provider.Settings.SettingNotFoundException;
import android.util.Slog;

public final class SettingsEx {
    public static final String AUTHORITY = "settings";
    public static final String TAG = "SettingsEx";

    public static final class Global {
        public static final String DB_KEY_UNIFIED_DEVICE_NAME = "unified_device_name";
        public static final String DB_KEY_UNIFIED_DEVICE_NAME_UPDATED = "unified_device_name_updated";
    }

    public static final class Secure {
        public static final String WIFI_AP_IGNOREBROADCASTSSID = "wifi_ap_ignorebroadcastssid";
    }

    public static final class System {
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
        public static final String KEY_VIRTUAL_KEY_TYPE = "virtual_key_type";
        public static final String NAVIGATIONBAR_HEIGHT_MIN = "navigationbar_height_min";
        public static final int NAVIGATIONBAR_HEIGHT_MIN_DEFAULT = 0;
        public static final String NAVIGATIONBAR_IS_MIN = "navigationbar_is_min";
        public static final int NAVIGATIONBAR_IS_MIN_DEFAULT = 0;
        public static final String NAVIGATIONBAR_MIN_PROMPT = "navigationbar_min_prompt";
        public static final int NAVIGATIONBAR_MIN_PROMPT_DEFAULT = 0;
        public static final String NAVIGATIONBAR_WIDTH_MIN = "navigationbar_width_min";
        public static final int NAVIGATIONBAR_WIDTH_MIN_DEFAULT = 0;
        public static final String PRESS_LIMIT = "pressure_habit_threshold";
        public static final float PRESS_LIMIT_DEFAULT = 0.2f;
        public static final String PRESS_MODE = "virtual_notification_key_type";
        public static final int PRESS_MODE_DEFAULT = 0;
        public static final String RINGTONE2 = "ringtone2";
        public static final String RTSP_MAX_PORT = "rtsp_max_udp_port";
        public static final String RTSP_MIN_PORT = "rtsp_min_udp_port";
        public static final String RTSP_PROXY_HOST = "rtsp_proxy_host";
        public static final String RTSP_PROXY_PORT = "rtsp_proxy_port";
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
    }

    public static final class Systemex {
        public static final String ASSISTED_GPS_CONFIGURABLE_LIST = "assisted_gps_configurable_list";
        public static final String ASSISTED_GPS_MODE = "assisted_gps_mode";
        public static final String ASSISTED_GPS_NETWORK = "assisted_gps_network";
        public static final String ASSISTED_GPS_POSITION_MODE = "assisted_gps_position_mode";
        public static final String ASSISTED_GPS_RESET_TYPE = "assisted_gps_reset_type";
        public static final String ASSISTED_GPS_ROAMING_ENABLED = "assisted_gps_roaming_enabled";
        public static final String ASSISTED_GPS_SERVICE_IP = "assisted_gps_service_IP";
        public static final String ASSISTED_GPS_SERVICE_PORT = "assisted_gps_service_port";
        public static final String ASSISTED_GPS_SUPL_HOST = "assisted_gps_supl_host";
        public static final String ASSISTED_GPS_SUPL_PORT = "assisted_gps_supl_port";
        public static final String ATTWIFI_HOTSPOT = "attwifi_hotspot";
        public static final Uri CONTENT_URI = Uri.parse("content://settings/system");
        public static final String GPS_START_MODE = "gps_start_mode";
        public static final String NAVIGATIONBAR_IS_MIN = "navigationbar_is_min";
        public static final int NAVIGATIONBAR_IS_MIN_DEFAULT = 0;
        public static final String SHOW_BROADCAST_SSID_CONFIG = "show_broadcast_ssid_config";
        public static final String USER_SET_AIRPLANE = "user_set_airplane";
        public static final String VSIM_EANBLED_SUBID = "vsim_enabled_subid";
        public static final String VSIM_SAVED_COMMRIL_MODE = "vsim_saved_commril_mode";
        public static final String VSIM_SAVED_MAINSLOT = "vsim_saved_mainslot";
        public static final String VSIM_SAVED_NETWORK_MODE = "vsim_saved_network_mode";
        public static final String VSIM_ULONLY_MODE = "vsim_ulonly_mode";
        public static final String VSIM_USER_RESERVED_SUBID = "vsim_user_reserved_subid";

        public static String getString(ContentResolver resolver, String name) {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.getString deprecated use Settings.System.getString instead ;callers=" + Debug.getCallers(3));
            return android.provider.Settings.System.getString(resolver, name);
        }

        public static boolean putString(ContentResolver cr, String name, String value) {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.putString deprecated use Settings.System.putString instead ;callers=" + Debug.getCallers(3));
            return android.provider.Settings.System.putString(cr, name, value);
        }

        public static int getInt(ContentResolver cr, String name, int def) {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.getInt deprecated use Settings.System.getInt instead ;callers=" + Debug.getCallers(3));
            return android.provider.Settings.System.getInt(cr, name, def);
        }

        public static int getInt(ContentResolver cr, String name) throws SettingNotFoundException {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.getInt deprecated use Settings.System.getInt instead ;callers=" + Debug.getCallers(3));
            return android.provider.Settings.System.getInt(cr, name);
        }

        public static boolean putInt(ContentResolver cr, String name, int value) {
            return putString(cr, name, Integer.toString(value));
        }

        public static long getLong(ContentResolver cr, String name, long def) {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.getLong deprecated use Settings.System.getLong instead ;callers=" + Debug.getCallers(3));
            return android.provider.Settings.System.getLong(cr, name, def);
        }

        public static long getLong(ContentResolver cr, String name) throws SettingNotFoundException {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.getLong deprecated use Settings.System.getLong instead ;callers=" + Debug.getCallers(3));
            return android.provider.Settings.System.getLong(cr, name);
        }

        public static boolean putLong(ContentResolver cr, String name, long value) {
            return putString(cr, name, Long.toString(value));
        }

        public static float getFloat(ContentResolver cr, String name, float def) {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.getFloat deprecated use Settings.System.getFloat instead ;callers=" + Debug.getCallers(3));
            return android.provider.Settings.System.getFloat(cr, name, def);
        }

        public static float getFloat(ContentResolver cr, String name) throws SettingNotFoundException {
            Slog.w(SettingsEx.TAG, "SettingsEx.Systemex.getFloat deprecated use Settings.System.getFloat instead ;callers=" + Debug.getCallers(3));
            return android.provider.Settings.System.getFloat(cr, name);
        }

        public static boolean putFloat(ContentResolver cr, String name, float value) {
            return putString(cr, name, Float.toString(value));
        }
    }
}
