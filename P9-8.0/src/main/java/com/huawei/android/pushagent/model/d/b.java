package com.huawei.android.pushagent.model.d;

import android.text.TextUtils;
import com.huawei.android.pushagent.a.a;
import com.huawei.android.pushagent.utils.d.c;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class b {
    private static b cl = new b();
    private Map<String, Integer> ck = new HashMap();

    private b() {
    }

    public static b jx() {
        return cl;
    }

    public void jw(String str) {
        if (!TextUtils.isEmpty(str)) {
            int i = 0;
            if (this.ck.containsKey(str)) {
                i = ((Integer) this.ck.get(str)).intValue();
            }
            this.ck.put(str, Integer.valueOf(i + 1));
            c.sg("PushLog2951", "reportEvent cacheTokenApplyTimesList:" + this.ck);
        }
    }

    public void jy() {
        int i = 20;
        int size = this.ck.size();
        if (size <= 20) {
            i = size;
        }
        Iterator it = this.ck.entrySet().iterator();
        while (it.hasNext() && i > 0) {
            int i2 = i - 1;
            Entry entry = (Entry) it.next();
            a.xx(61, a.xw((String) entry.getKey(), String.valueOf(entry.getValue())));
            it.remove();
            i = i2;
        }
    }
}
