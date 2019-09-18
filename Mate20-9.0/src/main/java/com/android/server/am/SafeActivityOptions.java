package com.android.server.am;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.util.Slog;
import android.view.RemoteAnimationAdapter;
import com.android.internal.annotations.VisibleForTesting;

class SafeActivityOptions {
    private static final String TAG = "ActivityManager";
    private ActivityOptions mCallerOptions;
    private final int mOriginalCallingPid = Binder.getCallingPid();
    private final int mOriginalCallingUid = Binder.getCallingUid();
    private final ActivityOptions mOriginalOptions;
    private int mRealCallingPid;
    private int mRealCallingUid;

    static SafeActivityOptions fromBundle(Bundle bOptions) {
        if (bOptions != null) {
            return new SafeActivityOptions(ActivityOptions.fromBundle(bOptions));
        }
        return null;
    }

    SafeActivityOptions(ActivityOptions options) {
        this.mOriginalOptions = options;
    }

    /* access modifiers changed from: package-private */
    public void setCallerOptions(ActivityOptions options) {
        this.mRealCallingPid = Binder.getCallingPid();
        this.mRealCallingUid = Binder.getCallingUid();
        this.mCallerOptions = options;
    }

    /* access modifiers changed from: package-private */
    public ActivityOptions getOptions(ActivityRecord r) throws SecurityException {
        return getOptions(r.intent, r.info, r.app, r.mStackSupervisor);
    }

    /* access modifiers changed from: package-private */
    public ActivityOptions getOptions(ActivityStackSupervisor supervisor) throws SecurityException {
        return getOptions(null, null, null, supervisor);
    }

    /* access modifiers changed from: package-private */
    public ActivityOptions getOptions(Intent intent, ActivityInfo aInfo, ProcessRecord callerApp, ActivityStackSupervisor supervisor) throws SecurityException {
        if (this.mOriginalOptions != null) {
            checkPermissions(intent, aInfo, callerApp, supervisor, this.mOriginalOptions, this.mOriginalCallingPid, this.mOriginalCallingUid);
            setCallingPidForRemoteAnimationAdapter(this.mOriginalOptions, this.mOriginalCallingPid);
        }
        if (this.mCallerOptions != null) {
            checkPermissions(intent, aInfo, callerApp, supervisor, this.mCallerOptions, this.mRealCallingPid, this.mRealCallingUid);
            setCallingPidForRemoteAnimationAdapter(this.mCallerOptions, this.mRealCallingPid);
        }
        return mergeActivityOptions(this.mOriginalOptions, this.mCallerOptions);
    }

    private void setCallingPidForRemoteAnimationAdapter(ActivityOptions options, int callingPid) {
        RemoteAnimationAdapter adapter = options.getRemoteAnimationAdapter();
        if (adapter != null) {
            if (callingPid == Process.myPid()) {
                Slog.wtf("ActivityManager", "Safe activity options constructed after clearing calling id");
            } else {
                adapter.setCallingPid(callingPid);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Bundle popAppVerificationBundle() {
        if (this.mOriginalOptions != null) {
            return this.mOriginalOptions.popAppVerificationBundle();
        }
        return null;
    }

    private void abort() {
        if (this.mOriginalOptions != null) {
            ActivityOptions.abort(this.mOriginalOptions);
        }
        if (this.mCallerOptions != null) {
            ActivityOptions.abort(this.mCallerOptions);
        }
    }

    static void abort(SafeActivityOptions options) {
        if (options != null) {
            options.abort();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public ActivityOptions mergeActivityOptions(ActivityOptions options1, ActivityOptions options2) {
        if (options1 == null) {
            return options2;
        }
        if (options2 == null) {
            return options1;
        }
        Bundle b1 = options1.toBundle();
        b1.putAll(options2.toBundle());
        return ActivityOptions.fromBundle(b1);
    }

    private void checkPermissions(Intent intent, ActivityInfo aInfo, ProcessRecord callerApp, ActivityStackSupervisor supervisor, ActivityOptions options, int callingPid, int callingUid) {
        if (options.getLaunchTaskId() == -1 || supervisor.mRecentTasks.isCallerRecents(callingUid) || supervisor.mService.checkPermission("android.permission.START_TASKS_FROM_RECENTS", callingPid, callingUid) != -1) {
            int launchDisplayId = options.getLaunchDisplayId();
            if (aInfo == null || launchDisplayId == -1 || supervisor.isCallerAllowedToLaunchOnDisplay(callingPid, callingUid, launchDisplayId, aInfo)) {
                boolean lockTaskMode = options.getLockTaskMode();
                if (aInfo != null && lockTaskMode && !supervisor.mService.getLockTaskController().isPackageWhitelisted(UserHandle.getUserId(callingUid), aInfo.packageName)) {
                    String msg = "Permission Denial: starting " + getIntentString(intent) + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") with lockTaskMode=true";
                    Slog.w("ActivityManager", msg);
                    throw new SecurityException(msg);
                } else if (options.getRemoteAnimationAdapter() != null && supervisor.mService.checkPermission("android.permission.CONTROL_REMOTE_APP_TRANSITION_ANIMATIONS", callingPid, callingUid) != 0) {
                    String msg2 = "Permission Denial: starting " + getIntentString(intent) + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") with remoteAnimationAdapter";
                    Slog.w("ActivityManager", msg2);
                    throw new SecurityException(msg2);
                }
            } else {
                String msg3 = "Permission Denial: starting " + getIntentString(intent) + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") with launchDisplayId=" + launchDisplayId;
                Slog.w("ActivityManager", msg3);
                throw new SecurityException(msg3);
            }
        } else {
            String msg4 = "Permission Denial: starting " + getIntentString(intent) + " from " + callerApp + " (pid=" + callingPid + ", uid=" + callingUid + ") with launchTaskId=" + options.getLaunchTaskId();
            Slog.w("ActivityManager", msg4);
            throw new SecurityException(msg4);
        }
    }

    private String getIntentString(Intent intent) {
        return intent != null ? intent.toString() : "(no intent)";
    }
}
