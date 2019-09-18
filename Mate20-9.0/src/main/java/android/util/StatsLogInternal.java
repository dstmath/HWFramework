package android.util;

public class StatsLogInternal {
    public static final int ACTIVITY_FOREGROUND_STATE_CHANGED = 42;
    public static final int ACTIVITY_FOREGROUND_STATE_CHANGED__STATE__BACKGROUND = 0;
    public static final int ACTIVITY_FOREGROUND_STATE_CHANGED__STATE__FOREGROUND = 1;
    public static final int ANOMALY_DETECTED = 46;
    public static final int ANROCCURRED__FOREGROUND_STATE__BACKGROUND = 1;
    public static final int ANROCCURRED__FOREGROUND_STATE__FOREGROUND = 2;
    public static final int ANROCCURRED__FOREGROUND_STATE__UNKNOWN = 0;
    public static final int ANROCCURRED__IS_INSTANT_APP__FALSE = 1;
    public static final int ANROCCURRED__IS_INSTANT_APP__TRUE = 2;
    public static final int ANROCCURRED__IS_INSTANT_APP__UNAVAILABLE = 0;
    public static final int ANR_OCCURRED = 79;
    public static final int APP_BREADCRUMB_REPORTED = 47;
    public static final int APP_BREADCRUMB_REPORTED__STATE__START = 3;
    public static final int APP_BREADCRUMB_REPORTED__STATE__STOP = 2;
    public static final int APP_BREADCRUMB_REPORTED__STATE__UNKNOWN = 0;
    public static final int APP_BREADCRUMB_REPORTED__STATE__UNSPECIFIED = 1;
    public static final int APP_CRASH_OCCURRED = 78;
    public static final int APP_CRASH_OCCURRED__FOREGROUND_STATE__BACKGROUND = 1;
    public static final int APP_CRASH_OCCURRED__FOREGROUND_STATE__FOREGROUND = 2;
    public static final int APP_CRASH_OCCURRED__FOREGROUND_STATE__UNKNOWN = 0;
    public static final int APP_CRASH_OCCURRED__IS_INSTANT_APP__FALSE = 1;
    public static final int APP_CRASH_OCCURRED__IS_INSTANT_APP__TRUE = 2;
    public static final int APP_CRASH_OCCURRED__IS_INSTANT_APP__UNAVAILABLE = 0;
    public static final int APP_DIED = 65;
    public static final int APP_START_CANCELED = 49;
    public static final int APP_START_CANCELED__TYPE__COLD = 3;
    public static final int APP_START_CANCELED__TYPE__HOT = 2;
    public static final int APP_START_CANCELED__TYPE__UNKNOWN = 0;
    public static final int APP_START_CANCELED__TYPE__WARM = 1;
    public static final int APP_START_FULLY_DRAWN = 50;
    public static final int APP_START_FULLY_DRAWN__TYPE__UNKNOWN = 0;
    public static final int APP_START_FULLY_DRAWN__TYPE__WITHOUT_BUNDLE = 2;
    public static final int APP_START_FULLY_DRAWN__TYPE__WITH_BUNDLE = 1;
    public static final int APP_START_MEMORY_STATE_CAPTURED = 55;
    public static final int APP_START_OCCURRED = 48;
    public static final int APP_START_OCCURRED__REASON__APP_TRANSITION_REASON_UNKNOWN = 0;
    public static final int APP_START_OCCURRED__REASON__APP_TRANSITION_RECENTS_ANIM = 5;
    public static final int APP_START_OCCURRED__REASON__APP_TRANSITION_SNAPSHOT = 4;
    public static final int APP_START_OCCURRED__REASON__APP_TRANSITION_SPLASH_SCREEN = 1;
    public static final int APP_START_OCCURRED__REASON__APP_TRANSITION_TIMEOUT = 3;
    public static final int APP_START_OCCURRED__REASON__APP_TRANSITION_WINDOWS_DRAWN = 2;
    public static final int APP_START_OCCURRED__TYPE__COLD = 3;
    public static final int APP_START_OCCURRED__TYPE__HOT = 2;
    public static final int APP_START_OCCURRED__TYPE__UNKNOWN = 0;
    public static final int APP_START_OCCURRED__TYPE__WARM = 1;
    public static final int AUDIO_STATE_CHANGED = 23;
    public static final int AUDIO_STATE_CHANGED__STATE__OFF = 0;
    public static final int AUDIO_STATE_CHANGED__STATE__ON = 1;
    public static final int AUDIO_STATE_CHANGED__STATE__RESET = 2;
    public static final int BATTERY_LEVEL_CHANGED = 30;
    public static final int BATTERY_SAVER_MODE_STATE_CHANGED = 20;
    public static final int BATTERY_SAVER_MODE_STATE_CHANGED__STATE__OFF = 0;
    public static final int BATTERY_SAVER_MODE_STATE_CHANGED__STATE__ON = 1;
    public static final int BLE_SCAN_RESULT_RECEIVED = 4;
    public static final int BLE_SCAN_STATE_CHANGED = 2;
    public static final int BLE_SCAN_STATE_CHANGED__STATE__OFF = 0;
    public static final int BLE_SCAN_STATE_CHANGED__STATE__ON = 1;
    public static final int BLE_SCAN_STATE_CHANGED__STATE__RESET = 2;
    public static final int BLUETOOTH_ACTIVITY_INFO = 10007;
    public static final int BLUETOOTH_BYTES_TRANSFER = 10006;
    public static final int BLUETOOTH_CLASSIC_PAIRING_EVENT_REPORTED = 166;
    public static final int BLUETOOTH_CONNECTION_STATE_CHANGED = 68;
    public static final int BLUETOOTH_CONNECTION_STATE_CHANGED__STATE__CONNECTION_STATE_CONNECTED = 2;
    public static final int BLUETOOTH_CONNECTION_STATE_CHANGED__STATE__CONNECTION_STATE_CONNECTING = 1;
    public static final int BLUETOOTH_CONNECTION_STATE_CHANGED__STATE__CONNECTION_STATE_DISCONNECTED = 0;
    public static final int BLUETOOTH_CONNECTION_STATE_CHANGED__STATE__CONNECTION_STATE_DISCONNECTING = 3;
    public static final int BLUETOOTH_ENABLED_STATE_CHANGED = 67;
    public static final int BLUETOOTH_ENABLED_STATE_CHANGED__REASON__ENABLE_DISABLE_REASON_AIRPLANE_MODE = 2;
    public static final int BLUETOOTH_ENABLED_STATE_CHANGED__REASON__ENABLE_DISABLE_REASON_APPLICATION_REQUEST = 1;
    public static final int BLUETOOTH_ENABLED_STATE_CHANGED__REASON__ENABLE_DISABLE_REASON_CRASH = 7;
    public static final int BLUETOOTH_ENABLED_STATE_CHANGED__REASON__ENABLE_DISABLE_REASON_DISALLOWED = 3;
    public static final int BLUETOOTH_ENABLED_STATE_CHANGED__REASON__ENABLE_DISABLE_REASON_RESTARTED = 4;
    public static final int BLUETOOTH_ENABLED_STATE_CHANGED__REASON__ENABLE_DISABLE_REASON_RESTORE_USER_SETTING = 9;
    public static final int BLUETOOTH_ENABLED_STATE_CHANGED__REASON__ENABLE_DISABLE_REASON_START_ERROR = 5;
    public static final int BLUETOOTH_ENABLED_STATE_CHANGED__REASON__ENABLE_DISABLE_REASON_SYSTEM_BOOT = 6;
    public static final int BLUETOOTH_ENABLED_STATE_CHANGED__REASON__ENABLE_DISABLE_REASON_UNSPECIFIED = 0;
    public static final int BLUETOOTH_ENABLED_STATE_CHANGED__REASON__ENABLE_DISABLE_REASON_USER_SWITCH = 8;
    public static final int BLUETOOTH_ENABLED_STATE_CHANGED__STATE__DISABLED = 2;
    public static final int BLUETOOTH_ENABLED_STATE_CHANGED__STATE__ENABLED = 1;
    public static final int BLUETOOTH_ENABLED_STATE_CHANGED__STATE__UNKNOWN = 0;
    public static final int BOOT_SEQUENCE_REPORTED = 57;
    public static final int CALL_STATE_CHANGED = 61;
    public static final int CALL_STATE_CHANGED__CALL_STATE__ABORTED = 8;
    public static final int CALL_STATE_CHANGED__CALL_STATE__ACTIVE = 5;
    public static final int CALL_STATE_CHANGED__CALL_STATE__CONNECTING = 1;
    public static final int CALL_STATE_CHANGED__CALL_STATE__DIALING = 3;
    public static final int CALL_STATE_CHANGED__CALL_STATE__DISCONNECTED = 7;
    public static final int CALL_STATE_CHANGED__CALL_STATE__DISCONNECTING = 9;
    public static final int CALL_STATE_CHANGED__CALL_STATE__NEW = 0;
    public static final int CALL_STATE_CHANGED__CALL_STATE__ON_HOLD = 6;
    public static final int CALL_STATE_CHANGED__CALL_STATE__PULLING = 10;
    public static final int CALL_STATE_CHANGED__CALL_STATE__RINGING = 4;
    public static final int CALL_STATE_CHANGED__CALL_STATE__SELECT_PHONE_ACCOUNT = 2;
    public static final int CALL_STATE_CHANGED__DISCONNECT_CAUSE__ANSWERED_ELSEWHERE = 11;
    public static final int CALL_STATE_CHANGED__DISCONNECT_CAUSE__BUSY = 7;
    public static final int CALL_STATE_CHANGED__DISCONNECT_CAUSE__CALL_PULLED = 12;
    public static final int CALL_STATE_CHANGED__DISCONNECT_CAUSE__CANCELED = 4;
    public static final int CALL_STATE_CHANGED__DISCONNECT_CAUSE__CONNECTION_MANAGER_NOT_SUPPORTED = 10;
    public static final int CALL_STATE_CHANGED__DISCONNECT_CAUSE__ERROR = 1;
    public static final int CALL_STATE_CHANGED__DISCONNECT_CAUSE__LOCAL = 2;
    public static final int CALL_STATE_CHANGED__DISCONNECT_CAUSE__MISSED = 5;
    public static final int CALL_STATE_CHANGED__DISCONNECT_CAUSE__OTHER = 9;
    public static final int CALL_STATE_CHANGED__DISCONNECT_CAUSE__REJECTED = 6;
    public static final int CALL_STATE_CHANGED__DISCONNECT_CAUSE__REMOTE = 3;
    public static final int CALL_STATE_CHANGED__DISCONNECT_CAUSE__RESTRICTED = 8;
    public static final int CALL_STATE_CHANGED__DISCONNECT_CAUSE__UNKNOWN = 0;
    public static final int CAMERA_STATE_CHANGED = 25;
    public static final int CAMERA_STATE_CHANGED__STATE__OFF = 0;
    public static final int CAMERA_STATE_CHANGED__STATE__ON = 1;
    public static final int CAMERA_STATE_CHANGED__STATE__RESET = 2;
    public static final int CHARGE_CYCLES_REPORTED = 74;
    public static final int CHARGING_STATE_CHANGED = 31;
    public static final int CHARGING_STATE_CHANGED__STATE__BATTERY_STATUS_CHARGING = 2;
    public static final int CHARGING_STATE_CHANGED__STATE__BATTERY_STATUS_DISCHARGING = 3;
    public static final int CHARGING_STATE_CHANGED__STATE__BATTERY_STATUS_FULL = 5;
    public static final int CHARGING_STATE_CHANGED__STATE__BATTERY_STATUS_INVALID = 0;
    public static final int CHARGING_STATE_CHANGED__STATE__BATTERY_STATUS_NOT_CHARGING = 4;
    public static final int CHARGING_STATE_CHANGED__STATE__BATTERY_STATUS_UNKNOWN = 1;
    public static final int CPU_ACTIVE_TIME = 10016;
    public static final int CPU_CLUSTER_TIME = 10017;
    public static final int CPU_TIME_PER_FREQ = 10008;
    public static final int CPU_TIME_PER_UID = 10009;
    public static final int CPU_TIME_PER_UID_FREQ = 10010;
    public static final int DAVEY_OCCURRED = 58;
    public static final int DEVICE_IDLE_MODE_STATE_CHANGED = 21;
    public static final int DEVICE_IDLE_MODE_STATE_CHANGED__STATE__DEVICE_IDLE_MODE_DEEP = 2;
    public static final int DEVICE_IDLE_MODE_STATE_CHANGED__STATE__DEVICE_IDLE_MODE_LIGHT = 1;
    public static final int DEVICE_IDLE_MODE_STATE_CHANGED__STATE__DEVICE_IDLE_MODE_OFF = 0;
    public static final int DEVICE_IDLING_MODE_STATE_CHANGED = 22;
    public static final int DEVICE_IDLING_MODE_STATE_CHANGED__STATE__DEVICE_IDLE_MODE_DEEP = 2;
    public static final int DEVICE_IDLING_MODE_STATE_CHANGED__STATE__DEVICE_IDLE_MODE_LIGHT = 1;
    public static final int DEVICE_IDLING_MODE_STATE_CHANGED__STATE__DEVICE_IDLE_MODE_OFF = 0;
    public static final int DISK_SPACE = 10018;
    public static final int FLASHLIGHT_STATE_CHANGED = 26;
    public static final int FLASHLIGHT_STATE_CHANGED__STATE__OFF = 0;
    public static final int FLASHLIGHT_STATE_CHANGED__STATE__ON = 1;
    public static final int FLASHLIGHT_STATE_CHANGED__STATE__RESET = 2;
    public static final int FOREGROUND_SERVICE_STATE_CHANGED = 60;
    public static final int FOREGROUND_SERVICE_STATE_CHANGED__STATE__ENTER = 1;
    public static final int FOREGROUND_SERVICE_STATE_CHANGED__STATE__EXIT = 2;
    public static final int FULL_BATTERY_CAPACITY = 10020;
    public static final int GPS_SCAN_STATE_CHANGED = 6;
    public static final int GPS_SCAN_STATE_CHANGED__STATE__OFF = 0;
    public static final int GPS_SCAN_STATE_CHANGED__STATE__ON = 1;
    public static final int HARDWARE_FAILED = 72;
    public static final int HARDWARE_FAILED__HARDWARE_TYPE__HARDWARE_FAILED_CODEC = 2;
    public static final int HARDWARE_FAILED__HARDWARE_TYPE__HARDWARE_FAILED_FINGERPRINT = 4;
    public static final int HARDWARE_FAILED__HARDWARE_TYPE__HARDWARE_FAILED_MICROPHONE = 1;
    public static final int HARDWARE_FAILED__HARDWARE_TYPE__HARDWARE_FAILED_SPEAKER = 3;
    public static final int HARDWARE_FAILED__HARDWARE_TYPE__HARDWARE_FAILED_UNKNOWN = 0;
    public static final int ISOLATED_UID_CHANGED = 43;
    public static final int ISOLATED_UID_CHANGED__EVENT__CREATED = 1;
    public static final int ISOLATED_UID_CHANGED__EVENT__REMOVED = 0;
    public static final int KERNEL_WAKELOCK = 10004;
    public static final int KERNEL_WAKEUP_REPORTED = 36;
    public static final int KEYGUARD_BOUNCER_PASSWORD_ENTERED = 64;
    public static final int KEYGUARD_BOUNCER_PASSWORD_ENTERED__RESULT__FAILURE = 1;
    public static final int KEYGUARD_BOUNCER_PASSWORD_ENTERED__RESULT__SUCCESS = 2;
    public static final int KEYGUARD_BOUNCER_PASSWORD_ENTERED__RESULT__UNKNOWN = 0;
    public static final int KEYGUARD_BOUNCER_STATE_CHANGED = 63;
    public static final int KEYGUARD_BOUNCER_STATE_CHANGED__STATE__HIDDEN = 1;
    public static final int KEYGUARD_BOUNCER_STATE_CHANGED__STATE__SHOWN = 2;
    public static final int KEYGUARD_BOUNCER_STATE_CHANGED__STATE__UNKNOWN = 0;
    public static final int KEYGUARD_STATE_CHANGED = 62;
    public static final int KEYGUARD_STATE_CHANGED__STATE__HIDDEN = 1;
    public static final int KEYGUARD_STATE_CHANGED__STATE__OCCLUDED = 3;
    public static final int KEYGUARD_STATE_CHANGED__STATE__SHOWN = 2;
    public static final int KEYGUARD_STATE_CHANGED__STATE__UNKNOWN = 0;
    public static final int LMK_KILL_OCCURRED = 51;
    public static final int LMK_STATE_CHANGED = 54;
    public static final int LMK_STATE_CHANGED__STATE__START = 1;
    public static final int LMK_STATE_CHANGED__STATE__STOP = 2;
    public static final int LMK_STATE_CHANGED__STATE__UNKNOWN = 0;
    public static final int LONG_PARTIAL_WAKELOCK_STATE_CHANGED = 11;
    public static final int LONG_PARTIAL_WAKELOCK_STATE_CHANGED__STATE__OFF = 0;
    public static final int LONG_PARTIAL_WAKELOCK_STATE_CHANGED__STATE__ON = 1;
    public static final int LOW_MEM_REPORTED = 81;
    public static final int MEDIA_CODEC_STATE_CHANGED = 24;
    public static final int MEDIA_CODEC_STATE_CHANGED__STATE__OFF = 0;
    public static final int MEDIA_CODEC_STATE_CHANGED__STATE__ON = 1;
    public static final int MEDIA_CODEC_STATE_CHANGED__STATE__RESET = 2;
    public static final int MOBILE_BYTES_TRANSFER = 10002;
    public static final int MOBILE_BYTES_TRANSFER_BY_FG_BG = 10003;
    public static final int MOBILE_CONNECTION_STATE_CHANGED = 75;
    public static final int MOBILE_CONNECTION_STATE_CHANGED__STATE__ACTIVATING = 2;
    public static final int MOBILE_CONNECTION_STATE_CHANGED__STATE__ACTIVE = 3;
    public static final int MOBILE_CONNECTION_STATE_CHANGED__STATE__DISCONNECTING = 4;
    public static final int MOBILE_CONNECTION_STATE_CHANGED__STATE__DISCONNECTION_ERROR_CREATING_CONNECTION = 5;
    public static final int MOBILE_CONNECTION_STATE_CHANGED__STATE__INACTIVE = 1;
    public static final int MOBILE_CONNECTION_STATE_CHANGED__STATE__UNKNOWN = 0;
    public static final int MOBILE_RADIO_POWER_STATE_CHANGED = 12;
    public static final int MOBILE_RADIO_POWER_STATE_CHANGED__STATE__DATA_CONNECTION_POWER_STATE_HIGH = 3;
    public static final int MOBILE_RADIO_POWER_STATE_CHANGED__STATE__DATA_CONNECTION_POWER_STATE_LOW = 1;
    public static final int MOBILE_RADIO_POWER_STATE_CHANGED__STATE__DATA_CONNECTION_POWER_STATE_MEDIUM = 2;
    public static final int MOBILE_RADIO_POWER_STATE_CHANGED__STATE__DATA_CONNECTION_POWER_STATE_UNKNOWN = Integer.MAX_VALUE;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED = 76;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_1XRTT = 7;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_CDMA = 4;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_EDGE = 2;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_EHRPD = 14;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_EVDO_0 = 5;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_EVDO_A = 6;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_EVDO_B = 12;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_GPRS = 1;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_GSM = 16;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_HSDPA = 8;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_HSPA = 10;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_HSPAP = 15;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_HSUPA = 9;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_IDEN = 11;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_IWLAN = 18;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_LTE = 13;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_LTE_CA = 19;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_TD_SCDMA = 17;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_UMTS = 3;
    public static final int MOBILE_RADIO_TECHNOLOGY_CHANGED__STATE__NETWORK_TYPE_UNKNOWN = 0;
    public static final int MODEM_ACTIVITY_INFO = 10012;
    public static final int OVERLAY_STATE_CHANGED = 59;
    public static final int OVERLAY_STATE_CHANGED__STATE__ENTERED = 1;
    public static final int OVERLAY_STATE_CHANGED__STATE__EXITED = 2;
    public static final int PACKET_WAKEUP_OCCURRED = 44;
    public static final int PHONE_SIGNAL_STRENGTH_CHANGED = 40;
    public static final int PHONE_SIGNAL_STRENGTH_CHANGED__SIGNAL_STRENGTH__SIGNAL_STRENGTH_GOOD = 3;
    public static final int PHONE_SIGNAL_STRENGTH_CHANGED__SIGNAL_STRENGTH__SIGNAL_STRENGTH_GREAT = 4;
    public static final int PHONE_SIGNAL_STRENGTH_CHANGED__SIGNAL_STRENGTH__SIGNAL_STRENGTH_MODERATE = 2;
    public static final int PHONE_SIGNAL_STRENGTH_CHANGED__SIGNAL_STRENGTH__SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    public static final int PHONE_SIGNAL_STRENGTH_CHANGED__SIGNAL_STRENGTH__SIGNAL_STRENGTH_POOR = 1;
    public static final int PHYSICAL_DROP_DETECTED = 73;
    public static final int PICTURE_IN_PICTURE_STATE_CHANGED = 52;
    public static final int PICTURE_IN_PICTURE_STATE_CHANGED__STATE__DISMISSED = 4;
    public static final int PICTURE_IN_PICTURE_STATE_CHANGED__STATE__ENTERED = 1;
    public static final int PICTURE_IN_PICTURE_STATE_CHANGED__STATE__EXPANDED_TO_FULL_SCREEN = 2;
    public static final int PICTURE_IN_PICTURE_STATE_CHANGED__STATE__MINIMIZED = 3;
    public static final int PLUGGED_STATE_CHANGED = 32;
    public static final int PLUGGED_STATE_CHANGED__STATE__BATTERY_PLUGGED_AC = 1;
    public static final int PLUGGED_STATE_CHANGED__STATE__BATTERY_PLUGGED_NONE = 0;
    public static final int PLUGGED_STATE_CHANGED__STATE__BATTERY_PLUGGED_USB = 2;
    public static final int PLUGGED_STATE_CHANGED__STATE__BATTERY_PLUGGED_WIRELESS = 4;
    public static final int PROCESS_LIFE_CYCLE_STATE_CHANGED = 28;
    public static final int PROCESS_LIFE_CYCLE_STATE_CHANGED__STATE__CRASHED = 2;
    public static final int PROCESS_LIFE_CYCLE_STATE_CHANGED__STATE__FINISHED = 0;
    public static final int PROCESS_LIFE_CYCLE_STATE_CHANGED__STATE__STARTED = 1;
    public static final int PROCESS_MEMORY_STATE = 10013;
    public static final int REMAINING_BATTERY_CAPACITY = 10019;
    public static final int RESOURCE_CONFIGURATION_CHANGED = 66;
    public static final int SCHEDULED_JOB_STATE_CHANGED = 8;
    public static final int SCHEDULED_JOB_STATE_CHANGED__STATE__FINISHED = 0;
    public static final int SCHEDULED_JOB_STATE_CHANGED__STATE__SCHEDULED = 2;
    public static final int SCHEDULED_JOB_STATE_CHANGED__STATE__STARTED = 1;
    public static final int SCHEDULED_JOB_STATE_CHANGED__STOP_REASON__STOP_REASON_CANCELLED = 0;
    public static final int SCHEDULED_JOB_STATE_CHANGED__STOP_REASON__STOP_REASON_CONSTRAINTS_NOT_SATISFIED = 1;
    public static final int SCHEDULED_JOB_STATE_CHANGED__STOP_REASON__STOP_REASON_DEVICE_IDLE = 4;
    public static final int SCHEDULED_JOB_STATE_CHANGED__STOP_REASON__STOP_REASON_PREEMPT = 2;
    public static final int SCHEDULED_JOB_STATE_CHANGED__STOP_REASON__STOP_REASON_TIMEOUT = 3;
    public static final int SCHEDULED_JOB_STATE_CHANGED__STOP_REASON__STOP_REASON_UNKNOWN = -1;
    public static final int SCREEN_BRIGHTNESS_CHANGED = 9;
    public static final int SCREEN_STATE_CHANGED = 29;
    public static final int SCREEN_STATE_CHANGED__STATE__DISPLAY_STATE_DOZE = 3;
    public static final int SCREEN_STATE_CHANGED__STATE__DISPLAY_STATE_DOZE_SUSPEND = 4;
    public static final int SCREEN_STATE_CHANGED__STATE__DISPLAY_STATE_OFF = 1;
    public static final int SCREEN_STATE_CHANGED__STATE__DISPLAY_STATE_ON = 2;
    public static final int SCREEN_STATE_CHANGED__STATE__DISPLAY_STATE_ON_SUSPEND = 6;
    public static final int SCREEN_STATE_CHANGED__STATE__DISPLAY_STATE_UNKNOWN = 0;
    public static final int SCREEN_STATE_CHANGED__STATE__DISPLAY_STATE_VR = 5;
    public static final int SENSOR_STATE_CHANGED = 5;
    public static final int SENSOR_STATE_CHANGED__STATE__OFF = 0;
    public static final int SENSOR_STATE_CHANGED__STATE__ON = 1;
    public static final int SETTING_CHANGED = 41;
    public static final int SETTING_CHANGED__REASON__DELETED = 2;
    public static final int SETTING_CHANGED__REASON__UPDATED = 1;
    public static final int SHUTDOWN_SEQUENCE_REPORTED = 56;
    public static final int SPEAKER_IMPEDANCE_REPORTED = 71;
    public static final int SUBSYSTEM_SLEEP_STATE = 10005;
    public static final int SYNC_STATE_CHANGED = 7;
    public static final int SYNC_STATE_CHANGED__STATE__OFF = 0;
    public static final int SYNC_STATE_CHANGED__STATE__ON = 1;
    public static final int SYSTEM_ELAPSED_REALTIME = 10014;
    public static final int SYSTEM_UPTIME = 10015;
    public static final int TEMPERATURE = 10021;
    public static final int TEMPERATURE__SENSOR_LOCATION__TEMPERATURE_TYPE_BATTERY = 2;
    public static final int TEMPERATURE__SENSOR_LOCATION__TEMPERATURE_TYPE_CPU = 0;
    public static final int TEMPERATURE__SENSOR_LOCATION__TEMPERATURE_TYPE_GPU = 1;
    public static final int TEMPERATURE__SENSOR_LOCATION__TEMPERATURE_TYPE_SKIN = 3;
    public static final int TEMPERATURE__SENSOR_LOCATION__TEMPERATURE_TYPE_UNKNOWN = -1;
    public static final int UID_PROCESS_STATE_CHANGED = 27;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_BACKUP = 1008;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_BOUND_FOREGROUND_SERVICE = 1004;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_CACHED_ACTIVITY = 1015;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_CACHED_ACTIVITY_CLIENT = 1016;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_CACHED_EMPTY = 1018;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_CACHED_RECENT = 1017;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_FOREGROUND_SERVICE = 1003;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_HEAVY_WEIGHT = 1012;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_HOME = 1013;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_IMPORTANT_BACKGROUND = 1006;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_IMPORTANT_FOREGROUND = 1005;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_LAST_ACTIVITY = 1014;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_NONEXISTENT = 1019;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_PERSISTENT = 1000;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_PERSISTENT_UI = 1001;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_RECEIVER = 1010;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_SERVICE = 1009;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_TOP = 1002;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_TOP_SLEEPING = 1011;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_TRANSIENT_BACKGROUND = 1007;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_UNKNOWN = 999;
    public static final int UID_PROCESS_STATE_CHANGED__STATE__PROCESS_STATE_UNKNOWN_TO_PROTO = 998;
    public static final int USB_CONNECTOR_STATE_CHANGED = 70;
    public static final int USB_CONNECTOR_STATE_CHANGED__STATE__CONNECTED = 1;
    public static final int USB_CONNECTOR_STATE_CHANGED__STATE__DISCONNECTED = 0;
    public static final int USB_DEVICE_ATTACHED = 77;
    public static final int WAKELOCK_STATE_CHANGED = 10;
    public static final int WAKELOCK_STATE_CHANGED__LEVEL__DOZE_WAKE_LOCK = 64;
    public static final int WAKELOCK_STATE_CHANGED__LEVEL__DRAW_WAKE_LOCK = 128;
    public static final int WAKELOCK_STATE_CHANGED__LEVEL__FULL_WAKE_LOCK = 26;
    public static final int WAKELOCK_STATE_CHANGED__LEVEL__PARTIAL_WAKE_LOCK = 1;
    public static final int WAKELOCK_STATE_CHANGED__LEVEL__PROXIMITY_SCREEN_OFF_WAKE_LOCK = 32;
    public static final int WAKELOCK_STATE_CHANGED__LEVEL__SCREEN_BRIGHT_WAKE_LOCK = 10;
    public static final int WAKELOCK_STATE_CHANGED__LEVEL__SCREEN_DIM_WAKE_LOCK = 6;
    public static final int WAKELOCK_STATE_CHANGED__STATE__ACQUIRE = 1;
    public static final int WAKELOCK_STATE_CHANGED__STATE__CHANGE_ACQUIRE = 3;
    public static final int WAKELOCK_STATE_CHANGED__STATE__CHANGE_RELEASE = 2;
    public static final int WAKELOCK_STATE_CHANGED__STATE__RELEASE = 0;
    public static final int WAKEUP_ALARM_OCCURRED = 35;
    public static final int WIFI_ACTIVITY_INFO = 10011;
    public static final int WIFI_BYTES_TRANSFER = 10000;
    public static final int WIFI_BYTES_TRANSFER_BY_FG_BG = 10001;
    public static final int WIFI_LOCK_STATE_CHANGED = 37;
    public static final int WIFI_LOCK_STATE_CHANGED__STATE__OFF = 0;
    public static final int WIFI_LOCK_STATE_CHANGED__STATE__ON = 1;
    public static final int WIFI_MULTICAST_LOCK_STATE_CHANGED = 53;
    public static final int WIFI_MULTICAST_LOCK_STATE_CHANGED__STATE__OFF = 0;
    public static final int WIFI_MULTICAST_LOCK_STATE_CHANGED__STATE__ON = 1;
    public static final int WIFI_RADIO_POWER_STATE_CHANGED = 13;
    public static final int WIFI_RADIO_POWER_STATE_CHANGED__STATE__DATA_CONNECTION_POWER_STATE_HIGH = 3;
    public static final int WIFI_RADIO_POWER_STATE_CHANGED__STATE__DATA_CONNECTION_POWER_STATE_LOW = 1;
    public static final int WIFI_RADIO_POWER_STATE_CHANGED__STATE__DATA_CONNECTION_POWER_STATE_MEDIUM = 2;
    public static final int WIFI_RADIO_POWER_STATE_CHANGED__STATE__DATA_CONNECTION_POWER_STATE_UNKNOWN = Integer.MAX_VALUE;
    public static final int WIFI_SCAN_STATE_CHANGED = 39;
    public static final int WIFI_SCAN_STATE_CHANGED__STATE__OFF = 0;
    public static final int WIFI_SCAN_STATE_CHANGED__STATE__ON = 1;
    public static final int WIFI_SIGNAL_STRENGTH_CHANGED = 38;
    public static final int WIFI_SIGNAL_STRENGTH_CHANGED__SIGNAL_STRENGTH__SIGNAL_STRENGTH_GOOD = 3;
    public static final int WIFI_SIGNAL_STRENGTH_CHANGED__SIGNAL_STRENGTH__SIGNAL_STRENGTH_GREAT = 4;
    public static final int WIFI_SIGNAL_STRENGTH_CHANGED__SIGNAL_STRENGTH__SIGNAL_STRENGTH_MODERATE = 2;
    public static final int WIFI_SIGNAL_STRENGTH_CHANGED__SIGNAL_STRENGTH__SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
    public static final int WIFI_SIGNAL_STRENGTH_CHANGED__SIGNAL_STRENGTH__SIGNAL_STRENGTH_POOR = 1;
    public static final int WTF_OCCURRED = 80;

