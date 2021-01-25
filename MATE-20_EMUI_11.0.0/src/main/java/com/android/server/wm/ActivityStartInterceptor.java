package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManagerInternal;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.HwPCUtils;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.HarmfulAppWarningActivity;
import com.android.internal.app.SuspendedAppActivity;
import com.android.internal.app.UnlaunchableAppActivity;
import com.android.server.LocalServices;

public class ActivityStartInterceptor extends AbsActivityStartInterceptor {
    ActivityInfo mAInfo;
    ActivityOptions mActivityOptions;
    protected String mCallingPackage;
    int mCallingPid;
    int mCallingUid;
    TaskRecord mInTask;
    Intent mIntent;
    ResolveInfo mRInfo;
    protected int mRealCallingPid;
    protected int mRealCallingUid;
    String mResolvedType;
    private final RootActivityContainer mRootActivityContainer;
    protected final ActivityTaskManagerService mService;
    private final Context mServiceContext;
    protected int mStartFlags;
    protected final ActivityStackSupervisor mSupervisor;
    protected int mUserId;
    private UserManager mUserManager;

    public ActivityStartInterceptor(ActivityTaskManagerService service, ActivityStackSupervisor supervisor) {
        this(service, supervisor, service.mRootActivityContainer, service.mContext);
    }

    @VisibleForTesting
    ActivityStartInterceptor(ActivityTaskManagerService service, ActivityStackSupervisor supervisor, RootActivityContainer root, Context context) {
        this.mService = service;
        this.mSupervisor = supervisor;
        this.mRootActivityContainer = root;
        this.mServiceContext = context;
    }

    /* access modifiers changed from: package-private */
    public void setStates(int userId, int realCallingPid, int realCallingUid, int startFlags, String callingPackage) {
        this.mRealCallingPid = realCallingPid;
        this.mRealCallingUid = realCallingUid;
        this.mUserId = userId;
        this.mStartFlags = startFlags;
        this.mCallingPackage = callingPackage;
    }

    private IntentSender createIntentSenderForOriginalIntent(int callingUid, int flags) {
        ActivityOptions activityOptions;
        Bundle activityOptions2 = deferCrossProfileAppsAnimationIfNecessary();
        if (HwPCUtils.isPcCastModeInServer() && (activityOptions = this.mActivityOptions) != null && HwPCUtils.isValidExtDisplayId(activityOptions.getLaunchDisplayId())) {
            ActivityOptions aOs = ActivityOptions.makeBasic();
            aOs.setLaunchDisplayId(HwPCUtils.getPCDisplayID());
            aOs.setLaunchWindowingMode(10);
            activityOptions2 = aOs.toBundle();
        }
        return new IntentSender(this.mService.getIntentSenderLocked(2, this.mCallingPackage, callingUid, this.mUserId, null, null, 0, new Intent[]{this.mIntent}, new String[]{this.mResolvedType}, flags, activityOptions2));
    }

    /* access modifiers changed from: package-private */
    public boolean intercept(Intent intent, ResolveInfo rInfo, ActivityInfo aInfo, String resolvedType, TaskRecord inTask, int callingPid, int callingUid, ActivityOptions activityOptions) {
        this.mUserManager = UserManager.get(this.mServiceContext);
        this.mIntent = intent;
        this.mCallingPid = callingPid;
        this.mCallingUid = callingUid;
        this.mRInfo = rInfo;
        this.mAInfo = aInfo;
        this.mResolvedType = resolvedType;
        this.mInTask = inTask;
        this.mActivityOptions = activityOptions;
        if (!interceptStartActivityIfNeed(intent, activityOptions) && !interceptSuspendedPackageIfNeeded() && !interceptQuietProfileIfNeeded() && !interceptHarmfulAppIfNeeded()) {
            return interceptWorkProfileChallengeIfNeeded();
        }
        return true;
    }

    private Bundle deferCrossProfileAppsAnimationIfNecessary() {
        ActivityOptions activityOptions = this.mActivityOptions;
        if (activityOptions == null || activityOptions.getAnimationType() != 12) {
            return null;
        }
        this.mActivityOptions = null;
        return ActivityOptions.makeOpenCrossProfileAppsAnimation().toBundle();
    }

