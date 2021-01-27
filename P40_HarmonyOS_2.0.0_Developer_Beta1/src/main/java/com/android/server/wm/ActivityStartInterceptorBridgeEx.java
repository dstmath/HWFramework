package com.android.server.wm;

import android.app.ActivityOptions;
import android.content.IIntentSenderEx;
import android.content.Intent;
import android.content.IntentSenderEx;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;

public class ActivityStartInterceptorBridgeEx {
    public static final String ACTION_CONFIRM_APPLOCK_CREDENTIAL = "huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER";
    public static final String ACTION_CONFIRM_APPLOCK_CREDENTIAL_OPAQUE = "huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER_LOCKSCREEN";
    public static final String ACTION_CONFIRM_APPLOCK_PACKAGENAME = "com.huawei.systemmanager";
    public static final String APP_LOCK = "com.huawei.systemmanager/com.huawei.securitycenter.applock.password.AuthLaunchLockedAppActivity";
    public static final String APP_OPAQUE_LOCK = "com.huawei.systemmanager/com.huawei.securitycenter.applock.password.LockScreenLaunchLockedAppActivity";
    private ActivityStartInterceptorBridge mActivityStartInterceptor;
    protected final ActivityTaskManagerServiceEx mService = this.mActivityStartInterceptor.getService();
    protected final ActivityStackSupervisorEx mSupervisor = this.mActivityStartInterceptor.getSupervisor();

    public ActivityStartInterceptorBridgeEx(ActivityTaskManagerServiceEx service, ActivityStackSupervisorEx supervisor) {
        this.mActivityStartInterceptor = new ActivityStartInterceptorBridge(service.getActivityTaskManagerService(), supervisor.getActivityStackSupervisor());
        this.mActivityStartInterceptor.setActivityStartInterceptorBridgeEx(this);
    }

    public ActivityStartInterceptorBridge getHwActivityStartInterceptor() {
        return this.mActivityStartInterceptor;
    }

    public int getUserId() {
        return this.mActivityStartInterceptor.mUserId;
    }

    public void setUserId(int userId) {
        this.mActivityStartInterceptor.mUserId = userId;
    }

    public Intent getIntent() {
        return this.mActivityStartInterceptor.mIntent;
    }

    public void setIntent(Intent intent) {
        this.mActivityStartInterceptor.mIntent = intent;
    }

    public Intent updateIntent(Intent intent, String flag, IntentSenderEx intentSenderEx) {
        if (intent == null || intentSenderEx == null) {
            return null;
        }
        return intent.putExtra(flag, intentSenderEx.getIntentSender());
    }

    public TaskRecordEx getInTask() {
        return this.mActivityStartInterceptor.getInTask();
    }

    public void setInTask(TaskRecordEx taskRecord) {
        this.mActivityStartInterceptor.setInTask(taskRecord);
    }

    public String getCallingPackage() {
        return this.mActivityStartInterceptor.mCallingPackage;
    }

    public int getCallingPid() {
        return this.mActivityStartInterceptor.mCallingPid;
    }

    public void setCallingPid(int callingPid) {
        this.mActivityStartInterceptor.mCallingPid = callingPid;
    }

    public int getCallingUid() {
        return this.mActivityStartInterceptor.mCallingUid;
    }

    public void setCallingUid(int callingUid) {
        this.mActivityStartInterceptor.mCallingUid = callingUid;
    }

    public String getResolvedType() {
        return this.mActivityStartInterceptor.mResolvedType;
    }

    public void setResolvedType(String resolvedType) {
        this.mActivityStartInterceptor.mResolvedType = resolvedType;
    }

    public int getRealCallingPid() {
        return this.mActivityStartInterceptor.mRealCallingPid;
    }

    public int getRealCallingUid() {
        return this.mActivityStartInterceptor.mRealCallingUid;
    }

    public int getStartFlags() {
        return this.mActivityStartInterceptor.mStartFlags;
    }

    public ResolveInfo getResolveInfo() {
        return this.mActivityStartInterceptor.mRInfo;
    }

    public void setResolveInfo(ResolveInfo resolveInfo) {
        this.mActivityStartInterceptor.mRInfo = resolveInfo;
    }

    public ActivityInfo getActivityInfo() {
        return this.mActivityStartInterceptor.mAInfo;
    }

    public void setActivityInfo(ActivityInfo activityInfo) {
        this.mActivityStartInterceptor.mAInfo = activityInfo;
    }

    public boolean interceptStartActivityIfNeed(Intent intent, ActivityOptions activityOptions) {
        return false;
    }

    public void setSourceRecord(ActivityRecordEx sourceRecord) {
    }

    public static boolean isAppLockActivity(String activity) {
        return false;
    }

    public static boolean isAppLockPackageName(String name) {
        return false;
    }

    public static boolean isAppLockAction(String action) {
        return false;
    }

    /* access modifiers changed from: package-private */
    public IIntentSenderEx makeIntentSender(Intent intent) {
        return null;
    }

    public static boolean isAppLockActionEx(String action) {
        return "huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER".equals(action) || "huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER_LOCKSCREEN".equals(action);
    }

    public static boolean isAppLockActivityEx(String activity) {
        return APP_LOCK.equals(activity) || APP_OPAQUE_LOCK.equals(activity);
    }

    public static boolean isAppLockPackageNameEx(String packageName) {
        return "com.huawei.systemmanager".equals(packageName);
    }
}