    public static native int write(int i);

    public static native int write(int i, int i2);

    public static native int write(int i, int i2, int i3);

    public static native int write(int i, int i2, int i3, float f, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13, int i14, int i15, int i16, int i17);

    public static native int write(int i, int i2, int i3, int i4);

    public static native int write(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9);

    public static native int write(int i, int i2, int i3, int i4, long j, boolean z);

    public static native int write(int i, int i2, int i3, long j);

    public static native int write(int i, int i2, int i3, boolean z, boolean z2);

    public static native int write(int i, int i2, int i3, boolean z, boolean z2, boolean z3);

    public static native int write(int i, int i2, long j);

    public static native int write(int i, int i2, long j, long j2);

    public static native int write(int i, int i2, long j, long j2, long j3, long j4);

    public static native int write(int i, int i2, String str, int i3);

    public static native int write(int i, int i2, String str, int i3, long j, long j2, long j3, long j4, long j5);

    public static native int write(int i, int i2, String str, int i3, String str2);

    public static native int write(int i, int i2, String str, int i3, String str2, String str3, String str4, int i4, int i5, int i6);

    public static native int write(int i, int i2, String str, int i3, String str2, String str3, boolean z, long j, int i4, int i5, int i6, int i7, int i8, String str4, int i9, int i10);

