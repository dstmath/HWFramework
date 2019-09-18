package com.huawei.wallet.sdk.common.apdu.tsm.http;

public interface ResponseHandlerInterface {
    void sendFailureMessage(int i, String str);

    void sendSuccessMessage(int i, String str);
}
