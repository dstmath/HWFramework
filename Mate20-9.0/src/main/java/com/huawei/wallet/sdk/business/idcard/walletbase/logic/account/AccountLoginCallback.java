package com.huawei.wallet.sdk.business.idcard.walletbase.logic.account;

import com.huawei.wallet.sdk.business.idcard.walletbase.model.account.AccountInfo;

public interface AccountLoginCallback {
    void onLoginError(int i);

    void onLoginSuccess(AccountInfo accountInfo);
}
