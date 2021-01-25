package com.huawei.android.telecom;

import android.content.Context;
import android.telecom.DefaultDialerManager;

public class DefaultDialerManagerEx {
    public static boolean isDefaultOrSystemDialer(Context context, String packageName) {
        return DefaultDialerManager.isDefaultOrSystemDialer(context, packageName);
    }
}
