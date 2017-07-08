package com.android.server.am;

import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManagerInternal;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.internal.app.UnlaunchableAppActivity;
import com.android.server.LocalServices;

class ActivityStartInterceptor {
    ActivityInfo mAInfo;
    ActivityOptions mActivityOptions;
    private String mCallingPackage;
    int mCallingPid;
    int mCallingUid;
    TaskRecord mInTask;
    Intent mIntent;
    ResolveInfo mRInfo;
    private int mRealCallingPid;
    private int mRealCallingUid;
    String mResolvedType;
    private final ActivityManagerService mService;
    private int mStartFlags;
    private final ActivityStackSupervisor mSupervisor;
    private int mUserId;
    private UserManager mUserManager;

    ActivityStartInterceptor(ActivityManagerService service, ActivityStackSupervisor supervisor) {
        this.mService = service;
        this.mSupervisor = supervisor;
    }

    void setStates(int userId, int realCallingPid, int realCallingUid, int startFlags, String callingPackage) {
        this.mRealCallingPid = realCallingPid;
        this.mRealCallingUid = realCallingUid;
        this.mUserId = userId;
        this.mStartFlags = startFlags;
        this.mCallingPackage = callingPackage;
    }

    void intercept(Intent intent, ResolveInfo rInfo, ActivityInfo aInfo, String resolvedType, TaskRecord inTask, int callingPid, int callingUid, ActivityOptions activityOptions) {
        this.mUserManager = UserManager.get(this.mService.mContext);
        this.mIntent = intent;
        this.mCallingPid = callingPid;
        this.mCallingUid = callingUid;
        this.mRInfo = rInfo;
        this.mAInfo = aInfo;
        this.mResolvedType = resolvedType;
        this.mInTask = inTask;
        this.mActivityOptions = activityOptions;
        if (!interceptSuspendPackageIfNeed() && !interceptQuietProfileIfNeeded()) {
            interceptWorkProfileChallengeIfNeeded();
        }
    }

    private boolean interceptQuietProfileIfNeeded() {
        if (!this.mUserManager.isQuietModeEnabled(UserHandle.of(this.mUserId))) {
            return false;
        }
        this.mIntent = UnlaunchableAppActivity.createInQuietModeDialogIntent(this.mUserId, new IntentSender(this.mService.getIntentSenderLocked(2, this.mCallingPackage, this.mCallingUid, this.mUserId, null, null, 0, new Intent[]{this.mIntent}, new String[]{this.mResolvedType}, 1342177280, null)));
        this.mCallingPid = this.mRealCallingPid;
        this.mCallingUid = this.mRealCallingUid;
        this.mResolvedType = null;
        this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, this.mUserManager.getProfileParent(this.mUserId).id);
        this.mAInfo = this.mSupervisor.resolveActivity(this.mIntent, this.mRInfo, this.mStartFlags, null);
        return true;
    }

    private boolean interceptSuspendPackageIfNeed() {
        if (this.mAInfo == null || this.mAInfo.applicationInfo == null || (this.mAInfo.applicationInfo.flags & 1073741824) == 0) {
            return false;
        }
        DevicePolicyManagerInternal devicePolicyManager = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        if (devicePolicyManager == null) {
            return false;
        }
        this.mIntent = devicePolicyManager.createPackageSuspendedDialogIntent(this.mAInfo.packageName, this.mUserId);
        this.mCallingPid = this.mRealCallingPid;
        this.mCallingUid = this.mRealCallingUid;
        this.mResolvedType = null;
        UserInfo parent = this.mUserManager.getProfileParent(this.mUserId);
        if (parent != null) {
            this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, parent.id);
        } else {
            this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, this.mUserId);
        }
        this.mAInfo = this.mSupervisor.resolveActivity(this.mIntent, this.mRInfo, this.mStartFlags, null);
        return true;
    }

    private boolean interceptWorkProfileChallengeIfNeeded() {
        Intent interceptingIntent = interceptWithConfirmCredentialsIfNeeded(this.mIntent, this.mResolvedType, this.mAInfo, this.mCallingPackage, this.mUserId);
        if (interceptingIntent == null) {
            return false;
        }
        this.mIntent = interceptingIntent;
        this.mCallingPid = this.mRealCallingPid;
        this.mCallingUid = this.mRealCallingUid;
        this.mResolvedType = null;
        if (this.mInTask != null) {
            this.mIntent.putExtra("android.intent.extra.TASK_ID", this.mInTask.taskId);
            this.mInTask = null;
        }
        if (this.mActivityOptions == null) {
            this.mActivityOptions = ActivityOptions.makeBasic();
        }
        ActivityRecord homeActivityRecord = this.mSupervisor.getHomeActivity();
        if (!(homeActivityRecord == null || homeActivityRecord.task == null)) {
            this.mActivityOptions.setLaunchTaskId(homeActivityRecord.task.taskId);
        }
        this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, this.mUserManager.getProfileParent(this.mUserId).id);
        this.mAInfo = this.mSupervisor.resolveActivity(this.mIntent, this.mRInfo, this.mStartFlags, null);
        return true;
    }

    private Intent interceptWithConfirmCredentialsIfNeeded(Intent intent, String resolvedType, ActivityInfo aInfo, String callingPackage, int userId) {
        if (!this.mService.mUserController.shouldConfirmCredentials(userId)) {
            return null;
        }
        IIntentSender target = this.mService.getIntentSenderLocked(2, callingPackage, Binder.getCallingUid(), userId, null, null, 0, new Intent[]{intent}, new String[]{resolvedType}, 1409286144, null);
        Intent newIntent = ((KeyguardManager) this.mService.mContext.getSystemService("keyguard")).createConfirmDeviceCredentialIntent(null, null, userId);
        if (newIntent == null) {
            return null;
        }
        newIntent.setFlags(276840448);
        newIntent.putExtra("android.intent.extra.PACKAGE_NAME", aInfo.packageName);
        newIntent.putExtra("android.intent.extra.INTENT", new IntentSender(target));
        return newIntent;
    }
}
