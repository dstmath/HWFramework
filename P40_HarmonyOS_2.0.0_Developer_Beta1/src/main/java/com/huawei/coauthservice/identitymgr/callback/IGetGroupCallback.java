package com.huawei.coauthservice.identitymgr.callback;

import com.huawei.coauthservice.identitymgr.IdmGroupInfo;
import java.util.List;

public interface IGetGroupCallback {
    void onFailed(int i);

    void onSuccess(List<IdmGroupInfo> list);
}
