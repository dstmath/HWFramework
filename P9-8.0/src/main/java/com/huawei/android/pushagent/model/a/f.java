package com.huawei.android.pushagent.model.a;

import android.content.Context;
import com.huawei.android.pushagent.utils.d.a;
import com.huawei.android.pushagent.utils.d.c;
import java.util.Map;
import java.util.Map.Entry;

public class f {
    private static final byte[] p = new byte[0];
    private static f q;
    private final a r;

    private f(Context context) {
        this.r = new a(context, "token_request_flag");
    }

    public static f ak(Context context) {
        return ap(context);
    }

    private static f ap(Context context) {
        f fVar;
        synchronized (p) {
            if (q == null) {
                q = new f(context);
            }
            fVar = q;
        }
        return fVar;
    }

    public boolean ao() {
        try {
            Map all = this.r.getAll();
            if (all == null) {
                return false;
            }
            Iterable<Entry> entrySet = all.entrySet();
            if (entrySet == null) {
                return false;
            }
            for (Entry value : entrySet) {
                if (((Boolean) value.getValue()).booleanValue()) {
                    return true;
                }
            }
            return false;
        } catch (Throwable e) {
            c.se("PushLog2951", e.toString(), e);
        }
    }

    public boolean an(String str) {
        return this.r.rx(str, false);
    }

    public void al(String str, boolean z) {
        this.r.rq(str, Boolean.valueOf(z));
    }

    public void am(String str) {
        this.r.rr(str);
    }
}
