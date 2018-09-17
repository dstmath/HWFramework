package com.huawei.android.pushagent.a;

import android.content.Context;
import com.huawei.android.pushagent.a.a.a;
import com.huawei.android.pushagent.utils.d.c;

final class b implements Runnable {
    final /* synthetic */ Context ib;

    b(Context context) {
        this.ib = context;
    }

    public void run() {
        if (this.ib == null) {
            c.sf("PushLog2951", "init reporter failed, context is null");
            return;
        }
        a.appCtx = this.ib.getApplicationContext();
        a.ia = new a();
        a.ia.xr(this.ib);
    }
}
