package huawei.android.provider;

import android.app.ActivityThread;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.hdm.HwDeviceManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ICancellationSignal;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.SettingsEx;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;
import java.util.HashMap;

public final class HwSettings {
    public static final String AUTHORITY = "settings";
    private static final boolean IS_LOCAL_LOGV = false;
    public static final String MDM_POLICY_INVALID_VALUE = "mdm_policy_invalid_value";
    private static final String MDM_POLICY_NAME = "mdm_policy_name";
    private static final String[] MDM_POLICY_RELATED_SET_ITEMS = {"location_mode", "screen_off_timeout", "location_providers_allowed"};
    private static final String POLICY_FORBIDDEN_LOCATION_MODE = "settings_policy_forbidden_location_mode";
    private static final String POLICY_FORBIDDEN_LOCATION_SERVICE = "settings_policy_forbidden_location_service";
    public static final String QUERY_ARG_SELECTION = "android:query-selection";
    public static final String QUERY_ARG_SELECTION_ARGS = "android:query-selection-args";
    public static final String QUERY_ARG_SORT_ORDER = "android:query-sort-order";
    private static final String RECEIVER_ACTION_POLICY_TOAST_SHOW = "com.android.settings.mdm.receiver.action.MDMPolicyToastShow";
    private static final String RECEVIER_PKG = "com.android.settings";
    private static final String TAG = "Settings";

    public static final class Global {
        public static final String SINGLE_HAND_MODE = "single_hand_mode";
        public static final String WIFI_REPEATER_ON = "wifi_repeater_on";
    }

    public static final class Secure {
        public static final String KEY_SECURE_GESTURE_NAVIGATION = "secure_gesture_navigation";
        public static final String WIFI_AP_IGNOREBROADCASTSSID = "wifi_ap_ignorebroadcastssid";
    }

