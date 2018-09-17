package com.huawei.android.pushagent.model.a;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.utils.a.e;
import com.huawei.android.pushagent.utils.d.c;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class a {
    private static final byte[] a = new byte[0];
    private static a b;
    private final com.huawei.android.pushagent.utils.d.a c;

    private a(Context context) {
        this.c = new com.huawei.android.pushagent.utils.d.a(context, "pclient_unRegist_info_v2");
    }

    public static a c(Context context) {
        return f(context);
    }

    private static a f(Context context) {
        a aVar;
        synchronized (a) {
            if (b == null) {
                b = new a(context);
            }
            aVar = b;
        }
        return aVar;
    }

    public boolean e() {
        return this.c.rp();
    }

    public String d(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        try {
            Map all = this.c.getAll();
            if (all == null) {
                return "";
            }
            Iterable<Entry> entrySet = all.entrySet();
            if (entrySet == null) {
                return "";
            }
            for (Entry entry : entrySet) {
                String str2 = (String) entry.getKey();
                String str3 = (String) entry.getValue();
                if (str.equals(e.nu(str2))) {
                    return str3;
                }
            }
            return "";
        } catch (Throwable e) {
            c.se("PushLog2951", e.toString(), e);
        }
    }

    public String b(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        String str2;
        for (Entry entry : this.c.getAll().entrySet()) {
            if (str.equals((String) entry.getValue())) {
                str2 = (String) entry.getKey();
                break;
            }
        }
        str2 = null;
        return str2;
    }

    public Set<Entry<String, String>> a() {
        Map all = this.c.getAll();
        if (all == null) {
            return new HashSet();
        }
        Set<Entry<String, String>> entrySet = all.entrySet();
        if (entrySet == null) {
            return new HashSet();
        }
        return entrySet;
    }

    public boolean h(String str, String str2) {
        if (TextUtils.isEmpty(str2)) {
            return false;
        }
        return this.c.rq(str, str2);
    }

    public boolean i(String str, String str2) {
        return h(e.nv(str), str2);
    }

    public void g(String str) {
        if (!TextUtils.isEmpty(str)) {
            try {
                Map all = this.c.getAll();
                if (all != null) {
                    Iterable<Entry> entrySet = all.entrySet();
                    if (entrySet != null) {
                        for (Entry key : entrySet) {
                            String str2 = (String) key.getKey();
                            if (str.equals(e.nu(str2))) {
                                this.c.rr(str2);
                                return;
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                c.se("PushLog2951", e.toString(), e);
            }
        }
    }
}
