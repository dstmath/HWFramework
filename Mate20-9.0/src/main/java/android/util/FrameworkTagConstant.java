package android.util;

public class FrameworkTagConstant {
    public static final String[] ALARM_MODULE_TAG = {ALARM_TAG, "AlarmManager_resource", "AlarmManager_clock", "AlarmManager_heartbeat"};
    private static final String ALARM_TAG = "AlarmManager";
    public static final String[] AM_MODULE_TAG = {AM_TAG, "ActivityManager_activity", "ActivityManager_service", "ActivityManager_contentProvider", "ActivityManager_broadcast", "ActivityManager_task", "ActivityManager_visibility", "ActivityManager_keyguard", "ActivityManager_fragment"};
    private static final String AM_TAG = "ActivityManager";
    public static final String[] ANIMATION_MODULE_TAG = {ANIMATION_TAG};
    private static final String ANIMATION_TAG = "Animation";
    public static final String[] DEAMON_MODULE_TAG = {DEAMON_TAG, "DeamonManager_create", "DeamonManager_instantiation"};
    private static final String DEAMON_TAG = "DeamonManager";
    private static final String FLOW_POSTFIX = "_FLOW";
    public static final String[] FWK_MODULE_TAG = {FWK_TAG, AM_TAG, PM_TAG, WM_TAG, NM_TAG, ALARM_TAG, POWER_TAG, DEAMON_TAG, SERVICES_TAG, USERS_TAG, STORAGE_TAG, SENSOR_TAG, VIEW_TAG, SECURITY_TAG, ANIMATION_TAG, INPUT_TAG, POWERONOFF_TAG, USM_TAG};
    private static final String FWK_TAG = "Framework";
    public static final int HWTAG_ALARM = 500;
    public static final int HWTAG_ALARM_CLOCK = 502;
    public static final int HWTAG_ALARM_HEARTBEAT = 503;
    public static final int HWTAG_ALARM_RESOURCE = 501;
    public static final int HWTAG_AM = 100;
    public static final int HWTAG_AM_ACTIVITY = 101;
    public static final int HWTAG_AM_BROADCAST = 104;
    public static final int HWTAG_AM_CONTENTPROVIDER = 103;
    public static final int HWTAG_AM_FRAGMENT = 108;
    public static final int HWTAG_AM_KEYGUARD = 107;
    public static final int HWTAG_AM_SERVICE = 102;
    public static final int HWTAG_AM_TASK = 105;
    public static final int HWTAG_AM_VISIBILITY = 106;
    public static final int HWTAG_ANIMATION = 1400;
    public static final int HWTAG_DEAMON = 700;
    public static final int HWTAG_DEAMON_CREATE = 701;
    public static final int HWTAG_DEAMON_INSTANTIATION = 702;
    public static final int HWTAG_FWK = 0;
    public static final int HWTAG_INPUT = 1500;
    public static final int HWTAG_INPUT_COMBKEYEXT = 1501;
    public static final int HWTAG_INPUT_DISPATCH = 1507;
    public static final int HWTAG_INPUT_FINGERNAVI = 1505;
    public static final int HWTAG_INPUT_FINGERPRESS = 1504;
    public static final int HWTAG_INPUT_FINGERSENSE = 1503;
    public static final int HWTAG_INPUT_INPUT = 1506;
    public static final int HWTAG_INPUT_INPUTMETHOD = 1502;
    public static final int HWTAG_NM = 400;
    public static final int HWTAG_NM_CACHE = 402;
    public static final int HWTAG_NM_REMOTEVIEW = 401;
    public static final int HWTAG_PM = 200;
    public static final int HWTAG_PM_ABI = 203;
    public static final int HWTAG_PM_CLEAN = 210;
    public static final int HWTAG_PM_CUST = 205;
    public static final int HWTAG_PM_DEX = 204;
    public static final int HWTAG_PM_INSTALL = 207;
    public static final int HWTAG_PM_LIB = 202;
    public static final int HWTAG_PM_PERMISSION = 201;
    public static final int HWTAG_PM_SCAN = 206;
    public static final int HWTAG_PM_UNINSTALL = 209;
    public static final int HWTAG_PM_UPDATE = 208;
    public static final int HWTAG_POWER = 600;
    public static final int HWTAG_POWERONOFF = 1600;
    public static final int HWTAG_POWERONOFF_BOOTLOADER = 1602;
    public static final int HWTAG_POWERONOFF_INIT = 1603;
    public static final int HWTAG_POWERONOFF_RESOURCE = 1605;
    public static final int HWTAG_POWERONOFF_SYSTEMSERVER = 1601;
    public static final int HWTAG_POWERONOFF_ZYGOTE = 1604;
    public static final int HWTAG_POWER_BATTERY = 605;
    public static final int HWTAG_POWER_SCREENOFF = 604;
    public static final int HWTAG_POWER_SCREENON = 603;
    public static final int HWTAG_POWER_SMARTDISPLAY = 602;
    public static final int HWTAG_POWER_SMARTLIGHT = 601;
    public static final int HWTAG_POWER_VIBRATOR = 606;
    public static final int HWTAG_SECURITY = 1300;
    public static final int HWTAG_SENSOR = 1100;
    public static final int HWTAG_SENSOR_GESTURE = 1102;
    public static final int HWTAG_SENSOR_HUB = 1101;
    public static final int HWTAG_SERVICES = 800;
    public static final int HWTAG_SERVICES_BINDER = 801;
    public static final int HWTAG_SERVICES_CONTEXT = 804;
    public static final int HWTAG_SERVICES_IPC = 803;
    public static final int HWTAG_SERVICES_PARCEL = 802;
    public static final int HWTAG_SSECURITY_DISKENCRYPTION = 1305;
    public static final int HWTAG_SSECURITY_FINGERPRINT = 1303;
    public static final int HWTAG_SSECURITY_KEYSTORE = 1301;
    public static final int HWTAG_SSECURITY_SD = 1302;
    public static final int HWTAG_SSECURITY_TRUSTZONE = 1304;
    public static final int HWTAG_SSECURITY_USB = 1306;
    public static final int HWTAG_STORAGE = 1000;
    public static final int HWTAG_STORAGE_DATA = 1004;
    public static final int HWTAG_STORAGE_MULTIPARTITION = 1001;
    public static final int HWTAG_STORAGE_SD = 1002;
    public static final int HWTAG_STORAGE_SQLITE = 1003;
    public static final int HWTAG_USERS = 900;
    public static final int HWTAG_USER_PERMISSION = 902;
    public static final int HWTAG_USER_PROCESS = 901;
    public static final int HWTAG_USER_STORAGE = 903;
    public static final int HWTAG_USM = 1700;
    public static final int HWTAG_USM_FGTIME = 1701;
    public static final int HWTAG_VIEW = 1200;
    public static final int HWTAG_VIEW_DRAW = 1203;
    public static final int HWTAG_VIEW_LAYOUT = 1202;
    public static final int HWTAG_VIEW_MEASURE = 1201;
    public static final int HWTAG_WM = 300;
    public static final int HWTAG_WM_COVER = 306;
    public static final int HWTAG_WM_FOCUS = 309;
    public static final int HWTAG_WM_KEYGUARD = 305;
    public static final int HWTAG_WM_NAVIGATIONBAR = 303;
    public static final int HWTAG_WM_ORIENTATION = 308;
    public static final int HWTAG_WM_STARTWINDOW = 301;
    public static final int HWTAG_WM_STATUSBAR = 302;
    public static final int HWTAG_WM_TRANSITION = 310;
    public static final int HWTAG_WM_VISIBILITY = 307;
    public static final int HWTAG_WM_WINDOWCHANGE = 304;
    public static final String HW_LOG_PREFIX = "UL_";
    public static final String HW_LOG_PREFIX_ALARM = "UL_Alarm";
    public static final String HW_LOG_PREFIX_AM = "UL_AM";
    public static final String HW_LOG_PREFIX_INPUT = "UL_Input";
    public static final String HW_LOG_PREFIX_NM = "UL_NM";
    public static final String HW_LOG_PREFIX_PM = "UL_PM";
    public static final String HW_LOG_PREFIX_POWER = "UL_Power";
    public static final String HW_LOG_PREFIX_VIEW = "UL_View";
    public static final String HW_LOG_PREFIX_WM = "UL_WM";
    private static final String INIT_POSTFIX = "_INIT";
    public static final String[] INPUT_MODULE_TAG = {INPUT_TAG, "InputManager_combKeyExt", "InputManager_inputMethod", "InputManager_fingersense", "InputManager_fingerpress", "InputManager_fingernavi", "InputManager_input", "InputEvent"};
    private static final String INPUT_TAG = "InputManager";
    public static final int MODULE_NUMBEL = 18;
    public static final String[] NM_MODULE_TAG = {NM_TAG, "NotificationManager_remoteView", "NotificationManager_cache"};
    private static final String NM_TAG = "NotificationManager";
    public static final String[] PM_MODULE_TAG = {PM_TAG, "PackageManager_pemission", "PackageManager_lib", "PackageManager_abi", "PackageManager_dex", "PackageManager_cust", "PackageManager_scan", "PackageManager_install", "PackageManager_update", "PackageManager_uninstall", "PackageManager_clean"};
    private static final String PM_TAG = "PackageManager";
    public static final String[] POWERONOFF_MODULE_TAG = {POWERONOFF_TAG, "PowerOnOff_systemServer", "PowerOnOff_bootLoader", "PowerOnOff_init", "PowerOnOff_zygote", "PowerOnOff_resource"};
    private static final String POWERONOFF_TAG = "PowerOnOff";
    public static final String[] POWER_MODULE_TAG = {POWER_TAG, "PowerManager_smartLight", "PowerManager_smartDisplay", "PowerManager_screenOn", "PowerManager_screenOff", "PowerManager_battery", "PowerManager_vibrator"};
    private static final String POWER_TAG = "PowerManager";
    public static final int SCALE = 100;
    public static final String[] SECURITY_MODULE_TAG = {SECURITY_TAG, "Security_keystore", "Security_SD", "Security_fingerprint", "Security_trustZone", "Security_diskencryption", "Security_usb"};
    private static final String SECURITY_TAG = "Security";
    public static final String[] SENSOR_MODULE_TAG = {SENSOR_TAG, "Sensor_hub", "Sensor_gesture"};
    private static final String SENSOR_TAG = "Sensor";
    public static final String[] SERVICES_MODULE_TAG = {SERVICES_TAG, "Services_binder", "Services_parcel", "Services_ipc", "Services_context"};
    private static final String SERVICES_TAG = "Services";
    public static final String[] STORAGE_MODULE_TAG = {STORAGE_TAG, "Storage_multiPartition", "Storage_SD", "Storage_SQLite", "Storage_data"};
    private static final String STORAGE_TAG = "Storage";
    public static final String[] USERS_MODULE_TAG = {USERS_TAG, "Users_process", "Users_permission", "Users_storage"};
    private static final String USERS_TAG = "Users";
    static final String[] USM_MODULE_TAG = {USM_TAG, "UsageStatsManager_fgTime"};
    private static final String USM_TAG = "UsageStatsManager";
    public static final String[] VIEW_MODULE_TAG = {VIEW_TAG, "View_measure", "View_layout", "View_draw"};
    private static final String VIEW_TAG = "View";
    public static final String[] WM_MODULE_TAG = {WM_TAG, "WindowManager_startWindow", "WindowManager_statusBar", "WindowManager_navigationBar", "WindowManager_windowChange", "WindowManager_keyguard", "WindowManager_cover", "WindowManager_visibility", "WindowManager_orientation", "WindowManager_focus", "WindowManager_transition"};
    private static final String WM_TAG = "WindowManager";

