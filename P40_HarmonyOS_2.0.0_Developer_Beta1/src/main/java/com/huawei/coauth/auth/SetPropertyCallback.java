package com.huawei.coauth.auth;

import android.util.Log;
import com.huawei.coauth.auth.CoAuth;
import com.huawei.coauth.auth.authentity.CoAuthPropertyEntity;
import com.huawei.coauth.auth.authmsg.CoAuthMsgDecodeMgr;
import com.huawei.coauth.msg.ICoMessageProcesser;
import java.util.function.Consumer;

public class SetPropertyCallback implements ICoMessageProcesser {
    private CoAuth.ISetPropCallback setPropCallback;

    SetPropertyCallback(CoAuth.ISetPropCallback setPropCallback2) {
        this.setPropCallback = setPropCallback2;
    }

    @Override // com.huawei.coauth.msg.ICoMessageProcesser
    public int handleMsg(byte[] msg) {
        Log.i(CoAuthUtil.TAG, "setPropCallback receive message from server");
        new CoAuthMsgDecodeMgr().executorPropertyMsg(msg).ifPresent(new Consumer<CoAuthPropertyEntity>() {
            /* class com.huawei.coauth.auth.SetPropertyCallback.AnonymousClass1 */

            public void accept(CoAuthPropertyEntity entity) {
                new CoAuthResponseProcesser().responseSetPropertyCallback(entity, SetPropertyCallback.this.setPropCallback);
            }
        });
        return 0;
    }
}
