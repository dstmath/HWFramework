package com.huawei.wallet.sdk.business.bankcard.api;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.request.WalletActionRequest;
import com.huawei.wallet.sdk.business.bankcard.response.WalletActionResult;

public interface WalletAction {
    WalletActionResult invoke(Context context, WalletActionRequest walletActionRequest);

    boolean isAsync(Context context, WalletActionRequest walletActionRequest);
}
