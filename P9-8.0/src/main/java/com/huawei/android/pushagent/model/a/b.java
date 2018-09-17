package com.huawei.android.pushagent.model.a;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.utils.a.e;
import com.huawei.android.pushagent.utils.d.a;
import com.huawei.android.pushagent.utils.d.c;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class b {
    private static final byte[] d = new byte[0];
    private static b e;
    private final a f;

    private b(Context context) {
        this.f = new a(context, "pclient_info_v2");
    }

    public static b l(Context context) {
        return q(context);
    }

    private static b q(Context context) {
        b bVar;
        synchronized (d) {
            if (e == null) {
                e = new b(context);
            }
            bVar = e;
        }
        return bVar;
    }

    public boolean o() {
        return this.f.rp();
    }

    public String k(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return this.f.rt(str);
    }

    public String n(String str) {
        return e.nu(k(str));
    }

    public boolean s(String str, String str2) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return this.f.rq(str, str2);
    }

    public boolean t(String str, String str2) {
        return s(str, e.nv(str2));
    }

    public void r(String str) {
        this.f.rr(str);
    }

    public Set<String> p() {
        Map all = this.f.getAll();
        if (all == null) {
            return new HashSet();
        }
        Set<String> keySet = all.keySet();
        if (keySet == null) {
            return new HashSet();
        }
        return keySet;
    }

    public String m(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        try {
            Map all = this.f.getAll();
            if (all == null) {
                return null;
            }
            Iterable<Entry> entrySet = all.entrySet();
            if (entrySet == null) {
                return null;
            }
            for (Entry entry : entrySet) {
                String str2 = (String) entry.getKey();
                if (str.equals(e.nu((String) entry.getValue()))) {
                    return str2;
                }
            }
            return null;
        } catch (Throwable e) {
            c.se("PushLog2951", e.toString(), e);
        }
    }

    public void j() {
        this.f.rs();
    }
}