    public static native int write(int i, int i2, String str, int i3, String str2, boolean z, long j);

    public static native int write(int i, int i2, String str, String str2, int i3);

    public static native int write(int i, int i2, String str, String str2, int i3, String str3, int i4, int i5);

    public static native int write(int i, int i2, String str, String str2, long j, long j2, long j3, long j4, long j5);

    public static native int write(int i, int i2, String str, String str2, String str3, int i3, int i4);

    public static native int write(int i, int i2, String str, boolean z, int i3);

    public static native int write(int i, int i2, boolean z, long j, long j2, long j3, long j4);

    public static native int write(int i, long j);

    public static native int write(int i, long j, int i2, long j2, long j3, long j4, long j5);

    public static native int write(int i, long j, long j2, long j3);

    public static native int write(int i, long j, long j2, long j3, long j4, long j5, long j6, long j7, long j8, long j9, long j10);

    public static native int write(int i, String str, int i2, int i3, int i4, int i5, int i6, long j);

    public static native int write(int i, String str, int i2, int i3, long j);

    public static native int write(int i, String str, long j);

    public static native int write(int i, String str, String str2, long j, long j2);

    public static native int write(int i, String str, String str2, long j, long j2, long j3, long j4);

    public static native int write(int i, String str, String str2, String str3, String str4, String str5, boolean z, int i2, int i3);

