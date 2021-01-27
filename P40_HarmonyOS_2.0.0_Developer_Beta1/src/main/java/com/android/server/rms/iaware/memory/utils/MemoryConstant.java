package com.android.server.rms.iaware.memory.utils;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.rms.iaware.cpu.CpuFeature;
import com.huawei.android.os.SystemPropertiesEx;
import java.util.ArrayList;
import java.util.Map;

public final class MemoryConstant {
    private static final int ANON_TARGET_MIN = 200;
    public static final long APP_AVG_USS = 20480;
    public static final int AWARE_INVAILD_KILL_NUM = 3;
    public static final int AWARE_INVAILD_KILL_THRESHOLD = 5;
    public static final String BASE_WEIGHT = "baseWeight";
    private static final long BIG_APP_MEMMORY_LIMIT = 600;
    public static final int BYTE_SIZE = 4;
    public static final String CACHED_MEMORY_SWITCH = "cachedMemorySwitch";
    public static final String CACHED_MOMORY_CLEAN = "CachedMemoryClean";
    public static final String CACHED_MOMORY_KILL_START_MAX_COUNT = "killStartMaxCount";
    public static final String CACHED_MOMORY_KILL_START_MAX_TIME = "killStartMaxTime";
    public static final int CAMERA_AUTOSTOP_TIMEOUT = 1;
    public static final String CAMERA_PACKAGE_NAME = "com.huawei.camera";
    private static final int CAMERA_POWERUP_WATERMARK_NOTINIT = -2;
    public static final int CAMERA_POWERUP_WORKER_MASK = 16746241;
    public static final int CAMERA_STREAMING_WORKER_MASK = 1013762;
    private static final String CONFIG_PROTECT_LRU_DEFAULT = "0 0 0";
    private static final long CRITICAL_MEMORY = 600;
    private static final int DEFAULT_ARRAYLIST_SIZE = 10;
    private static final int DEFAULT_COMPRESS_IDEL_THRESHOLD = 50;
    public static final double DEFAULT_COMPRESS_RATIO = 0.35d;
    private static final long DEFAULT_CPU_IDLE_THRESHOLD = 30;
    private static final long DEFAULT_CPU_NORMAL_THRESHOLD = 60;
    public static final int DEFAULT_DIRECT_SWAPPINESS = 60;
    private static final int DEFAULT_DIVISOR = 2;
    public static final int DEFAULT_EXTRA_FREE_KBYTES = SystemPropertiesEx.getInt("sys.sysctl.extra_free_kbytes", (int) PROCESS_LIST_EXTRA_FREE_KBYTES);
    public static final int DEFAULT_INVALID_UID = 0;
    public static final int DEFAULT_INVALID_VALUE = -1;
    private static final long DEFAULT_MEMORY_LIMIT = 600;
    private static final long DEFAULT_NOTIFICATION_INTERVAL = 1000;
    private static final long DEFAULT_PERIOD = 2000;
    private static final int DEFAULT_PERIOD_LOOP_TIMES = 3;
    public static final int DEFAULT_SWAPPINESS = 60;
    public static final int DIRCONSTANT = 50;
    public static final int DO_APPLY_MASK = 1;
    public static final int DO_SHRINK_MASK = 2;
    private static final long EMERGENCY_MEMORY = 300;
    private static final long ENABLE_POLLING_PERIOD = 10000;
    public static final String EXTRA_DEPEND_ON_PSI_SWITCH = "extraDependOnPsiSwitch";
    public static final int FACE_RECOGNIZE_AUTOSTOPTIMEOUT = -1;
    public static final String FACE_RECOGNIZE_CONFIGNAME = "face.recognize";
    public static final String FLOAT_BALL_STAT_MAX_TIME = "floatBallStateMaxTime";
    public static final String FLOAT_PROTECT_SWITCH = "switch";
    public static final String FLOAT_WINDOW = "floatWindow";
    public static final long GB_SIZE = 1073741824;
    private static final CameraCharacteristics.Key<int[]> HW_CAMERA_MEMORY_REQUIREMENT_SUPPORTED = new CameraCharacteristics.Key<>("com.huawei.device.capabilities.hwCameraMemoryRequirementSupported", int[].class);
    public static final String INTELLIGENT_CLEAN = "IntelligentClean";
    public static final String INTELLIGENT_CLEAN_SWITCH = "switch";
    public static final String IN_CALL_UI_PACKAGE_NAME_PHONE = "com.android.incallui";
    private static final long KILO = 1024;
    public static final int LARGE_CPU_MASK = 16711680;
    public static final String LAUNCHER_PACKAGE_NAME = "com.huawei.android.launcher";
    public static final String LINE_SEPARATOR = System.lineSeparator();
    public static final int LITTLE_CPU_MASK = 983040;
    private static final int LOAD_THRESHOULD = 100;
    public static final int MAX_APPNAME_LEN = 64;
    public static final int MAX_CAMERAPRELOADRECLAIM_KILLUSS = 102400;
    public static final int MAX_CAMERAPRELOADRECLAIM_RECLAIMDELAY = 10000;
    public static final int MAX_EXTRA_FREE_KBYTES = 200000;
    public static final String MAX_FLOAT_PROTECT = "maxFloatProtect";
    public static final int MAX_MEM_PINFILES_COUNT = 20;
    private static final long MAX_NOTIFICATION_INTERVAL = 10000;
    public static final int MAX_PERCENT = 100;
    private static final long MAX_PERIOD = 600000;
    private static final int MAX_PROTECT_LRU_OCCUPPY = 50;
    private static final long MAX_REQUEST_MEMORY_ALLOC = 1200;
    private static final long MAX_REQUEST_MEMORY_RELEASE = 2;
    private static final int MAX_SWAPPINESS = 200;
    public static final long MB_SIZE = 1048576;
    public static final String MEM_ACTION_SYSTRIM = "com.huawei.iaware.sys.trim";
    private static final int MEM_BALANCE_SWAPPINESS_INDEX = 1;
    public static final String MEM_BIGMEM_ENABLE_DMESERVER_SWITCH = "bigMemStartDmeSwitch";
    public static final String MEM_CAMERAPRELOADRECLAIM_CONFIGNAME = "CameraPreloadReclaim";
    public static final String MEM_CAMERAPRELOADRECLAIM_CONFIGNAME_KILLUSS = "cameraPreloadKillUss";
    public static final String MEM_CAMERAPRELOADRECLAIM_CONFIGNAME_RECLAIMDELAY = "cameraPreloadReclaimDelay";
    public static final String MEM_CAMERAPRELOADRECLAIM_CONFIGNAME_SWITCH = "cameraPreloadSwitch";
    public static final String MEM_CONSTANT_API_MAX_REQUEST_MEM = "api_max_req_mem";
    public static final String MEM_CONSTANT_APPTRIMSWITCH = "apptrim_switch";
    public static final String MEM_CONSTANT_BIGMEMCRITICALMEMORYNAME = "bigMemCriticalMemory";
    public static final String MEM_CONSTANT_CAMERAPOWERUPNAME = "camera_powerup_ion_memory";
    public static final String MEM_CONSTANT_CLEAN_ALL = "cleanAllSwitch";
    public static final String MEM_CONSTANT_CONFIGNAME = "MemoryConstant";
    public static final String MEM_CONSTANT_DEFAULTCRITICALMEMORYNAME = "defaultCriticalMemory";
    public static final String MEM_CONSTANT_DIRECTSWAPPINESSNAME = "direct_swappiness";
    public static final String MEM_CONSTANT_EMERGEMCYMEMORYNAME = "emergencyMemory";
    public static final String MEM_CONSTANT_EXACT_KILL = "exactKillSwitch";
    public static final String MEM_CONSTANT_EXTRAFREEKBYTESNAME = "extra_free_kbytes";
    public static final String MEM_CONSTANT_FAST_KILL = "fastKillSwitch";
    public static final String MEM_CONSTANT_FAST_QUICKKILL = "fastQuickKillSwitch";
    public static final String MEM_CONSTANT_GMCSWITCH = "gmc_switch";
    public static final String MEM_CONSTANT_GPUNAME = "gpuMemory";
    public static final String MEM_CONSTANT_HIGHCPULOADNAME = "highCpuLoad";
    public static final String MEM_CONSTANT_IONSPEEDUPSWITCH = "camera_ion_speedup_switch";
    public static final String MEM_CONSTANT_KERNEL_ANON_TARGET = "kernCompressAnonTarget";
    public static final String MEM_CONSTANT_KERNEL_COMPRESS_SWITCH = "kernCompressSwitch";
    public static final String MEM_CONSTANT_KERNEL_IDLE_THRESHOLD = "kernCompressIdleThreshold";
    public static final String MEM_CONSTANT_KERNEL_SWAP_PERCENT = "kernCompressSwapPercent";
    public static final String MEM_CONSTANT_KILL_GAP = "killGapMem";
    public static final String MEM_CONSTANT_LOWCPULOADNAME = "lowCpuLoad";
    private static final int MEM_CONSTANT_MAX_TOPN = 50;
    public static final String MEM_CONSTANT_MINTIMERPERIOD = "minTimerPeriod";
    private static final int MEM_CONSTANT_MIN_TOPN = 3;
    public static final String MEM_CONSTANT_MORE_PREVIOUS = "morePrevious";
    public static final String MEM_CONSTANT_NORMALMEMORYNAME = "normalMemory";
    public static final String MEM_CONSTANT_PREREAD_ODEX = "prereadOdexSwitch";
    public static final String MEM_CONSTANT_PRINT_ENHANCE = "printEnhance";
    public static final String MEM_CONSTANT_PROCESSLIMIT = "numProcessLimit";
    public static final String MEM_CONSTANT_PROCESSPERCENT = "numEmptyProcessPercent";
    public static final String MEM_CONSTANT_PROTECTLRULIMIT = "protect_lru_limit";
    public static final String MEM_CONSTANT_PROTECTLRU_SWITCH = "protectLruSwitch";
    public static final String MEM_CONSTANT_PROTECTRATIO = "protect_lru_ratio";
    public static final String MEM_CONSTANT_RAMSIZENAME = "ramsize";
    public static final String MEM_CONSTANT_RECLAIMFILECACHE = "reclaimFileCache";
    public static final String MEM_CONSTANT_RESERVEDZRAMNAME = "reservedZram";
    public static final String MEM_CONSTANT_SWAPPINESSNAME = "swappiness";
    public static final String MEM_CONSTANT_SYSTEMTRIMWITCH = "systemtrim_switch";
    public static final String MEM_CONSTANT_TOPN_PROCESS_MEM = "topNMemProc";
    public static final String MEM_EGL_INITOPT_SWITCH_CONFIGNAME = "egl_initopt_switch";
    public static final String MEM_ENHANCED_KILL_SIZE = "enhancedKillSize";
    public static final String MEM_ENHANCED_RCC_SIZE = "enhancedRccSize";
    public static final String MEM_FILECACHE_ITEM_LEVEL = "level";
    public static final String MEM_FILECACHE_ITEM_NAME = "name";
    public static final String MEM_FREQKILL_CONFIGNAME = "freqKillControl";
    public static final String MEM_FREQKILL_LIMIT = "killLimit";
    public static final String MEM_FREQKILL_PKG = "pkgFreqSwitch";
    public static final String MEM_FREQKILL_SCALETIMES = "scaleTimes";
    private static final int MEM_MAX_SWAPPINESS_INDEX = 2;
    private static final int MEM_MIN_SWAPPINESS_INDEX = 0;
    public static final String MEM_NATIVE_ITEM_PROCESSNAME = "processName";
    public static final String MEM_NOTIFICATION_CONFIGNAME = "Notification";
    public static final String MEM_NOTIFICATION_INTERVAL = "interval";
    public static final String MEM_NOTIFICATION_SWITCH = "notificationSwitch";
    public static final String MEM_PIN_FILE = "PinFile";
    public static final String MEM_PIN_FILE_CONSTANT = "file";
    public static final String MEM_PIN_FILE_CONSTANT_SIZE = "size";
    public static final String MEM_PIN_FILE_ITEM_NAME = "name";
    public static final String MEM_PIN_FILE_NAME = "files";
    public static final String MEM_POLICY_ACTIONNAME = "name";
    public static final String MEM_POLICY_BIGAPPNAME = "appname";
    public static final String MEM_POLICY_BIGMEMAPP = "BigMemApp";
    public static final String MEM_POLICY_CONFIGNAME = "Memoryitem";
    public static final String MEM_POLICY_FEATURENAME = "Memory";
    public static final String MEM_POLICY_FILECACHE = "FileCache";
    public static final String MEM_POLICY_GMCACTION = "gmc";
    public static final String MEM_POLICY_IONPROPERTYS = "ionPropertys";
    public static final String MEM_POLICY_KILLACTION = "kill";
    public static final String MEM_POLICY_NATIVE_MNG = "NativeMemMng";
    public static final String MEM_POLICY_QUICKKILLACTION = "quickkill";
    public static final String MEM_POLICY_RECLAIM = "reclaim";
    public static final String MEM_POLICY_REPAIR = "memRepair";
    public static final String MEM_POLICY_SCENE = "scene";
    public static final String MEM_POLICY_SYSTEMTRIMACTION = "systemtrim";
    public static final String MEM_POLICY_SYSTRIM = "SystemMemMng";
    public static final String MEM_PREREAD_CONFIGNAME = "PrereadFile";
    public static final String MEM_PREREAD_FILE = "file";
    public static final String MEM_PREREAD_ITEM_NAME = "pkgname";
    public static final String MEM_PREREAD_SWITCH = "prereadSwitch";
    public static final String MEM_PRESSURE_RECLAIM_SWITCH = "pressureReclaimSwitch";
    public static final int MEM_QOS_ORDER_ITEM_COUNT = 5;
    public static final int MEM_QOS_ORDER_MAX_MEMOEY = 10;
    public static final String MEM_QOS_SWITCH_CONFIGNAME = "qos_mem_switch";
    public static final String MEM_QOS_WATERMARK_CONFIGNAME = "qos_mem_watermark";
    public static final String MEM_REPAIR_CONSTANT_BASE = "base";
    public static final String MEM_REPAIR_CONSTANT_BG = "background";
    public static final String MEM_REPAIR_CONSTANT_BG_INTERVAL = "bg_interval";
    public static final String MEM_REPAIR_CONSTANT_BG_MIN_COUNT = "bg_min_count";
    public static final String MEM_REPAIR_CONSTANT_DV_NEGA_PERCENT = "dvalue_negative_percent";
    public static final String MEM_REPAIR_CONSTANT_FG = "foreground";
    public static final String MEM_REPAIR_CONSTANT_FG_INTERVAL = "fg_interval";
    public static final String MEM_REPAIR_CONSTANT_FG_MIN_COUNT = "fg_min_count";
    public static final String MEM_REPAIR_CONSTANT_ION_BASE = "ion_proc_emergency_base";
    public static final String MEM_REPAIR_CONSTANT_ION_PROC_EMERG_THRES = "ion_proc_emergency_threshold";
    public static final String MEM_REPAIR_CONSTANT_ION_SWITCH = "switch";
    public static final String MEM_REPAIR_CONSTANT_MIN_MAX_THRES = "min_max_threshold";
    public static final String MEM_REPAIR_CONSTANT_PARAS = "parameter";
    public static final String MEM_REPAIR_CONSTANT_PROC_EMERG_THRES = "proc_emergency_threshold";
    public static final String MEM_REPAIR_CONSTANT_SIZE = "size";
    public static final String MEM_REPAIR_CONSTANT_THRES = "threshold";
    public static final String MEM_REPAIR_CONSTANT_VSS_EMERG_THRES = "vss_emergency_threshold";
    public static final String MEM_REPAIR_CONSTANT_VSS_PARAMETER = "vss_parameter";
    public static final String MEM_REPAIR_ION = "memRepairIon";
    public static final String MEM_REPAIR_ITEM_NAME = "name";
    public static final String MEM_REPAIR_LOWMEM_BACKGROUND = "lowMemBackground";
    public static final String MEM_REPAIR_LOWMEM_FOREGROUND = "lowMemForeground";
    public static final String MEM_REPAIR_SUMPSS_CONFIGNAME = "memRepairBySumPss";
    public static final String MEM_REPAIR_SUMPSS_CONFIG_SWITCH = "sumPssSwitch";
    public static final String MEM_SCAN_MODE_OPT_CONFIGNAME = "ScanModeOpt";
    public static final String MEM_SCAN_MODE_OPT_SWITCH_CONFIGNAME = "scanModeOptSwitch";
    public static final String MEM_SCENE_BIGMEM = "BigMem";
    public static final String MEM_SCENE_DEFAULT = "default";
    public static final String MEM_SCENE_IDLE = "idle";
    public static final String MEM_SCENE_LAUNCH = "launch";
    public static final int MEM_SIZE_UNIT = 1024;
    public static final String MEM_SWAPPINESS_RANGE = "swappinessRange";
    private static final int MEM_SWAPPINESS_RANGE_LENGTH = 3;
    public static final int MEM_SWAPPINESS_STEP_SIZE = 10;
    public static final String MEM_SWAP_HIGH_THRESHOLD = "swapHighThreshold";
    public static final String MEM_SWAP_LOW_THRESHOLD = "swapLowThreshold";
    public static final String MEM_SWAP_MEDIUM_THRESHOLD = "swapMediumThreshold";
    public static final String MEM_SYSAPPREPAIR_TRIGGER_INTERVAL = "triggerInterval";
    public static final String MEM_SYSTRIM_ITEM_PKG = "packageName";
    public static final String MEM_SYSTRIM_ITEM_THRES = "threshold";
    public static final String MEM_SYSTRIM_POLICY = "policy";
    public static final String MEM_SYSTRIM_SCENE = "scene";
    public static final String MEM_SYSTRIM_SWITCH = "switch";
    public static final int MIN_CAMERAPRELOADRECLAIM_KILLUSS = 1024;
    public static final int MIN_CAMERAPRELOADRECLAIM_RECLAIMDELAY = 0;
    public static final long MIN_INTERVAL_OP_TIMEOUT = 10000;
    public static final int MIN_PERCENT = 0;
    private static final long MIN_PERIOD = 500;
    public static final int MSG_ACTIVITY_DISPLAY_STATISTICS = 330;
    public static final int MSG_BOOST_SIGKILL_SWITCH = 301;
    public static final int MSG_COMPRESS_GPU = 313;
    public static final int MSG_CONFIG_QOS_MEMORY_SWITCH = 371;
    public static final int MSG_CONFIG_QOS_MEMORY_WATERMARK = 370;
    public static final int MSG_DIRECT_SWAPPINESS = 303;
    public static final int MSG_MEM_BASE_VALUE = 300;
    public static final int MSG_MMONITOR_SWITCH = 332;
    public static final int MSG_PREREAD_DATA_REMOVE = 312;
    public static final int MSG_PREREAD_FILE = 314;
    public static final int MSG_PROCRECLAIM_ALL = 320;
    public static final int MSG_PROCRECLAIM_ALL_SUSPEND = 321;
    public static final int MSG_PROTECTLRU_CONFIG_UPDATE = 308;
    public static final int MSG_PROTECTLRU_SET_FILENODE = 304;
    public static final int MSG_PROTECTLRU_SET_PROTECTRATIO = 307;
    public static final int MSG_PROTECTLRU_SET_PROTECTZONE = 305;
    public static final int MSG_PROTECTLRU_SWITCH = 306;
    public static final int MSG_RCC_ANON_TARGET = 377;
    public static final int MSG_RCC_AVAIL_TARGET = 375;
    public static final int MSG_RCC_COMPRESS = 372;
    public static final int MSG_RCC_IDLE_COMPRESS_SWITCH = 373;
    public static final int MSG_RCC_IDLE_THRESHOLD = 374;
    public static final int MSG_RCC_PAUSE = 376;
    public static final int MSG_RCC_ZRAM_PERCENT_LOW = 378;
    public static final int MSG_SET_PREREAD_PATH = 311;
    public static final int MSG_SPECIAL_SCENE_POOL_BASE = 340;
    public static final int MSG_SPECIAL_SCENE_POOL_ENTER = 340;
    public static final int MSG_SPECIAL_SCENE_POOL_EXIT = 341;
    public static final int MSG_SPECIAL_SCENE_POOL_MAX = 349;
    public static final int MSG_SWAPPINESS = 302;
    public static final int MSG_TOPN_PROCESS_MEM = 385;
    public static final int MSG_UNMAP_FILE = 319;
    public static final int MSG_WORKINGSET_TOUCHEDFILES_ABORT = 353;
    public static final int MSG_WORKINGSET_TOUCHEDFILES_BACKUP = 356;
    public static final int MSG_WORKINGSET_TOUCHEDFILES_BASE = 350;
    public static final int MSG_WORKINGSET_TOUCHEDFILES_CLEAN = 355;
    public static final int MSG_WORKINGSET_TOUCHEDFILES_MAX = 359;
    public static final int MSG_WORKINGSET_TOUCHEDFILES_PAUSE = 351;
    public static final int MSG_WORKINGSET_TOUCHEDFILES_PAUSE_COLLECT = 357;
    public static final int MSG_WORKINGSET_TOUCHEDFILES_PREREAD = 354;
    public static final int MSG_WORKINGSET_TOUCHEDFILES_START = 350;
    public static final int MSG_WORKINGSET_TOUCHEDFILES_STOP = 352;
    private static final ArrayList<String> PINNED_FILES = new ArrayList<>(10);
    public static final int PROCESS_LIST_EXTRA_FREE_KBYTES = 24300;
    public static final int PROTECTLRU_ERROR_LEVEL = -1;
    public static final int PROTECTLRU_FIRST_LEVEL = 1;
    public static final int PROTECTLRU_MAX_LEVEL = 3;
    public static final int PROTECTLRU_STATE_PROTECT = 1;
    public static final int PROTECTLRU_STATE_UNPROTECT = 0;
    public static final String PSI_CPU_THRESHOLD = "psiCputhreshold";
    public static final String PSI_IO_THRESHOLD = "psiIothreshold";
    public static final String PSI_MEM_THRESHOLD = "psiMemthreshold";
    public static final int PSI_THRESHOLD_CONFIG_LENGTH = 6;
    private static final long RCC_HIGH_AVAILABLE_TARGET_PERCENT = 60;
    public static final String RECENT_APP_COUNT = "recentAppCount";
    public static final String RECENT_APP_TIME = "recentAppTime";
    public static final long RECLAIM_KILL_GAP_MEMORY = 102400;
    public static final int REPEAT_RECLAIM_TIME_GAP = 600000;
    private static final long RESERVED_ZRAM_MEMORY = 100;
    public static final int RESULT_ACTIVE = 1;
    public static final int RESULT_CONTINUE = 3;
    public static final int RESULT_FAIL = -1;
    public static final int RESULT_INACTIVE = 2;
    public static final int RESULT_OK = 0;
    public static final int SWITCH_OFF = 0;
    public static final int SWITCH_ON = 1;
    public static final String SYSTEM_UI_PACKAGE_NAME = "com.android.systemui";
    private static final String TAG = "AwareMem_MemoryConstant";
    public static final int THD_HIGH_PRIORITY = 30720;
    public static final int THD_LOW_PRIORITY = 34560;
    private static long bigMemoryAppLimit = 614400;
    private static int cameraPowerupWatermark = CAMERA_POWERUP_WATERMARK_NOTINIT;
    private static int cameraPowerupWatermarkXml = -1;
    private static int configAppTrimSwitch = 0;
    private static int configBalanceSwappiness = 100;
    private static int configBigMemEnableDmeSwitch = 0;
    private static int configCleanAllSwitch = 0;
    private static long configCpuIdleThreshold = DEFAULT_CPU_IDLE_THRESHOLD;
    private static long configCpuNormalThreshold = 60;
    private static long configCriticalMemory = 614400;
    private static int configDirectSwappiness = -1;
    private static long configEmergencyMemory = 307200;
    private static long configEnhancedKillSize = 51200;
    private static long configEnhancedRccSize = 51200;
    private static int configExactKillSwitch = 1;
    private static int configExtraDependOnPsiSwitch = 0;
    private static int configExtraFreeKbytes = -1;
    private static int configFastKillSwitch = 1;
    private static int configFastQuickKillSwitch = 1;
    private static int configGmcSwitch = 0;
    private static long configIdleMemory = MB_SIZE;
    private static int configIonSpeedupSwitch = 0;
    private static int configKernCompressAnonTarget = -1;
    private static int configKernCompressIdleThresHold = 50;
    private static int configKernCompressSwapPercent = -1;
    private static int configKernCompressSwitch = 0;
    private static long configKillGapMem = 0;
    private static int configMaxSwappiness = CpuFeature.MSG_AUX_COMM_CHANGE;
    private static int configMinSwappiness = 80;
    private static int configNotificationSwitch = 0;
    private static int configPressureReclaimSwitch = 0;
    private static int configPrintEnhanceSwitch = 0;
    private static String configProtectLruLimit = CONFIG_PROTECT_LRU_DEFAULT;
    private static int configProtectLruRatio = 50;
    private static String configPsiCpuThreshold = "0 0 0 0 0 750000";
    private static String configPsiIoThreshold = "0 0 0 50000 1 30000";
    private static String configPsiMemThreshold = "0 0 0 70000 1 50000";
    private static int configSwapHighThreshold = 20;
    private static int configSwapLowThreshold = 100;
    private static int configSwapMediumThreshold = 50;
    private static int configSwappiness = -1;
    private static int configSystemTrimSwitch = 0;
    private static int configTopnMemProcNum = 30;
    private static long defaultMemoryLimit = 614400;
    private static long eglInitOptConfigSwitch = 0;
    private static long gpuMemoryLimit = MB_SIZE;
    private static int iawareFifthSwitch = 0;
    private static int iawareThirdSwitch = 0;
    private static boolean isConfigMemRepairPssSwitch = false;
    private static boolean isConfigProtectLruSwitch = false;
    private static boolean isConfigReclaimFileCache = false;
    private static boolean isConfigScanModeOptSwitch = false;
    private static boolean isQosMemoryConfigState = false;
    private static long maxApiReqMem = 1228800;
    private static long maxReqMem = 2097152;
    private static long notificationInterval = 1000;
    private static long qosMemorySwitch = 0;
    private static String qosMemoryWatermark = "0 0 0 0 0";
    private static long ramSizeMB = -1;
    private static long rccHighAvailTarget = 0;
    private static long reservedZramMemory = RECLAIM_KILL_GAP_MEMORY;
    private static int sCameraPreloadKillUss = 0;
    private static int sCameraPreloadReclaimDelay = 0;
    private static int sCameraPreloadSwitch = 0;
    private static Context sContext;
    private static String sDisplayStartedActivityName = "";
    private static ArrayMap<Integer, ArraySet<String>> sFileCacheMap = new ArrayMap<>();
    private static Map<String, ArrayList<String>> sPrereadFileMap = new ArrayMap();
    private static int sysCameraUid = 0;
    private static long totalApiRequestMem = 0;

