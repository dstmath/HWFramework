package com.android.server.rms.iaware.memory.utils;

import android.util.ArrayMap;
import android.util.ArraySet;

public final class MemoryConstant {
    public static final long APP_AVG_USS = 20480;
    public static final int AWARE_INVAILD_KILL_NUM = 3;
    public static final int AWARE_INVAILD_KILL_THRESHOLD = 5;
    private static int CONFIG_DIRECT_SWAPPINESS = 0;
    private static int CONFIG_EXTRA_FREE_KBYTES = 0;
    private static String CONFIG_PROTECT_LRU_DEFAULT = null;
    private static String CONFIG_PROTECT_LRU_LIMIT = null;
    private static int CONFIG_SWAPPINESS = 0;
    private static long CPU_IDLE_THRESHOLD = 0;
    private static long CPU_NORMAL_THRESHOLD = 0;
    private static long CRITICAL_MEMORY = 0;
    public static final int DEFAULT_DIRECT_SWAPPINESS = 30;
    public static final int DEFAULT_EXTRA_FREE_KBYTES = 0;
    private static long DEFAULT_PERIOD = 0;
    public static final int DEFAULT_SWAPPINESS = 60;
    private static long EMERGENCY_MEMORY = 0;
    public static final int FILECACHE_ERROR_LEVEL = -1;
    public static final int FILECACHE_FIRST_LEVEL = 1;
    public static final int FILECACHE_MAX_LEVEL = 3;
    private static long IDLE_MEMORY = 0;
    public static final int MAX_APPNAME_LEN = 64;
    public static final int MAX_EXTRA_FREE_KBYTES = 200000;
    private static long MAX_PERIOD = 0;
    public static final long MB_SIZE = 1048576;
    public static final String MEM_CONSTANT_AVERAGEAPPUSSNAME = "averageAppUss";
    public static final String MEM_CONSTANT_BIGMEMCRITICALMEMORYNAME = "bigMemCriticalMemory";
    public static final String MEM_CONSTANT_CONFIGNAME = "MemoryConstant";
    public static final String MEM_CONSTANT_DEFAULTCRITICALMEMORYNAME = "defaultCriticalMemory";
    public static final String MEM_CONSTANT_DEFAULTTIMERPERIOD = "defaultTimerPeriod";
    public static final String MEM_CONSTANT_DIRECTSWAPPINESSNAME = "direct_swappiness";
    public static final String MEM_CONSTANT_EMERGEMCYMEMORYNAME = "emergencyMemory";
    public static final String MEM_CONSTANT_EXTRAFREEKBYTESNAME = "extra_free_kbytes";
    public static final String MEM_CONSTANT_HIGHCPULOADNAME = "highCpuLoad";
    public static final String MEM_CONSTANT_LOWCPULOADNAME = "lowCpuLoad";
    public static final String MEM_CONSTANT_MAXTIMERPERIOD = "maxTimerPeriod";
    public static final String MEM_CONSTANT_MINTIMERPERIOD = "minTimerPeriod";
    public static final String MEM_CONSTANT_NORMALMEMORYNAME = "normalMemory";
    public static final String MEM_CONSTANT_NUMTIMERPERIOD = "numTimerPeriod";
    public static final String MEM_CONSTANT_PROCESSLIMIT = "numProcessLimit";
    public static final String MEM_CONSTANT_PROTECTLRULIMIT = "protect_lru_limit";
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
    public static final String MEM_POLICY_KILLACTION = "kill";
    public static final String MEM_POLICY_QUICKKILLACTION = "quickkill";
    public static final String MEM_POLICY_RECLAIM = "reclaim";
    public static final String MEM_POLICY_SCENE = "scene";
    public static final String MEM_SCENE_BIGMEM = "BigMem";
    public static final String MEM_SCENE_DEFAULT = "default";
    public static final String MEM_SCENE_IDLE = "idle";
    public static final String MEM_SCENE_LAUNCH = "launch";
    public static final long MIN_INTERVAL_OP_TIMEOUT = 10000;
    private static long MIN_PERIOD = 0;
    public static final int MSG_BOOST_SIGKILL_SWITCH = 301;
    public static final int MSG_DIRECT_SWAPPINESS = 303;
    public static final int MSG_FILECACHE_NODE_REMOVE_PROTECT_LRU = 305;
    public static final int MSG_FILECACHE_NODE_SET_PROTECT_LRU = 304;
    public static final int MSG_FILECACHE_SET_CONFIG_PROTECT_LRU = 306;
    public static final int MSG_MEM_BASE_VALUE = 300;
    public static final int MSG_SWAPPINESS = 302;
    private static int NUM_PERIOD = 0;
    public static final int PROCESSLIST_EXTRA_FREE_KBYTES = 24300;
    public static final long RECLAIM_KILL_GAP_MEMORY = 51200;
    public static final int REPEAT_RECLAIM_TIME_GAP = 600000;
    private static long RESERVED_ZRAM_MEMORY = 0;
    public static final int RESULT_ACTIVE = 1;
    public static final int RESULT_CONTINUE = 3;
    public static final int RESULT_FAIL = -1;
    public static final int RESULT_INACTIVE = 2;
    public static final int RESULT_OK = 0;
    private static long bigMemoryAppLimit;
    private static long defaultMemoryLimit;
    private static ArrayMap<Integer, ArraySet<String>> mFileCacheMap;
    private static long maxReqMem;

