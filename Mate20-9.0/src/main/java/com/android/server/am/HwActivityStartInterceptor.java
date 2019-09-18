package com.android.server.am;

import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Binder;
import android.os.SystemProperties;
import com.android.server.pm.UserManagerService;

public final class HwActivityStartInterceptor extends ActivityStartInterceptor {
    private static final String ACTION_CONFIRM_APPLOCK_CREDENTIAL = "huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER";
    private static final String ACTION_CONFIRM_APPLOCK_PACKAGENAME = "com.huawei.systemmanager";
    public static final String EXTRA_USER_HANDLE_HWEX = "android.intent.extra.user_handle_hwex";
    private static final String KEY_BLUR_BACKGROUND = "blurBackground";
    private static final String LAUNCHER_PKGNAME = "com.huawei.android.launcher";
    private static final String WE_CHAT = "com.tencent.mm";
    private static final boolean mIsSupportGameAssist;
    private ActivityRecord mSourceRecord;

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.config.gameassist", 0) == 1) {
            z = true;
        }
        mIsSupportGameAssist = z;
    }

    public HwActivityStartInterceptor(ActivityManagerService service, ActivityStackSupervisor supervisor) {
        super(service, supervisor);
    }

    public boolean interceptStartActivityIfNeed(Intent intent) {
        if (!interceptStartVideoActivityIfNeed(intent) && !interceptStartActivityForAppLock(intent)) {
            return false;
        }
        return true;
    }

    private boolean interceptStartActivityForAppLock(Intent intent) {
        if ((this.mUserId != 0 && !UserManagerService.getInstance().getUserInfo(this.mUserId).isClonedProfile() && !UserManagerService.getInstance().getUserInfo(this.mUserId).isManagedProfile()) || this.mSupervisor.getKeyguardController().isKeyguardLocked() || !this.mSupervisor.isAppInLockList(intent.getComponent().getPackageName(), this.mUserId)) {
            return false;
        }
        if (this.mSourceRecord == null && this.mInTask == null) {
            this.mIntent.addFlags(268435456);
        }
        IIntentSender target = this.mService.getIntentSenderLocked(2, this.mCallingPackage, Binder.getCallingUid(), this.mUserId, null, null, 0, new Intent[]{this.mIntent}, new String[]{this.mResolvedType}, 1342177280, null);
        Intent newIntent = new Intent(ACTION_CONFIRM_APPLOCK_CREDENTIAL);
        newIntent.putExtra(EXTRA_USER_HANDLE_HWEX, this.mUserId);
        this.mUserId = 0;
        newIntent.setPackage("com.huawei.systemmanager");
        newIntent.setFlags(41943040);
        newIntent.putExtra("android.intent.extra.PACKAGE_NAME", this.mAInfo.packageName);
        newIntent.putExtra("android.intent.extra.INTENT", new IntentSender(target));
        if (!(!"com.huawei.android.launcher".equals(this.mCallingPackage) && (this.mIntent.getHwFlags() & 4096) == 0 && (this.mService.getFocusedStack() == null || this.mService.getFocusedStack().mResumedActivity == null || !"com.huawei.android.launcher".equals(this.mService.getFocusedStack().mResumedActivity.packageName)))) {
            newIntent.putExtra(KEY_BLUR_BACKGROUND, true);
        }
        this.mIntent = newIntent;
        this.mCallingPid = this.mRealCallingPid;
        this.mCallingUid = this.mRealCallingUid;
        this.mResolvedType = null;
        if (this.mInTask != null) {
            this.mIntent.putExtra("android.intent.extra.TASK_ID", this.mInTask.taskId);
            this.mInTask = null;
        }
        this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, this.mUserId, 0, this.mRealCallingUid);
        this.mAInfo = this.mSupervisor.resolveActivity(this.mIntent, this.mRInfo, this.mStartFlags, null);
        return true;
    }

    public void setSourceRecord(ActivityRecord sourceRecord) {
        this.mSourceRecord = sourceRecord;
    }

    private boolean interceptStartVideoActivityIfNeed(Intent intent) {
        if (!mIsSupportGameAssist) {
            return false;
        }
        HwSnsVideoManager manager = HwSnsVideoManager.getInstance(this.mService.mContext);
        if (intent.getComponent() == null || !manager.getDeferLaunchingActivitys().contains(intent.getComponent().flattenToShortString()) || !this.mService.isGameDndOn() || !manager.isGameDndOn()) {
            manager.setReadyToShowActivity(true);
            return false;
        }
        String pkgName = intent.getComponent().getPackageName();
        if (!WE_CHAT.equals(pkgName) || this.mService.getFocusedStack() == null || this.mService.getFocusedStack().mResumedActivity == null || !pkgName.equals(this.mService.getFocusedStack().mResumedActivity.packageName)) {
            manager.setActivityManager(this.mService);
            if (manager.isAttached()) {
                manager.updateFloatView(pkgName, makeIntentSender(intent));
                manager.setReadyToShowActivity(false);
            } else if (manager.getReadyToShowActivity(intent)) {
                return false;
            } else {
                if (!manager.isTransferActivity(intent)) {
                    manager.addFloatView(pkgName, makeIntentSender(intent));
                }
                manager.setReadyToShowActivity(false);
            }
            return true;
        }
        manager.setReadyToShowActivity(true);
        return false;
    }

    /* access modifiers changed from: package-private */
    public IntentSender makeIntentSender(Intent intent) {
        return new IntentSender(this.mService.getIntentSenderLocked(2, this.mCallingPackage, this.mCallingUid, this.mUserId, null, null, 0, new Intent[]{intent}, new String[]{this.mResolvedType}, 1409286144, null));
    }
}