    public enum MemActionType {
        ACTION_KILL,
        ACTION_COMPRESS,
        ACTION_RECLAIM
    }

    public enum MemLevel {
        MEM_LOW,
        MEM_CRITICAL
    }

    private MemoryConstant() {
    }

    public static void init(Context context) {
        sContext = context;
    }

    public static Context getContext() {
        return sContext;
    }

    public static long getIdleThresHold() {
        return configCpuIdleThreshold;
    }

    public static void setIdleThresHold(long idleLoad) {
        if (idleLoad < 0 || idleLoad > RESERVED_ZRAM_MEMORY) {
            AwareLog.w(TAG, "config error! IdleThresHold=" + idleLoad);
            return;
        }
        configCpuIdleThreshold = idleLoad;
    }

    public static long getNormalThresHold() {
        return configCpuNormalThreshold;
    }

    public static void setNormalThresHold(long normalLoad) {
        if (normalLoad < 0 || normalLoad > RESERVED_ZRAM_MEMORY) {
            AwareLog.w(TAG, "config error! NormalThresHold=" + normalLoad);
            return;
        }
        configCpuNormalThreshold = normalLoad;
    }

    public static long getReservedZramSpace() {
        return reservedZramMemory;
    }

    public static long getReclaimKillGapMemory() {
        return RECLAIM_KILL_GAP_MEMORY;
    }

