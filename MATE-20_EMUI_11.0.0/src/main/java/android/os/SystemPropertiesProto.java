package android.os;

public final class SystemPropertiesProto {
    public static final long AAC_DRC = 1146756268034L;
    public static final long AAUDIO = 1146756268035L;
    public static final long AF_FAST_TRACK_MULTIPLIER = 1120986464260L;
    public static final long CAMERA = 1146756268037L;
    public static final long DALVIK_VM = 1146756268038L;
    public static final long DRM_64BIT_ENABLED = 1133871366151L;
    public static final long DRM_SERVICE_ENABLED = 1133871366152L;
    public static final long DUMPSTATE_DRY_RUN = 1133871366153L;
    public static final long EXTRA_PROPERTIES = 2246267895809L;
    public static final long GSM_SIM_OPERATOR_NUMERIC = 1138166333450L;
    public static final long HAL_INSTRUMENTATION_ENABLE = 1133871366155L;
    public static final long INIT_SVC = 1146756268044L;
    public static final long KEYGUARD_NO_REQUIRE_SIM = 1133871366157L;
    public static final long LIBC_DEBUG_MALLOC_OPTIONS = 1138166333454L;
    public static final long LIBC_DEBUG_MALLOC_PROGRAM = 1138166333455L;
    public static final long LOG = 1146756268048L;
    public static final long MEDIA_MEDIADRMSERVICE_ENABLE = 1133871366161L;
    public static final long MEDIA_RECORDER_SHOW_MANUFACTURER_AND_MODEL = 1133871366162L;
    public static final long PERSIST = 1146756268051L;
    public static final long PM_DEXOPT = 1146756268052L;
    public static final long RO = 1146756268053L;
    public static final long SENDBUG_PREFERRED_DOMAIN = 1138166333462L;
    public static final long SERVICE_BOOTANIM_EXIT = 1120986464279L;
    public static final long SYS = 1146756268056L;
    public static final long TELEPHONY_LTE_ON_CDMA_DEVICE = 1120986464281L;
    public static final long TOMBSTONED_MAX_TOMBSTONE_COUNT = 1120986464282L;
    public static final long VOLD_DECRYPT = 1138166333467L;
    public static final long VOLD_POST_FS_DATA_DONE = 1120986464284L;
    public static final long VTS_NATIVE_SERVER_ON = 1120986464285L;
    public static final long WIFI_DIRECT_INTERFACE = 1138166333470L;
    public static final long WIFI_INTERFACE = 1138166333471L;

    public final class Property {
        public static final long NAME = 1138166333441L;
        public static final long VALUE = 1138166333442L;

        public Property() {
        }
    }

    public final class AacDrc {
        public static final long BOOST = 1120986464257L;
        public static final long CUT = 1120986464258L;
        public static final long ENC_TARGET_LEVEL = 1120986464259L;
        public static final long HEAVY = 1120986464260L;
        public static final long REFERENCE_LEVEL = 1120986464261L;

        public AacDrc() {
        }
    }

    public final class Aaudio {
        public static final long HW_BURST_MIN_USEC = 1120986464257L;
        public static final long MINIMUM_SLEEP_USEC = 1120986464258L;
        public static final long MIXER_BURSTS = 1120986464259L;
        public static final long MMAP_EXCLUSIVE_POLICY = 1120986464260L;
        public static final long MMAP_POLICY = 1120986464261L;
        public static final long WAKEUP_DELAY_USEC = 1120986464262L;

        public Aaudio() {
        }
    }

    public final class Camera {
        public static final long DISABLE_ZSL_MODE = 1133871366145L;
        public static final long FIFO_DISABLE = 1120986464258L;

        public Camera() {
        }
    }

