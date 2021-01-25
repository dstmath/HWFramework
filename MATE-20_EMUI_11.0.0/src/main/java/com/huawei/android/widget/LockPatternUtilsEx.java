package com.huawei.android.widget;

import android.content.Context;
import huawei.com.android.internal.widget.HwLockPatternUtils;

public class LockPatternUtilsEx extends HwLockPatternUtils {
    public LockPatternUtilsEx(Context context) {
        super(context);
    }

    public boolean saveLockPassword(byte[] password, byte[] savedPassword, int requestedQuality, int userHandle, boolean isAllowUntrustedChange, boolean hasFixed) {
        return LockPatternUtilsEx.super.saveLockPassword(password, savedPassword, requestedQuality, userHandle, isAllowUntrustedChange, hasFixed);
    }
}
