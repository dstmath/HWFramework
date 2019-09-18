package com.huawei.wallet.sdk.business.bankcard.api;

import com.huawei.wallet.sdk.business.bankcard.response.RouterResponse;

public interface RouterActionCallBack {
    void onFail(RouterResponse routerResponse);

    void onSuccess(RouterResponse routerResponse);
}