    public final class DalvikVm {
        public static final long APPIMAGEFORMAT = 1138166333441L;
        public static final long BACKGROUNDGCTYPE = 1138166333442L;
        public static final long CHECKJNI = 1133871366147L;
        public static final long DEX2OAT_FILTER = 1138166333444L;
        public static final long DEX2OAT_FLAGS = 1138166333445L;
        public static final long DEX2OAT_THREADS = 1120986464262L;
        public static final long DEX2OAT_XMS = 1138166333447L;
        public static final long DEX2OAT_XMX = 1138166333448L;
        public static final long DEXOPT_SECONDARY = 1133871366153L;
        public static final long EXECUTION_MODE = 1138166333450L;
        public static final long EXTRA_OPTS = 1138166333451L;
        public static final long GCTYPE = 1138166333452L;
        public static final long HEAPGROWTHLIMIT = 1138166333453L;
        public static final long HEAPMAXFREE = 1138166333454L;
        public static final long HEAPMINFREE = 1138166333455L;
        public static final long HEAPSIZE = 1138166333456L;
        public static final long HEAPSTARTSIZE = 1138166333457L;
        public static final long HEAPTARGETUTILIZATION = 1103806595090L;
        public static final long HOT_STARTUP_METHOD_SAMPLES = 1120986464275L;
        public static final long IMAGE_DEX2OAT_FILTER = 1138166333460L;
        public static final long IMAGE_DEX2OAT_FLAGS = 1138166333461L;
        public static final long IMAGE_DEX2OAT_THREADS = 1120986464278L;
        public static final long IMAGE_DEX2OAT_XMS = 1138166333463L;
        public static final long IMAGE_DEX2OAT_XMX = 1138166333464L;
        public static final long ISA_ARM64_FEATURES = 1138166333467L;
        public static final long ISA_ARM64_VARIANT = 1138166333468L;
        public static final long ISA_ARM_FEATURES = 1138166333465L;
        public static final long ISA_ARM_VARIANT = 1138166333466L;
        public static final long ISA_MIPS64_FEATURES = 1138166333471L;
        public static final long ISA_MIPS64_VARIANT = 1138166333472L;
        public static final long ISA_MIPS_FEATURES = 1138166333469L;
        public static final long ISA_MIPS_VARIANT = 1138166333470L;
        public static final long ISA_UNKNOWN_FEATURES = 1138166333473L;
        public static final long ISA_UNKNOWN_VARIANT = 1138166333474L;
        public static final long ISA_X86_64_FEATURES = 1138166333475L;
        public static final long ISA_X86_64_VARIANT = 1138166333476L;
        public static final long ISA_X86_FEATURES = 1138166333477L;
        public static final long ISA_X86_VARIANT = 1138166333478L;
        public static final long JITINITIALSIZE = 1138166333479L;
        public static final long JITMAXSIZE = 1138166333480L;
        public static final long JITPRITHREADWEIGHT = 1120986464297L;
        public static final long JITTHRESHOLD = 1120986464298L;
        public static final long JITTRANSITIONWEIGHT = 1120986464299L;
        public static final long JNIOPTS = 1138166333484L;
        public static final long LOCKPROF_THRESHOLD = 1120986464301L;
        public static final long METHOD_TRACE = 1133871366190L;
        public static final long METHOD_TRACE_FILE = 1138166333487L;
        public static final long METHOD_TRACE_FILE_SIZ = 1120986464304L;
        public static final long METHOD_TRACE_STREAM = 1133871366193L;
        public static final long PROFILESYSTEMSERVER = 1133871366194L;
        public static final long STACK_TRACE_DIR = 1138166333491L;
        public static final long USEJIT = 1133871366196L;
        public static final long USEJITPROFILES = 1133871366197L;
        public static final long ZYGOTE_MAX_BOOT_RETRY = 1120986464310L;

        public DalvikVm() {
        }
    }

    public final class InitSvc {
        public static final long ADBD = 1159641169921L;
        public static final long AUDIOSERVER = 1159641169922L;
        public static final long BOOTANIM = 1159641169923L;
        public static final long BUFFERHUBD = 1159641169924L;
        public static final long CAMERASERVER = 1159641169925L;
        public static final long CLEAR_BCB = 1159641169926L;
        public static final long DRM = 1159641169927L;
        public static final long GATEKEEPERD = 1159641169928L;
        public static final long HEALTHD = 1159641169929L;
        public static final long HIDL_MEMORY = 1159641169930L;
        public static final long HOSTAPD = 1159641169931L;
        public static final long HWSERVICEMANAGER = 1159641169932L;
        public static final long INSTALLD = 1159641169933L;
        public static final long KEYSTORE = 1159641169934L;
        public static final long LMKD = 1159641169935L;
        public static final long LOGD = 1159641169936L;
        public static final long LOGD_REINIT = 1159641169937L;
        public static final long MEDIA = 1159641169938L;
        public static final long MEDIADRM = 1159641169939L;
        public static final long MEDIAEXTRACTOR = 1159641169940L;
        public static final long MEDIAMETRICS = 1159641169941L;
        public static final long NETD = 1159641169942L;
        public static final long PERFORMANCED = 1159641169943L;
        public static final long RIL_DAEMON = 1159641169944L;
        public static final long SERVICEMANAGER = 1159641169945L;
        public static final int STATUS_RUNNING = 1;
        public static final int STATUS_STOPPED = 2;
        public static final int STATUS_UNKNOWN = 0;
        public static final long STORAGED = 1159641169946L;
        public static final long SURFACEFLINGER = 1159641169947L;
        public static final long THERMALSERVICE = 1159641169948L;
        public static final long TOMBSTONED = 1159641169949L;
        public static final long UEVENTD = 1159641169950L;
        public static final long UPDATE_ENGINE = 1159641169951L;
        public static final long UPDATE_VERIFIER_NONENCRYPTED = 1159641169952L;
        public static final long VIRTUAL_TOUCHPAD = 1159641169953L;
        public static final long VNDSERVICEMANAGER = 1159641169954L;
        public static final long VOLD = 1159641169955L;
        public static final long VR_HWC = 1159641169956L;
        public static final long WEBVIEW_ZYGOTE32 = 1159641169957L;
        public static final long WIFICOND = 1159641169958L;
        public static final long WPA_SUPPLICANT = 1159641169959L;
        public static final long ZYGOTE = 1159641169960L;
        public static final long ZYGOTE_SECONDARY = 1159641169961L;