    public static void setReservedZramSpace(long reserved) {
        if (reserved < 0) {
            AwareLog.w(TAG, "config error! ReservedZramSpace=" + reserved);
            return;
        }
        reservedZramMemory = reserved;
    }

    public static long getIdleMemory() {
        return configIdleMemory;
    }

    public static void setIdleMemory(long idleMemory) {
        if (idleMemory <= 0) {
            AwareLog.w(TAG, "config error! IdleMemory=" + idleMemory);
            return;
        }
        configIdleMemory = idleMemory;
    }

    public static long getEmergencyMemory() {
        return configEmergencyMemory;
    }

    public static void setEmergencyMemory(long emergemcyMemory) {
        if (emergemcyMemory <= 0) {
            AwareLog.w(TAG, "config error! EmergencyMemory=" + emergemcyMemory);
            return;
        }
        configEmergencyMemory = emergemcyMemory;
    }

    public static long getCriticalMemory() {
        return configCriticalMemory;
    }

    public static void setDefaultCriticalMemory(long criticalMemory) {
        if (criticalMemory <= 0) {
            AwareLog.w(TAG, "config error! DefaultCriticalMemory=" + criticalMemory);
            return;
        }
        configCriticalMemory = criticalMemory;
        defaultMemoryLimit = criticalMemory;
    }

