package com.android.server.am;

import android.app.ActivityManager;
import android.app.IAppTask;
import android.app.IApplicationThread;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import com.android.server.pm.DumpState;

class AppTaskImpl extends IAppTask.Stub {
    private int mCallingUid;
    private ActivityManagerService mService;
    private int mTaskId;

    public AppTaskImpl(ActivityManagerService service, int taskId, int callingUid) {
        this.mService = service;
        this.mTaskId = taskId;
        this.mCallingUid = callingUid;
    }

    private void checkCaller() {
        if (this.mCallingUid != Binder.getCallingUid()) {
            throw new SecurityException("Caller " + this.mCallingUid + " does not match caller of getAppTasks(): " + Binder.getCallingUid());
        }
    }

    public void finishAndRemoveTask() {
        long origId;
        checkCaller();
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                origId = Binder.clearCallingIdentity();
                if (this.mService.mStackSupervisor.removeTaskByIdLocked(this.mTaskId, false, true, "finish-and-remove-task")) {
                    Binder.restoreCallingIdentity(origId);
                } else {
                    throw new IllegalArgumentException("Unable to find task ID " + this.mTaskId);
                }
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
    }

    public ActivityManager.RecentTaskInfo getTaskInfo() {
        long origId;
        ActivityManager.RecentTaskInfo createRecentTaskInfo;
        checkCaller();
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                origId = Binder.clearCallingIdentity();
                TaskRecord tr = this.mService.mStackSupervisor.anyTaskForIdLocked(this.mTaskId, 1);
                if (tr != null) {
                    createRecentTaskInfo = this.mService.getRecentTasks().createRecentTaskInfo(tr);
                    Binder.restoreCallingIdentity(origId);
                } else {
                    throw new IllegalArgumentException("Unable to find task ID " + this.mTaskId);
                }
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        return createRecentTaskInfo;
    }

    public void moveToFront() {
        checkCaller();
        int callingPid = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();
        long origId = Binder.clearCallingIdentity();
        try {
            synchronized (this.mService) {
                ActivityManagerService.boostPriorityForLockedSection();
                this.mService.mStackSupervisor.startActivityFromRecents(callingPid, callingUid, this.mTaskId, null);
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
            Binder.restoreCallingIdentity(origId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    public int startActivity(IBinder whoThread, String callingPackage, Intent intent, String resolvedType, Bundle bOptions) {
        TaskRecord tr;
        IApplicationThread appThread;
        checkCaller();
        int callingUser = UserHandle.getCallingUserId();
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                tr = this.mService.mStackSupervisor.anyTaskForIdLocked(this.mTaskId, 1);
                if (tr != null) {
                    appThread = IApplicationThread.Stub.asInterface(whoThread);
                    if (appThread == null) {
                        throw new IllegalArgumentException("Bad app thread " + appThread);
                    }
                } else {
                    throw new IllegalArgumentException("Unable to find task ID " + this.mTaskId);
                }
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        return this.mService.getActivityStartController().obtainStarter(intent, "AppTaskImpl").setCaller(appThread).setCallingPackage(callingPackage).setResolvedType(resolvedType).setActivityOptions(bOptions).setMayWait(callingUser).setInTask(tr).execute();
    }

    public void setExcludeFromRecents(boolean exclude) {
        long origId;
        checkCaller();
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                origId = Binder.clearCallingIdentity();
                TaskRecord tr = this.mService.mStackSupervisor.anyTaskForIdLocked(this.mTaskId, 1);
                if (tr != null) {
                    Intent intent = tr.getBaseIntent();
                    if (exclude) {
                        intent.addFlags(DumpState.DUMP_VOLUMES);
                    } else {
                        intent.setFlags(intent.getFlags() & -8388609);
                    }
                    Binder.restoreCallingIdentity(origId);
                } else {
                    throw new IllegalArgumentException("Unable to find task ID " + this.mTaskId);
                }
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
    }
}
