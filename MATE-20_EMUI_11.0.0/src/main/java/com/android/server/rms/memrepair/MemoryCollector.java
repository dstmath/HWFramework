package com.android.server.rms.memrepair;

import com.huawei.android.os.ProcessExt;

public class MemoryCollector {
    private static final String[] REQ_FIELDS = {"PPid:", "VmSize:"};
    private static final String TAG = "MemoryCollector";
    private static final int VALUE_INDEX = 1;
    private static final String ZYGOTE64_PROC = "zygote64";
    private static final String ZYGOTE_PROC = "zygote";
    private static int sZygote64Pid = -1;
    private static int sZygotePid = -1;

    private MemoryCollector() {
    }

    public static long getVss(int pid) {
        if (pid <= 0) {
            return -1;
        }
        int ppid = -1;
        long vss = -1;
        String[] strArr = REQ_FIELDS;
        long[] outValues = new long[strArr.length];
        ProcessExt.readProcLines("/proc/" + pid + "/status", strArr, outValues);
        if (outValues.length == REQ_FIELDS.length) {
            ppid = outValues[0] < 2147483647L ? (int) outValues[0] : -1;
            vss = outValues[1];
        }
        if (ppid <= 0 || !isZygote32Fork(ppid)) {
            return 0;
        }
        return vss;
    }

    private static boolean isZygote32Fork(int ppid) {
        int i = sZygotePid;
        if (i <= 0) {
            int i2 = sZygote64Pid;
            if (i2 <= 0 || ppid != i2) {
                return updateZygote(ppid);
            }
            return false;
        } else if (ppid == i) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean updateZygote(int ppid) {
        String parentProcName = ProcessExt.getCmdlineForPid(ppid);
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
