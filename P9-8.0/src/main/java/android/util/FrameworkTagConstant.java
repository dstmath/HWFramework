package android.util;

public class FrameworkTagConstant {
    private static final /* synthetic */ int[] -android-util-FrameworkTagConstant$FWK_MODULESwitchesValues = null;
    public static final String[] ALARM_MODULE_TAG = new String[]{ALARM_TAG, "AlarmManager_resource", "AlarmManager_clock", "AlarmManager_heartbeat"};
    private static final String ALARM_TAG = "AlarmManager";
    public static final String[] AM_MODULE_TAG = new String[]{AM_TAG, "ActivityManager_activity", "ActivityManager_service", "ActivityManagercontentProvider", "ActivityManager_broadcast", "ActivityManager_task", "ActivityManager_visibility", "ActivityManager_keyguard"};
    private static final String AM_TAG = "ActivityManager";
    public static final String[] ANIMATION_MODULE_TAG = new String[]{ANIMATION_TAG};
    private static final String ANIMATION_TAG = "Animation";
    public static final String[] DEAMON_MODULE_TAG = new String[]{DEAMON_TAG, "DeamonManager_create", "DeamonManager_instantiation"};
    private static final String DEAMON_TAG = "DeamonManager";
    private static final String FLOW_POSTFIX = "_FLOW";
    public static final String[] FWK_MODULE_TAG = new String[]{FWK_TAG, AM_TAG, PM_TAG, WM_TAG, NM_TAG, ALARM_TAG, POWER_TAG, DEAMON_TAG, SERVICES_TAG, USERS_TAG, STORAGE_TAG, SENSOR_TAG, VIEW_TAG, SECURITY_TAG, ANIMATION_TAG, INPUT_TAG, POWERONOFF_TAG, USM_TAG};
    private static final String FWK_TAG = "Framework";
    public static final int HWTAG_ALARM = 500;
    public static final int HWTAG_ALARM_CLOCK = 502;
    public static final int HWTAG_ALARM_HEARTBEAT = 503;
    public static final int HWTAG_ALARM_RESOURCE = 501;
    public static final int HWTAG_AM = 100;
    public static final int HWTAG_AM_ACTIVITY = 101;
    public static final int HWTAG_AM_BROADCAST = 104;
    public static final int HWTAG_AM_CONTENTPROVIDER = 103;
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
    public static final int HWTAG_INPUT_FINGERNAVI = 1505;
    public static final int HWTAG_INPUT_FINGERPRESS = 1504;
    public static final int HWTAG_INPUT_FINGERSENSE = 1503;
    public static final int HWTAG_INPUT_INPUTMETHOD = 1502;
    public static final int HWTAG_NM = 400;
    public static final int HWTAG_NM_CACHE = 402;
    public static final int HWTAG_NM_REMOTEVIEW = 401;
    public static final int HWTAG_PM = 200;
    public static final int HWTAG_PM_ABI = 203;
    public static final int HWTAG_PM_CUST = 205;
    public static final int HWTAG_PM_DEX = 204;
    public static final int HWTAG_PM_LIB = 202;
    public static final int HWTAG_PM_PERMISSION = 201;
    public static final int HWTAG_PM_SCAN = 206;
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
    public static final int HWTAG_WM = 300;
    public static final int HWTAG_WM_COVER = 306;
    public static final int HWTAG_WM_KEYGUARD = 305;
    public static final int HWTAG_WM_NAVIGATIONBAR = 303;
    public static final int HWTAG_WM_ORIENTATION = 308;
    public static final int HWTAG_WM_STARTWINDOW = 301;
    public static final int HWTAG_WM_STATUSBAR = 302;
    public static final int HWTAG_WM_VISIBILITY = 307;
    public static final int HWTAG_WM_WINDOWCHANGE = 304;
    private static final String INIT_POSTFIX = "_INIT";
    public static final String[] INPUT_MODULE_TAG = new String[]{INPUT_TAG, "InputManager_combKeyExt", "InputManager_inputMethod", "InputManager_fingersense", "InputManager_fingerpress", "InputManager_fingernavi"};
    private static final String INPUT_TAG = "InputManager";
    public static final int MODULE_NUMBEL = 18;
    public static final String[] NM_MODULE_TAG = new String[]{NM_TAG, "NotificationManager_remoteView", "NotificationManager_cache"};
    private static final String NM_TAG = "NotificationManager";
    public static final String[] PM_MODULE_TAG = new String[]{PM_TAG, "PackageManager_pemission", "PackageManager_lib", "PackageManager_abi", "PackageManager_dex", "PackageManager_cust", "PackageManager_scan"};
    private static final String PM_TAG = "PackageManager";
    public static final String[] POWERONOFF_MODULE_TAG = new String[]{POWERONOFF_TAG, "PowerOnOff_systemServer", "PowerOnOff_bootLoader", "PowerOnOff_init", "PowerOnOff_zygote", "PowerOnOff_resource"};
    private static final String POWERONOFF_TAG = "PowerOnOff";
    public static final String[] POWER_MODULE_TAG = new String[]{POWER_TAG, "PowerManager_smartLight", "PowerManager_smartDisplay", "PowerManager_screenOn", "PowerManager_screenOff", "PowerManager_battery", "PowerManager_vibrator"};
    private static final String POWER_TAG = "PowerManager";
    public static final int SCALE = 100;
    public static final String[] SECURITY_MODULE_TAG = new String[]{SECURITY_TAG, "Security_keystore", "Security_SD", "Security_fingerprint", "Security_trustZone", "Security_diskencryption", "Security_usb"};
    private static final String SECURITY_TAG = "Security";
    public static final String[] SENSOR_MODULE_TAG = new String[]{SENSOR_TAG, "Sensor_hub", "Sensor_gesture"};
    private static final String SENSOR_TAG = "Sensor";
    public static final String[] SERVICES_MODULE_TAG = new String[]{SERVICES_TAG, "Services_binder", "Services_parcel", "Services_ipc", "Services_context"};
    private static final String SERVICES_TAG = "Services";
    public static final String[] STORAGE_MODULE_TAG = new String[]{STORAGE_TAG, "Storage_multiPartition", "Storage_SD", "Storage_SQLite", "Storage_data"};
    private static final String STORAGE_TAG = "Storage";
    public static final String[] USERS_MODULE_TAG = new String[]{USERS_TAG, "Users_process", "Users_permission", "Users_storage"};
    private static final String USERS_TAG = "Users";
    static final String[] USM_MODULE_TAG = new String[]{USM_TAG, "UsageStatsManager_fgTime"};
    private static final String USM_TAG = "UsageStatsManager";
    public static final String[] VIEW_MODULE_TAG = new String[]{VIEW_TAG};
    private static final String VIEW_TAG = "View";
    public static final String[] WM_MODULE_TAG = new String[]{WM_TAG, "WindowManager_startWindow", "WindowManager_statusBar", "WindowManager_navigationBar", "WindowManager_windowChange", "WindowManager_keyguard", "WindowManager_cover", "WindowManager_visibility", "WindowManager_orientation"};
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

