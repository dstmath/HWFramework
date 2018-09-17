package com.android.server.am;

import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Binder;
import com.android.server.pm.UserManagerService;

public final class HwActivityStartInterceptor extends ActivityStartInterceptor {
    private static final String ACTION_CONFIRM_APPLOCK_CREDENTIAL = "huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER";
    private static final String ACTION_CONFIRM_APPLOCK_PACKAGENAME = "com.huawei.systemmanager";
    private ActivityRecord mSourceRecord;

    public HwActivityStartInterceptor(ActivityManagerService _service, ActivityStackSupervisor _supervisor) {
        super(_service, _supervisor);
    }

    public boolean interceptStartActivityIfNeed(Intent intent) {
        if (this.mUserId != 0 && (UserManagerService.getInstance().getUserInfo(this.mUserId).isClonedProfile() ^ 1) != 0) {
            return false;
        }
        if (this.mSupervisor.mKeyguardController.isKeyguardLocked()) {
            return false;
        }
        if (!this.mSupervisor.isAppInLockList(intent.getComponent().getPackageName())) {
            return false;
        }
        if (this.mSourceRecord == null && this.mInTask == null) {
            this.mIntent.addFlags(268435456);
        }
        this.mIntent.setCallingUid(this.mRealCallingUid);
        IIntentSender target = this.mService.getIntentSenderLocked(2, this.mCallingPackage, Binder.getCallingUid(), this.mUserId, null, null, 0, new Intent[]{this.mIntent}, new String[]{this.mResolvedType}, 1342177280, null);
        Intent newIntent = new Intent(ACTION_CONFIRM_APPLOCK_CREDENTIAL);
        this.mUserId = 0;
        newIntent.setPackage(ACTION_CONFIRM_APPLOCK_PACKAGENAME);
        newIntent.setFlags(41943040);
        newIntent.putExtra("android.intent.extra.PACKAGE_NAME", this.mAInfo.packageName);
        newIntent.putExtra("android.intent.extra.INTENT", new IntentSender(target));
        this.mIntent = newIntent;
        this.mCallingPid = this.mRealCallingPid;
        this.mCallingUid = this.mRealCallingUid;
        this.mResolvedType = null;
        if (this.mInTask != null) {
            this.mIntent.putExtra("android.intent.extra.TASK_ID", this.mInTask.taskId);
            this.mInTask = null;
        }
        this.mRInfo = this.mSupervisor.resolveIntent(this.mIntent, this.mResolvedType, this.mUserId);
        this.mAInfo = this.mSupervisor.resolveActivity(this.mIntent, this.mRInfo, this.mStartFlags, null);
        return true;
    }

    public void setSourceRecord(ActivityRecord sourceRecord) {
        this.mSourceRecord = sourceRecord;
    }
}
