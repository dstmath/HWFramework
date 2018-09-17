package com.android.server.rms.iaware.memory.utils;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraCharacteristics.Key;
import android.hardware.camera2.CameraManager;
import android.os.SystemProperties;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import java.util.ArrayList;
import java.util.Map;

public final class MemoryConstant {
    public static final long APP_AVG_USS = 20480;
    public static final int AWARE_INVAILD_KILL_NUM = 3;
    public static final int AWARE_INVAILD_KILL_THRESHOLD = 5;
    public static final int CAMERA_AUTOSTOP_TIMEOUT = 1;
    public static final String CAMERA_PACKAGE_NAME = "com.huawei.camera";
    private static final int CAMERA_POWERUP_WATERMARK_NOTINIT = -2;
    public static final int CAMERA_POWERUP_WORKER_MASK = 16746241;
    public static final int CAMERA_STREAMING_WORKER_MASK = 1013762;
    private static final String CONFIG_PROTECT_LRU_DEFAULT = "0 0 0";
    private static long CPU_IDLE_THRESHOLD = 30;
    private static long CPU_NORMAL_THRESHOLD = 60;
    private static long CRITICAL_MEMORY = 614400;
    public static final int DEFAULT_DIRECT_SWAPPINESS = 60;
    public static final int DEFAULT_EXTRA_FREE_KBYTES = SystemProperties.getInt("sys.sysctl.extra_free_kbytes", PROCESSLIST_EXTRA_FREE_KBYTES);
    public static final int DEFAULT_INVALID_UID = 0;
    public static final int DEFAULT_SWAPPINESS = 60;
    public static final int DIRCONSTANT = 50;
    public static final int DO_APPLY_MASK = 1;
    public static final int DO_SHRINK_MASK = 2;
    private static long EMERGENCY_MEMORY = 307200;
    private static final Key<int[]> HW_CAMERA_MEMORY_REQUIREMENT_SUPPORTED = new Key("com.huawei.device.capabilities.hwCameraMemoryRequirementSupported", int[].class);
    private static long IDLE_MEMORY = MB_SIZE;
    public static final int LARGE_CPU_MASK = 16711680;
    public static final int LITTLE_CPU_MASK = 983040;
    public static final int MAX_APPNAME_LEN = 64;
    public static final int MAX_CAMERA_REQUEST_MEM = 1228800;
    public static final int MAX_EXTRA_FREE_KBYTES = 200000;
    private static final int MAX_SWAPPINESS = 200;
    public static final long MB_SIZE = 1048576;
    public static final String MEM_CONSTANT_AVERAGEAPPUSSNAME = "averageAppUss";
    public static final String MEM_CONSTANT_BIGMEMCRITICALMEMORYNAME = "bigMemCriticalMemory";
    public static final String MEM_CONSTANT_CAMERAPOWERUPNAME = "camera_powerup_ion_memory";
    public static final String MEM_CONSTANT_CONFIGNAME = "MemoryConstant";
    public static final String MEM_CONSTANT_DEFAULTCRITICALMEMORYNAME = "defaultCriticalMemory";
    public static final String MEM_CONSTANT_DEFAULTTIMERPERIOD = "defaultTimerPeriod";
    public static final String MEM_CONSTANT_DIRECTSWAPPINESSNAME = "direct_swappiness";
    public static final String MEM_CONSTANT_EMERGEMCYMEMORYNAME = "emergencyMemory";
    public static final String MEM_CONSTANT_EXTRAFREEKBYTESNAME = "extra_free_kbytes";
    public static final String MEM_CONSTANT_GMCSWITCH = "gmc_switch";
    public static final String MEM_CONSTANT_GPUNAME = "gpuMemory";
    public static final String MEM_CONSTANT_HIGHCPULOADNAME = "highCpuLoad";
    public static final String MEM_CONSTANT_IONSPEEDUPSWITCH = "camera_ion_speedup_switch";
    public static final String MEM_CONSTANT_LOWCPULOADNAME = "lowCpuLoad";
    public static final String MEM_CONSTANT_MAXTIMERPERIOD = "maxTimerPeriod";
    public static final String MEM_CONSTANT_MINTIMERPERIOD = "minTimerPeriod";
    public static final String MEM_CONSTANT_NORMALMEMORYNAME = "normalMemory";
    public static final String MEM_CONSTANT_NUMTIMERPERIOD = "numTimerPeriod";
    public static final String MEM_CONSTANT_PREREAD_ODEX = "prereadOdexSwitch";
    public static final String MEM_CONSTANT_PROCESSLIMIT = "numProcessLimit";
    public static final String MEM_CONSTANT_PROCESSPERCENT = "numEmptyProcessPercent";
    public static final String MEM_CONSTANT_PROTECTLRULIMIT = "protect_lru_limit";
    public static final String MEM_CONSTANT_PROTECTRATIO = "protect_lru_ratio";
    public static final String MEM_CONSTANT_RAMSIZENAME = "ramsize";
    public static final String MEM_CONSTANT_RESERVEDZRAMNAME = "reservedZram";
    public static final String MEM_CONSTANT_SWAPPINESSNAME = "swappiness";
    public static final String MEM_FILECACHE_ITEM_LEVEL = "level";
    public static final String MEM_FILECACHE_ITEM_NAME = "name";
    public static final String MEM_POLICY_ACTIONNAME = "name";
    public static final String MEM_POLICY_BIGAPPNAME = "appname";
    public static final String MEM_POLICY_BIGMEMAPP = "BigMemApp";
    public static final String MEM_POLICY_CONFIGNAME = "Memoryitem";
    public static final String MEM_POLICY_FEATURENAME = "Memory";
    public static final String MEM_POLICY_FILECACHE = "FileCache";
    public static final String MEM_POLICY_GMCACTION = "gmc";
    public static final String MEM_POLICY_IONPROPERTYS = "ionPropertys";
    public static final String MEM_POLICY_KILLACTION = "kill";
    public static final String MEM_POLICY_QUICKKILLACTION = "quickkill";
    public static final String MEM_POLICY_RECLAIM = "reclaim";
    public static final String MEM_POLICY_REPAIR = "memRepair";
    public static final String MEM_POLICY_SCENE = "scene";
    public static final String MEM_PREREAD_CONFIGNAME = "PrereadFile";
    public static final String MEM_PREREAD_FILE = "file";
    public static final String MEM_PREREAD_ITEM_NAME = "pkgname";
    public static final String MEM_PREREAD_SWITCH = "prereadSwitch";
    public static final String MEM_REPAIR_CONSTANT_BASE = "base";
    public static final String MEM_REPAIR_CONSTANT_BG = "background";
    public static final String MEM_REPAIR_CONSTANT_BG_INTERVAL = "bg_interval";
    public static final String MEM_REPAIR_CONSTANT_BG_MIN_COUNT = "bg_min_count";
    public static final String MEM_REPAIR_CONSTANT_DV_NEGA_PERCENT = "dvalue_negative_percent";
    public static final String MEM_REPAIR_CONSTANT_FG = "foreground";
    public static final String MEM_REPAIR_CONSTANT_FG_INTERVAL = "fg_interval";
    public static final String MEM_REPAIR_CONSTANT_FG_MIN_COUNT = "fg_min_count";
    public static final String MEM_REPAIR_CONSTANT_MIN_MAX_THRES = "min_max_threshold";
    public static final String MEM_REPAIR_CONSTANT_PROC_EMERG_THRES = "proc_emergency_threshold";
    public static final String MEM_REPAIR_CONSTANT_SIZE = "size";
    public static final String MEM_REPAIR_CONSTANT_THRES = "threshold";
    public static final String MEM_REPAIR_ITEM_NAME = "name";
    public static final String MEM_SCENE_BIGMEM = "BigMem";
    public static final String MEM_SCENE_DEFAULT = "default";
    public static final String MEM_SCENE_IDLE = "idle";
    public static final String MEM_SCENE_LAUNCH = "launch";
    public static final long MIN_INTERVAL_OP_TIMEOUT = 10000;
    public static final int MSG_ACTIVITY_DISPLAY_STATISTICS = 330;
    public static final int MSG_BOOST_SIGKILL_SWITCH = 301;
    public static final int MSG_COMPRESS_GPU = 313;
    public static final int MSG_DIRECT_SWAPPINESS = 303;
    public static final int MSG_MEM_BASE_VALUE = 300;
    public static final int MSG_MMONITOR_SWITCH = 332;
    public static final int MSG_PREREAD_DATA_REMOVE = 312;
    public static final int MSG_PREREAD_FILE = 314;
    public static final int MSG_PROTECTLRU_CONFIG_UPDATE = 308;
    public static final int MSG_PROTECTLRU_SET_FILENODE = 304;
    public static final int MSG_PROTECTLRU_SET_PROTECTRATIO = 307;
    public static final int MSG_PROTECTLRU_SET_PROTECTZONE = 305;
    public static final int MSG_PROTECTLRU_SWITCH = 306;
    public static final int MSG_SET_PREREAD_PATH = 311;
    public static final int MSG_SPECIAL_SCENE_POOL_BASE = 340;
    public static final int MSG_SPECIAL_SCENE_POOL_ENTER = 340;
    public static final int MSG_SPECIAL_SCENE_POOL_EXIT = 341;
    public static final int MSG_SPECIAL_SCENE_POOL_MAX = 349;
    public static final int MSG_SWAPPINESS = 302;
    public static final int PROCESSLIST_EXTRA_FREE_KBYTES = 24300;
    public static final int PROTECTLRU_ERROR_LEVEL = -1;
    public static final int PROTECTLRU_FIRST_LEVEL = 1;
    public static final int PROTECTLRU_MAX_LEVEL = 3;
    public static final int PROTECTLRU_STATE_PROTECT = 1;
    public static final int PROTECTLRU_STATE_UNPROTECT = 0;
    public static final long RECLAIM_KILL_GAP_MEMORY = 102400;
    public static final int REPEAT_RECLAIM_TIME_GAP = 600000;
    public static final int RESULT_ACTIVE = 1;
    public static final int RESULT_CONTINUE = 3;
    public static final int RESULT_FAIL = -1;
    public static final int RESULT_INACTIVE = 2;
    public static final int RESULT_OK = 0;
    private static final String TAG = "AwareMem_MemoryConstant";
    public static final int THD_HIGH_PRIORITY = 30720;
    public static final int THD_LOW_PRIORITY = 34560;
    private static long bigMemoryAppLimit = 614400;
    private static int cameraPowerupWatermark = -2;
    private static int cameraPowerupWatermarkXml = -1;
    private static int configDirectSwappiness = -1;
    private static int configExtraFreeKbytes = -1;
    private static int configGmcSwitch = 0;
    private static int configIonSpeedupSwitch = 0;
    private static String configProtectLruLimit = CONFIG_PROTECT_LRU_DEFAULT;
    private static int configProtectLruRatio = 50;
    private static int configSwappiness = -1;
    private static long defaultMemoryLimit = 614400;
    private static long defaultPeriod = 2000;
    private static long gpuMemoryLimit = MB_SIZE;
    private static CameraManager mCameraManager;
    private static Context mContext;
    private static ArrayMap<Integer, ArraySet<String>> mFileCacheMap = new ArrayMap();
    private static Map<String, ArrayList<String>> mPrereadFileMap = new ArrayMap();
    private static long maxPeriod = AwareAppMngSort.PREVIOUS_APP_DIRCACTIVITY_DECAYTIME;
    private static long maxReqMem = 921600;
    private static long minPeriod = 500;
    private static int numPeriod = 3;
    private static long reservedZramMemory = RECLAIM_KILL_GAP_MEMORY;
    private static int sysCameraUid = 0;

