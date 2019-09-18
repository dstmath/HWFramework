package com.huawei.wallet.sdk.business.idcard.walletbase.logic.oversea;

public interface ICheckSerCountryCallBack {
    public static final String ERROR_CODE_EMPTY_SERVICE_COUNTRY_CODE = "EER_10001";

    void checkError(String str);

    void isChineses(boolean z);
}
