package com.huawei.wallet.sdk.business.idcard.accesscard.logic.callback;

import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;

public interface OpenAccessCallback extends BaseCallback {
    void openAccessCardCallback(int i, ErrorInfo errorInfo);
}
