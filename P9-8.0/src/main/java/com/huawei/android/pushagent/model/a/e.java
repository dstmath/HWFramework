package com.huawei.android.pushagent.model.a;

import android.content.Context;
import com.huawei.android.pushagent.utils.d.a;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class e {
    private static final byte[] m = new byte[0];
    private static e n;
    private final a o;

    private e(Context context) {
        this.o = new a(context, "push_notify_switch");
    }

    public static e ae(Context context) {
        return aj(context);
    }

    private static e aj(Context context) {
        e eVar;
        synchronized (m) {
            if (n == null) {
                n = new e(context);
            }
            eVar = n;
        }
        return eVar;
    }

    public boolean af(String str) {
        return this.o.rx(str, false);
    }

    public boolean ah(String str, boolean z) {
        return this.o.rq(str, Boolean.valueOf(z));
    }

    public boolean ag(String str) {
        return this.o.rr(str);
    }

    public Set<Entry<String, Boolean>> ai() {
        Map all = this.o.getAll();
        if (all == null) {
            return new HashSet();
        }
        Set<Entry<String, Boolean>> entrySet = all.entrySet();
        if (entrySet == null) {
            return new HashSet();
        }
        return entrySet;
    }
}