    public enum FWK_MODULE {
        FWK,
        AM,
        PM,
        WM,
        NM,
        ALARM,
        POWER,
        DEAMON,
        SERVICES,
        USERS,
        STORAGE,
        SENSOR,
        VIEW,
        SECURITY,
        ANIMATION,
        INPUT,
        POWERONOFF,
        USM
    }

    public static final String getModuleTagStr(int module_tag) {
        int subModule_index = module_tag % 100;
        switch (FWK_MODULE.values()[module_tag / 100]) {
            case FWK:
                return FWK_TAG;
            case AM:
                if (subModule_index < AM_MODULE_TAG.length) {
                    return AM_MODULE_TAG[subModule_index];
                }
                return null;
            case PM:
                if (subModule_index < PM_MODULE_TAG.length) {
                    return PM_MODULE_TAG[subModule_index];
                }
                return null;
            case WM:
                if (subModule_index < WM_MODULE_TAG.length) {
                    return WM_MODULE_TAG[subModule_index];
                }
                return null;
            case NM:
                if (subModule_index < NM_MODULE_TAG.length) {
                    return NM_MODULE_TAG[subModule_index];
                }
                return null;
            case ALARM:
                if (subModule_index < ALARM_MODULE_TAG.length) {
                    return ALARM_MODULE_TAG[subModule_index];
                }
                return null;
            case POWER:
                if (subModule_index < POWER_MODULE_TAG.length) {
                    return POWER_MODULE_TAG[subModule_index];
                }
                return null;
            case DEAMON:
                if (subModule_index < DEAMON_MODULE_TAG.length) {
                    return DEAMON_MODULE_TAG[subModule_index];
                }
                return null;
            case SERVICES:
                if (subModule_index < SERVICES_MODULE_TAG.length) {
                    return SERVICES_MODULE_TAG[subModule_index];
                }
                return null;
            case USERS:
                if (subModule_index < USERS_MODULE_TAG.length) {
                    return USERS_MODULE_TAG[subModule_index];
                }
                return null;
            case STORAGE:
                if (subModule_index < STORAGE_MODULE_TAG.length) {
                    return STORAGE_MODULE_TAG[subModule_index];
                }
                return null;
            case SENSOR:
                if (subModule_index < SENSOR_MODULE_TAG.length) {
                    return SENSOR_MODULE_TAG[subModule_index];
                }
                return null;
            case VIEW:
                if (subModule_index < VIEW_MODULE_TAG.length) {
                    return VIEW_MODULE_TAG[subModule_index];
                }
                return null;
            case SECURITY:
                if (subModule_index < SECURITY_MODULE_TAG.length) {
                    return SECURITY_MODULE_TAG[subModule_index];
                }
                return null;
            case ANIMATION:
                if (subModule_index < ANIMATION_MODULE_TAG.length) {
                    return ANIMATION_MODULE_TAG[subModule_index];
                }
                return null;
            case INPUT:
                if (subModule_index < INPUT_MODULE_TAG.length) {
                    return INPUT_MODULE_TAG[subModule_index];
                }
                return null;
            case POWERONOFF:
                if (subModule_index < POWERONOFF_MODULE_TAG.length) {
                    return POWERONOFF_MODULE_TAG[subModule_index];
                }
                return null;
            case USM:
                if (subModule_index < USM_MODULE_TAG.length) {
                    return USM_MODULE_TAG[subModule_index];
                }
                return null;
            default:
                return null;
        }
    }
}
