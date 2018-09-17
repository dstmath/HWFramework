package com.huawei.android.pushagent.utils.f;

import android.content.Context;
import java.util.HashMap;

final class b implements Runnable {
    final /* synthetic */ Context gk;
    final /* synthetic */ com.huawei.android.pushagent.model.flowcontrol.a.b gl;
    final /* synthetic */ int gm;
    final /* synthetic */ HashMap gn;

    b(Context context, com.huawei.android.pushagent.model.flowcontrol.a.b bVar, int i, HashMap hashMap) {
        this.gk = context;
        this.gl = bVar;
        this.gm = i;
        this.gn = hashMap;
    }

    public void run() {
        a.td(this.gk, this.gl, this.gm, this.gn);
    }
}
