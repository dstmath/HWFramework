package com.leisen.wallet.sdk.tsm;

public interface TSMOperatorResponse {
    void onOperFailure(int i, Error error);

    void onOperSuccess(String str);
}
