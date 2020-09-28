package com.huawei.coauth.auth.authmsg;

import com.huawei.coauth.auth.CoAuthContext;
import com.huawei.coauth.auth.authentity.CoAuthHeaderEntity;
import com.huawei.coauth.auth.authentity.CoAuthPairGroupEntity;

public interface IcoAuthMsgEncodeMgr {
    byte[] cancelCoAuth(long j, CoAuthContext coAuthContext, CoAuthPairGroupEntity coAuthPairGroupEntity);

    byte[] coAuthMsg(long j, CoAuthContext coAuthContext, CoAuthPairGroupEntity coAuthPairGroupEntity, String str);

    byte[] createCoAuthPairGroupMsg(long j, String str, String str2, CoAuthHeaderEntity coAuthHeaderEntity);

    byte[] destroyCoAuthPairGroupMsg(long j, byte[] bArr, CoAuthPairGroupEntity coAuthPairGroupEntity);
}
