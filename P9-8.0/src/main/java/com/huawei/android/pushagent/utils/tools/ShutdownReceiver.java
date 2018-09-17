package com.huawei.android.pushagent.utils.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShutdownReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null) {
            if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction())) {
                new c(context).start();
            }
        }
    }
}
