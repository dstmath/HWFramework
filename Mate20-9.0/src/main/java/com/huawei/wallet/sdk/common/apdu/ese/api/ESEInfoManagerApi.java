package com.huawei.wallet.sdk.common.apdu.ese.api;

import com.huawei.wallet.sdk.common.apdu.base.WalletProcessTraceBase;

public interface ESEInfoManagerApi extends WalletProcessTraceBase {
    void deactivatePPSE();

    boolean esePowerOff();

    boolean esePowerOn(int i);

    String queryCplc();

    String queryCplcByMediaType(int i);

    String queryCplcFromSp();

    String queryCplcListString();

    int queryOpenMobileChannel();

    byte[] querySeid();
}