    public enum MemActionType {
        ACTION_KILL,
        ACTION_COMPRESS,
        ACTION_RECLAIM
    }

    public enum MemLevel {
        MEM_LOW,
        MEM_CRITICAL
    }

    public static void init(Context context) {
        mContext = context;
    }

    public static final long getIdleThresHold() {
        return CPU_IDLE_THRESHOLD;
    }

    public static final void setIdleThresHold(long idleLoad) {
        if (idleLoad < 0 || idleLoad > 100) {
            AwareLog.w(TAG, "config error! IdleThresHold=" + idleLoad);
        } else {
            CPU_IDLE_THRESHOLD = idleLoad;
        }
    }

    public static final long getNormalThresHold() {
        return CPU_NORMAL_THRESHOLD;
    }

    public static final void setNormalThresHold(long normalLoad) {
        if (normalLoad < 0 || normalLoad > 100) {
            AwareLog.w(TAG, "config error! NormalThresHold=" + normalLoad);
        } else {
            CPU_NORMAL_THRESHOLD = normalLoad;
        }
    }

    public static final long getReservedZramSpace() {
        return reservedZramMemory;
    }

    public static final long getReclaimKillGapMemory() {
        return RECLAIM_KILL_GAP_MEMORY;
    }

    public static final void setReservedZramSpace(long reserved) {
        if (reserved < 0) {
            AwareLog.w(TAG, "config error! ReservedZramSpace=" + reserved);
        } else {
            reservedZramMemory = reserved;
        }
    }