    private static /* synthetic */ int[] -getandroid-util-FrameworkTagConstant$FWK_MODULESwitchesValues() {
        if (-android-util-FrameworkTagConstant$FWK_MODULESwitchesValues != null) {
            return -android-util-FrameworkTagConstant$FWK_MODULESwitchesValues;
        }
        int[] iArr = new int[FWK_MODULE.values().length];
        try {
            iArr[FWK_MODULE.ALARM.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[FWK_MODULE.AM.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[FWK_MODULE.ANIMATION.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[FWK_MODULE.DEAMON.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[FWK_MODULE.FWK.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[FWK_MODULE.INPUT.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[FWK_MODULE.NM.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[FWK_MODULE.PM.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[FWK_MODULE.POWER.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[FWK_MODULE.POWERONOFF.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[FWK_MODULE.SECURITY.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[FWK_MODULE.SENSOR.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[FWK_MODULE.SERVICES.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[FWK_MODULE.STORAGE.ordinal()] = 14;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[FWK_MODULE.USERS.ordinal()] = 15;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[FWK_MODULE.USM.ordinal()] = 16;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[FWK_MODULE.VIEW.ordinal()] = 17;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[FWK_MODULE.WM.ordinal()] = 18;
        } catch (NoSuchFieldError e18) {
        }
        -android-util-FrameworkTagConstant$FWK_MODULESwitchesValues = iArr;
        return iArr;
    }

    public static final String getModuleTagStr(int module_tag) {
        int subModule_index = module_tag % 100;
        switch (-getandroid-util-FrameworkTagConstant$FWK_MODULESwitchesValues()[FWK_MODULE.values()[module_tag / 100].ordinal()]) {
            case 1:
                if (subModule_index < ALARM_MODULE_TAG.length) {
                    return ALARM_MODULE_TAG[subModule_index];
                }
                return null;
            case 2:
                if (subModule_index < AM_MODULE_TAG.length) {
                    return AM_MODULE_TAG[subModule_index];
                }
                return null;
            case 3:
                if (subModule_index < ANIMATION_MODULE_TAG.length) {
                    return ANIMATION_MODULE_TAG[subModule_index];
                }
                return null;
            case 4:
                if (subModule_index < DEAMON_MODULE_TAG.length) {
                    return DEAMON_MODULE_TAG[subModule_index];
                }
                return null;
            case 5:
                return FWK_TAG;
            case 6:
                if (subModule_index < INPUT_MODULE_TAG.length) {
                    return INPUT_MODULE_TAG[subModule_index];
                }
                return null;
            case 7:
                if (subModule_index < NM_MODULE_TAG.length) {
                    return NM_MODULE_TAG[subModule_index];
                }
                return null;
            case 8:
                if (subModule_index < PM_MODULE_TAG.length) {
                    return PM_MODULE_TAG[subModule_index];
                }
                return null;
            case 9:
                if (subModule_index < POWER_MODULE_TAG.length) {
                    return POWER_MODULE_TAG[subModule_index];
                }
                return null;
            case 10:
                if (subModule_index < POWERONOFF_MODULE_TAG.length) {
                    return POWERONOFF_MODULE_TAG[subModule_index];
                }
                return null;
            case 11:
                if (subModule_index < SECURITY_MODULE_TAG.length) {
                    return SECURITY_MODULE_TAG[subModule_index];
                }
                return null;
            case 12:
                if (subModule_index < SENSOR_MODULE_TAG.length) {
                    return SENSOR_MODULE_TAG[subModule_index];
                }
                return null;
            case 13:
                if (subModule_index < SERVICES_MODULE_TAG.length) {
                    return SERVICES_MODULE_TAG[subModule_index];
                }
                return null;
            case 14:
                if (subModule_index < STORAGE_MODULE_TAG.length) {
                    return STORAGE_MODULE_TAG[subModule_index];
                }
                return null;
            case 15:
                if (subModule_index < USERS_MODULE_TAG.length) {
                    return USERS_MODULE_TAG[subModule_index];
                }
                return null;
            case 16:
                if (subModule_index < USM_MODULE_TAG.length) {
                    return USM_MODULE_TAG[subModule_index];
                }
                return null;
            case 17:
                if (subModule_index < VIEW_MODULE_TAG.length) {
                    return VIEW_MODULE_TAG[subModule_index];
                }
                return null;
            case 18:
                if (subModule_index < WM_MODULE_TAG.length) {
                    return WM_MODULE_TAG[subModule_index];
                }
                return null;
            default:
                return null;
        }
    }
}
