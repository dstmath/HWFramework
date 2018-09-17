package com.huawei.android.pushagent;

import android.content.Intent;
import com.huawei.android.pushagent.model.c.e;

final class c implements Runnable {
    private Intent iw;
    private e ix;
    final /* synthetic */ b iy;

    /* synthetic */ c(b bVar, e eVar, Intent intent, c cVar) {
        this(bVar, eVar, intent);
    }

    private c(b bVar, e eVar, Intent intent) {
        this.iy = bVar;
        this.ix = eVar;
        this.iw = intent;
    }

    public void run() {
        try {
            this.ix.onReceive(this.iy.it, this.iw);
        } catch (Throwable e) {
            com.huawei.android.pushagent.utils.d.c.se("PushLog2951", "ReceiverDispatcher: call Receiver:" + this.ix.getClass().getSimpleName() + ", intent:" + this.iw + " failed:" + e.toString(), e);
        }
    }
}