    public static native int write(int i, boolean z, String str, long j, long j2);

    public static native int write(int i, int[] iArr, String[] strArr, int i2);

    public static native int write(int i, int[] iArr, String[] strArr, int i2, int i3);

    public static native int write(int i, int[] iArr, String[] strArr, int i2, int i3, String str);

    public static native int write(int i, int[] iArr, String[] strArr, int i2, String str, int i3);

    public static native int write(int i, int[] iArr, String[] strArr, int i2, boolean z, boolean z2, boolean z3);

    public static native int write(int i, int[] iArr, String[] strArr, String str);

    public static native int write(int i, int[] iArr, String[] strArr, String str, int i2);

    public static native int write(int i, int[] iArr, String[] strArr, String str, int i2, int i3);

    public static native int write(int i, int[] iArr, String[] strArr, String str, String str2, int i2);

    public static native int write_non_chained(int i, int i2, String str, int i3);

    public static native int write_non_chained(int i, int i2, String str, int i3, int i4);

    public static native int write_non_chained(int i, int i2, String str, int i3, int i4, String str2);

    public static native int write_non_chained(int i, int i2, String str, int i3, String str2, int i4);

    public static native int write_non_chained(int i, int i2, String str, int i3, boolean z, boolean z2, boolean z3);

    public static native int write_non_chained(int i, int i2, String str, String str2);

    public static native int write_non_chained(int i, int i2, String str, String str2, int i3);

    public static native int write_non_chained(int i, int i2, String str, String str2, int i3, int i4);

    public static native int write_non_chained(int i, int i2, String str, String str2, String str3, int i3);
}
