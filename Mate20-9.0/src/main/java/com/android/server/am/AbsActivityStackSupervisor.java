package com.android.server.am;

import android.app.ActivityOptions;
import android.app.IApplicationThread;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import com.android.server.wm.ConfigurationContainer;

public abstract class AbsActivityStackSupervisor extends ConfigurationContainer {
    protected static final int REPORT_DISPLAY_REMOVE_TIMEOUT_MSG = 10002;
    protected static final int REPORT_WINDOW_STATE_CHANGED_MSG = 10001;

    /* access modifiers changed from: protected */
    public void recognitionMaliciousApp(IApplicationThread caller, Intent intet, int userId) {
    }

    /* access modifiers changed from: package-private */
    public boolean shouldNotKillProcWhenRemoveTask(String pkg) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean resumeAppLockActivityIfNeeded(ActivityStack stack, ActivityOptions targetOptions) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isAppInLockList(String pkgName, int userId) {
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean hasActivityInStackLocked(ActivityInfo aInfo) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void uploadUnSupportSplitScreenAppPackageName(String pkgName) {
    }

    /* access modifiers changed from: protected */
    public ActivityStack getTargetSplitTopStack(ActivityStack current) {
        return null;
    }

    /* access modifiers changed from: protected */
    public ActivityStack getNextStackInSplitSecondary(ActivityStack current) {
        return null;
    }
}
