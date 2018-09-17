package com.huawei.android.pushagent.model.c;

import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.model.channel.a;

public class c implements e {
    private static String TAG = "PushLog2951";

    public c(Context context) {
    }

    public void onReceive(Context context, Intent intent) {
        com.huawei.android.pushagent.utils.d.c.sg(TAG, "enter ChannelStatusReceiver:onReceive");
        if ("com.huawei.android.push.intent.GET_PUSH_STATE".equals(intent.getAction())) {
            boolean gc = a.hk().gc();
            String stringExtra = intent.getStringExtra("pkg_name");
            com.huawei.android.pushagent.utils.d.c.sh(TAG, "packageName: " + stringExtra + " get push status, current push state is:" + gc);
            jo(context, gc, stringExtra);
        }
    }

    private static void jo(Context context, boolean z, String str) {
        Intent intent = new Intent();
        com.huawei.android.pushagent.utils.d.c.sg(TAG, "sendStateBroadcast the current push state is: " + z);
        intent.setAction("com.huawei.intent.action.PUSH_STATE").putExtra("push_state", z).setFlags(32).setPackage(str);
        context.sendBroadcast(intent);
    }
}