        public InitSvc() {
        }
    }

    public final class Log {
        public static final long TAG_STATS_LOG = 1138166333442L;
        public static final long TAG_WIFI_HAL = 1138166333441L;

        public Log() {
        }
    }

    public final class Persist {
        public static final long CONFIG_CALIBRATION_FAC = 1138166333441L;
        public static final long DBG_VOLTE_AVAIL_OVR = 1120986464258L;
        public static final long DBG_VT_AVAIL_OVR = 1120986464259L;
        public static final long DBG_WFC_AVAIL_OVR = 1120986464260L;
        public static final long RADIO_AIRPLANE_MODE_ON = 1120986464261L;
        public static final long RADIO_MULTISIM_CONFIG = 1138166333446L;
        public static final long RCS_SUPPORTED = 1120986464263L;
        public static final long SYS_CRASH_RCU = 1133871366152L;
        public static final long SYS_DALVIK_VM_LIB_2 = 1138166333449L;
        public static final long SYS_SF_COLOR_SATURATION = 1108101562378L;
        public static final long SYS_TIMEZONE = 1138166333451L;

        public Persist() {
        }
    }

    public final class PmDexopt {
        public static final long AB_OTA = 1138166333441L;
        public static final long BG_DEXOPT = 1138166333442L;
        public static final long BOOT = 1138166333443L;
        public static final long FIRST_BOOT = 1138166333444L;
        public static final long INSTALL = 1138166333445L;

        public PmDexopt() {
        }
    }

    public final class Ro {
        public static final long ADB_SECURE = 1133871366145L;
        public static final long ARCH = 1138166333442L;
        public static final long AUDIO_IGNORE_EFFECTS = 1133871366147L;
        public static final long AUDIO_MONITOR_ROTATION = 1133871366148L;
        public static final long BASEBAND = 1138166333445L;
        public static final long BOARD_PLATFORM = 1138166333446L;
        public static final long BOOT = 1146756268039L;
        public static final long BOOTIMAGE = 1146756268040L;
        public static final long BOOTLOADER = 1138166333449L;
        public static final long BOOTMODE = 1138166333450L;
        public static final long BUILD = 1146756268043L;
        public static final long CAMERA_NOTIFY_NFC = 1133871366156L;
        public static final long CARRIER = 1138166333453L;
        public static final long COM_ANDROID_DATAROAMING = 1133871366158L;
        public static final long COM_ANDROID_PROV_MOBILEDATA = 1133871366159L;
        public static final long COM_GOOGLE_CLIENTIDBASE = 1138166333456L;
        public static final long CONFIG = 1146756268049L;
        public static final long CONTROL_PRIVAPP_PERMISSIONS = 1138166333458L;
        public static final long CP_SYSTEM_OTHER_ODEX = 1120986464275L;
        public static final long CRYPTO_SCRYPT_PARAMS = 1138166333460L;
        public static final long CRYPTO_STATE = 1138166333461L;
        public static final long CRYPTO_TYPE = 1138166333462L;
        public static final long DALVIK_VM_NATIVE_BRIDGE = 1138166333463L;
        public static final long DEBUGGABLE = 1133871366168L;
        public static final long FRP_PST = 1138166333465L;
        public static final long GFX_DRIVER_0 = 1138166333466L;
        public static final long GFX_DRIVER_WHITELIST_0 = 1138166333485L;
        public static final long HARDWARE = 1146756268059L;
        public static final long KERNEL_QEMU = 1120986464284L;
        public static final long KERNEL_QEMU_GLES = 1120986464285L;
        public static final long OEM_UNLOCK_SUPPORTED = 1120986464286L;
        public static final long OPENGLES_VERSION = 1120986464287L;
        public static final long PRODUCT = 1146756268064L;
        public static final long PROPERTY_SERVICE_VERSION = 1120986464289L;
        public static final long REVISION = 1138166333475L;
        public static final long SF_LCD_DENSITY = 1120986464292L;
        public static final long STORAGE_MANAGER_ENABLED = 1133871366181L;
        public static final long TELEPHONY = 1146756268070L;
        public static final long URL_LEGAL = 1138166333479L;
        public static final long URL_LEGAL_ANDROID_PRIVACY = 1138166333480L;
        public static final long VENDOR = 1146756268073L;
        public static final long VNDK_VERSION = 1138166333482L;
        public static final long VTS_COVERAGE = 1120986464299L;
        public static final long ZYGOTE = 1138166333484L;

