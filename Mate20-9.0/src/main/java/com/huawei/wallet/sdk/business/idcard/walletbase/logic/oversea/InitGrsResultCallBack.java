package com.huawei.wallet.sdk.business.idcard.walletbase.logic.oversea;

public interface InitGrsResultCallBack {
    public static final String RESULT_CODE_FAILED_GET_SERVICECOUNTRY_CODE_FAILED = "02";
    public static final String RESULT_CODE_SUCCESS = "00";

    void finish(String str);
}