    public static final class System {
        public static final String AI_ENTRANCE = "ai_enable";
        public static final int AI_ENTRANCE_DISABLE = 0;
        public static final int AI_ENTRANCE_ENABLE = 1;
        public static final String AUTO_CONNECT_ATT = "auto_connect_att";
        public static final String AUTO_HIDE_NAVIGATIONBAR = "auto_hide_navigationbar_enable";
        public static final int AUTO_HIDE_NAVIGATIONBAR_DEFAULT = 0;
        public static final String AUTO_HIDE_NAVIGATIONBAR_TIMEOUT = "auto_hide_navigationbar_timeout";
        public static final int AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT = 2000;
        public static final String EASYFINGER_LETTER_SETTING_PREFIX = "persist.sys.fingerjoint.";
        public static final String EASYWAKE_ENABLE_FLAG = "persist.sys.easyflag";
        public static final int EASYWAKE_ENABLE_FLAG_MASK = 8192;
        public static final String EASYWAKE_LETTER_SETTING_PREFIX = "persist.sys.easywakeup.";
        public static final String ENABLE_EXPAND_ON_HUAWEI_UNLOCK = "enable_expand_on_huawei_unlock";
        public static final int FINGERSENSE_DEFAULT_GLOW_COLOR = Color.parseColor("#9755dc");
        public static final int FINGERSENSE_DEFAULT_PIXIE_COLOR = Color.parseColor("#b2ebf2");
        public static final int FINGERSENSE_DEFAULT_STROKE_COLOR = Color.parseColor("#00bcd4");
        public static final String FINGERSENSE_DOUBLE_KNOCK_DISTANCE = "double_knock_distance";
        public static final String FINGERSENSE_DOUBLE_KNOCK_TIMEOUT = "double_knock_timeout";
        public static final String FINGERSENSE_DOUBLE_KNUCKLE_ENABLED = "fingersense_double_knuckle_enabled";
        public static final String FINGERSENSE_DOUBLE_KNUCKLE_GESTURE_KNOCK = "fingersense_knuckle_gesture_double_knock";
        public static final String FINGERSENSE_DOUBLE_KNUCKLE_GESTURE_KNOCK_SUFFIX = "double_knock";
        public static final String FINGERSENSE_ENABLED = "fingersense_enabled";
        public static final int FINGERSENSE_ENABLED_NO = 0;
        public static final int FINGERSENSE_ENABLED_YES = 1;
        public static final String FINGERSENSE_GLOW_COLOR = "fingersense_glow_color";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_C_SUFFIX = "c";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_E_SUFFIX = "e";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_KNOCK = "fingersense_knuckle_gesture_knock";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_KNOCK_SUFFIX = "knock";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_LINE_SUFFIX = "line";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_M_SUFFIX = "m";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_NONE = "Nothing";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_OFF = "0";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_PREFIX = "fingersense_knuckle_gesture_";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_REGION = "fingersense_knuckle_gesture_region";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_REGION_SUFFIX = "region";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_REJECT_SUFFIX = "reject";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_S_SUFFIX = "s";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_V_SUFFIX = "v";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_W_SUFFIX = "w";
        public static final String FINGERSENSE_KNUCKLE_GESTURE_Z_SUFFIX = "z";
        public static final String FINGERSENSE_LETTERS_ENABLED = "fingersense_letters_enabled";
        public static final String FINGERSENSE_LINE_GESTURE_ENABLED = "fingersense_multiwindow_enabled";
        public static final String FINGERSENSE_PIXIE_COLOR = "fingersense_pixie_color";
        public static final String FINGERSENSE_SCREENRECORD_ENABLED = "fingersense_screen_recording_enabled";
        public static final String FINGERSENSE_SMARTSHOT_ENABLED = "fingersense_smartshot_enabled";
        public static final String FINGERSENSE_STROKE_COLOR = "fingersense_stroke_color";
        public static final String FIRST_DAY_OF_WEEK = "first_day_of_week";
        public static final String HIDE_VIRTUAL_KEY = "hide_virtual_key";
        public static final String HUAWEI_FORCEMINNAVIGATIONBAR = "forceMinNavigationBar";
        public static final String HUAWEI_MINNAVIGATIONBAR = "minNavigationBar";
        public static final String HUAWEI_NAVIGATIONBAR_STATUSCHANGE = "com.huawei.navigationbar.statuschange";
        public static final Uri HUAWEI_RINGTONE2_URI = Settings.System.getUriFor(RINGTONE2);
        public static final String NAVIGATIONBAR_HEIGHT_MIN = "navigationbar_height_min";
        public static final int NAVIGATIONBAR_HEIGHT_MIN_DEFAULT = 0;
        public static final String NAVIGATIONBAR_IS_MIN = "navigationbar_is_min";
        public static final int NAVIGATIONBAR_IS_MIN_DEFAULT = 0;
        public static final String NAVIGATIONBAR_MIN_PROMPT = "navigationbar_min_prompt";
        public static final int NAVIGATIONBAR_MIN_PROMPT_DEFAULT = 0;
        public static final String NAVIGATIONBAR_WIDTH_MIN = "navigationbar_width_min";
        public static final int NAVIGATIONBAR_WIDTH_MIN_DEFAULT = 0;
        public static final String NAVIGATION_BAR_ENABLE = "enable_navbar";
        public static final int NAVI_BAR_DISABLE = 0;
        public static final int NAVI_BAR_ENABLE = 1;
        public static final String PROXIMITY_WND_STATUS = "proximity_wnd_status";
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
        public static final String SINGLE_VIRTUAL_NAVBAR = "ai_navigationbar";
        public static final int SINGLE_VIRTUAL_NAVBAR_DISABLE = 0;
        public static final int SINGLE_VIRTUAL_NAVBAR_ENABLE = 1;
        public static final String SUPPORT_SENSOR_FEATURE = "support_sensor_feature";
        public static final String VOLUME_FM = "volume_fm";
        public static final String WEEKEND = "weekend";
    }

    /* access modifiers changed from: private */
    public static class NameValueCache {
        private static final String NAME_EQ_PLACEHOLDER = "name=?";
        private static final String[] SELECT_VALUE = {"value"};
        private final String mCallCommand;
        private IContentProvider mContentProvider = null;
        private final Uri mUri;
        private final HashMap<String, String> mValues = new HashMap<>();
        private long mValuesVersion = 0;
        private final String mVersionSystemProperty;

        public NameValueCache(String versionSystemProperty, Uri uri, String callCommand) {
            this.mVersionSystemProperty = versionSystemProperty;
            this.mUri = uri;
            this.mCallCommand = callCommand;
        }

