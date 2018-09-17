package com.huawei.android.pushagent.model.a;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.utils.d.a;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class j {
    private static final byte[] y = new byte[0];
    private static j z;
    private final a aa;

    private j(Context context) {
        this.aa = new a(context, "pclient_request_info");
    }

    public static j ev(Context context) {
        return fa(context);
    }

    private static j fa(Context context) {
        j jVar;
        synchronized (y) {
            if (z == null) {
                z = new j(context);
            }
            jVar = z;
        }
        return jVar;
    }

    public boolean ez() {
        return this.aa.rp();
    }

    public boolean ey(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return this.aa.rq(str, "true");
    }

    public boolean remove(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        return this.aa.rr(str);
    }

    public String ew(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return this.aa.rt(str);
    }

    public Set<String> ex() {
        Map all = this.aa.getAll();
        if (all == null) {
            return new HashSet();
        }
        Set<String> keySet = all.keySet();
        if (keySet == null) {
            return new HashSet();
        }
        return keySet;
    }
}