    public enum MemActionType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.memory.utils.MemoryConstant.MemActionType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.memory.utils.MemoryConstant.MemActionType.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.memory.utils.MemoryConstant.MemActionType.<clinit>():void");
        }
    }

    public enum MemLevel {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.memory.utils.MemoryConstant.MemLevel.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.memory.utils.MemoryConstant.MemLevel.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.memory.utils.MemoryConstant.MemLevel.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.memory.utils.MemoryConstant.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.memory.utils.MemoryConstant.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.memory.utils.MemoryConstant.<clinit>():void");
    }

    public MemoryConstant() {
    }

    public static final long getIdleThresHold() {
        return CPU_IDLE_THRESHOLD;
    }

    public static final void setIdleThresHold(long idleLoad) {
        CPU_IDLE_THRESHOLD = idleLoad;
    }

    public static final long getNormalThresHold() {
        return CPU_NORMAL_THRESHOLD;
    }

    public static final void setNormalThresHold(long normalLoad) {
        CPU_NORMAL_THRESHOLD = normalLoad;
    }

    public static final long getReservedZramSpace() {
        return RESERVED_ZRAM_MEMORY;
    }

    public static final void setReservedZramSpace(long reserved) {
        RESERVED_ZRAM_MEMORY = reserved;
    }

    public static final long getIdleMemory() {
        return IDLE_MEMORY;
    }

    public static final void setIdleMemory(long idleMemory) {
        IDLE_MEMORY = idleMemory;
    }

    public static final long getEmergencyMemory() {
        return EMERGENCY_MEMORY;
    }

    public static final void setEmergencyMemory(long emergemcyMemory) {
        EMERGENCY_MEMORY = emergemcyMemory;
    }

    public static final long getCriticalMemory() {
        return CRITICAL_MEMORY;
    }

    public static final void setDefaultCriticalMemory(long criticalMemory) {
        CRITICAL_MEMORY = criticalMemory;
        defaultMemoryLimit = criticalMemory;
    }

    public static final void enableBigMemCriticalMemory() {
        CRITICAL_MEMORY = bigMemoryAppLimit;
    }

    public static final void disableBigMemCriticalMemory() {
        CRITICAL_MEMORY = defaultMemoryLimit;
    }

    public static final void setBigMemoryAppCriticalMemory(long bigMemLimit) {
        bigMemoryAppLimit = bigMemLimit;
    }

    public static final void setMaxTimerPeriod(long maxTimerPeriod) {
        MAX_PERIOD = maxTimerPeriod;
    }

    public static final long getMaxTimerPeriod() {
        return MAX_PERIOD;
    }

    public static final void setMinTimerPeriod(long minTimerPeriod) {
        MIN_PERIOD = minTimerPeriod;
    }

    public static final long getMinTimerPeriod() {
        return MIN_PERIOD;
    }

    public static final void setDefaultTimerPeriod(long defaultTimerPeriod) {
        DEFAULT_PERIOD = defaultTimerPeriod;
    }

    public static final long getDefaultTimerPeriod() {
        return DEFAULT_PERIOD;
    }

    public static final void setNumTimerPeriod(int numTimerPeriod) {
        NUM_PERIOD = numTimerPeriod;
    }

    public static final int getNumTimerPeriod() {
        return NUM_PERIOD;
    }

    public static final long getMiddleWater() {
        return (EMERGENCY_MEMORY + CRITICAL_MEMORY) / 2;
    }

    public static final long getMaxReqMem() {
        return maxReqMem;
    }

    public static final void setFileCacheMap(ArrayMap<Integer, ArraySet<String>> fileCacheMap) {
        mFileCacheMap = fileCacheMap;
    }

    public static final ArrayMap<Integer, ArraySet<String>> getFileCacheMap() {
        return mFileCacheMap;
    }

    public static final int getConfigExtraFreeKbytes() {
        return CONFIG_EXTRA_FREE_KBYTES;
    }

    public static final void setConfigExtraFreeKbytes(int extraFreeKbytes) {
        CONFIG_EXTRA_FREE_KBYTES = extraFreeKbytes;
    }

    public static final int getConfigSwappiness() {
        return CONFIG_SWAPPINESS;
    }

    public static final void setConfigSwappiness(int swappiness) {
        CONFIG_SWAPPINESS = swappiness;
    }

    public static final int getConfigDirectSwappiness() {
        return CONFIG_DIRECT_SWAPPINESS;
    }

    public static final void setConfigDirectSwappiness(int directswappiness) {
        CONFIG_DIRECT_SWAPPINESS = directswappiness;
    }

    public static final String getConfigProtectLruLimit() {
        return CONFIG_PROTECT_LRU_LIMIT;
    }

    public static final void setConfigProtectLruLimit(String protectLruLimit) {
        CONFIG_PROTECT_LRU_LIMIT = protectLruLimit;
    }

    public static final String getConfigProtectLruDefault() {
        return CONFIG_PROTECT_LRU_DEFAULT;
    }
}