        /* JADX INFO: Multiple debug info for r3v3 android.database.Cursor: [D('e' android.os.RemoteException), D('cursor' android.database.Cursor)] */
        public String getString(ContentResolver cr, String name) {
            IContentProvider cp;
            long newValuesVersion = SystemProperties.getLong(this.mVersionSystemProperty, 0);
            synchronized (this) {
                if (this.mValuesVersion != newValuesVersion) {
                    this.mValues.clear();
                    this.mValuesVersion = newValuesVersion;
                }
                if (this.mValues.containsKey(name)) {
                    return this.mValues.get(name);
                }
            }
            synchronized (this) {
                cp = this.mContentProvider;
                if (cp == null) {
                    this.mContentProvider = cr.acquireProvider(this.mUri.getAuthority());
                    cp = this.mContentProvider;
                }
            }
            if (!(this.mCallCommand == null || cp == null)) {
                try {
                    Bundle bundle = cp.call(cr.getPackageName(), this.mCallCommand, name, (Bundle) null);
                    if (bundle != null) {
                        String value = bundle.getPairValue();
                        synchronized (this) {
                            this.mValues.put(name, value);
                        }
                        return value;
                    }
                } catch (RemoteException e) {
                    Log.e(HwSettings.TAG, "remote exception:" + e.getMessage());
                }
            }
            Cursor cursor = null;
            if (cp != null) {
                try {
                    cursor = cp.query(cr.getPackageName(), this.mUri, SELECT_VALUE, ContentResolver.createSqlQueryBundle(NAME_EQ_PLACEHOLDER, new String[]{name}, null), (ICancellationSignal) null);
                } catch (RemoteException e2) {
                    Log.w(HwSettings.TAG, "Can't get key " + name + " from " + this.mUri, e2);
                    if (0 != 0) {
                        cursor.close();
                    }
                    return null;
                } catch (Throwable th) {
                    if (0 != 0) {
                        cursor.close();
                    }
                    throw th;
                }
            }
            if (cursor == null) {
                Log.w(HwSettings.TAG, "Can't get key " + name + " from " + this.mUri);
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            String value2 = cursor.moveToNext() ? cursor.getString(0) : null;
            synchronized (this) {
                this.mValues.put(name, value2);
            }
            cursor.close();
            return value2;
        }
    }

    public static final class Systemex extends Settings.NameValueTable {
        public static final String ATTWIFI_HOTSPOT = "attwifi_hotspot";
        public static final Uri CONTENT_URI = SettingsEx.Systemex.CONTENT_URI;
        public static final String FAST_POWER_ON = "fast_power_on";
        public static final String NEED_RESTORE_PHONE_STATE = "need_restore_phone_state";
        public static final String SHOW_BROADCAST_SSID_CONFIG = "show_broadcast_ssid_config";
        public static final String SYS_PROP_SETTINGEX_VERSION = "sys.settings_system_version";
        public static final String USER_DATACALL_SUBSCRIPTION = "user_datacall_sub";
        public static final String USER_DEFAULT_SUBSCRIPTION = "user_default_sub";
        public static final String USER_SET_AIRPLANE = "user_set_airplane";
        private static volatile NameValueCache mNameValueCache = null;

        public static synchronized String getString(ContentResolver resolver, String name) {
            String string;
            synchronized (Systemex.class) {
                if (mNameValueCache == null) {
                    mNameValueCache = new NameValueCache(SYS_PROP_SETTINGEX_VERSION, CONTENT_URI, null);
                }
                string = mNameValueCache.getString(resolver, name);
            }
            return string;
        }

        public static boolean putString(ContentResolver resolver, String name, String value) {
            return putString(resolver, CONTENT_URI, name, value);
        }

        public static Uri getUriFor(String name) {
            return getUriFor(CONTENT_URI, name);
        }

        public static int getInt(ContentResolver cr, String name, int def) {
            String str = getString(cr, name);
            if (str == null) {
                return def;
            }
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return def;
            }
        }

        public static int getInt(ContentResolver cr, String name) throws Settings.SettingNotFoundException {
            try {
                return Integer.parseInt(getString(cr, name));
            } catch (NumberFormatException e) {
                throw new Settings.SettingNotFoundException(name);
            }
        }

        public static boolean putInt(ContentResolver cr, String name, int value) {
            return putString(cr, name, Integer.toString(value));
        }

        public static long getLong(ContentResolver cr, String name, long def) {
            String valString = getString(cr, name);
            if (valString == null) {
                return def;
            }
            try {
                return Long.parseLong(valString);
            } catch (NumberFormatException e) {
                return def;
            }
        }

        public static long getLong(ContentResolver cr, String name) throws Settings.SettingNotFoundException {
            try {
                return Long.parseLong(getString(cr, name));
            } catch (NumberFormatException e) {
                throw new Settings.SettingNotFoundException(name);
            }
        }

        public static boolean putLong(ContentResolver cr, String name, long value) {
            return putString(cr, name, Long.toString(value));
        }

        public static float getFloat(ContentResolver cr, String name, float def) {
            String str = getString(cr, name);
            if (str == null) {
                return def;
            }
            try {
                return Float.parseFloat(str);
            } catch (NumberFormatException e) {
                return def;
            }
        }

        public static float getFloat(ContentResolver cr, String name) throws Settings.SettingNotFoundException {
            String str = getString(cr, name);
            if (str == null) {
                return Float.parseFloat(StorageManagerExt.INVALID_KEY_DESC);
            }
            try {
                return Float.parseFloat(str);
            } catch (NumberFormatException e) {
                throw new Settings.SettingNotFoundException(name);
            }
        }

        public static boolean putFloat(ContentResolver cr, String name, float value) {
            return putString(cr, name, Float.toString(value));
        }
    }

