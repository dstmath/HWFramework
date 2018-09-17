package com.huawei.android.pushagent.b;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.utils.b;
import com.huawei.android.pushagent.utils.d.c;

public class a {
    private Context appCtx;
    private com.huawei.android.pushagent.utils.d.a if = new com.huawei.android.pushagent.utils.d.a(this.appCtx, "token_request_flag");

    public a(Context context) {
        this.appCtx = context.getApplicationContext();
    }

    public void yj() {
        yk("pclient_info_v2");
        yk("push_notify_key");
        yk("pclient_request_info");
    }

    private void yk(String str) {
        for (String yl : new com.huawei.android.pushagent.utils.d.a(this.appCtx, str).getAll().keySet()) {
            yl(yl);
        }
    }

    private void yl(String str) {
        if (TextUtils.isEmpty(str)) {
            c.sj("PushLog2951", "pkgNameWithUid is empty");
        } else {
            this.if.rz(b.ty(str), true);
        }
    }
}
