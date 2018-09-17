package com.huawei.android.pushselfshow.richpush.html.api;

import com.huawei.android.pushagent.a.a.c;

class a implements Runnable {
    final /* synthetic */ OnlineEventsBridgeMode a;

    a(OnlineEventsBridgeMode onlineEventsBridgeMode) {
        this.a = onlineEventsBridgeMode;
    }

    public void run() {
        boolean z = false;
        boolean a = NativeToJsMessageQueue.this.d();
        c.a("PushSelfShowLog", "bEmptyMsg is " + a);
        if (!a) {
            OnlineEventsBridgeMode onlineEventsBridgeMode = this.a;
            if (!this.a.a) {
                z = true;
            }
            onlineEventsBridgeMode.a = z;
            NativeToJsMessageQueue.this.a.setNetworkAvailable(this.a.a);
            c.a("PushSelfShowLog", "setNetworkAvailable ： " + this.a.a);
        }
    }
}
