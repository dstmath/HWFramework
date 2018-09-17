package com.huawei.android.app;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;

public class PendingIntentEx {
    public static PendingIntent getActivityAsUser(Context context, int requestCode, Intent intent, int flags, Bundle options, UserHandle user) {
        return PendingIntent.getActivityAsUser(context, requestCode, intent, flags, options, user);
    }

    public static PendingIntent getBroadcastAsUser(Context context, int requestCode, Intent intent, int flags, UserHandle userHandle) {
        return PendingIntent.getBroadcastAsUser(context, requestCode, intent, flags, userHandle);
    }
}
