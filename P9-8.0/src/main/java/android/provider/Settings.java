package android.provider;

import android.app.ActivityThread;
import android.app.AppOpsManager;
import android.app.Application;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AndroidException;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.LogException;
import android.util.MemoryIntArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.telephony.cdma.HwCustPlusAndIddNddConvertUtils;
import com.android.internal.util.ArrayUtils;
import com.android.internal.widget.ILockSettings;
import com.android.internal.widget.ILockSettings.Stub;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class Settings {
    public static final String ACTION_ACCESSIBILITY_SETTINGS = "android.settings.ACCESSIBILITY_SETTINGS";
    public static final String ACTION_ADD_ACCOUNT = "android.settings.ADD_ACCOUNT_SETTINGS";
    public static final String ACTION_AIRPLANE_MODE_SETTINGS = "android.settings.AIRPLANE_MODE_SETTINGS";
    public static final String ACTION_APN_SETTINGS = "android.settings.APN_SETTINGS";
    public static final String ACTION_APPLICATION_DETAILS_SETTINGS = "android.settings.APPLICATION_DETAILS_SETTINGS";
    public static final String ACTION_APPLICATION_DEVELOPMENT_SETTINGS = "android.settings.APPLICATION_DEVELOPMENT_SETTINGS";
    public static final String ACTION_APPLICATION_SETTINGS = "android.settings.APPLICATION_SETTINGS";
    public static final String ACTION_APP_NOTIFICATION_REDACTION = "android.settings.ACTION_APP_NOTIFICATION_REDACTION";
    public static final String ACTION_APP_NOTIFICATION_SETTINGS = "android.settings.APP_NOTIFICATION_SETTINGS";
    public static final String ACTION_APP_OPS_SETTINGS = "android.settings.APP_OPS_SETTINGS";
    public static final String ACTION_BATTERY_SAVER_SETTINGS = "android.settings.BATTERY_SAVER_SETTINGS";
    public static final String ACTION_BLUETOOTH_SETTINGS = "android.settings.BLUETOOTH_SETTINGS";
    public static final String ACTION_CAPTIONING_SETTINGS = "android.settings.CAPTIONING_SETTINGS";
    public static final String ACTION_CAST_SETTINGS = "android.settings.CAST_SETTINGS";
    public static final String ACTION_CHANNEL_NOTIFICATION_SETTINGS = "android.settings.CHANNEL_NOTIFICATION_SETTINGS";
    public static final String ACTION_CONDITION_PROVIDER_SETTINGS = "android.settings.ACTION_CONDITION_PROVIDER_SETTINGS";
    public static final String ACTION_DATA_ROAMING_SETTINGS = "android.settings.DATA_ROAMING_SETTINGS";
    public static final String ACTION_DATE_SETTINGS = "android.settings.DATE_SETTINGS";
    public static final String ACTION_DEVICE_INFO_SETTINGS = "android.settings.DEVICE_INFO_SETTINGS";
    public static final String ACTION_DISPLAY_SETTINGS = "android.settings.DISPLAY_SETTINGS";
    public static final String ACTION_DREAM_SETTINGS = "android.settings.DREAM_SETTINGS";
    public static final String ACTION_ENTERPRISE_PRIVACY_SETTINGS = "android.settings.ENTERPRISE_PRIVACY_SETTINGS";
    public static final String ACTION_FOREGROUND_SERVICES_SETTINGS = "android.settings.FOREGROUND_SERVICES_SETTINGS";
    public static final String ACTION_HARD_KEYBOARD_SETTINGS = "android.settings.HARD_KEYBOARD_SETTINGS";
    public static final String ACTION_HOME_SETTINGS = "android.settings.HOME_SETTINGS";
    public static final String ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS = "android.settings.IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS";
    public static final String ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS = "android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS";
    public static final String ACTION_INPUT_METHOD_SETTINGS = "android.settings.INPUT_METHOD_SETTINGS";
    public static final String ACTION_INPUT_METHOD_SUBTYPE_SETTINGS = "android.settings.INPUT_METHOD_SUBTYPE_SETTINGS";
    public static final String ACTION_INTERNAL_STORAGE_SETTINGS = "android.settings.INTERNAL_STORAGE_SETTINGS";
    public static final String ACTION_LOCALE_SETTINGS = "android.settings.LOCALE_SETTINGS";
    public static final String ACTION_LOCATION_SOURCE_SETTINGS = "android.settings.LOCATION_SOURCE_SETTINGS";
    public static final String ACTION_MANAGED_PROFILE_SETTINGS = "android.settings.MANAGED_PROFILE_SETTINGS";
    public static final String ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS = "android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS";
    public static final String ACTION_MANAGE_APPLICATIONS_SETTINGS = "android.settings.MANAGE_APPLICATIONS_SETTINGS";
    public static final String ACTION_MANAGE_DEFAULT_APPS_SETTINGS = "android.settings.MANAGE_DEFAULT_APPS_SETTINGS";
    public static final String ACTION_MANAGE_OVERLAY_PERMISSION = "android.settings.action.MANAGE_OVERLAY_PERMISSION";
    public static final String ACTION_MANAGE_UNKNOWN_APP_SOURCES = "android.settings.MANAGE_UNKNOWN_APP_SOURCES";
    public static final String ACTION_MANAGE_WRITE_SETTINGS = "android.settings.action.MANAGE_WRITE_SETTINGS";
    public static final String ACTION_MEMORY_CARD_SETTINGS = "android.settings.MEMORY_CARD_SETTINGS";
    public static final String ACTION_MONITORING_CERT_INFO = "com.android.settings.MONITORING_CERT_INFO";
    public static final String ACTION_NETWORK_OPERATOR_SETTINGS = "android.settings.NETWORK_OPERATOR_SETTINGS";
    public static final String ACTION_NFCSHARING_SETTINGS = "android.settings.NFCSHARING_SETTINGS";
    public static final String ACTION_NFC_PAYMENT_SETTINGS = "android.settings.NFC_PAYMENT_SETTINGS";
    public static final String ACTION_NFC_SETTINGS = "android.settings.NFC_SETTINGS";
    public static final String ACTION_NIGHT_DISPLAY_SETTINGS = "android.settings.NIGHT_DISPLAY_SETTINGS";
    public static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    public static final String ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS = "android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS";
    public static final String ACTION_NOTIFICATION_SETTINGS = "android.settings.NOTIFICATION_SETTINGS";
    public static final String ACTION_PAIRING_SETTINGS = "android.settings.PAIRING_SETTINGS";
    public static final String ACTION_PICTURE_IN_PICTURE_SETTINGS = "android.settings.PICTURE_IN_PICTURE_SETTINGS";
    public static final String ACTION_PRINT_SETTINGS = "android.settings.ACTION_PRINT_SETTINGS";
    public static final String ACTION_PRIVACY_SETTINGS = "android.settings.PRIVACY_SETTINGS";
    public static final String ACTION_QUICK_LAUNCH_SETTINGS = "android.settings.QUICK_LAUNCH_SETTINGS";
    public static final String ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = "android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS";
    public static final String ACTION_REQUEST_SET_AUTOFILL_SERVICE = "android.settings.REQUEST_SET_AUTOFILL_SERVICE";
    public static final String ACTION_SEARCH_SETTINGS = "android.search.action.SEARCH_SETTINGS";
    public static final String ACTION_SECURITY_SETTINGS = "android.settings.SECURITY_SETTINGS";
    public static final String ACTION_SETTINGS = "android.settings.SETTINGS";
    public static final String ACTION_SHOW_ADMIN_SUPPORT_DETAILS = "android.settings.SHOW_ADMIN_SUPPORT_DETAILS";
    public static final String ACTION_SHOW_INPUT_METHOD_PICKER = "android.settings.SHOW_INPUT_METHOD_PICKER";
    public static final String ACTION_SHOW_REGULATORY_INFO = "android.settings.SHOW_REGULATORY_INFO";
    public static final String ACTION_SHOW_REMOTE_BUGREPORT_DIALOG = "android.settings.SHOW_REMOTE_BUGREPORT_DIALOG";
    public static final String ACTION_SOUND_SETTINGS = "android.settings.SOUND_SETTINGS";
    public static final String ACTION_STORAGE_MANAGER_SETTINGS = "android.settings.STORAGE_MANAGER_SETTINGS";
    public static final String ACTION_SYNC_SETTINGS = "android.settings.SYNC_SETTINGS";
    public static final String ACTION_SYSTEM_UPDATE_SETTINGS = "android.settings.SYSTEM_UPDATE_SETTINGS";
    public static final String ACTION_TETHER_PROVISIONING = "android.settings.TETHER_PROVISIONING_UI";
    public static final String ACTION_TRUSTED_CREDENTIALS_USER = "com.android.settings.TRUSTED_CREDENTIALS_USER";
    public static final String ACTION_USAGE_ACCESS_SETTINGS = "android.settings.USAGE_ACCESS_SETTINGS";
    public static final String ACTION_USER_DICTIONARY_INSERT = "com.android.settings.USER_DICTIONARY_INSERT";
    public static final String ACTION_USER_DICTIONARY_SETTINGS = "android.settings.USER_DICTIONARY_SETTINGS";
    public static final String ACTION_USER_SETTINGS = "android.settings.USER_SETTINGS";
    public static final String ACTION_VOICE_CONTROL_AIRPLANE_MODE = "android.settings.VOICE_CONTROL_AIRPLANE_MODE";
    public static final String ACTION_VOICE_CONTROL_BATTERY_SAVER_MODE = "android.settings.VOICE_CONTROL_BATTERY_SAVER_MODE";
    public static final String ACTION_VOICE_CONTROL_DO_NOT_DISTURB_MODE = "android.settings.VOICE_CONTROL_DO_NOT_DISTURB_MODE";
    public static final String ACTION_VOICE_INPUT_SETTINGS = "android.settings.VOICE_INPUT_SETTINGS";
    public static final String ACTION_VPN_SETTINGS = "android.settings.VPN_SETTINGS";
    public static final String ACTION_VR_LISTENER_SETTINGS = "android.settings.VR_LISTENER_SETTINGS";
    public static final String ACTION_WEBVIEW_SETTINGS = "android.settings.WEBVIEW_SETTINGS";
    public static final String ACTION_WIFI_IP_SETTINGS = "android.settings.WIFI_IP_SETTINGS";
    public static final String ACTION_WIFI_SETTINGS = "android.settings.WIFI_SETTINGS";
    public static final String ACTION_WIRELESS_SETTINGS = "android.settings.WIRELESS_SETTINGS";
    public static final String ACTION_ZEN_MODE_AUTOMATION_SETTINGS = "android.settings.ZEN_MODE_AUTOMATION_SETTINGS";
    public static final String ACTION_ZEN_MODE_EVENT_RULE_SETTINGS = "android.settings.ZEN_MODE_EVENT_RULE_SETTINGS";
    public static final String ACTION_ZEN_MODE_EXTERNAL_RULE_SETTINGS = "android.settings.ZEN_MODE_EXTERNAL_RULE_SETTINGS";
    public static final String ACTION_ZEN_MODE_PRIORITY_SETTINGS = "android.settings.ZEN_MODE_PRIORITY_SETTINGS";
    public static final String ACTION_ZEN_MODE_SCHEDULE_RULE_SETTINGS = "android.settings.ZEN_MODE_SCHEDULE_RULE_SETTINGS";
    public static final String ACTION_ZEN_MODE_SETTINGS = "android.settings.ZEN_MODE_SETTINGS";
    public static final String AUTHORITY = "settings";
    public static final String CALL_METHOD_GENERATION_INDEX_KEY = "_generation_index";
    public static final String CALL_METHOD_GENERATION_KEY = "_generation";
    public static final String CALL_METHOD_GET_GLOBAL = "GET_global";
    public static final String CALL_METHOD_GET_SECURE = "GET_secure";
    public static final String CALL_METHOD_GET_SYSTEM = "GET_system";
    public static final String CALL_METHOD_MAKE_DEFAULT_KEY = "_make_default";
    public static final String CALL_METHOD_PUT_GLOBAL = "PUT_global";
    public static final String CALL_METHOD_PUT_SECURE = "PUT_secure";
    public static final String CALL_METHOD_PUT_SYSTEM = "PUT_system";
    public static final String CALL_METHOD_RESET_GLOBAL = "RESET_global";
    public static final String CALL_METHOD_RESET_MODE_KEY = "_reset_mode";
    public static final String CALL_METHOD_RESET_SECURE = "RESET_secure";
    public static final String CALL_METHOD_TAG_KEY = "_tag";
    public static final String CALL_METHOD_TRACK_GENERATION_KEY = "_track_generation";
    public static final String CALL_METHOD_USER_KEY = "_user";
    public static final String DEVICE_NAME_SETTINGS = "android.settings.DEVICE_NAME";
    public static final String EXTRA_ACCOUNT_TYPES = "account_types";
    public static final String EXTRA_AIRPLANE_MODE_ENABLED = "airplane_mode_enabled";
    public static final String EXTRA_APP_PACKAGE = "android.provider.extra.APP_PACKAGE";
    public static final String EXTRA_APP_UID = "app_uid";
    public static final String EXTRA_AUTHORITIES = "authorities";
    public static final String EXTRA_BATTERY_SAVER_MODE_ENABLED = "android.settings.extra.battery_saver_mode_enabled";
    public static final String EXTRA_CHANNEL_ID = "android.provider.extra.CHANNEL_ID";
    public static final String EXTRA_DO_NOT_DISTURB_MODE_ENABLED = "android.settings.extra.do_not_disturb_mode_enabled";
    public static final String EXTRA_DO_NOT_DISTURB_MODE_MINUTES = "android.settings.extra.do_not_disturb_mode_minutes";
    public static final String EXTRA_INPUT_DEVICE_IDENTIFIER = "input_device_identifier";
    public static final String EXTRA_INPUT_METHOD_ID = "input_method_id";
    public static final String EXTRA_NUMBER_OF_CERTIFICATES = "android.settings.extra.number_of_certificates";
    public static final String INTENT_CATEGORY_USAGE_ACCESS_CONFIG = "android.intent.category.USAGE_ACCESS_CONFIG";
    private static final String JID_RESOURCE_PREFIX = "android";
    private static final boolean LOCAL_LOGV = false;
    public static final String METADATA_USAGE_ACCESS_REASON = "android.settings.metadata.USAGE_ACCESS_REASON";
    private static final String[] PM_CHANGE_NETWORK_STATE = new String[]{"android.permission.CHANGE_NETWORK_STATE", "android.permission.WRITE_SETTINGS"};
    private static final String[] PM_SYSTEM_ALERT_WINDOW = new String[]{"android.permission.SYSTEM_ALERT_WINDOW"};
    private static final String[] PM_WRITE_SETTINGS = new String[]{"android.permission.WRITE_SETTINGS"};
    public static final int RESET_MODE_PACKAGE_DEFAULTS = 1;
    public static final int RESET_MODE_TRUSTED_DEFAULTS = 4;
    public static final int RESET_MODE_UNTRUSTED_CHANGES = 3;
    public static final int RESET_MODE_UNTRUSTED_DEFAULTS = 2;
    private static final String TAG = "Settings";
    private static final Object mLocationSettingsLock = new Object();
    private static boolean sInSystemServer = false;
    private static final Object sInSystemServerLock = new Object();

    public static final class Bookmarks implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://settings/bookmarks");
        public static final String FOLDER = "folder";
        public static final String ID = "_id";
        public static final String INTENT = "intent";
        public static final String ORDERING = "ordering";
        public static final String SHORTCUT = "shortcut";
        private static final String TAG = "Bookmarks";
        public static final String TITLE = "title";
        private static final String[] sIntentProjection = new String[]{"intent"};
        private static final String[] sShortcutProjection = new String[]{"_id", "shortcut"};
        private static final String sShortcutSelection = "shortcut=?";

        public static Intent getIntentForShortcut(ContentResolver cr, char shortcut) {
            Intent intent = null;
            ContentResolver contentResolver = cr;
            Cursor c = contentResolver.query(CONTENT_URI, sIntentProjection, sShortcutSelection, new String[]{String.valueOf(shortcut)}, ORDERING);
            while (intent == null) {
                try {
                    if (!c.moveToNext()) {
                        break;
                    }
                    intent = Intent.parseUri(c.getString(c.getColumnIndexOrThrow("intent")), 0);
                } catch (URISyntaxException e) {
                } catch (IllegalArgumentException e2) {
                    Log.w(TAG, "Intent column not found", e2);
                } catch (Throwable th) {
                    if (c != null) {
                        c.close();
                    }
                }
            }
            if (c != null) {
                c.close();
            }
            return intent;
        }

        public static Uri add(ContentResolver cr, Intent intent, String title, String folder, char shortcut, int ordering) {
            if (shortcut != 0) {
                cr.delete(CONTENT_URI, sShortcutSelection, new String[]{String.valueOf(shortcut)});
            }
            ContentValues values = new ContentValues();
            if (title != null) {
                values.put("title", title);
            }
            if (folder != null) {
                values.put("folder", folder);
            }
            values.put("intent", intent.toUri(0));
            if (shortcut != 0) {
                values.put("shortcut", Integer.valueOf(shortcut));
            }
            values.put(ORDERING, Integer.valueOf(ordering));
            return cr.insert(CONTENT_URI, values);
        }

        public static CharSequence getLabelForFolder(Resources r, String folder) {
            return folder;
        }

        public static CharSequence getTitle(Context context, Cursor cursor) {
            int titleColumn = cursor.getColumnIndex("title");
            int intentColumn = cursor.getColumnIndex("intent");
            if (titleColumn == -1 || intentColumn == -1) {
                throw new IllegalArgumentException("The cursor must contain the TITLE and INTENT columns.");
            }
            String title = cursor.getString(titleColumn);
            if (!TextUtils.isEmpty(title)) {
                return title;
            }
            String intentUri = cursor.getString(intentColumn);
            if (TextUtils.isEmpty(intentUri)) {
                return LogException.NO_VALUE;
            }
            try {
                CharSequence loadLabel;
                Intent intent = Intent.parseUri(intentUri, 0);
                PackageManager packageManager = context.getPackageManager();
                ResolveInfo info = packageManager.resolveActivity(intent, 0);
                if (info != null) {
                    loadLabel = info.loadLabel(packageManager);
                } else {
                    loadLabel = LogException.NO_VALUE;
                }
                return loadLabel;
            } catch (URISyntaxException e) {
                return LogException.NO_VALUE;
            }
        }
    }

    private static final class ContentProviderHolder {
        @GuardedBy("mLock")
        private IContentProvider mContentProvider;
        private final Object mLock = new Object();
        @GuardedBy("mLock")
        private final Uri mUri;

        public ContentProviderHolder(Uri uri) {
            this.mUri = uri;
        }

        public IContentProvider getProvider(ContentResolver contentResolver) {
            IContentProvider iContentProvider;
            synchronized (this.mLock) {
                if (this.mContentProvider == null) {
                    this.mContentProvider = contentResolver.acquireProvider(this.mUri.getAuthority());
                }
                iContentProvider = this.mContentProvider;
            }
            return iContentProvider;
        }
    }

    private static final class GenerationTracker {
        private final MemoryIntArray mArray;
        private int mCurrentGeneration;
        private final Runnable mErrorHandler;
        private final int mIndex;

        public GenerationTracker(MemoryIntArray array, int index, int generation, Runnable errorHandler) {
            this.mArray = array;
            this.mIndex = index;
            this.mErrorHandler = errorHandler;
            this.mCurrentGeneration = generation;
        }

        public boolean isGenerationChanged() {
            int currentGeneration = readCurrentGeneration();
            if (currentGeneration >= 0) {
                if (currentGeneration == this.mCurrentGeneration) {
                    return false;
                }
                this.mCurrentGeneration = currentGeneration;
            }
            return true;
        }

        public int getCurrentGeneration() {
            return this.mCurrentGeneration;
        }

        private int readCurrentGeneration() {
            try {
                return this.mArray.get(this.mIndex);
            } catch (IOException e) {
                Log.e(Settings.TAG, "Error getting current generation", e);
                if (this.mErrorHandler != null) {
                    this.mErrorHandler.run();
                }
                return -1;
            }
        }

        public void destroy() {
            try {
                this.mArray.close();
            } catch (IOException e) {
                Log.e(Settings.TAG, "Error closing backing array", e);
                if (this.mErrorHandler != null) {
                    this.mErrorHandler.run();
                }
            }
        }
    }

    public static class NameValueTable implements BaseColumns {
        public static final String NAME = "name";
        public static final String VALUE = "value";

        protected static boolean putString(ContentResolver resolver, Uri uri, String name, String value) {
            try {
                ContentValues values = new ContentValues();
                values.put("name", name);
                values.put("value", value);
                resolver.insert(uri, values);
                return true;
            } catch (SQLException e) {
                Log.w(Settings.TAG, "Can't set key " + name + " in " + uri, e);
                return false;
            }
        }

        public static Uri getUriFor(Uri uri, String name) {
            return Uri.withAppendedPath(uri, name);
        }
    }

    public static final class Global extends NameValueTable {
        public static final String ACTIVITY_MANAGER_CONSTANTS = "activity_manager_constants";
        public static final String ADB_ENABLED = "adb_enabled";
        public static final String ADD_USERS_WHEN_LOCKED = "add_users_when_locked";
        public static final String AIRPLANE_MODE_ON = "airplane_mode_on";
        public static final String AIRPLANE_MODE_RADIOS = "airplane_mode_radios";
        public static final String AIRPLANE_MODE_TOGGLEABLE_RADIOS = "airplane_mode_toggleable_radios";
        public static final String ALARM_MANAGER_CONSTANTS = "alarm_manager_constants";
        public static final String ALLOW_USER_SWITCHING_WHEN_SYSTEM_USER_LOCKED = "allow_user_switching_when_system_user_locked";
        public static final String ALWAYS_FINISH_ACTIVITIES = "always_finish_activities";
        public static final String ANIMATOR_DURATION_SCALE = "animator_duration_scale";
        public static final String APN_DB_UPDATE_CONTENT_URL = "apn_db_content_url";
        public static final String APN_DB_UPDATE_METADATA_URL = "apn_db_metadata_url";
        public static final String APP_IDLE_CONSTANTS = "app_idle_constants";
        public static final String ASSISTED_GPS_ENABLED = "assisted_gps_enabled";
        public static final String AUDIO_SAFE_VOLUME_STATE = "audio_safe_volume_state";
        public static final String AUTO_TIME = "auto_time";
        public static final String AUTO_TIME_ZONE = "auto_time_zone";
        public static final String BATTERY_DISCHARGE_DURATION_THRESHOLD = "battery_discharge_duration_threshold";
        public static final String BATTERY_DISCHARGE_THRESHOLD = "battery_discharge_threshold";
        public static final String BATTERY_SAVER_CONSTANTS = "battery_saver_constants";
        public static final String BLE_SCAN_ALWAYS_AVAILABLE = "ble_scan_always_enabled";
        public static final String BLUETOOTH_A2DP_OPTIONAL_CODECS_ENABLED_PREFIX = "bluetooth_a2dp_optional_codecs_enabled_";
        public static final String BLUETOOTH_A2DP_SINK_PRIORITY_PREFIX = "bluetooth_a2dp_sink_priority_";
        public static final String BLUETOOTH_A2DP_SRC_PRIORITY_PREFIX = "bluetooth_a2dp_src_priority_";
        public static final String BLUETOOTH_A2DP_SUPPORTS_OPTIONAL_CODECS_PREFIX = "bluetooth_a2dp_supports_optional_codecs_";
        public static final String BLUETOOTH_DISABLED_PROFILES = "bluetooth_disabled_profiles";
        public static final String BLUETOOTH_HEADSET_PRIORITY_PREFIX = "bluetooth_headset_priority_";
        public static final String BLUETOOTH_INPUT_DEVICE_PRIORITY_PREFIX = "bluetooth_input_device_priority_";
        public static final String BLUETOOTH_INTEROPERABILITY_LIST = "bluetooth_interoperability_list";
        public static final String BLUETOOTH_MAP_CLIENT_PRIORITY_PREFIX = "bluetooth_map_client_priority_";
        public static final String BLUETOOTH_MAP_PRIORITY_PREFIX = "bluetooth_map_priority_";
        public static final String BLUETOOTH_ON = "bluetooth_on";
        public static final String BLUETOOTH_PAN_PRIORITY_PREFIX = "bluetooth_pan_priority_";
        public static final String BLUETOOTH_PBAP_CLIENT_PRIORITY_PREFIX = "bluetooth_pbap_client_priority_";
        public static final String BLUETOOTH_SAP_PRIORITY_PREFIX = "bluetooth_sap_priority_";
        public static final String BOOT_COUNT = "boot_count";
        public static final String BUGREPORT_IN_POWER_MENU = "bugreport_in_power_menu";
        public static final String CALL_AUTO_RETRY = "call_auto_retry";
        @Deprecated
        public static final String CAPTIVE_PORTAL_DETECTION_ENABLED = "captive_portal_detection_enabled";
        public static final String CAPTIVE_PORTAL_FALLBACK_URL = "captive_portal_fallback_url";
        public static final String CAPTIVE_PORTAL_HTTPS_URL = "captive_portal_https_url";
        public static final String CAPTIVE_PORTAL_HTTP_URL = "captive_portal_http_url";
        public static final String CAPTIVE_PORTAL_MODE = "captive_portal_mode";
        public static final int CAPTIVE_PORTAL_MODE_AVOID = 2;
        public static final int CAPTIVE_PORTAL_MODE_IGNORE = 0;
        public static final int CAPTIVE_PORTAL_MODE_PROMPT = 1;
        public static final String CAPTIVE_PORTAL_OTHER_FALLBACK_URLS = "captive_portal_other_fallback_urls";
        public static final String CAPTIVE_PORTAL_SERVER = "captive_portal_server";
        public static final String CAPTIVE_PORTAL_USER_AGENT = "captive_portal_user_agent";
        public static final String CAPTIVE_PORTAL_USE_HTTPS = "captive_portal_use_https";
        public static final String CARRIER_APP_WHITELIST = "carrier_app_whitelist";
        public static final String CAR_DOCK_SOUND = "car_dock_sound";
        public static final String CAR_UNDOCK_SOUND = "car_undock_sound";
        public static final String CDMA_CELL_BROADCAST_SMS = "cdma_cell_broadcast_sms";
        public static final String CDMA_ROAMING_MODE = "roaming_settings";
        public static final String CDMA_SUBSCRIPTION_MODE = "subscription_mode";
        public static final String CELL_ON = "cell_on";
        public static final String CERT_PIN_UPDATE_CONTENT_URL = "cert_pin_content_url";
        public static final String CERT_PIN_UPDATE_METADATA_URL = "cert_pin_metadata_url";
        public static final String CHARGING_SOUNDS_ENABLED = "charging_sounds_enabled";
        public static final String COMPATIBILITY_MODE = "compatibility_mode";
        public static final String CONNECTIVITY_CHANGE_DELAY = "connectivity_change_delay";
        public static final String CONNECTIVITY_METRICS_BUFFER_SIZE = "connectivity_metrics_buffer_size";
        public static final String CONNECTIVITY_SAMPLING_INTERVAL_IN_SECONDS = "connectivity_sampling_interval_in_seconds";
        public static final String CONTACTS_DATABASE_WAL_ENABLED = "contacts_database_wal_enabled";
        @Deprecated
        public static final String CONTACT_METADATA_SYNC = "contact_metadata_sync";
        public static final String CONTACT_METADATA_SYNC_ENABLED = "contact_metadata_sync_enabled";
        public static final Uri CONTENT_URI = Uri.parse("content://settings/global");
        public static final String CURR_WALLPAPER_OFFSETS = "curr_wallpaper_offsets";
        public static final String DATABASE_CREATION_BUILDID = "database_creation_buildid";
        public static final String DATABASE_DOWNGRADE_REASON = "database_downgrade_reason";
        public static final String DATA_ACTIVITY_TIMEOUT_MOBILE = "data_activity_timeout_mobile";
        public static final String DATA_ACTIVITY_TIMEOUT_WIFI = "data_activity_timeout_wifi";
        public static final String DATA_ROAMING = "data_roaming";
        public static final String DATA_STALL_ALARM_AGGRESSIVE_DELAY_IN_MS = "data_stall_alarm_aggressive_delay_in_ms";
        public static final String DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS = "data_stall_alarm_non_aggressive_delay_in_ms";
        public static final String DEBUG_APP = "debug_app";
        public static final String DEBUG_VIEW_ATTRIBUTES = "debug_view_attributes";
        public static final String DEFAULT_DNS_SERVER = "default_dns_server";
        public static final String DEFAULT_INSTALL_LOCATION = "default_install_location";
        public static final String DESK_DOCK_SOUND = "desk_dock_sound";
        public static final String DESK_UNDOCK_SOUND = "desk_undock_sound";
        public static final String DEVELOPMENT_ENABLE_FREEFORM_WINDOWS_SUPPORT = "enable_freeform_support";
        public static final String DEVELOPMENT_FORCE_RESIZABLE_ACTIVITIES = "force_resizable_activities";
        public static final String DEVELOPMENT_FORCE_RTL = "debug.force_rtl";
        public static final String DEVELOPMENT_SETTINGS_ENABLED = "development_settings_enabled";
        public static final String DEVICE_DEMO_MODE = "device_demo_mode";
        public static final String DEVICE_IDLE_CONSTANTS = "device_idle_constants";
        public static final String DEVICE_IDLE_CONSTANTS_WATCH = "device_idle_constants_watch";
        public static final String DEVICE_NAME = "device_name";
        public static final String DEVICE_POLICY_CONSTANTS = "device_policy_constants";
        public static final String DEVICE_PROVISIONED = "device_provisioned";
        public static final String DEVICE_PROVISIONING_MOBILE_DATA_ENABLED = "device_provisioning_mobile_data";
        public static final String DISK_FREE_CHANGE_REPORTING_THRESHOLD = "disk_free_change_reporting_threshold";
        public static final String DISPLAY_SCALING_FORCE = "display_scaling_force";
        public static final String DISPLAY_SIZE_FORCED = "display_size_forced";
        public static final String DNS_RESOLVER_MAX_SAMPLES = "dns_resolver_max_samples";
        public static final String DNS_RESOLVER_MIN_SAMPLES = "dns_resolver_min_samples";
        public static final String DNS_RESOLVER_SAMPLE_VALIDITY_SECONDS = "dns_resolver_sample_validity_seconds";
        public static final String DNS_RESOLVER_SUCCESS_THRESHOLD_PERCENT = "dns_resolver_success_threshold_percent";
        public static final String DOCK_AUDIO_MEDIA_ENABLED = "dock_audio_media_enabled";
        public static final String DOCK_SOUNDS_ENABLED = "dock_sounds_enabled";
        public static final String DOCK_SOUNDS_ENABLED_WHEN_ACCESSIBILITY = "dock_sounds_enabled_when_accessbility";
        public static final String DOWNLOAD_MAX_BYTES_OVER_MOBILE = "download_manager_max_bytes_over_mobile";
        public static final String DOWNLOAD_RECOMMENDED_MAX_BYTES_OVER_MOBILE = "download_manager_recommended_max_bytes_over_mobile";
        public static final String DROPBOX_AGE_SECONDS = "dropbox_age_seconds";
        public static final String DROPBOX_MAX_FILES = "dropbox_max_files";
        public static final String DROPBOX_QUOTA_KB = "dropbox_quota_kb";
        public static final String DROPBOX_QUOTA_PERCENT = "dropbox_quota_percent";
        public static final String DROPBOX_RESERVE_PERCENT = "dropbox_reserve_percent";
        public static final String DROPBOX_TAG_PREFIX = "dropbox:";
        public static final String EMERGENCY_AFFORDANCE_NEEDED = "emergency_affordance_needed";
        public static final String EMERGENCY_TONE = "emergency_tone";
        public static final String ENABLE_ACCESSIBILITY_GLOBAL_GESTURE_ENABLED = "enable_accessibility_global_gesture_enabled";
        public static final String ENABLE_CACHE_QUOTA_CALCULATION = "enable_cache_quota_calculation";
        public static final String ENABLE_CELLULAR_ON_BOOT = "enable_cellular_on_boot";
        public static final String ENABLE_DISKSTATS_LOGGING = "enable_diskstats_logging";
        public static final String ENABLE_EPHEMERAL_FEATURE = "enable_ephemeral_feature";
        public static final String ENCODED_SURROUND_OUTPUT = "encoded_surround_output";
        public static final int ENCODED_SURROUND_OUTPUT_ALWAYS = 2;
        public static final int ENCODED_SURROUND_OUTPUT_AUTO = 0;
        public static final int ENCODED_SURROUND_OUTPUT_NEVER = 1;
        public static final String ENHANCED_4G_MODE_ENABLED = "volte_vt_enabled";
        public static final String EPHEMERAL_COOKIE_MAX_SIZE_BYTES = "ephemeral_cookie_max_size_bytes";
        public static final String ERROR_LOGCAT_PREFIX = "logcat_for_";
        public static final String FANCY_IME_ANIMATIONS = "fancy_ime_animations";
        public static final String FORCE_ALLOW_ON_EXTERNAL = "force_allow_on_external";
        public static final String FSTRIM_MANDATORY_INTERVAL = "fstrim_mandatory_interval";
        public static final String GLOBAL_HTTP_PROXY_EXCLUSION_LIST = "global_http_proxy_exclusion_list";
        public static final String GLOBAL_HTTP_PROXY_HOST = "global_http_proxy_host";
        public static final String GLOBAL_HTTP_PROXY_PAC = "global_proxy_pac_url";
        public static final String GLOBAL_HTTP_PROXY_PORT = "global_http_proxy_port";
        public static final String GPRS_REGISTER_CHECK_PERIOD_MS = "gprs_register_check_period_ms";
        public static final String HDMI_CONTROL_AUTO_DEVICE_OFF_ENABLED = "hdmi_control_auto_device_off_enabled";
        public static final String HDMI_CONTROL_AUTO_WAKEUP_ENABLED = "hdmi_control_auto_wakeup_enabled";
        public static final String HDMI_CONTROL_ENABLED = "hdmi_control_enabled";
        public static final String HDMI_SYSTEM_AUDIO_CONTROL_ENABLED = "hdmi_system_audio_control_enabled";
        public static final String HEADS_UP_NOTIFICATIONS_ENABLED = "heads_up_notifications_enabled";
        public static final int HEADS_UP_OFF = 0;
        public static final int HEADS_UP_ON = 1;
        public static final String HTTP_PROXY = "http_proxy";
        public static final String HW_BLUETOOTH_INTEROPERABILITY_ADDR_LIST = "hw_bluetooth_interoperability_addr_list";
        public static final String HW_BLUETOOTH_INTEROPERABILITY_MANU_LIST = "hw_bluetooth_interoperability_manu_list";
        public static final String HW_BLUETOOTH_INTEROPERABILITY_NAME_LIST = "hw_bluetooth_interoperability_name_list";
        public static final String HW_BLUETOOTH_INTEROP_EMUI_VERSION = "hw_bluetooth_interop_emui_version";
        public static final String HW_BLUETOOTH_INTEROP_VERSION = "hw_bluetooth_interop_version";
        public static final String INET_CONDITION_DEBOUNCE_DOWN_DELAY = "inet_condition_debounce_down_delay";
        public static final String INET_CONDITION_DEBOUNCE_UP_DELAY = "inet_condition_debounce_up_delay";
        public static final String INSTALLED_INSTANT_APP_MAX_CACHE_PERIOD = "installed_instant_app_max_cache_period";
        public static final String INSTALLED_INSTANT_APP_MIN_CACHE_PERIOD = "installed_instant_app_min_cache_period";
        @Deprecated
        public static final String INSTALL_NON_MARKET_APPS = "install_non_market_apps";
        public static final String INSTANT_APP_DEXOPT_ENABLED = "instant_app_dexopt_enabled";
        public static final Set<String> INSTANT_APP_SETTINGS = new ArraySet();
        public static final String INTENT_FIREWALL_UPDATE_CONTENT_URL = "intent_firewall_content_url";
        public static final String INTENT_FIREWALL_UPDATE_METADATA_URL = "intent_firewall_metadata_url";
        public static final String JOB_SCHEDULER_CONSTANTS = "job_scheduler_constants";
        public static final String LANG_ID_UPDATE_CONTENT_URL = "lang_id_content_url";
        public static final String LANG_ID_UPDATE_METADATA_URL = "lang_id_metadata_url";
        public static final String LOCATION_BACKGROUND_THROTTLE_INTERVAL_MS = "location_background_throttle_interval_ms";
        public static final String LOCATION_BACKGROUND_THROTTLE_PACKAGE_WHITELIST = "location_background_throttle_package_whitelist";
        public static final String LOCATION_BACKGROUND_THROTTLE_PROXIMITY_ALERT_INTERVAL_MS = "location_background_throttle_proximity_alert_interval_ms";
        public static final String LOCATION_SETTINGS_LINK_TO_PERMISSIONS_ENABLED = "location_settings_link_to_permissions_enabled";
        public static final String LOCK_SOUND = "lock_sound";
        public static final String LOW_BATTERY_SOUND = "low_battery_sound";
        public static final String LOW_BATTERY_SOUND_TIMEOUT = "low_battery_sound_timeout";
        public static final String LOW_POWER_MODE = "low_power";
        public static final String LOW_POWER_MODE_TRIGGER_LEVEL = "low_power_trigger_level";
        public static final String LTE_SERVICE_FORCED = "lte_service_forced";
        public static final String MAX_NOTIFICATION_ENQUEUE_RATE = "max_notification_enqueue_rate";
        public static final String MDC_INITIAL_MAX_RETRY = "mdc_initial_max_retry";
        public static final String MHL_INPUT_SWITCHING_ENABLED = "mhl_input_switching_enabled";
        public static final String MHL_POWER_CHARGE_ENABLED = "mhl_power_charge_enabled";
        public static final String MOBILE_DATA = "mobile_data";
        public static final String MOBILE_DATA_ALWAYS_ON = "mobile_data_always_on";
        public static final String MODE_RINGER = "mode_ringer";
        private static final HashSet<String> MOVED_TO_SECURE = new HashSet(1);
        public static final String MULTI_SIM_DATA_CALL_SUBSCRIPTION = "multi_sim_data_call";
        public static final String MULTI_SIM_SMS_PROMPT = "multi_sim_sms_prompt";
        public static final String MULTI_SIM_SMS_SUBSCRIPTION = "multi_sim_sms";
        public static final String[] MULTI_SIM_USER_PREFERRED_SUBS = new String[]{"user_preferred_sub1", "user_preferred_sub2", "user_preferred_sub3"};
        public static final String MULTI_SIM_VOICE_CALL_SUBSCRIPTION = "multi_sim_voice_call";
        public static final String MULTI_SIM_VOICE_PROMPT = "multi_sim_voice_prompt";
        public static final String NETSTATS_DEV_BUCKET_DURATION = "netstats_dev_bucket_duration";
        public static final String NETSTATS_DEV_DELETE_AGE = "netstats_dev_delete_age";
        public static final String NETSTATS_DEV_PERSIST_BYTES = "netstats_dev_persist_bytes";
        public static final String NETSTATS_DEV_ROTATE_AGE = "netstats_dev_rotate_age";
        public static final String NETSTATS_ENABLED = "netstats_enabled";
        public static final String NETSTATS_GLOBAL_ALERT_BYTES = "netstats_global_alert_bytes";
        public static final String NETSTATS_POLL_INTERVAL = "netstats_poll_interval";
        public static final String NETSTATS_SAMPLE_ENABLED = "netstats_sample_enabled";
        public static final String NETSTATS_TIME_CACHE_MAX_AGE = "netstats_time_cache_max_age";
        public static final String NETSTATS_UID_BUCKET_DURATION = "netstats_uid_bucket_duration";
        public static final String NETSTATS_UID_DELETE_AGE = "netstats_uid_delete_age";
        public static final String NETSTATS_UID_PERSIST_BYTES = "netstats_uid_persist_bytes";
        public static final String NETSTATS_UID_ROTATE_AGE = "netstats_uid_rotate_age";
        public static final String NETSTATS_UID_TAG_BUCKET_DURATION = "netstats_uid_tag_bucket_duration";
        public static final String NETSTATS_UID_TAG_DELETE_AGE = "netstats_uid_tag_delete_age";
        public static final String NETSTATS_UID_TAG_PERSIST_BYTES = "netstats_uid_tag_persist_bytes";
        public static final String NETSTATS_UID_TAG_ROTATE_AGE = "netstats_uid_tag_rotate_age";
        public static final String NETWORK_ACCESS_TIMEOUT_MS = "network_access_timeout_ms";
        public static final String NETWORK_AVOID_BAD_WIFI = "network_avoid_bad_wifi";
        public static final String NETWORK_METERED_MULTIPATH_PREFERENCE = "network_metered_multipath_preference";
        public static final String NETWORK_PREFERENCE = "network_preference";
        public static final String NETWORK_RECOMMENDATIONS_ENABLED = "network_recommendations_enabled";
        public static final String NETWORK_RECOMMENDATIONS_PACKAGE = "network_recommendations_package";
        public static final String NETWORK_RECOMMENDATION_REQUEST_TIMEOUT_MS = "network_recommendation_request_timeout_ms";
        public static final String NETWORK_SCORER_APP = "network_scorer_app";
        public static final String NETWORK_SCORING_PROVISIONED = "network_scoring_provisioned";
        public static final String NETWORK_SCORING_UI_ENABLED = "network_scoring_ui_enabled";
        public static final String NETWORK_SWITCH_NOTIFICATION_DAILY_LIMIT = "network_switch_notification_daily_limit";
        public static final String NETWORK_SWITCH_NOTIFICATION_RATE_LIMIT_MILLIS = "network_switch_notification_rate_limit_millis";
        public static final String NEW_CONTACT_AGGREGATOR = "new_contact_aggregator";
        public static final String NITZ_UPDATE_DIFF = "nitz_update_diff";
        public static final String NITZ_UPDATE_SPACING = "nitz_update_spacing";
        public static final String NSD_ON = "nsd_on";
        public static final String NTP_SERVER = "ntp_server";
        public static final String NTP_TIMEOUT = "ntp_timeout";
        public static final String OTA_DISABLE_AUTOMATIC_UPDATE = "ota_disable_automatic_update";
        public static final String OVERLAY_DISPLAY_DEVICES = "overlay_display_devices";
        public static final String PACKAGE_VERIFIER_DEFAULT_RESPONSE = "verifier_default_response";
        public static final String PACKAGE_VERIFIER_ENABLE = "package_verifier_enable";
        public static final String PACKAGE_VERIFIER_INCLUDE_ADB = "verifier_verify_adb_installs";
        public static final String PACKAGE_VERIFIER_SETTING_VISIBLE = "verifier_setting_visible";
        public static final String PACKAGE_VERIFIER_TIMEOUT = "verifier_timeout";
        public static final String PAC_CHANGE_DELAY = "pac_change_delay";
        public static final String PDP_WATCHDOG_ERROR_POLL_COUNT = "pdp_watchdog_error_poll_count";
        public static final String PDP_WATCHDOG_ERROR_POLL_INTERVAL_MS = "pdp_watchdog_error_poll_interval_ms";
        public static final String PDP_WATCHDOG_LONG_POLL_INTERVAL_MS = "pdp_watchdog_long_poll_interval_ms";
        public static final String PDP_WATCHDOG_MAX_PDP_RESET_FAIL_COUNT = "pdp_watchdog_max_pdp_reset_fail_count";
        public static final String PDP_WATCHDOG_POLL_INTERVAL_MS = "pdp_watchdog_poll_interval_ms";
        public static final String PDP_WATCHDOG_TRIGGER_PACKET_COUNT = "pdp_watchdog_trigger_packet_count";
        public static final String POLICY_CONTROL = "policy_control";
        public static final String POWER_MANAGER_CONSTANTS = "power_manager_constants";
        public static final String POWER_SOUNDS_ENABLED = "power_sounds_enabled";
        public static final String PREFERRED_NETWORK_MODE = "preferred_network_mode";
        public static final String PROVISIONING_APN_ALARM_DELAY_IN_MS = "provisioning_apn_alarm_delay_in_ms";
        public static final String RADIO_BLUETOOTH = "bluetooth";
        public static final String RADIO_CELL = "cell";
        public static final String RADIO_NFC = "nfc";
        public static final String RADIO_WIFI = "wifi";
        public static final String RADIO_WIMAX = "wimax";
        public static final String READ_EXTERNAL_STORAGE_ENFORCED_DEFAULT = "read_external_storage_enforced_default";
        public static final String RECOMMENDED_NETWORK_EVALUATOR_CACHE_EXPIRY_MS = "recommended_network_evaluator_cache_expiry_ms";
        public static final String REQUIRE_PASSWORD_TO_DECRYPT = "require_password_to_decrypt";
        public static final String RETAIL_DEMO_MODE_CONSTANTS = "retail_demo_mode_constants";
        public static final String SAFE_BOOT_DISALLOWED = "safe_boot_disallowed";
        public static final String SAMPLING_PROFILER_MS = "sampling_profiler_ms";
        public static final String SELINUX_STATUS = "selinux_status";
        public static final String SELINUX_UPDATE_CONTENT_URL = "selinux_content_url";
        public static final String SELINUX_UPDATE_METADATA_URL = "selinux_metadata_url";
        public static final String SEND_ACTION_APP_ERROR = "send_action_app_error";
        public static final String[] SETTINGS_TO_BACKUP = new String[]{"bugreport_in_power_menu", "stay_on_while_plugged_in", "auto_time", "auto_time_zone", "power_sounds_enabled", "dock_sounds_enabled", CHARGING_SOUNDS_ENABLED, "usb_mass_storage_enabled", NETWORK_RECOMMENDATIONS_ENABLED, WIFI_WAKEUP_ENABLED, "wifi_networks_available_notification_on", USE_OPEN_WIFI_PACKAGE, WIFI_WATCHDOG_POOR_NETWORK_TEST_ENABLED, EMERGENCY_TONE, CALL_AUTO_RETRY, DOCK_AUDIO_MEDIA_ENABLED, ENCODED_SURROUND_OUTPUT, LOW_POWER_MODE_TRIGGER_LEVEL, "bluetooth_on"};
        public static final String SETUP_PREPAID_DATA_SERVICE_URL = "setup_prepaid_data_service_url";
        public static final String SETUP_PREPAID_DETECTION_REDIR_HOST = "setup_prepaid_detection_redir_host";
        public static final String SETUP_PREPAID_DETECTION_TARGET_URL = "setup_prepaid_detection_target_url";
        public static final String SET_GLOBAL_HTTP_PROXY = "set_global_http_proxy";
        public static final String SET_INSTALL_LOCATION = "set_install_location";
        public static final String SHORTCUT_MANAGER_CONSTANTS = "shortcut_manager_constants";
        public static final String SHOW_NOTIFICATION_CHANNEL_WARNINGS = "show_notification_channel_warnings";
        @Deprecated
        public static final String SHOW_PROCESSES = "show_processes";
        public static final String SHOW_TEMPERATURE_WARNING = "show_temperature_warning";
        public static final String SMART_SELECTION_UPDATE_CONTENT_URL = "smart_selection_content_url";
        public static final String SMART_SELECTION_UPDATE_METADATA_URL = "smart_selection_metadata_url";
        public static final String SMS_OUTGOING_CHECK_INTERVAL_MS = "sms_outgoing_check_interval_ms";
        public static final String SMS_OUTGOING_CHECK_MAX_COUNT = "sms_outgoing_check_max_count";
        public static final String SMS_SHORT_CODES_UPDATE_CONTENT_URL = "sms_short_codes_content_url";
        public static final String SMS_SHORT_CODES_UPDATE_METADATA_URL = "sms_short_codes_metadata_url";
        public static final String SMS_SHORT_CODE_CONFIRMATION = "sms_short_code_confirmation";
        public static final String SMS_SHORT_CODE_RULE = "sms_short_code_rule";
        public static final String STAY_ON_WHILE_PLUGGED_IN = "stay_on_while_plugged_in";
        public static final String STORAGE_BENCHMARK_INTERVAL = "storage_benchmark_interval";
        public static final String SYNC_MAX_RETRY_DELAY_IN_SECONDS = "sync_max_retry_delay_in_seconds";
        public static final String SYS_FREE_STORAGE_LOG_INTERVAL = "sys_free_storage_log_interval";
        public static final String SYS_STORAGE_CACHE_MAX_BYTES = "sys_storage_cache_max_bytes";
        public static final String SYS_STORAGE_CACHE_PERCENTAGE = "sys_storage_cache_percentage";
        public static final String SYS_STORAGE_FULL_THRESHOLD_BYTES = "sys_storage_full_threshold_bytes";
        public static final String SYS_STORAGE_THRESHOLD_MAX_BYTES = "sys_storage_threshold_max_bytes";
        public static final String SYS_STORAGE_THRESHOLD_PERCENTAGE = "sys_storage_threshold_percentage";
        public static final String TCP_DEFAULT_INIT_RWND = "tcp_default_init_rwnd";
        public static final String TETHER_DUN_APN = "tether_dun_apn";
        public static final String TETHER_DUN_REQUIRED = "tether_dun_required";
        public static final String TETHER_SUPPORTED = "tether_supported";
        public static final String THEATER_MODE_ON = "theater_mode_on";
        public static final String TRANSITION_ANIMATION_SCALE = "transition_animation_scale";
        public static final String TRUSTED_SOUND = "trusted_sound";
        public static final String TZINFO_UPDATE_CONTENT_URL = "tzinfo_content_url";
        public static final String TZINFO_UPDATE_METADATA_URL = "tzinfo_metadata_url";
        public static final String UNINSTALLED_INSTANT_APP_MAX_CACHE_PERIOD = "uninstalled_instant_app_max_cache_period";
        public static final String UNINSTALLED_INSTANT_APP_MIN_CACHE_PERIOD = "uninstalled_instant_app_min_cache_period";
        public static final String UNLOCK_SOUND = "unlock_sound";
        public static final String UNUSED_STATIC_SHARED_LIB_MIN_CACHE_PERIOD = "unused_static_shared_lib_min_cache_period";
        public static final String USB_MASS_STORAGE_ENABLED = "usb_mass_storage_enabled";
        public static final String USER_SET_AIRPLANE = "user_set_airplane";
        public static final String USE_GOOGLE_MAIL = "use_google_mail";
        public static final String USE_OPEN_WIFI_PACKAGE = "use_open_wifi_package";
        public static final String VT_IMS_ENABLED = "vt_ims_enabled";
        public static final String WAIT_FOR_DEBUGGER = "wait_for_debugger";
        public static final String WARNING_TEMPERATURE = "warning_temperature";
        public static final String WEBVIEW_DATA_REDUCTION_PROXY_KEY = "webview_data_reduction_proxy_key";
        public static final String WEBVIEW_FALLBACK_LOGIC_ENABLED = "webview_fallback_logic_enabled";
        public static final String WEBVIEW_MULTIPROCESS = "webview_multiprocess";
        public static final String WEBVIEW_PROVIDER = "webview_provider";
        public static final String WFC_IMS_ENABLED = "wfc_ims_enabled";
        public static final String WFC_IMS_MODE = "wfc_ims_mode";
        public static final String WFC_IMS_ROAMING_ENABLED = "wfc_ims_roaming_enabled";
        public static final String WFC_IMS_ROAMING_MODE = "wfc_ims_roaming_mode";
        public static final String WIFI_BADGING_THRESHOLDS = "wifi_badging_thresholds";
        public static final String WIFI_BOUNCE_DELAY_OVERRIDE_MS = "wifi_bounce_delay_override_ms";
        public static final String WIFI_COUNTRY_CODE = "wifi_country_code";
        public static final String WIFI_DEVICE_OWNER_CONFIGS_LOCKDOWN = "wifi_device_owner_configs_lockdown";
        public static final String WIFI_DISPLAY_CERTIFICATION_ON = "wifi_display_certification_on";
        public static final String WIFI_DISPLAY_ON = "wifi_display_on";
        public static final String WIFI_DISPLAY_WPS_CONFIG = "wifi_display_wps_config";
        public static final String WIFI_ENHANCED_AUTO_JOIN = "wifi_enhanced_auto_join";
        public static final String WIFI_EPHEMERAL_OUT_OF_RANGE_TIMEOUT_MS = "wifi_ephemeral_out_of_range_timeout_ms";
        public static final String WIFI_FRAMEWORK_SCAN_INTERVAL_MS = "wifi_framework_scan_interval_ms";
        public static final String WIFI_FREQUENCY_BAND = "wifi_frequency_band";
        public static final String WIFI_IDLE_MS = "wifi_idle_ms";
        public static final String WIFI_MAX_DHCP_RETRY_COUNT = "wifi_max_dhcp_retry_count";
        public static final String WIFI_MOBILE_DATA_TRANSITION_WAKELOCK_TIMEOUT_MS = "wifi_mobile_data_transition_wakelock_timeout_ms";
        @Deprecated
        public static final String WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON = "wifi_networks_available_notification_on";
        public static final String WIFI_NETWORKS_AVAILABLE_REPEAT_DELAY = "wifi_networks_available_repeat_delay";
        public static final String WIFI_NETWORK_SHOW_RSSI = "wifi_network_show_rssi";
        public static final String WIFI_NUM_OPEN_NETWORKS_KEPT = "wifi_num_open_networks_kept";
        public static final String WIFI_ON = "wifi_on";
        public static final String WIFI_P2P_DEVICE_NAME = "wifi_p2p_device_name";
        public static final String WIFI_PASSPOINT_ON = "wifi_passpoint_on";
        public static final String WIFI_REENABLE_DELAY_MS = "wifi_reenable_delay";
        public static final String WIFI_SAVED_STATE = "wifi_saved_state";
        public static final String WIFI_SCAN_ALWAYS_AVAILABLE = "wifi_scan_always_enabled";
        public static final String WIFI_SCAN_BACKGROUND_THROTTLE_INTERVAL_MS = "wifi_scan_background_throttle_interval_ms";
        public static final String WIFI_SCAN_BACKGROUND_THROTTLE_PACKAGE_WHITELIST = "wifi_scan_background_throttle_package_whitelist";
        public static final String WIFI_SCAN_INTERVAL_WHEN_P2P_CONNECTED_MS = "wifi_scan_interval_p2p_connected_ms";
        public static final String WIFI_SLEEP_POLICY = "wifi_sleep_policy";
        public static final int WIFI_SLEEP_POLICY_DEFAULT = 0;
        public static final int WIFI_SLEEP_POLICY_NEVER = 2;
        public static final int WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED = 1;
        public static final String WIFI_SUPPLICANT_SCAN_INTERVAL_MS = "wifi_supplicant_scan_interval_ms";
        public static final String WIFI_SUSPEND_OPTIMIZATIONS_ENABLED = "wifi_suspend_optimizations_enabled";
        public static final String WIFI_VERBOSE_LOGGING_ENABLED = "wifi_verbose_logging_enabled";
        public static final String WIFI_WAKEUP_AVAILABLE = "wifi_wakeup_available";
        public static final String WIFI_WAKEUP_ENABLED = "wifi_wakeup_enabled";
        public static final String WIFI_WATCHDOG_ON = "wifi_watchdog_on";
        public static final String WIFI_WATCHDOG_POOR_NETWORK_TEST_ENABLED = "wifi_watchdog_poor_network_test_enabled";
        public static final String WIMAX_NETWORKS_AVAILABLE_NOTIFICATION_ON = "wimax_networks_available_notification_on";
        public static final String WINDOW_ANIMATION_SCALE = "window_animation_scale";
        public static final String WIRELESS_CHARGING_STARTED_SOUND = "wireless_charging_started_sound";
        public static final String WTF_IS_FATAL = "wtf_is_fatal";
        public static final String ZEN_MODE = "zen_mode";
        public static final int ZEN_MODE_ALARMS = 3;
        public static final String ZEN_MODE_CONFIG_ETAG = "zen_mode_config_etag";
        public static final int ZEN_MODE_IMPORTANT_INTERRUPTIONS = 1;
        public static final int ZEN_MODE_NO_INTERRUPTIONS = 2;
        public static final int ZEN_MODE_OFF = 0;
        public static final String ZEN_MODE_RINGER_LEVEL = "zen_mode_ringer_level";
        private static final NameValueCache sNameValueCache = new NameValueCache(CONTENT_URI, Settings.CALL_METHOD_GET_GLOBAL, Settings.CALL_METHOD_PUT_GLOBAL, sProviderHolder);
        private static final ContentProviderHolder sProviderHolder = new ContentProviderHolder(CONTENT_URI);

        static {
            MOVED_TO_SECURE.add("install_non_market_apps");
            INSTANT_APP_SETTINGS.add("wait_for_debugger");
            INSTANT_APP_SETTINGS.add("device_provisioned");
            INSTANT_APP_SETTINGS.add(DEVELOPMENT_FORCE_RESIZABLE_ACTIVITIES);
            INSTANT_APP_SETTINGS.add(DEVELOPMENT_FORCE_RTL);
            INSTANT_APP_SETTINGS.add(EPHEMERAL_COOKIE_MAX_SIZE_BYTES);
            INSTANT_APP_SETTINGS.add("airplane_mode_on");
            INSTANT_APP_SETTINGS.add("window_animation_scale");
            INSTANT_APP_SETTINGS.add("transition_animation_scale");
            INSTANT_APP_SETTINGS.add("animator_duration_scale");
            INSTANT_APP_SETTINGS.add(DEBUG_VIEW_ATTRIBUTES);
            INSTANT_APP_SETTINGS.add(WTF_IS_FATAL);
        }

        public static final String getBluetoothHeadsetPriorityKey(String address) {
            return BLUETOOTH_HEADSET_PRIORITY_PREFIX + address.toUpperCase(Locale.ROOT);
        }

        public static final String getBluetoothA2dpSinkPriorityKey(String address) {
            return BLUETOOTH_A2DP_SINK_PRIORITY_PREFIX + address.toUpperCase(Locale.ROOT);
        }

        public static final String getBluetoothA2dpSrcPriorityKey(String address) {
            return BLUETOOTH_A2DP_SRC_PRIORITY_PREFIX + address.toUpperCase(Locale.ROOT);
        }

        public static final String getBluetoothA2dpSupportsOptionalCodecsKey(String address) {
            return BLUETOOTH_A2DP_SUPPORTS_OPTIONAL_CODECS_PREFIX + address.toUpperCase(Locale.ROOT);
        }

        public static final String getBluetoothA2dpOptionalCodecsEnabledKey(String address) {
            return BLUETOOTH_A2DP_OPTIONAL_CODECS_ENABLED_PREFIX + address.toUpperCase(Locale.ROOT);
        }

        public static final String getBluetoothInputDevicePriorityKey(String address) {
            return BLUETOOTH_INPUT_DEVICE_PRIORITY_PREFIX + address.toUpperCase(Locale.ROOT);
        }

        public static final String getBluetoothPanPriorityKey(String address) {
            return BLUETOOTH_PAN_PRIORITY_PREFIX + address.toUpperCase(Locale.ROOT);
        }

        public static final String getBluetoothMapPriorityKey(String address) {
            return BLUETOOTH_MAP_PRIORITY_PREFIX + address.toUpperCase(Locale.ROOT);
        }

        public static final String getBluetoothMapClientPriorityKey(String address) {
            return BLUETOOTH_MAP_CLIENT_PRIORITY_PREFIX + address.toUpperCase(Locale.ROOT);
        }

        public static final String getBluetoothPbapClientPriorityKey(String address) {
            return BLUETOOTH_PBAP_CLIENT_PRIORITY_PREFIX + address.toUpperCase(Locale.ROOT);
        }

        public static final String getBluetoothSapPriorityKey(String address) {
            return BLUETOOTH_SAP_PRIORITY_PREFIX + address.toUpperCase(Locale.ROOT);
        }

        public static String zenModeToString(int mode) {
            if (mode == 1) {
                return "ZEN_MODE_IMPORTANT_INTERRUPTIONS";
            }
            if (mode == 3) {
                return "ZEN_MODE_ALARMS";
            }
            if (mode == 2) {
                return "ZEN_MODE_NO_INTERRUPTIONS";
            }
            return "ZEN_MODE_OFF";
        }

        public static boolean isValidZenMode(int value) {
            switch (value) {
                case 0:
                case 1:
                case 2:
                case 3:
                    return true;
                default:
                    return false;
            }
        }

        public static void getMovedToSecureSettings(Set<String> outKeySet) {
            outKeySet.addAll(MOVED_TO_SECURE);
        }

        public static String getString(ContentResolver resolver, String name) {
            return getStringForUser(resolver, name, UserHandle.myUserId());
        }

        public static String getStringForUser(ContentResolver resolver, String name, int userHandle) {
            if (!MOVED_TO_SECURE.contains(name)) {
                return sNameValueCache.getStringForUser(resolver, name, userHandle);
            }
            Log.w(Settings.TAG, "Setting " + name + " has moved from android.provider.Settings.Global" + " to android.provider.Settings.Secure, returning read-only value.");
            return Secure.getStringForUser(resolver, name, userHandle);
        }

        public static boolean putString(ContentResolver resolver, String name, String value) {
            return putStringForUser(resolver, name, value, null, false, UserHandle.myUserId());
        }

        public static boolean putString(ContentResolver resolver, String name, String value, String tag, boolean makeDefault) {
            return putStringForUser(resolver, name, value, tag, makeDefault, UserHandle.myUserId());
        }

        public static void resetToDefaults(ContentResolver resolver, String tag) {
            resetToDefaultsAsUser(resolver, tag, 1, UserHandle.myUserId());
        }

        public static void resetToDefaultsAsUser(ContentResolver resolver, String tag, int mode, int userHandle) {
            try {
                Bundle arg = new Bundle();
                arg.putInt(Settings.CALL_METHOD_USER_KEY, userHandle);
                if (tag != null) {
                    arg.putString(Settings.CALL_METHOD_TAG_KEY, tag);
                }
                arg.putInt(Settings.CALL_METHOD_RESET_MODE_KEY, mode);
                sProviderHolder.getProvider(resolver).call(resolver.getPackageName(), Settings.CALL_METHOD_RESET_GLOBAL, null, arg);
            } catch (RemoteException e) {
                Log.w(Settings.TAG, "Can't reset do defaults for " + CONTENT_URI, e);
            }
        }

        public static boolean putStringForUser(ContentResolver resolver, String name, String value, int userHandle) {
            return putStringForUser(resolver, name, value, null, false, userHandle);
        }

        public static boolean putStringForUser(ContentResolver resolver, String name, String value, String tag, boolean makeDefault, int userHandle) {
            HwFrameworkFactory.getHwSettingsManager().setAirplaneMode(resolver, name);
            if (!MOVED_TO_SECURE.contains(name)) {
                return sNameValueCache.putStringForUser(resolver, name, value, tag, makeDefault, userHandle);
            }
            Log.w(Settings.TAG, "Setting " + name + " has moved from android.provider.Settings.Global" + " to android.provider.Settings.Secure, value is unchanged.");
            return Secure.putStringForUser(resolver, name, value, tag, makeDefault, userHandle);
        }

        public static Uri getUriFor(String name) {
            return NameValueTable.getUriFor(CONTENT_URI, name);
        }

        public static int getInt(ContentResolver cr, String name, int def) {
            String v = getString(cr, name);
            if (v != null) {
                try {
                    def = Integer.parseInt(v);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            return def;
        }

        public static int getInt(ContentResolver cr, String name) throws SettingNotFoundException {
            try {
                return Integer.parseInt(getString(cr, name));
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
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

        public static long getLong(ContentResolver cr, String name) throws SettingNotFoundException {
            try {
                return Long.parseLong(getString(cr, name));
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
            }
        }

        public static boolean putLong(ContentResolver cr, String name, long value) {
            return putString(cr, name, Long.toString(value));
        }

        public static float getFloat(ContentResolver cr, String name, float def) {
            String v = getString(cr, name);
            if (v != null) {
                try {
                    def = Float.parseFloat(v);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            return def;
        }

        public static float getFloat(ContentResolver cr, String name) throws SettingNotFoundException {
            String v = getString(cr, name);
            if (v == null) {
                throw new SettingNotFoundException(name);
            }
            try {
                return Float.parseFloat(v);
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
            }
        }

        public static boolean putFloat(ContentResolver cr, String name, float value) {
            return putString(cr, name, Float.toString(value));
        }
    }

    private static class NameValueCache {
        private static final boolean DEBUG = false;
        private static final String NAME_EQ_PLACEHOLDER = "name=?";
        private static final String PKG_DESKTOP_SYSTEMUI = "com.huawei.desktop.systemui";
        private static final String PKG_MMITEST = "com.huawei.mmitest";
        private static final String PKG_SETTINGS = "com.android.settings";
        private static final String PKG_SYSTEMUI = "com.android.systemui";
        private static final String[] SELECT_VALUE_PROJECTION = new String[]{"value"};
        private String[] allowAdjustBrightnessApps = new String[]{"com.android.systemui", "com.android.settings", "com.huawei.desktop.systemui", PKG_MMITEST};
        private final String mCallGetCommand;
        private final String mCallSetCommand;
        @GuardedBy("this")
        private GenerationTracker mGenerationTracker;
        private final ContentProviderHolder mProviderHolder;
        private final Uri mUri;
        private final HashMap<String, String> mValues = new HashMap();

        public NameValueCache(Uri uri, String getCommand, String setCommand, ContentProviderHolder providerHolder) {
            this.mUri = uri;
            this.mCallGetCommand = getCommand;
            this.mCallSetCommand = setCommand;
            this.mProviderHolder = providerHolder;
        }

        private boolean allowBrightnessAdjust(String packageName) {
            if (packageName == null) {
                return false;
            }
            for (String startsWith : this.allowAdjustBrightnessApps) {
                if (packageName.startsWith(startsWith)) {
                    return true;
                }
            }
            return false;
        }

        public boolean putStringForUser(ContentResolver cr, String name, String value, String tag, boolean makeDefault, int userHandle) {
            if (cr == null) {
                try {
                    Log.w(Settings.TAG, "ContentResolver is mull!");
                    return false;
                } catch (RemoteException e) {
                    Log.w(Settings.TAG, "Can't set key " + name + " in " + this.mUri, e);
                    return false;
                }
            }
            String mode = getStringForUser(cr, System.SCREEN_BRIGHTNESS_MODE, userHandle);
            if (System.SCREEN_BRIGHTNESS_MODE.equals(name)) {
                String packageName = cr.getPackageName();
                if (allowBrightnessAdjust(packageName)) {
                    Log.i(Settings.TAG, "nosystemapp to change adjust brightness mode,packageName:" + packageName + " , mode:" + mode);
                    if (!mode.equals(value)) {
                        if (mode.equals("0")) {
                            putStringForUser(cr, System.HW_SCREEN_BRIGHTNESS_MODE_VALUE, "1", null, false, userHandle);
                        } else if (mode.equals("1")) {
                            putStringForUser(cr, System.HW_SCREEN_BRIGHTNESS_MODE_VALUE, "0", null, false, userHandle);
                        }
                    }
                }
            }
            if (System.SCREEN_BRIGHTNESS.equals(name) && allowBrightnessAdjust(cr.getPackageName()) && mode.equals("0")) {
                putStringForUser(cr, System.HW_SCREEN_TEMP_BRIGHTNESS, value, null, false, userHandle);
            }
            Bundle arg = new Bundle();
            arg.putString("value", value);
            arg.putInt(Settings.CALL_METHOD_USER_KEY, userHandle);
            if (tag != null) {
                arg.putString(Settings.CALL_METHOD_TAG_KEY, tag);
            }
            if (makeDefault) {
                arg.putBoolean(Settings.CALL_METHOD_MAKE_DEFAULT_KEY, true);
            }
            this.mProviderHolder.getProvider(cr).call(cr.getPackageName(), this.mCallSetCommand, name, arg);
            if (System.SCREEN_BRIGHTNESS_MODE.equals(name)) {
                Log.i(Settings.TAG, "package: " + cr.getPackageName() + " modify " + System.SCREEN_BRIGHTNESS_MODE + " to " + value);
            }
            if (System.SCREEN_OFF_TIMEOUT.equals(name) || "location_providers_allowed".equals(name) || System.NOTIFICATION_SOUND.equals(name) || System.RINGTONE.equals(name)) {
                Log.i(Settings.TAG, "package: " + cr.getPackageName() + " modify " + name + " to " + value);
            }
            return true;
        }

        /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
            jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:android.provider.Settings.NameValueCache.getStringForUser(android.content.ContentResolver, java.lang.String, int):java.lang.String, dom blocks: [B:91:0x014e, B:126:0x0219]
            	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
            	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
            	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
            	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        public java.lang.String getStringForUser(android.content.ContentResolver r23, java.lang.String r24, int r25) {
            /*
            r22 = this;
            r3 = android.os.UserHandle.myUserId();
            r0 = r25;
            if (r0 != r3) goto L_0x0102;
        L_0x0008:
            r17 = 1;
        L_0x000a:
            r13 = -1;
            if (r17 == 0) goto L_0x0034;
        L_0x000d:
            monitor-enter(r22);
            r0 = r22;	 Catch:{ all -> 0x0120 }
            r3 = r0.mGenerationTracker;	 Catch:{ all -> 0x0120 }
            if (r3 == 0) goto L_0x0033;	 Catch:{ all -> 0x0120 }
        L_0x0014:
            r0 = r22;	 Catch:{ all -> 0x0120 }
            r3 = r0.mGenerationTracker;	 Catch:{ all -> 0x0120 }
            r3 = r3.isGenerationChanged();	 Catch:{ all -> 0x0120 }
            if (r3 == 0) goto L_0x0106;	 Catch:{ all -> 0x0120 }
        L_0x001e:
            r0 = r22;	 Catch:{ all -> 0x0120 }
            r3 = r0.mValues;	 Catch:{ all -> 0x0120 }
            r3.clear();	 Catch:{ all -> 0x0120 }
        L_0x0025:
            r0 = r22;	 Catch:{ all -> 0x0120 }
            r3 = r0.mGenerationTracker;	 Catch:{ all -> 0x0120 }
            if (r3 == 0) goto L_0x0033;	 Catch:{ all -> 0x0120 }
        L_0x002b:
            r0 = r22;	 Catch:{ all -> 0x0120 }
            r3 = r0.mGenerationTracker;	 Catch:{ all -> 0x0120 }
            r13 = r3.getCurrentGeneration();	 Catch:{ all -> 0x0120 }
        L_0x0033:
            monitor-exit(r22);
        L_0x0034:
            r0 = r22;
            r3 = r0.mProviderHolder;
            r0 = r23;
            r2 = r3.getProvider(r0);
            r0 = r22;
            r3 = r0.mCallGetCommand;
            if (r3 == 0) goto L_0x012b;
        L_0x0044:
            r8 = 0;
            if (r17 != 0) goto L_0x026f;
        L_0x0047:
            r9 = new android.os.Bundle;	 Catch:{ RemoteException -> 0x012a }
            r9.<init>();	 Catch:{ RemoteException -> 0x012a }
            r3 = "_user";	 Catch:{ RemoteException -> 0x0265 }
            r0 = r25;	 Catch:{ RemoteException -> 0x0265 }
            r9.putInt(r3, r0);	 Catch:{ RemoteException -> 0x0265 }
        L_0x0054:
            r18 = 0;	 Catch:{ RemoteException -> 0x0265 }
            monitor-enter(r22);	 Catch:{ RemoteException -> 0x0265 }
            if (r17 == 0) goto L_0x0123;
        L_0x0059:
            r0 = r22;	 Catch:{ all -> 0x0126 }
            r3 = r0.mGenerationTracker;	 Catch:{ all -> 0x0126 }
            if (r3 != 0) goto L_0x0123;	 Catch:{ all -> 0x0126 }
        L_0x005f:
            r18 = 1;	 Catch:{ all -> 0x0126 }
            if (r9 != 0) goto L_0x026c;	 Catch:{ all -> 0x0126 }
        L_0x0063:
            r8 = new android.os.Bundle;	 Catch:{ all -> 0x0126 }
            r8.<init>();	 Catch:{ all -> 0x0126 }
        L_0x0068:
            r3 = "_track_generation";	 Catch:{ all -> 0x0269 }
            r4 = 0;	 Catch:{ all -> 0x0269 }
            r8.putString(r3, r4);	 Catch:{ all -> 0x0269 }
        L_0x006f:
            monitor-exit(r22);	 Catch:{ RemoteException -> 0x012a }
            r3 = android.provider.Settings.isInSystemServer();	 Catch:{ RemoteException -> 0x012a }
            if (r3 == 0) goto L_0x016e;	 Catch:{ RemoteException -> 0x012a }
        L_0x0076:
            r3 = android.os.Binder.getCallingUid();	 Catch:{ RemoteException -> 0x012a }
            r4 = android.os.Process.myUid();	 Catch:{ RemoteException -> 0x012a }
            if (r3 == r4) goto L_0x016e;	 Catch:{ RemoteException -> 0x012a }
        L_0x0080:
            r20 = android.os.Binder.clearCallingIdentity();	 Catch:{ RemoteException -> 0x012a }
            r3 = r23.getPackageName();	 Catch:{ all -> 0x0169 }
            r0 = r22;	 Catch:{ all -> 0x0169 }
            r4 = r0.mCallGetCommand;	 Catch:{ all -> 0x0169 }
            r0 = r24;	 Catch:{ all -> 0x0169 }
            r11 = r2.call(r3, r4, r0, r8);	 Catch:{ all -> 0x0169 }
            android.os.Binder.restoreCallingIdentity(r20);	 Catch:{ RemoteException -> 0x012a }
        L_0x0095:
            if (r11 == 0) goto L_0x012b;	 Catch:{ RemoteException -> 0x012a }
        L_0x0097:
            r3 = "value";	 Catch:{ RemoteException -> 0x012a }
            r19 = r11.getString(r3);	 Catch:{ RemoteException -> 0x012a }
            if (r17 == 0) goto L_0x0101;	 Catch:{ RemoteException -> 0x012a }
        L_0x00a0:
            monitor-enter(r22);	 Catch:{ RemoteException -> 0x012a }
            if (r18 == 0) goto L_0x00df;
        L_0x00a3:
            r3 = "_track_generation";	 Catch:{ all -> 0x019b }
            r10 = r11.getParcelable(r3);	 Catch:{ all -> 0x019b }
            r10 = (android.util.MemoryIntArray) r10;	 Catch:{ all -> 0x019b }
            r3 = "_generation_index";	 Catch:{ all -> 0x019b }
            r4 = -1;	 Catch:{ all -> 0x019b }
            r16 = r11.getInt(r3, r4);	 Catch:{ all -> 0x019b }
            if (r10 == 0) goto L_0x00df;	 Catch:{ all -> 0x019b }
        L_0x00b6:
            if (r16 < 0) goto L_0x00df;	 Catch:{ all -> 0x019b }
        L_0x00b8:
            r3 = "_generation";	 Catch:{ all -> 0x019b }
            r4 = 0;	 Catch:{ all -> 0x019b }
            r15 = r11.getInt(r3, r4);	 Catch:{ all -> 0x019b }
            r0 = r22;	 Catch:{ all -> 0x019b }
            r3 = r0.mGenerationTracker;	 Catch:{ all -> 0x019b }
            if (r3 == 0) goto L_0x00cd;	 Catch:{ all -> 0x019b }
        L_0x00c6:
            r0 = r22;	 Catch:{ all -> 0x019b }
            r3 = r0.mGenerationTracker;	 Catch:{ all -> 0x019b }
            r3.destroy();	 Catch:{ all -> 0x019b }
        L_0x00cd:
            r3 = new android.provider.Settings$GenerationTracker;	 Catch:{ all -> 0x019b }
            r4 = new android.provider.-$Lambda$87WmhkvObehVg0OMBzwa_MTVV8g;	 Catch:{ all -> 0x019b }
            r0 = r22;	 Catch:{ all -> 0x019b }
            r4.<init>(r0);	 Catch:{ all -> 0x019b }
            r0 = r16;	 Catch:{ all -> 0x019b }
            r3.<init>(r10, r0, r15, r4);	 Catch:{ all -> 0x019b }
            r0 = r22;	 Catch:{ all -> 0x019b }
            r0.mGenerationTracker = r3;	 Catch:{ all -> 0x019b }
        L_0x00df:
            r3 = "android_id";	 Catch:{ all -> 0x019b }
            r0 = r24;	 Catch:{ all -> 0x019b }
            r3 = r3.equals(r0);	 Catch:{ all -> 0x019b }
            if (r3 != 0) goto L_0x00f5;	 Catch:{ all -> 0x019b }
        L_0x00ea:
            r3 = "force_resizable_activities";	 Catch:{ all -> 0x019b }
            r0 = r24;	 Catch:{ all -> 0x019b }
            r3 = r3.equals(r0);	 Catch:{ all -> 0x019b }
            if (r3 == 0) goto L_0x017e;	 Catch:{ all -> 0x019b }
        L_0x00f5:
            r0 = r22;	 Catch:{ all -> 0x019b }
            r3 = r0.mValues;	 Catch:{ all -> 0x019b }
            r0 = r24;	 Catch:{ all -> 0x019b }
            r1 = r19;	 Catch:{ all -> 0x019b }
            r3.put(r0, r1);	 Catch:{ all -> 0x019b }
        L_0x0100:
            monitor-exit(r22);	 Catch:{ RemoteException -> 0x012a }
        L_0x0101:
            return r19;
        L_0x0102:
            r17 = 0;
            goto L_0x000a;
        L_0x0106:
            r0 = r22;	 Catch:{ all -> 0x0120 }
            r3 = r0.mValues;	 Catch:{ all -> 0x0120 }
            r0 = r24;	 Catch:{ all -> 0x0120 }
            r3 = r3.containsKey(r0);	 Catch:{ all -> 0x0120 }
            if (r3 == 0) goto L_0x0025;	 Catch:{ all -> 0x0120 }
        L_0x0112:
            r0 = r22;	 Catch:{ all -> 0x0120 }
            r3 = r0.mValues;	 Catch:{ all -> 0x0120 }
            r0 = r24;	 Catch:{ all -> 0x0120 }
            r3 = r3.get(r0);	 Catch:{ all -> 0x0120 }
            r3 = (java.lang.String) r3;	 Catch:{ all -> 0x0120 }
            monitor-exit(r22);
            return r3;
        L_0x0120:
            r3 = move-exception;
            monitor-exit(r22);
            throw r3;
        L_0x0123:
            r8 = r9;
            goto L_0x006f;
        L_0x0126:
            r3 = move-exception;
            r8 = r9;
        L_0x0128:
            monitor-exit(r22);	 Catch:{ RemoteException -> 0x012a }
            throw r3;	 Catch:{ RemoteException -> 0x012a }
        L_0x012a:
            r14 = move-exception;
        L_0x012b:
            r12 = 0;
            r3 = "name=?";	 Catch:{ RemoteException -> 0x01e6 }
            r4 = 1;	 Catch:{ RemoteException -> 0x01e6 }
            r4 = new java.lang.String[r4];	 Catch:{ RemoteException -> 0x01e6 }
            r5 = 0;	 Catch:{ RemoteException -> 0x01e6 }
            r4[r5] = r24;	 Catch:{ RemoteException -> 0x01e6 }
            r5 = 0;	 Catch:{ RemoteException -> 0x01e6 }
            r6 = android.content.ContentResolver.createSqlQueryBundle(r3, r4, r5);	 Catch:{ RemoteException -> 0x01e6 }
            r3 = android.provider.Settings.isInSystemServer();	 Catch:{ RemoteException -> 0x01e6 }
            if (r3 == 0) goto L_0x0219;	 Catch:{ RemoteException -> 0x01e6 }
        L_0x0140:
            r3 = android.os.Binder.getCallingUid();	 Catch:{ RemoteException -> 0x01e6 }
            r4 = android.os.Process.myUid();	 Catch:{ RemoteException -> 0x01e6 }
            if (r3 == r4) goto L_0x0219;	 Catch:{ RemoteException -> 0x01e6 }
        L_0x014a:
            r20 = android.os.Binder.clearCallingIdentity();	 Catch:{ RemoteException -> 0x01e6 }
            r3 = r23.getPackageName();	 Catch:{ all -> 0x01e1 }
            r0 = r22;	 Catch:{ all -> 0x01e1 }
            r4 = r0.mUri;	 Catch:{ all -> 0x01e1 }
            r5 = SELECT_VALUE_PROJECTION;	 Catch:{ all -> 0x01e1 }
            r7 = 0;	 Catch:{ all -> 0x01e1 }
            r12 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ all -> 0x01e1 }
            android.os.Binder.restoreCallingIdentity(r20);	 Catch:{ RemoteException -> 0x01e6 }
        L_0x0160:
            if (r12 != 0) goto L_0x022a;
        L_0x0162:
            r3 = 0;
            if (r12 == 0) goto L_0x0168;
        L_0x0165:
            r12.close();
        L_0x0168:
            return r3;
        L_0x0169:
            r3 = move-exception;
            android.os.Binder.restoreCallingIdentity(r20);	 Catch:{ RemoteException -> 0x012a }
            throw r3;	 Catch:{ RemoteException -> 0x012a }
        L_0x016e:
            r3 = r23.getPackageName();	 Catch:{ RemoteException -> 0x012a }
            r0 = r22;	 Catch:{ RemoteException -> 0x012a }
            r4 = r0.mCallGetCommand;	 Catch:{ RemoteException -> 0x012a }
            r0 = r24;	 Catch:{ RemoteException -> 0x012a }
            r11 = r2.call(r3, r4, r0, r8);	 Catch:{ RemoteException -> 0x012a }
            goto L_0x0095;
        L_0x017e:
            r0 = r22;	 Catch:{ all -> 0x019b }
            r3 = r0.mGenerationTracker;	 Catch:{ all -> 0x019b }
            if (r3 == 0) goto L_0x019e;	 Catch:{ all -> 0x019b }
        L_0x0184:
            r0 = r22;	 Catch:{ all -> 0x019b }
            r3 = r0.mGenerationTracker;	 Catch:{ all -> 0x019b }
            r3 = r3.getCurrentGeneration();	 Catch:{ all -> 0x019b }
            if (r13 != r3) goto L_0x019e;	 Catch:{ all -> 0x019b }
        L_0x018e:
            r0 = r22;	 Catch:{ all -> 0x019b }
            r3 = r0.mValues;	 Catch:{ all -> 0x019b }
            r0 = r24;	 Catch:{ all -> 0x019b }
            r1 = r19;	 Catch:{ all -> 0x019b }
            r3.put(r0, r1);	 Catch:{ all -> 0x019b }
            goto L_0x0100;
        L_0x019b:
            r3 = move-exception;
            monitor-exit(r22);	 Catch:{ RemoteException -> 0x012a }
            throw r3;	 Catch:{ RemoteException -> 0x012a }
        L_0x019e:
            r3 = "Settings";	 Catch:{ all -> 0x019b }
            r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x019b }
            r4.<init>();	 Catch:{ all -> 0x019b }
            r5 = "mValues not put! needsGenerationTracker: ";	 Catch:{ all -> 0x019b }
            r4 = r4.append(r5);	 Catch:{ all -> 0x019b }
            r0 = r18;	 Catch:{ all -> 0x019b }
            r4 = r4.append(r0);	 Catch:{ all -> 0x019b }
            r5 = " currentGeneration: ";	 Catch:{ all -> 0x019b }
            r4 = r4.append(r5);	 Catch:{ all -> 0x019b }
            r4 = r4.append(r13);	 Catch:{ all -> 0x019b }
            r5 = " name: ";	 Catch:{ all -> 0x019b }
            r4 = r4.append(r5);	 Catch:{ all -> 0x019b }
            r0 = r24;	 Catch:{ all -> 0x019b }
            r4 = r4.append(r0);	 Catch:{ all -> 0x019b }
            r5 = " value: ";	 Catch:{ all -> 0x019b }
            r4 = r4.append(r5);	 Catch:{ all -> 0x019b }
            r0 = r19;	 Catch:{ all -> 0x019b }
            r4 = r4.append(r0);	 Catch:{ all -> 0x019b }
            r4 = r4.toString();	 Catch:{ all -> 0x019b }
            android.util.Log.w(r3, r4);	 Catch:{ all -> 0x019b }
            goto L_0x0100;
        L_0x01e1:
            r3 = move-exception;
            android.os.Binder.restoreCallingIdentity(r20);	 Catch:{ RemoteException -> 0x01e6 }
            throw r3;	 Catch:{ RemoteException -> 0x01e6 }
        L_0x01e6:
            r14 = move-exception;
            r3 = "Settings";	 Catch:{ all -> 0x025e }
            r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x025e }
            r4.<init>();	 Catch:{ all -> 0x025e }
            r5 = "Can't get key ";	 Catch:{ all -> 0x025e }
            r4 = r4.append(r5);	 Catch:{ all -> 0x025e }
            r0 = r24;	 Catch:{ all -> 0x025e }
            r4 = r4.append(r0);	 Catch:{ all -> 0x025e }
            r5 = " from ";	 Catch:{ all -> 0x025e }
            r4 = r4.append(r5);	 Catch:{ all -> 0x025e }
            r0 = r22;	 Catch:{ all -> 0x025e }
            r5 = r0.mUri;	 Catch:{ all -> 0x025e }
            r4 = r4.append(r5);	 Catch:{ all -> 0x025e }
            r4 = r4.toString();	 Catch:{ all -> 0x025e }
            android.util.Log.w(r3, r4, r14);	 Catch:{ all -> 0x025e }
            r3 = 0;
            if (r12 == 0) goto L_0x0218;
        L_0x0215:
            r12.close();
        L_0x0218:
            return r3;
        L_0x0219:
            r3 = r23.getPackageName();	 Catch:{ RemoteException -> 0x01e6 }
            r0 = r22;	 Catch:{ RemoteException -> 0x01e6 }
            r4 = r0.mUri;	 Catch:{ RemoteException -> 0x01e6 }
            r5 = SELECT_VALUE_PROJECTION;	 Catch:{ RemoteException -> 0x01e6 }
            r7 = 0;	 Catch:{ RemoteException -> 0x01e6 }
            r12 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ RemoteException -> 0x01e6 }
            goto L_0x0160;	 Catch:{ RemoteException -> 0x01e6 }
        L_0x022a:
            r3 = r12.moveToNext();	 Catch:{ RemoteException -> 0x01e6 }
            if (r3 == 0) goto L_0x0258;	 Catch:{ RemoteException -> 0x01e6 }
        L_0x0230:
            r3 = 0;	 Catch:{ RemoteException -> 0x01e6 }
            r19 = r12.getString(r3);	 Catch:{ RemoteException -> 0x01e6 }
        L_0x0235:
            monitor-enter(r22);	 Catch:{ RemoteException -> 0x01e6 }
            r0 = r22;	 Catch:{ all -> 0x025b }
            r3 = r0.mGenerationTracker;	 Catch:{ all -> 0x025b }
            if (r3 == 0) goto L_0x0251;	 Catch:{ all -> 0x025b }
        L_0x023c:
            r0 = r22;	 Catch:{ all -> 0x025b }
            r3 = r0.mGenerationTracker;	 Catch:{ all -> 0x025b }
            r3 = r3.getCurrentGeneration();	 Catch:{ all -> 0x025b }
            if (r13 != r3) goto L_0x0251;	 Catch:{ all -> 0x025b }
        L_0x0246:
            r0 = r22;	 Catch:{ all -> 0x025b }
            r3 = r0.mValues;	 Catch:{ all -> 0x025b }
            r0 = r24;	 Catch:{ all -> 0x025b }
            r1 = r19;	 Catch:{ all -> 0x025b }
            r3.put(r0, r1);	 Catch:{ all -> 0x025b }
        L_0x0251:
            monitor-exit(r22);	 Catch:{ RemoteException -> 0x01e6 }
            if (r12 == 0) goto L_0x0257;
        L_0x0254:
            r12.close();
        L_0x0257:
            return r19;
        L_0x0258:
            r19 = 0;
            goto L_0x0235;
        L_0x025b:
            r3 = move-exception;
            monitor-exit(r22);	 Catch:{ RemoteException -> 0x01e6 }
            throw r3;	 Catch:{ RemoteException -> 0x01e6 }
        L_0x025e:
            r3 = move-exception;
            if (r12 == 0) goto L_0x0264;
        L_0x0261:
            r12.close();
        L_0x0264:
            throw r3;
        L_0x0265:
            r14 = move-exception;
            r8 = r9;
            goto L_0x012b;
        L_0x0269:
            r3 = move-exception;
            goto L_0x0128;
        L_0x026c:
            r8 = r9;
            goto L_0x0068;
        L_0x026f:
            r9 = r8;
            goto L_0x0054;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Settings.NameValueCache.getStringForUser(android.content.ContentResolver, java.lang.String, int):java.lang.String");
        }

        /* synthetic */ void lambda$-android_provider_Settings$NameValueCache_76353() {
            synchronized (this) {
                Log.e(Settings.TAG, "Error accessing generation tracker - removing");
                if (this.mGenerationTracker != null) {
                    GenerationTracker generationTracker = this.mGenerationTracker;
                    this.mGenerationTracker = null;
                    generationTracker.destroy();
                    this.mValues.clear();
                }
            }
        }
    }

    public static final class Secure extends NameValueTable {
        public static final String ACCESSIBILITY_AUTOCLICK_DELAY = "accessibility_autoclick_delay";
        public static final String ACCESSIBILITY_AUTOCLICK_ENABLED = "accessibility_autoclick_enabled";
        public static final String ACCESSIBILITY_BUTTON_TARGET_COMPONENT = "accessibility_button_target_component";
        public static final String ACCESSIBILITY_CAPTIONING_BACKGROUND_COLOR = "accessibility_captioning_background_color";
        public static final String ACCESSIBILITY_CAPTIONING_EDGE_COLOR = "accessibility_captioning_edge_color";
        public static final String ACCESSIBILITY_CAPTIONING_EDGE_TYPE = "accessibility_captioning_edge_type";
        public static final String ACCESSIBILITY_CAPTIONING_ENABLED = "accessibility_captioning_enabled";
        public static final String ACCESSIBILITY_CAPTIONING_FONT_SCALE = "accessibility_captioning_font_scale";
        public static final String ACCESSIBILITY_CAPTIONING_FOREGROUND_COLOR = "accessibility_captioning_foreground_color";
        public static final String ACCESSIBILITY_CAPTIONING_LOCALE = "accessibility_captioning_locale";
        public static final String ACCESSIBILITY_CAPTIONING_PRESET = "accessibility_captioning_preset";
        public static final String ACCESSIBILITY_CAPTIONING_TYPEFACE = "accessibility_captioning_typeface";
        public static final String ACCESSIBILITY_CAPTIONING_WINDOW_COLOR = "accessibility_captioning_window_color";
        public static final String ACCESSIBILITY_DISPLAY_DALTONIZER = "accessibility_display_daltonizer";
        public static final String ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED = "accessibility_display_daltonizer_enabled";
        public static final String ACCESSIBILITY_DISPLAY_INVERSION_ENABLED = "accessibility_display_inversion_enabled";
        @Deprecated
        public static final String ACCESSIBILITY_DISPLAY_MAGNIFICATION_AUTO_UPDATE = "accessibility_display_magnification_auto_update";
        public static final String ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED = "accessibility_display_magnification_enabled";
        public static final String ACCESSIBILITY_DISPLAY_MAGNIFICATION_NAVBAR_ENABLED = "accessibility_display_magnification_navbar_enabled";
        public static final String ACCESSIBILITY_DISPLAY_MAGNIFICATION_SCALE = "accessibility_display_magnification_scale";
        public static final String ACCESSIBILITY_ENABLED = "accessibility_enabled";
        public static final String ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED = "high_text_contrast_enabled";
        public static final String ACCESSIBILITY_LARGE_POINTER_ICON = "accessibility_large_pointer_icon";
        public static final String ACCESSIBILITY_SHORTCUT_DIALOG_SHOWN = "accessibility_shortcut_dialog_shown";
        public static final String ACCESSIBILITY_SHORTCUT_ENABLED = "accessibility_shortcut_enabled";
        public static final String ACCESSIBILITY_SHORTCUT_ON_LOCK_SCREEN = "accessibility_shortcut_on_lock_screen";
        public static final String ACCESSIBILITY_SHORTCUT_TARGET_SERVICE = "accessibility_shortcut_target_service";
        public static final String ACCESSIBILITY_SOFT_KEYBOARD_MODE = "accessibility_soft_keyboard_mode";
        @Deprecated
        public static final String ACCESSIBILITY_SPEAK_PASSWORD = "speak_password";
        @Deprecated
        public static final String ADB_ENABLED = "adb_enabled";
        public static final String ALLOWED_GEOLOCATION_ORIGINS = "allowed_geolocation_origins";
        @Deprecated
        public static final String ALLOW_MOCK_LOCATION = "mock_location";
        public static final String ALWAYS_ON_VPN_APP = "always_on_vpn_app";
        public static final String ALWAYS_ON_VPN_LOCKDOWN = "always_on_vpn_lockdown";
        public static final String ANDROID_ID = "android_id";
        public static final String ANR_SHOW_BACKGROUND = "anr_show_background";
        public static final String ASSISTANT = "assistant";
        public static final String ASSIST_DISCLOSURE_ENABLED = "assist_disclosure_enabled";
        public static final String ASSIST_GESTURE_ENABLED = "assist_gesture_enabled";
        public static final String ASSIST_GESTURE_SENSITIVITY = "assist_gesture_sensitivity";
        public static final String ASSIST_SCREENSHOT_ENABLED = "assist_screenshot_enabled";
        public static final String ASSIST_STRUCTURE_ENABLED = "assist_structure_enabled";
        public static final String AUTOFILL_SERVICE = "autofill_service";
        public static final String AUTOFILL_SERVICE_SEARCH_URI = "autofill_service_search_uri";
        public static final String AUTOMATIC_STORAGE_MANAGER_BYTES_CLEARED = "automatic_storage_manager_bytes_cleared";
        public static final String AUTOMATIC_STORAGE_MANAGER_DAYS_TO_RETAIN = "automatic_storage_manager_days_to_retain";
        public static final int AUTOMATIC_STORAGE_MANAGER_DAYS_TO_RETAIN_DEFAULT = 90;
        public static final String AUTOMATIC_STORAGE_MANAGER_ENABLED = "automatic_storage_manager_enabled";
        public static final String AUTOMATIC_STORAGE_MANAGER_LAST_RUN = "automatic_storage_manager_last_run";
        @Deprecated
        public static final String BACKGROUND_DATA = "background_data";
        public static final String BACKUP_AUTO_RESTORE = "backup_auto_restore";
        public static final String BACKUP_ENABLED = "backup_enabled";
        public static final String BACKUP_PROVISIONED = "backup_provisioned";
        public static final String BACKUP_TRANSPORT = "backup_transport";
        @Deprecated
        public static final String BLUETOOTH_ON = "bluetooth_on";
        @Deprecated
        public static final String BUGREPORT_IN_POWER_MENU = "bugreport_in_power_menu";
        public static final String CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED = "camera_double_tap_power_gesture_disabled";
        public static final String CAMERA_DOUBLE_TWIST_TO_FLIP_ENABLED = "camera_double_twist_to_flip_enabled";
        public static final String CAMERA_GESTURE_DISABLED = "camera_gesture_disabled";
        public static final String CARRIER_APPS_HANDLED = "carrier_apps_handled";
        public static final String CHANGE_RINGER_MODE_PKGS = "change_ringer_mode_pkgs";
        private static final Set<String> CLONE_TO_MANAGED_PROFILE = new ArraySet();
        public static final String CMAS_ADDITIONAL_BROADCAST_PKG = "cmas_additional_broadcast_pkg";
        public static final String COMPLETED_CATEGORY_PREFIX = "suggested.completed_category.";
        public static final String CONNECTIVITY_RELEASE_PENDING_INTENT_DELAY_MS = "connectivity_release_pending_intent_delay_ms";
        public static final Uri CONTENT_URI = Uri.parse("content://settings/secure");
        @Deprecated
        public static final String DATA_ROAMING = "data_roaming";
        public static final String DEFAULT_INPUT_METHOD = "default_input_method";
        public static final String DEMO_USER_SETUP_COMPLETE = "demo_user_setup_complete";
        @Deprecated
        public static final String DEVELOPMENT_SETTINGS_ENABLED = "development_settings_enabled";
        public static final String DEVICE_PAIRED = "device_paired";
        @Deprecated
        public static final String DEVICE_PROVISIONED = "device_provisioned";
        public static final String DIALER_DEFAULT_APPLICATION = "dialer_default_application";
        public static final String DISABLED_PRINT_SERVICES = "disabled_print_services";
        public static final String DISABLED_SYSTEM_INPUT_METHODS = "disabled_system_input_methods";
        public static final String DISPLAY_DENSITY_FORCED = "display_density_forced";
        public static final String DOUBLE_TAP_TO_WAKE = "double_tap_to_wake";
        public static final String DOZE_ALWAYS_ON = "doze_always_on";
        public static final String DOZE_ENABLED = "doze_enabled";
        public static final String DOZE_PULSE_ON_DOUBLE_TAP = "doze_pulse_on_double_tap";
        public static final String DOZE_PULSE_ON_PICK_UP = "doze_pulse_on_pick_up";
        public static final String EMERGENCY_ASSISTANCE_APPLICATION = "emergency_assistance_application";
        public static final String ENABLED_ACCESSIBILITY_SERVICES = "enabled_accessibility_services";
        public static final String ENABLED_INPUT_METHODS = "enabled_input_methods";
        public static final String ENABLED_NOTIFICATION_ASSISTANT = "enabled_notification_assistant";
        public static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
        public static final String ENABLED_NOTIFICATION_POLICY_ACCESS_PACKAGES = "enabled_notification_policy_access_packages";
        public static final String ENABLED_PRINT_SERVICES = "enabled_print_services";
        public static final String ENABLED_VR_LISTENERS = "enabled_vr_listeners";
        public static final String ENHANCED_VOICE_PRIVACY_ENABLED = "enhanced_voice_privacy_enabled";
        @Deprecated
        public static final String HTTP_PROXY = "http_proxy";
        public static final String IMMERSIVE_MODE_CONFIRMATIONS = "immersive_mode_confirmations";
        public static final String INCALL_BACK_BUTTON_BEHAVIOR = "incall_back_button_behavior";
        public static final int INCALL_BACK_BUTTON_BEHAVIOR_DEFAULT = 0;
        public static final int INCALL_BACK_BUTTON_BEHAVIOR_HANGUP = 1;
        public static final int INCALL_BACK_BUTTON_BEHAVIOR_NONE = 0;
        public static final String INCALL_POWER_BUTTON_BEHAVIOR = "incall_power_button_behavior";
        public static final int INCALL_POWER_BUTTON_BEHAVIOR_DEFAULT = 1;
        public static final int INCALL_POWER_BUTTON_BEHAVIOR_HANGUP = 2;
        public static final int INCALL_POWER_BUTTON_BEHAVIOR_SCREEN_OFF = 1;
        public static final String INPUT_METHODS_SUBTYPE_HISTORY = "input_methods_subtype_history";
        public static final String INPUT_METHOD_SELECTOR_VISIBILITY = "input_method_selector_visibility";
        public static final String INSTALL_NON_MARKET_APPS = "install_non_market_apps";
        public static final String INSTANT_APPS_ENABLED = "instant_apps_enabled";
        public static final Set<String> INSTANT_APP_SETTINGS = new ArraySet();
        public static final String KEY_CONTENT_SUITESTATE = "suitestate";
        public static final String LAST_SETUP_SHOWN = "last_setup_shown";
        public static final String LOCATION_MODE = "location_mode";
        public static final int LOCATION_MODE_BATTERY_SAVING = 2;
        public static final int LOCATION_MODE_HIGH_ACCURACY = 3;
        public static final int LOCATION_MODE_OFF = 0;
        public static final int LOCATION_MODE_PREVIOUS = -1;
        public static final int LOCATION_MODE_SENSORS_ONLY = 1;
        public static final String LOCATION_PREVIOUS_MODE = "location_previous_mode";
        @Deprecated
        public static final String LOCATION_PROVIDERS_ALLOWED = "location_providers_allowed";
        @Deprecated
        public static final String LOCK_BIOMETRIC_WEAK_FLAGS = "lock_biometric_weak_flags";
        @Deprecated
        public static final String LOCK_PATTERN_ENABLED = "lock_pattern_autolock";
        @Deprecated
        public static final String LOCK_PATTERN_TACTILE_FEEDBACK_ENABLED = "lock_pattern_tactile_feedback_enabled";
        @Deprecated
        public static final String LOCK_PATTERN_VISIBLE = "lock_pattern_visible_pattern";
        public static final String LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS = "lock_screen_allow_private_notifications";
        public static final String LOCK_SCREEN_ALLOW_REMOTE_INPUT = "lock_screen_allow_remote_input";
        @Deprecated
        public static final String LOCK_SCREEN_APPWIDGET_IDS = "lock_screen_appwidget_ids";
        @Deprecated
        public static final String LOCK_SCREEN_FALLBACK_APPWIDGET_ID = "lock_screen_fallback_appwidget_id";
        public static final String LOCK_SCREEN_LOCK_AFTER_TIMEOUT = "lock_screen_lock_after_timeout";
        @Deprecated
        public static final String LOCK_SCREEN_OWNER_INFO = "lock_screen_owner_info";
        @Deprecated
        public static final String LOCK_SCREEN_OWNER_INFO_ENABLED = "lock_screen_owner_info_enabled";
        public static final String LOCK_SCREEN_SHOW_NOTIFICATIONS = "lock_screen_show_notifications";
        @Deprecated
        public static final String LOCK_SCREEN_STICKY_APPWIDGET = "lock_screen_sticky_appwidget";
        public static final String LOCK_TO_APP_EXIT_LOCKED = "lock_to_app_exit_locked";
        @Deprecated
        public static final String LOGGING_ID = "logging_id";
        public static final String LONG_PRESS_TIMEOUT = "long_press_timeout";
        public static final String MANAGED_PROFILE_CONTACT_REMOTE_SEARCH = "managed_profile_contact_remote_search";
        public static final String MOUNT_PLAY_NOTIFICATION_SND = "mount_play_not_snd";
        public static final String MOUNT_UMS_AUTOSTART = "mount_ums_autostart";
        public static final String MOUNT_UMS_NOTIFY_ENABLED = "mount_ums_notify_enabled";
        public static final String MOUNT_UMS_PROMPT = "mount_ums_prompt";
        private static final HashSet<String> MOVED_TO_GLOBAL = new HashSet();
        private static final HashSet<String> MOVED_TO_LOCK_SETTINGS = new HashSet(3);
        public static final String MULTI_PRESS_TIMEOUT = "multi_press_timeout";
        @Deprecated
        public static final String NETWORK_PREFERENCE = "network_preference";
        public static final String NFC_PAYMENT_DEFAULT_COMPONENT = "nfc_payment_default_component";
        public static final String NFC_PAYMENT_FOREGROUND = "nfc_payment_foreground";
        public static final String NIGHT_DISPLAY_ACTIVATED = "night_display_activated";
        public static final String NIGHT_DISPLAY_AUTO_MODE = "night_display_auto_mode";
        public static final String NIGHT_DISPLAY_COLOR_TEMPERATURE = "night_display_color_temperature";
        public static final String NIGHT_DISPLAY_CUSTOM_END_TIME = "night_display_custom_end_time";
        public static final String NIGHT_DISPLAY_CUSTOM_START_TIME = "night_display_custom_start_time";
        public static final String NIGHT_DISPLAY_LAST_ACTIVATED_TIME = "night_display_last_activated_time";
        public static final String NOTIFICATION_BADGING = "notification_badging";
        public static final String OVERVIEW_LAST_STACK_ACTIVE_TIME = "overview_last_stack_active_time";
        public static final String PACKAGE_VERIFIER_STATE = "package_verifier_state";
        public static final String PACKAGE_VERIFIER_USER_CONSENT = "package_verifier_user_consent";
        public static final String PARENTAL_CONTROL_ENABLED = "parental_control_enabled";
        public static final String PARENTAL_CONTROL_LAST_UPDATE = "parental_control_last_update";
        public static final String PARENTAL_CONTROL_REDIRECT_URL = "parental_control_redirect_url";
        public static final String PAYMENT_SERVICE_SEARCH_URI = "payment_service_search_uri";
        public static final String PREFERRED_TTY_MODE = "preferred_tty_mode";
        public static final String PRINT_SERVICE_SEARCH_URI = "print_service_search_uri";
        public static final String QS_TILES = "sysui_qs_tiles";
        public static final String SCREENSAVER_ACTIVATE_ON_DOCK = "screensaver_activate_on_dock";
        public static final String SCREENSAVER_ACTIVATE_ON_SLEEP = "screensaver_activate_on_sleep";
        public static final String SCREENSAVER_COMPONENTS = "screensaver_components";
        public static final String SCREENSAVER_DEFAULT_COMPONENT = "screensaver_default_component";
        public static final String SCREENSAVER_ENABLED = "screensaver_enabled";
        public static final String SEARCH_GLOBAL_SEARCH_ACTIVITY = "search_global_search_activity";
        public static final String SEARCH_MAX_RESULTS_PER_SOURCE = "search_max_results_per_source";
        public static final String SEARCH_MAX_RESULTS_TO_DISPLAY = "search_max_results_to_display";
        public static final String SEARCH_MAX_SHORTCUTS_RETURNED = "search_max_shortcuts_returned";
        public static final String SEARCH_MAX_SOURCE_EVENT_AGE_MILLIS = "search_max_source_event_age_millis";
        public static final String SEARCH_MAX_STAT_AGE_MILLIS = "search_max_stat_age_millis";
        public static final String SEARCH_MIN_CLICKS_FOR_SOURCE_RANKING = "search_min_clicks_for_source_ranking";
        public static final String SEARCH_MIN_IMPRESSIONS_FOR_SOURCE_RANKING = "search_min_impressions_for_source_ranking";
        public static final String SEARCH_NUM_PROMOTED_SOURCES = "search_num_promoted_sources";
        public static final String SEARCH_PER_SOURCE_CONCURRENT_QUERY_LIMIT = "search_per_source_concurrent_query_limit";
        public static final String SEARCH_PREFILL_MILLIS = "search_prefill_millis";
        public static final String SEARCH_PROMOTED_SOURCE_DEADLINE_MILLIS = "search_promoted_source_deadline_millis";
        public static final String SEARCH_QUERY_THREAD_CORE_POOL_SIZE = "search_query_thread_core_pool_size";
        public static final String SEARCH_QUERY_THREAD_MAX_POOL_SIZE = "search_query_thread_max_pool_size";
        public static final String SEARCH_SHORTCUT_REFRESH_CORE_POOL_SIZE = "search_shortcut_refresh_core_pool_size";
        public static final String SEARCH_SHORTCUT_REFRESH_MAX_POOL_SIZE = "search_shortcut_refresh_max_pool_size";
        public static final String SEARCH_SOURCE_TIMEOUT_MILLIS = "search_source_timeout_millis";
        public static final String SEARCH_THREAD_KEEPALIVE_SECONDS = "search_thread_keepalive_seconds";
        public static final String SEARCH_WEB_RESULTS_OVERRIDE_LIMIT = "search_web_results_override_limit";
        public static final String SECURE_KEYBOARD = "secure_keyboard";
        public static final String SELECTED_INPUT_METHOD_SUBTYPE = "selected_input_method_subtype";
        public static final String SELECTED_SPELL_CHECKER = "selected_spell_checker";
        public static final String SELECTED_SPELL_CHECKER_SUBTYPE = "selected_spell_checker_subtype";
        public static final String SETTINGS_CLASSNAME = "settings_classname";
        public static final String[] SETTINGS_TO_BACKUP = new String[]{"bugreport_in_power_menu", ALLOW_MOCK_LOCATION, "parental_control_enabled", "parental_control_redirect_url", "usb_mass_storage_enabled", ACCESSIBILITY_DISPLAY_INVERSION_ENABLED, ACCESSIBILITY_DISPLAY_DALTONIZER, ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED, ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED, ACCESSIBILITY_DISPLAY_MAGNIFICATION_NAVBAR_ENABLED, AUTOFILL_SERVICE, ACCESSIBILITY_DISPLAY_MAGNIFICATION_SCALE, ENABLED_ACCESSIBILITY_SERVICES, ENABLED_NOTIFICATION_LISTENERS, ENABLED_VR_LISTENERS, ENABLED_INPUT_METHODS, TOUCH_EXPLORATION_GRANTED_ACCESSIBILITY_SERVICES, TOUCH_EXPLORATION_ENABLED, ACCESSIBILITY_ENABLED, ACCESSIBILITY_SHORTCUT_TARGET_SERVICE, ACCESSIBILITY_BUTTON_TARGET_COMPONENT, ACCESSIBILITY_SHORTCUT_DIALOG_SHOWN, ACCESSIBILITY_SHORTCUT_ENABLED, ACCESSIBILITY_SHORTCUT_ON_LOCK_SCREEN, ACCESSIBILITY_SPEAK_PASSWORD, ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED, ACCESSIBILITY_CAPTIONING_PRESET, ACCESSIBILITY_CAPTIONING_ENABLED, ACCESSIBILITY_CAPTIONING_LOCALE, ACCESSIBILITY_CAPTIONING_BACKGROUND_COLOR, ACCESSIBILITY_CAPTIONING_FOREGROUND_COLOR, ACCESSIBILITY_CAPTIONING_EDGE_TYPE, ACCESSIBILITY_CAPTIONING_EDGE_COLOR, ACCESSIBILITY_CAPTIONING_TYPEFACE, ACCESSIBILITY_CAPTIONING_FONT_SCALE, ACCESSIBILITY_CAPTIONING_WINDOW_COLOR, TTS_USE_DEFAULTS, TTS_DEFAULT_RATE, TTS_DEFAULT_PITCH, TTS_DEFAULT_SYNTH, TTS_DEFAULT_LANG, TTS_DEFAULT_COUNTRY, TTS_ENABLED_PLUGINS, TTS_DEFAULT_LOCALE, SHOW_IME_WITH_HARD_KEYBOARD, "wifi_networks_available_notification_on", "wifi_networks_available_repeat_delay", "wifi_num_open_networks_kept", SELECTED_SPELL_CHECKER, SELECTED_SPELL_CHECKER_SUBTYPE, SPELL_CHECKER_ENABLED, MOUNT_PLAY_NOTIFICATION_SND, MOUNT_UMS_AUTOSTART, MOUNT_UMS_PROMPT, MOUNT_UMS_NOTIFY_ENABLED, SLEEP_TIMEOUT, DOUBLE_TAP_TO_WAKE, WAKE_GESTURE_ENABLED, LONG_PRESS_TIMEOUT, CAMERA_GESTURE_DISABLED, ACCESSIBILITY_AUTOCLICK_ENABLED, ACCESSIBILITY_AUTOCLICK_DELAY, ACCESSIBILITY_LARGE_POINTER_ICON, PREFERRED_TTY_MODE, ENHANCED_VOICE_PRIVACY_ENABLED, TTY_MODE_ENABLED, INCALL_POWER_BUTTON_BEHAVIOR, NIGHT_DISPLAY_CUSTOM_START_TIME, NIGHT_DISPLAY_CUSTOM_END_TIME, NIGHT_DISPLAY_COLOR_TEMPERATURE, NIGHT_DISPLAY_AUTO_MODE, NIGHT_DISPLAY_LAST_ACTIVATED_TIME, NIGHT_DISPLAY_ACTIVATED, SYNC_PARENT_SOUNDS, CAMERA_DOUBLE_TWIST_TO_FLIP_ENABLED, CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, SYSTEM_NAVIGATION_KEYS_ENABLED, QS_TILES, DOZE_ENABLED, DOZE_PULSE_ON_PICK_UP, DOZE_PULSE_ON_DOUBLE_TAP, NFC_PAYMENT_DEFAULT_COMPONENT, AUTOMATIC_STORAGE_MANAGER_DAYS_TO_RETAIN, ASSIST_GESTURE_ENABLED, ASSIST_GESTURE_SENSITIVITY, VR_DISPLAY_MODE, NOTIFICATION_BADGING};
        public static final String SHOW_IME_WITH_HARD_KEYBOARD = "show_ime_with_hard_keyboard";
        public static final int SHOW_MODE_AUTO = 0;
        public static final int SHOW_MODE_HIDDEN = 1;
        public static final String SHOW_NOTE_ABOUT_NOTIFICATION_HIDING = "show_note_about_notification_hiding";
        public static final String SKIP_FIRST_USE_HINTS = "skip_first_use_hints";
        public static final String SLEEP_TIMEOUT = "sleep_timeout";
        public static final String SMS_DEFAULT_APPLICATION = "sms_default_application";
        public static final String SPELL_CHECKER_ENABLED = "spell_checker_enabled";
        public static final String SYNC_PARENT_SOUNDS = "sync_parent_sounds";
        public static final String SYSTEM_NAVIGATION_KEYS_ENABLED = "system_navigation_keys_enabled";
        public static final String TOUCH_EXPLORATION_ENABLED = "touch_exploration_enabled";
        public static final String TOUCH_EXPLORATION_GRANTED_ACCESSIBILITY_SERVICES = "touch_exploration_granted_accessibility_services";
        public static final String TRUST_AGENTS_INITIALIZED = "trust_agents_initialized";
        @Deprecated
        public static final String TTS_DEFAULT_COUNTRY = "tts_default_country";
        @Deprecated
        public static final String TTS_DEFAULT_LANG = "tts_default_lang";
        public static final String TTS_DEFAULT_LOCALE = "tts_default_locale";
        public static final String TTS_DEFAULT_PITCH = "tts_default_pitch";
        public static final String TTS_DEFAULT_RATE = "tts_default_rate";
        public static final String TTS_DEFAULT_SYNTH = "tts_default_synth";
        @Deprecated
        public static final String TTS_DEFAULT_VARIANT = "tts_default_variant";
        public static final String TTS_ENABLED_PLUGINS = "tts_enabled_plugins";
        @Deprecated
        public static final String TTS_USE_DEFAULTS = "tts_use_defaults";
        public static final String TTY_MODE_ENABLED = "tty_mode_enabled";
        public static final String TV_INPUT_CUSTOM_LABELS = "tv_input_custom_labels";
        public static final String TV_INPUT_HIDDEN_INPUTS = "tv_input_hidden_inputs";
        public static final String TV_USER_SETUP_COMPLETE = "tv_user_setup_complete";
        public static final String UI_NIGHT_MODE = "ui_night_mode";
        public static final String UNKNOWN_SOURCES_DEFAULT_REVERSED = "unknown_sources_default_reversed";
        public static final String UNSAFE_VOLUME_MUSIC_ACTIVE_MS = "unsafe_volume_music_active_ms";
        public static final String USB_AUDIO_AUTOMATIC_ROUTING_DISABLED = "usb_audio_automatic_routing_disabled";
        @Deprecated
        public static final String USB_MASS_STORAGE_ENABLED = "usb_mass_storage_enabled";
        public static final String USER_SETUP_COMPLETE = "user_setup_complete";
        @Deprecated
        public static final String USE_GOOGLE_MAIL = "use_google_mail";
        public static final String VOICE_INTERACTION_SERVICE = "voice_interaction_service";
        public static final String VOICE_RECOGNITION_SERVICE = "voice_recognition_service";
        public static final String VR_DISPLAY_MODE = "vr_display_mode";
        public static final int VR_DISPLAY_MODE_LOW_PERSISTENCE = 0;
        public static final int VR_DISPLAY_MODE_OFF = 1;
        public static final String WAKE_GESTURE_ENABLED = "wake_gesture_enabled";
        public static final String WIFI_AP_CHANNEL = "wifi_ap_channel";
        public static final String WIFI_AP_MAXSCB = "wifi_ap_maxscb";
        @Deprecated
        public static final String WIFI_IDLE_MS = "wifi_idle_ms";
        @Deprecated
        public static final String WIFI_MAX_DHCP_RETRY_COUNT = "wifi_max_dhcp_retry_count";
        @Deprecated
        public static final String WIFI_MOBILE_DATA_TRANSITION_WAKELOCK_TIMEOUT_MS = "wifi_mobile_data_transition_wakelock_timeout_ms";
        @Deprecated
        public static final String WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON = "wifi_networks_available_notification_on";
        @Deprecated
        public static final String WIFI_NETWORKS_AVAILABLE_REPEAT_DELAY = "wifi_networks_available_repeat_delay";
        @Deprecated
        public static final String WIFI_NUM_OPEN_NETWORKS_KEPT = "wifi_num_open_networks_kept";
        @Deprecated
        public static final String WIFI_ON = "wifi_on";
        @Deprecated
        public static final String WIFI_WATCHDOG_ACCEPTABLE_PACKET_LOSS_PERCENTAGE = "wifi_watchdog_acceptable_packet_loss_percentage";
        @Deprecated
        public static final String WIFI_WATCHDOG_AP_COUNT = "wifi_watchdog_ap_count";
        @Deprecated
        public static final String WIFI_WATCHDOG_BACKGROUND_CHECK_DELAY_MS = "wifi_watchdog_background_check_delay_ms";
        @Deprecated
        public static final String WIFI_WATCHDOG_BACKGROUND_CHECK_ENABLED = "wifi_watchdog_background_check_enabled";
        @Deprecated
        public static final String WIFI_WATCHDOG_BACKGROUND_CHECK_TIMEOUT_MS = "wifi_watchdog_background_check_timeout_ms";
        @Deprecated
        public static final String WIFI_WATCHDOG_INITIAL_IGNORED_PING_COUNT = "wifi_watchdog_initial_ignored_ping_count";
        @Deprecated
        public static final String WIFI_WATCHDOG_MAX_AP_CHECKS = "wifi_watchdog_max_ap_checks";
        @Deprecated
        public static final String WIFI_WATCHDOG_ON = "wifi_watchdog_on";
        @Deprecated
        public static final String WIFI_WATCHDOG_PING_COUNT = "wifi_watchdog_ping_count";
        @Deprecated
        public static final String WIFI_WATCHDOG_PING_DELAY_MS = "wifi_watchdog_ping_delay_ms";
        @Deprecated
        public static final String WIFI_WATCHDOG_PING_TIMEOUT_MS = "wifi_watchdog_ping_timeout_ms";
        @Deprecated
        public static final String WIFI_WATCHDOG_WATCH_LIST = "wifi_watchdog_watch_list";
        private static boolean sIsSystemProcess;
        private static ILockSettings sLockSettings = null;
        private static final NameValueCache sNameValueCache = new NameValueCache(CONTENT_URI, Settings.CALL_METHOD_GET_SECURE, Settings.CALL_METHOD_PUT_SECURE, sProviderHolder);
        private static final ContentProviderHolder sProviderHolder = new ContentProviderHolder(CONTENT_URI);

        static {
            MOVED_TO_LOCK_SETTINGS.add("lock_pattern_autolock");
            MOVED_TO_LOCK_SETTINGS.add("lock_pattern_visible_pattern");
            MOVED_TO_LOCK_SETTINGS.add("lock_pattern_tactile_feedback_enabled");
            MOVED_TO_GLOBAL.add("adb_enabled");
            MOVED_TO_GLOBAL.add(Global.ASSISTED_GPS_ENABLED);
            MOVED_TO_GLOBAL.add("bluetooth_on");
            MOVED_TO_GLOBAL.add("bugreport_in_power_menu");
            MOVED_TO_GLOBAL.add(Global.CDMA_CELL_BROADCAST_SMS);
            MOVED_TO_GLOBAL.add(Global.CDMA_ROAMING_MODE);
            MOVED_TO_GLOBAL.add(Global.CDMA_SUBSCRIPTION_MODE);
            MOVED_TO_GLOBAL.add(Global.DATA_ACTIVITY_TIMEOUT_MOBILE);
            MOVED_TO_GLOBAL.add(Global.DATA_ACTIVITY_TIMEOUT_WIFI);
            MOVED_TO_GLOBAL.add("data_roaming");
            MOVED_TO_GLOBAL.add("development_settings_enabled");
            MOVED_TO_GLOBAL.add("device_provisioned");
            MOVED_TO_GLOBAL.add(Global.DISPLAY_SIZE_FORCED);
            MOVED_TO_GLOBAL.add(Global.DOWNLOAD_MAX_BYTES_OVER_MOBILE);
            MOVED_TO_GLOBAL.add(Global.DOWNLOAD_RECOMMENDED_MAX_BYTES_OVER_MOBILE);
            MOVED_TO_GLOBAL.add(Global.MOBILE_DATA);
            MOVED_TO_GLOBAL.add(Global.NETSTATS_DEV_BUCKET_DURATION);
            MOVED_TO_GLOBAL.add(Global.NETSTATS_DEV_DELETE_AGE);
            MOVED_TO_GLOBAL.add(Global.NETSTATS_DEV_PERSIST_BYTES);
            MOVED_TO_GLOBAL.add(Global.NETSTATS_DEV_ROTATE_AGE);
            MOVED_TO_GLOBAL.add(Global.NETSTATS_ENABLED);
            MOVED_TO_GLOBAL.add(Global.NETSTATS_GLOBAL_ALERT_BYTES);
            MOVED_TO_GLOBAL.add(Global.NETSTATS_POLL_INTERVAL);
            MOVED_TO_GLOBAL.add(Global.NETSTATS_SAMPLE_ENABLED);
            MOVED_TO_GLOBAL.add(Global.NETSTATS_TIME_CACHE_MAX_AGE);
            MOVED_TO_GLOBAL.add(Global.NETSTATS_UID_BUCKET_DURATION);
            MOVED_TO_GLOBAL.add(Global.NETSTATS_UID_DELETE_AGE);
            MOVED_TO_GLOBAL.add(Global.NETSTATS_UID_PERSIST_BYTES);
            MOVED_TO_GLOBAL.add(Global.NETSTATS_UID_ROTATE_AGE);
            MOVED_TO_GLOBAL.add(Global.NETSTATS_UID_TAG_BUCKET_DURATION);
            MOVED_TO_GLOBAL.add(Global.NETSTATS_UID_TAG_DELETE_AGE);
            MOVED_TO_GLOBAL.add(Global.NETSTATS_UID_TAG_PERSIST_BYTES);
            MOVED_TO_GLOBAL.add(Global.NETSTATS_UID_TAG_ROTATE_AGE);
            MOVED_TO_GLOBAL.add("network_preference");
            MOVED_TO_GLOBAL.add(Global.NITZ_UPDATE_DIFF);
            MOVED_TO_GLOBAL.add(Global.NITZ_UPDATE_SPACING);
            MOVED_TO_GLOBAL.add(Global.NTP_SERVER);
            MOVED_TO_GLOBAL.add(Global.NTP_TIMEOUT);
            MOVED_TO_GLOBAL.add(Global.PDP_WATCHDOG_ERROR_POLL_COUNT);
            MOVED_TO_GLOBAL.add(Global.PDP_WATCHDOG_LONG_POLL_INTERVAL_MS);
            MOVED_TO_GLOBAL.add(Global.PDP_WATCHDOG_MAX_PDP_RESET_FAIL_COUNT);
            MOVED_TO_GLOBAL.add(Global.PDP_WATCHDOG_POLL_INTERVAL_MS);
            MOVED_TO_GLOBAL.add(Global.PDP_WATCHDOG_TRIGGER_PACKET_COUNT);
            MOVED_TO_GLOBAL.add(Global.SAMPLING_PROFILER_MS);
            MOVED_TO_GLOBAL.add(Global.SETUP_PREPAID_DATA_SERVICE_URL);
            MOVED_TO_GLOBAL.add(Global.SETUP_PREPAID_DETECTION_REDIR_HOST);
            MOVED_TO_GLOBAL.add(Global.SETUP_PREPAID_DETECTION_TARGET_URL);
            MOVED_TO_GLOBAL.add(Global.TETHER_DUN_APN);
            MOVED_TO_GLOBAL.add(Global.TETHER_DUN_REQUIRED);
            MOVED_TO_GLOBAL.add(Global.TETHER_SUPPORTED);
            MOVED_TO_GLOBAL.add("usb_mass_storage_enabled");
            MOVED_TO_GLOBAL.add("use_google_mail");
            MOVED_TO_GLOBAL.add(Global.WIFI_COUNTRY_CODE);
            MOVED_TO_GLOBAL.add(Global.WIFI_FRAMEWORK_SCAN_INTERVAL_MS);
            MOVED_TO_GLOBAL.add(Global.WIFI_FREQUENCY_BAND);
            MOVED_TO_GLOBAL.add("wifi_idle_ms");
            MOVED_TO_GLOBAL.add("wifi_max_dhcp_retry_count");
            MOVED_TO_GLOBAL.add("wifi_mobile_data_transition_wakelock_timeout_ms");
            MOVED_TO_GLOBAL.add("wifi_networks_available_notification_on");
            MOVED_TO_GLOBAL.add("wifi_networks_available_repeat_delay");
            MOVED_TO_GLOBAL.add("wifi_num_open_networks_kept");
            MOVED_TO_GLOBAL.add("wifi_on");
            MOVED_TO_GLOBAL.add(Global.WIFI_P2P_DEVICE_NAME);
            MOVED_TO_GLOBAL.add(Global.WIFI_SAVED_STATE);
            MOVED_TO_GLOBAL.add(Global.WIFI_SUPPLICANT_SCAN_INTERVAL_MS);
            MOVED_TO_GLOBAL.add(Global.WIFI_SUSPEND_OPTIMIZATIONS_ENABLED);
            MOVED_TO_GLOBAL.add(Global.WIFI_VERBOSE_LOGGING_ENABLED);
            MOVED_TO_GLOBAL.add(Global.WIFI_ENHANCED_AUTO_JOIN);
            MOVED_TO_GLOBAL.add(Global.WIFI_NETWORK_SHOW_RSSI);
            MOVED_TO_GLOBAL.add("wifi_watchdog_on");
            MOVED_TO_GLOBAL.add(Global.WIFI_WATCHDOG_POOR_NETWORK_TEST_ENABLED);
            MOVED_TO_GLOBAL.add(Global.WIMAX_NETWORKS_AVAILABLE_NOTIFICATION_ON);
            MOVED_TO_GLOBAL.add(Global.PACKAGE_VERIFIER_ENABLE);
            MOVED_TO_GLOBAL.add(Global.PACKAGE_VERIFIER_TIMEOUT);
            MOVED_TO_GLOBAL.add(Global.PACKAGE_VERIFIER_DEFAULT_RESPONSE);
            MOVED_TO_GLOBAL.add(Global.DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS);
            MOVED_TO_GLOBAL.add(Global.DATA_STALL_ALARM_AGGRESSIVE_DELAY_IN_MS);
            MOVED_TO_GLOBAL.add(Global.GPRS_REGISTER_CHECK_PERIOD_MS);
            MOVED_TO_GLOBAL.add(Global.WTF_IS_FATAL);
            MOVED_TO_GLOBAL.add(Global.BATTERY_DISCHARGE_DURATION_THRESHOLD);
            MOVED_TO_GLOBAL.add(Global.BATTERY_DISCHARGE_THRESHOLD);
            MOVED_TO_GLOBAL.add(Global.SEND_ACTION_APP_ERROR);
            MOVED_TO_GLOBAL.add(Global.DROPBOX_AGE_SECONDS);
            MOVED_TO_GLOBAL.add(Global.DROPBOX_MAX_FILES);
            MOVED_TO_GLOBAL.add(Global.DROPBOX_QUOTA_KB);
            MOVED_TO_GLOBAL.add(Global.DROPBOX_QUOTA_PERCENT);
            MOVED_TO_GLOBAL.add(Global.DROPBOX_RESERVE_PERCENT);
            MOVED_TO_GLOBAL.add(Global.DROPBOX_TAG_PREFIX);
            MOVED_TO_GLOBAL.add(Global.ERROR_LOGCAT_PREFIX);
            MOVED_TO_GLOBAL.add(Global.SYS_FREE_STORAGE_LOG_INTERVAL);
            MOVED_TO_GLOBAL.add(Global.DISK_FREE_CHANGE_REPORTING_THRESHOLD);
            MOVED_TO_GLOBAL.add(Global.SYS_STORAGE_THRESHOLD_PERCENTAGE);
            MOVED_TO_GLOBAL.add(Global.SYS_STORAGE_THRESHOLD_MAX_BYTES);
            MOVED_TO_GLOBAL.add(Global.SYS_STORAGE_FULL_THRESHOLD_BYTES);
            MOVED_TO_GLOBAL.add(Global.SYNC_MAX_RETRY_DELAY_IN_SECONDS);
            MOVED_TO_GLOBAL.add(Global.CONNECTIVITY_CHANGE_DELAY);
            MOVED_TO_GLOBAL.add(Global.CAPTIVE_PORTAL_DETECTION_ENABLED);
            MOVED_TO_GLOBAL.add(Global.CAPTIVE_PORTAL_SERVER);
            MOVED_TO_GLOBAL.add(Global.NSD_ON);
            MOVED_TO_GLOBAL.add(Global.SET_INSTALL_LOCATION);
            MOVED_TO_GLOBAL.add(Global.DEFAULT_INSTALL_LOCATION);
            MOVED_TO_GLOBAL.add(Global.INET_CONDITION_DEBOUNCE_UP_DELAY);
            MOVED_TO_GLOBAL.add(Global.INET_CONDITION_DEBOUNCE_DOWN_DELAY);
            MOVED_TO_GLOBAL.add(Global.READ_EXTERNAL_STORAGE_ENFORCED_DEFAULT);
            MOVED_TO_GLOBAL.add("http_proxy");
            MOVED_TO_GLOBAL.add(Global.GLOBAL_HTTP_PROXY_HOST);
            MOVED_TO_GLOBAL.add(Global.GLOBAL_HTTP_PROXY_PORT);
            MOVED_TO_GLOBAL.add(Global.GLOBAL_HTTP_PROXY_EXCLUSION_LIST);
            MOVED_TO_GLOBAL.add(Global.SET_GLOBAL_HTTP_PROXY);
            MOVED_TO_GLOBAL.add(Global.DEFAULT_DNS_SERVER);
            MOVED_TO_GLOBAL.add(Global.PREFERRED_NETWORK_MODE);
            MOVED_TO_GLOBAL.add(Global.WEBVIEW_DATA_REDUCTION_PROXY_KEY);
            CLONE_TO_MANAGED_PROFILE.add(ACCESSIBILITY_ENABLED);
            CLONE_TO_MANAGED_PROFILE.add(ALLOW_MOCK_LOCATION);
            CLONE_TO_MANAGED_PROFILE.add(ALLOWED_GEOLOCATION_ORIGINS);
            CLONE_TO_MANAGED_PROFILE.add(AUTOFILL_SERVICE);
            CLONE_TO_MANAGED_PROFILE.add(DEFAULT_INPUT_METHOD);
            CLONE_TO_MANAGED_PROFILE.add(ENABLED_ACCESSIBILITY_SERVICES);
            CLONE_TO_MANAGED_PROFILE.add(ENABLED_INPUT_METHODS);
            CLONE_TO_MANAGED_PROFILE.add(LOCATION_MODE);
            CLONE_TO_MANAGED_PROFILE.add(LOCATION_PREVIOUS_MODE);
            CLONE_TO_MANAGED_PROFILE.add("location_providers_allowed");
            CLONE_TO_MANAGED_PROFILE.add(SELECTED_INPUT_METHOD_SUBTYPE);
            CLONE_TO_MANAGED_PROFILE.add(SELECTED_SPELL_CHECKER);
            CLONE_TO_MANAGED_PROFILE.add(SELECTED_SPELL_CHECKER_SUBTYPE);
            INSTANT_APP_SETTINGS.add(ENABLED_ACCESSIBILITY_SERVICES);
            INSTANT_APP_SETTINGS.add(ACCESSIBILITY_SPEAK_PASSWORD);
            INSTANT_APP_SETTINGS.add(ACCESSIBILITY_DISPLAY_INVERSION_ENABLED);
            INSTANT_APP_SETTINGS.add(ACCESSIBILITY_CAPTIONING_ENABLED);
            INSTANT_APP_SETTINGS.add(ACCESSIBILITY_CAPTIONING_PRESET);
            INSTANT_APP_SETTINGS.add(ACCESSIBILITY_CAPTIONING_EDGE_TYPE);
            INSTANT_APP_SETTINGS.add(ACCESSIBILITY_CAPTIONING_EDGE_COLOR);
            INSTANT_APP_SETTINGS.add(ACCESSIBILITY_CAPTIONING_LOCALE);
            INSTANT_APP_SETTINGS.add(ACCESSIBILITY_CAPTIONING_BACKGROUND_COLOR);
            INSTANT_APP_SETTINGS.add(ACCESSIBILITY_CAPTIONING_FOREGROUND_COLOR);
            INSTANT_APP_SETTINGS.add(ACCESSIBILITY_CAPTIONING_TYPEFACE);
            INSTANT_APP_SETTINGS.add(ACCESSIBILITY_CAPTIONING_FONT_SCALE);
            INSTANT_APP_SETTINGS.add(ACCESSIBILITY_CAPTIONING_WINDOW_COLOR);
            INSTANT_APP_SETTINGS.add(ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED);
            INSTANT_APP_SETTINGS.add(ACCESSIBILITY_DISPLAY_DALTONIZER);
            INSTANT_APP_SETTINGS.add(ACCESSIBILITY_AUTOCLICK_DELAY);
            INSTANT_APP_SETTINGS.add(ACCESSIBILITY_AUTOCLICK_ENABLED);
            INSTANT_APP_SETTINGS.add(ACCESSIBILITY_LARGE_POINTER_ICON);
            INSTANT_APP_SETTINGS.add(DEFAULT_INPUT_METHOD);
            INSTANT_APP_SETTINGS.add(ENABLED_INPUT_METHODS);
            INSTANT_APP_SETTINGS.add("android_id");
            INSTANT_APP_SETTINGS.add(PACKAGE_VERIFIER_USER_CONSENT);
            INSTANT_APP_SETTINGS.add(ALLOW_MOCK_LOCATION);
        }

        public static void getMovedToGlobalSettings(Set<String> outKeySet) {
            outKeySet.addAll(MOVED_TO_GLOBAL);
        }

        public static String getString(ContentResolver resolver, String name) {
            return getStringForUser(resolver, name, UserHandle.myUserId());
        }

        public static String getStringForUser(ContentResolver resolver, String name, int userHandle) {
            if (MOVED_TO_GLOBAL.contains(name)) {
                Log.w(Settings.TAG, "Setting " + name + " has moved from android.provider.Settings.Secure" + " to android.provider.Settings.Global.");
                return Global.getStringForUser(resolver, name, userHandle);
            }
            if (MOVED_TO_LOCK_SETTINGS.contains(name)) {
                synchronized (Secure.class) {
                    if (sLockSettings == null) {
                        sLockSettings = Stub.asInterface(ServiceManager.getService("lock_settings"));
                        sIsSystemProcess = Process.myUid() == 1000;
                    }
                }
                if (!(sLockSettings == null || (sIsSystemProcess ^ 1) == 0)) {
                    Application application = ActivityThread.currentApplication();
                    boolean isPreMnc = (application == null || application.getApplicationInfo() == null) ? false : application.getApplicationInfo().targetSdkVersion <= 22;
                    if (isPreMnc) {
                        try {
                            return sLockSettings.getString(name, "0", userHandle);
                        } catch (RemoteException e) {
                        }
                    } else {
                        throw new SecurityException("Settings.Secure." + name + " is deprecated and no longer accessible." + " See API documentation for potential replacements.");
                    }
                }
            }
            return sNameValueCache.getStringForUser(resolver, name, userHandle);
        }

        public static boolean putString(ContentResolver resolver, String name, String value) {
            return putStringForUser(resolver, name, value, UserHandle.myUserId());
        }

        public static boolean putStringForUser(ContentResolver resolver, String name, String value, int userHandle) {
            return putStringForUser(resolver, name, value, null, false, userHandle);
        }

        public static boolean putStringForUser(ContentResolver resolver, String name, String value, String tag, boolean makeDefault, int userHandle) {
            String MDM_POLICY_INVALID_VALUE = "mdm_policy_invalid_value";
            String fixedValue = HwFrameworkFactory.getHwSettingsManager().adjustValueForMDMPolicy(resolver, name, value);
            if ("mdm_policy_invalid_value".equals(fixedValue)) {
                Log.w(Settings.TAG, "Setting '" + name + "' set operation was prevented by current MDM policy.");
                return false;
            }
            value = fixedValue;
            if (LOCATION_MODE.equals(name)) {
                return setLocationModeForUser(resolver, Integer.parseInt(fixedValue), userHandle);
            }
            if (!MOVED_TO_GLOBAL.contains(name)) {
                return sNameValueCache.putStringForUser(resolver, name, fixedValue, tag, makeDefault, userHandle);
            }
            Log.w(Settings.TAG, "Setting " + name + " has moved from android.provider.Settings.Secure" + " to android.provider.Settings.Global");
            return Global.putStringForUser(resolver, name, fixedValue, tag, makeDefault, userHandle);
        }

        public static boolean putString(ContentResolver resolver, String name, String value, String tag, boolean makeDefault) {
            return putStringForUser(resolver, name, value, tag, makeDefault, UserHandle.myUserId());
        }

        public static void resetToDefaults(ContentResolver resolver, String tag) {
            resetToDefaultsAsUser(resolver, tag, 1, UserHandle.myUserId());
        }

        public static void resetToDefaultsAsUser(ContentResolver resolver, String tag, int mode, int userHandle) {
            try {
                Bundle arg = new Bundle();
                arg.putInt(Settings.CALL_METHOD_USER_KEY, userHandle);
                if (tag != null) {
                    arg.putString(Settings.CALL_METHOD_TAG_KEY, tag);
                }
                arg.putInt(Settings.CALL_METHOD_RESET_MODE_KEY, mode);
                sProviderHolder.getProvider(resolver).call(resolver.getPackageName(), Settings.CALL_METHOD_RESET_SECURE, null, arg);
            } catch (RemoteException e) {
                Log.w(Settings.TAG, "Can't reset do defaults for " + CONTENT_URI, e);
            }
        }

        public static Uri getUriFor(String name) {
            if (!MOVED_TO_GLOBAL.contains(name)) {
                return NameValueTable.getUriFor(CONTENT_URI, name);
            }
            Log.w(Settings.TAG, "Setting " + name + " has moved from android.provider.Settings.Secure" + " to android.provider.Settings.Global, returning global URI.");
            return NameValueTable.getUriFor(Global.CONTENT_URI, name);
        }

        public static int getInt(ContentResolver cr, String name, int def) {
            if (HwPCUtils.isValidExtDisplayId(HwPCUtils.getPCDisplayID()) && (ActivityThread.isSystem() ^ 1) != 0 && SHOW_IME_WITH_HARD_KEYBOARD.equals(name)) {
                return 0;
            }
            return getIntForUser(cr, name, def, UserHandle.myUserId());
        }

        public static int getIntForUser(ContentResolver cr, String name, int def, int userHandle) {
            if (LOCATION_MODE.equals(name)) {
                return getLocationModeForUser(cr, userHandle);
            }
            String v = getStringForUser(cr, name, userHandle);
            if (v != null) {
                try {
                    def = Integer.parseInt(v);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            return def;
        }

        public static int getInt(ContentResolver cr, String name) throws SettingNotFoundException {
            if (HwPCUtils.isValidExtDisplayId(HwPCUtils.getPCDisplayID()) && (ActivityThread.isSystem() ^ 1) != 0 && SHOW_IME_WITH_HARD_KEYBOARD.equals(name)) {
                return 0;
            }
            return getIntForUser(cr, name, UserHandle.myUserId());
        }

        public static int getIntForUser(ContentResolver cr, String name, int userHandle) throws SettingNotFoundException {
            if (LOCATION_MODE.equals(name)) {
                return getLocationModeForUser(cr, userHandle);
            }
            try {
                return Integer.parseInt(getStringForUser(cr, name, userHandle));
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
            }
        }

        public static boolean putInt(ContentResolver cr, String name, int value) {
            return putIntForUser(cr, name, value, UserHandle.myUserId());
        }

        public static boolean putIntForUser(ContentResolver cr, String name, int value, int userHandle) {
            return putStringForUser(cr, name, Integer.toString(value), userHandle);
        }

        public static long getLong(ContentResolver cr, String name, long def) {
            return getLongForUser(cr, name, def, UserHandle.myUserId());
        }

        public static long getLongForUser(ContentResolver cr, String name, long def, int userHandle) {
            String valString = getStringForUser(cr, name, userHandle);
            if (valString == null) {
                return def;
            }
            try {
                return Long.parseLong(valString);
            } catch (NumberFormatException e) {
                return def;
            }
        }

        public static long getLong(ContentResolver cr, String name) throws SettingNotFoundException {
            return getLongForUser(cr, name, UserHandle.myUserId());
        }

        public static long getLongForUser(ContentResolver cr, String name, int userHandle) throws SettingNotFoundException {
            try {
                return Long.parseLong(getStringForUser(cr, name, userHandle));
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
            }
        }

        public static boolean putLong(ContentResolver cr, String name, long value) {
            return putLongForUser(cr, name, value, UserHandle.myUserId());
        }

        public static boolean putLongForUser(ContentResolver cr, String name, long value, int userHandle) {
            return putStringForUser(cr, name, Long.toString(value), userHandle);
        }

        public static float getFloat(ContentResolver cr, String name, float def) {
            return getFloatForUser(cr, name, def, UserHandle.myUserId());
        }

        public static float getFloatForUser(ContentResolver cr, String name, float def, int userHandle) {
            String v = getStringForUser(cr, name, userHandle);
            if (v != null) {
                try {
                    def = Float.parseFloat(v);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            return def;
        }

        public static float getFloat(ContentResolver cr, String name) throws SettingNotFoundException {
            return getFloatForUser(cr, name, UserHandle.myUserId());
        }

        public static float getFloatForUser(ContentResolver cr, String name, int userHandle) throws SettingNotFoundException {
            String v = getStringForUser(cr, name, userHandle);
            if (v == null) {
                throw new SettingNotFoundException(name);
            }
            try {
                return Float.parseFloat(v);
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
            }
        }

        public static boolean putFloat(ContentResolver cr, String name, float value) {
            return putFloatForUser(cr, name, value, UserHandle.myUserId());
        }

        public static boolean putFloatForUser(ContentResolver cr, String name, float value, int userHandle) {
            return putStringForUser(cr, name, Float.toString(value), userHandle);
        }

        public static void getCloneToManagedProfileSettings(Set<String> outKeySet) {
            outKeySet.addAll(CLONE_TO_MANAGED_PROFILE);
        }

        @Deprecated
        public static final boolean isLocationProviderEnabled(ContentResolver cr, String provider) {
            return isLocationProviderEnabledForUser(cr, provider, UserHandle.myUserId());
        }

        @Deprecated
        public static final boolean isLocationProviderEnabledForUser(ContentResolver cr, String provider, int userId) {
            return TextUtils.delimitedStringContains(getStringForUser(cr, "location_providers_allowed", userId), ',', provider);
        }

        @Deprecated
        public static final void setLocationProviderEnabled(ContentResolver cr, String provider, boolean enabled) {
            setLocationProviderEnabledForUser(cr, provider, enabled, UserHandle.myUserId());
        }

        @Deprecated
        public static final boolean setLocationProviderEnabledForUser(ContentResolver cr, String provider, boolean enabled, int userId) {
            boolean putStringForUser;
            synchronized (Settings.mLocationSettingsLock) {
                if (enabled) {
                    provider = HwCustPlusAndIddNddConvertUtils.PLUS_PREFIX + provider;
                } else {
                    provider = NativeLibraryHelper.CLEAR_ABI_OVERRIDE + provider;
                }
                putStringForUser = putStringForUser(cr, "location_providers_allowed", provider, userId);
            }
            return putStringForUser;
        }

        private static final boolean saveLocationModeForUser(ContentResolver cr, int userId) {
            return putIntForUser(cr, LOCATION_PREVIOUS_MODE, getLocationModeForUser(cr, userId), userId);
        }

        private static final boolean restoreLocationModeForUser(ContentResolver cr, int userId) {
            int mode = getIntForUser(cr, LOCATION_PREVIOUS_MODE, 3, userId);
            if (mode == 0) {
                mode = 3;
            }
            return setLocationModeForUser(cr, mode, userId);
        }

        /* JADX WARNING: Missing block: B:20:0x0096, code:
            return r3;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private static final boolean setLocationModeForUser(ContentResolver cr, int mode, int userId) {
            synchronized (Settings.mLocationSettingsLock) {
                boolean gps = false;
                boolean network = false;
                Log.i(Settings.TAG, "setLocationModeForUser: mode = " + mode);
                switch (mode) {
                    case -1:
                        boolean restoreLocationModeForUser = restoreLocationModeForUser(cr, userId);
                        return restoreLocationModeForUser;
                    case 0:
                        saveLocationModeForUser(cr, userId);
                        break;
                    case 1:
                        gps = true;
                        break;
                    case 2:
                        network = true;
                        break;
                    case 3:
                        gps = true;
                        network = true;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid location mode: " + mode);
                }
                boolean nlpSuccess = setLocationProviderEnabledForUser(cr, "network", network, userId);
                boolean gpsSuccess = setLocationProviderEnabledForUser(cr, "gps", gps, userId);
                Log.i(Settings.TAG, "setLocationModeForUser: gps = " + gps + ", network = " + network + ", nlpSuccess = " + nlpSuccess + ", gpsSuccess = " + gpsSuccess);
                if (!gpsSuccess) {
                    nlpSuccess = false;
                }
            }
        }

        private static final int getLocationModeForUser(ContentResolver cr, int userId) {
            synchronized (Settings.mLocationSettingsLock) {
                boolean gpsEnabled = isLocationProviderEnabledForUser(cr, "gps", userId);
                boolean networkEnabled = isLocationProviderEnabledForUser(cr, "network", userId);
                if (gpsEnabled && networkEnabled) {
                    return 3;
                } else if (gpsEnabled) {
                    return 1;
                } else if (networkEnabled) {
                    return 2;
                } else {
                    return 0;
                }
            }
        }
    }

    public static class SettingNotFoundException extends AndroidException {
        public SettingNotFoundException(String msg) {
            super(msg);
        }
    }

    public static final class System extends NameValueTable {
        public static final String ACCELEROMETER_ROTATION = "accelerometer_rotation";
        public static final Validator ACCELEROMETER_ROTATION_VALIDATOR = sBooleanValidator;
        @Deprecated
        public static final String ADB_ENABLED = "adb_enabled";
        public static final String ADVANCED_SETTINGS = "advanced_settings";
        public static final int ADVANCED_SETTINGS_DEFAULT = 0;
        private static final Validator ADVANCED_SETTINGS_VALIDATOR = sBooleanValidator;
        @Deprecated
        public static final String AIRPLANE_MODE_ON = "airplane_mode_on";
        @Deprecated
        public static final String AIRPLANE_MODE_RADIOS = "airplane_mode_radios";
        @Deprecated
        public static final String AIRPLANE_MODE_TOGGLEABLE_RADIOS = "airplane_mode_toggleable_radios";
        public static final String ALARM_ALERT = "alarm_alert";
        public static final String ALARM_ALERT_CACHE = "alarm_alert_cache";
        public static final Uri ALARM_ALERT_CACHE_URI = getUriFor(ALARM_ALERT_CACHE);
        private static final Validator ALARM_ALERT_VALIDATOR = sUriValidator;
        @Deprecated
        public static final String ALWAYS_FINISH_ACTIVITIES = "always_finish_activities";
        @Deprecated
        public static final String ANDROID_ID = "android_id";
        @Deprecated
        public static final String ANIMATOR_DURATION_SCALE = "animator_duration_scale";
        public static final String APPEND_FOR_LAST_AUDIBLE = "_last_audible";
        @Deprecated
        public static final String AUTO_TIME = "auto_time";
        @Deprecated
        public static final String AUTO_TIME_ZONE = "auto_time_zone";
        public static final String BLUETOOTH_DISCOVERABILITY = "bluetooth_discoverability";
        public static final String BLUETOOTH_DISCOVERABILITY_TIMEOUT = "bluetooth_discoverability_timeout";
        private static final Validator BLUETOOTH_DISCOVERABILITY_TIMEOUT_VALIDATOR = sNonNegativeIntegerValidator;
        private static final Validator BLUETOOTH_DISCOVERABILITY_VALIDATOR = new InclusiveIntegerRangeValidator(0, 2);
        @Deprecated
        public static final String BLUETOOTH_ON = "bluetooth_on";
        @Deprecated
        public static final String CAR_DOCK_SOUND = "car_dock_sound";
        @Deprecated
        public static final String CAR_UNDOCK_SOUND = "car_undock_sound";
        public static final Map<String, String> CLONE_FROM_PARENT_ON_VALUE = new ArrayMap();
        private static final Set<String> CLONE_TO_MANAGED_PROFILE = new ArraySet();
        public static final String CONTENTED_TYPEC_ANALOG_ALLOWED = "typec_analog_enabled";
        public static final String CONTENTED_TYPEC_DIGITAL_ALLOWED = "typec_digital_enabled";
        public static final Uri CONTENT_URI = Uri.parse("content://settings/system");
        @Deprecated
        public static final String DATA_ROAMING = "data_roaming";
        public static final String DATE_FORMAT = "date_format";
        public static final Validator DATE_FORMAT_VALIDATOR = new Validator() {
            public boolean validate(String value) {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(value);
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        };
        @Deprecated
        public static final String DEBUG_APP = "debug_app";
        public static final Uri DEFAULT_ALARM_ALERT_URI = getUriFor(ALARM_ALERT);
        private static final float DEFAULT_FONT_SCALE = 1.0f;
        public static final Uri DEFAULT_NOTIFICATION_URI = getUriFor(NOTIFICATION_SOUND);
        public static final Uri DEFAULT_RINGTONE_URI = getUriFor(RINGTONE);
        @Deprecated
        public static final String DESK_DOCK_SOUND = "desk_dock_sound";
        @Deprecated
        public static final String DESK_UNDOCK_SOUND = "desk_undock_sound";
        @Deprecated
        public static final String DEVICE_PROVISIONED = "device_provisioned";
        @Deprecated
        public static final String DIM_SCREEN = "dim_screen";
        private static final Validator DIM_SCREEN_VALIDATOR = sBooleanValidator;
        @Deprecated
        public static final String DOCK_SOUNDS_ENABLED = "dock_sounds_enabled";
        public static final String DTMF_TONE_TYPE_WHEN_DIALING = "dtmf_tone_type";
        public static final Validator DTMF_TONE_TYPE_WHEN_DIALING_VALIDATOR = sBooleanValidator;
        public static final String DTMF_TONE_WHEN_DIALING = "dtmf_tone";
        public static final Validator DTMF_TONE_WHEN_DIALING_VALIDATOR = sBooleanValidator;
        public static final String DTS_MODE = "dts_mode";
        public static final String EGG_MODE = "egg_mode";
        public static final Validator EGG_MODE_VALIDATOR = new Validator() {
            public boolean validate(String value) {
                boolean z = false;
                try {
                    if (Long.parseLong(value) >= 0) {
                        z = true;
                    }
                    return z;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        };
        public static final String END_BUTTON_BEHAVIOR = "end_button_behavior";
        public static final int END_BUTTON_BEHAVIOR_DEFAULT = 2;
        public static final int END_BUTTON_BEHAVIOR_HOME = 1;
        public static final int END_BUTTON_BEHAVIOR_SLEEP = 2;
        private static final Validator END_BUTTON_BEHAVIOR_VALIDATOR = new InclusiveIntegerRangeValidator(0, 3);
        public static final String FIRST_DAY_OF_WEEK = "first_day_of_week";
        public static final String FONT_SCALE = "font_scale";
        private static final Validator FONT_SCALE_VALIDATOR = new Validator() {
            public boolean validate(String value) {
                boolean z = false;
                try {
                    if (Float.parseFloat(value) >= 0.0f) {
                        z = true;
                    }
                    return z;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        };
        public static final String HAPTIC_FEEDBACK_ENABLED = "haptic_feedback_enabled";
        public static final Validator HAPTIC_FEEDBACK_ENABLED_VALIDATOR = sBooleanValidator;
        public static final String HEARING_AID = "hearing_aid";
        public static final Validator HEARING_AID_VALIDATOR = sBooleanValidator;
        public static final String HIDE_ROTATION_LOCK_TOGGLE_FOR_ACCESSIBILITY = "hide_rotation_lock_toggle_for_accessibility";
        public static final Validator HIDE_ROTATION_LOCK_TOGGLE_FOR_ACCESSIBILITY_VALIDATOR = sBooleanValidator;
        @Deprecated
        public static final String HTTP_PROXY = "http_proxy";
        public static final Uri HUAWEI_RINGTONE2_URI = getUriFor("ringtone2");
        public static final String HW_SCREEN_AUTO_BRIGHTNESS_ADJ = "hw_screen_auto_brightness_adj";
        public static final String HW_SCREEN_BRIGHTNESS_MODE_VALUE = "hw_screen_brightness_mode_value";
        public static final String HW_SCREEN_TEMP_BRIGHTNESS = "hw_screen_temp_brightness";
        @Deprecated
        public static final String INSTALL_NON_MARKET_APPS = "install_non_market_apps";
        public static final Set<String> INSTANT_APP_SETTINGS = new ArraySet();
        public static final String KEY_CONTENT_HDB_ALLOWED = "hdb_enabled";
        @Deprecated
        public static final String LOCATION_PROVIDERS_ALLOWED = "location_providers_allowed";
        public static final String LOCKSCREEN_DISABLED = "lockscreen.disabled";
        public static final Validator LOCKSCREEN_DISABLED_VALIDATOR = sBooleanValidator;
        public static final String LOCKSCREEN_SOUNDS_ENABLED = "lockscreen_sounds_enabled";
        public static final Validator LOCKSCREEN_SOUNDS_ENABLED_VALIDATOR = sBooleanValidator;
        @Deprecated
        public static final String LOCK_PATTERN_ENABLED = "lock_pattern_autolock";
        @Deprecated
        public static final String LOCK_PATTERN_TACTILE_FEEDBACK_ENABLED = "lock_pattern_tactile_feedback_enabled";
        @Deprecated
        public static final String LOCK_PATTERN_VISIBLE = "lock_pattern_visible_pattern";
        @Deprecated
        public static final String LOCK_SOUND = "lock_sound";
        public static final String LOCK_TO_APP_ENABLED = "lock_to_app_enabled";
        public static final Validator LOCK_TO_APP_ENABLED_VALIDATOR = sBooleanValidator;
        @Deprecated
        public static final String LOGGING_ID = "logging_id";
        @Deprecated
        public static final String LOW_BATTERY_SOUND = "low_battery_sound";
        public static final String MASTER_MONO = "master_mono";
        private static final Validator MASTER_MONO_VALIDATOR = sBooleanValidator;
        public static final String MEDIA_BUTTON_RECEIVER = "media_button_receiver";
        private static final Validator MEDIA_BUTTON_RECEIVER_VALIDATOR = new Validator() {
            public boolean validate(String value) {
                try {
                    ComponentName.unflattenFromString(value);
                    return true;
                } catch (NullPointerException e) {
                    return false;
                }
            }
        };
        public static final String MICROPHONE_MUTE = "microphone_mute";
        private static final Validator MICROPHONE_MUTE_VALIDATOR = sBooleanValidator;
        @Deprecated
        public static final String MODE_RINGER = "mode_ringer";
        public static final String MODE_RINGER_STREAMS_AFFECTED = "mode_ringer_streams_affected";
        private static final Validator MODE_RINGER_STREAMS_AFFECTED_VALIDATOR = sNonNegativeIntegerValidator;
        private static final HashSet<String> MOVED_TO_GLOBAL = new HashSet();
        private static final HashSet<String> MOVED_TO_SECURE = new HashSet(30);
        private static final HashSet<String> MOVED_TO_SECURE_THEN_GLOBAL = new HashSet();
        public static final String MUTE_STREAMS_AFFECTED = "mute_streams_affected";
        private static final Validator MUTE_STREAMS_AFFECTED_VALIDATOR = sNonNegativeIntegerValidator;
        @Deprecated
        public static final String NETWORK_PREFERENCE = "network_preference";
        @Deprecated
        public static final String NEXT_ALARM_FORMATTED = "next_alarm_formatted";
        private static final Validator NEXT_ALARM_FORMATTED_VALIDATOR = new Validator() {
            private static final int MAX_LENGTH = 1000;

            public boolean validate(String value) {
                return value == null || value.length() < 1000;
            }
        };
        @Deprecated
        public static final String NOTIFICATIONS_USE_RING_VOLUME = "notifications_use_ring_volume";
        private static final Validator NOTIFICATIONS_USE_RING_VOLUME_VALIDATOR = sBooleanValidator;
        public static final String NOTIFICATION_LIGHT_PULSE = "notification_light_pulse";
        public static final Validator NOTIFICATION_LIGHT_PULSE_VALIDATOR = sBooleanValidator;
        public static final String NOTIFICATION_SOUND = "notification_sound";
        public static final String NOTIFICATION_SOUND_CACHE = "notification_sound_cache";
        public static final Uri NOTIFICATION_SOUND_CACHE_URI = getUriFor(NOTIFICATION_SOUND_CACHE);
        private static final Validator NOTIFICATION_SOUND_VALIDATOR = sUriValidator;
        @Deprecated
        public static final String PARENTAL_CONTROL_ENABLED = "parental_control_enabled";
        @Deprecated
        public static final String PARENTAL_CONTROL_LAST_UPDATE = "parental_control_last_update";
        @Deprecated
        public static final String PARENTAL_CONTROL_REDIRECT_URL = "parental_control_redirect_url";
        public static final String POINTER_LOCATION = "pointer_location";
        public static final Validator POINTER_LOCATION_VALIDATOR = sBooleanValidator;
        public static final String POINTER_SPEED = "pointer_speed";
        public static final Validator POINTER_SPEED_VALIDATOR = new InclusiveFloatRangeValidator(-7.0f, 7.0f);
        @Deprecated
        public static final String POWER_SOUNDS_ENABLED = "power_sounds_enabled";
        public static final Set<String> PRIVATE_SETTINGS = new ArraySet();
        public static final Set<String> PUBLIC_SETTINGS = new ArraySet();
        @Deprecated
        public static final String RADIO_BLUETOOTH = "bluetooth";
        @Deprecated
        public static final String RADIO_CELL = "cell";
        @Deprecated
        public static final String RADIO_NFC = "nfc";
        @Deprecated
        public static final String RADIO_WIFI = "wifi";
        @Deprecated
        public static final String RADIO_WIMAX = "wimax";
        public static final String RINGTONE = "ringtone";
        public static final String RINGTONE2 = "ringtone2";
        public static final String RINGTONE2_CACHE = "ringtone2_cache";
        public static final Uri RINGTONE2_CACHE_URI = getUriFor(RINGTONE2_CACHE);
        public static final String RINGTONE_CACHE = "ringtone_cache";
        public static final Uri RINGTONE_CACHE_URI = getUriFor(RINGTONE_CACHE);
        private static final Validator RINGTONE_VALIDATOR = sUriValidator;
        public static final String SCREEN_AUTO_BRIGHTNESS = "screen_auto_brightness";
        public static final String SCREEN_AUTO_BRIGHTNESS_ADJ = "screen_auto_brightness_adj";
        private static final Validator SCREEN_AUTO_BRIGHTNESS_ADJ_VALIDATOR = new InclusiveFloatRangeValidator(-1.0f, 1.0f);
        public static final String SCREEN_BRIGHTNESS = "screen_brightness";
        public static final String SCREEN_BRIGHTNESS_CHANGE_PACKAGE = "screen_brightness_change_package";
        public static final String SCREEN_BRIGHTNESS_FOR_VR = "screen_brightness_for_vr";
        private static final Validator SCREEN_BRIGHTNESS_FOR_VR_VALIDATOR = new InclusiveIntegerRangeValidator(0, 255);
        public static final String SCREEN_BRIGHTNESS_MODE = "screen_brightness_mode";
        public static final int SCREEN_BRIGHTNESS_MODE_AUTOMATIC = 1;
        public static final String SCREEN_BRIGHTNESS_MODE_CHANGE_PACKAGE = "screen_brightness_mode_change_package";
        public static final int SCREEN_BRIGHTNESS_MODE_MANUAL = 0;
        private static final Validator SCREEN_BRIGHTNESS_MODE_VALIDATOR = sBooleanValidator;
        private static final Validator SCREEN_BRIGHTNESS_VALIDATOR = new InclusiveIntegerRangeValidator(0, 255);
        public static final String SCREEN_OFF_TIMEOUT = "screen_off_timeout";
        private static final Validator SCREEN_OFF_TIMEOUT_VALIDATOR = sNonNegativeIntegerValidator;
        @Deprecated
        public static final String SETTINGS_CLASSNAME = "settings_classname";
        public static final String[] SETTINGS_TO_BACKUP = new String[]{"stay_on_while_plugged_in", WIFI_USE_STATIC_IP, WIFI_STATIC_IP, WIFI_STATIC_GATEWAY, WIFI_STATIC_NETMASK, WIFI_STATIC_DNS1, WIFI_STATIC_DNS2, BLUETOOTH_DISCOVERABILITY, BLUETOOTH_DISCOVERABILITY_TIMEOUT, FONT_SCALE, DIM_SCREEN, SCREEN_OFF_TIMEOUT, SCREEN_BRIGHTNESS, SCREEN_BRIGHTNESS_MODE, SCREEN_AUTO_BRIGHTNESS_ADJ, SCREEN_BRIGHTNESS_FOR_VR, VIBRATE_INPUT_DEVICES, MODE_RINGER_STREAMS_AFFECTED, TEXT_AUTO_REPLACE, TEXT_AUTO_CAPS, TEXT_AUTO_PUNCTUATE, TEXT_SHOW_PASSWORD, "auto_time", "auto_time_zone", TIME_12_24, DATE_FORMAT, "first_day_of_week", "weekend", DTMF_TONE_WHEN_DIALING, DTMF_TONE_TYPE_WHEN_DIALING, HEARING_AID, TTY_MODE, MASTER_MONO, SOUND_EFFECTS_ENABLED, HAPTIC_FEEDBACK_ENABLED, "power_sounds_enabled", "dock_sounds_enabled", LOCKSCREEN_SOUNDS_ENABLED, SHOW_WEB_SUGGESTIONS, SIP_CALL_OPTIONS, SIP_RECEIVE_CALLS, POINTER_SPEED, VIBRATE_WHEN_RINGING, RINGTONE, SMART_BACKLIGHT, LOCK_TO_APP_ENABLED, NOTIFICATION_SOUND, ACCELEROMETER_ROTATION, SHOW_BATTERY_PERCENT};
        public static final String SETUP_WIZARD_HAS_RUN = "setup_wizard_has_run";
        public static final Validator SETUP_WIZARD_HAS_RUN_VALIDATOR = sBooleanValidator;
        public static final String SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
        private static final Validator SHOW_BATTERY_PERCENT_VALIDATOR = sBooleanValidator;
        public static final String SHOW_GTALK_SERVICE_STATUS = "SHOW_GTALK_SERVICE_STATUS";
        private static final Validator SHOW_GTALK_SERVICE_STATUS_VALIDATOR = sBooleanValidator;
        @Deprecated
        public static final String SHOW_PROCESSES = "show_processes";
        public static final String SHOW_TOUCHES = "show_touches";
        public static final Validator SHOW_TOUCHES_VALIDATOR = sBooleanValidator;
        @Deprecated
        public static final String SHOW_WEB_SUGGESTIONS = "show_web_suggestions";
        public static final Validator SHOW_WEB_SUGGESTIONS_VALIDATOR = sBooleanValidator;
        public static final String SIMPLEUI_MODE = "simpleui_mode";
        public static final String SIP_ADDRESS_ONLY = "SIP_ADDRESS_ONLY";
        public static final Validator SIP_ADDRESS_ONLY_VALIDATOR = sBooleanValidator;
        public static final String SIP_ALWAYS = "SIP_ALWAYS";
        public static final Validator SIP_ALWAYS_VALIDATOR = sBooleanValidator;
        @Deprecated
        public static final String SIP_ASK_ME_EACH_TIME = "SIP_ASK_ME_EACH_TIME";
        public static final Validator SIP_ASK_ME_EACH_TIME_VALIDATOR = sBooleanValidator;
        public static final String SIP_CALL_OPTIONS = "sip_call_options";
        public static final Validator SIP_CALL_OPTIONS_VALIDATOR = new DiscreteValueValidator(new String[]{SIP_ALWAYS, SIP_ADDRESS_ONLY});
        public static final String SIP_RECEIVE_CALLS = "sip_receive_calls";
        public static final Validator SIP_RECEIVE_CALLS_VALIDATOR = sBooleanValidator;
        public static final String SMART_BACKLIGHT = "smart_backlight_enable";
        public static final String SOUND_EFFECTS_ENABLED = "sound_effects_enabled";
        public static final Validator SOUND_EFFECTS_ENABLED_VALIDATOR = sBooleanValidator;
        public static final String SPLINE_AMBIENTLUX = "spline_ambient_lux";
        public static final String SPLINE_AMBIENTLUX_OFFSET = "spline_ambient_lux_offset";
        public static final String SPLINE_CALIBRATIONTEST = "spline_calibration_test";
        public static final String SPLINE_DELTA = "spline_delta";
        public static final String SPLINE_ISUSERCHANGE = "spline_is_user_change";
        public static final String SPLINE_LASTLUXDEFAULTBRIGHTNESS = "spline_last_lux_default_brightness";
        public static final String SPLINE_OFFSETBRIGHTNESS_LAST = "spline_offset_brightness_last";
        public static final String SPLINE_STARTLUXDEFAULTBRIGHTNESS = "spline_start_lux_default_brightness";
        @Deprecated
        public static final String STAY_ON_WHILE_PLUGGED_IN = "stay_on_while_plugged_in";
        public static final String SYSTEM_LOCALES = "system_locales";
        public static final String TEXT_AUTO_CAPS = "auto_caps";
        private static final Validator TEXT_AUTO_CAPS_VALIDATOR = sBooleanValidator;
        public static final String TEXT_AUTO_PUNCTUATE = "auto_punctuate";
        private static final Validator TEXT_AUTO_PUNCTUATE_VALIDATOR = sBooleanValidator;
        public static final String TEXT_AUTO_REPLACE = "auto_replace";
        private static final Validator TEXT_AUTO_REPLACE_VALIDATOR = sBooleanValidator;
        public static final String TEXT_SHOW_PASSWORD = "show_password";
        private static final Validator TEXT_SHOW_PASSWORD_VALIDATOR = sBooleanValidator;
        public static final String TIME_12_24 = "time_12_24";
        public static final Validator TIME_12_24_VALIDATOR = new DiscreteValueValidator(new String[]{"12", "24"});
        @Deprecated
        public static final String TRANSITION_ANIMATION_SCALE = "transition_animation_scale";
        public static final String TTY_MODE = "tty_mode";
        public static final Validator TTY_MODE_VALIDATOR = new InclusiveIntegerRangeValidator(0, 3);
        @Deprecated
        public static final String UNLOCK_SOUND = "unlock_sound";
        @Deprecated
        public static final String USB_MASS_STORAGE_ENABLED = "usb_mass_storage_enabled";
        public static final String USER_ROTATION = "user_rotation";
        public static final Validator USER_ROTATION_VALIDATOR = new InclusiveIntegerRangeValidator(0, 3);
        @Deprecated
        public static final String USE_GOOGLE_MAIL = "use_google_mail";
        public static final Map<String, Validator> VALIDATORS = new ArrayMap();
        public static final String VIBRATE_INPUT_DEVICES = "vibrate_input_devices";
        private static final Validator VIBRATE_INPUT_DEVICES_VALIDATOR = sBooleanValidator;
        public static final String VIBRATE_IN_SILENT = "vibrate_in_silent";
        private static final Validator VIBRATE_IN_SILENT_VALIDATOR = sBooleanValidator;
        public static final String VIBRATE_ON = "vibrate_on";
        private static final Validator VIBRATE_ON_VALIDATOR = sBooleanValidator;
        public static final String VIBRATE_WHEN_RINGING = "vibrate_when_ringing";
        public static final Validator VIBRATE_WHEN_RINGING_VALIDATOR = sBooleanValidator;
        public static final String VOLUME_ACCESSIBILITY = "volume_a11y";
        public static final String VOLUME_ALARM = "volume_alarm";
        public static final String VOLUME_BLUETOOTH_SCO = "volume_bluetooth_sco";
        public static final String VOLUME_MASTER = "volume_master";
        public static final String VOLUME_MASTER_MUTE = "volume_master_mute";
        private static final Validator VOLUME_MASTER_MUTE_VALIDATOR = sBooleanValidator;
        public static final String VOLUME_MUSIC = "volume_music";
        public static final String VOLUME_NOTIFICATION = "volume_notification";
        public static final String VOLUME_RING = "volume_ring";
        public static final String[] VOLUME_SETTINGS = new String[]{VOLUME_VOICE, VOLUME_SYSTEM, VOLUME_RING, VOLUME_MUSIC, VOLUME_ALARM, VOLUME_NOTIFICATION, VOLUME_BLUETOOTH_SCO};
        public static final String[] VOLUME_SETTINGS_INT = new String[]{VOLUME_VOICE, VOLUME_SYSTEM, VOLUME_RING, VOLUME_MUSIC, VOLUME_ALARM, VOLUME_NOTIFICATION, VOLUME_BLUETOOTH_SCO, LogException.NO_VALUE, LogException.NO_VALUE, LogException.NO_VALUE, VOLUME_ACCESSIBILITY};
        public static final String VOLUME_SYSTEM = "volume_system";
        public static final String VOLUME_VOICE = "volume_voice";
        @Deprecated
        public static final String WAIT_FOR_DEBUGGER = "wait_for_debugger";
        @Deprecated
        public static final String WALLPAPER_ACTIVITY = "wallpaper_activity";
        private static final Validator WALLPAPER_ACTIVITY_VALIDATOR = new Validator() {
            private static final int MAX_LENGTH = 1000;

            public boolean validate(String value) {
                boolean z = false;
                if (value != null && value.length() > 1000) {
                    return false;
                }
                if (ComponentName.unflattenFromString(value) != null) {
                    z = true;
                }
                return z;
            }
        };
        public static final String WEEKEND = "weekend";
        public static final String WHEN_TO_MAKE_WIFI_CALLS = "when_to_make_wifi_calls";
        public static final String WIFIPRO_ENABLE_SETTINGS = "smart_network_switching";
        @Deprecated
        public static final String WIFI_MAX_DHCP_RETRY_COUNT = "wifi_max_dhcp_retry_count";
        @Deprecated
        public static final String WIFI_MOBILE_DATA_TRANSITION_WAKELOCK_TIMEOUT_MS = "wifi_mobile_data_transition_wakelock_timeout_ms";
        @Deprecated
        public static final String WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON = "wifi_networks_available_notification_on";
        @Deprecated
        public static final String WIFI_NETWORKS_AVAILABLE_REPEAT_DELAY = "wifi_networks_available_repeat_delay";
        @Deprecated
        public static final String WIFI_NUM_OPEN_NETWORKS_KEPT = "wifi_num_open_networks_kept";
        @Deprecated
        public static final String WIFI_ON = "wifi_on";
        @Deprecated
        public static final String WIFI_SLEEP_POLICY = "wifi_sleep_policy";
        @Deprecated
        public static final int WIFI_SLEEP_POLICY_DEFAULT = 0;
        @Deprecated
        public static final int WIFI_SLEEP_POLICY_NEVER = 2;
        @Deprecated
        public static final int WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED = 1;
        @Deprecated
        public static final String WIFI_STATIC_DNS1 = "wifi_static_dns1";
        private static final Validator WIFI_STATIC_DNS1_VALIDATOR = sLenientIpAddressValidator;
        @Deprecated
        public static final String WIFI_STATIC_DNS2 = "wifi_static_dns2";
        private static final Validator WIFI_STATIC_DNS2_VALIDATOR = sLenientIpAddressValidator;
        @Deprecated
        public static final String WIFI_STATIC_GATEWAY = "wifi_static_gateway";
        private static final Validator WIFI_STATIC_GATEWAY_VALIDATOR = sLenientIpAddressValidator;
        @Deprecated
        public static final String WIFI_STATIC_IP = "wifi_static_ip";
        private static final Validator WIFI_STATIC_IP_VALIDATOR = sLenientIpAddressValidator;
        @Deprecated
        public static final String WIFI_STATIC_NETMASK = "wifi_static_netmask";
        private static final Validator WIFI_STATIC_NETMASK_VALIDATOR = sLenientIpAddressValidator;
        @Deprecated
        public static final String WIFI_USE_STATIC_IP = "wifi_use_static_ip";
        private static final Validator WIFI_USE_STATIC_IP_VALIDATOR = sBooleanValidator;
        @Deprecated
        public static final String WIFI_WATCHDOG_ACCEPTABLE_PACKET_LOSS_PERCENTAGE = "wifi_watchdog_acceptable_packet_loss_percentage";
        @Deprecated
        public static final String WIFI_WATCHDOG_AP_COUNT = "wifi_watchdog_ap_count";
        @Deprecated
        public static final String WIFI_WATCHDOG_BACKGROUND_CHECK_DELAY_MS = "wifi_watchdog_background_check_delay_ms";
        @Deprecated
        public static final String WIFI_WATCHDOG_BACKGROUND_CHECK_ENABLED = "wifi_watchdog_background_check_enabled";
        @Deprecated
        public static final String WIFI_WATCHDOG_BACKGROUND_CHECK_TIMEOUT_MS = "wifi_watchdog_background_check_timeout_ms";
        @Deprecated
        public static final String WIFI_WATCHDOG_INITIAL_IGNORED_PING_COUNT = "wifi_watchdog_initial_ignored_ping_count";
        @Deprecated
        public static final String WIFI_WATCHDOG_MAX_AP_CHECKS = "wifi_watchdog_max_ap_checks";
        @Deprecated
        public static final String WIFI_WATCHDOG_ON = "wifi_watchdog_on";
        @Deprecated
        public static final String WIFI_WATCHDOG_PING_COUNT = "wifi_watchdog_ping_count";
        @Deprecated
        public static final String WIFI_WATCHDOG_PING_DELAY_MS = "wifi_watchdog_ping_delay_ms";
        @Deprecated
        public static final String WIFI_WATCHDOG_PING_TIMEOUT_MS = "wifi_watchdog_ping_timeout_ms";
        @Deprecated
        public static final String WINDOW_ANIMATION_SCALE = "window_animation_scale";
        public static final String WINDOW_ORIENTATION_LISTENER_LOG = "window_orientation_listener_log";
        public static final Validator WINDOW_ORIENTATION_LISTENER_LOG_VALIDATOR = sBooleanValidator;
        private static final Validator sBooleanValidator = new DiscreteValueValidator(new String[]{"0", "1"});
        private static final Validator sLenientIpAddressValidator = new Validator() {
            private static final int MAX_IPV6_LENGTH = 45;

            public boolean validate(String value) {
                return value != null && value.length() <= 45;
            }
        };
        private static final NameValueCache sNameValueCache = new NameValueCache(CONTENT_URI, Settings.CALL_METHOD_GET_SYSTEM, Settings.CALL_METHOD_PUT_SYSTEM, sProviderHolder);
        private static final Validator sNonNegativeIntegerValidator = new Validator() {
            public boolean validate(String value) {
                boolean z = false;
                try {
                    if (Integer.parseInt(value) >= 0) {
                        z = true;
                    }
                    return z;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        };
        private static final ContentProviderHolder sProviderHolder = new ContentProviderHolder(CONTENT_URI);
        private static final Validator sUriValidator = new Validator() {
            public boolean validate(String value) {
                try {
                    Uri.decode(value);
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        };

        public interface Validator {
            boolean validate(String str);
        }

        private static final class DiscreteValueValidator implements Validator {
            private final String[] mValues;

            public DiscreteValueValidator(String[] values) {
                this.mValues = values;
            }

            public boolean validate(String value) {
                return ArrayUtils.contains(this.mValues, (Object) value);
            }
        }

        private static final class InclusiveFloatRangeValidator implements Validator {
            private final float mMax;
            private final float mMin;

            public InclusiveFloatRangeValidator(float min, float max) {
                this.mMin = min;
                this.mMax = max;
            }

            public boolean validate(String value) {
                boolean z = false;
                try {
                    float floatValue = Float.parseFloat(value);
                    if (floatValue >= this.mMin && floatValue <= this.mMax) {
                        z = true;
                    }
                    return z;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }

        private static final class InclusiveIntegerRangeValidator implements Validator {
            private final int mMax;
            private final int mMin;

            public InclusiveIntegerRangeValidator(int min, int max) {
                this.mMin = min;
                this.mMax = max;
            }

            public boolean validate(String value) {
                boolean z = false;
                try {
                    int intValue = Integer.parseInt(value);
                    if (intValue >= this.mMin && intValue <= this.mMax) {
                        z = true;
                    }
                    return z;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }

        static {
            MOVED_TO_SECURE.add("android_id");
            MOVED_TO_SECURE.add("http_proxy");
            MOVED_TO_SECURE.add("location_providers_allowed");
            MOVED_TO_SECURE.add(Secure.LOCK_BIOMETRIC_WEAK_FLAGS);
            MOVED_TO_SECURE.add("lock_pattern_autolock");
            MOVED_TO_SECURE.add("lock_pattern_visible_pattern");
            MOVED_TO_SECURE.add("lock_pattern_tactile_feedback_enabled");
            MOVED_TO_SECURE.add("logging_id");
            MOVED_TO_SECURE.add("parental_control_enabled");
            MOVED_TO_SECURE.add("parental_control_last_update");
            MOVED_TO_SECURE.add("parental_control_redirect_url");
            MOVED_TO_SECURE.add("settings_classname");
            MOVED_TO_SECURE.add("use_google_mail");
            MOVED_TO_SECURE.add("wifi_networks_available_notification_on");
            MOVED_TO_SECURE.add("wifi_networks_available_repeat_delay");
            MOVED_TO_SECURE.add("wifi_num_open_networks_kept");
            MOVED_TO_SECURE.add("wifi_on");
            MOVED_TO_SECURE.add("wifi_watchdog_acceptable_packet_loss_percentage");
            MOVED_TO_SECURE.add("wifi_watchdog_ap_count");
            MOVED_TO_SECURE.add("wifi_watchdog_background_check_delay_ms");
            MOVED_TO_SECURE.add("wifi_watchdog_background_check_enabled");
            MOVED_TO_SECURE.add("wifi_watchdog_background_check_timeout_ms");
            MOVED_TO_SECURE.add("wifi_watchdog_initial_ignored_ping_count");
            MOVED_TO_SECURE.add("wifi_watchdog_max_ap_checks");
            MOVED_TO_SECURE.add("wifi_watchdog_on");
            MOVED_TO_SECURE.add("wifi_watchdog_ping_count");
            MOVED_TO_SECURE.add("wifi_watchdog_ping_delay_ms");
            MOVED_TO_SECURE.add("wifi_watchdog_ping_timeout_ms");
            MOVED_TO_SECURE.add("install_non_market_apps");
            MOVED_TO_SECURE_THEN_GLOBAL.add("adb_enabled");
            MOVED_TO_SECURE_THEN_GLOBAL.add("bluetooth_on");
            MOVED_TO_SECURE_THEN_GLOBAL.add("data_roaming");
            MOVED_TO_SECURE_THEN_GLOBAL.add("device_provisioned");
            MOVED_TO_SECURE_THEN_GLOBAL.add("usb_mass_storage_enabled");
            MOVED_TO_SECURE_THEN_GLOBAL.add("http_proxy");
            MOVED_TO_GLOBAL.add("airplane_mode_on");
            MOVED_TO_GLOBAL.add("airplane_mode_radios");
            MOVED_TO_GLOBAL.add("airplane_mode_toggleable_radios");
            MOVED_TO_GLOBAL.add("auto_time");
            MOVED_TO_GLOBAL.add("auto_time_zone");
            MOVED_TO_GLOBAL.add("car_dock_sound");
            MOVED_TO_GLOBAL.add("car_undock_sound");
            MOVED_TO_GLOBAL.add("desk_dock_sound");
            MOVED_TO_GLOBAL.add("desk_undock_sound");
            MOVED_TO_GLOBAL.add("dock_sounds_enabled");
            MOVED_TO_GLOBAL.add("lock_sound");
            MOVED_TO_GLOBAL.add("unlock_sound");
            MOVED_TO_GLOBAL.add("low_battery_sound");
            MOVED_TO_GLOBAL.add("power_sounds_enabled");
            MOVED_TO_GLOBAL.add("stay_on_while_plugged_in");
            MOVED_TO_GLOBAL.add("wifi_sleep_policy");
            MOVED_TO_GLOBAL.add("mode_ringer");
            MOVED_TO_GLOBAL.add("window_animation_scale");
            MOVED_TO_GLOBAL.add("transition_animation_scale");
            MOVED_TO_GLOBAL.add("animator_duration_scale");
            MOVED_TO_GLOBAL.add(Global.FANCY_IME_ANIMATIONS);
            MOVED_TO_GLOBAL.add(Global.COMPATIBILITY_MODE);
            MOVED_TO_GLOBAL.add(Global.EMERGENCY_TONE);
            MOVED_TO_GLOBAL.add(Global.CALL_AUTO_RETRY);
            MOVED_TO_GLOBAL.add("debug_app");
            MOVED_TO_GLOBAL.add("wait_for_debugger");
            MOVED_TO_GLOBAL.add("always_finish_activities");
            MOVED_TO_GLOBAL.add(Global.TZINFO_UPDATE_CONTENT_URL);
            MOVED_TO_GLOBAL.add(Global.TZINFO_UPDATE_METADATA_URL);
            MOVED_TO_GLOBAL.add(Global.SELINUX_UPDATE_CONTENT_URL);
            MOVED_TO_GLOBAL.add(Global.SELINUX_UPDATE_METADATA_URL);
            MOVED_TO_GLOBAL.add(Global.SMS_SHORT_CODES_UPDATE_CONTENT_URL);
            MOVED_TO_GLOBAL.add(Global.SMS_SHORT_CODES_UPDATE_METADATA_URL);
            MOVED_TO_GLOBAL.add(Global.CERT_PIN_UPDATE_CONTENT_URL);
            MOVED_TO_GLOBAL.add(Global.CERT_PIN_UPDATE_METADATA_URL);
            PUBLIC_SETTINGS.add(END_BUTTON_BEHAVIOR);
            PUBLIC_SETTINGS.add(WIFI_USE_STATIC_IP);
            PUBLIC_SETTINGS.add(WIFI_STATIC_IP);
            PUBLIC_SETTINGS.add(WIFI_STATIC_GATEWAY);
            PUBLIC_SETTINGS.add(WIFI_STATIC_NETMASK);
            PUBLIC_SETTINGS.add(WIFI_STATIC_DNS1);
            PUBLIC_SETTINGS.add(WIFI_STATIC_DNS2);
            PUBLIC_SETTINGS.add(BLUETOOTH_DISCOVERABILITY);
            PUBLIC_SETTINGS.add(BLUETOOTH_DISCOVERABILITY_TIMEOUT);
            PUBLIC_SETTINGS.add(NEXT_ALARM_FORMATTED);
            PUBLIC_SETTINGS.add(FONT_SCALE);
            PUBLIC_SETTINGS.add(DIM_SCREEN);
            PUBLIC_SETTINGS.add(SCREEN_OFF_TIMEOUT);
            PUBLIC_SETTINGS.add(SCREEN_BRIGHTNESS);
            PUBLIC_SETTINGS.add(SCREEN_BRIGHTNESS_MODE);
            PUBLIC_SETTINGS.add(MODE_RINGER_STREAMS_AFFECTED);
            PUBLIC_SETTINGS.add(MUTE_STREAMS_AFFECTED);
            PUBLIC_SETTINGS.add(VIBRATE_ON);
            PUBLIC_SETTINGS.add(VOLUME_RING);
            PUBLIC_SETTINGS.add(VOLUME_SYSTEM);
            PUBLIC_SETTINGS.add(VOLUME_VOICE);
            PUBLIC_SETTINGS.add(VOLUME_MUSIC);
            PUBLIC_SETTINGS.add(VOLUME_ALARM);
            PUBLIC_SETTINGS.add(VOLUME_NOTIFICATION);
            PUBLIC_SETTINGS.add(VOLUME_BLUETOOTH_SCO);
            PUBLIC_SETTINGS.add(RINGTONE);
            PUBLIC_SETTINGS.add(NOTIFICATION_SOUND);
            PUBLIC_SETTINGS.add(ALARM_ALERT);
            PUBLIC_SETTINGS.add(TEXT_AUTO_REPLACE);
            PUBLIC_SETTINGS.add(TEXT_AUTO_CAPS);
            PUBLIC_SETTINGS.add(TEXT_AUTO_PUNCTUATE);
            PUBLIC_SETTINGS.add(TEXT_SHOW_PASSWORD);
            PUBLIC_SETTINGS.add(SHOW_GTALK_SERVICE_STATUS);
            PUBLIC_SETTINGS.add(WALLPAPER_ACTIVITY);
            PUBLIC_SETTINGS.add(TIME_12_24);
            PUBLIC_SETTINGS.add(DATE_FORMAT);
            PUBLIC_SETTINGS.add(SETUP_WIZARD_HAS_RUN);
            PUBLIC_SETTINGS.add(ACCELEROMETER_ROTATION);
            PUBLIC_SETTINGS.add(USER_ROTATION);
            PUBLIC_SETTINGS.add(DTMF_TONE_WHEN_DIALING);
            PUBLIC_SETTINGS.add(SOUND_EFFECTS_ENABLED);
            PUBLIC_SETTINGS.add(HAPTIC_FEEDBACK_ENABLED);
            PUBLIC_SETTINGS.add(SHOW_WEB_SUGGESTIONS);
            PUBLIC_SETTINGS.add(VIBRATE_WHEN_RINGING);
            PUBLIC_SETTINGS.add(LOCKSCREEN_SOUNDS_ENABLED);
            PUBLIC_SETTINGS.add(WIFIPRO_ENABLE_SETTINGS);
            PUBLIC_SETTINGS.add(HW_SCREEN_BRIGHTNESS_MODE_VALUE);
            PRIVATE_SETTINGS.add(WIFI_USE_STATIC_IP);
            PRIVATE_SETTINGS.add(END_BUTTON_BEHAVIOR);
            PRIVATE_SETTINGS.add(ADVANCED_SETTINGS);
            PRIVATE_SETTINGS.add(SCREEN_AUTO_BRIGHTNESS_ADJ);
            PRIVATE_SETTINGS.add(VIBRATE_INPUT_DEVICES);
            PRIVATE_SETTINGS.add(VOLUME_MASTER);
            PRIVATE_SETTINGS.add(MASTER_MONO);
            PRIVATE_SETTINGS.add(NOTIFICATIONS_USE_RING_VOLUME);
            PRIVATE_SETTINGS.add(VIBRATE_IN_SILENT);
            PRIVATE_SETTINGS.add(MEDIA_BUTTON_RECEIVER);
            PRIVATE_SETTINGS.add(HIDE_ROTATION_LOCK_TOGGLE_FOR_ACCESSIBILITY);
            PRIVATE_SETTINGS.add(DTMF_TONE_TYPE_WHEN_DIALING);
            PRIVATE_SETTINGS.add(HEARING_AID);
            PRIVATE_SETTINGS.add(TTY_MODE);
            PRIVATE_SETTINGS.add(NOTIFICATION_LIGHT_PULSE);
            PRIVATE_SETTINGS.add(POINTER_LOCATION);
            PRIVATE_SETTINGS.add(SHOW_TOUCHES);
            PRIVATE_SETTINGS.add(WINDOW_ORIENTATION_LISTENER_LOG);
            PRIVATE_SETTINGS.add("power_sounds_enabled");
            PRIVATE_SETTINGS.add("dock_sounds_enabled");
            PRIVATE_SETTINGS.add("lockscreen.disabled");
            PRIVATE_SETTINGS.add("low_battery_sound");
            PRIVATE_SETTINGS.add("desk_dock_sound");
            PRIVATE_SETTINGS.add("desk_undock_sound");
            PRIVATE_SETTINGS.add("car_dock_sound");
            PRIVATE_SETTINGS.add("car_undock_sound");
            PRIVATE_SETTINGS.add("lock_sound");
            PRIVATE_SETTINGS.add("unlock_sound");
            PRIVATE_SETTINGS.add(SIP_RECEIVE_CALLS);
            PRIVATE_SETTINGS.add(SIP_CALL_OPTIONS);
            PRIVATE_SETTINGS.add(SIP_ALWAYS);
            PRIVATE_SETTINGS.add(SIP_ADDRESS_ONLY);
            PRIVATE_SETTINGS.add(SIP_ASK_ME_EACH_TIME);
            PRIVATE_SETTINGS.add(POINTER_SPEED);
            PRIVATE_SETTINGS.add(LOCK_TO_APP_ENABLED);
            PRIVATE_SETTINGS.add(EGG_MODE);
            PRIVATE_SETTINGS.add(SHOW_BATTERY_PERCENT);
            VALIDATORS.put(END_BUTTON_BEHAVIOR, END_BUTTON_BEHAVIOR_VALIDATOR);
            VALIDATORS.put(WIFI_USE_STATIC_IP, WIFI_USE_STATIC_IP_VALIDATOR);
            VALIDATORS.put(BLUETOOTH_DISCOVERABILITY, BLUETOOTH_DISCOVERABILITY_VALIDATOR);
            VALIDATORS.put(BLUETOOTH_DISCOVERABILITY_TIMEOUT, BLUETOOTH_DISCOVERABILITY_TIMEOUT_VALIDATOR);
            VALIDATORS.put(NEXT_ALARM_FORMATTED, NEXT_ALARM_FORMATTED_VALIDATOR);
            VALIDATORS.put(FONT_SCALE, FONT_SCALE_VALIDATOR);
            VALIDATORS.put(DIM_SCREEN, DIM_SCREEN_VALIDATOR);
            VALIDATORS.put(SCREEN_OFF_TIMEOUT, SCREEN_OFF_TIMEOUT_VALIDATOR);
            VALIDATORS.put(SCREEN_BRIGHTNESS, SCREEN_BRIGHTNESS_VALIDATOR);
            VALIDATORS.put(SCREEN_BRIGHTNESS_FOR_VR, SCREEN_BRIGHTNESS_FOR_VR_VALIDATOR);
            VALIDATORS.put(SCREEN_BRIGHTNESS_MODE, SCREEN_BRIGHTNESS_MODE_VALIDATOR);
            VALIDATORS.put(MODE_RINGER_STREAMS_AFFECTED, MODE_RINGER_STREAMS_AFFECTED_VALIDATOR);
            VALIDATORS.put(MUTE_STREAMS_AFFECTED, MUTE_STREAMS_AFFECTED_VALIDATOR);
            VALIDATORS.put(VIBRATE_ON, VIBRATE_ON_VALIDATOR);
            VALIDATORS.put(RINGTONE, RINGTONE_VALIDATOR);
            VALIDATORS.put(NOTIFICATION_SOUND, NOTIFICATION_SOUND_VALIDATOR);
            VALIDATORS.put(ALARM_ALERT, ALARM_ALERT_VALIDATOR);
            VALIDATORS.put(TEXT_AUTO_REPLACE, TEXT_AUTO_REPLACE_VALIDATOR);
            VALIDATORS.put(TEXT_AUTO_CAPS, TEXT_AUTO_CAPS_VALIDATOR);
            VALIDATORS.put(TEXT_AUTO_PUNCTUATE, TEXT_AUTO_PUNCTUATE_VALIDATOR);
            VALIDATORS.put(TEXT_SHOW_PASSWORD, TEXT_SHOW_PASSWORD_VALIDATOR);
            VALIDATORS.put(SHOW_GTALK_SERVICE_STATUS, SHOW_GTALK_SERVICE_STATUS_VALIDATOR);
            VALIDATORS.put(WALLPAPER_ACTIVITY, WALLPAPER_ACTIVITY_VALIDATOR);
            VALIDATORS.put(TIME_12_24, TIME_12_24_VALIDATOR);
            VALIDATORS.put(DATE_FORMAT, DATE_FORMAT_VALIDATOR);
            VALIDATORS.put(SETUP_WIZARD_HAS_RUN, SETUP_WIZARD_HAS_RUN_VALIDATOR);
            VALIDATORS.put(ACCELEROMETER_ROTATION, ACCELEROMETER_ROTATION_VALIDATOR);
            VALIDATORS.put(USER_ROTATION, USER_ROTATION_VALIDATOR);
            VALIDATORS.put(DTMF_TONE_WHEN_DIALING, DTMF_TONE_WHEN_DIALING_VALIDATOR);
            VALIDATORS.put(SOUND_EFFECTS_ENABLED, SOUND_EFFECTS_ENABLED_VALIDATOR);
            VALIDATORS.put(HAPTIC_FEEDBACK_ENABLED, HAPTIC_FEEDBACK_ENABLED_VALIDATOR);
            VALIDATORS.put(SHOW_WEB_SUGGESTIONS, SHOW_WEB_SUGGESTIONS_VALIDATOR);
            VALIDATORS.put(WIFI_USE_STATIC_IP, WIFI_USE_STATIC_IP_VALIDATOR);
            VALIDATORS.put(END_BUTTON_BEHAVIOR, END_BUTTON_BEHAVIOR_VALIDATOR);
            VALIDATORS.put(ADVANCED_SETTINGS, ADVANCED_SETTINGS_VALIDATOR);
            VALIDATORS.put(SCREEN_AUTO_BRIGHTNESS_ADJ, SCREEN_AUTO_BRIGHTNESS_ADJ_VALIDATOR);
            VALIDATORS.put(VIBRATE_INPUT_DEVICES, VIBRATE_INPUT_DEVICES_VALIDATOR);
            VALIDATORS.put(MASTER_MONO, MASTER_MONO_VALIDATOR);
            VALIDATORS.put(NOTIFICATIONS_USE_RING_VOLUME, NOTIFICATIONS_USE_RING_VOLUME_VALIDATOR);
            VALIDATORS.put(VIBRATE_IN_SILENT, VIBRATE_IN_SILENT_VALIDATOR);
            VALIDATORS.put(MEDIA_BUTTON_RECEIVER, MEDIA_BUTTON_RECEIVER_VALIDATOR);
            VALIDATORS.put(HIDE_ROTATION_LOCK_TOGGLE_FOR_ACCESSIBILITY, HIDE_ROTATION_LOCK_TOGGLE_FOR_ACCESSIBILITY_VALIDATOR);
            VALIDATORS.put(VIBRATE_WHEN_RINGING, VIBRATE_WHEN_RINGING_VALIDATOR);
            VALIDATORS.put(DTMF_TONE_TYPE_WHEN_DIALING, DTMF_TONE_TYPE_WHEN_DIALING_VALIDATOR);
            VALIDATORS.put(HEARING_AID, HEARING_AID_VALIDATOR);
            VALIDATORS.put(TTY_MODE, TTY_MODE_VALIDATOR);
            VALIDATORS.put(NOTIFICATION_LIGHT_PULSE, NOTIFICATION_LIGHT_PULSE_VALIDATOR);
            VALIDATORS.put(POINTER_LOCATION, POINTER_LOCATION_VALIDATOR);
            VALIDATORS.put(SHOW_TOUCHES, SHOW_TOUCHES_VALIDATOR);
            VALIDATORS.put(WINDOW_ORIENTATION_LISTENER_LOG, WINDOW_ORIENTATION_LISTENER_LOG_VALIDATOR);
            VALIDATORS.put(LOCKSCREEN_SOUNDS_ENABLED, LOCKSCREEN_SOUNDS_ENABLED_VALIDATOR);
            VALIDATORS.put("lockscreen.disabled", LOCKSCREEN_DISABLED_VALIDATOR);
            VALIDATORS.put(SIP_RECEIVE_CALLS, SIP_RECEIVE_CALLS_VALIDATOR);
            VALIDATORS.put(SIP_CALL_OPTIONS, SIP_CALL_OPTIONS_VALIDATOR);
            VALIDATORS.put(SIP_ALWAYS, SIP_ALWAYS_VALIDATOR);
            VALIDATORS.put(SIP_ADDRESS_ONLY, SIP_ADDRESS_ONLY_VALIDATOR);
            VALIDATORS.put(SIP_ASK_ME_EACH_TIME, SIP_ASK_ME_EACH_TIME_VALIDATOR);
            VALIDATORS.put(POINTER_SPEED, POINTER_SPEED_VALIDATOR);
            VALIDATORS.put(LOCK_TO_APP_ENABLED, LOCK_TO_APP_ENABLED_VALIDATOR);
            VALIDATORS.put(EGG_MODE, EGG_MODE_VALIDATOR);
            VALIDATORS.put(WIFI_STATIC_IP, WIFI_STATIC_IP_VALIDATOR);
            VALIDATORS.put(WIFI_STATIC_GATEWAY, WIFI_STATIC_GATEWAY_VALIDATOR);
            VALIDATORS.put(WIFI_STATIC_NETMASK, WIFI_STATIC_NETMASK_VALIDATOR);
            VALIDATORS.put(WIFI_STATIC_DNS1, WIFI_STATIC_DNS1_VALIDATOR);
            VALIDATORS.put(WIFI_STATIC_DNS2, WIFI_STATIC_DNS2_VALIDATOR);
            VALIDATORS.put(SHOW_BATTERY_PERCENT, SHOW_BATTERY_PERCENT_VALIDATOR);
            CLONE_TO_MANAGED_PROFILE.add(DATE_FORMAT);
            CLONE_TO_MANAGED_PROFILE.add(HAPTIC_FEEDBACK_ENABLED);
            CLONE_TO_MANAGED_PROFILE.add(SOUND_EFFECTS_ENABLED);
            CLONE_TO_MANAGED_PROFILE.add(TEXT_SHOW_PASSWORD);
            CLONE_TO_MANAGED_PROFILE.add(TIME_12_24);
            CLONE_FROM_PARENT_ON_VALUE.put(RINGTONE, Secure.SYNC_PARENT_SOUNDS);
            CLONE_FROM_PARENT_ON_VALUE.put(NOTIFICATION_SOUND, Secure.SYNC_PARENT_SOUNDS);
            CLONE_FROM_PARENT_ON_VALUE.put(ALARM_ALERT, Secure.SYNC_PARENT_SOUNDS);
            INSTANT_APP_SETTINGS.add(TEXT_AUTO_REPLACE);
            INSTANT_APP_SETTINGS.add(TEXT_AUTO_CAPS);
            INSTANT_APP_SETTINGS.add(TEXT_AUTO_PUNCTUATE);
            INSTANT_APP_SETTINGS.add(TEXT_SHOW_PASSWORD);
            INSTANT_APP_SETTINGS.add(DATE_FORMAT);
            INSTANT_APP_SETTINGS.add(FONT_SCALE);
            INSTANT_APP_SETTINGS.add(HAPTIC_FEEDBACK_ENABLED);
            INSTANT_APP_SETTINGS.add(TIME_12_24);
            INSTANT_APP_SETTINGS.add(SOUND_EFFECTS_ENABLED);
            INSTANT_APP_SETTINGS.add(ACCELEROMETER_ROTATION);
        }

        public static void getMovedToGlobalSettings(Set<String> outKeySet) {
            outKeySet.addAll(MOVED_TO_GLOBAL);
            outKeySet.addAll(MOVED_TO_SECURE_THEN_GLOBAL);
        }

        public static void getMovedToSecureSettings(Set<String> outKeySet) {
            outKeySet.addAll(MOVED_TO_SECURE);
        }

        public static void getNonLegacyMovedKeys(HashSet<String> outKeySet) {
            outKeySet.addAll(MOVED_TO_GLOBAL);
        }

        public static String getString(ContentResolver resolver, String name) {
            return getStringForUser(resolver, name, UserHandle.myUserId());
        }

        public static String getStringForUser(ContentResolver resolver, String name, int userHandle) {
            if (MOVED_TO_SECURE.contains(name)) {
                return Secure.getStringForUser(resolver, name, userHandle);
            }
            if (MOVED_TO_GLOBAL.contains(name) || MOVED_TO_SECURE_THEN_GLOBAL.contains(name)) {
                return Global.getStringForUser(resolver, name, userHandle);
            }
            return sNameValueCache.getStringForUser(resolver, name, userHandle);
        }

        public static boolean putString(ContentResolver resolver, String name, String value) {
            return putStringForUser(resolver, name, value, UserHandle.myUserId());
        }

        public static boolean putStringForUser(ContentResolver resolver, String name, String value, int userHandle) {
            String MDM_POLICY_INVALID_VALUE = "mdm_policy_invalid_value";
            if ("mdm_policy_invalid_value".equals(HwFrameworkFactory.getHwSettingsManager().adjustValueForMDMPolicy(resolver, name, value))) {
                Log.w(Settings.TAG, "Setting '" + name + "' set operation was prevented by current MDM policy.");
                return false;
            } else if (MOVED_TO_SECURE.contains(name)) {
                Log.w(Settings.TAG, "Setting " + name + " has moved from android.provider.Settings.System" + " to android.provider.Settings.Secure, value is unchanged.");
                return false;
            } else if (!MOVED_TO_GLOBAL.contains(name) && !MOVED_TO_SECURE_THEN_GLOBAL.contains(name)) {
                return sNameValueCache.putStringForUser(resolver, name, value, null, false, userHandle);
            } else {
                Log.w(Settings.TAG, "Setting " + name + " has moved from android.provider.Settings.System" + " to android.provider.Settings.Global, value is unchanged.");
                return false;
            }
        }

        public static Uri getUriFor(String name) {
            if (MOVED_TO_SECURE.contains(name)) {
                Log.w(Settings.TAG, "Setting " + name + " has moved from android.provider.Settings.System" + " to android.provider.Settings.Secure, returning Secure URI.");
                return NameValueTable.getUriFor(Secure.CONTENT_URI, name);
            } else if (!MOVED_TO_GLOBAL.contains(name) && !MOVED_TO_SECURE_THEN_GLOBAL.contains(name)) {
                return NameValueTable.getUriFor(CONTENT_URI, name);
            } else {
                Log.w(Settings.TAG, "Setting " + name + " has moved from android.provider.Settings.System" + " to android.provider.Settings.Global, returning read-only global URI.");
                return NameValueTable.getUriFor(Global.CONTENT_URI, name);
            }
        }

        public static int getInt(ContentResolver cr, String name, int def) {
            return getIntForUser(cr, name, def, UserHandle.myUserId());
        }

        public static int getIntForUser(ContentResolver cr, String name, int def, int userHandle) {
            String v = getStringForUser(cr, name, userHandle);
            if (v != null) {
                try {
                    def = Integer.parseInt(v);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            return def;
        }

        public static int getInt(ContentResolver cr, String name) throws SettingNotFoundException {
            return getIntForUser(cr, name, UserHandle.myUserId());
        }

        public static int getIntForUser(ContentResolver cr, String name, int userHandle) throws SettingNotFoundException {
            try {
                return Integer.parseInt(getStringForUser(cr, name, userHandle));
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
            }
        }

        public static boolean putInt(ContentResolver cr, String name, int value) {
            return putIntForUser(cr, name, value, UserHandle.myUserId());
        }

        public static boolean putIntForUser(ContentResolver cr, String name, int value, int userHandle) {
            return putStringForUser(cr, name, Integer.toString(value), userHandle);
        }

        public static long getLong(ContentResolver cr, String name, long def) {
            return getLongForUser(cr, name, def, UserHandle.myUserId());
        }

        public static long getLongForUser(ContentResolver cr, String name, long def, int userHandle) {
            String valString = getStringForUser(cr, name, userHandle);
            if (valString == null) {
                return def;
            }
            try {
                return Long.parseLong(valString);
            } catch (NumberFormatException e) {
                return def;
            }
        }

        public static long getLong(ContentResolver cr, String name) throws SettingNotFoundException {
            return getLongForUser(cr, name, UserHandle.myUserId());
        }

        public static long getLongForUser(ContentResolver cr, String name, int userHandle) throws SettingNotFoundException {
            try {
                return Long.parseLong(getStringForUser(cr, name, userHandle));
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
            }
        }

        public static boolean putLong(ContentResolver cr, String name, long value) {
            return putLongForUser(cr, name, value, UserHandle.myUserId());
        }

        public static boolean putLongForUser(ContentResolver cr, String name, long value, int userHandle) {
            return putStringForUser(cr, name, Long.toString(value), userHandle);
        }

        public static float getFloat(ContentResolver cr, String name, float def) {
            return getFloatForUser(cr, name, def, UserHandle.myUserId());
        }

        public static float getFloatForUser(ContentResolver cr, String name, float def, int userHandle) {
            String v = getStringForUser(cr, name, userHandle);
            if (v != null) {
                try {
                    def = Float.parseFloat(v);
                } catch (NumberFormatException e) {
                    return def;
                }
            }
            return def;
        }

        public static float getFloat(ContentResolver cr, String name) throws SettingNotFoundException {
            return getFloatForUser(cr, name, UserHandle.myUserId());
        }

        public static float getFloatForUser(ContentResolver cr, String name, int userHandle) throws SettingNotFoundException {
            String v = getStringForUser(cr, name, userHandle);
            if (v == null) {
                throw new SettingNotFoundException(name);
            }
            try {
                return Float.parseFloat(v);
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
            }
        }

        public static boolean putFloat(ContentResolver cr, String name, float value) {
            return putFloatForUser(cr, name, value, UserHandle.myUserId());
        }

        public static boolean putFloatForUser(ContentResolver cr, String name, float value, int userHandle) {
            return putStringForUser(cr, name, Float.toString(value), userHandle);
        }

        public static void getConfiguration(ContentResolver cr, Configuration outConfig) {
            adjustConfigurationForUser(cr, outConfig, UserHandle.myUserId(), false);
        }

        public static void adjustConfigurationForUser(ContentResolver cr, Configuration outConfig, int userHandle, boolean updateSettingsIfEmpty) {
            outConfig.fontScale = getFloatForUser(cr, FONT_SCALE, 1.0f, userHandle);
            if (outConfig.fontScale < 0.0f) {
                outConfig.fontScale = 1.0f;
            }
            String localeValue = getStringForUser(cr, SYSTEM_LOCALES, userHandle);
            if (localeValue != null) {
                outConfig.setLocales(LocaleList.forLanguageTags(localeValue));
            } else if (updateSettingsIfEmpty) {
                putStringForUser(cr, SYSTEM_LOCALES, outConfig.getLocales().toLanguageTags(), userHandle);
            }
        }

        public static void clearConfiguration(Configuration inoutConfig) {
            inoutConfig.fontScale = 0.0f;
            if (!inoutConfig.userSetLocale && (inoutConfig.getLocales().isEmpty() ^ 1) != 0) {
                inoutConfig.clearLocales();
            }
        }

        public static boolean putConfiguration(ContentResolver cr, Configuration config) {
            return putConfigurationForUser(cr, config, UserHandle.myUserId());
        }

        public static boolean putConfigurationForUser(ContentResolver cr, Configuration config, int userHandle) {
            if (putFloatForUser(cr, FONT_SCALE, config.fontScale, userHandle)) {
                return putStringForUser(cr, SYSTEM_LOCALES, config.getLocales().toLanguageTags(), userHandle);
            }
            return false;
        }

        public static boolean hasInterestingConfigurationChanges(int changes) {
            return ((1073741824 & changes) == 0 && (changes & 4) == 0) ? false : true;
        }

        @Deprecated
        public static boolean getShowGTalkServiceStatus(ContentResolver cr) {
            return getShowGTalkServiceStatusForUser(cr, UserHandle.myUserId());
        }

        @Deprecated
        public static boolean getShowGTalkServiceStatusForUser(ContentResolver cr, int userHandle) {
            return getIntForUser(cr, SHOW_GTALK_SERVICE_STATUS, 0, userHandle) != 0;
        }

        @Deprecated
        public static void setShowGTalkServiceStatus(ContentResolver cr, boolean flag) {
            setShowGTalkServiceStatusForUser(cr, flag, UserHandle.myUserId());
        }

        @Deprecated
        public static void setShowGTalkServiceStatusForUser(ContentResolver cr, boolean flag, int userHandle) {
            putIntForUser(cr, SHOW_GTALK_SERVICE_STATUS, flag ? 1 : 0, userHandle);
        }

        public static void getCloneToManagedProfileSettings(Set<String> outKeySet) {
            outKeySet.addAll(CLONE_TO_MANAGED_PROFILE);
        }

        public static void getCloneFromParentOnValueSettings(Map<String, String> outMap) {
            outMap.putAll(CLONE_FROM_PARENT_ON_VALUE);
        }

        public static boolean canWrite(Context context) {
            return Settings.isCallingPackageAllowedToWriteSettings(context, Process.myUid(), context.getOpPackageName(), false);
        }
    }

    public static void setInSystemServer() {
        synchronized (sInSystemServerLock) {
            sInSystemServer = true;
        }
    }

    public static boolean isInSystemServer() {
        boolean z;
        synchronized (sInSystemServerLock) {
            z = sInSystemServer;
        }
        return z;
    }

    public static boolean canDrawOverlays(Context context) {
        return isCallingPackageAllowedToDrawOverlays(context, Process.myUid(), context.getOpPackageName(), false);
    }

    public static String getGTalkDeviceId(long androidId) {
        return "android-" + Long.toHexString(androidId);
    }

    public static boolean isCallingPackageAllowedToWriteSettings(Context context, int uid, String callingPackage, boolean throwException) {
        return isCallingPackageAllowedToPerformAppOpsProtectedOperation(context, uid, callingPackage, throwException, 23, PM_WRITE_SETTINGS, false);
    }

    public static boolean checkAndNoteWriteSettingsOperation(Context context, int uid, String callingPackage, boolean throwException) {
        return isCallingPackageAllowedToPerformAppOpsProtectedOperation(context, uid, callingPackage, throwException, 23, PM_WRITE_SETTINGS, true);
    }

    public static boolean checkAndNoteChangeNetworkStateOperation(Context context, int uid, String callingPackage, boolean throwException) {
        if (context.checkCallingOrSelfPermission("android.permission.CHANGE_NETWORK_STATE") == 0) {
            return true;
        }
        return isCallingPackageAllowedToPerformAppOpsProtectedOperation(context, uid, callingPackage, throwException, 23, PM_CHANGE_NETWORK_STATE, true);
    }

    public static boolean isCallingPackageAllowedToDrawOverlays(Context context, int uid, String callingPackage, boolean throwException) {
        return isCallingPackageAllowedToPerformAppOpsProtectedOperation(context, uid, callingPackage, throwException, 24, PM_SYSTEM_ALERT_WINDOW, false);
    }

    public static boolean checkAndNoteDrawOverlaysOperation(Context context, int uid, String callingPackage, boolean throwException) {
        return isCallingPackageAllowedToPerformAppOpsProtectedOperation(context, uid, callingPackage, throwException, 24, PM_SYSTEM_ALERT_WINDOW, true);
    }

    public static boolean isCallingPackageAllowedToPerformAppOpsProtectedOperation(Context context, int uid, String callingPackage, boolean throwException, int appOpsOpCode, String[] permissions, boolean makeNote) {
        if (callingPackage == null) {
            return false;
        }
        int mode;
        AppOpsManager appOpsMgr = (AppOpsManager) context.getSystemService("appops");
        if (makeNote) {
            mode = appOpsMgr.noteOpNoThrow(appOpsOpCode, uid, callingPackage);
        } else {
            mode = appOpsMgr.checkOpNoThrow(appOpsOpCode, uid, callingPackage);
        }
        if (mode == 2 && ("com.huawei.runningtestii".equals(callingPackage) || "com.huawei.hidp".equals(callingPackage) || uid == 1000 || UserHandle.getAppId(uid) == 1000)) {
            return true;
        }
        switch (mode) {
            case 0:
                return true;
            case 3:
                for (String permission : permissions) {
                    if (context.checkCallingOrSelfPermission(permission) == 0) {
                        return true;
                    }
                }
                break;
        }
        if (!throwException) {
            return false;
        }
        StringBuilder exceptionMessage = new StringBuilder();
        exceptionMessage.append(callingPackage);
        exceptionMessage.append(" was not granted ");
        if (permissions.length > 1) {
            exceptionMessage.append(" either of these permissions: ");
        } else {
            exceptionMessage.append(" this permission: ");
        }
        int i = 0;
        while (i < permissions.length) {
            exceptionMessage.append(permissions[i]);
            exceptionMessage.append(i == permissions.length + -1 ? "." : ", ");
            i++;
        }
        throw new SecurityException(exceptionMessage.toString());
    }

    public static String getPackageNameForUid(Context context, int uid) {
        String[] packages = context.getPackageManager().getPackagesForUid(uid);
        if (packages == null) {
            return null;
        }
        return packages[0];
    }
}
