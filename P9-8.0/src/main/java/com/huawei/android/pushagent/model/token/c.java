package com.huawei.android.pushagent.model.token;

import com.huawei.android.pushagent.datatype.http.server.TokenDelReq;
import com.huawei.android.pushagent.datatype.http.server.TokenDelRsp;
import com.huawei.android.pushagent.model.a.h;
import com.huawei.android.pushagent.model.d.f;
import com.huawei.android.pushagent.model.e.a;
import com.huawei.android.pushagent.utils.b;
import java.util.List;

final class c implements Runnable {
    final /* synthetic */ a cz;

    c(a aVar) {
        this.cz = aVar;
    }

    public void run() {
        if (b.ul(this.cz.appCtx)) {
            f.ky(this.cz.appCtx);
            int dq = h.dp(this.cz.appCtx).dq();
            List ld = this.cz.la();
            if (ld.size() != 0) {
                TokenDelRsp tokenDelRsp = (TokenDelRsp) new a(this.cz.appCtx, new TokenDelReq(dq, ld, h.dp(this.cz.appCtx).getDeviceIdType())).nd();
                if (tokenDelRsp == null) {
                    com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "fail to apply token");
                } else {
                    this.cz.lb(tokenDelRsp);
                }
            }
        }
    }
}
