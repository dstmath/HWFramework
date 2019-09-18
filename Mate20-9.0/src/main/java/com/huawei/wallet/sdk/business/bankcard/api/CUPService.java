package com.huawei.wallet.sdk.business.bankcard.api;

import com.huawei.wallet.sdk.common.apdu.base.WalletProcessTraceBase;

public interface CUPService extends WalletProcessTraceBase {
    int excuteCMD(String str, String str2);

    String getSeidForCMB();

    int init();

    void notifyCardState();
}
