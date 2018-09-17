package com.huawei.android.pushselfshow.richpush.favorites;

import com.huawei.android.pushagent.a.a.c;

class d implements Runnable {
    final /* synthetic */ c a;

    d(c cVar) {
        this.a = cVar;
    }

    public void run() {
        c.a("PushSelfShowLog", "deleteTipDialog mThread run");
        Object obj = null;
        for (e eVar : this.a.a.h.a()) {
            if (eVar.a()) {
                com.huawei.android.pushselfshow.utils.a.d.a(this.a.a.b, eVar.c());
                obj = 1;
            }
        }
        if (obj != null) {
            if (!this.a.a.k) {
                this.a.a.h.b();
            }
            this.a.a.a.sendEmptyMessage(1001);
        }
    }
}
