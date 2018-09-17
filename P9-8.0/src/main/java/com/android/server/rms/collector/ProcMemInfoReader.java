package com.android.server.rms.collector;

import android.os.Process;
import android.util.Log;

public final class ProcMemInfoReader {
    private static final String TAG = "RMS.ProcMemInfoReader";

    public long getProcessPss(String procName) {
        return getProcessPssByPID(getPidForProcName(new String[]{procName}));
    }

    public long getProcessPssByPID(int pid) {
        long pss = 0;
        if (pid > 0) {
            try {
                pss = Process.getPss(pid) / 1024;
            } catch (Exception e) {
                Log.w(TAG, e);
                return pss;
            } catch (Throwable th) {
                return pss;
            }
        }
        return pss;
    }

    public int getPidForProcName(String[] procName) {
        int pid = 0;
        if (procName != null) {
            try {
                int[] pids = Process.getPidsForCommands(procName);
                if (pids != null && pids.length > 0) {
                    pid = pids[0];
                }
            } catch (Exception e) {
                Log.w(TAG, e);
                return 0;
            } catch (Throwable th) {
                return 0;
            }
        }
        return pid;
    }
}
