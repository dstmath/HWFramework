package com.huawei.coauthservice.identitymgr.callback;

import com.huawei.coauthservice.identitymgr.IdmGroupInfo;

public interface ICreateGroupCallback {
    void onFailed(int i);

    void onSuccess(IdmGroupInfo idmGroupInfo);
}