    public static final long getIdleMemory() {
        return IDLE_MEMORY;
    }

    public static final void setIdleMemory(long idleMemory) {
        if (idleMemory <= 0) {
            AwareLog.w(TAG, "config error! IdleMemory=" + idleMemory);
        } else {
            IDLE_MEMORY = idleMemory;
        }
    }

    public static final long getEmergencyMemory() {
        return EMERGENCY_MEMORY;
    }

    public static final void setEmergencyMemory(long emergemcyMemory) {
        if (emergemcyMemory <= 0) {
            AwareLog.w(TAG, "config error! EmergencyMemory=" + emergemcyMemory);
        } else {
            EMERGENCY_MEMORY = emergemcyMemory;
        }
    }

    public static final long getCriticalMemory() {
        return CRITICAL_MEMORY;
    }

    public static final void setDefaultCriticalMemory(long criticalMemory) {
        if (criticalMemory <= 0) {
            AwareLog.w(TAG, "config error! DefaultCriticalMemory=" + criticalMemory);
            return;
        }
        CRITICAL_MEMORY = criticalMemory;
        defaultMemoryLimit = criticalMemory;
    }

    public static final void enableBigMemCriticalMemory() {
        CRITICAL_MEMORY = bigMemoryAppLimit;
        AwareLog.d(TAG, "setLowCriticalMemory, current limit is " + (CRITICAL_MEMORY / 1024) + "MB");
    }

