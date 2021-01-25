package com.huawei.coauth.auth;

import android.util.Log;
import com.huawei.coauth.auth.CoAuth;
import com.huawei.coauth.auth.authentity.CoAuthPairGroupEntity;
import com.huawei.coauth.auth.authentity.CoAuthResponseEntity;
import com.huawei.coauth.auth.authmsg.CoAuthMsgDecodeMgr;
import com.huawei.coauth.msg.ICoMessageProcesser;
import java.util.Map;
import java.util.Optional;

class DestroyCallback implements ICoMessageProcesser {
    Map<String, CoAuthPairGroupEntity> coAuthPairGroupMap;
    CoAuth.IDestroyCallback destroyCallback;

    DestroyCallback(CoAuth.IDestroyCallback destroyCallback2, Map<String, CoAuthPairGroupEntity> coAuthPairGroupMap2) {
        this.destroyCallback = destroyCallback2;
        this.coAuthPairGroupMap = coAuthPairGroupMap2;
    }

    @Override // com.huawei.coauth.msg.ICoMessageProcesser
    public int handleMsg(byte[] msg) {
        Log.i(CoAuthUtil.TAG, "destroyCallback receive message from server");
        Optional<CoAuthResponseEntity> coAuthResponseEntityOptional = new CoAuthMsgDecodeMgr().responseMsg(msg);
        if (!coAuthResponseEntityOptional.isPresent()) {
            return 0;
        }
        new CoAuthResponseProcesser().responseDestroyCallback(coAuthResponseEntityOptional.get(), this.destroyCallback, this.coAuthPairGroupMap);
        return 0;
    }
}