    private boolean interceptQuietProfileIfNeeded() {
        if (!this.mUserManager.isQuietModeEnabled(UserHandle.of(this.mUserId))) {
            return false;
        }
        this.mIntent = UnlaunchableAppActivity.createInQuietModeDialogIntent(this.mUserId, createIntentSenderForOriginalIntent(this.mCallingUid, 1342177280));
        this.mCallingPid = this.mRealCallingPid;
        this.mCallingUid = this.mRealCallingUid;
        this.mResolvedType = null;
        this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, this.mUserManager.getProfileParent(this.mUserId).id, 0, this.mRealCallingUid);
        this.mAInfo = this.mSupervisor.resolveActivity(this.mIntent, this.mRInfo, this.mStartFlags, null);
        return true;
    }

    private boolean interceptSuspendedByAdminPackage() {
        DevicePolicyManagerInternal devicePolicyManager = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        if (devicePolicyManager == null) {
            return false;
        }
        this.mIntent = devicePolicyManager.createShowAdminSupportIntent(this.mUserId, true);
        this.mIntent.putExtra("android.app.extra.RESTRICTION", "policy_suspend_packages");
        this.mCallingPid = this.mRealCallingPid;
        this.mCallingUid = this.mRealCallingUid;
        this.mResolvedType = null;
        UserInfo parent = this.mUserManager.getProfileParent(this.mUserId);
        if (parent != null) {
            this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, parent.id, 0, this.mRealCallingUid);
        } else {
            this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, this.mUserId, 0, this.mRealCallingUid);
        }
        this.mAInfo = this.mSupervisor.resolveActivity(this.mIntent, this.mRInfo, this.mStartFlags, null);
        return true;
    }

    private boolean interceptSuspendedPackageIfNeeded() {
        PackageManagerInternal pmi;
        ActivityInfo activityInfo = this.mAInfo;
        if (activityInfo == null || activityInfo.applicationInfo == null || (this.mAInfo.applicationInfo.flags & 1073741824) == 0 || (pmi = this.mService.getPackageManagerInternalLocked()) == null) {
            return false;
        }
        String suspendedPackage = this.mAInfo.applicationInfo.packageName;
        String suspendingPackage = pmi.getSuspendingPackage(suspendedPackage, this.mUserId);
        if ("android".equals(suspendingPackage)) {
            return interceptSuspendedByAdminPackage();
        }
        this.mIntent = SuspendedAppActivity.createSuspendedAppInterceptIntent(suspendedPackage, suspendingPackage, pmi.getSuspendedDialogInfo(suspendedPackage, this.mUserId), this.mUserId);
        this.mCallingPid = this.mRealCallingPid;
        int i = this.mRealCallingUid;
        this.mCallingUid = i;
        this.mResolvedType = null;
        this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, this.mUserId, 0, i);
        this.mAInfo = this.mSupervisor.resolveActivity(this.mIntent, this.mRInfo, this.mStartFlags, null);
        return true;
    }

    private boolean interceptWorkProfileChallengeIfNeeded() {
        Intent interceptingIntent = interceptWithConfirmCredentialsIfNeeded(this.mAInfo, this.mUserId);
        if (interceptingIntent == null) {
            return false;
        }
        this.mIntent = interceptingIntent;
        this.mCallingPid = this.mRealCallingPid;
        this.mCallingUid = this.mRealCallingUid;
        this.mResolvedType = null;
        TaskRecord taskRecord = this.mInTask;
        if (taskRecord != null) {
            this.mIntent.putExtra("android.intent.extra.TASK_ID", taskRecord.taskId);
            this.mInTask = null;
        }
        if (this.mActivityOptions == null) {
            this.mActivityOptions = ActivityOptions.makeBasic();
        }
        this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, this.mUserManager.getProfileParent(this.mUserId).id, 0, this.mRealCallingUid);
        this.mAInfo = this.mSupervisor.resolveActivity(this.mIntent, this.mRInfo, this.mStartFlags, null);
        return true;
    }

    private Intent interceptWithConfirmCredentialsIfNeeded(ActivityInfo aInfo, int userId) {
        if (!this.mService.mAmInternal.shouldConfirmCredentials(userId)) {
            return null;
        }
        IntentSender target = createIntentSenderForOriginalIntent(this.mCallingUid, 1409286144);
        Intent newIntent = ((KeyguardManager) this.mServiceContext.getSystemService("keyguard")).createConfirmDeviceCredentialIntent(null, null, userId);
        if (newIntent == null) {
            return null;
        }
        newIntent.setFlags(276840448);
        newIntent.putExtra("android.intent.extra.PACKAGE_NAME", aInfo.packageName);
        newIntent.putExtra("android.intent.extra.INTENT", target);
        return newIntent;
    }

    private boolean interceptHarmfulAppIfNeeded() {
        try {
            CharSequence harmfulAppWarning = this.mService.getPackageManager().getHarmfulAppWarning(this.mAInfo.packageName, this.mUserId);
            if (harmfulAppWarning == null) {
                return false;
            }
            this.mIntent = HarmfulAppWarningActivity.createHarmfulAppWarningIntent(this.mServiceContext, this.mAInfo.packageName, createIntentSenderForOriginalIntent(this.mCallingUid, 1409286144), harmfulAppWarning);
            this.mCallingPid = this.mRealCallingPid;
            int i = this.mRealCallingUid;
            this.mCallingUid = i;
            this.mResolvedType = null;
            this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, this.mUserId, 0, i);
            this.mAInfo = this.mSupervisor.resolveActivity(this.mIntent, this.mRInfo, this.mStartFlags, null);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }
}
