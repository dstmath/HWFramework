package com.huawei.wallet.sdk.business.bankcard.api;

public interface CUPCardOperatorApi {
    void registerOperationListener(String str, String str2, CUPOperationListener cUPOperationListener);

    void unregisterOperationListener(String str, String str2, CUPOperationListener cUPOperationListener);
}
