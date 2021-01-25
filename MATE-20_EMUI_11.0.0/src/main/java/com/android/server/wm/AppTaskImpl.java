package com.android.server.wm;

import android.app.ActivityManager;
import android.app.IAppTask;
import android.app.IApplicationThread;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Slog;

/* access modifiers changed from: package-private */
public class AppTaskImpl extends IAppTask.Stub {
    private static final String TAG = "AppTaskImpl";
    private int mCallingUid;
    private ActivityTaskManagerService mService;
    private int mTaskId;

    public AppTaskImpl(ActivityTaskManagerService service, int taskId, int callingUid) {
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
        checkCaller();
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    if (!this.mService.mStackSupervisor.removeTaskByIdLocked(this.mTaskId, false, true, "finish-and-remove-task")) {
                        throw new IllegalArgumentException("Unable to find task ID " + this.mTaskId);
                    }
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public ActivityManager.RecentTaskInfo getTaskInfo() {
        ActivityManager.RecentTaskInfo createRecentTaskInfo;
        checkCaller();
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    TaskRecord tr = this.mService.mRootActivityContainer.anyTaskForId(this.mTaskId, 1);
                    if (tr != null) {
                        createRecentTaskInfo = this.mService.getRecentTasks().createRecentTaskInfo(tr);
                    } else {
                        throw new IllegalArgumentException("Unable to find task ID " + this.mTaskId);
                    }
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return createRecentTaskInfo;
    }

    public void moveToFront(IApplicationThread appThread, String callingPackage) {
        WindowManagerGlobalLock windowManagerGlobalLock;
        Throwable th;
        checkCaller();
        int callingPid = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();
        if (this.mService.isSameApp(callingUid, callingPackage)) {
            long origId = Binder.clearCallingIdentity();
            try {
                WindowManagerGlobalLock windowManagerGlobalLock2 = this.mService.mGlobalLock;
                synchronized (windowManagerGlobalLock2) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        if (this.mService.checkAppSwitchAllowedLocked(callingPid, callingUid, -1, -1, "Move to front")) {
                            WindowProcessController callerApp = null;
                            if (appThread != null) {
                                callerApp = this.mService.getProcessController(appThread);
                            }
                            windowManagerGlobalLock = windowManagerGlobalLock2;
                            try {
                                if (!this.mService.getActivityStartController().obtainStarter(null, "moveToFront").shouldAbortBackgroundActivityStart(callingUid, callingPid, callingPackage, -1, -1, callerApp, null, false, null) || this.mService.isBackgroundActivityStartsEnabled()) {
                                    this.mService.mStackSupervisor.startActivityFromRecents(callingPid, callingUid, this.mTaskId, null);
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    Binder.restoreCallingIdentity(origId);
                                    return;
                                }
                                WindowManagerService.resetPriorityAfterLockedSection();
                                Binder.restoreCallingIdentity(origId);
                                return;
                            } catch (Throwable th2) {
                                th = th2;
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        windowManagerGlobalLock = windowManagerGlobalLock2;
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        } else {
            String msg = "Permission Denial: moveToFront() from pid=" + Binder.getCallingPid() + " as package " + callingPackage;
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
    }

    /* JADX INFO: finally extract failed */
    public int startActivity(IBinder whoThread, String callingPackage, Intent intent, String resolvedType, Bundle bOptions) {
        TaskRecord tr;
        IApplicationThread appThread;
        checkCaller();
        int callingUser = UserHandle.getCallingUserId();
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                tr = this.mService.mRootActivityContainer.anyTaskForId(this.mTaskId, 1);
                if (tr != null) {
                    appThread = IApplicationThread.Stub.asInterface(whoThread);
                    if (appThread == null) {
                        throw new IllegalArgumentException("Bad app thread " + appThread);
                    }
                } else {
                    throw new IllegalArgumentException("Unable to find task ID " + this.mTaskId);
                }
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        return this.mService.getActivityStartController().obtainStarter(intent, TAG).setCaller(appThread).setCallingPackage(callingPackage).setResolvedType(resolvedType).setActivityOptions(bOptions).setMayWait(callingUser).setInTask(tr).execute();
    }

    public void setExcludeFromRecents(boolean exclude) {
        checkCaller();
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long origId = Binder.clearCallingIdentity();
                try {
                    TaskRecord tr = this.mService.mRootActivityContainer.anyTaskForId(this.mTaskId, 1);
                    if (tr != null) {
                        Intent intent = tr.getBaseIntent();
                        if (exclude) {
                            intent.addFlags(8388608);
                        } else {
                            intent.setFlags(intent.getFlags() & -8388609);
                        }
                    } else {
                        throw new IllegalArgumentException("Unable to find task ID " + this.mTaskId);
                    }
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }
}
