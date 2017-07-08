package com.android.server.am;

import android.os.SystemClock;
import android.os.UserHandle;
import android.util.TimeUtils;

public final class UidRecord {
    static final int CHANGE_ACTIVE = 4;
    static final int CHANGE_GONE = 1;
    static final int CHANGE_GONE_IDLE = 2;
    static final int CHANGE_IDLE = 3;
    static final int CHANGE_PROCSTATE = 0;
    int curProcState;
    boolean idle;
    long lastBackgroundTime;
    int numProcs;
    ChangeItem pendingChange;
    int setProcState;
    final int uid;

    static final class ChangeItem {
        int change;
        int processState;
        int uid;
        UidRecord uidRecord;

        ChangeItem() {
        }
    }

    public UidRecord(int _uid) {
        this.setProcState = -1;
        this.uid = _uid;
        reset();
    }

    public void reset() {
        this.curProcState = 16;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(DumpState.DUMP_PACKAGES);
        sb.append("UidRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        UserHandle.formatUid(sb, this.uid);
        sb.append(' ');
        sb.append(ProcessList.makeProcStateString(this.curProcState));
        if (this.lastBackgroundTime > 0) {
            sb.append(" bg:");
            TimeUtils.formatDuration(SystemClock.elapsedRealtime() - this.lastBackgroundTime, sb);
        }
        if (this.idle) {
            sb.append(" idle");
        }
        sb.append(" procs:");
        sb.append(this.numProcs);
        sb.append("}");
        return sb.toString();
    }
}
