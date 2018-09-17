package com.huawei.android.pushagent.model.token;

import com.huawei.android.pushagent.datatype.http.server.TokenApplyReq;
import com.huawei.android.pushagent.datatype.http.server.TokenApplyRsp;
import com.huawei.android.pushagent.model.a.g;
import com.huawei.android.pushagent.model.a.h;
import com.huawei.android.pushagent.utils.d.c;
import java.util.List;

final class b implements Runnable {
    final /* synthetic */ TokenApply cy;

    b(TokenApply tokenApply) {
        this.cy = tokenApply;
    }

    public void run() {
        if (com.huawei.android.pushagent.utils.b.ul(this.cy.appCtx)) {
            String connId = g.aq(this.cy.appCtx).getConnId();
            int dq = h.dp(this.cy.appCtx).dq();
            List -wrap0 = this.cy.getTokenReqs();
            int deviceIdType = h.dp(this.cy.appCtx).getDeviceIdType();
            if (-wrap0.size() != 0) {
                TokenApplyRsp tokenApplyRsp = (TokenApplyRsp) new com.huawei.android.pushagent.model.e.b(this.cy.appCtx, new TokenApplyReq(connId, dq, -wrap0, deviceIdType)).nd();
                if (tokenApplyRsp == null) {
                    c.sf("PushLog2951", "fail to apply token, http level failed");
                } else {
                    this.cy.responseToken(tokenApplyRsp);
                }
            }
        }
    }
}
