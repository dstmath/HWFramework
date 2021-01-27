package com.huawei.coauth.auth;

import android.util.Log;
import com.huawei.coauth.auth.CoAuth;
import com.huawei.coauth.auth.authentity.CoAuthPropertyEntity;
import com.huawei.coauth.auth.authmsg.CoAuthMsgDecodeMgr;
import com.huawei.coauth.msg.ICoMessageProcesser;
import java.util.function.Consumer;

public class GetPropertyCallback implements ICoMessageProcesser {
    private CoAuth.IGetPropCallback getPropCallback;

    GetPropertyCallback(CoAuth.IGetPropCallback getPropCallback2) {
        this.getPropCallback = getPropCallback2;
    }

    @Override // com.huawei.coauth.msg.ICoMessageProcesser
    public int handleMsg(byte[] msg) {
        Log.i(CoAuthUtil.TAG, "getPropCallback receive message from server");
        new CoAuthMsgDecodeMgr().executorPropertyMsg(msg).ifPresent(new Consumer<CoAuthPropertyEntity>() {
            /* class com.huawei.coauth.auth.GetPropertyCallback.AnonymousClass1 */

            public void accept(CoAuthPropertyEntity entity) {
                new CoAuthResponseProcesser().responseGetPropertyCallback(entity, GetPropertyCallback.this.getPropCallback);
            }
        });
        return 0;
    }
}
