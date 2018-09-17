package com.huawei.android.widget;

import android.content.Context;
import com.android.internal.widget.LockPatternUtils;

public class HwLockPatternUtilsEx {
    private LockPatternUtils mLockPatternUtils;

    public HwLockPatternUtilsEx(Context context) {
        this.mLockPatternUtils = new LockPatternUtils(context);
    }

    public int getKeyguardStoredPasswordQuality(int userHandle) {
        return this.mLockPatternUtils.getKeyguardStoredPasswordQuality(userHandle);
    }
}
