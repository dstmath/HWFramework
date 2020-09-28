package com.huawei.android.app;

import android.app.ActivityManagerNative;
import android.content.Intent;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ActivityManagerNativeEx {
    public static void broadcastStickyIntent(Intent intent, String permission, int userId) {
        ActivityManagerNative.broadcastStickyIntent(intent, permission, userId);
    }
}
