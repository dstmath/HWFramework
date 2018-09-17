package com.huawei.android.pushagent.model.d;

import com.huawei.android.pushagent.datatype.http.server.TrsReq;
import com.huawei.android.pushagent.datatype.http.server.TrsRsp;
import com.huawei.android.pushagent.model.e.c;

final class g implements Runnable {
    final /* synthetic */ c cx;

    g(c cVar) {
        this.cx = cVar;
    }

    public void run() {
        try {
            TrsRsp trsRsp = (TrsRsp) new c(this.cx.appCtx, new TrsReq(this.cx.appCtx, this.cx.cp.getBelongId(), this.cx.cp.getConnId())).nd();
            if (trsRsp.isValid()) {
                this.cx.kh(trsRsp);
                return;
            }
            com.huawei.android.pushagent.utils.d.c.sh("PushLog2951", "query trs error:" + this.cx.cp.getResult());
            if (trsRsp.isNotAllowedPush()) {
                this.cx.cp.bm(trsRsp.getResult());
                this.cx.cp.bn(trsRsp.getNextConnectTrsInterval());
            }
        } catch (Throwable e) {
            com.huawei.android.pushagent.utils.d.c.se("PushLog2951", e.toString(), e);
        }
    }
}
