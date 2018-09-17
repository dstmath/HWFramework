package com.leisen.wallet.sdk.oma;

public interface SmartCardCallback {
    void onOperFailure(int i, Error error);

    void onOperSuccess(int i, String str);
}
