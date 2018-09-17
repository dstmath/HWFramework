package com.huawei.android.pushagent.a;

import com.huawei.android.pushagent.utils.d.a;
import com.huawei.android.pushagent.utils.d.c;
import java.util.Map;

final class e implements Runnable {
    e() {
    }

    public void run() {
        if (a.appCtx == null || a.ia == null) {
            c.sf("PushLog2951", "Please init reporter first");
            return;
        }
        a aVar = new a(a.appCtx, "push_report_cache");
        Map all = aVar.getAll();
        if (all != null && all.size() > 0) {
            for (String str : all.keySet()) {
                if (str.startsWith("shutdown")) {
                    try {
                        long longValue = Long.valueOf(str.substring("shutdown".length())).longValue();
                        String str2 = (String) all.get(str);
                        if (a.yb(1)) {
                            a.ia.xt(longValue, str2, String.valueOf(1), "");
                        }
                    } catch (NumberFormatException e) {
                        c.sf("PushLog2951", "time format error");
                    }
                }
            }
            aVar.rs();
        }
    }
}
