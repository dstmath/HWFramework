package com.android.server.am;

import android.content.ComponentName;
import android.content.IIntentReceiver.Stub;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Slog;
import com.android.internal.util.ProgressReporter;
import java.util.List;

public abstract class PreBootBroadcaster extends Stub {
    private static final String TAG = "PreBootBroadcaster";
    private int mIndex;
    private final Intent mIntent;
    private final ProgressReporter mProgress;
    private final ActivityManagerService mService;
    private final List<ResolveInfo> mTargets;
    private final int mUserId;

    public abstract void onFinished();

    public PreBootBroadcaster(ActivityManagerService service, int userId, ProgressReporter progress) {
        this.mIndex = 0;
        this.mService = service;
        this.mUserId = userId;
        this.mProgress = progress;
        this.mIntent = new Intent("android.intent.action.PRE_BOOT_COMPLETED");
        this.mIntent.addFlags(33554688);
        this.mTargets = this.mService.mContext.getPackageManager().queryBroadcastReceiversAsUser(this.mIntent, DumpState.DUMP_DEXOPT, UserHandle.of(userId));
    }

    public void sendNext() {
        if (this.mIndex >= this.mTargets.size()) {
            onFinished();
        } else if (this.mService.isUserRunning(this.mUserId, 0)) {
            List list = this.mTargets;
            int i = this.mIndex;
            this.mIndex = i + 1;
            ResolveInfo ri = (ResolveInfo) list.get(i);
            ComponentName componentName = ri.activityInfo.getComponentName();
            if (this.mProgress != null) {
                CharSequence label = ri.activityInfo.loadLabel(this.mService.mContext.getPackageManager());
                this.mProgress.setProgress(this.mIndex, this.mTargets.size(), this.mService.mContext.getString(17040290, new Object[]{label}));
            }
            Slog.i(TAG, "Pre-boot of " + componentName.toShortString() + " for user " + this.mUserId);
            EventLogTags.writeAmPreBoot(this.mUserId, componentName.getPackageName());
            this.mIntent.setComponent(componentName);
            this.mService.broadcastIntentLocked(null, null, this.mIntent, null, this, 0, null, null, null, -1, null, true, false, ActivityManagerService.MY_PID, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, this.mUserId);
        } else {
            Slog.i(TAG, "User " + this.mUserId + " is no longer running; skipping remaining receivers");
            onFinished();
        }
    }

    public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
        sendNext();
    }
}
