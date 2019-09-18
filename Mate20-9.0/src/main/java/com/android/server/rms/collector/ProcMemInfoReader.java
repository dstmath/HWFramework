package com.android.server.rms.collector;

import android.os.Process;
import android.util.Log;

public final class ProcMemInfoReader {
    private static final String TAG = "RMS.ProcMemInfoReader";

    public long getProcessPss(String procName) {
        return getProcessPssByPID(getPidForProcName(new String[]{procName}));
    }

    public long getProcessPssByPID(int pid) {
        if (pid <= 0) {
            return 0;
        }
        try {
            return Process.getPss(pid) / 1024;
        } catch (Exception e) {
            Log.w(TAG, e);
            return 0;
        } catch (Throwable th) {
            return 0;
        }
    }

    public int getPidForProcName(String[] procName) {
        if (procName == null) {
            return 0;
        }
        try {
            int[] pids = Process.getPidsForCommands(procName);
            if (pids == null || pids.length <= 0) {
                return 0;
            }
            return pids[0];
        } catch (Exception e) {
            Log.w(TAG, e);
            return 0;
        } catch (Throwable th) {
            return 0;
        }
    }
}
