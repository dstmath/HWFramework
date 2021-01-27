package com.android.server.rms.collector;

import com.huawei.android.os.ProcessExt;

public final class ProcMemInfoReader {
    private static final String TAG = "RMS.ProcMemInfoReader";

    public long getProcessPss(String procName) {
        return getProcessPssByPid(getPidForProcName(new String[]{procName}));
    }

    public long getProcessPssByPid(int pid) {
        if (pid > 0) {
            return ProcessExt.getPss(pid) / 1024;
        }
        return 0;
    }

    public int getPidForProcName(String[] procName) {
        int[] pids;
        if (procName == null || (pids = ProcessExt.getPidsForCommands(procName)) == null || pids.length <= 0) {
            return 0;
        }
        return pids[0];
    }
}
