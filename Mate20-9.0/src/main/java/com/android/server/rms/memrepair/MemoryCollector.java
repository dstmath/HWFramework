package com.android.server.rms.memrepair;

import android.os.Process;

public class MemoryCollector {
    private static final String[] REQ_FIELDS = {"PPid:", "VmSize:"};
    private static final String TAG = "MemoryCollector";
    private static final int VALUE_INDEX = 1;
    private static final String ZYGOTE64_PROC = "zygote64";
    private static final String ZYGOTE_PROC = "zygote";
    private static int sZygote64Pid = -1;
    private static int sZygotePid = -1;

    public static long getVSS(int pid) {
        if (pid <= 0) {
            return -1;
        }
        int ppid = -1;
        long vss = -1;
        long[] outValues = new long[REQ_FIELDS.length];
        Process.readProcLines("/proc/" + pid + "/status", REQ_FIELDS, outValues);
        if (outValues.length == REQ_FIELDS.length) {
            ppid = outValues[0] < 2147483647L ? (int) outValues[0] : -1;
            vss = outValues[1];
        }
        return (ppid <= 0 || !isZygote32Fork(ppid)) ? 0 : vss;
    }

    private static boolean isZygote32Fork(int ppid) {
        boolean z = false;
        if (sZygotePid > 0) {
            if (ppid == sZygotePid) {
                z = true;
            }
            return z;
        } else if (sZygote64Pid <= 0 || ppid != sZygote64Pid) {
            return updateZygote(ppid);
        } else {
            return false;
        }
    }

    private static boolean updateZygote(int ppid) {
        String parentProcName = Process.getCmdlineForPid(ppid);
        if (ZYGOTE_PROC.equals(parentProcName)) {
            sZygotePid = ppid;
            return true;
        } else if (!ZYGOTE64_PROC.equals(parentProcName)) {
            return false;
        } else {
            sZygote64Pid = ppid;
            return false;
        }
    }
}
