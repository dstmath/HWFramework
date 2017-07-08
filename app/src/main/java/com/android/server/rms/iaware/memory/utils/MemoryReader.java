package com.android.server.rms.iaware.memory.utils;

import android.os.Process;
import android.rms.iaware.AwareLog;
import com.android.server.rms.collector.MemInfoReader;

public class MemoryReader {
    private static final int BACKUP_APP_ADJ = 3;
    private static final int CACHED_APP_MAX_ADJ = 15;
    private static final int CACHED_APP_MIN_ADJ = 9;
    private static final int FOREGROUND_APP_ADJ = 0;
    private static final String LMK_KILL_COUNT = "/sys/module/lowmemorykiller/parameters/kill_count";
    private static final int LMK_MINFREE_SIZE = 6;
    private static final String MEMINFO_ALLOC_COUNT = "/sys/kernel/debug/slowpath_count";
    private static final int PERCEPTIBLE_APP_ADJ = 2;
    private static final int PREVIOUS_APP_ADJ = 7;
    private static final int[] PROCESS_STATM_FORMAT = null;
    private static final int PROCESS_STATM_SHARED = 1;
    private static final int PROCESS_STATM_VSS = 0;
    private static final int PROCESS_SWAPS_TOTAL = 0;
    private static final int PROCESS_SWAPS_USED = 1;
    private static final int PROC_COLON_TERM = 58;
    private static final int PROC_LINE_TERM = 10;
    private static final int[] PROC_MEMINFO_FORMAT = null;
    private static final String PROC_MEMINFO_NAME = "/proc/meminfo_lite";
    private static final int[] PROC_SWAPS_FORMAT = null;
    private static final String PROC_SWAPS_NAME = "/proc/swaps";
    private static final int SERVICE_B_ADJ = 8;
    private static final String TAG = "AwareMem_MemReader";
    private static final int VISIBLE_APP_ADJ = 1;
    private static final Object mLock = null;
    private static MemoryReader sReader;
    private long mMemAvailable;
    private long mMemFree;
    private MemInfoReader mMemInfo;
    private long mMemTotal;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.memory.utils.MemoryReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.memory.utils.MemoryReader.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.memory.utils.MemoryReader.<clinit>():void");
    }

    public static MemoryReader getInstance() {
        MemoryReader memoryReader;
        synchronized (mLock) {
            if (sReader == null) {
                sReader = new MemoryReader();
            }
            memoryReader = sReader;
        }
        return memoryReader;
    }

    private MemoryReader() {
        this.mMemAvailable = 0;
        this.mMemFree = 0;
        this.mMemTotal = 0;
        this.mMemInfo = new MemInfoReader();
    }

    public long getTotalRam() {
        long j;
        synchronized (mLock) {
            if (0 == this.mMemTotal) {
                this.mMemInfo.readMemInfo();
                this.mMemTotal = this.mMemInfo.getTotalSizeKb();
            }
            j = this.mMemTotal;
        }
        return j;
    }

    public long getFreeRam() {
        synchronized (mLock) {
            if (updateMemoryInfo() != 0) {
                return -1;
            }
            long j = this.mMemFree;
            return j;
        }
    }

    public long getMemAvailable() {
        synchronized (mLock) {
            if (updateMemoryInfo() != 0) {
                return -1;
            }
            long j = this.mMemAvailable;
            return j;
        }
    }

    public static boolean isZramOK() {
        long[] out = new long[PERCEPTIBLE_APP_ADJ];
        if (Process.readProcFile(PROC_SWAPS_NAME, PROC_SWAPS_FORMAT, null, out, null)) {
            long total = out[PROCESS_SWAPS_TOTAL];
            long used = out[VISIBLE_APP_ADJ];
            if (used < 0 || total < 0 || total - used <= MemoryConstant.getReservedZramSpace()) {
                return false;
            }
            return true;
        }
        AwareLog.e(TAG, "getUsedZram failed");
        return false;
    }

    public static long getPssForPid(int pid) {
        if (pid < VISIBLE_APP_ADJ) {
            return 0;
        }
        long[] statmData = new long[PERCEPTIBLE_APP_ADJ];
        if (Process.readProcFile("/proc/" + pid + "/statm", PROCESS_STATM_FORMAT, null, statmData, null)) {
            long vss = statmData[PROCESS_SWAPS_TOTAL];
            long shared = statmData[VISIBLE_APP_ADJ];
            if (vss > shared) {
                return (vss - shared) << 2;
            }
        }
        return 0;
    }

    private int updateMemoryInfo() {
        long[] memData = new long[PERCEPTIBLE_APP_ADJ];
        if (Process.readProcFile(PROC_MEMINFO_NAME, PROC_MEMINFO_FORMAT, null, memData, null)) {
            this.mMemFree = memData[PROCESS_SWAPS_TOTAL];
            this.mMemAvailable = memData[VISIBLE_APP_ADJ];
        } else if (this.mMemInfo.readMemInfo() != 0) {
            return -1;
        } else {
            this.mMemFree = this.mMemInfo.getFreeSizeKb();
            this.mMemAvailable = this.mMemInfo.getCachedSizeKb() + this.mMemInfo.getFreeSizeKb();
        }
        return PROCESS_SWAPS_TOTAL;
    }

    public static long getLmkOccurCount() {
        long[] occurCount = new long[VISIBLE_APP_ADJ];
        String str = LMK_KILL_COUNT;
        int[] iArr = new int[VISIBLE_APP_ADJ];
        iArr[PROCESS_SWAPS_TOTAL] = 8224;
        if (Process.readProcFile(str, iArr, null, occurCount, null)) {
            return occurCount[PROCESS_SWAPS_TOTAL];
        }
        return 0;
    }

    public static String[] getMeminfoAllocCount() {
        String[] meminfoAllocCount = new String[PERCEPTIBLE_APP_ADJ];
        if (Process.readProcFile(MEMINFO_ALLOC_COUNT, new int[]{4106, 4106}, meminfoAllocCount, null, null)) {
            return meminfoAllocCount;
        }
        return new String[PROCESS_SWAPS_TOTAL];
    }
}
