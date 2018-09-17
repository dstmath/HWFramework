package com.huawei.android.pushagent.a;

final class c implements Runnable {
    final /* synthetic */ boolean ic;

    c(boolean z) {
        this.ic = z;
    }

    public void run() {
        if (a.appCtx == null || a.ia == null) {
            com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "Please init reporter first");
        } else {
            a.ia.xu(this.ic);
        }
    }
}
