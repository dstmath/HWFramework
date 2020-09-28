package com.huawei.systemmanager.notificationmanager.restrictedlock;

import android.content.Context;
import com.android.internal.widget.LockPatternUtils;

public class HwLockPatternUtilsEx {
    private LockPatternUtils mInnerLockPatternUtils;

    public HwLockPatternUtilsEx(Context context) {
        this.mInnerLockPatternUtils = new LockPatternUtils(context);
    }

    public boolean isSeparateProfileChallengeEnabled(int userHandle) {
        return this.mInnerLockPatternUtils.isSeparateProfileChallengeEnabled(userHandle);
    }

    public boolean isLockScreenDisabled(int userId) {
        return this.mInnerLockPatternUtils.isLockScreenDisabled(userId);
    }

    public void setLockScreenDisabled(boolean disable, int userId) {
        this.mInnerLockPatternUtils.setLockScreenDisabled(disable, userId);
    }

    public boolean getPowerButtonInstantlyLocks(int userId) {
        return this.mInnerLockPatternUtils.getPowerButtonInstantlyLocks(userId);
    }

    public void setPowerButtonInstantlyLocks(boolean enabled, int userId) {
        this.mInnerLockPatternUtils.setPowerButtonInstantlyLocks(enabled, userId);
    }
}
