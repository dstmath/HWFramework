package com.huawei.coauth.auth.authmsg;

import com.huawei.coauth.auth.authentity.CoAuthResponseEntity;
import java.util.Optional;

public interface IcoAuthMsgDecodeMgr {
    Optional<CoAuthResponseEntity> responseMsg(byte[] bArr);
}