    public static final void disableBigMemCriticalMemory() {
        CRITICAL_MEMORY = defaultMemoryLimit;
        AwareLog.d(TAG, "resetLowCriticalMemory, current limit is " + (CRITICAL_MEMORY / 1024) + "MB");
    }

    public static final void setBigMemoryAppCriticalMemory(long bigMemLimit) {
        if (bigMemLimit <= 0) {
            AwareLog.w(TAG, "config error! BigMemoryAppCriticalMemory=" + bigMemLimit);
        } else {
            bigMemoryAppLimit = bigMemLimit;
        }
    }

    public static final void setMaxTimerPeriod(long maxTimerPeriod) {
        if (maxTimerPeriod <= 0) {
            AwareLog.w(TAG, "config error! MaxTimerPeriod=" + maxTimerPeriod);
        } else {
            maxPeriod = maxTimerPeriod;
        }
    }

    public static final long getMaxTimerPeriod() {
        return maxPeriod;
    }

    public static final void setMinTimerPeriod(long minTimerPeriod) {
        if (minTimerPeriod <= 0) {
            AwareLog.w(TAG, "config error! MinTimerPeriod=" + minTimerPeriod);
        } else {
            minPeriod = minTimerPeriod;
        }
    }

    public static final long getMinTimerPeriod() {
        return minPeriod;
    }

    public static final void setDefaultTimerPeriod(long defaultTimerPeriod) {
        if (defaultTimerPeriod <= 0) {
            AwareLog.w(TAG, "config error! DefaultTimerPeriod=" + defaultTimerPeriod);
        } else {
            defaultPeriod = defaultTimerPeriod;
        }
    }

    public static final long getDefaultTimerPeriod() {
        return defaultPeriod;
    }

    public static final void setNumTimerPeriod(int numTimerPeriod) {
        if (numTimerPeriod <= 0) {
            AwareLog.w(TAG, "config error! NumTimerPeriod=" + numTimerPeriod);
        } else {
            numPeriod = numTimerPeriod;
        }
    }

    public static final int getNumTimerPeriod() {
        return numPeriod;
    }

    public static final long getMiddleWater() {
        return (EMERGENCY_MEMORY + CRITICAL_MEMORY) / 2;
    }

    public static final long getMaxReqMem() {
        return maxReqMem;
    }

    public static final void setFileCacheMap(ArrayMap<Integer, ArraySet<String>> fileCacheMap) {
        if (fileCacheMap == null) {
            AwareLog.w(TAG, "config error! FileCacheMap null");
        } else {
            mFileCacheMap = fileCacheMap;
        }
    }

    public static final ArrayMap<Integer, ArraySet<String>> getFileCacheMap() {
        return mFileCacheMap;
    }

    public static final void setPrereadFileMap(Map<String, ArrayList<String>> prereadFileMap) {
        if (prereadFileMap == null) {
            AwareLog.w(TAG, "config error! prereadFileMap null");
        } else {
            mPrereadFileMap = prereadFileMap;
        }
    }

    public static Map<String, ArrayList<String>> getCameraPrereadFileMap() {
        return mPrereadFileMap;
    }

    public static final int getConfigExtraFreeKbytes() {
        return configExtraFreeKbytes;
    }

