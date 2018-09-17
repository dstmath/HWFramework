package com.huawei.android.app.admin;

import android.content.ComponentName;
import com.huawei.android.util.NoExtAPIException;

public class DevicePolicyManagerCustEx {
    public static void setAllowSimplePassword(ComponentName admin, boolean mode) {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean getAllowSimplePassword(ComponentName admin) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void saveCurrentPwdStatus(boolean isCurrentPwdSimple) {
        throw new NoExtAPIException("method not supported.");
    }
}
