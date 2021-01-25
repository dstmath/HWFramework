package com.huawei.coauth.auth;

import android.util.Log;
import com.huawei.coauth.auth.CoAuth;
import com.huawei.coauth.auth.authentity.CoAuthResponseEntity;
import com.huawei.coauth.auth.authmsg.CoAuthMsgDecodeMgr;
import com.huawei.coauth.msg.ICoMessageProcesser;
import java.util.Optional;

class CancelCoAuthCallback implements ICoMessageProcesser {
    CoAuth.ICancelCoAuthCallback cancelCoAuthCallback;
    CoAuthContext coAuthContext;

    CancelCoAuthCallback(CoAuthContext coAuthContext2, CoAuth.ICancelCoAuthCallback cancelCoAuthCallback2) {
        this.cancelCoAuthCallback = cancelCoAuthCallback2;
        this.coAuthContext = coAuthContext2;
    }

    @Override // com.huawei.coauth.msg.ICoMessageProcesser
    public int handleMsg(byte[] msg) {
        Log.i(CoAuthUtil.TAG, "cancelCoAuthCallback receive message from server");
        Optional<CoAuthResponseEntity> coAuthResponseEntityOptional = new CoAuthMsgDecodeMgr().responseMsg(msg);
        if (!coAuthResponseEntityOptional.isPresent()) {
            return 0;
        }
        new CoAuthResponseProcesser().responseCancelCoAuthCallback(coAuthResponseEntityOptional.get(), this.coAuthContext, this.cancelCoAuthCallback);
        return 0;
    }
}