    public static long getMaxRequestMemory() {
        return maxApiReqMem;
    }

    public static void setMaxRequestMemory(long maxRequestMemory) {
        if (maxRequestMemory >= 0) {
            long maxRequestMemoryKiloByte = KILO * maxRequestMemory;
            long j = maxApiReqMem;
            if (j < maxRequestMemoryKiloByte) {
                maxReqMem += maxRequestMemoryKiloByte - j;
            }
            maxApiReqMem = maxRequestMemoryKiloByte;
        }
    }

    public static long getTotalRequestMemory() {
        return totalApiRequestMem;
    }

    public static void resetTotalRequestMemory() {
        totalApiRequestMem = 0;
    }

    public static void addTotalRequestMemory(long addMemory) {
        if (addMemory > 0) {
            totalApiRequestMem += addMemory;
        }
    }

    public static void enableBigMemCriticalMemory() {
        configCriticalMemory = bigMemoryAppLimit;
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "setLowCriticalMemory, current limit is " + (configCriticalMemory / KILO) + "MB");
        }
    }

    public static void disableBigMemCriticalMemory() {
        configCriticalMemory = defaultMemoryLimit;
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "resetLowCriticalMemory, current limit is " + (configCriticalMemory / KILO) + "MB");
        }
    }

    public static void setBigMemoryAppCriticalMemory(long bigMemLimit) {
        if (bigMemLimit <= 0) {
            AwareLog.w(TAG, "config error! BigMemoryAppCriticalMemory=" + bigMemLimit);
            return;
        }
        bigMemoryAppLimit = bigMemLimit;
    }

    public static boolean isBigMemCriticalMemory() {
        return bigMemoryAppLimit == configCriticalMemory;
    }

    public static long getBigMemoryAppCriticalMemory() {
        return bigMemoryAppLimit;
    }

    public static long getMaxTimerPeriod() {
        return 600000;
    }

    public static long getMinTimerPeriod() {
        return 500;
    }

    public static long getDefaultTimerPeriod() {
        return DEFAULT_PERIOD;
    }

    public static long getEnableTimerPeriod() {
        return MIN_INTERVAL_OP_TIMEOUT;
    }

    public static int getNumTimerPeriod() {
        return 3;
    }

    public static long getMiddleWater() {
        return (configEmergencyMemory + configCriticalMemory) / MAX_REQUEST_MEMORY_RELEASE;
    }

    public static long getMaxReqMem() {
        return maxReqMem;
    }

    public static void setFileCacheMap(ArrayMap<Integer, ArraySet<String>> fileCacheMap) {
        if (fileCacheMap == null) {
            AwareLog.w(TAG, "config error! FileCacheMap null");
        } else {
            sFileCacheMap = fileCacheMap;
        }
    }

    public static ArrayMap<Integer, ArraySet<String>> getFileCacheMap() {
        return sFileCacheMap;
    }

    public static void setPrereadFileMap(Map<String, ArrayList<String>> prereadFileMap) {
        if (prereadFileMap == null) {
            AwareLog.w(TAG, "config error! prereadFileMap null");
        } else {
            sPrereadFileMap = prereadFileMap;
        }
    }

    public static Map<String, ArrayList<String>> getCameraPrereadFileMap() {
        return sPrereadFileMap;
    }

    public static void setCameraPreloadSwitch(int cameraPreloadSwitch) {
        sCameraPreloadSwitch = cameraPreloadSwitch;
    }

    public static int getCameraPreloadSwitch() {
        return sCameraPreloadSwitch;
    }

    public static void setCameraPreloadReclaimDelay(int cameraPreloadReclaimDelay) {
        sCameraPreloadReclaimDelay = cameraPreloadReclaimDelay;
    }

    public static int getCameraPreloadReclaimDelay() {
        return sCameraPreloadReclaimDelay;
    }

    public static void setCameraPreloadKillUss(int cameraPreloadKillUss) {
        sCameraPreloadKillUss = cameraPreloadKillUss;
    }

    public static int getCameraPreloadKillUss() {
        return sCameraPreloadKillUss;
    }

    public static void addPinnedFilesStr(String file) {
        PINNED_FILES.add(file);
    }

    public static void clearPinnedFilesStr() {
        PINNED_FILES.clear();
    }

    public static ArrayList<String> getFilesToPin() {
        return PINNED_FILES;
    }

    public static void setDisplayStartedActivityName(String name) {
        sDisplayStartedActivityName = name;
    }

    public static String getDisplayStartedActivityName() {
        return sDisplayStartedActivityName;
    }

    public static int getConfigExtraFreeKbytes() {
        return configExtraFreeKbytes;
    }

    public static void setConfigExtraFreeKbytes(int extraFreeKbytes) {
        if (extraFreeKbytes < 0) {
            AwareLog.w(TAG, "config error! ExtraFreeKbytes=" + extraFreeKbytes);
            return;
        }
        configExtraFreeKbytes = extraFreeKbytes;
    }

    public static int getConfigSwappiness() {
        return configSwappiness;
    }

    public static void setConfigSwappiness(int swappiness) {
        if (swappiness < 0 || swappiness > 200) {
            AwareLog.w(TAG, "config error! Swappiness=" + swappiness);
            return;
        }
        configSwappiness = swappiness;
    }

    public static int getConfigDirectSwappiness() {
        return configDirectSwappiness;
    }

    public static void setConfigDirectSwappiness(int directSwappiness) {
        if (directSwappiness < 0 || directSwappiness > 200) {
            AwareLog.w(TAG, "config error! DirectSwappiness=" + directSwappiness);
            return;
        }
        configDirectSwappiness = directSwappiness;
    }

    public static void setConfigReclaimFileCache(int reclaim) {
        if (reclaim != 0 && reclaim != 1) {
            AwareLog.w(TAG, "config error! Invalid parameter =" + reclaim);
        } else if (reclaim == 1) {
            isConfigReclaimFileCache = true;
        } else {
            isConfigReclaimFileCache = false;
        }
    }

    public static boolean getConfigReclaimFileCache() {
        return isConfigReclaimFileCache;
    }

    public static void setConfigQosMemoryWatermark(long[] val) {
        if (val == null || val.length != 5) {
            AwareLog.w(TAG, "setConfigQosMemory error");
            return;
        }
        String stringVal = "";
        int index = 0;
        for (long eachval : val) {
            if (eachval < 0 || eachval > 10) {
                AwareLog.e(TAG, "saveQosMemoryConfig invalid val " + eachval);
                return;
            }
            stringVal = stringVal + eachval;
            index++;
            if (index != 5) {
                stringVal = stringVal + " ";
            }
        }
        qosMemoryWatermark = stringVal;
    }

    public static String getConfigQosMemoryWatermark() {
        return qosMemoryWatermark;
    }

    public static void setQosMemoryConfigState(boolean isQosMemState) {
        isQosMemoryConfigState = isQosMemState;
    }

    public static boolean getQosMemoryConfigState() {
        return isQosMemoryConfigState;
    }

    public static void setConfigQosMemorySwitch(long val) {
        if (val == 0 || val == 1) {
            qosMemorySwitch = val;
            return;
        }
        AwareLog.w(TAG, "setQosMemorySwitch error val " + val);
    }

    public static long getConfigQosMemorySwitch() {
        return qosMemorySwitch;
    }

    public static void setEglInitOptConfigSwitch(long val) {
        if (val == 0 || val == 1) {
            eglInitOptConfigSwitch = val;
            return;
        }
        AwareLog.w(TAG, "setEglInitOptSwitch error val " + val);
    }

    public static long getEglInitOptConfigSwitch() {
        return eglInitOptConfigSwitch;
    }

    public static void setBigMemEnableDmeSwitch(int val) {
        if (val == 0 || val == 1) {
            configBigMemEnableDmeSwitch = val;
            return;
        }
        AwareLog.w(TAG, "setBigMemEnableDmeSwitch error val " + val);
    }

    public static boolean isBigMemEnableDmeSwitchOn() {
        return configBigMemEnableDmeSwitch == 1;
    }

    public static void setPressureReclaimSwitch(int val) {
        if (val == 0 || val == 1) {
            configPressureReclaimSwitch = val;
            return;
        }
        AwareLog.w(TAG, "setPressureReclaimSwitch error val " + val);
    }

    public static int getPressureReclaimSwitch() {
        return configPressureReclaimSwitch;
    }

    public static void setExtraDependOnPsiSwitch(int val) {
        if (val == 0 || val == 1) {
            configExtraDependOnPsiSwitch = val;
            return;
        }
        AwareLog.w(TAG, "setExtraDependPsiSwitch error val " + val);
    }

    public static int getExtraDependOnPsiSwitch() {
        return configExtraDependOnPsiSwitch;
    }

    public static void setSwapHighThreshold(int threshold) {
        if (threshold < 0) {
            AwareLog.w(TAG, "config error! threshold=" + threshold);
            return;
        }
        configSwapHighThreshold = threshold;
    }

    public static long getSwapHighThreshold() {
        return (long) configSwapHighThreshold;
    }

    public static void setSwapMediumThreshold(int threshold) {
        if (threshold < 0) {
            AwareLog.w(TAG, "config error! threshold=" + threshold);
            return;
        }
        configSwapMediumThreshold = threshold;
    }

    public static long getSwapMediumThreshold() {
        return (long) configSwapMediumThreshold;
    }

    public static void setSwapLowThreshold(int threshold) {
        if (threshold < 0) {
            AwareLog.w(TAG, "config error! threshold=" + threshold);
            return;
        }
        configSwapLowThreshold = threshold;
    }

    public static long getSwapLowThreshold() {
        return (long) configSwapLowThreshold;
    }

    public static long getRamSizeMB() {
        return ramSizeMB;
    }

    public static void setRamSizeMB(long size) {
        if (size >= 0 && ramSizeMB == -1) {
            ramSizeMB = size;
        }
    }

    public static void setRccHighAvailTarget() {
        long j = ramSizeMB;
        if (j != -1) {
            rccHighAvailTarget = (j * 60) / RESERVED_ZRAM_MEMORY;
        }
    }

    public static long getRccHighAvailTarget() {
        return rccHighAvailTarget;
    }

    public static void setConfigPsiThreshold(int res, String threshold) {
        if (threshold == null) {
            AwareLog.w(TAG, "config error! threshold null");
        } else if (res == 0) {
            configPsiIoThreshold = threshold;
        } else if (res == 1) {
            configPsiMemThreshold = threshold;
        } else {
            configPsiCpuThreshold = threshold;
        }
    }

    public static String getConfigPsiThreshold(int res) {
        if (res == 0) {
            return configPsiIoThreshold;
        }
        if (res == 1) {
            return configPsiMemThreshold;
        }
        return configPsiCpuThreshold;
    }

    public static void setConfigSwappinessRange(String swappinessRangeStr) {
        if (swappinessRangeStr == null) {
            AwareLog.w(TAG, "config error! swappinessRangeStr null");
            return;
        }
        String[] swappinessRangeArray = swappinessRangeStr.split(" ");
        if (swappinessRangeArray.length == 3) {
            int[] swappinessRangeInt = new int[swappinessRangeArray.length];
            for (int i = 0; i < swappinessRangeArray.length; i++) {
                swappinessRangeInt[i] = Integer.parseInt(swappinessRangeArray[i]);
                if (swappinessRangeInt[i] < 0 || swappinessRangeInt[i] > 200) {
                    AwareLog.w(TAG, "config error! swappinessRangeInt = " + swappinessRangeInt[i]);
                    return;
                }
            }
            configMinSwappiness = swappinessRangeInt[0];
            configBalanceSwappiness = swappinessRangeInt[1];
            configMaxSwappiness = swappinessRangeInt[2];
        }
    }

    public static int getConfigMinSwappiness() {
        return configMinSwappiness;
    }

    public static int getConfigBalanceSwappiness() {
        return configBalanceSwappiness;
    }

    public static int getConfigMaxSwappiness() {
        return configMaxSwappiness;
    }

    public static void setConfigEnhancedKillSize(long memoryKb) {
        if (memoryKb <= 0) {
            AwareLog.w(TAG, "setConfigEnhancedKillSize memoryKb error! " + memoryKb);
            return;
        }
        configEnhancedKillSize = memoryKb;
    }

    public static long getConfigEnhancedKillSize() {
        return configEnhancedKillSize;
    }

    public static void setConfigEnhancedRccSize(long memoryKb) {
        if (memoryKb <= 0) {
            AwareLog.w(TAG, "setConfigEnhancedRccSize memoryKb error! " + memoryKb);
            return;
        }
        configEnhancedRccSize = memoryKb;
    }

    public static long getConfigEnhancedRccSize() {
        return configEnhancedRccSize;
    }

    public static String getConfigProtectLruLimit() {
        return configProtectLruLimit;
    }

    public static void setConfigProtectLruLimit(String protectLruLimit) {
        if (protectLruLimit == null) {
            AwareLog.w(TAG, "config error! ProtectLruLimit null");
        } else {
            configProtectLruLimit = protectLruLimit;
        }
    }

    public static String getConfigProtectLruDefault() {
        return CONFIG_PROTECT_LRU_DEFAULT;
    }

    public static void setConfigProtectLruRatio(int ratio) {
        if (ratio < 0 || ratio > 100) {
            AwareLog.w(TAG, "config error! ProtectLruRatio=" + ratio);
            return;
        }
        configProtectLruRatio = ratio;
    }

    public static int getConfigProtectLruRatio() {
        return configProtectLruRatio;
    }

    public static void setConfigProtectLruSwitch(int switchValue) {
        boolean z = true;
        if (switchValue == 0 || switchValue == 1) {
            if (switchValue != 1) {
                z = false;
            }
            isConfigProtectLruSwitch = z;
            return;
        }
        AwareLog.w(TAG, "config error! isConfigProtectLruSwitch Invalid parameter =" + switchValue);
    }

    public static boolean getConfigProtectLruSwitch() {
        return isConfigProtectLruSwitch;
    }

    public static void setConfigIonSpeedupSwitch(int switchValue) {
        if (switchValue < 0 || switchValue > 1) {
            AwareLog.w(TAG, "config error! ionSpeedupSwitch=" + switchValue);
            return;
        }
        configIonSpeedupSwitch = switchValue;
    }

    public static boolean isConfigMemRepairBySumPssSwitch() {
        return isConfigMemRepairPssSwitch;
    }

    public static void setConfigMemRepairBySumPssSwitch(int switchValue) {
        boolean z = true;
        if (switchValue != 1) {
            z = false;
        }
        isConfigMemRepairPssSwitch = z;
        AwareLog.d(TAG, "config info! setConfigMemRepairBySumPssSwitch=" + isConfigMemRepairPssSwitch);
    }

    public static int getConfigIonSpeedupSwitch() {
        return configIonSpeedupSwitch;
    }

    public static void setCameraPowerUpMem() {
        int[] memoryRequirement;
        Context context = sContext;
        if (context != null) {
            Object obj = context.getSystemService("camera");
            if (!(obj instanceof CameraManager)) {
                AwareLog.w(TAG, "get camera service fail!");
                return;
            }
            try {
                CameraCharacteristics characteristics = ((CameraManager) obj).getCameraCharacteristics("0");
                if (characteristics != null && (memoryRequirement = (int[]) characteristics.get(HW_CAMERA_MEMORY_REQUIREMENT_SUPPORTED)) != null && memoryRequirement.length != 0) {
                    cameraPowerupWatermark = memoryRequirement[0] * 1024;
                }
            } catch (CameraAccessException e) {
                AwareLog.e(TAG, "getStartUpMemFromCamera: Characteristics CameraAccessException");
            } catch (IllegalArgumentException e2) {
                AwareLog.e(TAG, "getStartUpMemFromCamera: Characteristics IllegalArgumentException");
            }
        }
    }

    public static void setCameraPowerUpMemoryDefault(int memoryKb) {
        if (memoryKb <= 0) {
            AwareLog.w(TAG, "setCameraPowerUpMemory memoryKb error! " + memoryKb);
            return;
        }
        cameraPowerupWatermarkXml = memoryKb;
    }

    public static int getCameraPowerUpMemory() {
        if (cameraPowerupWatermark == CAMERA_POWERUP_WATERMARK_NOTINIT) {
            setCameraPowerUpMem();
        }
        if (cameraPowerupWatermark == CAMERA_POWERUP_WATERMARK_NOTINIT) {
            cameraPowerupWatermark = cameraPowerupWatermarkXml;
        }
        return cameraPowerupWatermark;
    }

    public static void setConfigAppTrimSwitch(int switchValue) {
        if (switchValue < 0 || switchValue > 1) {
            AwareLog.w(TAG, "config error! AppTrimSwitch=" + switchValue);
            return;
        }
        configAppTrimSwitch = switchValue;
    }

    public static boolean isAppTrimEnabled() {
        return configAppTrimSwitch == 1;
    }

    public static void setConfigGmcSwitch(int switchValue) {
        if (switchValue < 0 || switchValue > 1) {
            AwareLog.w(TAG, "config error! GmcSwitch=" + switchValue);
            return;
        }
        configGmcSwitch = switchValue;
    }

    public static int getConfigGmcSwitch() {
        return configGmcSwitch;
    }

    public static void setConfigKernCompressSwitch(int switchValue) {
        if (switchValue == 1 || switchValue == 0) {
            AwareLog.i(TAG, "set KernCompressSwitch=" + switchValue);
            configKernCompressSwitch = switchValue;
        }
    }

    public static boolean isKernCompressEnable() {
        return configKernCompressSwitch == 1 && iawareFifthSwitch == 1;
    }

    public static void setKernCompressIdleThreshold(int cpuPercent) {
        if (cpuPercent < 0 || cpuPercent > 100) {
            AwareLog.w(TAG, "config error! cpuPercent=" + cpuPercent);
            return;
        }
        configKernCompressIdleThresHold = cpuPercent;
    }

    public static int getKernCompressIdleThreshold() {
        return configKernCompressIdleThresHold;
    }

    public static void setKernCompressSwapPercent(int percent) {
        if (percent < 0 || percent > 100) {
            AwareLog.w(TAG, "config error! percent=" + percent);
            return;
        }
        configKernCompressSwapPercent = percent;
    }

    public static int getKernCompressSwapPercent() {
        return configKernCompressSwapPercent;
    }

    public static void setKernCompressAnonTarget(int target) {
        configKernCompressAnonTarget = target;
    }

    public static int getKernCompressAnonTarget() {
        int i = configKernCompressAnonTarget;
        return i == -1 ? MultiTaskManagerService.MSG_POLICY_BR : i;
    }

    public static void setGpuMemoryLimit(long limit) {
        gpuMemoryLimit = limit;
    }

    public static long getGpuMemoryLimit() {
        return gpuMemoryLimit;
    }

    public static void setConfigSystemTrimSwitch(int switchValue) {
        if (switchValue < 0 || switchValue > 1) {
            AwareLog.w(TAG, "config error! SystemTrimSwitch=" + switchValue);
            return;
        }
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "config system trim switch=" + switchValue);
        }
        configSystemTrimSwitch = switchValue;
    }

    public static int getConfigSystemTrimSwitch() {
        return configSystemTrimSwitch;
    }

    public static void setSysCameraUid(int uid) {
        sysCameraUid = uid;
    }

    public static int getSystemCameraUid() {
        return sysCameraUid;
    }

    public static void setCleanAllSwitch(int switchValue) {
        if (switchValue == 0 || switchValue == 1) {
            configCleanAllSwitch = switchValue;
            return;
        }
        AwareLog.w(TAG, "config error! CleanAllSwitch=" + switchValue);
    }

    public static boolean isCleanAllSwitch() {
        return configCleanAllSwitch == 1;
    }

    public static void setKillGapMemory(long gapMem) {
        if (gapMem < 0 || gapMem > configCriticalMemory) {
            AwareLog.w(TAG, "config error! killMemGap=" + gapMem);
            return;
        }
        configKillGapMem = gapMem;
    }

    public static long getKillGapMemory() {
        return configKillGapMem;
    }

    public static void setConfigNotificatinSwitch(int switchValue) {
        if (switchValue != 1) {
            AwareLog.w(TAG, "config error! NotificatinSwitch=" + switchValue);
            return;
        }
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "config notification trim switch=" + switchValue);
        }
        configNotificationSwitch = switchValue;
    }

    public static boolean isNotificatinSwitchEnable() {
        return configNotificationSwitch == 1;
    }

    public static void setNotificationInterval(long interval) {
        if (interval <= 0 || interval > MIN_INTERVAL_OP_TIMEOUT) {
            AwareLog.w(TAG, "config error! interval=" + interval);
            return;
        }
        notificationInterval = interval;
    }

    public static long getNotificationInterval() {
        return notificationInterval;
    }

    public static void setExactKillSwitch(int switchValue) {
        if (switchValue == 0 || switchValue == 1) {
            configExactKillSwitch = switchValue;
            return;
        }
        AwareLog.w(TAG, "config error! ExactKillSwitch=" + switchValue);
    }

    public static boolean isExactKillSwitch() {
        return configExactKillSwitch == 1 && iawareThirdSwitch == 1;
    }

    public static void setFastKillSwitch(int switchValue) {
        if (switchValue == 0 || switchValue == 1) {
            configFastKillSwitch = switchValue;
            return;
        }
        AwareLog.w(TAG, "config error! FastKillSwitch=" + switchValue);
    }

    public static boolean isFastKillSwitch() {
        return configFastKillSwitch == 1 && iawareThirdSwitch == 1;
    }

    public static void setIawareThirdSwitch(int switchValue) {
        if (switchValue == 0 || switchValue == 1) {
            iawareThirdSwitch = switchValue;
            return;
        }
        AwareLog.w(TAG, "set iaware3.0 switch error! configValue=" + switchValue);
    }

    public static boolean isFastQuickKillSwitch() {
        return configFastQuickKillSwitch == 1 && iawareThirdSwitch == 1;
    }

    public static void setFastQuickKillSwitch(int switchValue) {
        if (switchValue == 0 || switchValue == 1) {
            configFastQuickKillSwitch = switchValue;
            return;
        }
        AwareLog.w(TAG, "config error! FastQuickKillSwitch=" + switchValue);
    }

    public static void setIawareFifthSwitch(int switchValue) {
        if (switchValue == 0 || switchValue == 1) {
            iawareFifthSwitch = switchValue;
            return;
        }
        AwareLog.w(TAG, "set iaware5.0 switch error! configValue=" + switchValue);
    }

    public static void setPrintEnhanceSwitch(int switchValue) {
        if (switchValue == 0 || switchValue == 1) {
            configPrintEnhanceSwitch = switchValue;
            return;
        }
        AwareLog.w(TAG, "config error! Kill Print Switch=" + switchValue);
    }

    public static boolean isPrintEnhanceSwitch() {
        return configPrintEnhanceSwitch == 1 && (AwareConstant.CURRENT_USER_TYPE == 3 || AwareConstant.CURRENT_USER_TYPE == 5);
    }

    private static boolean isInvalidTopnValue(int topnValue) {
        return topnValue < 3 || topnValue > 50;
    }

    public static void setTopnMemProcNum(int topnValue) {
        if (isInvalidTopnValue(topnValue)) {
            AwareLog.w(TAG, "config error! top n process memory is invalid:= " + topnValue + ", use default value: " + configTopnMemProcNum);
            return;
        }
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "set topN process memory = " + topnValue);
        }
        configTopnMemProcNum = topnValue;
    }

    public static int getTopnMemProcNum() {
        return configTopnMemProcNum;
    }

    public static void setConfigScanModeOptSwitch(int switchValue) {
        boolean z = true;
        if (switchValue != 1) {
            z = false;
        }
        isConfigScanModeOptSwitch = z;
        if (AwareLog.getDebugLogSwitch()) {
            AwareLog.d(TAG, "config info! setConfigScanModeOptSwitch = " + isConfigScanModeOptSwitch);
        }
    }

    public static boolean isConfigScanModeOptSwitch() {
        return isConfigScanModeOptSwitch;
    }
}
