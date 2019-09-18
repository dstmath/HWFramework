package com.huawei.wallet.sdk.business.idcard.idcard;

import com.huawei.wallet.sdk.business.idcard.walletbase.model.IWalletCardBaseInfo;
import java.util.List;

public interface OnDataReadyListener {
    void refreshData(List<IWalletCardBaseInfo> list);
}
