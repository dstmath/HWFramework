package com.huawei.android.pushagent.b;

import android.content.Context;
import com.huawei.android.pushagent.model.a.i;
import com.huawei.android.pushagent.utils.d.c;

public class b {
    public static synchronized void ym(Context context) {
        synchronized (b.class) {
            int en = i.ea(context).en();
            if (4 == en) {
                return;
            }
            if (en < 3) {
                new a(context).yj();
            }
            c.sh("PushLog2951", "update xml data, old version is " + en + ",new version is " + 4);
            if (en < 4) {
                new c(context).yj();
            }
            i.ea(context).eo(4);
        }
    }
}
