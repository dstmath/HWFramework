package com.huawei.coauth.auth;

import android.util.Log;
import com.huawei.coauth.auth.CoAuth;
import com.huawei.coauth.auth.authentity.CoAuthResponseEntity;
import com.huawei.coauth.auth.authmsg.CoAuthMsgDecodeMgr;
import com.huawei.coauth.msg.ICoMessageProcesser;
import java.util.Optional;

class CoAuthCallback implements ICoMessageProcesser {
    CoAuth.ICoAuthCallback coAuthCallback;
    CoAuthContext coAuthContext;

    CoAuthCallback(CoAuthContext coAuthContext2, CoAuth.ICoAuthCallback coAuthCallback2) {
        this.coAuthCallback = coAuthCallback2;
        this.coAuthContext = coAuthContext2;
    }

    @Override // com.huawei.coauth.msg.ICoMessageProcesser
    public int handleMsg(byte[] msg) {
        Log.i(CoAuthUtil.TAG, "coAuthCallback receive message from server");
        Optional<CoAuthResponseEntity> coAuthResponseEntityOptional = new CoAuthMsgDecodeMgr().responseMsg(msg);
        if (!coAuthResponseEntityOptional.isPresent()) {
            return 0;
        }
        new CoAuthResponseProcesser().responseCoAuthCallback(coAuthResponseEntityOptional.get(), this.coAuthContext, this.coAuthCallback);
        return 0;
    }
}