    public static String adjustValueForMDMPolicy(ContentResolver cr, String name, String origValue) {
        if (cr == null || !isMonitorSettings(name)) {
            return origValue;
        }
        if ("location_providers_allowed".equals(name)) {
            return checkLocationPolicyForProviders(cr, name, origValue);
        }
        if ("location_mode".equals(name)) {
            return checkNetworkLocationPolicy(cr, name, origValue);
        }
        if ("screen_off_timeout".equals(name)) {
            return checkScreenOffPolicy(cr, name, origValue);
        }
        return origValue;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0081, code lost:
        r4 = true;
     */
    private static String checkLocationPolicyForProviders(ContentResolver cr, String name, String origValue) {
        if (origValue == null) {
            return origValue;
        }
        String[] providerUpdates = origValue.split(",");
        StringBuilder newString = new StringBuilder();
        boolean isInputError = false;
        int length = providerUpdates.length;
        char isToEnableGps = '0';
        char isToEnableNetwork = '0';
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            String provider = providerUpdates[i];
            if (!TextUtils.isEmpty(provider) && provider.length() > 1) {
                char prefix = provider.charAt(0);
                if (prefix != '+' && prefix != '-') {
                    isInputError = true;
                    break;
                }
                String providerTemp = provider.substring(1);
                if ("network".equals(providerTemp)) {
                    isToEnableNetwork = prefix;
                    if (isNetworkLocationDisabled() || isLocationServiceDisabled() || isLocationServiceModeDisabled()) {
                        prefix = '-';
                    }
                } else if ("gps".equals(providerTemp)) {
                    isToEnableGps = prefix;
                    if (isLocationServiceDisabled()) {
                        prefix = '-';
                    }
                    if (isLocationServiceModeDisabled()) {
                        prefix = '+';
                    }
                }
                newString.append(prefix);
                newString.append(providerTemp);
                newString.append(",");
                i++;
            } else {
                break;
            }
        }
        int newStringLen = newString.length();
        if (newStringLen < 1) {
            isInputError = true;
        } else {
            newString.setLength(newStringLen - 1);
        }
        if (isInputError) {
            return origValue;
        }
        if (isLocationServiceDisabled() && (isToEnableGps == '+' || isToEnableNetwork == '+')) {
            sendBroadcastByMDM(POLICY_FORBIDDEN_LOCATION_SERVICE);
        }
        if (isLocationServiceModeDisabled() && (isToEnableNetwork == '+' || isToEnableGps == '-')) {
            sendBroadcastByMDM(POLICY_FORBIDDEN_LOCATION_MODE);
        }
        return newString.toString();
    }

    private static String checkNetworkLocationPolicy(ContentResolver cr, String name, String origValue) {
        try {
            int origSetValue = Integer.parseInt(origValue);
            if (isNetworkLocationDisabled() && origSetValue != 0 && origSetValue != 1) {
                return Integer.toString(1);
            }
            if (isLocationServiceDisabled() && origSetValue != 0) {
                sendBroadcastByMDM(POLICY_FORBIDDEN_LOCATION_SERVICE);
                return Integer.toString(0);
            } else if (!isLocationServiceModeDisabled() || (origSetValue != 0 && origSetValue != 2)) {
                return origValue;
            } else {
                sendBroadcastByMDM(POLICY_FORBIDDEN_LOCATION_MODE);
                return Integer.toString(1);
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "HwSettings :: checkAndFixMDMPolicyValue() get invalid value: " + origValue);
            return origValue;
        }
    }

    private static void sendBroadcastByMDM(String policyName) {
        Context context = ActivityThread.currentActivityThread().getSystemContext();
        Intent intent = new Intent(RECEIVER_ACTION_POLICY_TOAST_SHOW);
        intent.putExtra(MDM_POLICY_NAME, policyName);
        intent.setPackage(RECEVIER_PKG);
        context.sendBroadcast(intent, "android.permission.WRITE_SECURE_SETTINGS");
    }

    private static boolean isNetworkLocationDisabled() {
        return HwDeviceManager.disallowOp(41);
    }

    private static boolean isLocationServiceDisabled() {
        return HwDeviceManager.disallowOp(39);
    }

    private static boolean isLocationServiceModeDisabled() {
        return HwDeviceManager.disallowOp(40);
    }

    private static String checkScreenOffPolicy(ContentResolver cr, String name, String origValue) {
        if (origValue.equals(Integer.toString(Integer.MAX_VALUE)) || !isScreenOffDisabled()) {
            return origValue;
        }
        return MDM_POLICY_INVALID_VALUE;
    }

    private static boolean isScreenOffDisabled() {
        return HwDeviceManager.disallowOp(36);
    }

    private static boolean isMonitorSettings(String name) {
        for (String item : MDM_POLICY_RELATED_SET_ITEMS) {
            if (item.equals(name)) {
                return true;
            }
        }
        return false;
    }
}
