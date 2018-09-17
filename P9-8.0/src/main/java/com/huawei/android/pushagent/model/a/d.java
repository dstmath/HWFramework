package com.huawei.android.pushagent.model.a;

import android.content.Context;
import com.huawei.android.pushagent.utils.d.a;

public class d {
    private static final byte[] j = new byte[0];
    private static d k;
    private final a l;

    private d(Context context) {
        this.l = new a(context, "push_disagreement");
    }

    public static d aa(Context context) {
        return ad(context);
    }

    private static d ad(Context context) {
        d dVar;
        synchronized (j) {
            if (k == null) {
                k = new d(context);
            }
            dVar = k;
        }
        return dVar;
    }

    public boolean ab(String str) {
        return this.l.rx(str, false);
    }

    public boolean ac(String str, boolean z) {
        return this.l.rq(str, Boolean.valueOf(z));
    }
}
