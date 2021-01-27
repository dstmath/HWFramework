package com.huawei.coauth.auth;

import android.util.Log;
import com.huawei.coauth.auth.CoAuth;
import com.huawei.coauth.auth.authentity.CoAuthIdmGroupEntity;
import com.huawei.coauth.auth.authmsg.CoAuthMsgDecodeMgr;
import com.huawei.coauth.msg.ICoMessageProcesser;
import java.util.function.Consumer;

public class InitCoAuthIdmGroupCallback implements ICoMessageProcesser {
    private CoAuth.IInitCallback initCallback;

    InitCoAuthIdmGroupCallback(CoAuth.IInitCallback initCallback2) {
        this.initCallback = initCallback2;
    }

    @Override // com.huawei.coauth.msg.ICoMessageProcesser
    public int handleMsg(byte[] msg) {
        Log.i(CoAuthUtil.TAG, "InitCoAuthIdmGroupCallback receive message from server");
        new CoAuthMsgDecodeMgr().responseIdmGroupMsg(msg).ifPresent(new Consumer<CoAuthIdmGroupEntity>() {
            /* class com.huawei.coauth.auth.InitCoAuthIdmGroupCallback.AnonymousClass1 */

            public void accept(CoAuthIdmGroupEntity entity) {
                new CoAuthResponseProcesser().responseInitIdmGroupCallback(entity, InitCoAuthIdmGroupCallback.this.initCallback);
            }
        });
        return 0;
    }
}
