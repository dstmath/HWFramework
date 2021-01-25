package com.huawei.coauth.auth.authmsg;

import com.huawei.coauth.auth.CoAuthContext;
import com.huawei.coauth.auth.CoAuthDevice;
import com.huawei.coauth.auth.authentity.CoAuthHeaderEntity;
import com.huawei.coauth.auth.authentity.CoAuthPairGroupEntity;
import java.util.List;

public interface IcoAuthMsgEncodeMgr {
    byte[] cancelCoAuth(long j, CoAuthContext coAuthContext, CoAuthPairGroupEntity coAuthPairGroupEntity);

    byte[] coAuthMsg(long j, CoAuthContext coAuthContext, String str);

    byte[] createCoAuthPairGroupMsg(long j, String str, String str2, CoAuthHeaderEntity coAuthHeaderEntity);

    byte[] destroyCoAuthPairGroupMsg(long j, byte[] bArr, CoAuthPairGroupEntity coAuthPairGroupEntity);

    byte[] getPropertyMsg(long j, byte[] bArr, CoAuthContext coAuthContext);

    byte[] initCoAuthIdmGroupMsg(long j, String str, List<CoAuthDevice> list);

    byte[] queryCoAuthMethodMsg(long j, CoAuthContext coAuthContext);

    byte[] setPropertyMsg(long j, byte[] bArr, byte[] bArr2, CoAuthContext coAuthContext);
}
