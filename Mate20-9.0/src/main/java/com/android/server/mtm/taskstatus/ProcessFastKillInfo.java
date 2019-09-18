package com.android.server.mtm.taskstatus;

import android.app.IApplicationThread;
import com.android.server.am.ProcessRecord;

final class ProcessFastKillInfo {
    final boolean mAllowRestart;
    final ProcessRecord mApp;
    final IApplicationThread mAppThread;
    final int mPid;
    final int mUid;

    ProcessFastKillInfo(ProcessRecord app, int uid, int pid, IApplicationThread thread, boolean allowRestart) {
        this.mApp = app;
        this.mUid = uid;
        this.mPid = pid;
        this.mAppThread = thread;
        this.mAllowRestart = allowRestart;
    }
}
