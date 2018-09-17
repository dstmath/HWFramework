package com.huawei.android.pushagent.utils.tools;

import android.content.Context;
import com.huawei.android.pushagent.utils.b;
import com.huawei.android.pushagent.utils.d.a;

class c extends Thread {
    private Context appCtx;

    public c(Context context) {
        this.appCtx = context.getApplicationContext();
    }

    public void run() {
        new a(this.appCtx, "push_report_cache").rq("shutdown" + System.currentTimeMillis(), String.valueOf(b.tm(this.appCtx)));
    }
}
