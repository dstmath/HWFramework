package com.huawei.wallet.sdk.business.idcard.walletbase.logic.account;

import com.huawei.wallet.sdk.business.idcard.walletbase.model.account.AccountInfo;

public interface AccountStateListener {
    void onAccountLogin(AccountInfo accountInfo);
}
