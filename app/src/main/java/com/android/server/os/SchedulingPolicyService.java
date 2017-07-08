package com.android.server.os;

import android.os.Binder;
import android.os.ISchedulingPolicyService.Stub;
import android.os.Process;

public class SchedulingPolicyService extends Stub {
    private static final int PRIORITY_MAX = 3;
    private static final int PRIORITY_MIN = 1;
    private static final String TAG = "SchedulingPolicyService";

    public int requestPriority(int pid, int tid, int prio) {
        int i = PRIORITY_MAX;
        if (!isPermittedCallingUid() || prio < PRIORITY_MIN || prio > PRIORITY_MAX || Process.getThreadGroupLeader(tid) != pid) {
            return -1;
        }
        try {
            if (Binder.getCallingPid() == pid) {
                i = 4;
            }
            Process.setThreadGroup(tid, i);
            Process.setThreadScheduler(tid, PRIORITY_MIN, prio);
            return 0;
        } catch (RuntimeException e) {
            return -1;
        }
    }

    private boolean isPermittedCallingUid() {
        switch (Binder.getCallingUid()) {
            case 1041:
            case 1047:
                return true;
            default:
                return false;
        }
    }
}
