package com.android.server.mtm.taskstatus;

import com.android.server.am.ProcessRecordEx;
import com.huawei.android.app.IApplicationThreadEx;

/* access modifiers changed from: package-private */
public final class ProcessFastKillInfo {
    final boolean mAllowRestart;
    final ProcessRecordEx mApp;
    final IApplicationThreadEx mAppThread;
    final int mPid;
    final int mUid;

    ProcessFastKillInfo(ProcessRecordEx app, int uid, int pid, IApplicationThreadEx thread, boolean allowRestart) {
        this.mApp = app;
        this.mUid = uid;
        this.mPid = pid;
        this.mAppThread = thread;
        this.mAllowRestart = allowRestart;
    }
}
