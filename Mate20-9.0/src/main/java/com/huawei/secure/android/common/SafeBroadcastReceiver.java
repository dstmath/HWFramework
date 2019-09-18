package com.huawei.secure.android.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

@Deprecated
public abstract class SafeBroadcastReceiver extends BroadcastReceiver {
    @Deprecated
    public abstract void onReceiveMsg(Context context, Intent intent);

    @Deprecated
    public final void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            onReceiveMsg(context, new SafeIntent(intent));
        }
    }
}
