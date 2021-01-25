package com.huawei.coauth.auth;

import android.util.Log;
import com.huawei.coauth.auth.CoAuth;
import com.huawei.coauth.auth.authentity.CoAuthQueryMethodEntity;
import com.huawei.coauth.auth.authmsg.CoAuthMsgDecodeMgr;
import com.huawei.coauth.msg.ICoMessageProcesser;
import java.util.function.Consumer;

/* access modifiers changed from: package-private */
public class QueryMethodCallback implements ICoMessageProcesser {
    private CoAuth.IQueryAuthCallback queryAuthCallback;

    QueryMethodCallback(CoAuth.IQueryAuthCallback queryAuthCallback2) {
        this.queryAuthCallback = queryAuthCallback2;
    }

    @Override // com.huawei.coauth.msg.ICoMessageProcesser
    public int handleMsg(byte[] msg) {
        Log.i(CoAuthUtil.TAG, "queryMethodCallback receive message from server");
        new CoAuthMsgDecodeMgr().queryMethodMsg(msg).ifPresent(new Consumer<CoAuthQueryMethodEntity>() {
            /* class com.huawei.coauth.auth.QueryMethodCallback.AnonymousClass1 */

            public void accept(CoAuthQueryMethodEntity coAuthQueryMethodEntity) {
                new CoAuthResponseProcesser().responseQueryCallback(coAuthQueryMethodEntity, QueryMethodCallback.this.queryAuthCallback);
            }
        });
        return 0;
    }
}
