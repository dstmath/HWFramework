package com.huawei.coauth.auth.authmsg;

import com.huawei.coauth.auth.authentity.CoAuthIdmGroupEntity;
import com.huawei.coauth.auth.authentity.CoAuthPropertyEntity;
import com.huawei.coauth.auth.authentity.CoAuthQueryMethodEntity;
import com.huawei.coauth.auth.authentity.CoAuthResponseEntity;
import java.util.Optional;

public interface IcoAuthMsgDecodeMgr {
    Optional<CoAuthPropertyEntity> executorPropertyMsg(byte[] bArr);

    Optional<CoAuthQueryMethodEntity> queryMethodMsg(byte[] bArr);

    Optional<CoAuthIdmGroupEntity> responseIdmGroupMsg(byte[] bArr);

    Optional<CoAuthResponseEntity> responseMsg(byte[] bArr);
}
