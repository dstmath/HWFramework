package com.android.server.rms.iaware.memory.utils;

import android.os.Process;
import android.rms.iaware.AwareLog;

public class CpuReader {
    private static final int CPU_INTERVAL_TIME = 20;
    private static final int[] SYSTEM_STAT_FORMAT = null;
    private static final String TAG = "AwareMem_CpuReader";
    private static long[] mSystemStatData;
    private static CpuReader sReader;

    private static class CpuData {
        long mIdleTickTime;
        long mTotalTickTime;

        CpuData() {
            this.mTotalTickTime = 0;
            this.mIdleTickTime = 0;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.memory.utils.CpuReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.memory.utils.CpuReader.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.memory.utils.CpuReader.<clinit>():void");
    }

    public static CpuReader getInstance() {
        CpuReader cpuReader;
        synchronized (CpuReader.class) {
            if (sReader == null) {
                sReader = new CpuReader();
            }
            cpuReader = sReader;
        }
        return cpuReader;
    }

    private int sample(CpuData data) {
        long[] sysCpu = mSystemStatData;
        if (Process.readProcFile("/proc/stat", SYSTEM_STAT_FORMAT, null, sysCpu, null)) {
            data.mTotalTickTime = (((((sysCpu[0] + sysCpu[1]) + sysCpu[2]) + sysCpu[3]) + sysCpu[4]) + sysCpu[5]) + sysCpu[6];
            data.mIdleTickTime = sysCpu[3];
            return 0;
        }
        AwareLog.w(TAG, "init read /proc/stat error !");
        return -1;
    }

    public final long getCpuPercent() {
        CpuData systemStatData1 = new CpuData();
        if (sample(systemStatData1) != 0) {
            return -1;
        }
        try {
            Thread.sleep(20);
            CpuData systemStatData2 = new CpuData();
            if (sample(systemStatData2) != 0) {
                return -1;
            }
            long totalTickTime = systemStatData2.mTotalTickTime - systemStatData1.mTotalTickTime;
            long idleTickTime = systemStatData2.mIdleTickTime - systemStatData1.mIdleTickTime;
            if (totalTickTime < 0 || idleTickTime < 0 || idleTickTime > totalTickTime) {
                return -1;
            }
            return totalTickTime == 0 ? 0 : 100 - ((100 * idleTickTime) / totalTickTime);
        } catch (InterruptedException e) {
            AwareLog.e(TAG, "interrupt error !");
            return -1;
        }
    }
}
