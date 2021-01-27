package com.huawei.android.internal.widget;

import android.content.Context;
import com.android.internal.widget.LockPatternUtils;

public class LockPatternUtilsEx {
    private LockPatternUtils mlockpatternutils;

    public LockPatternUtilsEx(Context context) {
        this.mlockpatternutils = new LockPatternUtils(context);
    }

    public boolean isLockScreenDisabled(int userId) {
        return this.mlockpatternutils.isLockScreenDisabled(userId);
    }
}