        public Ro() {
        }

        public final class Boot {
            public static final long AVB_VERSION = 1138166333441L;
            public static final long BASEBAND = 1138166333442L;
            public static final long BOOTDEVICE = 1138166333443L;
            public static final long BOOTLOADER = 1138166333444L;
            public static final long BOOTTIME = 2237677961221L;
            public static final long CONSOLE = 1138166333446L;
            public static final long FAKE_BATTERY = 1120986464263L;
            public static final long HARDWARE = 1138166333448L;
            public static final long HARDWARE_COLOR = 1138166333449L;
            public static final long HARDWARE_REVISION = 1138166333450L;
            public static final long HARDWARE_SKU = 1138166333451L;
            public static final long KEYMASTER = 1138166333452L;
            public static final long MODE = 1138166333453L;
            public static final long REVISION = 1138166333454L;
            public static final long SLOT_SUFFIX = 1138166333455L;
            public static final long VBMETA_AVB_VERSION = 1138166333456L;
            public static final long VENDOR_OVERLAY_THEME = 1138166333457L;
            public static final long VERIFIEDBOOTSTATE = 1138166333458L;
            public static final long VERITYMODE = 1138166333459L;
            public static final long WIFICOUNTRYCODE = 1138166333460L;

            public Boot() {
            }
        }

        public final class BootImage {
            public static final long BUILD_DATE = 1138166333441L;
            public static final long BUILD_DATE_UTC = 1112396529666L;
            public static final long BUILD_FINGERPRINT = 1138166333443L;

            public BootImage() {
            }
        }

        public final class Build {
            public static final long DATE = 1138166333441L;
            public static final long DATE_UTC = 1112396529666L;
            public static final long DESCRIPTION = 1138166333443L;
            public static final long DISPLAY_ID = 1138166333444L;
            public static final long HOST = 1138166333445L;
            public static final long ID = 1138166333446L;
            public static final long PRODUCT = 1138166333447L;
            public static final long SYSTEM_ROOT_IMAGE = 1133871366152L;
            public static final long TAGS = 1138166333449L;
            public static final long TYPE = 1138166333450L;
            public static final long USER = 1138166333451L;
            public static final long VERSION = 1146756268044L;

            public Build() {
            }

            public final class Version {
                public static final long BASE_OS = 1138166333441L;
                public static final long CODENAME = 1138166333442L;
                public static final long INCREMENTAL = 1138166333443L;
                public static final long PREVIEW_SDK = 1120986464260L;
                public static final long RELEASE = 1138166333445L;
                public static final long SDK = 1120986464262L;
                public static final long SECURITY_PATCH = 1138166333447L;

                public Version() {
                }
            }
        }

        public final class Config {
            public static final long ALARM_ALERT = 1138166333441L;
            public static final long MEDIA_VOL_STEPS = 1120986464258L;
            public static final long NOTIFICATION_SOUND = 1138166333443L;
            public static final long RINGTONE = 1138166333444L;
            public static final long VC_CALL_VOL_STEPS = 1120986464261L;

            public Config() {
            }
        }

