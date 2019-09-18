package com.huawei.demo.android;

import android.content.Context;
import com.android.internal.widget.LockPatternUtils;
import com.huawei.demo.HwDemoUtils;

public class HwDemoLockPatternUtilsEx {
    private LockPatternUtils mLockPatternUtils;

    public HwDemoLockPatternUtilsEx(Context context) {
        this.mLockPatternUtils = new LockPatternUtils(context);
    }

    public void setLockScreenDisabled(boolean disable, int userId) {
        if (HwDemoUtils.isDemoVersion()) {
            this.mLockPatternUtils.setLockScreenDisabled(disable, userId);
        }
    }

    public boolean isSecure(int userId) {
        return this.mLockPatternUtils.isSecure(userId);
    }

    public void clearLock(String savedCredential, int userHandle) {
        if (HwDemoUtils.isDemoVersion()) {
            this.mLockPatternUtils.clearLock(savedCredential, userHandle);
        }
    }
}
