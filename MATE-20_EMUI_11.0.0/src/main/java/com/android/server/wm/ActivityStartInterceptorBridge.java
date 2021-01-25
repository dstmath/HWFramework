package com.android.server.wm;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.IntentSender;

public class ActivityStartInterceptorBridge extends ActivityStartInterceptor {
    public static final String ACTION_CONFIRM_APPLOCK_CREDENTIAL = "huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER";
    public static final String ACTION_CONFIRM_APPLOCK_CREDENTIAL_OPAQUE = "huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER_LOCKSCREEN";
    public static final String ACTION_CONFIRM_APPLOCK_PACKAGENAME = "com.huawei.systemmanager";
    private ActivityStartInterceptorBridgeEx mActivityStartInterceptorBridgeEx;

    public ActivityStartInterceptorBridge(ActivityTaskManagerService service, ActivityStackSupervisor supervisor) {
        super(service, supervisor);
    }

    public static boolean isAppLockActivity(String activity) {
        return ActivityStartInterceptorBridgeEx.isAppLockActivityEx(activity);
    }

    public static boolean isAppLockPackageName(String packageName) {
        return ActivityStartInterceptorBridgeEx.isAppLockPackageNameEx(packageName);
    }

    public static boolean isAppLockAction(String action) {
        return ActivityStartInterceptorBridgeEx.isAppLockActionEx(action);
    }

    public static String getAppLock() {
        return ActivityStartInterceptorBridgeEx.APP_LOCK;
    }

    public static String getAppOpaqueLock() {
        return ActivityStartInterceptorBridgeEx.APP_OPAQUE_LOCK;
    }

    public void setActivityStartInterceptorBridgeEx(ActivityStartInterceptorBridgeEx activityStartInterceptorEx) {
        this.mActivityStartInterceptorBridgeEx = activityStartInterceptorEx;
    }

    public ActivityStartInterceptorBridgeEx getActivityStartInterceptorBridgeEx() {
        return this.mActivityStartInterceptorBridgeEx;
    }

    public ActivityTaskManagerServiceEx getService() {
        ActivityTaskManagerServiceEx activityTaskManagerServiceEx = new ActivityTaskManagerServiceEx();
        activityTaskManagerServiceEx.setActivityTaskManagerService(this.mService);
        return activityTaskManagerServiceEx;
    }

    public ActivityStackSupervisorEx getSupervisor() {
        ActivityStackSupervisorEx activityStackSupervisorEx = new ActivityStackSupervisorEx();
        activityStackSupervisorEx.setActivityStackSupervisor(this.mSupervisor);
        return activityStackSupervisorEx;
    }

    public TaskRecordEx getInTask() {
        TaskRecordEx taskRecordEx = new TaskRecordEx();
        if (this.mInTask != null) {
            taskRecordEx.setTaskRecord(this.mInTask);
        }
        return taskRecordEx;
    }

    public void setInTask(TaskRecordEx taskRecord) {
        if (taskRecord != null) {
            this.mInTask = taskRecord.getTaskRecord();
        } else {
            this.mInTask = null;
        }
    }

    public boolean interceptStartActivityIfNeed(Intent intent, ActivityOptions activityOptions) {
        return this.mActivityStartInterceptorBridgeEx.interceptStartActivityIfNeed(intent, activityOptions);
    }

    public void setSourceRecord(ActivityRecord sourceRecord) {
        ActivityRecordEx activityRecordEx = new ActivityRecordEx();
        if (sourceRecord != null) {
            activityRecordEx.setActivityRecord(sourceRecord);
        }
        this.mActivityStartInterceptorBridgeEx.setSourceRecord(activityRecordEx);
    }

    /* access modifiers changed from: package-private */
    public IntentSender makeIntentSender(Intent intent) {
        return this.mActivityStartInterceptorBridgeEx.makeIntentSender(intent);
    }
}
