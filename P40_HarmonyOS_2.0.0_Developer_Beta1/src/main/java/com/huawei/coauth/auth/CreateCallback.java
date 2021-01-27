package com.huawei.coauth.auth;

import android.util.Log;
import com.huawei.coauth.auth.CoAuth;
import com.huawei.coauth.auth.authentity.CoAuthHeaderEntity;
import com.huawei.coauth.auth.authentity.CoAuthPairGroupEntity;
import com.huawei.coauth.auth.authentity.CoAuthResponseEntity;
import com.huawei.coauth.auth.authmsg.CoAuthMsgDecodeMgr;
import com.huawei.coauth.msg.ICoMessageProcesser;
import java.util.Map;
import java.util.Optional;

class CreateCallback implements ICoMessageProcesser {
    CoAuthHeaderEntity coAuthHeaderEntity;
    Map<String, CoAuthPairGroupEntity> coAuthPairGroupMap;
    CoAuth.ICreateCallback createCallback;

    CreateCallback(CoAuthHeaderEntity coAuthHeaderEntity2, CoAuth.ICreateCallback createCallback2, Map<String, CoAuthPairGroupEntity> coAuthPairGroupMap2) {
        this.createCallback = createCallback2;
        this.coAuthHeaderEntity = coAuthHeaderEntity2;
        this.coAuthPairGroupMap = coAuthPairGroupMap2;
    }

    @Override // com.huawei.coauth.msg.ICoMessageProcesser
    public int handleMsg(byte[] msg) {
        Log.i(CoAuthUtil.TAG, "createCallback receive message from server");
        Optional<CoAuthResponseEntity> coAuthResponseEntityOptional = new CoAuthMsgDecodeMgr().responseMsg(msg);
        if (!coAuthResponseEntityOptional.isPresent()) {
            return 0;
        }
        new CoAuthResponseProcesser().responseCreateCallback(coAuthResponseEntityOptional.get(), this.coAuthHeaderEntity, this.createCallback, this.coAuthPairGroupMap);
        return 0;
    }
}