    public static final void setConfigExtraFreeKbytes(int extraFreeKbytes) {
        if (extraFreeKbytes < 0) {
            AwareLog.w(TAG, "config error! ExtraFreeKbytes=" + extraFreeKbytes);
        } else {
            configExtraFreeKbytes = extraFreeKbytes;
        }
    }

    public static final int getConfigSwappiness() {
        return configSwappiness;
    }

    public static final void setConfigSwappiness(int swappiness) {
        if (swappiness < 0 || swappiness > 200) {
            AwareLog.w(TAG, "config error! Swappiness=" + swappiness);
        } else {
            configSwappiness = swappiness;
        }
    }

    public static final int getConfigDirectSwappiness() {
        return configDirectSwappiness;
    }

    public static final void setConfigDirectSwappiness(int directswappiness) {
        if (directswappiness < 0 || directswappiness > 200) {
            AwareLog.w(TAG, "config error! DirectSwappiness=" + directswappiness);
        } else {
            configDirectSwappiness = directswappiness;
        }
    }

    public static final String getConfigProtectLruLimit() {
        return configProtectLruLimit;
    }

    public static final void setConfigProtectLruLimit(String protectLruLimit) {
        if (protectLruLimit == null) {
            AwareLog.w(TAG, "config error! ProtectLruLimit null");
        } else {
            configProtectLruLimit = protectLruLimit;
        }
    }

    public static final String getConfigProtectLruDefault() {
        return CONFIG_PROTECT_LRU_DEFAULT;
    }

    public static final void setConfigProtectLruRatio(int ratio) {
        if (ratio < 0 || ratio > 100) {
            AwareLog.w(TAG, "config error! ProtectLruRatio=" + ratio);
        } else {
            configProtectLruRatio = ratio;
        }
    }

    public static final int getConfigProtectLruRatio() {
        return configProtectLruRatio;
    }

    public static final void setConfigIonSpeedupSwitch(int switchValue) {
        if (switchValue < 0 || switchValue > 1) {
            AwareLog.w(TAG, "config error! ionSpeedupSwitch=" + switchValue);
        } else {
            configIonSpeedupSwitch = switchValue;
        }
    }

    public static final int getConfigIonSpeedupSwitch() {
        return configIonSpeedupSwitch;
    }

    public static final void setCameraPowerUPMem() {
        if (mContext != null) {
            mCameraManager = (CameraManager) mContext.getSystemService("camera");
            if (mCameraManager == null) {
                AwareLog.w(TAG, "mCameraManager is null!");
                return;
            }
            try {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics("0");
                if (characteristics != null) {
                    int[] memory_requirement = (int[]) characteristics.get(HW_CAMERA_MEMORY_REQUIREMENT_SUPPORTED);
                    if (!(memory_requirement == null || memory_requirement.length == 0)) {
                        cameraPowerupWatermark = memory_requirement[0] * 1024;
                    }
                }
            } catch (CameraAccessException e) {
                AwareLog.i(TAG, "getStartUpMemFromCamera: Characteristics CameraAccessException");
            } catch (IllegalArgumentException e2) {
                AwareLog.i(TAG, "getStartUpMemFromCamera: Characteristics IllegalArgumentException");
            }
        }
    }

    public static final void setCameraPowerUPMemoryDefault(int memoryKb) {
        if (memoryKb <= 0) {
            AwareLog.w(TAG, "setCameraPowerUPMemory  memoryKb error! " + memoryKb);
        } else {
            cameraPowerupWatermarkXml = memoryKb;
        }
    }

    public static final int getCameraPowerUPMemory() {
        if (cameraPowerupWatermark == -2) {
            setCameraPowerUPMem();
        }
        if (cameraPowerupWatermark == -2) {
            cameraPowerupWatermark = cameraPowerupWatermarkXml;
        }
        return cameraPowerupWatermark;
    }

    public static final void setConfigGmcSwitch(int switchValue) {
        if (switchValue < 0 || switchValue > 1) {
            AwareLog.w(TAG, "config error! GmcSwitch=" + switchValue);
        } else {
            configGmcSwitch = switchValue;
        }
    }

    public static final int getConfigGmcSwitch() {
        return configGmcSwitch;
    }

    public static final void setGpuMemoryLimit(long limit) {
        gpuMemoryLimit = limit;
    }

    public static final long getGpuMemoryLimit() {
        return gpuMemoryLimit;
    }

    public static final void setSysCameraUid(int uid) {
        sysCameraUid = uid;
    }

    public static final int getSystemCameraUid() {
        return sysCameraUid;
    }
}