        public final class Hardware {
            public static final long ACTIVITY_RECOGNITION = 1138166333442L;
            public static final long AUDIO = 1138166333443L;
            public static final long AUDIO_A2DP = 1138166333445L;
            public static final long AUDIO_POLICY = 1138166333444L;
            public static final long AUDIO_PRIMARY = 1138166333446L;
            public static final long AUDIO_USB = 1138166333447L;
            public static final long BOOTCTRL = 1138166333448L;
            public static final long CAMERA = 1138166333449L;
            public static final long CONSUMERIR = 1138166333450L;
            public static final long CONTEXT_HUB = 1138166333451L;
            public static final long EGL = 1138166333452L;
            public static final long FINGERPRINT = 1138166333453L;
            public static final long FLP = 1138166333454L;
            public static final long GATEKEEPER = 1138166333455L;
            public static final long GPS = 1138166333456L;
            public static final long GRALLOC = 1138166333457L;
            public static final long HDMI_CEC = 1138166333458L;
            public static final long HWCOMPOSER = 1138166333459L;
            public static final long INPUT = 1138166333460L;
            public static final long KEYSTORE = 1138166333461L;
            public static final long LIGHTS = 1138166333462L;
            public static final long LOCAL_TIME = 1138166333463L;
            public static final long MEMTRACK = 1138166333464L;
            public static final long NFC = 1138166333465L;
            public static final long NFC_NCI = 1138166333466L;
            public static final long NFC_TAG = 1138166333467L;
            public static final long NVRAM = 1138166333468L;
            public static final long POWER = 1138166333469L;
            public static final long RADIO = 1138166333470L;
            public static final long SENSORS = 1138166333471L;
            public static final long SOUND_TRIGGER = 1138166333472L;
            public static final long THERMAL = 1138166333473L;
            public static final long TV_INPUT = 1138166333474L;
            public static final long TYPE = 1138166333475L;
            public static final long VALUE = 1138166333441L;
            public static final long VEHICLE = 1138166333476L;
            public static final long VIBRATOR = 1138166333477L;
            public static final long VIRTUAL_DEVICE = 1138166333478L;
            public static final long VULKAN = 1138166333479L;

            public Hardware() {
            }
        }

        public final class Product {
            public static final long BOARD = 1138166333441L;
            public static final long BRAND = 1138166333442L;
            public static final long CPU_ABI = 1138166333443L;
            public static final long CPU_ABILIST = 2237677961220L;
            public static final long CPU_ABILIST32 = 2237677961221L;
            public static final long CPU_ABILIST64 = 2237677961222L;
            public static final long DEVICE = 1138166333447L;
            public static final long FIRST_API_LEVEL = 1120986464264L;
            public static final long MANUFACTURER = 1138166333449L;
            public static final long MODEL = 1138166333450L;
            public static final long NAME = 1138166333451L;
            public static final long VENDOR = 1146756268044L;

            public Product() {
            }

            public final class Vendor {
                public static final long BRAND = 1138166333441L;
                public static final long DEVICE = 1138166333442L;
                public static final long MANUFACTURER = 1138166333443L;
                public static final long MODEL = 1138166333444L;
                public static final long NAME = 1138166333445L;

                public Vendor() {
                }
            }
        }

        public final class Telephony {
            public static final long CALL_RING_MULTIPLE = 1133871366145L;
            public static final long DEFAULT_CDMA_SUB = 1120986464258L;
            public static final long DEFAULT_NETWORK = 1120986464259L;

            public Telephony() {
            }
        }

        public final class Vendor {
            public static final long BUILD_DATE = 1138166333441L;
            public static final long BUILD_DATE_UTC = 1112396529666L;
            public static final long BUILD_FINGERPRINT = 1138166333443L;

            public Vendor() {
            }
        }
    }

    public final class Sys {
        public static final long BOOT_COMPLETED = 1120986464257L;
        public static final long BOOT_FROM_CHARGER_MODE = 1120986464258L;
        public static final long RETAILDEMO_ENABLED = 1120986464259L;
        public static final long SHUTDOWN_REQUESTED = 1138166333444L;
        public static final long USB = 1146756268037L;

        public Sys() {
        }

        public final class Usb {
            public static final long CONFIG = 1138166333441L;
            public static final long CONFIGFS = 1120986464258L;
            public static final long CONTROLLER = 1138166333443L;
            public static final long FFS_MAX_READ = 1120986464260L;
            public static final long FFS_MAX_WRITE = 1120986464261L;
            public static final long FFS_MTP_READY = 1120986464262L;
            public static final long FFS_READY = 1120986464263L;
            public static final long MTP_DEVICE_TYPE = 1120986464264L;
            public static final long STATE = 1138166333449L;

            public Usb() {
            }
        }
    }
}
