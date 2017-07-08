package android.provider;

import android.Manifest.permission;
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
import android.location.LocationManager;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.security.keymaster.KeymasterDefs;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.text.TextUtils;
import android.util.AndroidException;
import android.util.Log;
import android.util.MemoryIntArray;
import com.android.internal.annotations.GuardedBy;
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
    public static final String ACTION_CONDITION_PROVIDER_SETTINGS = "android.settings.ACTION_CONDITION_PROVIDER_SETTINGS";
    public static final String ACTION_DATA_ROAMING_SETTINGS = "android.settings.DATA_ROAMING_SETTINGS";
    public static final String ACTION_DATE_SETTINGS = "android.settings.DATE_SETTINGS";
    public static final String ACTION_DEVICE_INFO_SETTINGS = "android.settings.DEVICE_INFO_SETTINGS";
    public static final String ACTION_DISPLAY_SETTINGS = "android.settings.DISPLAY_SETTINGS";
    public static final String ACTION_DREAM_SETTINGS = "android.settings.DREAM_SETTINGS";
    public static final String ACTION_HARD_KEYBOARD_SETTINGS = "android.settings.HARD_KEYBOARD_SETTINGS";
    public static final String ACTION_HOME_SETTINGS = "android.settings.HOME_SETTINGS";
    public static final String ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS = "android.settings.IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS";
    public static final String ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS = "android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS";
    public static final String ACTION_INPUT_METHOD_SETTINGS = "android.settings.INPUT_METHOD_SETTINGS";
    public static final String ACTION_INPUT_METHOD_SUBTYPE_SETTINGS = "android.settings.INPUT_METHOD_SUBTYPE_SETTINGS";
    public static final String ACTION_INTERNAL_STORAGE_SETTINGS = "android.settings.INTERNAL_STORAGE_SETTINGS";
    public static final String ACTION_LOCALE_SETTINGS = "android.settings.LOCALE_SETTINGS";
    public static final String ACTION_LOCATION_SOURCE_SETTINGS = "android.settings.LOCATION_SOURCE_SETTINGS";
    public static final String ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS = "android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS";
    public static final String ACTION_MANAGE_APPLICATIONS_SETTINGS = "android.settings.MANAGE_APPLICATIONS_SETTINGS";
    public static final String ACTION_MANAGE_DEFAULT_APPS_SETTINGS = "android.settings.MANAGE_DEFAULT_APPS_SETTINGS";
    public static final String ACTION_MANAGE_OVERLAY_PERMISSION = "android.settings.action.MANAGE_OVERLAY_PERMISSION";
    public static final String ACTION_MANAGE_WRITE_SETTINGS = "android.settings.action.MANAGE_WRITE_SETTINGS";
    public static final String ACTION_MEMORY_CARD_SETTINGS = "android.settings.MEMORY_CARD_SETTINGS";
    public static final String ACTION_MONITORING_CERT_INFO = "com.android.settings.MONITORING_CERT_INFO";
    public static final String ACTION_NETWORK_OPERATOR_SETTINGS = "android.settings.NETWORK_OPERATOR_SETTINGS";
    public static final String ACTION_NFCSHARING_SETTINGS = "android.settings.NFCSHARING_SETTINGS";
    public static final String ACTION_NFC_PAYMENT_SETTINGS = "android.settings.NFC_PAYMENT_SETTINGS";
    public static final String ACTION_NFC_SETTINGS = "android.settings.NFC_SETTINGS";
    public static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    public static final String ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS = "android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS";
    public static final String ACTION_NOTIFICATION_SETTINGS = "android.settings.NOTIFICATION_SETTINGS";
    public static final String ACTION_PAIRING_SETTINGS = "android.settings.PAIRING_SETTINGS";
    public static final String ACTION_PRINT_SETTINGS = "android.settings.ACTION_PRINT_SETTINGS";
    public static final String ACTION_PRIVACY_SETTINGS = "android.settings.PRIVACY_SETTINGS";
    public static final String ACTION_QUICK_LAUNCH_SETTINGS = "android.settings.QUICK_LAUNCH_SETTINGS";
    public static final String ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS = "android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS";
    public static final String ACTION_SEARCH_SETTINGS = "android.search.action.SEARCH_SETTINGS";
    public static final String ACTION_SECURITY_SETTINGS = "android.settings.SECURITY_SETTINGS";
    public static final String ACTION_SETTINGS = "android.settings.SETTINGS";
    public static final String ACTION_SHOW_ADMIN_SUPPORT_DETAILS = "android.settings.SHOW_ADMIN_SUPPORT_DETAILS";
    public static final String ACTION_SHOW_INPUT_METHOD_PICKER = "android.settings.SHOW_INPUT_METHOD_PICKER";
    public static final String ACTION_SHOW_REGULATORY_INFO = "android.settings.SHOW_REGULATORY_INFO";
    public static final String ACTION_SHOW_REMOTE_BUGREPORT_DIALOG = "android.settings.SHOW_REMOTE_BUGREPORT_DIALOG";
    public static final String ACTION_SOUND_SETTINGS = "android.settings.SOUND_SETTINGS";
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
    public static final String CALL_METHOD_PUT_GLOBAL = "PUT_global";
    public static final String CALL_METHOD_PUT_SECURE = "PUT_secure";
    public static final String CALL_METHOD_PUT_SYSTEM = "PUT_system";
    public static final String CALL_METHOD_TRACK_GENERATION_KEY = "_track_generation";
    public static final String CALL_METHOD_USER_KEY = "_user";
    public static final String DEVICE_NAME_SETTINGS = "android.settings.DEVICE_NAME";
    public static final String EXTRA_ACCOUNT_TYPES = "account_types";
    public static final String EXTRA_AIRPLANE_MODE_ENABLED = "airplane_mode_enabled";
    public static final String EXTRA_APP_PACKAGE = "app_package";
    public static final String EXTRA_APP_UID = "app_uid";
    public static final String EXTRA_AUTHORITIES = "authorities";
    public static final String EXTRA_BATTERY_SAVER_MODE_ENABLED = "android.settings.extra.battery_saver_mode_enabled";
    public static final String EXTRA_DO_NOT_DISTURB_MODE_ENABLED = "android.settings.extra.do_not_disturb_mode_enabled";
    public static final String EXTRA_DO_NOT_DISTURB_MODE_MINUTES = "android.settings.extra.do_not_disturb_mode_minutes";
    public static final String EXTRA_INPUT_DEVICE_IDENTIFIER = "input_device_identifier";
    public static final String EXTRA_INPUT_METHOD_ID = "input_method_id";
    public static final String EXTRA_NUMBER_OF_CERTIFICATES = "android.settings.extra.number_of_certificates";
    public static final String INTENT_CATEGORY_USAGE_ACCESS_CONFIG = "android.intent.category.USAGE_ACCESS_CONFIG";
    private static final String JID_RESOURCE_PREFIX = "android";
    private static final boolean LOCAL_LOGV = false;
    public static final String METADATA_USAGE_ACCESS_REASON = "android.settings.metadata.USAGE_ACCESS_REASON";
    private static final String[] PM_CHANGE_NETWORK_STATE = null;
    private static final String[] PM_SYSTEM_ALERT_WINDOW = null;
    private static final String[] PM_WRITE_SETTINGS = null;
    private static final String TAG = "Settings";
    private static final Object mLocationSettingsLock = null;

    public static final class Bookmarks implements BaseColumns {
        public static final Uri CONTENT_URI = null;
        public static final String FOLDER = "folder";
        public static final String ID = "_id";
        public static final String INTENT = "intent";
        public static final String ORDERING = "ordering";
        public static final String SHORTCUT = "shortcut";
        private static final String TAG = "Bookmarks";
        public static final String TITLE = "title";
        private static final String[] sIntentProjection = null;
        private static final String[] sShortcutProjection = null;
        private static final String sShortcutSelection = "shortcut=?";

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.Settings.Bookmarks.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.Settings.Bookmarks.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Settings.Bookmarks.<clinit>():void");
        }

        public static Intent getIntentForShortcut(ContentResolver cr, char shortcut) {
            Intent intent = null;
            ContentResolver contentResolver = cr;
            Cursor c = contentResolver.query(CONTENT_URI, sIntentProjection, sShortcutSelection, new String[]{String.valueOf(shortcut)}, ORDERING);
            while (intent == null) {
                if (!c.moveToNext()) {
                    break;
                }
                try {
                    intent = Intent.parseUri(c.getString(c.getColumnIndexOrThrow(INTENT)), 0);
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
            if (shortcut != '\u0000') {
                cr.delete(CONTENT_URI, sShortcutSelection, new String[]{String.valueOf(shortcut)});
            }
            ContentValues values = new ContentValues();
            if (title != null) {
                values.put(TITLE, title);
            }
            if (folder != null) {
                values.put(FOLDER, folder);
            }
            values.put(INTENT, intent.toUri(0));
            if (shortcut != '\u0000') {
                values.put(SHORTCUT, Integer.valueOf(shortcut));
            }
            values.put(ORDERING, Integer.valueOf(ordering));
            return cr.insert(CONTENT_URI, values);
        }

        public static CharSequence getLabelForFolder(Resources r, String folder) {
            return folder;
        }

        public static CharSequence getTitle(Context context, Cursor cursor) {
            int titleColumn = cursor.getColumnIndex(TITLE);
            int intentColumn = cursor.getColumnIndex(INTENT);
            if (titleColumn == -1 || intentColumn == -1) {
                throw new IllegalArgumentException("The cursor must contain the TITLE and INTENT columns.");
            }
            String title = cursor.getString(titleColumn);
            if (!TextUtils.isEmpty(title)) {
                return title;
            }
            String intentUri = cursor.getString(intentColumn);
            if (TextUtils.isEmpty(intentUri)) {
                return ProxyInfo.LOCAL_EXCL_LIST;
            }
            try {
                CharSequence loadLabel;
                Intent intent = Intent.parseUri(intentUri, 0);
                PackageManager packageManager = context.getPackageManager();
                ResolveInfo info = packageManager.resolveActivity(intent, 0);
                if (info != null) {
                    loadLabel = info.loadLabel(packageManager);
                } else {
                    loadLabel = ProxyInfo.LOCAL_EXCL_LIST;
                }
                return loadLabel;
            } catch (URISyntaxException e) {
                return ProxyInfo.LOCAL_EXCL_LIST;
            }
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
                    return Settings.LOCAL_LOGV;
                }
                this.mCurrentGeneration = currentGeneration;
            }
            return true;
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

        public NameValueTable() {
        }

        protected static boolean putString(ContentResolver resolver, Uri uri, String name, String value) {
            try {
                ContentValues values = new ContentValues();
                values.put(NAME, name);
                values.put(VALUE, value);
                resolver.insert(uri, values);
                return true;
            } catch (SQLException e) {
                Log.w(Settings.TAG, "Can't set key " + name + " in " + uri, e);
                return Settings.LOCAL_LOGV;
            }
        }

        public static Uri getUriFor(Uri uri, String name) {
            return Uri.withAppendedPath(uri, name);
        }
    }

    public static final class Global extends NameValueTable {
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
        public static final String BLE_SCAN_ALWAYS_AVAILABLE = "ble_scan_always_enabled";
        public static final String BLUETOOTH_A2DP_SINK_PRIORITY_PREFIX = "bluetooth_a2dp_sink_priority_";
        public static final String BLUETOOTH_A2DP_SRC_PRIORITY_PREFIX = "bluetooth_a2dp_src_priority_";
        public static final String BLUETOOTH_DISABLED_PROFILES = "bluetooth_disabled_profiles";
        public static final String BLUETOOTH_HEADSET_PRIORITY_PREFIX = "bluetooth_headset_priority_";
        public static final String BLUETOOTH_INPUT_DEVICE_PRIORITY_PREFIX = "bluetooth_input_device_priority_";
        public static final String BLUETOOTH_INTEROPERABILITY_LIST = "bluetooth_interoperability_list";
        public static final String BLUETOOTH_MAP_PRIORITY_PREFIX = "bluetooth_map_priority_";
        public static final String BLUETOOTH_ON = "bluetooth_on";
        public static final String BLUETOOTH_PBAP_CLIENT_PRIORITY_PREFIX = "bluetooth_pbap_client_priority_";
        public static final String BLUETOOTH_SAP_PRIORITY_PREFIX = "bluetooth_sap_priority_";
        public static final String BOOT_COUNT = "boot_count";
        public static final String BUGREPORT_IN_POWER_MENU = "bugreport_in_power_menu";
        public static final String CALL_AUTO_RETRY = "call_auto_retry";
        public static final String CAPTIVE_PORTAL_DETECTION_ENABLED = "captive_portal_detection_enabled";
        public static final String CAPTIVE_PORTAL_SERVER = "captive_portal_server";
        public static final String CAPTIVE_PORTAL_USE_HTTPS = "captive_portal_use_https";
        public static final String CARRIER_APP_WHITELIST = "carrier_app_whitelist";
        public static final String CAR_DOCK_SOUND = "car_dock_sound";
        public static final String CAR_UNDOCK_SOUND = "car_undock_sound";
        public static final String CDMA_CELL_BROADCAST_SMS = "cdma_cell_broadcast_sms";
        public static final String CDMA_ROAMING_MODE = "roaming_settings";
        public static final String CDMA_SUBSCRIPTION_MODE = "subscription_mode";
        public static final String CERT_PIN_UPDATE_CONTENT_URL = "cert_pin_content_url";
        public static final String CERT_PIN_UPDATE_METADATA_URL = "cert_pin_metadata_url";
        public static final String CHARGING_SOUNDS_ENABLED = "charging_sounds_enabled";
        public static final String COMPATIBILITY_MODE = "compatibility_mode";
        public static final String CONNECTIVITY_CHANGE_DELAY = "connectivity_change_delay";
        public static final String CONNECTIVITY_SAMPLING_INTERVAL_IN_SECONDS = "connectivity_sampling_interval_in_seconds";
        @Deprecated
        public static final String CONTACT_METADATA_SYNC = "contact_metadata_sync";
        public static final String CONTACT_METADATA_SYNC_ENABLED = "contact_metadata_sync_enabled";
        public static final Uri CONTENT_URI = null;
        public static final String CURR_WALLPAPER_OFFSETS = "curr_wallpaper_offsets";
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
        public static final String DEVICE_IDLE_CONSTANTS = "device_idle_constants";
        public static final String DEVICE_IDLE_CONSTANTS_WATCH = "device_idle_constants_watch";
        public static final String DEVICE_NAME = "device_name";
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
        public static final String DOWNLOAD_MAX_BYTES_OVER_MOBILE = "download_manager_max_bytes_over_mobile";
        public static final String DOWNLOAD_RECOMMENDED_MAX_BYTES_OVER_MOBILE = "download_manager_recommended_max_bytes_over_mobile";
        public static final String DROPBOX_AGE_SECONDS = "dropbox_age_seconds";
        public static final String DROPBOX_MAX_FILES = "dropbox_max_files";
        public static final String DROPBOX_QUOTA_KB = "dropbox_quota_kb";
        public static final String DROPBOX_QUOTA_PERCENT = "dropbox_quota_percent";
        public static final String DROPBOX_RESERVE_PERCENT = "dropbox_reserve_percent";
        public static final String DROPBOX_TAG_PREFIX = "dropbox:";
        public static final String EMERGENCY_TONE = "emergency_tone";
        public static final String ENABLE_ACCESSIBILITY_GLOBAL_GESTURE_ENABLED = "enable_accessibility_global_gesture_enabled";
        public static final String ENABLE_CELLULAR_ON_BOOT = "enable_cellular_on_boot";
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
        public static final String HDMI_SYSTEM_AUDIO_ENABLED = "hdmi_system_audio_enabled";
        public static final String HEADS_UP_NOTIFICATIONS_ENABLED = "heads_up_notifications_enabled";
        public static final int HEADS_UP_OFF = 0;
        public static final int HEADS_UP_ON = 1;
        public static final String HTTP_PROXY = "http_proxy";
        public static final String INET_CONDITION_DEBOUNCE_DOWN_DELAY = "inet_condition_debounce_down_delay";
        public static final String INET_CONDITION_DEBOUNCE_UP_DELAY = "inet_condition_debounce_up_delay";
        @Deprecated
        public static final String INSTALL_NON_MARKET_APPS = "install_non_market_apps";
        public static final String INTENT_FIREWALL_UPDATE_CONTENT_URL = "intent_firewall_content_url";
        public static final String INTENT_FIREWALL_UPDATE_METADATA_URL = "intent_firewall_metadata_url";
        public static final String JOB_SCHEDULER_CONSTANTS = "job_scheduler_constants";
        public static final String LENIENT_BACKGROUND_CHECK = "lenient_background_check";
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
        private static final HashSet<String> MOVED_TO_SECURE = null;
        public static final String MULTI_SIM_DATA_CALL_SUBSCRIPTION = "multi_sim_data_call";
        public static final String MULTI_SIM_SMS_PROMPT = "multi_sim_sms_prompt";
        public static final String MULTI_SIM_SMS_SUBSCRIPTION = "multi_sim_sms";
        public static final String[] MULTI_SIM_USER_PREFERRED_SUBS = null;
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
        public static final String NETWORK_PREFERENCE = "network_preference";
        public static final String NETWORK_SCORER_APP = "network_scorer_app";
        public static final String NETWORK_SCORING_PROVISIONED = "network_scoring_provisioned";
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
        public static final String POWER_SOUNDS_ENABLED = "power_sounds_enabled";
        public static final String PREFERRED_NETWORK_MODE = "preferred_network_mode";
        public static final String PROVISIONING_APN_ALARM_DELAY_IN_MS = "provisioning_apn_alarm_delay_in_ms";
        public static final String RADIO_BLUETOOTH = "bluetooth";
        public static final String RADIO_CELL = "cell";
        public static final String RADIO_NFC = "nfc";
        public static final String RADIO_WIFI = "wifi";
        public static final String RADIO_WIMAX = "wimax";
        public static final String READ_EXTERNAL_STORAGE_ENFORCED_DEFAULT = "read_external_storage_enforced_default";
        public static final String REQUIRE_PASSWORD_TO_DECRYPT = "require_password_to_decrypt";
        public static final String SAFE_BOOT_DISALLOWED = "safe_boot_disallowed";
        public static final String SAMPLING_PROFILER_MS = "sampling_profiler_ms";
        public static final String SELINUX_STATUS = "selinux_status";
        public static final String SELINUX_UPDATE_CONTENT_URL = "selinux_content_url";
        public static final String SELINUX_UPDATE_METADATA_URL = "selinux_metadata_url";
        public static final String SEND_ACTION_APP_ERROR = "send_action_app_error";
        public static final String[] SETTINGS_TO_BACKUP = null;
        public static final String SETUP_PREPAID_DATA_SERVICE_URL = "setup_prepaid_data_service_url";
        public static final String SETUP_PREPAID_DETECTION_REDIR_HOST = "setup_prepaid_detection_redir_host";
        public static final String SETUP_PREPAID_DETECTION_TARGET_URL = "setup_prepaid_detection_target_url";
        public static final String SET_GLOBAL_HTTP_PROXY = "set_global_http_proxy";
        public static final String SET_INSTALL_LOCATION = "set_install_location";
        public static final String SHORTCUT_MANAGER_CONSTANTS = "shortcut_manager_constants";
        public static final String SHOW_PROCESSES = "show_processes";
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
        public static final String UNINSTALLED_EPHEMERAL_APP_CACHE_DURATION_MILLIS = "uninstalled_ephemeral_app_cache_duration_millis";
        public static final String UNLOCK_SOUND = "unlock_sound";
        public static final String USB_MASS_STORAGE_ENABLED = "usb_mass_storage_enabled";
        public static final String USER_SET_AIRPLANE = "user_set_airplane";
        public static final String USE_GOOGLE_MAIL = "use_google_mail";
        public static final String VT_IMS_ENABLED = "vt_ims_enabled";
        public static final String WAIT_FOR_DEBUGGER = "wait_for_debugger";
        public static final String WEBVIEW_DATA_REDUCTION_PROXY_KEY = "webview_data_reduction_proxy_key";
        public static final String WEBVIEW_FALLBACK_LOGIC_ENABLED = "webview_fallback_logic_enabled";
        public static final String WEBVIEW_MULTIPROCESS = "webview_multiprocess";
        public static final String WEBVIEW_PROVIDER = "webview_provider";
        public static final String WFC_IMS_ENABLED = "wfc_ims_enabled";
        public static final String WFC_IMS_MODE = "wfc_ims_mode";
        public static final String WFC_IMS_ROAMING_ENABLED = "wfc_ims_roaming_enabled";
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
        public static final String WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON = "wifi_networks_available_notification_on";
        public static final String WIFI_NETWORKS_AVAILABLE_REPEAT_DELAY = "wifi_networks_available_repeat_delay";
        public static final String WIFI_NETWORK_SHOW_RSSI = "wifi_network_show_rssi";
        public static final String WIFI_NUM_OPEN_NETWORKS_KEPT = "wifi_num_open_networks_kept";
        public static final String WIFI_ON = "wifi_on";
        public static final String WIFI_P2P_DEVICE_NAME = "wifi_p2p_device_name";
        public static final String WIFI_REENABLE_DELAY_MS = "wifi_reenable_delay";
        public static final String WIFI_SAVED_STATE = "wifi_saved_state";
        public static final String WIFI_SCAN_ALWAYS_AVAILABLE = "wifi_scan_always_enabled";
        public static final String WIFI_SCAN_INTERVAL_WHEN_P2P_CONNECTED_MS = "wifi_scan_interval_p2p_connected_ms";
        public static final String WIFI_SLEEP_POLICY = "wifi_sleep_policy";
        public static final int WIFI_SLEEP_POLICY_DEFAULT = 0;
        public static final int WIFI_SLEEP_POLICY_NEVER = 2;
        public static final int WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED = 1;
        public static final String WIFI_SUPPLICANT_SCAN_INTERVAL_MS = "wifi_supplicant_scan_interval_ms";
        public static final String WIFI_SUSPEND_OPTIMIZATIONS_ENABLED = "wifi_suspend_optimizations_enabled";
        public static final String WIFI_VERBOSE_LOGGING_ENABLED = "wifi_verbose_logging_enabled";
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
        private static NameValueCache sNameValueCache;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.Settings.Global.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.Settings.Global.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Settings.Global.<clinit>():void");
        }

        public Global() {
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

        public static final String getBluetoothInputDevicePriorityKey(String address) {
            return BLUETOOTH_INPUT_DEVICE_PRIORITY_PREFIX + address.toUpperCase(Locale.ROOT);
        }

        public static final String getBluetoothMapPriorityKey(String address) {
            return BLUETOOTH_MAP_PRIORITY_PREFIX + address.toUpperCase(Locale.ROOT);
        }

        public static final String getBluetoothPbapClientPriorityKey(String address) {
            return BLUETOOTH_PBAP_CLIENT_PRIORITY_PREFIX + address.toUpperCase(Locale.ROOT);
        }

        public static final String getBluetoothSapPriorityKey(String address) {
            return BLUETOOTH_SAP_PRIORITY_PREFIX + address.toUpperCase(Locale.ROOT);
        }

        public static String zenModeToString(int mode) {
            if (mode == ZEN_MODE_IMPORTANT_INTERRUPTIONS) {
                return "ZEN_MODE_IMPORTANT_INTERRUPTIONS";
            }
            if (mode == ZEN_MODE_ALARMS) {
                return "ZEN_MODE_ALARMS";
            }
            if (mode == ZEN_MODE_NO_INTERRUPTIONS) {
                return "ZEN_MODE_NO_INTERRUPTIONS";
            }
            return "ZEN_MODE_OFF";
        }

        public static boolean isValidZenMode(int value) {
            switch (value) {
                case ZEN_MODE_OFF /*0*/:
                case ZEN_MODE_IMPORTANT_INTERRUPTIONS /*1*/:
                case ZEN_MODE_NO_INTERRUPTIONS /*2*/:
                case ZEN_MODE_ALARMS /*3*/:
                    return true;
                default:
                    return Settings.LOCAL_LOGV;
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
            return putStringForUser(resolver, name, value, UserHandle.myUserId());
        }

        public static boolean putStringForUser(ContentResolver resolver, String name, String value, int userHandle) {
            HwFrameworkFactory.getHwSettingsManager().setAirplaneMode(resolver, name);
            if (!MOVED_TO_SECURE.contains(name)) {
                return sNameValueCache.putStringForUser(resolver, name, value, userHandle);
            }
            Log.w(Settings.TAG, "Setting " + name + " has moved from android.provider.Settings.Global" + " to android.provider.Settings.Secure, value is unchanged.");
            return Secure.putStringForUser(resolver, name, value, userHandle);
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
        private static final String[] SELECT_VALUE = null;
        private final String mCallGetCommand;
        private final String mCallSetCommand;
        private IContentProvider mContentProvider;
        @GuardedBy("this")
        private GenerationTracker mGenerationTracker;
        private final Uri mUri;
        private final HashMap<String, String> mValues;

        final /* synthetic */ class -java_lang_String_getStringForUser_android_content_ContentResolver_cr_java_lang_String_name_int_userHandle_LambdaImpl0 implements Runnable {
            private /* synthetic */ NameValueCache val$this;

            public /* synthetic */ -java_lang_String_getStringForUser_android_content_ContentResolver_cr_java_lang_String_name_int_userHandle_LambdaImpl0(NameValueCache nameValueCache) {
                this.val$this = nameValueCache;
            }

            public void run() {
                this.val$this.-android_provider_Settings$NameValueCache_lambda$1();
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.Settings.NameValueCache.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.Settings.NameValueCache.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Settings.NameValueCache.<clinit>():void");
        }

        public NameValueCache(Uri uri, String getCommand, String setCommand) {
            this.mValues = new HashMap();
            this.mContentProvider = null;
            this.mUri = uri;
            this.mCallGetCommand = getCommand;
            this.mCallSetCommand = setCommand;
        }

        private IContentProvider lazyGetProvider(ContentResolver cr) {
            IContentProvider cp;
            synchronized (this) {
                cp = this.mContentProvider;
                if (cp == null) {
                    IContentProvider cp2 = cr.acquireProvider(this.mUri.getAuthority());
                    this.mContentProvider = cp2;
                    cp = cp2;
                }
            }
            return cp;
        }

        public boolean putStringForUser(ContentResolver cr, String name, String value, int userHandle) {
            try {
                Bundle arg = new Bundle();
                arg.putString(NameValueTable.VALUE, value);
                arg.putInt(Settings.CALL_METHOD_USER_KEY, userHandle);
                lazyGetProvider(cr).call(cr.getPackageName(), this.mCallSetCommand, name, arg);
                return true;
            } catch (RemoteException e) {
                Log.w(Settings.TAG, "Can't set key " + name + " in " + this.mUri, e);
                return DEBUG;
            }
        }

        public String getStringForUser(ContentResolver cr, String name, int userHandle) {
            String value;
            Throwable th;
            boolean isSelf = userHandle == UserHandle.myUserId() ? true : DEBUG;
            if (isSelf) {
                synchronized (this) {
                    if (this.mGenerationTracker != null) {
                        if (this.mGenerationTracker.isGenerationChanged()) {
                            this.mValues.clear();
                        } else if (this.mValues.containsKey(name)) {
                            String str = (String) this.mValues.get(name);
                            return str;
                        }
                    }
                }
            }
            IContentProvider cp = lazyGetProvider(cr);
            if (this.mCallGetCommand != null) {
                Bundle args;
                if (isSelf) {
                    args = null;
                } else {
                    try {
                        args = new Bundle();
                        try {
                            args.putInt(Settings.CALL_METHOD_USER_KEY, userHandle);
                        } catch (RemoteException e) {
                        }
                    } catch (RemoteException e2) {
                    }
                }
                boolean needsGenerationTracker = DEBUG;
                synchronized (this) {
                    Bundle args2;
                    Bundle b;
                    MemoryIntArray array;
                    int index;
                    NameValueCache nameValueCache;
                    if (isSelf) {
                        try {
                            if (this.mGenerationTracker == null) {
                                needsGenerationTracker = true;
                                if (args == null) {
                                    args2 = new Bundle();
                                } else {
                                    args2 = args;
                                }
                                try {
                                    args2.putString(Settings.CALL_METHOD_TRACK_GENERATION_KEY, null);
                                    b = cp.call(cr.getPackageName(), this.mCallGetCommand, name, args2);
                                    if (b != null) {
                                        value = b.getString(NameValueTable.VALUE);
                                        if (isSelf) {
                                            synchronized (this) {
                                                if (needsGenerationTracker) {
                                                    array = (MemoryIntArray) b.getParcelable(Settings.CALL_METHOD_TRACK_GENERATION_KEY);
                                                    index = b.getInt(Settings.CALL_METHOD_GENERATION_INDEX_KEY, -1);
                                                    if (array != null && index >= 0) {
                                                        nameValueCache = this;
                                                        this.mGenerationTracker = new GenerationTracker(array, index, b.getInt(Settings.CALL_METHOD_GENERATION_KEY, 0), new -java_lang_String_getStringForUser_android_content_ContentResolver_cr_java_lang_String_name_int_userHandle_LambdaImpl0());
                                                    }
                                                }
                                                this.mValues.put(name, value);
                                            }
                                        }
                                        return value;
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            throw th;
                        }
                    }
                    args2 = args;
                    b = cp.call(cr.getPackageName(), this.mCallGetCommand, name, args2);
                    if (b != null) {
                        value = b.getString(NameValueTable.VALUE);
                        if (isSelf) {
                            synchronized (this) {
                                if (needsGenerationTracker) {
                                    array = (MemoryIntArray) b.getParcelable(Settings.CALL_METHOD_TRACK_GENERATION_KEY);
                                    index = b.getInt(Settings.CALL_METHOD_GENERATION_INDEX_KEY, -1);
                                    nameValueCache = this;
                                    this.mGenerationTracker = new GenerationTracker(array, index, b.getInt(Settings.CALL_METHOD_GENERATION_KEY, 0), new -java_lang_String_getStringForUser_android_content_ContentResolver_cr_java_lang_String_name_int_userHandle_LambdaImpl0());
                                }
                                this.mValues.put(name, value);
                            }
                        }
                        return value;
                    }
                }
            }
            Cursor cursor = null;
            try {
                cursor = cp.query(cr.getPackageName(), this.mUri, SELECT_VALUE, NAME_EQ_PLACEHOLDER, new String[]{name}, null, null);
                if (cursor == null) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    return null;
                }
                value = cursor.moveToNext() ? cursor.getString(0) : null;
                synchronized (this) {
                    this.mValues.put(name, value);
                }
                if (cursor != null) {
                    cursor.close();
                }
                return value;
            } catch (RemoteException e3) {
                try {
                    Log.w(Settings.TAG, "Can't get key " + name + " from " + this.mUri, e3);
                    return null;
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }

        /* synthetic */ void -android_provider_Settings$NameValueCache_lambda$1() {
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
        public static final String ACCESSIBILITY_DISPLAY_COLOR_MATRIX = "accessibility_display_color_matrix";
        public static final String ACCESSIBILITY_DISPLAY_DALTONIZER = "accessibility_display_daltonizer";
        public static final String ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED = "accessibility_display_daltonizer_enabled";
        public static final String ACCESSIBILITY_DISPLAY_INVERSION_ENABLED = "accessibility_display_inversion_enabled";
        public static final String ACCESSIBILITY_DISPLAY_MAGNIFICATION_AUTO_UPDATE = "accessibility_display_magnification_auto_update";
        public static final String ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED = "accessibility_display_magnification_enabled";
        public static final String ACCESSIBILITY_DISPLAY_MAGNIFICATION_SCALE = "accessibility_display_magnification_scale";
        public static final String ACCESSIBILITY_ENABLED = "accessibility_enabled";
        public static final String ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED = "high_text_contrast_enabled";
        public static final String ACCESSIBILITY_LARGE_POINTER_ICON = "accessibility_large_pointer_icon";
        public static final String ACCESSIBILITY_SCREEN_READER_URL = "accessibility_script_injection_url";
        public static final String ACCESSIBILITY_SCRIPT_INJECTION = "accessibility_script_injection";
        public static final String ACCESSIBILITY_SOFT_KEYBOARD_MODE = "accessibility_soft_keyboard_mode";
        public static final String ACCESSIBILITY_SPEAK_PASSWORD = "speak_password";
        public static final String ACCESSIBILITY_WEB_CONTENT_KEY_BINDINGS = "accessibility_web_content_key_bindings";
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
        public static final String ASSIST_SCREENSHOT_ENABLED = "assist_screenshot_enabled";
        public static final String ASSIST_STRUCTURE_ENABLED = "assist_structure_enabled";
        @Deprecated
        public static final String BACKGROUND_DATA = "background_data";
        public static final String BACKUP_AUTO_RESTORE = "backup_auto_restore";
        public static final String BACKUP_ENABLED = "backup_enabled";
        public static final String BACKUP_PROVISIONED = "backup_provisioned";
        public static final String BACKUP_TRANSPORT = "backup_transport";
        public static final String BAR_SERVICE_COMPONENT = "bar_service_component";
        public static final String BLUETOOTH_HCI_LOG = "bluetooth_hci_log";
        @Deprecated
        public static final String BLUETOOTH_ON = "bluetooth_on";
        public static final String BRIGHTNESS_USE_TWILIGHT = "brightness_use_twilight";
        @Deprecated
        public static final String BUGREPORT_IN_POWER_MENU = "bugreport_in_power_menu";
        public static final String CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED = "camera_double_tap_power_gesture_disabled";
        public static final String CAMERA_GESTURE_DISABLED = "camera_gesture_disabled";
        public static final String CHANGE_RINGER_MODE_PKGS = "change_ringer_mode_pkgs";
        private static final Set<String> CLONE_TO_MANAGED_PROFILE = null;
        public static final String COMPLETED_CATEGORY_PREFIX = "suggested.completed_category.";
        public static final String CONNECTIVITY_RELEASE_PENDING_INTENT_DELAY_MS = "connectivity_release_pending_intent_delay_ms";
        public static final Uri CONTENT_URI = null;
        @Deprecated
        public static final String DATA_ROAMING = "data_roaming";
        public static final String DEFAULT_INPUT_METHOD = "default_input_method";
        @Deprecated
        public static final String DEVELOPMENT_SETTINGS_ENABLED = "development_settings_enabled";
        @Deprecated
        public static final String DEVICE_PROVISIONED = "device_provisioned";
        public static final String DIALER_DEFAULT_APPLICATION = "dialer_default_application";
        public static final String DISABLED_PRINT_SERVICES = "disabled_print_services";
        public static final String DISABLED_SYSTEM_INPUT_METHODS = "disabled_system_input_methods";
        public static final String DISPLAY_DENSITY_FORCED = "display_density_forced";
        public static final String DOUBLE_TAP_TO_WAKE = "double_tap_to_wake";
        public static final String DOZE_ENABLED = "doze_enabled";
        public static final String EMERGENCY_ASSISTANCE_APPLICATION = "emergency_assistance_application";
        public static final String ENABLED_ACCESSIBILITY_SERVICES = "enabled_accessibility_services";
        public static final String ENABLED_INPUT_METHODS = "enabled_input_methods";
        public static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
        public static final String ENABLED_NOTIFICATION_POLICY_ACCESS_PACKAGES = "enabled_notification_policy_access_packages";
        public static final String ENABLED_PRINT_SERVICES = "enabled_print_services";
        public static final String ENABLED_VR_LISTENERS = "enabled_vr_listeners";
        public static final String ENHANCED_VOICE_PRIVACY_ENABLED = "enhanced_voice_privacy_enabled";
        @Deprecated
        public static final String HTTP_PROXY = "http_proxy";
        public static final String IMMERSIVE_MODE_CONFIRMATIONS = "immersive_mode_confirmations";
        public static final String INCALL_POWER_BUTTON_BEHAVIOR = "incall_power_button_behavior";
        public static final int INCALL_POWER_BUTTON_BEHAVIOR_DEFAULT = 1;
        public static final int INCALL_POWER_BUTTON_BEHAVIOR_HANGUP = 2;
        public static final int INCALL_POWER_BUTTON_BEHAVIOR_SCREEN_OFF = 1;
        public static final String INPUT_METHODS_SUBTYPE_HISTORY = "input_methods_subtype_history";
        public static final String INPUT_METHOD_SELECTOR_VISIBILITY = "input_method_selector_visibility";
        public static final String INSTALL_NON_MARKET_APPS = "install_non_market_apps";
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
        public static final String LOCK_SCREEN_OWNER_INFO = "lock_screen_owner_info";
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
        private static final HashSet<String> MOVED_TO_GLOBAL = null;
        private static final HashSet<String> MOVED_TO_LOCK_SETTINGS = null;
        @Deprecated
        public static final String NETWORK_PREFERENCE = "network_preference";
        public static final String NFC_PAYMENT_DEFAULT_COMPONENT = "nfc_payment_default_component";
        public static final String NFC_PAYMENT_FOREGROUND = "nfc_payment_foreground";
        public static final String PACKAGE_VERIFIER_USER_CONSENT = "package_verifier_user_consent";
        public static final String PARENTAL_CONTROL_ENABLED = "parental_control_enabled";
        public static final String PARENTAL_CONTROL_LAST_UPDATE = "parental_control_last_update";
        public static final String PARENTAL_CONTROL_REDIRECT_URL = "parental_control_redirect_url";
        public static final String PAYMENT_SERVICE_SEARCH_URI = "payment_service_search_uri";
        public static final String PREFERRED_TTY_MODE = "preferred_tty_mode";
        public static final String PRINT_SERVICE_SEARCH_URI = "print_service_search_uri";
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
        public static final String[] SETTINGS_TO_BACKUP = null;
        public static final String SHOW_IME_WITH_HARD_KEYBOARD = "show_ime_with_hard_keyboard";
        public static final int SHOW_MODE_AUTO = 0;
        public static final int SHOW_MODE_HIDDEN = 1;
        public static final String SHOW_NOTE_ABOUT_NOTIFICATION_HIDING = "show_note_about_notification_hiding";
        public static final String SKIP_FIRST_USE_HINTS = "skip_first_use_hints";
        public static final String SLEEP_TIMEOUT = "sleep_timeout";
        public static final String SMS_DEFAULT_APPLICATION = "sms_default_application";
        public static final String SPELL_CHECKER_ENABLED = "spell_checker_enabled";
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
        public static final String TWILIGHT_MODE = "twilight_mode";
        public static final int TWILIGHT_MODE_AUTO = 2;
        public static final int TWILIGHT_MODE_AUTO_OVERRIDE_OFF = 3;
        public static final int TWILIGHT_MODE_AUTO_OVERRIDE_ON = 4;
        public static final int TWILIGHT_MODE_LOCKED_OFF = 0;
        public static final int TWILIGHT_MODE_LOCKED_ON = 1;
        public static final String UI_NIGHT_MODE = "ui_night_mode";
        public static final String UNSAFE_VOLUME_MUSIC_ACTIVE_MS = "unsafe_volume_music_active_ms";
        public static final String USB_AUDIO_AUTOMATIC_ROUTING_DISABLED = "usb_audio_automatic_routing_disabled";
        @Deprecated
        public static final String USB_MASS_STORAGE_ENABLED = "usb_mass_storage_enabled";
        public static final String USER_SETUP_COMPLETE = "user_setup_complete";
        @Deprecated
        public static final String USE_GOOGLE_MAIL = "use_google_mail";
        public static final String VOICE_INTERACTION_SERVICE = "voice_interaction_service";
        public static final String VOICE_RECOGNITION_SERVICE = "voice_recognition_service";
        public static final String VOLUME_CONTROLLER_SERVICE_COMPONENT = "volume_controller_service_component";
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
        private static ILockSettings sLockSettings;
        private static final NameValueCache sNameValueCache = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.Settings.Secure.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.Settings.Secure.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Settings.Secure.<clinit>():void");
        }

        public Secure() {
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
                        sIsSystemProcess = Process.myUid() == Process.SYSTEM_UID ? true : Settings.LOCAL_LOGV;
                    }
                }
                if (!(sLockSettings == null || sIsSystemProcess)) {
                    Application application = ActivityThread.currentApplication();
                    boolean isPreMnc = (application == null || application.getApplicationInfo() == null) ? Settings.LOCAL_LOGV : application.getApplicationInfo().targetSdkVersion <= 22 ? true : Settings.LOCAL_LOGV;
                    if (isPreMnc) {
                        try {
                            return sLockSettings.getString(name, WifiEnterpriseConfig.ENGINE_DISABLE, userHandle);
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
            if (LOCATION_MODE.equals(name)) {
                return setLocationModeForUser(resolver, Integer.parseInt(value), userHandle);
            }
            if (!MOVED_TO_GLOBAL.contains(name)) {
                return sNameValueCache.putStringForUser(resolver, name, value, userHandle);
            }
            Log.w(Settings.TAG, "Setting " + name + " has moved from android.provider.Settings.System" + " to android.provider.Settings.Global");
            return Global.putStringForUser(resolver, name, value, userHandle);
        }

        public static Uri getUriFor(String name) {
            if (!MOVED_TO_GLOBAL.contains(name)) {
                return NameValueTable.getUriFor(CONTENT_URI, name);
            }
            Log.w(Settings.TAG, "Setting " + name + " has moved from android.provider.Settings.Secure" + " to android.provider.Settings.Global, returning global URI.");
            return NameValueTable.getUriFor(Global.CONTENT_URI, name);
        }

        public static int getInt(ContentResolver cr, String name, int def) {
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
            return TextUtils.delimitedStringContains(getStringForUser(cr, LOCATION_PROVIDERS_ALLOWED, userId), ',', provider);
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
                    provider = "+" + provider;
                } else {
                    provider = "-" + provider;
                }
                putStringForUser = putStringForUser(cr, LOCATION_PROVIDERS_ALLOWED, provider, userId);
            }
            return putStringForUser;
        }

        private static final boolean saveLocationModeForUser(ContentResolver cr, int userId) {
            return putIntForUser(cr, LOCATION_PREVIOUS_MODE, getLocationModeForUser(cr, userId), userId);
        }

        private static final boolean restoreLocationModeForUser(ContentResolver cr, int userId) {
            int mode = getIntForUser(cr, LOCATION_PREVIOUS_MODE, TWILIGHT_MODE_AUTO_OVERRIDE_OFF, userId);
            if (mode == 0) {
                mode = TWILIGHT_MODE_AUTO_OVERRIDE_OFF;
            }
            return setLocationModeForUser(cr, mode, userId);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private static final boolean setLocationModeForUser(ContentResolver cr, int mode, int userId) {
            synchronized (Settings.mLocationSettingsLock) {
                boolean gps = Settings.LOCAL_LOGV;
                boolean network = Settings.LOCAL_LOGV;
                switch (mode) {
                    case LOCATION_MODE_PREVIOUS /*-1*/:
                        boolean restoreLocationModeForUser = restoreLocationModeForUser(cr, userId);
                        return restoreLocationModeForUser;
                    case VR_DISPLAY_MODE_LOW_PERSISTENCE /*0*/:
                        saveLocationModeForUser(cr, userId);
                        break;
                    case VR_DISPLAY_MODE_OFF /*1*/:
                        gps = true;
                        break;
                    case TWILIGHT_MODE_AUTO /*2*/:
                        network = true;
                        break;
                    case TWILIGHT_MODE_AUTO_OVERRIDE_OFF /*3*/:
                        gps = true;
                        network = true;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid location mode: " + mode);
                }
            }
        }

        private static final int getLocationModeForUser(ContentResolver cr, int userId) {
            synchronized (Settings.mLocationSettingsLock) {
                boolean gpsEnabled = isLocationProviderEnabledForUser(cr, LocationManager.GPS_PROVIDER, userId);
                boolean networkEnabled = isLocationProviderEnabledForUser(cr, LocationManager.NETWORK_PROVIDER, userId);
                if (gpsEnabled && networkEnabled) {
                    return TWILIGHT_MODE_AUTO_OVERRIDE_OFF;
                } else if (gpsEnabled) {
                    return VR_DISPLAY_MODE_OFF;
                } else if (networkEnabled) {
                    return TWILIGHT_MODE_AUTO;
                } else {
                    return VR_DISPLAY_MODE_LOW_PERSISTENCE;
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
        public static final Validator ACCELEROMETER_ROTATION_VALIDATOR = null;
        @Deprecated
        public static final String ADB_ENABLED = "adb_enabled";
        public static final String ADVANCED_SETTINGS = "advanced_settings";
        public static final int ADVANCED_SETTINGS_DEFAULT = 0;
        private static final Validator ADVANCED_SETTINGS_VALIDATOR = null;
        @Deprecated
        public static final String AIRPLANE_MODE_ON = "airplane_mode_on";
        @Deprecated
        public static final String AIRPLANE_MODE_RADIOS = "airplane_mode_radios";
        @Deprecated
        public static final String AIRPLANE_MODE_TOGGLEABLE_RADIOS = "airplane_mode_toggleable_radios";
        public static final String ALARM_ALERT = "alarm_alert";
        public static final String ALARM_ALERT_CACHE = "alarm_alert_cache";
        public static final Uri ALARM_ALERT_CACHE_URI = null;
        private static final Validator ALARM_ALERT_VALIDATOR = null;
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
        private static final Validator BLUETOOTH_DISCOVERABILITY_TIMEOUT_VALIDATOR = null;
        private static final Validator BLUETOOTH_DISCOVERABILITY_VALIDATOR = null;
        @Deprecated
        public static final String BLUETOOTH_ON = "bluetooth_on";
        @Deprecated
        public static final String CAR_DOCK_SOUND = "car_dock_sound";
        @Deprecated
        public static final String CAR_UNDOCK_SOUND = "car_undock_sound";
        private static final Set<String> CLONE_TO_MANAGED_PROFILE = null;
        public static final Uri CONTENT_URI = null;
        @Deprecated
        public static final String DATA_ROAMING = "data_roaming";
        public static final String DATE_FORMAT = "date_format";
        public static final Validator DATE_FORMAT_VALIDATOR = null;
        @Deprecated
        public static final String DEBUG_APP = "debug_app";
        public static final Uri DEFAULT_ALARM_ALERT_URI = null;
        private static final float DEFAULT_FONT_SCALE = 1.0f;
        public static final Uri DEFAULT_NOTIFICATION_URI = null;
        public static final Uri DEFAULT_RINGTONE_URI = null;
        @Deprecated
        public static final String DESK_DOCK_SOUND = "desk_dock_sound";
        @Deprecated
        public static final String DESK_UNDOCK_SOUND = "desk_undock_sound";
        @Deprecated
        public static final String DEVICE_PROVISIONED = "device_provisioned";
        @Deprecated
        public static final String DIM_SCREEN = "dim_screen";
        private static final Validator DIM_SCREEN_VALIDATOR = null;
        @Deprecated
        public static final String DOCK_SOUNDS_ENABLED = "dock_sounds_enabled";
        public static final String DTMF_TONE_TYPE_WHEN_DIALING = "dtmf_tone_type";
        public static final Validator DTMF_TONE_TYPE_WHEN_DIALING_VALIDATOR = null;
        public static final String DTMF_TONE_WHEN_DIALING = "dtmf_tone";
        public static final Validator DTMF_TONE_WHEN_DIALING_VALIDATOR = null;
        public static final String DTS_MODE = "dts_mode";
        public static final String EGG_MODE = "egg_mode";
        public static final Validator EGG_MODE_VALIDATOR = null;
        public static final String END_BUTTON_BEHAVIOR = "end_button_behavior";
        public static final int END_BUTTON_BEHAVIOR_DEFAULT = 2;
        public static final int END_BUTTON_BEHAVIOR_HOME = 1;
        public static final int END_BUTTON_BEHAVIOR_SLEEP = 2;
        private static final Validator END_BUTTON_BEHAVIOR_VALIDATOR = null;
        public static final String FIRST_DAY_OF_WEEK = "first_day_of_week";
        public static final String FONT_SCALE = "font_scale";
        private static final Validator FONT_SCALE_VALIDATOR = null;
        public static final String HAPTIC_FEEDBACK_ENABLED = "haptic_feedback_enabled";
        public static final Validator HAPTIC_FEEDBACK_ENABLED_VALIDATOR = null;
        public static final String HEARING_AID = "hearing_aid";
        public static final Validator HEARING_AID_VALIDATOR = null;
        public static final String HIDE_ROTATION_LOCK_TOGGLE_FOR_ACCESSIBILITY = "hide_rotation_lock_toggle_for_accessibility";
        public static final Validator HIDE_ROTATION_LOCK_TOGGLE_FOR_ACCESSIBILITY_VALIDATOR = null;
        @Deprecated
        public static final String HTTP_PROXY = "http_proxy";
        public static final Uri HUAWEI_RINGTONE2_URI = null;
        public static final String HW_SCREEN_AUTO_BRIGHTNESS_ADJ = "hw_screen_auto_brightness_adj";
        @Deprecated
        public static final String INSTALL_NON_MARKET_APPS = "install_non_market_apps";
        public static final String KEY_CONTENT_HDB_ALLOWED = "hdb_enabled";
        @Deprecated
        public static final String LOCATION_PROVIDERS_ALLOWED = "location_providers_allowed";
        public static final String LOCKSCREEN_DISABLED = "lockscreen.disabled";
        public static final Validator LOCKSCREEN_DISABLED_VALIDATOR = null;
        public static final String LOCKSCREEN_SOUNDS_ENABLED = "lockscreen_sounds_enabled";
        public static final Validator LOCKSCREEN_SOUNDS_ENABLED_VALIDATOR = null;
        @Deprecated
        public static final String LOCK_PATTERN_ENABLED = "lock_pattern_autolock";
        @Deprecated
        public static final String LOCK_PATTERN_TACTILE_FEEDBACK_ENABLED = "lock_pattern_tactile_feedback_enabled";
        @Deprecated
        public static final String LOCK_PATTERN_VISIBLE = "lock_pattern_visible_pattern";
        @Deprecated
        public static final String LOCK_SOUND = "lock_sound";
        public static final String LOCK_TO_APP_ENABLED = "lock_to_app_enabled";
        public static final Validator LOCK_TO_APP_ENABLED_VALIDATOR = null;
        @Deprecated
        public static final String LOGGING_ID = "logging_id";
        @Deprecated
        public static final String LOW_BATTERY_SOUND = "low_battery_sound";
        public static final String MASTER_MONO = "master_mono";
        private static final Validator MASTER_MONO_VALIDATOR = null;
        public static final String MEDIA_BUTTON_RECEIVER = "media_button_receiver";
        private static final Validator MEDIA_BUTTON_RECEIVER_VALIDATOR = null;
        public static final String MICROPHONE_MUTE = "microphone_mute";
        private static final Validator MICROPHONE_MUTE_VALIDATOR = null;
        @Deprecated
        public static final String MODE_RINGER = "mode_ringer";
        public static final String MODE_RINGER_STREAMS_AFFECTED = "mode_ringer_streams_affected";
        private static final Validator MODE_RINGER_STREAMS_AFFECTED_VALIDATOR = null;
        private static final HashSet<String> MOVED_TO_GLOBAL = null;
        private static final HashSet<String> MOVED_TO_SECURE = null;
        private static final HashSet<String> MOVED_TO_SECURE_THEN_GLOBAL = null;
        public static final String MUTE_STREAMS_AFFECTED = "mute_streams_affected";
        private static final Validator MUTE_STREAMS_AFFECTED_VALIDATOR = null;
        @Deprecated
        public static final String NETWORK_PREFERENCE = "network_preference";
        @Deprecated
        public static final String NEXT_ALARM_FORMATTED = "next_alarm_formatted";
        private static final Validator NEXT_ALARM_FORMATTED_VALIDATOR = null;
        @Deprecated
        public static final String NOTIFICATIONS_USE_RING_VOLUME = "notifications_use_ring_volume";
        private static final Validator NOTIFICATIONS_USE_RING_VOLUME_VALIDATOR = null;
        public static final String NOTIFICATION_LIGHT_PULSE = "notification_light_pulse";
        public static final Validator NOTIFICATION_LIGHT_PULSE_VALIDATOR = null;
        public static final String NOTIFICATION_SOUND = "notification_sound";
        public static final String NOTIFICATION_SOUND_CACHE = "notification_sound_cache";
        public static final Uri NOTIFICATION_SOUND_CACHE_URI = null;
        private static final Validator NOTIFICATION_SOUND_VALIDATOR = null;
        @Deprecated
        public static final String PARENTAL_CONTROL_ENABLED = "parental_control_enabled";
        @Deprecated
        public static final String PARENTAL_CONTROL_LAST_UPDATE = "parental_control_last_update";
        @Deprecated
        public static final String PARENTAL_CONTROL_REDIRECT_URL = "parental_control_redirect_url";
        public static final String POINTER_LOCATION = "pointer_location";
        public static final Validator POINTER_LOCATION_VALIDATOR = null;
        public static final String POINTER_SPEED = "pointer_speed";
        public static final Validator POINTER_SPEED_VALIDATOR = null;
        @Deprecated
        public static final String POWER_SOUNDS_ENABLED = "power_sounds_enabled";
        public static final Set<String> PRIVATE_SETTINGS = null;
        public static final Set<String> PUBLIC_SETTINGS = null;
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
        public static final Uri RINGTONE2_CACHE_URI = null;
        public static final String RINGTONE_CACHE = "ringtone_cache";
        public static final Uri RINGTONE_CACHE_URI = null;
        private static final Validator RINGTONE_VALIDATOR = null;
        public static final String SCREEN_AUTO_BRIGHTNESS = "screen_auto_brightness";
        public static final String SCREEN_AUTO_BRIGHTNESS_ADJ = "screen_auto_brightness_adj";
        private static final Validator SCREEN_AUTO_BRIGHTNESS_ADJ_VALIDATOR = null;
        public static final String SCREEN_BRIGHTNESS = "screen_brightness";
        public static final String SCREEN_BRIGHTNESS_MODE = "screen_brightness_mode";
        public static final int SCREEN_BRIGHTNESS_MODE_AUTOMATIC = 1;
        public static final int SCREEN_BRIGHTNESS_MODE_MANUAL = 0;
        private static final Validator SCREEN_BRIGHTNESS_MODE_VALIDATOR = null;
        private static final Validator SCREEN_BRIGHTNESS_VALIDATOR = null;
        public static final String SCREEN_OFF_TIMEOUT = "screen_off_timeout";
        private static final Validator SCREEN_OFF_TIMEOUT_VALIDATOR = null;
        @Deprecated
        public static final String SETTINGS_CLASSNAME = "settings_classname";
        public static final String[] SETTINGS_TO_BACKUP = null;
        public static final String SETUP_WIZARD_HAS_RUN = "setup_wizard_has_run";
        public static final Validator SETUP_WIZARD_HAS_RUN_VALIDATOR = null;
        public static final String SHOW_GTALK_SERVICE_STATUS = "SHOW_GTALK_SERVICE_STATUS";
        private static final Validator SHOW_GTALK_SERVICE_STATUS_VALIDATOR = null;
        @Deprecated
        public static final String SHOW_PROCESSES = "show_processes";
        public static final String SHOW_TOUCHES = "show_touches";
        public static final Validator SHOW_TOUCHES_VALIDATOR = null;
        @Deprecated
        public static final String SHOW_WEB_SUGGESTIONS = "show_web_suggestions";
        public static final Validator SHOW_WEB_SUGGESTIONS_VALIDATOR = null;
        public static final String SIMPLEUI_MODE = "simpleui_mode";
        public static final String SIP_ADDRESS_ONLY = "SIP_ADDRESS_ONLY";
        public static final Validator SIP_ADDRESS_ONLY_VALIDATOR = null;
        public static final String SIP_ALWAYS = "SIP_ALWAYS";
        public static final Validator SIP_ALWAYS_VALIDATOR = null;
        @Deprecated
        public static final String SIP_ASK_ME_EACH_TIME = "SIP_ASK_ME_EACH_TIME";
        public static final Validator SIP_ASK_ME_EACH_TIME_VALIDATOR = null;
        public static final String SIP_CALL_OPTIONS = "sip_call_options";
        public static final Validator SIP_CALL_OPTIONS_VALIDATOR = null;
        public static final String SIP_RECEIVE_CALLS = "sip_receive_calls";
        public static final Validator SIP_RECEIVE_CALLS_VALIDATOR = null;
        public static final String SMART_BACKLIGHT = "smart_backlight_enable";
        public static final String SOUND_EFFECTS_ENABLED = "sound_effects_enabled";
        public static final Validator SOUND_EFFECTS_ENABLED_VALIDATOR = null;
        public static final String SPLINE_AMBIENTLUX = "spline_ambient_lux";
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
        private static final Validator TEXT_AUTO_CAPS_VALIDATOR = null;
        public static final String TEXT_AUTO_PUNCTUATE = "auto_punctuate";
        private static final Validator TEXT_AUTO_PUNCTUATE_VALIDATOR = null;
        public static final String TEXT_AUTO_REPLACE = "auto_replace";
        private static final Validator TEXT_AUTO_REPLACE_VALIDATOR = null;
        public static final String TEXT_SHOW_PASSWORD = "show_password";
        private static final Validator TEXT_SHOW_PASSWORD_VALIDATOR = null;
        public static final String TIME_12_24 = "time_12_24";
        public static final Validator TIME_12_24_VALIDATOR = null;
        @Deprecated
        public static final String TRANSITION_ANIMATION_SCALE = "transition_animation_scale";
        public static final String TTY_MODE = "tty_mode";
        public static final Validator TTY_MODE_VALIDATOR = null;
        @Deprecated
        public static final String UNLOCK_SOUND = "unlock_sound";
        @Deprecated
        public static final String USB_MASS_STORAGE_ENABLED = "usb_mass_storage_enabled";
        public static final String USER_ROTATION = "user_rotation";
        public static final Validator USER_ROTATION_VALIDATOR = null;
        @Deprecated
        public static final String USE_GOOGLE_MAIL = "use_google_mail";
        public static final Map<String, Validator> VALIDATORS = null;
        public static final String VIBRATE_INPUT_DEVICES = "vibrate_input_devices";
        private static final Validator VIBRATE_INPUT_DEVICES_VALIDATOR = null;
        public static final String VIBRATE_IN_SILENT = "vibrate_in_silent";
        private static final Validator VIBRATE_IN_SILENT_VALIDATOR = null;
        public static final String VIBRATE_ON = "vibrate_on";
        private static final Validator VIBRATE_ON_VALIDATOR = null;
        public static final String VIBRATE_WHEN_RINGING = "vibrate_when_ringing";
        public static final Validator VIBRATE_WHEN_RINGING_VALIDATOR = null;
        public static final String VOLUME_ALARM = "volume_alarm";
        public static final String VOLUME_BLUETOOTH_SCO = "volume_bluetooth_sco";
        public static final String VOLUME_MASTER = "volume_master";
        public static final String VOLUME_MASTER_MUTE = "volume_master_mute";
        private static final Validator VOLUME_MASTER_MUTE_VALIDATOR = null;
        public static final String VOLUME_MUSIC = "volume_music";
        public static final String VOLUME_NOTIFICATION = "volume_notification";
        public static final String VOLUME_RING = "volume_ring";
        public static final String[] VOLUME_SETTINGS = null;
        public static final String VOLUME_SYSTEM = "volume_system";
        public static final String VOLUME_VOICE = "volume_voice";
        @Deprecated
        public static final String WAIT_FOR_DEBUGGER = "wait_for_debugger";
        @Deprecated
        public static final String WALLPAPER_ACTIVITY = "wallpaper_activity";
        private static final Validator WALLPAPER_ACTIVITY_VALIDATOR = null;
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
        private static final Validator WIFI_STATIC_DNS1_VALIDATOR = null;
        @Deprecated
        public static final String WIFI_STATIC_DNS2 = "wifi_static_dns2";
        private static final Validator WIFI_STATIC_DNS2_VALIDATOR = null;
        @Deprecated
        public static final String WIFI_STATIC_GATEWAY = "wifi_static_gateway";
        private static final Validator WIFI_STATIC_GATEWAY_VALIDATOR = null;
        @Deprecated
        public static final String WIFI_STATIC_IP = "wifi_static_ip";
        private static final Validator WIFI_STATIC_IP_VALIDATOR = null;
        @Deprecated
        public static final String WIFI_STATIC_NETMASK = "wifi_static_netmask";
        private static final Validator WIFI_STATIC_NETMASK_VALIDATOR = null;
        @Deprecated
        public static final String WIFI_USE_STATIC_IP = "wifi_use_static_ip";
        private static final Validator WIFI_USE_STATIC_IP_VALIDATOR = null;
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
        public static final Validator WINDOW_ORIENTATION_LISTENER_LOG_VALIDATOR = null;
        private static final Validator sBooleanValidator = null;
        private static final Validator sLenientIpAddressValidator = null;
        private static final NameValueCache sNameValueCache = null;
        private static final Validator sNonNegativeIntegerValidator = null;
        private static final Validator sUriValidator = null;

        public interface Validator {
            boolean validate(String str);
        }

        /* renamed from: android.provider.Settings.System.1 */
        static class AnonymousClass1 implements Validator {
            AnonymousClass1() {
            }

            public boolean validate(String value) {
                boolean z = Settings.LOCAL_LOGV;
                try {
                    if (Integer.parseInt(value) >= 0) {
                        z = true;
                    }
                    return z;
                } catch (NumberFormatException e) {
                    return Settings.LOCAL_LOGV;
                }
            }
        }

        /* renamed from: android.provider.Settings.System.2 */
        static class AnonymousClass2 implements Validator {
            AnonymousClass2() {
            }

            public boolean validate(String value) {
                try {
                    Uri.decode(value);
                    return true;
                } catch (IllegalArgumentException e) {
                    return Settings.LOCAL_LOGV;
                }
            }
        }

        /* renamed from: android.provider.Settings.System.3 */
        static class AnonymousClass3 implements Validator {
            private static final int MAX_IPV6_LENGTH = 45;

            AnonymousClass3() {
            }

            public boolean validate(String value) {
                return (value == null || value.length() > MAX_IPV6_LENGTH) ? Settings.LOCAL_LOGV : true;
            }
        }

        /* renamed from: android.provider.Settings.System.4 */
        static class AnonymousClass4 implements Validator {
            private static final int MAX_LENGTH = 1000;

            AnonymousClass4() {
            }

            public boolean validate(String value) {
                return (value == null || value.length() < MAX_LENGTH) ? true : Settings.LOCAL_LOGV;
            }
        }

        /* renamed from: android.provider.Settings.System.5 */
        static class AnonymousClass5 implements Validator {
            AnonymousClass5() {
            }

            public boolean validate(String value) {
                boolean z = Settings.LOCAL_LOGV;
                try {
                    if (Float.parseFloat(value) >= 0.0f) {
                        z = true;
                    }
                    return z;
                } catch (NumberFormatException e) {
                    return Settings.LOCAL_LOGV;
                }
            }
        }

        /* renamed from: android.provider.Settings.System.6 */
        static class AnonymousClass6 implements Validator {
            AnonymousClass6() {
            }

            public boolean validate(String value) {
                try {
                    ComponentName.unflattenFromString(value);
                    return true;
                } catch (NullPointerException e) {
                    return Settings.LOCAL_LOGV;
                }
            }
        }

        /* renamed from: android.provider.Settings.System.7 */
        static class AnonymousClass7 implements Validator {
            private static final int MAX_LENGTH = 1000;

            AnonymousClass7() {
            }

            public boolean validate(String value) {
                boolean z = Settings.LOCAL_LOGV;
                if (value != null && value.length() > MAX_LENGTH) {
                    return Settings.LOCAL_LOGV;
                }
                if (ComponentName.unflattenFromString(value) != null) {
                    z = true;
                }
                return z;
            }
        }

        /* renamed from: android.provider.Settings.System.8 */
        static class AnonymousClass8 implements Validator {
            AnonymousClass8() {
            }

            public boolean validate(String value) {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(value);
                    return true;
                } catch (IllegalArgumentException e) {
                    return Settings.LOCAL_LOGV;
                }
            }
        }

        /* renamed from: android.provider.Settings.System.9 */
        static class AnonymousClass9 implements Validator {
            AnonymousClass9() {
            }

            public boolean validate(String value) {
                boolean z = Settings.LOCAL_LOGV;
                try {
                    if (Long.parseLong(value) >= 0) {
                        z = true;
                    }
                    return z;
                } catch (NumberFormatException e) {
                    return Settings.LOCAL_LOGV;
                }
            }
        }

        private static final class DiscreteValueValidator implements Validator {
            private final String[] mValues;

            public DiscreteValueValidator(String[] values) {
                this.mValues = values;
            }

            public boolean validate(String value) {
                return ArrayUtils.contains(this.mValues, value);
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
                boolean z = Settings.LOCAL_LOGV;
                try {
                    float floatValue = Float.parseFloat(value);
                    if (floatValue >= this.mMin && floatValue <= this.mMax) {
                        z = true;
                    }
                    return z;
                } catch (NumberFormatException e) {
                    return Settings.LOCAL_LOGV;
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
                boolean z = Settings.LOCAL_LOGV;
                try {
                    int intValue = Integer.parseInt(value);
                    if (intValue >= this.mMin && intValue <= this.mMax) {
                        z = true;
                    }
                    return z;
                } catch (NumberFormatException e) {
                    return Settings.LOCAL_LOGV;
                }
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.Settings.System.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.Settings.System.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.provider.Settings.System.<clinit>():void");
        }

        public System() {
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
            if (MOVED_TO_SECURE.contains(name)) {
                Log.w(Settings.TAG, "Setting " + name + " has moved from android.provider.Settings.System" + " to android.provider.Settings.Secure, value is unchanged.");
                return Settings.LOCAL_LOGV;
            } else if (!MOVED_TO_GLOBAL.contains(name) && !MOVED_TO_SECURE_THEN_GLOBAL.contains(name)) {
                return sNameValueCache.putStringForUser(resolver, name, value, userHandle);
            } else {
                Log.w(Settings.TAG, "Setting " + name + " has moved from android.provider.Settings.System" + " to android.provider.Settings.Global, value is unchanged.");
                return Settings.LOCAL_LOGV;
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
            adjustConfigurationForUser(cr, outConfig, UserHandle.myUserId(), Settings.LOCAL_LOGV);
        }

        public static void adjustConfigurationForUser(ContentResolver cr, Configuration outConfig, int userHandle, boolean updateSettingsIfEmpty) {
            outConfig.fontScale = getFloatForUser(cr, FONT_SCALE, DEFAULT_FONT_SCALE, userHandle);
            if (outConfig.fontScale < 0.0f) {
                outConfig.fontScale = DEFAULT_FONT_SCALE;
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
            if (!inoutConfig.userSetLocale && !inoutConfig.getLocales().isEmpty()) {
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
            return Settings.LOCAL_LOGV;
        }

        public static boolean hasInterestingConfigurationChanges(int changes) {
            return ((KeymasterDefs.KM_UINT_REP & changes) == 0 && (changes & 4) == 0) ? Settings.LOCAL_LOGV : true;
        }

        @Deprecated
        public static boolean getShowGTalkServiceStatus(ContentResolver cr) {
            return getShowGTalkServiceStatusForUser(cr, UserHandle.myUserId());
        }

        public static boolean getShowGTalkServiceStatusForUser(ContentResolver cr, int userHandle) {
            return getIntForUser(cr, SHOW_GTALK_SERVICE_STATUS, WIFI_SLEEP_POLICY_DEFAULT, userHandle) != 0 ? true : Settings.LOCAL_LOGV;
        }

        @Deprecated
        public static void setShowGTalkServiceStatus(ContentResolver cr, boolean flag) {
            setShowGTalkServiceStatusForUser(cr, flag, UserHandle.myUserId());
        }

        @Deprecated
        public static void setShowGTalkServiceStatusForUser(ContentResolver cr, boolean flag, int userHandle) {
            putIntForUser(cr, SHOW_GTALK_SERVICE_STATUS, flag ? WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED : WIFI_SLEEP_POLICY_DEFAULT, userHandle);
        }

        public static void getCloneToManagedProfileSettings(Set<String> outKeySet) {
            outKeySet.addAll(CLONE_TO_MANAGED_PROFILE);
        }

        public static boolean canWrite(Context context) {
            return Settings.isCallingPackageAllowedToWriteSettings(context, Process.myUid(), context.getOpPackageName(), Settings.LOCAL_LOGV);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.Settings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.Settings.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.provider.Settings.<clinit>():void");
    }

    public Settings() {
    }

    public static boolean canDrawOverlays(Context context) {
        return isCallingPackageAllowedToDrawOverlays(context, Process.myUid(), context.getOpPackageName(), LOCAL_LOGV);
    }

    public static String getGTalkDeviceId(long androidId) {
        return "android-" + Long.toHexString(androidId);
    }

    public static boolean isCallingPackageAllowedToWriteSettings(Context context, int uid, String callingPackage, boolean throwException) {
        return isCallingPackageAllowedToPerformAppOpsProtectedOperation(context, uid, callingPackage, throwException, 23, PM_WRITE_SETTINGS, LOCAL_LOGV);
    }

    public static boolean checkAndNoteWriteSettingsOperation(Context context, int uid, String callingPackage, boolean throwException) {
        return isCallingPackageAllowedToPerformAppOpsProtectedOperation(context, uid, callingPackage, throwException, 23, PM_WRITE_SETTINGS, true);
    }

    public static boolean checkAndNoteChangeNetworkStateOperation(Context context, int uid, String callingPackage, boolean throwException) {
        if (context.checkCallingOrSelfPermission(permission.CHANGE_NETWORK_STATE) == 0) {
            return true;
        }
        return isCallingPackageAllowedToPerformAppOpsProtectedOperation(context, uid, callingPackage, throwException, 23, PM_CHANGE_NETWORK_STATE, true);
    }

    public static boolean isCallingPackageAllowedToDrawOverlays(Context context, int uid, String callingPackage, boolean throwException) {
        return isCallingPackageAllowedToPerformAppOpsProtectedOperation(context, uid, callingPackage, throwException, 24, PM_SYSTEM_ALERT_WINDOW, LOCAL_LOGV);
    }

    public static boolean checkAndNoteDrawOverlaysOperation(Context context, int uid, String callingPackage, boolean throwException) {
        return isCallingPackageAllowedToPerformAppOpsProtectedOperation(context, uid, callingPackage, throwException, 24, PM_SYSTEM_ALERT_WINDOW, true);
    }

    public static boolean isCallingPackageAllowedToPerformAppOpsProtectedOperation(Context context, int uid, String callingPackage, boolean throwException, int appOpsOpCode, String[] permissions, boolean makeNote) {
        if (callingPackage == null) {
            return LOCAL_LOGV;
        }
        int mode;
        AppOpsManager appOpsMgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (makeNote) {
            mode = appOpsMgr.noteOpNoThrow(appOpsOpCode, uid, callingPackage);
        } else {
            mode = appOpsMgr.checkOpNoThrow(appOpsOpCode, uid, callingPackage);
        }
        if (mode == 2 && ("com.huawei.runningtestii".equals(callingPackage) || "com.huawei.hidp".equals(callingPackage) || uid == Process.SYSTEM_UID || UserHandle.getAppId(uid) == Process.SYSTEM_UID)) {
            return true;
        }
        switch (mode) {
            case TextToSpeech.SUCCESS /*0*/:
                return true;
            case Engine.DEFAULT_STREAM /*3*/:
                for (String permission : permissions) {
                    if (context.checkCallingOrSelfPermission(permission) == 0) {
                        return true;
                    }
                }
                break;
        }
        if (!throwException) {
            return LOCAL_LOGV;
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
