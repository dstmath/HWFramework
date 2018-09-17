package com.huawei.android.pushagent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.utils.d.c;

class a extends BroadcastReceiver {
    final /* synthetic */ PushService iq;

    /* synthetic */ a(PushService pushService, a aVar) {
        this(pushService);
    }

    private a(PushService pushService) {
        this.iq = pushService;
    }

    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            c.sf(PushService.TAG, "context== null or intent == null");
            return;
        }
        try {
            c.sh(PushService.TAG, "action is " + intent.getAction());
            PushService.yx(intent);
        } catch (Throwable e) {
            c.se(PushService.TAG, "call PushInnerReceiver:onReceive cause " + e.toString(), e);
        }
    }
}
