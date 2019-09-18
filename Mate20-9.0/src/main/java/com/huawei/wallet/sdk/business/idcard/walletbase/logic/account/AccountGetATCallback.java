package com.huawei.wallet.sdk.business.idcard.walletbase.logic.account;

public interface AccountGetATCallback {
    void onGetATError(int i, String str);

    void onGetATSuccess(String str);
}
