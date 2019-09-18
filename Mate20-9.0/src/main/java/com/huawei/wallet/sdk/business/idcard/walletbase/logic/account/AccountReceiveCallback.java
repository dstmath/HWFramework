package com.huawei.wallet.sdk.business.idcard.walletbase.logic.account;

public interface AccountReceiveCallback {
    void onAccountHeadChanged();

    void onAccountNameChanged();

    void onAccountRemove();
}
