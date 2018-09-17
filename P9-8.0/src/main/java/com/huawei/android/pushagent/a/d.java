package com.huawei.android.pushagent.a;

import com.huawei.android.pushagent.utils.d.c;

final class d implements Runnable {
    final /* synthetic */ int id;
    final /* synthetic */ String ie;

    d(int i, String str) {
        this.id = i;
        this.ie = str;
    }

    public void run() {
        if (a.appCtx == null || a.ia == null) {
            c.sf("PushLog2951", "Please init reporter first");
        } else if (a.yb(this.id)) {
            a.ia.xs(String.valueOf(this.id), this.ie);
        }
    }
}
